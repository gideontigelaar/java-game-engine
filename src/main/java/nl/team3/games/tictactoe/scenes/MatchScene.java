package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.graphics.animation.SpriteAnimation;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.AIOpponent;
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
    private static final float MOVE_TIME = 0.3f;
    private static final float PLAY_MOVE_TIME = 0.5f;
    private static final float STACK_OFFSET_X = 240f;
    private static final float STACK_BASE_OFFSET_Y = 108f;
    private static final float STACK_STRIDE = 4f * BOARD_SCALE;
    private static final float STACK_TEXTURE_HEIGHT = 11f * BOARD_SCALE;
    private static final float BOB_HEIGHT = BOARD_SCALE;
    private static final float BOB_TIME = 1.2f;

    private static final float WIN_DELAY = 0.4f;
    private static final float WIN_HOP_TIME = 0.4f;
    private static final float WIN_HOP_GAP = 0.15f;
    private static final float WIN_REPEAT_TIME = 1.6f;
    private static final float WIN_HOP_HEIGHT = 3f * BOARD_SCALE;
    private static final float FADE_ALPHA = 0.35f;
    private static final float FADE_TIME = 0.4f;
    private static final float AI_MOVE_DELAY = 0.5f;
    private static final Vector4f TEXT_COLOR = new Vector4f(0f, 0.157f, 0.349f, 1f);

    protected final Board board;
    private final Board.Mark firstTurn;
    private final Board.Mark leftMark;
    private final AIOpponent ai;
    private final Board.Mark aiMark;
    private float aiMoveTimer;

    private Sprite boardSprite;
    private Texture boardTexture;
    private final Vector2f boardTopLeft = new Vector2f();
    private Sprite logo;
    private boolean logoVisible;

    private PieceSide xSide;
    private PieceSide oSide;
    private final Sprite[][] boardPieces = new Sprite[Board.SIZE][Board.SIZE];
    private final List<SpriteAnimation> settling = new ArrayList<>();

    private int[][] winningLine;
    private float winTime;
    private Animation fadeAnim;

    protected Button btnAction;
    private Button btnPlayAgain;
    private float statusY;
    private float nameY;

    private class PieceSide {
        private final Board.Mark mark;
        private final float direction;
        private final Texture pieceTexture;
        private final Texture stackTexture;
        private final List<Sprite> stack = new ArrayList<>();
        private Sprite active;
        private SpriteAnimation grabAnim;
        private SpriteAnimation returnAnim;
        private boolean pickedUp = false;
        private float bobTime;

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

        private void spawnNext() {
            pickedUp = false;
            returnAnim = null;
            bobTime = 0f;

            if (stack.isEmpty()) {
                active = null;
                return;
            }

            active = stack.remove(stack.size() - 1);
            active.setTexture(pieceTexture);
            active.setOrigin(0.5f, 0.5f);
            active.setScale(BOARD_SCALE);

            layout();
        }

        private void pickUp() {
            pickedUp = true;
            returnAnim = null;
            grabAnim = SpriteAnimation.builder().sprite(active).scale(new Vector2f(active.getScale()), new Vector2f(GRAB_SCALE, GRAB_SCALE)).duration(MOVE_TIME).easing("ExponentialOut").build();
            grabAnim.startAnimation();
            layout();
        }

        private void putBack() {
            pickedUp = false;
            bobTime = 0f;
            returnAnim = SpriteAnimation.builder().sprite(active)
                    .position(new Vector2f(active.getPosition()), new Vector2f(stackX(), levelY(stack.size())))
                    .scale(new Vector2f(active.getScale()), new Vector2f(BOARD_SCALE, BOARD_SCALE))
                    .duration(MOVE_TIME).easing("ExponentialOut").build();
            returnAnim.startAnimation();
            layout();
        }

        private float stackX() {
            return Math.round(currentWidth / 2f) + direction * STACK_OFFSET_X;
        }

        private float levelY(int level) {
            return Math.round(currentHeight / 2f) + STACK_BASE_OFFSET_Y - level * STACK_STRIDE;
        }

        private void snapActiveToStack() {
            float bob = BOB_HEIGHT * (1f - (float) Math.cos(2 * Math.PI * bobTime / BOB_TIME)) / 2f;
            active.setPosition(stackX(), levelY(stack.size()) - Math.round(bob));
        }

        private void layout() {
            float centerX = stackX();
            for (int i = 0; i < stack.size(); i++) {
                boolean exposed = (pickedUp || returnAnim != null) && i == stack.size() - 1;
                Sprite piece = stack.get(i);
                piece.setTexture(exposed ? pieceTexture : stackTexture);
                piece.setPosition(centerX, levelY(i) + STACK_TEXTURE_HEIGHT / 2f);
            }
            if (active != null && !pickedUp && returnAnim == null) {
                snapActiveToStack();
            }
        }

        private void setAlpha(float alpha) {
            for (Sprite s : stack) s.setAlpha(alpha);
            if (active != null) active.setAlpha(alpha);
        }

        private void drawResting() {
            for (Sprite s : stack) spriteRenderer.draw(s);
            if (active != null && !pickedUp && returnAnim == null) spriteRenderer.draw(active);
        }

        private void drawMoving() {
            if (active != null && (pickedUp || returnAnim != null)) spriteRenderer.draw(active);
        }
    }

    public MatchScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        this(input, actions, sceneManager, null, ThreadLocalRandom.current().nextBoolean() ? Board.Mark.X : Board.Mark.O,
                Board.Mark.X, null, null);
    }

    // Against the computer, which plays O on the right
    public MatchScene(InputManager input, ActionMap actions, SceneManager sceneManager, AIOpponent ai) {
        this(input, actions, sceneManager, null, Board.Mark.X, Board.Mark.X, ai, Board.Mark.O);
    }

    protected MatchScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerConnection connection,
                         Board.Mark firstTurn, Board.Mark leftMark, AIOpponent ai, Board.Mark aiMark) {
        super(input, actions, sceneManager, connection);
        this.board = new Board(firstTurn);
        this.firstTurn = firstTurn;
        this.leftMark = leftMark;
        this.ai = ai;
        this.aiMark = aiMark;
    }

    @Override
    protected void onInit(AssetManager assets) {
        boardTexture = assets.loadTexture("board", "/textures/tictactoe/board.png");
        boardSprite = new Sprite(boardTexture);
        boardSprite.setScale(BOARD_SCALE);
        logo = createLogo();

        Texture xPieceTexture = assets.loadTexture("xPiece", "/textures/tictactoe/x.png");
        Texture xStackTexture = assets.loadTexture("xStack", "/textures/tictactoe/xStack.png");
        Texture oPieceTexture = assets.loadTexture("oPiece", "/textures/tictactoe/o.png");
        Texture oStackTexture = assets.loadTexture("oStack", "/textures/tictactoe/oStack.png");

        xSide = new PieceSide(Board.Mark.X, leftMark == Board.Mark.X ? -1f : 1f, xPieceTexture, xStackTexture);
        oSide = new PieceSide(Board.Mark.O, leftMark == Board.Mark.O ? -1f : 1f, oPieceTexture, oStackTexture);

        btnAction = createButton("LEAVE GAME", BUTTON_WIDTH, this::leave);
        btnPlayAgain = createButton("PLAY AGAIN", BUTTON_WIDTH, this::playAgain);
    }

    protected boolean canMove(Board.Mark mark) {
        return true;
    }

    private boolean isHuman(Board.Mark mark) {
        return ai == null || mark != aiMark;
    }

    protected void onMove(int col, int row) {
    }

    protected boolean isMatchOver() {
        return board.isGameOver();
    }

    protected String getStatus() {
        Board.Mark winner = board.getWinner();
        if (winner != null) {
            if (ai == null) return winner + " WINS!";
            return winner == aiMark ? "AI WINS!" : "YOU WIN!";
        }
        if (board.isDraw()) {
            return "DRAW!";
        }
        if (ai == null) {
            return board.getTurn() + "'S TURN";
        }
        return board.getTurn() == aiMark ? "AI'S TURN" : "YOUR TURN";
    }

    protected String getPlayerName(Board.Mark mark) {
        if (ai == null) return null;
        return mark == aiMark ? "AI" : "YOU";
    }

    protected void leave() {
        leaveTo(new OfflineMenuScene(input, actions, sceneManager));
    }

    // Take turns starting
    protected void playAgain() {
        leaveTo(new MatchScene(input, actions, sceneManager, null, firstTurn.opponent(), leftMark, ai, aiMark));
    }

    protected void leaveTo(BaseScene scene) {
        if (isMatchOver()) {
            transitionTo(scene, btnPlayAgain, btnAction);
        } else {
            transitionTo(scene, btnAction);
        }
    }

    @Override
    protected int getPalette() {
        return board.isDraw() ? BackgroundRenderer.PALETTE_BLUE : paletteOf(board.getTurn());
    }

    protected static int paletteOf(Board.Mark mark) {
        return mark == Board.Mark.X ? BackgroundRenderer.PALETTE_RED : BackgroundRenderer.PALETTE_GREEN;
    }

    private Vector2f getGridCenter(int col, int row) {
        float cellTopLeftX = boardTopLeft.x + START_OFFSET_X + (col * STRIDE_X);
        float cellTopLeftY = boardTopLeft.y + START_OFFSET_Y + (row * STRIDE_Y);

        return new Vector2f(cellTopLeftX + CELL_HALF_W, cellTopLeftY + CELL_HALF_H);
    }

    private PieceSide sideOf(Board.Mark mark) {
        return mark == Board.Mark.X ? xSide : oSide;
    }

    @Override
    protected void onUpdate(float dt) {
        if (!isTransitioning()) {
            btnAction.update(input);
            if (isMatchOver()) {
                btnPlayAgain.update(input);
            }
        }

        updateAI(dt);
        updateSide(xSide, oSide, dt);
        updateSide(oSide, xSide, dt);
        updateSettling(dt);
        updateGameOver(dt);
    }

    private void updateAI(float dt) {
        if (ai == null || isTransitioning() || isMatchOver() || board.getTurn() != aiMark || !canMove(aiMark)) {
            aiMoveTimer = 0f;
            return;
        }

        aiMoveTimer += dt;
        if (aiMoveTimer < AI_MOVE_DELAY) return;

        aiMoveTimer = 0f;
        int[] move = ai.chooseMove(board);
        if (move != null) {
            playMove(move[0], move[1]);
            onMove(move[0], move[1]);
        }
    }

    private boolean isPlayable(PieceSide side) {
        return !isTransitioning() && !isMatchOver() && !board.isGameOver() && board.getTurn() == side.mark
                && canMove(side.mark) && isHuman(side.mark);
    }

    private void updateSide(PieceSide side, PieceSide other, float dt) {
        if (side.active == null) return;

        if (side.pickedUp && !isPlayable(side)) {
            side.putBack();
        }
        if (isPlayable(side) && !other.pickedUp && input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT) && side.active.contains(mousePosition)) {
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
        } else if (side.returnAnim != null) {
            side.returnAnim.UpdateAnimation(dt);
            side.returnAnim.setTransformation();
            if (side.returnAnim.isFinished()) {
                side.returnAnim = null;
                side.layout();
            }
        } else {
            side.bobTime = isPlayable(side) ? side.bobTime + dt : 0f;
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
                placePiece(side, bestCol, bestRow, MOVE_TIME);
                onMove(bestCol, bestRow);
                return;
            }
        }

        side.putBack();
    }

    protected void playMove(int col, int row) {
        PieceSide side = sideOf(board.getTurn());
        if (side.active != null) {
            side.active.setScale(GRAB_SCALE);
            placePiece(side, col, row, PLAY_MOVE_TIME);
        }
    }

    private void placePiece(PieceSide side, int col, int row, float moveTime) {
        Sprite piece = side.active;
        boardPieces[col][row] = piece;
        board.place(col, row);

        SpriteAnimation settle = SpriteAnimation.builder().sprite(piece)
                .position(new Vector2f(piece.getPosition()), getGridCenter(col, row))
                .scale(new Vector2f(piece.getScale()), new Vector2f(BOARD_SCALE, BOARD_SCALE))
                .duration(moveTime).easing("ExponentialOut").build();
        settle.startAnimation();
        settling.add(settle);

        side.spawnNext();

        if (board.isGameOver()) {
            finishMatch();
        } else {
            PieceSide next = sideOf(board.getTurn());
            updatePalette(next.stackX(), next.levelY(next.stack.size()));
        }
    }

    protected void finishMatch() {
        if (fadeAnim == null) {
            winningLine = board.getWinningLine();
            fadeAnim = new Animation(1f, FADE_ALPHA, "ExponentialOut", FADE_TIME);
            fadeAnim.startAnimation();
        }
        layoutButtons();
        updatePalette(boardSprite.getPosition().x, boardSprite.getPosition().y);
    }

    private void updateSettling(float dt) {
        Iterator<SpriteAnimation> it = settling.iterator();
        while (it.hasNext()) {
            SpriteAnimation settle = it.next();
            settle.UpdateAnimation(dt);
            settle.setTransformation();
            if (settle.isFinished()) {
                it.remove();
            }
        }
    }

    private void updateGameOver(float dt) {
        if (fadeAnim == null) return;

        fadeAnim.UpdateAnimation(dt);
        float alpha = fadeAnim.getValue();
        xSide.setAlpha(alpha);
        oSide.setAlpha(alpha);
        if (winningLine == null) return;

        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (boardPieces[c][r] != null && !isWinningCell(c, r)) boardPieces[c][r].setAlpha(alpha);
            }
        }

        winTime += dt;
        if (winTime < WIN_DELAY) return;

        for (int i = 0; i < winningLine.length; i++) {
            float t = (winTime - WIN_DELAY - i * WIN_HOP_GAP) % WIN_REPEAT_TIME;
            float hop = t > 0f && t < WIN_HOP_TIME ? (float) Math.sin(Math.PI * t / WIN_HOP_TIME) : 0f;
            int[] cell = winningLine[i];
            Vector2f center = getGridCenter(cell[0], cell[1]);
            boardPieces[cell[0]][cell[1]].setPosition(center.x, Math.round(center.y - hop * WIN_HOP_HEIGHT));
        }
    }

    private boolean isWinningCell(int col, int row) {
        for (int[] cell : winningLine) {
            if (cell[0] == col && cell[1] == row) return true;
        }
        return false;
    }

    @Override
    protected void onRender() {
        if (logoVisible) {
            spriteRenderer.draw(logo);
        }
        spriteRenderer.draw(boardSprite);

        xSide.drawResting();
        oSide.drawResting();
        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (boardPieces[c][r] != null) spriteRenderer.draw(boardPieces[c][r]);
            }
        }

        String status = getStatus();
        Font statusFont = gameFont.getTextWidth(status) <= currentWidth - 4 * UI_SPACING ? gameFont : smallFont;

        drawCenteredText(statusFont, status, statusY, TEXT_COLOR);
        drawPlayerName(xSide);
        drawPlayerName(oSide);

        btnAction.render(spriteRenderer, textRenderer);
        if (isMatchOver()) {
            btnPlayAgain.render(spriteRenderer, textRenderer);
        }

        xSide.drawMoving();
        oSide.drawMoving();
    }

    private void drawPlayerName(PieceSide side) {
        String name = getPlayerName(side.mark);
        if (name == null) return;

        float width = smallFont.getTextWidth(name);
        float x = side.stackX() - width / 2f;
        if (side.direction < 0) {
            x = Math.min(x, boardTopLeft.x - UI_SPACING - width);
        } else {
            x = Math.max(x, boardTopLeft.x + boardTexture.getWidth() * BOARD_SCALE + UI_SPACING);
        }
        textRenderer.drawText(smallFont, name, Math.round(x), Math.round(nameY + smallFont.getCapHeight()), TEXT_COLOR);
    }

    @Override
    protected void onResize(int width, int height) {
        float boardW = boardTexture.getWidth() * BOARD_SCALE;
        float boardH = boardTexture.getHeight() * BOARD_SCALE;
        boardSprite.setPosition(Math.round(width / 2f), Math.round(height / 2f));
        boardTopLeft.set(boardSprite.getPosition().x - boardW / 2f, boardSprite.getPosition().y - boardH / 2f);

        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        float statusHeight = gameFont.getCapHeight();
        logoVisible = logoHeight + UI_SPACING + statusHeight + 2 * UI_SPACING <= boardTopLeft.y;

        float headerHeight = logoVisible ? logoHeight + UI_SPACING + statusHeight : statusHeight;
        float headerY = Math.round((boardTopLeft.y - headerHeight) / 2f);
        logo.setPosition(Math.round(width / 2f), headerY + logoHeight / 2f);
        statusY = logoVisible ? headerY + logoHeight + UI_SPACING : headerY;
        nameY = Math.round(height / 2f) + STACK_BASE_OFFSET_Y + STACK_TEXTURE_HEIGHT / 2f + UI_SPACING;

        layoutButtons();

        settling.clear();
        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (boardPieces[c][r] != null) {
                    boardPieces[c][r].setPosition(getGridCenter(c, r));
                    boardPieces[c][r].setScale(BOARD_SCALE);
                }
            }
        }

        xSide.returnAnim = null;
        oSide.returnAnim = null;
        xSide.layout();
        oSide.layout();
    }

    private void layoutButtons() {
        float boardBottom = boardTopLeft.y + boardTexture.getHeight() * BOARD_SCALE;
        float y = Math.round(boardBottom + (currentHeight - boardBottom - BUTTON_HEIGHT) / 2f);
        float centerX = Math.round(currentWidth / 2f);

        if (isMatchOver()) {
            btnPlayAgain.setPosition(centerX - UI_SPACING / 2f - BUTTON_WIDTH, y);
            btnAction.setPosition(centerX + UI_SPACING / 2f, y);
        } else {
            btnAction.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        }
    }
}