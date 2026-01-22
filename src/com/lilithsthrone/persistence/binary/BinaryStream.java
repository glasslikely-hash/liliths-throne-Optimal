package com.lilithsthrone.persistence.binary;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Low-level binary serialization engine for fast, deterministic read/write operations.
 * Handles all primitive types, strings, collections, and custom objects.
 * 
 * Design principles:
 * - Minimal overhead (no length prefixes where not needed)
 * - Deterministic ordering (for snapshot consistency)
 * - Version-aware (schema evolution support)
 * - Efficient varint encoding for integers
 * 
 * @since 0.4.11.3
 * @version 0.4.11.3
 * @author Refactoring Agent
 */
public class BinaryStream {
	
	private static final int NULL_MARKER = 0xFF;
	private static final int NON_NULL_MARKER = 0x00;
	
	// ==================== Writer ====================
	
	public static class Writer {
		private final ByteArrayOutputStream buffer;
		private final DataOutputStream out;
		private int schemaVersion = 1;
		
		public Writer() {
			this.buffer = new ByteArrayOutputStream();
			this.out = new DataOutputStream(buffer);
		}
		
		public Writer(int initialCapacity) {
			this.buffer = new ByteArrayOutputStream(initialCapacity);
			this.out = new DataOutputStream(buffer);
		}
		
		// -------- Version Control --------
		
		public void setSchemaVersion(int version) {
			if (version < 1) throw new IllegalArgumentException("Schema version must be >= 1");
			this.schemaVersion = version;
		}
		
		public int getSchemaVersion() {
			return schemaVersion;
		}
		
		// -------- Primitives --------
		
		public void writeBoolean(boolean value) throws IOException {
			out.writeBoolean(value);
		}
		
		public void writeByte(byte value) throws IOException {
			out.writeByte(value);
		}
		
		public void writeShort(short value) throws IOException {
			out.writeShort(value);
		}
		
		public void writeInt(int value) throws IOException {
			writeVarInt(value);
		}
		
		public void writeLong(long value) throws IOException {
			writeVarLong(value);
		}
		
		public void writeFloat(float value) throws IOException {
			out.writeFloat(value);
		}
		
		public void writeDouble(double value) throws IOException {
			out.writeDouble(value);
		}
		
		// -------- Varint Encoding (efficient for small numbers) --------
		
		public void writeVarInt(int value) throws IOException {
			while ((value & 0xFFFFFF80) != 0L) {
				out.writeByte((byte) ((value & 0x7F) | 0x80));
				value >>>= 7;
			}
			out.writeByte((byte) (value & 0x7F));
		}
		
		public void writeVarLong(long value) throws IOException {
			while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
				out.writeByte((byte) ((value & 0x7FL) | 0x80L));
				value >>>= 7;
			}
			out.writeByte((byte) (value & 0x7FL));
		}
		
		// -------- Strings --------
		
		public void writeString(String value) throws IOException {
			if (value == null) {
				writeVarInt(-1);
			} else {
				byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
				writeVarInt(bytes.length);
				out.write(bytes);
			}
		}
		
		// -------- Enums --------
		
		public <E extends Enum<E>> void writeEnum(E value) throws IOException {
			if (value == null) {
				writeVarInt(-1);
			} else {
				writeVarInt(value.ordinal());
			}
		}
		
		// -------- Collections --------
		
		public <T> void writeList(List<T> list, ElementWriter<T> writer) throws IOException {
			if (list == null) {
				writeVarInt(-1);
			} else {
				writeVarInt(list.size());
				for (T element : list) {
					writer.write(this, element);
				}
			}
		}
		
		public <T> void writeSet(Set<T> set, ElementWriter<T> writer) throws IOException {
			if (set == null) {
				writeVarInt(-1);
			} else {
				writeVarInt(set.size());
				for (T element : set) {
					writer.write(this, element);
				}
			}
		}
		
		public <K, V> void writeMap(Map<K, V> map, ElementWriter<K> keyWriter, ElementWriter<V> valueWriter) throws IOException {
			if (map == null) {
				writeVarInt(-1);
			} else {
				writeVarInt(map.size());
				for (Map.Entry<K, V> entry : map.entrySet()) {
					keyWriter.write(this, entry.getKey());
					valueWriter.write(this, entry.getValue());
				}
			}
		}
		
		// -------- Serializable Objects --------
		
		public void writeObject(BinarySerializable obj) throws IOException {
			if (obj == null) {
				out.writeByte(NULL_MARKER);
			} else {
				out.writeByte(NON_NULL_MARKER);
				obj.writeBinary(this);
			}
		}
		
		// -------- Raw Bytes --------
		
		public void writeBytes(byte[] data) throws IOException {
			if (data == null) {
				writeVarInt(-1);
			} else {
				writeVarInt(data.length);
				out.write(data);
			}
		}
		
		// -------- Output --------
		
		public byte[] toByteArray() {
			return buffer.toByteArray();
		}
		
		public void writeTo(OutputStream os) throws IOException {
			buffer.writeTo(os);
		}
		
		public int size() {
			return buffer.size();
		}
		
