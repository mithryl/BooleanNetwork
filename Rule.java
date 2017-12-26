package BooleanNetwork;

import java.util.Random;

public class Rule {
    byte[] ruleTable;

    Random rand = new Random();
    static Random srand = new Random();
    int connections;

    public Rule(int connections){
        this.connections = connections;
        ruleTable = new byte[(int) Math.pow(2, connections)];
        genRules();
    }
    public Rule(){}//default constructor


    public byte getRule(int i){
        return ruleTable[i];
    }
    public void setRuleTable(byte[] ruleTable){
        this.ruleTable = ruleTable;
    }
    public byte[] getRuleTable(){
        return ruleTable;
    }
    public byte[] clone(){
        return ruleTable.clone();
    }

    public void genRules(){
        for(int i = 0; i < ruleTable.length; i++){
            ruleTable[i] = (byte) rand.nextInt(2);
        }
    }

    public static byte[] getRandomRule(int connections){
        byte[] ruletable = new byte[(int) Math.pow(2, connections)];

        for(int i = 0; i < ruletable.length; i++){
            ruletable[i] = (byte) srand.nextInt(2);
        }

        return ruletable;
    }

    public void printRules(){
        System.out.print("{");
        for(int i = 0; i < ruleTable.length; i++){
            System.out.print(ruleTable[i]);
        }
        System.out.println("}");
    }
}
