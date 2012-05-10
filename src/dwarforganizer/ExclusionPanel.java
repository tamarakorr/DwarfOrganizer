/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.dirty.DirtyHandler;
import dwarforganizer.dirty.DirtyListener;
import dwarforganizer.dirty.DirtyForm;
import dwarforganizer.swing.MyEditableTableModel;
import dwarforganizer.swing.MyTableModel;
import dwarforganizer.swing.SortKeySwapper;
import dwarforganizer.swing.PlaceholderTextField;
import dwarforganizer.broadcast.BroadcastMessage;
import dwarforganizer.broadcast.Broadcaster;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collection;
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
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import myutils.Adapters.KeyTypedAdapter;
import myutils.MyHandyTable;
import myutils.MyNumeric;
import myutils.MySimpleTableModel;
import myutils.MyTCRStripedHighlight;
import myutils.SortedComboBoxModel;

/**
 * For creating rules that exclude dwarves, and lists of excluded dwarves
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class ExclusionPanel extends JPanel implements DirtyForm, DirtyListener {

    private enum ExclusionAction { ADD, UPDATE, DELETE, EDIT }

    private static final String UNSELECTED_ATTRIBUTE = "-Select-";
    private static final String UNSELECTED_COMPARISON = "-Select-";
    private static final String COMPARISON_TEXT_ATTR_UNSELECTED = "(Select Attribute)";

    private static final String ACTIVE_COL_IDENTIFIER = "Active";

    //private Hashtable<Integer, Boolean> mhtExclusionsActive;

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
    private JScrollPane mspRule;

    // Exclusions by list controls
    private PlaceholderTextField mtxtListName;
    private ListExclusionTable moListTable;
    private DeletableCitizenList moListCitizen;
    private JButton mbtnListCitizenAdd;
    private CitizenNameCombo moCitizenNameCombo;
    private JScrollPane mspList;

    private DwarfOrganizerIO moIO;
    private Vector<Dwarf> mlstCitizen;

    private MasterDirtyHandler moMasterDirtyHandler;

    private Broadcaster defaultButtonBroadcaster;
    private Broadcaster appliedBroadcaster;
    private Broadcaster closeBroadcaster;

    protected enum EditingState { NEW, EDIT }

    public ExclusionPanel(DwarfOrganizerIO io) {
        super();

        // Set local variables--------------------------------------------------
        moIO = io;

        // Create objects-------------------------------------------------------
        moMasterDirtyHandler = new MasterDirtyHandler();
        defaultButtonBroadcaster = new Broadcaster();
        //exclusionActiveBroadcaster = new Broadcaster();
        appliedBroadcaster = new Broadcaster();
        closeBroadcaster = new Broadcaster();

        // Defaults-------------------------------------------------------------
        mlstCitizen = new Vector<Dwarf>();

        // Create combo box models----------------------------------------------
        moCompStringModel = new DefaultComboBoxModel(merge(UNSELECTED_COMPARISON
            , ExclusionRule.STRING_COMPARATORS));
        moCompNumModel = new DefaultComboBoxModel(merge(UNSELECTED_COMPARISON
            , ExclusionRule.NUMERIC_COMPARATORS));
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

        mbtnListCitizenAdd = createListCitizenAddButton(); // Must be created before moListTable   // moCitizenNameCombo
        JButton btnListCitizenRemove = createListCitizenRemoveButton();

        moRuleCitizen = new CitizenList();  // Must be created before tables
        moListCitizen = new DeletableCitizenList();

        moRuleTable = new RuleExclusionTable(moRuleCitizen);
        moRuleTable.addDirtyListener(this);
        moListTable = new ListExclusionTable(moListCitizen);
        moListTable.addDirtyListener(this);
        moListCitizen.setEditor(moListTable);

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

        mspRule = moRuleTable.create(new Vector<Exclusion>(), 325, 100, btnUpdateRule); // lstExclusion
        mspList = moListTable.create(new Vector<Exclusion>(), 325, 100, btnUpdateList); // lstExclusion

        mtxtListName = new PlaceholderTextField(20
                , "Enter a list name (optional)", true);

        JComboBox cmbCitizenName = moCitizenNameCombo.create();

        mlblMessage = new JLabel("(Message)");
        mlblMessage.setBorder(BorderFactory.createEtchedBorder());

        JButton btnSave = new JButton("Save and Apply");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveExclusions();
            }
        });

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeBroadcaster.notifyListeners(new BroadcastMessage("CloseExclusions"
                        , null, "Exclusions panel requests to be closed"));
            }
        });

        // Default buttons------------------------------------------------------
        JComponent[] compsAddRule = new JComponent[] { mtxtRuleName
            , mcmbAttribute, mcmbComparison, mtxtValue };
        JComponent[] compsAddList = new JComponent[] { mtxtListName };
        JComponent[] compsListCitizenAdd = new JComponent[] { cmbCitizenName };
        JComponent[] compsNull = new JComponent[] { moRuleTable.getTable()
                , moListTable.getTable() };
        final HashMap<JComponent, JButton> hmDefaultButtons
                = new HashMap<JComponent, JButton>();

        for (JComponent comp : compsAddRule)
            hmDefaultButtons.put(comp, cmdAddRule);
        for (JComponent comp : compsAddList)
            hmDefaultButtons.put(comp, cmdAddList);
        for (JComponent comp : compsListCitizenAdd)
            hmDefaultButtons.put(comp, mbtnListCitizenAdd);
        for (JComponent comp : compsNull)
            hmDefaultButtons.put(comp, null);

        for (final JComponent comp : hmDefaultButtons.keySet()) {
            comp.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    requestDefaultButton(hmDefaultButtons.get(comp));
                }
                @Override
                public void focusLost(FocusEvent e) {
                }
            });
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

        tablePanel.add(mspRule, BorderLayout.CENTER);

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
                BorderFactory.createLoweredBevelBorder() //Raised
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
        panel.add(mspList, BorderLayout.CENTER);
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
        panCitizens.add(panCitizenList, BorderLayout.CENTER);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLoweredBevelBorder()    // Raised
                , "Exclusions by list"));
        listPanel.add(listOfLists, BorderLayout.CENTER);
        listPanel.add(panCitizens, BorderLayout.EAST);

        JPanel messagePanel = new JPanel(new BorderLayout());

        panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        //panel.add(btnApply);
        panel.add(btnSave);
        panel.add(btnClose);
        messagePanel.add(mlblMessage, BorderLayout.SOUTH);
        messagePanel.add(panel, BorderLayout.NORTH);

        // ---Build UI---------
        this.setLayout(new BorderLayout());
        this.add(rulePanel, BorderLayout.NORTH);
        this.add(listPanel, BorderLayout.CENTER);
        this.add(messagePanel, BorderLayout.SOUTH); // mlblMessage

    }
    public void loadData(Collection<Exclusion> colExclusion
            , Vector<Dwarf> vCitizen) {

        //for (Exclusion excl : colExclusion)
            //System.out.println("Exclusion #" + excl.getID() + " is "
            //    + (excl.isActive() ? "" : "in") + "active");

        // Clear input----------------------------------------------------------
        setMessage("(Message)");
        moRuleTable.clearInput();
        moListTable.clearInput();
        moRuleCitizen.setList(new Vector<String>());
        moListCitizen.setList(new Vector<String>());

        // Set citizen list-----------------------------------------------------
        // Must be done before loading rule/list tables, or citizen counts will
        // be inaccurate
        moCitizenNameCombo.setCitizenList(vCitizen);
        mlstCitizen = vCitizen;

        // Set exclusion rules/lists--------------------------------------------
        moRuleTable.loadData(colExclusion);
        moListTable.loadData(colExclusion);

        // Resize components----------------------------------------------------
        // Resize table columns
        MyHandyTable.autoResizeTableColumns(moRuleTable.getTable());
        MyHandyTable.autoResizeTableColumns(moListTable.getTable());

        // Do layout for other components
        this.validate();

        // Set clean state------------------------------------------------------
        moMasterDirtyHandler.setClean();
    }

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

    private void requestDefaultButton(JButton btn) {
        defaultButtonBroadcaster.notifyListeners(new BroadcastMessage(
                "ExclusionPanelDefaultButton", btn
                , "New default button requested"));
    }
    public Broadcaster getDefaultButtonBroadcaster() {
        return defaultButtonBroadcaster;
    }
/*    public Broadcaster getExclusionActiveBroadcaster() {
        return exclusionActiveBroadcaster;
    } */
    public Broadcaster getAppliedBroadcaster() {
        return appliedBroadcaster;
    }
    public Broadcaster getCloseBroadcaster() {
        return closeBroadcaster;
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

    // Translate table items to exclusion objects, write to file,
    // set form state to clean, and send update to listeners (dwarf list)
    protected void saveExclusions() {

        setMessage("(Saving...)");

        // Translate list contents to a list of exclusions
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

        // Write exclusions to file
        moIO.writeExclusions(lstExclusion);

        // Set form state to clean
        moMasterDirtyHandler.setClean();

        // Send the update to listeners
        appliedBroadcaster.notifyListeners(new BroadcastMessage("ExclusionsApplied"
                , lstExclusion, "Exclusions were applied"));

        setMessage("Save complete.");
    }

    protected class TableItem implements MyPropertyGetter, MyPropertySetter {
        private Integer ID;
        private Exclusion exclusion;
        private Integer numCitizens;

        public TableItem(Integer ID, Exclusion exclusion
                , Integer citizenCount) {
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

        public Exclusion getExclusion() {
            return exclusion;
        }

        public void setID(Integer ID) {
            this.ID = ID;
        }

        public void setExclusion(Exclusion exclusion) {
            this.exclusion = exclusion;
        }

        @Override
        public Object getProperty(String propName, boolean humanReadable) {
            String prop = propName.toLowerCase();
            if (prop.equals("id"))
                return getID();
            /*else if (prop.equals("active"))
                return isActive(); */
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

        @Override
        public void setProperty(String propName, Object value) {
            try {
                String prop = propName.toLowerCase();
                if (prop.equals("id"))
                    setID((Integer) value);
                /*else if (prop.equals("active"))
                    setActive((Boolean) value); */
                else if (prop.startsWith("exclusion."))
                    getExclusion().setProperty(
                            propName.replace("exclusion.", ""), value);
                else
                    System.err.println("Unknown TableItem property: " + propName);
            } catch (Exception e) {
                System.err.println(e.getMessage() + " Failed to set TableItem property.");
            }
        }
    }

    // TODO: Doesn't work with stupid Nimbus boolean checkboxes
    // TODO: Cells may be misaligned in non-Nimbus due to hard-coded EmptyBorder insets
    private JTable createRowBorderTable(TableModel model) {

        final JTable table = new JTable(model) {
            // TODO: How can we obtain these insets for the inside border from the UI?
            // (Pixel spacing between cells in Nimbus is 5...by trial and error)
            Border outside = new MatteBorder(1, 0, 1, 0, Color.RED); // 1, 0, 1, 0, Color.RED (t l b r color)
            Border inside = new EmptyBorder(0, 5, 0, 5);    // 0, 1, 0, 1 (t l b r)
            Border highlightBorder = new CompoundBorder(outside, inside);

            @Override
            public Component prepareRenderer(TableCellRenderer renderer
                    , int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                JComponent jc = (JComponent) c;

                // Add a border to the selected row
                if (isRowSelected(row))
                    jc.setBorder(highlightBorder);
                return c;
            }
        };

        table.changeSelection(0, 0, false, false);
        return table;
    }

    private abstract class AbstractExclusionEditor<T extends Exclusion>
            extends AbstractEditor<TableItem> {

        private MyEditableTableModel<TableItem> exclModel;
        private JTable exclTable;

        private Vector<String> EMPTY_CITIZEN_LIST;

        private TableItem moCurrentTableItem;
        private CitizenList moCitizenList;

        private boolean bCreating;

        public abstract Vector<Object> getTableCols();
        public abstract Class[] getColClasses();
        public abstract String[] getColProperties();
        public abstract Vector<TableItem> toTableItems(Collection<Exclusion> lstExclusion);
        //public abstract Vector<C> toCitizenListFormat(Vector<Dwarf> vDwarf);
        public abstract void setCitizenList(TableItem tableItem);   // Should do getCitizenList().setCitizenList()

        public AbstractExclusionEditor(CitizenList citizenList) {
            super();

            // Set "constants"--------------------------------------------------
            EMPTY_CITIZEN_LIST = new Vector<String>(1);

            // Set variables
            moCitizenList = citizenList;  // List of citizens for this exclusion

            setCurrentTableItem(null);
        }

        public CitizenList getCitizenList() {
            return moCitizenList;
        }

        public MyTableModel<TableItem> getModel() {
            return exclModel;
        }
        public JTable getTable() {
            return exclTable;
        }

        // Returns the column index for the given column identifier.
        // Returns -1 if not found.
        public int getColIndex(String colIdentifier) {
            int iCount = 0;
            for (Object identifier : getTableCols()) {
                if (identifier.equals(colIdentifier))
                    return iCount;
                iCount++;
            }
            return -1;
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

        public void loadData(Collection<Exclusion> colExclusion) {
            bCreating = true;
            exclModel.setRowData(toTableItems(colExclusion));
            bCreating = false;
        }

        public JScrollPane create(Collection<Exclusion> lstExclusion, int prefWidth
                , int prefHeight, JButton btnUpdate) {

            SortKeySwapper swapper;

            bCreating = true;

            swapper = new SortKeySwapper();
            exclModel = new MyEditableTableModel<TableItem>(getTableCols(), getColClasses()
                    , getColProperties()
                    , toTableItems(lstExclusion), swapper);
            //exclModel.addEditableException(0);
            exclModel.addEditableException(ACTIVE_COL_IDENTIFIER); // "Active" checkbox editable

            // When the table is edited, update saved exclusions.
            exclModel.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    if (bCreating)
                        return;

                    int activeColumn = getColIndex(ACTIVE_COL_IDENTIFIER);
                    if (activeColumn >= 0) {
                        if (e.getType() == TableModelEvent.DELETE)
                            updateExclusions();
                        else if (e.getType() == TableModelEvent.INSERT)
                            updateExclusions();
                        else if (e.getType() == TableModelEvent.UPDATE)
                            if (e.getColumn() == TableModelEvent.ALL_COLUMNS
                                || e.getColumn() == activeColumn)
                                updateExclusions();
                    }
                }
            });

            //exclTable = new JTable(exclModel);
            exclTable = MyHandyTable.createSmarterFocusTable(new JTable(
                    exclModel)); // createRowBorderTable
            swapper.setTable(exclTable);

            // Single, full row selection
            exclTable.getSelectionModel().setSelectionMode(
                    ListSelectionModel.SINGLE_SELECTION);
            exclTable.setRowSelectionAllowed(true);
            exclTable.setColumnSelectionAllowed(false);

            // Add selection listener, to update citizen list when selection changes
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
                                Vector<TableItem> items = exclModel.getRowData();
                                setCurrentTableItem(items.get(modelIndex));
                                setCitizenList(modelIndex);
                                break;
                            }
                        }
                    }
                }
            });

            JScrollPane spReturn = new JScrollPane(exclTable);
            spReturn.setPreferredSize(new Dimension(prefWidth, prefHeight)); // w, h

            MyHandyTable.autoResizeTableColumns(exclTable);
            MyHandyTable.autoSortTable(exclTable);

            // TODO: It would be nice to set the default focus control for
            // editing, but this class is shared
            super.initialize(exclTable, exclModel, btnUpdate, false, false
                    , true, true, true, true, true, null); //, new int[] { KeyEvent.VK_ENTER }
                    //, new int[] { KeyEvent.VK_DELETE });

            bCreating = false;
            return spReturn;
        }

        protected void setCitizenList(int modelRow) {
            if (modelRow >= 0) {
                Vector<TableItem> items = exclModel.getRowData();
                TableItem tableItem = items.get(modelRow);
                setCitizenList(tableItem);
            }
            else
                moCitizenList.setList(EMPTY_CITIZEN_LIST);
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
                setMessage("Failed to create new exclusion.");
            else {
                setMessage("New exclusion created.");
                setCitizenList(super.getCurrentEditedRow());
            }
        }
        protected void updateItem() {
            if (! super.updateRecord())
                setMessage("Failed to update exclusion.");
            else {
                setMessage("Exclusion updated.");
                setCitizenList(super.getCurrentEditedRow());
            }
        }
        protected void editItem() {
            if (! super.editRow())
                setMessage(" Failed to translate exclusion.");
            else {
                setCitizenList(super.getCurrentEditedRow());
            }
        }
        protected void deleteItem() {
            if (! super.deleteRow())
                setMessage(" Failed to delete exclusion.");
            else {
                setMessage("Deleted exclusion.");
                setCitizenList(super.getCurrentEditedRow());
            }
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
                String citizen = moCitizenNameCombo.getSelectedCitizenName();
                if (citizen != null) {
                    if (! excl.getCitizenList().contains(citizen)) {
                        excl.getCitizenList().add(citizen);
                        this.getDirtyHandler().setDirty(true);
                        this.setCitizenList(tableItem);
                        refreshNumCitizens(tableItem);
                    }
                    else
                        setMessage("That citizen is already in this list.");
                }
                else
                    setMessage("Error: Citizen null");
            }
            else
                setMessage("Error: TableItem null");
            return true;
        }
        private void refreshNumCitizens(TableItem tableItem) {
            tableItem.setNumCitizens(this.getNumCitizens(tableItem.getExclusion()));
            this.getModel().fireUpdateForKey(tableItem.getKey());

/*            TableItem newTableItem = new TableItem(
                    tableItem.getID(), tableItem.getExclusion()
                    , this.getNumCitizens(tableItem.getExclusion())); // tableItem.isActive(),
            //setMessage("New number of citizens: " + newTableItem.getNumCitizens());
            this.getModel().updateRowByKey(tableItem.getID(), newTableItem); */
        }

        protected boolean removeCitizen() {
            TableItem tableItem = this.getCurrentTableItem();
            if (tableItem != null) {
                ExclusionList excl = (ExclusionList) tableItem.getExclusion();
                //Dwarf citizen = this.getCitizenList().getSelectedCitizen();
                String citizen = this.getCitizenList().getSelectedCitizen().toString();
                //System.out.println("Attempting to remove " + citizen);
                if (citizen != null) {
                    //int index = indexOfCitizen(excl.getCitizenList(), citizen);
                    //if (index >= 0)
                    //    excl.getCitizenList().remove(index);
                    excl.getCitizenList().remove(citizen);
                    setMessage("Removed " + citizen + " from " + excl.getName());
                    //System.out.println("Citizen list now has " + excl.getCitizenList().size() + " citizen(s)");
                    this.getDirtyHandler().setDirty(true);
                    this.setCitizenList(tableItem);
                    refreshNumCitizens(tableItem);
                }
                else
                    setMessage("Error: Citizen null");
            }
            else
                setMessage("Error: TableItem null");
            return true;
        }
