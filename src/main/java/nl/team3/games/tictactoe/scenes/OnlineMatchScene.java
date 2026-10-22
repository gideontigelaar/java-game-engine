package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.games.tictactoe.AIOpponent;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.Board;
import nl.team3.games.tictactoe.network.OnlineSession;
import nl.team3.games.tictactoe.network.ServerMessage;

import java.util.Locale;

public class OnlineMatchScene extends MatchScene {
    private static final String FORFEITED = "Player forfeited";
    private static final String DISCONNECTED = "disconnected";
    private static final float TURN_TIME_LIMIT = 10f;

    private final OnlineSession session;
    private final String opponentName;
    private final Board.Mark playerMark;
    private final boolean aiPlays;

    private boolean yourTurn;
    private float turnTime;
    private boolean forfeitSent;
    private boolean matchEnded;
    private Board.Mark winner;
    private String result;

    private OnlineMatchScene(InputManager input, ActionMap actions, SceneManager sceneManager, OnlineSession session,
                             String opponent, Board.Mark playerMark, AIOpponent ai) {
        super(input, actions, sceneManager, Board.Mark.X, playerMark, ai, playerMark);
        this.session = session;
        this.opponentName = OnlineSession.displayName(opponent);
        this.playerMark = playerMark;
        this.aiPlays = ai != null;
    }

    static OnlineMatchScene create(BaseScene from, OnlineSession session, ServerMessage match) {
        Board.Mark playerMark = session.isMe(match.get("PLAYERTOMOVE")) ? Board.Mark.X : Board.Mark.O;
        AIOpponent ai = session.getRoom() == OnlineSession.Room.AI_VS_AI
                ? new AIOpponent(AIOpponent.Difficulty.UNBEATABLE) : null;
        return new OnlineMatchScene(from.input, from.actions, from.sceneManager, session, match.get("OPPONENT"), playerMark, ai);
    }

    @Override
    protected void onInit(AssetManager assets) {
        super.onInit(assets);
        btnAction.setText("FORFEIT");
    }

    @Override
    protected void onUpdate(float dt) {
        super.onUpdate(dt);
        if (yourTurn) {
            turnTime += dt;
        }

        ServerMessage message;
        while (!isTransitioning() && (message = session.poll()) != null) {
            handleMessage(message);
        }

        if (!isTransitioning() && !session.isConnected()) {
            leaveTo(new OnlineMenuScene(input, actions, sceneManager, session.getAddress(), "Connection lost!"));
        }
    }

    private void handleMessage(ServerMessage message) {
        switch (message.type()) {
            case "SVR GAME YOURTURN" -> {
                yourTurn = true;
                turnTime = 0f;
            }
            case "SVR GAME MOVE" -> applyMove(message);
            case "SVR GAME WIN", "SVR GAME LOSS", "SVR GAME DRAW" -> endMatch(message);
            case "SVR GAME MATCH" -> startNextMatch(message);
            default -> { }
        }
    }

    private void startNextMatch(ServerMessage match) {
        if (session.isOurGame(match)) {
            leaveTo(create(this, session, match));
        } else {
            session.forfeit();
        }
    }

    private void applyMove(ServerMessage message) {
        if (session.isMe(message.get("PLAYER"))) return;

        int cell = parseCell(message.get("MOVE"));
        if (cell < 0) return;

        int col = cell % Board.SIZE;
        int row = cell / Board.SIZE;
        if (board.getTurn() == playerMark.opponent() && board.canPlace(col, row)) {
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
        session.move(row * Board.SIZE + col);
    }

    @Override
    protected boolean canMove(Board.Mark mark) {
        return mark == playerMark && yourTurn;
    }

    @Override
    protected boolean isMatchOver() {
        return matchEnded;
    }

    @Override
    protected boolean canPlayAgain() {
        return !aiPlays;
    }

    private void endMatch(ServerMessage message) {
        matchEnded = true;
        yourTurn = false;
        if (message.is("SVR GAME WIN")) {
            winner = playerMark;
        } else if (message.is("SVR GAME LOSS")) {
            winner = playerMark.opponent();
        }
        result = resultText(message);
        btnAction.setText("LEAVE GAME");
        finishMatch();
    }

    private String resultText(ServerMessage message) {
        String comment = message.get("COMMENT");
        boolean won = message.is("SVR GAME WIN");
        if (comment.startsWith(FORFEITED)) {
            return won ? opponentName + " FORFEITED, YOU WIN!" : "YOU FORFEITED!";
        }
        if (comment.endsWith(DISCONNECTED)) {
            return opponentName + " LEFT, YOU WIN!";
        }

        String text = message.is("SVR GAME DRAW") ? "DRAW!" : won ? "YOU WIN!" : "YOU LOSE!";
        return comment.isEmpty() ? text : text + " " + comment.toUpperCase(Locale.ROOT);
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
        if (board.getTurn() != playerMark) {
            return opponentName + "'S TURN";
        }
        if (aiPlays) {
            return "AI IS THINKING...";
        }
        if (!yourTurn) {
            return "YOUR TURN";
        }
        int secondsLeft = (int) Math.ceil(Math.max(0f, TURN_TIME_LIMIT - turnTime));
        return "YOUR TURN: " + secondsLeft;
    }

    @Override
    protected String getPlayerName(Board.Mark mark) {
        if (mark != playerMark) {
            return opponentName;
        }
        return aiPlays ? "YOUR AI" : "YOU";
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
            session.forfeit();
            forfeitSent = true;
            return;
        }
        leaveTo(new LobbyScene(input, actions, sceneManager, session, false));
    }

    @Override
    protected void playAgain() {
        leaveTo(new LobbyScene(input, actions, sceneManager, session, true));
    }
}