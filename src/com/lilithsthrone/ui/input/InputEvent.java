package com.lilithsthrone.ui.input;

/**
 * Input event data.
 * 
 * Replaces JavaFX input events with a unified LibGDX model.
 * Handles mouse, keyboard, and touch input.
 * 
 * @since 0.4.11.3
 * @version 1.0
 * @author Refactoring Agent
 */
public class InputEvent {
	
	public enum Type {
		MOUSE_MOVE,
		MOUSE_DOWN,
		MOUSE_UP,
		MOUSE_DRAG,
		TOUCH_DOWN,
		TOUCH_UP,
		TOUCH_DRAG,
		KEY_DOWN,
		KEY_UP,
		SCROLL,
		LONG_PRESS
	}
	
	public enum Button {
		LEFT, RIGHT, MIDDLE, NONE
	}
	
	public Type type;
	public Button button;
	public int key;
	
	public float x;
	public float y;
	public float screenX;
	public float screenY;
	
	public int pointer; // For multitouch
	
	public long timestamp;
	
	public InputEvent(Type type) {
		this.type = type;
		this.button = Button.NONE;
		this.key = -1;
		this.pointer = 0;
		this.timestamp = System.currentTimeMillis();
	}
	
	public InputEvent(Type type, float x, float y) {
		this(type);
		this.x = x;
		this.y = y;
		this.screenX = x;
		this.screenY = y;
	}
	
	public InputEvent(Type type, int key) {
		this(type);
		this.key = key;
	}
	
	@Override
	public String toString() {
		return String.format("InputEvent[type=%s, button=%s, pos=(%.0f,%.0f)]", type, button, x, y);
	}
}
