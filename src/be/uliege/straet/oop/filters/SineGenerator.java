package be.uliege.straet.oop.filters;

import java.util.HashMap;

import be.uliege.straet.oop.loader.Writer;

/**
 * <p>INFO0062 - Object-Oriented Programming project.</p>
 *
 * <p>A basic sine wave generator.</p>
 *
 * <p>François Straet</p>
 */
public class SineGenerator extends Generator {

    public static final double DEF_FREQUENCY = 440.0;
    public static final double DEF_AMPLITUDE = 1.0;
    public static final double DEF_SAMPLING_FREQUENCY = 
        IntegratorFilter.DEF_SAMPLING_FREQUENCY;

    private double amplitude;
    private double omega_0; // to avoid computing each time the same value


    /**
     * Constructor with default values: DEF_FREQUENCY, DEF_AMPLITUDE and 
     * DEF_SAMPLING_FREQUENCY
     */
    public SineGenerator() {
        this(DEF_FREQUENCY, DEF_AMPLITUDE, DEF_SAMPLING_FREQUENCY);
    }

    /**
     * Constructor with default sampling frequency DEF_SAMPLING_FREQUENCY Hz
     * 
     * @param frequency     The frequency of sine wave to produce, in Hz
     * @param amplitude     The amplitude of sine wave to produce
     */
    public SineGenerator(double frequency, double amplitude) {
        this(frequency, amplitude, 44100.0);
    }

    /**
     * Consrtuctor depending on sampling frequency.
     * @param frequency     The frequency of sine wave to produce, in Hz
     * @param amplitude     The amplitude of sine wave to produce
     * @param fs            The sampling frequency, in Hz
     */
    public SineGenerator(double frequency, double amplitude, double fs) {
        omega_0 = 2.0 * Math.PI * frequency;
        dt = 1.0 / fs;
        this.amplitude = amplitude;
    }

    /**
     * Compute the output, increments t.
     * @param input         Remains from Filter interface, unused
     */
    public double[] computeOneStep(double[] input) {
    t += dt;
    return new double[] { amplitude * Math.sin(omega_0 * t) };
    }

    public double[] incomingOutput() {
        return new double[] { amplitude * Math.sin(omega_0 * (t + dt)) };
    }

    @Override
    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put(Writer.FREQUENCY_ATTR_NAME, 
            String.valueOf(omega_0 / (2.0*Math.PI)));
        hm.put(Writer.AMPLITUDE_ATTR_NAME, String.valueOf(amplitude));
        hm.put(Writer.FS_ATTR_NAME, String.valueOf(1 / dt));
        return hm;
    }

    /**
     * @return      The frequency of generated sine wave.
     */
    public double getFrequency() { return omega_0 / (2.0 * Math.PI); }

    /**
     * @return      The amplitude of generated sine wave.
     */
    public double getAmplitude() { return amplitude; }
}
