package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.EngineContext;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.Settings;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// Shared layout so the logo and header stay put when switching between menus
public abstract class MenuScene extends BaseScene {
    private static final int MAX_BUTTONS = 4;
    private static final int STATUS_LINES = 2;
    private static final float STATUS_TIME = 3f;

    private final List<Button> buttons = new ArrayList<>();
    private Sprite logo;
    private Sprite profile;
    private boolean profileVisible;
    private boolean logoStays;

    private String status = "";
    private Vector4f statusColor = ERROR_COLOR;
    private float statusTimer;

    protected String playerName;
    protected float nameY;
    protected float detailY;
    private float statusY;

    protected MenuScene(EngineContext context, String status) {
        super(context);
        if (!status.isEmpty()) {
            setStatus(status, ERROR_COLOR, STATUS_TIME);
        }
    }

    @Override
    protected final void onInit(AssetManager assets) {
        playerName = Settings.getPlayerName();
        logo = createLogo();
        profile = new Sprite(assets.loadTexture("anonUserProfile", "/games/tictactoe/textures/anonUserProfile.png"));
        profile.setScale(UI_SCALE);
        initMenu(assets);
    }

    protected abstract void initMenu(AssetManager assets);

    protected void updateMenu(float dt) {
    }

    protected void onBack() {
    }

    protected String getDetail() {
        return "";
    }

    protected Sprite getDetailIcon() {
        return null;
    }

    protected void renderHeader() {
        drawCenteredText(smallFont, fitText(smallFont, playerName, getMaxTextWidth()), nameY, WHITE);

        Sprite icon = getDetailIcon();
        if (icon == null) {
            drawCenteredText(smallFont, fitText(smallFont, getDetail(), getMaxTextWidth()), detailY, DIM_WHITE);
            return;
        }

        float iconWidth = icon.getTexture().getWidth() * icon.getScale().x;
        String detail = fitText(smallFont, getDetail(), getMaxTextWidth() - iconWidth - UI_SPACING / 2f);
        float x = Math.round((currentWidth - iconWidth - UI_SPACING / 2f - smallFont.getTextWidth(detail)) / 2f);
        icon.setPosition(x + iconWidth / 2f + getContentOffset(), detailY + smallFont.getCapHeight() / 2f);
        spriteRenderer.draw(icon);
        drawText(smallFont, detail, x + iconWidth + UI_SPACING / 2f, detailY, DIM_WHITE);
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

    @Override
    protected Collection<Sprite> getTransitionSprites() {
        return logoStays ? List.of() : getHeaderSprites();
    }

    @Override
    protected Button[] getTransitionButtons() {
        return buttons.toArray(new Button[0]);
    }

    protected void open(BaseScene scene) {
        if (scene instanceof MenuScene next) {
            next.logoStays = true;   // logo and header stay put between menus
            transitionTo(scene, List.of(), getTransitionButtons());
        } else {
            transitionTo(scene, getHeaderSprites(), getTransitionButtons());
        }
    }

    private List<Sprite> getHeaderSprites() {
        List<Sprite> sprites = new ArrayList<>();
        sprites.add(logo);
        if (profileVisible) sprites.add(profile);
        return sprites;
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
        if (isLeaving()) return;

        updateMenu(dt);
        for (Button button : buttons) {
            if (isLeaving()) return;
            button.update(input);
        }
        if (!isLeaving() && isBackPressed()) {
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
            drawWrappedText(smallFont, status, statusY, getMaxTextWidth(), statusColor);
        }
    }

    @Override
    protected final void onResize(int width, int height) {
        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        float profileHeight = profile.getTexture().getHeight() * UI_SCALE + UI_SPACING;
        float lineHeight = smallFont.getCapHeight();
        float statusHeight = STATUS_LINES * (lineHeight + LINE_GAP) - LINE_GAP;

        float columnHeight = logoHeight + 2 * lineHeight + LINE_GAP + UI_SPACING
                + MAX_BUTTONS * (BUTTON_HEIGHT + UI_SPACING) + statusHeight;
        profileVisible = columnHeight + profileHeight + 2 * UI_SPACING <= height;
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