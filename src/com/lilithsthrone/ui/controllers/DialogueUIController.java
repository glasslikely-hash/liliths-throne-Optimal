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
 * Dialogue UI Controller
 * 
 * Manages dialogue display and response selection during conversations.
 * 
 * Displays:
 *  - NPC dialogue text
 *  - Character portrait (optional)
 *  - Available response options
 *  - Dialogue history
 *  - Skip/speed options
 * 
 * Input:
 *  - Response selection (keyboard/mouse)
 *  - Advance dialogue (click/space)
 *  - Skip dialogue (ESC)
 * 
 * @since Step 3
 * @version 1.0
 */
public class DialogueUIController extends UIControllerBase {

	private static final String CONTROLLER_NAME = "DialogueUIController";

	// UI elements
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private BitmapFont largeFontName;
	private com.badlogic.gdx.graphics.Texture npcPortraitTexture;

	// Dialogue display
	private String currentNpcName = "";
	private String currentDialogueText = "";
	private List<String> currentResponses = new ArrayList<>();
	private int selectedResponseIndex = 0;

	// Layout
	private float dialogueBoxX, dialogueBoxY;
	private float dialogueBoxWidth, dialogueBoxHeight;
	private float responseStartX, responseStartY;
	private float npcPortraitX, npcPortraitY;
	private static final float PORTRAIT_SIZE = 150f;
	private static final float RESPONSE_HEIGHT = 35f;
	private static final float RESPONSE_PADDING = 20f;

	// Animation
	private float textRevealProgress = 1f; // 0-1, for text animation
	private float textRevealSpeed = 20f; // characters per second
	private int revealedCharacters = 0;

	// Timing
	private float dialogueTimer = 0f;
	private static final float MIN_DIALOGUE_TIME = 0.5f; // Minimum time before can advance

	// State
	private boolean isWaitingForResponse = false;
	private boolean canAdvance = false;

	/**
	 * Constructor for dialogue UI controller
	 */
	public DialogueUIController(SpriteBatch batch, OrthographicCamera camera,
	                            LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		super(batch, camera, logicLayerAPI, inputManager);
		this.shapeRenderer = new ShapeRenderer();
		this.font = new BitmapFont();
		this.largeFontName = new BitmapFont();
	}

	@Override
	public void initialize() {
		System.out.println("[" + CONTROLLER_NAME + "] Initializing dialogue UI");
		setupLayout();
		resetDialogueState();
	}

	/**
	 * Setup initial UI layout for dialogue
	 */
	private void setupLayout() {
		// Dialogue box (centered, bottom 1/3)
		dialogueBoxWidth = screenWidth * 0.8f;
		dialogueBoxHeight = screenHeight * 0.3f;
		dialogueBoxX = (screenWidth - dialogueBoxWidth) / 2f;
		dialogueBoxY = 20f;

		// Portrait (left side)
		npcPortraitX = 30f;
		npcPortraitY = dialogueBoxY + (dialogueBoxHeight - PORTRAIT_SIZE) / 2f;

		// Responses (below dialogue box)
		responseStartX = dialogueBoxX + RESPONSE_PADDING;
		responseStartY = dialogueBoxY + dialogueBoxHeight + 20f;
	}

	/**
	 * Reset dialogue state
	 */
	private void resetDialogueState() {
		currentNpcName = "";
		currentDialogueText = "";
		currentResponses.clear();
		selectedResponseIndex = 0;
		isWaitingForResponse = false;
		canAdvance = false;
		textRevealProgress = 0f;
		revealedCharacters = 0;
		dialogueTimer = 0f;
	}

	/**
	 * Start a new dialogue line
	 * 
	 * @param npcName Name of speaking NPC
	 * @param dialogueText The dialogue text to display
	 */
	public void setDialogue(String npcName, String dialogueText) {
		this.currentNpcName = npcName;
		this.currentDialogueText = dialogueText;
		this.isWaitingForResponse = false;
		this.textRevealProgress = 0f;
		this.revealedCharacters = 0;
		this.dialogueTimer = 0f;
		this.canAdvance = false;
		currentResponses.clear();
		
		// Load NPC portrait based on character name
		loadNpcPortrait(npcName);
	}
	
