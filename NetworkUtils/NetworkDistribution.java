package NetworkUtils;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleFunction;
import java.util.function.IntSupplier;

import static java.lang.Math.pow;

/**     Methods for generating degree sequences following any specified distribution as well
 *  as generating random networks from these distributions.
 */

public class NetworkDistribution {

    public static ArrayList<SD> getNetwork(double s, int n, int maxk){
        return getNetwork(getDistributionSupplier(s,n * maxk),n,maxk);
    }

    public static ArrayList<SD> getNetwork(IntSupplier distribution, int n, int maxk){

        //Generate a degree sequence with a total sum less than n * maxk (for proper input distribution)
        int[] degrees = createDegreeSequence(distribution,n,n * maxk);

        ArrayList<SD> list = new ArrayList<>();

        int node = 0;

        //Create list of degree stubs with the source node only set
        for (int i = 0; i < degrees.length; i++) {
            for (int j = 0; j < degrees[i]; j++) {
                for (int k = 0; k < (i+1); k++) {
                    SD stub = new SD();
                    stub.setSource(node);
                    list.add(stub);
                }
                node++;
            }
        }

        //List of nodes whose input degree is less than maxk
        ArrayList<Integer> set = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            set.add(i);
        }

        //This should be faster than iterating over the list each time...
        int[] inputs = new int[n];

        //Create poissionian input distribution
        for(int i = 0; i < list.size(); i++){

            int index = ThreadLocalRandom.current().nextInt(set.size());
            node = set.get(index);

            if (inputs[node] >= maxk) {
                set.remove(index);
                i--;
            } else {
                inputs[node]++;
                list.get(i).setDestination(node);
            }
        }

        return list;
    }

    public static int[] createDegreeSequence(IntSupplier distribution,int n, int cap){

        int[] degrees;

        do  {
            degrees = new int[cap]; //setting cutoff to n * maxk

            //Fill array of degrees with proper values
            for (int i = 0; i < n; i++) {
                degrees[distribution.getAsInt() - 1]++;
            }

        } while (degreeTotal(degrees) > cap);

        System.out.println(degreeTotal(degrees));

        return degrees;
    }

    public static int degreeTotal(int[] degrees){
        int total = 0;

        for(int i = 0; i < degrees.length; i++){
            total += degrees[i] * (i + 1);
        }

        return total;
    }

    public static IntSupplier getDistributionSupplier(double lambda,int cap){
        return getDistributionSupplier(distribution(getPowerLaw(lambda),cap));
    }

    public static IntSupplier getDistributionSupplier(double[] dist){
        double max = dist[dist.length-1];

        return () -> {
            double r = ThreadLocalRandom.current().nextDouble(max);

            for(int i = 0; i < dist.length; i++){
                if(r < dist[i]) return i+1;
            }

            System.out.println("Number outside distribution range");

            return -1;
        };
    }

    public static double[] distribution(DoubleFunction<Double> dist, int cap){
        double[] degrees = new double[cap];

        double total = 0;

        for (int i = 1; i <= cap; i++) {
            total += dist.apply(i);
            degrees[i-1] = total;
        }

        return degrees;
    }

    public static DoubleFunction<Double> getPowerLaw(double s) {
        return x -> pow(x,-s) / zeta(s,10000);
    }

    //Riemann Zeta function, used for normalizing power law distributions
    private static double zeta(double s, int iterations){
        double sum = 0;

        for (int i = 1; i <= iterations; i++) {
            sum += 1 / pow(i,s);
        }

        return sum;
    }
}
