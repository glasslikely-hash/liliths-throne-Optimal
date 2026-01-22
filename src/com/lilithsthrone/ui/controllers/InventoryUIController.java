package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lilithsthrone.game.character.GameCharacter;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;
import com.lilithsthrone.ui.utils.ColorCache;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory UI Controller
 * 
 * Manages the inventory/equipment interface.
 * 
 * Displays:
 *  - Character inventory (items organized by category)
 *  - Equipment slots (currently equipped items)
 *  - Item details (name, description, stats)
 *  - Inventory tabs (weapons, armor, consumables, quest)
 *  - Item quantity/durability
 * 
 * Input:
 *  - Inventory navigation (arrow keys)
 *  - Item selection (Enter)
 *  - Equip/unequip items
 *  - Use consumables
 *  - Drop items
 * 
 * @since Step 3
 * @version 1.0
 */
public class InventoryUIController extends UIControllerBase {

	private static final String CONTROLLER_NAME = "InventoryUIController";

	// UI elements
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private BitmapFont largeFontName;

	// Inventory tabs
	private enum InventoryTab {
		WEAPONS,
		ARMOR,
		CONSUMABLES,
		QUEST,
		ALL
	}

	private InventoryTab currentTab = InventoryTab.ALL;
	private List<String> currentInventoryItems = new ArrayList<>();
	private int selectedItemIndex = 0;

	// Equipment slots display
	private float equipmentX, equipmentY;
	private static final float EQUIPMENT_SLOT_SIZE = 80f;
	private static final float EQUIPMENT_SLOT_SPACING = 20f;

	// Inventory list display
	private float inventoryListX, inventoryListY;
	private static final float ITEM_LIST_WIDTH = 400f;
	private static final float ITEM_LIST_HEIGHT = 500f;
	private static final float ITEM_ROW_HEIGHT = 40f;
	private int inventoryStartIndex = 0;
	private static final int VISIBLE_ITEM_ROWS = 12;

	// Item detail panel
	private float detailPanelX, detailPanelY;
	private static final float DETAIL_PANEL_WIDTH = 300f;
	private static final float DETAIL_PANEL_HEIGHT = 500f;

	// Tab buttons
	private float tabStartX, tabStartY;
	private static final float TAB_BUTTON_WIDTH = 100f;
	private static final float TAB_BUTTON_HEIGHT = 30f;
	private static final float TAB_BUTTON_SPACING = 10f;

	// Cached inventory state
	private GameCharacter cachedPlayer = null;
	private String cachedSelectedItemName = "";
	private String cachedSelectedItemDescription = "";

	/**
	 * Constructor for inventory UI controller
	 */
	public InventoryUIController(SpriteBatch batch, OrthographicCamera camera,
	                             LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		super(batch, camera, logicLayerAPI, inputManager);
		this.shapeRenderer = new ShapeRenderer();
		this.font = new BitmapFont();
		this.largeFontName = new BitmapFont();
	}

	@Override
	public void initialize() {
		System.out.println("[" + CONTROLLER_NAME + "] Initializing inventory UI");
		setupLayout();
		refreshInventoryList();
	}

	/**
	 * Setup initial UI layout
	 */
	private void setupLayout() {
		// Equipment display (top left)
		equipmentX = 20f;
		equipmentY = screenHeight - 100f;

		// Tab buttons (below equipment)
		tabStartX = equipmentX;
		tabStartY = screenHeight - 150f;

		// Inventory list (center)
		inventoryListX = 20f;
		inventoryListY = screenHeight - 200f;

		// Detail panel (right side)
		detailPanelX = screenWidth - DETAIL_PANEL_WIDTH - 20f;
		detailPanelY = screenHeight - DETAIL_PANEL_HEIGHT - 20f;
	}

	/**
	 * Refresh the inventory list from game state
	 */
	private void refreshInventoryList() {
		try {
			cachedPlayer = logicLayerAPI.getPlayerCharacter();
			if (cachedPlayer != null) {
				currentInventoryItems = logicLayerAPI.getInventoryItems(currentTab.toString());
				selectedItemIndex = 0;
				inventoryStartIndex = 0;
				updateSelectedItemDetails();
			}
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error refreshing inventory: " + e.getMessage());
		}
	}

	/**
	 * Update cached details of selected item
	 */
	private void updateSelectedItemDetails() {
		if (selectedItemIndex >= 0 && selectedItemIndex < currentInventoryItems.size()) {
			try {
				String itemName = currentInventoryItems.get(selectedItemIndex);
				cachedSelectedItemName = itemName;
				cachedSelectedItemDescription = logicLayerAPI.getItemDescription(itemName);
			} catch (Exception e) {
				cachedSelectedItemName = "";
				cachedSelectedItemDescription = "Error loading item details";
			}
		}
	}

