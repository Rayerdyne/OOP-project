package be.uliege.straet.oop.gui;

/**
 * Reapeatedly sends repaint messages to a WorkSpace object, when a `Draggable`
 * object is selected and dragged.
 */
public class Updater extends Thread {
    private WorkSpace ws;
    private boolean hasWire;
    private Draggable d;
    private Wire wire;
    public static final int FPS = 30;

    /**
     * Constructor.
     * @param ws        The `WorkSpace` this `Updater` updates
     * @param hasWire   Wether or not we are drawing a `Wire`
     */
    private Updater(WorkSpace ws, boolean hasWire) {
        this.ws = ws;
        this.hasWire = hasWire;
    }

    /**
     * Constructs an `Updater` that will make the `WorkSpace` to be repainted
     * until the `Draggable` is released.
     * @param ws    The `WorkSpace`
     * @param d     The `Draggable`
     */
    public Updater(WorkSpace ws, Draggable d) {
        this(ws, false);
        this.d = d;
    }

    /**
     * Builds an `Updater` that will make the `WorkSpace` to be repainted
     * until the `Wire` is complete.
     * @param ws    The `WorkSpace`
     * @param d     The `Wire`
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