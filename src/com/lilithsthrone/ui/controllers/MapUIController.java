package com.lilithsthrone.ui.controllers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lilithsthrone.logic.LogicLayerAPI;
import com.lilithsthrone.ui.input.InputManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Map UI Controller
 * 
 * Displays world map and location navigation.
 * 
 * Features:
 *  - World map with discovered locations
 *  - Minimap showing nearby areas
 *  - Location markers and icons
 *  - Fast travel to discovered locations
 *  - Quest markers on map
 *  - Current location display
 *  - Zoom in/out
 * 
 * @since Step 3
 * @version 1.0
 */
public class MapUIController extends UIControllerBase {

	private static final String CONTROLLER_NAME = "MapUIController";

	// UI elements
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private BitmapFont fontLarge;

	// Map display
	private float mapX, mapY;
	private static final float MAP_WIDTH = 800f;
	private static final float MAP_HEIGHT = 600f;
	private float mapZoom = 1.0f;
	private static final float MIN_ZOOM = 0.5f;
	private static final float MAX_ZOOM = 3.0f;

	// Map offset (panning)
	private float mapOffsetX = 0f;
	private float mapOffsetY = 0f;

	// Locations
	private List<MapLocation> discoveredLocations = new ArrayList<>();
	private MapLocation selectedLocation = null;
	private String currentLocationName = "";

	// Location details panel
	private float detailsX, detailsY;
	private static final float DETAILS_WIDTH = 250f;
	private static final float DETAILS_HEIGHT = 200f;

	// Inner class for map locations
	private static class MapLocation {
		String name;
		float mapX, mapY;
		boolean discovered;
		boolean hasQuestMarker;
		Color markerColor;

		MapLocation(String name, float mapX, float mapY) {
			this.name = name;
			this.mapX = mapX;
			this.mapY = mapY;
			this.discovered = false;
			this.hasQuestMarker = false;
			this.markerColor = Color.GRAY;
		}
	}

	/**
	 * Constructor for map UI controller
	 */
	public MapUIController(SpriteBatch batch, OrthographicCamera camera,
	                       LogicLayerAPI logicLayerAPI, InputManager inputManager) {
		super(batch, camera, logicLayerAPI, inputManager);
		this.shapeRenderer = new ShapeRenderer();
		this.font = new BitmapFont();
		this.fontLarge = new BitmapFont();
	}

	@Override
	public void initialize() {
		System.out.println("[" + CONTROLLER_NAME + "] Initializing map UI");
		mapX = 20f;
		mapY = (screenHeight - MAP_HEIGHT) / 2f;

		detailsX = mapX + MAP_WIDTH + 30f;
		detailsY = mapY + MAP_HEIGHT - DETAILS_HEIGHT;

		loadMapLocations();
		updateCurrentLocation();
	}

	/**
	 * Load available map locations from game
	 */
	private void loadMapLocations() {
		try {
			List<String> locationNames = logicLayerAPI.getMapLocations();
			discoveredLocations.clear();

			if (locationNames != null) {
				for (String locName : locationNames) {
					// Generate map coordinates (simplified - would normally come from game data)
					float x = (float) (Math.random() * 700) + 50;
					float y = (float) (Math.random() * 500) + 50;

					MapLocation loc = new MapLocation(locName, x, y);

					// Check if discovered
					if (logicLayerAPI.isLocationDiscovered(locName)) {
						loc.discovered = true;
						loc.markerColor = Color.WHITE;
					} else {
						loc.discovered = false;
						loc.markerColor = Color.DARK_GRAY;
					}

					// Check for quest markers
					if (logicLayerAPI.hasQuestMarkerAt(locName)) {
						loc.hasQuestMarker = true;
						loc.markerColor = Color.YELLOW;
					}

					discoveredLocations.add(loc);
				}
			}
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error loading map locations: " + e.getMessage());
		}
	}

