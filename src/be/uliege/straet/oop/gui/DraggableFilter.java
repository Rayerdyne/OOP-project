package be.uliege.straet.oop.gui;

import java.util.HashMap;
import java.util.Vector;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import be.uliege.straet.oop.filters.WFilter;
import be.uliege.straet.oop.loader.LoaderException;
import be.uliege.straet.oop.loader.NodeData;

public class DraggableFilter extends Draggable {

    public static final int NONE = 0;
    public static final int DOUBLE = 1;
    public static final int STRING = 2;
    public static final int VARIABLE_DECLARATION = 3;
    public static final int FILE_PATH = 4;

    protected Color color = WorkSpace.DEF_FOREGROUND;
    protected FixedBall[] inputs;
    protected FixedBall[] outputs;
    protected double[] parameterD;
    protected String parameterS;
    protected WFilter filterR, filterL; /* two filters must be different for 
                two `CompositeFilter`s that may be built for stereo output */

    private Vector<Wire> inConnectedWires = new Vector<Wire>();
    private Vector<Wire> outConnectedWires = new Vector<Wire>();

    public DraggableFilter(int x, int y, WorkSpace ws) {
        super(x, y, ws);
    }

    public DraggableFilter(int x, int y, WorkSpace ws, boolean selected) {
        super(x, y, ws, selected);
    }

    public FixedBall[] inputs() { return inputs; }
    public FixedBall[] outputs() { return outputs; }

