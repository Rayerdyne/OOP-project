package be.uliege.straet.oop.filters;

import java.util.HashMap;

import be.uliege.montefiore.oop.audio.FilterException;
import be.uliege.straet.oop.loader.Writer;

/** 
 * <p>This class provides another type of basic block, a simple differentiator,
 * i.e. it returns (n°i+1 - n°i) / dt, where dt is the time step, so that
 * dt = 1 / fs, where fs is the sampling frequency.<p>
 * 
 * <p>As expected, if it is applied "alone" on a sound, the output is just 
 * noise. </p>
 */
public class DifferentiatorFilter implements WFilter {

    public static final double DEF_SAMPLING_FREQUENCY = 
        IntegratorFilter.DEF_SAMPLING_FREQUENCY;

    private double prev = 0;
    private double fs;

    /**
     * Constructor.
     * @param fs        The sampling frequency
     */
    public DifferentiatorFilter (double fs) {
        this.fs = fs;
    }

    /**
     * Constructor, uses default sampling frequency 44100 Hz.
     */
    public DifferentiatorFilter() {
        this(DEF_SAMPLING_FREQUENCY);
    }

    /**
     * Computes one step of the filter, i.e. returns 
     * (input - prev) * fs.
     * @param input     The input to compute.
     */
    public double[] computeOneStep(double[] input) throws FilterException {
        if (input.length != nbInputs()) {
            throw new FilterException("Invalid input length (is " + 
                String.valueOf(input.length) + " instead of 1).");
        }

        double res = (input[0] - prev) * fs;
        prev = input[0];
        return new double[] { res };
    }


    /**
     * @return      The number of inputs, i.e. 1
     */
    public int nbInputs() {
        return 1;
    }

    /**
     * @return      The number of outputs, i.e. 1
     */
    public int nbOutputs() {
        return 1;
    }

    /**
     * Reset method for filter interface. Resets prev to 0.
     */
    public void reset() {
        prev = 0;
    }

    @Override
    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put(Writer.FS_ATTR_NAME, String.valueOf(fs));
        return hm;
    }

    /**
     * @return      The sampling frequency, in Hz.
     */
    public double getSamplingFrequency() { return fs; }
}
