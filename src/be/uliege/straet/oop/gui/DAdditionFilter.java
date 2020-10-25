package be.uliege.straet.oop.gui;

import java.awt.Color;

import be.uliege.straet.oop.filters.AdditionFilter;

/**
 * Represents graphically an addition filter.
 * 'D' stands for "Draggable"
 */
public class DAdditionFilter extends DraggableFilter {

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the `WorkSpace`
     * @param y     The y coordinate of the filter to place in the `WorkSpace`
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DAdditionFilter(int x, int y, WorkSpace ws, boolean selected) {
        super(x, y, ws, selected);

        xCorners = new int[3];      yCorners = new int[3];
        xCorners[0] = 0;            yCorners[0] = -40;
        xCorners[1] = 0;            yCorners[1] = 40;
        xCorners[2] = 50;           yCorners[2] = 0;
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;       yCorners[i] += y;
        }

        inputs = new FixedBall[2]; 
        outputs = new FixedBall[1];
        inputs[0] = new FixedBall(x, y + 30, true, this);
        inputs[1] = new FixedBall(x, y - 30, true, this, 1);
        outputs[0] = new FixedBall(x + 50, y, false, this);

        ws.addInputs(inputs);
        ws.addOutputs(outputs);

        color = Color.BLUE;

        filterR = new AdditionFilter();
        filterL = new AdditionFilter();
    }
    
}