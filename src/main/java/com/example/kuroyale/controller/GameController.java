package com.example.kuroyale.controller;

import com.example.kuroyale.model.*;
import com.example.kuroyale.network.NetworkManager;
import com.example.kuroyale.protocol.Message;
import com.example.kuroyale.model.challenge.Challenge;
import com.example.kuroyale.model.challenge.ChallengeManager;
import com.example.kuroyale.model.persistence.*;
import com.example.kuroyale.model.combo.*;
import com.example.kuroyale.model.quest.QuestManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameController {

    private Challenge activeChallenge;

    private static GameController instance;

    private Deck deck; // Player's deck
    private Deck savedPlayerDeck; // For temporary deck swapping during challenges
    private Arena arena;
    private ElixirManager playerElixirManager;
    private ElixirManager computerElixirManager;
    private ComputerOpponent computerOpponent;
    private List<Unit> activeUnits;
    private List<Building> activeBuildings;
    private List<Effect> activeEffects;
    private boolean isGameRunning;
    private UnitController unitController;

    private boolean isMultiplayer = false;
    private NetworkManager networkManager;

    // Local PvP Mode
    private boolean isLocalPvP = false;
    private Deck localPvPPlayer1Deck = new Deck();
    private Deck localPvPPlayer2Deck = new Deck();
    private ElixirManager player2ElixirManager;
    private int currentPlayerTurn = 1; // 1 or 2
    private boolean isPlayer2DeckSaved = false;

    // Local PvP Arena Design
    private List<Tower> localPvPPlayer1Towers = new ArrayList<>();
    private List<Tower> localPvPPlayer2Towers = new ArrayList<>();

    private double gameTime;

    private boolean isDeckSaved = false;
    private boolean isArenaSaved = false;
    private boolean isTestingMode = false;

    private boolean isPaused;
    private String gameResult; // "WIN", "LOSS", "DRAW", or null if game is ongoing

    // Combo System
    private ComboManager comboManager;
    private List<ComboVisualEffect> activeComboVisuals;

    // Tower destruction tracking for achievements
    private java.util.Set<Integer> destroyedTowerIds = new java.util.HashSet<>();

    // Persistence Data
    private PlayerProfile playerProfile;
    private QuestData questData;
    private Map<String, CardProgression> cardProgressions;
    private String saveFileName = "savegame.dat";

    public void setSaveFileName(String fileName) {
        this.saveFileName = fileName;
    }

    public void saveGame() {
        GameData data = new GameData();
        data.setPlayerProfile(playerProfile);

        // Export daily quests to questData before saving
        questData.setQuests(QuestManager.getInstance().exportDailyQuests());
        questData.setLastQuestResetTimestamp(QuestManager.getInstance().getLastQuestResetTime());
        data.setQuestData(questData);

        data.setCardProgressions(new ArrayList<>(cardProgressions.values()));
        data.setChallengeData(ChallengeManager.getInstance().exportData());
        data.setAchievementData(QuestManager.getInstance().exportAchievementData());

        PersistenceManager.getInstance().save(data, saveFileName);
    }

    public void loadGame() {
        GameData data = PersistenceManager.getInstance().load(saveFileName);
        if (data != null) {
            this.playerProfile = data.getPlayerProfile();
            this.questData = data.getQuestData();

            this.cardProgressions.clear();
            for (CardProgression cp : data.getCardProgressions()) {
                this.cardProgressions.put(cp.getCardName(), cp);
            }

            ChallengeManager.getInstance().importData(data.getChallengeData());
            QuestManager.getInstance().importAchievementData(data.getAchievementData());

            // Import daily quests from saved data
            if (questData != null && questData.getQuests() != null && !questData.getQuests().isEmpty()) {
                QuestManager.getInstance().importDailyQuests(
                        questData.getQuests(),
                        questData.getLastQuestResetTimestamp());
            }

            System.out.println("Game loaded.");
        } else {
            System.out.println("No save file found. Using defaults.");
        }
    }

    public PlayerProfile getPlayerProfile() {
        return playerProfile;
    }

    // For demo purposes, we might want to access the player's deck globally or pass
    // it in
    // In this design, the GameController owns the game state.

    private double timeScale = 1.0;

    private GameController() {
        this.deck = new Deck();
        this.arena = new Arena();
        this.playerElixirManager = new ElixirManager();
        this.computerElixirManager = new ElixirManager();
        this.computerOpponent = new ComputerOpponent(this);
        this.activeUnits = new ArrayList<>();
        this.activeBuildings = new ArrayList<>();
        this.activeEffects = new ArrayList<>();
        this.isGameRunning = false;
        this.unitController = new UnitController(this, this.arena);
        this.gameTime = 180.0; // 3 minutes

        // Initialize local PvP components
        this.localPvPPlayer2Deck = new Deck();
        this.player2ElixirManager = new ElixirManager();
        this.isPaused = false;
        this.gameResult = null;
        this.timeScale = 1.0;
        this.isDeckSaved = false;
        this.isArenaSaved = false;

        this.playerProfile = new PlayerProfile();
        this.questData = new QuestData();
        this.cardProgressions = new HashMap<>();

        // Initialize Combo Manager
        this.comboManager = new ComboManager();
        this.activeComboVisuals = new CopyOnWriteArrayList<>();

        // Load Network Config at startup
        com.example.kuroyale.config.NetworkConfig.getInstance();
        this.networkManager = NetworkManager.getInstance();

        // Try to load game strictly on startup? Or maybe call loadGame() explicitly.
        // For now, let's just initialize default empty structures.
        // Actually, let's try to load automatically for convenience.
        loadGame();

        initializeCards(); // Populate the deck with available cards
    }

    public void startChallenge(Challenge challenge) {
        startChallenge(challenge, false);
    }

    public void startChallenge(Challenge challenge, boolean isTestingMode) {
        this.activeChallenge = challenge;
        this.isTestingMode = isTestingMode;
        startGame();
        System.out.println("Started Challenge: " + challenge.getName() + (isTestingMode ? " (TEST MODE)" : ""));
    }

    public boolean isTestingMode() {
        return isTestingMode;
    }

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    public void setTimeScale(double scale) {
        this.timeScale = scale;
    }

    public double getTimeScale() {
        return timeScale;
    }

    private void initializeCards() {
        // Initialize deck with specific cards including Spear Goblins
        List<Card> library = CardLibrary.getAllCards();
        deck.clear();

        // Explicitly trying to find Spear Goblins to include
        // Also ensure Giant and Knight are included for combo testing if possible
        Card spearGoblins = CardLibrary.getCardByName("Spear Goblins");
        if (spearGoblins != null) {
            deck.addCard(spearGoblins);
        }

        // Fill the rest with other available cards
        for (Card card : library) {
            if (deck.getCards().size() >= 8)
                break;
            // distinct check not strictly needed as library is unique and we just cleared
            // deck,
            // but we don't want to add Spear Goblins twice if we iterate over it
            if (!deck.getCards().contains(card)) {
                deck.addCard(card);
            }
        }

        deck.initializeGameDeck();
    }

    public void setDeck(Deck newDeck) {
        this.deck = newDeck;
        this.isDeckSaved = true;
        // Re-initialize game deck state if game is running or about to start
        this.deck.initializeGameDeck();
    }

    public void confirmArenaDesign() {
        this.isArenaSaved = true;
    }

    public boolean isGameReady() {
        if (isLocalPvP) {
            return isDeckSaved && isPlayer2DeckSaved && isArenaSaved;
        }
        return isDeckSaved && isArenaSaved;
    }

    public boolean isDeckReady() {
        return isDeckSaved;
    }

    public boolean isArenaReady() {
        return isArenaSaved;
    }

    // Local PvP Methods
    public void setPlayer2Deck(Deck newDeck) {
        this.localPvPPlayer2Deck = newDeck;
        this.isPlayer2DeckSaved = true;
        this.localPvPPlayer2Deck.initializeGameDeck();
    }

    // Set BOTH PvP decks (completely separate from normal mode deck!)
    public void setLocalPvPDecks(Deck player1PvPDeck, Deck player2PvPDeck) {
        // Store in SEPARATE fields - doesn't touch this.deck at all!
        this.localPvPPlayer1Deck = player1PvPDeck;
        this.localPvPPlayer2Deck = player2PvPDeck;
        this.isPlayer2DeckSaved = true;
        this.localPvPPlayer1Deck.initializeGameDeck();
        this.localPvPPlayer2Deck.initializeGameDeck();
        System.out.println("Local PvP decks set (separate from normal mode)");
    }

    public Deck getLocalPvPPlayer1Deck() {
        return localPvPPlayer1Deck;
    }

    public Deck getPlayer2Deck() {
        return localPvPPlayer2Deck;
    }

    public ElixirManager getPlayer2ElixirManager() {
        return player2ElixirManager;
    }

    public boolean isLocalPvP() {
        return isLocalPvP;
    }

    public int getCurrentPlayerTurn() {
        return currentPlayerTurn;
    }

    public void switchTurn() {
        currentPlayerTurn = (currentPlayerTurn == 1) ? 2 : 1;
        System.out.println("Turn switched to Player " + currentPlayerTurn);
    }

    public void resetGameMode() {
        isLocalPvP = false;
        isPlayer2DeckSaved = false;
        currentPlayerTurn = 1;

        // Reset ONLY PvP decks - normal deck (this.deck) is untouched!
        localPvPPlayer1Deck = new Deck();
        localPvPPlayer2Deck = new Deck();
        player2ElixirManager = new ElixirManager();

        // Clear Local PvP tower layouts
        localPvPPlayer1Towers.clear();
        localPvPPlayer2Towers.clear();

        // Clear arena towers to prevent Local PvP arena from persisting
        arena.clearTowers();
        arena.clearBridges(); // Also clear bridges from Local PvP

        // Reset arena saved flag so normal game requires arena design
        isArenaSaved = false;

        // IMPORTANT: Clear all game state
        activeUnits.clear();
        activeBuildings.clear();
        activeEffects.clear();
        isGameRunning = false;
        gameResult = null;

        // Clear challenge state
        activeChallenge = null;
        isTestingMode = false;

        if (savedPlayerDeck != null) {
            this.deck = savedPlayerDeck;
            this.savedPlayerDeck = null;
            System.out.println("Restored player deck after reset.");
        }

        System.out.println("Game mode reset to normal.");
    }

    public void startGame() {
        isGameRunning = true;
        isPaused = false;
        gameResult = null;
        // Don't reset isTestingMode here as it might have been set by startChallenge
        // ensuring it defaults to false for normal games if not set explicitly via
        // startChallenge
        if (activeChallenge == null) {
            isTestingMode = false;
        } else {
            if (activeChallenge.isDeckProvided()) {
                this.savedPlayerDeck = this.deck;
                activeChallenge.onGameStart(this);
                System.out.println("Challenge deck provided. Player deck saved.");
            }
        }

        activeUnits.clear(); // Fix: Clear units from previous games
        activeBuildings.clear();
        activeEffects.clear();
        playerElixirManager = new ElixirManager(); // Reset player elixir
        computerElixirManager = new ElixirManager(); // Reset computer elixir
        playerElixirManager = new ElixirManager(); // Reset player elixir
        computerElixirManager = new ElixirManager(); // Reset computer elixir

        comboManager.reset(); // Reset combo manager for new game
        activeComboVisuals.clear(); // Clear combo visuals
        destroyedTowerIds.clear(); // Reset tower tracking for achievements

        if (!isMultiplayer) {
            computerOpponent = new ComputerOpponent(this); // Reset opponent logic
        } else {
            computerOpponent = null; // No AI in multiplayer
        }

        timeScale = 1.0; // Reset speed

        // Only set default if no towers exist (i.e. not customized)
        if (arena.getTowers().isEmpty()) {
            arena.setupDefaultTowers();
        } else {
            // Fix for Auto-Win bug: Reset existing towers (health) if reusing arena
            arena.reset();
        }

        deck.initializeGameDeck();
        gameTime = 180.0;
        System.out.println("Game Started!");
    }

    public void startLocalPvPGame() {
        isLocalPvP = true;
        isGameRunning = true;
        isPaused = false;
        gameResult = null;
        activeUnits.clear();
        activeBuildings.clear();
        activeEffects.clear();
        destroyedTowerIds.clear(); // Reset tower tracking for achievements

        // Initialize both players' elixir
        playerElixirManager = new ElixirManager();
        player2ElixirManager = new ElixirManager();

        // No computer opponent in local PvP
        computerOpponent = null;

        // Reset turn to Player 1
        currentPlayerTurn = 1;

        timeScale = 1.0;

        // Apply custom tower layouts if available
        if (!localPvPPlayer1Towers.isEmpty() && !localPvPPlayer2Towers.isEmpty()) {
            arena.clearTowers();

            // CRITICAL: Add bridges for Local PvP (fixed positions)
            arena.clearBridges();
            arena.addBridge("Bridge 1", 5.0);
            arena.addBridge("Bridge 2", 11.0);

            // Add Player 1's towers (bottom side, isPlayer=true)
            for (Tower t : localPvPPlayer1Towers) {
                arena.addTower(t);
            }

            // Add Player 2's towers (top side, isPlayer=false, already mirrored)
            for (Tower t : localPvPPlayer2Towers) {
                arena.addTower(t);
            }

            System.out.println("Custom tower layouts applied for Local PvP!");
        } else if (arena.getTowers().isEmpty()) {
            // Fallback to default
            arena.setupDefaultTowers();
        }

        // Initialize both decks
        deck.initializeGameDeck();
        localPvPPlayer2Deck.initializeGameDeck();

        gameTime = 180.0;
        System.out.println("Local PvP Game Started! Player 1's turn.");
    }

    public void togglePause() {
        isPaused = !isPaused;
        System.out.println("Game " + (isPaused ? "Paused" : "Resumed"));
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void update(double deltaTime) {
        if (!isGameRunning || isPaused)
            return;

        // Apply time scale
        double scaledDeltaTime = deltaTime * timeScale;

        gameTime -= scaledDeltaTime;

        // 1. Update Elixir
        if (isTestingMode) {
            playerElixirManager.setRegenerationRate(10.0); // Super fast for testing
        } else {
            // Standard (0-2m): 1 per 2.8s (~0.357)
            // Double (2-3m): 1 per 1.4s (~0.714)
            double rate = (gameTime < 120) ? (1.0 / 2.8) : (1.0 / 1.4);
            playerElixirManager.setRegenerationRate(rate);
        }
        playerElixirManager.update(scaledDeltaTime);
        if (isLocalPvP) {
            player2ElixirManager.update(scaledDeltaTime);
        } else {
            computerElixirManager.update(scaledDeltaTime);
        }

        // Update Computer Opponent (only in non-PvP modes)
        if (computerOpponent != null && !isLocalPvP) {
            computerOpponent.update(scaledDeltaTime);
        }

        // 2. Update Units (Movement and Attack)
        for (Unit unit : activeUnits) {
            unitController.updateUnitBehavior(unit, scaledDeltaTime);
        }

        // 3. Update Buildings (Attack, Spawn, Elixir Generation, Lifetime)
        for (Building building : activeBuildings) {
            updateBuildingBehavior(building, scaledDeltaTime);
        }

        // 4. Update Towers (Attack)
        for (Tower tower : arena.getTowers()) {
            updateTowerBehavior(tower, scaledDeltaTime);
        }

        // 5. Remove dead units and expired/destroyed buildings
        activeUnits.removeIf(Unit::isDead);
        activeBuildings.removeIf(Building::isDestroyed);

        // Update Effects
        for (Effect effect : activeEffects) {
            effect.update(deltaTime);
        }
        activeEffects.removeIf(Effect::isExpired);

        // Clean up expired combo visuals
        long currentTime = System.currentTimeMillis();
        activeComboVisuals.removeIf(visual -> !visual.isActive(currentTime));

        // Track crown tower destruction for achievements (before win conditions check)
        for (Tower tower : arena.getTowers()) {
            if (tower.isDestroyed() && !tower.isPlayer()) {
                int towerId = System.identityHashCode(tower);
                if (!destroyedTowerIds.contains(towerId)) {
                    destroyedTowerIds.add(towerId);
                    // Only track crown towers, not king towers
                    if (!tower.getType().equals("KING")) {
                        QuestManager.getInstance().onCrownTowerDestroyed();
                        System.out.println("[ACHIEVEMENT] Crown tower destroyed - tracking progress!");
                    }
                }
            }
        }

        // 6. Check win conditions (after all updates to catch tower destruction)
        checkWinConditions();

        // 7. Check for timeout after win condition check
        if (gameTime <= 0) {
            gameTime = 0;
            if (gameResult == null) {
                // Time ran out without a winner - determine winner by tower health
                determineTimeoutWinner();
            }
            isGameRunning = false;
            return;
        }
    }

    private void updateTowerBehavior(Tower tower, double deltaTime) {
        if (tower.isDestroyed())
            return;

        // Find nearest enemy unit in range
        Unit target = findNearestEnemyUnit(tower);
        if (target != null) {
            if (tower.canAttack(System.currentTimeMillis())) {
                tower.attack(System.currentTimeMillis());
                target.takeDamage(tower.getDamage());
            }
        }
    }

    private Unit findNearestEnemyUnit(Tower tower) {
        double minDistSq = Double.MAX_VALUE;
        Unit nearest = null;
        double rangeSq = tower.getRange() * tower.getRange();

        for (Unit unit : activeUnits) {
            if (tower.isPlayer() != unit.isPlayer()) {
                double dx = tower.getX() - unit.getX();
                double dy = tower.getY() - unit.getY();
                double distSq = dx * dx + dy * dy;
                if (distSq <= rangeSq && distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = unit;
                }
            }
        }
        return nearest;
    }

    private void updateBuildingBehavior(Building building, double deltaTime) {
        if (building.isDestroyed())
            return;

        // 1. Lifetime decay
        building.update(deltaTime);

        // 2. Spawner logic
        if (building.isSpawner() && building.shouldSpawn()) {
            String unitName = building.getSpawnedUnitType();
            Card unitCard = CardLibrary.getCardByName(unitName);

            if (unitCard != null) {
                // Determine spawn position (slightly offset so it doesn't look like it's inside
                // the building)
                // Spawn in front of building relative to player side
                double spawnX = building.getX();
                double spawnY = building.getY() + (building.isPlayer() ? -1.0 : 1.0);

                // Create the unit
                // For now, we spawn at base level (or we could store building level later)
                Unit spawnedUnit = UnitFactory.createUnit(unitCard, spawnX, spawnY, building.isPlayer());

                activeUnits.add(spawnedUnit);
                System.out
                        .println(building.getName() + " spawned " + unitName + " at (" + spawnX + ", " + spawnY + ")");
            }
        }

        // 3. Defensive Building Attack
        if (building.getDamage() > 0) {
            // Find target
            Unit target = findNearestEnemyUnitForBuilding(building);
            if (target != null) {
                if (building.canAttack(System.currentTimeMillis())) {
                    building.attack(System.currentTimeMillis());
                    target.takeDamage(building.getDamage());
                }
            }
        }
    }

    private Unit findNearestEnemyUnitForBuilding(Building building) {
        double minDistSq = Double.MAX_VALUE;
        Unit nearest = null;
        double rangeSq = building.getRange() * building.getRange();

        for (Unit unit : activeUnits) {
            if (building.isPlayer() != unit.isPlayer()) {
                if (!isValidTarget(building, unit)) {
                    continue;
                }

                double dx = building.getX() - unit.getX();
                double dy = building.getY() - unit.getY();
                double distSq = dx * dx + dy * dy;
                if (distSq <= rangeSq && distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = unit;
                }
            }
        }
        return nearest;
    }

    private void applySpellDamage(Card card, double x, double y, CardProgression progression) {
        double radius = card.getRange();
        double radiusSq = radius * radius;
        double damage = card.getDamage();

        // Apply level bonuses if progression is provided
        if (progression != null) {
            damage = progression.applyLevelBonus(damage);
            damage = Math.round(damage);
        }

        int totalDamageDealt = 0;

        // Damage Units
        for (Unit unit : activeUnits) {
            // Check if enemy? Spells usually hit everything or just enemies?
            // Let's assume spells hit enemies only for now, or everything?
            // Clash Royale spells (Fireball, Arrows) hit enemies.
            if (!unit.isPlayer()) { // Assuming player casts spell on enemies
                double dx = unit.getX() - x;
                double dy = unit.getY() - y;
                if (dx * dx + dy * dy <= radiusSq) {
                    unit.takeDamage(damage);
                    totalDamageDealt += (int) damage;
                }
            }
        }

        // Damage Towers
        for (Tower tower : arena.getTowers()) {
            if (!tower.isPlayer()) {
                double dx = tower.getX() - x;
                double dy = tower.getY() - y;
                // Tower hitbox is larger, but simple center check for now
                if (dx * dx + dy * dy <= radiusSq) {
                    // Spells deal reduced damage to towers (40%)
                    double towerDamage = damage * 0.4;
                    tower.takeDamage(towerDamage);
                    totalDamageDealt += (int) towerDamage;
                }
            }
        }

        // Damage Buildings
        for (Building building : activeBuildings) {
            if (!building.isPlayer()) {
                double dx = building.getX() - x;
                double dy = building.getY() - y;
                if (dx * dx + dy * dy <= radiusSq) {
                    building.takeDamage(damage);
                    totalDamageDealt += (int) damage;
                }
            }
        }

        // Track spell damage for achievements
        if (totalDamageDealt > 0) {
            QuestManager.getInstance().onSpellDamageDealt(totalDamageDealt);
        }

        // Add visual effect
        activeEffects.add(new Effect(x, y, 1.0, "SPELL", radius));
    }

    /**
     * Gets the CardProgression for a card based on its rarity.
     * Returns stored progression or creates new default Level 1 progression.
     */
    public CardProgression getCardProgression(Card card) {
        if (card == null)
            return null;

        if (cardProgressions.containsKey(card.getName())) {
            return cardProgressions.get(card.getName());
        }

        CardRarity rarity = CardLibrary.getCardRarity(card.getName());
        if (rarity != null) {
            CardProgression cp = new CardProgression(card.getName(), rarity);
            cardProgressions.put(card.getName(), cp);
            return cp;
        }
        return null; // Card not found in library
    }
    
    /**
     * Gets all card progressions for stats display.
     */
    public java.util.Collection<CardProgression> getAllCardProgressions() {
        return cardProgressions.values();
    }

    // Upgrades a card to the next level if the player has enough gold.
    public boolean upgradeCard(Card card) {
        if (card == null || playerProfile == null) {
            return false;
        }

        CardProgression progression = getCardProgression(card);
        if (progression == null || !progression.canUpgrade()) {
            return false;
        }

        int cost = progression.getUpgradeCost();
        if (playerProfile.spendGold(cost)) {
            int previousLevel = progression.getLevel();
            progression.upgrade();
            progression.addGoldSpent(cost);

            // Track legendary card level 3 upgrade for achievements
            if (progression.getRarity() == CardRarity.LEGENDARY && progression.getLevel() == 3 && previousLevel == 2) {
                QuestManager.getInstance().onLegendaryCardUpgradedToLevel3();
            }

            saveGame(); // Auto-save after upgrade
            return true;
        }
        return false;
    }

    /**
     * Play a card at the specified position
     * Handles different card types: TROOP, BUILDING, SPELL
     * Applies level-based stat bonuses from Card Evolution & Rarity System
     */
    /**
     * Play a card at the specified position for the player
     */
    public boolean playCard(Card card, double x, double y) {
        return playCard(card, x, y, true);
    }

    /**
     * Play a card at the specified position with player flag
     */
    public boolean playCard(Card card, double x, double y, boolean isPlayer) {
        if (isMultiplayer && isPlayer) {
            // Broadcast move
            String payload = card.getName() + "," + x + "," + y;
            networkManager.sendMessage(
                    new Message(Message.MessageType.CARD_PLAYED, networkManager.getLocalPlayerId(), payload));
        }

        // Select correct elixir manager
        ElixirManager rsc;
        if (isLocalPvP) {
            // In local PvP: isPlayer true = Player 1, false = Player 2
            rsc = isPlayer ? playerElixirManager : player2ElixirManager;
        } else {
            // In normal/network: isPlayer true = player, false = computer
            rsc = isPlayer ? playerElixirManager : computerElixirManager;
        }

        int cost = card.getElixirCost();
        if (activeChallenge != null) {
            cost = activeChallenge.getModifiedCost(card);
        }

        // Use tracking method for player to track SPEND_ELIXIR quest
        boolean elixirSpent;
        if (isPlayer) {
            elixirSpent = rsc.spendElixirWithTracking(cost);
        } else {
            elixirSpent = rsc.spendElixir(cost);
        }

        if (!elixirSpent) {
            // If remote player (Multiplayer and !isPlayer), we must allow it to sync
            // The remote client is the authority on valid moves for themselves
            if (isMultiplayer && !isPlayer) {
                System.out.println("Forcing remote player move despite low local elixir calculation (Sync)");
                // Force spend (might go negative locally, but keeps sync)
                rsc.forceSpendElixir(cost);
            } else {
                return false;
            }
        }

        // Track PLAY_CARDS_SINGLE_MATCH quest for player
        if (isPlayer) {
            QuestManager.getInstance().addQuestProgress(
                    com.example.kuroyale.model.quest.QuestType.PLAY_CARDS_SINGLE_MATCH, 1);
        }

        // Get card progression for level bonuses
        CardProgression progression = getCardProgression(card);

        // Record card play and detect combos
        long currentTime = System.currentTimeMillis();
        List<DetectedCombo> detectedCombos = comboManager.recordCardPlay(card, currentTime, isPlayer);

        // Handle combo effects
        for (DetectedCombo detectedCombo : detectedCombos) {
            ComboEffect effect = detectedCombo.getEffect();
            // Create visual effect for the combo
            ComboVisualEffect visual = new ComboVisualEffect(
                    x, y,
                    detectedCombo.getComboType().getDisplayName(),
                    detectedCombo.getComboType(),
                    currentTime);
            activeComboVisuals.add(visual);
            // Special case: Elixir refund for Spell Synergy
            if (effect.getEffectType() == ComboEffectType.ELIXIR_REFUND) {
                int refundAmount = (int) effect.getValue();
                rsc.addElixir(refundAmount);
                System.out.println("COMBO! " + detectedCombo.getComboType().getDisplayName() +
                        " - Refunded " + refundAmount + " Elixir!");
            } else {
                // Apply effects to units/buildings
                ComboEffectApplier.applyComboEffect(detectedCombo, activeUnits, activeBuildings, isPlayer);
                System.out.println("COMBO! " + detectedCombo.getComboType().getDisplayName() +
                        " - Effect applied!");
            }
        }

        switch (card.getType()) {
            case "TROOP":
                Unit newUnit = UnitFactory.createUnit(card, x, y, isPlayer, progression);
                activeUnits.add(newUnit);

                // Track troop deployment for achievements (count based on card, e.g. Skeleton
                // Army = many)
                if (isPlayer) {
                    int troopCount = card.getName().contains("Army") ? 15 : card.getName().contains("Horde") ? 6 : 1;
                    QuestManager.getInstance().onTroopsDeployed(troopCount);
                    // Track troop deployment for quests (each card counts as 1 deployment)
                    QuestManager.getInstance()
                            .addQuestProgress(com.example.kuroyale.model.quest.QuestType.DEPLOY_TROOPS, 1);
                }

                // Console output - different for Local PvP
                String playerLabel;
                if (isLocalPvP) {
                    playerLabel = isPlayer ? "Player 1" : "Player 2";
                } else {
                    playerLabel = isPlayer ? "Player" : "Computer";
                }
                System.out.println(playerLabel + " spawned Troop: " + card.getName() + " (Level " +
                        (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            case "BUILDING":
                Building newBuilding = BuildingFactory.createBuilding(card, x, y, isPlayer, progression);
                activeBuildings.add(newBuilding);

                // Track building deployment for quests
                if (isPlayer) {
                    QuestManager.getInstance()
                            .addQuestProgress(com.example.kuroyale.model.quest.QuestType.PLAY_BUILDINGS, 1);
                }

                System.out.println(
                        (isPlayer ? "Player" : "Computer") + " placed Building: " + card.getName() + " (Level " +
                                (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            case "SPELL":
                applySpellDamage(card, x, y, progression); // Spell works same way, damage logic handles friend/foe
                                                           // based on target

                // Track spell deployment for quests
                if (isPlayer) {
                    QuestManager.getInstance().addQuestProgress(com.example.kuroyale.model.quest.QuestType.PLAY_SPELLS,
                            1);
                }

                System.out.println((isPlayer ? "Player" : "Computer") + " cast Spell: " + card.getName() + " (Level " +
                        (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            default:
                System.out.println("Unknown card type: " + card.getType());
                break;
        }

        return true;
    }

    private long multiplayerSeed;
    private java.util.List<Tower> opponentTowers = new java.util.ArrayList<>();
    private boolean isLocalReady = false;
    private boolean isRemoteReady = false;
    private boolean isPeerConnected = false;

    public void setMultiplayerSeed(long seed) {
        this.multiplayerSeed = seed;
        // Apply synchronized bridge layout
        if (arena != null) {
            arena.setupFixedBridges();
            System.out.println("Applied fixed bridges (seed ignored)");
        }
    }

    public void resetMultiplayerStates() {
        isLocalReady = false;
        isRemoteReady = false;
        isPeerConnected = false;
        opponentTowers.clear();
    }

    public boolean isPeerConnected() {
        return isPeerConnected;
    }

    public void setPeerConnected(boolean connected) {
        this.isPeerConnected = connected;
    }

    public boolean isLocalReady() {
        return isLocalReady;
    }

    public void setLocalReady(boolean ready) {
        this.isLocalReady = ready;
    }

    public boolean isRemoteReady() {
        return isRemoteReady;
    }

    public void setRemoteReady(boolean ready) {
        this.isRemoteReady = ready;
    }

    public void setOpponentTowers(String layoutData) {
        opponentTowers.clear();
        String[] towerStrings = layoutData.split(";");
        for (String ts : towerStrings) {
            if (ts.isEmpty())
                continue;
            try {
                // Format: Type,x,y
                String[] parts = ts.split(",");
                String type = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                opponentTowers.add(new Tower(type, x, y, false));
            } catch (Exception e) {
                System.err.println("Error parsing opponent tower: " + ts);
            }
        }
    }

    /**
     * Store Player 1's tower layout for Local PvP
     */
    public void setLocalPvPPlayer1Towers(String layoutData) {
        localPvPPlayer1Towers.clear();
        String[] towerStrings = layoutData.split(";");
        for (String ts : towerStrings) {
            if (ts.isEmpty())
                continue;
            try {
                // Format: Type,x,y
                String[] parts = ts.split(",");
                String type = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                localPvPPlayer1Towers.add(new Tower(type, x, y, true));
            } catch (Exception e) {
                System.err.println("Error parsing Player 1 tower: " + ts);
            }
        }
        System.out.println("Player 1 tower layout stored: " + localPvPPlayer1Towers.size() + " towers");
    }

    /**
     * Store Player 2's tower layout for Local PvP
     * Note: Towers are automatically mirrored to the top side
     */
    public void setLocalPvPPlayer2Towers(String layoutData) {
        localPvPPlayer2Towers.clear();
        String[] towerStrings = layoutData.split(";");
        for (String ts : towerStrings) {
            if (ts.isEmpty())
                continue;
            try {
                // Format: Type,x,y
                String[] parts = ts.split(",");
                String type = parts[0];
                double x = Double.parseDouble(parts[1]);
                double y = Double.parseDouble(parts[2]);
                // Player 2 towers are mirrored (both X and Y, like multiplayer)
                double mirrorX = arena.getWidth() - x;
                double mirrorY = arena.getHeight() - y;
                localPvPPlayer2Towers.add(new Tower(type, mirrorX, mirrorY, false));
            } catch (Exception e) {
                System.err.println("Error parsing Player 2 tower: " + ts);
            }
        }
        System.out.println("Player 2 tower layout stored (mirrored): " + localPvPPlayer2Towers.size() + " towers");
    }

    public long getMultiplayerSeed() {
        return multiplayerSeed;
    }

    public void startMultiplayerGame() {
        this.isMultiplayer = true;
        this.isPaused = false;

        // Setup Arena for Multiplayer
        if (arena != null) {
            // 1. Bridges
            arena.setupFixedBridges();

            // 2. Towers
            // Capture local towers (Player) before clearing
            java.util.List<Tower> localTowers = new java.util.ArrayList<>();
            for (Tower t : arena.getTowers()) {
                if (t.isPlayer()) {
                    localTowers.add(t);
                }
            }

            arena.clearTowers();

            // Add Local Towers
            for (Tower t : localTowers) {
                arena.addTower(t);
            }

            // Add Opponent Towers (Mirrored)
            for (Tower opRequest : opponentTowers) {
                // Opponent sent their setup as if they were bottom (Player).
                // We must mirror them to Top.
                // wait, if they sent x,y relative to them (Bottom), we mirror to Top.
                // My logic in setOpponentTowers just parsed x,y.
                // Mirror now:
                double mirrorX = opRequest.getX(); // X is preserved usually? No, mirror X too for perspective?
                // Standard Clash Royale: Enemy left is my right?
                // If enemy puts King at 9,30 (Bottom Center).
                // I see it at 9, 2 (Top Center).
                // So X is same (9), Y is mirrored (Height - Y).
                // Wait, if he puts Princess at Left (3.5), it should appear on my Right (Top
                // Right)?
                // Or does it appear on my Left (Top Left)?
                // Usually lane mirroring. Left lane fights Right lane?
                // Visual mirror:
                // His 3.5 (Left) -> My 14.5 (Right) on Top?
                // Let's stick to X Mirroring for true PvP perspective.

                double mirrorXCoord = arena.getWidth() - opRequest.getX();
                double mirrorYCoord = arena.getHeight() - opRequest.getY();

                arena.addTower(new Tower(opRequest.getType(), mirrorXCoord, mirrorYCoord, false));
            }

            // If no opponent towers (e.g. error/sync fail), add defaults?
            if (opponentTowers.isEmpty()) {
                System.out.println("No opponent towers received! Adding defaults.");
                arena.addTower(new Tower("KING", 9.0, 2.0, false));
                arena.addTower(new Tower("PRINCESS", 3.5, 5.5, false));
                arena.addTower(new Tower("PRINCESS", 14.5, 5.5, false));
            }
        }

        // Setup listener
        networkManager.setMessageHandler(this::handleIncomingMessage);

        startGame();
    }

    private void handleIncomingMessage(Message msg) {
        javafx.application.Platform.runLater(() -> {
            switch (msg.getType()) {
                case CARD_PLAYED:
                    // Data format: "CardName,x,y"
                    String[] parts = ((String) msg.getData()).split(",");
                    String cardName = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);

                    // Mirror coordinates for enemy view
                    double mirroredX = arena.getWidth() - x;
                    double mirroredY = arena.getHeight() - y;

                    Card card = CardLibrary.getCardByName(cardName);
                    if (card != null) {
                        // Play as opponent (isPlayer = false)
                        // Avoid re-sending message by checking isPlayer in playCard
                        playCard(card, mirroredX, mirroredY, false);
                    }
                    break;
                case DISCONNECT:
                    System.out.println("Opponent disconnected!");
                    // Handle win directly or show dialog
                    endGame("WIN"); // Award win on disconnect
                    break;
                case GAME_OVER:
                    // If opponent says game over? Usually local check is enough,
                    // but if we want to sync result:
                    // endGame((String) msg.getData());
                    break;
                default:
                    break;
            }
        });
    }

    public Deck getDeck() {
        return deck;
    }

    public Arena getArena() {
        return arena;
    }

    public ElixirManager getElixirManager() {
        return playerElixirManager;
    }

    public ElixirManager getComputerElixirManager() {
        return computerElixirManager;
    }

    public List<Unit> getActiveUnits() {
        return activeUnits;
    }

    public List<Building> getActiveBuildings() {
        return activeBuildings;
    }

    public List<Effect> getActiveEffects() {
        return activeEffects;
    }

    public ComboManager getComboManager() {
        return comboManager;
    }

    public List<ComboVisualEffect> getActiveComboVisuals() {
        return activeComboVisuals;
    }

    public boolean isGameRunning() {
        return isGameRunning;
    }

    public double getGameTime() {
        return gameTime;
    }

    /**
     * Checks for win conditions and ends game immediately if met.
     * Win conditions:
     * 1. If any king tower is destroyed, the game ends immediately
     */
    private void checkWinConditions() {
        if (!isGameRunning || gameResult != null) {
            return; // Game already ended
        }

        // Check for king tower destruction
        boolean playerKingDestroyed = false;
        boolean enemyKingDestroyed = false;

        for (Tower tower : arena.getTowers()) {
            if (tower.getType().equals("KING")) {
                if (tower.isDestroyed()) {
                    if (tower.isPlayer()) {
                        playerKingDestroyed = true;
                    } else {
                        enemyKingDestroyed = true;
                    }
                }
            }
        }

        // If any king tower is destroyed, end game immediately
        if (playerKingDestroyed && enemyKingDestroyed) {
            // Both kings destroyed simultaneously (rare case)
            QuestManager.getInstance().onKingTowerDestroyed();
            endGame("DRAW");
            System.out.println("Game Over! DRAW - Both kings destroyed!");
        } else if (enemyKingDestroyed) {
            // Enemy king destroyed - player wins
            QuestManager.getInstance().onKingTowerDestroyed();
            endGame("WIN");
            System.out.println("Game Over! VICTORY - Enemy king tower destroyed!");
        } else if (playerKingDestroyed) {
            // Player king destroyed - player loses
            endGame("LOSS");
            System.out.println("Game Over! DEFEAT - Your king tower was destroyed!");
        }
    }

    /**
     * Determines the winner when time runs out by comparing remaining tower health.
     * Winner is the player with the highest total tower health.
     */
    private void determineTimeoutWinner() {
        if (gameResult != null) {
            return; // Winner already determined
        }

        double playerTowerHealth = 0.0;
        double enemyTowerHealth = 0.0;

        for (Tower tower : arena.getTowers()) {
            if (!tower.isDestroyed()) {
                double health = tower.getHealth();
                if (tower.isPlayer()) {
                    playerTowerHealth += health;
                } else {
                    enemyTowerHealth += health;
                }
            }
        }

        if (playerTowerHealth > enemyTowerHealth) {
            endGame("WIN");
            System.out.println("Game Over! VICTORY - You have more tower health!");
        } else if (enemyTowerHealth > playerTowerHealth) {
            endGame("LOSS");
            System.out.println("Game Over! DEFEAT - Enemy has more tower health!");
        } else {
            endGame("DRAW");
            System.out.println("Game Over! DRAW - Equal tower health!");
        }
    }

    /**
     * Ends the game and sets the result
     */
    private void endGame(String result) {
        isGameRunning = false;
        gameResult = result;
        gameTime = 0; // Stop timer

        // Calculate and award combo rewards
        int comboCount = comboManager.getUniqueComboCount();
        int comboReward = comboCount * 10;
        if (comboReward > 0 && playerProfile != null) {
            playerProfile.addGold(comboReward);
            System.out.println("Combo Reward: " + comboCount + " combos triggered = " + comboReward + " gold!");
            saveGame(); // Save after adding gold
        }

        // Track achievement progress
        QuestManager qm = QuestManager.getInstance();
        qm.onMatchPlayed();

        if ("WIN".equals(result)) {
            qm.onMatchWon(isMultiplayer);

            // Track WIN_PVP for local PvP wins
            if (isLocalPvP) {
                qm.addQuestProgress(com.example.kuroyale.model.quest.QuestType.WIN_PVP, 1);
            }

            // Track PERFECT_WIN if player didn't lose any crown towers
            boolean lostAnyCrownTower = false;
            for (Tower tower : arena.getTowers()) {
                if (tower.isPlayer() && tower.isDestroyed() && !tower.getType().equals("KING")) {
                    lostAnyCrownTower = true;
                    break;
                }
            }
            if (!lostAnyCrownTower) {
                qm.addQuestProgress(com.example.kuroyale.model.quest.QuestType.PERFECT_WIN, 1);
            }

            if (playerProfile != null) {
                playerProfile.incrementWins();
                playerProfile.addGold(150); // Victory Gold
            }
        } else if ("LOSS".equals(result)) {
            qm.onMatchLost();
            if (playerProfile != null) {
                playerProfile.incrementLosses();
                playerProfile.addGold(50); // Defeat Gold
            }
        } else {
            if (playerProfile != null) {
                playerProfile.addGold(75); // Draw Gold
            }
        }

        if (playerProfile != null) {
            playerProfile.incrementMatchesPlayed();
        }

        saveGame(); // Save progress at end of match

        // Handle Challenge Completion
        if (activeChallenge != null && "WIN".equals(result)) {
            int stars = activeChallenge.calculateStars(this);
            if (stars > 0) {
                ChallengeManager.getInstance().completeChallenge(activeChallenge.getName(), stars);
                System.out.println(
                        "Challenge Complete! Stars: " + stars + " Reward: " + activeChallenge.getReward() + " Gold");
            }
        }
    }

    public Challenge getActiveChallenge() {
        return activeChallenge;
    }

    public void clearActiveChallenge() {
        this.activeChallenge = null;
    }

    /**
     * Returns the game result: "WIN", "LOSS", "DRAW", or null if game is still
     * running
     */
    public String getGameResult() {
        return gameResult;
    }

    private boolean isValidTarget(Building attacker, Unit target) {
        TargetType attackerTargetType = attacker.getTargetType();
        TransportType targetTransport = target.getTransportType();

        if (attackerTargetType == TargetType.NONE) {
            return false;
        }

        if (attackerTargetType == TargetType.BUILDINGS) {
            return false;
        }

        if (targetTransport == TransportType.AIR) {
            return attackerTargetType == TargetType.AIR_AND_GROUND;
        } else if (targetTransport == TransportType.GROUND) {
            return (attackerTargetType == TargetType.GROUND || attackerTargetType == TargetType.AIR_AND_GROUND);
        }

        return false;
    }
}
