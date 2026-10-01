package com.al.fiap.cs.dealership.adapters.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "dealership")
public class DealershipProperties {

	private String frontendOrigin;
	private Lock lock = new Lock();
	private Snowflake snowflake = new Snowflake();
	private Keycloak keycloak = new Keycloak();
	private AccountApi accountApi = new AccountApi();
	private Garage garage = new Garage();
	private Bootstrap bootstrap = new Bootstrap();
	private String uploadIntentSecret;
	private String testJwtSecret;
	private Payment payment = new Payment();

	public String getFrontendOrigin() {
		return frontendOrigin;
	}

	public void setFrontendOrigin(String frontendOrigin) {
		this.frontendOrigin = frontendOrigin;
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

	public AccountApi getAccountApi() {
		return accountApi;
	}

	public void setAccountApi(AccountApi accountApi) {
		this.accountApi = accountApi;
	}

	public Garage getGarage() {
		return garage;
	}

	public void setGarage(Garage garage) {
		this.garage = garage;
	}

	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public String getUploadIntentSecret() {
		return uploadIntentSecret;
	}

	public void setUploadIntentSecret(String uploadIntentSecret) {
		this.uploadIntentSecret = uploadIntentSecret;
	}

	public String getTestJwtSecret() {
		return testJwtSecret;
	}

	public void setTestJwtSecret(String testJwtSecret) {
		this.testJwtSecret = testJwtSecret;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
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

	public static class AccountApi {
		private String baseUrl;

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}
	}

	public static class Garage {
		private String endpoint;
		private String publicEndpoint;
		private String region;
		private String bucket;
		private String accessKey;
		private String secretKey;
		private Duration presignedTtl = Duration.ofMinutes(15);

		public String getEndpoint() {
			return endpoint;
		}

		public void setEndpoint(String endpoint) {
			this.endpoint = endpoint;
		}

		public String getPublicEndpoint() {
			return publicEndpoint;
		}

		public void setPublicEndpoint(String publicEndpoint) {
			this.publicEndpoint = publicEndpoint;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getBucket() {
			return bucket;
		}

		public void setBucket(String bucket) {
			this.bucket = bucket;
		}

		public String getAccessKey() {
			return accessKey;
		}

		public void setAccessKey(String accessKey) {
			this.accessKey = accessKey;
		}

		public String getSecretKey() {
			return secretKey;
		}

		public void setSecretKey(String secretKey) {
			this.secretKey = secretKey;
		}

		public Duration getPresignedTtl() {
			return presignedTtl;
		}

		public void setPresignedTtl(Duration presignedTtl) {
			this.presignedTtl = presignedTtl;
		}
	}

	public static class Bootstrap {
		private Catalog catalog = new Catalog();

		public Catalog getCatalog() {
			return catalog;
		}

		public void setCatalog(Catalog catalog) {
			this.catalog = catalog;
		}

		public static class Catalog {
			private boolean enabled;
			private long actorId;

			public boolean isEnabled() {
				return enabled;
			}

			public void setEnabled(boolean enabled) {
				this.enabled = enabled;
			}

			public long getActorId() {
				return actorId;
			}

			public void setActorId(long actorId) {
				this.actorId = actorId;
			}
		}
	}

	public static class Payment {
		private List<String> allowedClientIds = new ArrayList<>(List.of("payment-processor"));

		public List<String> getAllowedClientIds() {
			return allowedClientIds;
		}

		public void setAllowedClientIds(List<String> allowedClientIds) {
			this.allowedClientIds = allowedClientIds;
		}
	}
}
