package com.example.kuroyale.controller;

import com.example.kuroyale.model.*;
import java.util.ArrayList;

import java.util.List;

public class GameController {

    private static GameController instance;

    private Deck deck;
    private Arena arena;
    private ElixirManager elixirManager;
    private List<Unit> activeUnits;
    private List<Building> activeBuildings;
    private List<Effect> activeEffects;
    private boolean isGameRunning;
    private UnitController unitController;

    private double gameTime;

    private boolean isPaused;
    private String gameResult; // "WIN", "LOSS", "DRAW", or null if game is ongoing

    // For demo purposes, we might want to access the player's deck globally or pass
    // it in
    // In this design, the GameController owns the game state.

    private double timeScale = 1.0;

    private GameController() {
        this.deck = new Deck();
        this.arena = new Arena();
        this.elixirManager = new ElixirManager();
        this.activeUnits = new ArrayList<>();
        this.activeBuildings = new ArrayList<>();
        this.activeEffects = new ArrayList<>();
        this.isGameRunning = false;
        this.unitController = new UnitController(this, this.arena);
        this.gameTime = 180.0; // 3 minutes
        this.isPaused = false;
        this.gameResult = null;
        this.timeScale = 1.0;

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
        // Re-initialize game deck state if game is running or about to start
        this.deck.initializeGameDeck();
    }

    public void startGame() {
        isGameRunning = true;
        isPaused = false;
        gameResult = null;
        activeUnits.clear();
        activeBuildings.clear();
        activeEffects.clear();
        elixirManager = new ElixirManager(); // Reset elixir
        timeScale = 1.0; // Reset speed

        // Only set default if no towers exist (i.e. not customized)
        if (arena.getTowers().isEmpty()) {
            arena.setupDefaultTowers();
        }

        deck.initializeGameDeck();
        gameTime = 180.0;
        System.out.println("Game Started!");
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
        elixirManager.update(scaledDeltaTime);

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
                }
            }
        }

        // Add visual effect
        activeEffects.add(new Effect(x, y, 1.0, "SPELL", radius));
    }

    /**
     * Gets the CardProgression for a card based on its rarity.
     * Currently returns Level 1 progression (upgrade system to be added later).
     */
    private CardProgression getCardProgression(Card card) {
        CardRarity rarity = CardLibrary.getCardRarity(card.getName());
        if (rarity != null) {
            return new CardProgression(card.getName(), rarity);
        }
        return null; // Card not found in library
    }

    /**
     * Play a card at the specified position
     * Handles different card types: TROOP, BUILDING, SPELL
     * Applies level-based stat bonuses from Card Evolution & Rarity System
     */
    public boolean playCard(Card card, double x, double y) {
        if (!elixirManager.spendElixir(card.getElixirCost())) {
            return false;
        }

        // Get card progression for level bonuses
        CardProgression progression = getCardProgression(card);

        switch (card.getType()) {
            case "TROOP":
                Unit newUnit = UnitFactory.createUnit(card, x, y, true, progression);
                activeUnits.add(newUnit);
                System.out.println("Troop spawned: " + card.getName() + " (Level " +
                        (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            case "BUILDING":
                Building newBuilding = BuildingFactory.createBuilding(card, x, y, true, progression);
                activeBuildings.add(newBuilding);
                System.out.println("Building placed: " + card.getName() + " (Level " +
                        (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            case "SPELL":
                applySpellDamage(card, x, y, progression);
                System.out.println("Spell cast: " + card.getName() + " (Level " +
                        (progression != null ? progression.getLevel() : 1) + ") at (" + x + ", " + y + ")");
                break;

            default:
                System.out.println("Unknown card type: " + card.getType());
                break;
        }

        return true;
    }

    public Deck getDeck() {
        return deck;
    }

    public Arena getArena() {
        return arena;
    }

    public ElixirManager getElixirManager() {
        return elixirManager;
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
            endGame("DRAW");
            System.out.println("Game Over! DRAW - Both kings destroyed!");
        } else if (enemyKingDestroyed) {
            // Enemy king destroyed - player wins
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
