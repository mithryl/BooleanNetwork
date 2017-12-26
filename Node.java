package BooleanNetwork;

import java.util.ArrayList;

public class Node {
    int state,buffer;
    Rule rule;
    ArrayList<Node> neighbors = new ArrayList<>();
    int ID;

        public Node(int ID){
            this.ID = ID;
        }

    public void initRules(){
        rule = new Rule(neighbors.size());
    }


    public byte nextState(){
        if(neighbors.size() == 0) return rule.getRule(0); //TODO: important, change this

        String s = "";
        for(Node n : neighbors){
            s += Integer.toString(n.getState());
        }
        return rule.getRule(Integer.parseInt(s,2));
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

    public void swap(){
        state = buffer;
    }

    public void setNeighbors(Node[] neighbors){
        for (Node n : neighbors) {
            this.neighbors.add(n);
        }
    }

    public void setNeighbors(ArrayList<Node> neighbors){
        this.neighbors = neighbors;
    }

    public void setRule(Rule rule){
        this.rule = rule;
    }
    public int getBuffer(){
        return buffer;
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
