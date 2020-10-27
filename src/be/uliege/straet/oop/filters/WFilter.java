package be.uliege.straet.oop.filters;

import java.util.HashMap;
import be.uliege.montefiore.oop.audio.Filter;

/**
 * Adds the methods {@code getParameters} and {@code getMaxSamplesInfluenced} 
 * to the {@code Filter} interface, in order to be able to get all the 
 * attributes we have to add when writing a {@code CompositeFilter} to a xml 
 * file, and to compute the maximum number of sample that will be influenced by
 * this filter in the future.
 */
public interface WFilter extends Filter {
    /** The greatest value an {@code int} can take in java. */
    public static final int INFINITY = 2147483647;

    /**
     * Gets the parameters related to the filter, e.g. the gain of a 
     * {@code GainFilter}.
     * @return      A {@code HashMap<String, String>} mapping all the 
     *              parameters name to their values
     */
    public HashMap<String, String> getParameters();

    /**
     * <p>Returns the number of sample that could be influenced by an input at
     * given time {@code t}. I.e., the greatest {@code n} such that the output
     * at time {@code t+n} depends on the input at time {@code t}.</p>
     * <p>An input has no influence if as we repeatedly input 0, the output
     * will remain 0.</p>
     * @return      The max number of sample influenced
     */
    public int getMaxSamplesInfluenced();
}