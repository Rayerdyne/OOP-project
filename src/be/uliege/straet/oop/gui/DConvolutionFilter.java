package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import be.uliege.straet.oop.filters.ConvolutionFilter;
import be.uliege.straet.oop.loader.LoaderException;

public class DConvolutionFilter extends DraggableFilter {

    public static final int MAX_CHARS_TO_DRAW = 20;
    
    /** semi-height, actually */
    public static final int HEIGHT = 26;
    public static final int WIDTH = 52; 

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param filter A {@code ConvolutionFilter} that will be placed in this filter
     */
    public DConvolutionFilter(int x, int y, WorkSpace ws, boolean selected,
        ConvolutionFilter filter) {
        super(x, y, ws, selected);

        xCorners = new int[4];  yCorners = new int[4];
        xCorners[0] = -WIDTH;   yCorners[0] = -HEIGHT;
        xCorners[1] = WIDTH;    yCorners[1] = -HEIGHT;
        xCorners[2] = WIDTH;    yCorners[2] = HEIGHT;
        xCorners[3] = -WIDTH;   yCorners[3] = HEIGHT;
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;       yCorners[i] += y;
        }

        inputs = new FixedBall[1]; 
        outputs = new FixedBall[1];
        inputs[0] = new FixedBall(x - WIDTH, y, true, this);
        outputs[0] = new FixedBall(x + WIDTH, y, false, this);

        ws.addInputs(inputs);
        ws.addOutputs(outputs);

        color = Color.PINK;

        filterR = filter;
        filterL = new ConvolutionFilter(filter.getVector());
        parameterD = filter.getVector();
        parameterS = "";
        for (int i = 0; i < parameterD.length; i++ ) {
            parameterS += Double.toString(parameterD[i]) + ", ";
        }
        // remove ending ", "
        parameterS = parameterS.substring(0, parameterS.length() - 2);
    }

    /**
     * Constructor with default {@code ConvolutionFilter}.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DConvolutionFilter(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws, selected, new ConvolutionFilter());
    }

    @Override
    public int getParameterType() {  return DraggableFilter.DOUBLE;  }

    @Override
    public String getParameterInfo() 
        {  return "vector, values sepated by a ','";  }

    @Override
    public void setParameter(double[] d) {
        parameterD = d;
        filterR = new ConvolutionFilter(d);
        filterL = new ConvolutionFilter(d);
    }

    @Override 
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        String save = parameterS;
        if (parameterS.endsWith(".csv")) {
            String[] parts = parameterS.split("[/]");
            parameterS = parts[parts.length-1];
        }
        else 
            parameterS = parameterS.length() > MAX_CHARS_TO_DRAW ?
                parameterS = parameterS.substring(0, MAX_CHARS_TO_DRAW) +"...":
                parameterS;
        super.paint(g, back, fore, zoom);
        parameterS = save;
    }

    @Override
    public void edit() {
        selected = false;
        
        int res = JOptionPane.showConfirmDialog(null, "Is vector in a csv " +
            "file ?", "Make your choice", JOptionPane.YES_NO_OPTION);

        String oldValue = parameterS;
        // Load csv file...
        if (res == JOptionPane.YES_OPTION) {
            byte[] encoded;
			try {
                parameterS = getFilePathInput(false, "csv");
                if (parameterS == null) {
                    parameterS = oldValue;
                    return;
                }
				encoded = Files.readAllBytes(Path.of(parameterS));
			} catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not read file: " +
                    e.getMessage());
                parameterS = oldValue;
                return;
			}
            String contents = new String(encoded, StandardCharsets.US_ASCII);
            String[] values = contents.split("[,]");
            int nb = values[values.length-1] == null ? 
                        values.length - 1 : values.length;
            double d[] = new double[nb];
            
            for (int i = 0; i < nb; i++) 
                d[i] = Double.valueOf(values[i].trim());
            
            setParameter(d);
        }
        // or parse input...
        else {
            parameterS = getParameterInput();
            if (parameterS == null || parameterS.isEmpty()) {
                parameterS = oldValue;
                return;
            }
            double[] d;
            try {
                d = parseStringValues(parameterS, ws.getParameterSet());
            } catch (NumberFormatException | LoaderException e) {
                JOptionPane.showMessageDialog(null, "Invalid input: " + 
                    e.getMessage());
                parameterS = oldValue;
                return;
            }
            setParameter(d);
        }
        ws.repaint();
    }
}
