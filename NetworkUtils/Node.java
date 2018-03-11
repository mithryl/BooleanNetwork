package NetworkUtils;

import java.util.ArrayList;
import java.util.BitSet;

public class Node {
    int state,buffer;
    Rule rule;
    ArrayList<Node> neighbors = new ArrayList<>();

    public BitSet neighborbit = new BitSet();//TODO: implement proper bitset size

    int ID;

    public Node(int ID) {
        this.ID = ID;
    }

    public void initRules() {
        rule = new Rule(neighbors.size());
    }

    public byte nextState() {
        if (neighbors.size() == 0) return 0;

        neighborbit.clear();

        for (int i = 0; i < neighbors.size(); i++) {
            if(neighbors.get(i).getState() > 0) neighborbit.set(neighbors.size() - i - 1);
        }

        return rule.getRule(convertBitset(neighborbit));
    }

    //Converts bitset into base 10 int
    public static int convertBitset(BitSet set) {
        int val = 0;
        for(int i = 0; i < set.length(); i++){
            val += set.get(i) ? (1 << i) : 0;
        }
        return val;
    }

    public void addNeighbor(Node n){
        neighbors.add(n);
    }

    /* getters and setters */

    public void setState(int state){
        this.state = state;
    }

    public void setBuffer(){
        buffer = nextState();
    }

    public void setBuffer(int buffer){
        this.buffer = buffer;
    }

    public void swapBuffer(){
        state = buffer;
    }

    public void setNeighbors(Node[] neighbors){
        for (Node n : neighbors) {
            this.neighbors.add(n);
        }
    }

    public void setNeighbors(ArrayList<Node> neighbors) {
        this.neighbors = neighbors;
    }

    public void setRule(Rule rule){
        this.rule = rule;
    }

    public int getState(){
        return state;
    }

    public int getID() {
        return ID;
    }

    public Rule getRule() {
        return rule;
    }

    public ArrayList<Node> getNeighbors() {
        return neighbors;
    }

}
