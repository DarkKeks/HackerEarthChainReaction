import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.List;

public class ChainReaction {

//    private static final String input =
//                    "00 00 00 00 00\n" +
//                    "00 00 00 00 00\n" +
//                    "00 00 00 00 00\n" +
//                    "00 00 00 00 00\n" +
//                    "00 00 00 00 00\n" +
//                    "1";

    private static final String input =
            "00 00 00 00 00\n" +
                    "00 00 00 00 00\n" +
                    "00 00 00 00 00\n" +
                    "00 00 00 00 00\n" +
                    "00 00 00 00 00\n" +
                    "1";

    public static final boolean test = false;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(getInput(test));
        int field[][][] = new int[5][5][2];
        for(int i = 0; i < 5; ++i) {
            String[] s = reader.readLine().split(" ");
            for(int j = 0; j < 5; ++j) {
                field[i][j][0] = s[j].charAt(0) - '0';
                field[i][j][1] = s[j].charAt(1) - '0';
            }
        }

        int player = Integer.parseInt(reader.readLine());

        Field start = new Field(field);
        MiniMax minimax = new MiniMax(start, player);

        IState result = minimax.getBestMove();
        System.out.println(result.getMove());
        System.out.println(minimax.alphaBetaCalls + " " + minimax.currentDepth);
        if(result.getScore().getScore(player) == MiniMax.MIN_VALUE) {
            System.out.println("lose :(");
        } else if(result.getScore().getScore(3 - player) == MiniMax.MAX_VALUE) {
            System.out.println("win :)");
        }
    }

    private static Reader getInput(boolean test) {
        return test ? new CharArrayReader(input.toCharArray()) : new InputStreamReader(System.in);
    }
}

class DummyState implements IState {
    private Score score;

    public DummyState(Score score) {
        this.score = score;
    }

    @Override
    public Field getField() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Move getMove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void setScore(Score score) {
        this.score = score;
    }

}


class Field {

    private static final int[] dx = new int[] {0, 1, 0, -1};
    private static final int[] dy = new int[] {1, 0, -1, 0};

    private static int[][] unstableCount = new int[][] {
            {2, 3, 3, 3, 2},
            {3, 4, 4, 4, 3},
            {3, 4, 4, 4, 3},
            {3, 4, 4, 4, 3},
            {2, 3, 3, 3, 2}
    };

    private int[][] owner;
    private int[][] count;
    private int[] moveCount;

    private Field() {
        this.owner = new int[5][5];
        this.count = new int[5][5];
        this.moveCount = new int[3];
    }

