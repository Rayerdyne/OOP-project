package be.uliege.straet.oop.loader;

import be.uliege.straet.oop.filters.*;
import be.uliege.straet.oop.gui.DraggableFilter;
import be.uliege.montefiore.oop.audio.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <p>Holds all informations that contained in a possible node in a file 
 * describing the composite filter.</p>
 * 
 * <p>All cases will have member type set to their actual type.</p>
 * 
 * <p> For a standard filter, it holds: <ul>
 * <li> An id, which is used to recognize the filter </li>
 * <li> Two arrays, inputsIds and inputsNums, containing logically at index i
 * the id of the filter that will be connected at the input i and the index
 * of the output refering to the filter to connect. </li>
 * <li> A filter, an instance of the filter that will have to be connected.
 * </li></ul></p>
 * 
 * <p>As the input of composite filters has also to be set, there is a special
 * case where we have no filter, but just stuff in order to connect the 
 * output  to composite filter's output:<ul>
 * <li> fOutputId, the id of the filter to connect the output</li>
 * <li> fOutputNum, the index of the output to connect</li>
 * <li> cfOutputNum, the index of the output of the composite filter to 
 *      connect.</li></ul></p>
 * 
 * <p>In such a case, specialCase will be set to OUTPUT_CONNECTION.</p>
 * 
 * <p>In the case of a let statement, which only defines the value of a 
 * variable, specialCase will become LET_STATEMENT</p>
 */
public class NodeData {
    public static final int NONE = 0;
    public static final int OUTPUT_CONNECTION = 1;
    public static final int LET_STATEMENT = 2;
    public static final int INPUT_NODE = 3;

    /** The {@code Node} the data is represented */
    public Node node;
    /** A {@code DraggableFilter} corresponding to that {@code Node} */
    public DraggableFilter draggableFilter;
    /** <p>A code to easily check potential particular types of nodes.</p>
     * <p>Possible values: </p>
     * <ul> <li> NONE </li>
     *      <li> OUTPUT_CONNECTION </li>
     *      <li> LET_STATEMENT </li>
     *      <li> INPUT_NODE </li>
     */
    public int specialCase = NONE;
    /** When true, input or output files are correctly specified in the file,
     * so that the whole xml file could be run without input and output 
     * specifications */
    public boolean standaloneRunOk = true;

    // output specific members
    /** The id of the output of that filter */
    public String fOutputId;
    /** The number of the output of that filter */
    public int fOutputNum;
    /** If connected to a `CompositeFilter`'s output, the number of the output
     * of the {@code CompositeFilter} it will be connected */
    public int cfOutputNum;
    /** <p>If input or output filter, the name of the file to read/write.</p>
     * <p>If composite filter from source file, name of that file.</p> */
    public String ioFileName;

    // "standard" types members
    /** The type of the filter (e.g. AdditionFilter, DelayFilter etc.) */
    public String type;
    /** The unique id of the filter */
    public String id;
    /** The ids of the parent filters that have an output connected to an 
     * input of this filter */
    public String[] inputsIds;
    /** The numbers of output, w.r.t. the parent filters that have an output 
     * connected to an input of this filter */
    public int[] inputsNums;
    // Loader l;

    // variable declaration specific members
    /** If the filter represents a variable declaration, the name of that 
     * variable */
    public String variableName;
    /** If the filter represents a variable declaration, the definition of 
     * that variable */
    public String variableDefinition;

    public int x = 0, y = 0, orientation = 0;
    /** Wether or not the position of the filter is set, i.e. if the 
     * coordinates have already been read */
    public boolean isPositionSet = false;

    /** The {@code WFilter} corresponding to the filter */
    public WFilter filter;

