package be.uliege.straet.oop.filters;

import java.util.HashMap;

/**
 * A basic square wave generator, switches from 0 to amplitude and 
 * vice-versa
 */ 
public class UpSquareGenerator extends Generator {

    public static final double DEF_FREQUENCY = SineGenerator.DEF_FREQUENCY;
    public static final double DEF_AMPLITUDE = SineGenerator.DEF_AMPLITUDE;
    public static final double DEF_SAMPLING_FREQUENCY = 
        IntegratorFilter.DEF_SAMPLING_FREQUENCY;

    private double amplitude;
    private double frequency;

    /**
     * Constructor with default values: DEF_FREQUENCY, DEF_AMPLITUDE and 
     * DEF_SAMPLING_FREQUENCY.
     */
    public UpSquareGenerator() {
        this(DEF_FREQUENCY, DEF_AMPLITUDE, DEF_SAMPLING_FREQUENCY);
    }
    
    /**
     * Constructor with default sampling frequency 44100 Hz.
     * @param frequency         The frequency of sine wave to produce, in Hz
     * @param amplitude         The amplitude of sine wave to produce
     */
    public UpSquareGenerator(double frequency, double amplitude) {
        this(frequency, amplitude, 44100.0);
    }

    /**
     * Constructor depending on sampling frequency.
     * @param frequency     The frequency of sine wave to produce, in Hz
     * @param amplitude     The amplitude of sine wave to produce
     * @param fs            The sampling frequency, in Hz
     */
    public UpSquareGenerator(double frequency, double amplitude, double fs) {
        this.frequency = frequency;
        dt = 1.0 / fs;
        this.amplitude = amplitude;
    }

    /**
     * Compute the output, increments t.
     * @param input Remains from Filter interface, unused
     */
    public double[] computeOneStep(double[] input) {
        t += dt;
        if (Math.floor(frequency * t) > 0.5)
            return new double[] { amplitude };
        else
            return new double[] { 0.0 };
    }

    public double[] incomingOutput() {    
        if (Math.floor(frequency * t) > 0.5)
            return new double[] { amplitude };
        else
            return new double[] { 0.0 };
    }

    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("frequency", String.valueOf(frequency));
        hm.put("amplitude", String.valueOf(amplitude));
        return hm;
    }

    /**
     * @return      The frequency of generated square signal.
     */
    public double getFrequency() { return frequency; }

    /**
     * @return      The amplitude of generated square signal.
     */
    public double getAmplitude() { return amplitude; }
}
