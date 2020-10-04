package be.uliege.straet.oop.filters;

import java.util.HashMap;

/**
 * <p>INFO0062 - Object-Oriented Programming project.</p>
 *
 * <p>A basic sine wave generator.</p>
 *
 * <p>François Straet</p>
 */
public class SineGenerator extends Generator {

    private double amplitude;
    private double omega_0; // to avoid computing each time the same value

    /**
     * Consrtuctor with default sampling frequency 44100 Hz
     * 
     * @param frequency                 The frequency of sine wave to produce,
     *                                  in Hz
     * @param amplitude                 The amplitude of sine wave to produce
     */
    public SineGenerator(double frequency, double amplitude) {
        this(frequency, amplitude, 44100.0);
    }

    /**
     * Consrtuctor depending on sampling frequency
     * 
     * @param frequency                 The frequency of sine wave to produce,
     *                                  in Hz
     * @param amplitude                 The amplitude of sine wave to produce
     * @param fs                        The sampling frequency, in Hz
     */
    public SineGenerator(double frequency, double amplitude, double fs) {
        omega_0 = 2.0 * Math.PI * frequency;
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
    return new double[] { amplitude * Math.sin(omega_0 * t) };
    }

    public double[] incomingOutput() {
        return new double[] { amplitude * Math.sin(omega_0 * (t + dt)) };
    }

    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("frequency", String.valueOf(omega_0 / (2.0 * Math.PI)));
        hm.put("amplitude", String.valueOf(amplitude));
        return hm;
    }
}