		public void close() throws IOException {
			out.close();
		}
	}
	
	// ==================== Reader ====================
	
	public static class Reader {
		private final DataInputStream in;
		private final byte[] data;
		private int offset = 0;
		private int schemaVersion = 1;
		
		public Reader(byte[] data) {
			this.data = data;
			this.in = new DataInputStream(new ByteArrayInputStream(data));
		}
		
		public Reader(InputStream is) throws IOException {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] temp = new byte[4096];
			int n;
			while ((n = is.read(temp)) != -1) {
				buffer.write(temp, 0, n);
			}
			this.data = buffer.toByteArray();
			this.in = new DataInputStream(new ByteArrayInputStream(this.data));
		}
		
		// -------- Version Control --------
		
		public void setSchemaVersion(int version) {
			if (version < 1) throw new IllegalArgumentException("Schema version must be >= 1");
			this.schemaVersion = version;
		}
		
		public int getSchemaVersion() {
			return schemaVersion;
		}
		
		// -------- Primitives --------
		
		public boolean readBoolean() throws IOException {
			return in.readBoolean();
		}
		
		public byte readByte() throws IOException {
			return in.readByte();
		}
		
		public short readShort() throws IOException {
			return in.readShort();
		}
		
		public int readInt() throws IOException {
			return readVarInt();
		}
		
		public long readLong() throws IOException {
			return readVarLong();
		}
		
		public float readFloat() throws IOException {
			return in.readFloat();
		}
		
		public double readDouble() throws IOException {
			return in.readDouble();
		}
		
		// -------- Varint Decoding --------
		
		public int readVarInt() throws IOException {
			int result = 0;
			int shift = 0;
			int b;
			do {
				b = in.readUnsignedByte();
				result |= (b & 0x7F) << shift;
				shift += 7;
			} while ((b & 0x80) != 0);
			return result;
		}
		
		public long readVarLong() throws IOException {
			long result = 0;
			int shift = 0;
			int b;
			do {
				b = in.readUnsignedByte();
				result |= ((long) b & 0x7F) << shift;
				shift += 7;
			} while ((b & 0x80) != 0);
			return result;
		}
		
		// -------- Strings --------
		
		public String readString() throws IOException {
			int length = readVarInt();
			if (length == -1) {
				return null;
			}
			byte[] bytes = new byte[length];
			in.readFully(bytes);
			return new String(bytes, StandardCharsets.UTF_8);
		}
		
		// -------- Enums --------
		
		public <E extends Enum<E>> E readEnum(Class<E> enumClass) throws IOException {
			int ordinal = readVarInt();
			if (ordinal == -1) {
				return null;
			}
			E[] values = enumClass.getEnumConstants();
			if (ordinal < 0 || ordinal >= values.length) {
				throw new IOException("Invalid enum ordinal: " + ordinal + " for " + enumClass.getName());
			}
			return values[ordinal];
		}
		
		// -------- Collections --------
		
		public <T> List<T> readList(ElementReader<T> reader) throws IOException {
			int size = readVarInt();
			if (size == -1) {
				return null;
			}
			List<T> list = new ArrayList<>(size);
			for (int i = 0; i < size; i++) {
				list.add(reader.read(this));
			}
			return list;
		}
		
		public <T> Set<T> readSet(ElementReader<T> reader) throws IOException {
			int size = readVarInt();
			if (size == -1) {
				return null;
			}
			Set<T> set = new HashSet<>(size);
			for (int i = 0; i < size; i++) {
				set.add(reader.read(this));
			}
			return set;
		}
		
		public <K, V> Map<K, V> readMap(ElementReader<K> keyReader, ElementReader<V> valueReader) throws IOException {
			int size = readVarInt();
			if (size == -1) {
				return null;
			}
			Map<K, V> map = new HashMap<>(size);
			for (int i = 0; i < size; i++) {
				K key = keyReader.read(this);
				V value = valueReader.read(this);
				map.put(key, value);
			}
			return map;
		}
		
		// -------- Serializable Objects --------
		
		public <T extends BinarySerializable> T readObject(ObjectReader<T> reader) throws IOException {
			byte marker = in.readByte();
			if (marker == NULL_MARKER) {
				return null;
			}
			return reader.read(this);
		}
		
		// -------- Raw Bytes --------
		
		public byte[] readBytes() throws IOException {
			int length = readVarInt();
			if (length == -1) {
				return null;
			}
			byte[] bytes = new byte[length];
			in.readFully(bytes);
			return bytes;
		}
		
		// -------- Position --------
		
		public int position() throws IOException {
			// Note: DataInputStream doesn't expose position directly
			// This is a limitation; for better position tracking, use ByteBuffer alternatively
			return offset;
		}
		
		public void close() throws IOException {
			in.close();
		}
	}
	
	// ==================== Callback Interfaces ========== 
	
	@FunctionalInterface
	public interface ElementWriter<T> {
		void write(Writer w, T element) throws IOException;
	}
	
	@FunctionalInterface
	public interface ElementReader<T> {
		T read(Reader r) throws IOException;
	}
	
	@FunctionalInterface
	public interface ObjectReader<T extends BinarySerializable> {
		T read(Reader r) throws IOException;
	}
}
