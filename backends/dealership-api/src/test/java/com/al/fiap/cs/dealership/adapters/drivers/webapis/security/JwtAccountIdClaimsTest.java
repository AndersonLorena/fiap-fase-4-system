package com.al.fiap.cs.dealership.adapters.drivers.webapis.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAccountIdClaimsTest {

	@Test
	void parsesStringClaim() {
		assertEquals(42L, JwtAccountIdClaims.parseAccountIdValue("42"));
	}

	@Test
	void parsesNumberClaim() {
		assertEquals(99L, JwtAccountIdClaims.parseAccountIdValue(99));
		assertEquals(100L, JwtAccountIdClaims.parseAccountIdValue(100L));
	}

	@Test
	void parsesListClaim() {
		assertEquals(7L, JwtAccountIdClaims.parseAccountIdValue(List.of("7")));
		assertEquals(8L, JwtAccountIdClaims.parseAccountIdValue(List.of(8)));
	}

	@Test
	void blankOrMissingReturnsNull() {
		assertNull(JwtAccountIdClaims.parseAccountIdValue(null));
		assertNull(JwtAccountIdClaims.parseAccountIdValue(""));
		assertNull(JwtAccountIdClaims.parseAccountIdValue("   "));
		assertNull(JwtAccountIdClaims.parseAccountIdValue(List.of()));
	}
}