    public void translateBox(int x, int y) {
        for (FixedBall fb : inputs) 
            fb.translate(x, y);
        for (FixedBall fb : outputs)
            fb.translate(x, y);
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;
            yCorners[i] += y;
        }
    }

    /**
     * Returns the name of the parameter that characterizes the filter, i.e. 
     * "gain", "delay", etc.
     * Obviously, it sould be overriden.
     * @return      The name of the parameter
     */
    public String getParameterInfo() { return "Should not be visible"; }

    public String getParameterDefinition() {  return parameterS;  }

    /**
     * Returns the parameter type of the filter:
     * - NONE or
     * - DOUBLE or 
     * - STRING
     */
    public int getParameterType() { return NONE; }

    /**
     * Shows the dialog and returns the entered String.
     */
    public String getParameterInput() {
        return JOptionPane.showInputDialog("Enter new " 
            + getParameterInfo() + ".");
    }

    /**
     * Gets the path of a file
     * @param saveFile  If true, select a file for saving (then the 
     * file will be created), else the file should exist.
     * @param extentions Allowed extentions for the file to select.
     * @return  A `String` containing the path of the selected file
     */
    public String getFilePathInput(boolean saveFile, String... extentions) {
        JFileChooser chooser = new JFileChooser();
        String description = "";
        if (extentions.length > 1) {
            for (int i = 0; i < extentions.length-1; i++) 
                description += extentions[i] + ", ";
            description += " or " + extentions[extentions.length-1] + " files";
        }
        else
            description = extentions[0] + " files";
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            description, extentions);
        chooser.setFileFilter(filter);
        if (saveFile)
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        int returnVal = chooser.showOpenDialog(ws);
        if (returnVal == JFileChooser.APPROVE_OPTION) 
            return chooser.getSelectedFile().getPath();
        else
            return null;
    }

    /**
     * Sets the property of a filter based on a double argument (i.e., rounded
     * for the delay filters). --> Will be overriden.
     * Missing a way to handle the convolution filter.
     * @param d
     */
    public void setParameter(double[] d) {
        parameterD = d;
    }

    /**
     * Sets the property of a filter based on a String argument that will be 
     * parsed in double (i.e., rounded for the delay filters).
     * --> Will be overriden.
     * Missing a way to handle the convolution filter.
     * @param s The String containing the value of the parameter.
     */
    public void setParameter(String s) {
        if (s != null)
            parameterS = s;
    }

    @Override
    public void edit() {
        selected = false;
        switch (getParameterType()) {
            case STRING:
                setParameter(getParameterInput());
                break;
            case DOUBLE:
                String oldValue = parameterS;
                parameterS = getParameterInput();
                if (parameterS == null) {
                    parameterS = oldValue;
                    return;
                }
                double[] d;
                try {
                    // d = WorkSpaceXML.parseStringValue(parameterS, 
                    //     ws.getParameterSet());
                    d = parseStringValues(parameterS, ws.getParameterSet());
                } catch (NumberFormatException | LoaderException e) {
                    JOptionPane.showMessageDialog(null, "Invalid input: " + 
                        e.getMessage());
                    parameterS = oldValue;
                    return;
                }
                setParameter(d);
                break;
            case FILE_PATH:
                setParameter(getFilePathInput(true, "csv", "wav", "WAV"));
                break;
            default:    break;
        }
        ws.repaint();
    }

    /**
     * Parse a double[] from a String.
     * @param s             The `String` to parse
     * @param parameters    A set of parameters
     * @return              The parsed array of double
     * @throws LoaderException  If some errors occur, eg some parameters
     *                          doesn't exists,...
     */
    public static double[] parseStringValues(String s, 
        HashMap<String, Double> parameters) throws LoaderException {
        String[] parts = s.split("[,]");
        double[] d = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            d[i] = NodeData.parseStringValue(parts[i], parameters);
        }

        return d;
    }

    public void refreshValue() {
        if (!dependsOnVariables(this))
            return;

        double[] d;
        try {
            d = parseStringValues(parameterS, 
                ws.getParameterSet());
        } catch (NumberFormatException | LoaderException e) {
            WorkSpace.showError("Definition became invalid due to some change",
                e);
            return;
        }
        setParameter(d);       
    }

    /**
     * @param df  A `DraggableFilter` instance
     * @return  Wether or not this instance should be updated after some 
     *          changes made to already existing variables.
     */
    private boolean dependsOnVariables(DraggableFilter df) {
        return !(df instanceof DInputFilter  ||
                 df instanceof DOutputFilter ||
                 df instanceof DAdditionFilter);
	}

	@Override
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        super.paint(g, back, color, zoom);
        for (FixedBall fb : inputs) {
            fb.paint(g, zoom);
        }
        for (FixedBall fb : outputs) {
            fb.paint(g, zoom);
        }

        // painting of the parameters of the filter if there are some
        if (getParameterType() == NONE) return;

        g.setColor(Color.BLACK);
        String[] parts = parameterS.split("[/]");
        String toDraw = parts[parts.length - 1];
        int width = g.getFontMetrics().stringWidth(toDraw);
        int height = g.getFontMetrics().getHeight();
        g.drawString(toDraw, x - width / 2, y - height / 2);
    }

    @Override
    public void updatePosition() {
        Point pos = ws.getMousePosition();
        if (xCorners == null) {
            System.out.println("The coordinates of the points have not been " +
                "set =(");
            return;
        }
        if (pos != null) {
            translateBox(-x, -y);
            x = (int) pos.getX() + a;   y = (int) pos.getY() + b;
            translateBox(x, y);
        }
    }

    @Override
    public void rotate() {
        super.rotate();
        for (int i = 0; i < inputs.length; i++) 
            inputs[i].translate(-x, -y);
        for (int i = 0; i < outputs.length; i++) 
            outputs[i].translate(-x, -y);
        

        for (int i = 0; i < inputs.length; i++) {
            int temp = inputs[i].getX();
            inputs[i].setX(-inputs[i].getY());
            inputs[i].setY(temp);
        }
        for (int i = 0; i < outputs.length; i++) {
            int temp = outputs[i].getX();
            outputs[i].setX(-outputs[i].getY());
            outputs[i].setY(temp);
        }
        
        for (int i = 0; i < inputs.length; i++) 
            inputs[i].translate(x, y);
        for (int i = 0; i < outputs.length; i++) 
            outputs[i].translate(x, y);
        
        
        ws.repaint();
    }

    @Override
    public void delete() {
        ws.delete(this);
        for (Wire w : inConnectedWires)
            ws.delete(w);
        for (Wire w : outConnectedWires)
            ws.delete(w);
        for (FixedBall fb : inputs)
            ws.delete(fb);
        for (FixedBall fb : outputs)
            ws.delete(fb);
    }

    public void addConnectedWire(Wire wire, boolean atInput) {
        if (atInput)
            inConnectedWires.add(wire);
        else
            outConnectedWires.add(wire);
    }

    /**
     * Gets the numeral associated to some input of the filter.
     * @param in    The `FixedBall` we want to get its input number
     * @return      The input number, -1 if `in` is not recognized.
     */
    public int numOfInput(FixedBall in) {
        return indexOf(inputs, in);
    }

    /**
     * Gets the numeral associated to some output of the filter.
     * @param out   The `FixedBall` we want to get its output number
     * @return      The output number, -1 if `out` is not recognized.
     */
    public int numOfOutput(FixedBall out) {
        return indexOf(outputs, out);
    }

    /**
     * @return      The right `WFilter` contained in this filter.
     */
    public WFilter filterR() {  return filterR;  }

    /**
     * @return      The left `WFilter` contained in this filter.
     */
    public WFilter filterL() {  return filterL;  }

    /**
     * Linear search for the index of a given element in an array.
     * @param array     The array we look in
     * @param o         The object we want the index in the array
     * @return          The index if `o` is present in `array`, else -1.
     */
    private int indexOf(Object[] array, Object o) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == o)
                return i;
        }
        return -1;
    }

    /**
     * @return      A `Vector<Wire>` containing the `Wire`s that are connected
     *              to an input of this filter.
     */
	public Vector<Wire> wireAtInputs() {
		return inConnectedWires;
    }
    
    /**
     * @return      A `Vector<Wire>` containing the `Wire`s that are connected
     *              to an output of this filter.
     */
	public Vector<Wire> wireAtOutputs() {
		return outConnectedWires;
	}
}