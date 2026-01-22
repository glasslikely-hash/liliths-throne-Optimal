package com.lilithsthrone.ui.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.BaseScreen;
import com.lilithsthrone.ui.ScreenManager;
import com.lilithsthrone.ui.components.UIButton;
import com.lilithsthrone.ui.components.UIPanel;
import com.lilithsthrone.ui.components.UIText;
import com.lilithsthrone.utils.logging.LogManager;

/**
 * Main Menu Screen - Title screen with New Game, Load Game, Settings, Quit.
 * 
 * Replaces JavaFX-based main menu.
 * 
 * Features:
 * - Game title display
 * - Navigation buttons (New Game, Continue, Settings, Credits, Exit)
 * - Responsive layout with UIComponents
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class MainMenuScreen extends BaseScreen {
	
	private static final String SCREEN_NAME = "MainMenuScreen";
	private ScreenManager screenManager;
	
	private UIPanel mainPanel;
	private UIText titleText;
	private UIButton newGameButton;
	private UIButton continueButton;
	private UIButton saveButton;
	private UIButton loadButton;
	private UIButton settingsButton;
	private UIButton creditsButton;
	private UIButton exitButton;
	
	private BitmapFont titleFont;
	private BitmapFont buttonFont;
	
	public MainMenuScreen(SpriteBatch batch, OrthographicCamera camera, LogicLayerAPI logicLayerAPI, ScreenManager screenManager) {
		super(batch, camera, logicLayerAPI);
		this.screenManager = screenManager;
	}
	
	@Override
	public void show() {
		LogManager.info(SCREEN_NAME, "Main menu shown");
		
		// Create fonts (TODO: load from asset manager)
		titleFont = new BitmapFont();
		buttonFont = new BitmapFont();
		
		// Create main panel background
		mainPanel = new UIPanel(0, 0, 800, 600);
		mainPanel.setBackgroundColor(new Color(0.1f, 0.1f, 0.15f, 1f));
		
		// Create title
		titleText = new UIText(50, 500, 700, 50, "Lilith's Throne", titleFont);
		titleText.setColor(new Color(1f, 0.8f, 0.3f, 1f));
		mainPanel.add(titleText);
		
		// Create buttons - centered vertically, stacked
		float buttonWidth = 300;
		float buttonHeight = 50;
		float centerX = (800 - buttonWidth) / 2;
		float startY = 400;
		float spacing = 60;
		
		newGameButton = new UIButton(centerX, startY, buttonWidth, buttonHeight, "New Game", buttonFont);
		newGameButton.onClick(() -> onNewGamePressed());
		mainPanel.add(newGameButton);
		
		continueButton = new UIButton(centerX, startY - spacing, buttonWidth, buttonHeight, "Continue", buttonFont);
		continueButton.onClick(() -> onContinuePressed());
		mainPanel.add(continueButton);
		
		saveButton = new UIButton(centerX, startY - spacing * 2, buttonWidth, buttonHeight, "Save", buttonFont);
		saveButton.onClick(() -> onSavePressed());
		mainPanel.add(saveButton);
		
		loadButton = new UIButton(centerX, startY - spacing * 3, buttonWidth, buttonHeight, "Load", buttonFont);
		loadButton.onClick(() -> onLoadPressed());
		mainPanel.add(loadButton);
		
		settingsButton = new UIButton(centerX, startY - spacing * 4, buttonWidth, buttonHeight, "Settings", buttonFont);
		settingsButton.onClick(() -> onSettingsPressed());
		mainPanel.add(settingsButton);
		
		creditsButton = new UIButton(centerX, startY - spacing * 5, buttonWidth, buttonHeight, "Credits", buttonFont);
		creditsButton.onClick(() -> onCreditsPressed());
		mainPanel.add(creditsButton);
		
		exitButton = new UIButton(centerX, startY - spacing * 6, buttonWidth, buttonHeight, "Exit", buttonFont);
		exitButton.onClick(() -> onExitPressed());
		mainPanel.add(exitButton);
	}
	
	@Override
	public void hide() {
		LogManager.info(SCREEN_NAME, "Main menu hidden");
		dispose();
	}
	
	@Override
	public void update(float delta) {
		inputManager.update(delta);
		
		// Update all UI components
		mainPanel.update(delta);
		
		// Delegate input to UI components
		mainPanel.onInput(inputManager.getLastInputEvent());
	}
	
	@Override
	public void render(SpriteBatch batch) {
		batch.begin();
		mainPanel.render(batch);
		batch.end();
	}
	
	@Override
	public void resize(int width, int height) {
		LogManager.info(SCREEN_NAME, "Resized to " + width + "x" + height);
		// Reposition UI components for new screen size
		mainPanel.setSize(width, height);
	}
	
	@Override
	public void dispose() {
		if (titleFont != null) titleFont.dispose();
		if (buttonFont != null) buttonFont.dispose();
	}
	
	/**
	 * New Game button pressed
	 */
	private void onNewGamePressed() {
		LogManager.info(SCREEN_NAME, "New Game pressed");
		// Initialize new game state via LogicLayerAPI
		if (screenManager != null) {
			screenManager.setScreen("game");
		}
	}
	
	/**
	 * Continue button pressed
	 */
	private void onContinuePressed() {
		LogManager.info(SCREEN_NAME, "Continue pressed");
		// Load last save game via LogicLayerAPI
		if (screenManager != null) {
			screenManager.setScreen("game");
		}
	}
	
	/**
	 * Settings button pressed
	 */
	private void onSettingsPressed() {
		LogManager.info(SCREEN_NAME, "Settings pressed");
		if (screenManager != null) {
			screenManager.setScreen("settings");
		}
	}
	
	/**
	 * Save button pressed
	 */
	private void onSavePressed() {
		LogManager.info(SCREEN_NAME, "Save pressed");
		if (screenManager != null) {
			screenManager.setScreen("save");
		}
	}
	
	/**
	 * Load button pressed
	 */
	private void onLoadPressed() {
		LogManager.info(SCREEN_NAME, "Load pressed");
		if (screenManager != null) {
			screenManager.setScreen("load");
		}
	}
	
	/**
	 * Credits button pressed
	 */
	private void onCreditsPressed() {
		LogManager.info(SCREEN_NAME, "Credits pressed");
		if (screenManager != null) {
			screenManager.setScreen("credits");
		}
	}
	
	/**
	 * Exit button pressed
	 */
	private void onExitPressed() {
		LogManager.info(SCREEN_NAME, "Exit pressed");
		// Use Gdx.app.exit() instead of System.exit() for cross-platform compatibility
		// Works on desktop (LWJGL) and Android
		com.badlogic.gdx.Gdx.app.exit();
	}
}
