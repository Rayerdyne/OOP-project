package be.uliege.straet.oop.filters;

import java.util.HashMap;

import be.uliege.montefiore.oop.audio.FilterException;

/** 
 * This class provides another type of primitive block, a simple integrator.
 * It computs the integral by the trapeze method, so that it adds at each step
 * input * dt,  where dt is the time step, so that dt = 1 / fs, where fs is the
 * sampling frequency.
 */
public class IntegratorFilter implements FeedbackableFilter {

    public static final double DEF_SAMPLING_FREQUENCY = 44100.0;

    private double integral;
    private double fs;

    /**
     * Constructor.
     * @param fs                The sampling frequency
     */
    public IntegratorFilter (double fs) {
        this.fs = fs;
        integral = 0;
    }

    /**
     * Constructor, uses default sampling frequency 44100 Hz.
     */
    public IntegratorFilter() {
        this(DEF_SAMPLING_FREQUENCY);
    }


    /**
     * Computes one step of the filter, i.e. returns this.gain * input.
     * @param input     The input to compute.
     */
    public double[] computeOneStep(double[] input) throws FilterException {
        if (input.length != nbInputs()) {
            throw new FilterException("Invalid input length (is " + 
                String.valueOf(input.length) + " instead of 1).");
        }

        integral += input[0] / fs;
        return new double[] { integral };
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
     * Reset method for filter interface. In this case, resets the integral
     * value to 0.
     */
    public void reset() {
        integral = 0;
    }

    public double[] incomingOutput() {
        return new double[] { integral };
    }

    public HashMap<String, String> getParameters() {
        return new HashMap<String, String>();
    }

    /**
     * @return      The sampling frequency, in Hz.
     */
    public double getSamplingFrequency() { return fs; }
}
