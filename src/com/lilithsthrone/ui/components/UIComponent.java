package com.lilithsthrone.ui.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Base class for all UI components.
 * 
 * Replaces JavaFX Node hierarchy.
 * 
 * Provides:
 * - Positioning and sizing
 * - Rendering
 * - Input handling
 * - Visibility/enabled state
 * - Parent-child hierarchy
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public abstract class UIComponent {
	
	protected float x;
	protected float y;
	protected float width;
	protected float height;
	
	protected boolean visible = true;
	protected boolean enabled = true;
	protected float alpha = 1.0f;
	
	protected UIComponent parent;
	
	/**
	 * Constructor
	 */
	public UIComponent(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Update component state
	 */
	public abstract void update(float delta);
	
	/**
	 * Render component
	 */
	public abstract void render(SpriteBatch batch);
	
	/**
	 * Handle input event
	 */
	public abstract void onInput(InputEvent event);
	
	/**
	 * Check if point is inside component
	 */
	public boolean contains(float px, float py) {
		return px >= x && px < x + width && py >= y && py < y + height;
	}
	
	/**
	 * Get bounds rectangle
	 */
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
	// Getters and setters
	
	public float getX() { return x; }
	public void setX(float x) { this.x = x; }
	
	public float getY() { return y; }
	public void setY(float y) { this.y = y; }
	
	public float getWidth() { return width; }
	public void setWidth(float width) { this.width = width; }
	
	public float getHeight() { return height; }
	public void setHeight(float height) { this.height = height; }
	
	public boolean isVisible() { return visible; }
	public void setVisible(boolean visible) { this.visible = visible; }
	
	public boolean isEnabled() { return enabled; }
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	
	public float getAlpha() { return alpha; }
	public void setAlpha(float alpha) { this.alpha = alpha; }
	
	public UIComponent getParent() { return parent; }
	public void setParent(UIComponent parent) { this.parent = parent; }
}
