package com.lilithsthrone.logic.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lilithsthrone.persistence.binary.BinarySerializable;
import com.lilithsthrone.persistence.binary.BinaryStream.Reader;
import com.lilithsthrone.persistence.binary.BinaryStream.Writer;

/**
 * Represents the player character's state (location, health, mana, etc.)
 * This is deterministic - same story choices lead to same state.
 */
public class PlayerState implements BinarySerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCHEMA_VERSION = 1;
    private static final String TYPE_ID = "player_state";

    private String characterName;
    private String characterId;
    private int locationX;
    private int locationY;
    private String currentLocation;     // Location ID (e.g., "dominion_alley")
    private int currentHealth;
    private int maxHealth;
    private int currentMana;
    private int maxMana;
    private long experiencePoints;
    private int level;

    public PlayerState() {
        this.characterName = "Player";
        this.characterId = "player_main";
        this.locationX = 0;
        this.locationY = 0;
        this.currentLocation = "dominion_street";
        this.currentHealth = 100;
        this.maxHealth = 100;
        this.currentMana = 50;
        this.maxMana = 50;
        this.experiencePoints = 0;
        this.level = 1;
    }

    // Getters and Setters
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String name) { this.characterName = name; }

    public String getCharacterId() { return characterId; }
    public void setCharacterId(String id) { this.characterId = id; }

    public int getLocationX() { return locationX; }
    public void setLocationX(int x) { this.locationX = x; }

    public int getLocationY() { return locationY; }
    public void setLocationY(int y) { this.locationY = y; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String location) { this.currentLocation = location; }

    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int health) { this.currentHealth = Math.max(0, Math.min(health, maxHealth)); }
    public void addHealth(int delta) { setCurrentHealth(currentHealth + delta); }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int max) { this.maxHealth = max; }

    public int getCurrentMana() { return currentMana; }
    public void setCurrentMana(int mana) { this.currentMana = Math.max(0, Math.min(mana, maxMana)); }
    public void addMana(int delta) { setCurrentMana(currentMana + delta); }

    public int getMaxMana() { return maxMana; }
    public void setMaxMana(int max) { this.maxMana = max; }

    public long getExperiencePoints() { return experiencePoints; }
    public void setExperiencePoints(long xp) { this.experiencePoints = xp; }
    public void addExperience(long xp) { this.experiencePoints += xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public PlayerState deepCopy() {
        PlayerState copy = new PlayerState();
        copy.characterName = this.characterName;
        copy.characterId = this.characterId;
        copy.locationX = this.locationX;
        copy.locationY = this.locationY;
        copy.currentLocation = this.currentLocation;
        copy.currentHealth = this.currentHealth;
        copy.maxHealth = this.maxHealth;
        copy.currentMana = this.currentMana;
        copy.maxMana = this.maxMana;
        copy.experiencePoints = this.experiencePoints;
        copy.level = this.level;
        return copy;
    }

    @Override
    public String getTypeId() { return TYPE_ID; }

    @Override
    public int getSchemaVersion() { return SCHEMA_VERSION; }

    @Override
    public void writeBinary(Writer writer) {
        writer.writeString(characterName);
        writer.writeString(characterId);
        writer.writeInt(locationX);
        writer.writeInt(locationY);
        writer.writeString(currentLocation);
        writer.writeInt(currentHealth);
        writer.writeInt(maxHealth);
        writer.writeInt(currentMana);
        writer.writeInt(maxMana);
        writer.writeLong(experiencePoints);
        writer.writeInt(level);
    }

    @Override
    public void readBinary(Reader reader) {
        this.characterName = reader.readString();
        this.characterId = reader.readString();
        this.locationX = reader.readInt();
        this.locationY = reader.readInt();
        this.currentLocation = reader.readString();
        this.currentHealth = reader.readInt();
        this.maxHealth = reader.readInt();
        this.currentMana = reader.readInt();
        this.maxMana = reader.readInt();
        this.experiencePoints = reader.readLong();
        this.level = reader.readInt();
    }

    @Override
    public String toString() {
        return String.format("PlayerState{%s, loc=%s, hp=%d/%d, xp=%d, lvl=%d}",
                characterName, currentLocation, currentHealth, maxHealth, experiencePoints, level);
    }
}

/**
 * Represents the world's state (locations, NPCs, events)
 * This is deterministic - same story path leads to same world state.
 */
