package be.uliege.straet.oop.filters;

import java.util.HashMap;
import be.uliege.montefiore.oop.audio.Filter;

/**<p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p><b>WriteableFilter</b></p>
 * <p>Adds a method `getParameters`, in order to be able to get all the 
 * attributes we have to add when writing a CompositeFilter to a xml file.
 * </p>
 */
public interface WFilter extends Filter {
    /**
     * Gets the parameters related to the filter, e.g. the gain of a 
     * `GainFilter`.
     * @return      A HashMap mapping all the parameters name to their values
     */
    public HashMap<String, String> getParameters();
}