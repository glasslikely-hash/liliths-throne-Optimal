#!/bin/bash

# APK Build Script for Lilith's Throne
# This script builds the Android APK using Gradle

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$SCRIPT_DIR/android"
BUILD_DIR="$ANDROID_DIR/build"
APK_DEBUG="$BUILD_DIR/outputs/apk/debug/app-debug.apk"
APK_RELEASE="$BUILD_DIR/outputs/apk/release/app-release.apk"

echo "======================================"
echo "Lilith's Throne - APK Build Script"
echo "======================================"
echo ""

# Check if gradle wrapper exists
if [ ! -f "$ANDROID_DIR/gradlew" ]; then
    echo "ERROR: Gradle wrapper not found at $ANDROID_DIR/gradlew"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Make gradlew executable
chmod +x "$ANDROID_DIR/gradlew"

# Build debug APK
echo "[1/4] Building debug APK..."
cd "$ANDROID_DIR"
./gradlew clean assembleDebug --warning-mode all

if [ -f "$APK_DEBUG" ]; then
    APK_SIZE=$(du -h "$APK_DEBUG" | cut -f1)
    echo "✅ Debug APK built successfully!"
    echo "   Location: $APK_DEBUG"
    echo "   Size: $APK_SIZE"
else
    echo "❌ Debug APK build failed!"
    exit 1
fi

# Build release APK
echo ""
echo "[2/4] Building release APK..."
./gradlew assembleRelease --warning-mode all

if [ -f "$APK_RELEASE" ]; then
    APK_SIZE=$(du -h "$APK_RELEASE" | cut -f1)
    echo "✅ Release APK built successfully!"
    echo "   Location: $APK_RELEASE"
    echo "   Size: $APK_SIZE"
else
    echo "⚠️  Release APK build failed (non-critical)"
fi

# Summary
echo ""
echo "======================================"
echo "Build Summary"
echo "======================================"
cd "$SCRIPT_DIR"

if [ -f "$APK_DEBUG" ]; then
    echo "✅ Debug APK:"
    ls -lh "$APK_DEBUG"
    echo ""
fi

if [ -f "$APK_RELEASE" ]; then
    echo "✅ Release APK:"
    ls -lh "$APK_RELEASE"
    echo ""
fi

echo "Build complete!"
echo ""
echo "To install on Android device:"
echo "  adb install -r $APK_DEBUG"
echo ""
echo "To uninstall:"
echo "  adb uninstall com.lilithsthrone.game"
