package com.lilithsthrone.logic;

import com.lilithsthrone.main.Main;
import java.util.*;

/**
 * Centralized Game Loop Coordinator
 * 
 * Manages main game loop execution, frame timing, and engine synchronization.
 * Coordinates all engine updates, persistence operations, and timing to ensure
 * smooth, synchronized gameplay.
 * 
 * @since Phase 2.6
 * @version 1.0
 */
public class GameLoopCoordinator {
	
	private static final String COORDINATOR_NAME = "GameLoopCoordinator";
	
	// Frame timing
	private static final int TARGET_FPS = 60;
	private static final float TARGET_DELTA_TIME = 1.0f / TARGET_FPS; // ~0.0167 seconds
	private static final long TARGET_FRAME_TIME_MS = 1000 / TARGET_FPS; // ~16.67ms
	
	// Autosave timing
	private static final int AUTOSAVE_INTERVAL = 30; // 30 seconds
	
	// Snapshot timing
	private static final int SNAPSHOT_INTERVAL = 600; // 10 minutes
	
	// Synchronization
	private static final int SYNC_CHECK_INTERVAL = 10; // 10 frames
	
	// State tracking
	private static long frameCount = 0;
	private static long elapsedTime = 0; // milliseconds
	private static long lastAutosaveTime = 0;
	private static long lastSnapshotTime = 0;
	
	private static boolean isRunning = false;
	private static boolean isPaused = false;
	
	/**
	 * Initializes the game loop coordinator
	 * Called once at game start
	 */
	public static void initialize() {
		frameCount = 0;
		elapsedTime = 0;
		lastAutosaveTime = System.currentTimeMillis();
		lastSnapshotTime = System.currentTimeMillis();
		isRunning = true;
		isPaused = false;
		
		System.out.println("[" + COORDINATOR_NAME + "] Initialized");
		System.out.println("[" + COORDINATOR_NAME + "] Target FPS: " + TARGET_FPS);
		System.out.println("[" + COORDINATOR_NAME + "] Autosave interval: " + AUTOSAVE_INTERVAL + "s");
		System.out.println("[" + COORDINATOR_NAME + "] Snapshot interval: " + SNAPSHOT_INTERVAL + "s");
	}
	
	/**
	 * Main game loop frame update
	 * Called every frame (typically 60 times per second)
	 * 
	 * @param actualDeltaTime Time since last frame in seconds
	 */
	public static void updateFrame(float actualDeltaTime) {
		if (!isRunning || isPaused) {
			return;
		}
		
		try {
			// Cap delta time to prevent spiral of death
			float clampedDelta = Math.min(actualDeltaTime, TARGET_DELTA_TIME * 2);
			
			// Update engine state
			GameLoopAdapter.delegateFrameUpdate(clampedDelta, frameCount);
			
			// Update timing counters
			frameCount++;
			long currentTime = System.currentTimeMillis();
			long frameElapsed = currentTime - lastAutosaveTime;
			elapsedTime += (long)(clampedDelta * 1000);
			
			// Check autosave timer
			if (frameElapsed >= (AUTOSAVE_INTERVAL * 1000)) {
				if (GameLoopAdapter.delegateAutosave(AUTOSAVE_INTERVAL)) {
					lastAutosaveTime = currentTime;
				}
			}
			
			// Check snapshot timer
			long snapshotElapsed = currentTime - lastSnapshotTime;
			if (snapshotElapsed >= (SNAPSHOT_INTERVAL * 1000)) {
				if (GameLoopAdapter.delegateSnapshot()) {
					lastSnapshotTime = currentTime;
				}
			}
			
			// Check synchronization timer
			if (frameCount % SYNC_CHECK_INTERVAL == 0) {
				GameLoopAdapter.delegateEngineSynchronization();
			}
			
		} catch (Exception e) {
			System.err.println("[" + COORDINATOR_NAME + "] Error updating frame");
			e.printStackTrace();
		}
	}
	
