package com.example.kuroyale.controller;

import com.example.kuroyale.model.Building;
import com.example.kuroyale.model.Card;

public class BuildingFactory {

    public static Building createBuilding(Card card, double x, double y, boolean isPlayer) {
        String name = card.getName();
        String type = determineBuildingType(name);

        Building building = new Building(
                name,
                x, y,
                isPlayer,
                card.getHealth(),
                card.getDamage(),
                card.getRange(),
                card.getHitSpeed(),
                getLifetime(name),
                type);

        // Set spawner properties if it's a spawner building
        if (type.equals("SPAWNER")) {
            setSpawnerProperties(building, name);
        }

        return building;
    }

    private static String determineBuildingType(String name) {
        switch (name) {
            case "Tombstone":
            case "Goblin Hut":
            case "Barbarian Hut":
                return "SPAWNER";
            case "Elixir Collector":
                return "SPECIAL";
            default:
                return "DEFENSIVE"; // Cannon, Tesla, Mortar, Bomb Tower, Inferno Tower
        }
    }

    private static double getLifetime(String name) {
        switch (name) {
            case "Cannon":
                return 30.0;
            case "Mortar":
                return 30.0;
            case "Tesla":
                return 40.0;
            case "Bomb Tower":
                return 40.0;
            case "Inferno Tower":
                return 40.0;
            case "Tombstone":
                return 40.0;
            case "Goblin Hut":
                return 60.0;
            case "Barbarian Hut":
                return 60.0;
            case "Elixir Collector":
                return 70.0;
            default:
                return 30.0;
        }
    }

    private static void setSpawnerProperties(Building building, String name) {
        switch (name) {
            case "Tombstone":
                building.setSpawnerProperties("Skeletons", 2.9);
                break;
            case "Goblin Hut":
                building.setSpawnerProperties("Spear Goblins", 4.9);
                break;
            case "Barbarian Hut":
                building.setSpawnerProperties("Barbarians", 14.0);
                break;
        }
    }
}
