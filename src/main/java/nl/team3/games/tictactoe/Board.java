package nl.team3.games.tictactoe;

import java.util.Arrays;

// Tic-tac-toe rules: whose turn it is, three in a row and draws
public class Board {
    public static final int SIZE = 3;

    public enum Mark { X, O }

    // Every row, column and diagonal as {col, row} cells
    private static final int[][][] LINES = {
            {{0, 0}, {1, 0}, {2, 0}}, {{0, 1}, {1, 1}, {2, 1}}, {{0, 2}, {1, 2}, {2, 2}},
            {{0, 0}, {0, 1}, {0, 2}}, {{1, 0}, {1, 1}, {1, 2}}, {{2, 0}, {2, 1}, {2, 2}},
            {{0, 0}, {1, 1}, {2, 2}}, {{2, 0}, {1, 1}, {0, 2}}
    };

    private final Mark[][] cells = new Mark[SIZE][SIZE];
    private Mark turn;
    private Mark winner;
    private int[][] winningLine;
    private int moves;

    public Board(Mark firstTurn) {
        this.turn = firstTurn;
    }

    public boolean canPlace(int col, int row) {
        return !isGameOver() && cells[col][row] == null;
    }

    // Place a mark for the player whose turn it is
    public void place(int col, int row) {
        if (!canPlace(col, row)) {
            throw new IllegalStateException("Cell " + col + "," + row + " is not available");
        }

        cells[col][row] = turn;
        moves++;

        for (int[][] line : LINES) {
            if (isLineOf(line, turn)) {
                winner = turn;
                winningLine = line;
                return;
            }
        }

        if (!isDraw()) {
            turn = turn == Mark.X ? Mark.O : Mark.X;
        }
    }

    private boolean isLineOf(int[][] line, Mark mark) {
        for (int[] cell : line) {
            if (cells[cell[0]][cell[1]] != mark) {
                return false;
            }
        }
        return true;
    }

    public Mark getCell(int col, int row) {
        return cells[col][row];
    }

    public Mark getTurn() {
        return turn;
    }

    // Null while nobody has three in a row
    public Mark getWinner() {
        return winner;
    }

    // The three {col, row} cells of the win, null without a winner
    public int[][] getWinningLine() {
        return winningLine == null ? null : Arrays.stream(winningLine).map(int[]::clone).toArray(int[][]::new);
    }

    public boolean isDraw() {
        return winner == null && moves == SIZE * SIZE;
    }

    public boolean isGameOver() {
        return winner != null || isDraw();
    }
}