package com.lilithsthrone.optimization;

import java.lang.management.*;

/**
 * Memory Manager - Monitors and optimizes memory usage
 * 
 * Tracks heap memory, garbage collection events, and memory pressure.
 * Provides alerts and optimization recommendations.
 * 
 * Responsibilities:
 *  - Monitor heap usage
 *  - Track garbage collection
 *  - Detect memory leaks
 *  - Provide memory optimization tips
 *  - Alert on memory pressure
 * 
 * Usage:
 *  // Get memory statistics
 *  MemoryStatistics stats = MemoryManager.getMemoryStatistics();
 *  
 *  // Check memory pressure
 *  if (MemoryManager.isMemoryPressureHigh()) {
 *      ObjectPoolManager.clearAllPools();
 *  }
 *  
 *  // Get recommendations
 *  String tips = MemoryManager.getOptimizationTips();
 * 
 * @since Step 5
 * @version 1.0
 */
public class MemoryManager {

	private static final String MANAGER_NAME = "MemoryManager";
	
	private static long totalGCTime = 0;
	private static int gcCount = 0;
	private static long peakMemory = 0;
	private static long lastMemoryCheck = System.currentTimeMillis();

	/**
	 * Memory statistics snapshot
	 */
	public static class MemoryStatistics {
		public long heapUsed;
		public long heapMax;
		public long heapFree;
		public long nonHeapUsed;
		public long totalGCTime;
		public int gcCount;
		public long peakMemory;
		public double memoryPressure; // 0.0 to 1.0

		@Override
		public String toString() {
			return String.format(
				"Memory{heap=%d/%dMB, free=%dMB, pressure=%.1f%%, GCs=%d, time=%dms}",
				heapUsed / (1024 * 1024), heapMax / (1024 * 1024),
				heapFree / (1024 * 1024), memoryPressure * 100, gcCount, totalGCTime
			);
		}
	}

	/**
	 * Get current memory statistics
	 * 
	 * @return Memory statistics snapshot
	 */
	public static MemoryStatistics getMemoryStatistics() {
		MemoryStatistics stats = new MemoryStatistics();

		Runtime runtime = Runtime.getRuntime();
		stats.heapUsed = runtime.totalMemory() - runtime.freeMemory();
		stats.heapMax = runtime.maxMemory();
		stats.heapFree = runtime.freeMemory();

		// Non-heap memory
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
		stats.nonHeapUsed = nonHeap.getUsed();

		stats.totalGCTime = totalGCTime;
		stats.gcCount = gcCount;
		stats.peakMemory = peakMemory;
		stats.memoryPressure = (double) stats.heapUsed / stats.heapMax;

		// Update peak
		if (stats.heapUsed > peakMemory) {
			peakMemory = stats.heapUsed;
		}

		return stats;
	}

	/**
	 * Get used memory in MB
	 * 
	 * @return Used heap memory in megabytes
	 */
	public static long getUsedMemoryMB() {
		Runtime runtime = Runtime.getRuntime();
		return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
	}

