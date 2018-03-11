package NetworkUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//TODO: start splitting methods into separate classes

public class BooleanNetwork {

    //Array of Nodes in network. Node ID should correspond to array index
    Node[] network;

    //Size of network, set by setNetwork methods
    int size;

    //For use in generation methods.
    ThreadLocalRandom rand = ThreadLocalRandom.current();

    //Iterates network using the default, synchronous update
    public BooleanNetwork iterate(int iter) {
        for(int i = 0; i < iter; i++){
            update();
        }
        return this;
    }

    //Iterates network using any specified update method
    public BooleanNetwork iterate(Runnable updateMethod, int iterations) {
        for(int i = 0; i < iterations; i++){
            updateMethod.run();
        }
        return this;
    }

    //For use in update methods
    ArrayList<Node>[] outputnodes;
    Set<Node> update = new HashSet<>();
    Set<Node> buffer = new HashSet<>();

    //Creates array to be used by cascade update -
    public BooleanNetwork initOutputNodes() {
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

        return this;
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
    public BooleanNetwork resetUpdateNodes(){
        update.clear();

        return this;
    }

    //Adds nodes to initial update set
    public BooleanNetwork setUpdateNodes(Node... n){
        update.clear();

        for(Node l : n){
            update.add(l);
        }
        return this;
    }

    public BooleanNetwork setUpdateNodes(int... n){
        update.clear();

        for(int i : n){
            update.add(getNode(i));
        }
        return this;
    }

    public Set<Node> getUpdateNodes(){
        return update;
    }

    //Updates a randomly selected node with uniform probability
    public void asyncUpdate(){
        Node n = getNode(ThreadLocalRandom.current().nextInt(size));

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
    public BooleanNetwork setNetwork(int[][] neighbors) {//includes placeholder value
        network = new Node[neighbors.length]; //set to n size
        this.size = neighbors.length;

        for (int i = 0; i < size; i++) {//initialize network of nodes
            network[i] = new Node(i);
        }

        for (int i = 0; i < size; i++) {
            ArrayList<Node> nb = new ArrayList<>();

            for (int j = 0; j < neighbors[i].length; j++) {

                if (neighbors[i][j] >= 0) {   //value is not -1, therefore it is used
                    nb.add(network[neighbors[i][j]]);
                }

            }

            network[i].setNeighbors(nb);
        }

        return this;
    }

    public BooleanNetwork seedNetwork() {
        seedNetwork(rand.nextDouble());

        return this;
    }

    //Sets state of network with probability P of a node being set to one. Does not guarantee exact probability
    public BooleanNetwork seedNetwork(double p) {
        for(Node n : network){
            if(rand.nextDouble() < p){
                n.setState(1);
            }else{
                n.setState(0);
            }
        }

        return this;
    }

    //Sets state of nodes to one until percent p of network is ones
    public BooleanNetwork seedNetworkExact(double p) {
        resetState(); //for repeated use of seed method

        while (getComposition() < p) {
            network[rand.nextInt(size)].setState(1);
        }

        return this;
    }


    //Set all nodes to zero
    public BooleanNetwork resetState() {
        for (Node n : network) {
            n.setState(0);
        }

        return this;
    }

    //Returns percent of network in state 1
    public double getComposition() {
        int counter = 0;

        for (Node n : network) {
            if(n.getState() == 1){
                counter++;
            }
        }

        return (double) counter / size;
    }

    //Creates new rule for each node in network
    public BooleanNetwork generateRandomRules() {
        for (Node n : network) {
            n.initRules();
        }

        return this;
    }

   /* Setter Methods */

    public BooleanNetwork setRules(Rule[] rules) {
        for (int i = 0; i < network.length; i++) {
            network[i].setRule(rules[i]);
        }

        return this;
    }

    public BooleanNetwork setRules(byte[][] rules) {
        for (int i = 0; i < rules.length; i++) {
            Rule rule = new Rule();
            rule.setRuleTable(rules[i]);
            network[i].setRule(rule);
        }

        return this;
    }

    public BooleanNetwork setNetwork(Node[] network) {
        this.network = network;
        return this;
    }

    public BooleanNetwork setState(int[] state) {
        for (int i = 0; i < network.length; i++) {
            network[i].setState(state[i]);
        }
        return this;
    }

    /* Getter Methods */

    public Node getNode(int i) {
        return network[i];
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
    public BooleanNetwork printStates(){
         for(Node n : network)
            System.out.printf("%d:\t%d\n",n.getID(),n.getState());  //print ID and State

        System.out.println();

        return this;
    }

    public BooleanNetwork printNetwork(){
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

        return this;
    }

    public BooleanNetwork printUpdateNodes(){
        System.out.println("Update Nodes:");
        for(Node n : update){
            System.out.print(n.getID() + "\t");
        }
        System.out.println();


        return this;
    }

    public BooleanNetwork printUpdateMatrix(){
        int[] updates = new int[size];

        for(Node n : update) {
            updates[n.getID()] = 1;
        }

        for(int i = 0; i < size; i++){
            System.out.print(updates[i] + "\t");
        }
        System.out.println();

        return this;
    }

    public BooleanNetwork printOutputNodes(){
        System.out.println("Output Nodes:");
        for(int i = 0; i < outputnodes.length; i++){
            System.out.print(i + ":\t");
            for(Node l : outputnodes[i]){
                System.out.print(l.getID() + "\t");
            }
            System.out.println();
        }
        System.out.println();

        return this;
    }
}
