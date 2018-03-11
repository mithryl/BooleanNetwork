package NetworkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** These methods were written for compatibility with the matlab RBN toolbox
 *  Adjacency lists are not zero indexed
 */


public class NetworkIO {

    //Reads in a network topology in the agreed upon format
    public static int[][] readTopology(Path network){

        if (Files.notExists(network)) {
            System.out.println("File does not exist");
            return null;
        }

        try {
            //Read lines from network file and create topology
            List<String> lines = Files.readAllLines(network);
            int[][] topology = new int[lines.size()][];

            for (int i = 0; i < lines.size(); i++) {
                String[] adj = lines.get(i).split(" ");

                topology[i] = new int[adj.length];

                for(int j = 0; j < adj.length; j++){
                    //Matlab is not zero indexed - must subtract one
                    topology[i][j] = Integer.parseInt(adj[j]) - 1;
                }
            }

            return topology;

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Something went very wrong...");

        return null;
    }


    public static byte[][] readRules(Path ruletable) {

        if (Files.notExists(ruletable)) {
            System.out.println("File does not exist");
            return null;
        }

        byte[][] ruleset = null;

        try {
            List<String> rules = Files.readAllLines(ruletable);

            int length = (rules.get(0).length() + 1) / 2;

            ruleset = new byte[length][];

            for(int i = 0; i < length; i++){
                ruleset[i] = getrule(rules,i*2);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ruleset;
    }

    public static byte[] getrule(List<String> rules, int index){
        byte[] rule = new byte[rules.size()];

        for(int i = 0; i < rules.size(); i++) {
            char c = rules.get(i).charAt(index);

            if (c == ' ') {
                return Arrays.copyOfRange(rule,0,i);
            } else {
                rule[i] = (byte) (c - 48); //convert from ascii number to decimal
            }
        }

        return rule;
    }



    public static void writeTopology(int[][] topology, String path) {

    }

    public static void writeRules(int[][] test){

    }

}
