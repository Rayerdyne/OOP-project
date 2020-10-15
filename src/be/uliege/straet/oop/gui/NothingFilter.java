package be.uliege.straet.oop.gui;

import javax.swing.JOptionPane;

import java.awt.Color;

/**
 * Part of Unrelated thing inside this project
 */
public class NothingFilter extends DraggableFilter {

    private static double nTimeStamp = 0.0;
    private boolean nextInterpMethodIsLin = true; 
    private double timeStamp;

    public NothingFilter(WorkSpace ws, boolean nextInterpMethodIsLin) {
        this(0, 0, ws, nextInterpMethodIsLin);
    }

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

    public boolean getNextInterpMethodIsLin() {
        return nextInterpMethodIsLin;
    }

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
