package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;

/**
 * Gameplay UI Controller
 * 
 * Manages the main in-game HUD and interface elements.
 * 
 * Displays:
 *  - Player character stats (health, mana, stamina)
 *  - Equipped items and quick slots
 *  - Active buffs/debuffs
 *  - Quest objectives and markers
 *  - Interaction prompts
 *  - Compass/minimap
 *  - Time of day
 * 
 * Input:
 *  - Mouse clicks for world interactions
 *  - Hotkey presses for quick items/spells
 *  - Menu opens (inventory, character, map)
 * 
 * @since Step 3
 * @version 1.0
 */
public class GameplayUIController extends UIControllerBase {

	private static final String CONTROLLER_NAME = "GameplayUIController";

	// UI elements
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;

	// HUD positions
	private float healthBarX, healthBarY;
	private float manaBarX, manaBarY;
	private float staminaBarX, staminaBarY;
	private float quickSlotStartX, quickSlotStartY;
	private float compassX, compassY;
	private float objectiveX, objectiveY;

	// Bar dimensions
	private static final float BAR_WIDTH = 200f;
	private static final float BAR_HEIGHT = 20f;
	private static final float QUICK_SLOT_SIZE = 60f;
	private static final float QUICK_SLOT_SPACING = 10f;

	// Update timers
	private float statUpdateTimer = 0f;
	private static final float STAT_UPDATE_INTERVAL = 0.1f; // Update stats 10x per second

	// Cached state
	private float cachedHealth = 0f;
	private float cachedMaxHealth = 100f;
	private float cachedMana = 0f;
	private float cachedMaxMana = 100f;
	private float cachedStamina = 0f;
	private float cachedMaxStamina = 100f;

	/**
	 * Constructor for gameplay UI controller
	 */
	public GameplayUIController(SpriteBatch batch, OrthographicCamera camera,
	                            LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		super(batch, camera, logicLayerAPI, inputManager);
		this.shapeRenderer = new ShapeRenderer();
		
		// Load custom font from assets or fall back to default
		try {
			this.font = new BitmapFont(com.badlogic.gdx.Gdx.files.internal("res/fonts/default.fnt"));
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Failed to load custom font, using default");
			this.font = new BitmapFont();
		}
		System.out.println("[" + CONTROLLER_NAME + "] Initializing gameplay UI");
		setupLayout();
		updateCachedStats();
	}

	/**
	 * Setup initial UI layout
	 */
	private void setupLayout() {
		// HUD element positions (in screen coordinates)
		healthBarX = 20f;
		healthBarY = screenHeight - 40f;

		manaBarX = 20f;
		manaBarY = screenHeight - 70f;

		staminaBarX = 20f;
		staminaBarY = screenHeight - 100f;

		quickSlotStartX = 20f;
		quickSlotStartY = 20f;

		compassX = screenWidth - 100f;
		compassY = screenHeight - 100f;

		objectiveX = screenWidth / 2f;
		objectiveY = screenHeight - 40f;
	}

