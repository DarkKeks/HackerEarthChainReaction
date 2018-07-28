import java.util.ArrayList;
import java.util.List;

public class Field {

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