package be.uliege.straet.oop.gui;

/**
 * Because of idk the `WorkSpace` has not the focus and I can't figure out how
 * to give it in another way.
 */
public class RetardatorFocusGiver implements Runnable {
    public static final int TO_WAIT = 200;    

    public WorkSpace ws;

    /**
     * Constructor.
     * @param ws        The `WorkSpace` this will give the focus to
     */
    public RetardatorFocusGiver(WorkSpace ws) {
        this.ws = ws;
    }

    /**
     * Sleeps TO_WAIT milliseconds then makes the `WorkSpace` request the 
     * focus.
     */
    public void run() {
        // System.out.println("Starting to wait");
        try {
            Thread.sleep(TO_WAIT);
        } catch (InterruptedException e) {}
        ws.requestFocus();
        // System.out.println("Requested focus.");
    }
}