class WorldState implements BinarySerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCHEMA_VERSION = 1;
    private static final String TYPE_ID = "world_state";

    private Map<String, Integer> locationFlags;     // Location-specific flags
    private List<String> completedEvents;           // Events that have occurred
    private Map<String, String> worldEventState;    // Persistent world event data

    public WorldState() {
        this.locationFlags = new HashMap<>();
        this.completedEvents = new ArrayList<>();
        this.worldEventState = new HashMap<>();
    }

    public Map<String, Integer> getLocationFlags() { return locationFlags; }
    public List<String> getCompletedEvents() { return completedEvents; }
    public Map<String, String> getWorldEventState() { return worldEventState; }

    public WorldState deepCopy() {
        WorldState copy = new WorldState();
        copy.locationFlags = new HashMap<>(this.locationFlags);
        copy.completedEvents = new ArrayList<>(this.completedEvents);
        copy.worldEventState = new HashMap<>(this.worldEventState);
        return copy;
    }

    @Override
    public String getTypeId() { return TYPE_ID; }

    @Override
    public int getSchemaVersion() { return SCHEMA_VERSION; }

    @Override
    public void writeBinary(Writer writer) {
        writer.writeMap(locationFlags, Writer::writeString, Writer::writeInt);
        writer.writeCollection(completedEvents, Writer::writeString);
        writer.writeMap(worldEventState, Writer::writeString, Writer::writeString);
    }

    @Override
    public void readBinary(Reader reader) {
        this.locationFlags = reader.readMap(Reader::readString, Reader::readInt);
        this.completedEvents = new ArrayList<>(reader.readCollection(Reader::readString));
        this.worldEventState = reader.readMap(Reader::readString, Reader::readString);
    }
}

/**
 * Represents the player's inventory (items, weapons, clothing, equipment)
 * This is dynamic - inventory changes frequently.
 */
class InventoryState implements BinarySerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCHEMA_VERSION = 1;
    private static final String TYPE_ID = "inventory_state";

    private Map<String, Integer> items;             // itemId -> quantity
    private Map<String, Integer> weapons;           // weaponId -> quantity
    private Map<String, Integer> clothing;          // clothingId -> quantity
    private Map<String, String> equippedItems;      // bodySlot -> itemId (currently equipped)
    private int maxInventorySlots;
    private int usedInventorySlots;

    public InventoryState() {
        this.items = new HashMap<>();
        this.weapons = new HashMap<>();
        this.clothing = new HashMap<>();
        this.equippedItems = new HashMap<>();
        this.maxInventorySlots = 100;
        this.usedInventorySlots = 0;
    }

    public Map<String, Integer> getItems() { return items; }
    public Map<String, Integer> getWeapons() { return weapons; }
    public Map<String, Integer> getClothing() { return clothing; }
    public Map<String, String> getEquippedItems() { return equippedItems; }

    public void addItem(String itemId, int quantity) {
        items.put(itemId, items.getOrDefault(itemId, 0) + quantity);
        usedInventorySlots += quantity;
    }

    public void removeItem(String itemId, int quantity) {
        int current = items.getOrDefault(itemId, 0);
        if (current > quantity) {
            items.put(itemId, current - quantity);
        } else {
            items.remove(itemId);
        }
        usedInventorySlots -= quantity;
    }

    public InventoryState deepCopy() {
        InventoryState copy = new InventoryState();
        copy.items = new HashMap<>(this.items);
        copy.weapons = new HashMap<>(this.weapons);
        copy.clothing = new HashMap<>(this.clothing);
        copy.equippedItems = new HashMap<>(this.equippedItems);
        copy.maxInventorySlots = this.maxInventorySlots;
        copy.usedInventorySlots = this.usedInventorySlots;
        return copy;
    }

    @Override
    public String getTypeId() { return TYPE_ID; }

    @Override
    public int getSchemaVersion() { return SCHEMA_VERSION; }

    @Override
    public void writeBinary(Writer writer) {
        writer.writeMap(items, Writer::writeString, Writer::writeInt);
        writer.writeMap(weapons, Writer::writeString, Writer::writeInt);
        writer.writeMap(clothing, Writer::writeString, Writer::writeInt);
        writer.writeMap(equippedItems, Writer::writeString, Writer::writeString);
        writer.writeInt(maxInventorySlots);
        writer.writeInt(usedInventorySlots);
    }

    @Override
    public void readBinary(Reader reader) {
        this.items = reader.readMap(Reader::readString, Reader::readInt);
        this.weapons = reader.readMap(Reader::readString, Reader::readInt);
        this.clothing = reader.readMap(Reader::readString, Reader::readInt);
        this.equippedItems = reader.readMap(Reader::readString, Reader::readString);
        this.maxInventorySlots = reader.readInt();
        this.usedInventorySlots = reader.readInt();
    }
}

