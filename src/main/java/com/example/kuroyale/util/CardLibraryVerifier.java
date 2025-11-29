package com.example.kuroyale.util;

import com.example.kuroyale.model.Card;
import com.example.kuroyale.model.CardLibrary;

import java.util.List;

public class CardLibraryVerifier {
    public static void main(String[] args) {
        System.out.println("KU Royale Card Library - Complete List");
        System.out.println("========================================\n");

        List<Card> allCards = CardLibrary.getAllCards();
        System.out.println("Total Cards: " + allCards.size() + "/28\n");

        int troopCount = 0, buildingCount = 0, spellCount = 0;

        System.out.println("TROOP CARDS:");
        System.out.println("------------");
        for (Card card : allCards) {
            if ("TROOP".equals(card.getType())) {
                troopCount++;
                System.out.printf(
                        "%2d. %-20s | Cost: %d | HP: %-4.0f | DMG: %-3.0f | Hit Speed: %.1fs | Range: %.1f | Speed: %.1f%n",
                        troopCount, card.getName(), card.getElixirCost(), card.getHealth(), card.getDamage(),
                        card.getHitSpeed(), card.getRange(), card.getSpeed());
            }
        }

        System.out.println("\nBUILDING CARDS:");
        System.out.println("---------------");
        for (Card card : allCards) {
            if ("BUILDING".equals(card.getType())) {
                buildingCount++;
                System.out.printf("%2d. %-20s | Cost: %d | HP: %-4.0f | DMG: %-3.0f | Range: %.1f%n",
                        buildingCount, card.getName(), card.getElixirCost(), card.getHealth(),
                        card.getDamage(), card.getRange());
            }
        }

        System.out.println("\nSPELL CARDS:");
        System.out.println("------------");
        for (Card card : allCards) {
            if ("SPELL".equals(card.getType())) {
                spellCount++;
                System.out.printf("%2d. %-20s | Cost: %d | DMG: %-3.0f | Radius: %.1f%n",
                        spellCount, card.getName(), card.getElixirCost(), card.getDamage(), card.getRange());
            }
        }

        System.out.println("\n========================================");
        System.out.println("Summary:");
        System.out.println("  Troops: " + troopCount + "/15");
        System.out.println("  Buildings: " + buildingCount + "/9");
        System.out.println("  Spells: " + spellCount + "/4");
        System.out.println("========================================");

        if (allCards.size() == 28 && troopCount == 15 && buildingCount == 9 && spellCount == 4) {
            System.out.println("✓ All cards implemented correctly!");
        } else {
            System.out.println("✗ Card count mismatch!");
        }
    }
}
