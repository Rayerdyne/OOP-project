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

/**
 * <p>Class reponsible of the writing of a {@code CompositeFilter} to a file.
 * </p> <p> It will hold all the constant {@code String}s for the nodes tags, 
 * attributes names etc. </p>
 */
public class Writer {

    public static final String 
        VARIABLE_NODE_TAG = "let",
        VALUE_NODE_TAG = "v",
        WIRE_NODE_TAG = "wire",
        FREE_BALL_NODE_TAG = "fb",
        INPUT_POS_NODE_TAG = "input",
        OUTPUT_NODE_TAG = "output",
        ADDITION_F_NODE_TAG = "addition",
        GAIN_F_NODE_TAG = "gain",
        DELAY_F_NODE_TAG = "delay",
        INTEGRATOR_F_NODE_TAG = "integrator",
        DIFFERENTIATOR_F_NODE_TAG = "differentiator",
        CONVOLUTION_F_NODE_TAG = "convolution",
        COMPOSITE_F_NODE_TAG = "filter",
        NESTED_COMPOSITE_NODE_TAG = "composite",
        SINE_GEN_NODE_TAG = "sine_gen",
        CENTERED_SQUARE_GEN_NODE_TAG = "centered_square_gen",
        UP_SQUARE_GEN_NODE_TAG = "up_square_gen",
        NOISE_GEN_NODE_TAG = "noise_gen",
        
        DEF_ROOT_ID = "whole_filter",
        OUTPUT_ID_PREFIX = "output_block_",

        ID_ATTR_NAME = "id",
        NB_INPUTS_ATTR_NAME = "in",
        NB_OUTPUTS_ATTR_NAME = "out",
        X_COORD_ATTR_NAME = "x",
        Y_COORD_ATTR_NAME = "y",
        ORIENTATION_ATTR_NAME = "orientation",
        REF_ATTR_NAME = "ref",
        NB_IOPUTS_ATTR_NAME = "n",
        SRC_FILE_ATTR_NAME = "src",
        WIRE_INPUT_ATTR_NAME = "input",
        IO_FILENAME_ATTR_NAME = "content",
        FREQUENCY_ATTR_NAME = "frequency",
        AMPLITUDE_ATTR_NAME = "amplitude",
        FS_ATTR_NAME = "fs", 
        CONVOLUTION_VECTOR_ATTR_NAME = "vector";

    public static final int INPUTS_PER_NODE = 2;
    public static final int ATTRIBUTES_PER_NODE = 2;
    // public static void main(String[] args) {

    // }

    /**
     * Writes a {@code CompositeFilter} to a xml file
     * @param cf                The `CompositeFilter to write
     * @param fileName          The name of the file to create
     * @throws WriterException  If something went wrong
     */
    public static void writeFilter(CompositeFilter cf, String fileName) 
        throws WriterException {
            
        writeDocument(documentFromFilter(cf), fileName);
    }

    /**
     * Constructs a {@code Document} from a {@code CompositeFilter}.
     * @param cf        The {@code CompositeFilter}
     * @return          The {@code Document} that has been built
     */
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
     * Write a {@code Document} to a xml file. 
     * @param docmument         The {@code Document}
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
     * Adds a filter to the {@code Document}.
     * @param cf                The {@code CompositeFilter} to add
     * @param d                 The {@code Document}
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
     * Append filters present in {@code CompositeFilter} cf to an element
     * @param cf                The {@code CompositeFilter}
     * @param cfId              The id associated to the 
     *                          {@code CompositeFilter}
     * @param e                 The {@code Element}
     * @param d                 The associated {@code Document}
     * @throws BlockException   If we could not determine some filter's type
     * @throws WriterException  If some sub-{@code CompositeFilter} is 
     *                          ill-formatted
     */
    private static void appendFilters(CompositeFilter cf, String cfId,
        Element e, Document d) throws BlockException, WriterException {
        
        e.setAttribute(Writer.NB_INPUTS_ATTR_NAME, 
            String.valueOf(cf.nbInputs()));
        e.setAttribute(Writer.NB_OUTPUTS_ATTR_NAME, 
            String.valueOf(cf.nbOutputs()));

        // Outputs:
        for (int i = 0; i < cf.nbOutputs(); i++) {
            ReadDouble output;
            try {
                output = cf.output(i);
            } catch (FilterException fe) {
                throw new BlockException(fe.getMessage());
            }
            Element o = d.createElement(OUTPUT_NODE_TAG);
            o.setAttribute(NB_IOPUTS_ATTR_NAME, 
                           String.valueOf(output.outputIndex()));
            String outputId = output.source() == null ? 
                                                   cfId : output.source().id();
            o.setAttribute(REF_ATTR_NAME, outputId + "." 
                                                   + output.outputIndex());
            e.appendChild(o);
        }

        // Blocks:
        for (Block b : cf.blocks()) {
            e.appendChild(elementFromBlock(b, cfId, d));
        }
    }

    /**
     * Builds an {@code Element} from a {@code Block} in the 
     * {@code CompositeFilter}.
     * @param b     The {@code Block}
     * @param cfId  The id of the {@code CompositeFilter}
     * @param d     The {@code Document} to create the {@code Element} for
     * @return      The newly built {@code Element}
     * @throws BlockException   If a class name could not be found for the 
     *                          {@code Block} or an index exceeds the array's 
     *                          size
     * @throws WriterException  If a composite {@code Block} does not contains
     *                          a {@code CompositeFilter} (???)
     */
    private static Element elementFromBlock(Block b, String cfId,
        Document d) throws BlockException, WriterException {
        Element x = d.createElement(b.type());
            // id
            x.setAttribute(Writer.ID_ATTR_NAME, b.id());

            // inputs references, 2 by 2 in children
            Element y = d.createElement(VALUE_NODE_TAG);
            for (int i = 0; i < b.nbInputs(); i++) {
                ReadDouble input = b.input(i);
                String inputId = input.source() == null ? 
                                                    cfId : input.source().id();
                y.setAttribute("input." + i, inputId + "." 
                                                     + input.outputIndex());
                if (i != 0 && i % INPUTS_PER_NODE == 0) {
                    x.appendChild(y);
                    y = d.createElement(VALUE_NODE_TAG);
                }
            }
            if (b.nbInputs() > 0 && (b.nbInputs() % INPUTS_PER_NODE == 0 || 
                                     b.nbInputs() < INPUTS_PER_NODE)) { 
                x.appendChild(y);
            }
            
            // parameters (e.g. gain="0.7")
            y = d.createElement(VALUE_NODE_TAG);
            int i = 0;
            HashMap<String, String> parameters = b.filter().getParameters();
            for (HashMap.Entry<String, String> entry : parameters.entrySet()) {
                if ((i+1) % ATTRIBUTES_PER_NODE == 0) {
                    x.appendChild(y);
                    y = d.createElement(VALUE_NODE_TAG);
                }
                y.setAttribute(entry.getKey(), entry.getValue());
                i++;
            }
            if (y.getAttributes().getLength() >= 1) {
                x.appendChild(y);
            }

            // if CompositeFilter, then write it
            if (b.type() == Writer.NESTED_COMPOSITE_NODE_TAG) {
                if (!(b.filter() instanceof CompositeFilter))
                    throw new WriterException("composite Block contains " + 
                        "non-CompositeFilter filter ?!?!?");
                appendFilters((CompositeFilter) b.filter(), b.id(), x, d);
            }
        return x;
    }

    
}