package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Graphics;

import be.uliege.straet.oop.filters.IntegratorFilter;

public class DIntegratorFilter extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 30;
    public static final int WIDTH = 20;

    public static final int SYMBOL_HEIGHT = 20;
    public static final int SYMBOL_RADIUS = 8;

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param filter    A {@code IntegratorFilter} that will be used in that
     *                  filter
     */
    public DIntegratorFilter(int x, int y, WorkSpace ws, boolean selected,
        IntegratorFilter filter) {
        super(x, y, ws, selected);

        xCorners = new int[4];      yCorners = new int[4];
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

        color = Color.WHITE;

        filterR = filter;
        filterL = new IntegratorFilter(filter.getSamplingFrequency());
        parameterD = new double[] { filter.getSamplingFrequency() };
        parameterS = Double.toString(filter.getSamplingFrequency());
    }

    /**
     * Constructor with default {@code IntegratorFilter}.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DIntegratorFilter(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws, selected, new IntegratorFilter());
    }

    @Override
    public int getParameterType() {  return DraggableFilter.DOUBLE;  }

    @Override
    public String getParameterInfo() 
        {  return "sampling frequency in Hz";  }

    @Override
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        super.paint(g, back, fore, zoom);
        g.drawPolygon(xCorners, yCorners, xCorners.length);
        int sh = WorkSpace.zoom(SYMBOL_HEIGHT, zoom);
        int sr = WorkSpace.zoom(SYMBOL_RADIUS, zoom);
        g.drawLine(x, y - sh, x, y + sh);
        g.drawArc(x, y - sh - sr/2, sr, sr, 0, 180);
        g.drawArc(x - sr, y + sh - sr/2, sr, sr, 180, 180);
    }
    
    @Override
    public void setParameter(double[] d) {
        if (d.length > 1)
            return;
        parameterD = d;
        filterR = new IntegratorFilter(d[0]);
        filterL = new IntegratorFilter(d[0]);
    }
}