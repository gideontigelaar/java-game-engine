package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.Settings;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

// Shared layout so the logo and header stay put when switching between menus
public abstract class MenuScene extends BaseScene {
    protected static final float LINE_GAP = 12f;

    private static final int MAX_BUTTONS = 4;
    private static final float STATUS_TIME = 3f;

    private final List<Button> buttons = new ArrayList<>();
    private Sprite logo;
    private Sprite profile;
    private boolean profileVisible;

    private String status = "";
    private Vector4f statusColor = ERROR_COLOR;
    private float statusTimer;

    protected String playerName;
    protected float nameY;
    protected float detailY;
    private float statusY;

    protected MenuScene(InputManager input, ActionMap actions, SceneManager sceneManager, String status) {
        super(input, actions, sceneManager);
        if (!status.isEmpty()) {
            setStatus(status, ERROR_COLOR, STATUS_TIME);
        }
    }

    @Override
    protected final void onInit(AssetManager assets) {
        playerName = Settings.getPlayerName();
        logo = createLogo();
        profile = new Sprite(assets.loadTexture("anonUserProfile", "/textures/tictactoe/anonUserProfile.png"));
        profile.setScale(UI_SCALE);
        initMenu();
    }

    protected abstract void initMenu();

    protected void updateMenu(float dt) {
    }

    protected void onBack() {
    }

    protected String getDetail() {
        return "";
    }

    protected void renderHeader() {
        drawCenteredText(smallFont, playerName, nameY, WHITE);
        drawCenteredText(smallFont, getDetail(), detailY, DIM_WHITE);
    }

    protected void layoutHeader(float centerX) {
    }

    protected Button addButton(String text, Runnable onClick) {
        Button button = createButton(text, BUTTON_WIDTH, onClick);
        buttons.add(button);
        return button;
    }

    protected Button addSmallButton(String text, Runnable onClick) {
        Button button = createButton(text, SMALL_BUTTON_WIDTH, onClick);
        buttons.add(button);
        return button;
    }

    protected void open(BaseScene scene) {
        transitionTo(scene, buttons.toArray(new Button[0]));
    }

    protected void showStatus(String message) {
        setStatus(message, ERROR_COLOR, STATUS_TIME);
    }

    protected void showProgress(String message) {
        setStatus(message, WHITE, Float.POSITIVE_INFINITY);
    }

    protected void clearStatus() {
        statusTimer = 0f;
    }

    private void setStatus(String message, Vector4f color, float time) {
        status = message;
        statusColor = color;
        statusTimer = time;
    }

    @Override
    protected final void onUpdate(float dt) {
        statusTimer = Math.max(0f, statusTimer - dt);
        if (isTransitioning()) return;

        updateMenu(dt);
        for (Button button : buttons) {
            if (isTransitioning()) return;
            button.update(input);
        }
        if (!isTransitioning() && isBackPressed()) {
            onBack();
        }
    }

    @Override
    protected final void onRender() {
        spriteRenderer.draw(logo);
        if (profileVisible) {
            spriteRenderer.draw(profile);
        }
        renderHeader();
        for (Button button : buttons) {
            button.render(spriteRenderer, textRenderer);
        }
        if (statusTimer > 0f) {
            drawCenteredText(smallFont, status, statusY, statusColor);
        }
    }

    @Override
    protected final void onResize(int width, int height) {
        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        float profileHeight = profile.getTexture().getHeight() * UI_SCALE + UI_SPACING;
        float lineHeight = smallFont.getCapHeight();

        float columnHeight = logoHeight + 2 * lineHeight + LINE_GAP + UI_SPACING
                + MAX_BUTTONS * (BUTTON_HEIGHT + UI_SPACING) + lineHeight;
        profileVisible = columnHeight + profileHeight + 4 * UI_SPACING <= height;
        if (profileVisible) {
            columnHeight += profileHeight;
        }

        float logoGap = Math.max(UI_SPACING, Math.min(2 * UI_SPACING, height - columnHeight - 2 * UI_SPACING));
        float centerX = Math.round(width / 2f);
        float y = Math.max(0, Math.round((height - columnHeight - logoGap) / 2f));

        logo.setPosition(centerX, y + logoHeight / 2f);
        y += logoHeight + logoGap;

        if (profileVisible) {
            profile.setPosition(centerX, y + (profileHeight - UI_SPACING) / 2f);
            y += profileHeight;
        }

        nameY = y;
        y += lineHeight + LINE_GAP;
        detailY = y;
        y += lineHeight + UI_SPACING;

        for (Button button : buttons) {
            button.setPosition(centerX - button.getSize().x / 2f, y);
            y += BUTTON_HEIGHT + UI_SPACING;
        }
        statusY = y;

        layoutHeader(centerX);
    }
}