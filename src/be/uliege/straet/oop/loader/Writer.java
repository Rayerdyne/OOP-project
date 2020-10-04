/**
 * OOP: project ?
 * 
 * This class is designed to write an instance of CompositeFilter into a XML
 * file parsable by the Loader class.
 */

package be.uliege.straet.oop.loader;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import be.uliege.straet.oop.filters.CompositeFilter;
import be.uliege.straet.oop.filters.ReadDouble;
import be.uliege.montefiore.oop.audio.FilterException;
import be.uliege.straet.oop.filters.Block;
import be.uliege.straet.oop.filters.BlockException;

public class Writer {

    public static final String DEF_ROOT_ID = "whole_filter";
    public static final String VARIABLE_NODE_TAG = "let";
    public static final String VALUE_NODE_TAG = "v";
    public static final String WIRE_NODE_TAG = "wire";
    public static final String WIRE_INPUT_ATTR_NAME = "input";
    public static final String FREE_BALL_NODE_TAG = "fb";
    public static final String INPUT_POS_NODE_TAG = "input";
    public static final String OUTPUT_NODE_TAG = "output";
    public static final String OUTPUT_ID_PREFIX = "output_block_";
    public static final String REF_ATTRIBUTE_NAME = "ref";
    public static final String IO_FILENAME_ATTRIBUTE_NAME = "content";
    private static int INPUTS_PER_NODE = 2;
    private static int PARAMETERS_PER_NODE = 2;
    // public static void main(String[] args) {

    // }

    /**
     * Writes a `CompositeFilter` to a xml file
     * @param cf                The `CompositeFilter to write
     * @param fileName          The name of the file to create
     * @throws WriterException  If something went wrong
     */
    public static void writeFilter(CompositeFilter cf, String fileName) 
        throws WriterException {
            
        writeDocument(documentFromFilter(cf), fileName);
    }

    public static Document documentFromFilter(CompositeFilter cf) 
        throws WriterException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new WriterException("Could not create new document.");
        }
        Document document = db.newDocument();

        try {
            addFilter(cf, document);
        }
        catch(BlockException be) {
            throw new WriterException("Could not add filters to document");
        }

        return document;
    }

    /**
     * Write a `Document` to a xml file  
     * @param docmument         The `Document`
     * @param fileName          The name of the file to write
     * @throws WriterException  If someting went wrong
     */
    public static void writeDocument(Document document, String fileName)
        throws WriterException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t;
        try {
            t = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new WriterException("Could not instanciate transfomer.");
        }
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(fileName));
        try {
            t.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new WriterException("Coud not transform document into " + 
                "stream");
        }
    }

    /**
     * Adds a filter to the Document
     * @param cf                The CompositeFilter to add
     * @param d                 The Document
     * @throws BlockException   If we could not determine some filter's type
     * @throws WriterException  If some-sub-CompositeFilter is ill-formatted
     */
    private static void addFilter(CompositeFilter cf, Document d) 
        throws BlockException, WriterException {
        Element root = d.createElement("filter");
        root.setAttribute("id", DEF_ROOT_ID);
        d.appendChild(root);

        appendFilters(cf, DEF_ROOT_ID, root, d);
    }

    /**
     * Append filters present in CompositeFilter cf to an element
     * @param cf                The CompositeFilter
     * @param cfId              The id associated to the CompositeFilter
     * @param e                 The Element
     * @param d                 The associated Document
     * @throws BlockException   If we could not determine some filter's type
     * @throws WriterException  If some sub-CompositeFilter is ill-formatted
     */
    private static void appendFilters(CompositeFilter cf, String cfId,
        Element e, Document d) throws BlockException, WriterException {
        
        e.setAttribute("in", String.valueOf(cf.nbInputs()));
        e.setAttribute("out", String.valueOf(cf.nbOutputs()));

        // Outputs:
        for (int i = 0; i < cf.nbOutputs(); i++) {
            ReadDouble output;
            try {
                output = cf.output(i);
            } catch (FilterException fe) {
                throw new BlockException(fe.getMessage());
            }
            Element o = d.createElement(OUTPUT_NODE_TAG);
            o.setAttribute("n", String.valueOf(output.outputIndex()));
            String outputId = output.source() == null ? 
                                                   cfId : output.source().id();
            o.setAttribute(REF_ATTRIBUTE_NAME, outputId + "." + output.outputIndex());
            e.appendChild(o);
        }

        // Blocks:
        for (Block b : cf.blocks()) {
            e.appendChild(elementFromBlock(b, cfId, d));
        }
    }

    private static Element elementFromBlock(Block b, String cfId,
        Document d) throws BlockException, WriterException {
        Element x = d.createElement(b.type());
            // id
            x.setAttribute("id", b.id());

            // inputs references, 2 by 2 in children
            Element y = d.createElement(VALUE_NODE_TAG);
            for (int i = 0; i < b.nbInputs(); i++) {
                ReadDouble input = b.input(i);
                String inputId = input.source() == null ? 
                                                    cfId : input.source().id();
                y.setAttribute("input." + i, inputId + "." + input.outputIndex());
                if (i != 0 && i % INPUTS_PER_NODE == 0) {
                    x.appendChild(y);
                    y = d.createElement(VALUE_NODE_TAG);
                }
            }
            if (b.nbInputs() % INPUTS_PER_NODE == 0 || 
                b.nbInputs() < INPUTS_PER_NODE ) 
                x.appendChild(y);
            
            // parameters (e.g. gain="0.7")
            y = d.createElement(VALUE_NODE_TAG);
            int i = 0;
            HashMap<String, String> parameters = b.filter().getParameters();
            for (HashMap.Entry<String, String> entry : parameters.entrySet()) {
                y.setAttribute(entry.getKey(), entry.getValue());
                if (i != 0 && i % PARAMETERS_PER_NODE == 0) {
                    x.appendChild(y);
                    y = d.createElement(VALUE_NODE_TAG);
                }
                i++;
            }
            if (i == 1)
                x.appendChild(y);

            // if CompositeFilter, then write it
            if (b.type() == "composite") {
                if (!(b.filter() instanceof CompositeFilter))
                    throw new WriterException("composite Block contains " + 
                        "non-CompositeFilter filter ?!?!?");
                appendFilters((CompositeFilter) b.filter(), b.id(), x, d);
            }
        return x;
    }

    
}