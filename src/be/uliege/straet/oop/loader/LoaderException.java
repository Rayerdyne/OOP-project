package be.uliege.straet.oop.loader;

import be.uliege.montefiore.oop.audio.FilterException;

/** INFO0062 - Object-Oriented Programming
 *  Project.
 * 
 * ADDITIONNAL
 * 
 * Exception that will be thrown when something goes wrong during the 
 * construction of a composite filter based on a file.
 * 
 * François Straet
 */
public class LoaderException extends FilterException {

    /**
     * To please warnings
     */
    private static final long serialVersionUID = 1618L;

    public LoaderException() {
        super();
    }

    public LoaderException(String s) {
        super(s);
    }
}
