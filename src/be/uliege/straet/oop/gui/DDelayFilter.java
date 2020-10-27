package be.uliege.straet.oop.gui;

import java.awt.Color;

import be.uliege.straet.oop.filters.DelayFilter;

public class DDelayFilter extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 26;
    public static final int WIDTH = 26; 

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param filter A {@code DelayFilter} that will be used in that filter
     */
    public DDelayFilter(int x, int y, WorkSpace ws, boolean selected,
        DelayFilter filter) {
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

        color = Color.GRAY;

        int shift = filter.getShift();
        filterR = filter;
        filterL = new DelayFilter(shift);
        parameterD = new double[] { shift };
        parameterS = Integer.toString(shift);
    }

    /**
     * Constructor with default {@code DelayFilter}.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DDelayFilter(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws, selected, new DelayFilter());
    }

    @Override
    public int getParameterType() {  return DraggableFilter.DOUBLE;  }

    @Override
    public String getParameterInfo() 
        {  return "delay (in number of samples)";  }

    @Override
    public void setParameter(double[] d) {
        if (d.length > 1)
            return;
        parameterD = d;
        filterR = new DelayFilter((int) Math.round(d[0]));
        filterL = new DelayFilter((int) Math.round(d[0]));
    }
    
}