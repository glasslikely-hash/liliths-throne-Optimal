package com.lilithsthrone.optimization;

import java.util.*;

/**
 * Performance Monitor - Comprehensive performance tracking and analysis
 * 
 * Collects performance data from all optimization systems and provides
 * analysis, bottleneck detection, and performance reports.
 * 
 * Responsibilities:
 *  - Aggregate performance metrics
 *  - Detect performance bottlenecks
 *  - Generate performance reports
 *  - Track performance trends
 *  - Provide optimization recommendations
 * 
 * Usage:
 *  // Start monitoring
 *  PerformanceMonitor.start();
 *  
 *  // Get comprehensive report
 *  String report = PerformanceMonitor.getFullReport();
 *  
 *  // Detect bottlenecks
 *  List<PerformanceBottleneck> bottlenecks = 
 *      PerformanceMonitor.detectBottlenecks();
 * 
 * @since Step 5
 * @version 1.0
 */
public class PerformanceMonitor {

	private static final String MONITOR_NAME = "PerformanceMonitor";
	
	private static boolean monitoring = false;
	private static long monitoringStartTime = 0;
	private static final Map<String, Long> sectionStartTimes = new HashMap<>();
	private static final Map<String, Long> sectionTotalTimes = new HashMap<>();
	private static final Map<String, Integer> sectionCounts = new HashMap<>();

	/**
	 * Performance bottleneck
	 */
	public static class PerformanceBottleneck {
		public String name;
		public long timeMs;
		public int count;
		public double averageMs;
		public double severity; // 0.0 to 1.0

		public PerformanceBottleneck(String name, long timeMs, int count, double severity) {
			this.name = name;
			this.timeMs = timeMs;
			this.count = count;
			this.averageMs = count > 0 ? (double) timeMs / count : 0;
			this.severity = severity;
		}

		@Override
		public String toString() {
			return String.format("%s: %dms (avg: %.2fms, count: %d, severity: %.1f%%)",
				name, timeMs, averageMs, count, severity * 100);
		}
	}

	/**
	 * Start performance monitoring
	 */
	public static void start() {
		if (monitoring) {
			System.out.println("[" + MONITOR_NAME + "] Already monitoring");
			return;
		}

		monitoring = true;
		monitoringStartTime = System.currentTimeMillis();
		sectionStartTimes.clear();
		sectionTotalTimes.clear();
		sectionCounts.clear();

		System.out.println("[" + MONITOR_NAME + "] Started monitoring");
	}

	/**
	 * Stop performance monitoring
	 */
	public static void stop() {
		if (!monitoring) {
			return;
		}

		monitoring = false;
		long elapsed = System.currentTimeMillis() - monitoringStartTime;
		System.out.println("[" + MONITOR_NAME + "] Stopped monitoring (elapsed: " + elapsed + "ms)");
	}

	/**
	 * Mark start of a performance section
	 * 
	 * @param sectionName Name of section to monitor
	 */
	public static void startSection(String sectionName) {
		if (!monitoring) return;
		sectionStartTimes.put(sectionName, System.nanoTime());
	}

	/**
	 * Mark end of a performance section
	 * 
	 * @param sectionName Name of section to monitor
	 */
	public static void endSection(String sectionName) {
		if (!monitoring) return;

		Long startTime = sectionStartTimes.remove(sectionName);
		if (startTime == null) {
			System.err.println("[" + MONITOR_NAME + "] Section not started: " + sectionName);
			return;
		}

		long durationNano = System.nanoTime() - startTime;
		long durationMs = durationNano / 1_000_000;

		sectionTotalTimes.put(sectionName, sectionTotalTimes.getOrDefault(sectionName, 0L) + durationMs);
		sectionCounts.put(sectionName, sectionCounts.getOrDefault(sectionName, 0) + 1);
	}

	/**
	 * Get total time for a section
	 * 
	 * @param sectionName Section name
	 * @return Total time in milliseconds
	 */
	public static long getSectionTime(String sectionName) {
		return sectionTotalTimes.getOrDefault(sectionName, 0L);
	}

	/**
	 * Get average time for a section
	 * 
	 * @param sectionName Section name
	 * @return Average time in milliseconds
	 */
	public static double getSectionAverageTime(String sectionName) {
		Integer count = sectionCounts.get(sectionName);
		if (count == null || count == 0) return 0;
		return (double) sectionTotalTimes.getOrDefault(sectionName, 0L) / count;
	}

	/**
	 * Detect performance bottlenecks
	 * 
	 * @return List of detected bottlenecks sorted by severity
	 */
	public static List<PerformanceBottleneck> detectBottlenecks() {
		List<PerformanceBottleneck> bottlenecks = new ArrayList<>();

		long totalTime = sectionTotalTimes.values().stream().mapToLong(Long::longValue).sum();
		if (totalTime == 0) {
			return bottlenecks;
		}

		for (Map.Entry<String, Long> entry : sectionTotalTimes.entrySet()) {
			String name = entry.getKey();
			long timeMs = entry.getValue();
			int count = sectionCounts.getOrDefault(name, 0);
			double severity = (double) timeMs / totalTime;

			if (severity > 0.05) { // Only bottlenecks using >5% of time
				bottlenecks.add(new PerformanceBottleneck(name, timeMs, count, severity));
			}
		}

		// Sort by severity (descending)
		bottlenecks.sort((a, b) -> Double.compare(b.severity, a.severity));
		return bottlenecks;
	}

