package com.example.kuroyale;

import com.example.kuroyale.controller.GameController;

public class TestRun {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Headless Test...");
        GameController controller = GameController.getInstance();
        controller.startGame();

        // Simulate 20 seconds at 60 FPS (approx)
        double deltaTime = 0.016; // 16ms

        // Give some initial elixir
        // Actually elixir starts at 5.

        for (int i = 0; i < 20 * 60; i++) {
            controller.update(deltaTime);

            if (i % 60 == 0) { // Every ~1 second
                System.out.println("Time: " + String.format("%.1f", controller.getGameTime()) +
                        " | Computer Elixir: " + controller.getComputerElixirManager().getElixir());
            }

            // Sleep a tiny bit to avoid busy loop consuming 100% CPU if desired, but for
            // test logic not needed
            // Thread.sleep(1);
        }

        System.out.println("Test Run Complete.");
    }
}
