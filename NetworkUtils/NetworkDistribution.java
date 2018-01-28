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
        return getNetwork(getDistributionSupplier(s,maxk),n,maxk);
    }

    public static ArrayList<SD> getNetwork(IntSupplier distribution, int n, int maxk){
        int[] degrees = new int[maxk];

        //Fill array of degrees with proper values TODO: implement both methods for generating these networks
        for(int i = 0;i < n; i++){
            degrees[distribution.getAsInt()-1]++;
        }

        ArrayList<SD> list = new ArrayList<>();

        int node = 0;

        //Create list of degree stubs with the source node only set
        for(int i = 0; i < degrees.length; i++){
            for(int j = 0; j < degrees[i]; j++){
                for(int k = 0; k < (i+1); k++){

                    SD stub = new SD();
                    stub.setSource(node);
                    list.add(stub);

                }
                node++;
            }
        }

        //Create poissionian input distribution
        for(SD sd : list){
            sd.setDestination(ThreadLocalRandom.current().nextInt(n));
        }

        return list;
    }

    public static int degreeTotal(int[] degrees){
        int total = 0;

        for(int i = 0; i < degrees.length; i++){
            total += degrees[i] * (i + 1);
        }

        return total;
    }


    public static double[] distribution(DoubleFunction<Double> dist, int maxk){
        double[] degrees = new double[maxk];

        double total = 0;

        for(int i = 1; i <= maxk; i++){
            total += dist.apply(i);
            degrees[i-1] = total;
        }

        return degrees;
    }

    public static IntSupplier getDistributionSupplier(double lambda,int maxk){
        return getDistributionSupplier(distribution(getPowerLaw(lambda),maxk));
    }

    public static IntSupplier getDistributionSupplier(double[] dist){
        double max = dist[dist.length-1];

        return () -> {
            double r = ThreadLocalRandom.current().nextDouble(max);

            for(int i = 0; i < dist.length; i++){
                if(r <= dist[i]) return i+1;
            }

            System.out.println("Number outside distribution range");

            return -1;
        };
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
