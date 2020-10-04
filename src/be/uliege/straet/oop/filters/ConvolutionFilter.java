package be.uliege.straet.oop.filters;

import java.util.Arrays;
import java.util.HashMap;

import be.uliege.montefiore.oop.audio.FilterException;

/** <p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p>This filter represents a delay filter, i.e. it shifts its input by a 
 * constant number of samples.</p>
 * 
 * <p>François Straet</p>
 */
public class ConvolutionFilter implements WFilter {

    public static final double[] DEF_VECTOR = { 1.0, 0.0, 0.2 };

    private double[] values;
    private double[] v;
    private int index;  // as we store inputs in an circular array, the index
                        // we are reading at.
    
    /**
     * Constructor.
     * @param v                         The vector to convolute with
     */
    public ConvolutionFilter (double[] v) {
        values = new double[v.length];
        Arrays.fill(values, 0.0);
        this.v = v;
        index = 0;
    }

    /**
     * Constructor, uses default vector DEF_VECTOR
     */
    public ConvolutionFilter() {
        this(DEF_VECTOR);
    }

    /**
     * <p>Computes one step of the filter, i.e. <ul>
     * <li> Compute output</li>
     * <li> Increment index and place the input there;</li>
     * <li> Manage index too large.</li></ul></p>
     * @param input                 The input to compute.
     */
    public double[] computeOneStep(double[] input) throws FilterException {
        if (input.length != nbInputs()) {
            throw new FilterException("Invalid input length (is " + 
                String.valueOf(input.length) + " instead of 1).");
        }

        double[] output = new double[1];
        output[0] = 0;

        values[index] = input[0];
        index = (index + 1) % values.length;

        int k = index;
        for (int i = 0; i < v.length; i++) {
            output[0] += v[i] * values[k];
            k = (k + 1) % v.length;
        }
        return output;
    }

    /**
     * Implementation of nbInputs and nbOutputs...
     */
    public int nbInputs() {
        return 1;
    }

    public int nbOutputs() {
        return 1;
    }

    /**
     * <p>Reset method for filter interface:<ul>
     * <li> Refill values with zeros</li>
     * <li> Reset index to zero (unusefull, because circular array, but 
     * why not ?)</li></ul>
     * </p>
     */
    public void reset() {
        index = 0;
        Arrays.fill(values, 0.0);
    }

    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        String sv = new String();
        for (double x : v) {
            sv += String.valueOf(x) + ", ";
        }
        hm.put("v", sv);
        return hm;
    }


    public double[] getVector() { return v;  }

}
