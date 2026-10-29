package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.EngineContext;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.logic.AIOpponent;
import nl.team3.games.tictactoe.logic.Board;
import nl.team3.games.tictactoe.network.OnlineSession;
import nl.team3.games.tictactoe.network.ServerMessage;
import org.joml.Vector4f;

import java.util.Locale;

public class OnlineMatchScene extends MatchScene {
    private static final String FORFEITED = "player forfeited";
    private static final String DISCONNECTED = "disconnected";
    private static final String TIME_LIMIT = "time";
    private static final String ILLEGAL_MOVE = "illegal";
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
    private String resultDetail;

    private OnlineMatchScene(EngineContext context, OnlineSession session,
                             String opponent, Board.Mark playerMark, AIOpponent ai) {
        super(context, Board.Mark.X, playerMark, ai, playerMark);
        this.session = session;
        this.opponentName = OnlineSession.displayName(opponent);
        this.playerMark = playerMark;
        this.aiPlays = ai != null;
    }

    static OnlineMatchScene create(EngineContext context, OnlineSession session, ServerMessage match) {
        Board.Mark playerMark = session.isMe(match.get("PLAYERTOMOVE")) ? Board.Mark.X : Board.Mark.O;
        AIOpponent ai = session.getMode() == OnlineSession.Mode.AI_VS_AI
                ? new AIOpponent(AIOpponent.Difficulty.UNBEATABLE) : null;
        return new OnlineMatchScene(context, session, match.get("OPPONENT"), playerMark, ai);
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

        if (!isLeaving() && !session.isConnected()) {
            leaveTo(new OnlineMenuScene(context, session.getAddress(), "Lost connection to the server!"));
        }
    }

    private void handleMessage(ServerMessage message) {
        switch (message.type()) {
            case "SVR GAME YOURTURN" -> {
                yourTurn = true;
                turnTime = message.getAge();
            }
            case "SVR GAME MOVE" -> applyMove(message);
            case "SVR GAME WIN", "SVR GAME LOSS", "SVR GAME DRAW" -> endMatch(message);
            case "SVR GAME MATCH" -> startNextMatch(message);
            default -> { }
        }
    }

    private void startNextMatch(ServerMessage match) {
        if (session.isOurGame(match)) {
            leaveTo(create(context, session, match));
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
        boolean won = message.is("SVR GAME WIN");
        boolean lost = message.is("SVR GAME LOSS");
        if (won) {
            winner = playerMark;
        } else if (lost) {
            winner = playerMark.opponent();
        }
        result = won ? "YOU WIN!" : lost ? "YOU LOSE!" : "DRAW!";
        resultDetail = resultDetail(message.get("COMMENT"), won, lost);
        btnAction.setText("LEAVE GAME");
        finishMatch();
    }

    private String resultDetail(String comment, boolean won, boolean lost) {
        String reason = comment.toLowerCase(Locale.ROOT);
        if (reason.startsWith(FORFEITED)) {
            return won ? opponentName + " FORFEITED" : "YOU FORFEITED";
        }
        if (reason.endsWith(DISCONNECTED)) {
            return won ? opponentName + " LEFT THE GAME" : "YOU LEFT THE GAME";
        }
        if (reason.contains(TIME_LIMIT)) {
            return won ? opponentName + " RAN OUT OF TIME" : "YOU RAN OUT OF TIME";
        }
        if (reason.contains(ILLEGAL_MOVE)) {
            return won ? opponentName + " MADE AN ILLEGAL MOVE" : "ILLEGAL MOVE";
        }
        if (!comment.isBlank()) {
            return comment.toUpperCase(Locale.ROOT);
        }
        if (won || lost) {
            return won ? "THREE IN A ROW!" : opponentName + " GOT THREE IN A ROW";
        }
        return "NO MOVES LEFT";
    }

    private Board.Mark getWinner() {
        return matchEnded ? winner : board.getWinner();
    }

    @Override
    protected String getStatus() {
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
    protected String getResultTitle() {
        if (result != null) {
            return result;
        }
        if (getWinner() == null) {
            return "DRAW!";
        }
        return getWinner() == playerMark ? "YOU WIN!" : "YOU LOSE!";
    }

    @Override
    protected String getResultDetail() {
        if (resultDetail != null) {
            return resultDetail;
        }
        if (getWinner() == null) {
            return "NO MOVES LEFT";
        }
        return getWinner() == playerMark ? "THREE IN A ROW!" : opponentName + " GOT THREE IN A ROW";
    }

    @Override
    protected Vector4f getResultColor() {
        if (getWinner() == null) {
            return WHITE;
        }
        return getWinner() == playerMark ? WIN_COLOR : LOSE_COLOR;
    }

    @Override
    protected boolean isCelebrating() {
        return getWinner() == playerMark;
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
        leaveTo(new LobbyScene(context, session, false));
    }

    @Override
    protected void playAgain() {
        leaveTo(new LobbyScene(context, session, true));
    }
}