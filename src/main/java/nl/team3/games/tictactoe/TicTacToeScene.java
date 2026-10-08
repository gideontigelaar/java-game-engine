package nl.team3.games.tictactoe;

import nl.team3.engine.graphics.*;
import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.Scene;
import nl.team3.engine.graphics.animation.*;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import org.joml.Vector2f;
import nl.team3.engine.core.Config;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.SpriteRenderer;
import nl.team3.engine.graphics.Texture;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;

public class TicTacToeScene implements Scene {

    private ShaderProgram spriteShader;
    private SpriteRenderer spriteRenderer;

    private int currentHeight = Config.WINDOW_HEIGHT;
    private int currentWidth = Config.WINDOW_WIDTH;

    //mouse
    private boolean blue = true;
    private boolean lastblue = true;
    private Animation mouseGrabAnim;
    private Animation mouseReleaseAnim;

    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;
    private Sprite mouseGrab;
    private Vector2f mousePosition;

    //board
    private Vector2f screenCenter = new Vector2f(Math.round((float) currentWidth/2),Math.round((float) currentHeight/2));
    private Sprite Xpiece;
    private Texture XpieceTexture;
    private BackgroundRenderer background;

    private Vector2f mousePosEffect;

    //xPieces

    private Sprite Xpiece1;
    private Sprite Xpiece2;
    private Sprite Xpiece3;
    private Sprite Xpiece4;
    private Sprite Xpiece5;

    private Animation grabPiece;
    private Animation releasePiece;

    private Texture redPiece;
    private Texture redStack;

    private List<Sprite> redPieces = new ArrayList<Sprite>();
    private List<Vector2f> gridLocations = new ArrayList<Vector2f>();

    private final InputManager input;
    private final ActionMap actions;


    private boolean isPickedUp = false;

    public TicTacToeScene(InputManager input, ActionMap actions) {
        this.input = input;
        this.actions = actions;
    }

    @Override
    public void init(AssetManager assets) {
        System.out.println("TicTacToeScene loaded");

        //initialize mouse
        mousePointTexture = assets.loadTexture("mousePoint", "/textures/tictactoe/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/textures/tictactoe/cursorGrab.png");
        spriteRenderer = new SpriteRenderer(assets);
        input.setCursorVisible(false);
        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);
        mousePoint.setAlpha(1f);

