# NetworkUtils

Includes methods for generating network topologies/rules and iterating them with a given updating scheme.

## Example
```Java
    //Initialize empty network
    booleanNetwork network = new booleanNetwork();

    //Manually specify a topology
    network.setNetwork(new int[][]{
        {0,1},
        {1,0}};

    //Or generate a network topology with N 30, K 2, with a maximum K of 10. Each int[] is an array of input connections for a given  node
    int[][] topology = booleanNetwork.getRandomNetwork(30,2,10);

    //Set the topology
    network.setNetwork(topology);

    //Generate rules for the given topology
    network.generateRandomRules();

    //Or generate rules for a network N 30 with a maximum K of 10;
    byte[][] rule = booleanNetwork.getRandomRulesetByte(30,10);

    //Set the rules
    network.setRules(rule);

    //Update the network 100 times using a synchronous update
    network.iterate(100);

    //Or specify any updating scheme
    network.iterate(network::asyncUpdate,100);

    //Certain schemes require adding nodes to an initial update set, or initializing an list of nodes to aid with cascade updates
    network.setUpdateNodes(0,1,2);

    network.initOutputNodes(); //for cascade updates

    network.iterate(network::cascadeUpdate,100);

    //Values can be retrieved by calling getNetwork() to return the array of Nodes making up the network, with state and rules included
    //Node has getters for retrieving specific state values / rules
    Node[] network = network.getNetwork();
```
