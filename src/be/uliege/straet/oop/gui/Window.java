package be.uliege.straet.oop.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import be.uliege.montefiore.oop.audio.FilterException;

import javax.swing.JButton;

/**
 * Holds the whole window
 */
public class Window extends JFrame {
    /**
     * To please warnings...
     */
    private static final long serialVersionUID = 1L;
    public static final String WINDOW_NAME = "Filter";

    private WorkSpace ws = new WorkSpace();
    private FMenuBar fmb = new FMenuBar(ws);

    /** It is a `LinkedHashMap` to be able to get the buttons in the order we
     * inserted them.
     */
    private static LinkedHashMap<JButton, Procedure> buttonsActions = 
                                       new LinkedHashMap<JButton, Procedure>();

    public static void main(String[] args) {
        Window w = new Window(WINDOW_NAME);
        w.setVisible(true);
    }

    /**
     * @param name The name of the window
     */
    public Window(String name) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(610, 377);
        buttonsActions.put(new JButton("Connection"), () -> {
            try {ws.addConnection();}
            catch (FilterException e) 
                {System.out.println(e.getMessage());}});
        buttonsActions.put(new JButton("Addition"), () -> {ws.addAddition();});
        buttonsActions.put(new JButton("Delay"), () -> {ws.addDelay();});
        buttonsActions.put(new JButton("Gain"), () -> {ws.addGain();});
        buttonsActions.put(new JButton("Composite"), () -> {ws.addComposite();});
        buttonsActions.put(new JButton("Input"), () -> {ws.addInput();});
        buttonsActions.put(new JButton("Output"), () -> {ws.addOutput();});
        buttonsActions.put(new JButton("Cancel"), () -> {ws.cancelCurrent();});
        buttonsActions.put(new JButton("Variable"), 
            () -> {ws.addVariableDeclaration();});

        initUi();
        new Thread(new RetardatorFocusGiver(ws)).start();
    }
    
    private void initUi() {

        JPanel buttonsPanel = new JPanel();
        // add all buttons and connect them to the action listener
        ActionListener bal = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Procedure todo = buttonsActions.get(e.getSource());
                    todo.run();
                }
            };
        // for (int i = 0; i < buttonsActions.size(); i++) {
        //     JButton b = buttonsActions.
        //     buttonsPanel.add(b);
        //     b.addActionListener(bal);

        // }
        for (JButton b : buttonsActions.keySet()) {
            buttonsPanel.add(b);
            b.addActionListener(bal);
        }

        this.setJMenuBar(fmb);

        Container cp = getContentPane();
        cp.add(ws, BorderLayout.CENTER);
        // this.addKeyListener(ws);
        buttonsPanel.addKeyListener(ws);
        ws.addKeyListener(ws);
        cp.add(buttonsPanel, BorderLayout.NORTH);
    }
}