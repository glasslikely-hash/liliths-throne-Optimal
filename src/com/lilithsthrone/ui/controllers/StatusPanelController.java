package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;
import com.lilithsthrone.ui.utils.ColorCache;
import java.util.ArrayList;
import java.util.List;

/**
 * Status Panel Controller
 * 
 * Displays comprehensive character stats and status effects.
 * 
 * Displays:
 *  - Character attributes (STR, DEX, CON, INT, WIS, CHA)
 *  - Health, mana, stamina pools
 *  - Active buffs and debuffs
 *  - Resistances and vulnerabilities
 *  - Skill proficiencies
 *  - Character level and experience progress
 * 
 * This is typically shown as a side panel or overlay during gameplay.
 * 
 * @since Step 3
 * @version 1.0
 */
public class StatusPanelController extends UIControllerBase {

	private static final String CONTROLLER_NAME = "StatusPanelController";

	// UI elements
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private BitmapFont fontLarge;

	// Panel position and size
	private float panelX, panelY;
	private static final float PANEL_WIDTH = 280f;
	private static final float PANEL_HEIGHT = 600f;

	// Update timer for stats
	private float updateTimer = 0f;
	private static final float UPDATE_INTERVAL = 0.2f;

	// Cached stats
	private String cachedCharacterName = "";
	private int cachedLevel = 1;
	private float cachedExperience = 0f;
	private float cachedMaxExperience = 100f;

	// Attributes
	private float cachedStr = 10f, cachedDex = 10f, cachedCon = 10f;
	private float cachedInt = 10f, cachedWis = 10f, cachedCha = 10f;

	// Resources
	private float cachedHealth = 100f, cachedMaxHealth = 100f;
	private float cachedMana = 100f, cachedMaxMana = 100f;
	private float cachedStamina = 100f, cachedMaxStamina = 100f;

	// Status effects
	private List<String> cachedBuffs = new ArrayList<>();
	private List<String> cachedDebuffs = new ArrayList<>();

	/**
	 * Constructor for status panel controller
	 */
	public StatusPanelController(SpriteBatch batch, OrthographicCamera camera,
	                             LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		super(batch, camera, logicLayerAPI, inputManager);
		this.shapeRenderer = new ShapeRenderer();
		this.font = new BitmapFont();
		this.fontLarge = new BitmapFont();
	}

	@Override
	public void initialize() {
		System.out.println("[" + CONTROLLER_NAME + "] Initializing status panel");
		panelX = screenWidth - PANEL_WIDTH - 20f;
		panelY = screenHeight - PANEL_HEIGHT - 20f;
		updateCachedStats();
	}

	/**
	 * Update cached stats from game state
	 */
	private void updateCachedStats() {
		try {
			var player = logicLayerAPI.getPlayerCharacter();
			if (player != null) {
				cachedCharacterName = player.getName();
				cachedLevel = player.getLevel();
				cachedExperience = player.getExperience();
				cachedMaxExperience = player.getExperienceForNextLevel();

				// Attributes
				cachedStr = player.getStrength();
				cachedDex = player.getDexterity();
				cachedCon = player.getConstitution();
				cachedInt = player.getIntelligence();
				cachedWis = player.getWisdom();
				cachedCha = player.getCharisma();

				// Resources
				cachedHealth = player.getHealth();
				cachedMaxHealth = player.getMaxHealth();
				cachedMana = player.getMana();
				cachedMaxMana = player.getMaxMana();
				cachedStamina = player.getStamina();
				cachedMaxStamina = player.getMaxStamina();

				// Status effects
				cachedBuffs = logicLayerAPI.getPlayerBuffs();
				cachedDebuffs = logicLayerAPI.getPlayerDebuffs();
			}
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error updating stats: " + e.getMessage());
		}
	}

	@Override
	public void update(float deltaTime) {
		if (!isActive || !isVisible) {
			return;
		}

		// Update cached stats periodically
		updateTimer += deltaTime;
		if (updateTimer >= UPDATE_INTERVAL) {
			updateCachedStats();
			updateTimer = 0f;
		}
	}

	@Override
	public void render(float deltaTime) {
		if (!isVisible) {
			return;
		}

		batch.begin();

		// Render panel background
		renderPanelBackground();

		// Render character info
		renderCharacterInfo();

		// Render resources (health, mana, stamina)
		renderResources();

		// Render attributes
		renderAttributes();

		// Render status effects
		renderStatusEffects();

		batch.end();
	}

	/**
	 * Render panel background and border
	 */
	private void renderPanelBackground() {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Background
		shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
		shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

		// Border
		shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1f);
		shapeRenderer.rect(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

		shapeRenderer.end();
	}

