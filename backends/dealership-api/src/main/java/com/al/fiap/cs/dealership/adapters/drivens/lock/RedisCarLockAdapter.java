package com.al.fiap.cs.dealership.adapters.drivens.lock;

import com.al.fiap.cs.dealership.adapters.config.DealershipProperties;
import com.al.fiap.cs.dealership.core.domain.exceptions.CarLockUnavailableException;
import com.al.fiap.cs.dealership.ports.lock.CarLockPort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

public class RedisCarLockAdapter implements CarLockPort {

	private final StringRedisTemplate redisTemplate;
	private final Duration lease;

	public RedisCarLockAdapter(StringRedisTemplate redisTemplate, DealershipProperties properties) {
		this.redisTemplate = redisTemplate;
		this.lease = properties.getLock().getLease();
	}

	@Override
	public LockHandle acquire(Long carId) {
		String key = key(carId);
		String token = UUID.randomUUID().toString();
		long deadline = System.currentTimeMillis() + lease.toMillis();
		while (System.currentTimeMillis() < deadline) {
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, lease);
			if (Boolean.TRUE.equals(acquired)) {
				return new LockHandle(carId, token);
			}
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new CarLockUnavailableException("Interrupted while acquiring car lock");
			}
		}
		throw new CarLockUnavailableException("Unable to acquire car lock");
	}

	@Override
	public void release(LockHandle handle) {
		String key = key(handle.carId());
		String current = redisTemplate.opsForValue().get(key);
		if (handle.token().equals(current)) {
			redisTemplate.delete(key);
		}
	}

	private static String key(Long carId) {
		return "lock:dealership:car:" + carId;
	}
}
