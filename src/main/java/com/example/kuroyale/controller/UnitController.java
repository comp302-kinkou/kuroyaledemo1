package com.example.kuroyale.controller;

import com.example.kuroyale.model.*;

public class UnitController {

    private Arena arena;
    private GameController gameController;

    public UnitController(GameController gameController, Arena arena) {
        this.gameController = gameController;
        this.arena = arena;
    }

    public void updateUnitBehavior(Unit unit, double deltaTime) {
        // move to or attack nearest target
        Object target = findNearestTarget(unit);

        if (target != null) {
            double targetX = 0, targetY = 0;
            double targetRadius = 0.5; // hitbox radius

            if (target instanceof Unit) {
                targetX = ((Unit) target).getX();
                targetY = ((Unit) target).getY();
            } else if (target instanceof Tower) {
                targetX = ((Tower) target).getX();
                targetY = ((Tower) target).getY();
            } else if (target instanceof Building) {
                targetX = ((Building) target).getX();
                targetY = ((Building) target).getY();
            }

            if (unit.isInRange(targetX, targetY, targetRadius)) {
                // attack
                if (unit.canAttack(System.currentTimeMillis())) {
                    unit.attack(System.currentTimeMillis());
                    if (target instanceof Unit) {
                        ((Unit) target).takeDamage(unit.getDamage());
                    } else if (target instanceof Tower) {
                        ((Tower) target).takeDamage(unit.getDamage());
                    } else if (target instanceof Building) {
                        ((Building) target).takeDamage(unit.getDamage());
                    }
                }
            } else {
                // pathfinding
                moveUnit(unit, targetX, targetY, deltaTime);
            }
        }
    }

    private void moveUnit(Unit unit, double targetX, double targetY, double deltaTime) {
        double currentX = unit.getX();
        double currentY = unit.getY();
        double riverY = arena.getRiverY();

        // check if we need to cross the river
        boolean needsToCross = (currentY < riverY && targetY > riverY) || (currentY > riverY && targetY < riverY);

        // If unit is AIR, it can fly over the river
        if (unit.getTransportType() == TransportType.AIR) {
            unit.moveTowards(targetX, targetY, deltaTime);
            return;
        }

        if (needsToCross) {
            // find closest bridge
            Arena.Bridge nearestBridge = null;
            double minDistance = Double.MAX_VALUE;

            for (Arena.Bridge bridge : arena.getBridges()) {
                // distance to the center x of the bridge
                double bridgeCenterX = bridge.x + bridge.width / 2.0;
                double dist = Math.abs(currentX - bridgeCenterX);
                if (dist < minDistance) {
                    minDistance = dist;
                    nearestBridge = bridge;
                }
            }

            if (nearestBridge != null) {
                double bridgeCenterX = nearestBridge.x + nearestBridge.width / 2.0;
                // move to bridge x to cross the river
                boolean onBridgeX = currentX >= nearestBridge.x && currentX <= (nearestBridge.x + nearestBridge.width);

                if (!onBridgeX) {
                    unit.moveTowards(bridgeCenterX, riverY, deltaTime);
                } else {
                    unit.moveTowards(targetX, targetY, deltaTime);
                }
            }
        } else {
            // same side
            unit.moveTowards(targetX, targetY, deltaTime);
        }
    }

    private Object findNearestTarget(Unit unit) {
        // Search for nearest enemy Unit or Tower
        double minDistSq = Double.MAX_VALUE;
        Object nearest = null;

        // Check enemy units
        for (Unit other : gameController.getActiveUnits()) {
            if (unit.isPlayer() != other.isPlayer()) {
                // Check TargetType
                if (!isValidTarget(unit, other)) {
                    continue;
                }

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
                if (!isValidTarget(unit, tower)) {
                    continue;
                }

                double dx = unit.getX() - tower.getX();
                double dy = unit.getY() - tower.getY();
                double distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = tower;
                }
            }
        }

        // Check enemy buildings
        for (Building building : gameController.getActiveBuildings()) {
            if (unit.isPlayer() != building.isPlayer() && !building.isDestroyed()) {
                if (!isValidTarget(unit, building)) {
                    continue;
                }

                double dx = unit.getX() - building.getX();
                double dy = unit.getY() - building.getY();
                double distSq = dx * dx + dy * dy;
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = building;
                }
            }
        }

        return nearest;
    }

    private boolean isValidTarget(Unit attacker, Object target) {
        TargetType attackerTargetType = attacker.getTargetType();

        if (attackerTargetType == TargetType.NONE) {
            return false;
        }

        // 1. Handle Buildings & Towers (They are "Ground" targets concept, but not
        // TransportType.GROUND)
        if (target instanceof Tower || target instanceof Building) {
            // Who targets buildings?
            // - GROUND targeters (Knight) -> YES
            // - AIR_AND_GROUND targeters (Minions) -> YES
            // - BUILDINGS targeters (Giant) -> YES
            return true;
        }

        // 2. Handle Units (Troops)
        if (target instanceof Unit) {
            Unit targetUnit = (Unit) target;
            TransportType targetTransport = targetUnit.getTransportType();

            // Units cannot target strictly "BUILDINGS" types
            if (attackerTargetType == TargetType.BUILDINGS) {
                return false;
            }

            // Logic for hitting troops
            if (targetTransport == TransportType.AIR) {
                // Only AIR_AND_GROUND can hit Air
                return attackerTargetType == TargetType.AIR_AND_GROUND;
            } else if (targetTransport == TransportType.GROUND) {
                // GROUND and AIR_AND_GROUND can hit Ground troops
                return (attackerTargetType == TargetType.GROUND || attackerTargetType == TargetType.AIR_AND_GROUND);
            }
        }

        return false;
    }
}
