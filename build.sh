#!/bin/bash

# Lilith's Throne - Complete Build Script
# Builds desktop JAR and Android APK

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"
ANDROID_DIR="$PROJECT_ROOT/android"

echo "╔════════════════════════════════════════╗"
echo "║  Lilith's Throne - Build Script        ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Parse arguments
BUILD_DESKTOP=true
BUILD_ANDROID=true
ANDROID_TYPE="debug"  # debug or release
INSTALL_APK=false
LAUNCH_GAME=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --desktop-only)
            BUILD_ANDROID=false
            shift
            ;;
        --android-only)
            BUILD_DESKTOP=false
            shift
            ;;
        --release)
            ANDROID_TYPE="release"
            shift
            ;;
        --install)
            INSTALL_APK=true
            shift
            ;;
        --launch)
            LAUNCH_GAME=true
            INSTALL_APK=true
            shift
            ;;
        --help)
            echo "Usage: $0 [options]"
            echo ""
            echo "Options:"
            echo "  --desktop-only    Build only desktop JAR"
            echo "  --android-only    Build only Android APK"
            echo "  --release         Build release APK (default: debug)"
            echo "  --install         Install APK on connected device"
            echo "  --launch          Install and launch game on device"
            echo "  --help            Show this help message"
            echo ""
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print status
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_section() {
    echo ""
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
}

# Build Desktop JAR
if [ "$BUILD_DESKTOP" = true ]; then
    print_section "STEP 1: Building Desktop JAR"
    
    if command -v mvn &> /dev/null; then
        cd "$PROJECT_ROOT"
        echo "Running: mvn clean package -DskipTests -q"
        mvn clean package -DskipTests -q
        
        JAR_FILE=$(find "$PROJECT_ROOT/target" -name "*-uber.jar" | head -1)
        if [ -f "$JAR_FILE" ]; then
            print_status "Desktop JAR built successfully"
            echo "  Location: $JAR_FILE"
            echo "  Size: $(du -h "$JAR_FILE" | cut -f1)"
        else
            print_error "Failed to build desktop JAR"
            exit 1
        fi
    else
        print_error "Maven not found. Install Maven and try again."
        exit 1
    fi
fi

# Build Android APK
if [ "$BUILD_ANDROID" = true ]; then
    print_section "STEP 2: Building Android APK ($ANDROID_TYPE)"
    
    if [ ! -d "$ANDROID_DIR" ]; then
        print_error "Android directory not found: $ANDROID_DIR"
        exit 1
    fi
    
    cd "$ANDROID_DIR"
    
    # Check if gradlew exists
    if [ ! -f "gradlew" ]; then
        print_error "Gradle wrapper not found. Initializing Gradle..."
        chmod +x gradlew
    else
        chmod +x gradlew
    fi
    
    # Determine build task
    if [ "$ANDROID_TYPE" = "release" ]; then
        BUILD_TASK="clean assembleRelease"
        APK_PATH="$ANDROID_DIR/build/outputs/apk/release/app-release.apk"
    else
        BUILD_TASK="clean assembleDebug"
        APK_PATH="$ANDROID_DIR/build/outputs/apk/debug/app-debug.apk"
    fi
    
    echo "Running: ./gradlew $BUILD_TASK"
    ./gradlew $BUILD_TASK
    
    if [ -f "$APK_PATH" ]; then
        print_status "Android APK built successfully"
        echo "  Type: $ANDROID_TYPE"
        echo "  Location: $APK_PATH"
        echo "  Size: $(du -h "$APK_PATH" | cut -f1)"
    else
        print_error "Failed to build Android APK"
        exit 1
    fi
fi

# Install APK
if [ "$INSTALL_APK" = true ] && [ "$BUILD_ANDROID" = true ]; then
    print_section "STEP 3: Installing APK on device"
    
    if command -v adb &> /dev/null; then
        echo "Running: adb install -r $APK_PATH"
        adb install -r "$APK_PATH"
        print_status "APK installed successfully"
    else
        print_error "ADB not found. Cannot install APK. Skipping..."
    fi
fi

# Launch Game
if [ "$LAUNCH_GAME" = true ] && [ "$BUILD_ANDROID" = true ]; then
    print_section "STEP 4: Launching game on device"
    
    if command -v adb &> /dev/null; then
        echo "Launching: com.lilithsthrone.game/com.lilithsthrone.main.AndroidLauncher"
        adb shell am start -n com.lilithsthrone.game/com.lilithsthrone.main.AndroidLauncher
        print_status "Game launched on device"
    else
        print_error "ADB not found. Cannot launch game."
    fi
fi

# Summary
print_section "Build Complete!"
echo "Summary:"
if [ "$BUILD_DESKTOP" = true ]; then
    echo "  ✓ Desktop JAR built"
fi
if [ "$BUILD_ANDROID" = true ]; then
    echo "  ✓ Android APK ($ANDROID_TYPE) built"
    [ "$INSTALL_APK" = true ] && echo "  ✓ APK installed on device"
    [ "$LAUNCH_GAME" = true ] && echo "  ✓ Game launched on device"
fi
echo ""
print_status "All done!"
