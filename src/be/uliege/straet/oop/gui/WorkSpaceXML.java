package be.uliege.straet.oop.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import be.uliege.montefiore.oop.audio.FilterException;
import be.uliege.straet.oop.filters.CompositeFilter;
import be.uliege.straet.oop.filters.DelayFilter;
import be.uliege.straet.oop.filters.GainFilter;
import be.uliege.straet.oop.loader.LoaderException;
import be.uliege.straet.oop.loader.NodeData;
import be.uliege.straet.oop.loader.Writer;
import be.uliege.straet.oop.loader.WriterException;

/**
 * This class aims to let the file WorkSpace.java smaller.
 * It will handle all the stuff needed to read-write in xml files.
 * (... it uses instance variables for saving and not for openning, it's bad)
 */
public class WorkSpaceXML {

    private static final int MAX_PARAMETERS_LOOPS = 100;

    /** Members required to open */
    WorkSpace ws;
    int nbInputs, nbOutputs;
    HashMap<String, NodeData> subFilters = new HashMap<String, NodeData>();
    HashMap<String, Double> parameters = new HashMap<String, Double>();

    /** Members required to save */
    Document doc;
    CompositeFilter cfR;

    Vector<DVariableDeclaration> variables;
    Vector<DraggableFilter> filters;
    Vector<DInputFilter> inputFilters;
    Vector<DOutputFilter> outputFilters;

    HashMap<String, Element> children = new HashMap<String, Element>();

    /**
     * Constructor for saving.
     * @param cfR           The `CompositeFilter` represented in the 
     *                      `WorkSpace`
     * @param variables     The array of the variables declarations 
     * @param filters       The array of the 'active' filters of the 
     *                      `WorkSpace`
     * @param inputFilters  The array of the input filters
     * @param outputFilters The array of the output filters
     */
    public WorkSpaceXML(CompositeFilter cfR, 
        Vector<DVariableDeclaration> variables, 
        Vector<DraggableFilter> filters,
        Vector<DInputFilter> inputFilters,
        Vector<DOutputFilter> outputFilters) {
        
        this.cfR = cfR;
        this.variables = variables;
        this.filters = filters;
        this.inputFilters = inputFilters;
        this.outputFilters = outputFilters;
    }

    /**
     * Constructor for opening files
     * @param ws    The `WorkSpace` we load the contents in.
     */
    public WorkSpaceXML(WorkSpace ws) {
        this.ws = ws;
    }

    /**
     * Adds the data contained in the `WorkSpace` current state, i.e. all the 
     * positions of the filters, variables etc.
     * @return  A `Document` holding the `CompositeFilter` and the data needed
     *          to reconstruct the `WorkSpace`
     * @throws WriterException  If some error due to a `Node` occured, e.g. 
     *                          some could not be found
     */
    public Document buildDocument() throws WriterException {
		try {
            doc = Writer.documentFromFilter(cfR);
            
		} catch (WriterException e) {
            WorkSpace.showError("An error occured when building `Document`",e);
            return null;
		}
        Element root = doc.getDocumentElement();
        NodeList childrenNL = root.getChildNodes();
        for (int i = 0; i < childrenNL.getLength(); i++) {
            Node n = childrenNL.item(i);
            if (n.getNodeName() == "#text")     continue;

            String typeOfFilter = n.getNodeName();
            if (typeOfFilter.equals("output")) {
                String id = Writer.OUTPUT_ID_PREFIX + 
                    n.getAttributes().getNamedItem("n").getNodeValue();
                children.put(id, (Element) n);
            }
            else if (!typeOfFilter.equals(Writer.VALUE_NODE_TAG)) {
                String id = n.getAttributes().getNamedItem("id").getNodeValue();
                children.put(id, (Element) n);
            }
        }

        addWSVariables(root);
        addWSFilters(root);
        addWSIOFilters(root);
        return doc;
    }

    /**
     * Adds the data relative to the variables definitions in the `WorkSpace`
     * @param root      The `Element` at the root of the `Document` (doc)
     */
    private void addWSVariables(Element root) {
        for (DVariableDeclaration vd : variables) {
            Element e = doc.createElement(Writer.VARIABLE_NODE_TAG);
            e.setAttribute("orientation", 
                Integer.toString(vd.getOrientation()));
            e.setAttribute("x", Integer.toString(vd.getX()));
            e.setAttribute("y", Integer.toString(vd.getY()));
            e.setAttribute(vd.getVariableName(), vd.getParameterDefinition()); 
            root.appendChild(e);
        }
        // TODO : check if let statement works *after* the filters
    }

