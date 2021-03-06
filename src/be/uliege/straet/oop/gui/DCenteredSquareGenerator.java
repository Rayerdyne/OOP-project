package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Graphics;

import be.uliege.straet.oop.filters.CenteredSquareGenerator;

public class DCenteredSquareGenerator extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 30;
    public static final int WIDTH = 20;

    public static final int SQUARE_SIZE = 8;
    public static final int GAP = 10;

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param filter    A {@code CenteredSquareGenerator} that will be used in
     *                  that filter
     */
    public DCenteredSquareGenerator(int x, int y, WorkSpace ws, 
        boolean selected, CenteredSquareGenerator filter) {
        super(x, y, ws, selected);

        xCorners = new int[4];      yCorners = new int[4];
        xCorners[0] = -WIDTH;   yCorners[0] = -HEIGHT;
        xCorners[1] = WIDTH;    yCorners[1] = -HEIGHT;
        xCorners[2] = WIDTH;    yCorners[2] = HEIGHT;
        xCorners[3] = -WIDTH;   yCorners[3] = HEIGHT;
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;       yCorners[i] += y;
        }

        inputs = new FixedBall[0]; 
        outputs = new FixedBall[1];
        outputs[0] = new FixedBall(x + WIDTH, y, false, this);

        ws.addInputs(inputs);
        ws.addOutputs(outputs);

        color = Color.WHITE;

        filterR = filter;
        filterL = new CenteredSquareGenerator(filter.getFrequency(), 
            filter.getAmplitude(), filter.getSamplingFrequency());
        parameterD = new double[] { 
            filter.getFrequency(), filter.getAmplitude(),
            filter.getSamplingFrequency() };
        parameterS = Double.toString(parameterD[0]) + ", " +
                     Double.toString(parameterD[1]) + ", " +
                     Double.toString(parameterD[2]);
    }

    /**
     * Constructor with default {@code CenteredSquareGenerator}.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DCenteredSquareGenerator(int x, int y, WorkSpace ws, 
        boolean selected) {
        this(x, y, ws, selected, new CenteredSquareGenerator());
    }

    @Override
    public int getParameterType() {  return DraggableFilter.MULTIPLE_DOUBLE;  }

    @Override
    public String getParameterInfo() 
        {  return "frequency, amplitude [and sampling frequency] (in order, " +
                  "separated by commas)";  }

    @Override
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        super.paint(g, back, fore, zoom);
        g.setColor(Color.BLUE);
        g.drawPolygon(xCorners, yCorners, xCorners.length);
        int ss = WorkSpace.zoom(SQUARE_SIZE, zoom);
        int gap = WorkSpace.zoom(GAP, zoom);
        int x0 = x - 3*ss/2;
        int y0 = y + gap + ss/2;
        g.drawPolyline(new int[] { x0, x0       , x0 + ss  , x0 + ss  , 
            x0 + 2*ss, x0 + 2*ss, x0 + 3*ss, x0 + 3*ss},
                       new int[] { y0, y0 + ss/2, y0 + ss/2, y0 - ss/2,
            y0 - ss/2, y0 + ss/2, y0 + ss/2, y0 }, 8);
    }
    
    @Override
    public void setParameter(double[] d) {
        double[] e = new double[3];
        e[0] = d[0];    e[1] = d[1];
        e[2] = d.length >= 3 ? d[2] : 
                               CenteredSquareGenerator.DEF_SAMPLING_FREQUENCY;
        parameterD = e;
        filterR = new CenteredSquareGenerator(e[0], e[1], e[2]);
        filterL = new CenteredSquareGenerator(e[0], e[1], e[2]);
    }
}