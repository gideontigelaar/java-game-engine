package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.graphics.animation.AnimationSequence;
import nl.team3.engine.graphics.animation.SpriteAnimation;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.Board;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class MatchScene extends BaseScene {
    private static final float BOARD_SCALE = 8f;
    private static final float START_OFFSET_X = 2f * BOARD_SCALE;
    private static final float START_OFFSET_Y = 2f * BOARD_SCALE;
    private static final float STRIDE_X = 13f * BOARD_SCALE;
    private static final float STRIDE_Y = 14f * BOARD_SCALE;
    private static final float CELL_HALF_W = 6f * BOARD_SCALE;
    private static final float CELL_HALF_H = 6.5f * BOARD_SCALE;

    private static final int PIECES_PER_SIDE = 5;
    private static final float GRAB_SCALE = BOARD_SCALE + 1f;
    private static final float STACK_OFFSET_X = 240f;
    private static final float STACK_BASE_OFFSET_Y = 108f;
    private static final float STACK_STRIDE = 4f * BOARD_SCALE;
    private static final float STACK_TEXTURE_HEIGHT = 11f * BOARD_SCALE;

    private static final float WIN_POP_SCALE = BOARD_SCALE + 2f;
    private static final float WIN_POP_TIME = 0.4f;
    private static final float WIN_HIGHLIGHT_PAUSE = 0.6f;
    private static final Vector4f STATUS_COLOR = new Vector4f(1, 1, 0, 1);

    private Sprite boardSprite;
    private Texture boardTexture;
    private final Vector2f boardTopLeft = new Vector2f();
    private Sprite logo;
    private boolean logoVisible;

    private PieceSide xSide;
    private PieceSide oSide;
    private final Sprite[][] boardPieces = new Sprite[Board.SIZE][Board.SIZE];

    // Random player starts
    private final Board board = new Board(ThreadLocalRandom.current().nextBoolean() ? Board.Mark.X : Board.Mark.O);
    private final List<SpriteAnimation> winPops = new ArrayList<>();
    private AnimationSequence winHighlight;
    private float winHighlightDelay;

    private Button btnAction;
    private Button btnPlayAgain;
    private boolean matchEnded = false;
    private String gameStatusMessage = "";
    private float statusY;

    private class PieceSide {
        private final Board.Mark mark;
        private final float direction;
        private final Texture pieceTexture;
        private final Texture stackTexture;
        private final List<Sprite> stack = new ArrayList<>();
        private final List<SpriteAnimation> settling = new ArrayList<>();
        private Sprite active;
        private SpriteAnimation grabAnim;
        private SpriteAnimation releaseAnim;
        private boolean pickedUp = false;

        private PieceSide(Board.Mark mark, float direction, Texture pieceTexture, Texture stackTexture) {
            this.mark = mark;
            this.direction = direction;
            this.pieceTexture = pieceTexture;
            this.stackTexture = stackTexture;

            for (int i = 0; i < PIECES_PER_SIDE; i++) {
                Sprite s = new Sprite(stackTexture);
                s.setScale(BOARD_SCALE);
                s.setOrigin(0.5f, 1f);
                stack.add(s);
            }
            spawnNext();
        }

        private void setupAnimations() {
            grabAnim = SpriteAnimation.builder().sprite(active).scale(new Vector2f(BOARD_SCALE, BOARD_SCALE), new Vector2f(GRAB_SCALE, GRAB_SCALE)).duration(0.3f).easing("ExponentialOut").build();
            releaseAnim = SpriteAnimation.builder().sprite(active).scale(new Vector2f(GRAB_SCALE, GRAB_SCALE), new Vector2f(BOARD_SCALE, BOARD_SCALE)).duration(0.3f).easing("ExponentialOut").build();
        }

        private void spawnNext() {
            pickedUp = false;

            if (stack.isEmpty()) {
                active = null;
                return;
            }

            active = stack.remove(stack.size() - 1);
            active.setTexture(pieceTexture);
            active.setOrigin(0.5f, 0.5f);
            active.setScale(BOARD_SCALE);

            if (grabAnim != null) {
                grabAnim.SetSprite(active);
                releaseAnim.SetSprite(active);
                grabAnim.getTimer().stop();
                grabAnim.getTimer().reset();
                releaseAnim.getTimer().stop();
                releaseAnim.getTimer().reset();
            }

            layout();
        }

        private void pickUp() {
            pickedUp = true;
            grabAnim.startAnimation();
            layout();
        }

        private void putBack() {
            pickedUp = false;
            releaseAnim.startAnimation();
            layout();
        }

        // Rounded like the board, half pixels make scaled sprites shimmer
        private float stackX() {
            return Math.round(currentWidth / 2f) + direction * STACK_OFFSET_X;
        }

        private float levelY(int level) {
            return Math.round(currentHeight / 2f) + STACK_BASE_OFFSET_Y - level * STACK_STRIDE;
        }

        private void snapActiveToStack() {
            active.setPosition(stackX(), levelY(stack.size()));
        }

        private void layout() {
            float centerX = stackX();
            for (int i = 0; i < stack.size(); i++) {
                // Top of the stack shows the full piece once the piece above it is picked up
                boolean exposed = pickedUp && i == stack.size() - 1;
                Sprite piece = stack.get(i);
                piece.setTexture(exposed ? pieceTexture : stackTexture);
                piece.setPosition(centerX, levelY(i) + STACK_TEXTURE_HEIGHT / 2f);
            }
            if (active != null && !pickedUp) {
                snapActiveToStack();
            }
        }

        private void drawResting() {
            for (Sprite s : stack) spriteRenderer.draw(s);
            if (active != null && !pickedUp) spriteRenderer.draw(active);
        }

        private void drawHeld() {
            if (active != null && pickedUp) spriteRenderer.draw(active);
        }
    }

    public MatchScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection) {
        super(input, actions, sceneManager, connection);
    }

    @Override
    protected void onInit(AssetManager assets) {
        System.out.println("Game loaded");

        boardTexture = assets.loadTexture("board", "/textures/tictactoe/board.png");
        boardSprite = new Sprite(boardTexture);
        boardSprite.setScale(BOARD_SCALE);
        logo = createLogo();

        Texture xPieceTexture = assets.loadTexture("xPiece", "/textures/tictactoe/x.png");
        Texture xStackTexture = assets.loadTexture("xStack", "/textures/tictactoe/xStack.png");
        Texture oPieceTexture = assets.loadTexture("oPiece", "/textures/tictactoe/o.png");
        Texture oStackTexture = assets.loadTexture("oStack", "/textures/tictactoe/oStack.png");

        xSide = new PieceSide(Board.Mark.X, -1f, xPieceTexture, xStackTexture);
        oSide = new PieceSide(Board.Mark.O, 1f, oPieceTexture, oStackTexture);

        xSide.setupAnimations();
        oSide.setupAnimations();

        btnAction = createButton(connection != null ? "FORFEIT" : "LEAVE GAME", BUTTON_WIDTH, this::handleGameAction);
        btnPlayAgain = createButton("PLAY AGAIN", BUTTON_WIDTH,
                () -> sceneManager.changeScene(new MatchScene(input, actions, sceneManager, null)));

        if (connection != null) {
            gameStatusMessage = "Match Started!";
        } else {
            updateStatus();
        }
    }

    private Vector2f getGridCenter(int col, int row) {
        float cellTopLeftX = boardTopLeft.x + START_OFFSET_X + (col * STRIDE_X);
        float cellTopLeftY = boardTopLeft.y + START_OFFSET_Y + (row * STRIDE_Y);

        return new Vector2f(cellTopLeftX + CELL_HALF_W, cellTopLeftY + CELL_HALF_H);
    }

    private void handleGameAction() {
        if (connection != null && !matchEnded) {
            connection.sendCommand("forfeit");
        }
        if (connection != null && connection.isConnected()) {
            sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, connection));
        } else if (isPlayAgainShown()) {
            transitionTo(this::createMainMenu, BackgroundRenderer.PALETTE_BLUE, btnPlayAgain, btnAction);
        } else {
            transitionTo(this::createMainMenu, BackgroundRenderer.PALETTE_BLUE, btnAction);
        }
    }

    private MainMenuScene createMainMenu() {
        return new MainMenuScene(input, actions, sceneManager);
    }

    // Offline only, the server decides what happens after an online match
    private boolean isPlayAgainShown() {
        return connection == null && board.isGameOver();
    }

    @Override
    protected void onUpdate(float dt) {
        if (!isTransitioning()) {
            btnAction.update(input);
            if (isPlayAgainShown()) {
                btnPlayAgain.update(input);
            }
        }

        updateSide(xSide, oSide, dt);
        updateSide(oSide, xSide, dt);

        updateSettling(xSide, dt);
        updateSettling(oSide, dt);

        updateWinHighlight(dt);

        if (connection != null) {
            connection.update(msg -> {
                if (msg.startsWith("SVR GAME WIN")) {
                    gameStatusMessage = "YOU WIN! " + extractComment(msg);
                    endMatch();
                } else if (msg.startsWith("SVR GAME LOSS")) {
                    gameStatusMessage = "YOU LOSE! " + extractComment(msg);
                    endMatch();
                } else if (msg.startsWith("SVR GAME DRAW")) {
                    gameStatusMessage = "DRAW! " + extractComment(msg);
                    endMatch();
                } else if (msg.startsWith("SVR GAME YOURTURN")) {
                    gameStatusMessage = "Your turn!";
                }
            });

            if (!connection.isConnected()) {
                sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
            }
        }
    }

    private boolean canPickUp(PieceSide side, PieceSide other) {
        return !other.pickedUp && !isTransitioning() && !matchEnded && !board.isGameOver() && board.getTurn() == side.mark;
    }

    private void updateSide(PieceSide side, PieceSide other, float dt) {
        if (side.active == null) return;

        if (canPickUp(side, other) && input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && side.active.contains(mousePosition)) {
            side.pickUp();
        }

        if (side.pickedUp) {
            if (input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                side.grabAnim.UpdateAnimation(dt);
                side.grabAnim.setScale();
                Vector2f position = side.active.getPosition();
                side.active.setPosition(position.x + (float) input.getMouseDeltaX(), position.y + (float) input.getMouseDeltaY());
            } else {
                dropPiece(side);
            }
        } else {
            side.releaseAnim.UpdateAnimation(dt);
            if (side.releaseAnim.getTimer().isRunning() || side.releaseAnim.getTimer().isFinished()) {
                side.releaseAnim.setScale();
            }
            side.snapActiveToStack();
        }
    }

    private void dropPiece(PieceSide side) {
        float boardW = boardTexture.getWidth() * BOARD_SCALE;
        float boardH = boardTexture.getHeight() * BOARD_SCALE;

        Vector2f dropPos = side.active.getPosition();
        if (dropPos.x >= boardTopLeft.x && dropPos.x <= boardTopLeft.x + boardW
                && dropPos.y >= boardTopLeft.y && dropPos.y <= boardTopLeft.y + boardH) {
            int bestCol = 0;
            int bestRow = 0;
            float bestDist = Float.MAX_VALUE;
            for (int c = 0; c < Board.SIZE; c++) {
                for (int r = 0; r < Board.SIZE; r++) {
                    float dist = dropPos.distanceSquared(getGridCenter(c, r));
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestCol = c;
                        bestRow = r;
                    }
                }
            }

            if (board.canPlace(bestCol, bestRow)) {
                placePiece(side, bestCol, bestRow);
                return;
            }
        }

        side.putBack();
    }

    private void placePiece(PieceSide side, int col, int row) {
        Sprite piece = side.active;
        piece.setPosition(getGridCenter(col, row));
        boardPieces[col][row] = piece;
        board.place(col, row);

        SpriteAnimation settle = SpriteAnimation.builder().sprite(piece).scale(new Vector2f(GRAB_SCALE, GRAB_SCALE), new Vector2f(BOARD_SCALE, BOARD_SCALE)).duration(0.3f).easing("ExponentialOut").build();
        settle.startAnimation();
        side.settling.add(settle);

        side.spawnNext();

        if (board.getWinner() != null) {
            startWinHighlight();
        }
        if (connection == null) {
            updateStatus();
        }
        if (board.isGameOver()) {
            layoutButtons();
        }
    }

    // Winning pieces pop one after another, repeating while the win is shown
    private void startWinHighlight() {
        for (int[] cell : board.getWinningLine()) {
            winPops.add(SpriteAnimation.builder().sprite(boardPieces[cell[0]][cell[1]]).scale(new Vector2f(WIN_POP_SCALE, WIN_POP_SCALE), new Vector2f(BOARD_SCALE, BOARD_SCALE)).duration(WIN_POP_TIME).easing("ExponentialInOut").build());
        }
        winHighlight = new AnimationSequence(winPops);
        winHighlightDelay = WIN_HIGHLIGHT_PAUSE;
    }

    private void updateWinHighlight(float dt) {
        if (winHighlight == null) return;

        if (!winHighlight.isRunning()) {
            winHighlightDelay -= dt;
            if (winHighlightDelay <= 0f) {
                winHighlight.startSequence();
                winHighlightDelay = WIN_HIGHLIGHT_PAUSE;
            }
        }
        winHighlight.update(dt);

        for (SpriteAnimation pop : winPops) {
            pop.setScale();
        }
    }

    private void updateStatus() {
        if (board.getWinner() != null) {
            gameStatusMessage = board.getWinner() + " WINS!";
        } else if (board.isDraw()) {
            gameStatusMessage = "DRAW!";
        } else {
            gameStatusMessage = board.getTurn() + "'S TURN";
        }
    }

    private void updateSettling(PieceSide side, float dt) {
        Iterator<SpriteAnimation> it = side.settling.iterator();
        while (it.hasNext()) {
            SpriteAnimation settle = it.next();
            settle.UpdateAnimation(dt);
            settle.setScale();
            if (settle.getTimer().isFinished()) {
                it.remove();
            }
        }
    }

    private void endMatch() {
        matchEnded = true;
        btnAction.setText("BACK TO LOBBY");
    }

    private String extractComment(String msg) {
        int idx = msg.indexOf("COMMENT: \"");
        if (idx != -1) {
            int endIdx = msg.indexOf("\"", idx + 10);
            if (endIdx != -1) {
                return msg.substring(idx + 10, endIdx);
            }
        }
        return "";
    }

    @Override
    protected void onRender() {
        if (logoVisible) {
            spriteRenderer.draw(logo);
        }
        spriteRenderer.draw(boardSprite);

        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (boardPieces[c][r] != null) spriteRenderer.draw(boardPieces[c][r]);
            }
        }
        xSide.drawResting();
        oSide.drawResting();

        if (!gameStatusMessage.isEmpty()) {
            drawCenteredText(smallFont, gameStatusMessage, statusY, STATUS_COLOR);
        }

        btnAction.render(spriteRenderer, textRenderer);
        if (isPlayAgainShown()) {
            btnPlayAgain.render(spriteRenderer, textRenderer);
        }

        xSide.drawHeld();
        oSide.drawHeld();
    }

    @Override
    protected void onResize(int width, int height) {
        float boardW = boardTexture.getWidth() * BOARD_SCALE;
        float boardH = boardTexture.getHeight() * BOARD_SCALE;
        boardSprite.setPosition(Math.round(width / 2f), Math.round(height / 2f));
        boardTopLeft.set(boardSprite.getPosition().x - boardW / 2f, boardSprite.getPosition().y - boardH / 2f);

        // Logo and status above the board, the logo is dropped when it doesn't fit
        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        float statusHeight = smallFont.getCapHeight();
        logoVisible = logoHeight + UI_SPACING + statusHeight + 2 * UI_SPACING <= boardTopLeft.y;
        float headerHeight = logoVisible ? logoHeight + UI_SPACING + statusHeight : statusHeight;
        float headerY = Math.round((boardTopLeft.y - headerHeight) / 2f);
        logo.setPosition(Math.round(width / 2f), headerY + logoHeight / 2f);
        statusY = logoVisible ? headerY + logoHeight + UI_SPACING : headerY;

        layoutButtons();

        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (boardPieces[c][r] != null) boardPieces[c][r].setPosition(getGridCenter(c, r));
            }
        }

        xSide.layout();
        oSide.layout();
    }

    // Buttons centered below the board, side by side once the game is over
    private void layoutButtons() {
        float boardBottom = boardTopLeft.y + boardTexture.getHeight() * BOARD_SCALE;
        float y = Math.round(boardBottom + (currentHeight - boardBottom - BUTTON_HEIGHT) / 2f);
        float centerX = Math.round(currentWidth / 2f);

        if (isPlayAgainShown()) {
            btnPlayAgain.setPosition(centerX - UI_SPACING / 2f - BUTTON_WIDTH, y);
            btnAction.setPosition(centerX + UI_SPACING / 2f, y);
        } else {
            btnAction.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        }
    }
}