	@Override
	public void update(float deltaTime) {
		if (!isActive || !isVisible) {
			return;
		}

		// Periodically refresh inventory to catch changes
		// (Optional - only if real-time updates needed)
	}

	@Override
	public void render(float deltaTime) {
		if (!isVisible) {
			return;
		}

		batch.begin();
		batch.setColor(Color.WHITE);

		// Render tab buttons
		renderTabButtons();

		// Render equipment display
		renderEquipmentDisplay();

		// Render inventory list
		renderInventoryList();

		// Render item detail panel
		renderDetailPanel();

		batch.end();
	}

	/**
	 * Render tab selection buttons
	 */
	private void renderTabButtons() {
		float buttonX = tabStartX;
		InventoryTab[] tabs = InventoryTab.values();

		for (int i = 0; i < tabs.length; i++) {
			InventoryTab tab = tabs[i];
			boolean isSelected = (tab == currentTab);

			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			Color bgColor = isSelected ? ColorCache.UI_SELECTED : ColorCache.UI_UNSELECTED_ALT;
			shapeRenderer.setColor(bgColor);
			shapeRenderer.rect(buttonX, tabStartY, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT);

			shapeRenderer.setColor(isSelected ? Color.CYAN : Color.WHITE);
			shapeRenderer.rect(buttonX, tabStartY, TAB_BUTTON_WIDTH, TAB_BUTTON_HEIGHT);
			shapeRenderer.end();

			// Tab label
			font.setColor(isSelected ? Color.CYAN : Color.WHITE);
			font.draw(batch, tab.toString(), buttonX + 5, tabStartY + 20);

			buttonX += (TAB_BUTTON_WIDTH + TAB_BUTTON_SPACING);
		}
	}

