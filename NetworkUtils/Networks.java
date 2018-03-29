package NetworkUtils;

//Helper class for generating network topologies / states
//include factory methods

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Networks {
    static ThreadLocalRandom rand = ThreadLocalRandom.current();

    public static void setRandomNetwork(BooleanNetwork net, int n, double k, int maxk) {
        net.setNetwork(randomTopology(n,k,maxk))
                .generateRandomRules();
    }

    public static void setRandomNetwork(BooleanNetwork net, int n, double k, int maxk,boolean b) {
        net.setNetwork(randomTopology(n,k,maxk))
                .generateRandomRules()
                .initOutputNodes();
    }

    //Returns topology with a set length in array representation, with unused nodes being set to -1 (Useful in GA's)
    //Follows poission distribution (Passed check!)
    public static int[][] randomTopology(int size, double k, int maxk) {
        int[][] topology = new int[size][maxk];
        List<Integer> open = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            open.add(i);
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < maxk; j++) {
                topology[i][j] = -1;
            }
        }

        int connections = 0;

        while ((double) connections / (double) size < k) {
            addConnection(topology,maxk,open);
            connections++;
        }

        return topology;
    }

    private static void addConnection(int[][] topology, int maxk, List<Integer> open) {
        int index = rand.nextInt(open.size());
        int node = open.get(index);

        for (int i = 0; i < maxk - 1; i++) {
            if (topology[node][i] == -1) {
                topology[node][i] = rand.nextInt(topology.length);
                return;
            }
        }

        topology[node][maxk-1] = rand.nextInt(topology.length);
        open.remove(index);
    }


    //Returns a topology with an exact K for each node
    public static int[][] randomTopology(int size, int k) {
        int[][] topology = new int[size][k];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < k; j++) {
                topology[i][j] = rand.nextInt(size);
            }
        }

        return topology;
    }

    public static double averageK(List<SD> net, int size) {
        return (double) net.size() / size;
    }

     //Average connectivity for int array format
    public static double averageK(int[][] net) {
        int counter = 0;

        for (int i = 0; i < net.length; i++) {
            for (int j = 0; j < net[i].length; j++) {
                if(net[i][j] >= 0) counter++;
            }
        }

        return (double) counter / net.length;
    }

    //Random ruleset as array of rule objects
    public static Rule[] randomRuleset(int size, int maxK) {
        Rule[] randomRules = new Rule[size];

        for (int i = 0; i < randomRules.length; i++) {
            randomRules[i] = new Rule(maxK);
        }

        return randomRules;
    }

    //Returns ruleset as byte array (Useful in GA's)
    public static byte[][] randomRulesetByte(int size, int maxK) {
        byte[][] rules = new byte[size][];

        for (int i = 0; i < size; i++) {
            rules[i] = randomRule(maxK);
        }

        return rules;
    }

    //Returns random state as byte array
    public static int[] randomState(int size, double p) {
        int[] state = new int[size];

        for (int i = 0; i < state.length; i++) {
            if (rand.nextDouble() < p) {
                state[i] = 1;
            }
        }

        return state;
    }

    public static int[] randomState(int size) {
        int[] state = new int[size];
        for (int i = 0; i < size; i++) {
            if (rand.nextBoolean()) state[i] = 1;
        }
        return state;
    }

    public static byte[] randomRule(int connections) {
        byte[] ruletable = new byte[(int) Math.pow(2, connections)];

        for (int i = 0; i < ruletable.length; i++) {
            ruletable[i] = (byte) rand.nextInt(2);
        }

        return ruletable;
    }
}
