package nl.team3.games.tictactoe;

import java.util.Arrays;

public class Board {
    public static final int SIZE = 3;

    public enum Mark {
        X, O;

        public Mark opponent() {
            return this == X ? O : X;
        }
    }

    // All rows, columns and diagonals as {col, row}
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

    public void place(int col, int row) {
        if (!canPlace(col, row)) {
            throw new IllegalStateException("Cell " + col + "," + row + " is not available");
        }

        cells[col][row] = turn;
        moves++;

        winningLine = findLine(cells, turn);
        if (winningLine != null) {
            winner = turn;
        } else if (!isDraw()) {
            turn = turn.opponent();
        }
    }

    static int[][] findLine(Mark[][] cells, Mark mark) {
        for (int[][] line : LINES) {
            if (cells[line[0][0]][line[0][1]] == mark
                    && cells[line[1][0]][line[1][1]] == mark
                    && cells[line[2][0]][line[2][1]] == mark) {
                return line;
            }
        }
        return null;
    }

    Mark[][] copyCells() {
        Mark[][] copy = new Mark[SIZE][];
        for (int col = 0; col < SIZE; col++) {
            copy[col] = cells[col].clone();
        }
        return copy;
    }

    public Mark getCell(int col, int row) {
        return cells[col][row];
    }

    public Mark getTurn() {
        return turn;
    }

    public Mark getWinner() {
        return winner;
    }

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