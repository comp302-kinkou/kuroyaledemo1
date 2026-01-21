package com.example.kuroyale.model.persistence;

import java.io.*;

public class PersistenceManager {

    private static PersistenceManager instance;

    private PersistenceManager() {
    }

    /**
     * Singleton Design Pattern Implementation.
     * <p>
     * Provides a single global access point to the persistence logic, ensuring
     * consistent file I/O operations across the application.
     * </p>
     * 
     * @return The single instance of PersistenceManager
     */
    public static PersistenceManager getInstance() {
        if (instance == null) {
            instance = new PersistenceManager();
        }
        return instance;
    }

    /**
     * Saves the game data to the specified file.
     * 
     * @param data     The GameData object to save.
     * @param filename The name of the file to save to.
     * @return true if successful, false otherwise.
     */
    public boolean save(GameData data, String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(data);
            System.out.println("Game saved successfully to " + filename);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads the game data from the specified file.
     * 
     * @param filename The name of the file to load from.
     * @return The loaded GameData object, or null if loading failed.
     */
    public GameData load(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Save file not found: " + filename);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            GameData data = (GameData) ois.readObject();
            System.out.println("Game loaded successfully from " + filename);
            return data;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
