package com.lilithsthrone.ui.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.BaseScreen;
import com.lilithsthrone.ui.ScreenManager;
import com.lilithsthrone.ui.components.UIButton;
import com.lilithsthrone.ui.components.UIList;
import com.lilithsthrone.ui.components.UIPanel;
import com.lilithsthrone.ui.components.UIText;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Save/Load Game Screen
 * 
 * Displays:
 * - List of save slots
 * - Save slot metadata (timestamp, playtime, location)
 * - Save button (creates new save)
 * - Load button (loads selected save)
 * - Delete button (removes save slot)
 * - Cancel button (return to main menu)
 * 
 * Replaces JavaFX-based save/load dialogs.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class SaveLoadScreen extends BaseScreen {

	private static final String SCREEN_NAME = "SaveLoadScreen";
	
	private ScreenManager screenManager;
	
	private UIPanel mainPanel;
	private UIText titleText;
	private UIList saveSlotList;
	private UIText slotInfoText;
	
	private UIButton newSaveButton;
	private UIButton loadButton;
	private UIButton deleteButton;
	private UIButton cancelButton;
	
	private BitmapFont titleFont;
	private BitmapFont labelFont;
	
	private boolean isSaveMode = true;  // true for save, false for load
	private float screenWidth = 1200;
	private float screenHeight = 800;
	
	public SaveLoadScreen(SpriteBatch batch, OrthographicCamera camera, 
	                       ScreenManager screenManager, LogicLayerAPI logicApi, 
	                       boolean saveMode) {
		super(batch, camera, logicApi);
		this.screenManager = screenManager;
		this.isSaveMode = saveMode;
		initializeComponents();
	}

	/**
	 * Initialize all UI components
	 */
	private void initializeComponents() {
		// Fonts
		titleFont = new BitmapFont();
		labelFont = new BitmapFont();
		
		// Main panel
		mainPanel = new UIPanel(200, 100, 800, 600);
		mainPanel.setBackgroundColor(new Color(0.05f, 0.05f, 0.1f, 0.95f));
		mainPanel.setBorderColor(new Color(0.6f, 0.4f, 0.2f, 1f));
		mainPanel.setBorderThickness(3);
		
		// Title
		String title = isSaveMode ? "Save Game" : "Load Game";
		titleText = new UIText(220, 650, 760, 50, title, titleFont);
		titleText.setColor(new Color(1f, 0.8f, 0.2f, 1f));
		mainPanel.add(titleText);
		
		// Save slot list
		saveSlotList = new UIList(220, 150, 450, 480, labelFont);
		saveSlotList.setItemColor(new Color(0.8f, 0.8f, 0.8f, 1f));
		saveSlotList.setHoverColor(new Color(0.5f, 0.7f, 1f, 1f));
		saveSlotList.setSelectedColor(new Color(0.2f, 0.5f, 1f, 1f));
		
		// Load save slots from LogicLayerAPI
		loadSaveSlots();
		mainPanel.add(saveSlotList);
		
		// Slot info display (timestamp, playtime, location)
		slotInfoText = new UIText(700, 400, 280, 230, "", labelFont);
		slotInfoText.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
		slotInfoText.setWordWrap(true);
		mainPanel.add(slotInfoText);
		
		// Buttons
		float buttonY = 150;
		float buttonX = 700;
		float buttonWidth = 120;
		float buttonHeight = 40;
		float buttonSpacing = 50;
		
		if (isSaveMode) {
			newSaveButton = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, "New Save", labelFont);
			newSaveButton.onClick(() -> onNewSavePressed());
			mainPanel.add(newSaveButton);
			buttonY -= buttonSpacing;
		}
		
		loadButton = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, 
		                          isSaveMode ? "Overwrite" : "Load", labelFont);
		loadButton.onClick(() -> onLoadPressed());
		mainPanel.add(loadButton);
		
		buttonY -= buttonSpacing;
		deleteButton = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, "Delete", labelFont);
		deleteButton.onClick(() -> onDeletePressed());
		mainPanel.add(deleteButton);
		
		buttonY -= buttonSpacing;
		cancelButton = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, "Cancel", labelFont);
		cancelButton.onClick(() -> onCancelPressed());
		mainPanel.add(cancelButton);
	}

	/**
	 * Load save slot list from persistence manager
	 */
	private void loadSaveSlots() {
		try {
			java.util.List<String> slots = logicApi.getSaveSlots();
			if (slots != null) {
				for (String slot : slots) {
					saveSlotList.addItem(slot);
				}
			}
			
			if (slots == null || slots.isEmpty()) {
				saveSlotList.addItem("<No saves>");
			}
		} catch (Exception e) {
			Gdx.app.error("SaveLoadScreen", "Failed to load save slots: " + e.getMessage());
			saveSlotList.addItem("<Error loading saves>");
		}
	}

	/**
	 * Called when "New Save" button is pressed
	 */
	private void onNewSavePressed() {
		// Prompt for save name (simplified for now)
		String timestamp = java.time.LocalDateTime.now().format(
			java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		);
		String saveName = "Save-" + timestamp;
		
		try {
			logicApi.saveGame(saveName);
			Gdx.app.log("SaveLoadScreen", "Game saved to: " + saveName);
			
			// Reload save slots
			saveSlotList.clearItems();
			loadSaveSlots();
		} catch (Exception e) {
			Gdx.app.error("SaveLoadScreen", "Failed to save game: " + e.getMessage());
		}
	}

	/**
	 * Called when "Load" or "Overwrite" button is pressed
	 */
	private void onLoadPressed() {
		String selectedSlot = saveSlotList.getSelectedItem();
		if (selectedSlot == null || selectedSlot.contains("No saves") || selectedSlot.contains("Error")) {
			Gdx.app.log("SaveLoadScreen", "No valid save selected");
			return;
		}
		
		try {
			logicApi.loadGame(selectedSlot);
			Gdx.app.log("SaveLoadScreen", "Game loaded from: " + selectedSlot);
			
			// Return to game screen
			screenManager.setScreen(ScreenManager.ScreenType.GAME);
		} catch (Exception e) {
			Gdx.app.error("SaveLoadScreen", "Failed to load game: " + e.getMessage());
		}
	}

	/**
	 * Called when "Delete" button is pressed
	 */
	private void onDeletePressed() {
		String selectedSlot = saveSlotList.getSelectedItem();
		if (selectedSlot == null || selectedSlot.contains("No saves")) {
			return;
		}
		
		try {
			logicApi.deleteSaveSlot(selectedSlot);
			Gdx.app.log("SaveLoadScreen", "Save deleted: " + selectedSlot);
			
			// Reload save slots
			saveSlotList.clearItems();
			loadSaveSlots();
		} catch (Exception e) {
			Gdx.app.error("SaveLoadScreen", "Failed to delete save: " + e.getMessage());
		}
	}

	/**
	 * Called when "Cancel" button is pressed
	 */
	private void onCancelPressed() {
		screenManager.setScreen(ScreenManager.ScreenType.MAIN_MENU);
	}

	@Override
	public void show() {
		Gdx.app.log("SaveLoadScreen", "Screen shown");
	}

	@Override
	public void hide() {
		Gdx.app.log("SaveLoadScreen", "Screen hidden");
	}

	@Override
	public void update(float delta) {
		mainPanel.update(delta);
	}

	@Override
	public void render(SpriteBatch batch) {
		// Clear screen
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
		Gdx.gl.glClear(Gdx.gl.GL_COLOR_BUFFER_BIT);
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		// Render main panel
		mainPanel.render(batch);
		
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
		
		// Recenter panel
		float panelWidth = 800;
		float panelHeight = 600;
		mainPanel.setPosition((width - panelWidth) / 2, (height - panelHeight) / 2);
		mainPanel.setSize(panelWidth, panelHeight);
	}

	@Override
	public void onInput(InputEvent event) {
		mainPanel.onInput(event);
	}

	@Override
	public void dispose() {
		if (titleFont != null) titleFont.dispose();
		if (labelFont != null) labelFont.dispose();
	}
}
