# APK Build & Launch Instructions

**Status:** Infrastructure Complete - Ready for Build  
**Date:** January 22, 2026

---

## Quick Start: Build APK

### Prerequisites Check
Before building, ensure these are installed:
- Java 11 or higher: `java -version`
- Android SDK: Set `ANDROID_SDK_ROOT` environment variable
- Gradle: `gradle --version`

### Build Command

From project root directory:

```bash
cd android
chmod +x gradlew
./gradlew clean assembleDebug
```

### Expected Output
```
BUILD SUCCESSFUL in Xs
7 actionable tasks: 7 executed

Built the following APK(s):
  ~/liliths-throne-Optimal/android/build/outputs/apk/debug/app-debug.apk
```

---

## Install & Launch on Android Device

### Prerequisites
- Android device (API 21+) or emulator running
- USB debugging enabled (for physical device)
- ADB installed: `adb devices`

### Install APK

```bash
adb install -r android/build/outputs/apk/debug/app-debug.apk
```

Expected output:
```
Success
```

### Launch App

```bash
adb shell am start -n com.lilithsthrone.game/com.lilithsthrone.main.AndroidLauncher
```

Or simply tap the app icon on the device to launch.

### View Live Logs

```bash
adb logcat com.lilithsthrone.game:V "*:E"
```

Watch for startup messages and any errors.

---

## Full Build & Test Workflow

### 1. Clean Build
```bash
cd android
./gradlew clean build
```
Time: ~2-3 minutes

### 2. Build Debug APK Only
```bash
cd android
./gradlew assembleDebug
```
Time: ~45 seconds

### 3. Install to Device
```bash
adb install -r android/build/outputs/apk/debug/app-debug.apk
```

### 4. Launch App
```bash
adb shell am start -n com.lilithsthrone.game/com.lilithsthrone.main.AndroidLauncher
```

### 5. View Logs
```bash
adb logcat com.lilithsthrone.game:V
```

### 6. Test Gameplay
- App should launch in fullscreen
- Text should render on screen
- Touch input should work
- Game state should update on click
- Exit button should close app

---

## Troubleshooting

### Issue: "Could not determine android.jar location"
**Solution:**
```bash
sdkmanager --install "platforms;android-33"
sdkmanager --install "build-tools;33.0.2"
```

### Issue: "Gradle sync failed"
**Solution:**
```bash
cd android
./gradlew clean
./gradlew sync
```

### Issue: "APK not installed on device"
**Solution:**
```bash
# Uninstall first
adb uninstall com.lilithsthrone.game

# Install again
adb install -r android/build/outputs/apk/debug/app-debug.apk

# Verify
adb shell pm list packages | grep lilithsthrone
```

### Issue: "App crashes on launch"
**Check logs:**
```bash
adb logcat | grep FATAL
adb logcat | grep GameActivity
```

**Common causes:**
- Assets not bundled (res/ or data/ missing)
- Source files not found (check android/src/ and ../src/)
- LibGDX initialization failed

### Issue: "Touch input not working"
**Check:**
- InputManager is capturing touch events in InputManager.java
- GameScreen is routing through LibGdxUIManager.mousePressed()
- AndroidGameActivity sets up input processor

### Issue: "Missing res/ or data/ folder"
**Solution:** Assets should be auto-copied to assets/ during build
```bash
# Manually verify:
ls -la android/assets/res/
ls -la android/assets/data/
```

---

## Build Files Generated

After successful build, you'll have:

```
android/build/outputs/apk/
├── debug/
│   └── app-debug.apk              ← Use this for testing
└── release/
    └── app-release.apk            ← For distribution
```

### APK Information
- **Debug APK size:** 40-50 MB
- **Includes:** Full debug symbols, unoptimized code
- **Signing:** Automatic (Android debug certificate)
- **Min API:** 21 (Android 5.0)
- **Target API:** 33 (Android 13)

---

## Device/Emulator Requirements

### Minimum Specs
- API Level: 21 (Android 5.0)
- RAM: 512 MB minimum
- Storage: 150 MB free space
- Screen: Any size (game auto-scales)

### Recommended Specs
- API Level: 29+ (Android 10+)
- RAM: 2 GB+
- Storage: SSD
- Screen: 5" - 6" (phone or tablet)

### Emulator Setup
```bash
# Create emulator (if not exists)
avdmanager create avd -n pixel4 -k "system-images;android-33;google_apis;x86_64"

# Start emulator
emulator -avd pixel4 &

# Verify it's running
adb devices
```

---

## What Happens on Launch

### Sequence
1. **AndroidLauncher.java starts**
   - Sets fullscreen mode
   - Hides system UI
   - Enables immersive mode
   - Transitions to GameActivity

2. **GameActivity initializes**
   - Detects screen dimensions
   - Sets PlatformConfig.IS_MOBILE = true
   - Creates LibGdxApp instance
   - Initializes LibGDX renderer

