package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.games.tictactoe.AIOpponent;

public class OfflineMenuScene extends MenuScene {
    public OfflineMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager) {
        super(input, actions, sceneManager, "");
    }

    @Override
    protected void initMenu() {
        addButton("PLAYER VS PLAYER", () -> open(new MatchScene(input, actions, sceneManager)));
        addButton("50 / 50 AI", () -> playAI(AIOpponent.Difficulty.FIFTY_FIFTY));
        addButton("UNBEATABLE AI", () -> playAI(AIOpponent.Difficulty.UNBEATABLE));
        addSmallButton("BACK", this::onBack);
    }

    private void playAI(AIOpponent.Difficulty difficulty) {
        open(new MatchScene(input, actions, sceneManager, new AIOpponent(difficulty)));
    }

    @Override
    protected void onBack() {
        open(new MainMenuScene(input, actions, sceneManager));
    }
}