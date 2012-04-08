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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.Collator;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import myutils.MyHandyTable;
import myutils.MyNumeric;
import myutils.MyTCRStripedHighlight;
import myutils.SortedComboBoxModel;

/**
 *
 * @author Tamara Orr
 */
public class ExclusionPanel extends JPanel implements DirtyForm, DirtyListener {

    private enum ExclusionAction { ADD, UPDATE, DELETE, EDIT }
    
    private static final String UNSELECTED_ATTRIBUTE = "-Select-";
    private static final String UNSELECTED_COMPARISON = "-Select-";
    private static final String COMPARISON_TEXT_ATTR_UNSELECTED = "(Select Attribute)";
    
    private String[] masDwarfProperties; // Dwarf supported properties
    
    // Models are set up in constructor
    private DefaultComboBoxModel moCompStringModel;
    private DefaultComboBoxModel moCompNumModel;
    private DefaultComboBoxModel moCompUnknownModel;
    private HashMap<String, ModelWithDescriptor> mhmAttrModelMap; // Maps attributes to comparison combo models
    private ModelWithDescriptor STRING_MODEL;
    private ModelWithDescriptor NUMERIC_MODEL;
    private ModelWithDescriptor UNKNOWN_MODEL;
    private ModelWithDescriptor moCurrentCompModel;
    
    // Exclusions by rule controls
    private JComboBox mcmbAttribute;
    private JComboBox mcmbComparison;
    private PlaceholderTextField mtxtValue;
    private PlaceholderTextField mtxtRuleName;
    private RuleExclusionTable moRuleTable;
    private CitizenList moRuleCitizen;
    private JLabel mlblMessage;
    
    // Exclusions by list controls
    private PlaceholderTextField mtxtListName;
    private ListExclusionTable moListTable;
    private CitizenList moListCitizen;
    private JButton mbtnListCitizenAdd;
    private CitizenNameCombo moCitizenNameCombo;
    
    private DwarfOrganizerIO moIO;
    private List<Dwarf> mlstCitizen;
    
    //private boolean mbDirty = true; //TODO
    private MasterDirtyHandler moMasterDirtyHandler;
    
    protected enum EditingState { NEW, EDIT }
    
    private class ModelWithDescriptor {
        private String description;
        private ComboBoxModel model;

        public ModelWithDescriptor(String description, ComboBoxModel model) {
            this.description = description;
            this.model = model;
        }

        public String getDescription() {
            return description;
        }

        public ComboBoxModel getModel() {
            return model;
        }
        
    }
    
