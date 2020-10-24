package be.uliege.straet.oop.gui;

import java.awt.Color;

import be.uliege.straet.oop.filters.GainFilter;

public class DGainFilter extends DraggableFilter {
    /** semi-height, actually */
    public static final int HEIGHT = 21;
    public static final int WIDTH = 34; 

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The `WorkSpace` it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param filter A `GainFilter` that will be used in that filter
     */
    public DGainFilter(int x, int y, WorkSpace ws, boolean selected, 
        GainFilter filter) {
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

        color = Color.ORANGE;

        filterR = filter;
        filterL = new GainFilter(filter.getGain());
        parameterD = new double[] { GainFilter.DEF_GAIN };
        parameterS = Double.toString(GainFilter.DEF_GAIN);
    }

    /**
     * Constructor with default `GainFilter`.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The `WorkSpace` it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DGainFilter(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws, selected, new GainFilter());
    }

    @Override
    public int getParameterType() {  return DraggableFilter.DOUBLE;  }

    @Override
    public String getParameterInfo() 
        {  return "gain (in units)";  }
    
    @Override 
    public void setParameter(double[] d) {
        if (d.length > 1)
            return;
        parameterD = d;
        filterR = new GainFilter(d[0]);
        filterL = new GainFilter(d[0]);
    }
}