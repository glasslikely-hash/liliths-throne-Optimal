package com.lilithsthrone.ui.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.components.UIButton;
import com.lilithsthrone.ui.components.UIPanel;
import com.lilithsthrone.ui.components.UIText;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Dialogue Layer - Renders NPC dialogue and choices.
 * 
 * Displays:
 * - Dialogue text
 * - Character portrait
 * - Dialogue choices
 * - Typewriter effect
 * 
 * Replaces JavaFX-based dialogue rendering.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class DialogueLayer extends UILayer {
	
	private static final String LAYER_NAME = "DialogueLayer";
	private boolean dialogueActive = false;
	
	private UIPanel dialoguePanel;
	private UIText speakerName;
	private UIText dialogueText;
	private UIButton choice1Button;
	private UIButton choice2Button;
	private UIButton choice3Button;
	
	private BitmapFont dialogueFont;
	private BitmapFont choiceFont;
	
	private float screenWidth = 800;
	private float screenHeight = 600;
	
	private float typewriterProgress = 0f;
	private String fullDialogueText = "";
	
	public DialogueLayer(LogicLayerAPI logicApi) {
		super(logicApi);
		initializeComponents();
	}
	
	/**
	 * Initialize dialogue components
	 */
	private void initializeComponents() {
		dialogueFont = new BitmapFont();
		choiceFont = new BitmapFont();
		
		// Dialogue panel background
		dialoguePanel = new UIPanel(50, 50, 700, 500);
		dialoguePanel.setBackgroundColor(new Color(0.05f, 0.05f, 0.1f, 0.95f));
		dialoguePanel.setBorderColor(new Color(0.6f, 0.4f, 0.2f, 1f));
		dialoguePanel.setBorderThickness(3);
		
		// Speaker name
		speakerName = new UIText(70, 500, 660, 30, "Npc Name", dialogueFont);
		speakerName.setColor(new Color(1f, 0.8f, 0.2f, 1f));
		dialoguePanel.add(speakerName);
		
		// Dialogue text
		dialogueText = new UIText(70, 300, 660, 150, "Dialogue text goes here...", dialogueFont);
		dialogueText.setColor(new Color(0.8f, 0.8f, 0.8f, 1f));
		dialogueText.setWordWrap(true);
		dialogueText.setLineSpacing(1.2f);
		dialoguePanel.add(dialogueText);
		
		// Choice buttons
		float choiceY = 250;
		float choiceSpacing = 50;
		float choiceWidth = 200;
		float choiceHeight = 35;
		float choiceX = 150;
		
		choice1Button = new UIButton(choiceX, choiceY, choiceWidth, choiceHeight, "Choice 1", choiceFont);
		choice1Button.onClick(() -> selectChoice(0));
		dialoguePanel.add(choice1Button);
		
		choice2Button = new UIButton(choiceX, choiceY - choiceSpacing, choiceWidth, choiceHeight, "Choice 2", choiceFont);
		choice2Button.onClick(() -> selectChoice(1));
		dialoguePanel.add(choice2Button);
		
		choice3Button = new UIButton(choiceX, choiceY - choiceSpacing * 2, choiceWidth, choiceHeight, "Choice 3", choiceFont);
		choice3Button.onClick(() -> selectChoice(2));
		dialoguePanel.add(choice3Button);
	}
	
	@Override
	public void update(float delta) {
		if (!visible || !dialogueActive) {
			return;
		}
		
		// Update typewriter effect
		if (typewriterProgress < 1f) {
			typewriterProgress += delta * 2f;  // 2 chars per second base
			
			// Display partial text
			int visibleChars = (int)(fullDialogueText.length() * typewriterProgress);
			String displayText = fullDialogueText.substring(0, Math.min(visibleChars, fullDialogueText.length()));
			dialogueText.setText(displayText);
		}
		
		// Update choice highlighting
		dialoguePanel.update(delta);
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible || !dialogueActive) {
			return;
		}
		
		// Render dialogue panel with all components
		dialoguePanel.render(batch);
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!visible || !dialogueActive || !enabled) {
			return;
		}
		
		// Skip to end of typewriter on click
		if (event.getType() == InputEvent.InputType.MOUSE_CLICK && typewriterProgress < 1f) {
			typewriterProgress = 1f;
			dialogueText.setText(fullDialogueText);
			event.consume();
			return;
		}
		
		// Delegate choice selection
		dialoguePanel.onInput(event);
	}
	
	/**
	 * Start dialogue with NPC
	 */
	public void startDialogue(String npcName, String dialogueText, String[] choices) {
		dialogueActive = true;
		setVisible(true);
		
		speakerName.setText(npcName);
		this.fullDialogueText = dialogueText;
		this.typewriterProgress = 0f;
		
		// Set choice buttons
		if (choices.length >= 1) {
			choice1Button.setLabel(choices[0]);
			choice1Button.setVisible(true);
		} else {
			choice1Button.setVisible(false);
		}
		
		if (choices.length >= 2) {
			choice2Button.setLabel(choices[1]);
			choice2Button.setVisible(true);
		} else {
			choice2Button.setVisible(false);
		}
		
		if (choices.length >= 3) {
			choice3Button.setLabel(choices[2]);
			choice3Button.setVisible(true);
		} else {
			choice3Button.setVisible(false);
		}
	}
	
	/**
	 * End current dialogue
	 */
	public void endDialogue() {
		dialogueActive = false;
		setVisible(false);
	}
	
	/**
	 * Handle dialogue choice selection
	 */
	private void selectChoice(int choiceIndex) {
		// TODO: Call LogicLayerAPI to process dialogue choice
		// logicApi.selectDialogueChoice(choiceIndex);
		
		// End dialogue
		endDialogue();
	}
	
	/**
	 * Check if dialogue is active
	 */
	public boolean isDialogueActive() {
		return dialogueActive;
	}
	
	@Override
	public void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
		
		// Recenter dialogue panel
		float panelWidth = 700;
		float panelHeight = 500;
		dialoguePanel.setPosition((width - panelWidth) / 2, (height - panelHeight) / 2 - 50);
		dialoguePanel.setSize(panelWidth, panelHeight);
	}
	
	@Override
	public void dispose() {
		if (dialogueFont != null) dialogueFont.dispose();
		if (choiceFont != null) choiceFont.dispose();
	}
}
