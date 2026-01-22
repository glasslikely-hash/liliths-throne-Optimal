╔═══════════════════════════════════════════════════════════════════════════════╗
║                    STEP 2: LOGIC LAYER REFACTORING                             ║
║              Snapshot + Delta Persistence Architecture                          ║
╚═══════════════════════════════════════════════════════════════════════════════╝

OVERVIEW:
═════════

Goal: Extract all core mechanics into a decoupled logic layer that uses snapshot+delta
       persistence for game state. This enables:
       - Replay/undo capabilities (snapshots)
       - Efficient saves (delta compression)
       - Cross-platform deterministic state
       - Mobile/cloud save compatibility
       - Rollback for debugging

ARCHITECTURE LAYERS:
═══════════════════

┌─────────────────────────────────────────────────────────────────────┐
│ UI LAYER (Step 3, LibGDX)                                           │
│ ├─ Query API: getPlayerState(), getWorldState(), getInventory()    │
│ ├─ Action API: equip(), move(), interact(), useItem(), etc.        │
│ └─ Listen to state change events                                    │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ LOGIC LAYER (Step 2 - THIS STEP)                                   │
│                                                                      │
│ Core Mechanics Engines:                                             │
│ ├─ CombatEngine        - Attack, defense, spell casting, status fx  │
│ ├─ InventoryEngine     - Equip, use, sell, upgrade items           │
│ ├─ CharacterEngine     - Attributes, experience, level up          │
│ ├─ MovementEngine      - Pathfinding, world traversal              │
│ ├─ QuestEngine         - Progress, completion, rewards             │
│ ├─ EventEngine         - Dialogue trees, encounters, triggers       │
│ ├─ BuffEngine          - Status effects, perks, temporary buffs     │
│ └─ WorldEngine         - Location state, NPCs, events              │
│                                                                      │
│ State Management:                                                    │
│ ├─ SnapshotEngine      - Full state serialization (periodic)        │
│ ├─ DeltaEngine         - Incremental changes (async flush)         │
│ ├─ GameState           - Unified state container                    │
│ └─ StateChangeEvents   - Listen to mutations                        │
│                                                                      │
│ Persistence:                                                         │
│ ├─ BinaryReader/Writer - Use Step 1 binary engine                  │
│ ├─ SnapshotStore       - Manage .snapshot files                    │
│ ├─ DeltaStore          - Manage .delta files                       │
│ └─ Checkpointer        - Create save points                         │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ PERSISTENCE LAYER (Binary Engine from Step 1)                       │
│ ├─ BinaryStream        - Varint encoding                           │
│ ├─ BinaryCatalog       - Index + payload                           │
│ └─ SchemaRegistry      - Type versioning & migration               │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│ DATA LAYER (From Step 1.2)                                         │
│ ├─ DataStore           - Read-only static data (items, NPCs, etc.) │
│ └─ .bin files          - Serialized game content                   │
└─────────────────────────────────────────────────────────────────────┘


STATE STRUCTURE:
════════════════

DETERMINISTIC STATE (Snapshot):
  • Story choices made by player
  • Quest progress and completion flags
  • Visited world locations
  • Relationship/affection levels with NPCs
  • Major story events that occurred
  • Locked/unlocked areas
  • Permanent character changes (race, appearance, attributes that persist)
  
  → Serialized periodically (every 10-30 minutes) to disk
  → Creates "checkpoint" saves that can be loaded later
  → Deterministic: same sequence of choices = same state

DYNAMIC STATE (Delta):
  • Player inventory (items, weapons, clothing)
  • Current equipment
  • Temporary buffs/debuffs (status effects)
  • Current health, mana, experience points
  • NPC temporary states (combat status, dialogue state)
  • In-progress transactions (shop visit, trading)
  
  → Tracked as changes to previous snapshot
  → Flushed asynchronously at intervals (every 1-5 minutes)
  → Compressed into "delta" format (only changed fields)
  → Can be discarded without losing major progress

HYBRID STATE (Deterministic but Mutable):
  • Player character attributes (strength, intelligence, etc.)
  • NPC status (alive/dead/enslaved/befriended)
  • Building/structure ownership and upgrades
  
  → Stored in snapshots (deterministic)
  → Updated via mechanics engines (mutable)
  → Changes contribute to next snapshot


DATA MODEL HIERARCHY:
═════════════════════

