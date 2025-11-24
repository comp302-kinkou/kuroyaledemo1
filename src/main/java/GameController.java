import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameController {

    private Scanner scanner = new Scanner(System.in);
    private Deck deck = new Deck();
    private Arena arena = null;
    private List<Card> allCards = new ArrayList<>();

    public GameController() {
        allCards.add(new Card("Knight", 3));
        allCards.add(new Card("Archer", 2));
        allCards.add(new Card("Fireball", 4));
        allCards.add(new Card("Giant", 5));
    }

    public void run() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Build Deck");
            System.out.println("2. Design Arena");
            System.out.println("3. Start Game");
            System.out.println("4. Quit");
            System.out.print("Select: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> buildDeck();
                case "2" -> designArena();
                case "3" -> startGame();
                case "4" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }

        System.out.println("Goodbye!");
    }

    private void buildDeck() {
        System.out.println("\n--- Deck Builder ---");

        for (int i = 0; i < allCards.size(); i++) {
            System.out.println((i+1) + ". " + allCards.get(i));
        }

        System.out.print("Choose card number: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index >= 0 && index < allCards.size()) {
                deck.addCard(allCards.get(index));
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }

        System.out.println(deck);
    }

    private void designArena() {
        System.out.println("\n--- Arena Designer ---");
        arena = new Arena();

        System.out.print("How many bridges (1-3)? ");
        try {
            int count = Integer.parseInt(scanner.nextLine());
            for (int i = 1; i <= count; i++) {
                arena.addBridge("Bridge " + i);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Creating default arena.");
        }

        System.out.println("Arena created:");
        System.out.println(arena);
    }

    private void startGame() {
        System.out.println("\n--- Start Game ---");

        if (deck.getCards().isEmpty()) {
            System.out.println("You must build a deck first!");
            return;
        }

        if (arena == null) {
            System.out.println("You must design an arena first!");
            return;
        }

        System.out.println("Game starting with:");
        System.out.println(deck);
        System.out.println(arena);

        System.out.println("Match not implemented yet.");
        System.out.println("(This will be developed in the next phase.)");
    }
}
