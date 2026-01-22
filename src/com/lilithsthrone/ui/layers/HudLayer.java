package com.lilithsthrone.ui.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.components.UIProgressBar;
import com.lilithsthrone.ui.components.UIText;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * HUD Layer - Renders status information on screen.
 * 
 * Displays:
 * - Player health/mana/stamina bars
 * - Current location name
 * - Current time
 * - Quick item slots
 * - Buff/effect icons
 * 
 * Replaces JavaFX-based HUD rendering.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class HudLayer extends UILayer {
	
	private static final String LAYER_NAME = "HudLayer";
	
	private UIProgressBar healthBar;
	private UIProgressBar manaBar;
	private UIProgressBar staminaBar;
	
	private UIText locationText;
	private UIText timeText;
	private UIText levelText;
	
	private BitmapFont hudFont;
	
	private float screenWidth = 800;
	private float screenHeight = 600;
	
	public HudLayer(LogicLayerAPI logicApi) {
		super(logicApi);
		initializeComponents();
	}
	
	/**
	 * Initialize HUD components
	 */
	private void initializeComponents() {
		hudFont = new BitmapFont();
		
		// Health bar (top-left)
		healthBar = new UIProgressBar(10, screenHeight - 50, 250, 20, hudFont);
		healthBar.setMaxValue(100);
		healthBar.setValue(100);
		healthBar.setFillColor(new Color(1f, 0f, 0f, 1f));  // Red
		healthBar.setBackgroundColor(new Color(0.2f, 0f, 0f, 1f));
		healthBar.setShowText(true);
		healthBar.setCustomLabel("Health");
		
		// Mana bar (top-left, below health)
		manaBar = new UIProgressBar(10, screenHeight - 80, 250, 20, hudFont);
		manaBar.setMaxValue(100);
		manaBar.setValue(75);
		manaBar.setFillColor(new Color(0f, 0.5f, 1f, 1f));  // Blue
		manaBar.setBackgroundColor(new Color(0f, 0.1f, 0.2f, 1f));
		manaBar.setShowText(true);
		manaBar.setCustomLabel("Mana");
		
		// Stamina bar (top-left, below mana)
		staminaBar = new UIProgressBar(10, screenHeight - 110, 250, 20, hudFont);
		staminaBar.setMaxValue(100);
		staminaBar.setValue(60);
		staminaBar.setFillColor(new Color(0f, 1f, 0f, 1f));  // Green
		staminaBar.setBackgroundColor(new Color(0f, 0.2f, 0f, 1f));
		staminaBar.setShowText(true);
		staminaBar.setCustomLabel("Stamina");
		
		// Location text (top-center)
		locationText = new UIText(300, screenHeight - 50, 200, 30, "Starting Area", hudFont);
		locationText.setColor(new Color(1f, 1f, 0.5f, 1f));
		
		// Time text (top-right)
		timeText = new UIText(screenWidth - 110, screenHeight - 50, 100, 30, "9:00 AM", hudFont);
		timeText.setColor(new Color(1f, 1f, 0.5f, 1f));
		
		// Level text (top-right, below time)
		levelText = new UIText(screenWidth - 110, screenHeight - 80, 100, 30, "Lvl 1", hudFont);
		levelText.setColor(new Color(0.5f, 1f, 1f, 1f));
	}
	
	@Override
	public void update(float delta) {
		if (!visible) {
			return;
		}
		
		// Query game state from LogicLayerAPI and update HUD
		try {
			// Update player stats from logic layer
			int currentHealth = logicApi.getPlayerHealth();
			int maxHealth = logicApi.getPlayerMaxHealth();
			float healthPercent = (float) currentHealth / Math.max(1, maxHealth);
			healthBar.setProgress(healthPercent);
			
			int currentMana = logicApi.getPlayerMana();
			int maxMana = logicApi.getPlayerMaxMana();
			float manaPercent = (float) currentMana / Math.max(1, maxMana);
			manaBar.setProgress(manaPercent);
			
			int currentStamina = logicApi.getPlayerStamina();
			int maxStamina = logicApi.getPlayerMaxStamina();
			float staminaPercent = (float) currentStamina / Math.max(1, maxStamina);
			staminaBar.setProgress(staminaPercent);
			
			// Update location and level
			String location = logicApi.getPlayerLocation();
			if (location != null) {
				locationText.setText(location);
			}
			
			int level = logicApi.getPlayerLevel();
			levelText.setText("Lvl " + level);
			
		} catch (Exception e) {
			// If state not available, keep default values
			// This can happen during initialization or state loading
		}
		
		// Update components
		healthBar.update(delta);
		manaBar.update(delta);
		staminaBar.update(delta);
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible) {
			return;
		}
		
		// Render status bars
		healthBar.render(batch);
		manaBar.render(batch);
		staminaBar.render(batch);
		
		// Render text information
		locationText.render(batch);
		timeText.render(batch);
		levelText.render(batch);
		
		// TODO: Render buff/effect icons
		// TODO: Render quick item slots
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!enabled) {
			return;
		}
		
		// Handle clicks on HUD elements
		healthBar.onInput(event);
		manaBar.onInput(event);
		staminaBar.onInput(event);
		
		// TODO: Handle quick slot clicks
	}
	
	/**
	 * Set player health value
	 */
	public void setHealth(float current, float max) {
		if (healthBar != null) {
			healthBar.setMaxValue(max);
			healthBar.setValue(current);
		}
	}
	
	/**
	 * Set player mana value
	 */
	public void setMana(float current, float max) {
		if (manaBar != null) {
			manaBar.setMaxValue(max);
			manaBar.setValue(current);
		}
	}
	
	/**
	 * Set player stamina value
	 */
	public void setStamina(float current, float max) {
		if (staminaBar != null) {
			staminaBar.setMaxValue(max);
			staminaBar.setValue(current);
		}
	}
	
	/**
	 * Set location text
	 */
	public void setLocation(String location) {
		if (locationText != null) {
			locationText.setText(location);
		}
	}
	
	/**
	 * Set time text
	 */
	public void setTime(String time) {
		if (timeText != null) {
			timeText.setText(time);
		}
	}
	
	/**
	 * Set level text
	 */
	public void setLevel(int level) {
		if (levelText != null) {
			levelText.setText("Lvl " + level);
		}
	}
	
	@Override
	public void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
		
		// Reposition elements for new screen size
		if (healthBar != null) healthBar.setPosition(10, screenHeight - 50);
		if (manaBar != null) manaBar.setPosition(10, screenHeight - 80);
		if (staminaBar != null) staminaBar.setPosition(10, screenHeight - 110);
		if (locationText != null) locationText.setPosition((screenWidth - 200) / 2, screenHeight - 50);
		if (timeText != null) timeText.setPosition(screenWidth - 110, screenHeight - 50);
		if (levelText != null) levelText.setPosition(screenWidth - 110, screenHeight - 80);
	}
	
	@Override
	public void dispose() {
		if (hudFont != null) {
			hudFont.dispose();
		}
	}
}
