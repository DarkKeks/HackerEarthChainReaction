public class Move {
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
