import java.util.ArrayList;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<>();

    public boolean addCard(Card card) {
        if (cards.size() >= 4) { // simplified for now
            System.out.println("Deck full!");
            return false;
        }
        cards.add(card);
        return true;
    }

    public List<Card> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        String result = "Deck:\n";
        for (Card c : cards) {
            result += "- " + c + "\n";
        }
        return result;
    }
}
