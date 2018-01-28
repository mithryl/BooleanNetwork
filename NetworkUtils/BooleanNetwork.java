package NetworkUtils;

import java.util.*;

public class BooleanNetwork {

    //Array of Nodes in network. Node ID should correspond to array index
    Node[] network;

    //Size of network, set by setNetwork methods
    int size;

    //For use in generation methods. //TODO: perhaps ThreadLocalRandom would be better here...
    Random rand = new Random();

    //Iterates network using the default, synchronous update
    public void iterate(int iter){
        for(int i = 0; i < iter; i++){
            update();
        }
    }

    //Iterates network using any specified update method
    public void iterate(Runnable updateMethod, int iterations){
        for(int i = 0; i < iterations; i++){
            updateMethod.run();
        }
    }

    /* Update Methods */
    ArrayList<Node>[] outputnodes;
    Set<Node> update = new HashSet<>();
    Set<Node> buffer = new HashSet<>();

    //Creates array to be used by cascade udpate. Must be called before cascade update, though not needed for other updates
    public void initOutputNodes(){
        outputnodes = new ArrayList[size];
        for (int i = 0; i < size; i++) {
            outputnodes[i] = new ArrayList<>();
        }

        //Add nodes with inputs N to list
        for (Node n : network) {
            for (Node k : network) {
                if (k.getNeighbors().contains(n)) {
                    outputnodes[n.getID()].add(k);
                }
            }
        }
    }

    //Does not update nodes, use for testing the update set only
    public void cascadeUpdateNoBuffer(){
        buffer.clear();

        for (Node n : update) {
            buffer.addAll(outputnodes[n.getID()]);
        }

        update.clear();
        update.addAll(buffer);
    }

    //Cascade updating, only updates nodes connected to previously updated nodes
    public void cascadeUpdate(){
        buffer.clear();

        for (Node n : update) {
            buffer.addAll(outputnodes[n.getID()]);
            n.setBuffer();
        }

        for (Node n : update) {
            n.swapBuffer();
        }

        update.clear();
        update.addAll(buffer);
    }

    //Clears nodes in update set
    public void resetUpdateNodes(){
        update.clear();
    }

    //Adds nodes to initial update set
    public void setUpdateNodes(Node... n){
        for(Node l : n){
            update.add(l);
        }
    }

    public void setUpdateNodes(int... n){
        for(int i : n){
            update.add(get(i));
        }
    }

    public Set<Node> getUpdateNodes(){
        return update;
    }




    //Updates a randomly selected node with uniform probability
    public void asyncUpdate(){
        int i = rand.nextInt(getSize());

        Node n = get(i);

        n.setState(n.nextState());
    }

    //Classical updating scheme
    public void update(){
        for(Node n : network){
            n.setBuffer();
        }

        for(Node n : network){
            n.swapBuffer();
        }
    }

    //Searches for an attractor present in iterations. Returns attractor length, if no attractor returns -1
    public int findAttractorLength(Runnable update, int iterations) {
        List<int[]> states = new ArrayList<>();

        for(int i = 0; i < iterations; i++){
            //add current state to list
            states.add(getState());

            //update network
            update.run();

            int length = hasState(states,getState());

            //if attractor has been found, return its length
            if(length != -1){
                return length;
            }
        }

        //no attractor found in given iterations
        return -1;
    }

    //Returns attractor length given current state and state history
    private int hasState(List<int[]> states, int[] currentState) {
        for (int i = 0; i < states.size(); i++) {
            if (Arrays.equals(states.get(i),currentState)) {
                return states.size() - i;
            }
        }

        //no value found
        return -1;
    }



    /* Network Generation Methods*/

    //Constructs network topology using list of source/destination nodes
    public BooleanNetwork setNetwork(List<SD> connections, int size){
        network = new Node[size];
        this.size = size;

        for(int i = 0; i < size; i++){
            network[i] = new Node(i);
        }

        ArrayList<Node>[] neighbors = new ArrayList[size]; //Store node neighbors in array

        for(int i = 0; i < neighbors.length; i++){
            neighbors[i] = new ArrayList<>();
        }

        for(SD sd : connections){
            neighbors[sd.getDestination()].add(network[sd.getSource()]);//Fill array with neighbors
        }

        for(int i = 0; i < size; i++){
            network[i].setNeighbors(neighbors[i]);//Add neighbor arrays to nodes
        }

        return this;
    }


