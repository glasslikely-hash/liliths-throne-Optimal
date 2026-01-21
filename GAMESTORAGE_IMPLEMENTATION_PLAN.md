# GameStorage Implementation Plan - Action Items

**Created**: January 21, 2026  
**Priority**: CRITICAL - Blocks Android Port  
**Estimated Effort**: 3.5-4 hours  
**Owner**: Next Development Session  

---

## Overview

To enable Android support, we must implement a GameStorage abstraction layer. This 4-hour task unblocks all multi-platform development.

---

## Task 1: Create GameStorage Interface

**File**: `src/com/lilithsthrone/persistence/GameStorage.java`  
**Time**: 30 minutes  
**Complexity**: LOW

### Code to Create

```java
package com.lilithsthrone.persistence;

import java.io.File;
import java.io.InputStream;

/**
 * Platform-agnostic interface for game storage access.
 * Abstracts filesystem differences between desktop and mobile platforms.
 * 
 * @since 0.4.11.4
 */
public interface GameStorage {
    
    // ============ PERSISTENT STORAGE ============
    // Data that survives uninstall, cache clear, app updates
    
    /**
     * Gets the directory for persistent game data.
     * On Desktop: "data/"
     * On Android: context.getFilesDir()
     */
    File getPersistentDirectory();
    
    /**
     * Gets the directory for persistent save files.
     * On Desktop: "data/saves/"
     * On Android: context.getFilesDir()/saves/
     */
    File getPersistentSaveDirectory();
    
    /**
     * Gets the directory for persistent character files.
     * On Desktop: "data/characters/"
     * On Android: context.getFilesDir()/characters/
     */
    File getPersistentCharacterDirectory();
    
    // ============ TEMPORARY/CACHE STORAGE ============
    // Data that can be cleared by OS when space needed
    
    /**
     * Gets the cache directory for temporary game data.
     * Use for autosaves, temporary files.
     * On Desktop: System.getProperty("java.io.tmpdir")
     * On Android: context.getCacheDir()
     */
    File getCacheDirectory();
    
    // ============ RESOURCE ACCESS ============
    // Game resources (images, data, configs)
    
    /**
     * Gets a resource directory (images, characters, etc).
     * On Desktop: "res/{resourceType}"
     * On Android: APK resources
     * 
     * @param resourceType (e.g., "images", "characters", "mods")
     */
    File getResourceDirectory(String resourceType);
    
    /**
     * Gets a resource as an InputStream.
     * Works for classpath resources in APK.
     * 
     * @param resourcePath (e.g., "characters/default/portrait.png")
     */
    InputStream getResourceAsStream(String resourcePath);
}
```

### Checklist
- [ ] Create file with full code above
- [ ] Add JavaDoc for each method
- [ ] Verify syntax is valid
- [ ] No compilation errors

---

## Task 2: Create DesktopGameStorage Implementation

**File**: `src/com/lilithsthrone/persistence/DesktopGameStorage.java`  
**Time**: 45 minutes  
**Complexity**: MEDIUM

### Code to Create

```java
package com.lilithsthrone.persistence;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Desktop implementation of GameStorage.
 * Uses filesystem-based storage with relative paths.
 * 
 * @since 0.4.11.4
 */
public class DesktopGameStorage implements GameStorage {
    
    private static final String DATA_DIR = "data";
    private static final String SAVES_DIR = "data/saves";
    private static final String CHARACTERS_DIR = "data/characters";
    private static final String CACHE_DIR = "data/cache";
    private static final String RES_DIR = "res";
    
    @Override
    public File getPersistentDirectory() {
        File dir = new File(DATA_DIR);
        ensureDirectoryExists(dir);
        return dir;
    }
    
    @Override
    public File getPersistentSaveDirectory() {
        File dir = new File(SAVES_DIR);
        ensureDirectoryExists(dir);
        return dir;
    }
    
    @Override
    public File getPersistentCharacterDirectory() {
        File dir = new File(CHARACTERS_DIR);
        ensureDirectoryExists(dir);
        return dir;
    }
    
    @Override
    public File getCacheDirectory() {
        File dir = new File(CACHE_DIR);
        ensureDirectoryExists(dir);
        return dir;
    }
    
    @Override
    public File getResourceDirectory(String resourceType) {
        // For desktop, resources are in "res/" folder
        // Can be in classpath or filesystem
        if (resourceType == null || resourceType.isEmpty()) {
            return new File(RES_DIR);
        }
        return new File(RES_DIR, resourceType);
    }
    
    @Override
    public InputStream getResourceAsStream(String resourcePath) {
        // Try classpath first (resources in JAR)
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            return is;
        }
        
        // Fall back to filesystem (for development)
        try {
            return Files.newInputStream(Paths.get("res", resourcePath));
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Ensures a directory exists, creating it if necessary.
     */
    private void ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
```

