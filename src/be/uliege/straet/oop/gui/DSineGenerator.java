package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Graphics;

import be.uliege.straet.oop.filters.SineGenerator;

public class DSineGenerator extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 30;
    public static final int WIDTH = 20;

    public static final int ARC_RADIUS = 8;
    public static final int GAP = 0;

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The `WorkSpace` it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param filter A `DSineGenerator` that will be used in that filter
     */
    public DSineGenerator(int x, int y, WorkSpace ws, boolean selected,
        SineGenerator filter) {
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
        filterL = new SineGenerator(filter.getFrequency(), 
            filter.getAmplitude(), filter.getSamplingFrequency());
        parameterD = new double[] { 
            filter.getFrequency(), filter.getAmplitude(),
            filter.getSamplingFrequency() };
        parameterS = Double.toString(parameterD[0]) + ", " +
                     Double.toString(parameterD[1]) + ", " +
                     Double.toString(parameterD[2]);

    }

    /**
     * Constructor with default `SineGenerator`.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The `WorkSpace` it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DSineGenerator(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws, selected, new SineGenerator());
    }

    @Override
    public int getParameterType() {  return DraggableFilter.MULTIPLE_DOUBLE;  }

    @Override
    public String getParameterInfo() 
        {  return "frequency, amplitude and sampling frequency (in order)";  }

    @Override
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        super.paint(g, back, fore, zoom);
        g.setColor(Color.BLUE);
        g.drawPolygon(xCorners, yCorners, xCorners.length);
        int ar = WorkSpace.zoom(ARC_RADIUS, zoom);
        int gap = WorkSpace.zoom(GAP, zoom);
        g.drawArc(x - ar, y + gap + ar/2, ar, ar, 0, 180);
        g.drawArc(x, y + gap + ar/2, ar, ar, 180, 180);
    }
    
    @Override
    public void setParameter(double[] d) {
        parameterD = d;
        filterR = new SineGenerator(d[0], d[1], d[2]);
        filterL = new SineGenerator(d[0], d[1], d[2]);
    }
}