package BooleanNetwork;

import java.util.*;

public class booleanNetwork {
    Node[] network;
    int size;

    Random rand = new Random(); // used for generation methods

    public booleanNetwork(){//default constructor
    }

    public void iterate(int iter){
        for(int i = 0; i < iter; i++){
            update();
        }
    }

    public void iterate(Runnable updateMethod, int iterations){
        for(int i = 0; i < iterations; i++){
            updateMethod.run();
        }
    }


    /* Update Methods */

    Set<Node> update = new HashSet<>();
    public void eventUpdate(){
        Set<Node> buffer = new HashSet<>();

        for (Node n : update) {
            if (eventNodeUpdate(n)) {
                buffer.addAll(n.getNeighbors());
            }
        }

        for (Node n : update) {
            n.swap();
        }

        update.clear();
        update.addAll(buffer);
    }

    public void setUpdateNodes(Node... n){
        for(Node l : n){
            update.add(l);
        }
    }

    public boolean eventNodeUpdate(Node n){
        n.setBuffer();

        if(n.getBuffer() != n.getState()){
            return true;
        }else{
            return false;
        }
    }

    public void asyncUpdate(){
        int i = rand.nextInt(getSize());

        Node n = get(i);

        n.setState(n.nextState());
    }

    public void update(){
        int[] buffer = new int[network.length];

        for(int i = 0; i < buffer.length; i++){
            buffer[i] = network[i].nextState();
        }

        for(int i = 0; i < buffer.length; i++){
            network[i].setState(buffer[i]);
        }
    }

    /* NETWORK GENERATION METHODS */

    public booleanNetwork setNetwork(ArrayList<SD> connections, int size){
        network = new Node[size];
        this.size = size;

        for(int i = 0; i < size; i++){
            network[i] = new Node(i);
        }

        ArrayList<Node>[] neighbors = new ArrayList[size]; //store each nodes neighbors in an array

        for(int i = 0; i < neighbors.length; i++){//array init
            neighbors[i] = new ArrayList<>();
        }

        for(SD sd : connections){
            neighbors[sd.getSource()].add(network[sd.getDestination()]);//fill arraylists
        }

        for(int i = 0; i < size; i++){
            network[i].setNeighbors(neighbors[i]);//
        }

        generateRandomRules();//TODO: might be able to get rid of this

        return this;
    }

    public booleanNetwork setNetwork(int[][] neighbors){//includes placeholder value
        network = new Node[neighbors.length]; //set to n size
        this.size = neighbors.length;

        for(int i = 0; i < size; i++){//initialize network of nodes
            network[i] = new Node(i);
        }

        Node placehold = new Node(-1);
        placehold.setState(0);//placeholder state is always zero

        for(int i = 0; i < size; i++){
            ArrayList<Node> nb = new ArrayList<>();

            for(int j = 0; j < neighbors[i].length; j++){

                if(neighbors[i][j] >= 0){//not special value
                    nb.add(network[neighbors[i][j]]);
                }else if(neighbors[i][j] == -2){
                    //do nothing, this is used for fixed length topologies which don't use placeholder nodes
                }else{
                    nb.add(placehold);//must be == -1
                }

            }

            network[i].setNeighbors(nb);
        }

        generateRandomRules(); //TODO: remember what to do about this

        return this;
    }


    public booleanNetwork generateRandomNetwork(int size,double k){
        network = new Node[size];
        this.size = size;

        for(int i = 0; i < size; i++){
            network[i] = new Node(i);
        }

        while(getAverageK() < k){
            network[rand.nextInt(size)].addNeighbor(network[rand.nextInt(size)]);
        }

        return this;
    }

    /* MISC NETWORK METHODS*/

    public byte getMajority(){ //returns the majority state of the network
        int counter = 0;

        for(Node n : network){
            if(n.getState() == 0){
                counter++;
            }
        }

        if(counter > (size/2)){ //TODO: adjust this to allow for even majority
            return 0;
        }else{
            return 1;
        }
    }

    public void seedNetwork(double p){//p is percent of nodes set to 1
        for(Node n : network){
            if(rand.nextDouble() < p){
                n.setState(1);
            }else{
                n.setState(0);
            }
        }
    }

    public void seedNetworkExact(double p){
        resetState(); //for repeated use of seed method

        while(getComposition() < p){
            network[rand.nextInt(size)].setState(1);
        }
    }

    public void resetState(){ //set all nodes to zero
        for(Node n : network){
            n.setState(0);
        }
    }

    public double getComposition(){//returns percent of network in one state
        int counter = 0;

        for(Node n : network){
            if(n.getState() == 1){
                counter++;
            }
        }

        return (double) counter / size;
    }

    public void generateRandomRules(){
        for(Node n : network){
            n.initRules();
        }
    }



    /* Fitness Methods */

    public double syncFitness(int iterations){//returns # nodes in proper state for sync, for given iter
        double[] fit = new double[iterations];
        byte majority = getMajority();
        byte alt = majority == 0 ? (byte) 1 : (byte) 0;

        double fitnessSum = 0;

        for(int i = 0; i < iterations; i++){
            if(i % 2 == 0){
                fitnessSum += densityFitness(majority);
            }else{
                fitnessSum += densityFitness(alt);
            }
            update();
        }

        return fitnessSum / iterations;
    }
    public double densityFitness(int iterations){

        double total = 0;
        for(int i = 0; i < iterations; i++){
            seedNetwork(rand.nextDouble());
            byte maj = getMajority();
            iterate(100);

            total += densityFitness(maj);
        }

        return total / iterations;
    }

