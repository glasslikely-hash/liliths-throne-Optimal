package com.lilithsthrone.optimization;

import java.util.*;

/**
 * Frame Rate Optimizer - Maintains consistent frame rate through delta time optimization
 * 
 * Monitors frame timing and adjusts rendering/update frequency to maintain
 * target FPS. Handles frame skipping, delta time accumulation, and frame timing.
 * 
 * Responsibilities:
 *  - Track frame timing
 *  - Manage delta time
 *  - Detect frame drops
 *  - Optimize frame rate
 *  - Monitor performance metrics
 * 
 * Usage:
 *  // Initialize at startup
 *  FrameRateOptimizer.initialize(60); // 60 FPS target
 *  
 *  // Each frame
 *  float deltaTime = FrameRateOptimizer.updateFrame();
 *  
 *  // Use delta time for updates
 *  gameLogic.update(deltaTime);
 *  
 *  // Get statistics
 *  PerformanceMetrics metrics = FrameRateOptimizer.getMetrics();
 * 
 * @since Step 5
 * @version 1.0
 */
public class FrameRateOptimizer {

	private static final String OPTIMIZER_NAME = "FrameRateOptimizer";

	// Timing configuration
	private static int targetFPS = 60;
	private static float targetFrameTime = 1.0f / 60.0f; // seconds per frame
	private static long targetFrameTimeNano = 16_666_666; // nanoseconds per frame

	// Frame timing
	private static long lastFrameTime = System.nanoTime();
	private static float currentDeltaTime = 0f;
	private static float accumulatedDeltaTime = 0f;
	private static int frameCount = 0;

	// Frame rate management
	private static long frameStartTime = System.currentTimeMillis();
	private static int framesThisSecond = 0;
	private static int currentFPS = 0;

	// Performance metrics
	private static double averageFrameTime = 0;
	private static double maxFrameTime = 0;
	private static double minFrameTime = Double.MAX_VALUE;
	private static int droppedFrames = 0;

	// Configuration
	private static boolean initialized = false;
	private static float maxDeltaTime = 0.05f; // Max 50ms per frame
	private static float timeScale = 1.0f; // Game speed multiplier

	/**
	 * Performance metrics snapshot
	 */
	public static class PerformanceMetrics {
		public int currentFPS;
		public int targetFPS;
		public float deltaTime;
		public float averageFrameTime;
		public float maxFrameTime;
		public float minFrameTime;
		public int frameCount;
		public int droppedFrames;
		public double frameTimeVariance;

		@Override
		public String toString() {
			return String.format(
				"Metrics{FPS=%d/%d, delta=%.2fms, avg=%.2fms, max=%.2fms, dropped=%d}",
				currentFPS, targetFPS, deltaTime * 1000, averageFrameTime * 1000, 
				maxFrameTime * 1000, droppedFrames
			);
		}
	}

	/**
	 * Initialize frame rate optimizer
	 * 
	 * @param fps Target frames per second
	 */
	public static void initialize(int fps) {
		if (initialized) {
			System.out.println("[" + OPTIMIZER_NAME + "] Already initialized");
			return;
		}

		targetFPS = fps;
		targetFrameTime = 1.0f / fps;
		targetFrameTimeNano = (long) (targetFrameTime * 1_000_000_000);
		initialized = true;

		System.out.println("[" + OPTIMIZER_NAME + "] Initialized with target FPS: " + fps);
	}

	/**
	 * Update frame timing
	 * Should be called once per frame
	 * 
	 * @return Delta time in seconds (capped)
	 */
	public static float updateFrame() {
		if (!initialized) {
			throw new RuntimeException("FrameRateOptimizer not initialized");
		}

		long currentTime = System.nanoTime();
		long frameDurationNano = currentTime - lastFrameTime;
		lastFrameTime = currentTime;

		// Convert to seconds
		currentDeltaTime = Math.min(frameDurationNano / 1_000_000_000.0f, maxDeltaTime);
		accumulatedDeltaTime += currentDeltaTime;

		// Apply time scale
		float scaledDeltaTime = currentDeltaTime * timeScale;

		// Update frame statistics
		updateFrameStatistics(frameDurationNano);

		// Check for frame drops
		if (frameDurationNano > targetFrameTimeNano * 1.5) {
			droppedFrames++;
		}

		frameCount++;
		return scaledDeltaTime;
	}

