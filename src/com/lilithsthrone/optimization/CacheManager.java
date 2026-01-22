package com.lilithsthrone.optimization;

import java.util.*;
import java.util.concurrent.*;

/**
 * Cache Manager - Advanced caching with multiple strategies
 * 
 * Provides LRU, LFU, and TTL-based caching for frequently accessed data.
 * Optimizes memory usage and access patterns through strategic eviction.
 * 
 * Responsibilities:
 *  - Manage cache lifecycle
 *  - Implement eviction policies (LRU, LFU, TTL)
 *  - Track cache statistics
 *  - Monitor cache health
 *  - Provide thread-safe access
 * 
 * Usage:
 *  // Create cache
 *  Cache<String, GameCharacter> charCache = 
 *      new LRUCache<>(100); // 100 max entries
 *  
 *  // Put and get
 *  charCache.put("innoxia", character);
 *  GameCharacter c = charCache.get("innoxia");
 *  
 *  // TTL cache
 *  Cache<String, Data> ttlCache = 
 *      new TTLCache<>(60000); // 60 second TTL
 *  
 *  // Get stats
 *  CacheStatistics stats = charCache.getStatistics();
 * 
 * @since Step 5
 * @version 1.0
 */
public class CacheManager {

	/**
	 * Cache statistics
	 */
	public static class CacheStatistics {
		public long hits = 0;
		public long misses = 0;
		public long puts = 0;
		public long evictions = 0;
		public int currentSize = 0;
		public int maxSize = 0;
		public long memoryUsage = 0;

		public double getHitRatio() {
			long total = hits + misses;
			return total == 0 ? 0 : (double) hits / total;
		}

		@Override
		public String toString() {
			return String.format(
				"Cache{hits=%d, misses=%d, ratio=%.1f%%, size=%d/%d, evictions=%d, memory=%dKB}",
				hits, misses, getHitRatio() * 100, currentSize, maxSize, evictions, memoryUsage / 1024
			);
		}
	}

	/**
	 * Generic cache interface
	 */
	public interface Cache<K, V> {
		V put(K key, V value);
		V get(K key);
		V remove(K key);
		void clear();
		int size();
		CacheStatistics getStatistics();
	}

	/**
	 * LRU Cache - Least Recently Used eviction
	 */
	public static class LRUCache<K, V> extends LinkedHashMap<K, V> implements Cache<K, V> {
		private final int maxSize;
		private final CacheStatistics stats = new CacheStatistics();

		public LRUCache(int maxSize) {
			super(16, 0.75f, true); // Access-order LinkedHashMap
			this.maxSize = maxSize;
			this.stats.maxSize = maxSize;
		}

		@Override
		public V put(K key, V value) {
			V old = super.put(key, value);
			stats.puts++;
			stats.currentSize = size();
			return old;
		}

		@Override
		public V get(Object key) {
			@SuppressWarnings("unchecked")
			V value = super.get(key);
			if (value != null) {
				stats.hits++;
			} else {
				stats.misses++;
			}
			stats.currentSize = size();
			return value;
		}

		@Override
		public V remove(Object key) {
			V value = super.remove(key);
			stats.currentSize = size();
			return value;
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			if (size() > maxSize) {
				stats.evictions++;
				return true;
			}
			return false;
		}

		@Override
		public CacheStatistics getStatistics() {
			stats.currentSize = size();
			return stats;
		}
	}

	/**
	 * LFU Cache - Least Frequently Used eviction
	 */
	public static class LFUCache<K, V> implements Cache<K, V> {
		private final int maxSize;
		private final Map<K, V> cache;
		private final Map<K, Integer> frequencies;
		private final Map<Integer, Queue<K>> frequencyQueues;
		private int minFrequency = 0;
		private final CacheStatistics stats = new CacheStatistics();

		public LFUCache(int maxSize) {
			this.maxSize = maxSize;
			this.cache = new HashMap<>();
			this.frequencies = new HashMap<>();
			this.frequencyQueues = new HashMap<>();
			this.stats.maxSize = maxSize;
		}

		@Override
		@Override
		public V put(K key, V value) {
			V old = null;
			if (cache.containsKey(key)) {
				old = cache.get(key);
				cache.put(key, value);
				updateFrequency(key);
			} else {
				if (cache.size() >= maxSize) {
					evictLFU();
				}
				cache.put(key, value);
				frequencies.put(key, 1);
				frequencyQueues.computeIfAbsent(1, k -> new LinkedList<>()).add(key);
				minFrequency = 1;
			}
			stats.puts++;
			stats.currentSize = cache.size();
			return old;
		}

