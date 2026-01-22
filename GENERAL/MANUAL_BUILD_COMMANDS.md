# Manual Build Commands - Run These Directly

The automated tool is having issues, but gradle is installed on your system. Execute these commands directly in VS Code's integrated terminal or your system terminal:

## Build APK - Run This Now

Copy and paste into your terminal:

```bash
cd /workspaces/liliths-throne-Optimal/android && chmod +x gradlew && ./gradlew clean assembleDebug
```

### What This Does:
1. Navigates to android directory
2. Makes gradlew executable
3. Cleans previous builds
4. Compiles and builds debug APK

### Expected Output:
```
BUILD SUCCESSFUL in 2m 15s
7 actionable tasks: 7 executed

Built the following APK(s):
  /workspaces/liliths-throne-Optimal/android/build/outputs/apk/debug/app-debug.apk
```

### Build Time:
- First build: 2-3 minutes (downloads dependencies)
- Subsequent builds: 30-45 seconds

---

## Install & Run (Once Build Succeeds)

### If you have Android device/emulator connected:

```bash
adb install -r /workspaces/liliths-throne-Optimal/android/build/outputs/apk/debug/app-debug.apk
```

### Launch the app:

```bash
adb shell am start -n com.lilithsthrone.game/com.lilithsthrone.main.AndroidLauncher
```

### View logs:

```bash
adb logcat com.lilithsthrone.game:V "*:E"
```

---

## Alternative: Use Build Script

If gradlew has issues, try the build script:

```bash
bash /workspaces/liliths-throne-Optimal/build-apk.sh
```

Or on Windows:

```bash
cd /workspaces/liliths-throne-Optimal && build-apk.bat
```

---

## Check Build Status

After running build, check if APK was created:

```bash
ls -lh /workspaces/liliths-throne-Optimal/android/build/outputs/apk/debug/
```

Should show `app-debug.apk` file (~40-50 MB)

---

**Execute the build command in your terminal and watch for SUCCESS message!**
