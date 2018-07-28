public class State implements IState {

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
