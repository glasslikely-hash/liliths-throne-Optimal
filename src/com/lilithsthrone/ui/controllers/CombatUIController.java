package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;
import com.lilithsthrone.ui.utils.ColorCache;
import java.util.ArrayList;
import java.util.List;

/**
 * Combat UI Controller
 * 
 * Manages the combat interface during battles.
 * 
 * Displays:
 *  - Enemy stats and health bars
 *  - Combat action menu (attack, magic, defend, flee)
 *  - Available spells/abilities
 *  - Combat log (recent actions)
 *  - Turn indicator
 *  - Status effects on combatants
 * 
 * Input:
 *  - Action selection (keyboard/mouse)
 *  - Target selection (mouse clicks)
 *  - Spell/ability hotkeys
 * 
 * @since Step 3
 * @version 1.0
 */
public class CombatUIController extends UIControllerBase {

	private static final String CONTROLLER_NAME = "CombatUIController";

	// UI elements
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;

	// Combat action menu
	private static final String[] COMBAT_ACTIONS = { "Attack", "Magic", "Defend", "Flee" };
	private int selectedAction = 0;
	private float actionMenuX, actionMenuY;
	private static final float ACTION_BUTTON_WIDTH = 120f;
	private static final float ACTION_BUTTON_HEIGHT = 40f;
	private static final float ACTION_BUTTON_SPACING = 10f;

	// Combat log
	private List<String> combatLog = new ArrayList<>();
	private static final int MAX_LOG_ENTRIES = 8;
	private float logX, logY;
	private static final float LOG_LINE_HEIGHT = 20f;

	// Enemy display
	private float enemyHealthX, enemyHealthY;
	private float enemyNameX, enemyNameY;

	// Turn indicator
	private float turnX, turnY;
	private boolean playerTurn = true;

	// Status effect display
	private float statusEffectX, statusEffectY;

	// Cached combat state
	private String cachedEnemyName = "";
	private float cachedEnemyHealth = 0f;
	private float cachedEnemyMaxHealth = 100f;

	/**
	 * Constructor for combat UI controller
	 */
	public CombatUIController(SpriteBatch batch, OrthographicCamera camera,
	                          LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		super(batch, camera, logicLayerAPI, inputManager);
		this.shapeRenderer = new ShapeRenderer();
		this.font = new BitmapFont();
	}

	@Override
	public void initialize() {
		System.out.println("[" + CONTROLLER_NAME + "] Initializing combat UI");
		setupLayout();
		combatLog.clear();
		selectedAction = 0;
		playerTurn = true;
		logCombatEvent("Battle started!");
	}

	/**
	 * Setup initial UI layout for combat
	 */
	private void setupLayout() {
		// Action menu (bottom center)
		actionMenuX = (screenWidth - (COMBAT_ACTIONS.length * (ACTION_BUTTON_WIDTH + ACTION_BUTTON_SPACING))) / 2f;
		actionMenuY = 20f;

		// Combat log (bottom left)
		logX = 20f;
		logY = 20f;

		// Enemy display (top right)
		enemyHealthX = screenWidth - 300f;
		enemyHealthY = screenHeight - 80f;
		enemyNameX = screenWidth - 300f;
		enemyNameY = screenHeight - 50f;

		// Turn indicator (top center)
		turnX = screenWidth / 2f;
		turnY = screenHeight - 40f;

		// Status effects (below player stats, left side)
		statusEffectX = 20f;
		statusEffectY = 200f;
	}

	@Override
	public void update(float deltaTime) {
		if (!isActive || !isVisible) {
			return;
		}

		// Update cached combat state
		updateCachedCombatState();
	}

	/**
	 * Update cached combat state from game
	 */
	private void updateCachedCombatState() {
		try {
			// Get current enemy from LogicLayerAPI
			Object enemy = logicLayerAPI.getCurrentCombatEnemy();
			if (enemy != null) {
				// This would normally get the enemy name and health from the game
				cachedEnemyName = "Enemy"; // Placeholder
				cachedEnemyHealth = 100f; // Placeholder
				cachedEnemyMaxHealth = 100f; // Placeholder
			}

			// Update player turn status
			playerTurn = logicLayerAPI.isPlayerCombatTurn();
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error updating combat state: " + e.getMessage());
		}
	}

	@Override
	public void render(float deltaTime) {
		if (!isVisible) {
			return;
		}

		batch.begin();
		batch.setColor(Color.WHITE);

		// Render enemy stats
		renderEnemyStats();

		// Render turn indicator
		renderTurnIndicator();

		// Render status effects
		renderStatusEffects();

		// Render combat log
		renderCombatLog();

		batch.end();

		// Render action menu (after batch to use shape renderer)
		renderActionMenu();
	}