	/**
	 * Render character name and level
	 */
	private void renderCharacterInfo() {
		float y = panelY + PANEL_HEIGHT - 30;

		fontLarge.setColor(Color.YELLOW);
		fontLarge.draw(batch, cachedCharacterName, panelX + 10, y);

		font.setColor(Color.WHITE);
		font.draw(batch, "Level: " + cachedLevel, panelX + 10, y - 25);

		// Experience bar
		float expPercent = cachedMaxExperience > 0 ? cachedExperience / cachedMaxExperience : 0f;
		expPercent = Math.min(1f, Math.max(0f, expPercent));

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(panelX + 10, y - 60, PANEL_WIDTH - 20, 15);

		shapeRenderer.setColor(0.5f, 0.8f, 0.5f, 1f);
		shapeRenderer.rect(panelX + 11, y - 59, (PANEL_WIDTH - 22) * expPercent, 13);
		shapeRenderer.end();

		font.setColor(Color.WHITE);
		font.draw(batch, String.format("EXP: %.0f/%.0f", cachedExperience, cachedMaxExperience),
		         panelX + 15, y - 50);
	}

	/**
	 * Render health, mana, stamina bars
	 */
	private void renderResources() {
		float y = panelY + PANEL_HEIGHT - 150;
		float barWidth = PANEL_WIDTH - 20;

		// Health
		renderResourceBar(y, "HP", cachedHealth, cachedMaxHealth, ColorCache.RESOURCE_HP);
		y -= 50;

		// Mana
		renderResourceBar(y, "Mana", cachedMana, cachedMaxMana, ColorCache.RESOURCE_MANA);
		y -= 50;

		// Stamina
		renderResourceBar(y, "Stamina", cachedStamina, cachedMaxStamina, ColorCache.RESOURCE_STAMINA);
	}

	/**
	 * Render a single resource bar
	 * 
	 * @param y Y position
	 * @param label Bar label
	 * @param current Current value
	 * @param max Maximum value
	 * @param color Bar color
	 */
	private void renderResourceBar(float y, String label, float current, float max, Color color) {
		float barWidth = PANEL_WIDTH - 20;
		float percent = max > 0 ? current / max : 0f;
		percent = Math.min(1f, Math.max(0f, percent));

		// Label
		font.setColor(Color.WHITE);
		font.draw(batch, label, panelX + 10, y + 5);

		// Bar background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(panelX + 10, y - 15, barWidth - 20, 15);

		// Bar fill
		shapeRenderer.setColor(color);
		shapeRenderer.rect(panelX + 11, y - 14, (barWidth - 22) * percent, 13);

		// Bar border
		shapeRenderer.setColor(1f, 1f, 1f, 1f);
		shapeRenderer.rect(panelX + 10, y - 15, barWidth - 20, 15);
		shapeRenderer.end();

		// Values
		font.setColor(Color.WHITE);
		font.draw(batch, String.format("%.0f/%.0f", current, max), panelX + barWidth - 55, y + 5);
	}

	/**
	 * Render attributes (STR, DEX, CON, INT, WIS, CHA)
	 */
	private void renderAttributes() {
		float y = panelY + 200;
		float labelX = panelX + 10;
		float valueX = panelX + 120;

		font.setColor(Color.YELLOW);
		font.draw(batch, "--- Attributes ---", labelX, y);
		y -= 25;

		// Attributes in 2 columns
		String[] labels = { "STR", "DEX", "CON", "INT", "WIS", "CHA" };
		float[] values = { cachedStr, cachedDex, cachedCon, cachedInt, cachedWis, cachedCha };

		for (int i = 0; i < labels.length; i++) {
			font.setColor(Color.WHITE);
			font.draw(batch, labels[i], labelX, y);
			font.draw(batch, String.format("%.0f", values[i]), valueX, y);

			if (i % 2 == 1) {
				y -= 20;
			}
		}
	}

	/**
	 * Render buffs and debuffs
	 */
	private void renderStatusEffects() {
		float y = panelY + 80;
		float labelX = panelX + 10;

		// Buffs
		if (!cachedBuffs.isEmpty()) {
			font.setColor(Color.LIME);
			font.draw(batch, "--- Buffs ---", labelX, y);
			y -= 20;

			for (String buff : cachedBuffs) {
				if (y < panelY + 20) break;
				font.setColor(Color.GREEN);
				font.draw(batch, "• " + buff, labelX + 5, y);
				y -= 15;
			}
		}

		// Debuffs
		if (!cachedDebuffs.isEmpty()) {
			y -= 10;
			font.setColor(Color.RED);
			font.draw(batch, "--- Debuffs ---", labelX, y);
			y -= 20;

			for (String debuff : cachedDebuffs) {
				if (y < panelY + 20) break;
				font.setColor(ColorCache.RED_LIGHT);
				font.draw(batch, "• " + debuff, labelX + 5, y);
				y -= 15;
			}
		}
	}

	@Override
	public boolean handleInput() {
		// Status panel is typically read-only, no special input handling
		return false;
	}

	@Override
	protected void onResize() {
		panelX = screenWidth - PANEL_WIDTH - 20f;
		panelY = screenHeight - PANEL_HEIGHT - 20f;
	}

	@Override
	public void dispose() {
		if (shapeRenderer != null) {
			shapeRenderer.dispose();
		}
		if (font != null) {
			font.dispose();
		}
		if (fontLarge != null) {
			fontLarge.dispose();
		}
	}
}