	/**
	 * Load and cache NPC portrait sprite
	 * 
	 * @param npcName Name of the NPC whose portrait to load
	 */
	private void loadNpcPortrait(String npcName) {
		try {
			// Dispose of previous texture if exists
			if (npcPortraitTexture != null) {
				npcPortraitTexture.dispose();
			}
			
			// Generate portrait path from character name (convert to lowercase, remove spaces)
			String portraitPath = "res/characters/" + npcName.toLowerCase().replace(" ", "_") + "/portrait.png";
			
			// Try to load portrait, fall back to default if not found
			try {
				npcPortraitTexture = new com.badlogic.gdx.graphics.Texture(portraitPath);
			} catch (Exception e) {
				// If specific portrait not found, use default NPC portrait
				try {
					npcPortraitTexture = new com.badlogic.gdx.graphics.Texture("res/characters/default_portrait.png");
				} catch (Exception e2) {
					// If even default doesn't exist, leave null and render placeholder
					npcPortraitTexture = null;
					System.warn("[" + CONTROLLER_NAME + "] Failed to load portrait for: " + npcName);
				}
			}
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error loading portrait for: " + npcName);
			npcPortraitTexture = null;
		}
	}

	/**
	 * Set the available responses for player selection
	 * 
	 * @param responses List of response options
	 */
	public void setResponses(List<String> responses) {
		this.currentResponses = new ArrayList<>(responses);
		this.selectedResponseIndex = 0;
		this.isWaitingForResponse = true;
		this.canAdvance = true;
	}

	@Override
	public void update(float deltaTime) {
		if (!isActive || !isVisible) {
			return;
		}

		// Update dialogue timer
		dialogueTimer += deltaTime;
		if (dialogueTimer >= MIN_DIALOGUE_TIME) {
			canAdvance = true;
		}

		// Update text reveal animation
		if (!isWaitingForResponse && textRevealProgress < 1f) {
			revealedCharacters += (int) (textRevealSpeed * deltaTime);
			if (revealedCharacters >= currentDialogueText.length()) {
				textRevealProgress = 1f;
				revealedCharacters = currentDialogueText.length();
				canAdvance = true;
			} else {
				textRevealProgress = (float) revealedCharacters / currentDialogueText.length();
			}
		}
	}

	@Override
	public void render(float deltaTime) {
		if (!isVisible) {
			return;
		}

		batch.begin();

		// Render NPC portrait
		renderPortrait();

		// Render dialogue box
		renderDialogueBox();

		// Render responses if available
		if (isWaitingForResponse) {
			renderResponses();
		}

		batch.end();
	}

	/**
	 * Render NPC portrait
	 */
	private void renderPortrait() {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Portrait background
		shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
		shapeRenderer.rect(npcPortraitX, npcPortraitY, PORTRAIT_SIZE, PORTRAIT_SIZE);

		// Portrait border
		shapeRenderer.setColor(1f, 1f, 1f, 1f);
		shapeRenderer.rect(npcPortraitX, npcPortraitY, PORTRAIT_SIZE, PORTRAIT_SIZE);

		shapeRenderer.end();

		// Load and render actual NPC portrait sprite
		if (npcPortraitTexture != null) {
			batch.draw(npcPortraitTexture, npcPortraitX, npcPortraitY, PORTRAIT_SIZE, PORTRAIT_SIZE);
		} else {
			// Render placeholder text if portrait not available
			font.setColor(Color.GRAY);
			font.draw(batch, "[No Portrait]", npcPortraitX + 20, npcPortraitY + PORTRAIT_SIZE / 2);
		}
	}

	/**
	 * Render dialogue box with text
	 */
	private void renderDialogueBox() {
		// Dialogue box background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
		shapeRenderer.rect(dialogueBoxX, dialogueBoxY, dialogueBoxWidth, dialogueBoxHeight);

		// Border
		shapeRenderer.setColor(1f, 0.8f, 0.2f, 1f); // Gold border
		shapeRenderer.rect(dialogueBoxX, dialogueBoxY, dialogueBoxWidth, dialogueBoxHeight);

		shapeRenderer.end();

		// NPC name
		largeFontName.setColor(Color.YELLOW);
		largeFontName.draw(batch, currentNpcName, dialogueBoxX + 20, dialogueBoxY + dialogueBoxHeight - 20);

		// Dialogue text (with reveal animation)
		font.setColor(Color.WHITE);
		String displayText = currentDialogueText.substring(0, revealedCharacters);
		
		// Word wrap and render dialogue text
		float textX = dialogueBoxX + 20;
		float textY = dialogueBoxY + dialogueBoxHeight - 60;
		float maxWidth = dialogueBoxWidth - 40;

		renderWrappedText(displayText, textX, textY, maxWidth);

		// "Continue" prompt if text is fully revealed
		if (textRevealProgress >= 1f && !isWaitingForResponse) {
			font.setColor(Color.CYAN);
			font.draw(batch, "Press SPACE to continue...", dialogueBoxX + dialogueBoxWidth - 250, dialogueBoxY + 15);
		}
	}

