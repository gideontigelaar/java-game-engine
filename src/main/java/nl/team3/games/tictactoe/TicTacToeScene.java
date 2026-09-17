package nl.team3.games.tictactoe;

import nl.team3.engine.core.Scene;

public class TicTacToeScene implements Scene {

    @Override
    public void init() {
        System.out.println("TicTacToeScene loaded");
    }

    @Override
    public void update(float dt) {
        // Game logic
    }

    @Override
    public void render() {
        // OpenGL calls
    }

    @Override
    public void cleanup() {
        System.out.println("TicTacToeScene closed");
    }
}