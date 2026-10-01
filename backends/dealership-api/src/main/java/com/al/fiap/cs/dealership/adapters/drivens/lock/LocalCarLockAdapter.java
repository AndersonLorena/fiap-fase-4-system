package com.al.fiap.cs.dealership.adapters.drivens.lock;

import com.al.fiap.cs.dealership.core.domain.exceptions.CarLockUnavailableException;
import com.al.fiap.cs.dealership.ports.lock.CarLockPort;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalCarLockAdapter implements CarLockPort {

	private final Map<Long, String> locks = new ConcurrentHashMap<>();

	@Override
	public LockHandle acquire(Long carId) {
		String token = UUID.randomUUID().toString();
		String previous = locks.putIfAbsent(carId, token);
		if (previous != null) {
			throw new CarLockUnavailableException("Unable to acquire car lock");
		}
		return new LockHandle(carId, token);
	}

	@Override
	public void release(LockHandle handle) {
		locks.remove(handle.carId(), handle.token());
	}
}
