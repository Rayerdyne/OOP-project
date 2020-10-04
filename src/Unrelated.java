/**
 * Lol.
 * As I made another program that needs as an input a suite of coordinates in
 * order to make a drawing, I re-use the system I made here for my own 
 * convenience.
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import be.uliege.montefiore.oop.audio.FilterException;
import be.uliege.straet.oop.gui.Procedure;
import be.uliege.straet.oop.gui.WorkSpace;


public class Unrelated extends JFrame {
    /**
     * To please warnings...
     */
    private static final long serialVersionUID = 1L;
    public static final String WINDOW_NAME = "Filter";

    private static LinkedHashMap<JButton, Procedure> buttonsActions = 
                                       new LinkedHashMap<JButton, Procedure>();
                                       
    private WorkSpace ws = new WorkSpace();

    public static void main(String[] args) {
        Unrelated w = new Unrelated(WINDOW_NAME);
        w.setVisible(true);
    }

    /**
     * @param name The name of the window
     */
    public Unrelated(String name) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(610, 377);

        buttonsActions.put(new JButton("Add CIA"), 
            () ->  { ws.addNothing(false); });
        buttonsActions.put(new JButton("Add LIA"), 
            () -> { ws.addNothing(true); });
        buttonsActions.put(new JButton("Connection"), () -> {
            try {ws.addConnection();}
            catch (FilterException e) 
                {System.out.println(e.getMessage());}});
        buttonsActions.put(new JButton("Gogo export go"), () -> { 
            try { gogoExportGo(); }
            catch (IOException e) 
                { System.out.println("Oh Oh: " + e.getMessage()); }} );

        initUi();
    }
    
    private void initUi() {

        ActionListener bal = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Procedure todo = buttonsActions.get(e.getSource());
                    todo.run();
                }
            };
            
        JPanel buttonsPanel = new JPanel();
        for (JButton b : buttonsActions.keySet()) {
            buttonsPanel.add(b);
            b.addActionListener(bal);
        }

        Container cp = getContentPane();
        cp.add(ws, BorderLayout.CENTER);
        buttonsPanel.addKeyListener(ws);
        ws.addKeyListener(ws);
        cp.add(buttonsPanel, BorderLayout.NORTH);
    }

    public void gogoExportGo() throws IOException {
        String filename = JOptionPane.showInputDialog(null, "File name: ", 
                                                            "output.txt");
        if (filename == null)
            return;
        
        FileWriter fw = new FileWriter(filename);
        PrintWriter pw = new PrintWriter(fw);
        
        try {
            ws.printNothing(pw);
        } catch (Exception e) {
            System.out.println("Caught exception while printing nothing. " + 
                "Message: " + e.getMessage());
        }
        pw.close();
    }
}
