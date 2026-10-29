# Tic-tac-toe
Tic-tac-toe game made with this engine. Play offline against a friend or the AI, or online through the server.

## Running
```bash
mvn clean package -pl games/tictactoe -am
java -jar games/tictactoe/target/tictactoe.jar
```

## How to play
Get three in a row.
- Drag your piece onto the board, or click a free tile.
- Esc goes back.

## Offline
- PLAYER VS PLAYER: two players on one computer.
- 50 / 50 AI: plays a random move half of the time.
- UNBEATABLE AI: never loses.

## Online
1. Enter your name and the server address, then choose LOGIN ONLINE.
2. Choose PLAYER VS PLAYER, or AI VS AI to let the AI play for you.
3. Press FIND MATCH, or CHALLENGE a player. Invites show at the top of the list, press ACCEPT to play.

For a tournament, log in with AI VS AI and wait in the lobby.

## Server
Start the server:
```bash
java -jar gameserver.jar
```

It runs on port 7789 by default. You can keep it in a `server/` folder in the root.