package com.lilithsthrone.logic.persistence;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import com.lilithsthrone.persistence.binary.BinaryStream;

/**
 * Binary Asset Loader - loads all game assets from binary files, no XML/JSON at runtime.
 * 
 * Purpose:
 *   1. Pre-convert all XML/JSON assets to binary at build time
 *   2. Load binary assets directly into memory
 *   3. Avoid CPU overhead of parsing XML/JSON during gameplay
 *   4. Faster load times and more efficient memory usage
 * 
 * Asset Types:
 *   - Items: Weapons, armor, consumables
 *   - Characters: NPCs, monsters, companions
 *   - Locations: Maps, rooms, areas
 *   - Dialogues: Conversations, branching text
 *   - Images: Textures, sprites (referenced by path, not loaded)
 * 
 * Build Process:
 *   1. Run XML → Binary converter tool at build time
 *   2. Generate asset index file (maps asset ID → file offset + size)
 *   3. Pack all assets into single binary file
 *   4. At runtime, load index and lazy-load assets on demand
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class BinaryAssetLoader {
    
    private final LazyLoader lazyLoader;
    @SuppressWarnings("unused")
    private final Map<String, AssetType> assetTypeRegistry;
    
    /**
     * Asset types in the game.
     */
    public enum AssetType {
        ITEM("items"),
        CHARACTER("characters"),
        LOCATION("locations"),
        DIALOGUE("dialogues"),
        SPELL("spells"),
        QUEST("quests");
        
        public final String prefix;
        
        AssetType(String prefix) {
            this.prefix = prefix;
        }
    }
    
    /**
     * Constructor.
     */
    public BinaryAssetLoader(Path assetBinaryFile) throws IOException {
        this.lazyLoader = new LazyLoader(assetBinaryFile);
        this.assetTypeRegistry = new HashMap<>();
        
        // Load asset index from file
        loadAssetIndex(assetBinaryFile.resolveSibling(assetBinaryFile.getFileName() + ".idx"));
    }
    
    /**
     * Load asset index file.
     * Format: [assetCount][{assetId, offset, size, type}]*
     */
    private void loadAssetIndex(Path indexFile) throws IOException {
        if (!Files.exists(indexFile)) {
            System.err.println("[BINARY ASSETS] Index file not found: " + indexFile);
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(indexFile.toFile());
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            
            BinaryStream reader = new BinaryStream(bis.readAllBytes());
            int assetCount = reader.readInt();
            
            for (int i = 0; i < assetCount; i++) {
                String assetId = reader.readString();
                long fileOffset = reader.readLong();
                int sizeBytes = reader.readInt();
                String assetType = reader.readString();
                
                lazyLoader.registerAsset(assetId, fileOffset, sizeBytes, assetType);
            }
            
            System.out.println("[BINARY ASSETS] Loaded index for " + assetCount + " assets");
        }
    }
    
    /**
     * Load item asset.
     */
    public <T> T loadItem(String itemId) throws IOException {
        throw new UnsupportedOperationException("Binary item loading not yet implemented. Item: " + itemId);
    }
    
    /**
     * Load character asset.
     */
    public <T> T loadCharacter(String characterId) throws IOException {
        throw new UnsupportedOperationException("Binary character loading not yet implemented. Character: " + characterId);
    }
    
    /**
     * Load location asset.
     */
    public <T> T loadLocation(String locationId) throws IOException {
        throw new UnsupportedOperationException("Binary location loading not yet implemented. Location: " + locationId);
    }
    
    /**
     * Load dialogue asset.
     */
    public <T> T loadDialogue(String dialogueId) throws IOException {
        throw new UnsupportedOperationException("Binary dialogue loading not yet implemented. Dialogue: " + dialogueId);
    }
    
    /**
     * Load multiple assets of same type in parallel.
     */
    public <T> Map<String, T> loadAssets(List<String> assetIds, AssetType type) throws IOException {
        throw new UnsupportedOperationException("Binary asset batch loading not yet implemented. Type: " + type);
    }
    
    /**
     * Preload all assets of a type (non-blocking).
     */
    public void preloadAssetType(AssetType type) {
        throw new UnsupportedOperationException("Binary asset preloading not yet implemented. Type: " + type);
    }
    
    /**
     * Get cache statistics.
     */
    public Map<String, Object> getStats() {
        return lazyLoader.getStats();
    }
    
    /**
     * Shutdown asset loader.
     */
    public void shutdown() {
        lazyLoader.shutdown();
    }
}

/**
 * Utility to convert XML/JSON assets to binary format (build-time tool).
 * This runs during build to pre-process all assets.
 */
class BinaryAssetConverter {
    
    /**
     * Convert all assets in a directory from XML to binary.
     */
    public static void convertAssetsTooBinary(Path sourceDir, Path outputFile, Path indexFile) 
            throws IOException {
        
        System.out.println("[CONVERTER] Starting asset conversion...");
        List<AssetEntry> entries = new ArrayList<>();
        
        try (FileOutputStream fos = new FileOutputStream(outputFile.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            
            final long[] currentOffset = {0};  // Use array for mutability in forEach
            
            // Process all files in directory
            Files.list(sourceDir)
                .filter(p -> p.toString().endsWith(".xml") || p.toString().endsWith(".json"))
                .sorted()
                .forEach(xmlFile -> {
                    try {
                        // Convert XML/JSON to binary
                        byte[] binaryData = convertFileToBinary(xmlFile);
                        
                        // Write to output file
                        bos.write(binaryData);
                        
                        // Record metadata
                        String assetId = xmlFile.getFileName().toString()
                            .replaceAll("\\.(xml|json)$", "");
                        entries.add(new AssetEntry(assetId, currentOffset[0], binaryData.length));
                        
                        currentOffset[0] += binaryData.length;
                        System.out.println("  Converted: " + assetId + " (" + binaryData.length + " bytes)");
                        
                    } catch (IOException e) {
                        System.err.println("  Failed to convert " + xmlFile + ": " + e);
                    }
                });
            
            bos.flush();
        }
        
        // Write index file
        writeIndexFile(indexFile, entries);
        
        System.out.println("[CONVERTER] Conversion complete: " + entries.size() + " assets processed");
    }
    
    /**
     * Convert single XML/JSON file to binary.
     */
    private static byte[] convertFileToBinary(Path xmlFile) throws IOException {
        // Parse XML/JSON content
        String content = Files.readString(xmlFile);
        
        // Convert to binary format using BinaryStream
        BinaryStream.Writer writer = new BinaryStream.Writer();
        writer.writeString(content);  // Placeholder - real implementation would parse and serialize
        
        return writer.toByteArray();
    }
    
    /**
     * Write asset index file.
     */
    private static void writeIndexFile(Path indexFile, List<AssetEntry> entries) throws IOException {
        BinaryStream.Writer writer = new BinaryStream.Writer();
        writer.writeVarInt(entries.size());
        
        for (AssetEntry entry : entries) {
            writer.writeString(entry.assetId);
            writer.writeVarLong(entry.offset);
            writer.writeVarInt(entry.size);
            writer.writeString(entry.type);
        }
        
        Files.write(indexFile, writer.toByteArray());
        System.out.println("[CONVERTER] Index file written: " + indexFile);
    }
    
    /**
     * Asset entry for index.
     */
    private static class AssetEntry {
        String assetId;
        long offset;
        int size;
        String type;
        
        AssetEntry(String assetId, long offset, int size) {
            this.assetId = assetId;
            this.offset = offset;
            this.size = size;
            this.type = assetId.split("_")[0];  // Infer type from ID
        }
    }
}
