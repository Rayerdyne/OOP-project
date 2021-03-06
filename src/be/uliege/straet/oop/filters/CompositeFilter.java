package be.uliege.straet.oop.filters;

import java.util.Vector;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collection;

import be.uliege.montefiore.oop.audio.FilterException;
import be.uliege.straet.oop.loader.Writer;

/**
 * Composite filter class, to build and hold "block diagrams" of filters.
 */
 public class CompositeFilter implements WFilter {

    private int nbInputs = 0, nbOutputs = 0;
    private HashMap<WFilter, Block> blocks;
    /** blocks connected to output, starting point when computing output */
    private Vector<Block> outputBlocks;  

    private boolean[] isOutputConnected;
    private ReadDouble[] outputRefs;
    private WriteDouble[] inputObjs;
    private ReadDouble[] inputRefs;

    private String fileName;

    /**
     * Constructor.
     * @param nbInputs          The number of inputs of the composite filter
     * @param nbOuputs          The number of outputs of the composite filter
     * @param fileName          The name of the file that represents this 
     *                          filter, to make writing to file easier
     * @throws FilterException  If a number of input-output is <= 0
     */
    public CompositeFilter(int nbInputs, int nbOutputs, String fileName) throws
        FilterException {

        if (nbInputs <= 0) 
            throw new FilterException("A composite filter should have a " + 
                "strictly positive number of inputs !");
        if (nbOutputs <= 0) 
            throw new FilterException("A composite filter should have a " +
                "strictly positive number of outputs !");
        this.nbInputs = nbInputs;
        this.nbOutputs = nbOutputs;

        blocks = new HashMap<WFilter, Block>();
        outputBlocks = new Vector<Block>();
        outputRefs = new ReadDouble[nbOutputs];

        inputObjs = new WriteDouble[nbInputs];
        inputRefs = new ReadDouble[nbInputs];
        for (int i = 0; i < nbInputs; i++) {
            inputObjs[i] = new WriteDouble();
            inputRefs[i] = new ReadDouble(inputObjs[i], i);
        }
        
        isOutputConnected = new boolean[nbOutputs];
        Arrays.fill(isOutputConnected, false);

        this.fileName = fileName;
    }

    /**
     * Constructor.
     * @param nbInputs          The number of inputs of the composite filter
     * @param nbOuputs          The number of outputs of the composite filter
     * @throws FilterException  If a number of input-output is <= 0
     */
    public CompositeFilter(int nbInputs, int nbOutputs) throws 
        FilterException {
        this(nbInputs, nbOutputs, null);
    }

    /**
     * Constructs an invalid {@code CompositeFilter}. This is useful when 
     * placing a {@code DCompositeFilter} whose file representing the desired
     * filter has not been set.
     */
    public CompositeFilter() {}

    /**
     * @return      All the {@code Block}s contained in that
     * {@code CompositeFilter}
     */
    public Collection<Block> blocks() {
        return this.blocks.values();
    }

    /**
     * Adds a block to the composite filter (unconnected). Does nothing if the
     * filter is already addded.
     * @param f                     The filter to add.
     * @throws FilterException      If the {@code Block} could not be   
     *                              instanciated
     */
    public void addBlock(WFilter f) throws FilterException {
        if (!blocks.containsKey(f)) {
            blocks.put(f, new Block(f));
        }
    }

    /**
     * Adds a block with an id to the composite filter (unconnected). Does 
     * nothing if the filter is already addded.
     * @param f                     The filter to add.
     * @param id                    The id of the {@code Block} to create
     * @throws FilterException      If the {@code Block} could not be 
     *                              instanciated
     */
    public void addBlock(WFilter f, String id) throws FilterException {
        if (!blocks.containsKey(f)) {
            blocks.put(f, new Block(f, id));
        }
    }

    /**
     * Connects two sub-filters, input to output of the composite filter.
     * @param f1                    The filter to connect the output
     * @param o1                    The index of the output to be connected
     * @param f2                    The filter to connect the input
     * @param i2                    The index of the input to be connected
     * @throws FilterException      If the connection could not be made
     */
    public void connectBlockToBlock(WFilter f1, int o1, WFilter f2, int i2) 
        throws FilterException  {
    
        Block b1 = blockFromFilter(f1);
        Block b2 = blockFromFilter(f2);

        b2.connectInput(b1, o1, i2);
    }

    /**
     * Connects output of sub-filter to composite filter's output.
     * @param f1                    The filter to connect
     * @param o1                    The index of the output of the filter
     * @param o2                    The index of the output of the composite 
     *                              filter (this)
     * @throws FilterException      If some index are mismatched
     */
    public void connectBlockToOutput(WFilter f1, int o1, int o2) 
        throws FilterException {

        checkIndex("output", o2, nbOutputs);
        Block b1 = blockFromFilter(f1);
            
        if (outputBlocks.indexOf(b1) == -1)
            outputBlocks.add(b1);

        outputRefs[o2] = b1.outputRead(o1);
        isOutputConnected[o2] = true;
    }

    /**
     * Connects input of sub-filter to composite filter's input.
     * @param i1                    The index of the input of the filter
     * @param f2                    The filter to connect
     * @param i2                    The index of the input of the filter
     * @throws FilterException      If some index are mismatched
     */
    public void connectInputToBlock(int i1, WFilter f2, int i2)
        throws FilterException {
        checkIndex("input", i1, nbInputs);

        Block b2 = blockFromFilter(f2);
        b2.connectInput(inputRefs[i1], i2);
    }

    /**
     * Computes one step of the filter, i.e. propagates results for a given 
     * input.
     * @param input                 The input to compute.
     * @throws FilterException      If the input length is mismatched, or there
     *                              is a connection to output missing, or the
     *                              computation of the step threw an excpetion
     */
    public double[] computeOneStep(double[] input) throws FilterException {
        if (input.length != nbInputs) {
            throw new FilterException("Invalid input length (is " + 
                String.valueOf(input.length) + " instead of " + 
                String.valueOf(this.nbInputs) + ").");
        }

        for (int i = 0; i < nbOutputs; i++) {
            if (! isOutputConnected[i])
                throw new FilterException(
                    "All outputs aren't yet connected !");
        }

        for (int i = 0; i < nbInputs; i++)
            inputObjs[i].value = input[i];

        blocks.forEach((f, b) -> {   b.resetState();   });

        double[] output = new double[nbOutputs];
        Vector<Block> updateAfter = new Vector<Block>();
        // the remaining blocks to update, i.e. the DelayBlocks
        
        for (int i = 0; i < outputBlocks.size(); i++) {
            outputBlocks.get(i).computeOneStep(updateAfter);
        }
        
        Vector<Block> updateAfter2 = new Vector<Block>();
        // it may happen multible feedback loops
        while (!updateAfter.isEmpty()) {
            for (int i = 0; i < updateAfter.size(); i++) {
                updateAfter.get(i).computeOneStep(updateAfter2);
            }
            updateAfter = updateAfter2;
            updateAfter2.clear();
        }

        for (int i = 0; i < nbOutputs; i++) 
            output[i] = outputRefs[i].value();

        return output;
    }

    /**
     * Gets the {@code Block} corresponding to a given filter.
     * @param f                     The filter to get the corresponding block
     * @throws FilterException      If the filter could not be found, i.e. it
     *                              has not been added
     */
    private Block blockFromFilter(WFilter f) throws FilterException {
        Block b= blocks.get(f);

        if (b == null)
            throw new FilterException("Could not find filter.");

        return b;
    }

    /**
     * Checks if 0 < value < max, if not, throws an error.
     * @param type                  The type of the index, to be written in
     *                              the error message
     * @param value                 The actual value of the index
     * @param max                   The upper bound for the index
     * @throws FilterException      If index mismatches
     */
    private static void checkIndex(String type, int value, int max) 
        throws FilterException {
        
        if (value >= max || value < 0) 
            throw new FilterException("Invalid " + type + " index (is " +
                value + ", but should be in [0; " + max  + "[).");
    }

    /**
     * @return      The number of inputs of the composite filter
     */
    public int nbInputs() {
        return nbInputs;
    }

    /**
     * @return      The number of outputs of the composite filter
     */
    public int nbOutputs() {
        return nbOutputs;
    }

    /**
     * @param i                 The index of the output we want to get
     * @return                  A {@code ReadDouble} to the i-th output of the
     *                          filter
     * @throws FilterException  If index exceeds the output count.
     */
    public ReadDouble output(int i) throws FilterException {
        if (i > outputRefs.length)
            throw new FilterException("Index " + i + " exceeds array length " 
                + outputRefs.length + ".");
        return outputRefs[i];
    }

    /**
     * Reset method for filter interface, i.e. resets all filters present in
     * the composite filter (even if they are not connected).
     */
    public void reset() {
        blocks.forEach((f, b) -> {
            f.reset();
        });
    }

    @Override
    public HashMap<String, String> getParameters() {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put(Writer.SRC_FILE_ATTR_NAME, String.valueOf(fileName));
        return hm;
    }

    @Override
    public int getMaxSamplesInfluenced() { 
        int n = 0;
        for (WFilter f : blocks.keySet()) 
            n += f.getMaxSamplesInfluenced();

        return n;
    }

    /**
     * Gets the unique id associated to a filter.
     * @param f     The {@code WFilter}
     * @return      The id      
     */
    public String idOf(WFilter f) {
        Block b = blocks.get(f);
        if (b == null) {
            System.out.println("Block not found...");
        }
        return b.id();
    }

    /**
     * @param fileName      The name of the file used to build this 
     *                      {@code CompositeFilter}
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return The name of the file used to build this {@code CompositeFilter}
     */
    public String getFileName() { return fileName; }

    /**
     * <p>Computes a vector of numbers, returned as a {@code Double[]}, that is
     * the convolution vector equivalent to the application of that filter to
     * the input. That is, applying the filter or a {@code ConvolutionFilter}
     * with that vector in argument will have approximately the same effect.
     * </p><p>Note that this will have to {@code reset()} this 
     * {@code CompositeFilter}</p>
     * @param precision         The maximal difference between two consecutive
     *                          outputs that has to be carried out 
     *                          {@code getMaxSamplesInfluenced()} times.
     * @return                  A {@code double[]} containing the values of 
     *                          that vector
     * @throws FilterException  If this {@code CompositeFilter} is invalid and
     *                          any output could be computed.
     */
    public double[] computeEquivalentConvolutionVector(double precision) 
        throws FilterException {

        int maxsi = this.getMaxSamplesInfluenced();
        if (maxsi < 0 || maxsi >= WFilter.INFINITY)
            throw new FilterException("Could not compute equivalent " + 
                "convolution vector as the maximum number of samples " +
                "influenced is unbouned (due to generator).");

        Vector<Double> v = new Vector<Double>();
        
        this.reset();
        double cur, prev;
        cur = this.computeOneStep(new double[]{ 1 })[0];
        int i = 0;
        do {
            prev = cur;
            cur = this.computeOneStep(new double[] { 0 })[0];
            v.add(prev);
            if (Math.abs(cur - prev) < precision)
                i++;
            else 
                i = 0;
        } while (i <= maxsi + 1);

        this.reset();

        double[] res = new double[v.size() - maxsi + 1];
        for (i = 0; i < v.size() - maxsi + 1; i++) 
            res[i] = v.get(i);
        
        return res;
    }
}