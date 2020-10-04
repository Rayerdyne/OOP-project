/** INFO0062 - Object-Oriented Programming
 *  Project.
 * 
 * This class holds the information that a filter needs to "propagate", i.e.
 * references to its inputs and outputs.
 * 
 * A Block has references to read-only object for each of its inputs, and write
 *  and read objects for its input, in order to avoid instanciating  several 
 * times a read-only reference to the same output.
 * 
 * François Straet
 */

package be.uliege.straet.oop.filters;

import java.util.Vector;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;

import be.uliege.montefiore.oop.audio.FilterException;

public class Block {
    private static final int SLEEPING = 0;
    private static final int WAIT_INPUT = 1;
    private static final int FINISHED = 2;
    private static final String DEF_PREFIX = "filter_";
    private static int count = 0;
    private static HashSet<String> idInUse = new HashSet<String>();

    private static HashMap<String, String> nameMap;

    private WFilter filter;
    private FeedbackableFilter feedbackableFilter = null;
    private boolean isFeedbackableFilter = false;
    private int state;

    private String id;

    private Vector<Block> parents = new Vector<Block>();
    private double[] input;
    private ReadDouble[] inputRefs; 
    private boolean[] isInputConnected;
    private ReadDouble[] outputRefs; // so that we instanciate only one object
    private WriteDouble[] outputObjs;

    /**
     * Contructor.
     * @param filter                The filter associated
     */
    public Block(WFilter filter) throws BlockException {
        this(filter, null);
    }

    /**
     * Contructor.
     * @param filter                The filter associated
     * @param id                    The id given to the Block
     * @throws BlockException       If something went wrong, i.e. the class 
     *                              is not found, or the filter is null.
     */
    public Block(WFilter filter, String id) throws BlockException {
        if (filter == null)
            throw new BlockException("Given filter is null.");
        this.filter = filter;

        // initialize IO stuff
        input = new double[filter.nbInputs()];
        inputRefs = new ReadDouble[filter.nbInputs()];
        isInputConnected = new boolean[filter.nbInputs()];
        Arrays.fill(isInputConnected, false);

        outputObjs = new WriteDouble[filter.nbOutputs()];
        outputRefs = new ReadDouble[filter.nbOutputs()];

        for (int i = 0; i < filter.nbOutputs(); i++) {
            outputObjs[i] = new WriteDouble();
            outputRefs[i] = new ReadDouble(outputObjs[i], this, i);
        }

        // check for FeedbackableFilters
        if (filter instanceof FeedbackableFilter) {
            isFeedbackableFilter = true;
            this.feedbackableFilter = (FeedbackableFilter) filter;
        }

        // set the id
        if (id != null && id.startsWith(DEF_PREFIX)) {
            throw new BlockException("Attempted to set the id a Block with " + 
                "forbidden prefix " + DEF_PREFIX + ".");
        }
        if (id == null || (id != null && id.length() == 0))
            this.id = DEF_PREFIX + type() + (count++);
        else {
            if (idInUse.contains(id)) 
                throw new BlockException("Id " + id + " is already used.");
            this.id = id;
            idInUse.add(id);
        }

        // System.out.println("dabou: " + this.id);

        state = SLEEPING;
    }

    /**
     * <p>Parents of a Blocks</p>
     * @return  A vector containing all the parents of the Block
     *
    public Vector<Block> parents() {
        return this.parents;
    }*/

    /**
     * <p>Returns the name of the node for representing the filter contained 
     * in the Block</p>
     * @return                  The name for the xml node
     * @throws BlockException   If the class name has not been found
     */
    public String type() throws BlockException {
        return typeOfFilter(filter);
    }

    /**
     * @return                  The filter contained in this Block
     */
    public WFilter filter() {
        return filter;
    }

    /**
     * Refreshes the input with new values referenced in inputRefs
     */
    private void refreshInput() {
        for (int i = 0; i < filter.nbInputs(); i++) {
            input[i] = inputRefs[i].value();
        }
    }

    /**
     * Returns a reference to a ReadDouble containing the o-th output.
     * @param o                 The index of output to return a reference to
     * @return                  The ReadDouble to the o-th output
     */
    public ReadDouble outputRead(int o) throws BlockException {
        
        checkIndex("output", o, filter.nbOutputs());
        return outputRefs[o];
    }

    /**
     * Returns the Block current state.
     * @return                  The Block current state
     */
    private int state() {
        return state;
    }

    /**
     * Resets the Block state to SLEEPING, at the beginning of each 
     * computation of output.
     */
    public void resetState() {
        state = SLEEPING;
    }

    /**
     * Wether or not the Blocks contains a class implementing 
     * FeedbackableFilter.
     * @return                      true when FeedbackableFilter, else false
     */
    private boolean isFeedbackableFilter() {
        return isFeedbackableFilter;
    }

    /**
     * Connects output of another Block, as an input of this (Block).
     * @param b1                    The Block to connect
     * @param o1                    The index of the output of the Block
     * @param i2                    The index of the input of this Block 
     *                              filter (this)
     * @throws BlockException       If the connection could not be done, e.g.
     *                              the input-output index are invalid.
     */
    public void connectInput(Block b1, int o1, int i2) throws BlockException {
        checkIndex("input", i2, filter.nbInputs());
        checkIndex("input", i2, filter.nbInputs());
        
        if (parents.indexOf(b1) == -1)
            parents.add(b1);

        inputRefs[i2] = b1.outputRead(o1);
        isInputConnected[i2] = true;
    }

