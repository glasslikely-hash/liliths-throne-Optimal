package com.lilithsthrone.ui.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputEvent;

/**
 * Map Layer - Renders the game world.
 * 
 * Displays:
 * - Terrain and environment
 * - Character sprites
 * - NPC sprites
 * - Objects and items on ground
 * - Lighting and fog of war
 * 
 * Replaces JavaFX/SVG-based world rendering.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class MapLayer extends UILayer {
	
	private static final String LAYER_NAME = "MapLayer";
	
	private float cameraX;
	private float cameraY;
	private float zoomLevel = 1.0f;
	
	private float screenWidth = 800;
	private float screenHeight = 600;
	
	private static final int TILE_SIZE = 32;
	private static final int WORLD_WIDTH = 20;   // tiles
	private static final int WORLD_HEIGHT = 15;  // tiles
	
	public MapLayer(LogicLayerAPI logicApi) {
		super(logicApi);
		cameraX = 5f;
		cameraY = 7f;
	}
	
	@Override
	public void update(float delta) {
		if (!visible) {
			return;
		}
		
		// Update camera position based on player location
		try {
			String playerLocation = logicApi.getPlayerLocation();
			int playerX = logicApi.getPlayerX();
			int playerY = logicApi.getPlayerY();
			
			// Smooth camera follow (lerp towards player position)
			float targetCameraX = playerX;
			float targetCameraY = playerY;
			float cameraSpeed = 5f;  // Camera smoothing factor
			
			cameraX += (targetCameraX - cameraX) * cameraSpeed * delta;
			cameraY += (targetCameraY - cameraY) * cameraSpeed * delta;
			
		} catch (Exception e) {
			// If player position not available, keep current camera position
		}
		
		// Update animations, effects, and NPC states from game world state
	}
	
	@Override
	public void render(SpriteBatch batch) {
		if (!visible) {
			return;
		}
		
		// Render terrain tiles
		renderTerrain(batch);
		
		// Render objects on ground
		renderGround(batch);
		
		// Render characters and NPCs
		renderCharacters(batch);
		
		// Render effects (fog of war, lighting, etc)
		renderEffects(batch);
	}
	
	/**
	 * Render terrain grid
	 */
	private void renderTerrain(SpriteBatch batch) {
		// Calculate visible tile range based on camera
		int startTileX = Math.max(0, (int)(cameraX - screenWidth / (TILE_SIZE * 2 * zoomLevel)));
		int endTileX = Math.min(WORLD_WIDTH, (int)(cameraX + screenWidth / (TILE_SIZE * 2 * zoomLevel)) + 2);
		int startTileY = Math.max(0, (int)(cameraY - screenHeight / (TILE_SIZE * 2 * zoomLevel)));
		int endTileY = Math.min(WORLD_HEIGHT, (int)(cameraY + screenHeight / (TILE_SIZE * 2 * zoomLevel)) + 2);
		
		// Render visible tiles
		for (int x = startTileX; x < endTileX; x++) {
			for (int y = startTileY; y < endTileY; y++) {
				float screenX = (x - cameraX) * TILE_SIZE * zoomLevel + screenWidth / 2;
				float screenY = (y - cameraY) * TILE_SIZE * zoomLevel + screenHeight / 2;
				
				// Draw terrain tile (grass by default)
				Color tileColor = getTileColor(x, y);
				drawTile(batch, screenX, screenY, TILE_SIZE, TILE_SIZE, tileColor);
				
				// Draw grid lines
				drawGridLine(batch, screenX, screenY, TILE_SIZE);
			}
		}
	}
	
	/**
	 * Get color for tile at coordinates
	 */
	private Color getTileColor(int x, int y) {
		// TODO: Query tile type from game state
		// For now, alternate pattern
		if ((x + y) % 2 == 0) {
			return new Color(0.3f, 0.4f, 0.2f, 1f);  // Dark grass
		} else {
			return new Color(0.4f, 0.5f, 0.3f, 1f);  // Light grass
		}
	}
	
	/**
	 * Render ground objects and items
	 */
	private void renderGround(SpriteBatch batch) {
		// TODO: Query game objects from LogicLayerAPI
		// Render items, corpses, furniture, etc.
	}
	
	/**
	 * Render characters and NPCs
	 */
	private void renderCharacters(SpriteBatch batch) {
		// TODO: Query player and NPCs from LogicLayerAPI
		// Render character sprites at their locations
		// Draw name labels above characters
	}
	
	/**
	 * Render visual effects
	 */
	private void renderEffects(SpriteBatch batch) {
		// TODO: Render fog of war
		// TODO: Render lighting overlays
		// TODO: Render auras and status effects
	}
	
	/**
	 * Draw a single tile
	 */
	private void drawTile(SpriteBatch batch, float x, float y, float width, float height, Color color) {
		com.lilithsthrone.ui.graphics.PixelDraw.drawRectangle(batch, (int)x, (int)y, (int)width, (int)height, color);
	}
	
	/**
	 * Draw grid lines around tile
	 */
	private void drawGridLine(SpriteBatch batch, float x, float y, float size) {
		Color gridColor = new Color(0.2f, 0.2f, 0.2f, 0.5f);
		com.lilithsthrone.ui.graphics.PixelDraw.drawRectangleOutline(batch, (int)x, (int)y, (int)size, (int)size, gridColor, 1);
	}
	
	@Override
	public void onInput(InputEvent event) {
		// Handle world interaction
		if (event.getType() == InputEvent.InputType.MOUSE_CLICK) {
			// Convert screen coordinates to world coordinates
			float worldX = (event.getScreenX() - screenWidth / 2) / (TILE_SIZE * zoomLevel) + cameraX;
			float worldY = (event.getScreenY() - screenHeight / 2) / (TILE_SIZE * zoomLevel) + cameraY;
			
			// Handle interaction (move, attack, etc)
			// TODO: Call LogicLayerAPI methods to perform actions
			event.consume();
		}
	}
	
	/**
	 * Set camera position
	 */
	public void setCameraPosition(float x, float y) {
		this.cameraX = x;
		this.cameraY = y;
	}
	
	/**
	 * Get zoom level
	 */
	public float getZoomLevel() {
		return zoomLevel;
	}
	
	/**
	 * Set zoom level
	 */
	public void setZoomLevel(float zoom) {
		this.zoomLevel = Math.max(0.5f, Math.min(3f, zoom));  // Clamp to 0.5x - 3x
	}
	
	@Override
	public void resize(int width, int height) {
		screenWidth = width;
		screenHeight = height;
	}
	
	@Override
	public void dispose() {
		// Nothing to dispose
	}
}
