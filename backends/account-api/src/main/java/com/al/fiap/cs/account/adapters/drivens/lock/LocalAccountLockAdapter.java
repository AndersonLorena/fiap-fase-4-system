package com.al.fiap.cs.account.adapters.drivens.lock;

import com.al.fiap.cs.account.ports.lock.AccountLockPort;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocalAccountLockAdapter implements AccountLockPort {

	private final Map<Long, String> locks = new ConcurrentHashMap<>();

	@Override
	public LockHandle acquire(Long accountId) {
		String token = UUID.randomUUID().toString();
		String previous = locks.putIfAbsent(accountId, token);
		if (previous != null) {
			throw new IllegalStateException("Unable to acquire account lock");
		}
		return new LockHandle(accountId, token);
	}

	@Override
	public void release(LockHandle handle) {
		locks.remove(handle.accountId(), handle.token());
	}
}
