package com.al.fiap.cs.dealership.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Configuration
public class SecurityConfig {

	private final DealershipProperties properties;

	public SecurityConfig(DealershipProperties properties) {
		this.properties = properties;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health", "/actuator/prometheus", "/actuator/info").permitAll()
				.requestMatchers(
					"/swagger",
					"/swagger-ui/**",
					"/v3/api-docs",
					"/v3/api-docs/**",
					"/webjars/**"
				).permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/brands", "/api/v1/brands/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/models", "/api/v1/models/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/colors", "/api/v1/colors/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/years", "/api/v1/years/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1/cars", "/api/v1/cars/**").permitAll()
				.anyRequest().authenticated()
			)
			.oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of(properties.getFrontendOrigin()));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(new RealmRolesAuthoritiesConverter());
		return jwt -> {
			Collection<GrantedAuthority> authorities = new RealmRolesAuthoritiesConverter().convert(jwt);
			return new JwtAuthenticationToken(jwt, authorities);
		};
	}

	private static class RealmRolesAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
		@Override
		public Collection<GrantedAuthority> convert(Jwt jwt) {
			Set<GrantedAuthority> authorities = new HashSet<>();
			Object realmAccess = jwt.getClaim("realm_access");
			if (realmAccess instanceof Map<?, ?> realmMap) {
				Object rolesClaim = realmMap.get("roles");
				if (rolesClaim instanceof Collection<?> roles) {
					for (Object role : roles) {
						if (role != null) {
							authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
						}
					}
				}
			}
			return new ArrayList<>(authorities);
		}
	}
}
