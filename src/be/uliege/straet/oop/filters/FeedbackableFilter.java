package be.uliege.straet.oop.filters;

/** 
 * <p>Extention of the filter interface, to the specific filters that will 
 * support connections in feedback, i.e., the computation of their next output
 * does not need the new input.</p>
 * 
 * <p>In particular, delay blocks and integrators will implements this 
 * interface.</p>
 */
public interface FeedbackableFilter extends WFilter {
    /**
     * For `FeedbackableFilter`s instances,
     * @return          The output array that will be outputed on next step
     */
    public double[] incomingOutput();
}