3. **LibGdxApp starts**
   - Creates rendering resources (BitmapFont, ShapeRenderer)
   - Initializes GameScreen
   - Requests initial game content
   - Enters render loop

4. **Game renders**
   - Text appears on screen
   - Colors display correctly
   - Touch input detected
   - State updates on click

---

## Testing Checklist

After app launches on device:

- [ ] **Visual:**
  - App fills entire screen
  - No system UI visible
  - Text is readable
  - Colors match expected theme
  - Layout is appropriate for screen size

- [ ] **Input:**
  - Touch is detected on buttons
  - Single tap works (not held)
  - No phantom clicks
  - Appropriate visual feedback

- [ ] **Gameplay:**
  - Game state updates on click
  - Dialogue choices work
  - Navigation buttons work
  - Inventory accessible
  - Character sheet accessible
  - Map navigation works

- [ ] **Performance:**
  - Smooth interaction (30+ FPS)
  - No stuttering
  - Responsive to input
  - No memory warnings

- [ ] **Stability:**
  - No crashes on startup
  - No crashes during gameplay
  - Exit button closes app cleanly
  - No logcat errors (only info/warnings)

---

## Multi-Device Testing

### Test on Multiple APIs
```bash
# Build for different target SDKs (requires emulator setup)
adb devices  # List all connected devices

# Run on specific device
adb -s <device_id> install -r app-debug.apk
```

### Test Orientations
Currently locked to portrait, but verify:
```bash
adb shell settings get global accelerometer_rotation
adb shell settings put global accelerometer_rotation 0  # Lock
```

### Test Different Screen Sizes
- Small phone (4.5")
- Large phone (6.5")
- Tablet (7")
- Tablet (10")

The game should auto-scale to fit all screen sizes.

---

## Performance Profiling

### CPU Usage
```bash
adb shell top | grep lilithsthrone
```

Should be low (single-digit %) due to event-driven architecture.

### Memory Usage
```bash
adb shell dumpsys meminfo com.lilithsthrone.game
```

Should be 200-300 MB on typical device.

### FPS Monitoring
Add to game if desired:
```java
// In GameScreen.update()
frameCounter++;
if (frameCounter % 60 == 0) {
    LogManager.info("FPS", "Current: " + Gdx.graphics.getFramesPerSecond());
}
```

---

## CI/CD Testing

### GitHub Actions
The workflow automatically:
1. Builds desktop JAR (sanity check)
2. Builds Android debug APK
3. Builds Android release APK
4. Uploads APKs to artifacts

**Access built APKs:**
- GitHub repo → Actions → Build APK workflow
- Download from "Artifacts" section

---

## Release Preparation

### For Production Distribution
1. **Set up signing key:**
   ```bash
   keytool -genkey -v -keystore lilithsthrone.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias lilithsthrone
   ```

2. **Configure in build.gradle:**
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

3. **Build release APK:**
   ```bash
   cd android
   export KEYSTORE_PASSWORD=yourpassword
   export KEY_ALIAS=lilithsthrone
   export KEY_PASSWORD=yourpassword
   ./gradlew assembleRelease
   ```

4. **Test release APK:**
   ```bash
   adb install -r android/build/outputs/apk/release/app-release.apk
   ```

---

## Next Steps

1. **Build immediately:**
   ```bash
   cd android && ./gradlew assembleDebug
   ```

2. **Install on device:**
   ```bash
   adb install -r android/build/outputs/apk/debug/app-debug.apk
   ```

3. **Launch app:**
   ```bash
   adb shell am start -n com.lilithsthrone.game/com.lilithsthrone.main.AndroidLauncher
   ```

4. **Watch logs:**
   ```bash
   adb logcat com.lilithsthrone.game:V
   ```

5. **Test gameplay** - See testing checklist above

---

## Success Criteria

✅ **Build successful** - No compile errors, APK created  
✅ **Installation successful** - APK installs without errors  
✅ **Launch successful** - App appears on screen  
✅ **Text renders** - Game content visible and readable  
✅ **Input works** - Touch input detected and processed  
✅ **Gameplay works** - Game state updates on interaction  
✅ **Performance good** - 30+ FPS, responsive interaction  
✅ **Stable** - No crashes, clean shutdown  

---

## Support

**Build Issues:**
- Check `adb logcat` for error messages
- Verify Android SDK is installed: `sdkmanager --list_installed`
- Check Java version: `java -version` (need 11+)

**Runtime Issues:**
- View logs: `adb logcat | grep GameActivity`
- Check if device has 150 MB free space
- Try uninstall + reinstall
- Test on emulator if device fails

**Performance Issues:**
- Verify target device meets minimum specs
- Check CPU/memory usage with `adb shell top`
- Profile with Android Profiler (in Android Studio)

---

**Ready to build and test!** 🚀
