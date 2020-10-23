package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Graphics;

import be.uliege.straet.oop.filters.NoiseGenerator;

public class DNoiseGenerator extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 30;
    public static final int WIDTH = 20;

    public static final int GAP = 0;
    public static final int RADIUS = 10;

    public DNoiseGenerator(int x, int y, WorkSpace ws, boolean selected,
        NoiseGenerator filter) {
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
        filterL = new NoiseGenerator(filter.getAmplitude());
        parameterD = new double[] { filter.getAmplitude() };
        parameterS = Double.toString(parameterD[0]);
    }

    public DNoiseGenerator(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws, selected, new NoiseGenerator());
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

        int gap = WorkSpace.zoom(GAP, zoom);
        int r = WorkSpace.zoom(RADIUS, zoom);
        int x0 = x;
        int y0 = y + gap;

        g.drawLine(x0, y0, x0, y0);
        g.drawLine(x0+r/2, y0, x0+r/2, y0);
        g.drawLine(x0+r/3, y0+3, x0+r/3, y0+3);
        g.drawLine(x0-r/2, y0-r/4, x0-r/2, y0-r/4);
        g.drawLine(x0, y0+3, x0, y0+3);
        g.drawLine(x0-2, y0, x0-2, y0);
        g.drawLine(x0-r/3, y0+r, x0-r/3, y0+r);
        g.drawLine(x0-r/3+3, y0+r-4, x0-r/3+4, y0+r-5);
    }
    
    @Override
    public void setParameter(double[] d) {
        if (d.length > 1)
            return;
        parameterD = d;
        filterR = new NoiseGenerator(d[0]);
        filterL = new NoiseGenerator(d[0]);
    }
}