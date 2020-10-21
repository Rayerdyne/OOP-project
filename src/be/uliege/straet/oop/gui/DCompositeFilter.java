package be.uliege.straet.oop.gui;

import java.awt.Color;

import be.uliege.straet.oop.loader.Loader;

public class DCompositeFilter extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 26;
    public static final int WIDTH = 52; 

    public DCompositeFilter(int x, int y, WorkSpace ws, boolean selected,
        String fileName) {
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

        color = Color.ORANGE;

        // cannot have default values...
        try {
            filterR = Loader.load(fileName, null, false);
            filterL = Loader.load(fileName, null, false);
        } catch (Exception e) {
            WorkSpace.showError("Could not load file \"" + fileName + "\"", e);
        }

        parameterS = fileName;
    }

    // public DCompositeFilter(int x, int y, WorkSpace ws, boolean selected) {
    //     this(x, y, ws, selected, new DelayFilter());
    // }

    @Override
    public int getParameterType() {  return DraggableFilter.FILE_PATH;  }

    @Override
    public String getParameterInfo() 
        {  return "input file.";  }

    
    @Override
    public void edit() {
        selected = false;
        setParameter(getFilePathInput(false, "xml"));
        ws.repaint();
    }

    @Override
    public void setParameter(String s) {
        if (s ==  null)
            return;
        parameterS = s;
        try {
            filterR = Loader.load(parameterS, null, false);
            filterL = Loader.load(parameterS, null, false);
        } catch (Exception e) {
            WorkSpace.showError("Could not load file \"" + parameterS + "\"", e);
        }
    }
    
}