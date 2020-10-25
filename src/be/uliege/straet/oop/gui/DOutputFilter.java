package be.uliege.straet.oop.gui;

import java.awt.Color;

public class DOutputFilter extends DraggableFilter {
    public static final int RADIUS = 34; 

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
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
        selected = false;
        setParameter(getFilePathInput(false, "csv", "wav", "WAV"));
        ws.repaint();
    }
    
}