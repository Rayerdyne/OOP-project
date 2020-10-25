package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * <p>This class represents a ball that connects an input or an output to a 
 * given filter.</p>
 * <p>Input {@code FixedBall}s will be green, and output ones blue.</p>
 */
public class FixedBall implements MouseListener, Locatable {

    public static final Color INPUT_COLOR = Color.green;
    public static final Color OUTPUT_COLOR = Color.blue;

    private int x, y;
    /** the index of the {@code FixedBall} as an input or output */
    private final int index;
    private boolean isInput;
    private DraggableFilter owner;
    private boolean isConnected;
    private boolean isHighlited;
    private double prevZoom;

    /**
     * Constructor.
     * @param x         The x coordinate
     * @param y         The y coordinate
     * @param isInput   Wether or not the {@code FixedBall} is the input of a 
     *                  filter
     * @param owner     The `DraggableFilter that has this input-output
     * @param index     The index of the {@code FixedBall} as an input or 
     *                  output of the filter
     */
    public FixedBall(int x, int y, boolean isInput,
        DraggableFilter owner, int index) {
        this.x = x;
        this.y = y;
        this.isInput = isInput;
        this.owner = owner;
        this.prevZoom = 1.0;
        this.index = index;
        this.isHighlited = false;
    }

    /**
     * Constructs a {@code FixedBall}, with index as input or output of the 
     * filter 0.
     * @param x         The x coordinate
     * @param y         The y coordinate
     * @param isInput   Wether or not the {@code FixedBall} is the input of a 
     *                  filter
     * @param owner     The `DraggableFilter that has this input-output
     */
    public FixedBall(int x, int y, boolean isInput,
        DraggableFilter owner) {
        this(x, y, isInput, owner, 0);
    }

    /**
     * Translates this {@code FixedBall}.
     * @param x     The change in x coordinate
     * @param y     The change in y coordinate
     */
    public void translate(int x, int y) {
        this.x += x;
        this.y += y;
    }

    /**
     * Paints this {@code FixedBall} on a {@code Graphics} with zoom factor.
     * @param g         The {@code Graphics}
     * @param zoom      The zoom factor
     */
    public void paint(Graphics g, double zoom) {
        g.setColor(isInput ? INPUT_COLOR : OUTPUT_COLOR);
        int r = WorkSpace.zoom(FreeBall.BALL_RADIUS, zoom);
        if (zoom != prevZoom) {
            double z = zoom / prevZoom;
            x *= z;
            y *= z;
            prevZoom = zoom;
        }
        // we don't want connected inputs to be highlighted as they can't be 
        // used
        if (isHighlited && !(isConnected && isInput))
            g.fillOval(x - r/2, y - r/2, r, r);
        else
            g.drawOval(x - r/2, y - r/2, r, r);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point pos = e.getPoint();
        int dx = getX() - (int) pos.getX();
        int dy = getY() - (int) pos.getY();
        int r = FreeBall.BALL_RADIUS;
        if (dx * dx + dy * dy <= r * r && (!isConnected || !isInput)) {
            owner.ws.sendFixedBall(this);
            isConnected = true;
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    @Override public int getX() { return x; }
    @Override public int getY() { return y; }

    /**
     * Sets the x coordinate of this {@code FixedBall}.
     * @param x     The x coordinate
     */
    public void setX(int x) { this.x = x; }

    /**
     * Sets the y coordinate of this {@code FixedBall}.
     * @param y     The y coordinate
     */
    public void setY(int y) { this.y = y; }

    /**
     * @return      The {@code DraggableFilter} that owns this 
     *              {@code FixedBall}
     */
    public DraggableFilter owner() { return owner; }

    /**
     * @return      Wether or not this {@code FixedBall} is connected to at 
     *              least one {@code Wire}
     */
    public boolean isConnected() { return isConnected; }

    /**
     * @param connected     The state of the connection to set to this 
     *                     {@code FixedBall}
     */
    public void setConnected(boolean connected) { isConnected = connected; }

    /**
     * @return  Wether or not the {@code FixedBall} represents an input of the
     *          filter it belongs to
     */
    public boolean isInput() { return isInput; }
    /**
     * @return  Wether or not the {@code FixedBall} represents an output of the
     *          filter it belongs to
     */
    public int index() { return index; }

    /**
     * Sets the highlight policy of this {@code FixedBall}.
     * @param isHighlightied    Wether or not this {@code FixedBall} is 
     *                          highlighted
     */
    public void setHighlighted(boolean isHighlited) { 
        this.isHighlited = isHighlited;
    }
}