GameState
├─ PlayerState
│  ├─ Id: UUID
│  ├─ Name, appearance, attributes
│  ├─ Health, mana, experience
│  ├─ Location (world position)
│  ├─ Flags: Map<String, Boolean> (quest progress, story choices)
│  └─ Relationships: Map<NpcId, Relationship>
│
├─ WorldState
│  ├─ Locations: Map<LocationId, LocationState>
│  │  ├─ Visited: Boolean
│  │  ├─ NPCs: List<NpcState>
│  │  └─ Items on ground: List<Item>
│  ├─ NPCStates: Map<NpcId, NpcState>
│  │  ├─ Status (alive, dead, enslaved, friendly)
│  │  ├─ Health, position
│  │  ├─ Inventory
│  │  └─ Relationships
│  └─ Events: List<WorldEvent>
│
├─ InventoryState
│  ├─ Items: Map<ItemId, Quantity>
│  ├─ Equipment: Map<BodySlot, EquippedItem>
│  ├─ Weapons: List<Weapon>
│  └─ Clothing: List<Clothing>
│
├─ BuffState
│  ├─ ActiveEffects: Map<EffectId, Duration>
│  ├─ Perks: Set<PerkId>
│  └─ CumulativeBuffs: Map<AttributeId, Modifier>
│
├─ CombatState (transient, not persisted)
│  ├─ InCombat: Boolean
│  ├─ Combatants: List<GameCharacter>
│  ├─ Turn: Int
│  └─ HealthMap: Map<CharacterId, Health>
│
└─ Metadata
   ├─ SaveTime: Timestamp
   ├─ PlayTime: Duration
   ├─ Checkpoints: List<CheckpointReference>
   └─ GameVersion: String


ENGINE DESCRIPTIONS:
═══════════════════

1. CombatEngine
   Purpose: Resolve combat interactions, damage, status effects
   State: InCombat (temporary), turn counter, damage tracking
   Actions:
     - initiateCombat(player, enemies)
     - selectAction(attacker, action)
     - resolveTurn()
     - endCombat(victor)
   Persistence: Combat state is transient (lost on reload), but results are persisted
               (health changes, items used, status effects applied)

2. InventoryEngine
   Purpose: Manage player and NPC inventory
   State: Owned items/weapons/clothing, equipped items
   Actions:
     - addItem(itemId, quantity)
     - removeItem(itemId, quantity)
     - equip(itemSlot, item)
     - unequip(itemSlot)
     - useItem(itemId, target)
   Persistence: Persisted in delta (inventory changes frequent)

3. CharacterEngine
   Purpose: Manage character attributes and progression
   State: Health, mana, XP, attributes (strength, intelligence, etc.)
   Actions:
     - gainExperience(amount)
     - levelUp()
     - takeDamage(amount)
     - heal(amount)
     - modifyAttribute(attribute, modifier)
   Persistence: Persisted in snapshots (changes less frequent)

4. MovementEngine
   Purpose: Handle world navigation
   State: Player location, explored areas, unlocked paths
   Actions:
     - move(direction) → navigate world grid
     - teleport(location)
     - unlockArea(areaId)
   Persistence: Persisted in snapshots (location is deterministic)

5. QuestEngine
   Purpose: Track quest progress
   State: Quest flags, objective completion, rewards
   Actions:
     - startQuest(questId)
     - updateProgress(questId, objective)
     - completeQuest(questId)
   Persistence: Persisted in snapshots (quest state is deterministic)

6. EventEngine
   Purpose: Trigger world events and dialogue
   State: Event queue, completed events, flags
   Actions:
     - triggerEvent(eventId)
     - progressDialogue(dialogueNodeId, choice)
     - checkTrigger(eventCondition)
   Persistence: Event flags persisted in snapshots

7. BuffEngine
   Purpose: Manage temporary buffs and status effects
   State: Active effects, duration counters, perk selections
   Actions:
     - applyEffect(effectId, duration)
     - removeEffect(effectId)
     - addPerk(perkId)
     - removePerk(perkId)
   Persistence: Persisted in delta (temporary effects change often)

8. WorldEngine
   Purpose: Manage world state beyond player
   State: NPC status, location state, world events
   Actions:
     - updateNpcState(npcId, changes)
     - triggerWorldEvent(eventId)
   Persistence: Persisted in snapshots


PERSISTENCE STRATEGY:
═════════════════════

