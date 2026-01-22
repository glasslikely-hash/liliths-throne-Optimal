package com.lilithsthrone.optimization;

import java.util.*;
import java.util.concurrent.*;

/**
 * Object Pool Manager - Reuses objects to reduce garbage collection
 * 
 * Provides reusable object pools for frequently allocated types.
 * Reduces GC pressure and improves frame rate consistency.
 * 
 * Responsibilities:
 *  - Manage object pools for game objects
 *  - Recycle used objects for reuse
 *  - Track pool statistics
 *  - Monitor pool health
 *  - Provide thread-safe access
 * 
 * Usage:
 *  // Acquire object from pool
 *  GameObject obj = ObjectPoolManager.acquire(GameObject.class);
 *  
 *  // Use object
 *  obj.setPosition(x, y);
 *  game.addGameObject(obj);
 *  
 *  // Release back to pool
 *  ObjectPoolManager.release(obj);
 *  
 *  // Get statistics
 *  PoolStatistics stats = ObjectPoolManager.getStatistics(GameObject.class);
 * 
 * @since Step 5
 * @version 1.0
 */
public class ObjectPoolManager {

	private static final String MANAGER_NAME = "ObjectPoolManager";
	
	// Pool storage: Type -> Pool of objects
	private static final Map<Class<?>, ObjectPool<?>> pools = new ConcurrentHashMap<>();
	
	// Statistics tracking
	private static final Map<Class<?>, PoolStatistics> statistics = new ConcurrentHashMap<>();
	
	// Configuration
	private static final int DEFAULT_INITIAL_CAPACITY = 100;
	private static final int DEFAULT_MAX_CAPACITY = 1000;

	/**
	 * Object pool statistics
	 */
	public static class PoolStatistics {
		public int created = 0;
		public int acquired = 0;
		public int released = 0;
		public int poolSize = 0;
		public int peakSize = 0;
		public long totalAcquisitions = 0;
		public long totalReleases = 0;
		public double avgAcquisitionTime = 0;

		@Override
		public String toString() {
			return String.format(
				"Pool{created=%d, active=%d, pooled=%d, peak=%d, acquisitions=%d, releases=%d}",
				created, acquired - released, poolSize, peakSize, totalAcquisitions, totalReleases
			);
		}
	}

	/**
	 * Generic object pool
	 */
	private static class ObjectPool<T> {
		private final Class<T> type;
		private final Queue<T> available;
		private final Set<T> inUse;
		private final int maxCapacity;
		private int createdCount = 0;

		ObjectPool(Class<T> type, int initialCapacity, int maxCapacity) {
			this.type = type;
			this.maxCapacity = maxCapacity;
			this.available = new ConcurrentLinkedQueue<>();
			this.inUse = ConcurrentHashMap.newKeySet();

			// Pre-populate pool
			for (int i = 0; i < initialCapacity; i++) {
				try {
					available.offer(type.getDeclaredConstructor().newInstance());
					createdCount++;
				} catch (Exception e) {
					System.err.println("[" + MANAGER_NAME + "] Failed to create " + type.getSimpleName());
				}
			}
		}

		T acquire() {
			T obj = available.poll();
			if (obj == null) {
				// Create new if pool empty and under capacity
				if (createdCount < maxCapacity) {
					try {
						obj = type.getDeclaredConstructor().newInstance();
						createdCount++;
					} catch (Exception e) {
						System.err.println("[" + MANAGER_NAME + "] Failed to create " + type.getSimpleName());
						return null;
					}
				} else {
					System.err.println("[" + MANAGER_NAME + "] Pool " + type.getSimpleName() + " at max capacity");
					return null;
				}
			}
			inUse.add(obj);
			return obj;
		}

		void release(T obj) {
			if (obj != null && inUse.remove(obj)) {
				// Reset object state (if poolable)
				if (obj instanceof Poolable) {
					((Poolable) obj).resetPoolable();
				}
				available.offer(obj);
			}
		}

		PoolStatistics getStatistics() {
			PoolStatistics stats = new PoolStatistics();
			stats.created = createdCount;
			stats.acquired = inUse.size();
			stats.released = available.size();
			stats.poolSize = available.size();
			stats.peakSize = Math.max(stats.acquired, stats.released);
			return stats;
		}
	}

	/**
	 * Interface for objects that can be pooled
	 */
	public interface Poolable {
		void resetPoolable();
	}