/**
 * Represents the player's active buffs and status effects
 * This is dynamic - buffs are temporary and change frequently.
 */
class BuffState implements BinarySerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCHEMA_VERSION = 1;
    private static final String TYPE_ID = "buff_state";

    private Map<String, Integer> activeEffects;     // effectId -> turnsRemaining
    private List<String> activePerkIds;             // perks that are currently active
    private Map<String, Integer> attributeModifiers; // attributeId -> totalModifier

    public BuffState() {
        this.activeEffects = new HashMap<>();
        this.activePerkIds = new ArrayList<>();
        this.attributeModifiers = new HashMap<>();
    }

    public Map<String, Integer> getActiveEffects() { return activeEffects; }
    public List<String> getActivePerkIds() { return activePerkIds; }
    public Map<String, Integer> getAttributeModifiers() { return attributeModifiers; }

    public void applyEffect(String effectId, int duration) {
        activeEffects.put(effectId, duration);
    }

    public void removeEffect(String effectId) {
        activeEffects.remove(effectId);
    }

    public void addPerk(String perkId) {
        if (!activePerkIds.contains(perkId)) {
            activePerkIds.add(perkId);
        }
    }

    public void removePerk(String perkId) {
        activePerkIds.remove(perkId);
    }

    public BuffState deepCopy() {
        BuffState copy = new BuffState();
        copy.activeEffects = new HashMap<>(this.activeEffects);
        copy.activePerkIds = new ArrayList<>(this.activePerkIds);
        copy.attributeModifiers = new HashMap<>(this.attributeModifiers);
        return copy;
    }

    @Override
    public String getTypeId() { return TYPE_ID; }

    @Override
    public int getSchemaVersion() { return SCHEMA_VERSION; }

    @Override
    public void writeBinary(Writer writer) {
        writer.writeMap(activeEffects, Writer::writeString, Writer::writeInt);
        writer.writeCollection(activePerkIds, Writer::writeString);
        writer.writeMap(attributeModifiers, Writer::writeString, Writer::writeInt);
    }

    @Override
    public void readBinary(Reader reader) {
        this.activeEffects = reader.readMap(Reader::readString, Reader::readInt);
        this.activePerkIds = new ArrayList<>(reader.readCollection(Reader::readString));
        this.attributeModifiers = reader.readMap(Reader::readString, Reader::readInt);
    }
}

/**
 * Represents the player character's attributes (strength, intelligence, etc.)
 * This is hybrid - deterministic in snapshots but mutable by mechanics.
 */
class CharacterAttributeState implements BinarySerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCHEMA_VERSION = 1;
    private static final String TYPE_ID = "character_attributes";

    private Map<String, Integer> attributes;        // attributeId -> currentValue

    public CharacterAttributeState() {
        this.attributes = new HashMap<>();
        // Initialize default attributes
        initializeDefaults();
    }

    private void initializeDefaults() {
        attributes.put("strength", 50);
        attributes.put("intelligence", 50);
        attributes.put("constitution", 50);
        attributes.put("dexterity", 50);
        attributes.put("charisma", 50);
        attributes.put("spirituality", 50);
    }

    public Map<String, Integer> getAttributes() { return attributes; }

    public int getAttribute(String attributeId) {
        return attributes.getOrDefault(attributeId, 0);
    }

    public void setAttribute(String attributeId, int value) {
        attributes.put(attributeId, value);
    }

    public void modifyAttribute(String attributeId, int delta) {
        int current = getAttribute(attributeId);
        setAttribute(attributeId, current + delta);
    }

    public CharacterAttributeState deepCopy() {
        CharacterAttributeState copy = new CharacterAttributeState();
        copy.attributes = new HashMap<>(this.attributes);
        return copy;
    }

    @Override
    public String getTypeId() { return TYPE_ID; }

    @Override
    public int getSchemaVersion() { return SCHEMA_VERSION; }

    @Override
    public void writeBinary(Writer writer) {
        writer.writeMap(attributes, Writer::writeString, Writer::writeInt);
    }

    @Override
    public void readBinary(Reader reader) {
        this.attributes = reader.readMap(Reader::readString, Reader::readInt);
    }
}

