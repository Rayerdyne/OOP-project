package be.uliege.straet.oop.filters;

/** 
 * This abstract class represents a sound generator, that depends on time.
 * In this case, it will be considered as a particular filter with no inputs.
 */
public abstract class Generator implements FeedbackableFilter {

    public static final double DEF_SAMPLING_FREQUENCY = 
        IntegratorFilter.DEF_SAMPLING_FREQUENCY;

    protected double t = 0;
    protected double dt;

    /**
     * Constructor with default sampling frequency 44100 Hz.
     */
    public Generator() {
        dt = 1.0 / 44100.0;
    }

    /**
     * Constructor depending on the sampling frequency.
     * @param fs                        The sampling frequency, in Hz
     */
    public Generator(double fs) {
        dt = 1.0 / fs;
    }

    /**
     * @return      The number of inputs, i.e. 0 for a generator
     */
    public int nbInputs() {
        return 0;
    }

    /**
     * <p>Returns the number of outputs of a generator, i.e. 1.</p>
     * <p>Will have to be overrided if we want to make a generator with more 
     * than one output. </p>
     * @return      The number of outputs a generator, 1
     */
    public int nbOutputs() {
        return 1;
    }

    /**
     * Resets time to 0.
     */
    public void reset() {
        t = 0;
    }

    /**
     * @return      The sampling frequency, in Hz.
     */
    public double getSamplingFrequency() { return 1.0 / dt; }

}
