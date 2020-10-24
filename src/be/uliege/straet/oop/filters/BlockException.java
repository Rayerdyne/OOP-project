
package be.uliege.straet.oop.filters;

import be.uliege.montefiore.oop.audio.FilterException;

/** 
 * Exception that will be thrown when something goes wrong in blocks.
 */
public class BlockException extends FilterException {
    /**
     * To please the compiler...
     */
    private static final long serialVersionUID = 31415L;

    public BlockException() {
        super();
    }

    public BlockException(String s) {
        super(s);
    }
}
