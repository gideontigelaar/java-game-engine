package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.ServerConnection;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private Sprite boardSprite;
    private Texture boardTexture;

    private Texture xPieceTexture;
    private Texture xStackTexture;
    private Texture oPieceTexture;
    private Texture oStackTexture;

    private PieceSide xSide;
    private PieceSide oSide;
    private final Sprite[][] boardPieces = new Sprite[3][3];

    private Button btnAction;
    private boolean matchEnded = false;
    private String gameStatusMessage = "";

    private class PieceSide {
        private final float direction;
        private final Texture pieceTexture;
        private final List<Sprite> stack = new ArrayList<>();
        private final List<Animation> settling = new ArrayList<>();
        private Sprite active;
        private Animation grabAnim;
        private Animation releaseAnim;
        private boolean pickedUp = false;

        private PieceSide(float direction, Texture pieceTexture, Texture stackTexture) {
            this.direction = direction;
            this.pieceTexture = pieceTexture;

            for (int i = 0; i < PIECES_PER_SIDE; i++) {
                Sprite s = new Sprite(stackTexture);
                s.setScale(BOARD_SCALE);
                s.setOrigin(0.5f, 1f);
                stack.add(s);
            }
            spawnNext();
        }

        private void setupAnimations() {
            grabAnim = Animation.builder().sprite(active).scale(new Vector2f(BOARD_SCALE, BOARD_SCALE), new Vector2f(GRAB_SCALE, GRAB_SCALE)).duration(0.3f).easing("ExponentialOut").build();
            releaseAnim = Animation.builder().sprite(active).scale(new Vector2f(GRAB_SCALE, GRAB_SCALE), new Vector2f(BOARD_SCALE, BOARD_SCALE)).duration(0.3f).easing("ExponentialOut").build();
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

            snapActiveToStack();
        }

        private float levelY(int level) {
            return currentHeight / 2f + STACK_BASE_OFFSET_Y - level * STACK_STRIDE;
        }

        private void snapActiveToStack() {
            active.setPosition(currentWidth / 2f + direction * STACK_OFFSET_X, levelY(stack.size()));
        }

        private void layout() {
            float centerX = currentWidth / 2f + direction * STACK_OFFSET_X;
            for (int i = 0; i < stack.size(); i++) {
                stack.get(i).setPosition(centerX, levelY(i) + STACK_TEXTURE_HEIGHT / 2f);
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

        xPieceTexture = assets.loadTexture("xPiece", "/textures/tictactoe/x.png");
        xStackTexture = assets.loadTexture("xStack", "/textures/tictactoe/xStack.png");
        oPieceTexture = assets.loadTexture("oPiece", "/textures/tictactoe/o.png");
        oStackTexture = assets.loadTexture("oStack", "/textures/tictactoe/oStack.png");

        xSide = new PieceSide(-1f, xPieceTexture, xStackTexture);
        oSide = new PieceSide(1f, oPieceTexture, oStackTexture);

        xSide.setupAnimations();
        oSide.setupAnimations();

        btnAction = new Button(connection != null ? "FORFEIT" : "LEAVE GAME", smallFont, new Vector2f(0, 0), new Vector2f(200, 50));
        btnAction.setOnClick(this::handleGameAction);

        if (connection != null) gameStatusMessage = "Match Started!";
    }

    private Vector2f getGridCenter(int col, int row) {
        float boardW = boardTexture.getWidth() * BOARD_SCALE;
        float boardH = boardTexture.getHeight() * BOARD_SCALE;
        float boardTopLeftX = boardSprite.getPosition().x - (boardW * boardSprite.getOrigin().x);
        float boardTopLeftY = boardSprite.getPosition().y - (boardH * boardSprite.getOrigin().y);

        float cellTopLeftX = boardTopLeftX + START_OFFSET_X + (col * STRIDE_X);
        float cellTopLeftY = boardTopLeftY + START_OFFSET_Y + (row * STRIDE_Y);

        return new Vector2f(cellTopLeftX + CELL_HALF_W, cellTopLeftY + CELL_HALF_H);
    }

    private void handleGameAction() {
        if (connection != null && !matchEnded) {
            connection.sendCommand("forfeit");
        }
        if (connection != null && connection.isConnected()) {
            sceneManager.changeScene(new LobbyScene(input, actions, sceneManager, connection));
        } else {
            sceneManager.changeScene(new MainMenuScene(input, actions, sceneManager));
        }
    }

    @Override
    protected void onUpdate(float dt) {
        btnAction.update(input);

        updateSide(xSide, oSide, dt);
        updateSide(oSide, xSide, dt);

        updateSettling(xSide, dt);
        updateSettling(oSide, dt);

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

    private void updateSide(PieceSide side, PieceSide other, float dt) {
        if (side.active == null) return;

        if (!other.pickedUp && side.active.contains(new Vector2f((float) input.getMouseX(), (float) input.getMouseY()))) {
            if (input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)) {
                side.grabAnim.startAnimation();
                side.pickedUp = true;
            }
        }

        if (side.pickedUp) {
            if (input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                side.grabAnim.UpdateAnimation(dt);
                side.grabAnim.setScale();
                side.active.setPosition((float) (side.active.getPosition().x + input.getMouseDeltaX()), (float) (side.active.getPosition().y + input.getMouseDeltaY()));
            } else {
                side.pickedUp = false;
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
        float left = boardSprite.getPosition().x - (boardW * 0.5f);
        float right = left + boardW;
        float top = boardSprite.getPosition().y - (boardH * 0.5f);
        float bottom = top + boardH;

        Vector2f dropPos = side.active.getPosition();
        if (dropPos.x >= left && dropPos.x <= right && dropPos.y >= top && dropPos.y <= bottom) {
            int bestCol = 0;
            int bestRow = 0;
            float bestDist = Float.MAX_VALUE;
            for (int c = 0; c < 3; c++) {
                for (int r = 0; r < 3; r++) {
                    Vector2f cellCenter = getGridCenter(c, r);
                    float dist = dropPos.distanceSquared(cellCenter);
                    if (dist < bestDist) {
                        bestDist = dist;
                        bestCol = c;
                        bestRow = r;
                    }
                }
            }

            if (boardPieces[bestCol][bestRow] == null) {
                placePiece(side, bestCol, bestRow);
                return;
            }
        }

        side.releaseAnim.startAnimation();
    }

    private void placePiece(PieceSide side, int col, int row) {
        Sprite piece = side.active;
        piece.setPosition(getGridCenter(col, row));
        boardPieces[col][row] = piece;

        Animation settle = Animation.builder().sprite(piece).scale(new Vector2f(GRAB_SCALE, GRAB_SCALE), new Vector2f(BOARD_SCALE, BOARD_SCALE)).duration(0.3f).easing("ExponentialOut").build();
        settle.startAnimation();
        side.settling.add(settle);

        side.spawnNext();
    }

    private void updateSettling(PieceSide side, float dt) {
        Iterator<Animation> it = side.settling.iterator();
        while (it.hasNext()) {
            Animation settle = it.next();
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
        btnAction.setColors(new Vector4f(0, 1, 0, 1), new Vector4f(0.8f, 1, 0.8f, 1), new Vector4f(0, 0.5f, 0, 1));
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
        spriteRenderer.draw(boardSprite);

        for (int c = 0; c < 3; c++) {
            for (int r = 0; r < 3; r++) {
                if (boardPieces[c][r] != null) spriteRenderer.draw(boardPieces[c][r]);
            }
        }
        xSide.drawResting();
        oSide.drawResting();

        String mode = connection != null ? "ONLINE" : "OFFLINE";
        float modeW = gameFont.getTextWidth(mode);
        textRenderer.drawText(gameFont, mode, (currentWidth - modeW) / 2f, currentHeight * 0.1f, new Vector4f(1, 1, 1, 1));

        if (!gameStatusMessage.isEmpty()) {
            float textW = smallFont.getTextWidth(gameStatusMessage);
            textRenderer.drawText(smallFont, gameStatusMessage, (currentWidth - textW) / 2f, currentHeight * 0.15f, new Vector4f(1, 1, 0, 1));
        }
        btnAction.render(spriteRenderer, textRenderer);

        xSide.drawHeld();
        oSide.drawHeld();
    }

    @Override
    protected void onResize(int width, int height) {
        boardSprite.setPosition(new Vector2f(Math.round((float) width / 2), Math.round((float) height / 2)));
        btnAction.setPosition(new Vector2f((width - 200f) / 2f, height - 100f));

        for (int c = 0; c < 3; c++) {
            for (int r = 0; r < 3; r++) {
                if (boardPieces[c][r] != null) boardPieces[c][r].setPosition(getGridCenter(c, r));
            }
        }

        xSide.layout();
        oSide.layout();
    }
}