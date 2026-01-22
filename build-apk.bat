@echo off
REM APK Build Script for Lilith's Throne (Windows)
REM This script builds the Android APK using Gradle

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "ANDROID_DIR=%SCRIPT_DIR%android"
set "BUILD_DIR=%ANDROID_DIR%\build"
set "APK_DEBUG=%BUILD_DIR%\outputs\apk\debug\app-debug.apk"
set "APK_RELEASE=%BUILD_DIR%\outputs\apk\release\app-release.apk"

echo ======================================
echo Lilith's Throne - APK Build Script
echo ======================================
echo.

REM Check if gradle wrapper exists
if not exist "%ANDROID_DIR%\gradlew.bat" (
    echo ERROR: Gradle wrapper not found at %ANDROID_DIR%\gradlew.bat
    echo Please run this script from the project root directory
    exit /b 1
)

REM Build debug APK
echo [1/2] Building debug APK...
cd /d "%ANDROID_DIR%"
call gradlew.bat clean assembleDebug --warning-mode all

if exist "%APK_DEBUG%" (
    echo.
    echo ✓ Debug APK built successfully!
    echo   Location: %APK_DEBUG%
    dir "%APK_DEBUG%"
) else (
    echo.
    echo ✗ Debug APK build failed!
    exit /b 1
)

REM Build release APK
echo.
echo [2/2] Building release APK...
call gradlew.bat assembleRelease --warning-mode all

if exist "%APK_RELEASE%" (
    echo.
    echo ✓ Release APK built successfully!
    echo   Location: %APK_RELEASE%
    dir "%APK_RELEASE%"
) else (
    echo.
    echo ⚠  Release APK build skipped or failed
)

REM Summary
echo.
echo ======================================
echo Build Complete
echo ======================================
cd /d "%SCRIPT_DIR%"

echo.
echo To install on Android device:
echo   adb install -r %APK_DEBUG%
echo.
echo To uninstall:
echo   adb uninstall com.lilithsthrone.game
echo.
pause
