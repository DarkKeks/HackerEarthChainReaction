package old;

import java.util.Arrays;
import java.util.concurrent.TimeoutException;

/**
 * Contains a lot of objects for metrics. Should ideally be separated from those responsibilities.
 */
class MinMax {
    private static final int MAX_DEPTH = 60;
    public static int TIME_OUT = 1280;
    public int computations = 0, depth = 4, moves = 0;
    public long eval = 0;
    static final int MAX_VALUE = 1000000, MIN_VALUE = -MAX_VALUE;
    private final long startTime = System.currentTimeMillis();
    private boolean test;
    private Configuration[] startConfigs;
    private final Move[][] killerMoves = new Move[MAX_DEPTH][2];
    private final int[][] efficiency = new int[MAX_DEPTH][2];

    public MinMax() {
        Board.setMoves();
        Board.setNeighbours();
    }

    private boolean timeOut;

    /**
     * Iterative deepening is implemented for flexible depth search. Also, it allows us to rearrange all moves as per
     * (known) optimal ordering after each iteration. This is important because alpha-beta
     * performs best when given a good move order.
     * On the final iteration, when an exception is thrown, the best move will be propagated upwards from the
     * {@link #findBestMove} method.
     */
    public String iterativeSearchForBestMove(final int[][][] game, final int player) {
        final Board board = new Board(game);
        if (board.choices[player] + board.choices[0] == 0) {
            throw new RuntimeException("No possible moves");
        }
        startConfigs = new Configuration[board.choices[player] + board.choices[0]];
        for (int i = 0; i < board.choices[0]; i++) {
            startConfigs[i] = new Configuration(board.moves[0][i], board, player, 0);
        }
        for (int i = 0; i < board.choices[player]; i++) {
            startConfigs[i + board.choices[0]] = new Configuration(board.moves[player][i], board, player, 0);
        }
        Arrays.sort(startConfigs);
        Move bestMove = startConfigs[0].move;
        while (depth < MAX_DEPTH && !timeOut) {
            bestMove = findBestMove(player, 0);
            depth++;
        }
        eval = startConfigs[0].strength;
        moves = board.choices[player] + board.choices[0];
        return bestMove.describe();
    }

    /**
     * Returns the best known move till now for the entire board.
     *
     * @param player Player to play
     * @param level  Current Level
     * @return Best move found
     */
    private Move findBestMove(final int player, final int level) {
        long toTake = MIN_VALUE, toGive = MAX_VALUE;
        int max = MIN_VALUE;
        Move bestMove = startConfigs[0].move;
        try {
            for (final Configuration possibleConfig : startConfigs) {
                final int moveValue = evaluate(possibleConfig.board.getCopy(),
                        flip(player),
                        level,
                        toTake,
                        toGive,
                        -possibleConfig.strength);
                possibleConfig.strength = moveValue;
                if (player == 1) {
                    if (toTake < moveValue) {
                        toTake = moveValue;
                    }
                } else {
                    if (toGive > -moveValue) {
                        toGive = -moveValue;
                    }
                }
                if (moveValue > max) {
                    max = moveValue;
                    bestMove = possibleConfig.move;
                    if (Math.abs(max - MAX_VALUE) <= 100) {
                        break;
                    }
                }
                if (toTake >= toGive) {
                    if (possibleConfig.killer) {
                        if (killerMoves[level][0] == possibleConfig.move) {
                            efficiency[level][0]++;
                        } else {
                            efficiency[level][1]++;
                            if (efficiency[level][0] < efficiency[level][1]) {
                                final Move temp = killerMoves[level][0];
                                killerMoves[level][0] = killerMoves[level][1];
                                killerMoves[level][1] = temp;
                            }
                        }
                    } else {
                        if (killerMoves[level][0] == null) {
                            killerMoves[level][0] = possibleConfig.move;
                            efficiency[level][0] = 1;
                        } else if (killerMoves[level][1] == null) {
                            killerMoves[level][1] = possibleConfig.move;
                            efficiency[level][1] = 1;
                        }
                    }
                    break;
                } else if (possibleConfig.killer) {
                    if (killerMoves[level][0] == possibleConfig.move) {
                        efficiency[level][0]--;
                    } else {
                        efficiency[level][1]--;
                    }
                    if (efficiency[level][0] < efficiency[level][1]) {
                        final Move temp = killerMoves[level][0];
                        killerMoves[level][0] = killerMoves[level][1];
                        killerMoves[level][1] = temp;
                    }
                    if (efficiency[level][1] <= 0) {
                        efficiency[level][1] = 0;
                        killerMoves[level][1] = null;
                    }
                }
            }
        } catch (TimeoutException e) {
            timeOut = true;
        }
        Arrays.sort(startConfigs);
        return bestMove;
    }

