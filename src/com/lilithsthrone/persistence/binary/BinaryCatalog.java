package com.lilithsthrone.persistence.binary;

import java.io.*;
import java.util.*;
import java.util.zip.CRC32;

/**
 * Catalog for storing multiple serialized objects with indexing.
 * Combines index + payload into a single efficient file.
 * 
 * Format:
 * [INDEX_LENGTH:4][INDEX_DATA...][PAYLOAD_DATA...]
 * 
 * This allows:
 * - Reading index first without full payload load
 * - Selective object deserialization
 * - Fast O(1) lookups
 * - Schema validation before reading
 * 
 * @since 0.4.11.3
 * @version 0.4.11.3
 * @author Refactoring Agent
 */
public class BinaryCatalog {
	
	private final BinaryIndex index;
	private final Map<String, BinarySerializable> loadedObjects = new HashMap<>();
	private final byte[] payloadData;
	
	private BinaryCatalog(BinaryIndex index, byte[] payloadData) {
		this.index = index;
		this.payloadData = payloadData;
	}
	
	// ==================== Builder ====================
	
	public static class Builder {
		private final BinaryIndex.Builder indexBuilder = new BinaryIndex.Builder();
		private final ByteArrayOutputStream payload = new ByteArrayOutputStream();
		
		public Builder withDescription(String desc) {
			indexBuilder.withDescription(desc);
			return this;
		}
		
		/**
		 * Add serializable object to catalog.
		 */
		public Builder addObject(String id, BinarySerializable obj) throws IOException {
			if (obj == null) {
				throw new IllegalArgumentException("Object cannot be null");
			}
			
			BinaryStream.Writer payloadWriter = new BinaryStream.Writer();
			payloadWriter.setSchemaVersion(obj.getSchemaVersion());
			payloadWriter.writeString(obj.getTypeId());
			obj.writeBinary(payloadWriter);
			
			byte[] objectData = payloadWriter.toByteArray();
			payload.write(objectData);
			
			long hash = calculateCRC32(objectData);
			indexBuilder.addEntry(id, obj.getTypeId(), objectData.length, hash);
			
			return this;
		}
		
		public BinaryCatalog build() throws IOException {
			BinaryIndex index = indexBuilder.build();
			return new BinaryCatalog(index, payload.toByteArray());
		}
	}
	
	// ==================== Serialization ====================
	
	/**
	 * Write entire catalog to file/stream.
	 */
	public void writeTo(OutputStream os) throws IOException {
		BinaryStream.Writer writer = new BinaryStream.Writer();
		
		// Write index
		BinaryStream.Writer indexWriter = new BinaryStream.Writer();
		index.writeTo(indexWriter);
		byte[] indexData = indexWriter.toByteArray();
		
		// Write index length
		writer.writeInt(indexData.length);
		os.write(writer.toByteArray());
		
		// Write index
		os.write(indexData);
		
		// Write payload
		os.write(payloadData);
	}
	
	/**
	 * Read catalog from file/stream.
	 */
	public static BinaryCatalog readFrom(InputStream is) throws IOException {
		// Read index length
		byte[] lengthBytes = new byte[4];
		int read = is.read(lengthBytes);
		if (read != 4) {
			throw new IOException("Failed to read index length");
		}
		
		BinaryStream.Reader lengthReader = new BinaryStream.Reader(lengthBytes);
		int indexLength = lengthReader.readInt();
		
		// Read index
		byte[] indexData = new byte[indexLength];
		read = is.read(indexData);
		if (read != indexLength) {
			throw new IOException("Failed to read complete index, expected " + indexLength + 
					" bytes, got " + read);
		}
		
		BinaryStream.Reader indexReader = new BinaryStream.Reader(indexData);
		BinaryIndex index = BinaryIndex.readFrom(indexReader);
		
		// Read payload
		ByteArrayOutputStream payloadBuffer = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int n;
		while ((n = is.read(buffer)) != -1) {
			payloadBuffer.write(buffer, 0, n);
		}
		byte[] payloadData = payloadBuffer.toByteArray();
		
		return new BinaryCatalog(index, payloadData);
	}
	
	// ==================== Loading ====================
	
	/**
	 * Get deserialized object by ID. Lazy-loads if not already loaded.
	 */
	public BinarySerializable getObject(String id) throws IOException {
		BinarySerializable cached = loadedObjects.get(id);
		if (cached != null) {
			return cached;
		}
		
		BinaryIndex.IndexEntry entry = index.getEntry(id);
		if (entry == null) {
			return null;
		}
		
		return loadObject(entry);
	}
	
	/**
	 * Load object from index entry.
	 */
	private BinarySerializable loadObject(BinaryIndex.IndexEntry entry) throws IOException {
		byte[] objectData = new byte[entry.size];
		System.arraycopy(payloadData, entry.offset, objectData, 0, entry.size);
		
		// Validate checksum
		long actualHash = calculateCRC32(objectData);
		if (actualHash != entry.hash) {
			throw new IOException("Checksum mismatch for object " + entry.id + 
					": expected " + entry.hash + ", got " + actualHash);
		}
		
		BinaryStream.Reader reader = new BinaryStream.Reader(objectData);
		String typeId = reader.readString();
		
		if (!typeId.equals(entry.typeId)) {
			throw new IOException("Type mismatch for object " + entry.id);
		}
		
		SchemaRegistry registry = SchemaRegistry.getInstance();
		SchemaRegistry.SerializableFactory<?> factory = registry.getFactory(typeId);
		BinarySerializable obj = factory.create(reader);
		
		loadedObjects.put(entry.id, obj);
		return obj;
	}
	
	/**
	 * Preload all objects into memory.
	 */
	public void loadAll() throws IOException {
		for (String id : index.getAllIds()) {
			getObject(id);
		}
	}
	
	/**
	 * Get all loaded objects.
	 */
	public Collection<BinarySerializable> getLoadedObjects() {
		return loadedObjects.values();
	}
	
	// ==================== Index Access ====================
	
	public BinaryIndex getIndex() {
		return index;
	}
	
	public int getSize() {
		return index.getSize();
	}
	
	public boolean contains(String id) {
		return index.contains(id);
	}
	
	public Set<String> getAllIds() {
		return index.getAllIds();
	}
	
	// ==================== Utilities ====================
	
	private static long calculateCRC32(byte[] data) {
		CRC32 crc = new CRC32();
		crc.update(data);
		return crc.getValue();
	}
	
	@Override
	public String toString() {
		return "BinaryCatalog(" + index + ")";
	}
}