	/**
	 * Update frame statistics
	 */
	private static void updateFrameStatistics(long frameDurationNano) {
		double frameTimeMs = frameDurationNano / 1_000_000.0;

		// Update average
		averageFrameTime = (averageFrameTime * (frameCount - 1) + frameTimeMs) / frameCount;

		// Update min/max
		maxFrameTime = Math.max(maxFrameTime, frameTimeMs);
		minFrameTime = Math.min(minFrameTime, frameTimeMs);

		// Update FPS counter
		framesThisSecond++;
		long currentTime = System.currentTimeMillis();
		if (currentTime - frameStartTime >= 1000) {
			currentFPS = framesThisSecond;
			framesThisSecond = 0;
			frameStartTime = currentTime;
		}
	}

	/**
	 * Get current delta time
	 * 
	 * @return Time since last frame in seconds
	 */
	public static float getDeltaTime() {
		return currentDeltaTime * timeScale;
	}

	/**
	 * Get raw delta time (before time scale)
	 * 
	 * @return Time since last frame in seconds (unscaled)
	 */
	public static float getRawDeltaTime() {
		return currentDeltaTime;
	}

	/**
	 * Get accumulated delta time
	 * 
	 * @return Total accumulated time in seconds
	 */
	public static float getAccumulatedDeltaTime() {
		return accumulatedDeltaTime;
	}

	/**
	 * Reset accumulated delta time
	 */
	public static void resetAccumulatedDeltaTime() {
		accumulatedDeltaTime = 0f;
	}

	/**
	 * Get current FPS
	 * 
	 * @return Current frames per second
	 */
	public static int getCurrentFPS() {
		return currentFPS;
	}

	/**
	 * Get target FPS
	 * 
	 * @return Target frames per second
	 */
	public static int getTargetFPS() {
		return targetFPS;
	}

	/**
	 * Set time scale (game speed multiplier)
	 * 
	 * @param scale Time scale (1.0 = normal, 0.5 = half speed, 2.0 = double speed)
	 */
	public static void setTimeScale(float scale) {
		timeScale = Math.max(0, scale);
	}

	/**
	 * Get time scale
	 * 
	 * @return Current time scale
	 */
	public static float getTimeScale() {
		return timeScale;
	}

	/**
	 * Set maximum delta time (prevents large jumps)
	 * 
	 * @param maxDelta Maximum delta time in seconds
	 */
	public static void setMaxDeltaTime(float maxDelta) {
		maxDeltaTime = maxDelta;
	}

	/**
	 * Get performance metrics
	 * 
	 * @return Current performance snapshot
	 */
	public static PerformanceMetrics getMetrics() {
		PerformanceMetrics metrics = new PerformanceMetrics();
		metrics.currentFPS = currentFPS;
		metrics.targetFPS = targetFPS;
		metrics.deltaTime = currentDeltaTime;
		metrics.averageFrameTime = (float) averageFrameTime;
		metrics.maxFrameTime = (float) maxFrameTime;
		metrics.minFrameTime = (float) minFrameTime;
		metrics.frameCount = frameCount;
		metrics.droppedFrames = droppedFrames;

		// Calculate variance
		if (frameCount > 1) {
			metrics.frameTimeVariance = Math.pow(maxFrameTime - minFrameTime, 2) / frameCount;
		}

		return metrics;
	}

	/**
	 * Get frame count
	 * 
	 * @return Total frames rendered
	 */
	public static int getFrameCount() {
		return frameCount;
	}

	/**
	 * Check if frame rate is stable
	 * 
	 * @return true if current FPS within 10% of target
	 */
	public static boolean isStable() {
		int tolerance = Math.max(1, targetFPS / 10);
		return Math.abs(currentFPS - targetFPS) <= tolerance;
	}

	/**
	 * Get frame rate stability percentage
	 * 
	 * @return Stability as percentage (100 = perfect)
	 */
	public static double getStabilityPercentage() {
		if (frameCount == 0) return 100;
		return (1.0 - (droppedFrames / (double) frameCount)) * 100;
	}

	/**
	 * Print performance report
	 */
	public static void printReport() {
		PerformanceMetrics metrics = getMetrics();
		System.out.println("[" + OPTIMIZER_NAME + "] " + metrics);
		System.out.println("  Stability: " + String.format("%.1f%%", getStabilityPercentage()));
		System.out.println("  Time Scale: " + timeScale);
	}
}
