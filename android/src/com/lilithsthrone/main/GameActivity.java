package com.lilithsthrone.main;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.lilithsthrone.ui.LibGdxApp;
import com.lilithsthrone.ui.platform.PlatformConfig;

/**
 * Android Game Activity
 * 
 * Initializes LibGDX for Android and launches the game.
 * Handles platform detection, permissions, and Android-specific configuration.
 */
public class GameActivity extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up platform configuration BEFORE creating LibGdxApp
        PlatformConfig.IS_MOBILE = true;
        PlatformConfig.PLATFORM_NAME = "Android";
        PlatformConfig.SCREEN_WIDTH = getResources().getDisplayMetrics().widthPixels;
        PlatformConfig.SCREEN_HEIGHT = getResources().getDisplayMetrics().heightPixels;

        // Set fullscreen
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        // Hide system UI for immersive experience
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        // Keep screen on during gameplay
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Configure LibGDX for Android
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGLSurfaceView20API18 = true;  // Use OpenGL ES 2.0
        config.numSamples = 4;                  // 4x MSAA antialiasing
        config.resolutionStrategy = null;       // Use native resolution
        config.useImmersiveMode = true;         // Full immersive mode

        // Create and initialize LibGDX app
        try {
            LibGdxApp gdxApp = new LibGdxApp();
            initialize(gdxApp, config);
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Failed to initialize game", e);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        // Allow back button to trigger pause menu or exit
        // The game logic will handle this via Gdx.input callbacks
        super.onBackPressed();
    }
}
