import java.util.ArrayList;
import java.util.List;

public class Arena {

    private List<String> towers = new ArrayList<>();
    private List<String> bridges = new ArrayList<>();

    public Arena() {
        // default towers
        towers.add("Player King Tower");
        towers.add("Player Left Crown Tower");
        towers.add("Player Right Crown Tower");

        towers.add("Enemy King Tower");
        towers.add("Enemy Left Crown Tower");
        towers.add("Enemy Right Crown Tower");
    }

    public void addBridge(String name) {
        if (bridges.size() >= 3) {
            System.out.println("Maximum 3 bridges allowed.");
            return;
        }
        bridges.add(name);
    }

    public List<String> getTowers() {
        return towers;
    }

    public List<String> getBridges() {
        return bridges;
    }

    @Override
    public String toString() {
        return "Arena:\nTowers: " + towers + "\nBridges: " + bridges;
    }
}
