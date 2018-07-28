public interface IState {
    Field getField();

    Move getMove();

    Score getScore();

    void setScore(Score score);
}
