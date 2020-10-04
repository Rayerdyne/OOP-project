package be.uliege.straet.oop.filters;

import be.uliege.montefiore.oop.audio.FilterException;

/** INFO0062 - Object-Oriented Programming
 *  Project.
 * 
 * This class implements an all-pass filter used for the reverberator
 *
 * François Straet
 */
public class AllPassFilter extends CompositeFilter {

    // instanciate members filters
    GainFilter gfp;
    GainFilter gfn;
    DelayFilter df;
    AdditionFilter afa = new AdditionFilter();
    AdditionFilter afb = new AdditionFilter();

    /**
     * Constructs a classic all-pass filter given its delay and its gain
     * @param gain                  The gain to use
     * @param delay                 The delay to use
     * @throws FilterException      If some filter could not be instanciated
     */
    public AllPassFilter(double gain, int delay) throws FilterException {
        super(1, 1);

        base(gain, delay);

        this.connectBlockToBlock(afa, 0, df, 0);
    }

    /**
     * Constructs an all-pass filter with an inner composite filter nested in.
     * Of course it will be used for nested all-pass filters needed for the 
     * reverberator.
     * @param gain                  The gain to use
     * @param delay                 The delay to use
     * @param inner                 The inner composite filter to nest in
     * @throws FilterException      If some filter could not be instanciated
     */
    public AllPassFilter (double gain, int delay, CompositeFilter inner) 
        throws FilterException {
        
        super(1, 1);
        if (inner.nbInputs() != 1 || inner.nbOutputs() != 1) {
            throw new FilterException("Inner filter for nested all-pass " + 
            "filter does not match number of inputs and outputs.");   
        }
        
        base(gain, delay);

        this.addBlock(inner);
        
        this.connectBlockToBlock(afa, 0, inner, 0);
        this.connectBlockToBlock(inner, 0, df, 0);
    }

    /**
     * Connects what is common between nested and normal allpass filter
     * @param gain                  The gain used
     * @param delay                 The delay used
     * @throws FilterException      If some filter could not be instanciated
     */
    private void base(double gain, int delay) throws FilterException {
        // instanciate filters that require the delay or the gain
        gfp = new GainFilter(gain);
        gfn = new GainFilter(-gain);
        df = new DelayFilter(delay);

        // add them to composite filter
        this.addBlock(gfp);
        this.addBlock(gfn);
        this.addBlock(df);
        this.addBlock(afa);
        this.addBlock(afb);

        // connections...
        this.connectInputToBlock(0, afa, 0);
        this.connectInputToBlock(0, gfn, 0);

        this.connectBlockToBlock(df, 0, afb, 1);
        this.connectBlockToBlock(afb, 0, gfp, 0);
        this.connectBlockToBlock(gfp, 0, afa, 1);
        this.connectBlockToBlock(gfn, 0, afb, 0);

        this.connectBlockToOutput(afb, 0, 0);
    }


}