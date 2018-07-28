package old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * A representation of the board as bit array would be better. Some analysis states that only 7 configurations are
 * possible for each cell:
 * <p>
 * Config Orbs Player
 * <p>
 * 0    0      0
 * <p>
 * 1    1      1
 * <p>
 * 2    2      1
 * <p>
 * 3    3      1
 * <p>
 * 4    1      2
 * <p>
 * 5    2      2
 * <p>
 * 6    3      2
 * <p>
 * So each board cell can be represented by log(7) base 2 => 3 bits. As there are 25 cells in a 5*5 board, each board
 * should require just 75 bits, or three integers.
 * However, due to performance and complexity considerations, I believe 4 bits per position is better. 2 for player
 * info and 2 for orb count. The practical reality was that none of these considerations worked well enough to reach
 * the final submission. However, if the bugs were fewer and I had more time, this was a good place to work on
 * efficiency.
 */
class Board {
    Function<int[], Integer> heuristicEval = (vals) -> Arrays.stream(vals).sum();
    private int[][][] board;
    private static final int BOARD_SIZE = 5;
    private static final int PLAYERS = 3;
    private static final int neighbours[][][] = new int[BOARD_SIZE][BOARD_SIZE][];
    final Move[][] moves = new Move[PLAYERS][BOARD_SIZE * BOARD_SIZE];
    final int[] choices = new int[PLAYERS];
    static final Move ALL_MOVES[][][] = new Move[PLAYERS][BOARD_SIZE][BOARD_SIZE];

