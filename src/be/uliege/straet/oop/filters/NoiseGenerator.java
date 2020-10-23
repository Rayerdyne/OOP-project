package be.uliege.straet.oop.filters;

import java.util.HashMap;
import java.util.Random;

/**
 * <p>INFO0062 - Object-Oriented Programming project.</p>
 *
 * <p>A basic noise generator.</p>
 *
 * <p>François Straet</p>
 */
public class NoiseGenerator extends Generator {

    public static final double DEF_AMPLITUDE = SineGenerator.DEF_AMPLITUDE;

    private double amplitude2;
    private Random random = new Random(); // not a random line

    /**
     * Constructs a noise generator with default amplitude set to 1.
     */
    public NoiseGenerator() {
        this(DEF_AMPLITUDE);
    }

    /**
     * Constructor
     * @param amplitude           The amplitude of noise to produce
     */
    public NoiseGenerator(double amplitude) {
        this.amplitude2 = 2.0 * amplitude;
    }

    /**
     * Compute the output
     * @param input Remains from Filter interface, unused
     */
    public double[] computeOneStep(double[] input) {
    return new double[] { amplitude2 * (random.nextDouble() - 0.5) };
    }

    /**
     * Notice: values returned by incomingOutput and computeOneStep will not
     * be the same, but, its noise, so that I don't care =)
     */
    public double[] incomingOutput() {
        return new double[] { amplitude2 * (random.nextDouble() - 0.5) };
    }

    public HashMap<String, String> getParameters() {
        return new HashMap<String, String>();
    }

    /**
     * @return The amplitude of generated noise.
     */
    public double getAmplitude() { return amplitude2/2.0; }
}