    /**
     * Min Max tree generator and traverse.  Implements Alpha Beta along with the killer heuristic.
     *
     * @param board          Input Board. All branches in the Min Max Tree from this node are possible moves from this board.
     * @param player         Player making the move.
     * @param level          Depth on which this tree is now.
     * @param a              Alpha
     * @param b              Beta
     * @param heuristicValue The heuristic value of board
     * @return The value of current board position
     * @throws TimeoutException if it runs out of time.
     */
    private int evaluate(final Board board,
                         final int player,
                         final int level,
                         final long a,
                         final long b,
                         final int heuristicValue) throws TimeoutException {
        long toTake = a, toGive = b;
        int max = MIN_VALUE;
        if (!test && System.currentTimeMillis() - startTime >= TIME_OUT) {
            throw new TimeoutException("Time out...");
        }
        final Integer terminalValue;
        if ((terminalValue = board.terminalValue()) != null) {
            max = terminalValue * ((-player << 1) + 3);
            max += max < 0 ? level : -level;
        } else if (level >= depth) {
            max = heuristicValue;
        } else {
            final Configuration[] configurations = new Configuration[board.choices[player] + board.choices[0]];
            for (int i = 0; i < board.choices[0]; i++) {
                configurations[i] = new Configuration(board.moves[0][i], board, player, level);
            }
            for (int i = 0; i < board.choices[player]; i++) {
                configurations[i + board.choices[0]] = new Configuration(board.moves[player][i],
                        board,
                        player,
                        level);
            }
            Arrays.sort(configurations);
            int index = 0;
            for (; index < configurations.length; index++) {
                final Configuration possibleConfig = configurations[index];
                computations++;
                final int moveValue = evaluate(possibleConfig.board,
                        flip(player),
                        level + 1,
                        toTake,
                        toGive,
                        -possibleConfig.strength);
                if (player == 1) {
                    if (toTake < moveValue) {
                        toTake = moveValue;
                    }
                } else {
                    if (toGive > -moveValue) {
                        toGive = -moveValue;
                    }
                }
                if (moveValue > max) {
                    max = moveValue;
                    if (Math.abs(max - MAX_VALUE) <= 100) {
                        break;
                    }
                }
                if (toTake >= toGive) {
                    max = moveValue;
                    if (possibleConfig.killer) {
                        if (killerMoves[level][0] == possibleConfig.move) {
                            efficiency[level][0]++;
                        } else {
                            efficiency[level][1]++;
                            if (efficiency[level][0] < efficiency[level][1]) {
                                final Move temp = killerMoves[level][0];
                                killerMoves[level][0] = killerMoves[level][1];
                                killerMoves[level][1] = temp;
                            }
                        }
                    } else {
                        if (killerMoves[level][0] == null) {
                            killerMoves[level][0] = possibleConfig.move;
                            efficiency[level][0] = 1;
                        } else if (killerMoves[level][1] == null) {
                            killerMoves[level][1] = possibleConfig.move;
                            efficiency[level][1] = 1;
                        }
                    }
                    break;
                } else if (possibleConfig.killer) {
                    if (killerMoves[level][0] == possibleConfig.move) {
                        efficiency[level][0]--;
                    } else {
                        efficiency[level][1]--;
                    }
                    if (efficiency[level][0] < efficiency[level][1]) {
                        final Move temp = killerMoves[level][0];
                        killerMoves[level][0] = killerMoves[level][1];
                        killerMoves[level][1] = temp;
                    }
                    if (efficiency[level][1] <= 0) {
                        efficiency[level][1] = 0;
                        killerMoves[level][1] = null;
                    }
                }
            }
        }
        return -max;
    }

    /**
     * A board and move combination.
     */
    private class Configuration implements Comparable<Configuration> {
        final Move move;
        final Board board;
        /**
         * Represents how good the move is for the player making the move
         */
        int strength;
        /**
         * True only if the move is considered a 'killer' move as per the killer heuristic.
         */
        final boolean killer;

        private Configuration(final Move move,
                              final Board board,
                              final int player,
                              final int level) {
            final Move moveToBeMade = Board.ALL_MOVES[player][move.x][move.y];
            this.board = board.makeMove(moveToBeMade);
            if (killerMoves[level][0] == moveToBeMade || killerMoves[level][1] == moveToBeMade) {
                killer = true;
            } else {
                this.strength = this.board.heuristicValue(player);
                killer = false;
            }
            this.move = moveToBeMade;
        }

        @Override
        public int compareTo(Configuration o) {
            if (killer && o.killer) {
                return 0;
            } else if (!killer && o.killer) {
                return +1;
            } else if (killer) {
                return -1;
            }
            return o.strength - strength;
        }

        @Override
        public String toString() {
            return "Configuration{" +
                    "move=" + move +
                    ", board=" + board +
                    '}';
        }
    }

    static int flip(final int player) {
        return ~player & 3;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
