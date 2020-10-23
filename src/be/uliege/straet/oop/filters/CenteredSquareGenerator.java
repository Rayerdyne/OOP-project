package be.uliege.straet.oop.filters;

import java.util.HashMap;

/**
 * <p>INFO0062 - Object-Oriented Programming Project.</p>
 *
 * <p>A basic square wave generator, switches from -amplitude to amplitude and 
 * vice-versa</p>
 *
 * <p>François Straet</p>
 */
public class CenteredSquareGenerator extends Generator {

    public static final double DEF_FREQUENCY = 440.0;
    public static final double DEF_AMPLITUDE = 1.0;
    public static final double DEF_SAMPLING_FREQUENCY = 
        IntegratorFilter.DEF_SAMPLING_FREQUENCY;

    private double amplitude;
    private double frequency;

    /**
     * Constructor with default values: DEF_FREQUENCY, DEF_AMPLITUDE and 
     * DEF_SAMPLING_FREQUENCY
     */
    public CenteredSquareGenerator() {
        this(DEF_FREQUENCY, DEF_AMPLITUDE, DEF_SAMPLING_FREQUENCY);
    }

    /**
     * Constructor with default sampling frequency 44100 Hz
     * 
     * @param frequency The frequency of sine wave to produce, in Hz
     * @param amplitude         The amplitude of sine wave to produce
     */
    public CenteredSquareGenerator(double frequency, double amplitude) {
        this(frequency, amplitude, 44100.0);
    }

    /**
     * Constructor depending on sampling frequency
     * 
     * @param frequency                 The frequency of sine wave to produce,
     *                                  in Hz
     * @param amplitude                 The amplitude of sine wave to produce
     * @param fs                        The sampling frequency, in Hz
     */
    public CenteredSquareGenerator(double frequency, double amplitude, 
        double fs) {
        this.frequency = frequency;
        dt = 1.0 / fs;
        this.amplitude = amplitude;
    }

    /**
     * Compute the output, increments t.
     * 
     * @param input Remains from Filter interface, unused
     */
    public double[] computeOneStep(double[] input) {
        t += dt;
        if (Math.floor(frequency * t) > 0.5)
            return new double[] { amplitude };
        else
            return new double[] { -amplitude };
    }

    public double[] incomingOutput() {    
        if (Math.floor(frequency * t) > 0.5)
            return new double[] { amplitude };
        else
            return new double[] { -amplitude };
    }

    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("frequency", String.valueOf(frequency));
        hm.put("amplitude", String.valueOf(amplitude));
        return hm;
    }

    /**
     * @return      The frequency of generated sine wave.
     */
    public double getFrequency() { return frequency; }

    /**
     * @return      The amplitude of generated sine wave.
     */
    public double getAmplitude() { return amplitude; }
}