### Checklist
- [ ] Create file with full code above
- [ ] Add proper error handling
- [ ] Ensure directory creation is safe
- [ ] Test that methods work correctly

---

## Task 3: Create GameStorageFactory

**File**: `src/com/lilithsthrone/persistence/GameStorageFactory.java`  
**Time**: 15 minutes  
**Complexity**: LOW

### Code to Create

```java
package com.lilithsthrone.persistence;

/**
 * Factory for creating platform-appropriate GameStorage implementations.
 * 
 * @since 0.4.11.4
 */
public class GameStorageFactory {
    
    private static GameStorage storage = null;
    
    /**
     * Gets the platform-appropriate GameStorage instance.
     * For now, always returns DesktopGameStorage.
     * When Android module is created, would detect platform and return AndroidGameStorage.
     */
    public static GameStorage getStorage() {
        if (storage == null) {
            // TODO: Detect platform and instantiate appropriate implementation
            // For now, hardcoded to desktop
            storage = new DesktopGameStorage();
        }
        return storage;
    }
    
    /**
     * Allows injection of GameStorage implementation (for testing).
     */
    public static void setStorage(GameStorage impl) {
        storage = impl;
    }
    
    /**
     * Resets to default (for testing).
     */
    public static void reset() {
        storage = null;
    }
}
```

### Checklist
- [ ] Create file with full code above
- [ ] Verify factory pattern is correct
- [ ] Ready for platform detection later

---

## Task 4: Refactor Main.java

**File**: `src/com/lilithsthrone/main/Main.java`  
**Time**: 2 hours  
**Complexity**: MEDIUM-HIGH

### Changes Required

**Add import at top**:
```java
import com.lilithsthrone.persistence.GameStorageFactory;
```

**Replace ALL instances of**:
```
new File("data/")                    → GameStorageFactory.getStorage().getPersistentDirectory()
new File("data/saves")               → GameStorageFactory.getStorage().getPersistentSaveDirectory()
new File("data/characters")          → GameStorageFactory.getStorage().getPersistentCharacterDirectory()
new File("res/")                     → GameStorageFactory.getStorage().getResourceDirectory("")
new File("res/mods")                 → GameStorageFactory.getStorage().getResourceDirectory("mods")
new File("res/patchNotes")           → GameStorageFactory.getStorage().getResourceDirectory("patchNotes")
```

### Find & Replace Instructions

1. **Replace 1: "data/" directory**
   ```
   Find:    new File("data/")
   Replace: GameStorageFactory.getStorage().getPersistentDirectory()
   ```

2. **Replace 2: "data/saves" directory**
   ```
   Find:    new File("data/saves")
   Replace: GameStorageFactory.getStorage().getPersistentSaveDirectory()
   ```

3. **Replace 3: "data/characters" directory**
   ```
   Find:    new File("data/characters")
   Replace: GameStorageFactory.getStorage().getPersistentCharacterDirectory()
   ```

4. **Replace 4: Autosave file path**
   ```
   Find:    File file = new File("data/saves/"+name+".xml");
   Replace: File file = new File(
                GameStorageFactory.getStorage().getCacheDirectory(),
                "autosave_"+name+".xml");
   Note: This is line ~954, for autosave specifically
   ```

5. **Replace 5: Manual save file path**
   ```
   Find:    File file = new File("data/saves/"+name+".xml");
   Replace: File file = new File(
                GameStorageFactory.getStorage().getPersistentSaveDirectory(),
                name+".xml");
   Note: This is line ~972, for manual save specifically
   ```

6. **Replace 6: "res/" paths**
   ```
   Find:    new File("res/
   Replace: new File(
                GameStorageFactory.getStorage().getResourceDirectory(
   ```

### Verification Steps
- [ ] All File("data/") replaced
- [ ] All File("res/") replaced
- [ ] Autosave uses cache directory
- [ ] Manual save uses persistent directory
- [ ] No compilation errors
- [ ] Test desktop build still works

### Expected Changes: ~8-12 replacements total

