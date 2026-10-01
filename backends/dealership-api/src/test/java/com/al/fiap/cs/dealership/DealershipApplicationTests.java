package com.al.fiap.cs.dealership;

import com.al.fiap.cs.dealership.adapters.config.TestInfrastructureConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestInfrastructureConfig.class)
class DealershipApplicationTests {

	@Test
	void contextLoads() {
	}
}
