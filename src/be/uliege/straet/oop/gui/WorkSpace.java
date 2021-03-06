package be.uliege.straet.oop.gui;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import be.uliege.straet.oop.filters.CenteredSquareGenerator;
import be.uliege.straet.oop.filters.CompositeFilter;
import be.uliege.straet.oop.filters.ConvolutionFilter;
import be.uliege.straet.oop.filters.DelayFilter;
import be.uliege.straet.oop.filters.DifferentiatorFilter;
import be.uliege.straet.oop.filters.GainFilter;
import be.uliege.straet.oop.filters.IntegratorFilter;
import be.uliege.straet.oop.filters.NoiseGenerator;
import be.uliege.straet.oop.filters.SineGenerator;
import be.uliege.straet.oop.filters.UpSquareGenerator;
import be.uliege.straet.oop.filters.WFilter;
import be.uliege.straet.oop.loader.LoaderException;
import be.uliege.straet.oop.loader.Writer;
import be.uliege.straet.oop.loader.WriterException;
import be.uliege.montefiore.oop.audio.AudioSequenceException;
import be.uliege.montefiore.oop.audio.FilterException;

/**
 * Holds the area where filters and wires are placed - moved - modified
 */
public class WorkSpace extends JPanel implements KeyListener {
    private static final long serialVersionUID = 1L;
    public static final int NORMAL = 0;
    public static final int SELECT_INPUT = 1;
    public static final int DRAW_WIRE = 2;

    // private static final int DEF_HEIGHT = 100;
    // private static final int DEF_WIDTH = 300;

    public static final double ZOOM_INCR = 1.3;

    public static final Color DEF_BACKGROUND = Color.white;
    public static final Color DEF_FOREGROUND = Color.black;

    /** Value used for filters that need a string as parameter, when that 
     * parameter has not been set. */
    public static final String DEF_UNDEFINED_STRING_VALUE = "To be defined";

    public static final String TMP_FILE_NAME = "/tmp/filteroutput.wav";

    private Color back = DEF_BACKGROUND;
    private Color fore = DEF_FOREGROUND;

    private int state = NORMAL;
    private double zoom;

    private Vector<DraggableFilter> filters = new Vector<DraggableFilter>();
    private Vector<FreeBall> freeBalls = new Vector<FreeBall>();
    private Vector<Wire> wires = new Vector<Wire>();
    private boolean wireHasInput = false;

    private Vector<FixedBall> inputs = new Vector<FixedBall>();
    private Vector<FixedBall> outputs = new Vector<FixedBall>();

    private Vector<DInputFilter> inputFilters = new Vector<DInputFilter>();
    private Vector<DOutputFilter> outputFilters = new Vector<DOutputFilter>();
    private Vector<DVariableDeclaration> variables = 
        new Vector<DVariableDeclaration>();

    private HashMap<String, Double> parameterSet = 
        new HashMap<String, Double>();

    private CompositeFilter cfR, cfL;
    // stuff to store the data
    private long audioSize = 0;
    private Computer computer = null;

    // shortcuts and deplacement stuff
    private boolean isCtrlHold = false;
    private boolean isShiftHold = false;
    private String openedFileName;

    /**
     * Constructor.
     */
    public WorkSpace() {
        super();
        // setSize(DEF_WIDTH, DEF_HEIGHT);
        // setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
        zoom = 1.0;
    }

    /**
     * Paints all the stuff in this {@code WorkSpace} on a {@code Graphics}.
     * @param g     The {@code Grapihcs} to paint on
     */
    public void paintComponent(Graphics g) {
        Dimension dim = getSize();
        g.setColor(back);
        g.fillRect(0, 0, (int) dim.getWidth(), (int) dim.getHeight());
        for (DraggableFilter d : filters)
            d.paint(g, back, fore, zoom);
        for (FreeBall fb : freeBalls)
            fb.paint(g, back, fore, zoom);
        for (Wire w : wires)
            w.paint(g, back, fore, zoom);
        
        if (state == DRAW_WIRE) {
            g.setColor(Color.BLACK);
            
            Wire w = wires.lastElement();
            int x1, y1;
            if (w.freeBalls().size() >= 1) {
                FreeBall fb = w.freeBalls().lastElement();
                x1 = fb.getX();
                y1 = fb.getY();
            }
            else {
                FixedBall fb = w.firstEnd();
                x1 = fb.getX();
                y1 = fb.getY();
            }
            Point mousePos = getMousePosition();
            g.drawLine(x1, y1, (int) mousePos.getX(), (int) mousePos.getY());
        }
    }

    /**
     * Deletes a {@code Draggable} element of this {@code WorkSpace}.
     * @param d     The {@code Draggable} to delete
     */
    public void delete(Draggable d) {
        if (d instanceof DraggableFilter)
            delete((DraggableFilter) d);
        else if (d instanceof FreeBall)
            delete((FreeBall) d);
        else
            System.out.println("SHOULD NEVER BE PRINTED.");
    }

