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

    public void reset() {
        for (Tower tower : towers) {
            tower.reset();
        }
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

    public void randomizeBridges(long seed) {
        bridges.clear();
        java.util.Random random = new java.util.Random(seed);

        // Random number of bridges: 1 to 3
        int bridgeCount = 1 + random.nextInt(3);

        // Define valid range for bridge placement (0 to width - 2.0)
        double minX = 1.0;
        double maxX = width - 3.0; // Ensure some padding

        for (int i = 0; i < bridgeCount; i++) {
            // Try to place a bridge
            for (int attempt = 0; attempt < 10; attempt++) {
                double x = minX + (maxX - minX) * random.nextDouble();

                // Check overlap with existing bridges (simple check)
                boolean overlaps = false;
                for (Bridge b : bridges) {
                    if (Math.abs(b.x - x) < 3.0) { // Keep them at least 3 units apart
                        overlaps = true;
                        break;
                    }
                }

                if (!overlaps) {
                    addBridge("Bridge " + (i + 1), x);
                    break;
                }
            }
        }

        // Fallback: Ensure at least one bridge if random placement failed repeatedly
        if (bridges.isEmpty()) {
            addBridge("Bridge 1", width / 2.0 - 1.0); // Center
        }
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
