package old;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;

/**
 * A game playing bot for Chain Reaction. Takes as input an array of {@link Board#BOARD_SIZE}*{@link Board#BOARD_SIZE}.
 * Each cell is represented by (ORB_COUNT,PLAYER). Takes time = {@link MinMax#TIME_OUT} to return an answer.
 *
 * @author Gaurav Sen
 */
public class ChainReaction {

    private static final String input = "00 00 00 00 00\n" +
            "00 00 00 00 00\n" +
            "00 00 00 00 00\n" +
            "00 00 00 00 00\n" +
            "00 00 00 00 00\n" +
            "1";

    public static void main(String[] args) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new CharArrayReader(input.toCharArray()));
//        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        final int[][][] board = new int[5][5][2];
        for (int i = 0; i < board.length; i++) {
            final String cols[] = bufferedReader.readLine().split(" ");
            for (int j = 0; j < board[i].length; j++) {
                for (int k = 0; k < board[i][j].length; k++) {
                    board[i][j][k] = cols[j].charAt(k) - '0';
                }
            }
        }
        final int player = Integer.parseInt(bufferedReader.readLine());
        final MinMax minMax = new MinMax();
        System.out.println(minMax.iterativeSearchForBestMove(board, player));
        System.out.println(minMax.eval + " " + minMax.depth + " " + minMax.moves + " " + minMax.computations);
    }

}

