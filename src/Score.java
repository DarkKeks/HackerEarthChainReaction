public class Score {
    private final int score[];

    public Score(int a, int b) {
        score = new int[]{a, b};
    }

    public int getScore(int player) {
        return score[player - 1];
    }
}
