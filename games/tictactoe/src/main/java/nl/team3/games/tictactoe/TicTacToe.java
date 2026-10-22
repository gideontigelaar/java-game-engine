package nl.team3.games.tictactoe;

import nl.team3.engine.core.Engine;
import nl.team3.engine.core.EngineConfig;
import nl.team3.engine.input.Keys;
import nl.team3.games.tictactoe.scenes.MainMenuScene;
import org.joml.Vector4f;

public final class TicTacToe {
    private TicTacToe() {
    }

    public static void main(String[] args) {
        EngineConfig config = EngineConfig.builder("Tic-tac-toe")
                .windowSize(1920, 1080)
                .minWindowSize(960, 540)
                .minGuiSize(1280, 720)
                .clearColor(new Vector4f(0.62f, 0.62f, 0.62f, 1.0f))
                .build();

        Engine.run(config, context -> {
            context.actions().bind("back", Keys.ESCAPE);

            // Start in Main Menu instead of directly in the game
            return new MainMenuScene(context);
        });
    }
}