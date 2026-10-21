package nl.team3.games.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AIOpponent {
    public enum Difficulty { FIFTY_FIFTY, UNBEATABLE }

    private static final int WIN_SCORE = 10;
    private static final int INFINITY = 100;

    private final Difficulty difficulty;

    public AIOpponent(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int[] chooseMove(Board board) {
        if (board.isGameOver()) {
            return null;
        }

        Board.Mark[][] cells = board.copyCells();
        List<int[]> free = freeCells(cells);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (difficulty == Difficulty.FIFTY_FIFTY && random.nextBoolean()) {
            return free.get(random.nextInt(free.size()));
        }

        Board.Mark mark = board.getTurn();
        List<int[]> best = new ArrayList<>();
        int bestScore = -INFINITY;
        for (int[] cell : free) {
            cells[cell[0]][cell[1]] = mark;
            int score = -negamax(cells, mark.opponent(), 1, -INFINITY, INFINITY);
            cells[cell[0]][cell[1]] = null;

            if (score > bestScore) {
                bestScore = score;
                best.clear();
            }
            if (score == bestScore) {
                best.add(cell);
            }
        }
        // Pick between good moves so each game doesn't look the same
        return best.get(random.nextInt(best.size()));
    }

    private static int negamax(Board.Mark[][] cells, Board.Mark toMove, int depth, int alpha, int beta) {
        if (Board.findLine(cells, toMove.opponent()) != null) {
            return depth - WIN_SCORE;
        }

        int best = -INFINITY;
        boolean moved = false;
        for (int col = 0; col < Board.SIZE; col++) {
            for (int row = 0; row < Board.SIZE; row++) {
                if (cells[col][row] != null) continue;

                moved = true;
                cells[col][row] = toMove;
                int score = -negamax(cells, toMove.opponent(), depth + 1, -beta, -alpha);
                cells[col][row] = null;

                best = Math.max(best, score);
                alpha = Math.max(alpha, score);
                if (alpha >= beta) {
                    return best;
                }
            }
        }
        return moved ? best : 0;
    }

    private static List<int[]> freeCells(Board.Mark[][] cells) {
        List<int[]> free = new ArrayList<>();
        for (int col = 0; col < Board.SIZE; col++) {
            for (int row = 0; row < Board.SIZE; row++) {
                if (cells[col][row] == null) {
                    free.add(new int[]{col, row});
                }
            }
        }
        return free;
    }
}