package com.al.fiap.cs.account.adapters.drivers.webapis.security;

import java.util.Set;

public record AuthenticatedAccount(
		Long accountId,
		String subject,
		String authorizedParty,
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
}