    /**
     * Adds the data relative to the filters in the `WorkSpace`
     * @param root      The `Element` at the root of the `Document` (doc)
     */
    private void addWSFilters(Element root) throws WriterException {
        for (DraggableFilter df : filters) {
            if (!WorkSpace.isActiveFilter(df))  continue;
            String id = cfR.idOf(df.filterR);
            Element e = children.get(id);
            if (e == null) throw new WriterException("Could not find node " + 
                "with id \"" + id + "\".");
            
            Element pos = doc.createElement(Writer.VALUE_NODE_TAG);
            pos.setAttribute("x", Integer.toString(df.getX()));
            pos.setAttribute("y", Integer.toString(df.getY()));
            pos.setAttribute("orientation", 
                Integer.toString(df.getOrientation()));

            e.appendChild(pos);

            Vector<Wire> wires = df.wireAtInputs();
            for (Wire wire : wires) 
                e.appendChild(elementFromWire(wire));
        }
    }

    /**
     * Adds the data relative to the in-out filters in the `WorkSpace`
     * @param root      The `Element` at the root of the `Document` (doc)
     */
    private void addWSIOFilters(Element root) {
        for (int i = 0; i < inputFilters.size(); i++){
            DInputFilter dif = inputFilters.elementAt(i);
            Element newE = doc.createElement(Writer.INPUT_POS_NODE_TAG);
            newE.setAttribute("x", Integer.toString(dif.getX()));
            newE.setAttribute("y", Integer.toString(dif.getY()));
            newE.setAttribute("n",Integer.toString(inputFilters.indexOf(dif)));
            newE.setAttribute("orientation", 
                Integer.toString(dif.getOrientation()));
            newE.setAttribute(Writer.IO_FILENAME_ATTRIBUTE_NAME, 
                              dif.getParameterDefinition());
            root.appendChild(newE);
        }

        for (int i = 0; i < outputFilters.size(); i++) {
            DOutputFilter dof = outputFilters.elementAt(i);
            String id = Writer.OUTPUT_ID_PREFIX + Integer.toString(i);
            Element e = children.get(id);
            Element pos = doc.createElement(Writer.VALUE_NODE_TAG);
            pos.setAttribute("x", Integer.toString(dof.getX()));
            pos.setAttribute("y", Integer.toString(dof.getY()));
            pos.setAttribute("orientation", 
                Integer.toString(dof.getOrientation()));
            e.appendChild(pos);

            Element wire = elementFromWire(dof.wireAtInputs().firstElement());
            e.appendChild(wire);
            e.setAttribute(Writer.IO_FILENAME_ATTRIBUTE_NAME, 
                           dof.getParameterDefinition());
        }
    }

    /**
     * Creates an `Element` containing the data relative to a `Wire`.
     * @param wire  The `Wire`
     * @return      The built `Element` 
     */
    private Element elementFromWire(Wire wire) {
        Element e = doc.createElement(Writer.WIRE_NODE_TAG);
        e.setAttribute(Writer.WIRE_INPUT_ATTR_NAME, 
            Integer.toString(wire.input().index()));
        Vector<FreeBall> fbs = wire.freeBalls();
        for (int i = 0; i < fbs.size(); i++) {
            FreeBall fb = fbs.elementAt(i);
            Element fbElm = doc.createElement(Writer.FREE_BALL_NODE_TAG);
            fbElm.setAttribute("x", Integer.toString(fb.getX()));
            fbElm.setAttribute("y", Integer.toString(fb.getY()));

            e.appendChild(fbElm);
        }
        return e;
    }

    /**
     * <p>Parses the value represented by a String, whose variable parameters 
     * are mapped in parameters.</p>
     * <p>Only combinations of sums and products are supported.</p>
     * <p>Have a copied-pasted version in `NodeData`.</p>
     * @param s                     The String to be parsed
     * @param parameters            A HashMap mapping variable parameters
     *                              to their value
     * @return                      The parsed value
     * @throws NumberFormatException In case of invalid expression, such as
     *                              inexisting variable.
     * /
    public static double parseStringValue(String s, 
        HashMap<String, Double> parameters) throws NumberFormatException {

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
                        throw new NumberFormatException("Could not parse " + 
                        "String \"" + s + "\" into value.");
                    toAdd *= elm;
                }
            } // for mulTerm in parts_add
            value += toAdd;
        }
        return value;
    }*/


