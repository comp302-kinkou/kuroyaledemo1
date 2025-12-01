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
            }

            if (unit.isInRange(targetX, targetY, targetRadius)) {
                // attack
                if (unit.canAttack(System.currentTimeMillis())) {
                    unit.attack(System.currentTimeMillis());
                    if (target instanceof Unit) {
                        ((Unit) target).takeDamage(unit.getDamage());
                    } else if (target instanceof Tower) {
                        ((Tower) target).takeDamage(unit.getDamage());
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
}
