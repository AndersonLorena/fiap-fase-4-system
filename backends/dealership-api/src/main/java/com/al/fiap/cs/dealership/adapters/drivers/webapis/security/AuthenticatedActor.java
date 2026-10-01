package com.al.fiap.cs.dealership.adapters.drivers.webapis.security;

import java.util.Set;

public record AuthenticatedActor(
		Long accountId,
		String subject,
		String clientId,
		Set<String> roles,
		boolean serviceAccount) {

	public boolean hasRole(String role) {
		if (roles.contains(role)) {
			return true;
		}
		if (role.startsWith("ROLE_")) {
			return roles.contains(role.substring("ROLE_".length()));
		}
		return roles.contains("ROLE_" + role);
	}

	public Long actorId() {
		return accountId == null ? 0L : accountId;
	}
}
