package be.uliege.straet.oop.gui;

import java.awt.Color;

import javax.swing.JOptionPane;

import be.uliege.straet.oop.loader.LoaderException;
import be.uliege.straet.oop.loader.Writer;

public class DVariableDeclaration extends DraggableFilter {
    public static final int RADIUS = 20;

    private String variableName;
    private double variableValue;
    private String variableDefinition;

    /**
     * Constructor.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     * @param varName   The name of the parameter the "filter" will hold
     * @param varDefinition     The definition of the parameter the "filter"
     *                          will hold
     */
    public DVariableDeclaration(int x, int y, WorkSpace ws, boolean selected,
        String varName, String varDefinition) {
        this(x, y, ws, selected);
        change(varName, varDefinition);
        } 

    /**
     * Constructor with undefined parameter name and definition.
     * @param x     The x coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param y     The y coordinate of the filter to place in the
     *              {@code WorkSpace}
     * @param ws    The {@code WorkSpace} it belongs to
     * @param selected  If true, the user is currently dragging this filter
     */
    public DVariableDeclaration(int x, int y, WorkSpace ws, boolean selected) {
        super(x, y, ws, selected);
        
        xCorners = DInputFilter.xRegPolygon(8, RADIUS);
        yCorners = DInputFilter.yRegPolygon(8, RADIUS);
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x;
            yCorners[i] += y;
        }

        inputs = new FixedBall[0];
        outputs = new FixedBall[0];

        color = Color.CYAN;
        parameterS = "Future name of variable";
        parameterD = new double[] { 0.0 };
    }

    @Override
    public int getParameterType() { 
        return DraggableFilter.VARIABLE_DECLARATION; }

    @Override
    public String getParameterInfo() { 
        return "name and value of the variable"; }
    
    @Override
    public void edit() {
        selected = false;
        String newVarName = JOptionPane.showInputDialog(
            "Enter the variable's name.");
        String newVarDefinition = JOptionPane.showInputDialog(
            "Enter the variable's value.");

        if (newVarName == null || newVarDefinition == null) {
            WorkSpace.showError("Either name or definition is empty.", null);
            return;
        }

        if (newVarName.equals(Writer.X_COORD_ATTR_NAME) || 
            newVarName.equals(Writer.Y_COORD_ATTR_NAME) || 
            newVarName.equals(Writer.ORIENTATION_ATTR_NAME)) {
            WorkSpace.showError("A variable can not take the names \"x\" or " +
                "\"y\"or \"orientation\" because of name collision.", null);
            return;
            }

        change(newVarName, newVarDefinition);
        ws.repaint();
    }

    /**
     * Edits this parameter, and triggers refresh on all parameters already
     * set.
     * @param newVarName
     * @param newVarDefinition
     */
    private void change(String newVarName, String newVarDefinition) {
        double d[];
        try {
            d = DraggableFilter.parseStringValues(newVarDefinition, 
                ws.getParameterSet());
            variableValue = d[0];
        } catch (LoaderException e) {
            WorkSpace.showError("Invalid input (" + newVarDefinition + 
                ") to define \"" + newVarName + "\".\nParameter set: " +
                 ws.getParameterSet().toString() + "\nCancelling.", null);
        }

        variableName = newVarName;
        variableDefinition = newVarDefinition;
        ws.refreshVariablesValues();

        parameterS = variableName + " = " + variableDefinition + 
                                    " (" + variableValue + ")";
    }

    /**
     * @return      The name of the parameter held in this "filter"
     */
    public String getVariableName() { return variableName; }
    /**
     * @return      The value of the parameter held in this "filter"
     */
    public double getVariableValue() { return refreshVariableValue(); }
    @Override
    public String getParameterDefinition() {  return variableDefinition; }

    /**
     * Refreshes the value of the variable (if some other variable in its 
     * definition have changed), and returns the new value;
     * @return  The value of the variable held in this.
     */
    public double refreshVariableValue() { 
        double d[];
        try {
            d = DraggableFilter.parseStringValues(variableDefinition,
                 ws.getParameterSet());
                 variableValue = d[0];
        } catch (LoaderException e) {
            WorkSpace.showError("Invalid input (" + variableDefinition + 
                ") for variable \"" + variableName +  "\" due to some " +
                "changes.\nParameter set: " + ws.getParameterSet().toString() +
                "\nCancelling refresh.", null);
        }

        parameterS = variableName + " = " + variableDefinition + 
                                    " (" + variableValue + ")";
        return variableValue;
    }

    @Override
    public void refreshValue() {
        refreshVariableValue();
    }
}