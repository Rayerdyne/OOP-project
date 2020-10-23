package be.uliege.straet.oop.loader;

import be.uliege.straet.oop.filters.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import be.uliege.montefiore.oop.audio.*;

/** <p>INFO0062 - Object-Oriented Programming
 *  Project.</p>
 * 
 * <p>ADDITIONNAL</p>
 * 
 * <p> This class is designed to parse a composite filter from a file. These 
 * will have to follow xml standard, see exemple.xml for a working example.
 * </p>
 * 
 * <p> I used some more uncommon packages, so that I'm not 100% sure that it 
 * will compile on every configuration, but as these are in the standard 
 * library, only some restricted ones may not be able to run it.</p>
 * 
 * <p> The class provides two static methods: <ul>
 *  <li> a main, that parses a file and apply it to a .wav file. </li>
 *  <li> a `load` method, wich takes the name of the file to parse in argument 
 *     and returns a CompositeFilter.</li></ul></p>
 * 
 * <p> A more detailed description of the structure that xml files have to 
 * follow can be found in ./xml/Structure.md </p>
 * 
 * François Straet
 */
public class Loader {
    /**
     * See README.md for description.
     */
    public static void main(String[] args) {
        String fileName = "test.xml";
        String inName = "Source.wav";
        String outName = "Filtered.wav";

        HashMap<String, Double> parameters = new HashMap<String, Double>();
        parameters.put(":PI", Math.PI);

        boolean verbose = false;

        int k = 0;
        for (int i = 0; i < args.length; i++) {
            // verbose option
            if (args[i].equals("-v")) {
                verbose = true;
                continue;
            }
            // parameter value
            else if (args[i].startsWith(":")) {
                String[] parts = args[i].split("[=]");
                if (parts.length != 2) {
                    System.out.println("Ignoring ill-formatted argument" +
                        " \"" + args[i] + "\".");
                    continue;
                }
                try {
                    double d = Double.valueOf(parts[1]);
                    parameters.put(parts[0], d);
                }
                catch (NumberFormatException e) {
                    System.out.println("Ignoring argument parameter " +
                        parts[0] + " that could not be parsed. " +
                        e.getMessage());
                }
            }
            // argument value
            else {
                switch (k) {
                    case 0:
                        fileName = args[i];
                    case 1:
                        inName = args[i];   break;
                    case 2:
                        outName = args[i];  break;
                    default:                break;
                }
                k++; // switch (k++) would be nice, but harder to undestand x)
            }
        }

        if (verbose) {
            System.out.println("<" + fileName + ">");
            System.out.println("----------");
        }

        try {
            CompositeFilter cf = load(fileName, parameters, verbose);
            try {
                TestAudioFilter.applyFilter(cf, inName, outName);
            } // if we don't get the input in the current directory, then try in ./wav/
            catch (AudioSequenceException e) {
                TestAudioFilter.applyFilter(cf, "wav/" + inName,
                                                "wav/" + outName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * <p>Creates a composite filter based on the file at "fileName", in wich
     * parameters are set to the value found in parameters.</p>
     * 
     * <p>Note: it will have to read files so that it also throws IOExceptions,
     * ParserConfigurationExceptions and SAXExceptions.</p>
     * 
     * @param fileName                  The location of file to read
     * @param parameters                A HashMap with values of the variable 
     *                                  parameters introduced in the input file
     * @param verbose                   If true, print connections info
     * @return                          The newly created CompositeFilter
     * @throws FilterException              If an error occured when 
     *                                      instanciating and connecting filter
     * @throws LoaderException              If an error occured when loading 
     *                                      the file
     * @throws ParserConfigurationException If an error occurs when reading and
     *                                      parsing the input file
     * @throws SAXException                 Idem
     * @throws IOException                  Idem
     * @throws DOMException                 Idem
     */
    public static CompositeFilter load(String fileName, 
        HashMap<String, Double> parameters, boolean verbose) 
        throws FilterException, LoaderException, ParserConfigurationException,
        SAXException, DOMException, IOException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringComments(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc;
        try {
            doc = db.parse(new File(fileName));
        }
        catch (FileNotFoundException e) {
            doc = db.parse(new File("xml/" + fileName));
        }

        Node filterNode = doc.getFirstChild();

        CompositeFilter f;
        if (parameters == null)
            parameters = new HashMap<String, Double>();

        try {
            f = filterFromNode(filterNode, parameters, verbose);
        } catch (LoaderException e) { // re-throw with filename info
            e.printStackTrace();
            throw new LoaderException(e.getMessage() + " File: \"" + fileName + "\".");
        }

        return f;
    }

    /**
     * <p> Builds a composite filter represented at a node and its children 
     * </p>
     * /!\ Almost copied-pasted version in `WorkSpaceXML.loadWSFromNode` method
     * 
     * @param n                 The node to make a filter from
     * @param parameters        A HashMap with values of the variable 
     *                          parameters introduced in the input file
     * @param verbose           If true, print connections info
     * @return                  The built CompositeFilter
     * @throws LoaderException              In case of error in instanciating or
     *                                      connecting sub-filters
     * @throws FilterException              If something went wrong whe
     *                                      instanciating CompositeFilter
     * @throws NumberFormatException        If an error occured when parsing a
     *                                      String to a value
     * @throws IOException                  As it can instanciate filter 
     *                                      from other files, all the 
     *                                      exceptions also thrown by load
     * @throws SAXException                 Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     */
    protected static CompositeFilter filterFromNode(Node n, 
        HashMap<String, Double> parameters, boolean verbose) 
        throws LoaderException, FilterException, NumberFormatException, 
        DOMException, ParserConfigurationException, SAXException, IOException {

        NamedNodeMap attributes = n.getAttributes();

        // get its id
        Node idNode = attributes.getNamedItem(Writer.ID_ATTR_NAME);
        if (idNode == null)
            throw new LoaderException("No id provided for a composite filter" + 
                " node.");
        String cfId = idNode.getNodeValue();

        // Get nbInputs and nbOutputs
        int nbInputs = 0, nbOutputs = 0;
        Node nbInputsNode = attributes
                            .getNamedItem(Writer.NB_INPUTS_ATTR_NAME);
        Node nbOutputsNode = attributes
                            .getNamedItem(Writer.NB_OUTPUTS_ATTR_NAME);
        if (nbInputsNode == null)
            throw new LoaderException("Composite filter has no input number" +
                " specified.");
        if (nbOutputsNode == null)
            throw new LoaderException("Composite filter has no output number" +
                " specified.");

        try {
            nbInputs = Integer.valueOf(nbInputsNode.getNodeValue());
            nbOutputs = Integer.valueOf(nbOutputsNode.getNodeValue());
        }
        catch (NumberFormatException e) {
            throw new LoaderException("The number of inputs (" + nbInputs + 
                ") or outputs (" + nbOutputs + ") could not be parsed. " + 
                "Filter id: \"" + cfId + "\". " + e.getMessage());
        }

        CompositeFilter cf = new CompositeFilter(nbInputs, nbOutputs);

        // get all childrens
        NodeList nl = n.getChildNodes();
        HashMap<String, NodeData> subFilters = new HashMap<String, NodeData>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            if (!child.getNodeName().equals("#text") &&
                !child.getNodeName().equals(Writer.VALUE_NODE_TAG)) {
                NodeData data = new NodeData(child, parameters, verbose);
                subFilters.put(data.id, data);
            }
        }

        connectSubFilters(cf, cfId, subFilters, verbose);
        return cf;
    }

    /**
     * <p> Connects all subfilters to its corresponding composite filter. </p>
     * 
     * @param cf                        The composite filter to "fill"
     * @param cfId                      The id of the composite filter
     * @param subFilters                Data to filters to connect
     * @param verbose                   If true, print connections info
     * @throws FilterException          If somthing goes wrong when connecting
     *                                  the sub-filters
     */
    private static void connectSubFilters(CompositeFilter cf, String cfId,
        HashMap<String, NodeData> subFilters, boolean verbose) 
        throws FilterException {

        // add all the actual filters
        for (String key : subFilters.keySet()) {
            NodeData element = subFilters.get(key);
            if (element.specialCase == NodeData.NONE)
                cf.addBlock(element.filter);
        }

        if (verbose)
            System.out.println("-- " + cfId + " connections --");

        for (String key : subFilters.keySet()) {
            NodeData sfData = subFilters.get(key);

            // output connection to this composite filter
            if (sfData.specialCase == NodeData.OUTPUT_CONNECTION) {
                NodeData inFilterData = subFilters.get(sfData.fOutputId);
                if (inFilterData == null)
                    throw new LoaderException("Id \"" + sfData.fOutputId +
                        "\" not found.");

                if (verbose)
                    System.out.println("> connect output " + inFilterData.id + 
                        "." + sfData.fOutputNum + " to output this(" + cfId + 
                        ")." + sfData.cfOutputNum);
                try {
                    cf.connectBlockToOutput(inFilterData.filter, sfData.fOutputNum,
                        sfData.cfOutputNum);
                }
                catch (FilterException e) {
                    throw new LoaderException("Output connection error in "+ 
                        "composite filter of id \"" + cfId + "\", output n°" +
                        sfData.cfOutputNum + ", filter to connect id: \"" + 
                        inFilterData.id + "\": " + e.getMessage());
                }
                continue;
            }
            else if (sfData.specialCase != NodeData.NONE)
                continue;

            // all input connection of the sub-filter
            connectOneFilter(sfData, cf, cfId, subFilters, verbose);  
        } // for (String key: subFilters.keySet())
        
    }

    /**
     * <p> Connect one input of node described in sfData </p>
     * @param sfData        The data related to the filter we are connecting
     * @param cf            The composite filter
     * @param cfId          The id of the associated domposite filter
     * @param subFilters    The HashMap containing all the info about the 
     *                      filters
     * @param verbose       Wether or not we have to print information.
     * @throws LoaderException  If some connection could not be made
     */
    private static void connectOneFilter(NodeData sfData, CompositeFilter cf, 
        String cfId, HashMap<String, NodeData> subFilters,
        boolean verbose) throws LoaderException {

        for (int i = 0; i < sfData.inputsIds.length; i++) {
            // intput connection to this composite filter
            if (sfData.inputsIds[i].equals(cfId)) {
                if (verbose) 
                    System.out.println("> connect input this(" + cfId + 
                        ")." + sfData.inputsNums[i] + " to input " + sfData.id
                        + "." + i + ".");
                try {
                    cf.connectInputToBlock(sfData.inputsNums[i], 
                        sfData.filter, i);
                }
                catch (FilterException e) {
                    throw new LoaderException("Input connection error in "+ 
                        "composite filter of id \"" + cfId + "\", filter" +
                        " to connect id: \"" + sfData.id + "\": " + 
                        e.getMessage());
                }
                
            }
            // connection to another bloc in same composite filter
            else {
                NodeData inFilterData = subFilters.get(sfData.inputsIds[i]);
                if (inFilterData == null)
                    throw new LoaderException("Id \"" + sfData.inputsIds[i]
                        + "\" not found.");

                if (verbose)
                    System.out.println("> connect output " + 
                        inFilterData.id + "." + sfData.inputsNums[i] + 
                        " to input " + sfData.id + "." + i);
                try {
                    cf.connectBlockToBlock(inFilterData.filter, 
                        sfData.inputsNums[i], sfData.filter, i);
                }
                catch (FilterException e) {
                    throw new LoaderException("Connection error in " + 
                        "composite filter of id \"" + cfId +
                        "\". Sub-filter input id: \"" + sfData.id +
                        "\". Sub-filter output id: \"" + inFilterData.id +
                        "\": " + e.getMessage());
                }
            } // else
        } // for
    } // method
}
