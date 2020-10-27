package be.uliege.straet.oop.filters;

import java.util.HashMap;

import be.uliege.montefiore.oop.audio.FilterException;

/** 
 * <p>This filter represents an adder, i.e. it returns the sum of its inputs.
 * </p>
 * 
 * <p>I think that a filter that adds every input vector could be better, but
 * it will thus require to set the input length, wether by giving it to a
 * constructor, or by inferring it at the first computation of output.</p>
 */
public class AdditionFilter implements WFilter {

    /**
     * Constructor. Does nothing.
     */
    public AdditionFilter() {}

    /**
     * Computes one step of the filter.
     * @param input                 The input to compute.
     * @throws FilterException      If the input's length is mismatched
     */
    public double[] computeOneStep(double[] input) throws FilterException {
            
        if (input.length != nbInputs()) {
            throw new FilterException("Invalid input length (is " + 
                String.valueOf(input.length) + " instead of 2).");
        }

        return new double[] { input[0] + input[1] };
    }

    /**
     * Implementation of nbInputs and nbOutputs...
     * @return      The number of inputs, i.e. 2.
     */
    public int nbInputs() {
        return 2;
    }

    /**
     * @return      The number of outputs, i.e. 1.
     */
    public int nbOutputs() {
        return 1;
    }

    /**
     * Reset method for filter interface. In this case, does nothing.
     */
    public void reset() {}

    public int getMaxSamplesInfluenced() { return 0; }

    public HashMap<String, String> getParameters() {
        return new HashMap<String, String>();
    }
}