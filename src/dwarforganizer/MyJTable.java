/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;

/**
 * Fixes a very old bug in the JVM which causes JTable to start editing when a
 * mnemonic key or function key is pressed.
 * 
 * Also allows accelerators for cut, copy, and paste operations in the table
 * to work.
 * 
 * A convenience method to create a popup cut/copy/paste menu is provided.
 * 
 * @author Tamara Orr
 */

public class MyJTable extends JTable {
    
    // This class is based on Sun "CCP in a non text component" tutorial
    protected class TransferActionListener implements ActionListener,
                                                  PropertyChangeListener {
        private JComponent focusOwner = null;

        public TransferActionListener() {
            KeyboardFocusManager manager = KeyboardFocusManager.
               getCurrentKeyboardFocusManager();
            manager.addPropertyChangeListener("permanentFocusOwner", this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            Object o = e.getNewValue();
            if (o instanceof JComponent) {
                focusOwner = (JComponent)o;
            } else {
                focusOwner = null;
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (focusOwner == null)
                return;
            String action = (String)e.getActionCommand();
            Action a = focusOwner.getActionMap().get(action);
            if (a != null) {
                a.actionPerformed(new ActionEvent(focusOwner,
                                                  ActionEvent.ACTION_PERFORMED,
                                                  null));
            }
        }
    }    
    
    public MyJTable(TableModel tableModel) {
        super(tableModel);
        setCutCopyPasteMappings();
    }

    // Starting an edit session is horribly bugged so we are preventing
    // Control, Alt, and Meta from starting the edit session.
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e
            , int condition, boolean pressed) {

        // We cannot just ignore every key that isn't in the input map
        // these days, because in modern Java EVERY DARN KEY is in the
        // JTable input map.
        
        // First, allow Ctrl+C, Ctrl+X, Ctrl+V to be processed (copy, cut, paste)
        // TODO: There is probably some way to avoid hard-coding these bindings,
        // and instead read them properly from a data structure, but I don't know it...
        if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_C
                || e.getKeyCode() == KeyEvent.VK_X
                || e.getKeyCode() == KeyEvent.VK_V)) {
            //System.out.println("cut copy paste");
            return super.processKeyBinding(ks, e, condition, pressed);
        }
        // Stop Alt, Ctrl, and Meta from starting an edit
        else if (e.isAltDown() || e.isControlDown() || e.isMetaDown())
            return false;
        else
            return super.processKeyBinding(ks, e, condition, pressed);
    }
    
    private void setCutCopyPasteMappings() {
        ActionMap map = this.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
    }
    public void createEditMenuItems(JComponent editMenu) {
        TransferActionListener actionListener = new TransferActionListener();
        
        JMenuItem menuItem = new JMenuItem("Cut Cell");
        menuItem.setActionCommand((String)TransferHandler.getCutAction().
                 getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_T);
        menuItem.setEnabled(false); // TODO: Make Cut work, or remove it
        editMenu.add(menuItem);
        
        menuItem = new JMenuItem("Copy Cell");
        menuItem.setActionCommand((String)TransferHandler.getCopyAction().
                 getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(menuItem);
        
        menuItem = new JMenuItem("Paste Cell");
        menuItem.setActionCommand((String)TransferHandler.getPasteAction().
                 getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_P);
        editMenu.add(menuItem);
        
    }
    
}
