package be.uliege.straet.oop.gui;

/**
 * Because of idk the `WorkSpace` has not the focus and I can't figure out how
 * to give it in another way.
 */
public class RetardatorFocusGiver implements Runnable {
    public static final int TO_WAIT = 200;    

    public WorkSpace ws;

    public RetardatorFocusGiver(WorkSpace ws) {
        this.ws = ws;
    }

    public void run() {
        // System.out.println("Starting to wait");
        try {
            Thread.sleep(TO_WAIT);
        } catch (InterruptedException e) {}
        ws.requestFocus();
        // System.out.println("Requested focus.");
    }
}