    /**
     * Deletes a {@code DraggableFilter} in the {@code WorkSpace}.
     * @param d The {@code DraggableFilter} to delete
     */
    public void delete(DraggableFilter d) {
        filters.remove(d);
        if (d instanceof DInputFilter)
            inputFilters.remove(d);
        else if (d instanceof DOutputFilter)
            outputFilters.remove(d);
        else if (d instanceof DVariableDeclaration) 
            variables.remove(d);
        repaint();
    }

    /**
     * Delete a {@code FreeBall} of this {@code WorkSpace}.
     * @param fb The {@code FreeBall} to delete
     */
    public void delete(FreeBall fb) {
        freeBalls.remove(fb);
        repaint();
    }

    /**
     * Delete a {@code Wire} of this {@code WorkSpace}.
     * @param wire The {@code Wire} to delete
     */
    public void delete(Wire wire) {
        wires.remove(wire);
        for (FreeBall fb : wire.freeBalls())
            freeBalls.remove(fb);
        wire.freeEnds();
        repaint();
    }

    /**
     * Deletes a {@code FixedBall} of this {@code WorkSpace}.
     * @param fb    The {@code FixedBall} to delete
     */
    public void delete(FixedBall fb) {
        inputs.remove(fb);
        outputs.remove(fb);
    }

    /**
     * Adds {@code FixedBall}s in the vector of inputs.
     * @param fbs   The inputs to add
     */
    public void addInputs(FixedBall[] fbs) {
        for (FixedBall fb : fbs)
            inputs.add(fb);
    }

    /**
     * Adds {@code FixedBall}s in the vector of outputs.
     * @param fbs   The outputs to add
     */
    public void addOutputs(FixedBall[] fbs) {
        for (FixedBall fb : fbs)
            outputs.add(fb);
    }

    /**
     * @return      A {@code Vector<FixedBall>} containing all the 
     *              {@code FixedBall}s in this {@code WorkSpace} that are 
     *              inputs w.r.t. to their filter
     */
    public Vector<FixedBall> inputs() {
        return inputs;
    }

    /**
     * @return      A {@code Vector<FixedBall>} containing all the 
     *              {@code FixedBall}s in this {@code WorkSpace} that are 
     *              outputs w.r.t. to their filter
     */
    public Vector<FixedBall> outputs() {
        return outputs;
    }

    /**
     * Sets the current state of the {@code WorkSpace}. Be carefull xD
     * @param state The new state of the {@code WorkSpace}.
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return The current state of the {@code WorkSpace}
     */
    public int getState() {
        return state;
    }

    /**
     * Starts the drawing of a {@code Wire}.
     * @throws FilterException  If there is no input or output to connect.
     */
    public void addConnection() throws FilterException {
        if (isBusy())
            return;
        if (inputs.size() <= 0) 
            throw new FilterException("There is no input to connect !");
        else if (outputs.size() <= 0) 
            throw new FilterException("There is no outputs to connect !");
        
        Wire w = new Wire(this);
        wires.add(w);
        state = SELECT_INPUT;
        for (FixedBall fb : inputs) 
            fb.setHighlighted(true);
        for (FixedBall fb : outputs) 
            fb.setHighlighted(true);
        repaint();
    }

    /**
     * Creates a connection between two filters knowing the intermediates
     * points.
     * @param origin    The {@code FixedBall} that outputs its result in the
     *                  {@code Wire}
     * @param dest      The {@code FixedBall} that the {@code Wire} give its 
     *                  input
     * @param x         The x coordinates of intermediates points
     * @param y         The y coordinates of intermediates points
     * @throws FilterException If the x and y arrays size mismatch
     */
    public void addConnection(FixedBall origin, FixedBall dest, 
        int[] x, int[] y) throws FilterException {
        
        if (x.length != y.length) 
            throw new FilterException("X coordinates length doesn't match Y " +
                "coordinales length.");
        
        Wire wire = new Wire(this);
        wire.setFirst(origin);
        for (int i = 0; i < x.length; i++) {
            FreeBall fr = new FreeBall(x[i], y[i], this, wire);
            wire.addFreeBall(fr);
            freeBalls.add(fr);
        }
        wire.setSecond(dest);
        dest.setConnected(true);
        wires.add(wire);
    }


    /**
     * Starts the placing of an addition filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     */
    public DraggableFilter addAddition(int x, int y, int orientation, 
        boolean selected) {
        DAdditionFilter af = new DAdditionFilter(x, y, this, selected);
        filters.add(af);
        return af;
    }
    
    /** Starts the placing of an addition filter at (0, 0), needs focus */
    public void addAddition() {  
        if (isBusy())  return; 
        addAddition(0, 0, 0, true);
    }

    /**
     * Starts the placing of a gain filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      A {@code GainFilter} that will be used in that 
     *                   {@code DGainFilter}
     */
    public DraggableFilter addGain(int x, int y, int orientation, 
        boolean selected, GainFilter filter) {
        DGainFilter df = new DGainFilter(x, y, this, selected, filter);
        filters.add(df);
        return df;
    }
    
