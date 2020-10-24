package be.uliege.straet.oop.filters;

import java.util.HashMap;
import be.uliege.montefiore.oop.audio.Filter;

/**
 * Adds a method `getParameters` to the `Filter` interface, in order to be
 * able to get all the attributes we have to add when writing a 
 * `CompositeFilter` to a xml file.
 */
public interface WFilter extends Filter {
    /**
     * Gets the parameters related to the filter, e.g. the gain of a 
     * `GainFilter`.
     * @return      A `HashMap<String, String>` mapping all the parameters name
     *              to their values
     */
    public HashMap<String, String> getParameters();
}