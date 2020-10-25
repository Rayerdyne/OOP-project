package be.uliege.straet.oop.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>Represents a connection between two filters.</p>
 * <p>It can start and end with a {@code FixedBall}, and can go through 
 * {@code FreeBall}s that are {@code Draggable}.</p>
 */
public class Wire implements MouseListener {

    private Vector<Locatable> points;
    private Vector<FreeBall> freeBalls;
    private WorkSpace ws;

    private FixedBall input = null;
    private FixedBall output = null;

    /**
     * Constructs a {@code Wire}.
     * @param ws    The {@code WorkSpace} the {@code Wire} belongs to
     */
    public Wire(WorkSpace ws) {
        this.ws = ws;
        for (FixedBall fb : ws.inputs())
            ws.addMouseListener(fb);
        
        for (FixedBall fb : ws.outputs()) 
            ws.addMouseListener(fb);

        points = new Vector<Locatable>();
        freeBalls = new Vector<FreeBall>();
    }

    /**
     * Paints the {@code Wire} on a {@code Graphics}.
     * @param g         The {@code Graphics} object to draw on
     * @param back      The background {@code Color}
     * @param fore      The foreground {@code Color}
     */
    public void paint(Graphics g, Color back, Color fore, double zoom) {
        if (points.size() >= 2) {
            Iterator<Locatable> it = points.iterator();
            try {
                Locatable prev = it.next();
                g.setColor(fore);

                while (it.hasNext()) {
                    Locatable next = it.next();
                    g.drawLine(prev.getX(), prev.getY(), 
                               next.getX(), next.getY());
                    prev = next;
                }
            } catch (NoSuchElementException e) {
                System.out.println("THIS ERROR SHOULD NOT HAPPEN");
                e.printStackTrace();
            }

        }
    }
    
    /**
     * Sets one end of the {@code Wire}.
     * @param origin        The {@code FixedBall}
     */
    public void setFirst(FixedBall fb) {
        if (fb.isInput())
            input = fb;
        else 
            output = fb;
        
        points.add(fb);
        
        ws.addMouseListener(this);
        ws.setState(WorkSpace.DRAW_WIRE);
        Updater updater = new Updater(ws, this);
        updater.start();
        fb.owner().addConnectedWire(this, fb.isInput());
    }

    /**
     * Sets the second end of a {@code Wire}.
     * @param destination       The {@code FixedBall}
     * @return                  Wether or not we could successfully add the
     *                          second end
     */
    public boolean setSecond(FixedBall fb) {
        if (fb.isInput() && input != null) 
            return false;
        
        else if (fb.isInput())
            input = fb;
        else 
            output = fb;

        for (FixedBall fb2 : ws.inputs()) 
            ws.removeMouseListener(fb2);
        for (FixedBall fb2 : ws.outputs()) 
            ws.removeMouseListener(fb2);
        ws.removeMouseListener(this);

        points.add(fb);
        fb.owner().addConnectedWire(this, fb.isInput());
        ws.setState(WorkSpace.NORMAL);
        return true;
    }

    /**
     * Gets the {@code FixedBall} that has been selected first (to draw the 
     * wire dynamically and see it when drawing).
     * @return  The {@code FixedBall}
     */
    public FixedBall firstEnd() {
        if (input == null)
            return output;
        else
            return input;
    }

    /**
     * Adds a {@code FreeBall} to the {@code Wire}.
     * @param fr    The {@code FreeBall} to add
     */
    public void addFreeBall(FreeBall fr) {
        points.add(fr);
        freeBalls.add(fr);
    }

    /**
     * When the {@code Wire} is deleted, frees the destination end of the 
     * {@code Wire}.
     */
    public void freeEnds() {
        if (input != null)
            input.setConnected(false);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        ws.addFreeBall((int) p.getX(), (int) p.getY(), this);
        ws.repaint();
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    /**
     * @return      The end of this {@code Wire} that is an input w.r.t. to its
     *              filter. This could be veiwed as the output of this
     *              {@code Wire}
     */
    public FixedBall input() { return input; }
    /**
     * @return      The end of this {@code Wire} that is an output w.r.t. to 
     *              its filter. This could be veiwed as the input of this
     *              {@code Wire}
     */
    public FixedBall output() { return output; }
    /**
     * @return      True if both ends of the wire are connected.
     */
    public boolean isComplete() { return input != null &&
                                         output != null; }

    /**
     * @return      A {@code Vector<FreeBall>} containing all the
     *              {@code FreeBall}s that are checkpoints of this {@code Wire}
     */
    public Vector<FreeBall> freeBalls() { return freeBalls; }
}