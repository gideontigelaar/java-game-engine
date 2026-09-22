# Java Game Engine
A custom game engine built in Java using LWJGL.

## Getting started
You'll need Java 17+ and Maven installed.

Build and run it on macOS:
```bash
mvn clean package && java -XstartOnFirstThread -jar target/game-engine-1.0-SNAPSHOT.jar
```

Build and run it on Windows/Linux:
```bash
mvn clean package
java -jar target/game-engine-1.0-SNAPSHOT.jar
```

## Notes
Maven pulls in the right native libraries for your OS automatically. Engine dependencies and LWJGL version live in `pom.xml`.