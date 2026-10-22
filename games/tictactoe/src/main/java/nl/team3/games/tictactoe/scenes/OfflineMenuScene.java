package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.core.EngineContext;
import nl.team3.games.tictactoe.AIOpponent;

public class OfflineMenuScene extends MenuScene {
    public OfflineMenuScene(EngineContext context) {
        super(context, "");
    }

    @Override
    protected void initMenu() {
        addButton("PLAYER VS PLAYER", () -> open(new MatchScene(context)));
        addButton("50 / 50 AI", () -> playAI(AIOpponent.Difficulty.FIFTY_FIFTY));
        addButton("UNBEATABLE AI", () -> playAI(AIOpponent.Difficulty.UNBEATABLE));
        addSmallButton("BACK", this::onBack);
    }

    private void playAI(AIOpponent.Difficulty difficulty) {
        open(new MatchScene(context, new AIOpponent(difficulty)));
    }

    @Override
    protected void onBack() {
        open(new MainMenuScene(context));
    }
}