	/**
	 * Render enemy stats and health bar
	 */
	private void renderEnemyStats() {
		float healthPercent = cachedEnemyMaxHealth > 0 ? cachedEnemyHealth / cachedEnemyMaxHealth : 0f;
		healthPercent = Math.min(1f, Math.max(0f, healthPercent));

		// Enemy name
		font.setColor(Color.WHITE);
		font.draw(batch, cachedEnemyName, enemyNameX, enemyNameY + 50);

		// Health bar background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(enemyHealthX, enemyHealthY, 250f, 30f);

		// Health fill
		shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(enemyHealthX + 2, enemyHealthY + 2, (250f - 4) * healthPercent, 26f);

		// Border
		shapeRenderer.setColor(1f, 1f, 1f, 1f);
		shapeRenderer.rect(enemyHealthX, enemyHealthY, 250f, 30f);
		shapeRenderer.end();

		// Health text
		font.setColor(Color.WHITE);
		font.draw(batch, String.format("%.0f/%.0f", cachedEnemyHealth, cachedEnemyMaxHealth),
		         enemyHealthX + 10, enemyHealthY + 35);
	}

	/**
	 * Render turn indicator
	 */
	private void renderTurnIndicator() {
		font.setColor(playerTurn ? Color.YELLOW : Color.ORANGE);
		String turnText = playerTurn ? "YOUR TURN" : "ENEMY TURN";
		font.draw(batch, turnText, turnX - 50, turnY + 20);
	}

	/**
	 * Render status effects on player and enemy
	 */
	private void renderStatusEffects() {
		try {
			List<String> playerEffects = logicLayerAPI.getPlayerStatusEffects();
			if (playerEffects != null && !playerEffects.isEmpty()) {
				font.setColor(Color.CYAN);
				float y = statusEffectY;
				for (String effect : playerEffects) {
					font.draw(batch, "• " + effect, statusEffectX, y);
					y -= LOG_LINE_HEIGHT;
				}
			}
		} catch (Exception e) {
			// Silent fail - effects optional
		}
	}

	/**
	 * Render combat log
	 */
	private void renderCombatLog() {
		font.setColor(Color.WHITE);
		float y = logY + (combatLog.size() * LOG_LINE_HEIGHT);

		for (int i = 0; i < combatLog.size(); i++) {
			font.draw(batch, combatLog.get(i), logX, y);
			y -= LOG_LINE_HEIGHT;
		}
	}

	/**
	 * Render action menu
	 */
	private void renderActionMenu() {
		for (int i = 0; i < COMBAT_ACTIONS.length; i++) {
			float buttonX = actionMenuX + (i * (ACTION_BUTTON_WIDTH + ACTION_BUTTON_SPACING));
			float buttonY = actionMenuY;

			// Highlight selected action
			Color buttonColor = (i == selectedAction) ? ColorCache.UI_SELECTED_BRIGHT : ColorCache.UI_UNSELECTED_ALT2;

			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(buttonColor);
			shapeRenderer.rect(buttonX, buttonY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);

			shapeRenderer.setColor(1f, 1f, 1f, 1f);
			shapeRenderer.rect(buttonX, buttonY, ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT);
			shapeRenderer.end();

			// Button text
			batch.begin();
			font.setColor(Color.WHITE);
			float textWidth = font.getBounds(COMBAT_ACTIONS[i]).width;
			font.draw(batch, COMBAT_ACTIONS[i], buttonX + (ACTION_BUTTON_WIDTH - textWidth) / 2f, buttonY + 30);
			batch.end();
		}
	}

	/**
	 * Log a combat event to the combat log
	 * 
	 * @param message Event message
	 */
	public void logCombatEvent(String message) {
		combatLog.add(message);
		if (combatLog.size() > MAX_LOG_ENTRIES) {
			combatLog.remove(0);
		}
	}

	/**
	 * Select the next action (cycle through menu)
	 */
	public void selectNextAction() {
		selectedAction = (selectedAction + 1) % COMBAT_ACTIONS.length;
	}

	/**
	 * Select the previous action (reverse cycle)
	 */
	public void selectPreviousAction() {
		selectedAction = (selectedAction - 1 + COMBAT_ACTIONS.length) % COMBAT_ACTIONS.length;
	}

	/**
	 * Confirm the selected action
	 * 
	 * @return true if action was executed
	 */
	public boolean confirmAction() {
		if (!playerTurn) {
			return false;
		}

		try {
			switch (selectedAction) {
				case 0: // Attack
					logicLayerAPI.executeCombatAction("attack", null);
					logCombatEvent("Player attacks!");
					playerTurn = false;
					return true;
				case 1: // Magic
					logicLayerAPI.executeCombatAction("magic", null);
					logCombatEvent("Player casts magic!");
					playerTurn = false;
					return true;
				case 2: // Defend
					logicLayerAPI.executeCombatAction("defend", null);
					logCombatEvent("Player takes defensive stance!");
					playerTurn = false;
					return true;
				case 3: // Flee
					logicLayerAPI.executeCombatAction("flee", null);
					logCombatEvent("Player flees!");
					return true;
				default:
					return false;
			}
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error executing combat action: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean handleInput() {
		if (!isActive || !playerTurn) {
			return false;
		}

		// Action selection
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
			selectPreviousAction();
			return true;
		}

		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
			selectNextAction();
			return true;
		}

		// Confirm action
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
			return confirmAction();
		}

		// Cancel/menu
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
			logicLayerAPI.requestScreenChange("GAME");
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
