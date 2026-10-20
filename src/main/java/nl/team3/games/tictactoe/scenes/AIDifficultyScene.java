package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.Board;

public class AIDifficultyScene extends BaseScene {
    private Sprite logo;
    private Button btnUnbeatable;
    private Button btnNormal;
    private Button btnBack;

    public AIDifficultyScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        super(input, actions, sceneManager, null);
    }

    @Override
    protected void onInit(AssetManager assets) {
        logo = createLogo();

        btnUnbeatable = createButton("UNBEATABLE AI", BUTTON_WIDTH, () -> startAIMatch(true));
        btnNormal = createButton("50 / 50 AI", BUTTON_WIDTH, () -> startAIMatch(false));
        btnBack = createButton("BACK", SMALL_BUTTON_WIDTH, () -> transitionTo(new MainMenuScene(input, actions, sceneManager), btnUnbeatable, btnNormal, btnBack));
    }

    private void startAIMatch(boolean unbeatable) {
        transitionTo(new MatchScene(input, actions, sceneManager, null, Board.Mark.X, Board.Mark.X, true, unbeatable), btnUnbeatable, btnNormal, btnBack);
    }

    @Override
    protected void onUpdate(float dt) {
        if (isTransitioning()) return;

        btnUnbeatable.update(input);
        btnNormal.update(input);
        btnBack.update(input);
    }

    @Override
    protected void onRender() {
        spriteRenderer.draw(logo);

        btnUnbeatable.render(spriteRenderer, textRenderer);
        btnNormal.render(spriteRenderer, textRenderer);
        btnBack.render(spriteRenderer, textRenderer);
    }

    @Override
    protected void onResize(int width, int height) {
        float logoHeight = logo.getTexture().getHeight() * UI_SCALE;
        // Gebruik exact dezelfde hoogte en gap-formule als MainMenuScene
        // MainMenu heeft: profileHeight (32*UI_SCALE), usernameInput line, 4 buttons
        float profileHeight = 32f * UI_SCALE; // Standaard hoogte van het profielicoon in MainMenu
        float lineHeight = smallFont.getCapHeight();

        float columnHeight = logoHeight + profileHeight + UI_SPACING + lineHeight
                + UI_SPACING + 4 * BUTTON_HEIGHT + 4 * UI_SPACING + lineHeight;
        float logoGap = Math.max(UI_SPACING, Math.min(2 * UI_SPACING, height - columnHeight - 2 * UI_SPACING));
        float centerX = Math.round(width / 2f);
        float y = Math.max(0, Math.round((height - columnHeight - logoGap) / 2f));

        // Het logo wordt exact zo geplaatst als in MainMenuScene
        logo.setPosition(centerX, y + logoHeight / 2f);
        y += logoHeight + logoGap;

        // Sla de ruimte over die MainMenu gebruikt voor profiel + input field
        y += profileHeight + UI_SPACING + lineHeight + UI_SPACING;

        // Positioneer de knoppen van het AI menu
        btnUnbeatable.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;

        btnNormal.setPosition(centerX - BUTTON_WIDTH / 2f, y);
        y += BUTTON_HEIGHT + UI_SPACING;

        btnBack.setPosition(centerX - SMALL_BUTTON_WIDTH / 2f, y);
    }
}