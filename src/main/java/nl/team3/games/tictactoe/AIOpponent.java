package nl.team3.games.tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class AIOpponent {

    public static int[] calculateBestMove(Board board, Board.Mark aiMark, boolean unbeatable) {
        if (!unbeatable && ThreadLocalRandom.current().nextBoolean()) {
            List<int[]> availableMoves = getAvailableMoves(board);
            if (!availableMoves.isEmpty()) {
                return availableMoves.get(ThreadLocalRandom.current().nextInt(availableMoves.size()));
            }
        }

        Board.Mark opponentMark = (aiMark == Board.Mark.X) ? Board.Mark.O : Board.Mark.X;
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (board.canPlace(c, r)) {
                    board.cells[c][r] = aiMark;
                    int score = minimax(board, 0, false, aiMark, opponentMark);
                    board.cells[c][r] = null;
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{c, r};
                    }
                }
            }
        }

        return bestMove;
    }

    private static List<int[]> getAvailableMoves(Board board) {
        List<int[]> moves = new ArrayList<>();
        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (board.getCell(c, r) == null && !board.isGameOver()) {
                    moves.add(new int[]{c, r});
                }
            }
        }
        return moves;
    }

    private static int minimax(Board board, int depth, boolean isMaximizing, Board.Mark aiMark, Board.Mark opponentMark) {
        Board.Mark winner = checkWinner(board);
        if (winner == aiMark) return 10 - depth;
        if (winner == opponentMark) return depth - 10;
        if (isBoardFull(board)) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int c = 0; c < Board.SIZE; c++) {
                for (int r = 0; r < Board.SIZE; r++) {
                    if (board.cells[c][r] == null) {
                        board.cells[c][r] = aiMark;
                        int score = minimax(board, depth + 1, false, aiMark, opponentMark);
                        board.cells[c][r] = null;
                        bestScore = Math.max(score, bestScore);
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int c = 0; c < Board.SIZE; c++) {
                for (int r = 0; r < Board.SIZE; r++) {
                    if (board.cells[c][r] == null) {
                        board.cells[c][r] = opponentMark;
                        int score = minimax(board, depth + 1, true, aiMark, opponentMark);
                        board.cells[c][r] = null;
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
            return bestScore;
        }
    }

    private static Board.Mark checkWinner(Board board) {
        int[][][] lines = {
                {{0, 0}, {1, 0}, {2, 0}}, {{0, 1}, {1, 1}, {2, 1}}, {{0, 2}, {1, 2}, {2, 2}},
                {{0, 0}, {0, 1}, {0, 2}}, {{1, 0}, {1, 1}, {1, 2}}, {{2, 0}, {2, 1}, {2, 2}},
                {{0, 0}, {1, 1}, {2, 2}}, {{2, 0}, {1, 1}, {0, 2}}
        };
        for (int[][] line : lines) {
            Board.Mark m1 = board.cells[line[0][0]][line[0][1]];
            Board.Mark m2 = board.cells[line[1][0]][line[1][1]];
            Board.Mark m3 = board.cells[line[2][0]][line[2][1]];
            if (m1 != null && m1 == m2 && m2 == m3) {
                return m1;
            }
        }
        return null;
    }

    private static boolean isBoardFull(Board board) {
        for (int c = 0; c < Board.SIZE; c++) {
            for (int r = 0; r < Board.SIZE; r++) {
                if (board.cells[c][r] == null) return false;
            }
        }
        return true;
    }
}