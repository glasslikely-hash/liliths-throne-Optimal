package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;

/**
 * Base class for all UI controllers
 * 
 * Provides common functionality for managing specific UI subsystems.
 * Controllers are responsible for:
 *  - State display management (converting game state to UI elements)
 *  - Input handling (user interactions)
 *  - Layout management (positioning UI elements)
 *  - Updates (model-view synchronization)
 *  - Rendering (drawing UI elements)
 * 
 * Architecture:
 *  UIControllerBase (abstract)
 *    ├── GameplayUIController (HUD, stats, inventory display)
 *    ├── CombatUIController (combat interface, move selection)
 *    ├── DialogueUIController (dialogue trees, responses)
 *    ├── InventoryUIController (item management, equipment)
 *    ├── MapUIController (world navigation, locations)
 *    ├── StatusPanelController (character stats, buffs)
 *    ├── EventLogController (event history, dialogue log)
 *    └── PauseMenuController (pause menu, settings)
 * 
 * @since Step 3
 * @version 1.0
 */
public abstract class UIControllerBase {

	protected SpriteBatch batch;
	protected OrthographicCamera camera;
	protected LogicLayerAPI logicLayerAPI;
	protected InputManager inputManager;

	// UI dimensions
	protected float screenWidth;
	protected float screenHeight;
	protected float scale = 1.0f;

	// Visibility state
	protected boolean isVisible = true;
	protected boolean isActive = true;

	/**
	 * Constructor for UI controller
	 * 
	 * @param batch SpriteBatch for rendering
	 * @param camera Camera for transformations
	 * @param logicLayerAPI Logic layer API for game state queries
	 * @param inputManager Input manager for user input
	 */
	public UIControllerBase(SpriteBatch batch, OrthographicCamera camera, 
	                         LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		this.batch = batch;
		this.camera = camera;
		this.logicLayerAPI = logicLayerAPI;
		this.inputManager = inputManager;
		this.screenWidth = camera.viewportWidth;
		this.screenHeight = camera.viewportHeight;
	}

	/**
	 * Initialize the controller
	 * Called once when screen becomes active
	 */
	public abstract void initialize();

	/**
	 * Update controller logic
	 * Called once per frame
	 * 
	 * @param deltaTime Time since last frame in seconds
	 */
	public abstract void update(float deltaTime);

	/**
	 * Render controller UI elements
	 * Called once per frame after update
	 * 
	 * @param deltaTime Time since last frame in seconds
	 */
	public abstract void render(float deltaTime);

	/**
	 * Handle input for this controller
	 * Called when user provides input
	 * 
	 * @return true if input was consumed, false to pass to next handler
	 */
	public abstract boolean handleInput();

	/**
	 * Show this controller's UI elements
	 */
	public void show() {
		this.isVisible = true;
		this.isActive = true;
	}

	/**
	 * Hide this controller's UI elements
	 */
	public void hide() {
		this.isVisible = false;
	}

	/**
	 * Enable/disable input processing
	 * 
	 * @param active true to enable, false to disable
	 */
	public void setActive(boolean active) {
		this.isActive = active;
	}

	/**
	 * Resize the UI for new viewport dimensions
	 * 
	 * @param newWidth New screen width
	 * @param newHeight New screen height
	 */
	public void resize(float newWidth, float newHeight) {
		this.screenWidth = newWidth;
		this.screenHeight = newHeight;
		onResize();
	}

	/**
	 * Called when screen is resized
	 * Subclasses should override to reposition elements
	 */
	protected abstract void onResize();

	/**
	 * Cleanup resources when controller is disposed
	 */
	public abstract void dispose();

	// Getters
	public boolean isVisible() {
		return isVisible;
	}

	public boolean isActive() {
		return isActive;
	}

	public float getScreenWidth() {
		return screenWidth;
	}

	public float getScreenHeight() {
		return screenHeight;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
}