    /**
     * Opens a file in the `WorkSpace` ws (member of the object). 
     * @param fileName      The name of the file to open.
     * @throws NumberFormatException        If some number could not be parsed
     *                                      during the processes
     * @throws IOException                  As it can instanciate filter 
     *                                      from other files, all the 
     *                                      exceptions also thrown by load
     * @throws SAXException                 Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     */
    public void openFile(String fileName) throws ParserConfigurationException,
        SAXException, IOException, NumberFormatException, DOMException, FilterException {
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

        Node node = doc.getFirstChild();
        getIDandIONumbers(node);
        addSubNodesData(node);
        ws.repaint();
    }

	/**
     * Gets the id of the whole filter and the numbers of inputs and outputs.
     * @param node  The node that contains the filter's data
     * @throws LoaderException If something could not be parsed.
     */
	private void getIDandIONumbers(Node n) throws LoaderException {
        
        NamedNodeMap attributes = n.getAttributes();

        // get its id
        Node idNode = attributes.getNamedItem("id");
        if (idNode == null)
            throw new LoaderException("No id provided for a composite filter" + 
                " node.");
        String cfId = idNode.getNodeValue();

        // Get nbInputs and nbOutputs
        nbInputs = 0; nbOutputs = 0;
        Node nbInputsNode = attributes.getNamedItem("in");
        Node nbOutputsNode = attributes.getNamedItem("out");
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
    }
    
    /**
     * Adds the data related to the filter contained in the subnodes.
     * @param node  The `Node` whose children are the filters, for short.d
     * @throws NumberFormatException        If some number could not be parsed
     *                                      during the processes
     * @throws IOException                  As it can instanciate filter 
     *                                      from other files, all the 
     *                                      exceptions also thrown by load
     * @throws SAXException                 Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     */
    private void addSubNodesData(Node node) throws NumberFormatException,
        DOMException, FilterException, ParserConfigurationException, 
        SAXException, IOException {
        // get all childrens
        NodeList nl = node.getChildNodes();

        // setting the parameters, we will need them first
        getParameters(nl);

        //HashMap<String, NodeData> subFilters = new HashMap<String, NodeData>
        // already in class members
        
        /** Mechanism to avoid re-looping through the whole NodeList: all the
          * 'abc' filters are stored in the vector at 'abc' in the 'sorted'
          * `HashMap` */
        HashMap<String, Vector<NodeData>> sorted = 
            new HashMap<String, Vector<NodeData>>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            String name = child.getNodeName();
            
            // ignore children that are text between tags
            if (name.equals("#text")) continue;
            NodeData data = new NodeData(child, parameters, false);
            subFilters.put(data.id, data);
            
            if (!sorted.containsKey(name)) 
                sorted.put(name, new Vector<NodeData>());
            sorted.get(name).add(data);
        }

        // check if input-output filter get at their place in sorted HashMap
        if (!sorted.containsKey(Writer.INPUT_POS_NODE_TAG) ||
            !sorted.containsKey(Writer.OUTPUT_NODE_TAG))
            throw new LoaderException("File does not specifies input or " +
                "output filters.");

