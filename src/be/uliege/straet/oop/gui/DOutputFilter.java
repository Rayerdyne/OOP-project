package be.uliege.straet.oop.gui;

import java.awt.Color;

public class DOutputFilter extends DraggableFilter {
    public static final int RADIUS = 34; 

    /**
     * Constructs a filter that represents an output
     * @param x             Initial x coordinate
     * @param y             Initial y coordinate
     * @param ws            The `WorkSpace` the filter belongs to
     * @param selected      Wether or not the filter is initally selected
     * @param oFileName     The name of the file in which the output will be 
     *                      written
     */
    public DOutputFilter(int x, int y, WorkSpace ws, boolean selected, 
        String oFileName) {
        super(x, y, ws, selected);

        xCorners = DInputFilter.xRegPolygon(6, RADIUS); 
        yCorners = DInputFilter.yRegPolygon(6, RADIUS);
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;       yCorners[i] += y;
        }

        inputs = new FixedBall[1];
        inputs[0] = new FixedBall(x - RADIUS, y, true, this);
        outputs = new FixedBall[0];

        ws.addInputs(inputs);

        color = Color.LIGHT_GRAY;
        parameterS = oFileName;
    }

    @Override
    public int getParameterType() {  return DraggableFilter.STRING;  }

    @Override
    public String getParameterInfo() {  return "output file name";  }

    @Override
    public void edit() {
        setParameter(getFilePathInput(false));
    }
    
}