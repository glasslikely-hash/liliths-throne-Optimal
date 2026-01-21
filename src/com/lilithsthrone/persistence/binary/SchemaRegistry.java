package com.lilithsthrone.persistence.binary;

import java.io.IOException;
import java.util.*;

/**
 * Central registry for managing binary schemas, type mappings, and version compatibility.
 * 
 * Responsibilities:
 * - Type ID to class mapping
 * - Version migration handling
 * - Schema validation
 * - Deterministic ID assignment
 * 
 * Design: Static singleton for global access. Thread-safe for registration.
 * 
 * @since 0.4.11.3
 * @version 0.4.11.3
 * @author Refactoring Agent
 */
public class SchemaRegistry {
	
	private static final SchemaRegistry INSTANCE = new SchemaRegistry();
	
	// Type ID to factory mapping
	private final Map<String, SerializableFactory<?>> typeRegistry = new HashMap<>();
	
	// Type class to type ID mapping (for serialization)
	private final Map<Class<?>, String> classToTypeId = new HashMap<>();
	
	// Schema version info
	private final Map<String, Integer> currentSchemaVersions = new HashMap<>();
	
	// Migration handlers (version migration logic)
	private final Map<String, MigrationHandler> migrationHandlers = new HashMap<>();
	
	private volatile int currentEngineVersion = 1;
	
	private SchemaRegistry() {}
	
	public static SchemaRegistry getInstance() {
		return INSTANCE;
	}
	
	// ==================== Registration ====================
	
	/**
	 * Register a serializable type.
	 * Must be called during initialization before any persistence operations.
	 * 
	 * @param typeId unique identifier (e.g., "item.weapon.sword")
	 * @param factory factory function to create instances
	 * @param schemaVersion current schema version for this type
	 */
	public synchronized <T extends BinarySerializable> void registerType(
			String typeId,
			SerializableFactory<T> factory,
			int schemaVersion) {
		
		if (typeId == null || typeId.isEmpty()) {
			throw new IllegalArgumentException("Type ID cannot be null or empty");
		}
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Schema version must be >= 1");
		}
		
		typeRegistry.put(typeId, factory);
		classToTypeId.put(factory.getTargetClass(), typeId);
		currentSchemaVersions.put(typeId, schemaVersion);
	}
	
	/**
	 * Register a migration handler for schema evolution.
	 * Handles reading old versions and converting to new format.
	 * 
	 * @param typeId type identifier
	 * @param fromVersion source schema version
	 * @param toVersion target schema version
	 * @param handler migration logic
	 */
	public synchronized void registerMigration(
			String typeId,
			int fromVersion,
			int toVersion,
			MigrationHandler handler) {
		
		String key = typeId + ":v" + fromVersion + "->v" + toVersion;
		migrationHandlers.put(key, handler);
	}
	
	// ==================== Lookup ====================
	
	/**
	 * Get factory for a type ID.
	 */
	public <T extends BinarySerializable> SerializableFactory<T> getFactory(String typeId) {
		@SuppressWarnings("unchecked")
		SerializableFactory<T> factory = (SerializableFactory<T>) typeRegistry.get(typeId);
		if (factory == null) {
			throw new IllegalArgumentException("Unknown type: " + typeId);
		}
		return factory;
	}
	
	/**
	 * Get type ID for a class.
	 */
	public String getTypeId(Class<?> clazz) {
		String typeId = classToTypeId.get(clazz);
		if (typeId == null) {
			throw new IllegalArgumentException("Unregistered class: " + clazz.getName());
		}
		return typeId;
	}
	
	/**
	 * Get current schema version for a type.
	 */
	public int getCurrentSchemaVersion(String typeId) {
		Integer version = currentSchemaVersions.get(typeId);
		if (version == null) {
			throw new IllegalArgumentException("Unknown type: " + typeId);
		}
		return version;
	}
	
	/**
	 * Check if type is registered.
	 */
	public boolean isTypeRegistered(String typeId) {
		return typeRegistry.containsKey(typeId);
	}
	
	// ==================== Migration ====================
	
	/**
	 * Apply migration to upgrade old version to new format.
	 */
	public Object migrate(String typeId, int fromVersion, int toVersion, 
	                      BinaryStream.Reader stream) throws IOException {
		
		if (fromVersion == toVersion) {
			// No migration needed
			SerializableFactory<?> factory = getFactory(typeId);
			return factory.create(stream);
		}
		
		String migrationKey = typeId + ":v" + fromVersion + "->v" + toVersion;
		MigrationHandler handler = migrationHandlers.get(migrationKey);
		
		if (handler == null) {
			throw new IOException("No migration path from version " + fromVersion + 
					" to " + toVersion + " for type " + typeId);
		}
		
		return handler.migrate(stream);
	}
	
	// ==================== Engine Version ====================
	
	/**
	 * Get global engine version for save files.
	 * Bump when making incompatible changes to binary format.
	 */
	public int getEngineVersion() {
		return currentEngineVersion;
	}
	
	/**
	 * Set engine version. Should only be called during initialization.
	 */
	public synchronized void setEngineVersion(int version) {
		if (version < 1) throw new IllegalArgumentException("Version must be >= 1");
		this.currentEngineVersion = version;
	}
	
	// ==================== Callbacks ====================
	
	@FunctionalInterface
	public interface SerializableFactory<T extends BinarySerializable> {
		/**
		 * Create and deserialize instance from stream.
		 */
		T create(BinaryStream.Reader stream) throws IOException;
		
		/**
		 * Get target class (for reflection).
		 */
		Class<T> getTargetClass();
	}
	
	@FunctionalInterface
	public interface MigrationHandler {
		/**
		 * Migrate object from old format to new.
		 */
		Object migrate(BinaryStream.Reader stream) throws IOException;
	}
	
	// ==================== Debug ====================
	
	public Set<String> getAllRegisteredTypes() {
		return new HashSet<>(typeRegistry.keySet());
	}
	
	public Map<String, Integer> getSchemaVersions() {
		return new HashMap<>(currentSchemaVersions);
	}
}