    /** Starts the placing of an default gain filter at (0, 0), needs focus. */
    public void addGain() {   
        if (isBusy())  return; 
        addGain(0, 0, 0, true, new GainFilter());
      }

    /**
     * Starts the placing of a delay filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      A {@code DelayFilter} that will be used in that 
     *                   {@code DDelayFilter}
     */
    public DraggableFilter addDelay(int x, int y, int orientation, 
        boolean selected, DelayFilter filter) {
        DDelayFilter df = new DDelayFilter(x, y, this, selected, filter);
        filters.add(df);
        return df;
    }

    /** Starts the placing of a default delay filter at (0, 0), needs focus. */
    public void addDelay() {     
        if (isBusy())  return; 
        addDelay(0, 0, 0, true, new DelayFilter());
    }

    /** Starts the placing of a default convolution filter at (0, 0), needs 
     * focus. */
    public void addConvolution() {     
        if (isBusy())  return; 
        addConvolution(0, 0, 0, true, new ConvolutionFilter());
    }

    /**
     * Starts the placing of a convolution filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      A {@code ConvolutionFilter} that will be used in that
     *                   {@code DConvolutionFilter}
     */
    public DraggableFilter addConvolution(int x, int y, int orientation, 
        boolean selected, ConvolutionFilter filter) {
        DConvolutionFilter cf = 
            new DConvolutionFilter(x, y, this, selected, filter);
        filters.add(cf);
        return cf;
    }
    
    /** 
     * Starts the placing of an undefined composite filter at (0, 0), needs
     * focus.
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
    public void addComposite() throws LoaderException, 
        DOMException, FilterException, ParserConfigurationException, 
        SAXException, IOException {     
        if (isBusy())  return; 
        addComposite(0, 0, 0, true, null);
    }

    /**
     * Starts the placing of a composite filter. It will be invalid 
     * until it is set.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param fileName    The name of the file describing this 
     *                    {@code CompositeFilter}
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
    public DraggableFilter addComposite(int x, int y, int orientation, 
        boolean selected, String fileName) throws LoaderException, 
        DOMException, FilterException, ParserConfigurationException, 
        SAXException, IOException {
        
        DCompositeFilter cf;
        if (fileName != null)
            cf = new DCompositeFilter(x, y, this, selected, fileName);
        else 
            cf = new DCompositeFilter(x, y, this, selected);
        filters.add(cf);
        return cf;
    }

    /**
     * Starts the placing of a integrator filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      An {@code Integrator} that will be used in that 
     *                   {@code DIntegratorFilter}
     */
    public DraggableFilter addIntegrator(int x, int y, int orientation, 
        boolean selected, IntegratorFilter filter) {
        DIntegratorFilter intf = new DIntegratorFilter(x, y, this, selected, 
                                                       filter);
        filters.add(intf);
        return intf;
    }

    /** Starts the placing of a default integrator filter at (0, 0), needs 
     * focus. */
    public void addIntegrator() {     
        if (isBusy())  return; 
        addIntegrator(0, 0, 0, true, new IntegratorFilter());
    }

    /**
     * Starts the placing of a differentiator filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      An {@code DifferentiatorFilter} that will be used in 
     *                    that {@code DDifferentiatorFilter}
     */
    public DraggableFilter addDifferentiator(int x, int y, int orientation, 
        boolean selected, DifferentiatorFilter filter) {
        DDifferentiatorFilter df = new DDifferentiatorFilter(x, y, this, 
            selected, filter);
        filters.add(df);
        return df;
    }

    /** Starts the placing of a default integrator filter at (0, 0), needs 
     * focus. */
    public void addDifferentiator() {     
        if (isBusy())  return; 
        addDifferentiator(0, 0, 0, true, new DifferentiatorFilter());
    }

    /**
     * Starts the placing of a sine generator.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      A {@code Sinegenerator} that will be used in that 
     *                   {@code DSineGenerator}
     */
    public DraggableFilter addSineGenerator(int x, int y, int orientation, 
        boolean selected, SineGenerator filter) {
        DSineGenerator sg = new DSineGenerator(x, y, this, 
            selected, filter);
        filters.add(sg);
        return sg;
    }

    /** Starts the placing of a default sine generator at (0, 0), needs 
     * focus. */
    public void addSineGenerator() {     
        if (isBusy())  return; 
        addSineGenerator(0, 0, 0, true, new SineGenerator());
    }

    /**
     * Starts the placing of a centered square generator.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      A {@code CenteredSquaregenerator} that will be used 
     *                    in that {@code DCenteredSquareGenerator}
     */
    public DraggableFilter addCenteredSquareGenerator(int x, int y, 
        int orientation, boolean selected, CenteredSquareGenerator filter) {
        DCenteredSquareGenerator sg = new DCenteredSquareGenerator(x, y, this,
            selected, filter);
        filters.add(sg);
        return sg;
    }

    /** Starts the placing of a default centered square generator at (0, 0), 
     * needs focus. */
    public void addCenteredSquareGenerator() {     
        if (isBusy())  return; 
        addCenteredSquareGenerator(0, 0, 0, true, 
            new CenteredSquareGenerator());
    }

