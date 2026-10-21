package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.Board;
import nl.team3.games.tictactoe.network.ServerConnection;
import nl.team3.games.tictactoe.network.ServerMessage;

public class OnlineMatchScene extends MatchScene {
    private static final String FORFEITED = "Player forfeited match";
    private static final String DISCONNECTED = "Client disconnected";

    private final String playerName;
    private final String opponentName;
    private final Board.Mark playerMark;
    private final Board.Mark opponentMark;

    private boolean yourTurn;
    private boolean forfeitSent;
    private boolean matchEnded;
    private Board.Mark winner;
    private String result;

    public OnlineMatchScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection,
                            String playerName, String opponentName, boolean playerStarts) {
        super(input, actions, sceneManager, connection, Board.Mark.X, playerStarts ? Board.Mark.X : Board.Mark.O, null, null);
        this.playerName = playerName;
        this.opponentName = opponentName.toUpperCase();
        this.playerMark = playerStarts ? Board.Mark.X : Board.Mark.O;
        this.opponentMark = playerStarts ? Board.Mark.O : Board.Mark.X;
    }

    @Override
    protected void onInit(AssetManager assets) {
        super.onInit(assets);
        btnAction.setText("FORFEIT");
    }

    @Override
    protected void onUpdate(float dt) {
        super.onUpdate(dt);

        ServerMessage message;
        while (!isTransitioning() && (message = connection.poll()) != null) {
            handleMessage(message);
        }

        if (!isTransitioning() && !connection.isConnected()) {
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager, "Connection lost!"));
        }
    }

    private void handleMessage(ServerMessage message) {
        switch (message.type()) {
            case "SVR GAME YOURTURN" -> yourTurn = true;
            case "SVR GAME MOVE" -> applyMove(message);
            case "SVR GAME WIN", "SVR GAME LOSS", "SVR GAME DRAW" -> endMatch(message);
            default -> { }
        }
    }

    private void applyMove(ServerMessage message) {
        if (message.get("PLAYER").equalsIgnoreCase(playerName)) return;

        int cell = parseCell(message.get("MOVE"));
        if (cell < 0) return;

        int col = cell % Board.SIZE;
        int row = cell / Board.SIZE;
        if (board.getTurn() == opponentMark && board.canPlace(col, row)) {
            playMove(col, row);
        }
    }

    private static int parseCell(String move) {
        try {
            int cell = Integer.parseInt(move.trim());
            return cell >= 0 && cell < Board.SIZE * Board.SIZE ? cell : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    protected void onMove(int col, int row) {
        yourTurn = false;
        connection.sendCommand("move " + (row * Board.SIZE + col));
    }

    @Override
    protected boolean canMove(Board.Mark mark) {
        return mark == playerMark && yourTurn;
    }

    @Override
    protected boolean isMatchOver() {
        return matchEnded;
    }

    private void endMatch(ServerMessage message) {
        matchEnded = true;
        yourTurn = false;
        if (message.is("SVR GAME WIN")) {
            winner = playerMark;
        } else if (message.is("SVR GAME LOSS")) {
            winner = opponentMark;
        }
        result = resultText(message);
        btnAction.setText("LEAVE GAME");
        finishMatch();
    }

    private String resultText(ServerMessage message) {
        String comment = message.get("COMMENT");
        boolean won = message.is("SVR GAME WIN");
        if (comment.equals(FORFEITED)) {
            return won ? opponentName + " FORFEITED, YOU WIN!" : "YOU FORFEITED!";
        }
        if (comment.equals(DISCONNECTED)) {
            return opponentName + " LEFT, YOU WIN!";
        }

        String text = message.is("SVR GAME DRAW") ? "DRAW!" : won ? "YOU WIN!" : "YOU LOSE!";
        return comment.isEmpty() ? text : text + " " + comment.toUpperCase();
    }

    @Override
    protected String getStatus() {
        if (result != null) {
            return result;
        }
        if (board.getWinner() != null) {
            return board.getWinner() == playerMark ? "YOU WIN!" : "YOU LOSE!";
        }
        if (board.isDraw()) {
            return "DRAW!";
        }
        return board.getTurn() == playerMark ? "YOUR TURN" : opponentName + "'S TURN";
    }

    @Override
    protected String getPlayerName(Board.Mark mark) {
        return mark == playerMark ? "YOU" : opponentName;
    }

    @Override
    protected int getPalette() {
        if (!matchEnded) {
            return super.getPalette();
        }
        return winner == null ? BackgroundRenderer.PALETTE_BLUE : paletteOf(winner);
    }

    @Override
    protected void leave() {
        if (!matchEnded && !forfeitSent) {
            connection.sendCommand("forfeit");
            forfeitSent = true;
            return;
        }
        leaveTo(new LobbyScene(input, actions, sceneManager, connection, playerName, false));
    }

    @Override
    protected void playAgain() {
        leaveTo(new LobbyScene(input, actions, sceneManager, connection, playerName, true));
    }
}