	/**
	 * Get available memory in MB
	 * 
	 * @return Available heap memory in megabytes
	 */
	public static long getAvailableMemoryMB() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.freeMemory() / (1024 * 1024);
	}

	/**
	 * Get max memory in MB
	 * 
	 * @return Maximum heap memory in megabytes
	 */
	public static long getMaxMemoryMB() {
		return Runtime.getRuntime().maxMemory() / (1024 * 1024);
	}

	/**
	 * Get memory pressure (0.0 to 1.0)
	 * 
	 * @return Memory usage ratio
	 */
	public static double getMemoryPressure() {
		Runtime runtime = Runtime.getRuntime();
		long used = runtime.totalMemory() - runtime.freeMemory();
		return (double) used / runtime.maxMemory();
	}

	/**
	 * Check if memory pressure is high (>80%)
	 * 
	 * @return true if memory pressure exceeds 80%
	 */
	public static boolean isMemoryPressureHigh() {
		return getMemoryPressure() > 0.8;
	}

	/**
	 * Check if memory pressure is critical (>95%)
	 * 
	 * @return true if memory pressure exceeds 95%
	 */
	public static boolean isMemoryPressureCritical() {
		return getMemoryPressure() > 0.95;
	}

	/**
	 * Force garbage collection
	 * Should only be used in exceptional circumstances
	 */
	public static void forceGarbageCollection() {
		long startTime = System.currentTimeMillis();
		System.gc();
		long gcTime = System.currentTimeMillis() - startTime;
		totalGCTime += gcTime;
		gcCount++;

		System.out.println("[" + MANAGER_NAME + "] Forced GC completed in " + gcTime + "ms");
	}

	/**
	 * Get total garbage collection time
	 * 
	 * @return Total GC time in milliseconds
	 */
	public static long getTotalGCTime() {
		return totalGCTime;
	}

	/**
	 * Get garbage collection count
	 * 
	 * @return Number of GC events
	 */
	public static int getGCCount() {
		return gcCount;
	}

	/**
	 * Get memory optimization tips based on current state
	 * 
	 * @return String with optimization recommendations
	 */
	public static String getOptimizationTips() {
		StringBuilder tips = new StringBuilder();
		MemoryStatistics stats = getMemoryStatistics();

		tips.append("[Memory Optimization Tips]\n");

		if (stats.memoryPressure > 0.9) {
			tips.append("- CRITICAL: Memory pressure is very high (").append(String.format("%.0f%%", stats.memoryPressure * 100)).append(")\n");
			tips.append("  → Clear caches and object pools\n");
			tips.append("  → Reduce active entities\n");
			tips.append("  → Force garbage collection\n");
		} else if (stats.memoryPressure > 0.75) {
			tips.append("- WARNING: Memory pressure is high (").append(String.format("%.0f%%", stats.memoryPressure * 100)).append(")\n");
			tips.append("  → Consider clearing unused caches\n");
			tips.append("  → Monitor memory trends\n");
		} else {
			tips.append("- OK: Memory usage is normal\n");
		}

		tips.append("\nGarbage Collection:\n");
		tips.append("- Total GC time: ").append(totalGCTime).append("ms\n");
		tips.append("- GC events: ").append(gcCount).append("\n");
		if (gcCount > 0) {
			tips.append("- Average GC time: ").append(totalGCTime / gcCount).append("ms\n");
		}

		tips.append("\nGeneral Tips:\n");
		tips.append("- Use object pooling for frequently created objects\n");
		tips.append("- Implement LRU/LFU caching strategies\n");
		tips.append("- Profile memory allocation patterns\n");
		tips.append("- Use weak references for caches when appropriate\n");
		tips.append("- Monitor GC pause times during gameplay\n");

		return tips.toString();
	}

	/**
	 * Print memory report
	 */
	public static void printReport() {
		System.out.println("[" + MANAGER_NAME + "] ===== MEMORY REPORT =====");
		MemoryStatistics stats = getMemoryStatistics();
		System.out.println(stats);
		System.out.println(getOptimizationTips());
	}

	/**
	 * Monitor memory continuously
	 * Prints alerts if thresholds exceeded
	 */
	public static void monitorMemory() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - lastMemoryCheck >= 1000) { // Check every 1 second
			lastMemoryCheck = currentTime;

			double pressure = getMemoryPressure();

			if (isMemoryPressureCritical()) {
				System.err.println("[" + MANAGER_NAME + "] CRITICAL memory pressure: " + 
									String.format("%.1f%%", pressure * 100));
			} else if (isMemoryPressureHigh()) {
				System.out.println("[" + MANAGER_NAME + "] WARNING memory pressure: " + 
									String.format("%.1f%%", pressure * 100));
			}
		}
	}
}
