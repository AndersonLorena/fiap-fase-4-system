package com.al.fiap.cs.account.adapters.drivers.webapis.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AuthenticatedAccountArgumentResolver implements HandlerMethodArgumentResolver {

	private final Set<String> allowedServiceClientIds;

	public AuthenticatedAccountArgumentResolver(List<String> allowedServiceClientIds) {
		this.allowedServiceClientIds = Set.copyOf(
			allowedServiceClientIds == null ? List.of() : allowedServiceClientIds
		);
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return AuthenticatedAccount.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(
			MethodParameter parameter,
			ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest,
			WebDataBinderFactory binderFactory) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			throw new IllegalStateException("Authenticated JWT is required");
		}
		return fromJwt(jwt, allowedServiceClientIds);
	}

	public static AuthenticatedAccount fromJwt(Jwt jwt, Set<String> allowedServiceClientIds) {
		Long accountId = JwtAccountIdClaims.parseAccountId(jwt);
		String azp = jwt.getClaimAsString("azp");
		if (azp == null || azp.isBlank()) {
			azp = jwt.getClaimAsString("client_id");
		}
		boolean serviceAccount = azp != null
			&& allowedServiceClientIds.contains(azp)
			&& accountId == null;
		return new AuthenticatedAccount(accountId, jwt.getSubject(), azp, extractRoles(jwt), serviceAccount);
	}

	public static boolean currentRequesterIsAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
			return false;
		}
		return fromJwt(jwt, Set.of()).hasRole("ROLE_ADMIN");
	}

	@SuppressWarnings("unchecked")
	private static Set<String> extractRoles(Jwt jwt) {
		Set<String> roles = new HashSet<>();
		Object realmAccess = jwt.getClaim("realm_access");
		if (realmAccess instanceof Map<?, ?> realmMap) {
			Object rolesClaim = realmMap.get("roles");
			if (rolesClaim instanceof Collection<?> collection) {
				for (Object role : collection) {
					if (role != null) {
						roles.add(String.valueOf(role));
					}
				}
			}
		}
		Object roleClaim = jwt.getClaim("roles");
		if (roleClaim instanceof Collection<?> collection) {
			for (Object role : collection) {
				if (role != null) {
					roles.add(String.valueOf(role));
				}
			}
		}
		return Set.copyOf(roles);
	}
}
