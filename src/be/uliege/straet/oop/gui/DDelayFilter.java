package be.uliege.straet.oop.gui;

import java.awt.Color;

import be.uliege.straet.oop.filters.DelayFilter;

public class DDelayFilter extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 26;
    public static final int WIDTH = 26; 

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

        filterR = filter;
        filterL = new DelayFilter(filter.getShift());
        parameterD = new double[] { DelayFilter.DEF_SHIFT };
        parameterS = Integer.toString(DelayFilter.DEF_SHIFT);
    }

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