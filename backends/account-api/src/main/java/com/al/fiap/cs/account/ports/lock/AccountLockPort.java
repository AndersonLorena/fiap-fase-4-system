package com.al.fiap.cs.account.ports.lock;

public interface AccountLockPort {

	LockHandle acquire(Long accountId);

	void release(LockHandle handle);

	record LockHandle(Long accountId, String token) {
	}
}
