package com.lilithsthrone.ui.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Slider UI component for selecting numeric values.
 * 
 * Features:
 * - Drag handle to adjust value
 * - Click on track to jump to position
 * - Customizable range (min to max)
 * - Optional text label
 * - Step size for discrete values
 * 
 * Common uses:
 * - Volume control
 * - Brightness/contrast
 * - Game settings
 * - Value selection
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class UISlider extends UIComponent {
	
	private float minValue = 0f;
	private float maxValue = 100f;
	private float currentValue = 50f;
	private float stepSize = 1f;
	
	private float handleWidth = 20f;
	private float handleHeight = 20f;
	private boolean isDragging = false;
	
	private Color trackColor = new Color(0.3f, 0.3f, 0.3f, 1f);
	private Color handleColor = new Color(0.7f, 0.7f, 0.7f, 1f);
	private Color handleHoverColor = new Color(1f, 1f, 1f, 1f);
	private Color fillColor = new Color(0.2f, 0.6f, 1f, 1f);
	
	private BitmapFont font;
	private boolean showValue = false;
	private String label;
	
	private Runnable onValueChanged;
	
	/**
	 * Constructor.
	 */
	public UISlider(float x, float y, float width, float height) {
		super(x, y, width, height);
	}
	
	/**
	 * Constructor with font for text display.
	 */
	public UISlider(float x, float y, float width, float height, BitmapFont font) {
		this(x, y, width, height);
		this.font = font;
	}
	
	@Override
	public void update(float delta) {
		// Slider updates happen in onInput
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible) {
			return;
		}
		
		float trackY = y + height / 2 - 2;
		float trackHeight = 4f;
		
		// Draw track background
		drawRectangle(batch, x, trackY, width, trackHeight, trackColor);
		
		// Draw fill (completed portion)
		float fillWidth = ((currentValue - minValue) / (maxValue - minValue)) * width;
		drawRectangle(batch, x, trackY, fillWidth, trackHeight, fillColor);
		
		// Draw handle
		float handleX = x + fillWidth - handleWidth / 2;
		float handleY = y + (height - handleHeight) / 2;
		Color handleDrawColor = isDragging ? handleHoverColor : handleColor;
		drawRectangle(batch, handleX, handleY, handleWidth, handleHeight, handleDrawColor);
		
		// Draw border around handle
		Color borderColor = new Color(0.5f, 0.5f, 0.5f, 1f);
		drawRectangleOutline(batch, handleX, handleY, handleWidth, handleHeight, borderColor, 1);
		
		// Draw text if enabled
		if (showValue && font != null) {
			drawText(batch);
		}
	}
	
	/**
	 * Draw slider text.
	 */
	private void drawText(SpriteBatch batch) {
		String text;
		if (label != null) {
			text = label + ": " + String.format("%.0f", currentValue);
		} else {
			text = String.format("%.0f", currentValue);
		}
		
		com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, text);
		float textX = x + width + 10;
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
		if (!enabled) {
			return;
		}
		
		float trackY = y + height / 2 - 2;
		float trackHeight = 4f;
		float fillWidth = ((currentValue - minValue) / (maxValue - minValue)) * width;
		float handleX = x + fillWidth - handleWidth / 2;
		float handleY = y + (height - handleHeight) / 2;
		
		if (event.getType() == InputEvent.InputType.MOUSE_DOWN) {
			// Check if clicking on handle
			if (event.getScreenX() >= handleX && event.getScreenX() < handleX + handleWidth &&
				event.getScreenY() >= handleY && event.getScreenY() < handleY + handleHeight) {
				isDragging = true;
				event.consume();
			}
			// Check if clicking on track
			else if (event.getScreenX() >= x && event.getScreenX() < x + width &&
					 event.getScreenY() >= trackY && event.getScreenY() < trackY + trackHeight) {
				updateValueFromMouse(event.getScreenX());
				isDragging = true;
				event.consume();
			}
		} else if (event.getType() == InputEvent.InputType.MOUSE_UP) {
			isDragging = false;
			event.consume();
		} else if (event.getType() == InputEvent.InputType.MOUSE_MOVE && isDragging) {
			updateValueFromMouse(event.getScreenX());
			event.consume();
		}
	}
	
	/**
	 * Update value based on mouse position.
	 */
	private void updateValueFromMouse(float mouseX) {
		float relativePos = (mouseX - x) / width;
		relativePos = Math.max(0, Math.min(1, relativePos));
		
		float newValue = minValue + relativePos * (maxValue - minValue);
		
		// Apply step size
		if (stepSize > 0) {
			newValue = Math.round(newValue / stepSize) * stepSize;
		}
		
		if (newValue != currentValue) {
			currentValue = newValue;
			if (onValueChanged != null) {
				onValueChanged.run();
			}
		}
	}
	
	/**
	 * Set value range.
	 */
	public void setRange(float min, float max) {
		this.minValue = min;
		this.maxValue = Math.max(min + 1, max);
		this.currentValue = Math.max(min, Math.min(currentValue, maxValue));
	}
	
	/**
	 * Set current value.
	 */
	public void setValue(float value) {
		this.currentValue = Math.max(minValue, Math.min(value, maxValue));
	}
	
	/**
	 * Get current value.
	 */
	public float getValue() {
		return currentValue;
	}
	
	/**
	 * Set step size (0 = continuous).
	 */
	public void setStepSize(float step) {
		this.stepSize = step;
	}
	
	/**
	 * Set value changed callback.
	 */
	public UISlider onValueChanged(Runnable callback) {
		this.onValueChanged = callback;
		return this;
	}
	
	/**
	 * Enable/disable value display.
	 */
	public void setShowValue(boolean show) {
		this.showValue = show;
	}
	
	/**
	 * Set label text.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Set track color.
	 */
	public void setTrackColor(Color color) {
		this.trackColor = color;
	}
	
	/**
	 * Set handle color.
	 */
	public void setHandleColor(Color color) {
		this.handleColor = color;
	}
	
	/**
	 * Set fill color.
	 */
	public void setFillColor(Color color) {
		this.fillColor = color;
	}
}
