package com.lilithsthrone.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Progress bar UI component for displaying status bars.
 * 
 * Features:
 * - Display progress as percentage fill
 * - Customizable colors for background, fill, and border
 * - Optional text label display
 * - Smooth value transitions (lerping)
 * 
 * Common uses:
 * - Health bars
 * - Mana bars
 * - Experience/level progress
 * - Loading indicators
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class UIProgressBar extends UIComponent {
	
	private float currentValue = 0f;
	private float maxValue = 100f;
	private float displayValue = 0f;  // Animated value for smooth transitions
	private float smoothSpeed = 5f;   // Lerp speed
	
	private Color backgroundColor = new Color(0.1f, 0.1f, 0.1f, 0.8f);
	private Color fillColor = new Color(0f, 1f, 0f, 1f);
	private Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1f);
	private float borderThickness = 2f;
	
	private BitmapFont font;
	private boolean showText = false;
	private String customLabel = null;
	
	/**
	 * Constructor.
	 */
	public UIProgressBar(float x, float y, float width, float height) {
		super(x, y, width, height);
		this.displayValue = currentValue;
	}
	
	/**
	 * Constructor with font for text display.
	 */
	public UIProgressBar(float x, float y, float width, float height, BitmapFont font) {
		this(x, y, width, height);
		this.font = font;
	}
	
	@Override
	public void update(float delta) {
		// Smoothly transition display value toward current value
		if (displayValue != currentValue) {
			float diff = currentValue - displayValue;
			float step = smoothSpeed * delta * maxValue;
			
			if (Math.abs(diff) < step) {
				displayValue = currentValue;
			} else {
				displayValue += (diff > 0 ? step : -step);
			}
		}
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible) {
			return;
		}
		
		// Draw background
		drawRectangle(batch, x, y, width, height, backgroundColor);
		
		// Draw fill
		float fillWidth = (displayValue / maxValue) * width;
		if (fillWidth > 0) {
			drawRectangle(batch, x, y, fillWidth, height, fillColor);
		}
		
		// Draw border
		drawRectangleOutline(batch, x, y, width, height, borderColor, borderThickness);
		
		// Draw text if enabled
		if (showText && font != null) {
			drawText(batch);
		}
	}
	
	/**
	 * Draw progress bar text.
	 */
	private void drawText(SpriteBatch batch) {
		String text;
		if (customLabel != null) {
			text = customLabel;
		} else {
			text = String.format("%.0f%%", (currentValue / maxValue) * 100);
		}
		
		com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, text);
		float textX = x + (width - layout.width) / 2;
		float textY = y + (height + font.getCapHeight()) / 2;
		
		font.draw(batch, text, textX, textY);
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
		// Progress bars typically don't handle input
	}
	
	/**
	 * Set current value (0 to maxValue).
	 */
	public void setValue(float value) {
		this.currentValue = Math.max(0, Math.min(value, maxValue));
	}
	
	/**
	 * Get current value.
	 */
	public float getValue() {
		return currentValue;
	}
	
	/**
	 * Set maximum value.
	 */
	public void setMaxValue(float max) {
		this.maxValue = Math.max(1, max);
		this.currentValue = Math.min(currentValue, maxValue);
	}
	
	/**
	 * Get maximum value.
	 */
	public float getMaxValue() {
		return maxValue;
	}
	
	/**
	 * Get current percentage (0-1).
	 */
	public float getPercentage() {
		return currentValue / maxValue;
	}
	
	/**
	 * Set fill color.
	 */
	public void setFillColor(Color color) {
		this.fillColor = color;
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
	 * Enable/disable text display.
	 */
	public void setShowText(boolean show) {
		this.showText = show;
	}
	
	/**
	 * Set custom label text.
	 */
	public void setCustomLabel(String label) {
		this.customLabel = label;
	}
	
	/**
	 * Set smooth transition speed (0.0f = instant, higher = slower).
	 */
	public void setSmoothSpeed(float speed) {
		this.smoothSpeed = Math.max(0, speed);
	}
}