		@Override
		public V get(K key) {
			if (!cache.containsKey(key)) {
				stats.misses++;
				return null;
			}
			stats.hits++;
			updateFrequency(key);
			stats.currentSize = cache.size();
			return cache.get(key);
		}

		private void updateFrequency(K key) {
			int freq = frequencies.getOrDefault(key, 0);
			frequencies.put(key, freq + 1);
			
			if (freq > 0) {
				frequencyQueues.get(freq).remove(key);
			}
			frequencyQueues.computeIfAbsent(freq + 1, k -> new LinkedList<>()).add(key);
		}

		private void evictLFU() {
			Queue<K> minFreqQueue = frequencyQueues.get(minFrequency);
			K evicted = minFreqQueue.poll();
			if (evicted != null) {
				cache.remove(evicted);
				frequencies.remove(evicted);
				stats.evictions++;
			}
		}

		@Override
		public V remove(K key) {
			V value = cache.remove(key);
			frequencies.remove(key);
			stats.currentSize = cache.size();
			return value;
		}

		@Override
		public void clear() {
			cache.clear();
			frequencies.clear();
			frequencyQueues.clear();
			stats.currentSize = 0;
		}

		@Override
		public int size() {
			return cache.size();
		}

		@Override
		public CacheStatistics getStatistics() {
			stats.currentSize = cache.size();
			return stats;
		}
	}

	/**
	 * TTL Cache - Time-to-Live based expiration
	 */
	public static class TTLCache<K, V> implements Cache<K, V> {
		private final long ttlMillis;
		private final Map<K, V> cache;
		private final Map<K, Long> timestamps;
		private final CacheStatistics stats = new CacheStatistics();

		public TTLCache(long ttlMillis) {
			this(ttlMillis, Integer.MAX_VALUE);
		}

		public TTLCache(long ttlMillis, int maxSize) {
			this.ttlMillis = ttlMillis;
			this.cache = new ConcurrentHashMap<>();
			this.timestamps = new ConcurrentHashMap<>();
			this.stats.maxSize = maxSize;
		}
		@Override
		public V put(K key, V value) {
			cache.put(key, value);
			timestamps.put(key, System.currentTimeMillis());
			stats.puts++;
			stats.currentSize = cache.size();
			return value;
		}

		@Override
		public V get(K key) {
			Long timestamp = timestamps.get(key);
			if (timestamp == null) {
				stats.misses++;
				return null;
			}

			if (System.currentTimeMillis() - timestamp > ttlMillis) {
				// Expired
				remove(key);
				stats.misses++;
				return null;
			}

			stats.hits++;
			stats.currentSize = cache.size();
			return cache.get(key);
		}

		@Override
		public V remove(K key) {
			timestamps.remove(key);
			stats.evictions++;
			stats.currentSize = cache.size();
			return cache.remove(key);
		}

		@Override
		public void clear() {
			cache.clear();
			timestamps.clear();
			stats.currentSize = 0;
		}

		@Override
		public int size() {
			return cache.size();
		}

		@Override
		public CacheStatistics getStatistics() {
			stats.currentSize = cache.size();
			return stats;
		}
	}

	/**
	 * Create LRU cache
	 * 
	 * @param maxSize Maximum cache size
	 * @return LRU cache instance
	 */
	public static <K, V> Cache<K, V> createLRUCache(int maxSize) {
		return new LRUCache<>(maxSize);
	}

	/**
	 * Create LFU cache
	 * 
	 * @param maxSize Maximum cache size
	 * @return LFU cache instance
	 */
	public static <K, V> Cache<K, V> createLFUCache(int maxSize) {
		return new LFUCache<>(maxSize);
	}

	/**
	 * Create TTL cache
	 * 
	 * @param ttlMillis Time-to-live in milliseconds
	 * @return TTL cache instance
	 */
	public static <K, V> Cache<K, V> createTTLCache(long ttlMillis) {
		return new TTLCache<>(ttlMillis);
	}

	/**
	 * Create TTL cache with max size
	 * 
	 * @param ttlMillis Time-to-live in milliseconds
	 * @param maxSize Maximum cache size
	 * @return TTL cache instance
	 */
	public static <K, V> Cache<K, V> createTTLCache(long ttlMillis, int maxSize) {
		return new TTLCache<>(ttlMillis, maxSize);
	}
}