        addIOFilters(sorted.get(Writer.INPUT_POS_NODE_TAG),
                     sorted.get(Writer.OUTPUT_NODE_TAG));
        addFilters(sorted);
        addWires(sorted);
    }
    
    /**
     * Gets the parameters contained in the `NodeList` and adds them to the
     * `WorkSpace`.
     * @param nl    The `NodeList` to get the parameters from
     * @throws LoaderException  In case of an ill-formated parameter definition
     */
    private void getParameters(NodeList nl) 
        throws LoaderException {
        // setting the parameters
        parameters = new HashMap<String, Double>();

        // getting all the nodes representing a variable to add.
        HashSet<Node> varsToAdd = new HashSet<Node>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals(Writer.VARIABLE_NODE_TAG))
                varsToAdd.add(n);
        }

        /* Because some parameters may rely on other parameters by their 
         * definition, we need to re-loop as many times as needed, and we also
         * prevent looping infinitely. */
        int guard = 0;
        while (!varsToAdd.isEmpty() && guard < MAX_PARAMETERS_LOOPS) {
            Iterator<Node> it = varsToAdd.iterator();
            outer_loop:
            while (it.hasNext()) {
                Node n = it.next();
                NamedNodeMap nnm = n.getAttributes();
                for (int j = 0; j < nnm.getLength(); j++) {
                    Node item = nnm.item(j);
                    String name = item.getNodeName();
                    if (!name.equals("x") && ! name.equals("y") && 
                        !name.equals("orientation")) {
                        
                        try {
                            double value = NodeData.parseStringValue(
                                item.getNodeValue(), parameters);
                            parameters.put(name, value);
                            // varsToAdd.remove(n);
                            // this may raise a ConcurrentModificationException
                            // so that we do:
                            it.remove();
                            // System.out.println(name + " = " + value);
                        } catch (LoaderException e) {
                            continue outer_loop;
                        }
                        try {
                            int x = Integer.valueOf(
                                nnm.getNamedItem("x").getNodeValue());
                            int y = Integer.valueOf(
                                nnm.getNamedItem("y").getNodeValue());
                            int orientation = Integer.valueOf(
                                nnm.getNamedItem("orientation").getNodeValue());
                            ws.addVariableDeclaration(x, y, orientation, false, 
                                name, item.getNodeValue() );
                        } catch (NullPointerException e) {
                            throw new LoaderException("Position unavailible " + 
                                "for some variable declaration."); 
                        }
                    }
                }
            }
            guard++;
        }

        if (guard == MAX_PARAMETERS_LOOPS) 
            throw new LoaderException("Could not parse all the parameters. " + 
                "Built HashMap: \"" + parameters.toString() + "\"");
        return;
    }

    /**
     * Adds the `DInputFilter`s and `DOutputFilter`s to the `WorkSpace`.
     * @param inputs    A `Vector` of `Node`s representing the `DInputFilters`
     * @param outputs   A `Vector` of `Node`s representing the `DOutputFilters`
     * @param subFilters A `HashMap<String, NodeData>` mapping all the ids to
     *                   their related `NodeData` object.
     * @throws NumberFormatException        If some number could not be parsed
     *                                      during the processes
     * @throws IOException                  As it can instanciate filter 
     *                                      from other files, all the 
     *                                      exceptions also thrown by load
     * @throws SAXException                 Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     */
    private void addIOFilters(Vector<NodeData> inputs, 
        Vector<NodeData> outputs) {
        for (NodeData data : inputs)  
            data.draggableFilter = ws.addInput(data.x, data.y, 
                data.orientation, false, data.ioFileName);
        
        for (NodeData data : outputs) 
            data.draggableFilter = ws.addOutput(data.x, data.y, 
                data.orientation, false, data.ioFileName);
        
    }
    
    /**
     * Adds to the `WorkSpace` all the other filters (i.e., neither input, 
     * output or parameter.)
     * @param sorted    The `HashMap` containing the vectors of nodes.
     * @throws LoaderException  If some type could not be recognized.
     */
    private void addFilters(HashMap<String, Vector<NodeData>> sorted) 
        throws LoaderException {

        for (Map.Entry<String, Vector<NodeData>> entry: sorted.entrySet()) {
            String name = entry.getKey();

            switch(name) {
case "gain":    
    for (NodeData d : entry.getValue()) 
        d.draggableFilter = ws.addGain(d.x, d.y, d.orientation, false, 
                                       (GainFilter) d.filter);
    break;
case "delay":   
    for (NodeData d : entry.getValue())  
        d.draggableFilter = ws.addDelay(d.x, d.y, d.orientation, false, 
                                        (DelayFilter) d.filter);
    break;
case "addition":
    for (NodeData d : entry.getValue()) 
        d.draggableFilter = ws.addAddition(d.x, d.y, d.orientation, false);
    break;
/*
TODO case "integrator":for (NodeData n : entry.getValue())  addGain(n);
    break;
case "differentiator":for (NodeData n : entry.getValue())  addGain(n);
    break;
case "convolution":for (NodeData n : entry.getValue())  addGain(n);
    break;
case "sine_generator":for (NodeData n : entry.getValue())  addGain(n);
    break; 
case "square_up_generator": for (NodeData n : entry.getValue())  addGain(n);
    break; 
case "square_centered_generator": for (NodeData n : entry.getValue())  addGain(n);
    break; 
case "noise_generator": for (NodeData n : entry.getValue())  addGain(n);
    break;
case Writer.OUTPUT_NODE_TAG:for (NodeData n : entry.getValue())  addGain(n);
    break;
case Writer.VARIABLE_NODE_TAG:for (NodeData n : entry.getValue())  addGain(n);
    break;
case "composite":for (NodeData n : entry.getValue())  addGain(n);
    break;
case "filter":for (NodeData n : entry.getValue())  addGain(n);
    break;*/
case Writer.INPUT_POS_NODE_TAG:
case Writer.OUTPUT_NODE_TAG:
case Writer.VARIABLE_NODE_TAG:
    break;
default:
    throw new LoaderException("Type \"" + name +
        "\" not found.");
            }
            
        }
    }
    
    /**
     * Adds the `Wire`s represented in one node to connect it.
     * @param sorted    The `HashMap` containing the vectors of nodes.
     * @throws FilterException  If some connection could not be made.
     * @throws NumberFormatException If some index of the filter could not be
     *                               parsed.
     */
    private void addWires(HashMap<String, Vector<NodeData>> sorted) 
        throws NumberFormatException, FilterException {
        for (Map.Entry<String, Vector<NodeData>> entry: sorted.entrySet()) {
            String name = entry.getKey();
            if (name.equals(Writer.INPUT_POS_NODE_TAG))
                continue;
            
            for (NodeData data : entry.getValue()) {
                Node n = data.node;
                NodeList nl = n.getChildNodes();
                for (int i = 0; i < nl.getLength(); i++) {
                    Node item = nl.item(i);
                    if (item.getNodeName().equals(Writer.WIRE_NODE_TAG))
                        addWire(item, data);
                }
            }
        }
	}

    /**
     * Adds a single `Wire` to the `WorkSpace`.
     * @param node  The `Node` that represents the `Wire`
     * @param data  `NodeData` related to the filter that inputs the `Wire`
     *              output.
     * @throws NumberFormatException If the index of the filter could not be
     *                               parsed.
     * @throws FilterException      If some connection could not be made
     */
    private void addWire(Node node, NodeData data) 
        throws NumberFormatException, FilterException {
        // get the index of the filter's input that connects the wire
        NamedNodeMap attr = node.getAttributes();
        Node inputNumNode = attr.getNamedItem(Writer.WIRE_INPUT_ATTR_NAME);
        if (inputNumNode == null)
            throw new LoaderException("Index of filter input not found in " + 
                "`Wire`.");
        
        int inputIndex = Integer.parseInt(inputNumNode.getNodeValue()); 
        // output do not have inputsId, so: 
        if (data.specialCase != NodeData.OUTPUT_CONNECTION && 
            inputIndex >= data.inputsIds.length)
            throw new LoaderException("Wire connects inexistant input !" +
                " ( input n°" + (inputIndex+1) + " but " + 
                data.inputsIds.length + "needed).");
        
        
        // get the references of the filter that holds the output that inputs
        // in the wire
        String sourceId;
        int sourceOutputIndex;
        if (data.specialCase == NodeData.OUTPUT_CONNECTION) {
            sourceId = data.fOutputId;
            sourceOutputIndex = data.fOutputNum;
        } else {
            sourceId = data.inputsIds[inputIndex];
            sourceOutputIndex = data.inputsNums[inputIndex];
        }
        // handling connection to inputs of whole filter...
        if (sourceId.equals(Writer.DEF_ROOT_ID)) {
            sourceId = Writer.INPUT_POS_NODE_TAG + "_" + 
                Integer.toString(sourceOutputIndex);
            sourceOutputIndex = 0;
        }

        NodeData sourceData = subFilters.get(sourceId);
        if (sourceData == null)  
            throw new LoaderException("Could not find `NodeData` to connect " + 
                "input. Id \"" + data.id + "\" attempted to connect to \"" +
                sourceId + "\" output but did not found its data.");
        
        FixedBall origin = sourceData.draggableFilter
                                     .outputs[sourceOutputIndex];
        FixedBall dest = data.draggableFilter.inputs[inputIndex];
        Map.Entry<int[], int[]> xy = getXY(node);
        ws.addConnection(origin, dest, xy.getKey(), xy.getValue());
	}

	private Entry<int[], int[]> getXY(Node node) {
        NodeList nl = node.getChildNodes();
        // We eploit the fact that the 'interesting' nodes and #text nodes 
        // alternate...
        int nbBalls = (nl.getLength() - 1) / 2;
        int[] x = new int[nbBalls];
        int[] y = new int[nbBalls];
        for (int i = 0; i < nbBalls; i++) {
            Node fb = nl.item(2*i + 1);
            NamedNodeMap nnm = fb.getAttributes();
            x[i] = Integer.parseInt(nnm.getNamedItem("x").getNodeValue());
            y[i] = Integer.parseInt(nnm.getNamedItem("y").getNodeValue());
        }
		return new AbstractMap.SimpleEntry<int[], int[]>(x, y);
	}

}