    /**
     * Starts the placing of a up square generator.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      A {@code UpSquaregenerator} that will be used in that
     *                   {@code DUpSquareGenerator}
     */
    public DraggableFilter addUpSquareGenerator(int x, int y, int orientation, 
        boolean selected, UpSquareGenerator filter) {
        DUpSquareGenerator sg = new DUpSquareGenerator(x, y, this, 
            selected, filter);
        filters.add(sg);
        return sg;
    }

    /** Starts the placing of a default up square generator at (0, 0), needs 
     * focus. */
    public void addUpSquareGenerator() {     
        if (isBusy())  return; 
        addUpSquareGenerator(0, 0, 0, true, new UpSquareGenerator());
    }

    /**
     * Starts the placing of a noise generator.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param filter      A {@code Noisegenerator} that will be used in that 
     *                   {@code DNoiseGenerator}
     */
    public DraggableFilter addNoiseGenerator(int x, int y, int orientation, 
        boolean selected, NoiseGenerator filter) {
        DNoiseGenerator sg = new DNoiseGenerator(x, y, this, 
            selected, filter);
        filters.add(sg);
        return sg;
    }

    /** Starts the placing of a default noise generator at (0, 0), needs 
     * focus. */
    public void addNoiseGenerator() {     
        if (isBusy())  return; 
        addNoiseGenerator(0, 0, 0, true, new NoiseGenerator());
    }
    
    /**
     * Starts the placing of an input filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     */
    public DraggableFilter addInput(int x, int y, int orientation, 
        boolean selected, String iFileName) {
        DInputFilter dif = new DInputFilter(x, y, this, selected, iFileName);
        filters.add(dif);
        inputFilters.add(dif);
        return dif;
    }

    /** Starts the placing of an input filter at (0, 0), needs focus. */
    public void addInput() {
        if (isBusy())  return; 
        addInput(0, 0, 0, true, DEF_UNDEFINED_STRING_VALUE);
    }

    /**
     * Starts the placing of an output filter.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     */
    public DraggableFilter addOutput(int x, int y, int orientation, 
        boolean selected, String oFileName) {
        DOutputFilter dof = new DOutputFilter(x, y, this, selected, oFileName);
        filters.add(dof);
        outputFilters.add(dof);
        return dof;
    }

    /** Starts the placing of an output filter at (0, 0), needs focus. */
    public void addOutput() { 
        if (isBusy())  return; 
        addOutput(0, 0, 0, true, DEF_UNDEFINED_STRING_VALUE);
     }

    /** Starts the placing of a variable declaration.
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param orientation The orientation of the filter: the filter is oriented
     *                    "normally" + orientation * 90° clockwise.
     * @param selected    Wether or not the user is dragging this filter when 
     *                    it is placed.
     * @param varName The name of the new variable to add
     * @param varDefinition The definition of the new variable to add.
     */
    public DraggableFilter addVariableDeclaration(int x, int y, 
        int orientation, boolean selected, 
        String varName, String varDefinition) {
        DVariableDeclaration vd = new DVariableDeclaration(x, y, this, 
            selected, varName, varDefinition);
        filters.add(vd);
        variables.add(vd);
        parameterSet.put(vd.getVariableName(), vd.getVariableValue());
        return vd;
    }

    /** Starts the placing of a variable declaration at (0, 0), needs focus. */
    public void addVariableDeclaration() {     
        if (isBusy())  return; 
        addVariableDeclaration(0, 0, 0, true, "To be defined", "0");
     }

    /**
     * Cancels the current operation (e.g. the drawing of a new `Wire`).
     */
    public void cancelCurrent() {
        if (state == DRAW_WIRE) {
            Wire w = wires.elementAt(wires.size() - 1);
            removeMouseListener(w);
            for (FreeBall fb : w.freeBalls())
                freeBalls.remove(fb);
            wires.removeElementAt(wires.size() - 1);
        } else if (state == SELECT_INPUT) {
            for (FixedBall fb : inputs) {
                removeMouseListener(fb);
                fb.setHighlighted(false);
            }
            for (FixedBall fb : outputs) {
                removeMouseListener(fb);
                fb.setHighlighted(false);
            }

            wires.removeElementAt(wires.size() - 1);
        }
        state = NORMAL;
        repaint();
    }

    /**
     * Links the {@code FixedBall}s and the {@code Wire}s.
     * @param fb The {@code FixedBall} to add to the constructed {@code Wire}
     */
    public void sendFixedBall(FixedBall fb) {
        if (!wireHasInput) {
            wires.lastElement().setFirst(fb);
            if (fb.isInput()) 
                for (FixedBall f : inputs)
                    f.setHighlighted(false);
            else 
                for (FixedBall f : outputs) 
                    f.setHighlighted(false);
            
            state = DRAW_WIRE;
        } else {
            if (!wires.lastElement().setSecond(fb))
                return;
            if (fb.isInput())
                for (FixedBall f : inputs) 
                    f.setHighlighted(false);
            else 
                for (FixedBall f : outputs)
                    f.setHighlighted(false);
            state = NORMAL;
        }
        
        repaint();
        wireHasInput = !wireHasInput;
    }

