package com.lilithsthrone.persistence.binary;

import java.io.*;
import java.util.*;

/**
 * Index for fast O(1) lookup of items in binary catalogs without full deserialization.
 * Stores metadata (offset, size, type) for each serialized object.
 * 
 * Format:
 * - Header with version and entry count
 * - Index entries: typeId, offset, size, hash(metadata)
 * - Payload: actual serialized objects
 * 
 * Usage: Before reading full object, check index to verify it exists and get metadata.
 * 
 * @since 0.4.11.3
 * @version 0.4.11.3
 * @author Refactoring Agent
 */
public class BinaryIndex {
	
	private static final int MAGIC_HEADER = 0xB1FADA7A; // "BIFADATA"
	private static final int INDEX_VERSION = 1;
	
	/**
	 * Entry in the index pointing to a serialized object.
	 */
	public static class IndexEntry {
		public final String id;           // Unique identifier in catalog
		public final String typeId;       // Type identifier for factory lookup
		public final int offset;          // Byte offset in payload
		public final int size;            // Byte size of object data
		public final long hash;           // CRC32 or similar for validation
		public final long timestamp;      // When object was serialized
		
		public IndexEntry(String id, String typeId, int offset, int size, long hash, long timestamp) {
			this.id = id;
			this.typeId = typeId;
			this.offset = offset;
			this.size = size;
			this.hash = hash;
			this.timestamp = timestamp;
		}
	}
	
	private final Map<String, IndexEntry> entries = new LinkedHashMap<>();
	private long createdAt = System.currentTimeMillis();
	private String description = "";
	
	// ==================== Building ====================
	
	public static class Builder {
		private final BinaryIndex index = new BinaryIndex();
		private int currentOffset = 0;
		
		public Builder withDescription(String desc) {
			index.description = desc;
			return this;
		}
		
		/**
		 * Add entry to index.
		 * Call this after serializing each object.
		 */
		public Builder addEntry(String id, String typeId, int size, long hash) {
			if (id == null || id.isEmpty()) {
				throw new IllegalArgumentException("ID cannot be null or empty");
			}
			if (index.entries.containsKey(id)) {
				throw new IllegalArgumentException("Duplicate ID: " + id);
			}
			
			IndexEntry entry = new IndexEntry(
					id, typeId, currentOffset, size, hash, System.currentTimeMillis()
			);
			index.entries.put(id, entry);
			currentOffset += size;
			return this;
		}
		
		public BinaryIndex build() {
			return index;
		}
	}
	
	// ==================== Lookup ====================
	
	/**
	 * Get entry by ID. Returns null if not found.
	 */
	public IndexEntry getEntry(String id) {
		return entries.get(id);
	}
	
	/**
	 * Check if ID exists in index.
	 */
	public boolean contains(String id) {
		return entries.containsKey(id);
	}
	
	/**
	 * Get all entries of a specific type.
	 */
	public List<IndexEntry> getEntriesByType(String typeId) {
		List<IndexEntry> result = new ArrayList<>();
		for (IndexEntry entry : entries.values()) {
			if (entry.typeId.equals(typeId)) {
				result.add(entry);
			}
		}
		return result;
	}
	
	/**
	 * Get all entry IDs (in insertion order).
	 */
	public Set<String> getAllIds() {
		return new LinkedHashSet<>(entries.keySet());
	}
	
	/**
	 * Get total number of entries.
	 */
	public int getSize() {
		return entries.size();
	}
	
	// ==================== Serialization ====================
	
	/**
	 * Write index to stream. Format:
	 * [MAGIC_HEADER:4][VERSION:4][CREATED_AT:8][DESC_LEN:4][DESC:?][ENTRY_COUNT:4]
	 * [ENTRY...ENTRY]
	 */
	public void writeTo(BinaryStream.Writer stream) throws IOException {
		stream.writeInt(MAGIC_HEADER);
		stream.writeInt(INDEX_VERSION);
		stream.writeLong(createdAt);
		stream.writeString(description);
		
		stream.writeInt(entries.size());
		for (IndexEntry entry : entries.values()) {
			stream.writeString(entry.id);
			stream.writeString(entry.typeId);
			stream.writeInt(entry.offset);
			stream.writeInt(entry.size);
			stream.writeLong(entry.hash);
			stream.writeLong(entry.timestamp);
		}
	}
	
	/**
	 * Read index from stream.
	 */
	public static BinaryIndex readFrom(BinaryStream.Reader stream) throws IOException {
		int magic = stream.readInt();
		if (magic != MAGIC_HEADER) {
			throw new IOException("Invalid index header: 0x" + Integer.toHexString(magic));
		}
		
		int version = stream.readInt();
		if (version != INDEX_VERSION) {
			throw new IOException("Unsupported index version: " + version);
		}
		
		long createdAt = stream.readLong();
		String description = stream.readString();
		
		BinaryIndex index = new BinaryIndex();
		index.createdAt = createdAt;
		index.description = description;
		
		int entryCount = stream.readInt();
		for (int i = 0; i < entryCount; i++) {
			String id = stream.readString();
			String typeId = stream.readString();
			int offset = stream.readInt();
			int size = stream.readInt();
			long hash = stream.readLong();
			long timestamp = stream.readLong();
			
			IndexEntry entry = new IndexEntry(id, typeId, offset, size, hash, timestamp);
			index.entries.put(id, entry);
		}
		
		return index;
	}
	
	// ==================== Utilities ====================
	
	public String getDescription() {
		return description;
	}
	
	public long getCreatedAt() {
		return createdAt;
	}
	
	/**
	 * Calculate total payload size in bytes.
	 */
	public int getTotalPayloadSize() {
		if (entries.isEmpty()) return 0;
		IndexEntry last = (IndexEntry) entries.values().toArray()[entries.size() - 1];
		return last.offset + last.size;
	}
	
	@Override
	public String toString() {
		return String.format("BinaryIndex(%d entries, %d bytes, created=%d)",
				entries.size(),
				getTotalPayloadSize(),
				createdAt);
	}
}
