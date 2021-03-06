package be.uliege.straet.oop.loader;

/**
 * Exception that will be raised when something goes wrong during the 
 * process of writing a composite filter in a file.
 */
public class WriterException extends Exception {
    /**
     * To please warnings
     */
    private static final long serialVersionUID = 1618L;

    public WriterException() {
        super();
    }

    public WriterException(String s) {
        super(s);
    }
}