package com.lilithsthrone.ui.layers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.components.UIButton;
import com.lilithsthrone.ui.components.UIList;
import com.lilithsthrone.ui.components.UIPanel;
import com.lilithsthrone.ui.components.UIText;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Menu Layer - Renders UI menus and panels.
 * 
 * Displays:
 * - Inventory menu
 * - Character screen
 * - Spell/skill list
 * - Map view
 * - Settings menu
 * - Pause menu
 * - Save/load dialog
 * 
 * Replaces JavaFX-based menu rendering.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class MenuLayer extends UILayer {
	
	private static final String LAYER_NAME = "MenuLayer";
	private boolean menuOpen = false;
	
	private UIPanel menuPanel;
	private UIList itemList;
	private UIText menuTitle;
	private UIButton closeButton;
	
	private BitmapFont menuFont;
	
	private float screenWidth = 800;
	private float screenHeight = 600;
	
	public MenuLayer(LogicLayerAPI logicApi) {
		super(logicApi);
		initializeComponents();
	}
	
	/**
	 * Initialize menu components
	 */
	private void initializeComponents() {
		menuFont = new BitmapFont();
		
		// Create semi-transparent panel background
		menuPanel = new UIPanel(150, 100, 500, 400);
		menuPanel.setBackgroundColor(new Color(0.1f, 0.1f, 0.15f, 0.9f));
		menuPanel.setBorderColor(new Color(0.8f, 0.6f, 0.2f, 1f));
		menuPanel.setBorderThickness(2);
		
		// Menu title
		menuTitle = new UIText(160, 450, 480, 30, "Inventory", menuFont);
		menuTitle.setColor(new Color(1f, 0.8f, 0.3f, 1f));
		menuPanel.add(menuTitle);
		
		// Item list
		itemList = new UIList(160, 110, 320, 300, menuFont);
		itemList.setItemColor(new Color(0.7f, 0.7f, 0.7f, 1f));
		itemList.setHoverColor(new Color(0.5f, 0.7f, 1f, 1f));
		itemList.setSelectedColor(new Color(0.2f, 0.5f, 1f, 1f));
		// Populate with sample items (TODO: load from game state)
		itemList.addItem("Health Potion");
		itemList.addItem("Mana Potion");
		itemList.addItem("Iron Sword");
		itemList.addItem("Leather Armor");
		menuPanel.add(itemList);
		
		// Close button
		closeButton = new UIButton(400, 110, 100, 40, "Close", menuFont);
		closeButton.onClick(() -> closeMenu());
		menuPanel.add(closeButton);
	}
	
	@Override
	public void update(float delta) {
		if (!visible || !menuOpen) {
			return;
		}
		
		// Update menu UI components
		menuPanel.update(delta);
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible || !menuOpen) {
			return;
		}
		
		// Render menu panel with items
		menuPanel.render(batch);
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!visible || !menuOpen || !enabled) {
			return;
		}
		
		// Delegate input to menu components
		menuPanel.onInput(event);
	}
	
	/**
	 * Open inventory menu
	 */
	public void openInventory() {
		menuOpen = true;
		menuTitle.setText("Inventory");
		setVisible(true);
		
		// Load actual inventory from game state
		try {
			java.util.List<String> inventoryItems = logicApi.getPlayerInventory();
			if (inventoryItems != null) {
				itemList.clearItems();
				for (String item : inventoryItems) {
					itemList.addItem(item);
				}
			}
		} catch (Exception e) {
			// If inventory not available, keep existing items
			Gdx.app.error("MenuLayer", "Failed to load inventory: " + e.getMessage());
		}
	}
	
	/**
	 * Open character screen
	 */
	public void openCharacterScreen() {
		menuOpen = true;
		menuTitle.setText("Character");
		setVisible(true);
	}
	
	/**
	 * Open spell/skills menu
	 */
	public void openSpellList() {
		menuOpen = true;
		menuTitle.setText("Spells & Skills");
		setVisible(true);
	}
	
	/**
	 * Open map view
	 */
	public void openMap() {
		menuOpen = true;
		menuTitle.setText("Map");
		setVisible(true);
	}
	
	/**
	 * Close current menu
	 */
	public void closeMenu() {
		menuOpen = false;
		setVisible(false);
	}
	
	/**
	 * Check if menu is open
	 */
	public boolean isMenuOpen() {
		return menuOpen;
	}
	
	/**
	 * Update menu items from game state
	 */
	public void setMenuItems(String[] items) {
		itemList.clearItems();
		for (String item : items) {
			itemList.addItem(item);
		}
	}
	
	@Override
	public void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
		
		// Recenter menu panel
		float panelWidth = 500;
		float panelHeight = 400;
		menuPanel.setPosition((width - panelWidth) / 2, (height - panelHeight) / 2);
		menuPanel.setSize(panelWidth, panelHeight);
	}
	
	@Override
	public void dispose() {
		if (menuFont != null) {
			menuFont.dispose();
		}
	}
}