    public Field(int[][][] field) {
        this();
        for(int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                owner[i][j] = field[i][j][0];
                count[i][j] = field[i][j][1];
                moveCount[field[i][j][0]]++;
            }
        }
    }

    private Field(Field field) {
        this();
        for(int i = 0; i < 5; ++i) {
            for (int j = 0; j < 5; ++j) {
                owner[i][j] = field.owner[i][j];
                count[i][j] = field.count[i][j];
            }
        }
        System.arraycopy(field.moveCount, 0, moveCount, 0, 3);
    }

    public Field applyMove(Move move) {
        Field result = new Field(this);
        result.makeMove(move);
        return result;
    }

    private void makeMove(Move move) {
        if(owner[move.x][move.y] != move.player) {
            moveCount[owner[move.x][move.y]]--;
            moveCount[move.player]++;
            owner[move.x][move.y] = move.player;
        }

        count[move.x][move.y]++;

        if(isWinning() != 0) return;

        if(count[move.x][move.y] >= unstableCount[move.x][move.y]) {
            count[move.x][move.y] -= unstableCount[move.x][move.y];
            for(int i = 0; i < dx.length; ++i) {
                Move explodeMove = Move.getMove(move.x + dx[i], move.y + dy[i], move.player);
                if(explodeMove != null) {
                    makeMove(explodeMove);
                }
            }
        }
    }

    public int isWinning() {
        if((moveCount[1] | moveCount[2]) > 1) {
            if(moveCount[1] == 0) return 2;
            if(moveCount[2] == 0) return 1;
        }
        return 0;
    }

    public Score getScore() {
        return new Score(getScore(1), getScore(2));
    }

    private int getScore(int player) {
        int otherPlayer = 3 - player;

        int res = moveCount[player] - moveCount[otherPlayer] / (moveCount[player] + moveCount[otherPlayer] + 1);
        int mobility = (moveCount[player] - moveCount[otherPlayer]) / (moveCount[player] + moveCount[otherPlayer] + moveCount[0] * 2);
        int periphery = 0;
        int explosivity = 0;

        for(int i = 0; i < 5; ++i) {
            for(int j = 0; j < 5; ++j) {
                if(unstableCount[i][j] < 4) {
                    if(owner[i][j] == player) {
                        periphery += 5 - unstableCount[i][j];
                    } else {
                        periphery -= 5 - unstableCount[i][j];
                    }
                }

                if(count[i][j] == unstableCount[i][j]) {
                    if(owner[i][j] == player) {
                        explosivity++;
                    } else {
                        explosivity--;
                    }
                }
            }
        }

        return res + mobility + explosivity + periphery;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < 5; ++i) {
            for(int j = 0; j < 5; ++j) {
                builder.append(owner[i][j]);
                builder.append(count[i][j]);
                builder.append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public int getMoveCount(int player) {
        return moveCount[player];
    }

    public List<Move> getMoves(int player) {
        List<Move> result = new ArrayList<>(moveCount[0] + moveCount[player]);
        for(int i = 0; i < 5; ++i)
            for(int j = 0; j < 5; ++j)
                if(owner[i][j] == 0 || owner[i][j] == player)
                    result.add(Move.getMove(i, j, player));
        return result;
    }
}
interface IState {
    Field getField();

    Move getMove();

    Score getScore();

    void setScore(Score score);
}


class MiniMax {

    public static final int MAX_VALUE = 10000000;
    public static final int MIN_VALUE = -MAX_VALUE;
    public static final boolean NO_TL = false;

    private Timer timer;

    private Field field;
    private int maximizer;

    public int alphaBetaCalls = 0;
    public int currentDepth;

    public MiniMax(Field field, int player) {
        this.field = field;
        this.maximizer = player;
        this.timer = new Timer((long)(1e9d * 0.9));
    }

    public IState getBestMove() {
        timer.reset();

        State startState = new State(field, null);
        IState result = null;
        try {
            currentDepth = 1;
            while(true) {
                currentDepth++;
                result = alphabeta(startState, maximizer, currentDepth, MIN_VALUE, MAX_VALUE);
            }
        } catch (IllegalStateException ignore) {}

        return result;
    }

    private IState alphabeta(IState state, int player, int depth, int alpha, int beta) {
        if(!NO_TL) timer.check();
        int value = (player == maximizer ? MIN_VALUE : MAX_VALUE);
        IState bestState = null;

        int winner = state.getField().isWinning();
        if(winner == maximizer)
            return new DummyState(new Score(MAX_VALUE, MIN_VALUE));
        if(winner == 3 - maximizer)
            return new DummyState(new Score(MIN_VALUE, MAX_VALUE));

        if(depth == 0) {
            return new DummyState(state.getField().getScore());
        } else {
            List<State> states = getStates(state.getField(), player);
            for(State nextState : states) {
                alphaBetaCalls++;
                IState resultState = alphabeta(nextState, 3 - player, depth - 1, alpha, beta);
                nextState.setScore(resultState.getScore());
                int score = resultState.getScore().getScore(maximizer);
                if(player == maximizer) {
                    if(score > value) {
                        value = score;
                        bestState = nextState;
                    }
                    if(score > alpha)
                        alpha = score;
                    if(value > beta) break;
                } else {
                    if(score < value) {
                        value = score;
                        bestState = nextState;
                    }
                    if(score < beta)
                        beta = score;
                    if(value < beta) break;
                }
            }

            if(bestState == null)
                bestState = states.get(0);
        }

        return bestState;
    }

    private List<State> getStates(Field field, int player) {
        ArrayList<State> result = new ArrayList<>(field.getMoveCount(player) + field.getMoveCount(0));
        for(Move move : field.getMoves(player)) {
            result.add(new State(field.applyMove(move), move));
        }
        result.sort((a, b) -> {
            boolean aWin = a.getField().isWinning() > 0,
                    bWin = b.getField().isWinning() > 0;
            if(aWin && bWin) return 0;
            if(aWin ^ bWin) return (aWin ? -1 : 1);
            return b.getScore().getScore(maximizer) - a.getScore().getScore(maximizer);
        });
        return result;
    }
}

class Move {
    private static final Move[][][] MOVES;

    static {
        MOVES = new Move[5][5][3];
        for(int i = 0; i < 5; ++i)
            for(int j = 0; j < 5; ++j)
                for(int k = 0; k < 3; ++k)
                    MOVES[i][j][k] = new Move(i, j, k);
    }

    public final int x, y;
    public final int player;

    private Move(int x, int y, int player) {
        this.x = x;
        this.y = y;
        this.player = player;
    }

    public static Move getMove(int x, int y, int player) {
        if(x >= 0 && x < 5 && y >= 0 && y < 5)
            return MOVES[x][y][player];
        return null;
    }

    @Override
    public String toString() {
        return x + " " + y;
    }
}

class Score {
    private final int score[];

    public Score(int a, int b) {
        score = new int[]{a, b};
    }

    public int getScore(int player) {
        return score[player - 1];
    }
}

class State implements IState {

    private Field field;
    private Move move;
    private Score score;

    public State(Field field, Move move) {
        this.field = field;
        this.move = move;
        this.score = field.getScore();
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Move getMove() {
        return move;
    }

    @Override
    public Score getScore() {
        return score;
    }

    @Override
    public void setScore(Score score) {
        this.score = score;
    }
}

class Timer {
    private long start;
    private long time;

    public Timer(long time) {
        this.time = time;
    }

    public void reset() {
        start = System.nanoTime();
    }

    public void check() {
        if(System.nanoTime() - start >= time) {
            throw new IllegalStateException("Timeout");
        }
    }

}