    //constructs network using int array in form of [node][neighbors]
    public BooleanNetwork setNetwork(int[][] neighbors){//includes placeholder value
        network = new Node[neighbors.length]; //set to n size
        this.size = neighbors.length;

        for(int i = 0; i < size; i++){//initialize network of nodes
            network[i] = new Node(i);
        }

        for(int i = 0; i < size; i++){
            ArrayList<Node> nb = new ArrayList<>();

            for(int j = 0; j < neighbors[i].length; j++){

                if(neighbors[i][j] >= 0){   //value is not -1, therefore it is used
                    nb.add(network[neighbors[i][j]]);
                }

            }

            network[i].setNeighbors(nb);
        }

        return this;
    }

    //Adds random connections until average K is >= specified k
    public BooleanNetwork generateRandomNetwork(int size, double k){
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

    //Returns the majority state of the network
    public byte getMajority(){
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


    public void seedNetwork(){
        seedNetwork(rand.nextDouble());
    }

    //Sets state of network with probability P of a node being set to one. Does not guarantee exact probability
    public void seedNetwork(double p){

        for(Node n : network){
            if(rand.nextDouble() < p){
                n.setState(1);
            }else{
                n.setState(0);
            }
        }
    }

    //Sets state of nodes to one until percent p of network is ones
    public void seedNetworkExact(double p){
        resetState(); //for repeated use of seed method

        while(getComposition() < p){
            network[rand.nextInt(size)].setState(1);
        }
    }



    //Set all nodes to zero
    public void resetState(){
        for(Node n : network){
            n.setState(0);
        }
    }

    //Returns percent of network in state 1
    public double getComposition(){
        int counter = 0;

        for(Node n : network){
            if(n.getState() == 1){
                counter++;
            }
        }

        return (double) counter / size;
    }

    //Creates new rule for each node in network
    public void generateRandomRules(){
        for(Node n : network){
            n.initRules();
        }
    }



    /* Fitness Methods */

    //Returns number of nodes in proper state for sync after set iterations
    public double syncFitness(int iterations){
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

    //Sync fitness for non CRBN update schemes
    public double syncFitness(int iterations, Runnable update){
        byte majority = getMajority();
        byte alt = majority == 0 ? (byte) 1 : (byte) 0;

        double fitnessSum = 0;

        for(int i = 0; i < iterations; i++){
            if(i % 2 == 0){
                fitnessSum += densityFitness(majority);
            }else{
                fitnessSum += densityFitness(alt);
            }

            update.run();

        }

        return fitnessSum / iterations;
    }

    //Performs density test on network for set iterations
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

    //Returns percent of network in state
    public double densityFitness(byte state){
        int counter = 0;
        for(Node n : network){
            if(n.getState() == state){
                counter++;
            }
        }
        return (double) counter / (double) size;
    }


    /* Static Methods*/

    static Random srand = new Random();

    //Returns topology in source,destination format. Size of list may vary
    public static ArrayList<SD> getRandomNetworkApproxSD(int size, double k){
        ArrayList<SD> topology = new ArrayList<>();

        while(averageK(topology,size) < k){
            topology.add(new SD(srand.nextInt(size),srand.nextInt(size)));
        }
        return topology;
    }

    //Returns topology with a set length in array representation, with unused nodes being set to -1 (Useful in GA's)
    public static int[][] getRandomNetworkApprox(int size, double k, int maxk){
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

    //Returns a topology with an exact K for each node
    public static int[][] getRandomNetworkClassic(int size, int k){
        int[][] topology = new int[size][k];

        for(int i = 0; i < size; i++){
            for(int j = 0; j < k; j++){
                topology[i][j] = srand.nextInt(size);
            }
        }

        return topology;
    }

    //Random distribution (not approximate) for integers only
    //This might affect GA crossover performance since all -1 values are clustered towards end of a single int[]
    public static int[][] getRandomNetwork(int size, int k, int maxk){
        int connections = size * k;
        int[][] topology = new int[size][maxk];

        for(int i = 0; i < size; i++){
            for(int j = 0; j < maxk; j++){
                topology[i][j] = -1;
            }
        }

        ArrayList<Integer> inputs = new ArrayList<>();//used to get rid of redundant rand() calls
        for(int i = 0; i < size; i++){
            inputs.add(i);
        }

        int j;
        for(int i = 0; i < connections; i++){
            j = srand.nextInt(inputs.size());

            if(!addConnection(topology,inputs.get(j))){
                inputs.remove(j);//enforce maximum K, remove from list
                i--;//restart
            }
        }

        return topology;
    }

    //helper method for the above
    private static boolean addConnection(int[][] topology, int j){
        for (int i = 0; i < topology[j].length; i++){
            if(topology[j][i] < 0){
                topology[j][i] = srand.nextInt(topology.length);
                return true;
            }
        }

        return false;   //node has reached maximum allowed K, return false
    }


    //Average connectivity for source, destination representation
    private static double averageK(ArrayList<SD> gene, int size){
        int[] connections = new int[size];

        for(SD sd : gene){
            connections[sd.getSource()]++;
        }

        double avg = 0;
        for(int i : connections){
            avg += i;
        }

        return avg/size;
    }

    //Average connectivity for int array format
    public static double averageK(int[][] net){
        int counter = 0;
        for(int i = 0; i < net.length; i++){
            for(int j = 0; j < net[i].length; j++){
                if(net[i][j] >= 0) counter++;
            }
        }
        return (double) counter / net.length;
    }

    //Random ruleset as array of rule objects
    public static Rule[] getRandomRuleset(int size,int maxK){
        Rule[] randomRules = new Rule[size];
        for(int i = 0; i < randomRules.length; i++){
            randomRules[i] = new Rule(maxK);
        }
        return randomRules;
    }

    //Returns ruleset as byte array (Useful in GA's)
    public static byte[][] getRandomRulesetByte(int size, int maxK){
        byte[][] rules = new byte[size][];

        for(int i = 0; i < size; i++){
            rules[i] = Rule.getRandomRule(maxK);
        }

        return rules;
    }

    //Returns random state as byte array
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

    public BooleanNetwork setRules(Rule[] rules){
        for(int i = 0; i < network.length; i++){
            network[i].setRule(rules[i]);
        }

        return this;
    }
    public BooleanNetwork setRules(byte[][] rules){
        for(int i = 0; i < rules.length; i++){
            Rule rule = new Rule();
            rule.setRuleTable(rules[i]);
            network[i].setRule(rule);
        }

        return this;
    }

    public BooleanNetwork setNetwork(Node[] network){
        this.network = network;

        return this;
    }

    public BooleanNetwork setState(int[] state){
        for(int i = 0; i < network.length; i++){
            network[i].setState(state[i]);
        }

        return this;
    }

    public BooleanNetwork setState(byte[] state){
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

    //TODO: could also just return size... perhaps this is safer
    public int getSize(){
        return network.length;
    }

    public Node[] getNetwork(){
        return network;
    }

    /* print methods */
    public void printStates(){
         for(Node n : network)
            System.out.printf("%d:\t%d\n",n.getID(),n.getState());  //print ID and State

        System.out.println();
    }

    public void printNetwork(){
        for(Node n : network){
            System.out.printf("%d:\t%d\n\t",n.getID(),n.getState());  //print ID and State
            n.getRule().printRules();//print rules
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
        System.out.println();
    }

    public void printUpdateNodes(){
        System.out.println("Update Nodes:");
        for(Node n : update){
            System.out.print(n.getID() + "\t");
        }
        System.out.println();
    }

    public void printUpdateMatrix(){
        int[] updates = new int[size];

        for(Node n : update) {
            updates[n.getID()] = 1;
        }

        for(int i = 0; i < size; i++){
            System.out.print(updates[i] + "\t");
        }
        System.out.println();
    }

    public void printOutputNodes(){
        System.out.println("Output Nodes:");
        for(int i = 0; i < outputnodes.length; i++){
            System.out.print(i + ":\t");
            for(Node l : outputnodes[i]){
                System.out.print(l.getID() + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}
