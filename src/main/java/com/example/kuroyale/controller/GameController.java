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

public class GameController {

    private static GameController instance;

    private Deck deck; // Player's deck
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

    private double gameTime;

    private boolean isDeckSaved = false;
    private boolean isArenaSaved = false;

    private boolean isPaused;
    private String gameResult; // "WIN", "LOSS", "DRAW", or null if game is ongoing

    // Combo System
    private ComboManager comboManager;

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
        data.setQuestData(questData);
        data.setCardProgressions(new ArrayList<>(cardProgressions.values()));
        data.setChallengeData(ChallengeManager.getInstance().exportData());

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

        // Load Network Config at startup
        com.example.kuroyale.config.NetworkConfig.getInstance();
        this.networkManager = NetworkManager.getInstance();

        // Try to load game strictly on startup? Or maybe call loadGame() explicitly.
        // For now, let's just initialize default empty structures.
        // Actually, let's try to load automatically for convenience.
        loadGame();

        initializeCards(); // Populate the deck with available cards
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

        // IMPORTANT: Clear all game state
        activeUnits.clear();
        activeBuildings.clear();
        activeEffects.clear();
        isGameRunning = false;
        gameResult = null;

        System.out.println("Game mode reset to normal.");
    }

    public void startGame() {
        isGameRunning = true;
        isPaused = false;
        gameResult = null;
        activeBuildings.clear();
        activeEffects.clear();
        playerElixirManager = new ElixirManager(); // Reset player elixir
        computerElixirManager = new ElixirManager(); // Reset computer elixir
        playerElixirManager = new ElixirManager(); // Reset player elixir
        computerElixirManager = new ElixirManager(); // Reset computer elixir

        comboManager.reset(); // Reset combo manager for new game

        if (!isMultiplayer) {
            computerOpponent = new ComputerOpponent(this); // Reset opponent logic
        } else {
            computerOpponent = null; // No AI in multiplayer
        }

        timeScale = 1.0; // Reset speed

        // Only set default if no towers exist (i.e. not customized)
        if (arena.getTowers().isEmpty()) {
            arena.setupDefaultTowers();
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

        // Initialize both players' elixir
        playerElixirManager = new ElixirManager();
        player2ElixirManager = new ElixirManager();

        // No computer opponent in local PvP
        computerOpponent = null;

        // Reset turn to Player 1
        currentPlayerTurn = 1;

        timeScale = 1.0;

        // Setup arena if needed
        if (arena.getTowers().isEmpty()) {
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

    private void applySpellDamage(Card card, double x, double y) {
        applySpellDamage(card, x, y, null);
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
                    tower.takeDamage(damage);
                    totalDamageDealt += (int) damage;
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

        if (!rsc.spendElixir(cost)) {
            return false;
        }

        // Get card progression for level bonuses
        CardProgression progression = getCardProgression(card);

        // Record card play and detect combos
        long currentTime = System.currentTimeMillis();
        List<DetectedCombo> detectedCombos = comboManager.recordCardPlay(card, currentTime, isPlayer);

        // Handle combo effects
        for (DetectedCombo detectedCombo : detectedCombos) {
            ComboEffect effect = detectedCombo.getEffect();
            
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
                System.out.println(
                        (isPlayer ? "Player" : "Computer") + " placed Building: " + card.getName() + " (Level " +
                                (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            case "SPELL":
                applySpellDamage(card, x, y, progression); // Spell works same way, damage logic handles friend/foe
                                                           // based on target
                System.out.println((isPlayer ? "Player" : "Computer") + " cast Spell: " + card.getName() + " (Level " +
                        (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            default:
                System.out.println("Unknown card type: " + card.getType());
                break;
        }

        return true;
    }

    public void startMultiplayerGame() {
        this.isMultiplayer = true;
        this.isPaused = false;

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

    private Challenge activeChallenge;

    /**
     * Ends the game and sets the result
     */
    private void endGame(String result) {
        isGameRunning = false;
        gameResult = result;
        gameTime = 0; // Stop timer

        // Track achievement progress
        QuestManager qm = QuestManager.getInstance();
        qm.onMatchPlayed();

        if ("WIN".equals(result)) {
            qm.onMatchWon(isMultiplayer);
        } else if ("LOSS".equals(result)) {
            qm.onMatchLost();
        }

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

    public void startChallenge(Challenge challenge) {
        this.activeChallenge = challenge;
        challenge.onGameStart(this);
        startGame();
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
