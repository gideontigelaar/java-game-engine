package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.EngineContext;
import nl.team3.engine.core.Scene;
import nl.team3.engine.graphics.*;
import nl.team3.engine.graphics.animation.Animation;
import nl.team3.engine.graphics.animation.EasingType;
import nl.team3.engine.graphics.animation.SpriteAnimation;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.input.MouseButtons;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.BackgroundRenderer;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseScene implements Scene {
    protected static final int UI_SCALE = 4;
    protected static final float UI_SPACING = 4 * UI_SCALE;
    protected static final float BUTTON_WIDTH = 300f;
    protected static final float SMALL_BUTTON_WIDTH = 200f;
    protected static final float BUTTON_HEIGHT = 68f;
    protected static final Vector4f WHITE = new Vector4f(1, 1, 1, 1);
    protected static final Vector4f DIM_WHITE = new Vector4f(1, 1, 1, 0.7f);
    protected static final Vector4f ERROR_COLOR = new Vector4f(1, 0, 0, 1);
    protected static final Vector4f HIGHLIGHT_COLOR = new Vector4f(1, 1, 0, 1);
    protected static final Vector4f HOVER_COLOR = new Vector4f(0.8f, 0.8f, 0, 1);
    protected static final Vector4f DARK_TEXT_COLOR = new Vector4f(0f, 0.157f, 0.349f, 1f);

    private static final NineSlice BUTTON_SLICE = new NineSlice(5, 6, 5, 5);
    private static final float TRANSITION_TIME = 0.7f;
    private static final float PALETTE_SWITCH_TIME = 0.2f;

    protected final EngineContext context;
    protected final InputManager input;

    protected SpriteRenderer spriteRenderer;
    protected TextRenderer textRenderer;
    protected BackgroundRenderer background;

    protected Font gameFont;
    protected Font smallFont;

    protected int currentWidth;
    protected int currentHeight;

    private Texture mousePointTexture;
    private Texture mouseGrabTexture;
    private Sprite mousePoint;

    private Texture buttonTexture;
    private Texture buttonPressedTexture;
    private Texture logoTexture;

    protected Vector2f mousePosition = new Vector2f();

    private SpriteAnimation mouseGrabAnim;
    private SpriteAnimation mouseReleaseAnim;

    private BaseScene nextScene;
    private float transitionTime;

    private static final float SLIDE_TIME = 0.4f;

    private interface PositionSetter {
        void set(float x, float y);
    }

    private static final class Slide {
        final Vector2f start;
        final float travel;
        final Animation anim;
        final PositionSetter setter;

        Slide(Vector2f start, float travel, Animation anim, PositionSetter setter) {
            this.start = start;
            this.travel = travel;
            this.anim = anim;
            this.setter = setter;
        }
    }

    protected boolean isAnimating() {
        return introducing || nextScene != null;
    }

    private static final float INTRO_BUTTON_TIME = 0.3f;

    private boolean introducing = true;
    private float introTime;
    private float introDuration;
    private final List<Slide> intros = new ArrayList<>();
    private boolean introBuilt;


    private final List<Slide> slides = new ArrayList<>();



    public BaseScene(EngineContext context) {
        this.context = context;
        this.input = context.input();
    }

    @Override
    public final void init(AssetManager assets) {
        currentWidth = context.sceneManager().getWidth();
        currentHeight = context.sceneManager().getHeight();

        textRenderer = assets.getTextRenderer();
        spriteRenderer = assets.getSpriteRenderer();
        background = assets.getShared(BackgroundRenderer.class, BackgroundRenderer::new);

        gameFont = assets.loadFont("gameFont", "/games/tictactoe/fonts/3x5-Microfont-Mono.ttf", 48f);
        smallFont = assets.loadFont("smallFont", "/games/tictactoe/fonts/3x5-Microfont-Mono.ttf", 24f);

        buttonTexture = assets.loadTexture("btnTexture", "/games/tictactoe/textures/button.png");
        buttonPressedTexture = assets.loadTexture("btnPressedTexture", "/games/tictactoe/textures/buttonPressed.png");
        logoTexture = assets.loadTexture("tripleTlogo", "/games/tictactoe/textures/tripleTlogo.png");

        mousePointTexture = assets.loadTexture("mousePoint", "/games/tictactoe/textures/cursorPoint.png");
        mouseGrabTexture = assets.loadTexture("mouseGrab", "/games/tictactoe/textures/cursorGrab.png");
        context.window().setCursorVisible(false);

        mousePoint = new Sprite(mousePointTexture);
        mousePoint.setScale(4f);

        mouseGrabAnim = SpriteAnimation.builder().sprite(mousePoint).scale(new Vector2f(4f, 4f), new Vector2f(3f, 3f)).duration(0.3f).easing(EasingType.EXPONENTIAL_OUT).build();
        mouseReleaseAnim = SpriteAnimation.builder().sprite(mousePoint).scale(new Vector2f(3f, 3f), new Vector2f(4f, 4f)).duration(0.3f).easing(EasingType.EXPONENTIAL_OUT).build();

        onInit(assets);
        updatePalette(currentWidth / 2f, currentHeight / 2f);
    }

    @Override
    public final void update(float dt) {
        mousePosition.set((float) input.getMouseX(), (float) input.getMouseY());

        if (input.isButtonPressed(MouseButtons.LEFT)) mouseGrabAnim.startAnimation();
        if (input.isButtonReleased(MouseButtons.LEFT)) mouseReleaseAnim.startAnimation();

        if (input.isButtonDown(MouseButtons.LEFT)) {
            mousePoint.setTexture(mouseGrabTexture);
            mouseGrabAnim.update(dt);
            mouseGrabAnim.setScale();
        } else {
            mousePoint.setTexture(mousePointTexture);
            mouseReleaseAnim.update(dt);
            mouseReleaseAnim.setScale();
        }
        mousePoint.setPosition(mousePosition.x + 4f, mousePosition.y + 20f);

        background.update(dt);

        onUpdate(dt);
        updateIntro(dt);

        if (nextScene != null) {
            updateTransition(dt);
        }
    }

    @Override
    public final void render() {
        background.render(currentWidth, currentHeight);

        onRender();

        spriteRenderer.draw(mousePoint);
    }

    @Override
    public final void resize(int width, int height) {
        this.currentWidth = width;
        this.currentHeight = height;
        spriteRenderer.setProjection(width, height);
        textRenderer.setProjection(width, height);

        onResize(width, height);
        if (introducing) buildIntro();
    }

    protected Button createButton(String text, float width, Runnable onClick) {
        return createButton(text, width, BUTTON_HEIGHT, onClick);
    }

    protected Button createButton(String text, float width, float height, Runnable onClick) {
        Button button = new Button(text, smallFont, new Vector2f(), new Vector2f(width, height));
        button.setBackgroundSprite(new Sprite(buttonTexture));
        button.setPressedSprite(new Sprite(buttonPressedTexture), UI_SCALE);
        button.setNineSlice(BUTTON_SLICE, UI_SCALE);
        button.setOnClick(onClick);
        return button;
    }

    protected Sprite createLogo() {
        Sprite logo = new Sprite(logoTexture);
        logo.setScale(UI_SCALE);
        return logo;
    }

    protected void drawCenteredText(Font font, String text, float y, Vector4f color) {
        float x = Math.round((currentWidth - font.getTextWidth(text)) / 2f);
        textRenderer.drawText(font, text, x, Math.round(y + font.getCapHeight()), color);
    }

    protected void drawText(Font font, String text, float x, float y, Vector4f color) {
        textRenderer.drawText(font, text, Math.round(x), Math.round(y + font.getCapHeight()), color);
    }

    protected boolean isBackPressed() {
        return context.actions().isActionPressed("back");
    }

    protected int getPalette() {
        return BackgroundRenderer.PALETTE_BLUE;
    }

    protected void updatePalette(float x, float y) {
        if (background.getPalette() != getPalette()) {
            background.setPalette(x, y, getPalette());
        }
    }

    protected void transitionTo(BaseScene scene, Button... buttons) {
        transitionTo(scene, List.of(), buttons);
    }

    protected void transitionTo(BaseScene scene, Collection<Sprite> sprites, Button... buttons) {
        nextScene = scene;
        transitionTime = 0f;
        slides.clear();

        for (Sprite sprite : sprites) {
            if (sprite == null) continue;
            Vector2f start = new Vector2f(sprite.getPosition());
            float travel = start.x + sprite.getTexture().getWidth() * sprite.getScale().x;
            addSlide(start, travel, SLIDE_TIME, sprite::setPosition);
        }

        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            Vector2f start = new Vector2f(button.getPosition());
            float travel = start.x + button.getSize().x;
            addSlide(start, travel, 0.4f, button::setPosition);
        }
    }
    protected Collection<Sprite> getTransitionSprites() {
        return List.of();
    }

    protected Button[] getTransitionButtons() {
        return new Button[0];
    }
    private void buildIntro() {
        intros.clear();
        introDuration = 0f;

        for (Sprite sprite : getTransitionSprites()) {
            if (sprite == null) continue;
            Vector2f target = new Vector2f(sprite.getPosition());
            float travel = currentWidth - target.x + sprite.getTexture().getWidth() * sprite.getScale().x;
            addIntro(target, travel, SLIDE_TIME, sprite::setPosition);
        }

        Button[] buttons = getTransitionButtons();
        for (int i = 0; i < buttons.length; i++) {
            Button button = buttons[i];
            Vector2f target = new Vector2f(button.getPosition());
            float travel = currentWidth - target.x + button.getSize().x;
            addIntro(target, travel, INTRO_BUTTON_TIME + 0.1f * i, button::setPosition);
        }

        introBuilt = true;
        for (Slide slide : intros) {
            slide.anim.update(introTime);   // keeps progress if the window is resized mid-intro
        }
        applyIntro();
    }

    private void addIntro(Vector2f target, float travel, float duration, PositionSetter setter) {
        Animation anim = new Animation(0f, 1f, EasingType.EXPONENTIAL_OUT, duration);
        anim.startAnimation();
        intros.add(new Slide(target, travel, anim, setter));
        introDuration = Math.max(introDuration, duration);
    }

    private void applyIntro() {
        for (Slide slide : intros) {
            float remaining = 1f - slide.anim.getValue();
            slide.setter.set(Math.round(slide.start.x + remaining * slide.travel), slide.start.y);
        }
    }

    private void updateIntro(float dt) {
        if (!introducing) return;
        if (!introBuilt) buildIntro();

        introTime += dt;
        for (Slide slide : intros) {
            slide.anim.update(dt);
        }

        if (introTime >= introDuration) {
            introducing = false;
            for (Slide slide : intros) {
                slide.setter.set(slide.start.x, slide.start.y);
            }
            intros.clear();
            return;
        }
        applyIntro();
    }

    private void addSlide(Vector2f start, float travel, float duration, PositionSetter setter) {
        Animation anim = new Animation(0f, 1f, EasingType.EXPONENTIAL_IN, duration);
        anim.startAnimation();
        slides.add(new Slide(start, travel, anim, setter));
    }

    protected boolean isTransitioning() {
        return nextScene != null || introducing;
    }

    protected void transitionTo(BaseScene scene) {
        transitionTo(scene, getTransitionSprites(), getTransitionButtons());
    }

    private void updateTransition(float dt) {
        float previousTime = transitionTime;
        transitionTime += dt;

        for (Slide slide : slides) {
            slide.anim.update(dt);
            slide.setter.set(Math.round(slide.start.x - slide.anim.getValue() * slide.travel), slide.start.y);
        }

        if (previousTime < PALETTE_SWITCH_TIME && transitionTime >= PALETTE_SWITCH_TIME) {
            background.setPalette(0, 0, nextScene.getPalette());
        }



        if (transitionTime >= TRANSITION_TIME) {
            context.sceneManager().changeScene(nextScene);
        }
    }

    protected abstract void onInit(AssetManager assets);
    protected abstract void onUpdate(float dt);
    protected abstract void onRender();
    protected abstract void onResize(int width, int height);
}