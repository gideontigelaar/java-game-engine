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
                .windowSize(720, 720)
                .minWindowSize(640, 640)
                .minGuiSize(640, 640)
                .clearColor(new Vector4f(0.62f, 0.62f, 0.62f, 1.0f))
                .build();

        Engine.run(config, context -> {
            context.actions().bind("back", Keys.ESCAPE);

            return new MainMenuScene(context);
        });
    }
}