package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Graphics;

import be.uliege.straet.oop.filters.ConvolutionFilter;

public class DConvolutionFilter extends DraggableFilter {

    public static final int MAX_CHARS_TO_DRAW = 20;
    
    /** semi-height, actually */
    public static final int HEIGHT = 26;
    public static final int WIDTH = 52; 

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
        parameterS = parameterS.length() > MAX_CHARS_TO_DRAW ?
            parameterS = parameterS.substring(0, MAX_CHARS_TO_DRAW) + "..." :
            parameterS;
        super.paint(g, back, fore, zoom);
        parameterS = save;
    }
}
