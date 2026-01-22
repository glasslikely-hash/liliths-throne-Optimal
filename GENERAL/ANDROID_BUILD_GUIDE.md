# Android Build Instructions

This document explains how to build Lilith's Throne for Android.

## Prerequisites

1. **Java Development Kit (JDK) 11+**
   ```bash
   java -version  # Should show Java 11 or higher
   ```

2. **Android SDK** (API 33)
   - Install via Android Studio or command-line tools
   - Set `ANDROID_SDK_ROOT` environment variable:
     ```bash
     export ANDROID_SDK_ROOT=/path/to/android/sdk
     ```

3. **Build tools for API 33**
   ```bash
   sdkmanager --install "platforms;android-33" "build-tools;33.0.2"
   ```

## Quick Build

### Option 1: Build APK with Gradle Wrapper (Recommended)

```bash
cd android/
chmod +x gradlew  # On macOS/Linux
./gradlew assembleDebug   # Build debug APK
./gradlew assembleRelease # Build release APK
```

APKs will be created in:
- Debug: `android/build/outputs/apk/debug/app-debug.apk`
- Release: `android/build/outputs/apk/release/app-release.apk`

### Option 2: Build Both Desktop and Android

From the project root:

```bash
# Build desktop JAR
mvn clean package -DskipTests

# Build Android APK
cd android && ./gradlew assembleDebug && cd ..
```

## Gradle Tasks

### Build Tasks
- `./gradlew build` - Build all variants (debug + release)
- `./gradlew assembleDebug` - Build debug APK only
- `./gradlew assembleRelease` - Build release APK only
- `./gradlew clean` - Clean build artifacts

### Testing Tasks
- `./gradlew connectedAndroidTest` - Run tests on connected device
- `./gradlew lint` - Run Android lint checks

### Installation
- `./gradlew installDebug` - Install debug APK to connected device
- `./gradlew installRelease` - Install release APK to connected device

## Android Manifest

The `AndroidManifest.xml` file specifies:
- App name: "Lilith's Throne"
- Package name: `com.lilithsthrone.game`
- Min API: 21 (Android 5.0)
- Target API: 33 (Android 13)
- Permissions:
  - `READ_EXTERNAL_STORAGE` - Access game data
  - `WRITE_EXTERNAL_STORAGE` - Save game data
  - `INTERNET` - Future networking features

## Architecture

### Entry Point
1. `AndroidLauncher` - Initial activity that sets up fullscreen mode
2. `GameActivity` - Main game activity that initializes LibGDX
3. `LibGdxApp` - Core game loop (cross-platform)

### Key Classes

- `GameActivity.java` - Android entry point, sets up LibGDX configuration
- `AndroidLauncher.java` - Handles Android-specific UI setup
- `PlatformConfig.java` - Detects platform and adjusts settings

### Platform Detection

The app automatically detects Android via:
```java
PlatformConfig.IS_MOBILE = true;  // Set in GameActivity
```

This affects:
- Input handling (touch vs mouse)
- Screen resolution (native Android screen size)
- File paths (using app-specific directories)
- Frame rate targets (30 FPS on mobile vs 60 FPS on desktop)

## File Structure

```
android/
├── AndroidManifest.xml          # Android app metadata
├── build.gradle                 # Gradle build config
├── gradle.properties            # Gradle settings
├── gradlew                       # Gradle wrapper (Linux/macOS)
├── gradlew.bat                  # Gradle wrapper (Windows)
├── settings.gradle              # Gradle project settings
├── proguard-rules.pro           # Code obfuscation rules
├── src/
│   └── com/lilithsthrone/main/
│       ├── AndroidLauncher.java # Initial activity
│       └── GameActivity.java    # Main game activity
├── res/                         # Android resources (placeholder)
├── assets/                      # Game assets (auto-copied)
└── build/                       # Build outputs (generated)
    └── outputs/apk/
        ├── debug/               # Debug APK
        └── release/             # Release APK
```

## Troubleshooting

### Build Fails with "Could not find android.jar"
```bash
# Ensure SDK platform is installed
sdkmanager --install "platforms;android-33"

# Set SDK path
export ANDROID_SDK_ROOT=/path/to/android/sdk
```

### Gradle Sync Fails
```bash
./gradlew clean
./gradlew sync  # Or just rebuild
```

### Class Not Found at Runtime
- Check that all game source files are in `src/` directory
- Verify `build.gradle` source sets point to correct directories
- Rebuild with `./gradlew clean assembleDebug`

### APK Won't Install
```bash
# Check device compatibility
adb devices  # List connected devices

# Install debug APK
./gradlew installDebug

# View logs
adb logcat | grep GameActivity
```

## GitHub Actions

The repository includes automated APK builds via `.github/workflows/build-apk.yml`:

- **Trigger:** Pushes to `dev` or `main` branches, or manual workflow dispatch
- **Jobs:**
  1. Build desktop JAR (verify core build)
  2. Build Android debug APK
  3. Build Android release APK
  4. Upload APKs as build artifacts
  5. Create GitHub release (on `main` branch)

Access built APKs from GitHub Actions "Artifacts" tab.

## Code Signing

### Debug Signing (Automatic)
Debug APKs are automatically signed with Android's default debug certificate.
No additional setup needed for development builds.

### Release Signing
For production releases, create a keystore:

```bash
keytool -genkey -v -keystore lilithsthrone.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias lilithsthrone
```

Then configure in `build.gradle`:
```gradle
signingConfigs {
    release {
        storeFile file('lilithsthrone.jks')
        storePassword System.getenv("KEYSTORE_PASSWORD")
        keyAlias System.getenv("KEY_ALIAS")
        keyPassword System.getenv("KEY_PASSWORD")
    }
}
```

## Performance Optimization

### Gradle Configuration
- Parallel builds enabled (4 workers)
- Build caching enabled
- Daemon mode enabled

### App Optimization
- ProGuard minification (release builds)
- 4x MSAA antialiasing
- OpenGL ES 2.0 (compatible with older devices)
- Full screen immersive mode

### Target Performance
- **Desktop:** 60 FPS
- **Android:** 30+ FPS on mid-range devices

## Testing on Device

```bash
# Build and install
./gradlew installDebug

# View logs
adb logcat com.lilithsthrone.game

# Clear app data
adb shell pm clear com.lilithsthrone.game

# Uninstall
adb uninstall com.lilithsthrone.game
```

## Next Steps

1. Connect Android device or start emulator
2. Build APK: `./gradlew assembleDebug`
3. Install: `./gradlew installDebug`
4. Run app and test gameplay

For issues, check `adb logcat` output or file a GitHub issue.

---

**Note:** This is a text-based, event-driven game with no continuous rendering loop.
It works identically on desktop and Android, with UI automatically adapting to screen size.
