package com.al.fiap.cs.dealership.ports.lock;

public interface CarLockPort {

	LockHandle acquire(Long carId);

	void release(LockHandle handle);

	record LockHandle(Long carId, String token) {
	}
}
