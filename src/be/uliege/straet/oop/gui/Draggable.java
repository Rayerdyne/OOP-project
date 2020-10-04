package be.uliege.straet.oop.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics;

/**
 * Represents something that can be drag and dropped in the WorkSpace.
 */
public class Draggable implements MouseListener, KeyListener, Locatable {

    public static final int ROT_0 = 0;
    public static final int ROT_90 = 1;
    public static final int ROT_180 = 2;
    public static final int ROT_270 = 3;

    protected int x, y;
    protected int a, b;
    protected boolean selected = false;
    WorkSpace ws;

    private double prevZoom;

    /** Coordinates are absolute.
     *  It could be better to let it static, relative and only save the 
     *  orientation... */
    protected int[] xCorners, yCorners;
    private int orientation = ROT_0;
    // private Vector<Ball> balls;

    protected int r = 30;

    /**
     * Constructs a Draggable living in WorkSpace ws and in (x, y)
     * @param x         X coordinate
     * @param y         Y coordinate
     * @param ws        The WorkSpace
     */
    public Draggable(int x, int y, WorkSpace ws) {
        this.x = x;
        this.y = y;

        this.ws = ws;
        prevZoom = 1.0;
        ws.addMouseListener(this);
        ws.addKeyListener(this);
    }

    /**
     * Constructs a Draggable object living in WorkSpace ws
     * @param ws        The WorkSpace
     */
    public Draggable(WorkSpace ws) {
        this(30, 30, ws);
        Point p = MouseInfo.getPointerInfo().getLocation();
        x = (int) p.getX();
        y = (int) p.getY();
    }

    /**
     * Constructs a Draggable that will be selected for positioning, living in
     * WorkSpace ws.
     * @param x         The x coordinate of the element
     * @param y         The y coordinate of the element
     * @param ws        The WorkSpace
     * @param selected  Wether or not the Draggable is selected wen created. 
     *                  Basically, it will be true, because we won't place the
     *                  filters statically.
     */
    public Draggable(int x, int y, WorkSpace ws, boolean selected) {
        this(x, y, ws);
        this.selected = selected;
        
        a = -r / 2;  b = -r / 2;
        if (selected) {
            ws.requestFocus();
            new Updater(ws, this).start(); 
        }
    }

    /**
     * Paints the `Draggable`
     * @param g     The `Graphics` object to paint on it
     * @param back  The background `Color`
     * @param fore  The foreground `Color`
     * @param zoom  The zooming factor
     */
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        if (selected)
            updatePosition();
        g.setColor(fore);

        if (zoom != prevZoom) {
            double z = zoom / prevZoom;
            x *= z;
            y *= z;
            r *= z;
            for (int i = 0; i < xCorners.length; i++) {
                xCorners[i] = WorkSpace.zoom(xCorners[i], z);
                yCorners[i] = WorkSpace.zoom(yCorners[i], z);
            }
            prevZoom = zoom;
        }
        g.fillPolygon(xCorners, yCorners, xCorners.length);
        // g.setColor(Color.RED);
        // g.drawOval(x - r, y - r, 2*r, 2*r);
    }

    public void paint(Graphics g, Color back, Color fore) {
        paint(g, back, fore, ws.zoomFactor());
    }

    /**
     * @return Wether or not the Draggable element is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Updates the x, y, (mainly, when this is selected)
     */
    public void updatePosition() {
        Point pos = ws.getMousePosition();
        if (xCorners == null ) {
            System.out.println("The coordinates of the points have not been set. RIP");
            return;
        }
        if (pos != null) {
            for (int i = 0; i < xCorners.length; i++) {
                xCorners[i] -= x;       yCorners[i] -= y;
            }
            x = (int) pos.getX() + a;   y = (int) pos.getY() + b;
            for (int i = 0; i < xCorners.length; i++) {
                xCorners[i] += x;       yCorners[i] += y;
            }
        }
    }

    /**
     * Rotates the `Draggable`
     */
    public void rotate() {
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] -= x; yCorners[i] -= y;
        }
        for (int i = 0; i < xCorners.length; i++) {
            int temp = xCorners[i];
            xCorners[i] = - yCorners[i];
            yCorners[i] = temp;
        }
        for (int i = 0; i < xCorners.length; i++) {
            xCorners[i] += x; yCorners[i] += y;
        }
        ws.repaint();
        orientation = (orientation + 1) % 4;
    }

    /** Get the orientation of the object
     * @return  The orientation of the object (ROT_{0, 90, 180, 270});
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Delete this from its `WorkSpace`
     */
    public void delete() {
        ws.delete(this);
    }

    /**
     * Edits the current 'thing', i.e., does nothing if it's not a filter, and
     * opens the input dialog otherwise, e.g. for setting the delay.
     * Will be overriden xD
     */
    public void edit() {}
    
    @Override
    public void mousePressed(MouseEvent e) {
        Point pos = e.getPoint();
        double dx = pos.getX() - x, dy = pos.getY() - y;
        Point absPos = ws.getMousePosition(); 
        if (dx * dx + dy * dy < r * r && !ws.isBusy()) {
            selected = true;
            a = x - (int) absPos.getX();
            b = y - (int) absPos.getY();
            updatePosition();
            ws.requestFocus();
            new Updater(ws, this).start(); 
            
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        selected = false;
    }
    
    @Override       public void mouseClicked(MouseEvent e) {}
    @Override       public void mouseEntered(MouseEvent e) {}
    @Override       public void mouseExited(MouseEvent e) {}
    @Override       public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (!selected) 
            return;
        switch (e.getKeyChar()) {
            case 'r':
                rotate();       break;
            case 'd':
                delete();       break;
            case 'e':
            case ' ':
                edit();         break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public int getX() { return (int) x; }
    @Override public int getY() { return (int) y; }
}