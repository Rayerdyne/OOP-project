package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import be.uliege.montefiore.oop.audio.FilterException;
import be.uliege.straet.oop.filters.CompositeFilter;
import be.uliege.straet.oop.filters.WFilter;
import be.uliege.straet.oop.loader.Loader;
import be.uliege.straet.oop.loader.LoaderException;

public class DCompositeFilter extends DraggableFilter {

    /** semi-height, actually */
    public static final int HEIGHT = 26;
    public static final int WIDTH = 52; 

    /**
     * Constructs a `DCompositeFilter`
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param ws    The `WorkSpace` the filter belongs to
     * @param selected  Wether or not the filter is selected and dragged ad the
     *                  moment of the instanciation.
     * @param fileName  The name of the file representing the `CompositeFilter`
     * @throws FilterException              If an error occured when 
     *                                      instanciating and connecting filter
     * @throws LoaderException              If an error occured when loading 
     *                                      the file
     * @throws ParserConfigurationException If an error occurs when reading and
     *                                      parsing the input file
     * @throws SAXException                 Idem
     * @throws IOException                  Idem
     * @throws DOMException                 Idem
     */
    public DCompositeFilter(int x, int y, WorkSpace ws, boolean selected,
        String fileName) throws LoaderException, DOMException, FilterException,
        ParserConfigurationException, SAXException, IOException {

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

        if (fileName == null) {
            filterR = new CompositeFilter();
            filterL = new CompositeFilter();
            return;
        }

        HashMap<String, Double> parameterSet = ws.getParameterSet();
        filterR = Loader.load(fileName, parameterSet, false);
        filterL = Loader.load(fileName, parameterSet, false);

        parameterS = fileName;
    }

    /**
     * Constructs a `DCompositeFilter`, with initially invalid 
     * `CompositeFilter` in it
     * @param x     The x coordinate of the filter
     * @param y     The y coordinate of the filter
     * @param ws    The `WorkSpace` the filter belongs to
     * @param selected  Wether or not the filter is selected and dragged ad the
     *                  moment of the instanciation.
     * @param fileName  The name of the file representing the `CompositeFilter`
     * @throws FilterException              If an error occured when 
     *                                      instanciating and connecting filter
     * @throws LoaderException              If an error occured when loading 
     *                                      the file
     * @throws ParserConfigurationException If an error occurs when reading and
     *                                      parsing the input file
     * @throws SAXException                 Idem
     * @throws IOException                  Idem
     * @throws DOMException                 Idem
     */
    public DCompositeFilter(int x, int y, WorkSpace ws, boolean selected) 
        throws LoaderException, DOMException, FilterException, 
        ParserConfigurationException, SAXException, IOException {
        this(x, y, ws, selected, null);
    }

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
        String oldParameterS = parameterS;
        WFilter oldR = filterR;
        parameterS = s;
        try {
            HashMap<String, Double> parameterSet = ws.getParameterSet();
            filterR = Loader.load(parameterS, parameterSet, false);
            if (filterR.nbInputs() != 1 || filterR.nbOutputs() != 1) {
                filterR = oldR;
                parameterS = oldParameterS;
                WorkSpace.showError("Specified file describes a filter that " +
                    "doesn't have one input and one output.", null);
                return;
            }
            filterL = Loader.load(parameterS, parameterSet, false);
        } catch (Exception e) {
            WorkSpace.showError("Could not load file \"" + parameterS + "\"", e);
        }
    }
    
}