	/**
	 * Update cached stats from game state
	 */
	private void updateCachedStats() {
		try {
			GameCharacter player = logicLayerAPI.getPlayerCharacter();
			if (player != null) {
				cachedHealth = player.getHealth();
				cachedMaxHealth = player.getMaxHealth();
				cachedMana = player.getMana();
				cachedMaxMana = player.getMaxMana();
				cachedStamina = player.getStamina();
				cachedMaxStamina = player.getMaxStamina();
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

		// Update cached stats at regular intervals
		statUpdateTimer += deltaTime;
		if (statUpdateTimer >= STAT_UPDATE_INTERVAL) {
			updateCachedStats();
			statUpdateTimer = 0f;
		}
	}

	@Override
	public void render(float deltaTime) {
		if (!isVisible) {
			return;
		}

		batch.begin();

		// Render stat bars
		renderHealthBar();
		renderManaBar();
		renderStaminaBar();

		// Render quick slots
		renderQuickSlots();

		// Render objective display
		renderObjective();

		batch.end();

		// Render compass
		renderCompass();
	}

	/**
	 * Render player health bar
	 */
	private void renderHealthBar() {
		float healthPercent = cachedMaxHealth > 0 ? cachedHealth / cachedMaxHealth : 0f;
		healthPercent = Math.min(1f, Math.max(0f, healthPercent));

		// Background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(healthBarX, healthBarY, BAR_WIDTH, BAR_HEIGHT);

		// Health fill
		shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f); // Red
		shapeRenderer.rect(healthBarX + 2, healthBarY + 2, (BAR_WIDTH - 4) * healthPercent, BAR_HEIGHT - 4);

		// Border
		shapeRenderer.setColor(1f, 1f, 1f, 1f);
		shapeRenderer.rect(healthBarX, healthBarY, BAR_WIDTH, BAR_HEIGHT);
		shapeRenderer.end();

		// Text label
		font.setColor(Color.WHITE);
		font.draw(batch, String.format("HP: %.0f/%.0f", cachedHealth, cachedMaxHealth),
		         healthBarX + BAR_WIDTH + 10, healthBarY + BAR_HEIGHT);
	}

	/**
	 * Render player mana bar
	 */
	private void renderManaBar() {
		float manaPercent = cachedMaxMana > 0 ? cachedMana / cachedMaxMana : 0f;
		manaPercent = Math.min(1f, Math.max(0f, manaPercent));

		// Background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(manaBarX, manaBarY, BAR_WIDTH, BAR_HEIGHT);

		// Mana fill
		shapeRenderer.setColor(0.2f, 0.5f, 1f, 1f); // Blue
		shapeRenderer.rect(manaBarX + 2, manaBarY + 2, (BAR_WIDTH - 4) * manaPercent, BAR_HEIGHT - 4);

		// Border
		shapeRenderer.setColor(1f, 1f, 1f, 1f);
		shapeRenderer.rect(manaBarX, manaBarY, BAR_WIDTH, BAR_HEIGHT);
		shapeRenderer.end();

		// Text label
		font.setColor(Color.WHITE);
		font.draw(batch, String.format("Mana: %.0f/%.0f", cachedMana, cachedMaxMana),
		         manaBarX + BAR_WIDTH + 10, manaBarY + BAR_HEIGHT);
	}

	/**
	 * Render player stamina bar
	 */
	private void renderStaminaBar() {
		float staminaPercent = cachedMaxStamina > 0 ? cachedStamina / cachedMaxStamina : 0f;
		staminaPercent = Math.min(1f, Math.max(0f, staminaPercent));

		// Background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(staminaBarX, staminaBarY, BAR_WIDTH, BAR_HEIGHT);

		// Stamina fill
		shapeRenderer.setColor(0.2f, 1f, 0.2f, 1f); // Green
		shapeRenderer.rect(staminaBarX + 2, staminaBarY + 2, (BAR_WIDTH - 4) * staminaPercent, BAR_HEIGHT - 4);

		// Border
		shapeRenderer.setColor(1f, 1f, 1f, 1f);
		shapeRenderer.rect(staminaBarX, staminaBarY, BAR_WIDTH, BAR_HEIGHT);
		shapeRenderer.end();

		// Text label
		font.setColor(Color.WHITE);
		font.draw(batch, String.format("Stamina: %.0f/%.0f", cachedStamina, cachedMaxStamina),
		         staminaBarX + BAR_WIDTH + 10, staminaBarY + BAR_HEIGHT);
	}

	/**
	 * Render quick item slots
	 */
	private void renderQuickSlots() {
		for (int i = 0; i < 4; i++) {
			float slotX = quickSlotStartX + (i * (QUICK_SLOT_SIZE + QUICK_SLOT_SPACING));
			float slotY = quickSlotStartY;

			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
			shapeRenderer.rect(slotX, slotY, QUICK_SLOT_SIZE, QUICK_SLOT_SIZE);

			shapeRenderer.setColor(1f, 1f, 1f, 1f);
			shapeRenderer.rect(slotX, slotY, QUICK_SLOT_SIZE, QUICK_SLOT_SIZE);
			shapeRenderer.end();

			// Slot number
			font.setColor(Color.WHITE);
			font.draw(batch, String.valueOf(i + 1), slotX + 25, slotY + 30);
		}
	}

	/**
	 * Render objective/quest display
	 */
	private void renderObjective() {
		try {
			String objective = logicLayerAPI.getActiveQuestObjective();
			if (objective != null && !objective.isEmpty()) {
				font.setColor(Color.YELLOW);
				font.draw(batch, objective, objectiveX - 200, objectiveY);
			}
		} catch (Exception e) {
			// Silent fail - objective optional
		}
	}

	/**
	 * Render compass
	 */
	private void renderCompass() {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.8f);
		shapeRenderer.circle(compassX, compassY, 30f);

		shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f); // North indicator
		shapeRenderer.circle(compassX, compassY + 20, 5f);
		shapeRenderer.end();
	}

	@Override
	public boolean handleInput() {
		if (!isActive) {
			return false;
		}

		// Check for inventory hotkey
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.I)) {
			logicLayerAPI.requestScreenChange("INVENTORY");
			return true;
		}

		// Check for character screen hotkey
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.C)) {
			logicLayerAPI.requestScreenChange("CHARACTER");
			return true;
		}

		// Check for map hotkey
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.M)) {
			logicLayerAPI.requestScreenChange("MAP");
			return true;
		}

		// Check for pause hotkey
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
			logicLayerAPI.requestScreenChange("PAUSE_MENU");
			return true;
		}

		return false;
	}

	@Override
	protected void onResize() {
		setupLayout();
	}

	@Override
	public void dispose() {
		if (shapeRenderer != null) {
			shapeRenderer.dispose();
		}
		if (font != null) {
			font.dispose();
		}
	}
}
