package com.al.fiap.cs.account.adapters.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_JWT = "bearer-jwt";

	@Bean
	OpenAPI accountOpenApi() {
		return new OpenAPI()
			.info(new Info()
				.title("Account API")
				.description("FIAP Car Sales account and authentication API")
				.version("v1"))
			.servers(List.of(new Server().url("/api/account").description("Gateway")))
			.components(new Components()
				.addSecuritySchemes(BEARER_JWT, new SecurityScheme()
					.name(BEARER_JWT)
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT")))
			.addSecurityItem(new SecurityRequirement().addList(BEARER_JWT));
	}
}
