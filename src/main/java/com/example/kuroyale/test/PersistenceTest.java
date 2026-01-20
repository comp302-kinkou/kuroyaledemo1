package com.example.kuroyale.test;

import com.example.kuroyale.model.persistence.*;
import com.example.kuroyale.model.CardProgression;
import com.example.kuroyale.model.CardRarity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PersistenceTest {
    public static void main(String[] args) {
        System.out.println("Starting Persistence Test...");

        String filename = "test_save.dat";
        PersistenceManager pm = PersistenceManager.getInstance();

        // 1. Create Data
        GameData data = new GameData();
        data.getPlayerProfile().setName("TestPlayer");
        data.getPlayerProfile().setTotalGold(500);
        data.getPlayerProfile().incrementWins();
        data.getPlayerProfile().incrementWins();
        data.getPlayerProfile().incrementWins();
        data.getPlayerProfile().incrementWins();
        data.getPlayerProfile().incrementWins();

        CardProgression card = new CardProgression("Knight", CardRarity.COMMON);
        // Manually level up to matches previous test setup (Level 2)
        card.upgrade();

        card.addGoldSpent(100);
        List<CardProgression> cards = new ArrayList<>();
        cards.add(card);
        data.setCardProgressions(cards);

        System.out.println("Saving data...");
        // 2. Save
        if (pm.save(data, filename)) {
            System.out.println("Save successful.");
        } else {
            System.err.println("Save failed!");
            return;
        }

        // 3. Load
        System.out.println("Loading data...");
        GameData loadedData = pm.load(filename);

        // 4. Verify
        if (loadedData != null) {
            System.out.println("Load successful.");
            boolean passed = true;

            if (!"TestPlayer".equals(loadedData.getPlayerProfile().getName())) {
                System.err.println("Name mismatch: " + loadedData.getPlayerProfile().getName());
                passed = false;
            }
            if (500 != loadedData.getPlayerProfile().getTotalGold()) {
                System.err.println("Gold mismatch: " + loadedData.getPlayerProfile().getTotalGold());
                passed = false;
            }
            if (5 != loadedData.getPlayerProfile().getTotalWins()) {
                System.err.println("Stats mismatch: " + loadedData.getPlayerProfile().getTotalWins());
                passed = false;
            }
            if (loadedData.getCardProgressions().size() != 1) {
                System.err.println("Card count mismatch");
                passed = false;
            } else {
                CardProgression loadedCard = loadedData.getCardProgressions().get(0);
                if (!"Knight".equals(loadedCard.getCardName())) {
                    System.err.println("Card name mismatch");
                    passed = false;
                }
                if (2 != loadedCard.getLevel()) {
                    System.err.println("Card level mismatch");
                    passed = false;
                }
                if (100 != loadedCard.getTotalGoldSpent()) {
                    System.err.println("Card gold spent mismatch");
                    passed = false;
                }
            }

            if (passed) {
                System.out.println("VERIFICATION PASSED!");
            } else {
                System.out.println("VERIFICATION FAILED!");
            }
        } else {
            System.err.println("Load returned null!");
        }

        // Clean up
        new File(filename).delete();
    }
}
