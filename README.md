# kuroyaledemo1
Input Output based demo just for the game logic.

## How to Run

### Prerequisites
- Maven (installed automatically or via `brew install maven`)
- Java 21+

### Run GUI (ClashRoyaleFX)
```bash
mvn javafx:run
```

### Run CLI (Main)
```bash
mvn exec:java -Dexec.mainClass="Main"
```

## Design Patterns

The project utilizes several key design patterns to ensure modularity and maintainability:

1.  **Singleton Pattern**:
    *   Used in `GameController` to manage central game state.
    *   Used in `PersistenceManager` for handling file I/O operations.
    *   Used in `QuestManager` for tracking achievements and quests.

2.  **Factory Pattern**:
    *   `UnitFactory` encapsulates the logic for creating game units.
    *   `BuildingFactory` handles the creation of building objects.

3.  **Strategy Pattern**:
    *   The `Challenge` interface defines a strategy for different game modes.
    *   Implementations like `TankRushChallenge` and `NoBuildingsChallenge` provide specific rulesets that can be swapped dynamically.

4.  **Observer Pattern**:
    *   `QuestManager` allows listeners (via `AchievementListener`) to subscribe to achievement completion events.
