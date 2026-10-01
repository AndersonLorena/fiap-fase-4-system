package com.al.fiap.cs.account.core.domain.shared;

import java.util.concurrent.locks.ReentrantLock;

public final class SnowflakeIdGenerator {

	private static final long EPOCH = 1704067200000L;
	private static final long MACHINE_ID_BITS = 2L;
	private static final long SEQUENCE_BITS = 10L;
	private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
	private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
	private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
	private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

	private final long machineId;
	private long sequence = 0L;
	private long lastTimestamp = -1L;
	private final ReentrantLock lock = new ReentrantLock();

	private static volatile SnowflakeIdGenerator instance;

	private SnowflakeIdGenerator(long machineId) {
		if (machineId < 0 || machineId > MAX_MACHINE_ID) {
			throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
		}
		this.machineId = machineId;
	}

	public static SnowflakeIdGenerator getInstance() {
		if (instance == null) {
			synchronized (SnowflakeIdGenerator.class) {
				if (instance == null) {
					instance = new SnowflakeIdGenerator(1L);
				}
			}
		}
		return instance;
	}

	public static void configure(long machineId) {
		synchronized (SnowflakeIdGenerator.class) {
			instance = new SnowflakeIdGenerator(machineId);
		}
	}

	public long nextId() {
		lock.lock();
		try {
			long timestamp = System.currentTimeMillis();
			if (timestamp < lastTimestamp) {
				throw new IllegalStateException("Clock moved backwards");
			}
			if (timestamp == lastTimestamp) {
				sequence = (sequence + 1) & MAX_SEQUENCE;
				if (sequence == 0) {
					timestamp = waitNextMillis(lastTimestamp);
				}
			}
			else {
				sequence = 0L;
			}
			lastTimestamp = timestamp;
			return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
				| (machineId << MACHINE_ID_SHIFT)
				| sequence;
		}
		finally {
			lock.unlock();
		}
	}

	private long waitNextMillis(long lastTimestamp) {
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = System.currentTimeMillis();
		}
		return timestamp;
	}
}
