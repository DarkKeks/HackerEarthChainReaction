import java.util.ArrayList;
import java.util.List;

public class MiniMax {

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
