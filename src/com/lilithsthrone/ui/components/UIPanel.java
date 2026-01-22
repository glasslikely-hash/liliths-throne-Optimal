package com.lilithsthrone.ui.components;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Panel UI component - container for other UI elements.
 * 
 * Features:
 * - Holds child components
 * - Background color/texture
 * - Border styling
 * - Automatic child update/render
 * - Input delegation to children
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class UIPanel extends UIComponent {
	
	private List<UIComponent> children = new ArrayList<>();
	
	// Visual properties
	private Color backgroundColor = new Color(0.1f, 0.1f, 0.1f, 0.8f);
	private Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1f);
	private float borderThickness = 2f;
	private boolean drawBorder = true;
	private boolean drawBackground = true;
	
	/**
	 * Constructor
	 */
	public UIPanel(float x, float y, float width, float height) {
		super(x, y, width, height);
	}
	
	@Override
	public void update(float delta) {
		if (!visible) {
			return;
		}
		
		// Update all children
		for (UIComponent child : children) {
			if (child.isVisible()) {
				child.update(delta);
			}
		}
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible) {
			return;
		}
		
		// Draw background
		if (drawBackground) {
			drawRectangle(batch, x, y, width, height, backgroundColor);
		}
		
		// Draw border
		if (drawBorder) {
			drawRectangleOutline(batch, x, y, width, height, borderThickness, borderColor);
		}
		
		// Render all children
		for (UIComponent child : children) {
			if (child.isVisible()) {
				child.render(batch);
			}
		}
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!visible || !enabled) {
			return;
		}
		
		// Delegate to children in reverse order (top to bottom)
		for (int i = children.size() - 1; i >= 0; i--) {
			UIComponent child = children.get(i);
			if (child.isEnabled()) {
				child.onInput(event);
				if (event.isConsumed()) {
					return;  // Stop propagation if consumed
				}
			}
		}
	}
	
	/**
	 * Add child component.
	 */
	public UIPanel add(UIComponent child) {
		children.add(child);
		child.setParent(this);
		return this;
	}
	
	/**
	 * Remove child component.
	 */
	public void remove(UIComponent child) {
		children.remove(child);
		child.setParent(null);
	}
	
	/**
	 * Get child at index.
	 */
	public UIComponent getChild(int index) {
		return children.get(index);
	}
	
	/**
	 * Get number of children.
	 */
	public int getChildCount() {
		return children.size();
	}
	
	/**
	 * Clear all children.
	 */
	public void clearChildren() {
		children.clear();
	}
	
	/**
	 * Set background color.
	 */
	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}
	
	/**
	 * Set border color.
	 */
	public void setBorderColor(Color color) {
		this.borderColor = color;
	}
	
	/**
	 * Set border thickness.
	 */
	public void setBorderThickness(float thickness) {
		this.borderThickness = thickness;
	}
	
	/**
	 * Enable/disable background rendering.
	 */
	public void setDrawBackground(boolean draw) {
		this.drawBackground = draw;
	}
	
	/**
	 * Enable/disable border rendering.
	 */
	public void setDrawBorder(boolean draw) {
		this.drawBorder = draw;
	}
	
	/**
	 * Draw filled rectangle.
	 */
	private void drawRectangle(SpriteBatch batch, float x, float y, float width, float height, Color color) {
		batch.setColor(color);
		batch.draw(batch.getBlendingDisabledTexture(), x, y, width, height);
	}
	
	/**
	 * Draw rectangle outline.
	 */
	private void drawRectangleOutline(SpriteBatch batch, float x, float y, float width, float height, float thickness, Color color) {
		batch.setColor(color);
		// Top
		batch.draw(batch.getBlendingDisabledTexture(), x, y + height - thickness, width, thickness);
		// Bottom
		batch.draw(batch.getBlendingDisabledTexture(), x, y, width, thickness);
		// Left
		batch.draw(batch.getBlendingDisabledTexture(), x, y, thickness, height);
		// Right
		batch.draw(batch.getBlendingDisabledTexture(), x + width - thickness, y, thickness, height);
	}
}
