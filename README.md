# Java Game Engine
A custom game engine built in Java using LWJGL.

## Project structure
- `engine/`: the engine itself
- `games/tictactoe/`: Tic-tac-toe, see [its README](games/tictactoe/README.md)

## Requirements
- Java 17 or newer
- Maven 3.6 or newer

## Building
Run all commands from the root of the repository.

Build everything:
```bash
mvn clean package
```

Build only tic-tac-toe:
```bash
mvn clean package -pl games/tictactoe -am
```

Build one jar for Windows, Linux and macOS:
```bash
mvn clean package -DallNatives
```

The game is built to `games/tictactoe/target/tictactoe.jar`.

## Running
```bash
java -jar games/tictactoe/target/tictactoe.jar
```

On macOS, add `-XstartOnFirstThread`:
```bash
java -XstartOnFirstThread -jar games/tictactoe/target/tictactoe.jar
```

## Notes
- Engine dependencies are in `engine/pom.xml`, shared versions like LWJGL in the root `pom.xml`.
- Each game is its own module in `games/`.
- Each module keeps its resources in a folder that matches its package.