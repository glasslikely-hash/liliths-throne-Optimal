package com.lilithsthrone.logic.persistence;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.*;

/**
 * Autosave manager - handles periodic asynchronous autosaves.
 *
 * Responsibilities:
 *  - Maintain timer for autosave intervals
 *  - Trigger autosave on schedule
 *  - Run autosaves asynchronously (non-blocking)
 *  - Handle start/stop of autosave cycle
 *  - Graceful shutdown with cleanup
 *
 * Design:
 *  ├─ Runs on game update loop (not separate thread)
 *  ├─ Delegates actual save to background thread
 *  ├─ Non-blocking (no impact on frame time)
 *  └─ Can be disabled via settings
 *
 * Flow:
 *  1. update(delta) called each frame
 *  2. Timer accumulates delta
 *  3. When timer >= interval, trigger autosave
 *  4. Reset timer
 *  5. Background thread performs actual save
 */
public class AutoSaveManager {

    private PersistenceManager persistenceManager;
    private float interval;  // Seconds between autosaves
    private float timeSinceLastAutosave;
    private boolean isRunning;
    private ScheduledExecutorService executor;

    /**
     * Constructor.
     *
     * @param persistenceManager The persistence manager
     * @param intervalSeconds Autosave interval in seconds
     */
    public AutoSaveManager(PersistenceManager persistenceManager, float intervalSeconds) {
        this.persistenceManager = persistenceManager;
        this.interval = intervalSeconds;
        this.timeSinceLastAutosave = 0f;
        this.isRunning = false;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AutoSaveThread");
            t.setDaemon(true);
            return t;
        });

        Gdx.app.log("AutoSaveManager", "Initialized with " + intervalSeconds + "s interval");
    }

    /**
     * Start autosave cycle.
     */
    public void start() {
        if (isRunning) {
            return;
        }

        isRunning = true;
        timeSinceLastAutosave = 0f;
        Gdx.app.log("AutoSaveManager", "Autosave started");
    }

    /**
     * Stop autosave cycle.
     * Does NOT perform a save.
     */
    public void stop() {
        isRunning = false;
        Gdx.app.log("AutoSaveManager", "Autosave stopped");
    }

    /**
     * Update autosave timer (call once per frame).
     * Triggers autosave when interval elapses.
     *
     * @param delta Time since last frame in seconds
     */
    public void update(float delta) {
        if (!isRunning) {
            return;
        }

        timeSinceLastAutosave += delta;

        if (timeSinceLastAutosave >= interval) {
            // Time to autosave
            persistenceManager.autoSave();
            timeSinceLastAutosave = 0f;
        }
    }

    /**
     * Set autosave interval.
     *
     * @param seconds New interval in seconds
     */
    public void setInterval(float seconds) {
        this.interval = Math.max(30f, seconds);  // Minimum 30 seconds
        Gdx.app.log("AutoSaveManager", "Interval set to " + this.interval + "s");
    }

    /**
     * Get time until next autosave.
     *
     * @return Seconds until next autosave
     */
    public float getTimeToNextAutosave() {
        return Math.max(0f, interval - timeSinceLastAutosave);
    }

    /**
     * Force autosave immediately.
     */
    public void forceAutosaveNow() {
        if (isRunning) {
            Gdx.app.log("AutoSaveManager", "Forcing immediate autosave");
            persistenceManager.autoSave();
            timeSinceLastAutosave = 0f;
        }
    }

    /**
     * Shutdown autosave manager cleanly.
     * Must be called before app exit.
     */
    public void shutdown() {
        stop();

        try {
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            Gdx.app.log("AutoSaveManager", "Shutdown complete");
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Gdx.app.error("AutoSaveManager", "Shutdown interrupted: " + e.getMessage());
        }
    }

    /**
     * Check if autosave is currently running.
     */
    public boolean isAutoSaving() {
        return isRunning;
    }

    /**
     * Get current autosave interval.
     */
    public float getInterval() {
        return interval;
    }
}
