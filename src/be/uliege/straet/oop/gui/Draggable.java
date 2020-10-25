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
 * Represents something that can be dragged and dropped in the WorkSpace.
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
     * Constructs a {@code Draggable} in {@code WorkSpace} at coordinates 
     * (x, y).
     * @param x         x coordinate
     * @param y         y coordinate
     * @param ws        The {@code WorkSpace} the {@code Draggable} belongs to
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
     * Constructs a {@code Draggable} in{@code WorkSpace}
     * @param ws        The{@code WorkSpace}
     */
    public Draggable(WorkSpace ws) {
        this(30, 30, ws);
        Point p = MouseInfo.getPointerInfo().getLocation();
        x = (int) p.getX();
        y = (int) p.getY();
    }

    /**
     * Constructs a {@code Draggable} that will be selected for positioning in
     * given {@code WorkSpace}.
     * @param x         The x coordinate
     * @param y         The y coordinate
     * @param ws        The{@code WorkSpace}
     * @param selected  Wether or not the {@code Draggable} is selected wen 
     *                  created. Basically, it will be true, because we won't
     *                  place the filters statically.
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
     * Paints the {@code Draggable}.
     * @param g         The {@code Graphics} object to paint on it
     * @param back      The background{@code Color}
     * @param fore      The foreground{@code Color}
     * @param zoom      The zooming factor
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

    /**
     * Paints the {@code Draggable} with {@code WorkSpace} zoom factor.
     * @param g         The {@code Graphics} object to paint on it
     * @param back      The background{@code Color}
     * @param fore      The foreground{@code Color}
     */
    public void paint(Graphics g, Color back, Color fore) {
        paint(g, back, fore, ws.zoomFactor());
    }

    /**
     * @return      Wether or not the {@code Draggable} element is selected, 
     *              i.e. if the user is dragging it         
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Updates the x, y coordinates of the {@code Draggable} (when this is 
     * selected).
     */
    public void updatePosition() {
        Point pos = ws.getMousePosition();
        if (xCorners == null ) {
            System.out.println("The coordinates of the points have not been " +
                "set. RIP");
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
     * Rotates the {@code Draggable} 90° clockwise
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

    /** 
     * Get the orientation of the object.
     * @return  The orientation of the object (ROT_{0, 90, 180, 270});
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Delete this from its {@code WorkSpace}.
     */
    public void delete() {
        ws.delete(this);
    }

    /**
     * <p>Edits the current 'thing', i.e., does nothing if it's not a filter, 
     * and opens the input dialog otherwise, e.g. for setting the delay.</p>
     * <p>Will be overriden xD</p>
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