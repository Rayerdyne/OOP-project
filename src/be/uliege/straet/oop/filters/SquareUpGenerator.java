package be.uliege.straet.oop.filters;

import java.util.HashMap;

/**
 * <p>INFO0062 - Object-Oriented Programming project.</p>
 *
 * <p>A basic square wave generator, switches from 0 to amplitude and 
 * vice-versa</p>
 *
 * <p>François Straet</p>
 */ 
public class SquareUpGenerator extends Generator {

    private double amplitude;
    private double frequency;

    /**
     * Consrtuctor with default sampling frequency 44100 Hz
     * 
     * @param frequency The frequency of sine wave to produce, in Hz
     * @param amplitude         The amplitude of sine wave to produce
     */
    public SquareUpGenerator(double frequency, double amplitude) {
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
    public SquareUpGenerator(double frequency, double amplitude, double fs) {
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
}
