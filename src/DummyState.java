public class DummyState implements IState {
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
