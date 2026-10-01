package com.al.fiap.cs.dealership.adapters.drivers.webapis.security;

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

public class AuthenticatedActorArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return AuthenticatedActor.class.isAssignableFrom(parameter.getParameterType());
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
		Long accountId = JwtAccountIdClaims.parseAccountId(jwt);
		boolean serviceAccount = accountId == null;
		String clientId = jwt.getClaimAsString("azp");
		if (clientId == null || clientId.isBlank()) {
			clientId = jwt.getClaimAsString("client_id");
		}
		Set<String> roles = extractRoles(jwt);
		return new AuthenticatedActor(accountId, jwt.getSubject(), clientId, roles, serviceAccount);
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
		if (roles.isEmpty()) {
			List<String> emptyList = List.of();
			return Set.copyOf(emptyList);
		}
		return Set.copyOf(roles);
	}
}
