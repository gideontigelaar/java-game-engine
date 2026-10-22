# Java Game Engine
A custom game engine built in Java using LWJGL.

## Project structure
- `engine/`: the engine itself (window, rendering, input, UI and animations)
- `games/tictactoe/`: Tic-tac-toe, built on top of the engine

## Getting started
You'll need Java 17+ and Maven installed. Run all commands from the root of the repository.

## Building
Maven works the same on macOS, Windows and Linux, so these commands are identical everywhere.

Build everything:
```bash
mvn clean package
```

Build only the engine:
```bash
mvn clean package -pl engine
```

Build only Tic-tac-toe (this also builds the engine, because the game needs it):
```bash
mvn clean package -pl games/tictactoe -am
```

## Running
Run Tic-tac-toe after building it.

macOS:
```bash
java -XstartOnFirstThread -jar games/tictactoe/target/tictactoe.jar
```

Windows/Linux:
```bash
java -jar games/tictactoe/target/tictactoe.jar
```

## Notes
Maven pulls in the right native libraries for your OS automatically. Engine dependencies live in `engine/pom.xml`, each game is its own module in `games/`, and shared versions like LWJGL live in the root `pom.xml`.

Each module builds into its own `target/` folder. `engine/target/engine.jar` is the engine as a library and can't be run on its own. `games/tictactoe/target/tictactoe.jar` is the playable game with the engine included.