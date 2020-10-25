package be.uliege.straet.oop.gui;

import javax.swing.JOptionPane;

import java.awt.Color;

/**
 * <p>Part of Unrelated thing inside this project xD.</p>
 * <p>This class is a checkpoint of the drawing, with the ability so set the
 * interpolating method (linear or cubic) for the next points.</p>
 */
public class NothingFilter extends DraggableFilter {

    private static double nTimeStamp = 0.0;
    private boolean nextInterpMethodIsLin = true; 
    private double timeStamp;

    /**
     * Constructor.
     * @param ws        The {@code WorkSpace} this {@code NothingFilter} 
     *                  belongs to
     * @param nextInterpMethodIsLin If true, next interpolation method is 
     *                              linear, else it's cubic
     */
    public NothingFilter(WorkSpace ws, boolean nextInterpMethodIsLin) {
        this(0, 0, ws, nextInterpMethodIsLin);
    }

    /**
     * Constructor.
     * @param x         The x coordinate of this {@code NothingFilter}
     * @param y         The y coordinate of this {@code NothingFilter}
     * @param ws        The {@code WorkSpace} this belongs to
     * @param nextInterpMethodIsLin If true, next interpolation method is 
     *                              linear, else it's cubic
     */
    public NothingFilter(int x, int y, WorkSpace ws, 
        boolean nextInterpMethodIsLin) {
        super(x, y, ws, true);

        xCorners = new int[4];      yCorners = new int[4];
        xCorners[0] = -10;          yCorners[0] = 5;
        xCorners[1] = 10;           yCorners[1] = 5;
        xCorners[2] = 10;           yCorners[2] = -5;
        xCorners[3] = -10;          yCorners[3] = -5;
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;       yCorners[i] += y;
        }
        inputs = new FixedBall[1]; 
        outputs = new FixedBall[1];
        inputs[0] = new FixedBall(x - 10, y, true, this);
        outputs[0] = new FixedBall(x + 10, y, false, this);

        ws.addInputs(inputs);
        ws.addOutputs(outputs);

        this.nextInterpMethodIsLin = nextInterpMethodIsLin;
        color = nextInterpMethodIsLin ? Color.BLUE : Color.RED;
        this.timeStamp = nTimeStamp;
        nTimeStamp++;
        parameterS = Double.toString(timeStamp);

        selected = true;
    }

    /**
     * @return      Wether or not the next interpolation method is linear
     */
    public boolean getNextInterpMethodIsLin() {
        return nextInterpMethodIsLin;
    }

    /**
     * @return      The time stamp of this {@code NothingFilter}, i.e. the time
     *              at wich the drawing will at this point
     */
    public double getTimeStamp() {
        return timeStamp;
    }

    @Override
    public void edit() {
        int res = JOptionPane.showConfirmDialog(null, "Is a linear " +
            "interpolation method used after ?", "Make your choice", 
            JOptionPane.YES_NO_OPTION);

        nextInterpMethodIsLin = res == JOptionPane.YES_OPTION;

        parameterS = JOptionPane.showInputDialog("Enter timestamp: ");
        if (parameterS == null)
            return;
        
        try {
            timeStamp = Double.parseDouble(parameterS);
        } catch (Exception e) {
            System.out.println("Could not parse time stamp " + parameterS + 
                ": " + e.getMessage());
        }

        return;
    }

    @Override
    public int getParameterType() {  return DraggableFilter.STRING;  }
    
}
