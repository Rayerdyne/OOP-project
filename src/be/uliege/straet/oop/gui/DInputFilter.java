package be.uliege.straet.oop.gui;

import java.awt.Color;

import be.uliege.montefiore.oop.audio.AudioSequenceException;

public class DInputFilter extends DraggableFilter {
    public static final int RADIUS = 34;

    private AudioSequence2 audioSequence;

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param iFileName     The name of the file in which the input will be 
     *                      read 
     */
    public DInputFilter(int x, int y, WorkSpace ws, boolean selected, 
        String iFileName) {
        super(x, y, ws, selected);

        xCorners = xRegPolygon(6, RADIUS);
        yCorners = yRegPolygon(6, RADIUS);
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;
            yCorners[i] += y;
        }

        outputs = new FixedBall[1];
        outputs[0] = new FixedBall(x + RADIUS, y, false, this);
        inputs = new FixedBall[0];

        ws.addOutputs(outputs);

        color = Color.DARK_GRAY;
        parameterS = iFileName;
    }

    /**
     * Loads the audio sequence based on the file name given in
     * {@code parameterS}
     * @throws AudioSequenceException   If the sequence could not be loaded
     *  (e.g., I/O error)
     */
    public void loadAudioSequence() 
        throws AudioSequenceException {
        audioSequence = new AudioSequence2(parameterS);
    }

    /**
     * @return  The {@code AudioSequence2} represented by this input filter
     */
    public AudioSequence2 getAudioSequence() {  return audioSequence;  }

    /**
     * x coordinates of vertices of regular polygon.
     * @param n     The number of sides of the plolygon
     * @param r     The radius of the polygon
     * @return      An array of int containing the x coordinates of a regular
     *              polygon of radius r with n sides
     */
    public static int[] xRegPolygon(int n, int r) {
        int[] x = new int[n];
        for (int i = 0; i < n; i++) {
            double theta = ((double) i / (double) n) * 2.0 * Math.PI;
            x[i] = (int) ((double) r * Math.cos(theta));
        }
        return x;
    }
    /**
     * y coordinates of vertices of regular polygon.
     * @param n     The number of sides of the plolygon
     * @param r     The radius of the polygon
     * @return      An array of int containing the y coordinates of a regular
     *              polygon of radius r with n sides
     */
    public static int[] yRegPolygon(int n, int r) {
        int[] y = new int[n];
        for (int i = 0; i < n; i++) {
            double theta = ((double) i / (double) n) * 2.0 * Math.PI;
            y[i] = (int) ((double) r * Math.sin(theta));
        }
        return y;
    }

    @Override
    public int getParameterType() { return DraggableFilter.STRING;  }

    @Override 
    public String getParameterInfo() {  return "input file name.";  }

    @Override
    public void edit() {
        selected = false;
        setParameter(getFilePathInput(true, "csv", "wav", "WAV"));
        ws.repaint();
    }
}