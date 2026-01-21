package com.lilithsthrone.logic;

import com.lilithsthrone.main.Main;
import com.lilithsthrone.utils.logging.LogManager;
import java.util.*;

/**
 * Adapter for Game Loop ↔ Engine Coordination
 * 
 * Bridges main game loop with all mechanics engines, coordinating updates,
 * synchronization, and persistence. Manages frame timing, autosave intervals,
 * and ensures all engines stay in sync during gameplay.
 * 
 * @since Phase 2.6
 * @version 1.0
 */
public class GameLoopAdapter {
	
	private static final String ADAPTER_NAME = "GameLoopAdapter";
	
	/**
	 * Delegates game loop frame update
	 * Coordinates all engine updates for a single frame
	 * 
	 * @param deltaTime Time elapsed since last frame in seconds
	 * @param frameNumber Current frame count
	 */
	public static void delegateFrameUpdate(float deltaTime, long frameNumber) {
		if (isBridgeAvailable()) {
			try {
				// Get LogicLayerAPI and update all engines
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.update(deltaTime);
					
					if (frameNumber % 60 == 0) { // Log every 60 frames (roughly 1 second at 60 FPS)
						LogManager.logicDebug("Frame " + frameNumber + " updated (delta: " + deltaTime + "s)");
					}
				}
			} catch (Exception e) {
			LogManager.logicError("Error updating frame", e);
			}
		}
	}
	
	/**
	 * Delegates autosave trigger
	 * Initiates autosave when conditions met
	 * 
	 * @param interval Autosave interval in seconds
	 * @return true if autosave was triggered, false otherwise
	 */
	public static boolean delegateAutosave(int interval) {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					// Trigger autosave through persistence
					logicAPI.requestAutosave();
					
					logicAPI.triggerEvent("game_autosave");
					
					System.out.println("[" + ADAPTER_NAME + "] Autosave triggered (interval: " + interval + "s)");
					return true;
				}
			} catch (Exception e) {
			LogManager.logicError("Error triggering autosave", e);
			}
		}
		return false;
	}
	
	/**
	 * Delegates snapshot creation
	 * Initiates snapshot for full state backup
	 * 
	 * @return true if snapshot was created, false otherwise
	 */
	public static boolean delegateSnapshot() {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.requestSnapshot();
					
					logicAPI.triggerEvent("game_snapshot");
					
					System.out.println("[" + ADAPTER_NAME + "] Snapshot created");
					return true;
				}
			} catch (Exception e) {
			LogManager.logicError("Error creating snapshot", e);
			}
		}
		return false;
	}
	
	/**
	 * Delegates engine state synchronization
	 * Ensures all engines are in sync after major changes
	 */
	public static void delegateEngineSynchronization() {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.synchronizeEngines();
					
					logicAPI.triggerEvent("engines_synchronized");
					
					System.out.println("[" + ADAPTER_NAME + "] Engines synchronized");
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error synchronizing engines");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates game pause
	 * Pauses all engine updates
	 * 
	 * @return true if game was paused, false otherwise
	 */
	public static boolean delegateGamePause() {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.pause();
					
					logicAPI.triggerEvent("game_paused");
					
					System.out.println("[" + ADAPTER_NAME + "] Game paused");
					return true;
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error pausing game");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates game resume
	 * Resumes engine updates after pause
	 * 
	 * @return true if game was resumed, false otherwise
	 */
	public static boolean delegateGameResume() {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.resume();
					
					logicAPI.triggerEvent("game_resumed");
					
					System.out.println("[" + ADAPTER_NAME + "] Game resumed");
					return true;
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error resuming game");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates game shutdown
	 * Gracefully shuts down all engines and saves state
	 */
	public static void delegateGameShutdown() {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					// Create final snapshot before shutdown
					logicAPI.requestSnapshot();
					
					// Shutdown all engines
					logicAPI.shutdown();
					
					logicAPI.triggerEvent("game_shutdown");
					
					System.out.println("[" + ADAPTER_NAME + "] Game shutdown initiated");
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error shutting down game");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Delegates game state save
	 * Manually saves game state to file
	 * 
	 * @param saveName Name/identifier for save
	 * @return true if save was successful, false otherwise
	 */
	public static boolean delegateGameSave(String saveName) {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.saveGame(saveName);
					
					logicAPI.triggerEvent("game_saved_" + saveName);
					
					System.out.println("[" + ADAPTER_NAME + "] Game saved: " + saveName);
					return true;
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error saving game: " + saveName);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates game state load
	 * Manually loads game state from file
	 * 
	 * @param saveName Name/identifier of save to load
	 * @return true if load was successful, false otherwise
	 */
	public static boolean delegateGameLoad(String saveName) {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.loadGame(saveName);
					
					logicAPI.triggerEvent("game_loaded_" + saveName);
					
					System.out.println("[" + ADAPTER_NAME + "] Game loaded: " + saveName);
					return true;
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error loading game: " + saveName);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Delegates new game initialization
	 * Starts fresh game with new character
	 * 
	 * @return true if new game initialized, false otherwise
	 */
	public static boolean delegateNewGame() {
		if (isBridgeAvailable()) {
			try {
				LogicLayerAPI logicAPI = Main.game.getLogicLayerAPI();
				if (logicAPI != null) {
					logicAPI.newGame();
					
					logicAPI.triggerEvent("game_new_started");
					
					System.out.println("[" + ADAPTER_NAME + "] New game initialized");
					return true;
				}
			} catch (Exception e) {
				System.err.println("[" + ADAPTER_NAME + "] Error initializing new game");
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Checks if integration bridge is available
	 * Verifies GameIntegrationBridge is initialized
	 * 
	 * @return true if bridge is ready, false otherwise
	 */
	private static boolean isBridgeAvailable() {
		try {
			return GameIntegrationBridge.getInstance() != null 
				&& Main.game != null 
				&& Main.game.getLogicLayerAPI() != null;
		} catch (Exception e) {
			return false;
		}
	}
}