        //init Mouseanims
        mouseGrabAnim = Animation.builder()
                .sprite(mousePoint)
                .scale(new Vector2f(8f,8f), new Vector2f(6f,6f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();
        mouseReleaseAnim = Animation.builder()
                .sprite(mousePoint)
                .scale(new Vector2f(6f,6f), new Vector2f(8f,8f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();



        //board init
        XpieceTexture = assets.loadTexture("Xpiece", "/textures/tictactoe/board.png");
        Xpiece = new Sprite(XpieceTexture);
        Xpiece.setPosition(screenCenter.get(0), screenCenter.get(1) - 8f);
        Xpiece.setScale(8f);

        //init background
        background = new BackgroundRenderer(assets);
        mousePosEffect = new Vector2f(0f,0f);


        //red pieces
        redPiece = assets.loadTexture("redPieceTexture", "/textures/tictactoe/x.png");
        redStack = assets.loadTexture("redPieceStackTexture", "/textures/tictactoe/xStack.png");

        //animations
        grabPiece = Animation.builder()
                .scale(new Vector2f(8f,8f), new Vector2f(9f,9f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();


        releasePiece = Animation.builder()
                .scale(new Vector2f(9f,9f), new Vector2f(8f,8f))
                .duration(0.3f)
                .easing("ExponentialOut")
                .build();

        float offset = 0f;
        for (int i = 0; i < 5; i++) {
            Sprite s = new Sprite(redPiece);
            s.setScale(8f);
            s.setPosition(screenCenter.x - 240, (screenCenter.y) - offset + 108);
            redPieces.add(s);
            offset += 32f;
        }
        grabPiece.SetSprite(redPieces.get(4));
        releasePiece.SetSprite(redPieces.get(4));

        float gridOffsetx = 104f;
        float gridOffsety = 116f;
        //init grid positions
        gridLocations.add(new Vector2f(screenCenter.x,screenCenter.y + 108));
        gridLocations.add(new Vector2f(screenCenter.x,screenCenter.y-4f));
        gridLocations.add(new Vector2f(screenCenter.x,screenCenter.y - 116));

        gridLocations.add(new Vector2f(screenCenter.x + gridOffsetx,screenCenter.y + 108));
        gridLocations.add(new Vector2f(screenCenter.x + gridOffsetx,screenCenter.y-4f));
        gridLocations.add(new Vector2f(screenCenter.x + gridOffsetx,screenCenter.y - 116));

        gridLocations.add(new Vector2f(screenCenter.x - gridOffsetx,screenCenter.y + 108));
        gridLocations.add(new Vector2f(screenCenter.x - gridOffsetx,screenCenter.y-4f));
        gridLocations.add(new Vector2f(screenCenter.x - gridOffsetx,screenCenter.y - 116));

        }



    private int getClosestGridIndex(Vector2f point) {
        int closest = -1;
        float bestDist = Float.MAX_VALUE;

        for (int i = 0; i < gridLocations.size(); i++) {
            float d = point.distanceSquared(gridLocations.get(i));
            if (d < bestDist) {
                bestDist = d;
                closest = i;
            }
        }
        return closest;
    }

    private void setGrid(){

        gridLocations.set(0, new Vector2f(screenCenter.x,               screenCenter.y + 108));
        gridLocations.set(1, new Vector2f(screenCenter.x,               screenCenter.y - 4f));
        gridLocations.set(2, new Vector2f(screenCenter.x,               screenCenter.y - 116));

        gridLocations.set(3, new Vector2f(screenCenter.x + 104, screenCenter.y + 108));
        gridLocations.set(4, new Vector2f(screenCenter.x + 104, screenCenter.y - 4f));
        gridLocations.set(5, new Vector2f(screenCenter.x + 104, screenCenter.y - 116));

        gridLocations.set(6, new Vector2f(screenCenter.x - 104, screenCenter.y + 108));
        gridLocations.set(7, new Vector2f(screenCenter.x -  104, screenCenter.y - 4f));
        gridLocations.set(8, new Vector2f(screenCenter.x - 104, screenCenter.y - 116));
    }

    @Override
    public void update(float dt) {

        //mouse logic
        mousePosition = new Vector2f(
                (Math.round(input.getMouseX()/4) + 2f)*4,
                (Math.round(input.getMouseY()/4) + 6f)*4);

        if(input.isButtonPressed(GLFW_MOUSE_BUTTON_LEFT)){
            mouseGrabAnim.startAnimation();

        }
        if(input.isButtonReleased(GLFW_MOUSE_BUTTON_LEFT)){

            mouseReleaseAnim.startAnimation();

        }

        if(input.isButtonDown(GLFW_MOUSE_BUTTON_LEFT)){
            mousePoint.setTexture(mouseGrabTexture);
            mouseGrabAnim.UpdateAnimation(dt);
            mouseGrabAnim.setScale();
        }
        else{
            mousePoint.setTexture(mousePointTexture);
            mouseReleaseAnim.UpdateAnimation(dt);
            mouseReleaseAnim.setScale();
        }
        mousePoint.setPosition(mousePosition);

        //animation test
        if(input.isKeyPressed(GLFW_KEY_2)){
            blue = !blue;
            mousePosEffect = mousePosition;
            background.startAnimation();
        }

        //board rendering
        screenCenter = new Vector2f(Math.round((float) currentWidth/2),Math.round((float) currentHeight/2));
        background.update(dt, blue, (float) mousePosEffect.x, (float) mousePosEffect.y);

        //red pieces

        //set correct sprites to pieces
        for(int i = 0; i < 4; i++)
        {
            redPieces.get(i).setTexture(redStack);
        }

        //pickup logic

        if(redPieces.get(4).contains(new Vector2f((float) input.getMouseX(), (float) input.getMouseY() )))
        {
            if(input.isButtonPressed(GLFW_MOUSE_BUTTON_1)){
                grabPiece.startAnimation();
                isPickedUp = true;
            }
        }
        if(isPickedUp) {
            if (input.isButtonDown(GLFW_MOUSE_BUTTON_1)) {
                grabPiece.UpdateAnimation(dt);
                grabPiece.setScale();
                redPieces.get(4).setPosition((float) (redPieces.get(4).getXPosition() + input.getMouseDeltaX()), (float) (redPieces.get(4).getYPosition() + input.getMouseDeltaY()));
            } else {
                isPickedUp = false;

                Vector2f target = gridLocations.get(getClosestGridIndex(redPieces.get(4).getPosition()));
                redPieces.get(4).setPosition(new Vector2f(target));

                releasePiece.startAnimation();
            }
        }
        else{
            releasePiece.UpdateAnimation(dt);
            releasePiece.setScale();
        }


    }


    @Override
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        //spriteRenderer.draw(board);
        background.render(currentWidth,currentHeight);
        spriteRenderer.draw(Xpiece);


        for (Sprite s : redPieces) {
            spriteRenderer.draw(s);
        }

        spriteRenderer.draw(mousePoint);

    }

    @Override
    public void resize(int width, int height) {
        // Store new dimensions every time window resizes
        this.currentWidth = width;
        this.currentHeight = height;
        screenCenter = new Vector2f(Math.round((float) currentWidth/2),Math.round((float) currentHeight/2));
        setGrid();
        spriteRenderer.setProjection(width, height);
        Xpiece.setPosition(new Vector2f(Math.round((float) width/2),Math.round((float) height/2)));
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
        background.cleanup();
    }


}