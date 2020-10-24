
package be.uliege.straet.oop.filters;

import java.util.HashMap;

import be.uliege.montefiore.oop.audio.FilterException;

/** 
 * This filter represents the GainFilter, i.e. it scales its input by a 
 * constant number.
 */
public class GainFilter implements WFilter {

    public static final double DEF_GAIN = 1.3;

    private double gain = 0;

    /**
     * Constructor.
     * @param gain      The gain to multiply the input by.
     */
    public GainFilter(double gain) {
        this.gain = gain;
    }

    /**
     * Constructor with default gain value of DEF_GAIN.
     */
    public GainFilter() {
        this(DEF_GAIN);
    }

    /**
     * Computes one step of the filter, i.e. returns this.gain * input.
     * @param input                 The input to compute.
     * @throws FilterException      If the input's length is mismatched
     */
    public double[] computeOneStep(double[] input) throws FilterException {
        if (input.length != nbInputs()) {
            throw new FilterException("Invalid input length (is " + 
                String.valueOf(input.length) + " instead of 1).");
        }

        return new double[] { gain * input[0] };
    }


    /**
     * @return      The number of inputs, i.e. 1.
     */
    public int nbInputs() {
        return 1;
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

    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("gain", String.valueOf(gain));
        return hm;
    }

    public double getGain() { return gain; }
}