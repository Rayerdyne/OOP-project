package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;

/**
 * This class represents a draggable ball that will guide the wires beteween
 * the different connection between filters.
 */
public class FreeBall extends Draggable {

    public static int BALL_RADIUS = 20;
    private Wire wire;

    public FreeBall(WorkSpace ws, Wire wire) {
        this(0, 0, ws, wire);
    }

    public FreeBall(int x, int y, WorkSpace ws, Wire wire) {
        super(x, y, ws);
        this.wire = wire;
    }

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
        // `FreeBall`s must be deleted alongside with their `Wire`
        ws.delete(this);
    }

}