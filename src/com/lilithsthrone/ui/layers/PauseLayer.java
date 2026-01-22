package com.lilithsthrone.ui.layers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.components.UIButton;
import com.lilithsthrone.ui.components.UIPanel;
import com.lilithsthrone.ui.components.UIText;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Pause Layer - Renders pause menu overlay.
 * 
 * Displays:
 * - Pause menu panel with semi-transparent background
 * - Resume button (return to game)
 * - Save button (open save screen)
 * - Settings button (game settings)
 * - Main Menu button (return to menu)
 * - Exit button (quit game)
 * 
 * Overlay priority: Renders on top of all other layers
 * Input priority: Highest - consumes input when visible
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class PauseLayer extends UILayer {
	
	private static final String LAYER_NAME = "PauseLayer";
	
	private UIPanel pausePanel;
	private UIText pauseTitle;
	private UIButton resumeButton;
	private UIButton saveButton;
	private UIButton settingsButton;
	private UIButton mainMenuButton;
	private UIButton exitButton;
	
	private BitmapFont pauseFont;
	private BitmapFont buttonFont;
	
	private float screenWidth = 1200;
	private float screenHeight = 800;
	
	// Callbacks for parent screen
	private Runnable onResumeCallback;
	private Runnable onSaveCallback;
	private Runnable onSettingsCallback;
	private Runnable onMainMenuCallback;
	private Runnable onExitCallback;
	
	public PauseLayer(LogicLayerAPI logicApi) {
		super(logicApi);
		this.priority = 10;  // Highest priority - renders on top
		initializeComponents();
	}
	
	/**
	 * Initialize pause menu components
	 */
	private void initializeComponents() {
		pauseFont = new BitmapFont();
		buttonFont = new BitmapFont();
		
		// Semi-transparent dark overlay panel
		pausePanel = new UIPanel(200, 150, 800, 500);
		pausePanel.setBackgroundColor(new Color(0.05f, 0.05f, 0.08f, 0.85f));
		pausePanel.setBorderColor(new Color(0.8f, 0.6f, 0.2f, 1f));
		pausePanel.setBorderThickness(3);
		
		// "PAUSED" title
		pauseTitle = new UIText(220, 600, 760, 50, "PAUSED", pauseFont);
		pauseTitle.setColor(new Color(1f, 0.8f, 0.2f, 1f));
		pausePanel.add(pauseTitle);
		
		// Buttons arranged vertically
		float buttonWidth = 300;
		float buttonHeight = 50;
		float centerX = (800 - buttonWidth) / 2 + 200;  // Account for panel position
		float startY = 500;
		float spacing = 70;
		
		// Resume button
		resumeButton = new UIButton(centerX, startY, buttonWidth, buttonHeight, "Resume", buttonFont);
		resumeButton.onClick(() -> {
			if (onResumeCallback != null) {
				onResumeCallback.run();
			}
		});
		pausePanel.add(resumeButton);
		
		// Save button
		saveButton = new UIButton(centerX, startY - spacing, buttonWidth, buttonHeight, "Save Game", buttonFont);
		saveButton.onClick(() -> {
			if (onSaveCallback != null) {
				onSaveCallback.run();
			}
		});
		pausePanel.add(saveButton);
		
		// Settings button
		settingsButton = new UIButton(centerX, startY - spacing * 2, buttonWidth, buttonHeight, "Settings", buttonFont);
		settingsButton.onClick(() -> {
			if (onSettingsCallback != null) {
				onSettingsCallback.run();
			}
		});
		pausePanel.add(settingsButton);
		
		// Main Menu button
		mainMenuButton = new UIButton(centerX, startY - spacing * 3, buttonWidth, buttonHeight, "Main Menu", buttonFont);
		mainMenuButton.onClick(() -> {
			if (onMainMenuCallback != null) {
				onMainMenuCallback.run();
			}
		});
		pausePanel.add(mainMenuButton);
		
		// Exit button
		exitButton = new UIButton(centerX, startY - spacing * 4, buttonWidth, buttonHeight, "Exit Game", buttonFont);
		exitButton.onClick(() -> {
			if (onExitCallback != null) {
				onExitCallback.run();
			}
		});
		pausePanel.add(exitButton);
	}
	
	@Override
	public void update(float delta) {
		if (!visible) {
			return;
		}
		
		// Update pause panel (buttons handle their own state)
		pausePanel.update(delta);
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible) {
			return;
		}
		
		// Render pause panel with all buttons
		pausePanel.render(batch);
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!visible || !enabled) {
			return;
		}
		
		// Consume all input when pause menu is open
		pausePanel.onInput(event);
		event.consume();
	}
	
	/**
	 * Show pause menu
	 */
	public void show() {
		setVisible(true);
		enabled = true;
	}
	
	/**
	 * Hide pause menu
	 */
	public void hide() {
		setVisible(false);
		enabled = false;
	}
	
	/**
	 * Check if pause menu is visible
	 */
	public boolean isPauseMenuVisible() {
		return visible;
	}
	
	// ==================== Callbacks ====================
	
	/**
	 * Set callback for resume button
	 */
	public void setOnResumeCallback(Runnable callback) {
		this.onResumeCallback = callback;
	}
	
	/**
	 * Set callback for save button
	 */
	public void setOnSaveCallback(Runnable callback) {
		this.onSaveCallback = callback;
	}
	
	/**
	 * Set callback for settings button
	 */
	public void setOnSettingsCallback(Runnable callback) {
		this.onSettingsCallback = callback;
	}
	
	/**
	 * Set callback for main menu button
	 */
	public void setOnMainMenuCallback(Runnable callback) {
		this.onMainMenuCallback = callback;
	}
	
	/**
	 * Set callback for exit button
	 */
	public void setOnExitCallback(Runnable callback) {
		this.onExitCallback = callback;
	}
	
	@Override
	public void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
		
		// Recenter pause panel
		float panelWidth = 800;
		float panelHeight = 500;
		pausePanel.setPosition((width - panelWidth) / 2, (height - panelHeight) / 2);
		pausePanel.setSize(panelWidth, panelHeight);
	}
	
	@Override
	public void dispose() {
		if (pauseFont != null) pauseFont.dispose();
		if (buttonFont != null) buttonFont.dispose();
	}
}
