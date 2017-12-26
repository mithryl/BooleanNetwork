package BooleanNetwork;

public class SD {
    int source;
    int destination;

    public SD(){}
    public SD(int source, int destination){
        this.source = source;
        this.destination = destination;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }
}
