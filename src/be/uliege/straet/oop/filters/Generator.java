package be.uliege.straet.oop.filters;

/** <p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p>This abstract class represents a sound generator, that depends on time.
 * In this case, it will be considered as a particular filter with no inputs.
 * </p>
 * 
 * <p>François Straet</p>
 */
public abstract class Generator implements FeedbackableFilter {

    public static final double DEF_SAMPLING_FREQUENCY = 
        IntegratorFilter.DEF_SAMPLING_FREQUENCY;

    protected double t = 0;
    protected double dt;

    /**
     * Constructor with default sampling frequency 44100 Hz
     */
    public Generator() {
        dt = 1.0 / 44100.0;
    }

    /**
     * Constructor depending on the sampling frequency
     * @param fs                        The sampling frequency, in Hz
     */
    public Generator(double fs) {
        dt = 1.0 / fs;
    }

    /**
     * nbInputs() implementation...
     */
    public int nbInputs() {
        return 0;
    }

    /**
     * Will have to be overrided if we want to make a generator with more than
     * one output.
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
