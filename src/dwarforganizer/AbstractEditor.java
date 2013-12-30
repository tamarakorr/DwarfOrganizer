/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.dirty.DirtyForm;
import dwarforganizer.dirty.DirtyHandler;
import dwarforganizer.dirty.DirtyListener;
import dwarforganizer.swing.MyTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import myutils.Adapters.KeyTypedAdapter;
import myutils.Adapters.MouseClickedAdapter;
import myutils.MyHandyTable;

/**
 * Makes programming typical table add/edit/delete functions more convenient.
 *
 * How to use:
 * Extend AbstractEditor
 * Implement the abstract functions
 * Call super.initialize() to set up editor
 * Call super.addOrUpdateRecord() to add or update a record
 * Call editRow()/copyRow()/deleteRow() to edit, copy, or delete a row
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public abstract class AbstractEditor<T extends MyPropertyGetter>
        implements DirtyForm {

    private static final Logger logger = Logger.getLogger(
            AbstractEditor.class.getName());

    private JTable moTable;
    private MyTableModel moModel; // DefaultTableModel
    private JButton mbtnUpdate;

    private enum EditingState { NEW, EDIT }
    private EditingState meEditState;

    // Model index. The currently edited row, if any.
    // Only valid while EditingState is EDIT
    private int mintCurrentEditedRow = -1;

    private boolean mbClearAfterAdd = true;
    private boolean mbClearAfterEdit = true;

    private DirtyHandler moDirtyHandler;

    private JMenuItem mmuEditMenu;
    private JMenuItem mmuDeleteMenu;

    private JComponent focusOnEdit;

    public AbstractEditor() {
        super();

        moDirtyHandler = new DirtyHandler();
    }

    public void initialize(final JTable table, final MyTableModel model
            , final JButton btnUpdate, final boolean clearAfterAdd
            , final boolean clearAfterEdit, final boolean editOnDoubleClick
            , final boolean ctrlEnterToEdit, final boolean deleteKeyToDelete
            , final boolean createPopUpEdit, final boolean createPopUpDelete
            , final JComponent focusOnEdit) {

        moTable = table;
        moModel = model;
        mbtnUpdate = btnUpdate;
        mbClearAfterAdd = clearAfterAdd;
        mbClearAfterEdit = clearAfterEdit;
        this.focusOnEdit = focusOnEdit;     // Can be null

        if (editOnDoubleClick) {
            moTable.addMouseListener(new MouseClickedAdapter() {
                @Override
                public void mouseClicked(final MouseEvent e) {
                    // Double click to edit row
                    if (e.getButton() == MouseEvent.BUTTON1
                            && e.getClickCount() == 2) {
                        startEditingRow(moTable.rowAtPoint(e.getPoint()));
                    }
                }
            });
        }

        if (ctrlEnterToEdit) {
            moTable.addKeyListener(new KeyTypedAdapter() {
                @Override
                public void keyTyped(final KeyEvent e) {
                    if (e.isControlDown()
                            && (e.getKeyChar() == KeyEvent.VK_ENTER)) {
                        editRow();
                    }
                }
            });
        }
        if (deleteKeyToDelete) {
            moTable.addKeyListener(new KeyTypedAdapter() {
                @Override
                public void keyTyped(final KeyEvent e) {
                    if (e.getKeyChar() == KeyEvent.VK_DELETE)
                        deleteRow();
                }
            });
        }
        if (createPopUpEdit || createPopUpDelete) {
            moTable.setComponentPopupMenu(createPopUpMenu(createPopUpEdit
                    , createPopUpDelete, ctrlEnterToEdit, deleteKeyToDelete));
        }

        // Revert to EditingState.NEW if we're updating and the selection
        // changes.
        // Only enable popup menu items that will do something to the current
        // selection.
        moTable.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent e) {
                if (! e.getValueIsAdjusting()) {
                    if (meEditState.equals(EditingState.EDIT)) {
                        setEditingState(EditingState.NEW);
                    }
                    updatePopupsEnabled();
                }
            }
        });
        // Initialize enabled popup menus state
        updatePopupsEnabled();

        this.setEditingState(EditingState.NEW); // Default editing state
    }
    // Enable the menus if anything is selected.
    private void updatePopupsEnabled() {
        final JMenuItem[] menuItemList = new JMenuItem[] {
            mmuEditMenu, mmuDeleteMenu };

        for (final JMenuItem menuItem : menuItemList) {
            if (null != menuItem) { // It may not exist
                menuItem.setEnabled(moTable.getSelectedRowCount() > 0);
            }
        }
    }

    public abstract void clearInput();
    public abstract boolean validateInput();
    public abstract T createRowData(boolean isNew); // Create row data from input control contents // Vector<Object>
    public abstract boolean rowDataToInput(T rowData); // int modelRow

    protected int getCurrentEditedRow() {
        return mintCurrentEditedRow;
    }

    public boolean addRecord() {
        try {
            if (inputToNewRow()) {
                setEditingState(EditingState.EDIT);
                if (mbClearAfterAdd) clearInput();
                return true;
            }
            else
                return false;
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Failed to add row.", e);
            return false;
        }
    }
    public boolean updateRecord() {
        try {
            if (inputToRow(mintCurrentEditedRow)) {

                setEditingState(EditingState.EDIT);
                if (mbClearAfterEdit) clearInput();
            }
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Failed to update row.", e);
            return false;
        }

        return true;
    }

    // Edits the first selected row
    protected boolean editRow() {
        final int row = moTable.getSelectedRow();
        if (row != -1)
            return startEditingRow(row);
        else
            return false;
    }
    // Edits the given row (table row index)
    protected boolean startEditingRow(final int tableRow) {

        final int modelRow = moTable.convertRowIndexToModel(tableRow);
        mintCurrentEditedRow = modelRow;

        setEditingState(EditingState.EDIT);
        rowDataToInput((T) moModel.getRowData().get(modelRow));

        // Request focus to a certain JComponent if set
        if (focusOnEdit != null)
            focusOnEdit.requestFocusInWindow();

        return true;
    }

    // Deletes the first selected row
    protected boolean deleteRow() {
        final int row = moTable.getSelectedRow();
        if (row != -1) {
            final int modelRow = moTable.convertRowIndexToModel(row);
            moModel.removeRow(modelRow);
            mintCurrentEditedRow = -1;
            setEditingState(EditingState.NEW);
            moDirtyHandler.setDirty(true);
            return true;
        }
        else
            return false;
    }

    private boolean inputToRow(final int modelRow) {
        try {
            if (! validateInput()) {
                return false;
            }

            final T rowData = createRowData(false);
            moModel.updateRow(modelRow, rowData);

            moDirtyHandler.setDirty(true);
            MyHandyTable.ensureIndexIsVisible(moTable
                    , moTable.convertRowIndexToView(modelRow));

        } catch (final Exception ignore) {
            return false;
        }
        return true;
    }
    private boolean inputToNewRow() {
        try {
            if (! validateInput()) {
                return false;
            }

            moModel.addRow(createRowData(true));

            moDirtyHandler.setDirty(true);
            final int modelIndex = moModel.getRowCount() - 1;
            MyHandyTable.ensureIndexIsVisible(moTable
                    , moTable.convertRowIndexToView(modelIndex));

            mintCurrentEditedRow = modelIndex;

        } catch (final Exception ignore) {
            return false;
        }
        return true;
    }

    private void setEditingState(final EditingState newState) {
        switch (newState) {
            case NEW:
                mbtnUpdate.setEnabled(false);
                break;
            case EDIT:
                mbtnUpdate.setEnabled(true);
                break;
        }
        meEditState = newState;
    }
    @Override
    public void addDirtyListener(final DirtyListener listener) {
        moDirtyHandler.addDirtyListener(listener);
    }
    @Override
    public boolean isDirty() {
        return moDirtyHandler.isDirty();
    }
    @Override
    public void setClean() {
        moDirtyHandler.setClean();
    }
    protected DirtyHandler getDirtyHandler() {
        return moDirtyHandler;
    }
    // Creates the right-click context popup menu for the table
    private JPopupMenu createPopUpMenu(final boolean allowEdit
            , final boolean allowDelete
            , final boolean ctrlEnterToEdit, final boolean deleteKeyToDelete) {

        final JPopupMenu popUp = new JPopupMenu();
        JMenuItem menuItem;

        if (allowEdit) {
            menuItem = new JMenuItem("Edit", KeyEvent.VK_E);
            if (ctrlEnterToEdit)
                menuItem.setAccelerator(
                        KeyStroke.getKeyStroke("control ENTER"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    editRow();
                }
            });
            mmuEditMenu = menuItem;
            popUp.add(menuItem);
        }

        // ---------------------------------------
        if (allowDelete) {
            menuItem = new JMenuItem("Delete", KeyEvent.VK_D);
            if (deleteKeyToDelete)
                menuItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    deleteRow();
                }
            });
            mmuDeleteMenu = menuItem;
            popUp.add(menuItem);
        }

        return popUp;
    }
}