	/**
	 * Pauses the game loop
	 * All engine updates stop
	 */
	public static void pause() {
		if (!isPaused && isRunning) {
			GameLoopAdapter.delegateGamePause();
			isPaused = true;
			System.out.println("[" + COORDINATOR_NAME + "] Game paused at frame " + frameCount);
		}
	}
	
	/**
	 * Resumes the game loop
	 * Engine updates resume
	 */
	public static void resume() {
		if (isPaused && isRunning) {
			GameLoopAdapter.delegateGameResume();
			isPaused = false;
			lastAutosaveTime = System.currentTimeMillis(); // Reset timers
			System.out.println("[" + COORDINATOR_NAME + "] Game resumed at frame " + frameCount);
		}
	}
	
	/**
	 * Toggles pause state
	 */
	public static void togglePause() {
		if (isPaused) {
			resume();
		} else {
			pause();
		}
	}
	
	/**
	 * Triggers manual save
	 * 
	 * @param saveName Name for this save
	 * @return true if save successful, false otherwise
	 */
	public static boolean save(String saveName) {
		return GameLoopAdapter.delegateGameSave(saveName);
	}
	
	/**
	 * Triggers manual load
	 * 
	 * @param saveName Name of save to load
	 * @return true if load successful, false otherwise
	 */
	public static boolean load(String saveName) {
		return GameLoopAdapter.delegateGameLoad(saveName);
	}
	
	/**
	 * Starts a new game
	 * 
	 * @return true if new game started, false otherwise
	 */
	public static boolean newGame() {
		return GameLoopAdapter.delegateNewGame();
	}
	
	/**
	 * Shuts down the game loop gracefully
	 * Saves state and stops all engines
	 */
	public static void shutdown() {
		if (isRunning) {
			GameLoopAdapter.delegateGameShutdown();
			isRunning = false;
			isPaused = false;
			
			System.out.println("[" + COORDINATOR_NAME + "] Game loop shutdown complete");
			System.out.println("[" + COORDINATOR_NAME + "] Total frames: " + frameCount);
			System.out.println("[" + COORDINATOR_NAME + "] Total time: " + (elapsedTime / 1000.0f) + "s");
		}
	}
	
	/**
	 * Checks if game loop is running
	 * 
	 * @return true if running, false otherwise
	 */
	public static boolean isRunning() {
		return isRunning;
	}
	
	/**
	 * Checks if game is paused
	 * 
	 * @return true if paused, false otherwise
	 */
	public static boolean isPaused() {
		return isPaused;
	}
	
	/**
	 * Gets current frame number
	 * 
	 * @return Frame count since start
	 */
	public static long getFrameCount() {
		return frameCount;
	}
	
	/**
	 * Gets elapsed time since start
	 * 
	 * @return Time in seconds
	 */
	public static float getElapsedTime() {
		return elapsedTime / 1000.0f;
	}
	
	/**
	 * Gets target frame time
	 * 
	 * @return Time per frame in milliseconds
	 */
	public static long getTargetFrameTime() {
		return TARGET_FRAME_TIME_MS;
	}
	
	/**
	 * Gets target FPS
	 * 
	 * @return Target frames per second
	 */
	public static int getTargetFPS() {
		return TARGET_FPS;
	}
	
	/**
	 * Gets coordinator status for debugging
	 * 
	 * @return Status string
	 */
	public static String getStatus() {
		return "[" + COORDINATOR_NAME + "] " +
			"Status: " + (isRunning ? (isPaused ? "PAUSED" : "RUNNING") : "STOPPED") + ", " +
			"Frame: " + frameCount + ", " +
			"Elapsed: " + String.format("%.2f", getElapsedTime()) + "s, " +
			"Next Autosave: " + (AUTOSAVE_INTERVAL - (System.currentTimeMillis() - lastAutosaveTime) / 1000) + "s";
	}
}
