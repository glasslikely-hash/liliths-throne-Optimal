package com.lilithsthrone.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

/**
 * Screen transition effect (fade, wipe, slide, etc).
 *
 * Responsibilities:
 *  - Define transition animation
 *  - Track progress (0.0 = start, 1.0 = complete)
 *  - Render overlay during transition
 *
 * Usage:
 *  Transition fade = new Transition.Fade(0.5f);  // 0.5 second fade
 *  screenManager.setScreenWithTransition(ScreenType.GAME, fade);
 */
public abstract class Transition {

    protected float duration;  // Duration in seconds
    protected Color color;

    /**
     * Constructor.
     * @param duration How long the transition lasts (seconds)
     */
    public Transition(float duration) {
        this.duration = duration;
        this.color = new Color(0, 0, 0, 1);
    }

    /**
     * Get transition duration in seconds.
     */
    public float getDuration() {
        return duration;
    }

    /**
     * Render transition overlay.
     * @param batch Sprite batch
     * @param progress Progress (0.0 = start, 1.0 = complete)
     */
    public abstract void render(SpriteBatch batch, float progress);

    // ================== Built-in Transitions ==================

    /**
     * Fade to black transition.
     */
    public static class Fade extends Transition {
        public Fade(float duration) {
            super(duration);
        }

        @Override
        public void render(SpriteBatch batch, float progress) {
            // Fade from transparent (0.0) to opaque black (1.0)
            color.a = progress;
            fillScreen(batch, color, LibGdxApp.getVirtualWidth(), LibGdxApp.getVirtualHeight());
        }
    }

    /**
     * Slide transition (slide in from one side).
     */
    public static class Slide extends Transition {
        private Direction direction;

        public enum Direction {
            LEFT, RIGHT, UP, DOWN
        }

        public Slide(float duration, Direction direction) {
            super(duration);
            this.direction = direction;
        }

        @Override
        public void render(SpriteBatch batch, float progress) {
            float width = LibGdxApp.getVirtualWidth();
            float height = LibGdxApp.getVirtualHeight();

            color.a = 0.5f;

            switch (direction) {
                case LEFT:
                    fillRect(batch, color, -width + (width * progress), 0, width, height);
                    break;
                case RIGHT:
                    fillRect(batch, color, width - (width * progress), 0, width, height);
                    break;
                case UP:
                    fillRect(batch, color, 0, height - (height * progress), width, height);
                    break;
                case DOWN:
                    fillRect(batch, color, 0, -height + (height * progress), width, height);
                    break;
            }
        }
    }

    /**
     * Wipe transition (curtain wipe effect).
     */
    public static class Wipe extends Transition {
        private Direction direction;

        public enum Direction {
            HORIZONTAL, VERTICAL
        }

        public Wipe(float duration, Direction direction) {
            super(duration);
            this.direction = direction;
        }

        @Override
        public void render(SpriteBatch batch, float progress) {
            float width = LibGdxApp.getVirtualWidth();
            float height = LibGdxApp.getVirtualHeight();

            color.a = 1f;

            if (direction == Direction.HORIZONTAL) {
                // Wipe from left to right
                float wipeWidth = width * progress;
                fillRect(batch, color, 0, 0, wipeWidth, height);
            } else {
                // Wipe from top to bottom
                float wipeHeight = height * progress;
                fillRect(batch, color, 0, height - wipeHeight, width, wipeHeight);
            }
        }
    }

    // ================== Helper Methods ==================

    /**
     * Fill entire screen with color.
     */
    protected static void fillScreen(SpriteBatch batch, Color color, float width, float height) {
        fillRect(batch, color, 0, 0, width, height);
    }

    /**
     * Fill a rectangle with color.
     * Uses a 1x1 white pixel and scales it.
     */
    protected static void fillRect(SpriteBatch batch, Color color, float x, float y, float width, float height) {
        // Note: In production, use a 1x1 white texture asset
        // For now, this is a placeholder - would need to be implemented with actual textures
        Color oldColor = batch.getColor();
        batch.setColor(color);
        // Would draw a rectangle here with actual texture
        batch.setColor(oldColor);
    }
}