    /**
     * Creates a new board using the given board array to initialize move lists and counters.
     *
     * @param board the game board
     */
    Board(final int[][][] board) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                moves[board[i][j][0]][choices[board[i][j][0]]++] = ALL_MOVES[board[i][j][0]][i][j];
            }
        }
        this.board = getCopy(board);
    }

    /**
     * Completely copies a board onto another.
     *
     * @param board   Original Board
     * @param moves   Original Move list
     * @param choices Original Player Cell counter
     */
    private Board(final int[][][] board, final Move[][] moves, final int choices[]) {
        System.arraycopy(choices, 0, this.choices, 0, choices.length);
        for (int i = 0; i < PLAYERS; i++) {
            System.arraycopy(moves[i], 0, this.moves[i], 0, choices[i]);
        }
        this.board = getCopy(board);
    }

    /**
     * Sets all the neighbours of each possible cell in the chain reaction board. This method runs only once for each
     * game.
     */
    static void setNeighbours() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                final long x = i * BOARD_SIZE + j;
                final List<Long> near = new ArrayList<>();
                near.add(x + 1);
                near.add(x + BOARD_SIZE);
                near.add(x - 1);
                near.add(x - BOARD_SIZE);
                if (i == 0) {
                    near.remove(x - BOARD_SIZE);
                }
                if (j == 0) {
                    near.remove(x - 1);
                }
                if (i == BOARD_SIZE - 1) {
                    near.remove(x + BOARD_SIZE);
                }
                if (j == BOARD_SIZE - 1) {
                    near.remove(x + 1);
                }
                neighbours[i][j] = new int[near.size()];
                for (int k = 0; k < near.size(); k++) {
                    if (near.get(k) >= 0 && near.get(k) <= BOARD_SIZE * BOARD_SIZE) {
                        neighbours[i][j][k] = Math.toIntExact(near.get(k));
                    }
                }
            }
        }
    }

    /**
     * Make a move returning a new board. Method <b>is</b> idempotent.
     *
     * @param move Move to be played
     * @return New board with move played.
     */
    Board makeMove(final Move move) {
        return getCopy().play(move);
    }

    /**
     * Plays a move on the current board, updating the state and respective variables. If it looks complicated, thats
     * because it is.
     *
     * @param move The move played on the board.
     * @return The changed board. This operation is <b>NOT</b> idempotent.
     */
    private Board play(final Move move) {
        if (board[move.x][move.y][0] == MinMax.flip(move.player)) {
            //We just captured an opponents block. Updating move list and counters
            final int opponent = MinMax.flip(move.player);
            int index;
            for (index = choices[opponent] - 1; index >= 0; index--) {
                if (moves[opponent][index].x == move.x && moves[opponent][index].y == move.y) {
                    break;
                }
            }
            moves[opponent][index] = moves[opponent][choices[opponent] - 1];
            choices[opponent]--;
            moves[move.player][choices[move.player]++] = ALL_MOVES[move.player][move.x][move.y];
        } else if (board[move.x][move.y][0] == 0) {
            //We just captured an an empty block. Updating move list and counters
            int index;
            for (index = choices[0] - 1; index >= 0; index--) {
                if (moves[0][index].x == move.x && moves[0][index].y == move.y) {
                    break;
                }
            }
            moves[0][index] = moves[0][choices[0] - 1];
            choices[0]--;
            moves[move.player][choices[move.player]++] = ALL_MOVES[move.player][move.x][move.y];
        }
        //Else we played in our own cell. No updates needed, except to increment cell count as always
        board[move.x][move.y][0] = move.player;
        board[move.x][move.y][1]++;
        if (terminalValue() != null) {
            return this;
        }
        /*
         * Checks if an explosion needed.
         */
        if (neighbours[move.x][move.y].length <= board[move.x][move.y][1]) {
            board[move.x][move.y][1] = board[move.x][move.y][1] - neighbours[move.x][move.y].length;
            if (board[move.x][move.y][1] == 0) {
                //Set he cell to blank and update move lists
                board[move.x][move.y][0] = 0;
                int index;
                for (index = choices[move.player] - 1; index >= 0; index--) {
                    if (moves[move.player][index].x == move.x && moves[move.player][index].y == move.y) {
                        break;
                    }
                }
                moves[move.player][index] = moves[move.player][choices[move.player] - 1];
                choices[move.player]--;
                moves[0][choices[0]++] = ALL_MOVES[0][move.x][move.y];
            }
            explode(move.x, move.y, move.player);
        }
        return this;
    }

    /**
     * Explode the cell at the specified position. All neighbouring cells are acted upon as if a move was played on
     * them.
     *
     * @param x      X coordinate
     * @param y      Y coordinate
     * @param player Player who caused the explosion
     */
    private void explode(final int x, final int y, final int player) {
        for (final int neighbour : neighbours[x][y]) {
            play(ALL_MOVES[player][neighbour / BOARD_SIZE][neighbour % BOARD_SIZE]);
        }
    }

    /**
     * Used to check if the given board position is terminal.
     *
     * @return An integer value if the position is a terminal position. Else return null.
     */
    Integer terminalValue() {
        if (((choices[1] | choices[2]) > 1) && (choices[1] == 0 || choices[2] == 0)) {
            return choices[1] == 0 ? MinMax.MIN_VALUE : MinMax.MAX_VALUE;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return Arrays.deepToString(board);
    }

    /**
     * It takes the difference in number of cells and add the difference in explosives.
     *
     * @param player Player to move
     * @return Heuristic value of the board
     */

    int heuristicValue(final int player) {
        final int opponent = MinMax.flip(player);
        int orbs = choices[player] - choices[opponent];
        int explosives = 0;
        for (int m = 0; m < choices[player]; m++) {
            final int i = moves[player][m].x;
            final int j = moves[player][m].y;
            if (board[i][j][1] == neighbours[i][j].length - 1) {
                explosives++;
            }
        }
        for (int m = 0; m < choices[opponent]; m++) {
            final int i = moves[opponent][m].x;
            final int j = moves[opponent][m].y;
            if (board[i][j][1] == neighbours[i][j].length - 1) {
                explosives--;
            }
        }
        return orbs + explosives;
    }

    /**
     * Returns a copy of the board state. Skips copying the zeros of the original.
     *
     * @param board The original board representation
     * @return A new board array having all the copied elements
     */

    private int[][][] getCopy(final int board[][][]) {
        final int copyBoard[][][] = new int[board.length][board.length][2];
        for (int k = 1; k < PLAYERS; k++) {
            for (int l = 0; l < choices[k]; l++) {
                final int i = moves[k][l].x;
                final int j = moves[k][l].y;
                System.arraycopy(board[i][j], 0, copyBoard[i][j], 0, board[i][j].length);
            }
        }
        return copyBoard;
    }

    /**
     * Initializes the moves array with static objects. These are the only move objects created in the entire game.
     */
    static void setMoves() {
        for (int player = 0; player < PLAYERS; player++) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    ALL_MOVES[player][i][j] = new Move(i, j, player);
                }
            }
        }
    }

    /**
     * Necessary to keep the preserve the state of the board when searching in the min-max tree.
     *
     * @return A copy of the board. The copy refers to none of the mutable objects being referred to by the original.
     */
    Board getCopy() {
        return new Board(board, moves, choices);
    }
}
