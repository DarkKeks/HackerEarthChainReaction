import java.io.*;

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
