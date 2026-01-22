package com.lilithsthrone.ui.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

/**
 * Unified input handler for keyboard, mouse, and touch input.
 * Detects platform (desktop/mobile) and adapts input handling.
 *
 * Responsibilities:
 *  - Poll keyboard state (desktop)
 *  - Poll mouse state (desktop)
 *  - Handle touch events (mobile)
 *  - Detect gestures (swipes, long-press)
 *  - Convert raw input to semantic InputEvents
 *
 * Usage:
 *  InputManager inputManager = new InputManager();
 *  InputEvent event = inputManager.update(delta);
 *  if (event != null) {
 *      handleInput(event);
 *  }
 */
public class InputManager implements InputProcessor {

    /**
     * Input event types.
     */
    public enum InputType {
        KEY_DOWN,       // Keyboard key pressed
        KEY_UP,         // Keyboard key released
        MOUSE_MOVED,    // Mouse moved
        MOUSE_BUTTON,   // Mouse button clicked
        TOUCH_DOWN,     // Touch screen pressed
        TOUCH_UP,       // Touch screen released
        TOUCH_DRAGGED,  // Touch dragged
        SWIPE,          // Swipe gesture detected
        LONG_PRESS      // Long press detected
    }

    /**
     * Represents a single input event.
     */
    public static class InputEvent {
        public InputType type;
        public int key;                 // For KEY_DOWN/KEY_UP
        public int button;              // For MOUSE_BUTTON (0=left, 1=right, 2=middle)
        public int x;                   // Screen X coordinate
        public int y;                   // Screen Y coordinate
        public int pointer;             // Touch pointer ID (mobile)
        public long timestamp;          // Time of event
        private boolean consumed = false;  // Whether event has been consumed

        // For swipe events
        public float swipeStartX;
        public float swipeStartY;
        public float swipeDeltaX;
        public float swipeDeltaY;

        public InputEvent(InputType type) {
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }
        
        /**
         * Mark this event as consumed (won't propagate to other handlers)
         */
        public void consume() {
            this.consumed = true;
        }
        
        /**
         * Check if this event has been consumed
         */
        public boolean isConsumed() {
            return consumed;
        }
    }

    // Mouse state
    private int mouseX;
    private int mouseY;
    private boolean[] mouseButtonPressed = new boolean[3];
    private boolean[] mouseButtonJustPressed = new boolean[3];

    // Keyboard state
    private boolean[] keyPressed = new boolean[256];
    private boolean[] keyJustPressed = new boolean[256];

    // Touch state
    private static final int MAX_TOUCH_POINTERS = 10;
    private boolean[] touchPressed = new boolean[MAX_TOUCH_POINTERS];
    private float[] touchX = new float[MAX_TOUCH_POINTERS];
    private float[] touchY = new float[MAX_TOUCH_POINTERS];
    private long[] touchStartTime = new long[MAX_TOUCH_POINTERS];
    private float[] touchStartX = new float[MAX_TOUCH_POINTERS];
    private float[] touchStartY = new float[MAX_TOUCH_POINTERS];

    // Gesture detection
    private static final float SWIPE_MIN_DISTANCE = 100f;
    private static final float SWIPE_MAX_TIME = 300f;  // milliseconds
    private static final long LONG_PRESS_TIME = 500L;  // milliseconds

    // Frame timing
    private float deltaTime;
    private InputEvent lastInputEvent = null;

    public InputManager() {
        // Register as input processor
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Update input state each frame.
     * Call this once per frame to process accumulated input.
     *
     * @param delta Time since last frame in seconds
     * @return Next pending input event, or null if no input
     */
    public InputEvent update(float delta) {
        this.deltaTime = delta;

        // Check for long press (desktop mouse click held for 500ms)
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            // Could detect long press here
        }

        // Clear "just pressed" flags from last frame
        for (int i = 0; i < 3; i++) {
            mouseButtonJustPressed[i] = false;
        }
        for (int i = 0; i < 256; i++) {
            keyJustPressed[i] = false;
        }

        return null; // No event this frame
    }

    /**
     * Check if a key is currently pressed.
     */
    public boolean isKeyPressed(int keyCode) {
        return keyCode < keyPressed.length && keyPressed[keyCode];
    }

    /**
     * Check if a key was just pressed this frame.
     */
    public boolean isKeyJustPressed(int keyCode) {
        return keyCode < keyJustPressed.length && keyJustPressed[keyCode];
    }

    /**
     * Check if mouse button is pressed.
     * @param button 0=left, 1=right, 2=middle
     */
    public boolean isMouseButtonPressed(int button) {
        return button < 3 && mouseButtonPressed[button];
    }

    /**
     * Check if mouse button was just pressed this frame.
     */
    public boolean isMouseButtonJustPressed(int button) {
        return button < 3 && mouseButtonJustPressed[button];
    }

    /**
     * Get current mouse position.
     */
    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    /**
     * Check if touch is active on a pointer.
     */
    public boolean isTouchPressed(int pointer) {
        return pointer < MAX_TOUCH_POINTERS && touchPressed[pointer];
    }

    /**
     * Get touch position for a pointer.
     */
    public float getTouchX(int pointer) {
        return pointer < MAX_TOUCH_POINTERS ? touchX[pointer] : 0f;
    }

    public float getTouchY(int pointer) {
        return pointer < MAX_TOUCH_POINTERS ? touchY[pointer] : 0f;
    }

    // ================== InputProcessor Implementation ==================

    @Override
    public boolean keyDown(int keycode) {
        if (keycode < keyPressed.length) {
            keyPressed[keycode] = true;
            keyJustPressed[keycode] = true;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode < keyPressed.length) {
            keyPressed[keycode] = false;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Create input event for mouse button press / touch down
        InputEvent event = new InputEvent(InputType.MOUSE_BUTTON);
        event.x = screenX;
        event.y = screenY;
        event.button = button;
        event.pointer = pointer;
        this.lastInputEvent = event;
        
        if (pointer < MAX_TOUCH_POINTERS) {
            touchPressed[pointer] = true;
            touchX[pointer] = screenX;
            touchY[pointer] = screenY;
            touchStartX[pointer] = screenX;
            touchStartY[pointer] = screenY;
            touchStartTime[pointer] = System.currentTimeMillis();
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer < MAX_TOUCH_POINTERS) {
            touchPressed[pointer] = false;

            // Detect swipe gesture
            float deltaX = screenX - touchStartX[pointer];
            float deltaY = screenY - touchStartY[pointer];
            long touchDuration = System.currentTimeMillis() - touchStartTime[pointer];

            if (Math.abs(deltaX) >= SWIPE_MIN_DISTANCE || Math.abs(deltaY) >= SWIPE_MIN_DISTANCE) {
                if (touchDuration <= SWIPE_MAX_TIME) {
                    // Swipe detected
                    // handleSwipe(pointer, deltaX, deltaY);
                }
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer < MAX_TOUCH_POINTERS) {
            touchX[pointer] = screenX;
            touchY[pointer] = screenY;
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mouseX = screenX;
        mouseY = screenY;
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
    
    /**
     * Get the last input event that occurred
     */
    public InputEvent getLastInputEvent() {
        InputEvent event = lastInputEvent;
        lastInputEvent = null; // Clear for next frame
        return event;
    }
}
