package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;

/**
 * This class represents a draggable ball that will guide, "checkpoints" the 
 * wires between the different connection between filters.
 */
public class FreeBall extends Draggable {

    public static int BALL_RADIUS = 20;
    private Wire wire;

    /**
     * Constructor of a {@code FreeBall} at coordinates (0, 0).
     * @param ws        The {@code WorkSpace} this {@code FreeBall} belongs to
     * @param wire      The {@code Wire} this {@code FreeBall} is a checkpoint 
     *                  of
     */
    public FreeBall(WorkSpace ws, Wire wire) {
        this(0, 0, ws, wire);
    }

    /**
     * Constructor of a {@code FreeBall} at coordinates (x, y).
     * @param x         The x coordinate of the{@code FreeBall}
     * @param y         The y coordinate of the{@code FreeBall}
     * @param ws        The {@code WorkSpace} this {@code FreeBall} belongs to
     * @param wire      The {@code Wire} this {@code FreeBall} is a checkpoint
     *                  of
     */
    public FreeBall(int x, int y, WorkSpace ws, Wire wire) {
        super(x, y, ws);
        this.wire = wire;
    }

    /**
     * @return      The {@code Wire} this {@code FreeBall} belongs to
     */
    public Wire wire() { return wire; }

    @Override
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        if (selected)
            updatePosition();
        g.setColor(fore);
        int r = WorkSpace.zoom(BALL_RADIUS, zoom);
        g.fillOval((int) x - r/2, (int) y - r/2, r, r);
    }

    @Override
    public void updatePosition() {
        Point pos = ws.getMousePosition();
        if (pos != null) {
            x = (int) pos.getX() + a;   y = (int) pos.getY() + b;
        }
    }

    @Override
    public void delete() {
        ws.delete(wire);
        // {@code FreeBall}s must be deleted alongside with their{@code Wire}
        ws.delete(this);
    }

}