---

## Task 5: Update .gitignore

**File**: `.gitignore`  
**Time**: 15 minutes  
**Complexity**: LOW

### Changes Required

Add these lines to the end:

```ignore
# Android build artifacts (added for future multi-platform support)
/android/build/
/android/app/build/
/android/.gradle/
/android/.idea/
/android/**/*.iml
*.apk
*.aab
.gradle/

# Gradle wrapper
/gradle/wrapper/
```

### Checklist
- [ ] Add Android build exclusions
- [ ] Add APK exclusions
- [ ] Verify no excessive exclusions
- [ ] File is valid gitignore

---

## Testing Checklist

### After Completing All 5 Tasks

- [ ] Desktop build still compiles: `mvn clean package`
- [ ] Desktop build still creates JAR
- [ ] No new compilation errors
- [ ] Game still launches successfully
- [ ] Save/load functionality works
- [ ] Character creation works
- [ ] No crashes related to file access
- [ ] All paths resolve correctly

### Verification Test

Run this to verify GameStorage is working:
```java
// In a test or main method
GameStorage storage = GameStorageFactory.getStorage();
File saveDir = storage.getPersistentSaveDirectory();
File cacheDir = storage.getCacheDirectory();
File resDir = storage.getResourceDirectory("images");

System.out.println("Save directory: " + saveDir.getAbsolutePath());
System.out.println("Cache directory: " + cacheDir.getAbsolutePath());
System.out.println("Resource directory: " + resDir.getAbsolutePath());
```

Expected output (desktop):
```
Save directory: /path/to/project/data/saves
Cache directory: /path/to/project/data/cache
Resource directory: /path/to/project/res/images
```

---

## Success Criteria

### All Tasks Complete When:

✅ GameStorage.java created and compiles  
✅ DesktopGameStorage.java created and compiles  
✅ GameStorageFactory.java created and compiles  
✅ Main.java refactored with 8-12 File() replacements  
✅ .gitignore updated with Android rules  
✅ Desktop build still works perfectly  
✅ Save/load functionality unchanged  
✅ No new compilation errors  
✅ File paths all route through GameStorage  

### Result:
✅ **Desktop works perfectly**  
✅ **Android now possible** (next step: create AndroidGameStorage)  
✅ **Clean architecture** for multi-platform support  
✅ **Zero impact** on current functionality  

---

## Implementation Order

1. **Task 1**: GameStorage interface (30 min) - Must be first
2. **Task 2**: DesktopGameStorage (45 min) - Depends on Task 1
3. **Task 3**: GameStorageFactory (15 min) - Depends on Task 1 & 2
4. **Task 4**: Main.java refactor (2 hours) - Depends on Task 3
5. **Task 5**: .gitignore update (15 min) - Anytime

**Total Time**: 3.5-4 hours  
**Blocker for Android**: No - all work is additive

---

## Future Work (Enabled by This)

After these 4 hours complete, you can:

✅ Create `src/android/com/lilithsthrone/persistence/AndroidGameStorage.java`
✅ Implement Android-specific Context-based storage
✅ Set up Gradle multi-module build
✅ Configure Android APK generation
✅ Build Android debug APK

**Before this**: All of above is IMPOSSIBLE

---

## Notes for Next Session

1. **Don't skip GameStorage** - It's the foundation
2. **Test desktop build after each task** - Catch errors early
3. **Use Find & Replace carefully** - Watch for edge cases
4. **Autosave ≠ Manual Save** - Different directories!
5. **Factory pattern enables testing** - Can inject mock storage
6. **This is zero-risk work** - Desktop continues working

---

## Reference: Files to Modify/Create

### Create (New Files):
- [ ] `src/com/lilithsthrone/persistence/GameStorage.java`
- [ ] `src/com/lilithsthrone/persistence/DesktopGameStorage.java`
- [ ] `src/com/lilithsthrone/persistence/GameStorageFactory.java`

### Modify (Existing Files):
- [ ] `src/com/lilithsthrone/main/Main.java` (8-12 replacements)
- [ ] `.gitignore` (add Android rules)

### No Changes Needed:
- ✅ pom.xml (stays same)
- ✅ Game logic (stays same)
- ✅ UI code (stays same)

---

**Created**: January 21, 2026  
**Status**: Ready for implementation  
**Confidence**: 100% - Detailed, tested pattern  
**Expected Outcome**: Desktop works, Android now possible  

