package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.EngineContext;
import nl.team3.engine.graphics.Font;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.graphics.animation.EasingType;
import nl.team3.engine.graphics.animation.SpriteAnimation;
import nl.team3.engine.input.MouseButtons;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.BackgroundRenderer;
import nl.team3.games.tictactoe.logic.AIOpponent;
import nl.team3.games.tictactoe.logic.Board;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MatchScene extends BaseScene {
    protected static final Vector4f WIN_COLOR = HIGHLIGHT_COLOR;
    protected static final Vector4f LOSE_COLOR = new Vector4f(1f, 0.25f, 0.25f, 1f);

    private static final Vector4f X_COLOR = new Vector4f(1f, 0.149f, 0.455f, 1f);
    private static final Vector4f O_COLOR = new Vector4f(0.063f, 0.824f, 0.459f, 1f);
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
    private static final float PLAY_MOVE_ARC = 3f * BOARD_SCALE;
    private static final float GHOST_ALPHA = 0.4f;
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

    private static final float RESULT_DROP = 2 * UI_SPACING;
    private static final float RESULT_DROP_TIME = 0.5f;
    private static final int CONFETTI_COUNT = 90;
    private static final float CONFETTI_GRAVITY = 900f;
    private static final float CONFETTI_TIME = 2.5f;
    private static final float CONFETTI_SIZE = 2 * UI_SCALE;

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
    private Sprite ghost;
    private Sprite pixel;

    private PieceSide xSide;
    private PieceSide oSide;
    private final Sprite[][] boardPieces = new Sprite[Board.SIZE][Board.SIZE];
    private final List<PieceMove> settling = new ArrayList<>();
    private int pressedCell = -1;
    private int ghostCell = -1;

    private int[][] winningLine;
    private float winTime;
    private Animation fadeAnim;
    private Animation resultAnim;
    private final List<Confetti> confetti = new ArrayList<>();

    protected Button btnAction;
    private Button btnPlayAgain;
    private float statusY;
    private float statusHeight;
    private float nameY;
    private boolean namesBelowBoard;

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
        private float nameX;

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
            grabAnim = SpriteAnimation.builder().sprite(active).scale(new Vector2f(active.getScale()), new Vector2f(GRAB_SCALE, GRAB_SCALE)).duration(MOVE_TIME).easing(EasingType.EXPONENTIAL_OUT).build();
            grabAnim.startAnimation();
            layout();
        }

        private void putBack() {
            pickedUp = false;
            bobTime = 0f;
            returnAnim = SpriteAnimation.builder().sprite(active)
                    .position(new Vector2f(active.getPosition()), new Vector2f(stackX(), levelY(stack.size())))
                    .scale(new Vector2f(active.getScale()), new Vector2f(BOARD_SCALE, BOARD_SCALE))
                    .duration(MOVE_TIME).easing(EasingType.EXPONENTIAL_OUT).build();
            returnAnim.startAnimation();
            layout();
        }

        private float stackX() {
            return Math.round(currentWidth / 2f) + direction * STACK_OFFSET_X;
        }

        private float levelY(int level) {
            return Math.round(currentHeight / 2f) + STACK_BASE_OFFSET_Y - level * STACK_STRIDE;
        }

        private float bobOffset() {
            return Math.round(BOB_HEIGHT * (1f - (float) Math.cos(2 * Math.PI * bobTime / BOB_TIME)) / 2f);
        }

        private void snapActiveToStack() {
            float bob = bobOffset();
            active.setPosition(stackX(), levelY(stack.size()) - bob);
            if (!stack.isEmpty()) {
                stack.get(stack.size() - 1).setTexture(bobTime > 0f ? pieceTexture : stackTexture);
            }
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

        private void collectSprites(List<Sprite> out) {
            out.addAll(stack);
            if (active != null) out.add(active);
        }
    }

    private static final class PieceMove {
        private final Sprite piece;
        private final Vector2f from;
        private final Vector2f to;
        private final float fromScale;
        private final float lift;
        private final float arc;
        private final Animation progress;

        private PieceMove(Sprite piece, Vector2f to, float lift, float arc, float time, EasingType easing) {
            this.piece = piece;
            this.from = new Vector2f(piece.getPosition());
            this.to = to;
            this.fromScale = piece.getScale().x;
            this.lift = lift;
            this.arc = arc;
            this.progress = new Animation(0f, 1f, easing, time);
            progress.startAnimation();
        }

        private boolean update(float dt) {
            progress.update(dt);
            float amount = progress.getValue();
            float bump = (float) Math.sin(Math.PI * amount);
            piece.setPosition(Math.round(from.x + (to.x - from.x) * amount),
                    Math.round(from.y + (to.y - from.y) * amount - arc * bump));
            piece.setScale(fromScale + (BOARD_SCALE - fromScale) * amount + lift * bump);
            return progress.isFinished();
        }
    }

    private static final class Confetti {
        private final Vector2f position = new Vector2f();
        private final Vector2f velocity = new Vector2f();
        private final Vector4f color = new Vector4f();
        private float flipTime;
        private float age;
    }

    public MatchScene(EngineContext context) {
        this(context, ThreadLocalRandom.current().nextBoolean() ? Board.Mark.X : Board.Mark.O,
                Board.Mark.X, null, null);
    }

    // Against the computer, which plays O on the right
    public MatchScene(EngineContext context, AIOpponent ai) {
        this(context, Board.Mark.X, Board.Mark.X, ai, Board.Mark.O);
    }

    protected MatchScene(EngineContext context,
                         Board.Mark firstTurn, Board.Mark leftMark, AIOpponent ai, Board.Mark aiMark) {
        super(context);
        this.board = new Board(firstTurn);
        this.firstTurn = firstTurn;
        this.leftMark = leftMark;
        this.ai = ai;
        this.aiMark = aiMark;
    }

    @Override
    protected void onInit(AssetManager assets) {
        boardTexture = assets.loadTexture("board", "/games/tictactoe/textures/board.png");
        boardSprite = new Sprite(boardTexture);
        boardSprite.setScale(BOARD_SCALE);
        logo = createLogo();
        pixel = new Sprite(assets.getTexture(AssetManager.WHITE_TEXTURE));

        Texture xPieceTexture = assets.loadTexture("xPiece", "/games/tictactoe/textures/x.png");
        Texture xStackTexture = assets.loadTexture("xStack", "/games/tictactoe/textures/xStack.png");
        Texture oPieceTexture = assets.loadTexture("oPiece", "/games/tictactoe/textures/o.png");
        Texture oStackTexture = assets.loadTexture("oStack", "/games/tictactoe/textures/oStack.png");

        xSide = new PieceSide(Board.Mark.X, leftMark == Board.Mark.X ? -1f : 1f, xPieceTexture, xStackTexture);
        oSide = new PieceSide(Board.Mark.O, leftMark == Board.Mark.O ? -1f : 1f, oPieceTexture, oStackTexture);

        ghost = new Sprite(xPieceTexture);
        ghost.setScale(BOARD_SCALE);
        ghost.setAlpha(GHOST_ALPHA);

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

    protected boolean canPlayAgain() {
        return true;
    }

    private boolean showsPlayAgain() {
        return isMatchOver() && canPlayAgain();
    }

    protected String getStatus() {
        if (ai == null) {
            return board.getTurn() + "'S TURN";
        }
        return board.getTurn() == aiMark ? "AI'S TURN" : "YOUR TURN";
    }

    protected String getResultTitle() {
        Board.Mark winner = board.getWinner();
        if (winner == null) {
            return "DRAW!";
        }
        if (ai == null) {
            return winner + " WINS!";
        }
        return winner == aiMark ? "YOU LOSE!" : "YOU WIN!";
    }

    protected String getResultDetail() {
        if (board.getWinner() == null) {
            return "NO MOVES LEFT";
        }
        if (ai == null || board.getWinner() != aiMark) {
            return "THREE IN A ROW!";
        }
        return "THE AI GOT THREE IN A ROW";
    }

    protected Vector4f getResultColor() {
        Board.Mark winner = board.getWinner();
        if (winner == null) {
            return WHITE;
        }
        return ai != null && winner == aiMark ? LOSE_COLOR : WIN_COLOR;
    }

    protected boolean isCelebrating() {
        return board.getWinner() != null && isHuman(board.getWinner());
    }

    protected String getPlayerName(Board.Mark mark) {
        if (ai == null) return null;
        return mark == aiMark ? "AI" : "YOU";
    }

    protected void leave() {
        leaveTo(new OfflineMenuScene(context));
    }

    // Take turns starting
    protected void playAgain() {
        leaveTo(new MatchScene(context, firstTurn.opponent(), leftMark, ai, aiMark));
    }

    protected void leaveTo(BaseScene scene) {
        transitionTo(scene, getTransitionSprites(), getTransitionButtons());
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
        if (!isLeaving()) {
            btnAction.update(input);
            if (showsPlayAgain()) {
                btnPlayAgain.update(input);
            }
        }

        updateAI(dt);
        updateSide(xSide, oSide, dt);
        updateSide(oSide, xSide, dt);
        updateTap();
        updateSettling(dt);
        updateGameOver(dt);
        updateConfetti(dt);
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
        if (isPlayable(side) && !other.pickedUp && input.isButtonPressed(MouseButtons.LEFT) && side.active.contains(mousePosition)) {
            side.pickUp();
        }

        if (side.pickedUp) {
            if (input.isButtonDown(MouseButtons.LEFT)) {
                side.grabAnim.update(dt);
                side.grabAnim.setScale();
                Vector2f position = side.active.getPosition();
                side.active.setPosition(position.x + (float) input.getMouseDeltaX(), position.y + (float) input.getMouseDeltaY());
            } else {
                dropPiece(side);
            }
        } else if (side.returnAnim != null) {
            side.returnAnim.update(dt);
            side.returnAnim.setTransformation();
            if (side.returnAnim.isFinished()) {
                side.returnAnim = null;
                side.layout();
            }
        } else {
            side.bobTime = isPlayable(side) ? side.bobTime + dt : settleBob(side.bobTime, dt);
            side.snapActiveToStack();
        }
    }

    private static float settleBob(float bobTime, float dt) {
        float end = (float) Math.ceil(bobTime / BOB_TIME) * BOB_TIME;
        float next = bobTime + dt;
        return next >= end ? 0f : next;
    }

    private void updateTap() {
        PieceSide side = sideOf(board.getTurn());
        ghostCell = -1;
        if (side.active == null || !isPlayable(side)) {
            pressedCell = -1;
            return;
        }
        if (side.pickedUp) {
            pressedCell = -1;
            ghostCell = cellAt(side.active.getPosition());
            return;
        }

        int cell = cellAt(mousePosition);
        if (input.isButtonPressed(MouseButtons.LEFT)) {
            pressedCell = cell;
        }
        if (input.isButtonReleased(MouseButtons.LEFT)) {
            int tapped = pressedCell;
            pressedCell = -1;
            if (cell >= 0 && cell == tapped) {
                placePiece(side, cell % Board.SIZE, cell / Board.SIZE, true);
                onMove(cell % Board.SIZE, cell / Board.SIZE);
                return;
            }
        }
        ghostCell = cell;
    }

    private int cellAt(Vector2f position) {
        float boardW = boardTexture.getWidth() * BOARD_SCALE;
        float boardH = boardTexture.getHeight() * BOARD_SCALE;
        if (position.x < boardTopLeft.x || position.x > boardTopLeft.x + boardW
                || position.y < boardTopLeft.y || position.y > boardTopLeft.y + boardH) {
            return -1;
        }

        int bestCol = 0;
        int bestRow = 0;
        float bestDist = Float.MAX_VALUE;
        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                float dist = position.distanceSquared(getGridCenter(c, r));
                if (dist < bestDist) {
                    bestDist = dist;
                    bestCol = c;
                    bestRow = r;
                }
            }
        }
        return board.canPlace(bestCol, bestRow) ? bestRow * Board.SIZE + bestCol : -1;
    }

    private void dropPiece(PieceSide side) {
        int cell = cellAt(side.active.getPosition());
        if (cell >= 0) {
            placePiece(side, cell % Board.SIZE, cell / Board.SIZE, false);
            onMove(cell % Board.SIZE, cell / Board.SIZE);
            return;
        }

        side.putBack();
    }

    protected void playMove(int col, int row) {
        PieceSide side = sideOf(board.getTurn());
        if (side.active != null) {
            placePiece(side, col, row, true);
        }
    }

    private void placePiece(PieceSide side, int col, int row, boolean carried) {
        Sprite piece = side.active;
        boardPieces[col][row] = piece;
        board.place(col, row);

        PieceMove move = carried
                ? new PieceMove(piece, getGridCenter(col, row), GRAB_SCALE - BOARD_SCALE, PLAY_MOVE_ARC, PLAY_MOVE_TIME, EasingType.SINE_IN_OUT)
                : new PieceMove(piece, getGridCenter(col, row), 0f, 0f, MOVE_TIME, EasingType.EXPONENTIAL_OUT);
        settling.add(move);

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
            fadeAnim = new Animation(1f, FADE_ALPHA, EasingType.EXPONENTIAL_OUT, FADE_TIME);
            fadeAnim.startAnimation();
            resultAnim = new Animation(0f, 1f, EasingType.BACK_OUT, RESULT_DROP_TIME);
        }
        layoutButtons();
        updatePalette(boardSprite.getPosition().x, boardSprite.getPosition().y);
    }

    private void updateSettling(float dt) {
        Iterator<PieceMove> it = settling.iterator();
        while (it.hasNext()) {
            if (it.next().update(dt)) {
                it.remove();
            }
        }
    }

    private void updateGameOver(float dt) {
        if (fadeAnim == null || !settling.isEmpty()) return;

        if (!resultAnim.hasStarted()) {
            resultAnim.startAnimation();
            if (isCelebrating()) {
                spawnConfetti();
            }
        }
        resultAnim.update(dt);
        fadeAnim.update(dt);
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

    private void spawnConfetti() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Vector4f main = getResultColor();
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            Confetti piece = new Confetti();
            boolean left = i % 2 == 0;
            piece.position.set(left ? -UI_SPACING : currentWidth + UI_SPACING, currentHeight * random.nextFloat(0.55f, 0.85f));
            float angle = (float) Math.toRadians(random.nextFloat(50f, 80f));
            float speed = random.nextFloat(500f, 1000f);
            piece.velocity.set((left ? 1f : -1f) * (float) Math.cos(angle) * speed, -(float) Math.sin(angle) * speed);
            Vector4f color = switch (i % 4) {
                case 0 -> main;
                case 1 -> WHITE;
                case 2 -> X_COLOR;
                default -> O_COLOR;
            };
            piece.color.set(color);
            piece.flipTime = random.nextFloat(0.08f, 0.2f);
            confetti.add(piece);
        }
    }

    private void updateConfetti(float dt) {
        Iterator<Confetti> it = confetti.iterator();
        while (it.hasNext()) {
            Confetti piece = it.next();
            piece.age += dt;
            piece.velocity.y += CONFETTI_GRAVITY * dt;
            piece.velocity.x -= piece.velocity.x * Math.min(1f, 1.5f * dt);
            piece.position.add(piece.velocity.x * dt, piece.velocity.y * dt);
            if (piece.age > CONFETTI_TIME || piece.position.y > currentHeight + UI_SPACING) {
                it.remove();
            }
        }
    }

    private boolean isSettling(Sprite piece) {
        for (PieceMove move : settling) {
            if (move.piece == piece) return true;
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
                Sprite piece = boardPieces[c][r];
                if (piece != null && !isSettling(piece)) spriteRenderer.draw(piece);
            }
        }
        renderGhost();
        renderConfetti();

        renderStatus();
        drawPlayerName(xSide);
        drawPlayerName(oSide);

        btnAction.render(spriteRenderer, textRenderer);
        if (showsPlayAgain()) {
            btnPlayAgain.render(spriteRenderer, textRenderer);
        }

        for (PieceMove move : settling) {
            spriteRenderer.draw(move.piece);
        }
        xSide.drawMoving();
        oSide.drawMoving();
    }

    private void renderGhost() {
        if (ghostCell < 0) return;

        PieceSide side = sideOf(board.getTurn());
        ghost.setTexture(side.pieceTexture);
        ghost.setPosition(getGridCenter(ghostCell % Board.SIZE, ghostCell / Board.SIZE));
        spriteRenderer.draw(ghost);
    }

    private void renderConfetti() {
        pixel.setOrigin(0.5f, 0.5f);
        for (Confetti piece : confetti) {
            boolean flipped = (int) (piece.age / piece.flipTime) % 2 == 0;
            pixel.setPosition(Math.round(piece.position.x), Math.round(piece.position.y));
            pixel.setScale(flipped ? CONFETTI_SIZE : UI_SCALE, flipped ? UI_SCALE : CONFETTI_SIZE);
            pixel.setTint(piece.color.x, piece.color.y, piece.color.z, 1f);
            pixel.setAlpha(Math.min(1f, 3f * (CONFETTI_TIME - piece.age)));
            spriteRenderer.draw(pixel);
        }
        pixel.setAlpha(1f);
    }

    private void renderStatus() {
        if (fadeAnim != null) {
            if (!resultAnim.hasStarted()) return;

            float drop = Math.round((1f - resultAnim.getValue()) * RESULT_DROP);
            drawCenteredText(gameFont, fitText(gameFont, getResultTitle(), getMaxTextWidth()), statusY - drop, getResultColor());

            String detail = fitText(smallFont, getResultDetail(), getMaxTextWidth());
            drawCenteredText(smallFont, detail, statusY + gameFont.getCapHeight() + LINE_GAP - drop, WHITE);
            return;
        }

        String status = getStatus();
        Font statusFont = gameFont.getTextWidth(status) <= getMaxTextWidth() ? gameFont : smallFont;
        float y = statusY + (statusHeight - statusFont.getCapHeight()) / 2f;
        drawCenteredText(statusFont, fitText(statusFont, status, getMaxTextWidth()), y, WHITE);
    }

    private void drawPlayerName(PieceSide side) {
        String name = getPlayerName(side.mark);
        if (name == null) return;

        drawText(smallFont, fitText(smallFont, name, getNameSpace()), side.nameX, nameY, WHITE);
    }

    private float getNameSpace() {
        return currentWidth / 2f - 1.5f * UI_SPACING;
    }

    @Override
    protected void onResize(int width, int height) {
        float boardW = boardTexture.getWidth() * BOARD_SCALE;
        float boardH = boardTexture.getHeight() * BOARD_SCALE;
        boardSprite.setPosition(Math.round(width / 2f), Math.round(height / 2f));
        boardTopLeft.set(boardSprite.getPosition().x - boardW / 2f, boardSprite.getPosition().y - boardH / 2f);

        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        statusHeight = gameFont.getCapHeight() + LINE_GAP + smallFont.getCapHeight();
        logoVisible = logoHeight + UI_SPACING + statusHeight + 2 * UI_SPACING <= boardTopLeft.y;

        float headerHeight = logoVisible ? logoHeight + UI_SPACING + statusHeight : statusHeight;
        float headerY = Math.round((boardTopLeft.y - headerHeight) / 2f);
        logo.setPosition(Math.round(width / 2f), headerY + logoHeight / 2f);
        statusY = logoVisible ? headerY + logoHeight + UI_SPACING : headerY;

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

        layoutNames();
        layoutButtons();
    }

    private void layoutNames() {
        float boardLeft = boardTopLeft.x;
        float boardRight = boardLeft + boardTexture.getWidth() * BOARD_SCALE;
        float boardBottom = boardTopLeft.y + boardTexture.getHeight() * BOARD_SCALE;
        namesBelowBoard = false;
        for (PieceSide side : List.of(xSide, oSide)) {
            String name = getPlayerName(side.mark);
            if (name != null && smallFont.getTextWidth(name) > boardLeft - 2 * UI_SPACING) {
                namesBelowBoard = true;
            }
        }

        nameY = namesBelowBoard
                ? boardBottom + UI_SPACING
                : Math.round(currentHeight / 2f) + STACK_BASE_OFFSET_Y + STACK_TEXTURE_HEIGHT / 2f + UI_SPACING;
        float leftLimit = namesBelowBoard ? currentWidth / 2f : boardLeft;
        float rightLimit = namesBelowBoard ? currentWidth / 2f : boardRight;

        for (PieceSide side : List.of(xSide, oSide)) {
            String name = getPlayerName(side.mark);
            float width = name == null ? 0f : smallFont.getTextWidth(fitText(smallFont, name, getNameSpace()));
            float min = side.direction < 0 ? UI_SPACING : rightLimit + UI_SPACING / 2f;
            float max = side.direction < 0 ? leftLimit - UI_SPACING / 2f - width : currentWidth - UI_SPACING - width;
            side.nameX = Math.round(Math.max(min, Math.min(max, side.stackX() - width / 2f)));
        }
    }

    @Override
    protected Collection<Sprite> getTransitionSprites() {
        List<Sprite> sprites = new ArrayList<>();
        sprites.add(boardSprite);
        if (logoVisible) sprites.add(logo);
        xSide.collectSprites(sprites);
        oSide.collectSprites(sprites);
        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (boardPieces[c][r] != null) sprites.add(boardPieces[c][r]);
            }
        }
        return sprites;
    }

    @Override
    protected Button[] getTransitionButtons() {
        return showsPlayAgain() ? new Button[]{btnPlayAgain, btnAction} : new Button[]{btnAction};
    }

    private void layoutButtons() {
        float boardBottom = boardTopLeft.y + boardTexture.getHeight() * BOARD_SCALE;
        float top = namesBelowBoard ? nameY + smallFont.getCapHeight() : boardBottom;
        float y = Math.round(top + (currentHeight - top - BUTTON_HEIGHT) / 2f);
        float centerX = Math.round(currentWidth / 2f);

        if (showsPlayAgain()) {
            btnPlayAgain.setPosition(centerX - UI_SPACING / 2f - BUTTON_WIDTH, y);
            btnAction.setPosition(centerX + UI_SPACING / 2f, y);
        } else {
            btnAction.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        }
    }
}