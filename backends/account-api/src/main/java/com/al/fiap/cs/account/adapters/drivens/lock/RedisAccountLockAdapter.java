package com.al.fiap.cs.account.adapters.drivens.lock;

import com.al.fiap.cs.account.adapters.config.AccountProperties;
import com.al.fiap.cs.account.ports.lock.AccountLockPort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

public class RedisAccountLockAdapter implements AccountLockPort {

	private final StringRedisTemplate redisTemplate;
	private final Duration lease;

	public RedisAccountLockAdapter(StringRedisTemplate redisTemplate, AccountProperties properties) {
		this.redisTemplate = redisTemplate;
		this.lease = properties.getLock().getLease();
	}

	@Override
	public LockHandle acquire(Long accountId) {
		String key = key(accountId);
		String token = UUID.randomUUID().toString();
		long deadline = System.currentTimeMillis() + lease.toMillis();
		while (System.currentTimeMillis() < deadline) {
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, lease);
			if (Boolean.TRUE.equals(acquired)) {
				return new LockHandle(accountId, token);
			}
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while acquiring account lock");
			}
		}
		throw new IllegalStateException("Unable to acquire account lock");
	}

	@Override
	public void release(LockHandle handle) {
		String key = key(handle.accountId());
		String current = redisTemplate.opsForValue().get(key);
		if (handle.token().equals(current)) {
			redisTemplate.delete(key);
		}
	}

	private static String key(Long accountId) {
		return "lock:account:" + accountId;
	}
}
