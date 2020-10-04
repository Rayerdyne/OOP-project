package be.uliege.straet.oop.filters;

/** <p>INFO0062 - Object-Oriented Programming project.</p>
 * 
 * <p>Extention of the filter interface, to the specific filters that will 
 * support connections in feedback, i.e., the computation of their next output
 * does not need the new input.</p>
 * 
 * <p>In particular, delay blocks and integrators will implements this 
 * interface.</p>
 * 
 * <p>François Straet</p>
 */
public interface FeedbackableFilter extends WFilter {
    /**
     * @return          The output array that will be outputed on next step
     */
    public double[] incomingOutput();
}