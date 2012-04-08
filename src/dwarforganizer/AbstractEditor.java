/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JTable;
import myutils.MyHandyTable;

/**
 * Makes programming typical table add/edit/delete functions more convenient
 * 
 * How to use:
 * Extend AbstractEditor
 * Implement the abstract functions
 * Call super.initialize() to set up editor
 * Call super.addOrUpdateRecord() to add or update a record
 * Call editRow()/copyRow()/deleteRow() to edit, copy, or delete a row
 * 
 * @author Tamara Orr
 */
public abstract class AbstractEditor<T extends MyPropertyGetter> implements DirtyForm {

    private JTable moTable;
    private MyTableModel moModel; // DefaultTableModel
    private JButton mbtnUpdate;
    
    private enum EditingState { NEW, EDIT }
    private EditingState meEditState;    
    private int mintCurrentEditedRow = -1;  // Model index. The currently edited row, if any. Only valid while EditingState is EDIT
    private boolean mbClearAfterAdd = true;
    private boolean mbClearAfterEdit = true;
    //private boolean mbDirty = false;    // True if any data has been changed.
                                        // Use setDirty() to set this variable so that listeners are notified.
    //private Vector<DirtyListener> mvDirtyListener = new Vector<DirtyListener>();
    
    private DirtyHandler moDirtyHandler;
    
    public AbstractEditor() {
        super();
        
        moDirtyHandler = new DirtyHandler();
    }
    
    protected void initialize(JTable table, MyTableModel model
            , JButton btnUpdate, boolean clearAfterAdd
            , boolean clearAfterEdit, boolean editOnDoubleClick
            , final int[] editKeys, final int[] deleteKeys) { // DefaultTableModel
        
        moTable = table;
        moModel = model;
        mbtnUpdate = btnUpdate;
        mbClearAfterAdd = clearAfterAdd;
        mbClearAfterEdit = clearAfterEdit;
        
        if (editOnDoubleClick) {
            moTable.addMouseListener(new MouseListener() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Double click to edit row
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                        startEditingRow(moTable.rowAtPoint(e.getPoint()));
                    }   
                }
                @Override
                public void mousePressed(MouseEvent e) { //Do nothing
                }
                @Override
                public void mouseReleased(MouseEvent e) { // Do nothing
                }
                @Override
                public void mouseEntered(MouseEvent e) { // Do nothing
                }
                @Override
                public void mouseExited(MouseEvent e) { // Do nothing
                }
            });
        }
        
        if (editKeys.length > 0 || deleteKeys.length > 0) {
            Arrays.sort(editKeys);      // Arrays must be sorted before binarySearch
            Arrays.sort(deleteKeys);
            moTable.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                    if (Arrays.binarySearch(editKeys, e.getKeyCode()) >= 0)
                        editRow();
                    else if (Arrays.binarySearch(deleteKeys, e.getKeyCode()) >= 0)
                        deleteRow();
                }
                @Override
                public void keyPressed(KeyEvent e) { // Do nothing
                }
                @Override
                public void keyReleased(KeyEvent e) { // Do nothing
                }
            });
        }
        this.setEditingState(EditingState.NEW); // Default editing state
    }
    
    public abstract void clearInput();
    public abstract boolean validateInput();
    public abstract T createRowData(); // Create row data from input control contents // Vector<Object>
    public abstract boolean rowDataToInput(T rowData); // int modelRow
    
    @Override
    public DirtyHandler getDirtyHandler() {
        return moDirtyHandler;
    }
    
    protected int getCurrentEditedRow() {
        return mintCurrentEditedRow;
    }
    
/*    protected void addDirtyListener(DirtyListener newListener) {
        Vector<DirtyListener> v = (Vector<DirtyListener>) mvDirtyListener.clone();
        if (! v.contains(newListener))
            mvDirtyListener.add(newListener);
    }
    private void notifyDirtyListeners() {
        Vector<DirtyListener> v = (Vector<DirtyListener>) mvDirtyListener.clone();
        for (DirtyListener listener : v) {
            listener.dirtyChanged(mbDirty);
        }
    }
    protected boolean isDirty() { return mbDirty; }
    protected void setClean() { setDirty(false); }
    protected void setDirty(boolean newValue) {
        if (mbDirty != newValue) {
            mbDirty = newValue;
            notifyDirtyListeners();
        }
    }    */
    
    public boolean addRecord() {
        try {
            if (inputToNewRow()) {                
                setEditingState(EditingState.EDIT);
                if (mbClearAfterAdd) clearInput();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage() + " Failed to add or update row.");
            return false;            
        }
        return true;
    }
    public boolean updateRecord() {
        try {
            if (inputToRow(mintCurrentEditedRow)) {

                setEditingState(EditingState.EDIT);
                if (mbClearAfterEdit) clearInput();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage() + " Failed to update row.");
            return false;
        }
        
        return true;
    }
    
    // Edits the first selected row
    protected boolean editRow() {
        int row = moTable.getSelectedRow();
        if (row != -1)
            return startEditingRow(row);
        else
            return false;
    }
    // Edits the given row (table row index)
    protected boolean startEditingRow(int tableRow) {
        
        int modelRow = moTable.convertRowIndexToModel(tableRow);
        mintCurrentEditedRow = modelRow;
        
        setEditingState(EditingState.EDIT);
        rowDataToInput((T) moModel.getRowData().get(modelRow));
        
        return true;
    }

    // Copies the first selected row
    protected void copyRow() {
        int row = moTable.getSelectedRow();
        if (row != -1) {
            mintCurrentEditedRow = moTable.convertRowIndexToModel(row);
            setEditingState(EditingState.EDIT);
            rowDataToInput((T) moModel.getRowData().get(mintCurrentEditedRow)); // row
        }
    }
    // Deletes the first selected row
    protected boolean deleteRow() {
        int row = moTable.getSelectedRow();
        if (row != -1) {
            int modelRow = moTable.convertRowIndexToModel(row);
            moModel.removeRow(modelRow);
            mintCurrentEditedRow = -1;
            moDirtyHandler.setDirty(true);
            return true;
        }
        else
            return false;
        
    }
    
    private boolean inputToRow(int modelRow) {        
        try {
            if (! validateInput()) {
                return false;
            }
            
            //Vector<Object> vRowData = createRowData();
            T rowData = createRowData();
            moModel.updateRow(modelRow, rowData);
            //moModel.removeRow(modelRow);
            //moModel.insertRow(modelRow, vRowData);

            moDirtyHandler.setDirty(true);
            MyHandyTable.ensureIndexIsVisible(moTable
                    , moTable.convertRowIndexToView(modelRow));

        } catch (Exception e) {
            return false;
        }
        return true;        
    }
    private boolean inputToNewRow() {
        try {
            if (! validateInput()) {
                return false;
            }
            
            moModel.addRow(createRowData());

            moDirtyHandler.setDirty(true);
            int modelIndex = moModel.getRowCount() - 1;
            MyHandyTable.ensureIndexIsVisible(moTable, moTable.convertRowIndexToView(modelIndex));

            mintCurrentEditedRow = modelIndex;
            
        } catch (Exception ignore) {
            return false;
        }
        return true;
    }
    
    private void setEditingState(EditingState newState) {
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
}