    public ExclusionPanel(List<Exclusion> lstExclusion, List<Dwarf> lstCitizen
            , DwarfOrganizerIO io) {
        super();
        
        // Set local variables--------------------------------------------------
        moIO = io;
        mlstCitizen = lstCitizen;
        
        // Create objects-------------------------------------------------------
        moMasterDirtyHandler = new MasterDirtyHandler();
        
        // Create combo box models----------------------------------------------
        moCompStringModel = new DefaultComboBoxModel(merge(UNSELECTED_COMPARISON
            , ExclusionRule.maStringComparators));
        moCompNumModel = new DefaultComboBoxModel(merge(UNSELECTED_COMPARISON
            , ExclusionRule.maNumericComparators));
        moCompUnknownModel = new DefaultComboBoxModel(new Object[] {
            COMPARISON_TEXT_ATTR_UNSELECTED });
        
        // Create attribute->comparator combo model map
        STRING_MODEL = new ModelWithDescriptor("String", moCompStringModel);
        NUMERIC_MODEL = new ModelWithDescriptor("Numeric", moCompNumModel);
        UNKNOWN_MODEL = new ModelWithDescriptor("Unknown", moCompUnknownModel);
        masDwarfProperties = Dwarf.getSupportedProperties();
        Class[] acPropClasses = Dwarf.getSupportedPropClasses();
        mhmAttrModelMap = new HashMap<String, ModelWithDescriptor>();
        for (int iCount = 0; iCount < masDwarfProperties.length; iCount++) {
            String prop = masDwarfProperties[iCount];
            mhmAttrModelMap.put(prop, getCompModelForClass(acPropClasses[iCount]));
        }
        mhmAttrModelMap.put(UNSELECTED_ATTRIBUTE, UNKNOWN_MODEL);  // Unselected
        
        // Create UI elements---------------------------------------------------
        mcmbAttribute = createAttributeCombo();
        
        mcmbComparison = new JComboBox();
        mcmbComparison.setPrototypeDisplayValue("Greater than or equal to");    // Use this to calculate width
        
        mtxtValue = new PlaceholderTextField(8, null, true);
        mtxtRuleName = new PlaceholderTextField(20
                , "Enter a rule name (optional)", true);
        
        moCitizenNameCombo = new CitizenNameCombo();  // Must be created before mbtnListCitizenAdd

        mbtnListCitizenAdd = createListCitizenAddButton(moCitizenNameCombo); // Must be created before moListTable
        JButton btnListCitizenRemove = createListCitizenRemoveButton();
        
        moRuleCitizen = new CitizenList();  // Must be created before tables
        moListCitizen = new CitizenList();

        moRuleTable = new RuleExclusionTable(moRuleCitizen);
        moRuleTable.getDirtyHandler().addDirtyListener(this);
        moListTable = new ListExclusionTable(moListCitizen);
        moListTable.getDirtyHandler().addDirtyListener(this);
        
        // TODO: Handle default buttons
        final ExclusionActionButton cmdAddRule = new ExclusionActionButton("Add New"
                , ExclusionAction.ADD, moRuleTable);
        ExclusionActionButton cmdAddList = new ExclusionActionButton("Add New"
                , ExclusionAction.ADD, moListTable);
        ExclusionActionButton btnUpdateRule
                = new ExclusionActionButton("Update", ExclusionAction.UPDATE
                , moRuleTable);
        ExclusionActionButton btnUpdateList
                = new ExclusionActionButton("Update", ExclusionAction.UPDATE
                , moListTable);
        ExclusionActionButton btnEditRule = new ExclusionActionButton("Edit Rule"
                , ExclusionAction.EDIT, moRuleTable);
        ExclusionActionButton btnEditList = new ExclusionActionButton("Edit List"
                , ExclusionAction.EDIT, moListTable);
        ExclusionActionButton btnDeleteRule = new ExclusionActionButton("Delete Rule"
                , ExclusionAction.DELETE, moRuleTable);
        ExclusionActionButton btnDeleteList = new ExclusionActionButton("Delete List"
                , ExclusionAction.DELETE, moListTable);        
        
        mtxtListName = new PlaceholderTextField(20
                , "Enter a list name (optional)", true);
        
        JComboBox cmbCitizenName = moCitizenNameCombo.create();
        
        mlblMessage = new JLabel("(Message)");
        mlblMessage.setBorder(BorderFactory.createEtchedBorder());

        // Default buttons------------------------------------------------------
        HashMap<JButton, JComponent[]> hmDefaultButtons = new HashMap<JButton, JComponent[]>();
        hmDefaultButtons.put(cmdAddRule, new JComponent[] { mtxtRuleName
            , mcmbAttribute, mcmbComparison, mtxtValue });
        hmDefaultButtons.put(cmdAddList, new JComponent[] { mtxtListName });
        hmDefaultButtons.put(mbtnListCitizenAdd, new JComponent[] { cmbCitizenName });
        
        for (final JButton btn : hmDefaultButtons.keySet()) {
            for (JComponent comp : hmDefaultButtons.get(btn)) {
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
        }        
        // Initialize combo boxes-----------------------------------------------
        updateAllowedComparisons();
        
        // Build UI-------------------------------------------------------------
        JPanel ruleEntryPanel = new JPanel();
        ruleEntryPanel.setLayout(new FlowLayout());
        ruleEntryPanel.setBorder(BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Rule Name"), BorderLayout.NORTH);
        panel.add(mtxtRuleName, BorderLayout.CENTER);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Attribute"), BorderLayout.NORTH);
        panel.add(mcmbAttribute, BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Comparison"), BorderLayout.NORTH);
        panel.add(mcmbComparison, BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Value"), BorderLayout.NORTH);
        panel.add(mtxtValue, BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(cmdAddRule, BorderLayout.NORTH);
        panel.add(btnUpdateRule, BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        tablePanel.add(moRuleTable.create(lstExclusion, 325, 100, btnUpdateRule)
                , BorderLayout.CENTER);
        
        panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEADING));
        panel.add(btnEditRule);
        panel.add(btnDeleteRule);
        tablePanel.add(panel, BorderLayout.SOUTH);        
        
        JPanel panCitizenRule = new JPanel(new BorderLayout());
        panCitizenRule.add(moRuleCitizen.create(), BorderLayout.CENTER);
        
        JPanel panRuleInfo = new JPanel(new BorderLayout());
        panRuleInfo.add(tablePanel, BorderLayout.CENTER);
        panRuleInfo.add(panCitizenRule, BorderLayout.EAST);
        
        JPanel rulePanel = new JPanel();
        rulePanel.setLayout(new BorderLayout());
        rulePanel.add(ruleEntryPanel, BorderLayout.NORTH);
        rulePanel.add(panRuleInfo, BorderLayout.SOUTH);
        rulePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createRaisedBevelBorder()
                , "Exclusions by rule"));
        
        // --------------------Exclusions by list
        JPanel listOfLists = new JPanel();
        listOfLists.setLayout(new BorderLayout());
        
        JPanel panListNameEdit = new JPanel();
        panListNameEdit.setLayout(new BorderLayout());
        panListNameEdit.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(mtxtListName, BorderLayout.NORTH);
        panListNameEdit.add(panel, BorderLayout.CENTER);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(cmdAddList, BorderLayout.NORTH);
        panel.add(btnUpdateList, BorderLayout.SOUTH);
        panListNameEdit.add(panel, BorderLayout.EAST);
        listOfLists.add(panListNameEdit, BorderLayout.NORTH);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());        
        panel.add(moListTable.create(lstExclusion, 350, 100, btnUpdateList)
                , BorderLayout.CENTER);
        listOfLists.add(panel, BorderLayout.CENTER);        
        
        panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(btnEditList);
        panel.add(btnDeleteList);
        listOfLists.add(panel, BorderLayout.SOUTH);
        
        JPanel panCitizenList = new JPanel();
        panCitizenList.setLayout(new BorderLayout());
        panCitizenList.add(moListCitizen.create(), BorderLayout.CENTER); // createCitizenList()
        
        JPanel panCitizenSelect = new JPanel();
        panCitizenSelect.setLayout(new BorderLayout());
        panCitizenSelect.add(new JLabel("Citizen"), BorderLayout.NORTH);
        panCitizenSelect.add(cmbCitizenName, BorderLayout.CENTER);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(btnListCitizenRemove, BorderLayout.WEST);
        panCitizenList.add(panel, BorderLayout.SOUTH);
        
        JPanel panAdd = new JPanel(new BorderLayout());   // new FlowLayout(FlowLayout.TRAILING)
        panAdd.add(mbtnListCitizenAdd, BorderLayout.SOUTH);
        
        JPanel panCitizenAdd = new JPanel();
        panCitizenAdd.setLayout(new BorderLayout());
        panCitizenAdd.setBorder(BorderFactory.createEtchedBorder());
        panCitizenAdd.add(panCitizenSelect, BorderLayout.WEST);
        panCitizenAdd.add(panAdd, BorderLayout.EAST);
        
        JPanel panCitizens = new JPanel();
        panCitizens.setLayout(new BorderLayout());
        panCitizens.add(panCitizenAdd, BorderLayout.NORTH);
        panCitizens.add(panCitizenList, BorderLayout.SOUTH);
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createRaisedBevelBorder()
                , "Exclusions by list"));
        listPanel.add(listOfLists, BorderLayout.CENTER);
        listPanel.add(panCitizens, BorderLayout.EAST);
        
