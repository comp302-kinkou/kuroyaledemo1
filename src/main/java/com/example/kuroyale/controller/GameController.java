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
    private boolean isGameRunning;

    // For demo purposes, we might want to access the player's deck globally or pass
    // it in
    // In this design, the GameController owns the game state.

    private GameController() {
        this.deck = new Deck();
        this.arena = new Arena();
        this.elixirManager = new ElixirManager();
        this.activeUnits = new ArrayList<>();
        this.activeBuildings = new ArrayList<>();
        this.isGameRunning = false;

        initializeCards(); // Populate the deck with available cards
    }

    public static GameController getInstance() {
        if (instance == null) {
            instance = new GameController();
        }
        return instance;
    }

    private void initializeCards() {
        // Initialize deck with first 8 cards from library by default
        List<Card> library = CardLibrary.getAllCards();
        for (int i = 0; i < 8 && i < library.size(); i++) {
            deck.addCard(library.get(i));
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
        activeUnits.clear();
        activeBuildings.clear();
        elixirManager = new ElixirManager(); // Reset elixir
        arena.setupDefaultTowers();
        deck.initializeGameDeck();
        System.out.println("Game Started!");
    }

    public void update(double deltaTime) {
        if (!isGameRunning)
            return;

        // 1. Update Elixir
        elixirManager.update(deltaTime);

        // 2. Update Units (Movement and Attack)
        for (Unit unit : activeUnits) {
            updateUnitBehavior(unit, deltaTime);
        }

        // 3. Update Buildings (Attack, Spawn, Elixir Generation, Lifetime)
        for (Building building : activeBuildings) {
            updateBuildingBehavior(building, deltaTime);
        }

        // 4. Update Towers (Attack)
        for (Tower tower : arena.getTowers()) {
            updateTowerBehavior(tower, deltaTime);
        }

        // 5. Remove dead units and expired/destroyed buildings
        activeUnits.removeIf(Unit::isDead);
        activeBuildings.removeIf(Building::isDestroyed);
    }

    private void updateUnitBehavior(Unit unit, double deltaTime) {
        // Simple AI: Find nearest enemy target (Tower or Unit)
        // If in range -> Attack
        // If not in range -> Move towards it

        Object target = findNearestTarget(unit);

        if (target != null) {
            double targetX = 0, targetY = 0;
            double targetRadius = 0.5; // Hitbox radius

            if (target instanceof Unit) {
                targetX = ((Unit) target).getX();
                targetY = ((Unit) target).getY();
            } else if (target instanceof Tower) {
                targetX = ((Tower) target).getX();
                targetY = ((Tower) target).getY();
                targetRadius = 1.0; // Towers are bigger
            }

            if (unit.isInRange(targetX, targetY, targetRadius)) {
                // Attack
                if (unit.canAttack(System.currentTimeMillis())) {
                    unit.attack(System.currentTimeMillis());
                    if (target instanceof Unit) {
                        ((Unit) target).takeDamage(unit.getDamage());
                    } else if (target instanceof Tower) {
                        ((Tower) target).takeDamage(unit.getDamage());
                    }
                }
            } else {
                // Move
                // For simple pathfinding, move towards bridge if on own side, or target if
                // across river
                // Simplified: just move towards target
                unit.moveTowards(targetX, targetY, deltaTime);
            }
        } else {
            // No target, move forward (up for player, down for enemy)
            double forwardY = unit.isPlayer() ? 0 : arena.getHeight();
            unit.moveTowards(unit.getX(), forwardY, deltaTime);
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

    private Object findNearestTarget(Unit unit) {
        // Search for nearest enemy Unit or Tower
        double minDistSq = Double.MAX_VALUE;
        Object nearest = null;

        // Check enemy units
        for (Unit other : activeUnits) {
            if (unit.isPlayer() != other.isPlayer()) {
                double dx = unit.getX() - other.getX();
                double dy = unit.getY() - other.getY();
                double distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = other;
                }
            }
        }

        // Check enemy towers
        for (Tower tower : arena.getTowers()) {
            if (unit.isPlayer() != tower.isPlayer() && !tower.isDestroyed()) {
                double dx = unit.getX() - tower.getX();
                double dy = unit.getY() - tower.getY();
                double distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = tower;
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

        // 2. Spawner logic (if applicable)
        // (Simplified: just print for now or implement if Building has spawn logic)
        // For this demo, we assume Building handles its own internal logic in update()
        // But we might need to handle spawning here if Building just stores data.
        // Let's assume Building.update() handles lifetime.

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
        double radius = card.getRange();
        double radiusSq = radius * radius;
        double damage = card.getDamage();

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
    }

    /**
     * Play a card at the specified position
     * Handles different card types: TROOP, BUILDING, SPELL
     */
    public boolean playCard(Card card, double x, double y) {
        if (!elixirManager.spendElixir(card.getElixirCost())) {
            return false;
        }

        switch (card.getType()) {
            case "TROOP":
                Unit newUnit = UnitFactory.createUnit(card, x, y, true);
                activeUnits.add(newUnit);
                System.out.println("Troop spawned: " + card.getName() + " at (" + x + ", " + y + ")");
                break;

            case "BUILDING":
                Building newBuilding = BuildingFactory.createBuilding(card, x, y, true);
                activeBuildings.add(newBuilding);
                System.out.println("Building placed: " + card.getName() + " at (" + x + ", " + y + ")");
                break;

            case "SPELL":
                applySpellDamage(card, x, y);
                System.out.println("Spell cast: " + card.getName() + " at (" + x + ", " + y + ")");
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

    public boolean isGameRunning() {
        return isGameRunning;
    }
}
