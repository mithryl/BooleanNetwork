package NetworkUtils;

import java.util.concurrent.ThreadLocalRandom;

public class NetworkStats {
    static ThreadLocalRandom rand = ThreadLocalRandom.current();

    public static double getAverageK(BooleanNetwork network) {
        int counter = 0;
        for (Node n : network.getNetwork()) {
            counter += n.getNeighbors().size();
        }
        return (double) counter / network.getSize();
    }

    public static byte getMajority(BooleanNetwork network){
        int counter = 0;

        for(Node n : network.getNetwork()){
            if(n.getState() == 0){
                counter++;
            }
        }

        if(counter > (network.getSize()/2)){ //TODO: adjust this to allow for even majority
            return 0;
        }else{
            return 1;
        }
    }

    public static double syncFitness(BooleanNetwork network, int iterations) {
        byte majority = NetworkStats.getMajority(network);
        byte alt = majority == 0 ? (byte) 1 : (byte) 0;

        double fitnessSum = 0;

        for(int i = 0; i < iterations; i++){
            if(i % 2 == 0){
                fitnessSum += densityFitness(network,majority);
            }else{
                fitnessSum += densityFitness(network,alt);
            }
            network.update();
        }

        return fitnessSum / iterations;
    }

    //Sync fitness for non CRBN update schemes
    public static double syncFitness(BooleanNetwork network, int iterations, Runnable update) {
        byte majority = getMajority(network);

        byte alt = majority == 0 ? (byte) 1 : (byte) 0;

        double fitnessSum = 0;

        for (int i = 0; i < iterations; i++) {
            if(i % 2 == 0){
                fitnessSum += densityFitness(network,majority);
            }else{
                fitnessSum += densityFitness(network,alt);
            }
            update.run();
        }

        return fitnessSum / iterations;
    }

    //Performs density test on network for set iterations
    public static double densityFitness(BooleanNetwork network, int iterations) {
        double total = 0;

        for (int i = 0; i < iterations; i++) {
            network.seedNetwork(rand.nextDouble());
            byte maj = getMajority(network);
            network.iterate(100);

            total += densityFitness(network,maj);
        }

        return total / iterations;
    }

    //Returns percent of network in state
    public static double densityFitness(BooleanNetwork network, byte state) {
        int counter = 0;
        for (Node n : network.getNetwork()) {
            if(n.getState() == state){
                counter++;
            }
        }
        return (double) counter / (double) network.getSize();
    }
}
