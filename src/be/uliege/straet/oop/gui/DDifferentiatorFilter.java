package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Graphics;

import be.uliege.straet.oop.filters.DifferentiatorFilter;

public class DDifferentiatorFilter extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 30;
    public static final int WIDTH = 20;

    public DDifferentiatorFilter(int x, int y, WorkSpace ws, boolean selected,
        DifferentiatorFilter filter) {
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
        filterL = new DifferentiatorFilter(filter.getSamplingFrequency());
        parameterD = new double[] { filter.getSamplingFrequency() };
        parameterS = Double.toString(filter.getSamplingFrequency());
    }

    public DDifferentiatorFilter(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws, selected, new DifferentiatorFilter());
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
        int width = g.getFontMetrics().stringWidth("d/dt");
        int height = g.getFontMetrics().getHeight();
        g.drawString("d/dt", x - width / 2, y + height / 2);
    }
    
    @Override
    public void setParameter(double[] d) {
        if (d.length > 1)
            return;
        parameterD = d;
        filterR = new DifferentiatorFilter(d[0]);
        filterL = new DifferentiatorFilter(d[0]);
    }
}