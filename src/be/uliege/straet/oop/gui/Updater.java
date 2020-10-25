package be.uliege.straet.oop.gui;

/**
 * Reapeatedly sends repaint messages to a WorkSpace object, when a 
 * {@code Draggable} object is selected and dragged.
 */
public class Updater extends Thread {
    private WorkSpace ws;
    private boolean hasWire;
    private Draggable d;
    private Wire wire;
    public static final int FPS = 30;

    /**
     * Constructor.
     * @param ws        The {@code WorkSpace} this {@code Updater} updates
     * @param hasWire   Wether or not we are drawing a {@code Wire}
     */
    private Updater(WorkSpace ws, boolean hasWire) {
        this.ws = ws;
        this.hasWire = hasWire;
    }

    /**
     * Constructs an {@code Updater} that will make the {@code WorkSpace} to be
     * repainted until the {@code Draggable} is released.
     * @param ws    The{@code WorkSpace}
     * @param d     The{@code Draggable}
     */
    public Updater(WorkSpace ws, Draggable d) {
        this(ws, false);
        this.d = d;
    }

    /**
     * Builds an {@code Updater} that will make the {@code WorkSpace} to be 
     * repainted until the {@code Wire} is complete.
     * @param ws    The{@code WorkSpace}
     * @param d     The{@code Wire}
     */
    public Updater(WorkSpace ws, Wire wire) {
        this(ws, true);
        this.wire = wire;
    }

    public void run() {
        if (hasWire) {
            while (!wire.isComplete()) {
                ws.repaint();
                try {
                    Thread.sleep(1000 / FPS);
                } catch (InterruptedException e) {}
            }
        }
        else {
            while (d.isSelected()) {
                ws.repaint();
                try {
                    Thread.sleep(1000 / FPS);
                } catch (InterruptedException e) {}
            }
        }
    }

}