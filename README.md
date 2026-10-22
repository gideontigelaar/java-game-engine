# Java Game Engine
A custom game engine built in Java using LWJGL.

## Getting started
You'll need Java 17+ and Maven installed.

Build and run it on macOS:
```bash
mvn clean package && java -XstartOnFirstThread -jar games/tictactoe/target/tictactoe.jar
```

Build and run it on Windows/Linux:
```bash
mvn clean package
java -jar games/tictactoe/target/tictactoe.jar
```

Build only one game and the engine:
```bash
mvn clean package -pl games/tictactoe -am
```

## Notes
Maven pulls in the right native libraries for your OS automatically. Engine dependencies live in `engine/pom.xml`, each game is its own module in `games/`, and shared versions like LWJGL live in the root `pom.xml`.