	/**
	 * Get performance summary
	 * 
	 * @return Summary string
	 */
	public static String getSummary() {
		StringBuilder summary = new StringBuilder();
		summary.append("[Performance Summary]\n");

		if (!monitoring) {
			summary.append("Monitoring not active\n");
			return summary.toString();
		}

		long elapsed = System.currentTimeMillis() - monitoringStartTime;
		summary.append("Elapsed time: ").append(elapsed).append("ms\n");
		summary.append("Sections tracked: ").append(sectionTotalTimes.size()).append("\n");
		summary.append("Total samples: ").append(sectionCounts.values().stream().mapToInt(Integer::intValue).sum()).append("\n");

		return summary.toString();
	}

	/**
	 * Get full performance report
	 * 
	 * @return Detailed performance report
	 */
	public static String getFullReport() {
		StringBuilder report = new StringBuilder();

		report.append("========================================\n");
		report.append("PERFORMANCE MONITOR REPORT\n");
		report.append("========================================\n\n");

		// System metrics
		FrameRateOptimizer.PerformanceMetrics frameMetrics = FrameRateOptimizer.getMetrics();
		report.append("FRAME RATE:\n");
		report.append("  Current FPS: ").append(frameMetrics.currentFPS).append("\n");
		report.append("  Target FPS: ").append(frameMetrics.targetFPS).append("\n");
		report.append("  Avg Frame Time: ").append(String.format("%.2f", frameMetrics.averageFrameTime * 1000)).append("ms\n");
		report.append("  Max Frame Time: ").append(String.format("%.2f", frameMetrics.maxFrameTime * 1000)).append("ms\n");
		report.append("  Dropped Frames: ").append(frameMetrics.droppedFrames).append("\n\n");

		// Memory metrics
		MemoryManager.MemoryStatistics memStats = MemoryManager.getMemoryStatistics();
		report.append("MEMORY:\n");
		report.append("  Used: ").append(memStats.heapUsed / (1024 * 1024)).append("MB / ")
			   .append(memStats.heapMax / (1024 * 1024)).append("MB\n");
		report.append("  Pressure: ").append(String.format("%.1f%%", memStats.memoryPressure * 100)).append("\n");
		report.append("  GC Count: ").append(memStats.gcCount).append("\n");
		report.append("  Total GC Time: ").append(memStats.totalGCTime).append("ms\n\n");

		// Performance sections
		if (!sectionTotalTimes.isEmpty()) {
			report.append("PERFORMANCE SECTIONS:\n");
			for (Map.Entry<String, Long> entry : sectionTotalTimes.entrySet()) {
				String name = entry.getKey();
				long timeMs = entry.getValue();
				int count = sectionCounts.get(name);
				double avgMs = (double) timeMs / count;

				report.append(String.format("  %s: %dms (avg: %.2fms, count: %d)\n",
					name, timeMs, avgMs, count));
			}
			report.append("\n");
		}

		// Bottlenecks
		List<PerformanceBottleneck> bottlenecks = detectBottlenecks();
		if (!bottlenecks.isEmpty()) {
			report.append("BOTTLENECKS:\n");
			for (PerformanceBottleneck bottleneck : bottlenecks) {
				report.append("  ").append(bottleneck).append("\n");
			}
			report.append("\n");
		}

		// Recommendations
		report.append("RECOMMENDATIONS:\n");
		if (frameMetrics.currentFPS < frameMetrics.targetFPS * 0.9) {
			report.append("  ⚠ Frame rate is below target - optimize rendering or logic\n");
		}
		if (memStats.memoryPressure > 0.8) {
			report.append("  ⚠ Memory usage is high - reduce cache sizes or pooled objects\n");
		}
		if (frameMetrics.droppedFrames > frameMetrics.frameCount * 0.1) {
			report.append("  ⚠ Frame drops detected - optimize long-running operations\n");
		}
		if (bottlenecks.isEmpty()) {
			report.append("  ✓ No significant bottlenecks detected\n");
		}

		report.append("\n========================================\n");

		return report.toString();
	}

	/**
	 * Print performance report to console
	 */
	public static void printReport() {
		System.out.println(getFullReport());
	}

	/**
	 * Reset all performance data
	 */
	public static void reset() {
		sectionStartTimes.clear();
		sectionTotalTimes.clear();
		sectionCounts.clear();
		System.out.println("[" + MONITOR_NAME + "] Performance data reset");
	}

	/**
	 * Export performance data as CSV
	 * 
	 * @return CSV formatted string
	 */
	public static String exportAsCSV() {
		StringBuilder csv = new StringBuilder();
		csv.append("Section,Total(ms),Count,Average(ms)\n");

		for (Map.Entry<String, Long> entry : sectionTotalTimes.entrySet()) {
			String name = entry.getKey();
			long timeMs = entry.getValue();
			int count = sectionCounts.getOrDefault(name, 0);
			double avgMs = count > 0 ? (double) timeMs / count : 0;

			csv.append(name).append(",").append(timeMs).append(",")
			   .append(count).append(",").append(String.format("%.2f", avgMs)).append("\n");
		}

		return csv.toString();
	}
}
