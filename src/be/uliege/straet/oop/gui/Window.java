package be.uliege.straet.oop.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
    private static LinkedHashMap<JButton, Procedure> buttonsActionsUp = 
                                       new LinkedHashMap<JButton, Procedure>();
    private static LinkedHashMap<JButton, Procedure> buttonsActionsDown = 
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
        buttonsActionsUp.put(new JButton("Connection"), () -> {
            try { ws.addConnection(); }
            catch (FilterException e) {
                System.out.println(e.getMessage());
            }  });
        buttonsActionsUp.put(new JButton("Addition"), () -> {ws.addAddition();});
        buttonsActionsUp.put(new JButton("Delay"), () -> {ws.addDelay();});
        buttonsActionsUp.put(new JButton("Gain"), () -> {ws.addGain();});
        buttonsActionsUp.put(new JButton("Composite"), () -> {
            try { ws.addComposite(); }
            catch (Exception e) {
                WorkSpace.showError("Could not add the composite filter", e);
            }  });

        buttonsActionsDown.put(new JButton("Variable"), 
            () -> {ws.addVariableDeclaration();});
        buttonsActionsDown.put(new JButton("Input"), () -> {ws.addInput();});
        buttonsActionsDown.put(new JButton("Output"), () -> {ws.addOutput();});
        buttonsActionsDown.put(new JButton("Cancel"), () -> {ws.cancelCurrent();});

        initUi();
        new Thread(new RetardatorFocusGiver(ws)).start();
    }
    
    private void initUi() {

        JPanel buttonsPanelUp = new JPanel();
        JPanel buttonsPanelDown = new JPanel();
        // add all buttons and connect them to the action listener
        ActionListener bal = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Object source = e.getSource();
                    Procedure todo = buttonsActionsUp.containsKey(source) ? 
                                     buttonsActionsUp.get(source)  :
                                     buttonsActionsDown.get(source);
                    todo.run();
                }
            };
        // for (int i = 0; i < buttonsActions.size(); i++) {
        //     JButton b = buttonsActions.
        //     buttonsPanel.add(b);
        //     b.addActionListener(bal);

        // }
        for (JButton b : buttonsActionsUp.keySet()) {
            buttonsPanelUp.add(b);
            b.addActionListener(bal);
        }
        for (JButton b : buttonsActionsDown.keySet()) {
            buttonsPanelDown.add(b);
            b.addActionListener(bal);
        }

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 0));
        buttonsPanel.add(buttonsPanelUp);
        buttonsPanel.add(buttonsPanelDown);

        this.setJMenuBar(fmb);

        Container cp = getContentPane();
        cp.add(ws, BorderLayout.CENTER);
        // this.addKeyListener(ws);
        buttonsPanelUp.addKeyListener(ws);
        buttonsPanelDown.addKeyListener(ws);
        ws.addKeyListener(ws);
        cp.add(buttonsPanel, BorderLayout.NORTH);
    }
}