	/**
	 * Render wrapped dialogue text
	 * 
	 * @param text Text to render
	 * @param x Starting X position
	 * @param y Starting Y position
	 * @param maxWidth Maximum line width
	 */
	private void renderWrappedText(String text, float x, float y, float maxWidth) {
		String[] words = text.split(" ");
		StringBuilder currentLine = new StringBuilder();
		float currentY = y;

		for (String word : words) {
			String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
			if (font.getBounds(testLine).width <= maxWidth) {
				currentLine = new StringBuilder(testLine);
			} else {
				// Render current line
				if (currentLine.length() > 0) {
					font.draw(batch, currentLine.toString(), x, currentY);
					currentY -= 25f; // Line height
				}
				currentLine = new StringBuilder(word);
			}
		}

		// Render final line
		if (currentLine.length() > 0) {
			font.draw(batch, currentLine.toString(), x, currentY);
		}
	}

	/**
	 * Render available responses
	 */
	private void renderResponses() {
		float responseY = responseStartY;

		for (int i = 0; i < currentResponses.size(); i++) {
			boolean isSelected = (i == selectedResponseIndex);

			// Response box background
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Color bgColor = isSelected ? ColorCache.UI_SELECTED : ColorCache.UI_UNSELECTED;
			shapeRenderer.setColor(bgColor);
			shapeRenderer.rect(responseStartX, responseY, dialogueBoxWidth - (RESPONSE_PADDING * 2), RESPONSE_HEIGHT);

			// Border
			shapeRenderer.setColor(isSelected ? Color.CYAN : Color.WHITE);
			shapeRenderer.rect(responseStartX, responseY, dialogueBoxWidth - (RESPONSE_PADDING * 2), RESPONSE_HEIGHT);

			shapeRenderer.end();

			// Response text
			font.setColor(isSelected ? Color.CYAN : Color.WHITE);
			font.draw(batch, "► " + currentResponses.get(i), responseStartX + 10, responseY + 25);

			responseY -= (RESPONSE_HEIGHT + 10f);
		}
	}

	/**
	 * Select the next response
	 */
	public void selectNextResponse() {
		if (!isWaitingForResponse || currentResponses.isEmpty()) {
			return;
		}
		selectedResponseIndex = (selectedResponseIndex + 1) % currentResponses.size();
	}

	/**
	 * Select the previous response
	 */
	public void selectPreviousResponse() {
		if (!isWaitingForResponse || currentResponses.isEmpty()) {
			return;
		}
		selectedResponseIndex = (selectedResponseIndex - 1 + currentResponses.size()) % currentResponses.size();
	}

	/**
	 * Confirm the selected response
	 * 
	 * @return true if a response was confirmed
	 */
	public boolean confirmResponse() {
		if (!isWaitingForResponse || selectedResponseIndex < 0 || selectedResponseIndex >= currentResponses.size()) {
			return false;
		}

		try {
			String selectedResponse = currentResponses.get(selectedResponseIndex);
			logicLayerAPI.executeDialogueResponse(selectedResponse);
			return true;
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error confirming response: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Advance dialogue (skip animation or confirm if waiting)
	 */
	public void advanceDialogue() {
		if (!canAdvance) {
			return;
		}

		if (isWaitingForResponse) {
			confirmResponse();
		} else if (textRevealProgress < 1f) {
			// Skip animation
			textRevealProgress = 1f;
			revealedCharacters = currentDialogueText.length();
		} else {
			// Advance to next dialogue line
			try {
				logicLayerAPI.advanceDialogue();
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error advancing dialogue: " + e.getMessage());
			}
		}
	}

	@Override
	public boolean handleInput() {
		if (!isActive || !isVisible) {
			return false;
		}

		// Response navigation
		if (isWaitingForResponse) {
			if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.UP)) {
				selectPreviousResponse();
				return true;
			}
			if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
				selectNextResponse();
				return true;
			}
		}

		// Advance/confirm
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.SPACE) ||
		    inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
			advanceDialogue();
			return true;
		}

		// Cancel dialogue
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
			try {
				logicLayerAPI.exitDialogue();
				return true;
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error exiting dialogue: " + e.getMessage());
			}
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
		if (largeFontName != null) {
			largeFontName.dispose();
		}
		if (npcPortraitTexture != null) {
			npcPortraitTexture.dispose();
		}
	}
}