        // ---Build UI---------
        this.setLayout(new BorderLayout());
        this.add(rulePanel, BorderLayout.NORTH);
        this.add(listPanel, BorderLayout.CENTER);
        this.add(mlblMessage, BorderLayout.SOUTH);
        
        // Default Button (TODO)-----------
        //this.getRootPane().setDefaultButton(cmdAddRule); Root pane null->doesn't work
    }
    
    private void requestDefaultButton(JButton btn) {
        //notifyDefaultButton(btn); TODO
    }
    
    private class ExclusionActionButton extends JButton {
        public ExclusionActionButton(String caption
                , final ExclusionAction action
                , final AbstractExclusionEditor editor) {
            super(caption);
        
            this.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (action.equals(ExclusionAction.ADD))
                        editor.addItem();
                    else if (action.equals(ExclusionAction.UPDATE))
                        editor.updateItem();
                    else if (action.equals(ExclusionAction.DELETE))
                        editor.deleteItem();
                    else if (action.equals(ExclusionAction.EDIT))
                        editor.editItem();
                    else
                        System.err.println("Unknown ExclusionAction: " + action);
                }
            });                                
        }
    }
    
/*    protected JMenuBar createMenuBar() {
        // TODO: Let the owner do this
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        
        JMenuItem menuItem = new JMenuItem("Save");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveExclusions();
            }
        });
        menu.add(menuItem);
        
        menu.add(new JSeparator());
        
        menuItem = new JMenuItem("Close");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        
        return menuBar;
    } */

    protected void saveExclusions() {
        
        Vector<Exclusion> lstExclusion = new Vector<Exclusion>();
        AbstractExclusionEditor[] editors = new AbstractExclusionEditor[] {
            moRuleTable, moListTable
        };
        
        for (AbstractExclusionEditor editor : editors) {
            Vector<TableItem> vItem = editor.getModel().getRowData();
            for (TableItem item : vItem) {
                lstExclusion.add(item.getExclusion());
            }
        }
        moIO.writeExclusions(lstExclusion);
    }
    
    protected class TableItem implements MyPropertyGetter {
        private boolean active;
        private Integer ID;
        private Exclusion exclusion;
        private Integer numCitizens;
            
        public TableItem(boolean active, Integer ID, Exclusion exclusion
                , Integer citizenCount) {
            this.active = active;
            this.ID = ID;
            this.exclusion = exclusion;
            this.numCitizens = citizenCount;
        }

        public Integer getNumCitizens() {
            return numCitizens;
        }
        public void setNumCitizens(Integer numCitizens) {
            this.numCitizens = numCitizens;
        }            
        public Integer getID() {
            return ID;
        }
        public boolean isActive() {
            return active;
        }
        public Exclusion getExclusion() {
            return exclusion;
        }

        @Override
        public Object getProperty(String propName, boolean humanReadable) {
            String prop = propName.toLowerCase();
            if (prop.equals("id"))
                return getID();
            else if (prop.equals("active"))
                return isActive();
            else if (prop.equals("numcitizens"))
                return getNumCitizens();
            else if (prop.startsWith("exclusion."))
                return getExclusion().getProperty(
                        propName.replace("exclusion.", ""), humanReadable);
            else
                return "Unknown property: " + propName;
        }

        @Override
        public long getKey() {
            return (long) this.ID;
        }
    }    
    
    private class ListExclusionTable extends AbstractExclusionEditor<ExclusionList> {

        public ListExclusionTable(CitizenList citizenList) {
            super(citizenList);
        }
        
        protected boolean addCitizen() {
            TableItem tableItem = this.getCurrentTableItem();
            if (tableItem != null) {
                ExclusionList excl = (ExclusionList) tableItem.getExclusion();
                //Dwarf citizen = this.getCitizenList().getSelectedCitizen();
                Dwarf citizen = moCitizenNameCombo.getSelectedCitizen();
                if (citizen != null) {
                    if (! excl.getCitizenList().contains(citizen)) {
                        excl.getCitizenList().add(citizen);
                        this.getDirtyHandler().setDirty(true);
                        this.setCitizenList(tableItem);
                        refreshNumCitizens(tableItem);
                    }
                    else
                        System.out.println("That citizen is already in this list.");
                }
                else
                    System.out.println("citizen null");
            }
            else
                System.out.println("tableItem null");
            return true;
        }
        private void refreshNumCitizens(TableItem tableItem) {
            TableItem newTableItem = new TableItem(tableItem.isActive()
                    , tableItem.getID(), tableItem.getExclusion()
                    , this.getNumCitizens(tableItem.getExclusion()));
            //System.out.println("New number of citizens: " + newTableItem.getNumCitizens());
            this.getModel().updateRowByKey(tableItem.getID(), newTableItem);
        }
        
        protected boolean removeCitizen() {
            TableItem tableItem = this.getCurrentTableItem();
            if (tableItem != null) {
                ExclusionList excl = (ExclusionList) tableItem.getExclusion();
                Dwarf citizen = this.getCitizenList().getSelectedCitizen();
                if (citizen != null) {
                    excl.getCitizenList().remove(citizen);
                    this.getDirtyHandler().setDirty(true);
                    this.setCitizenList(tableItem);
                    refreshNumCitizens(tableItem);
                }
                else
                    System.out.println("citizen null");
            }
            else
                System.out.println("tableItem null");
            return true;
        }
        
        // Enable/disable the add button as a currently-selected exclusion is set
        @Override
        protected void setCurrentTableItem(TableItem tableItem) {
            super.setCurrentTableItem(tableItem);
            mbtnListCitizenAdd.setEnabled(tableItem != null);
        }
        
        @Override
        public void clearInput() {
            mtxtListName.setText("");
        }

        @Override
        public boolean validateInput() {
            return true;
        }

        @Override
        public TableItem createRowData() {
            ExclusionList list = new ExclusionList(moIO.incrementExclusionID()
                , mtxtListName.getText(), new Vector<Dwarf>());
            return new TableItem(true, list.getID(), list, 0);
        }

        @Override
        public boolean rowDataToInput(TableItem rowData) {
            mtxtListName.setText(rowData.getExclusion().getName());
            return true;
        }

        @Override
        public Vector<Object> getTableCols() {
            return new Vector<Object>(Arrays.asList(
                new Object[] { "Active", "Name", "Citizens"}));
        }

        @Override
        public Class[] getColClasses() {
            return new Class[] {Boolean.class, String.class, Integer.class };
        }

        @Override
        public String[] getColProperties() {
            return new String[] { "active", "exclusion.name", "numcitizens" };
        }

        @Override
        public Vector<TableItem> toTableItems(List<Exclusion> lstExclusion) {
            Vector<TableItem> vReturn = new Vector<TableItem>();
            for (Exclusion excl : lstExclusion) {
                if (excl.getClass().equals(ExclusionList.class)) {
                    ExclusionList list = (ExclusionList) excl;
                    vReturn.add(new TableItem(true, excl.getID(), excl
                        , super.getNumCitizens(list)));
                }
            }
            return vReturn;
        }
    }    
    
    private abstract class AbstractExclusionEditor<T extends Exclusion>
            extends AbstractEditor<TableItem> {
        
        private MyTableModel<TableItem> exclModel;
        private JTable exclTable;
        
        private Vector<Dwarf> EMPTY_CITIZEN_LIST;
        
        private TableItem moCurrentTableItem;
        private CitizenList moCitizenList;
        
        public abstract Vector<Object> getTableCols();
        public abstract Class[] getColClasses();
        public abstract String[] getColProperties();
        public abstract Vector<TableItem> toTableItems(List<Exclusion> lstExclusion);
        
        public AbstractExclusionEditor(CitizenList citizenList) {
            super();
            
            // Set "constants"--------------------------------------------------
            EMPTY_CITIZEN_LIST = new Vector<Dwarf>();
            
            // Set variables
            moCitizenList = citizenList;
            setCurrentTableItem(null);
        }

        public CitizenList getCitizenList() {
            return moCitizenList;
        }

        public MyTableModel<TableItem> getModel() {
            return exclModel;
        }
        
        // Returns the currently user-highlighted exclusion in the list.
        // This can be different from the current edited exclusion!
        public TableItem getCurrentTableItem() {
            return moCurrentTableItem;
        }
        
        // Always use this function to set the current selected exclusion
        // @see getCurrentExclusion()
        protected void setCurrentTableItem(TableItem tableItem) {
            moCurrentTableItem = tableItem;
        }
        
        public JScrollPane create(List<Exclusion> lstExclusion, int prefWidth
                , int prefHeight, JButton btnUpdate) {
            
            SortKeySwapper swapper = new SortKeySwapper();
            exclModel = new MyTableModel<TableItem>(getTableCols(), getColClasses()
                    , getColProperties()
                    , toTableItems(lstExclusion), swapper);
            exclModel.addEditableException(0);      // TODO: lookup column index from col name. Active checkbox editable
            
            exclTable = new JTable(exclModel);
            swapper.setTable(exclTable);
            exclTable.getSelectionModel().setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION);
            
            // Add selection listener
            exclTable.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (! e.getValueIsAdjusting()) {    // Be sure mouse button released
                        for (int iCount = e.getFirstIndex(); iCount <= e.getLastIndex(); iCount++) {
                            if (exclTable.getSelectionModel().isSelectedIndex(iCount)) {
                                // Set the citizen list to the first selected exclusion
                                int modelIndex = exclTable.convertRowIndexToModel(
                                        iCount);
                                setCurrentTableItem(exclModel.getRowData().get(modelIndex));
                                setCitizenList(modelIndex);
                                break;
                            }
                        }
                    }
                }
            });
            
            JScrollPane spReturn = new JScrollPane(exclTable);
            spReturn.setPreferredSize(new Dimension(prefWidth, prefHeight)); // w, h

            MyHandyTable.autoResizeTableColumns(exclTable, spReturn);
            MyHandyTable.autoSortTable(exclTable, exclModel);

            super.initialize(exclTable, exclModel, btnUpdate, false, false
                    , true, new int[] { KeyEvent.VK_ENTER }
                    , new int[] { KeyEvent.VK_DELETE });
            
            return spReturn;
        }
        
        protected void setCitizenList(int modelRow) {
            if (modelRow >= 0) {
                TableItem tableItem = exclModel.getRowData().get(modelRow);
                setCitizenList(tableItem);
            }
            else
                moCitizenList.setList(EMPTY_CITIZEN_LIST);
        }
        protected void setCitizenList(TableItem tableItem) {
            Vector<Dwarf> list = new Vector<Dwarf>();
            for (Dwarf citizen : mlstCitizen) {
                if (((T) tableItem.getExclusion()).appliesTo(citizen)) {
                    list.add(citizen);
                }
            }
            moCitizenList.setList(list);
        }
        
        // Returns the number of citizens affected by the given exclusion
        protected int getNumCitizens(Exclusion exclusion) {
            int count = 0;
            for (Dwarf citizen : mlstCitizen) {
                if (exclusion.appliesTo(citizen))
                    count++;
            }
            return count;
        }
        
        protected void addItem() {
            if (! super.addRecord())
                System.out.println(" Failed to create new exclusion.");            
            else
                setCitizenList(super.getCurrentEditedRow());            
        }
        protected void updateItem() {
            if (! super.updateRecord())
                System.out.println(" Failed to update exclusion.");
            else
                setCitizenList(super.getCurrentEditedRow());
        }
        protected void editItem() {
            if (! super.editRow())
                System.out.println(" Failed to translate exclusion.");
            else
                setCitizenList(super.getCurrentEditedRow());                
        }
        protected void deleteItem() {
            if (! super.deleteRow())
                System.out.println(" Failed to delete exclusion.");
            else
                setCitizenList(super.getCurrentEditedRow());                
        }
        
    }
    
    private class RuleExclusionTable extends AbstractExclusionEditor<ExclusionRule> { // AbstractEditor<TableItem> {
                                  
        public RuleExclusionTable(CitizenList citizenList) {
            super(citizenList);
        }
        
        @Override
        public Vector<TableItem> toTableItems(List<Exclusion> lstExclusion) {
            Vector<TableItem> vReturn = new Vector<TableItem>();
            for (Exclusion excl : lstExclusion) {
                if (excl.getClass().equals(ExclusionRule.class)) {
                    ExclusionRule rule = (ExclusionRule) excl;
                    vReturn.add(new TableItem(true, excl.getID(), excl
                        , getNumCitizens(rule)));
                }
            }
            return vReturn;
        }

        // Clears input controls
        @Override
        public void clearInput() {
            mtxtRuleName.setText("");
            mcmbAttribute.setSelectedItem(UNSELECTED_ATTRIBUTE);
            // (mcmbComparison should clear itself when mcmbAttribute is cleared)
            mtxtValue.setText("");
        }

        @Override
        public boolean validateInput() {
            try {
                if (mcmbAttribute.getSelectedItem().equals(UNSELECTED_ATTRIBUTE)
                        || mcmbComparison.getSelectedItem().equals(UNSELECTED_COMPARISON)
                        || mcmbComparison.getSelectedItem().equals(COMPARISON_TEXT_ATTR_UNSELECTED)
                        || ! validateValue())
                    return false;
                else
                    return true;
            } catch (Exception ignore) {
                return false;
            }            
        }
        private boolean validateValue() {
            String value = mtxtValue.getText();

            try {
                if (moCurrentCompModel.getDescription().equals("String"))
                    return true;
                else if (moCurrentCompModel.getDescription().equals("Numeric")) {
                    int temp = Integer.parseInt(value); // will throw error to be caught if it can't parse
                    return true;
                }
                else if (moCurrentCompModel.getDescription().equals("Unknown"))
                    return false;
                else
                    return false;
            } catch (Exception e) {
                return false;
            }
        }
        
        @Override
        public TableItem createRowData() {  // Vector<Object>
            ExclusionRule rule = new ExclusionRule(moIO.incrementExclusionID()
                , mtxtRuleName.getText(), mcmbAttribute.getSelectedItem().toString()
                , mcmbComparison.getSelectedItem().toString(), mtxtValue.getText());
            return new TableItem(true, rule.getID(), rule, getNumCitizens(rule));
        }
        
        @Override
        public boolean rowDataToInput(TableItem rowData) {   
            try {
                clearInput();

                setControlValues(rowData);    
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        private void setControlValues(TableItem item) {
            
            if (item != null) {  
                ExclusionRule rule = (ExclusionRule) item.getExclusion();
                mtxtRuleName.setText(item.getExclusion().getName());
                mcmbAttribute.setSelectedItem(rule.getPropertyName());
                mcmbComparison.setSelectedItem(rule.getComparator());
                mtxtValue.setText(rule.getValue().toString());
            }
        }
        
        @Override
        public Vector<Object> getTableCols() {
            return new Vector<Object>(Arrays.asList(
                new Object[] { "Active", "Citizens", "Name", "Attribute"
                        , "Comparison", "Value" }));  // Column identifiers
        }

        @Override
        public Class[] getColClasses() {
            return new Class[] {Boolean.class, Integer.class
                , String.class
                , String.class, String.class, Object.class };   // Column classes        

        }

        @Override
        public String[] getColProperties() {
            return new String[] { "active", "numcitizens"
                , "exclusion.name", "exclusion.propertyname"
                , "exclusion.comparator", "exclusion.value"};     // MyPropertyGetter properties
        }        
    }
    class CitizenList {
        
        private MyTableModel<Dwarf> model;
        private JTable table;
        
        public CitizenList() {
            super();
        }
        public JScrollPane create() {
            SortKeySwapper swapper = new SortKeySwapper();
            model = new MyTableModel(new Object[] { "Citizen" }
                , new Class[] { String.class }, new String[] { "name" }
                , new Vector<Dwarf>(), swapper);
            table = new JTable(model);
            table.setDefaultRenderer(Object.class, new MyTCRStripedHighlight(2));
            //MyHandyTable.autoSortTable(table, model, 1);  // Works for non-empty tables
            MyHandyTable.sortByCol(table, model, 0, SortOrder.ASCENDING);   // Works for empty tables
            //TableRowSorter rowSorter = new TableRowSorter<MyTableModel>(model);   // Works for any
            //rowSorter.toggleSortOrder(0);   // Sort by citizen name
            //table.setRowSorter(rowSorter);
            swapper.setTable(table);
            
            JScrollPane spReturn = new JScrollPane(table);
            spReturn.setPreferredSize(new Dimension(220, 100)); // w, h
            return spReturn;
        }
        public void setList(Vector<Dwarf> list) {
            for (int iCount = model.getRowData().size() - 1; iCount >= 0; iCount--)
                model.removeRow(iCount);
            model.addRows(list);
        }
        public void addCitizen(Dwarf citizen) {
            model.addRow(citizen);
            System.out.println("Setting dirty = true");
            //moMasterDirtyHandler.setDirty(true);
        }
        // Removes the current selected citizen from the list
        public void removeCitizen() {
            int tableRow = table.getSelectedRow();
            if (tableRow >= 0) {
                model.removeRow(table.convertRowIndexToModel(tableRow));
                System.out.println("Setting dirty = true");
                //moMasterDirtyHandler.setDirty(true);
            }
        }
        public Dwarf getSelectedCitizen() {
            Dwarf oReturn = null;
            int tableRow = table.getSelectedRow();
            if (tableRow >=0) {
                oReturn = model.getRowData().get(table.convertRowIndexToModel(tableRow));
            }
                    
            return oReturn;
        }
    }
    
    private Vector<String> getCitizenNames(List<Dwarf> list) {
        Vector<String> vReturn = new Vector<String>();
        for (Dwarf dwarf : list) {
            vReturn.add(dwarf.name);
        }
        return vReturn;
    }
    
    private Vector<String> merge(String entry, String[] array) {
        Vector<String> vReturn = new Vector<String>(Arrays.asList(array));
        vReturn.add(0, entry);
        return vReturn;
    }
    
    private JComboBox createAttributeCombo() {
        JComboBox cmbReturn = new JComboBox(merge(UNSELECTED_ATTRIBUTE
                , masDwarfProperties));
        cmbReturn.setPrototypeDisplayValue("Nickname");
        cmbReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAllowedComparisons();
            }
        });
        
        return cmbReturn;
    }
    private void updateAllowedComparisons() {
        String strAttribute = mcmbAttribute.getSelectedItem().toString();
        
        // Choose the current comparison model
        if (strAttribute != null)
            moCurrentCompModel = mhmAttrModelMap.get(strAttribute);
        else
            moCurrentCompModel = mhmAttrModelMap.get("Unknown");
        mcmbComparison.setModel(moCurrentCompModel.getModel());
            
    }
    private ModelWithDescriptor getCompModelForClass(Class cls) {
        if (MyNumeric.isNumericClass(cls))
            return NUMERIC_MODEL;
        else if (cls.equals(String.class))
            return STRING_MODEL;
        else
            return UNKNOWN_MODEL;
    }
    private class CitizenNameCombo {
        
        JComboBox combo;
        
        public CitizenNameCombo() {
            super();
        }
        public JComboBox create() {
            // Alphabetize by name
            SortedComboBoxModel<Dwarf> model = new SortedComboBoxModel(
                    getCitizenNames(mlstCitizen), Collator.getInstance());
            combo = new JComboBox(model);
            return combo;            
        }
        public Dwarf getSelectedCitizen() {
            String strName = combo.getSelectedItem().toString();
            for (Dwarf citizen : mlstCitizen) {
                if (citizen.name.equals(strName))
                    return citizen;
            }
            return null;
        }
    }

    private JButton createListCitizenAddButton(final CitizenNameCombo combo) {
        JButton btnReturn = new JButton("Add");
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moListTable.addCitizen();
            }
        });        
        return btnReturn;
    }
    
    private JButton createListCitizenRemoveButton() {
        JButton btnReturn = new JButton("Remove from List");
        btnReturn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moListTable.removeCitizen();
            }
        });
        return btnReturn;
    }

    @Override
    public DirtyHandler getDirtyHandler() {
        return moMasterDirtyHandler;
    }

    // If children are dirty, consider the whole form to be dirty
    @Override
    public void dirtyChanged(boolean newDirtyState) {
        moMasterDirtyHandler.setDirty(
                moMasterDirtyHandler.isDirty() || newDirtyState);
    }
    
    // Sets children clean when set clean
    private class MasterDirtyHandler extends DirtyHandler {
        @Override
        public void setClean() {
            moRuleTable.getDirtyHandler().setClean();
            moListTable.getDirtyHandler().setClean();
            super.setClean();
        }
    }
    
}
