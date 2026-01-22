package com.lilithsthrone.logic.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.lilithsthrone.persistence.binary.BinarySerializable;
import com.lilithsthrone.persistence.binary.BinaryStream.Reader;
import com.lilithsthrone.persistence.binary.BinaryStream.Writer;

/**
 * Unified game state container holding all mutable game data.
 * 
 * Separated into:
 * - DETERMINISTIC STATE: Quest progress, story choices, visited areas, relationships
 * - DYNAMIC STATE: Inventory, buffs, health, temporary effects
 * - HYBRID STATE: Character attributes (deterministic but mutable)
 * 
 * This is the single source of truth for game state. All logic engines read/write
 * through this container, and all persistence (snapshots/deltas) serializes this.
 * 
 * @author Refactoring Agent
 * @version 1.0
 */
public class GameState implements BinarySerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCHEMA_VERSION = 1;
    private static final String TYPE_ID = "game_state"; // Logic Layer GameState

    // DETERMINISTIC STATE (Snapshot)
    private PlayerState playerState;
    private WorldState worldState;
    private Map<String, Boolean> questFlags;        // Quest completion, story choices
    private Map<String, Integer> questProgress;     // Objective completion counts
    private Set<String> visitedLocations;           // Locations player has been to
    private Map<String, String> npcRelationships;   // npcId -> relationshipStatus (FRIENDLY, NEUTRAL, HOSTILE, ENSLAVED)
    private Map<String, Integer> affectionLevels;   // npcId -> affection value
    
    // DYNAMIC STATE (Delta)
    private InventoryState inventoryState;
    private BuffState buffState;
    private Map<String, Integer> temporaryBuffs;    // buffId -> stacks/duration
    
    // HYBRID STATE (Deterministic but Mutable)
    private CharacterAttributeState playerAttributes;
    private Map<String, NpcState> npcStates;        // npcId -> current status (health, location, etc)
    
    // METADATA
    private UUID gameId;                            // Unique game instance ID
    private long createdTime;                       // When this save was created
    private long lastSavedTime;                     // When last snapshot was saved
    private long totalPlayTimeMs;                   // Total play time in milliseconds
    private int turnCounter;                        // Global turn/action counter for determinism
    private String gameVersion;                     // Game version when saved
    private boolean isDirty;                        // Flag indicating state has changed

    // TRANSIENT STATE (Not persisted, lost on reload)
    private transient CombatState currentCombatState;  // Only exists during combat
    private transient List<GameStateChangeListener> changeListeners = new ArrayList<>();

    /**
     * Create a new game state from scratch
     */
    public GameState() {
        this.gameId = UUID.randomUUID();
        this.createdTime = System.currentTimeMillis();
        this.lastSavedTime = System.currentTimeMillis();
        this.totalPlayTimeMs = 0;
        this.turnCounter = 0;
        this.gameVersion = "1.0.0";
        this.isDirty = true;

        // Initialize all state containers
        this.playerState = new PlayerState();
        this.worldState = new WorldState();
        this.inventoryState = new InventoryState();
        this.buffState = new BuffState();
        this.playerAttributes = new CharacterAttributeState();
        
        // Initialize maps
        this.questFlags = new HashMap<>();
        this.questProgress = new HashMap<>();
        this.visitedLocations = new HashSet<>();
        this.npcRelationships = new HashMap<>();
        this.affectionLevels = new HashMap<>();
        this.temporaryBuffs = new HashMap<>();
        this.npcStates = new HashMap<>();
    }

    // ==================== DETERMINISTIC STATE GETTERS ====================

    public PlayerState getPlayerState() {
        return playerState;
    }

    public WorldState getWorldState() {
        return worldState;
    }

    public Map<String, Boolean> getQuestFlags() {
        return questFlags;
    }

    public Map<String, Integer> getQuestProgress() {
        return questProgress;
    }

    public Set<String> getVisitedLocations() {
        return visitedLocations;
    }

    public Map<String, String> getNpcRelationships() {
        return npcRelationships;
    }

    public Map<String, Integer> getAffectionLevels() {
        return affectionLevels;
    }

    // ==================== DYNAMIC STATE GETTERS ====================

    public InventoryState getInventoryState() {
        return inventoryState;
    }

    public BuffState getBuffState() {
        return buffState;
    }

    public Map<String, Integer> getTemporaryBuffs() {
        return temporaryBuffs;
    }

    // ==================== HYBRID STATE GETTERS ====================

    public CharacterAttributeState getPlayerAttributes() {
        return playerAttributes;
    }

    public Map<String, NpcState> getNpcStates() {
        return npcStates;
    }

    public NpcState getNpcState(String npcId) {
        return npcStates.get(npcId);
    }

    // ==================== TRANSIENT STATE GETTERS ====================

    public CombatState getCurrentCombatState() {
        return currentCombatState;
    }

    public void setCurrentCombatState(CombatState combatState) {
        this.currentCombatState = combatState;
        markDirty();
    }

    public boolean isInCombat() {
        return currentCombatState != null;
    }

    // ==================== METADATA GETTERS ====================

    public UUID getGameId() {
        return gameId;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getLastSavedTime() {
        return lastSavedTime;
    }

    public void setLastSavedTime(long timestamp) {
        this.lastSavedTime = timestamp;
    }

    public long getTotalPlayTimeMs() {
        return totalPlayTimeMs;
    }

    public void addPlayTime(long timeMs) {
        this.totalPlayTimeMs += timeMs;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public int incrementTurnCounter() {
        return ++this.turnCounter;
    }

    public String getGameVersion() {
        return gameVersion;
    }

    public void setGameVersion(String version) {
        this.gameVersion = version;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void markDirty() {
        this.isDirty = true;
        notifyChangeListeners();
    }

    public void markClean() {
        this.isDirty = false;
    }

    // ==================== QUERY METHODS ====================

    /**
     * Check if a quest flag is set
     */
    public boolean hasQuestFlag(String flagId) {
        return questFlags.getOrDefault(flagId, false);
    }

    /**
     * Set a quest flag
     */
    public void setQuestFlag(String flagId, boolean value) {
        if (questFlags.put(flagId, value) != (value ? true : null)) {
            markDirty();
        }
    }

    /**
     * Get quest progress for an objective
     */
    public int getQuestProgress(String objectiveId) {
        return questProgress.getOrDefault(objectiveId, 0);
    }

    /**
     * Update quest progress
     */
    public void setQuestProgress(String objectiveId, int value) {
        if (questProgress.put(objectiveId, value) != value) {
            markDirty();
        }
    }

    /**
     * Mark a location as visited
     */
    public void visitLocation(String locationId) {
        if (visitedLocations.add(locationId)) {
            markDirty();
        }
    }

    /**
     * Check if location has been visited
     */
    public boolean hasVisited(String locationId) {
        return visitedLocations.contains(locationId);
    }

    /**
     * Update NPC relationship status
     */
    public void setNpcRelationship(String npcId, String status) {
        if (!status.equals(npcRelationships.get(npcId))) {
            npcRelationships.put(npcId, status);
            markDirty();
        }
    }

    /**
     * Get NPC relationship status
     */
    public String getNpcRelationship(String npcId) {
        return npcRelationships.getOrDefault(npcId, "NEUTRAL");
    }

    /**
     * Update NPC affection level
     */
    public void setAffectionLevel(String npcId, int level) {
        if (affectionLevels.put(npcId, level) != level) {
            markDirty();
        }
    }

    /**
     * Get NPC affection level
     */
    public int getAffectionLevel(String npcId) {
        return affectionLevels.getOrDefault(npcId, 0);
    }

    /**
     * Add to NPC affection (can be negative for loss)
     */
    public void addAffection(String npcId, int delta) {
        int current = getAffectionLevel(npcId);
        setAffectionLevel(npcId, current + delta);
    }

    // ==================== STATE CHANGE LISTENER SUPPORT ====================

    public void addChangeListener(GameStateChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(GameStateChangeListener listener) {
        changeListeners.remove(listener);
    }

    private void notifyChangeListeners() {
        for (GameStateChangeListener listener : changeListeners) {
            listener.onStateChanged(this);
        }
    }

    // ==================== BINARY SERIALIZATION ====================

    @Override
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public int getSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public void writeBinary(Writer writer) {
        // Write metadata
        writer.writeString(gameId.toString());
        writer.writeLong(createdTime);
        writer.writeLong(lastSavedTime);
        writer.writeLong(totalPlayTimeMs);
        writer.writeInt(turnCounter);
        writer.writeString(gameVersion);

        // Write deterministic state
        writer.writeObject(playerState);
        writer.writeObject(worldState);
        writer.writeMap(questFlags, Writer::writeString, Writer::writeBoolean);
        writer.writeMap(questProgress, Writer::writeString, Writer::writeInt);
        writer.writeCollection(visitedLocations, Writer::writeString);
        writer.writeMap(npcRelationships, Writer::writeString, Writer::writeString);
        writer.writeMap(affectionLevels, Writer::writeString, Writer::writeInt);

        // Write dynamic state
        writer.writeObject(inventoryState);
        writer.writeObject(buffState);
        writer.writeMap(temporaryBuffs, Writer::writeString, Writer::writeInt);

        // Write hybrid state
        writer.writeObject(playerAttributes);
        writer.writeMap(npcStates, Writer::writeString, (w, v) -> w.writeObject(v));
    }

    @Override
    public void readBinary(Reader reader) {
        // Read metadata
        this.gameId = UUID.fromString(reader.readString());
        this.createdTime = reader.readLong();
        this.lastSavedTime = reader.readLong();
        this.totalPlayTimeMs = reader.readLong();
        this.turnCounter = reader.readInt();
        this.gameVersion = reader.readString();

        // Read deterministic state
        this.playerState = (PlayerState) reader.readObject();
        this.worldState = (WorldState) reader.readObject();
        this.questFlags = reader.readMap(Reader::readString, Reader::readBoolean);
        this.questProgress = reader.readMap(Reader::readString, Reader::readInt);
        this.visitedLocations = new HashSet<>(reader.readCollection(Reader::readString));
        this.npcRelationships = reader.readMap(Reader::readString, Reader::readString);
        this.affectionLevels = reader.readMap(Reader::readString, Reader::readInt);

        // Read dynamic state
        this.inventoryState = (InventoryState) reader.readObject();
        this.buffState = (BuffState) reader.readObject();
        this.temporaryBuffs = reader.readMap(Reader::readString, Reader::readInt);

        // Read hybrid state
        this.playerAttributes = (CharacterAttributeState) reader.readObject();
        this.npcStates = reader.readMap(Reader::readString, (r) -> (NpcState) r.readObject());

        markClean();
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Create a deep copy of current state (for snapshots)
     */
    public GameState deepCopy() {
        GameState copy = new GameState();
        copy.gameId = this.gameId;
        copy.createdTime = this.createdTime;
        copy.lastSavedTime = this.lastSavedTime;
        copy.totalPlayTimeMs = this.totalPlayTimeMs;
        copy.turnCounter = this.turnCounter;
        copy.gameVersion = this.gameVersion;
        copy.playerState = this.playerState.deepCopy();
        copy.worldState = this.worldState.deepCopy();
        copy.questFlags = new HashMap<>(this.questFlags);
        copy.questProgress = new HashMap<>(this.questProgress);
        copy.visitedLocations = new HashSet<>(this.visitedLocations);
        copy.npcRelationships = new HashMap<>(this.npcRelationships);
        copy.affectionLevels = new HashMap<>(this.affectionLevels);
        copy.inventoryState = this.inventoryState.deepCopy();
        copy.buffState = this.buffState.deepCopy();
        copy.temporaryBuffs = new HashMap<>(this.temporaryBuffs);
        copy.playerAttributes = this.playerAttributes.deepCopy();
        copy.npcStates = new HashMap<>();
        for (Map.Entry<String, NpcState> entry : this.npcStates.entrySet()) {
            copy.npcStates.put(entry.getKey(), entry.getValue().deepCopy());
        }
        copy.isDirty = false;
        return copy;
    }

    @Override
    public String toString() {
        return String.format("GameState{gameId=%s, playTime=%dms, turnCounter=%d, players=%s}",
                gameId, totalPlayTimeMs, turnCounter, playerState);
    }
}
