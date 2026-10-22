package nl.team3.games.tictactoe;

import nl.team3.engine.core.Engine;
import nl.team3.games.tictactoe.scenes.MainMenuScene;
import org.lwjgl.glfw.GLFW;

public final class TicTacToe {
    private TicTacToe() {
    }

    public static void main(String[] args) {
        Engine.run("Tic-tac-toe", (input, actions, sceneManager) -> {
            actions.bind("back", GLFW.GLFW_KEY_ESCAPE);

            // Start in Main Menu instead of directly in the game
            return new MainMenuScene(input, actions, sceneManager);
        });
    }
}