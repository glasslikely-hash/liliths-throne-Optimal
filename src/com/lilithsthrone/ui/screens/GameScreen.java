package com.lilithsthrone.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.BaseScreen;
import com.lilithsthrone.ui.LibGdxUIManager;
import com.lilithsthrone.ui.input.InputEvent;
import com.lilithsthrone.utils.logging.LogManager;

/**
 * Main game screen - handles all gameplay rendering and logic loop.
 * 
 * Replaces the old WebView-based Game rendering with LibGdxUIManager text rendering.
 * 
 * Responsibilities:
 * - Update game state each frame via Game.java
 * - Render game content via LibGdxUIManager (text-based rendering)
 * - Handle mouse input and route clicks to LibGdxUIManager
 * - Manage pause menu overlay
 * 
 * Architecture:
 *  Player Click → InputManager → GameScreen.mousePressed() 
 *  → LibGdxUIManager.mousePressed() → Game.java state change
 *  → Game calls setContent() → LibGdxUIManager re-renders
 * 
 * @since 0.4.11.3
 * @version 2.0 (Refactored to text-based UI)
 * @author Refactoring Agent
 */
public class GameScreen extends BaseScreen {
	
	private static final String SCREEN_NAME = "GameScreen";
	
	private boolean isPaused;
	private float deltaAccumulator;
	
	// UI Manager for text-based rendering
	private LibGdxUIManager uiManager;
	private BitmapFont font;
	private ShapeRenderer shapeRenderer;
	
	/**
	 * Constructor
	 */
	public GameScreen(SpriteBatch batch, OrthographicCamera camera, LogicLayerAPI logicLayerAPI) {
		super(batch, camera, logicLayerAPI);
		this.isPaused = false;
		this.deltaAccumulator = 0f;
	}
	
	@Override
	public void show() {
		LogManager.info(SCREEN_NAME, "Game screen shown");
		isPaused = false;
		
		// Initialize rendering resources
		try {
			// Create bitmap font (use default Gdx font for now, can be replaced with custom font later)
			font = new BitmapFont();
			font.getData().setScale(0.75f);  // Scale down for readability
			
			// Create shape renderer for drawing UI elements (status bars, borders)
			shapeRenderer = new ShapeRenderer();
			
			// Initialize UI manager with rendering resources
			uiManager = new LibGdxUIManager(batch, font, shapeRenderer, camera);
			uiManager.setScreenSize((int)camera.viewportWidth, (int)camera.viewportHeight);
			
			LogManager.info(SCREEN_NAME, "UI Manager initialized successfully");
		} catch (Exception e) {
			LogManager.error(SCREEN_NAME, "Failed to initialize UI Manager: " + e.getMessage());
			e.printStackTrace();
			throw new RuntimeException("Cannot start game without UI manager", e);
		}
		
		// Request initial game content from Game.java
		try {
			// This should trigger Game.render() which will call setContent()
			logicLayerAPI.renderGame();
		} catch (Exception e) {
			LogManager.error(SCREEN_NAME, "Failed to render initial game content: " + e.getMessage());
		}
	}
	
	@Override
	public void hide() {
		LogManager.info(SCREEN_NAME, "Game screen hidden");
		disposeResources();
	}
	
	/**
	 * Update game logic each frame
	 */
	@Override
	public void update(float delta) {
		if (isPaused) {
			return;
		}
		
		deltaAccumulator += delta;
		
		// Fixed timestep for game logic (60 FPS)
		float fixedTimeStep = 1f / 60f;
		while (deltaAccumulator >= fixedTimeStep) {
			updateGameLogic(fixedTimeStep);
			deltaAccumulator -= fixedTimeStep;
		}
		
		// Handle input
		inputManager.update(delta);
		InputEvent inputEvent = inputManager.getLastInputEvent();
		
		// Process mouse input
		if (inputEvent != null && inputEvent.type == InputEvent.InputType.MOUSE_BUTTON) {
			// Convert screen coordinates to world coordinates if needed
			float x = inputEvent.x;
			float y = inputEvent.y;
			
			// Route click through UI manager
			uiManager.mousePressed(x, y);
			inputEvent.consume();  // Mark as consumed
		}
		
		// Update UI manager (for rendering)
		if (uiManager != null) {
			uiManager.update(delta);
		}
	}
	
	/**
	 * Update core game logic
	 */
	private void updateGameLogic(float delta) {
		// Query game state from logic layer API
		// Update world, NPCs, effects, etc.
		// Process player input actions
		// Handle state transitions
	}
	
	/**
	 * Render game screen
	 */
	@Override
	public void render(SpriteBatch batch) {
		// Render game UI via UI manager
		if (uiManager != null) {
			uiManager.render();
		}
	}
	
	@Override
	public void resize(int width, int height) {
		LogManager.info(SCREEN_NAME, "Resized to " + width + "x" + height);
		
		// Notify UI manager of resize
		if (uiManager != null) {
			uiManager.setScreenSize(width, height);
		}
	}
	
	@Override
	public void dispose() {
		LogManager.info(SCREEN_NAME, "Disposing game screen");
		disposeResources();
	}
	
	/**
	 * Dispose all resources
	 */
	private void disposeResources() {
		if (uiManager != null) {
			// UIManager doesn't own font/shapeRenderer, so don't dispose them
			uiManager = null;
		}
		
		if (shapeRenderer != null) {
			shapeRenderer.dispose();
			shapeRenderer = null;
		}
		
		if (font != null) {
			font.dispose();
			font = null;
		}
	}
	
	/**
	 * Pause the game
	 */
	public void pause() {
		isPaused = true;
		LogManager.info(SCREEN_NAME, "Game paused");
	}
	
	/**
	 * Resume the game
	 */
	public void resume() {
		isPaused = false;
		LogManager.info(SCREEN_NAME, "Game resumed");
	}
	
	/**
	 * Check if game is paused
	 */
	public boolean isPaused() {
		return isPaused;
	}
}
