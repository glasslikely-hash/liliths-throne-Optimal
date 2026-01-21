package com.lilithsthrone.logic.persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/**
 * Lazy Loading System - on-demand loading of large datasets from binary files.
 * 
 * Strategy:
 *   1. Don't load entire world/character databases on startup
 *   2. Load chunks/sections on first access (lazy)
 *   3. Cache loaded data in memory (with eviction policy)
 *   4. Keep file offsets for unloaded data (no re-parsing needed)
 * 
 * Benefits:
 *   - Startup time: O(1) instead of O(assets)
 *   - Memory: Only loaded assets in RAM
 *   - Performance: Background loading possible for preloading
 *   
 * Implementation:
 *   - File index: Maps asset keys to (offset, size) in binary file
 *   - Memory cache: Keeps loaded assets with LRU eviction
 *   - Background loader: Preloads assets in background thread
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class LazyLoader {
    
    // Configuration
    private static final int DEFAULT_CACHE_SIZE = 100;       // Max cached items
    @SuppressWarnings("unused")
    private static final long CACHE_ITEM_TIMEOUT_MS = 300_000;  // 5 minutes
    
    // Asset metadata
    private final Map<String, AssetMetadata> assetIndex;
    private final Path binaryFilePath;
    
    // Caching
    private final ConcurrentHashMap<String, CachedAsset<?>> cache;
    private final ExecutorService preloadExecutor;
    
    /**
     * Metadata about an asset in the binary file.
     */
    public static class AssetMetadata {
        public final String assetId;
        public final long fileOffset;
        public final int sizeBytes;
        public final String assetType;
        public final long createdTime;
        
        public AssetMetadata(String assetId, long fileOffset, int sizeBytes, String assetType) {
            this.assetId = assetId;
            this.fileOffset = fileOffset;
            this.sizeBytes = sizeBytes;
            this.assetType = assetType;
            this.createdTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Cached asset with timestamp and load status.
     */
    private static class CachedAsset<T> {
        T data;
        long loadedTime;
        long lastAccessTime;
        int accessCount;
        
        CachedAsset(T data) {
            this.data = data;
            this.loadedTime = System.currentTimeMillis();
            this.lastAccessTime = loadedTime;
            this.accessCount = 1;
        }
    }
    
    /**
     * Function interface for loading asset from bytes.
     */
    @FunctionalInterface
    public interface AssetLoader<T> {
        /**
         * Load asset from binary data.
         * @param data Raw binary data for asset
         * @param offset Offset in binary file
         * @return Loaded asset
         * @throws IOException If loading fails
         */
        T load(byte[] data, long offset) throws IOException;
    }
    
    /**
     * Constructor.
     */
    public LazyLoader(Path binaryFilePath) {
        this.binaryFilePath = binaryFilePath;
        this.assetIndex = new ConcurrentHashMap<>();
        this.cache = new ConcurrentHashMap<>();
        this.preloadExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "LazyLoaderPreload");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Register asset metadata (file position).
     * Called during binary file parsing to build index.
     */
    public void registerAsset(String assetId, long fileOffset, int sizeBytes, String assetType) {
        assetIndex.put(assetId, new AssetMetadata(assetId, fileOffset, sizeBytes, assetType));
    }
    
    /**
     * Load asset on demand.
     * Returns cached copy if available, otherwise loads from disk.
     */
    public <T> T load(String assetId, AssetLoader<T> loader) throws IOException {
        // Check cache first
        CachedAsset<T> cached = (CachedAsset<T>) cache.get(assetId);
        if (cached != null) {
            cached.lastAccessTime = System.currentTimeMillis();
            cached.accessCount++;
            return cached.data;
        }
        
        // Load from disk
        AssetMetadata metadata = assetIndex.get(assetId);
        if (metadata == null) {
            throw new IOException("Asset not found: " + assetId);
        }
        
        T data = loadFromFile(metadata, loader);
        
        // Cache result
        cache.put(assetId, new CachedAsset<>(data));
        
        // Cleanup if cache is too large
        if (cache.size() > DEFAULT_CACHE_SIZE) {
            evictLeastRecentlyUsed();
        }
        
        return data;
    }
    
    /**
     * Load multiple assets in parallel.
     */
    public <T> Map<String, T> loadMultiple(Collection<String> assetIds, AssetLoader<T> loader) 
            throws IOException {
        Map<String, T> result = new ConcurrentHashMap<>();
        
        // Load in parallel
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (String assetId : assetIds) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    result.put(assetId, load(assetId, loader));
                } catch (IOException e) {
                    System.err.println("Failed to load asset " + assetId + ": " + e);
                }
            }, preloadExecutor));
        }
        
        // Wait for all loads
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        return result;
    }
    
    /**
     * Preload asset in background (non-blocking).
     */
    public <T> void preloadAsync(String assetId, AssetLoader<T> loader) {
        if (cache.containsKey(assetId) || !assetIndex.containsKey(assetId)) {
            return;
        }
        
        preloadExecutor.execute(() -> {
            try {
                load(assetId, loader);
            } catch (IOException e) {
                System.err.println("Failed to preload asset " + assetId + ": " + e);
            }
        });
    }
    
    /**
     * Preload group of assets (e.g., all location assets).
     */
    public <T> void preloadGroup(String groupPrefix, AssetLoader<T> loader) {
        assetIndex.keySet().stream()
            .filter(id -> id.startsWith(groupPrefix))
            .filter(id -> !cache.containsKey(id))
            .forEach(id -> preloadAsync(id, loader));
    }
    
    /**
     * Clear cache entry.
     */
    public void evictAsset(String assetId) {
        cache.remove(assetId);
    }
    
    /**
     * Clear all cache.
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * Evict least recently used items from cache.
     */
    private void evictLeastRecentlyUsed() {
        // Find LRU item
        cache.entrySet().stream()
            .min(Comparator.comparingLong(e -> e.getValue().lastAccessTime))
            .ifPresent(entry -> cache.remove(entry.getKey()));
    }
    
    /**
     * Load asset from binary file.
     */
    private <T> T loadFromFile(AssetMetadata metadata, AssetLoader<T> loader) throws IOException {
        byte[] data = new byte[metadata.sizeBytes];
        
        try (RandomAccessFile file = new RandomAccessFile(binaryFilePath.toFile(), "r")) {
            file.seek(metadata.fileOffset);
            int bytesRead = file.read(data);
            
            if (bytesRead != metadata.sizeBytes) {
                throw new IOException("Failed to read asset: expected " + metadata.sizeBytes + 
                    " bytes, got " + bytesRead);
            }
            
            return loader.load(data, metadata.fileOffset);
        }
    }
    
    /**
     * Get cache statistics.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("registeredAssets", assetIndex.size());
        stats.put("cachedAssets", cache.size());
        stats.put("cacheUtilization", String.format("%.1f%%", 
            (100.0 * cache.size()) / DEFAULT_CACHE_SIZE));
        
        long totalAccess = cache.values().stream().mapToLong(c -> c.accessCount).sum();
        stats.put("totalCacheAccess", totalAccess);
        
        double hitRate = totalAccess > 0 ? 100.0 * cache.size() / totalAccess : 0;
        stats.put("estimatedHitRate", String.format("%.1f%%", hitRate));
        
        return stats;
    }
    
    /**
     * Shutdown lazy loader.
     */
    public void shutdown() {
        preloadExecutor.shutdown();
        try {
            if (!preloadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                preloadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            preloadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
