package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Main UI Controller
 * 
 * Coordinates all UI subsystems and manages the UI layer hierarchy.
 * 
 * Responsibilities:
 *  - Manage all specialized UI controllers
 *  - Handle inter-controller communication
 *  - Coordinate controller lifecycle (initialize, update, render, dispose)
 *  - Route input to active controller
 *  - Manage controller visibility and activity
 *  - Update timing and metrics
 * 
 * Controller Hierarchy:
 *  MainUIController (root)
 *    - GameplayUIController (HUD during gameplay)
 *    - CombatUIController (combat mode)
 *    - DialogueUIController (dialogue mode)
 *    - InventoryUIController (inventory screen)
 *    - StatusPanelController (character stats overlay)
 *    - EventLogController (event history)
 *    - MapUIController (world map)
 * 
 * @since Step 3
 * @version 1.0
 */
public class MainUIController {

	private static final String CONTROLLER_NAME = "MainUIController";

	// Rendering resources
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private LogicLayerAPI logicLayerAPI;
	private InputManager inputManager;

	// All UI controllers
	private Map<String, UIControllerBase> controllers = new HashMap<>();
	private UIControllerBase activeController = null;

	// Performance metrics
	private float fps = 0f;
	private float frameTime = 0f;
	private int frameCount = 0;
	private float fpsUpdateTimer = 0f;
	private static final float FPS_UPDATE_INTERVAL = 1.0f;

	// State
	private boolean isInitialized = false;
	private boolean isPaused = false;

	/**
	 * Constructor for main UI controller
	 * 
	 * @param batch SpriteBatch for rendering
	 * @param camera Camera for transformations
	 * @param logicLayerAPI Logic layer API for game queries
	 * @param inputManager Input manager for user input
	 */
	public MainUIController(SpriteBatch batch, OrthographicCamera camera,
	                         LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		this.batch = batch;
		this.camera = camera;
		this.logicLayerAPI = logicLayerAPI;
		this.inputManager = inputManager;
	}

