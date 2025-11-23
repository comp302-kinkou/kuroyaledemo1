public class Card {
    private String name;
    private int elixirCost;

    public Card(String name, int elixirCost) {
        this.name = name;
        this.elixirCost = elixirCost;
    }

    public String getName() {
        return name;
    }

    public int getElixirCost() {
        return elixirCost;
    }

    @Override
    public String toString() {
        return name + " (cost " + elixirCost + ")";
    }
}