/*        private int indexOfCitizen(Vector<String> vCitizens, String citizenName) {
            for (int iCount = 0; iCount < vCitizens.size(); iCount++) {
                if (vCitizens.get(iCount).equals(citizenName)) {
                    return iCount;
                }
                //else
                    //System.out.println("Checking " + vCitizens.get(iCount) + " for " + citizenName);
            }
            return -1;
        } */

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
        public TableItem createRowData(boolean isNew) {
            int ID;

            if (isNew)
                ID = moIO.incrementExclusionID();
            else
                ID = this.getModel().getRowData().get(this.getCurrentEditedRow()).getID();

            ExclusionList list = new ExclusionList(ID
                , mtxtListName.getText(), true, new Vector<String>());
            return new TableItem(list.getID(), list, 0); // true
        }

        @Override
        public boolean rowDataToInput(TableItem rowData) {
            mtxtListName.setText(rowData.getExclusion().getName());
            return true;
        }

        @Override
        public Vector<Object> getTableCols() {
            return new Vector<Object>(Arrays.asList(
                new Object[] { ACTIVE_COL_IDENTIFIER, "Name", "Citizens"}));
        }

        @Override
        public Class[] getColClasses() {
            return new Class[] {Boolean.class, String.class, Integer.class };
        }

        @Override
        public String[] getColProperties() {
            return new String[] { "exclusion.active", "exclusion.name", "numcitizens" };
        }

        @Override
        public Vector<TableItem> toTableItems(Collection<Exclusion> lstExclusion) {
            Vector<TableItem> vReturn = new Vector<TableItem>();
            for (Exclusion excl : lstExclusion) {
                if (excl.getClass().equals(ExclusionList.class)) {
                    ExclusionList list = (ExclusionList) excl;
                    vReturn.add(new TableItem(excl.getID(), excl
                        , super.getNumCitizens(list))); // excl.isActive(),
                }
            }
            return vReturn;
        }

