package be.uliege.straet.oop.filters;

import java.util.Arrays;
import java.util.HashMap;

import be.uliege.montefiore.oop.audio.FilterException;

/** 
 * This filter represents a delay filter, i.e. it shifts its input by a 
 * constant number of samples.
 */
public class DelayFilter implements FeedbackableFilter {
    public static final int DEF_SHIFT = 22000;

    private double[] values;
    /** As data is stored in a circular array, index we are reading at */
    private int index;  
    
    /**
     * Constructor.
     * @param shift         The number of samples to wait until the first input
     *                      is returned. 
     */
    public DelayFilter (int shift) {
        values = new double[shift];
        Arrays.fill(values, 0.0);
        index = 0;
    }

    /**
     * Constructor with default shift of DEF_SHIFT.
     */
    public DelayFilter() {
        this(DEF_SHIFT);
    }

    /**
     * <p>Computes one step of the filter, i.e.:<ul> 
     * <li> Reads the value stored at current position, ans store it in output
     * </li>
     * <li> Increment index and place the input there</li>
     * <li> Manage index too large.</li></ul></p>
     * @param input                 The input to compute.
     * @throws FilterException      If the input's length is mismatched
     */
    public double[] computeOneStep(double[] input) throws FilterException {
        if (input.length != nbInputs()) {
            throw new FilterException("Invalid input length (is " + 
                String.valueOf(input.length) + " instead of 1).");
        }

        double[] output = new double[] { values[index] };

        values[index] = input[0];
        index++;
        if (index >= values.length)
            index = 0;
        return output;
    }

    /**
     * @return      The number of inputs, i.e. 1.
     */
    public int nbInputs() {
        return 1;
    }

    /**
     * @return      The number of outputs, i.e. 1.
     */
    public int nbOutputs() {
        return 1;
    }

    /**
     * <p>Reset method for filter interface:<ul>
     * <li> Refill values with zeros</li>
     * <li> Reset index to zero (unusefull, because circular array, but why 
     * not ?)</li></ul></p>
     */
    public void reset() {
        index = 0;
        Arrays.fill(values, 0.0);
    }

    /**
     * Returns the incoming value, to handle feeback in {@code CompositeFilter}.
     * @return      The next output of the filter
     */
    public double[] incomingOutput() {
        return new double[] { values[index] };
    }

    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("delay", String.valueOf(values.length));
        return hm;
    }

    /**
     * @return      Returns the number of samples the {@code DelayFilter} shifts its
     *              input by
     */
    public int getShift() { return values.length; }
}