	/**
	 * Render equipped items display
	 */
	private void renderEquipmentDisplay() {
		// Equipment slots grid
		String[] slotNames = { "Head", "Chest", "Legs", "Feet", "Hands", "Neck", "Weapon L", "Weapon R" };
		int slotIndex = 0;

		for (int row = 0; row < 2; row++) {
			for (int col = 0; col < 4; col++) {
				if (slotIndex >= slotNames.length) break;

				float slotX = equipmentX + (col * (EQUIPMENT_SLOT_SIZE + EQUIPMENT_SLOT_SPACING));
				float slotY = equipmentY - (row * (EQUIPMENT_SLOT_SIZE + EQUIPMENT_SLOT_SPACING));

				// Slot background
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
				shapeRenderer.rect(slotX, slotY, EQUIPMENT_SLOT_SIZE, EQUIPMENT_SLOT_SIZE);

				// Slot border
				shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1f);
				shapeRenderer.rect(slotX, slotY, EQUIPMENT_SLOT_SIZE, EQUIPMENT_SLOT_SIZE);
				shapeRenderer.end();

				// Slot label
				font.setColor(Color.WHITE);
				font.draw(batch, slotNames[slotIndex], slotX + 5, slotY + EQUIPMENT_SLOT_SIZE - 10);

				slotIndex++;
			}
		}
	}

	/**
	 * Render inventory item list
	 */
	private void renderInventoryList() {
		// List background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
		shapeRenderer.rect(inventoryListX, inventoryListY - ITEM_LIST_HEIGHT, ITEM_LIST_WIDTH, ITEM_LIST_HEIGHT);

		// List border
		shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1f);
		shapeRenderer.rect(inventoryListX, inventoryListY - ITEM_LIST_HEIGHT, ITEM_LIST_WIDTH, ITEM_LIST_HEIGHT);
		shapeRenderer.end();

		// Render visible items
		float itemY = inventoryListY - ITEM_ROW_HEIGHT;
		for (int i = inventoryStartIndex; i < Math.min(inventoryStartIndex + VISIBLE_ITEM_ROWS, currentInventoryItems.size()); i++) {
			boolean isSelected = (i == selectedItemIndex);

			// Item row background
			if (isSelected) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.setColor(0.4f, 0.6f, 1f, 0.5f);
				shapeRenderer.rect(inventoryListX + 2, itemY - ITEM_ROW_HEIGHT + 2, ITEM_LIST_WIDTH - 4, ITEM_ROW_HEIGHT - 4);
				shapeRenderer.end();
			}

			// Item name
			font.setColor(isSelected ? Color.CYAN : Color.WHITE);
			font.draw(batch, currentInventoryItems.get(i), inventoryListX + 10, itemY - 10);

			itemY -= ITEM_ROW_HEIGHT;
		}

		// Scroll indicator
		if (currentInventoryItems.size() > VISIBLE_ITEM_ROWS) {
			float scrollPercent = (float) inventoryStartIndex / (currentInventoryItems.size() - VISIBLE_ITEM_ROWS);
			font.setColor(Color.GRAY);
			font.draw(batch, "▼", inventoryListX + ITEM_LIST_WIDTH - 20, inventoryListY - 20);
		}
	}

	/**
	 * Render item detail panel
	 */
	private void renderDetailPanel() {
		// Panel background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
		shapeRenderer.rect(detailPanelX, detailPanelY, DETAIL_PANEL_WIDTH, DETAIL_PANEL_HEIGHT);

		// Panel border
		shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1f);
		shapeRenderer.rect(detailPanelX, detailPanelY, DETAIL_PANEL_WIDTH, DETAIL_PANEL_HEIGHT);
		shapeRenderer.end();

		// Item name header
		largeFontName.setColor(Color.YELLOW);
		largeFontName.draw(batch, cachedSelectedItemName, detailPanelX + 10, detailPanelY + DETAIL_PANEL_HEIGHT - 30);

		// Item description
		font.setColor(Color.WHITE);
		float descY = detailPanelY + DETAIL_PANEL_HEIGHT - 80;
		
		// Word wrap description
		String[] descWords = cachedSelectedItemDescription.split(" ");
		StringBuilder line = new StringBuilder();
		for (String word : descWords) {
			if (line.length() > 0) {
				line.append(" ");
			}
			line.append(word);
			if (font.getBounds(line.toString()).width > DETAIL_PANEL_WIDTH - 20) {
				font.draw(batch, line.toString(), detailPanelX + 10, descY);
				descY -= 20f;
				line = new StringBuilder(word);
			}
		}
		if (line.length() > 0) {
			font.draw(batch, line.toString(), detailPanelX + 10, descY);
		}

		// Action buttons (equip/use/drop)
		font.setColor(Color.CYAN);
		font.draw(batch, "E: Equip | U: Use | D: Drop", detailPanelX + 10, detailPanelY + 20);
	}

	/**
	 * Select the next item in the list
	 */
	public void selectNextItem() {
		if (currentInventoryItems.isEmpty()) {
			return;
		}

		selectedItemIndex = Math.min(selectedItemIndex + 1, currentInventoryItems.size() - 1);

		// Scroll list if needed
		if (selectedItemIndex >= inventoryStartIndex + VISIBLE_ITEM_ROWS) {
			inventoryStartIndex = selectedItemIndex - VISIBLE_ITEM_ROWS + 1;
		}

		updateSelectedItemDetails();
	}

	/**
	 * Select the previous item in the list
	 */
	public void selectPreviousItem() {
		selectedItemIndex = Math.max(selectedItemIndex - 1, 0);

		// Scroll list if needed
		if (selectedItemIndex < inventoryStartIndex) {
			inventoryStartIndex = selectedItemIndex;
		}

		updateSelectedItemDetails();
	}

	/**
	 * Switch to a different inventory tab
	 * 
	 * @param tab The tab to switch to
	 */
	public void switchTab(InventoryTab tab) {
		this.currentTab = tab;
		refreshInventoryList();
	}

	@Override
	public boolean handleInput() {
		if (!isActive || !isVisible) {
			return false;
		}

		// Item navigation
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.UP)) {
			selectPreviousItem();
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
			selectNextItem();
			return true;
		}

		// Tab switching
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
			InventoryTab[] tabs = InventoryTab.values();
			int currentIndex = currentTab.ordinal();
			int newIndex = (currentIndex - 1 + tabs.length) % tabs.length;
			switchTab(tabs[newIndex]);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
			InventoryTab[] tabs = InventoryTab.values();
			int currentIndex = currentTab.ordinal();
			int newIndex = (currentIndex + 1) % tabs.length;
			switchTab(tabs[newIndex]);
			return true;
		}

		// Item actions
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.E)) {
			// Equip item
			try {
				logicLayerAPI.equipItem(cachedSelectedItemName);
				refreshInventoryList();
				return true;
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error equipping item: " + e.getMessage());
			}
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.U)) {
			// Use item
			try {
				logicLayerAPI.useItem(cachedSelectedItemName);
				refreshInventoryList();
				return true;
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error using item: " + e.getMessage());
			}
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.D)) {
			// Drop item
			try {
				logicLayerAPI.dropItem(cachedSelectedItemName);
				refreshInventoryList();
				return true;
			} catch (Exception e) {
				System.err.println("[" + CONTROLLER_NAME + "] Error dropping item: " + e.getMessage());
			}
		}

		// Close inventory
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
		if (largeFontName != null) {
			largeFontName.dispose();
		}
	}
}
