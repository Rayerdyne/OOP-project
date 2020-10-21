package be.uliege.straet.oop.gui;

import java.util.HashMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import be.uliege.montefiore.oop.audio.FilterException;
import be.uliege.straet.oop.loader.WriterException;

/**
 * Custom menu with all features of this system...
 * (FilterMenBar)
 */
public class FMenuBar extends JMenuBar implements ActionListener {
    private static final long serialVersionUID = 1L;

    private WorkSpace ws;

    JMenu menuFile = new JMenu("File");
    HashMap<JMenuItem, Procedure> miFile = new HashMap<JMenuItem, Procedure>();
    // ...

    JMenu menuView = new JMenu("View");
    JMenuItem miZoomIn = new JMenuItem("Zoom in");
    JMenuItem miZoomOut = new JMenuItem("Zoom out");

    JMenu menuRun = new JMenu("Run");
    HashMap<JMenuItem, Procedure> miRun = new HashMap<JMenuItem, Procedure>();

    JMenu menuAdd = new JMenu("Add");
    HashMap<JMenuItem, Procedure> miAdd = new HashMap<JMenuItem, Procedure>();

    public FMenuBar(WorkSpace ws) {
        super();
        this.ws = ws;

        miAdd.put(new JMenuItem("Connection"), 
            () -> { 
            try { ws.addConnection();}
            catch (FilterException e) {
                WorkSpace.showError("Could not add connection", e); 
            }  });
        miAdd.put(new JMenuItem("Output filter"), 
                    () -> {  ws.addOutput();  });
        miAdd.put(new JMenuItem("Gain filter"),
                    () -> { ws.addGain();        });
        miAdd.put(new JMenuItem("Input filter"), 
                    () -> {  ws.addInput();  });
        miAdd.put(new JMenuItem("Delay filter"),
                    () -> { ws.addDelay();       });
        miAdd.put(new JMenuItem("Composite filter"), 
            () -> {
            try { ws.addComposite(); }
            catch (Exception e) {
                WorkSpace.showError("Could not add the composite filter.", e);
            }  });
        miAdd.put(new JMenuItem("Addition filter"), 
                    () -> { ws.addAddition();    });
        miAdd.put(new JMenuItem("Convolution filter"), 
                    () -> { ws.addConvolution();    });

        miFile.put(new JMenuItem("Save"), 
                    () -> { ws.save();           });
        miFile.put(new JMenuItem("Save as..."), 
                    () -> { ws.saveAs();           });
        miFile.put(new JMenuItem("Open"), 
                    () -> { ws.open();           });
        miFile.put(new JMenuItem("Quit"), 
                    () -> { ws.quit();           });
        miFile.put(new JMenuItem("Export standalone filter"), () -> { 
            try { ws.exportStandaloneFilter(); }
            catch (WriterException we) {
                WorkSpace.showError("The filter could not be written in the " +
                    "output file", we);
            } catch (FilterException fe) {
                WorkSpace.showError("Filter represented by the `WorkSpace` " +
                    "could not be built", fe);
            }  });

        miRun.put(new JMenuItem("Build filter"), () -> {           
            try { ws.buildFilter(true);	}
            catch (FilterException e1) {
                WorkSpace.showError("The filter is not complete: ", e1);
			} });
        miRun.put(new JMenuItem("Apply to voice"), () -> { 
            try { ws.applyToVoice(); }
            catch (ComputationException e2) {
                WorkSpace.showError("Computation exception: ", e2);
            } catch (FilterException e3) {
                WorkSpace.showError("The filter is not complete: ", e3);
            } });
        miRun.put(new JMenuItem("Build output file"), 
                    () -> { ws.buildOutputFile(false);  });
        miRun.put(new JMenuItem("Play result"), 
                    () -> { ws.playResult();            });
        miRun.put(new JMenuItem("Pause"), 
                    () -> { ws.pauseResult();           });
        miRun.put(new JMenuItem("End"), 
                    () -> { ws.abortResult();           });

        for (JMenuItem mi : miFile.keySet()) {
            menuFile.add(mi);
            mi.addActionListener(this);
        }
        add(menuFile);

        menuView.add(miZoomIn);
        menuView.add(miZoomOut);
        miZoomIn.addActionListener(this);
        miZoomOut.addActionListener(this);
        add(menuView);

        for (JMenuItem mi : miRun.keySet()) {
            menuRun.add(mi);
            mi.addActionListener(this);
        }
        add(menuRun);

        for (JMenuItem mi : miAdd.keySet()) {
            menuAdd.add(mi);
            mi.addActionListener(this);
        }
        add(menuAdd);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == miZoomIn) {
            ws.zoomIn();
        } else if (s == miZoomOut) {
            ws.zoomOut();
        } else if (miAdd.containsKey(s)) {
            miAdd.get(s).run();
        } else if (miFile.containsKey(s)) {
            miFile.get(s).run();
        } else if (miRun.containsKey(s)) {
            miRun.get(s).run();
        }
    }

}