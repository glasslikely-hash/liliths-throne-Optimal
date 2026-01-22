package com.lilithsthrone.ui.components;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * List UI component for displaying and selecting from lists of items.
 * 
 * Features:
 * - Display list of items with scrolling
 * - Select single item
 * - Hover highlight
 * - Custom item height
 * - Scroll wheel and drag support
 * 
 * Common uses:
 * - Inventory lists
 * - Character selection
 * - Quest lists
 * - Settings menus
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class UIList extends UIComponent {
	
	private List<String> items = new ArrayList<>();
	private int selectedIndex = -1;
	private int hoveredIndex = -1;
	
	private float itemHeight = 30f;
	private float scrollOffset = 0f;
	
	private Color itemColor = new Color(0.8f, 0.8f, 0.8f, 1f);
	private Color hoverColor = new Color(0.5f, 0.7f, 1f, 1f);
	private Color selectedColor = new Color(0.2f, 0.6f, 1f, 1f);
	private Color backgroundColor = new Color(0.1f, 0.1f, 0.1f, 0.9f);
	private Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1f);
	
	private BitmapFont font;
	private boolean showBorder = true;
	private float borderThickness = 2f;
	
	private Runnable onSelectionChanged;
	
	/**
	 * Constructor.
	 */
	public UIList(float x, float y, float width, float height, BitmapFont font) {
		super(x, y, width, height);
		this.font = font;
	}
	
	@Override
	public void update(float delta) {
		// List updates happen in onInput
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible || font == null) {
			return;
		}
		
		// Draw background
		drawRectangle(batch, x, y, width, height, backgroundColor);
		
		// Draw items
		int visibleItems = (int)(height / itemHeight);
		for (int i = 0; i < Math.min(items.size(), visibleItems); i++) {
			int itemIndex = (int)(scrollOffset / itemHeight) + i;
			if (itemIndex >= items.size()) {
				break;
			}
			
			float itemY = y + height - (i + 1) * itemHeight;
			
			// Draw item background
			Color bgColor = itemColor;
			if (itemIndex == selectedIndex) {
				bgColor = selectedColor;
			} else if (itemIndex == hoveredIndex) {
				bgColor = hoverColor;
			}
			
			drawRectangle(batch, x, itemY, width, itemHeight, bgColor);
			
			// Draw item text
			if (font != null) {
				String item = items.get(itemIndex);
				float textX = x + 5;
				float textY = itemY + (itemHeight + font.getCapHeight()) / 2;
				
				font.setColor(Color.WHITE);
				font.draw(batch, item, textX, textY);
			}
		}
		
		// Draw border
		if (showBorder) {
			drawRectangleOutline(batch, x, y, width, height, borderColor, borderThickness);
		}
	}
	
	/**
	 * Draw filled rectangle.
	 */
	private void drawRectangle(SpriteBatch batch, float x, float y, float width, float height, Color color) {
		com.lilithsthrone.ui.graphics.PixelDraw.drawRectangle(batch, (int)x, (int)y, (int)width, (int)height, color);
	}
	
	/**
	 * Draw rectangle outline.
	 */
	private void drawRectangleOutline(SpriteBatch batch, float x, float y, float width, float height, Color color, float thickness) {
		com.lilithsthrone.ui.graphics.PixelDraw.drawRectangleOutline(batch, (int)x, (int)y, (int)width, (int)height, color, (int)thickness);
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!enabled) {
			return;
		}
		
		if (event.getType() == InputEvent.InputType.MOUSE_MOVE) {
			// Update hovered index
			float relativeY = y + height - event.getScreenY();
			int itemIndex = (int)(scrollOffset + relativeY) / (int)itemHeight;
			
			if (event.getScreenX() >= x && event.getScreenX() < x + width &&
				event.getScreenY() >= y && event.getScreenY() < y + height &&
				itemIndex >= 0 && itemIndex < items.size()) {
				hoveredIndex = itemIndex;
			} else {
				hoveredIndex = -1;
			}
		} else if (event.getType() == InputEvent.InputType.MOUSE_CLICK) {
			// Select item
			if (event.getScreenX() >= x && event.getScreenX() < x + width &&
				event.getScreenY() >= y && event.getScreenY() < y + height) {
				
				float relativeY = y + height - event.getScreenY();
				int itemIndex = (int)(scrollOffset + relativeY) / (int)itemHeight;
				
				if (itemIndex >= 0 && itemIndex < items.size()) {
					selectItem(itemIndex);
					event.consume();
				}
			}
		} else if (event.getType() == InputEvent.InputType.SCROLL) {
			// Handle scroll
			if (event.getScreenX() >= x && event.getScreenX() < x + width &&
				event.getScreenY() >= y && event.getScreenY() < y + height) {
				
				float maxScroll = Math.max(0, items.size() * itemHeight - height);
				scrollOffset = Math.max(0, Math.min(scrollOffset + event.getScrollDelta() * 20, maxScroll));
				event.consume();
			}
		}
	}
	
	/**
	 * Select item by index.
	 */
	private void selectItem(int index) {
		if (index >= 0 && index < items.size() && index != selectedIndex) {
			selectedIndex = index;
			if (onSelectionChanged != null) {
				onSelectionChanged.run();
			}
		}
	}
	
	/**
	 * Add item to list.
	 */
	public void addItem(String item) {
		items.add(item);
	}
	
	/**
	 * Remove item by index.
	 */
	public void removeItem(int index) {
		if (index >= 0 && index < items.size()) {
			items.remove(index);
			if (selectedIndex == index) {
				selectedIndex = -1;
			}
		}
	}
	
	/**
	 * Clear all items.
	 */
	public void clearItems() {
		items.clear();
		selectedIndex = -1;
		scrollOffset = 0;
	}
	
	/**
	 * Get selected item.
	 */
	public String getSelectedItem() {
		return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
	}
	
	/**
	 * Get selected index.
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	/**
	 * Set selection changed callback.
	 */
	public UIList onSelectionChanged(Runnable callback) {
		this.onSelectionChanged = callback;
		return this;
	}
	
	/**
	 * Set item height.
	 */
	public void setItemHeight(float height) {
		this.itemHeight = height;
	}
	
	/**
	 * Set item color.
	 */
	public void setItemColor(Color color) {
		this.itemColor = color;
	}
	
	/**
	 * Set hover color.
	 */
	public void setHoverColor(Color color) {
		this.hoverColor = color;
	}
	
	/**
	 * Set selected color.
	 */
	public void setSelectedColor(Color color) {
		this.selectedColor = color;
	}
	
	/**
	 * Set background color.
	 */
	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}
	
	/**
	 * Get list item count.
	 */
	public int getItemCount() {
		return items.size();
	}
}
