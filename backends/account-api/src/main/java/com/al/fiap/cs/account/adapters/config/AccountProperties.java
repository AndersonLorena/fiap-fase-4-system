package com.al.fiap.cs.account.adapters.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "account")
public class AccountProperties {

	private String frontendOrigin;
	private String passwordRecoveryUrl;
	private Duration passwordRecoveryTtl;
	private Throttle throttle = new Throttle();
	private Lock lock = new Lock();
	private Snowflake snowflake = new Snowflake();
	private Keycloak keycloak = new Keycloak();
	private Resend resend = new Resend();
	private Bootstrap bootstrap = new Bootstrap();
	private S2s s2s = new S2s();
	private String testJwtSecret;

	public String getFrontendOrigin() {
		return frontendOrigin;
	}

	public void setFrontendOrigin(String frontendOrigin) {
		this.frontendOrigin = frontendOrigin;
	}

	public String getPasswordRecoveryUrl() {
		return passwordRecoveryUrl;
	}

	public void setPasswordRecoveryUrl(String passwordRecoveryUrl) {
		this.passwordRecoveryUrl = passwordRecoveryUrl;
	}

	public Duration getPasswordRecoveryTtl() {
		return passwordRecoveryTtl;
	}

	public void setPasswordRecoveryTtl(Duration passwordRecoveryTtl) {
		this.passwordRecoveryTtl = passwordRecoveryTtl;
	}

	public Throttle getThrottle() {
		return throttle;
	}

	public void setThrottle(Throttle throttle) {
		this.throttle = throttle;
	}

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public Snowflake getSnowflake() {
		return snowflake;
	}

	public void setSnowflake(Snowflake snowflake) {
		this.snowflake = snowflake;
	}

	public Keycloak getKeycloak() {
		return keycloak;
	}

	public void setKeycloak(Keycloak keycloak) {
		this.keycloak = keycloak;
	}

	public Resend getResend() {
		return resend;
	}

	public void setResend(Resend resend) {
		this.resend = resend;
	}

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public S2s getS2s() {
		return s2s;
	}

	public void setS2s(S2s s2s) {
		this.s2s = s2s;
	}

	public String getTestJwtSecret() {
		return testJwtSecret;
	}

	public void setTestJwtSecret(String testJwtSecret) {
		this.testJwtSecret = testJwtSecret;
	}

	public static class Throttle {
		private Duration window;
		private int maxAttempts;

		public Duration getWindow() {
			return window;
		}

		public void setWindow(Duration window) {
			this.window = window;
		}

		public int getMaxAttempts() {
			return maxAttempts;
		}

		public void setMaxAttempts(int maxAttempts) {
			this.maxAttempts = maxAttempts;
		}
	}

	public static class Lock {
		private Duration lease;

		public Duration getLease() {
			return lease;
		}

		public void setLease(Duration lease) {
			this.lease = lease;
		}
	}

	public static class Snowflake {
		private long machineId;

		public long getMachineId() {
			return machineId;
		}

		public void setMachineId(long machineId) {
			this.machineId = machineId;
		}
	}

	public static class Keycloak {
		private String url;
		private String realm;
		private String clientId;
		private String clientSecret;
		@NestedConfigurationProperty
		private Jwks jwks = new Jwks();

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getRealm() {
			return realm;
		}

		public void setRealm(String realm) {
			this.realm = realm;
		}

		public String getClientId() {
			return clientId;
		}

		public void setClientId(String clientId) {
			this.clientId = clientId;
		}

		public String getClientSecret() {
			return clientSecret;
		}

		public void setClientSecret(String clientSecret) {
			this.clientSecret = clientSecret;
		}

		public String realmBaseUrl() {
			return url + "/realms/" + realm;
		}

		public String tokenUrl() {
			return realmBaseUrl() + "/protocol/openid-connect/token";
		}

		public String certsUrl() {
			return realmBaseUrl() + "/protocol/openid-connect/certs";
		}

		public Jwks getJwks() {
			return jwks;
		}

		public void setJwks(Jwks jwks) {
			this.jwks = jwks;
		}

		public String adminUsersUrl() {
			return url + "/admin/realms/" + realm + "/users";
		}

		public String adminUserProfileUrl() {
			return url + "/admin/realms/" + realm + "/users/profile";
		}

		public static class Jwks {
			private Duration connectTimeout;
			private Duration readTimeout;
			private int retryAttempts;
			private Duration retryBackoff;

			public Duration getConnectTimeout() {
				return connectTimeout;
			}

			public void setConnectTimeout(Duration connectTimeout) {
				this.connectTimeout = connectTimeout;
			}

			public Duration getReadTimeout() {
				return readTimeout;
			}

			public void setReadTimeout(Duration readTimeout) {
				this.readTimeout = readTimeout;
			}

			public int getRetryAttempts() {
				return retryAttempts;
			}

			public void setRetryAttempts(int retryAttempts) {
				this.retryAttempts = retryAttempts;
			}

			public Duration getRetryBackoff() {
				return retryBackoff;
			}

			public void setRetryBackoff(Duration retryBackoff) {
				this.retryBackoff = retryBackoff;
			}
		}
	}

	public static class Resend {
		private String apiKey;
		private String from;
		private String baseUrl;

		public String getApiKey() {
			return apiKey;
		}

		public void setApiKey(String apiKey) {
			this.apiKey = apiKey;
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}

	public static class S2s {
		private List<String> allowedClientIds = new ArrayList<>(List.of("dealership-api"));

		public List<String> getAllowedClientIds() {
			return allowedClientIds;
		}

		public void setAllowedClientIds(List<String> allowedClientIds) {
			this.allowedClientIds = allowedClientIds;
		}
	}

	public static class Bootstrap {
		private Admin admin = new Admin();

		public Admin getAdmin() {
			return admin;
		}

		public void setAdmin(Admin admin) {
			this.admin = admin;
		}

		public static class Admin {
			private boolean enabled;
			private String email;
			private String password;
			private String fullName;

			public boolean isEnabled() {
				return enabled;
			}

			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}

			public String getEmail() {
				return email;
			}

			public void setEmail(String email) {
				this.email = email;
			}

			public String getPassword() {
				return password;
			}

			public void setPassword(String password) {
				this.password = password;
			}

			public String getFullName() {
				return fullName;
			}

			public void setFullName(String fullName) {
				this.fullName = fullName;
			}
		}
	}
}
