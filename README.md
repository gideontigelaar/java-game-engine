# Java Game Engine
A custom game engine built in Java using LWJGL.

## Project structure
- `engine/`: the game engine itself
- `games/tictactoe/`: Tic-tac-toe game, built using the engine

## Getting started
You'll need Java 17+ and Maven installed. Run all commands from the root of the repository.

## Building
Maven works the same on macOS, Windows and Linux, these commands are identical everywhere.

Build everything:
```bash
mvn clean package
```

```bash
mvn clean package -pl games/tictactoe -am
```

Build a jar that runs on every OS, with the native libraries for Windows, Linux and macOS included:
```bash
mvn clean package -DallNatives
```

## Running
macOS:
```bash
java -XstartOnFirstThread -jar games/tictactoe/target/tictactoe.jar
```

Windows/Linux:
```bash
java -jar games/tictactoe/target/tictactoe.jar
```

## Notes
Maven pulls in the right native libraries for your OS automatically, or for every OS with `-DallNatives`. Engine dependencies live in `engine/pom.xml`, each game is its own module in `games/`, and shared versions like LWJGL live in the root `pom.xml`.

Each module builds into its own `target/` folder. The playable game is located in `games/tictactoe/target/tictactoe.jar` after building.

Each module keeps its resources under a folder that matches its package.