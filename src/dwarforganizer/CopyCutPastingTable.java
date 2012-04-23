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
import java.util.Vector;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * Fixes a very old bug in the JVM which causes JTable to start editing when a
 * mnemonic key or function key is pressed.
 *
 * Also allows accelerators for cut, copy, and paste operations in the table
 * to work.
 *
 * A method to create a popup cut/copy/paste menu is provided.
 *
 * @author Tamara Orr
 */

public class CopyCutPastingTable extends JTable {
    public CopyCutPastingTable() {
        super();
        initialize();
    }
    public CopyCutPastingTable(int numRows, int numColumns) {
        super(numRows, numColumns);
        initialize();
    }
    public CopyCutPastingTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
        initialize();
    }
    public CopyCutPastingTable(TableModel dm) {
        super(dm);
        initialize();
    }
    public CopyCutPastingTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
        initialize();
    }
    public CopyCutPastingTable(TableModel dm, TableColumnModel cm
            , ListSelectionModel sm) {
        super(dm, cm, sm);
        initialize();
    }
    public CopyCutPastingTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
        initialize();
    }
    private void initialize() {
        setCutCopyPasteMappings(this);
    }

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

    // Starting an edit session is horribly bugged. So we are preventing
    // Control, Alt, and Meta from starting the edit session.
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e
            , int condition, boolean pressed) {

        // We cannot just ignore every key that isn't in the input map
        // these days, because in modern Java EVERY DARN KEY is in the
        // JTable input map.

        // First, allow Ctrl+C, Ctrl+X, Ctrl+V to be processed (copy, cut, paste)
        // TODO: There is probably some way to avoid hard-coding these bindings,
        // and instead read them properly from a data structure, but I don't know
        // the best practice to deal with the masks...
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

    private void setCutCopyPasteMappings(JTable table) {
        ActionMap map = table.getActionMap();
        map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                TransferHandler.getPasteAction());
    }

    public void createEditMenuItems(JComponent editMenu) {
        final JMenuItem[] editMenuItems = new JMenuItem[3];
        TransferActionListener actionListener = new TransferActionListener();

        JMenuItem menuItem = new JMenuItem("Cut Cell");
        menuItem.setActionCommand((String)TransferHandler.getCutAction().
                 getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_U);
        menuItem.setVisible(false); // TODO: Make Cut work, or remove it
        editMenuItems[0] = menuItem;
        editMenu.add(menuItem);

        menuItem = new JMenuItem("Copy Cell");
        menuItem.setActionCommand((String)TransferHandler.getCopyAction().
                 getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_C);
        editMenuItems[1] = menuItem;
        editMenu.add(menuItem);

        menuItem = new JMenuItem("Paste Cell");
        menuItem.setActionCommand((String)TransferHandler.getPasteAction().
                 getValue(Action.NAME));
        menuItem.addActionListener(actionListener);
        menuItem.setAccelerator(
          KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.setMnemonic(KeyEvent.VK_T);
        editMenuItems[2] = menuItem;
        editMenu.add(menuItem);

        // Create a selection listener to keep menu items enabled/disabled
        // states up to date
        this.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (! e.getValueIsAdjusting())
                    updateMenuEnabledStates(editMenuItems);
            }
        });

        // Set the initial enabled state of the menu items
        updateMenuEnabledStates(editMenuItems);
    }
    private void updateMenuEnabledStates(JMenuItem[] menuItems) {
        boolean bAnythingSelected = this.getSelectedRowCount() > 0;
        for (JMenuItem menuItem : menuItems) {
            menuItem.setEnabled(bAnythingSelected);
        }
    }
}
