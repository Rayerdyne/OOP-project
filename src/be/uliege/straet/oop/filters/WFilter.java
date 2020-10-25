package be.uliege.straet.oop.filters;

import java.util.HashMap;
import be.uliege.montefiore.oop.audio.Filter;

/**
 * Adds a method {@code getParameters} to the {@code Filter} interface, in order to be
 * able to get all the attributes we have to add when writing a 
 * {@code CompositeFilter} to a xml file.
 */
public interface WFilter extends Filter {
    /**
     * Gets the parameters related to the filter, e.g. the gain of a 
     * {@code GainFilter}.
     * @return      A {@code HashMap<String, String>} mapping all the parameters name
     *              to their values
     */
    public HashMap<String, String> getParameters();
}