	/**
	 * Update the current location
	 */
	private void updateCurrentLocation() {
		try {
			currentLocationName = logicLayerAPI.getCurrentLocation();
		} catch (Exception e) {
			currentLocationName = "Unknown";
		}
	}

	@Override
	public void update(float deltaTime) {
		if (!isActive || !isVisible) {
			return;
		}

		// Periodically update map state
	}

	@Override
	public void render(float deltaTime) {
		if (!isVisible) {
			return;
		}

		batch.begin();
		batch.setColor(Color.WHITE);

		// Render map background
		renderMapBackground();

		// Render locations on map
		renderMapLocations();

		// Render location details panel
		renderDetailsPanel();

		batch.end();

		// Render map grid/borders
		renderMapBorders();
	}

	/**
	 * Render map background
	 */
	private void renderMapBackground() {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Background
		shapeRenderer.setColor(0.05f, 0.05f, 0.1f, 0.95f);
		shapeRenderer.rect(mapX, mapY, MAP_WIDTH, MAP_HEIGHT);

		// Border
		shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1f);
		shapeRenderer.rect(mapX, mapY, MAP_WIDTH, MAP_HEIGHT);

		shapeRenderer.end();

		// Map label
		fontLarge.setColor(Color.YELLOW);
		fontLarge.draw(batch, "World Map", mapX + 20, mapY + MAP_HEIGHT - 30);
	}

	/**
	 * Render location markers on the map
	 */
	private void renderMapLocations() {
		for (MapLocation loc : discoveredLocations) {
			if (!loc.discovered) {
				continue; // Don't show undiscovered locations
			}

			// Scale location coordinates to map display
			float screenX = mapX + (loc.mapX * mapZoom) + mapOffsetX;
			float screenY = mapY + (loc.mapY * mapZoom) + mapOffsetY;

			// Skip if off-screen
			if (screenX < mapX || screenX > mapX + MAP_WIDTH || screenY < mapY || screenY > mapY + MAP_HEIGHT) {
				continue;
			}

			// Determine marker color
			Color markerColor = loc.markerColor;
			if (loc == selectedLocation) {
				markerColor = Color.CYAN;
			}

			// Draw marker circle
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(markerColor);
			shapeRenderer.circle(screenX, screenY, 8f);

			// Marker border
			shapeRenderer.setColor(Color.WHITE);
			shapeRenderer.circle(screenX, screenY, 8f);
			shapeRenderer.end();

			// Quest marker indicator
			if (loc.hasQuestMarker) {
				shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
				shapeRenderer.setColor(Color.YELLOW);
				shapeRenderer.rect(screenX - 12, screenY + 12, 24f, 8f);
				shapeRenderer.end();
			}

			// Location label (on hover or selection)
			if (loc == selectedLocation) {
				font.setColor(Color.CYAN);
				font.draw(batch, loc.name, screenX + 15, screenY + 5);
			}
		}

		// Draw player position (center of map)
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.LIME);
		shapeRenderer.circle(mapX + MAP_WIDTH / 2, mapY + MAP_HEIGHT / 2, 6f);

		shapeRenderer.setColor(Color.WHITE);
		shapeRenderer.circle(mapX + MAP_WIDTH / 2, mapY + MAP_HEIGHT / 2, 6f);
		shapeRenderer.end();
	}

	/**
	 * Render location details panel
	 */
	private void renderDetailsPanel() {
		if (selectedLocation == null) {
			return;
		}

		// Panel background
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.95f);
		shapeRenderer.rect(detailsX, detailsY, DETAILS_WIDTH, DETAILS_HEIGHT);

		// Panel border
		shapeRenderer.setColor(0.8f, 0.6f, 0.2f, 1f);
		shapeRenderer.rect(detailsX, detailsY, DETAILS_WIDTH, DETAILS_HEIGHT);
		shapeRenderer.end();

		// Location name
		fontLarge.setColor(Color.YELLOW);
		fontLarge.draw(batch, selectedLocation.name, detailsX + 10, detailsY + DETAILS_HEIGHT - 30);

		// Location status
		font.setColor(Color.WHITE);
		font.draw(batch, "Discovered: Yes", detailsX + 10, detailsY + DETAILS_HEIGHT - 70);

		if (selectedLocation.hasQuestMarker) {
			font.setColor(Color.YELLOW);
			font.draw(batch, "Quest Marker: Active", detailsX + 10, detailsY + DETAILS_HEIGHT - 90);
		}

		// Travel button
		font.setColor(Color.CYAN);
		font.draw(batch, "Press T to Travel", detailsX + 10, detailsY + 20);
	}

	/**
	 * Render map borders and UI elements
	 */
	private void renderMapBorders() {
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Zoom level indicator
		String zoomText = String.format("Zoom: %.1fx", mapZoom);
		font.setColor(Color.WHITE);
		font.draw(batch, zoomText, mapX + 20, mapY + 30);

		// Zoom controls hint
		font.setColor(Color.GRAY);
		font.draw(batch, "Scroll to zoom, Click to select", mapX + 20, mapY + 10);

		shapeRenderer.end();
	}

	/**
	 * Select a location on the map
	 * 
	 * @param location The location to select
	 */
	public void selectLocation(MapLocation location) {
		this.selectedLocation = location;
	}

	/**
	 * Travel to the selected location
	 * 
	 * @return true if travel was successful
	 */
	public boolean travelToSelected() {
		if (selectedLocation == null) {
			return false;
		}

		try {
			logicLayerAPI.travelToLocation(selectedLocation.name);
			updateCurrentLocation();
			return true;
		} catch (Exception e) {
			System.err.println("[" + CONTROLLER_NAME + "] Error traveling to location: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Zoom the map in
	 */
	public void zoomIn() {
		mapZoom = Math.min(mapZoom * 1.1f, MAX_ZOOM);
	}

	/**
	 * Zoom the map out
	 */
	public void zoomOut() {
		mapZoom = Math.max(mapZoom / 1.1f, MIN_ZOOM);
	}

	/**
	 * Pan the map
	 * 
	 * @param deltaX X offset
	 * @param deltaY Y offset
	 */
	public void panMap(float deltaX, float deltaY) {
		mapOffsetX += deltaX;
		mapOffsetY += deltaY;

		// Clamp panning
		mapOffsetX = Math.max(-200f, Math.min(200f, mapOffsetX));
		mapOffsetY = Math.max(-200f, Math.min(200f, mapOffsetY));
	}

	@Override
	public boolean handleInput() {
		if (!isActive || !isVisible) {
			return false;
		}

		// Zoom controls
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.EQUALS)) {
			zoomIn();
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.MINUS)) {
			zoomOut();
			return true;
		}

		// Pan controls
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.LEFT)) {
			panMap(20f, 0);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.RIGHT)) {
			panMap(-20f, 0);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.UP)) {
			panMap(0, -20f);
			return true;
		}
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.DOWN)) {
			panMap(0, 20f);
			return true;
		}

		// Travel to selected location
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.T)) {
			if (travelToSelected()) {
				logicLayerAPI.requestScreenChange("GAME");
			}
			return true;
		}

		// Close map
		if (inputManager.isKeyPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
			logicLayerAPI.requestScreenChange("GAME");
			return true;
		}

		return false;
	}

	@Override
	protected void onResize() {
		mapX = 20f;
		mapY = (screenHeight - MAP_HEIGHT) / 2f;

		detailsX = mapX + MAP_WIDTH + 30f;
		detailsY = mapY + MAP_HEIGHT - DETAILS_HEIGHT;
	}

	@Override
	public void dispose() {
		if (shapeRenderer != null) {
			shapeRenderer.dispose();
		}
		if (font != null) {
			font.dispose();
		}
		if (fontLarge != null) {
			fontLarge.dispose();
		}
	}
}
