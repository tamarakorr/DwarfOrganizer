/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.broadcast.BroadcastMessage;
import dwarforganizer.broadcast.Broadcaster;
import dwarforganizer.dirty.DirtyForm;
import dwarforganizer.dirty.DirtyListener;
import dwarforganizer.swing.MyTableModel;
import dwarforganizer.swing.PlaceholderTextField;
import dwarforganizer.swing.SortKeySwapper;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.*;
import javax.swing.*;
import myutils.DefaultFocus;
import myutils.MyHandyTable;

/**
 * A screen for editing rules.txt (the blacklist and whitelist)
 * The file is viewed as a table. Entries can be added, removed, edited, and
 * saved to file.
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class RulesEditorUI extends JPanel implements DirtyForm {

    private static final int NUM_TEXT_COLUMNS = 49; // For preferred width

    private static final List<Object> mvHeadings = new ArrayList<Object>(
            Arrays.asList(new Object[]
            { "Entry Type", "First Labor", "Second Labor", "Comment"}));

    private static final String DEFAULT_TYPE = "-Select-";
    private static final String DEFAULT_LABOR = "-Select Labor-";
    private static final String DEFAULT_COMMENT = "";

    private static final String TEXT_ADD = "Add New";
    private static final String TEXT_EDIT = "Update";

    private JButton mbtnAdd; // mbtnAddOrUpdate
    private JButton mbtnUpdate;

    private JTable mtblRules;
    private MyTableModel<LaborRule> mmdlRules;

    private JComboBox mcboType;
    private JComboBox mcboFirstLabor;
    private JComboBox mcboSecondLabor;
    private PlaceholderTextField mtxtComment;
    private JTextArea mtxtMeaning;
    private JScrollPane mspScrollPane;

    private RulesEditor moRulesEditor;
    private Broadcaster defaultButtonBroadcaster;

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

    public RulesEditorUI(List<Labor> vLabors) { // Vector<String[]> ruleFileContents

        // Super constructor----------------------------------------------------
        super();

        // Declarations---------------------------------------------------------
        JPanel panEdit;

        // Create objects-------------------------------------------------------
        //moDirtyHandler = new DirtyHandler();
        defaultButtonBroadcaster = new Broadcaster();

        final List<String> lstLaborNames = getLaborNames(vLabors);
        lstLaborNames.add(0, DEFAULT_LABOR);

        // Dummy data vector
        //Vector<String[]> ruleFileContents = new Vector<String[]>();

        moRulesEditor = new RulesEditor(this);

        // Create model---------------------------------------------------------
        Class[] aColClasses = new Class[] { String.class, String.class
                , String.class, String.class };
        String[] aColProps = new String[] { "type", "firstlabor", "secondlabor"
                , "comment" };
        SortKeySwapper swapper = new SortKeySwapper();
        mmdlRules = new MyTableModel<LaborRule>(mvHeadings, aColClasses
                , aColProps, new ArrayList<LaborRule>(), swapper);

        // Create table---------------------------------------------------------
        mtblRules = MyHandyTable.createSmarterFocusTable(new JTable(mmdlRules));
        //mtblRules.setComponentPopupMenu(createPopUpMenu());

        mspScrollPane = new JScrollPane(mtblRules);
        mspScrollPane.setPreferredSize(new Dimension(750, 350));
        MyHandyTable.handyTable(mtblRules, mmdlRules, false, true);

        swapper.setTable(mtblRules);
        // Create other UI controls---------------------------------------------

        mtxtMeaning = new JTextArea("[Message]");
        mtxtMeaning.setEditable(false);
        mtxtMeaning.setLineWrap(true);
        mtxtMeaning.setWrapStyleWord(true);
        mtxtMeaning.setColumns(NUM_TEXT_COLUMNS);   // For preferred width
        mtxtMeaning.setRows(2);                     // For preferred height

        // (Nimbus bug workaround - setBackground() doesn't work properly)
        mtxtMeaning.setOpaque(false);
        mtxtMeaning.setBorder(BorderFactory.createEmptyBorder());
        mtxtMeaning.setBackground(new Color(0, 0, 0, 0)); // JLabel().getBackground()

        ActionListener alUpdateMeaning = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMeaning();
            }
        };

        mcboType = new JComboBox(new String[] { DEFAULT_TYPE, "BLACKLIST"
                , "WHITELIST" });
        mcboType.addActionListener(alUpdateMeaning);
        mcboType.setPrototypeDisplayValue("BLACKLISTXX");

        mcboFirstLabor = new JComboBox(lstLaborNames.toArray());
        mcboFirstLabor.addActionListener(alUpdateMeaning);
        mcboFirstLabor.setPrototypeDisplayValue("Small Animal DissectionXX");

        mcboSecondLabor = new JComboBox(lstLaborNames.toArray());
        mcboSecondLabor.addActionListener(alUpdateMeaning);
        mcboSecondLabor.setPrototypeDisplayValue("Small Animal DissectionXX");

        mbtnAdd = new JButton(TEXT_ADD);
        mbtnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moRulesEditor.addRecord();
            }
        });

        mbtnUpdate = new JButton(TEXT_EDIT);
        mbtnUpdate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                moRulesEditor.updateRecord();
            }
        });

        mtxtComment = new PlaceholderTextField(NUM_TEXT_COLUMNS
                , "Add a comment (optional)", true);
        DefaultFocus.setDefaultComponent(mtxtComment);

        // Post-initialize controls---------------------------------------------
        moRulesEditor.initialize(mtblRules, mmdlRules, mbtnUpdate
                , false, false, true, true, true, true, true, mtxtComment);

        // Set up default buttons
        Map<JComponent, JButton> hmDefaultButtons = new HashMap<JComponent
                , JButton>(5);
        JComponent[] compsForDefaultAdd = new JComponent[] { mcboType
                , mcboFirstLabor, mcboSecondLabor, mtxtComment };
        for (JComponent comp : compsForDefaultAdd) {
            hmDefaultButtons.put(comp, mbtnAdd);
        }
        hmDefaultButtons.put(mtblRules, null);

        for (JComponent comp : hmDefaultButtons.keySet()) {
            final JButton btn = hmDefaultButtons.get(comp);
            comp.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    requestDefaultButton(btn);
                }
                @Override
                public void focusLost(FocusEvent e) {
                }
            });
        }

        // Build the UI---------------------------------------------------------
        JPanel panCommentOnTop = new JPanel();
        panCommentOnTop.setLayout(new BorderLayout());
        panEdit = new JPanel();
        panEdit.setLayout(new FlowLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Rule Type"), BorderLayout.NORTH);
        panel.add(mcboType, BorderLayout.SOUTH);
        panEdit.add(panel);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("First Labor"), BorderLayout.NORTH);
        panel.add(mcboFirstLabor, BorderLayout.SOUTH);
        panEdit.add(panel);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Second Labor"), BorderLayout.NORTH);
        panel.add(mcboSecondLabor, BorderLayout.SOUTH);
        panEdit.add(panel);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(mbtnAdd, BorderLayout.NORTH);
        panel.add(mbtnUpdate, BorderLayout.SOUTH);
        panEdit.add(panel);

        panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Comment"), BorderLayout.NORTH);
        panel.add(mtxtComment, BorderLayout.SOUTH);

        JPanel flowPanel = new JPanel(new FlowLayout());
        flowPanel.add(panel);

        panCommentOnTop.add(flowPanel, BorderLayout.NORTH); // panel
        panCommentOnTop.add(panEdit, BorderLayout.CENTER);

        panel = new JPanel();
        panel.add(mtxtMeaning);
        updateMeaning();
        panCommentOnTop.add(panel, BorderLayout.SOUTH);
        panCommentOnTop.setBorder(BorderFactory.createEtchedBorder());

        this.setLayout(new BorderLayout());
        this.add(mspScrollPane, BorderLayout.CENTER);
        this.add(panCommentOnTop, BorderLayout.SOUTH);
    }

    private class RulesEditor extends AbstractEditor<LaborRule> {
        private Component dialogParent;
        public RulesEditor(final Component dialogParent) {
            this.dialogParent = dialogParent;
        }

        @Override
        public void clearInput() {
            mcboType.setSelectedItem(DEFAULT_TYPE);
            mcboFirstLabor.setSelectedItem(DEFAULT_LABOR);
            mcboSecondLabor.setSelectedItem(DEFAULT_LABOR);
            mtxtComment.setText(DEFAULT_COMMENT);
        }

        @Override
        public boolean validateInput() {
            final String type = mcboType.getSelectedItem().toString();
            final String first = mcboFirstLabor.getSelectedItem().toString();
            final String second = mcboSecondLabor.getSelectedItem().toString();

            if (type.equals(DEFAULT_TYPE)) {
                warn("Select a rule type.");
                return false;
            }
            if (first.equals(DEFAULT_LABOR)) {
                warn("Select the first labor.");
                return false;
            }
            if (second.equals(DEFAULT_LABOR)) {
                warn("Select the second labor.");
                return false;
            }
            if (first.equals(second)) {
                warn("The first labor and the second labor must be different.");
                return false;
            }
            return true;
        }
        private void warn(final String messageBody) {
            JOptionPane.showMessageDialog(dialogParent, messageBody
                    , "Cannot add rule", JOptionPane.OK_OPTION);
        }

        @Override
        public LaborRule createRowData(boolean isNew) {
            return new LaborRule(mcboType.getSelectedItem().toString()
                    , mcboFirstLabor.getSelectedItem().toString()
                    , mcboSecondLabor.getSelectedItem().toString()
                    , mtxtComment.getText());
        }

        @Override
        public boolean rowDataToInput(LaborRule rowData) {
            clearInput();

            if (rowData.getType() != null)
                mcboType.setSelectedItem(rowData.getType());
            if (rowData.getFirstLabor() != null)
                mcboFirstLabor.setSelectedItem(rowData.getFirstLabor());
            if (rowData.getSecondLabor() != null)
                mcboSecondLabor.setSelectedItem(rowData.getSecondLabor());
            if (rowData.getComment() != null)
                mtxtComment.setText(rowData.getComment());

            //updateMeaning();  Unnecessary

            return true;
        }
    }

    protected Broadcaster getDefaultButtonBroadcaster() {
        return defaultButtonBroadcaster;
    }
    private void requestDefaultButton(JButton btn) {
        defaultButtonBroadcaster.notifyListeners(new BroadcastMessage(
                "RulesEditorDefaultButton", btn
                , "New default button requested"));
    }

    // Returns the desired default focus component
    protected JComponent getDefaultFocusComp() {
        return mtxtComment;
    }

    // Load the given rule file contents
    public void loadData(List<LaborRule> ruleFileContents) { // String[]
        // Clear input----------------------------------------------------------
        moRulesEditor.clearInput(); // clearInput();

        // Set data-------------------------------------------------------------
        mmdlRules.setRowData(ruleFileContents);

        // Adjust components----------------------------------------------------
        // Resize the table columns
        MyHandyTable.autoResizeTableColumns(mtblRules);

        // Resize the rest
        this.validate();

        // Set clean state------------------------------------------------------
        moRulesEditor.setClean(); // getDirtyHandler()
    }

    // Returns the current file data in Vector<LaborRule> format
    protected List<LaborRule> getCurrentFile() { // String[]
        return mmdlRules.getRowData();
    }

    // Converts Vector<Labor> into Vector<String> (.name property)
    private ArrayList<String> getLaborNames(final List<Labor> lstLabors) {
        ArrayList<String> lstReturn = new ArrayList<String>(lstLabors.size());

        for (final Labor labor : lstLabors)
            lstReturn.add(labor.getName());
        Collections.sort(lstReturn);
        return lstReturn;
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
                    + strSecond + ", and any other " + strFirst
                    + " WHITELIST labors.";
        else    // blacklist
            strMeaning = strFirst + " and " + strSecond
                    + " may never be done by the same citizen.";

        mtxtMeaning.setText(strMeaning);
    }

    @Override
    public void addDirtyListener(DirtyListener listener) {
        moRulesEditor.addDirtyListener(listener); // getDirtyHandler()
    }

    @Override
    public boolean isDirty() {
        return moRulesEditor.isDirty(); // getDirtyHandler()
    }
    @Override
    public void setClean() {
        moRulesEditor.setClean(); // getDirtyHandler()
    }

}
