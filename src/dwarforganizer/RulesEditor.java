/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import myutils.MyHandyTable;
import myutils.MySimpleTableModel;

/**
 * A screen for editing rules.txt (the blacklist and whitelist)
 * The file is viewed as a table. Entries can be added, removed, edited, and
 * saved to file.
 * 
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class RulesEditor extends JPanel implements DirtyForm {

    private static final Vector<String> mvHeadings = new Vector<String>(
            Arrays.asList(new String[]
            { "Entry Type", "First Labor", "Second Labor", "Comment"}));
    private static final int COLUMN_COUNT = mvHeadings.size();
    
    private static final String DEFAULT_TYPE = "-Select-";
    private static final String DEFAULT_LABOR = "-Select Labor-";
    private static final String DEFAULT_COMMENT = "";
    
    private static final String TEXT_ADD = "Add New";
    private static final String TEXT_EDIT = "Update";
        
    private int mintCurrentEditedRow = -1;  // The currently edited row, if any. Only valid while EditingState is EDIT
    
    private enum EditingState { NEW, EDIT }
    private EditingState meEditState = EditingState.NEW;
    private JButton mbtnAddOrUpdate = new JButton(TEXT_ADD);
    private JButton mbtnStopEditing;
    
    private JTable mtblRules;
    private MySimpleTableModel mmdlRules;
    
    private JComboBox mcboType = new JComboBox(new String[] {
        DEFAULT_TYPE, "BLACKLIST", "WHITELIST" }) ;
    private JComboBox mcboFirstLabor;
    private JComboBox mcboSecondLabor;
    private PlaceholderTextField mtxtComment;
    private JLabel mlblMeaning;
    
    //private boolean mbDirty = false;    // True if any data has been changed.
                                        // Use setDirty() to set this variable so that listeners are notified.
    //private Vector<DirtyListener> mvDirtyListener = new Vector<DirtyListener>();
    private DirtyHandler moDirtyHandler;
    
    class InvalidInputException extends Exception { }
    
    // Never mind
    // JTable editing interface is just too awful to inflict on users
/*    class LaborCellEditor extends DefaultCellEditor {
        public LaborCellEditor(JComboBox comboBox) {
            super(comboBox);
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value
                , boolean isSelected, int row, int column) {
            
            Component c = super.getTableCellEditorComponent(table, value
                    , isSelected, row, column);
            if (c instanceof JComboBox) {
                final JComboBox jcb = (JComboBox) c;
                
                //System.out.println(value);
                jcb.setSelectedItem(value);
                //System.out.println(jcb.getSelectedItem().toString());
            }
            return c;
        }
    } */
    
    // TODO: Normalize constructor
    public RulesEditor(Vector<String[]> ruleFileContents
            , Vector<Labor> vLabors) {
        
        super();
        
        moDirtyHandler = new DirtyHandler();
        
        JPanel panEdit;
        
        Vector<String> vLaborNames = getLaborNames(vLabors);
        
        vLaborNames.add(0, DEFAULT_LABOR);
        
        mmdlRules = new MySimpleTableModel();
        mmdlRules.setDataVector(stringArrayToVVO(ruleFileContents), mvHeadings);

        //mdlRules.addEditableException(3);   // Comment editable
                
        mtblRules = new JTable(mmdlRules);
        mtblRules.setComponentPopupMenu(createPopUpMenu());
        mtblRules.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // Double click to edit row
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    startEditingRow(mtblRules.rowAtPoint(e.getPoint()));
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
        mtblRules.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.isControlDown() && (e.getKeyChar() == KeyEvent.VK_C))
                    copyRow();
                else if (e.isControlDown() && (e.getKeyChar() == KeyEvent.VK_ENTER))
                    editRow();
                else if (e.getKeyChar() == KeyEvent.VK_DELETE)
                    deleteRow();
            }
            @Override
            public void keyPressed(KeyEvent e) { // Do nothing
            }
            @Override
            public void keyReleased(KeyEvent e) { // Do nothing
            }
        });
        
        // Not editing in JTable anymore
        // Edit Entry Type, First Labor, and Second Labor using a combo box
        //tblRules.getColumn("Entry Type").setCellEditor(new LaborCellEditor(
        //        new JComboBox(new String[] {
        //    "COMMENT", "BLACKLIST", "WHITELIST" } )));
        //mdlRules.addEditableException(0);
        //JComboBox<String> cboLabor = new JComboBox<String>(getLaborNames(vLabors));
        
        //LaborCellEditor lceLabor = new LaborCellEditor(cboLabor);
        
        //tblRules.getColumn("First Labor").setCellEditor(lceLabor);
        //mdlRules.addEditableException(1);
        //tblRules.getColumn("Second Labor").setCellEditor(lceLabor);
        //mdlRules.addEditableException(2);
        
        JScrollPane sp = new JScrollPane(mtblRules);
        sp.setPreferredSize(new Dimension(700, 300));
        MyHandyTable.handyTable(mtblRules, sp, mmdlRules, false
                , true);
        
        // -------- Editing panel -------------
        JPanel panCommentOnTop = new JPanel();
        panCommentOnTop.setLayout(new BorderLayout());
        panEdit = new JPanel();
        panEdit.setLayout(new FlowLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Rule Type"), BorderLayout.NORTH);
        mcboType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMeaning();
            }
        });
        panel.add(mcboType, BorderLayout.SOUTH);
        panEdit.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("First Labor"), BorderLayout.NORTH);
        mcboFirstLabor = new JComboBox(vLaborNames);
        mcboFirstLabor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMeaning();
            }
        });
        panel.add(mcboFirstLabor, BorderLayout.SOUTH);
        panEdit.add(panel);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Second Labor"), BorderLayout.NORTH);
        mcboSecondLabor = new JComboBox(vLaborNames);
        mcboSecondLabor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMeaning();
            }
        });        
        panel.add(mcboSecondLabor, BorderLayout.SOUTH);
        panEdit.add(panel);        
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        mbtnAddOrUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOrUpdateRecord();
            }
        });
        panel.add(mbtnAddOrUpdate, BorderLayout.NORTH);
        mbtnStopEditing = new JButton("Cancel Edit");
        mbtnStopEditing.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEditingState(EditingState.NEW);
            }
        });
        panel.add(mbtnStopEditing, BorderLayout.SOUTH);
        panEdit.add(panel);
        mbtnStopEditing.setVisible(false);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Comment"), BorderLayout.NORTH);
        mtxtComment = new PlaceholderTextField(20, "Add a comment (optional)", true);
        panel.add(mtxtComment, BorderLayout.SOUTH);
        
        panCommentOnTop.add(panel, BorderLayout.NORTH);
        panCommentOnTop.add(panEdit, BorderLayout.CENTER);
        
        panel = new JPanel();
        mlblMeaning = new JLabel("[Message]");
        //mlblMeaning.setFont(mlblMeaning.getFont().deriveFont(Font.ITALIC));
        panel.add(mlblMeaning);
        updateMeaning();
        panCommentOnTop.add(panel, BorderLayout.SOUTH);
        
        this.setLayout(new BorderLayout());
        this.add(sp, BorderLayout.CENTER);
        this.add(panCommentOnTop, BorderLayout.SOUTH);
        
    }
    
    /*
    @Override
    public void addDirtyListener(DirtyListener newListener) {
        Vector<DirtyListener> v = (Vector<DirtyListener>) mvDirtyListener.clone();
        if (! v.contains(newListener))
            mvDirtyListener.add(newListener);
    }
    @Override
    public void notifyDirtyListeners() {
        Vector<DirtyListener> v = (Vector<DirtyListener>) mvDirtyListener.clone();
        for (DirtyListener listener : v) {
            listener.dirtyChanged(mbDirty);
        }
    }
    @Override
    public boolean isDirty() { return mbDirty; }
    @Override
    public void setClean() { setDirty(false); }
    @Override
    public void setDirty(boolean newValue) {
        if (mbDirty != newValue) {
            mbDirty = newValue;
            notifyDirtyListeners();
        }
    } */
    
    // Transforms the given vector of string arrays to a vector of vectors of
    // objects.
    private Vector<Vector<Object>> stringArrayToVVO(Vector<String[]> transformMe) {
        
        Vector<Vector<Object>> vReturn = new Vector<Vector<Object>>();
        
        for (String[] strRow : transformMe) {
            Vector<Object> vRow = new Vector<Object>();
            vRow.addAll(Arrays.asList(strRow));
            vReturn.add(vRow);
        }
        
        return vReturn;
    }
    // Does the reverse of stringArrayToVVO
    private Vector<String[]> VVOToStringArray(Vector<Vector<Object>> transformMe) {
        Vector<String[]> vReturn = new Vector<String[]>();
        
        for (Vector<Object> vFields : transformMe) {
            String[] row = new String[vFields.size()];
            row = (String[]) vFields.toArray(row);
            vReturn.add(row);
        }
        
        return vReturn;
    }
    
    // Gets the current file data in Vector<String[]> format
    protected Vector<String[]> getCurrentFile() {
        return VVOToStringArray(mmdlRules.getDataVector());
    }
    
    // Converts Vector<Labor> into Vector<String> (.name property)
    private Vector<String> getLaborNames(Vector<Labor> vLabors) {
        Vector<String> vReturn = new Vector<String>();
        
        for (Labor labor : vLabors)
            vReturn.add(labor.name);
        Collections.sort(vReturn);
        return vReturn;
    }
    
    // Adds or updates a record depending on the current edit state
    private void addOrUpdateRecord() {
        try {
            if (meEditState.equals(EditingState.EDIT)) {
                inputToRowData(mintCurrentEditedRow);
                
                setEditingState(EditingState.NEW);
                clearInput();
            }
            else if (meEditState.equals(EditingState.NEW)) {
                inputToNewRow();
                
                setEditingState(EditingState.NEW);
                clearInput();
            }
            else
                System.err.println("Invalid editing state: " + meEditState.toString());
            
        } catch (InvalidInputException e) {
            System.err.println("Failed to add or update row: an entry is invalid.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to add or update row.");
        }
    }
    
    // Edits the first selected row
    private void editRow() {
        int row = mtblRules.getSelectedRow();
        if (row != -1)
            startEditingRow(row);
    }
    
    // Edits the given row (table row index)
    private void startEditingRow(int tableRow) {
        
        int modelRow = mtblRules.convertRowIndexToModel(tableRow);
        mintCurrentEditedRow = modelRow;
        
        setEditingState(EditingState.EDIT);
        rowDataToInput(modelRow);
        
    }

    // Copies the first selected row
    private void copyRow() {
        int row = mtblRules.getSelectedRow();
        if (row != -1) {
            setEditingState(EditingState.NEW);
            rowDataToInput(row);
        }
    }
    
    // Deletes the first selected row
    private void deleteRow() {
        int row = mtblRules.getSelectedRow();
        if (row != -1) {
            int modelRow = mtblRules.convertRowIndexToModel(row);
            mmdlRules.removeRow(modelRow);
            moDirtyHandler.setDirty(true);
        }
    }
    
    // Copies the given table model row data to the input controls
    private void rowDataToInput(int modelRow) {
        
        clearInput();
        
        if (mmdlRules.getValueAt(modelRow, 0) != null)
            mcboType.setSelectedItem(mmdlRules.getValueAt(modelRow, 0));
        if (mmdlRules.getValueAt(modelRow, 1) != null)
            mcboFirstLabor.setSelectedItem(mmdlRules.getValueAt(modelRow, 1));
        if (mmdlRules.getValueAt(modelRow, 2) != null)
            mcboSecondLabor.setSelectedItem(mmdlRules.getValueAt(modelRow, 2));
        if (mmdlRules.getValueAt(modelRow, 3) != null)
            mtxtComment.setText(mmdlRules.getValueAt(modelRow, 3).toString());
        
        updateMeaning();
    }
    
    // Copies the data in the input controls to the given table model row
    private void inputToRowData(int modelRow) throws InvalidInputException {
        Vector<Object> vRow = inputToVector();
        for (int iCount = 0; iCount < COLUMN_COUNT; iCount++)
            mmdlRules.setValueAt(vRow.get(iCount), modelRow, iCount);
        moDirtyHandler.setDirty(true);
    }
    
    // Copies the data in the input controls to a new table row (inserted at end)
    private void inputToNewRow() throws InvalidInputException {
        mmdlRules.addRow(inputToVector());
        moDirtyHandler.setDirty(true);
        MyHandyTable.ensureIndexIsVisible(mtblRules, mmdlRules.getRowCount() - 1);
    }

    // Converts the data in the input controls to a vector (in the style of
    // TableModel.setDataVector())
    private Vector<Object> inputToVector() throws InvalidInputException {
        Vector<Object> vReturn = new Vector<Object>();
        
        if (validateInput()) {        
            vReturn.add(mcboType.getSelectedItem().toString());
            vReturn.add(mcboFirstLabor.getSelectedItem().toString());
            vReturn.add(mcboSecondLabor.getSelectedItem().toString());
            vReturn.add(mtxtComment.getText());
        }
        return vReturn;
    }
    
    // Clears the input controls
    private void clearInput() {
        mcboType.setSelectedItem(DEFAULT_TYPE);
        mcboFirstLabor.setSelectedItem(DEFAULT_LABOR);
        mcboSecondLabor.setSelectedItem(DEFAULT_LABOR);
        mtxtComment.setText(DEFAULT_COMMENT);
    }
    
    // Throws an exception if the input is invalid;
    // returns true otherwise.
    private boolean validateInput() throws InvalidInputException {
        if (mcboType.getSelectedItem().toString().equals(DEFAULT_TYPE)
                || mcboFirstLabor.getSelectedItem().toString().equals(DEFAULT_LABOR)
                || mcboSecondLabor.getSelectedItem().toString().equals(DEFAULT_LABOR)) {
            
            //System.out.println("Invalid input");
            throw new InvalidInputException();
        }
        else
            return true;
    }
    
    // Sets the editing state of the panel and updates button text
    private void setEditingState(EditingState newState) {
        
        switch (newState) {
            case NEW:
                mbtnAddOrUpdate.setText(TEXT_ADD);
                //System.out.println("Set text to " + TEXT_ADD);
                break;
            case EDIT:
                mbtnAddOrUpdate.setText(TEXT_EDIT);
                //System.out.println("Set text to " + TEXT_EDIT);
                break;
        }
        // "Stop Editing" is only visible while editing.
        mbtnStopEditing.setVisible(newState.equals(EditingState.EDIT));
        
        meEditState = newState;
    }
    
    // Creates the right-click context popup menu for the rules table
    private JPopupMenu createPopUpMenu() {
        JPopupMenu popUp = new JPopupMenu();
        
        JMenuItem menuItem = new JMenuItem("Edit", KeyEvent.VK_E);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("control ENTER"));
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                editRow();
            }
        });
        popUp.add(menuItem);
        
        // ---------------------------------------
        menuItem = new JMenuItem("Copy to New", KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("control C"));
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                copyRow();
            }
        });        
        popUp.add(menuItem);
        
        // ---------------------------------------
        popUp.add(new JSeparator());
        
        // ---------------------------------------
        menuItem = new JMenuItem("Delete", KeyEvent.VK_D);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRow();
            }
        });
        popUp.add(menuItem);
        
        return popUp;
        
    }
    
    // Updates the text description of the current contents of the input controls
    private void updateMeaning() {
                
        String strType = mcboType.getSelectedItem().toString();
        String strFirst = mcboFirstLabor.getSelectedItem().toString();
        String strSecond = mcboSecondLabor.getSelectedItem().toString();
        String strMeaning;
        
        if (strFirst.equals(DEFAULT_LABOR))
            strFirst = "[the first labor]";
        if (strSecond.equals(DEFAULT_LABOR))
            strSecond = "[the second labor]";
        
        if (strType.equals(DEFAULT_TYPE))
            strMeaning = "Adding a rule relating " + strFirst
                    + " and " + strSecond + "...";
        else if (strType.toLowerCase().equals("whitelist"))
            strMeaning = "Citizens assigned to " + strFirst + " may ONLY do "
                    + strSecond + " and any other " + strFirst
                    + " WHITELIST labors.";
        else    // blacklist
            strMeaning = strFirst + " and " + strSecond
                    + " may never be done by the same citizen.";
        
        mlblMeaning.setText(strMeaning);
    }

    @Override
    public DirtyHandler getDirtyHandler() {
        return moDirtyHandler;
    }
    
}