    /**
     * Builds a NodeData based on the given node.
     * @param n          The node to process
     * @param parameters A HashMap with values of the parameters introduced 
     *                   in the input file
     * @param verbose    If true, print connections info
     * @throws LoaderException         If an attribute is missing
     * @throws FilterException         If sth goes wrong when instanciating
     *                                 sub-composite filters.
     * @throws NumberFormatException        If an error occured when 
     *                                      parsing a String to a value
     * @throws IOException                  As it can instanciate filter 
     *                                      from other files, all the 
     *                                      exceptions also thrown by load
     * @throws SAXException                 Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     */
    public NodeData(Node n, HashMap<String, Double> parameters, 
        boolean verbose) throws LoaderException, NumberFormatException,
        FilterException, ParserConfigurationException, 
        SAXException, DOMException, IOException {

        ValueMapper attributes = new ValueMapper(n);
        node = n;
        type = n.getNodeName();

        // node id
        if (type.equals(Writer.OUTPUT_NODE_TAG) ||
            type.equals(Writer.INPUT_POS_NODE_TAG)) {
            
            Node numNode = attributes.getNamedItem(Writer.NB_IOPUTS_ATTR_NAME);
            if (numNode == null)
                throw new LoaderException("No " + type + " index provided " + 
                    "for a " + type + " node.");
            id = type + "_" + numNode.getNodeValue();
        }
        else if (!type.equals(Writer.VARIABLE_NODE_TAG)) { 
                                                // specialCase flag is not set
            Node idNode = attributes.getNamedItem(Writer.ID_ATTR_NAME);
            if (idNode == null)
                throw new LoaderException("No id provided for a " + type
                    + " node.");
            id = idNode.getNodeValue();
        }

        getTypeContent(n, parameters, verbose);
        try {
            getPositionning(n, attributes);
            isPositionSet = true;
        } catch (LoaderException e) {
            isPositionSet = false;
            throw e;
        }

        // get all the input references
        if (specialCase == NONE)
            getInputsRefs(attributes);

    } // NodeData constructor

    /**
     * <p>Sets members relative to the type of the node i.e. members:<ul>
     * <li> filter in case of a standard filter node </li>
     * <li> specialCase set to true, fOuputNum, fOuputId, cfOutputNum 
     * in case of a node representing an output connection to a composite 
     * filter.</li></ul></p>
     * @param n                     The node to extract data
     * @param parameters            A HashMap with values of the parameters
     *                              introduced in the input file
     * @param verbose               If true, print connection info
     * @throws NumberFormatException If If an error occured when parsing a
     *                              String to a value
     * @throws FilterException      As it can instanciate filter from other
     *                              files, all the exceptions also thrown 
     *                              by load
     * @throws IOException                  Idem
     * @throws SAXException                 Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     */
    private void getTypeContent(Node n, HashMap<String, Double>
        parameters, boolean verbose) throws NumberFormatException,
        DOMException, FilterException, ParserConfigurationException,
        SAXException, IOException {

        ValueMapper attributes = new ValueMapper(n);
        switch (type) {
            case Writer.GAIN_F_NODE_TAG:
                double gain = getNodeParamValue(attributes, "gain", 
                                                parameters)[0];
                filter = new GainFilter(gain);
                break;
            case Writer.DELAY_F_NODE_TAG:
                int delay = (int) Math.round(getNodeParamValue(attributes, 
                    Writer.DELAY_F_NODE_TAG, parameters)[0]);
                filter = new DelayFilter(delay);
                break;
            case Writer.ADDITION_F_NODE_TAG:
                filter = new AdditionFilter();
                break;
            case Writer.INTEGRATOR_F_NODE_TAG:   // we could handle the case of
                filter = new IntegratorFilter(); // fs != 44100
                break;
            case Writer.DIFFERENTIATOR_F_NODE_TAG:
                filter = new DifferentiatorFilter();
                break;
            case Writer.CONVOLUTION_F_NODE_TAG:
                double[] v = getNodeParamValue(attributes, 
                    Writer.CONVOLUTION_VECTOR_ATTR_NAME, parameters);
                filter = new ConvolutionFilter(v);
                break;
            case Writer.SINE_GEN_NODE_TAG: {
                double frequency = getNodeParamValue(attributes, 
                    Writer.FREQUENCY_ATTR_NAME, parameters)[0];
                double amplitude = getNodeParamValue(attributes, 
                    Writer.AMPLITUDE_ATTR_NAME, parameters)[0];
                double fs = getNodeParamValue(attributes, 
                    Writer.FREQUENCY_ATTR_NAME, parameters)[0];
                filter = new SineGenerator(frequency, amplitude, fs);
                break; }
            case Writer.UP_SQUARE_GEN_NODE_TAG: {
                double frequency = getNodeParamValue(attributes, 
                    Writer.FREQUENCY_ATTR_NAME, parameters)[0];
                double amplitude = getNodeParamValue(attributes, 
                    Writer.AMPLITUDE_ATTR_NAME, parameters)[0];
                double fs = getNodeParamValue(attributes, 
                    Writer.FREQUENCY_ATTR_NAME, parameters)[0];
                filter = new UpSquareGenerator(frequency, amplitude, fs);
                break; }
            case Writer.CENTERED_SQUARE_GEN_NODE_TAG: {
                double frequency = getNodeParamValue(attributes, 
                    Writer.FREQUENCY_ATTR_NAME, parameters)[0];
                double amplitude = getNodeParamValue(attributes, 
                    Writer.AMPLITUDE_ATTR_NAME, parameters)[0];
                double fs = getNodeParamValue(attributes, 
                    Writer.FREQUENCY_ATTR_NAME, parameters)[0];
                filter = new CenteredSquareGenerator(frequency, amplitude, fs); 
                break; }
            case Writer.NOISE_GEN_NODE_TAG: {
                double amplitude = getNodeParamValue(attributes, 
                    Writer.AMPLITUDE_ATTR_NAME, parameters)[0];
                filter = new NoiseGenerator(amplitude); 
                break;}
            case Writer.OUTPUT_NODE_TAG:
                setOutputMembers(n, attributes);
                break;
            case Writer.INPUT_POS_NODE_TAG:
                specialCase = INPUT_NODE;
                setIOFileName(n, attributes);
                break;
            case Writer.VARIABLE_NODE_TAG:
                addVariables(n, attributes, parameters);
                break;
            case Writer.NESTED_COMPOSITE_NODE_TAG:
                filter = Loader.filterFromNode(n, parameters, verbose);
                break;
            case Writer.COMPOSITE_F_NODE_TAG:
                setFilterFromSrc(n, attributes, parameters, verbose);
                break;

            default:
                throw new LoaderException("Type \"" + type +
                    "\" not found.");
        }
    }

