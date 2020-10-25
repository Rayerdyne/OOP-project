package be.uliege.straet.oop.gui;

/**
 * Exception type that will be raised if something goes wrong in the window.
 */
public class WindowException extends Exception {
   /**
     * To please the compiler...
     */
    private static final long serialVersionUID = 31415L;

    public WindowException() {
        super();
    }

    public WindowException(String s) {
        super(s);
    }
}