    /**
     * Connects the i1 input of the Block to an arbitrary ReadDouble input,
     * in order to connect a Block to the input of the composite filter.
     * @param input                 The ReadDouble to connect
     * @param i1                    The index of the input we connect.
     * @throws BlockException       If the connection could not be done, e.g.
     *                              the input-output index are invalid.
     */
    public void connectInput(ReadDouble input, int i1) throws BlockException {
        checkIndex("input", i1, filter.nbInputs());
        
        inputRefs[i1] = input;
        isInputConnected[i1] = true;
    }

    /**
     * Makes the filter run on the current input and update outputs values.
     * @throws FilterException      If the filter could not compute one sep.
     */
    public void computeFilterStep() throws FilterException {
        double[] output = filter.computeOneStep(input);
        for (int i = 0; i < filter.nbOutputs(); i++)
            outputObjs[i].value = output[i];
        
    }

    /**
     * Computes one step of the whole "Block diagram", and returns a vector
     * containing the Blocks that will have to be updated.
     * @param updateAfter               A vector of Block containing the Blocks
     *                                  that will have to be updated.
     * @throws BlockException           If something impossible occured in the
     *                                  connections (impossible feedback loop,
     *                                  no input connection...)
     * @throws FilterException          If some filter could not compute one 
     *                                  step.
     */
    public Vector<Block> computeOneStep(Vector<Block> updateAfter)
        throws BlockException, FilterException {

        for (int i = 0; i < filter.nbInputs(); i++) {
            if (!isInputConnected[i])
                throw new BlockException("Missing input connections.");
        }
        state = WAIT_INPUT;

        for (int i = 0; i < parents.size(); i++) {
            Block parent = parents.get(i);

            if (parent.isFeedbackableFilter()) {
                updateAfter.add(parent);
                parent.moveOn();
            }
            else if (parent.state() == SLEEPING)
                parent.computeOneStep(updateAfter);
            else if (parent.state() == WAIT_INPUT)
                throw new BlockException("Impossible feedback loop occured.");
            
            // else (parent.state() == FINISHED) nothing.
        }

        refreshInput();
        computeFilterStep();
        state = FINISHED;

        return updateAfter;
    }

    /**
     * Makes the filter to set its output to the next values if availible.
     * @throws BlockException       If we try to get the predicted incoming 
     *                              value of a non-feedbackable filter
     */
    private void moveOn() throws BlockException {
        if (!isFeedbackableFilter)
            throw new BlockException("Tried to move on without input on a non-delay Block.");
        
        double[] incoming = feedbackableFilter.incomingOutput();
        for (int i = 0; i < filter.nbOutputs(); i++)
            outputObjs[i].value = incoming[i];
    }

     /**
     * Checks if 0 < value < max, if not, throws an error. 
     * @param type                  The type of the index, to be written in
     *                              the error message
     * @param value                 The actual value of the index
     * @param max                   The upper bound for the index
     * @throws BlockException       If index are mismatched.
     */
    private static void checkIndex(String type, int value, int max) 
        throws BlockException {
        
        if (value >= max || value < 0) 
            throw new BlockException("Invalid " + type + " index (is " +
                value + ", but should be in [0; " + max + "[).");
    }

    /**
     * @return          The id associated to this Block
     */
    public String id() {  return this.id;  }

    /**
     * @return          The number of inputs of the filter contained in this 
     *                  Block
     */
    public int nbInputs() { return filter.nbInputs(); }

    /**
     * @return          The number of outputs of the filter contained in this 
     *                  Block
     */
    public int nbOutputs() { return filter.nbOutputs(); }

    /**
     * @return          i-th reference to ReadDouble describing the i-th input 
     *                  of the Block.
     */
    public ReadDouble input(int i) throws BlockException {
        if (i >= inputRefs.length)
            throw new BlockException("Index " + i + "exceeds array length " + 
                inputRefs.length + ".");
        return inputRefs[i];
    }

    /**
     * @return          i-th reference to ReadDouble describing the i-th 
     *                  output of the Block.
     */
    public ReadDouble output(int i) throws BlockException {
        if (i >= outputRefs.length)
            throw new BlockException("Index " + i + "exceeds array length " + 
                outputRefs.length + ".");
        return outputRefs[i];
    }


    static {
        nameMap = new HashMap<String, String>();
        nameMap.put("CompositeFilter"         , "composite");
        nameMap.put("AdditionFilter"          , "addition");
        nameMap.put("DelayFilter"             , "delay");
        nameMap.put("GainFilter"              , "gain");
        nameMap.put("ConvolutionFilter"       , "convolution");
        nameMap.put("DifferentiatorFilter"    , "differentiator");
        nameMap.put("IntegratorFilter"        , "integrator");
        nameMap.put("NoiseGenerator"          , "noise_generator");
        nameMap.put("SineGenerator"           , "sine_generator");
        nameMap.put("SquareUpGenerator"       , "square_up_generator");
        nameMap.put("SquareCenteredGenerator" , "square_centered_generator");
    }

    /**
     * <p>Returns the name in xml files associated to an instance name</p>
     * @param s                 
     * @return                  The name to describe that instance in xml files
     * @throws BlockException   If s is not found
     */
    public static String typeOfFilter(WFilter f) throws BlockException {
        String s = f.getClass().getName();
        String[] parts = s.split("[.]");
        String t = parts[parts.length - 1];

        if (!nameMap.containsKey(t)) {
            if (f instanceof CompositeFilter)
                return nameMap.get("CompositeFilter");
            throw new BlockException("Class name \"" + s + "\" not found");
        }
        
        return nameMap.get(t);
    }
}