    /**
     * Gets the x and y coordinates and orientation if present in the node.
     * @param n          The node to get its position and orientation
     * @param attributes The {@code ValueMapper} that contains the data of the
     *                   {@code Node} we are interested in
     * @throws LoaderException If some value is not valid, but not when it is 
     *                         absent
     */
    private void getPositionning(Node n, ValueMapper attributes) 
        throws LoaderException {
        try {
            x = (int) getNodeParamValue(
                attributes, Writer.X_COORD_ATTR_NAME, null)[0];
            y = (int) getNodeParamValue(
                attributes, Writer.Y_COORD_ATTR_NAME , null)[0];
            orientation = (int) getNodeParamValue(attributes, 
                Writer.ORIENTATION_ATTR_NAME, null)[0];
            // System.out.println("id: " + id + " (x, y) = (" + x + ", " + y
            // + ").");
        } catch (LoaderException le) {
            String m = le.getMessage();
            // rip regex...
            if (!(m.startsWith("No ") && m.contains("provided. "))) 
                throw le;
        }
    }

    /**
     * <p>Returns the value of the parameter associated to a basic filter.</p>
     * <p> It returns a array of doubles, elements have to be separated by
     * commas. </p>
     * <p>Parses an expression made of '+' and '*', use "+ -1" for 
     * substractions.</p>
     * @param vm                    A {@code ValueMapper} listing the 
     *                              attributes of the {@code Node}
     * @param valueName             The name of the value to get, i.e. the 
     *                              one of the attribute
     * @param parameters            A {@code HashMap} with values of the 
     *                              parameters introduced in the xml input file
     * @return                      The values contained (array >1 if 
     *                              convolution)
     * @throws LoaderException      If no valid value is given
     */
    private double[] getNodeParamValue(ValueMapper vm, String valueName, 
        HashMap<String, Double> parameters) throws LoaderException {

        if (parameters == null)
            parameters = new HashMap<String, Double>();

        Node valueNode = vm.getNamedItem(valueName);
        if (valueNode == null)
            throw new LoaderException("No " + valueName + " provided. " +
                "Filter id: \"" + id + "\".");

        String valueStr = valueNode.getNodeValue();
        String[] parts = valueStr.split("[,]");
        double[] values = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            try {
                values[i] = parseStringValue(parts[i].trim(), parameters);
            }
            catch (LoaderException e) {
                throw new LoaderException("Parameter value \"" +  valueStr+
                    "\" of " + valueName + " not found or invalid. " + 
                    "Parameter set: " + parameters.toString() + 
                    " Filter id: \"" + id + "\".");
            }
        }
        return values;
    }

    /**
     * <p>Parses the value represented by a String, whose variable parameters 
     * are mapped in parameters.</p>
     * <p>Only combinations of sums and products are supported.</p>
     * @param s                     The String to be parsed
     * @param parameters            A HashMap mapping variable parameters
     *                              to their value
     * @return                      The parsed value
     * @throws LoaderException      In case of invalid expression, such as
     *                              inexisting variable.
     */
    public static double parseStringValue(String s, 
        HashMap<String, Double> parameters) throws LoaderException {

        double value = 0.0;
        String[] parts_add = s.split("[+]");
        for (String addTerm: parts_add) {
            double toAdd = 1.0;
            String[] parts_mul = addTerm.split("[\\*]");
            
            for (String mulTerm: parts_mul) {
                try {
                    double d = Double.valueOf(mulTerm);
                    toAdd *= d;
                }
                catch (NumberFormatException e) {
                    Double elm = parameters.get(mulTerm.trim());
                    if (elm == null)
                        throw new LoaderException("Could not parse " + 
                        "String " + s + " into value.");
                    toAdd *= elm;
                }
            } // for mulTerm in parts_add
            value += toAdd;
        }
        return value;
    }

    /**
     * Sets {@code ioFileName} member, for {@code DInputFilter}s and 
     * {@code DOutputFilter}s to recall their file name.
     * @param n                 The {@code Node} that represents the io filter
     * @param attributes        A {@code ValueMapper} containing the attributes
     *                          of the {@code Node}
     * @throws LoaderException  If not present
     */
    public void setIOFileName(Node n, ValueMapper attributes) 
        throws LoaderException {

        Node fileNameNode = attributes.getNamedItem(
            Writer.IO_FILENAME_ATTR_NAME);
        
        if (fileNameNode == null) 
            standaloneRunOk = false;
        else 
            ioFileName = fileNameNode.getNodeValue();
        
    }

    /**
     * For the simplicity of getTypeContent method, sets the members 
     * related to a node representing a connection beween a composite 
     * filter ouput and another filter output.
     * @param n                     The node to get data from
     * @param attributes            A {@code ValueMapper} containing the 
     *                              attributes of the {@code Node}
     * @throws LoaderException      If some reference is absent, wrong or 
     *                              ill formatted
     */
    private void setOutputMembers(Node n, ValueMapper attributes) 
        throws LoaderException {
        specialCase = OUTPUT_CONNECTION;
        setIOFileName(n, attributes);
        Node refNode = attributes.getNamedItem(Writer.REF_ATTR_NAME);
        if (refNode == null || standaloneRunOk == false) 
            standaloneRunOk = false;
        String ref = refNode.getNodeValue();
        String[] refparts = ref.split("[.]");

        if (refparts.length != 2)
            throw new LoaderException("Invalid output referencing \"" +
                ref + "\", should be formatted as: \"myId.num\", where " +
                "num is the output index. ");

        
        fOutputId = refparts[0];
        Node cfOutputNumNode = attributes
                               .getNamedItem(Writer.NB_IOPUTS_ATTR_NAME);
        if (cfOutputNumNode == null) 
            standaloneRunOk = false;
    
        try {
            fOutputNum = Integer.valueOf(refparts[1]);
        }
        catch (NumberFormatException e) {
            throw new LoaderException("Filter output index to connect " +
                "could not be parsed. Filter id: \"" + id + "\". " + 
                e.getMessage());
        }

        try {
            cfOutputNum = Integer.valueOf(cfOutputNumNode.getNodeValue());
        }
        catch (NumberFormatException e) {
            throw new LoaderException("Composite filter output index to "  +
                "connect could not be parsed. Filter id: \"" + id + "\". " +
                e.getMessage());
        }
    }

    /**
     * For the simplicity of getTypeContent method, sets the members 
     * related to a node representing a filter located in another xml file.
     * @param n                     The node to get data from
     * @param attributes            A {@code ValueMapper} containing the 
     *                              attributes of the {@code Node}
     * @param parameters            A HashMap with values of the parameters
     *                              introduced in the input file
     * @param verbose               If true, print connections info
     * @throws IOException                  As it can instanciate filter 
     *                                      from other files, all the 
     *                                      exceptions also thrown by load
     * @throws SAXException                 Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     */
    private void setFilterFromSrc(Node n, ValueMapper attributes,
        HashMap<String, Double> parameters, boolean verbose) 
        throws FilterException, DOMException, ParserConfigurationException,
        SAXException, IOException {

        Node srcNode = attributes.getNamedItem(Writer.SRC_FILE_ATTR_NAME);
        if (srcNode == null)
            throw new LoaderException("No source file provided " +
                "for filter. Filter id: \"" + id + "\".");
        
        // extend the current parameters hashmap by specifications in the
        // attibutes.
        HashMap<String, Double> parameters2 = 
            new HashMap<String, Double> (parameters);
        
        for (Entry<String, Node> entry : attributes.entries()) {
            Node item = entry.getValue();
            String name = item.getNodeName();
            double value;

            if (name.startsWith(":")) {
                value = parseStringValue(item.getNodeValue(),
                    parameters);
                    
                parameters2.put(name, value);                  
            }

        } // for over attributes

        String fileName = srcNode.getNodeValue();
        ioFileName = fileName;
        CompositeFilter cf = Loader.load(fileName, parameters2, verbose);
        cf.setFileName(fileName);
        filter = cf;
    }

    /**
     * Reads attributes and sets the inputs data (the id and corresponding
     * numbers).
     * @param attributes        The NamedNodeMap to read inputs from
     * @throws LoaderException  If something goes wrong, mainly, ill-
     *                          formatted data or no data.
     */
    private void getInputsRefs(ValueMapper attributes) throws LoaderException {

        int nbInputs = filter.nbInputs();

        inputsNums = new int[nbInputs];
        inputsIds = new String[nbInputs];
        for (int i = 0; i < nbInputs; i++) {
            String s = "input." + String.valueOf(i);
            Node in_i = attributes.getNamedItem(s);
            if (in_i == null)
                throw new LoaderException("Missing input to " + type + 
                    " filter: input of index " + i + " (" + nbInputs + 
                    " needed) not found. Filter id: \"" + id + "\".");
            
            String[] idSubStr = in_i.getNodeValue().split("[.]");
            if (idSubStr.length != 2) 
                throw new LoaderException("Invalid input referencing \"" + 
                    in_i.getNodeValue() + "\", should be formatted as: " +
                    "\"myId.num\", where num is the input index. Filter" +
                    " id: \"" + id + "\".");

            inputsIds[i] = idSubStr[0];
            try {
                inputsNums[i] = Integer.valueOf(idSubStr[1]);
            }
            catch(NumberFormatException e) {
                throw new LoaderException("Index of filter to connect " + 
                    "could not be parsed. Filter id: \"" + id + "\". " +
                    e.getMessage());
            }
        }
    } // getInputsRefs method

    /**
     * Add all variables definitions in the attributes of a {@code let} node.
     * @param n                 The node
     * @param attributes        A {@code ValueMapper} containing the attributes
     *                          of the {@code Node}
     * @param parameters        A {@code HashMap<String, Double>} with values 
     *                          of the parameters introduced in the input file
     * @throws LoaderException  If some parsing failed.
     */
    private void addVariables(Node n, ValueMapper attributes, 
        HashMap<String, Double> parameters) 
        throws LoaderException {
        specialCase = LET_STATEMENT;
        for (Node cur : attributes.values()) {
            String name = cur.getNodeName();
            if (name.equals(Writer.X_COORD_ATTR_NAME) || 
                name.equals(Writer.Y_COORD_ATTR_NAME) ||
                name.equals(Writer.ORIENTATION_ATTR_NAME)) 
                continue;
            variableName = name;
            variableDefinition = cur.getNodeValue();
            double varValue = parseStringValue(variableDefinition, parameters);
            if (parameters.containsKey(variableName)) 
                parameters.replace(variableName, varValue);
            else 
                parameters.put(variableName, varValue);
        }
    }
}