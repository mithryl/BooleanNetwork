# NetworkUtils

Includes methods for generating network topologies/rules and iterating them with a given updating scheme.

## Basic Usage:
```Java
    //Create an empty BooleanNetwork
    BooleanNetwork network = new BooleanNetwork();

    //Generate a network topology with N 30, K 2, with a maximum K of 10 (Poissionian input/output distribution)
    //Topologies are in the form [node][inputs]
    int[][] topology = Networks.randomTopology(30,2,10);

    //Manually specify a topology.
    //The list below creates a 3 node loop (Node 0 has Node 1 as input, Node 1 has Node 2, etc)
    network.setNetwork(new int[][]{
        {1},
        {2},
        {0}});

    //Set network topology
    network.setNetwork(topology);

    //Generate rules for a network N 30 with a maximum K of 10;
    //Each byte[] is a lookup table consisting of (2 ^ maxK) inputs
    byte[][] rule = Networks.randomRulesetByte(30,10);

    //Set the rules
    network.setRules(rule);

    //Generate and set random rules for a given topology (faster than the above method, no need to specify max K);
    network.generateRandomRules();

    //Update the network 100 times using a synchronous update
    network.iterate(100);

    //Or specify any updating scheme
    network.iterate(network::asyncUpdate,100);

    //Certain schemes require adding nodes to an initial update set, or initializing an list of nodes to aid with cascade updates
    network.setUpdateNodes(0,1,2);

    //Necessary for cascade Update
    network.initOutputNodes();

    //Some methods can be chained
    BooleanNetwork net = new BooleanNetwork()
        .setNetwork(Networks.randomTopology(100,3,10))
        .setRules(Networks.randomRuleset(100,10));

    int[] state = net.iterate(net::asyncUpdate,100)
            .getState();
```

## Example Program
```Java
public static double averageHammingDistance(int networks, int states, int N, double K) {
//Create empty boolean network
    BooleanNetwork network = new BooleanNetwork();
    ThreadLocalRandom rand = ThreadLocalRandom.current();

    int totaldamage = 0;

    for (int i = 0; i < networks; i++) {
        //Generate random topology and rule tables
        network.setNetwork(Networks.randomTopology(N,K,10))
                .generateRandomRules();

        for (int j = 0; j < states; j++) {
            //Generate random state and save it
            int[] state = Networks.randomState(N);

            //Set state of network to saved state, iterate for 200 steps
            int[] state1 = network.setState(state)
                    .iterate(200)
                    .getState();

            //Revert back to initial state, but change perturb value of a single node
            network.setState(state);
            Node n = network.getNode(rand.nextInt(N));
            n.setState(n.getState() == 0 ? 1 : 0);

            //Iterate for 200 steps with perturbed state
            int[] state2 = network.iterate(200)
                    .getState();

            //Compare iterated states, add any damage to total damage count
            for (int k = 0; k < N; k++) {
                if (state1[k] != state2[k]) {
                    totaldamage++;
                }
            }
        }
    }
    //Return averaged damage
    return (double) totaldamage / (double) (states * networks);
    }
```
