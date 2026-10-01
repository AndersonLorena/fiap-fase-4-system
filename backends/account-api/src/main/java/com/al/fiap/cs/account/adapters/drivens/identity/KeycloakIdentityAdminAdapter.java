package com.al.fiap.cs.account.adapters.drivens.identity;

import com.al.fiap.cs.account.adapters.config.AccountProperties;
import com.al.fiap.cs.account.core.domain.exceptions.AccountAlreadyExistsException;
import com.al.fiap.cs.account.core.domain.exceptions.IdentityProviderException;
import com.al.fiap.cs.account.ports.identity.IdentityAdminPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class KeycloakIdentityAdminAdapter implements IdentityAdminPort {

	private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityAdminAdapter.class);
	private static final String ACCOUNT_ID_ATTRIBUTE = "accountId";

	private final RestClient restClient;
	private final AccountProperties properties;
	private final AtomicReference<CachedToken> cachedToken = new AtomicReference<>();
	private final AtomicBoolean accountIdAttributeReady = new AtomicBoolean(false);

	public KeycloakIdentityAdminAdapter(RestClient.Builder restClientBuilder, AccountProperties properties) {
		this.restClient = restClientBuilder.build();
		this.properties = properties;
	}

	@Override
	@Retryable(
		retryFor = { IdentityProviderException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 200, multiplier = 2)
	)
	public String createUser(CreateIdentityUserCommand command) {
		ensureAccountIdAttributeSupport();
		String accessToken = serviceAccountToken();
		String location;
		try {
			var response = restClient.post()
				.uri(properties.getKeycloak().adminUsersUrl())
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + accessToken)
				.body(Map.of(
					"username", command.email(),
					"email", command.email(),
					"enabled", true,
					"emailVerified", true,
					"firstName", firstName(command.fullName()),
					"lastName", lastName(command.fullName()),
					"attributes", Map.of(ACCOUNT_ID_ATTRIBUTE, List.of(command.accountId())),
					"credentials", List.of(Map.of(
						"type", "password",
						"value", command.password(),
						"temporary", false
					))
				))
				.retrieve()
				.toBodilessEntity();
			location = response.getHeaders().getFirst("Location");
		}
		catch (HttpClientErrorException.Conflict ex) {
			throw new AccountAlreadyExistsException("Account email already exists");
		}
		catch (HttpClientErrorException.BadRequest ex) {
			throw new IdentityProviderException("Identity provider rejected user creation");
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Identity provider unavailable");
		}

		String userId = extractUserId(location, command.email(), accessToken);
		ensureAccountIdPresent(userId, command.accountId(), accessToken);
		assignRealmRole(userId, command.type().realmRoleName(), accessToken);
		return userId;
	}

	@Override
	@Retryable(
		retryFor = { IdentityProviderException.class },
		maxAttempts = 3,
		backoff = @Backoff(delay = 200, multiplier = 2)
	)
	public void resetPassword(String keycloakUserId, String newPassword) {
		String accessToken = serviceAccountToken();
		try {
			restClient.put()
				.uri(properties.getKeycloak().adminUsersUrl() + "/" + keycloakUserId + "/reset-password")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + accessToken)
				.body(Map.of(
					"type", "password",
					"value", newPassword,
					"temporary", false
				))
				.retrieve()
				.toBodilessEntity();
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Failed to reset password in identity provider");
		}
	}

	@Override
	public void logoutSessions(String keycloakUserId) {
		String accessToken = serviceAccountToken();
		try {
			restClient.post()
				.uri(properties.getKeycloak().adminUsersUrl() + "/" + keycloakUserId + "/logout")
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.toBodilessEntity();
		}
		catch (Exception ignored) {
			// Best-effort session invalidation
		}
	}

	@Override
	public void deleteUser(String keycloakUserId) {
		String accessToken = serviceAccountToken();
		try {
			restClient.delete()
				.uri(properties.getKeycloak().adminUsersUrl() + "/" + keycloakUserId)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.toBodilessEntity();
		}
		catch (Exception ignored) {
			// Compensation best-effort
		}
	}

	private void ensureAccountIdAttributeSupport() {
		if (accountIdAttributeReady.get()) {
			return;
		}
		synchronized (accountIdAttributeReady) {
			if (accountIdAttributeReady.get()) {
				return;
			}
			String accessToken = serviceAccountToken();
			Map<String, Object> profile = fetchUserProfile(accessToken);
			boolean changed = false;

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> attributes = (List<Map<String, Object>>) profile.get("attributes");
			if (attributes == null) {
				attributes = new ArrayList<>();
				profile.put("attributes", attributes);
				changed = true;
			}
			if (!hasAccountIdAttribute(attributes)) {
				attributes.add(accountIdAttributeDefinition());
				changed = true;
			}
			if (!"ADMIN_EDIT".equals(String.valueOf(profile.get("unmanagedAttributePolicy")))) {
				profile.put("unmanagedAttributePolicy", "ADMIN_EDIT");
				changed = true;
			}
			if (changed) {
				putUserProfile(profile, accessToken);
				log.info("Keycloak user profile updated to support accountId attribute");
			}
			accountIdAttributeReady.set(true);
		}
	}

	@SuppressWarnings("unchecked")
	private void ensureAccountIdPresent(String keycloakUserId, String accountId, String accessToken) {
		Map<String, Object> user = fetchUser(keycloakUserId, accessToken);
		Map<String, Object> attributes = (Map<String, Object>) user.get("attributes");
		if (attributes == null) {
			attributes = new HashMap<>();
			user.put("attributes", attributes);
		}
		Object current = attributes.get(ACCOUNT_ID_ATTRIBUTE);
		if (containsAccountId(current, accountId)) {
			return;
		}
		attributes.put(ACCOUNT_ID_ATTRIBUTE, List.of(accountId));
		try {
			restClient.put()
				.uri(properties.getKeycloak().adminUsersUrl() + "/" + keycloakUserId)
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + accessToken)
				.body(user)
				.retrieve()
				.toBodilessEntity();
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Failed to persist accountId attribute");
		}
		Map<String, Object> verified = fetchUser(keycloakUserId, accessToken);
		Map<String, Object> verifiedAttributes = (Map<String, Object>) verified.get("attributes");
		Object verifiedValue = verifiedAttributes == null ? null : verifiedAttributes.get(ACCOUNT_ID_ATTRIBUTE);
		if (!containsAccountId(verifiedValue, accountId)) {
			throw new IdentityProviderException("accountId attribute was not persisted by identity provider");
		}
	}

	private Map<String, Object> fetchUser(String keycloakUserId, String accessToken) {
		try {
			Map<String, Object> user = restClient.get()
				.uri(properties.getKeycloak().adminUsersUrl() + "/" + keycloakUserId)
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
			if (user == null) {
				throw new IdentityProviderException("Identity user not found");
			}
			return new LinkedHashMap<>(user);
		}
		catch (IdentityProviderException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Unable to load identity user");
		}
	}

	private Map<String, Object> fetchUserProfile(String accessToken) {
		try {
			Map<String, Object> profile = restClient.get()
				.uri(properties.getKeycloak().adminUserProfileUrl())
				.header("Authorization", "Bearer " + accessToken)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});
			if (profile == null) {
				throw new IdentityProviderException("Unable to load user profile");
			}
			return new LinkedHashMap<>(profile);
		}
		catch (IdentityProviderException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Unable to load user profile");
		}
	}

	private void putUserProfile(Map<String, Object> profile, String accessToken) {
		try {
			restClient.put()
				.uri(properties.getKeycloak().adminUserProfileUrl())
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + accessToken)
				.body(profile)
				.retrieve()
				.toBodilessEntity();
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Unable to update user profile for accountId support");
		}
	}

	private static boolean hasAccountIdAttribute(List<Map<String, Object>> attributes) {
		for (Map<String, Object> attribute : attributes) {
			if (ACCOUNT_ID_ATTRIBUTE.equals(String.valueOf(attribute.get("name")))) {
				return true;
			}
		}
		return false;
	}

	private static Map<String, Object> accountIdAttributeDefinition() {
		Map<String, Object> attribute = new LinkedHashMap<>();
		attribute.put("name", ACCOUNT_ID_ATTRIBUTE);
		attribute.put("displayName", "Account ID");
		attribute.put("multivalued", false);
		attribute.put("permissions", Map.of(
			"view", List.of("admin"),
			"edit", List.of("admin")
		));
		return attribute;
	}

	private static boolean containsAccountId(Object attributeValue, String expected) {
		if (attributeValue == null) {
			return false;
		}
		if (attributeValue instanceof List<?> values) {
			return values.stream().anyMatch(value -> expected.equals(String.valueOf(value)));
		}
		return expected.equals(String.valueOf(attributeValue));
	}

	@SuppressWarnings("unchecked")
	private String serviceAccountToken() {
		CachedToken current = cachedToken.get();
		if (current != null && current.expiresAt().isAfter(Instant.now().plusSeconds(30))) {
			return current.token();
		}
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("grant_type", "client_credentials");
		form.add("client_id", properties.getKeycloak().getClientId());
		form.add("client_secret", properties.getKeycloak().getClientSecret());
		try {
			Map<String, Object> body = restClient.post()
				.uri(properties.getKeycloak().tokenUrl())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(form)
				.retrieve()
				.body(Map.class);
			if (body == null || body.get("access_token") == null) {
				throw new IdentityProviderException("Unable to obtain service account token");
			}
			long expiresIn = ((Number) body.getOrDefault("expires_in", 60)).longValue();
			String token = String.valueOf(body.get("access_token"));
			cachedToken.set(new CachedToken(token, Instant.now().plusSeconds(expiresIn)));
			return token;
		}
		catch (IdentityProviderException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new IdentityProviderException("Unable to obtain service account token");
		}
	}

	private void assignRealmRole(String userId, String roleName, String accessToken) {
		List<Map<String, Object>> roles = restClient.get()
			.uri(properties.getKeycloak().getUrl() + "/admin/realms/" + properties.getKeycloak().getRealm() + "/roles")
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
		if (roles == null) {
			throw new IdentityProviderException("Unable to load realm roles");
		}
		Map<String, Object> role = roles.stream()
			.filter(item -> roleName.equals(String.valueOf(item.get("name"))))
			.findFirst()
			.orElseThrow(() -> new IdentityProviderException("Realm role not found: " + roleName));
		restClient.post()
			.uri(properties.getKeycloak().adminUsersUrl() + "/" + userId + "/role-mappings/realm")
			.contentType(MediaType.APPLICATION_JSON)
			.header("Authorization", "Bearer " + accessToken)
			.body(List.of(role))
			.retrieve()
			.toBodilessEntity();
	}

	private String extractUserId(String location, String email, String accessToken) {
		if (location != null && location.contains("/")) {
			return location.substring(location.lastIndexOf('/') + 1);
		}
		List<Map<String, Object>> users = restClient.get()
			.uri(properties.getKeycloak().adminUsersUrl() + "?email={email}&exact=true", email)
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});
		if (users == null || users.isEmpty()) {
			throw new IdentityProviderException("Created user id could not be resolved");
		}
		return String.valueOf(users.getFirst().get("id"));
	}

	private static String firstName(String fullName) {
		String[] parts = fullName.trim().split("\\s+", 2);
		return parts[0];
	}

	private static String lastName(String fullName) {
		String[] parts = fullName.trim().split("\\s+", 2);
		return parts.length > 1 ? parts[1] : "-";
	}

	private record CachedToken(String token, Instant expiresAt) {
	}
}