    /**
     * Adds a {@code FreeBall} to the elements of the {@code WorkSpace}.
     * @param x    The x coordinate
     * @param y    The y coordinate
     * @param wire The {@code Wire} that owns this {@code FreeBall}
     */
    public void addFreeBall(int x, int y, Wire wire) {
        if (wire.isComplete())
            return;
        FreeBall fr = new FreeBall(x, y, this, wire);
        freeBalls.add(fr);
        wires.lastElement().addFreeBall(fr);
    }

    @Override  public void keyTyped(KeyEvent e) { }
    @Override  public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL:   isCtrlHold = false;     break;
            case KeyEvent.VK_SHIFT:     isShiftHold = false;    break;
            default:    break;
        }
    }

    @Override  public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        switch (code) {
            case KeyEvent.VK_CONTROL:   isCtrlHold = true;     break;
            case KeyEvent.VK_SHIFT:     isShiftHold = true;    break;
            case KeyEvent.VK_S:
                if (isCtrlHold) {
                    if (isShiftHold)
                        saveAs();
                    else
                        save();
                }                                               break;
            case KeyEvent.VK_O:
                if (isCtrlHold)         open();                 break;
            case KeyEvent.VK_Q:
                if (isCtrlHold)         quit();                 break;
            default:                                            break;
        }
    }

    /**
     * Handles busyness properly...
     * @return Wether or not we are able to start a new action
     */
    public boolean isBusy() { 
       return state != NORMAL; 
    }

    /**
     * Multiplies the zoom factor by 1.3.
     */
    public void zoomIn() {
        if (zoom < 200)
            zoom *= ZOOM_INCR;
        repaint();
    }

    /**
     * Divides the zoom factor by 1.3;
     */
    public void zoomOut() {
        if (zoom > 0.005)
            zoom /= ZOOM_INCR;
        repaint();
    }

    /**
     * @return      The zoom factor of this {@code WorkSpace}
     */
    public double zoomFactor() {
        return zoom;
    }

    /**
     * Returns an int scaled by {@code zoom}. Mainly to handle casts proprely.
     * @param value The value to scale
     * @param zoom  The scaling factor
     * @return The scaled value
     */
    public static int zoom(int value, double zoom) {
        double d = ((double) value) * zoom;
        return (int) d;
    }

    /**
     * Pre-builds the represented filter and stores it in resultFilter. Tests 
     * it to detect if some connections are missing or impossible.
     * @param isRight       Wether or not this will be the right filter (only 
     *                      to build {@code CompositeFilter} from different 
     *                      filter)
     * @return              The {@code CompositeFilter} that has been built
     * @throws FilterException      When some connection or instanciation could
     *                              not be made when building the filter 
     *                              represented by the {@code WorkSpace}
     */
    public CompositeFilter buildFilter(boolean isRight) 
        throws FilterException {

        int nInputs = inputFilters.size();
        int nOutputs = outputFilters.size();

        CompositeFilter cf = new CompositeFilter(nInputs, nOutputs);

        for (DraggableFilter df : filters) {
            if (isActiveFilter(df)) {
                if (isRight)
                    cf.addBlock(df.filterR);
                else
                    cf.addBlock(df.filterL);
            }
        }

        makeWiresConnections(cf, isRight);

        testFilter(cf);

        return cf;
    }

    /**
     * <p>Returns true if the parameter is a filter that "does" something, i.e.
     * is not a {@code DInputFilter}, {@code DOutputFilter} nor a 
     * {@code DVariableDeclaration}.</p>
     * <p>"Active" filters will be added as blocks in the 
     * {@code CompositeFilter}.</p>
     * @param df        The {@code DraggableFilter}
     * @return          true if the {@code DraggableFilter} is "active".
     */
    public static boolean isActiveFilter(DraggableFilter df) {
        return  (!(df instanceof DInputFilter) && 
                 !(df instanceof DOutputFilter) && 
                 !(df instanceof DVariableDeclaration) );
    }

    /**
     * Loops over all the wires and makes the connections they represents in 
     * the {@code CompositeFilter}.
     * @param filter    The {@code CompositeFilter} in wich we makes the 
     *                  connections
     * @param isRight   If we build the right or the left filter (to use 
     *                  differnent filters in the different 
     *                  {@code CompositeFilter}s)
     * @throws FilterException  If some {@code CompositeFilter} could not be 
     *              created or some connection could not be made.
     */
    private boolean makeWiresConnections(CompositeFilter filter, 
        boolean isRight)  throws FilterException {

        for (Wire w : wires) {
            DraggableFilter a = ownerOf(w.output());
            DraggableFilter b = ownerOf(w.input());
            // handle cases: in is input, out is output

            if (a instanceof DInputFilter) {
                // first end of wire is an input filter
                if (b instanceof DOutputFilter) {
                    showError("Could not connect directly input to output " + 
                        "(useless)", null);
                    return false;
                } else {
                    WFilter fb = isRight ? b.filterR : b.filterL;
                    filter.connectInputToBlock(inputFilters.indexOf(a), fb, 
                        b.numOfInput(w.input()));
                }
            } else {
                // first end of wire is not an input filter

                WFilter fa = isRight ? a.filterR : a.filterL;
                if (b instanceof DOutputFilter) {
                    filter.connectBlockToOutput(fa, a.numOfOutput(w.output()), 
                        outputFilters.indexOf(b));
                    
                } else {
                    WFilter fb = isRight ? b.filterR : b.filterL;
                    filter.connectBlockToBlock(fa, a.numOfOutput(w.output()), 
                        fb, b.numOfInput(w.input()));
                }
            }

        } // loop over {@code Wire}s
        return true;
    }

    /**
     * Returns the {@code DraggableFilter} that owns this {@code FixedBall}.
     * @param fb The {@code FixedBall}
     * @return The {@code DraggableFilter} that owns the {@code FixedBall}, 
     *          {@code null} if it could not be found.
     */
    private DraggableFilter ownerOf(FixedBall fb) {
        for (DraggableFilter df : filters) {
            if (df.numOfInput(fb) != -1 || df.numOfOutput(fb) != -1)
                return df;
        }
        return null;
    }

    /**
     * Tests the current {@code CompositeFilter} to look for problems in the 
     * connections (e.g. impossible feeback loops).
     * @param cf        The {@code CompositeFilter} to test
     */
    private void testFilter(CompositeFilter cf) throws FilterException {
        double[] in = new double[cf.nbInputs()];

        cf.computeOneStep(in);
        cf.reset();
    }

    /**
     * Runs the represented filter on the specified input (described by one or
     * more {@code DInputFilter}(s)) and outputs it to the file represented in
     * an {@code DOutputFilter}.
     * @param isTmp If true, the file will be outputted in a tmp file
     *              (TMP_FILE_NAME)
     * @return  The {@code Computer} that will be responsible of the output in
     *          the file.
     */
    public Computer buildOutputFile(boolean isTmp) {
        if (outputFilters.size() != 1) {
            showError("There must be one and only one output filter in order" +
                " to output the result to a file.", null);
            return null;
        }

        Computer c = null;
        try {
            cfR = buildFilter(true);
            cfL = buildFilter(false);
            String name = isTmp ? TMP_FILE_NAME : 
                outputFilters.firstElement().parameterS;

            c = new Computer(getAudioSequences(), cfR, cfL, name);
            new Thread(c, "Computation of output").start();
        } catch (FilterException e) {
            showError("Could not buid the filter.", e);
        } catch (ComputationException e) {
            showError("Could not proceed to computation.", e);
        } catch (AudioSequenceException e) {
            showError("Could not load an AudioSequence.", e);
        }

        return c;
    }

    /**
     * Gets the {@code AudioSequence2}s ready for the computation.
     * @return An array containing all the {@code AudioSequence2}s
     * @throws AudioSequenceException If some {@code AudioSequence2} could not
     *                                be loaded.
     */
    private AudioSequence2[] getAudioSequences() throws AudioSequenceException 
        {

        int n = inputFilters.size();
        AudioSequence2[] as = new AudioSequence2[n];
        int i = 0;
        for (DInputFilter dif : inputFilters) {
            dif.loadAudioSequence();
            as[i] = dif.getAudioSequence();
            i++;
        }

        audioSize = 0;
        for (DInputFilter dif : inputFilters) {
            long cur = dif.getAudioSequence().getSize();
            audioSize = audioSize < cur ? cur : audioSize;
        }

        return as;
    }

    /**
     * Applies the filter represented in this {@code WorkSpace} to a sample of
     * voice.
     * @throws ComputationException    If the computation could not be made
     * @throws FilterException         If the represented filter is invalid, or
     *                                  could not be built
     */
    public void applyToVoice() throws ComputationException, FilterException {
        JOptionPane.showMessageDialog(this, "Click OK to start recording",
            "Record", JOptionPane.INFORMATION_MESSAGE);
        
        cfR = buildFilter(true);
        cfL = buildFilter(false);
        int[] res = new int[1];
        res[0] = -1;

        computer = new Computer(null, cfR, cfL, res);
        new Thread(computer, "Play of voice input").start();

        res[0] = JOptionPane.showConfirmDialog(this, 
            "Click OK to end recording", "Record", 
            JOptionPane.OK_CANCEL_OPTION);
    }

    /**
     * Plays the result of the filter based on the specifed input files.
     */
    public void playResult() {
        if (computer != null && computer.isAudioPaused()) {
            computer.play();
            return;
        }
        try {
            cfR = buildFilter(true);
            cfL = buildFilter(false);
            computer = new Computer(getAudioSequences(), cfR, cfL, 
                                    Computer.PLAY_AUDIO);
            new Thread(computer, "Play of the output").start();
        } catch (FilterException e) {
            showError("Could not buid the filter.", e);
        } catch (ComputationException e) {
            showError("Wrong number of inputs/outputs.", e);
        } catch (AudioSequenceException e) {
            showError("Could not load some input data.", e);
        }
    }

    /**
     * Pauses the play of the computed output.
     */
    public void pauseResult() {
        if (computer == null)   return;
        synchronized(computer) {
            computer.pause();
        }
    }

    /**
     * Stops the play of the computed output.
     */
    public void abortResult() {
        if (computer == null)   return;
        synchronized(computer) {
            computer.end();
        }

        computer = null;
    }

    /**
     * Refreshes the list of variables, the names and the values.
     */
    public void refreshVariablesValues() {
        for (int i = 0; i < variables.size(); i++) {
            DVariableDeclaration vd = variables.elementAt(i);
            parameterSet.put(vd.getVariableName(), vd.getVariableValue());
        }
        for (DraggableFilter df : filters) {
            try {
				df.refreshValue();
            } catch (DOMException | FilterException | 
                     ParserConfigurationException | SAXException | 
                     IOException e) {
                showError("Could not reload some file: parameters needed are" +
                    "no longer valid (bugs may happen now man, be happy)", e);
			}
        }
    } 

    /**
     * Parameter set of this {@code WorkSpace}.
     * @return      A {@code HashMap<String, Double>} containing all the 
     *              parameters in this {@code WorkSpace} and their values
     */
    public HashMap<String, Double> getParameterSet() {
        return parameterSet;
    }
    
    /**
     * Saves the current {@code WorkSpace} (gets file name and saves it).
     */
    public void saveAs() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "XML Files", "xml");
        chooser.setFileFilter(filter);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileName = chooser.getSelectedFile().getPath();
            saveToFile(fileName);
            openedFileName = fileName;
        }
    }

    /**
     * Saves the current {@code WorkSpace} in the currently opened file. If
     * none, saves as.
     */
    public void save() {
        if (openedFileName == null)  
            saveAs();
        else 
            saveToFile(openedFileName);
        
        
    }

    /**
     * Saves the current {@code WorkSpace} state to an xml file.
     * @param fileName      The name of the file to write.
     */
    public void saveToFile(String fileName) {
        try {
            cfR = buildFilter(true);
        } catch (FilterException e) {
            showError("Could not build filter", e);
        }
        try {
            WorkSpaceXML wxml = new WorkSpaceXML(cfR, variables, filters, 
                inputFilters, outputFilters);
            Writer.writeDocument(wxml.buildDocument(), fileName);
        } catch(WriterException e) {
            WorkSpace.showError("An error occured when building `Document`", 
                                e);
        }
    } 

    /**
     * Opens a file in this {@code WorkSpace} (gets file name and opens it).
     */
    public void open() {
        String fileName;
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "XML Files", "xml");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            fileName = chooser.getSelectedFile().getPath();
            try {
                clear();
                openFile(fileName);
            } catch (Exception e) {
                System.out.println("Could not read or parse file: \""
                    + fileName +"\". Message: " + e.getMessage() + 
                    "\nStacktrace: ");
                e.printStackTrace();
            }
        }
    }

    /**
     * Opens a file and loads its content to the {@code WorkSpace}.
     * @param fileName      The name of the file to open.
     * @throws IOException  If some error occured when reading and parsing the
     *                      file or files required by recursion.
     * @throws SAXException Idem
     * @throws ParserConfigurationException Idem
     * @throws DOMException                 Idem
     * @throws NumberFormatException        Idem
     */
    public void openFile(String fileName) throws ParserConfigurationException,
        SAXException, IOException, NumberFormatException, DOMException, 
        FilterException {
        WorkSpaceXML wxml = new WorkSpaceXML(this);
        wxml.openFile(fileName);
        openedFileName = fileName;
    }

    /**
     * Export the current filter as a standalone filter, i.e. including all the
     * inner filter included via {@code CompositeFilter}s.
     * @throws FilterException      When some connection or instanciation could
     *                              not be made when building the filter 
     *                              represented by the {@code WorkSpace}
     * @throws WriterException      If the {@code CompositeFilter} could not be
     *                              written in the file
     */
    public void exportStandaloneFilter() throws FilterException, 
        WriterException {

        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "XML Files", "xml");
        chooser.setFileFilter(filter);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String fileName = chooser.getSelectedFile().getPath();
            CompositeFilter cf = buildFilter(true);
            Writer.writeFilter(cf, fileName);
        }
    }

    /**
     * Exports an equivalent convolution vector to a csv chosen csv file, 
     * requires the user to enter the precision.
     * @throws IOException      If some file could not be opened - written.
     * @throws FilterException  If the filter is not currently complete, or an 
     *                          output could not be computed.
     */
    public void exportConvolutionVector() throws IOException, FilterException {
        // get the precision...
        double precision;
        String s = JOptionPane.showInputDialog(this, 
            "Enter the precision (the maximal difference between two " +
            "consecutive outputs that\nhas to be carried out " +
            "getMaxSamplesInfluenced() times. (0.001 is a good value)");
        if (s == null) 
            return;
        
        precision = Double.parseDouble(s);
        if (precision < 0) {
            showError("The precision cannot be negative !", null);
            return;
        }
        
        // get the file
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "CSV files", "csv", "CSV");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("CSV to save the vector in");
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            cfR = buildFilter(true);
            
            double[] d = cfR.computeEquivalentConvolutionVector(precision);
            for (int i = 0; i < d.length-1; i++) 
                bw.write(Double.toString(d[i]) + ", ");
            bw.write(Double.toString(d[d.length-1]));
            bw.close();
        }
    }

    /**
     * Clears the {@code WorkSpace} from all its components.
     */
    public void clear() {
        state = NORMAL;
        zoom = 1.0;

        filters.clear();
        freeBalls.clear();
        wires.clear();
        wireHasInput = false;

        inputs.clear();         outputs.clear();
        inputFilters.clear();   outputFilters.clear();
        variables.clear();

        parameterSet =  new HashMap<String, Double>();

        cfR = null;             cfL = null;

        audioSize = 0;
        computer = null;

        isCtrlHold = false;
        isShiftHold = false;
        openedFileName = null;
    }

    /**
     * Shows an error with a user friendly message and to the standard output.
     * @param s     A {@code String} containing some message
     * @param e     An {@code Exception}, if not null print its stacktrace to 
     *              the standard output
     */
    public static void showError(String s, Exception e) {
        String t = e == null ? s : s + " (" + e.getMessage() + ")";
        System.out.println("[ERROR] " + t);
        if (e != null) {
            System.out.println("Stacktrace:");
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(null, t, "Error occured", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Makes the program to stop.
     */
    public void quit() {
        if (JOptionPane.showConfirmDialog(null, "Quit program now ?\n " + 
            "Unsaved changes will be lost.", "Quit", JOptionPane.YES_NO_OPTION)
            == JOptionPane.YES_OPTION)
            System.exit(0);
    }

    /**
     * <p>Part of the Nothing things...</p>
     * <p>Don't look at this...</p>
     * <p>Adds a nothing filter to the `WorkSpace`</p>
     * @param isLinearInterpAfter (...)
     */
	public NothingFilter addNothing(boolean nextInterpMethodIsLin) {
       NothingFilter nf = new NothingFilter(this, nextInterpMethodIsLin);
       filters.add(nf);
       return nf;

	}

    /**
     * <p>Part of the Nothing things...</p>
     * <p>Don't look at this</p>
     * 
     * <p>Writes the stuff needed for the fg program.</p>
     * @throws Exception   if sth went wrong
     */
	public void printNothing(PrintWriter pw) throws Exception {
        if (filters.isEmpty())
            throw new Exception("filters is empty");

        if (!(filters.get(0) instanceof NothingFilter))
            throw new Exception("first filter is not a Nothing Filter");

        NothingFilter f = (NothingFilter) filters.get(0);
        HashSet<NothingFilter> visited = new HashSet<NothingFilter>();
        while (true) {
            visited.add(f);
            boolean isLine = f.getNextInterpMethodIsLin();
            double timeStamp = f.getTimeStamp();
            nothingLine(timeStamp, f, isLine, pw);
            
            if (f.wireAtOutputs().isEmpty()) {
                System.out.println("No more continuing wire. " + 
                    "Stopping file writing.");
                return;
            }

            Wire w = f.wireAtOutputs().get(0);
            NothingFilter next = (NothingFilter) w.input().owner();
            Vector<FreeBall> fbs = w.freeBalls();
            for (int i = 0; i < fbs.size(); i++) {
                FreeBall fb = fbs.get(i);
                double fbTimeStamp;
                if (!visited.contains(next)) {
                    double nextTimeStamp = next.getTimeStamp();
                    double dt = (nextTimeStamp - timeStamp) / fbs.size();
                    fbTimeStamp = timeStamp + dt * i;
                }
                else 
                    fbTimeStamp = timeStamp + i + 1;

                nothingLine(fbTimeStamp, fb, isLine, pw);
            }

            if (!(next instanceof NothingFilter)) {
                System.out.println("Output of wire is not a NothingFilter. " + 
                    "Stopping file writing.");
                return;
            }
            else if (visited.contains(next)) {
                nothingLine(timeStamp + fbs.size() + 1, next, isLine, pw);
                System.out.println("Reached already visited NothingFilter. " +
                    "Stopping file writing.");
                return;
            }
            f = (NothingFilter) next;
        }
    }
    
    /**
     * <p>Part of the Nothing things...</p>
     * <p>Don't look at this</p>
     * 
     * <p>Prints a line representing a to the file...</p>
     * @param timeStamp     The time stamp of the point
     * @param l             A {@code Locatable} object at this point
     * @param isLine        Wether or not the next line is line or cubic spline
     */
    public void nothingLine(double timeStamp, Locatable l, boolean isLine,
        PrintWriter pw) {

        if (isLine)
            pw.println(timeStamp + ": l " +
                       "(" + l.getX() + ", " + l.getY() + ")");
        else
            pw.println(timeStamp + ": " +
                       "(" + l.getX() + ", " + l.getY() + ")");

    }
}