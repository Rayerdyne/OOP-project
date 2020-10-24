package be.uliege.straet.oop.gui;

/**
 * Exception that will be raised by `Computer` instances when they'll raise 
 * exception during their execution.
 */
public class ComputationException extends Exception {
   /**
     * To please the compiler...
     */
    private static final long serialVersionUID = 31415L;

    public ComputationException() {
        super();
    }

    public ComputationException(String s) {
        super(s);
    }
}