    public double densityFitness(byte type){
        int counter = 0;
        for(Node n : network){
            if(n.getState() == type){
                counter++;
            }
        }
        return (double) counter / (double) size;
    }


    /* Static Methods*/

    static Random srand = new Random(); // for use in static methods

    public static ArrayList<SD> getRandomNetwork(int size, double k){
        ArrayList<SD> topology = new ArrayList<>();

        while(averageK(topology,size) < k){
            topology.add(new SD(srand.nextInt(size),srand.nextInt(size)));
        }

        return topology;
    }
    public static int[][] getRandomNetworkMax(int size, double k, int maxk){//k for any node will not be > maxk
        int[][] topology = new int[size][0];

        while(averageK(topology) < k){
            int n = srand.nextInt(size);

            if(topology[n].length < maxk){
                topology[n] = new int[topology[n].length + 1];
            }
        }

        for(int i = 0; i < size; i++){
            for(int j = 0; j < topology[i].length; j++){
                topology[i][j] = srand.nextInt(size);
            }
        }

        return topology;
    }

    public static int[][] getRandomNetworkMaxPlaceholder(int size, double k, int maxk){//k for any node will not be > maxk,
        int[][] topology = new int[size][maxk];

        for(int i = 0; i < size; i++){
            for(int j = 0; j < maxk; j++){
                topology[i][j] = -1;
            }
        }
        while(averageK(topology) < k){
            topology[srand.nextInt(size)][srand.nextInt(maxk)] = srand.nextInt(size);
        }
        return topology;
    }

    public static int[][] getRandomNetworkMaxFixed(int size, double k, int maxk){
        int[][] topology = new int[size][maxk];

        for(int i = 0; i < size; i++){
            for(int j = 0; j < maxk; j++){
                topology[i][j] = -2;
            }
        }
        while(averageK(topology) < k){
            topology[srand.nextInt(size)][srand.nextInt(maxk)] = srand.nextInt(size);
        }
        return topology;
    }

    private static double averageK(ArrayList<SD> gene, int size){//for use in above method
        short[] connections = new short[size];

        for(SD sd : gene){
            connections[sd.getSource()]++;
        }

        double avg = 0;
        for(short i : connections){
            avg += i;
        }

        return avg/size;
    }

    private static double averageK(int[][] net){//for use in above method
        int counter = 0;
        for(int i = 0; i < net.length; i++){
            for(int j = 0; j < net[i].length; j++){
                if(net[i][j] >= 0) counter++;
            }
        }
        return (double) counter / net.length;
    }

    public static Rule[] getRandomRuleset(int size,int maxK){
        Rule[] randomRules = new Rule[size];
        for(int i = 0; i < randomRules.length; i++){
            randomRules[i] = new Rule(maxK);
        }
        return randomRules;
    }
    public static byte[][] getRandomRulesetByte(int size, int maxK){
        byte[][] rules = new byte[size][];

        for(int i = 0; i < size; i++){
            rules[i] = Rule.getRandomRule(maxK);
        }

        return rules;
    }

    public static int[] getRandomState(int size, double p){
        int[] state = new int[size];
        for(int i = 0; i < state.length; i++){
            if(srand.nextDouble() < p){
                state[i] = 1;
            }
        }
        return state;
    }





   /* Setter Methods */

    public booleanNetwork setRules(Rule[] rules){
        for(int i = 0; i < network.length; i++){
            network[i].setRule(rules[i]);
        }

        return this;
    }
    public booleanNetwork setRules(byte[][] rules){
        for(int i = 0; i < rules.length; i++){
            Rule rule = new Rule();
            rule.setRuleTable(rules[i]);
            network[i].setRule(rule);
        }

        return this;
    }

    public booleanNetwork setNetwork(Node[] network){
        this.network = network;

        return this;
    }

    public booleanNetwork setState(int[] state){
        for(int i = 0; i < network.length; i++){
            network[i].setState(state[i]);
        }

        return this;
    }



    /* Getter Methods */

    public Node get(int i){
        return network[i];
    }

    public ArrayList<SD> getTopology(){//returns source/dest format of current network topology
        ArrayList<SD> top = new ArrayList<>();

        for(Node n : network){
            for(Node k : n.getNeighbors()){
                top.add(new SD(n.getID(),k.getID()));
            }
        }

        return top;
    }

    public double getAverageK(){
        int counter = 0;
        for(Node n : network){
            counter += n.getNeighbors().size();
        }
        return (double) counter / size;
    }


    public Rule[] getRules(){
        Rule[] rules = new Rule[network.length];

        for(int i = 0; i < rules.length; i++){
            rules[i] = network[i].getRule();
        }
        return rules;
    }

    public int[] getState(){
        int[] state = new int[network.length];
        for(int i = 0; i < network.length; i++){
            state[i] = network[i].getState();
        }
        return state;
    }

    public int getSize(){
        return network.length;
    }

    public Node[] getNetwork(){
        return network;
    }

    public Random getRand(){
        return rand;
    }

    /* print methods */

    public void printNetwork(){
        for(Node n : network){
            System.out.print(n.getID() + ":" + "\t" + n.getState());
            System.out.println();
            System.out.print("\t");
            n.getRule().printRules();
            System.out.print("\t[");

            ArrayList<Node> k = n.getNeighbors();
            for(int i = 0; i < k.size(); i++){
                System.out.print(k.get(i).getID());
                if(i != k.size()-1){
                    System.out.print(",");
                }
            }
            System.out.println("]");
        }
    }
}