	/**
	 * Initialize the UI controller system
	 * Called once at application startup
	 */
	public void initialize() {
		System.out.println("[" + CONTROLLER_NAME + "] Initializing main UI controller");

		// Create all UI controllers
		createControllers();

		// Initialize all controllers
		for (UIControllerBase controller : controllers.values()) {
			try {
				controller.initialize();
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error initializing " + controller.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		// Set initial active controller (gameplay UI)
		setActiveController("GAMEPLAY");

		isInitialized = true;
		System.out.println("[" + CONTROLLER_NAME + "] Initialized with " + controllers.size() + " controllers");
	}

	/**
	 * Create all UI controllers
	 */
	private void createControllers() {
		// Gameplay HUD
		controllers.put("GAMEPLAY", new GameplayUIController(batch, camera, logicLayerAPI, inputManager));

		// Combat UI
		controllers.put("COMBAT", new CombatUIController(batch, camera, logicLayerAPI, inputManager));

		// Dialogue UI
		controllers.put("DIALOGUE", new DialogueUIController(batch, camera, logicLayerAPI, inputManager));

		// Inventory UI
		controllers.put("INVENTORY", new InventoryUIController(batch, camera, logicLayerAPI, inputManager));

		// Status Panel (overlay)
		controllers.put("STATUS", new StatusPanelController(batch, camera, logicLayerAPI, inputManager));

		// Event Log (overlay)
		controllers.put("LOG", new EventLogController(batch, camera, logicLayerAPI, inputManager));

		// Map UI
		controllers.put("MAP", new MapUIController(batch, camera, logicLayerAPI, inputManager));

		System.out.println("[" + CONTROLLER_NAME + "] Created " + controllers.size() + " UI controllers");
	}

	/**
	 * Update the UI controller system
	 * Called once per frame
	 * 
	 * @param deltaTime Time since last frame in seconds
	 */
	public void update(float deltaTime) {
		if (!isInitialized || isPaused) {
			return;
		}

		// Update all active controllers
		for (UIControllerBase controller : controllers.values()) {
			if (controller.isActive() && controller.isVisible()) {
				try {
					controller.update(deltaTime);
				} catch (Exception e) {
					System.err.println("[" + CONTROLLER_NAME + "] Error updating " + controller.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}

		// Update FPS metrics
		updateFPSMetrics(deltaTime);
	}

	/**
	 * Render the UI layer
	 * Called once per frame after update
	 * 
	 * @param deltaTime Time since last frame in seconds
	 */
	public void render(float deltaTime) {
		if (!isInitialized) {
			return;
		}

		// Render all active controllers
		for (UIControllerBase controller : controllers.values()) {
			if (controller.isVisible()) {
				try {
					controller.render(deltaTime);
				} catch (Exception e) {
					System.err.println("[" + CONTROLLER_NAME + "] Error rendering " + controller.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Handle input
	 * Routes input to active controller
	 * 
	 * @return true if input was handled
	 */
	public boolean handleInput() {
		if (!isInitialized) {
			return false;
		}

		// Route input to active controller first
		if (activeController != null && activeController.isActive()) {
			try {
				if (activeController.handleInput()) {
					return true;
				}
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error handling input in " + activeController.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		// Then try overlay controllers (status, log)
		for (String name : new String[]{ "STATUS", "LOG" }) {
			UIControllerBase controller = controllers.get(name);
			if (controller != null && controller.isActive() && controller.isVisible()) {
				try {
					if (controller.handleInput()) {
						return true;
					}
				} catch (Exception e) {
					System.err.println("[" + CONTROLLER_NAME + "] Error handling input in " + controller.getClass().getSimpleName() + ": " + e.getMessage());
				}
			}
		}

		return false;
	}

	/**
	 * Set the active UI controller
	 * Only one main controller can be active at a time
	 * 
	 * @param controllerName Name of controller to activate
	 */
	public void setActiveController(String controllerName) {
		UIControllerBase controller = controllers.get(controllerName);
		if (controller == null) {
			System.err.println("[" + CONTROLLER_NAME + "] Controller not found: " + controllerName);
			return;
		}

		// Deactivate current controller
		if (activeController != null) {
			activeController.hide();
		}

		// Activate new controller
		activeController = controller;
		controller.show();

		System.out.println("[" + CONTROLLER_NAME + "] Switched to controller: " + controllerName);
	}

	/**
	 * Show an overlay controller (in addition to active controller)
	 * 
	 * @param controllerName Name of overlay controller
	 * @param visible true to show, false to hide
	 */
	public void setOverlayVisible(String controllerName, boolean visible) {
		UIControllerBase controller = controllers.get(controllerName);
		if (controller == null) {
			System.err.println("[" + CONTROLLER_NAME + "] Overlay controller not found: " + controllerName);
			return;
		}

		if (visible) {
			controller.show();
		} else {
			controller.hide();
		}
	}

	/**
	 * Pause the UI (no updates or input)
	 */
	public void pause() {
		isPaused = true;
		System.out.println("[" + CONTROLLER_NAME + "] UI paused");
	}

	/**
	 * Resume the UI
	 */
	public void resume() {
		isPaused = false;
		System.out.println("[" + CONTROLLER_NAME + "] UI resumed");
	}

	/**
	 * Handle screen resize
	 * 
	 * @param width New screen width
	 * @param height New screen height
	 */
	public void resize(int width, int height) {
		for (UIControllerBase controller : controllers.values()) {
			try {
				controller.resize(width, height);
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error resizing " + controller.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		System.out.println("[" + CONTROLLER_NAME + "] Resized to " + width + "x" + height);
	}

	/**
	 * Update FPS metrics
	 * 
	 * @param deltaTime Time since last frame
	 */
	private void updateFPSMetrics(float deltaTime) {
		frameTime = deltaTime;
		frameCount++;
		fpsUpdateTimer += deltaTime;

		if (fpsUpdateTimer >= FPS_UPDATE_INTERVAL) {
			fps = frameCount / fpsUpdateTimer;
			frameCount = 0;
			fpsUpdateTimer = 0f;
		}
	}

	/**
	 * Get current FPS
	 * 
	 * @return Frames per second
	 */
	public float getFPS() {
		return fps;
	}

	/**
	 * Get current frame time
	 * 
	 * @return Time for last frame in seconds
	 */
	public float getFrameTime() {
		return frameTime;
	}

	/**
	 * Get current active controller name
	 * 
	 * @return Name of active controller
	 */
	public String getActiveControllerName() {
		if (activeController == null) {
			return "NONE";
		}

		for (String name : controllers.keySet()) {
			if (controllers.get(name) == activeController) {
				return name;
			}
		}

		return "UNKNOWN";
	}

	/**
	 * Get controller by name
	 * 
	 * @param controllerName Name of controller
	 * @return The controller, or null if not found
	 */
	public UIControllerBase getController(String controllerName) {
		return controllers.get(controllerName);
	}

	/**
	 * Cleanup and dispose all controllers
	 */
	public void dispose() {
		System.out.println("[" + CONTROLLER_NAME + "] Disposing UI controllers");

		for (UIControllerBase controller : controllers.values()) {
			try {
				controller.dispose();
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error disposing " + controller.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

		controllers.clear();
		activeController = null;
		isInitialized = false;

		System.out.println("[" + CONTROLLER_NAME + "] Disposed");
	}

	/**
	 * Get debug status information
	 * 
	 * @return Status string
	 */
	public String getStatus() {
		return String.format("UI: %s | FPS: %.1f | Active: %s | Controllers: %d",
		                     isPaused ? "PAUSED" : "RUNNING",
		                     fps,
		                     getActiveControllerName(),
		                     controllers.size());
	}
}
