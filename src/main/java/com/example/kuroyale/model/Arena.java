package com.example.kuroyale.model;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private double width = 18.0;
    private double height = 32.0;

    private static final int MAX_BRIDGE = 3;

    private List<Tower> towers = new ArrayList<>();

    // Bridge positions for collision detection
    public static class Bridge {
        public double x;
        public double width;
        public String name;

        public Bridge(String name, double x, double width) {
            this.name = name;
            this.x = x;
            this.width = width;
        }

        public boolean contains(double testX, double testY, double riverY) {
            // Check if point is on bridge (within width and near river)
            return Math.abs(testY - riverY) <= 1.0
                    && testX >= x && testX <= (x + width);
        }
    }

    private List<Bridge> bridges = new ArrayList<>();
    private double riverY = 16.0; // Middle of arena

    public Arena() {
        // Towers will be initialized by GameController or ArenaDesigner
    }

    public void setupDefaultTowers() {
        towers.clear();
        // Player Towers (Bottom)
        towers.add(new Tower("KING", 9.0, 30.0, true));
        towers.add(new Tower("PRINCESS", 3.5, 26.5, true));
        towers.add(new Tower("PRINCESS", 14.5, 26.5, true));

        // Enemy Towers (Top)
        towers.add(new Tower("KING", 9.0, 2.0, false));
        towers.add(new Tower("PRINCESS", 3.5, 5.5, false));
        towers.add(new Tower("PRINCESS", 14.5, 5.5, false));
    }

    public void addTower(Tower tower) {
        towers.add(tower);
    }

    public void clearTowers() {
        towers.clear();
    }

    public void clearBridges() {
        bridges.clear();
    }

    public boolean addBridge(String name, double x) {
        if (bridges.size() >= MAX_BRIDGE) {
            System.out.println("Maximum " + MAX_BRIDGE + " bridges allowed.");
            return false;
        }

        if (x < 0 || x > width - 2.0) {
            return false;
        }

        bridges.add(new Bridge(name, x, 2.0));
        return true;
    }

    public List<Tower> getTowers() {
        return towers;
    }

    public List<Bridge> getBridges() {
        return bridges;
    }

    /**
     * Check if a position is on any bridge (for building placement restriction)
     */
    public boolean isOnBridge(double x, double y) {
        for (Bridge bridge : bridges) {
            if (bridge.contains(x, y, riverY)) {
                return true;
            }
        }
        return false;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getRiverY() {
        return riverY;
    }

    @Override
    public String toString() {
        return "Arena [Towers=" + towers.size() + ", Bridges=" + bridges.size() + "]";
    }
}