/**
 * Represents an NPC's state (health, location, status, inventory, etc.)
 * This is hybrid - some parts deterministic (status), some dynamic (health).
 */
class NpcState implements BinarySerializable, Serializable {
    private static final long serialVersionUID = 1L;
    private static final int SCHEMA_VERSION = 1;
    private static final String TYPE_ID = "npc_state";

    private String npcId;
    private int currentHealth;
    private int maxHealth;
    private String status;                  // ALIVE, DEAD, ENSLAVED, PREGNANT, etc.
    private String currentLocation;
    private Map<String, Integer> inventory; // itemId -> quantity
    private boolean hasBeenMet;

    public NpcState(String npcId) {
        this.npcId = npcId;
        this.currentHealth = 100;
        this.maxHealth = 100;
        this.status = "ALIVE";
        this.currentLocation = "unknown";
        this.inventory = new HashMap<>();
        this.hasBeenMet = false;
    }

    public String getNpcId() { return npcId; }
    public int getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(int health) { this.currentHealth = health; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int max) { this.maxHealth = max; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String location) { this.currentLocation = location; }
    public Map<String, Integer> getInventory() { return inventory; }
    public boolean hasBeenMet() { return hasBeenMet; }
    public void setHasBeenMet(boolean met) { this.hasBeenMet = met; }

    public NpcState deepCopy() {
        NpcState copy = new NpcState(this.npcId);
        copy.currentHealth = this.currentHealth;
        copy.maxHealth = this.maxHealth;
        copy.status = this.status;
        copy.currentLocation = this.currentLocation;
        copy.inventory = new HashMap<>(this.inventory);
        copy.hasBeenMet = this.hasBeenMet;
        return copy;
    }

    @Override
    public String getTypeId() { return TYPE_ID; }

    @Override
    public int getSchemaVersion() { return SCHEMA_VERSION; }

    @Override
    public void writeBinary(Writer writer) {
        writer.writeString(npcId);
        writer.writeInt(currentHealth);
        writer.writeInt(maxHealth);
        writer.writeString(status);
        writer.writeString(currentLocation);
        writer.writeMap(inventory, Writer::writeString, Writer::writeInt);
        writer.writeBoolean(hasBeenMet);
    }

    @Override
    public void readBinary(Reader reader) {
        this.npcId = reader.readString();
        this.currentHealth = reader.readInt();
        this.maxHealth = reader.readInt();
        this.status = reader.readString();
        this.currentLocation = reader.readString();
        this.inventory = reader.readMap(Reader::readString, Reader::readInt);
        this.hasBeenMet = reader.readBoolean();
    }
}

/**
 * Represents active combat state (transient, not persisted)
 * Only exists during combat, discarded when combat ends
 */
class CombatState implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> combatantIds;
    private String playerCombatantId;
    private int currentTurn;
    private int roundCounter;
    private Map<String, Integer> healthMap;     // combatantId -> currentHealth

    public CombatState() {
        this.combatantIds = new ArrayList<>();
        this.playerCombatantId = null;
        this.currentTurn = 0;
        this.roundCounter = 0;
        this.healthMap = new HashMap<>();
    }

    public List<String> getCombatantIds() { return combatantIds; }
    public String getPlayerCombatantId() { return playerCombatantId; }
    public void setPlayerCombatantId(String id) { this.playerCombatantId = id; }
    public int getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(int turn) { this.currentTurn = turn; }
    public int getRoundCounter() { return roundCounter; }
    public void setRoundCounter(int round) { this.roundCounter = round; }
    public Map<String, Integer> getHealthMap() { return healthMap; }

    public void addCombatant(String combatantId, int health) {
        combatantIds.add(combatantId);
        healthMap.put(combatantId, health);
    }

    public int getCombatantHealth(String combatantId) {
        return healthMap.getOrDefault(combatantId, 0);
    }

    public void setCombatantHealth(String combatantId, int health) {
        healthMap.put(combatantId, health);
    }
}

/**
 * Listener interface for state changes
 */
interface GameStateChangeListener {
    void onStateChanged(GameState state);
}