SNAPSHOTS (Periodic Full State Save):
  Frequency: Every 10-30 minutes of gameplay (configurable)
  Trigger: Time-based OR checkpoint (quest complete, dungeon clear)
  File Format: .snapshot binary file using Step 1 engine
  Contains: Full PlayerState + WorldState + InventoryState + BuffState
  Size: ~100-500 KB depending on game progress
  Load Time: 50-100ms
  
  Format:
    [MAGIC_HEADER: "SNAP"]
    [VERSION: 1]
    [TIMESTAMP]
    [GAME_STATE_SERIALIZED as binary]
    [CRC32_CHECKSUM]

DELTAS (Incremental Changes):
  Frequency: Every 1-5 minutes (async, non-blocking)
  Trigger: Change accumulation or time interval
  File Format: .delta binary files using Step 1 engine
  Contains: Only fields that changed since last snapshot
  Size: ~5-50 KB
  Load Time: <10ms
  
  Format:
    [MAGIC_HEADER: "DELT"]
    [VERSION: 1]
    [BASE_SNAPSHOT_ID]
    [FIELD_CHANGES: [fieldId, typeId, newValue] × N]
    [CRC32_CHECKSUM]

MERGE PROCESS (Load Game):
  1. Load latest .snapshot file
  2. List all .delta files newer than snapshot
  3. Apply each delta in chronological order
  4. Validate CRC32 checksums
  5. GameState ready for play

CHECKPOINT SYSTEM:
  Purpose: Player-initiated save points (named saves)
  Action: "Save game to slot X"
  Creates: New snapshot + metadata file
  File: saves/slot_X.snapshot + saves/slot_X.metadata.json
  Metadata: Save time, play time, location, thumbnail

AUTO-BACKUP SYSTEM:
  Purpose: Recover from crashes
  Frequency: Every 2 minutes
  Location: Temporary directory
  Retention: Keep last 5 auto-backups
  On crash: Offer to restore from auto-backup


ENGINE ARCHITECTURE:
════════════════════

┌─────────────────────────────────────────────────────┐
│ Abstract BaseEngine                                  │
├─────────────────────────────────────────────────────┤
│ Fields:                                              │
│  - gameState: GameState (reference)                 │
│  - isDirty: boolean (state changed)                 │
│  - lastFlushTime: long                              │
│                                                      │
│ Methods:                                             │
│  - abstract execute(action)                          │
│  - markDirty()                                       │
│  - isDirty(): boolean                                │
│  - flush(deltaEngine): void                         │
│  - handleRollback(snapshot): void                    │
└─────────────────────────────────────────────────────┘

Each engine extends BaseEngine and implements specific mechanics.
When an action is triggered:
  1. Engine validates action (is it legal?)
  2. Engine modifies GameState
  3. Engine calls markDirty()
  4. DeltaEngine detects dirty flag and records change
  5. SnapshotEngine periodically creates checkpoints

DETERMINISM:
═════════════
For save compatibility:
  1. All random numbers use seeded RNG (seed = snapshot hash + action count)
  2. All timestamps use game time (not system time)
  3. All lookups use IDs not object references
  4. All collections are ordered deterministically
  5. No floating point operations (use integers + fixed decimals)


LOAD/SAVE EXAMPLES:
═══════════════════

SAVE GAME:
  // Every game loop iteration or after significant action
  if (deltaEngine.shouldFlush()) {
      deltaEngine.flush(); // Writes .delta file
  }
  if (snapshotEngine.shouldSnapshot()) {
      snapshotEngine.snapshot(); // Writes .snapshot file
  }

LOAD GAME:
  GameState state = GameStateLoader.load("saves/slot_1.snapshot");
  // Automatically applies all deltas newer than snapshot

ROLLBACK:
  GameState oldState = snapshotEngine.load("saves/checkpoint_2.snapshot");
  // Revert to this checkpoint, losing all progress since then

EXPORT (for cloud/mobile):
  byte[] exportedState = gameState.toBinary(); // Uses BinaryStream
  // Can be synced to cloud or transferred to mobile device


NEXT STEPS:
═══════════
1. Create GameState data model (holds all game state)
2. Create SnapshotEngine (serialize full state periodically)
3. Create DeltaEngine (track changes incrementally)
4. Extract CombatEngine (from existing Combat.java)
5. Extract InventoryEngine (from existing CharacterInventory.java)
6. Extract other mechanics engines
7. Create unified API for UI layer
8. Integrate with existing game code

═══════════════════════════════════════════════════════════════════════════════