/*        @Override
        public Vector<String> toCitizenListFormat(Vector<Dwarf> vDwarf) {
            return vDwarfToVString(vDwarf);
        } */

        @Override
        public void setCitizenList(TableItem tableItem) {
            ExclusionList excl = (ExclusionList) tableItem.getExclusion();
            getCitizenList().setList(excl.getCitizenList());
        }
    }

    private Vector<String> vDwarfToVString(Vector<Dwarf> vDwarf) {
        Vector<String> vReturn = new Vector<String>(vDwarf.size());
        for (Dwarf dwarf : vDwarf) {
            vReturn.add(dwarf.getName());
        }
        return vReturn;
    }

    private class RuleExclusionTable extends AbstractExclusionEditor<ExclusionRule> { // AbstractEditor<TableItem> {

        public RuleExclusionTable(CitizenList citizenList) {
            super(citizenList);
        }

        @Override
        public Vector<TableItem> toTableItems(Collection<Exclusion> lstExclusion) {
            Vector<TableItem> vReturn = new Vector<TableItem>();
            for (Exclusion excl : lstExclusion) {
                if (excl.getClass().equals(ExclusionRule.class)) {
                    ExclusionRule rule = (ExclusionRule) excl;
                    vReturn.add(new TableItem(excl.getID(), excl
                        , getNumCitizens(rule))); // excl.isActive(),
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
        public TableItem createRowData(boolean isNew) {  // Vector<Object>

            int ID;

            if (isNew)
                ID = moIO.incrementExclusionID();
            else
                ID = this.getModel().getRowData().get(this.getCurrentEditedRow()).getID();

            ExclusionRule rule = new ExclusionRule(ID
                , mtxtRuleName.getText(), true, mcmbAttribute.getSelectedItem().toString()
                , mcmbComparison.getSelectedItem().toString(), mtxtValue.getText());
            return new TableItem(rule.getID(), rule, getNumCitizens(rule)); // true
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
                new Object[] { ACTIVE_COL_IDENTIFIER, "Citizens", "Name", "Attribute"
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
            return new String[] { "exclusion.active", "numcitizens"
                , "exclusion.name", "exclusion.propertyname"
                , "exclusion.comparator", "exclusion.value"};     // MyPropertyGetter properties
        }

        public Vector<String> toCitizenListFormat(Vector<Dwarf> vDwarf) {
            return vDwarfToVString(vDwarf);
        }

        @Override
        public void setCitizenList(TableItem tableItem) {
            Vector<Dwarf> list = new Vector<Dwarf>();
            for (Dwarf citizen : mlstCitizen) {
                if (((ExclusionRule) tableItem.getExclusion()).appliesTo(citizen)) {
                    list.add(citizen);
                }
            }
            getCitizenList().setList(toCitizenListFormat(list));
        }

    }

/*    private abstract class KeyTypedAdapter implements KeyListener {
        @Override
        public abstract void keyTyped(KeyEvent e);
        @Override
        public void keyPressed(KeyEvent e) { // Do nothing
        }
        @Override
        public void keyReleased(KeyEvent e) { // Do nothing
        }
    } */

    // A CitizenList with the [delete] key enabled, to call ListExclusionTable.removeCitizen()
    // setEditor() must be set before calling create()
    private class DeletableCitizenList extends CitizenList {

        private ListExclusionTable listExclTable;

        public DeletableCitizenList() {
            super();
        }
        public void setEditor(ListExclusionTable listExclTable) {
            this.listExclTable = listExclTable;
        }
        @Override
        public JScrollPane create() {
            JScrollPane jsp = super.create();

            this.getTable().addKeyListener(new KeyTypedAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (e.getKeyChar() == KeyEvent.VK_DELETE)
                        listExclTable.removeCitizen();
                }
            });
            return jsp;
        }
    }

    protected class CitizenList {

        //private MyTableModel<Dwarf> model;
        private MySimpleTableModel model;
        private JTable table;
        private Vector<Object> columns = new Vector<Object>(Arrays.asList(
                new Object[] { "Citizen" }));

        public CitizenList() {
            super();
        }
        protected JTable getTable() {
            return table;
        }
        public JScrollPane create() {
            //SortKeySwapper swapper = new SortKeySwapper();
            //model = new MyTableModel(new Object[] { "Citizen" }
            //    , new Class[] { String.class }, new String[] { "name" }
            //    , new Vector<Dwarf>(), swapper);
            model = new MySimpleTableModel(columns, 0);
            table = MyHandyTable.createSmarterFocusTable(new JTable(model));
            table.setDefaultRenderer(Object.class, new MyTCRStripedHighlight(2));
            MyHandyTable.sortByCol(table, 0, SortOrder.ASCENDING);   // Works for empty tables
            //swapper.setTable(table);

            JScrollPane spReturn = new JScrollPane(table);
            spReturn.setPreferredSize(new Dimension(220, 100)); // w, h
            return spReturn;
        }
        public void setList(Vector<String> list) {
            //model.setRowData(list);
            model.setDataVector(toVVO(list), columns);  // Seems to eat sorting
            MyHandyTable.sortByCol(table, 0, SortOrder.ASCENDING);   // setDataVector ate our sortkeys
        }
        private Vector<Vector<Object>> toVVO(Vector<String> list) {
            Vector<Vector<Object>> vReturn = new Vector<Vector<Object>>(list.size());
            for (String item : list) {
                vReturn.add(new Vector<Object>(Arrays.asList(new Object[] { item })));
            }
            return vReturn;
        }
        public void addCitizen(String citizen) {
            //model.addRow(citizen);
            model.addRow(new Vector<Object>(Arrays.asList(new Object[] { citizen })));
        }
        // Removes the current selected citizen from the list
        public void removeCitizen() {
            int tableRow = table.getSelectedRow();
            if (tableRow >= 0) {
                model.removeRow(table.convertRowIndexToModel(tableRow));
            }
        }
        public String getSelectedCitizen() {
            String oReturn = null;
            int tableRow = table.getSelectedRow();
            if (tableRow >=0) {
                //oReturn = model.getRowData().get(table.convertRowIndexToModel(tableRow));
                Vector<Object> vRow = (Vector<Object>) model.getDataVector().get(table.convertRowIndexToModel(tableRow));
                oReturn = vRow.get(0).toString();   // "Citizen" column
            }

            return oReturn;
        }
    }

    private void updateExclusions() {
        // Never mind - done differently now
        /*FunctionToDoOnEDT fte = new FunctionToDoOnEDT() {
            @Override
            public void runOnEDT() {
                Hashtable<Integer, Boolean> htActive = new Hashtable<Integer, Boolean>();

                AbstractExclusionEditor[] editors = new AbstractExclusionEditor[] {
                    moListTable, moRuleTable };
                for (AbstractExclusionEditor editor : editors) {
                    Vector<TableItem> vItem = editor.getModel().getRowData();
                    for (TableItem item : vItem) {
                        Exclusion exclusion = item.getExclusion();
                        htActive.put(exclusion.getID(), exclusion.isActive());
                    }
                }
                exclusionActiveBroadcaster.notifyListeners(new BroadcastMessage(
                        "ExclusionPanelActiveExclusions", htActive
                        , "Active exclusions changed"));
            }
        }; */
        //EventDispatchUtils.runFireOnEDT(fte, true);

        moMasterDirtyHandler.setDirty(true);        // For now, make the form dirty when "Active" is changed
    }

    private Vector<String> getCitizenNames(List<Dwarf> list) {
        Vector<String> vReturn = new Vector<String>(list.size());
        for (Dwarf dwarf : list) {
            vReturn.add(dwarf.getName());
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
        cmbReturn.setPrototypeDisplayValue("NicknameX");    // Add "X" because otherwise Nimbus will put "..." instead of last two characters
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
        SortedComboBoxModel<Dwarf> model;

        public CitizenNameCombo() {
            super();
        }
        public JComboBox create() {
            // Alphabetize by name
            model = new SortedComboBoxModel<Dwarf>();
            combo = new JComboBox(model);
            return combo;
        }
        public void setCitizenList(Vector<Dwarf> lstCitizen) {
            model = new SortedComboBoxModel<Dwarf>(
                    getCitizenNames(lstCitizen), Collator.getInstance());
            combo.setModel(model);
        }
        public Dwarf getSelectedCitizen() {
            String strName = combo.getSelectedItem().toString();
            for (Dwarf citizen : mlstCitizen) {
                if (citizen.getName().equals(strName))
                    return citizen;
            }
            return null;
        }
        public String getSelectedCitizenName() {
            return combo.getSelectedItem().toString();
        }
    }

    private JButton createListCitizenAddButton() { // final CitizenNameCombo combo
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

/*    @Override
    public DirtyHandler getDirtyHandler() {
        return moMasterDirtyHandler;
    } */

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
            moRuleTable.setClean(); // getDirtyHandler()
            moListTable.setClean(); // getDirtyHandler()
            super.setClean();
        }
    }

    private void setMessage(String newMessage) {
        mlblMessage.setText(newMessage);
    }

    @Override
    public void addDirtyListener(DirtyListener listener) {
        moMasterDirtyHandler.addDirtyListener(listener);
    }

    @Override
    public boolean isDirty() {
        return moMasterDirtyHandler.isDirty();
    }

    @Override
    public void setClean() {
        moMasterDirtyHandler.setClean();
    }
}
