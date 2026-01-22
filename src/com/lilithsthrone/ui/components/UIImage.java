package com.lilithsthrone.ui.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Image UI component for displaying sprites and textures.
 * 
 * Features:
 * - Display full textures or texture regions
 * - Scaling to fit component bounds
 * - Rotation and tint color
 * - Click handling for image buttons
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class UIImage extends UIComponent {
	
	private TextureRegion textureRegion;
	private Sprite sprite;
	private boolean scaleToFit = true;
	private float rotation = 0f;
	
	private Runnable onClickCallback;
	
	/**
	 * Constructor with Texture
	 */
	public UIImage(float x, float y, float width, float height, Texture texture) {
		this(x, y, width, height, new TextureRegion(texture));
	}
	
	/**
	 * Constructor with TextureRegion
	 */
	public UIImage(float x, float y, float width, float height, TextureRegion textureRegion) {
		super(x, y, width, height);
		this.textureRegion = textureRegion;
		this.sprite = new Sprite(textureRegion);
		updateSpriteSize();
	}
	
	@Override
	public void update(float delta) {
		// Image components don't need updates
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible || textureRegion == null) {
			return;
		}
		
		sprite.draw(batch);
	}
	
	@Override
	public void onInput(InputEvent event) {
		if (!enabled) {
			return;
		}
		
		if (event.getType() == InputEvent.InputType.MOUSE_CLICK) {
			if (contains(event.getScreenX(), event.getScreenY())) {
				if (onClickCallback != null) {
					onClickCallback.run();
				}
				event.consume();
			}
		}
	}
	
	/**
	 * Check if point is inside image.
	 */
	private boolean contains(float px, float py) {
		return px >= x && px < x + width && py >= y && py < y + height;
	}
	
	/**
	 * Update sprite size based on component bounds.
	 */
	private void updateSpriteSize() {
		if (sprite == null) {
			return;
		}
		
		sprite.setPosition(x, y);
		
		if (scaleToFit) {
			// Scale to fit within bounds while maintaining aspect ratio
			float textureWidth = textureRegion.getRegionWidth();
			float textureHeight = textureRegion.getRegionHeight();
			
			if (textureWidth > 0 && textureHeight > 0) {
				float aspectRatio = textureWidth / textureHeight;
				float targetWidth = width;
				float targetHeight = width / aspectRatio;
				
				if (targetHeight > height) {
					targetHeight = height;
					targetWidth = height * aspectRatio;
				}
				
				sprite.setSize(targetWidth, targetHeight);
			}
		} else {
			sprite.setSize(width, height);
		}
	}
	
	/**
	 * Set texture region.
	 */
	public void setTextureRegion(TextureRegion textureRegion) {
		this.textureRegion = textureRegion;
		if (sprite != null) {
			sprite.setRegion(textureRegion);
			updateSpriteSize();
		}
	}
	
	/**
	 * Set rotation in degrees.
	 */
	public void setRotation(float degrees) {
		this.rotation = degrees;
		if (sprite != null) {
			sprite.setRotation(degrees);
		}
	}
	
	/**
	 * Enable/disable scaling to fit.
	 */
	public void setScaleToFit(boolean scale) {
		this.scaleToFit = scale;
		updateSpriteSize();
	}
	
	/**
	 * Set click callback.
	 */
	public UIImage onClick(Runnable callback) {
		this.onClickCallback = callback;
		return this;
	}
	
	/**
	 * Override position to update sprite.
	 */
	@Override
	public void setPosition(float x, float y) {
		super.setPosition(x, y);
		if (sprite != null) {
			sprite.setPosition(x, y);
		}
	}
	
	/**
	 * Override size to update sprite.
	 */
	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		updateSpriteSize();
	}
}