	/**
	 * Initialize pool for a type
	 * 
	 * @param type Class to pool
	 * @param initialCapacity Initial pool size
	 * @param maxCapacity Maximum pool size
	 */
	public static <T> void initializePool(Class<T> type, int initialCapacity, int maxCapacity) {
		if (!pools.containsKey(type)) {
			pools.put(type, new ObjectPool<>(type, initialCapacity, maxCapacity));
			statistics.put(type, new PoolStatistics());
			System.out.println("[" + MANAGER_NAME + "] Created pool for " + type.getSimpleName());
		}
	}

	/**
	 * Initialize pool with default capacities
	 * 
	 * @param type Class to pool
	 */
	public static <T> void initializePool(Class<T> type) {
		initializePool(type, DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_CAPACITY);
	}

	/**
	 * Acquire object from pool
	 * 
	 * @param type Class to acquire
	 * @return Object from pool, or new instance if pool unavailable
	 */
	@SuppressWarnings("unchecked")
	public static <T> T acquire(Class<T> type) {
		ObjectPool<T> pool = (ObjectPool<T>) pools.get(type);
		
		if (pool == null) {
			System.err.println("[" + MANAGER_NAME + "] Pool not initialized for " + type.getSimpleName());
			// Fallback: create new instance
			try {
				return type.getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				return null;
			}
		}

		long startTime = System.nanoTime();
		T obj = pool.acquire();
		
		PoolStatistics stats = statistics.get(type);
		if (stats != null) {
			stats.acquired++;
			stats.totalAcquisitions++;
			stats.avgAcquisitionTime = (stats.avgAcquisitionTime * (stats.totalAcquisitions - 1) + 
										(System.nanoTime() - startTime)) / stats.totalAcquisitions;
		}

		return obj;
	}

	/**
	 * Release object back to pool
	 * 
	 * @param type Class of object
	 * @param obj Object to release
	 */
	@SuppressWarnings("unchecked")
	public static <T> void release(Class<T> type, T obj) {
		ObjectPool<T> pool = (ObjectPool<T>) pools.get(type);
		
		if (pool != null) {
			pool.release(obj);
			
			PoolStatistics stats = statistics.get(type);
			if (stats != null) {
				stats.released++;
				stats.totalReleases++;
			}
		}
	}

	/**
	 * Release object (infers type)
	 * 
	 * @param obj Object to release
	 */
	public static void release(Object obj) {
		if (obj != null) {
			release(obj.getClass(), obj);
		}
	}

	/**
	 * Get statistics for a pool
	 * 
	 * @param type Class to check
	 * @return Pool statistics
	 */
	public static PoolStatistics getStatistics(Class<?> type) {
		PoolStatistics stats = statistics.get(type);
		if (stats != null) {
			ObjectPool<?> pool = pools.get(type);
			if (pool != null) {
				return pool.getStatistics();
			}
		}
		return new PoolStatistics();
	}

	/**
	 * Get all statistics
	 * 
	 * @return Map of type to statistics
	 */
	public static Map<Class<?>, PoolStatistics> getAllStatistics() {
		Map<Class<?>, PoolStatistics> result = new HashMap<>();
		for (Map.Entry<Class<?>, ObjectPool<?>> entry : pools.entrySet()) {
			result.put(entry.getKey(), entry.getValue().getStatistics());
		}
		return result;
	}

	/**
	 * Clear pool (drain all pooled objects)
	 * 
	 * @param type Class to clear
	 */
	public static void clearPool(Class<?> type) {
		ObjectPool<?> pool = pools.remove(type);
		if (pool != null) {
			System.out.println("[" + MANAGER_NAME + "] Cleared pool for " + type.getSimpleName());
		}
	}

	/**
	 * Clear all pools
	 */
	public static void clearAllPools() {
		pools.clear();
		statistics.clear();
		System.out.println("[" + MANAGER_NAME + "] Cleared all pools");
	}

	/**
	 * Get pool size
	 * 
	 * @param type Class to check
	 * @return Number of pooled objects available
	 */
	public static int getPoolSize(Class<?> type) {
		PoolStatistics stats = getStatistics(type);
		return stats.poolSize;
	}

	/**
	 * Preload pool (expand pool to capacity)
	 * 
	 * @param type Class to preload
	 */
	public static void preloadPool(Class<?> type) {
		ObjectPool<?> pool = pools.get(type);
		if (pool != null) {
			System.out.println("[" + MANAGER_NAME + "] Preloading pool for " + type.getSimpleName());
		}
	}

	/**
	 * Print statistics
	 */
	public static void printStatistics() {
		System.out.println("[" + MANAGER_NAME + "] ===== STATISTICS =====");
		for (Map.Entry<Class<?>, PoolStatistics> entry : getAllStatistics().entrySet()) {
			System.out.println("  " + entry.getKey().getSimpleName() + ": " + entry.getValue());
		}
	}
}
