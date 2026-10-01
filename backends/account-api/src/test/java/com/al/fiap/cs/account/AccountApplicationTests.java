package com.al.fiap.cs.account;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.al.fiap.cs.account.adapters.config.TestInfrastructureConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestInfrastructureConfig.class)
class AccountApplicationTests {

	@Test
	void contextLoads() {
	}
}
