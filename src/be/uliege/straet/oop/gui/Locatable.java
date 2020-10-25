package be.uliege.straet.oop.gui;

/**
 * Basic interface that specifies getX() and getY() method to locate an object.
 */
public interface Locatable {
    /**
     * @return      The x coordinate of the `Object`
     */
    public int getX();
    /**
     * @return      The y coordinate of the `Object`
     */
    public int getY();    
}