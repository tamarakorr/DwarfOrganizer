/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import myutils.MyArrayUtils;
import myutils.MyHandyTable;
import myutils.MyTCRStripedHighlight;
import myutils.MyTCRStripedHighlightCheckBox;

/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public class DwarfListWindow extends JPanel implements BroadcastListener {

    private static final int INCLUDE_COLUMN = 0;  // Index of the "Include" column in the table
    private static final String DEFAULT_VIEW_NAME = "Default View";

    //private static final int MAX_DWARF_TIME = 100;
    //private static final String DEFAULT_DWARF_AGE = "999";
    //private static final String DEFAULT_TRAIT_VALUE = "50";

    //private String[] masSocialTraits = { "Friendliness", "Self_consciousness"
    //    , "Straightforwardness", "Cooperation", "Assertiveness" };

    /*private int[] strengthValues = { 2250, 2000, 1750, 1500, 1001, 751, 501, 251, 0 };
    private int[] patienceValues = { 2250, 2000, 1750, 1500, 1001, 501, 251, 0 };
    private int[] kinsenseValues = { 2000, 1750, 1500, 1250, 751, 501, 251, 1, 0 };
    private int[] focusValues = { 2542, 2292, 2042, 1792, 1293, 1043, 793, 543, 0 };
    private int[] agiValues = {1900, 1650, 1400, 1150, 651, 401, 151, 0 };
    */

    //private long[] plusplusRange = { 700, 1200, 1400, 1500, 1600, 1800, 2500 };
    //private long[] plusRange = { 450, 950, 1150, 1250, 1350, 1550, 2250 };
    //private long[] avgRange = { 200, 750, 900, 1000, 1100, 1300, 2000 };
    //private long[] minusRange = { 150, 600, 800, 900, 1000, 1100, 1500 };
    //private long[] socialRange = { 0, 10, 25, 61, 76, 91, 100 };

    /*private String[] strengthDesc = { "9 unbelievable", "8 mighty", "7 very strong"
            , "6 strong", "5 average", "4 weak", "3 very weak", "2 unquestionably weak"
            , "1 unfathomably weak", "0 ERROR" };
    private String[] kinsenseDesc = { "9 astounding", "8 great", "7 very good", "6 good"
                , "5 average", "4 meager", "3 poor", "2 very clumsy"
                , "1 unbelievably atrocious", "0 ERROR" };
    private String[] patienceDesc = { "9 absolutely boundless", "8 a deep well"
                , "7 great", "6 a sum of", "5 average", "4 a shortage"
                , "3 little", "2 very little", "1 no patience at all"
                , "0 ERROR" };
    private String[] agiDesc = { "9 amazing", "8 extreme", "7 very agile", "6 agile"
            , "5 average", "4 clumsy", "3 quite clumsy", "2 totally clumsy"
            , "1 abysmally clumsy", "0 ERROR" };
    private String[] focusDesc = { "9 unbreakable", "8 great", "7 very good"
        , "6 ability to focus", "5 average", "4 poor", "3 quite poor"
        , "2 really poor", "1 absolute inability", "0 ERROR" }; */
    //private String[] plusplusDesc = { "ERROR", "1 Very Poor", "2 Poor"
    //        , "3 Below Average", "4 Above Average", "5 Good", "6 Very Good" };
    private String[] masAttributeDesc = { "ERROR: Impossibly Low", "Very Poor"
            , "Poor", "Below Average", "Average", "Good", "Great"
            , "Extraordinary" };

    private String[] masPotentialDesc = { "Horrible", "Very Poor"
            , "Poor", "Below Average", "Above Average", "Good", "Very Good"
            , "Superb" };
    private int[] maiPotentialBracket = { 9, 24, 39, 49, 60, 75, 90 };

    private String[] masSkillLevelDesc = { "", "Dabbling", "Novice", "Adequate"
        , "Competent", "Skilled", "Proficient", "Talented", "Adept", "Expert"
        , "Professional", "Accomplished", "Great", "Master", "High Master"
        , "Grand Master", "Legendary", "Legendary +1", "Legendary +2"
        , "Legendary +3", "Legendary +4", "Legendary +5" };
    private int[] maiSkillLevelBracket = MyArrayUtils.createCountingArray(21); // (int) MainWindow.MAX_SKILL_LEVEL + 1

    private Hashtable<String, Stat> mhtStats;
    private Hashtable<String, Skill> mhtSkills;
    private Vector<Dwarf> mvDwarves;
    private Hashtable<String, MetaSkill> mhtMetaSkills;

    private CompositeTable moTable; // JTable
    private MyEditableTableModel<DwarfListItem> moModel;
    private JLabel mlblSelected;
    private JLabel mlblExclusions;
    private JLabel mlblPop;
    private JScrollPane mspScrollPane;

    private boolean mbLoading = false;

    // Any character 0 or more times, followed by a [, followed by one or more
    // digits, followed by a ], followed by any character 0 or more times:
    //private static Pattern mpatSkillLevel = Pattern.compile("(.*\\[)(\\d+)(\\].*)");

    private Vector<Labor> mvLabors; // Set in constructor

    private Map<String, GridView> moViews;  // Keyed by view name
    private Map<String, List<String>> moViewColumnGroups; // Keyed by view name, values are lists of column group keys
    private ViewHandler moViewHandler;

    private JMenuBar moMenuBar;
    private Map<String, JCheckBoxMenuItem> moColMenus;

    private VisibilityHandler moTableVis;

    private ColumnFreezingTable moFreezer;
    private HideableTableColumnModel moFreezerColModel;

    private enum ValueAndTextFormat {
        NUMBER ("Value"),
        TEXT ("Text"),
        NUMBER_AND_TEXT ("Value and Text");

        private String humanText;
        //private JMenuItem menuItem;

        ValueAndTextFormat(String humanText) {
            this.humanText = humanText;
        }
        public String getHumanText() { return humanText; }
    };
    private ValueAndTextFormat currentStatFormat;
    private Map<ValueAndTextFormat, JMenuItem> mmStatFormatMenus;

    private ValueAndTextFormat currentPotFormat;
    private Map<ValueAndTextFormat, JMenuItem> mmPotFormatMenus;

    private ValueAndTextFormat currentLevelFormat;
    private Map<ValueAndTextFormat, JMenuItem> mmLevelFormatMenus;

    public DwarfListWindow(Vector<Labor> vLabors, Hashtable<String, Stat> htStat
            , Hashtable<String, Skill> htSkill, Hashtable<String, MetaSkill> htMeta
            , Vector<LaborGroup> vLaborGroups) {

        // Parent constructor---------------------------------------------------
        super();

        // Set local variables--------------------------------------------------
        mvLabors = vLabors;
        mhtStats = htStat;
        mhtSkills = htSkill;
        mhtMetaSkills = htMeta;

        // Create objects-------------------------------------------------------
        Map[] skillMaps = { mhtSkills, mhtMetaSkills };

        mvDwarves = new Vector<Dwarf>(); //vDwarves;
        Collection<Exclusion> vExclusions = new Vector<Exclusion>();

        mbLoading = true;

        // Create view data-----------------------------------------------------
        moViewHandler = new ViewHandler(vLaborGroups);
        moModel = moViewHandler.createDwarfListModel(vExclusions);

        // Create the dwarf data table------------------------------------------
        JTable mainTable = createDwarfDataTable(moModel); // moTable
        mspScrollPane = new JScrollPane(mainTable);       // moTable
        mspScrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);  // For freeze

        // Sort by name
        MyHandyTable.handyTable(mainTable, moModel, true, 1, true);   // this
        moTableVis = new VisibilityHandler(mainTable);
        moTableVis.initialize();

        moViewHandler.createDwarfListViews();

        // Create the column freeze pane----------------------------------------
        // Should be done after setting up renderers and table
        moFreezerColModel = new HideableTableColumnModel();
        moFreezer = new ColumnFreezingTable(mspScrollPane, moFreezerColModel); // TODO: make sure all changes to columns are applied to this model
        //moFreezer.getFixedTable().setColumnModel(new HideableTableColumnModel());
        moTable = new CompositeTable(new JTable[]
            { mainTable, moFreezer.getFixedTable() });
        // Create the popup menu for mass including/excluding.
        // Must be done after composite table is created.
        createPopup();

        moMenuBar = createMenu();   // Must be done after creating views & composite table

        // Must be done after creating menu but before setting views------------
        // Set column renderer for stats
        mmStatFormatMenus.get(ValueAndTextFormat.NUMBER).doClick(0);
        for (String key : mhtStats.keySet()) {
            moTable.getColumn(key).setCellRenderer(new ValueAndTextRenderer(
               mhtStats.get(key).getRange(), masAttributeDesc
               , new CurrentFormatGetter() {

                @Override
                public ValueAndTextFormat getCurrentFormat() {
                    return currentStatFormat;
                }
            }));
        }

        // Set column renderer for potentials
        mmPotFormatMenus.get(ValueAndTextFormat.NUMBER).doClick(0);
        for (Map map : skillMaps) {
            Map<String, GenericSkill> ht = (Map<String, GenericSkill>) map;
            for (String key : ht.keySet()) {
                String id = getColumnNameForSkill(ht.get(key).getName());
                moTable.getColumn(id).setCellRenderer(new ValueAndTextRenderer(
                        maiPotentialBracket, masPotentialDesc
                        , new CurrentFormatGetter() {
                    @Override
                    public ValueAndTextFormat getCurrentFormat() {
                        return currentPotFormat;
                    }
                }));
            }
        }

        mmLevelFormatMenus.get(ValueAndTextFormat.NUMBER).doClick(0);
        for (Map map : new Map[] { mhtSkills }) {
            Map<String, GenericSkill> ht = (Map<String, GenericSkill>) map;
            for (String key : ht.keySet()) {
                String id = getColumnNameForSkillLevel(ht.get(key).getName());
                moTable.getColumn(id).setCellRenderer(new ValueAndTextRenderer(
                        maiSkillLevelBracket, masSkillLevelDesc
                        , new CurrentFormatGetter() {
                    @Override
                    public ValueAndTextFormat getCurrentFormat() {
                        return currentLevelFormat;
                    }
                }));
            }
        }
        // ---------------------------------------------------------------------

        setView(DEFAULT_VIEW_NAME);    // // "Military View"

        // Show some statistics-------------------------------------------------
        mlblPop = new JLabel("X total dwarves from XML"); // total adult
        mlblSelected = new JLabel(getNumSelectedText(mvDwarves.size()));
        mlblExclusions = new JLabel("X active exclusion rules/lists");
        applyExclusions(vExclusions); // Apply exclusions to data & update labels

        // Build the UI---------------------------------------------------------
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(moFreezer.getSplitPane()); // mspScrollPane
        tablePanel.setPreferredSize(new Dimension(750, 250));

        JPanel panInfo = new JPanel();
        panInfo.setLayout(new BorderLayout());
        panInfo.add(mlblPop, BorderLayout.NORTH);
        panInfo.add(mlblSelected, BorderLayout.CENTER);
        panInfo.add(mlblExclusions, BorderLayout.SOUTH);

        JPanel panDwarfList = new JPanel();
        panDwarfList.setLayout(new BorderLayout());
        panDwarfList.add(tablePanel, BorderLayout.CENTER);
        panDwarfList.add(panInfo, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(panDwarfList); // , BorderLayout.LINE_START

        mbLoading = false;
    }
    private class VisibilityHandler extends StateIncrementHandler {
        private JComponent comp;
        private ThresholdFunctions tf;

        public VisibilityHandler(JComponent comp) {
            super(StateIncrementHandler.DefaultState.POSITIVE_STATE);

            this.comp = comp;
        }
        public void initialize() {
            tf = new ThresholdFunctions() {
                @Override
                public void doAtNegativeThreshold() {
                    comp.setVisible(false);
                }
                @Override
                public void doAtPositiveThreshold() {
                    comp.setVisible(true);
                }
            };
            super.initialize(tf);
        }
        public void incrementShown() {
            super.increment();
        }
        public void incrementHidden() {
            super.decrement();
        }
    }
    private JTable createDwarfDataTable(TableModel model) {
        JTable tblReturn;

        tblReturn = new JTable(model, new HideableTableColumnModel());
        // Necessary when using HideableTableColumnModel:
        tblReturn.createDefaultColumnsFromModel();

        tblReturn.setDefaultRenderer(Boolean.class
                , new MyTCRStripedHighlightCheckBox());
        tblReturn.setDefaultRenderer(Object.class, new MyTCRStripedHighlight());

        // For cleaner copy/paste to spreadsheets
        tblReturn.setColumnSelectionAllowed(false);
        tblReturn.setRowSelectionAllowed(true);
        tblReturn.addKeyListener(new ClipboardKeyAdapter(tblReturn, true, false
                , false));

        tblReturn.setUpdateSelectionOnSort(false); // For freezing columns

        return tblReturn;
    }
    private void setView(String viewName) {
        // Set the table columns/view
        moViews.get(viewName).applyToTable(moTable
                , moFreezer.getAllColumnsModel());

        // Set the selected column groups in the menu
        for (String columnKey : moColMenus.keySet()) {
            moColMenus.get(columnKey).setSelected(
                    moViewColumnGroups.get(viewName).contains(columnKey));
        }
    }
    private class ColumnGroup {
        private String[] columns;
        private boolean showByDefault;
        private String name;

        public ColumnGroup(String name, String[] columns, boolean showByDefault) {
            this.name = name;
            this.columns = columns;
            this.showByDefault = showByDefault;
        }
        public String[] getColumns() {
            return columns;
        }
        public boolean isShownByDefault() {
            return showByDefault;
        }
        public String getName() {
            return name;
        }
    }
    private class ViewHandler {

        // Define column group keys
        public static final String COL_GROUP_ALWAYS_SHOW = "Always Show";
        public static final String COL_GROUP_NICKNAME = "Nickname";
        public static final String COL_GROUP_GENDER = "Gender";
        public static final String COL_GROUP_AGE = "Age";
        public static final String COL_GROUP_EXCL = "Exclusion Info";
        public static final String COL_GROUP_STATS = "Stats";
        public static final String COL_GROUP_SECONDARY = "Secondary Skills";
        public static final String COL_GROUP_CUR_LABORS = "Current Labors";

        private Map<String, ColumnGroup> mhmColGroups;
        private ArrayList<ColumnGroup> malColOrder;

        private Vector<Object> mvColumns;
        private Vector<Class> mvClasses;
        private Vector<String> mvColProps;

        public ViewHandler(Vector<LaborGroup> vLaborGroups) {

            mhmColGroups = createColumnGroups(vLaborGroups);
            malColOrder = setColumnOrder(vLaborGroups);
            //System.out.println(mhmColGroups.size() + " column groups");
            //System.out.println(mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()[0]);

            // Create all the column data---------------------------------------
            mvColumns = new Vector<Object>(Arrays.asList(MyArrayUtils.concatAll(
                    mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()
                    , mhmColGroups.get(COL_GROUP_NICKNAME).getColumns()
                    , mhmColGroups.get(COL_GROUP_GENDER).getColumns()
                    , mhmColGroups.get(COL_GROUP_AGE).getColumns()
                    , mhmColGroups.get(COL_GROUP_EXCL).getColumns())));
            mvClasses = new Vector<Class>(Arrays.asList(new Class[]
                { Boolean.class, String.class, String.class, String.class
                    , Integer.class, String.class, String.class }));
            mvColProps = new Vector<String>(Arrays.asList(new String[]
                { "include", "dwarf.name", "dwarf.nickname", "dwarf.gender"
                          , "dwarf.age", "activeexclusions"
                          , "inactiveexclusions" }));

            // Stats
            String colName;
            for (String key : mhtStats.keySet()) {
                colName = mhtStats.get(key).getName();
                //msaStatCols[statCount++] = colName;
                mvColumns.add(colName);
                mvClasses.add(Long.class);   // Stats are Longs
                mvColProps.add("dwarf.statvalues." + key);
            }

            // Skills
            for (String key : mhtSkills.keySet()) {
                colName = getColumnNameForSkill(mhtSkills.get(key).getName());
                mvColumns.add(colName);       //  + " Potential"
                mvClasses.add(Long.class);   // Skills are Longs
                mvColProps.add("dwarf.skillpotentials." + key);

                // Print relevant skill levels for ranged/close combat
                // (Override dwarf.skilllevels.)
                colName = getColumnNameForSkillLevel(mhtSkills.get(key).getName());
                mvColumns.add(colName);
                if (mhtSkills.get(key).getName().equals("Ranged Combat")) {
                    mvClasses.add(String.class);
                    mvColProps.add("rangedcombatlevels");
                }
                else if (mhtSkills.get(key).getName().equals("Close Combat")) {
                    mvClasses.add(String.class);
                    mvColProps.add("closecombatlevels");
                }
                else {
                    mvClasses.add(Long.class);   // Skill levels are Longs
                    mvColProps.add("dwarf.skilllevels." + key);
                }
            }
            for (String key : mhtMetaSkills.keySet()) {
                //System.out.println("Adding " + key);
                mvColumns.add(getColumnNameForSkill(mhtMetaSkills.get(key).getName()));   //  + " Potential"
                mvClasses.add(Long.class);
                mvColProps.add("dwarf.skillpotentials." + key);
            }
            mvColumns.addAll(Arrays.asList(mhmColGroups.get(
                    COL_GROUP_CUR_LABORS).getColumns())); // "Jobs"
            mvClasses.add(String.class);
            mvColProps.add("dwarf.jobtext");
        }

        protected ArrayList<ColumnGroup> getColumnGroups() {
            return malColOrder;
        }
        protected Map<String, ColumnGroup> getColumnGroupMap() {
            return mhmColGroups;
        }

        private Map<String, ColumnGroup> createColumnGroups(
                Vector<LaborGroup> vLaborGroups) {

            Map<String, ColumnGroup> hmReturn;

            final String[] asAlwaysShowCols = new String[] { "Include"
                    , "Name" };
            final String[] asNickCols = new String[] { "Nickname" };
            final String[] asGenderCols = new String[] { "Gender" };
            final String[] asAgeCols = new String[] { "Age" };
            final String[] asExclCols = new String[] { "Exclusion"
                    , "Inactive Lists" };
            final String[] asJobsCols = new String[] { "Jobs" };
            String[] asStatCols;
            Vector<String> vsSecondaryCols;

            if (mhtStats == null) {
                System.err.println("Failed to create column groups: stat table is null");
                return new HashMap<String, ColumnGroup>();
            }
            else if (mhtSkills == null) {
                System.err.println("Failed to create column groups: skill table is null");
                return new HashMap<String, ColumnGroup>();
            }

            // Stat columns
            asStatCols = new String[mhtStats.size()];
            int iCount = 0;
            for (String key : mhtStats.keySet()) {
                asStatCols[iCount++] = mhtStats.get(key).getName();
            }

            // Secondary columns
            vsSecondaryCols = new Vector<String>();
            for (String key : mhtSkills.keySet()) {
                Skill skill = mhtSkills.get(key);
                if (skill instanceof SecondarySkill) {
                    String skillName = skill.getName();
                    vsSecondaryCols.add(getColumnNameForSkill(skillName));
                    vsSecondaryCols.add(getColumnNameForSkillLevel(skillName));
                }
            }

            hmReturn = new HashMap<String, ColumnGroup>();

            hmReturn.put(COL_GROUP_ALWAYS_SHOW, new ColumnGroup(
                    COL_GROUP_ALWAYS_SHOW, asAlwaysShowCols, true));
            hmReturn.put(COL_GROUP_NICKNAME, new ColumnGroup(COL_GROUP_NICKNAME
                    , asNickCols, true));
            hmReturn.put(COL_GROUP_GENDER, new ColumnGroup(COL_GROUP_GENDER
                    , asGenderCols, true));
            hmReturn.put(COL_GROUP_AGE, new ColumnGroup(COL_GROUP_AGE
                    , asAgeCols, true));
            hmReturn.put(COL_GROUP_EXCL, new ColumnGroup(COL_GROUP_EXCL
                    , asExclCols, true));
            hmReturn.put(COL_GROUP_STATS, new ColumnGroup(COL_GROUP_STATS
                    , asStatCols, true));
            hmReturn.put(COL_GROUP_SECONDARY, new ColumnGroup(
                    COL_GROUP_SECONDARY, vsSecondaryCols.toArray(
                    new String[vsSecondaryCols.size()]), false));
            for (LaborGroup group : vLaborGroups) {
                Vector<String> vCols = getLaborColsForGroup(group.getName());
                String strName = getJobGroupKey(group.getName());
                hmReturn.put(strName, new ColumnGroup(strName
                        , vCols.toArray(new String[vCols.size()]), false));
            }
            hmReturn.put(COL_GROUP_CUR_LABORS, new ColumnGroup(
                    COL_GROUP_CUR_LABORS, asJobsCols, true));

            return hmReturn;
        }
        private ArrayList<ColumnGroup> setColumnOrder(
                Vector<LaborGroup> vLaborGroups) {

            ArrayList<ColumnGroup> alReturn = new ArrayList<ColumnGroup>(
                    mhmColGroups.size());
            alReturn.add(mhmColGroups.get(COL_GROUP_NICKNAME));
            alReturn.add(mhmColGroups.get(COL_GROUP_GENDER));
            alReturn.add(mhmColGroups.get(COL_GROUP_AGE));
            alReturn.add(mhmColGroups.get(COL_GROUP_EXCL));
            alReturn.add(mhmColGroups.get(COL_GROUP_STATS));
            alReturn.add(mhmColGroups.get(COL_GROUP_SECONDARY));
            for (LaborGroup group : vLaborGroups) {
                alReturn.add(mhmColGroups.get(getJobGroupKey(group.getName())));
            }
            alReturn.add(mhmColGroups.get(COL_GROUP_CUR_LABORS));
            return alReturn;
        }

        private Vector<String> getLaborColsForGroup(String groupName) {
            Vector<String> vReturn = new Vector<String>();
            for (Labor labor : mvLabors) {
                if (labor.getGroupName().equals(groupName)) {
                    vReturn.add(getColumnNameForSkill(labor.getSkillName()));

                    // Meta skills don't have skill levels
                    if (! mhtMetaSkills.containsKey(labor.getSkillName()))
                        vReturn.add(getColumnNameForSkillLevel(labor.getSkillName()));
                }
            }
            return vReturn;
        }

        public void createDwarfListViews() {
            ArrayList<String> alColGroupList;

            moViews = new HashMap<String, GridView>();
            moViewColumnGroups = new HashMap<String, List<String>>();

            // TODO: Read from file?
            // Default view-----------------------------------------------------
            alColGroupList = new ArrayList<String>(Arrays.asList(new String[] {
                COL_GROUP_ALWAYS_SHOW, COL_GROUP_NICKNAME, COL_GROUP_GENDER
                        , COL_GROUP_AGE, COL_GROUP_EXCL, COL_GROUP_STATS
                        , COL_GROUP_CUR_LABORS
            }));
            List<Object> colOrder = new ArrayList<Object>();
            for (String columnKey : alColGroupList) {
                colOrder.addAll(Arrays.asList(mhmColGroups.get(columnKey).getColumns()));
            }
            //System.out.println("colOrder has " + colOrder.size() + " entries");

            moViews.put(DEFAULT_VIEW_NAME, new GridView(DEFAULT_VIEW_NAME, "Name"
                    , GridView.KeyAxis.X_AXIS, false, colOrder));
            moViewColumnGroups.put(DEFAULT_VIEW_NAME, alColGroupList);

            // Military view----------------------------------------------------
            String[] skills = new String[] { "Close Combat", "Ranged Combat" };

            colOrder = new ArrayList<Object>();
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_NICKNAME).getColumns()));

            // Add close/ranged combat potentials
            for (String skillName : skills) {
                colOrder.add(getColumnNameForSkill(skillName));
                colOrder.add(getColumnNameForSkillLevel(skillName));
            }

            // Add the relevant stats
            for (String skillName : skills) {
                for (Stat stat : mhtSkills.get(skillName).getStats()) {
                    if (! colOrder.contains(stat.getName()))
                        colOrder.add(stat.getName());
                }
            }

            // Gender, exclusions
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_GENDER).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_EXCL).getColumns()));

            moViews.put("Military View", new GridView("Military View", "Name"
                    , GridView.KeyAxis.X_AXIS, false, colOrder));
            moViewColumnGroups.put("Military View", new ArrayList<String>(
                Arrays.asList(new String[] { COL_GROUP_ALWAYS_SHOW
                        , COL_GROUP_NICKNAME, COL_GROUP_GENDER, COL_GROUP_EXCL
            })));

            // TODO: Potential view
        }

        public MyEditableTableModel<DwarfListItem> createDwarfListModel(
                Collection<Exclusion> vExclusions) {

            MyEditableTableModel<DwarfListItem> mdlReturn;

            //Create the table model--------------------------------------------
            SortKeySwapper swapper = new SortKeySwapper();
            mdlReturn = new MyEditableTableModel<DwarfListItem>(mvColumns
                    , mvClasses
                    , mvColProps, toDwarfListItems(mvDwarves, vExclusions)
                    , swapper);
            // (We apply exclusions to this data after the statistic monitors
            // are created)

            // Display data in a grid (JTable)
            mdlReturn.addEditableException(0);     // First column editable.
            mdlReturn.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    if (! mbLoading)
                        updateSelectedLabel();
                }
            });
            //setModelData(moModel, vExclusions);       // , nodes

            return mdlReturn;
        }
        public Vector<Object> getAllColumns() {
            return mvColumns;
        }
    }
    private String getJobGroupKey(String groupName) {
        return "Job Group: " + groupName;
    }

    // Table cell renderer for number-formatted column with null values
    // displayed as blanks
    private static class NumberOrNullRenderer extends MyTCRStripedHighlight {
        NumberFormat formatter;
        public NumberOrNullRenderer() { super(); }

        @Override
        public void setValue(Object value) {
            if (formatter == null) {
                formatter = NumberFormat.getIntegerInstance();
            }
            if (value == null)
                setText("");
            else if (! isInteger(value))
                setText(value.toString());
            else
                setText(formatter.format(value));
        }
        private boolean isInteger(Object value) {
            try {
                Integer.parseInt(value.toString());
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
    }
    private class ValueAndTextRenderer extends MyTCRStripedHighlight {
        private int[] range;
        private NumberFormat formatter;
        private String[] rangeDesc;
        private CurrentFormatGetter formatGetter;

        public ValueAndTextRenderer(int[] range, String[] rangeDesc
                , CurrentFormatGetter formatGetter) {
            super();

            formatter = NumberFormat.getIntegerInstance();

            this.range = range;
            this.rangeDesc = rangeDesc;
            this.formatGetter = formatGetter;
        }

        @Override
        public void setValue(Object value) {

            if (value == null)                  // Nulls
                setText("");
            else if (! isInteger(value))        // Non-integers
                setText(value.toString());
            else if (formatGetter.getCurrentFormat().equals(ValueAndTextFormat.NUMBER))
                setText(formatter.format(value));
            else {
                String desc = describeAttribute(rangeDesc, range
                        , Integer.parseInt(value.toString()));

                if (formatGetter.getCurrentFormat().equals(ValueAndTextFormat.TEXT))
                    setText(desc);
                else if (formatGetter.getCurrentFormat().equals(
                        ValueAndTextFormat.NUMBER_AND_TEXT))
                    setText(desc + " (" + formatter.format(value) + ")");
                else
                    setText("???");
            }
        }
        private boolean isInteger(Object value) {
            try {
                Integer.parseInt(value.toString());
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
    }
    private interface CurrentFormatGetter {
        public ValueAndTextFormat getCurrentFormat();
    }
    private Vector<DwarfListItem> toDwarfListItems(Vector<Dwarf> vDwarves
            , Collection<Exclusion> colExclusions) {

        Vector<DwarfListItem> vReturn = new Vector<DwarfListItem>(vDwarves.size());

        for (Dwarf dwarf : vDwarves) {
            vReturn.add(new DwarfListItem(DwarfOrganizerIO.DEFAULT_EXCLUSION_ACTIVE
                    , dwarf, formatJuvenile(dwarf)
                    , formatExclusions(dwarf, true, colExclusions)
                    , formatExclusions(dwarf, false, colExclusions)
                    , listCombatLevels("Close", dwarf.skillLevels)
                    , listCombatLevels("Ranged", dwarf.skillLevels)));
        }

        return vReturn;
    }
    public void loadData(Vector<Dwarf> vDwarves
            , Collection<Exclusion> vExclusions) { //, Hashtable<String, Stat> htStat
            //, Hashtable<String, Skill> htSkill, Hashtable<String, MetaSkill> htMeta) {
            // , Vector<Labor> vLabors
        mbLoading = true;

        // Set locals-----------------------------------------------------------
        mvDwarves = vDwarves;

        // Set table contents---------------------------------------------------
        moModel.setRowData(toDwarfListItems(mvDwarves, vExclusions));
        //MyHandyTable.autoResizeTableColumns(moTable.getTables());
        moFreezer.autoResizeTableColumns();

        // Update labels--------------------------------------------------------
        applyExclusions(vExclusions);
        updatePopulationLabel();

        mbLoading = false;
    }
    private void updatePopulationLabel() {
        mlblPop.setText(mvDwarves.size() + " total dwarves from XML");
    }

    private void createPopup() {
        JPopupMenu popUp = new JPopupMenu();
        JMenuItem menuItem;
        final JMenuItem[] selectionNeededItems = new JMenuItem[2];

        // Include selected
        menuItem = new JMenuItem("Include Selected");
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setIncluded(true);
            }
        });
        selectionNeededItems[0] = menuItem;
        popUp.add(menuItem);

        menuItem = new JMenuItem("Exclude Selected");
        menuItem.setMnemonic(KeyEvent.VK_U);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setIncluded(false);
            }
        });
        selectionNeededItems[1] = menuItem;
        popUp.add(menuItem);

        // ---------------------------------------------------------------------
        popUp.add(new JSeparator());

        // Copy selected rows to clipboard in spreadsheet-friendly format-----
        menuItem = new JMenuItem("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MyHandyTable.cancelEditing(moTable.getMainTable());
                MyHandyTable.copyToClipboard(moTable.getTables(), false);
            }
        });
        popUp.add(menuItem);

        // ---------------------------------------------------------------------
        for (JTable table : moTable.getTables())
            table.setComponentPopupMenu(popUp); // moTable
        moTable.getMainTable().getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (! e.getValueIsAdjusting())
                    setMenusEnabledBySelection(selectionNeededItems);
            }
        });
        setMenusEnabledBySelection(selectionNeededItems); // Initialize
    }
    private void setMenusEnabledBySelection(JMenuItem[] menuItems) {
        boolean bAnySelected = moTable.getMainTable().getSelectedRowCount() > 0;
        for (JMenuItem menuItem : menuItems) {
            menuItem.setEnabled(bAnySelected);
        }
    }

    // Sets whether selected table rows are Included
    private void setIncluded(boolean included) {
        for (int row = 0; row < moTable.getMainTable().getRowCount(); row++)
            if (moTable.getMainTable().isRowSelected(row))
                moModel.setValueAt(included, moTable.getMainTable().convertRowIndexToModel(row)
                        , moTable.getMainTable().convertColumnIndexToModel(INCLUDE_COLUMN));
    }

    // Returns the desired menu for this panel.
    // Expected to be called by owner frame
    protected JMenuBar getMenu() {
        return moMenuBar;
    }
    private JMenuBar createMenu() {

        JMenu menu;
        JMenu subMenu;
        JMenuItem item;
        ButtonGroup group;
        final Object[] hideableColumns = getHideableColumns();
        
        //JTable mainTable = moTable.getMainTable();
        //final HideableTableColumnModel hideableModel
        //        = (HideableTableColumnModel) mainTable.getColumnModel();

        JMenuBar menuBar = new JMenuBar();

        // Columns--------------------------------------------------------------
        ArrayList<ColumnGroup> alColGroups = moViewHandler.getColumnGroups();
        moColMenus = new HashMap<String, JCheckBoxMenuItem>(alColGroups.size());

        menu = new JMenu("Columns");
        menu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(menu);

        item = new JMenuItem("Show All");
        item.setMnemonic(KeyEvent.VK_S);
/*        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (moColMenus != null) {
                    moTableVis.incrementHidden();
                    for (String key : moColMenus.keySet()) {
                        JCheckBoxMenuItem menuItem = moColMenus.get(key);
                        if (! menuItem.isSelected())
                            menuItem.doClick(0);
                    }
                    moTableVis.incrementShown();
                }
            }
        }; */
        item.addActionListener(CursorController.createListener(this
                , createAllVisActionListener(hideableColumns, true)));  // Hourglass  al
        menu.add(item);

        item = new JMenuItem("Hide All");
        item.setMnemonic(KeyEvent.VK_I);
        item.addActionListener(CursorController.createListener(this
                , createAllVisActionListener(hideableColumns, false)));  // Hourglass  al
        menu.add(item);

        // -------------------------------
        menu.add(new JSeparator());

        for (ColumnGroup colGroup : alColGroups) {
            if (! colGroup.getName().equals("Always Show")) {
                //ColumnGroup columnGroup = hmColGroups.get(key);
                final String[] arrColumns = colGroup.getColumns();
                final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(
                        colGroup.getName(), colGroup.isShownByDefault());
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //setColumnsVisible(arrColumns, menuItem.isSelected()
                        //        , hideableModel);
                        setColumnsVisible(arrColumns, menuItem.isSelected()
                                , moFreezerColModel);
                    }
                });

                // Store a reference to the menu item, keyed by group name
                moColMenus.put(colGroup.getName(), menuItem);

                /*if (arrColumns.length > 1) {
                    System.out.println("Submenu needed for " + group.getName());
                } */

                menu.add(menuItem);
            }
        }

        // Formats--------------------------------------------------------------
        menu = new JMenu("Format");
        menu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(menu);

        subMenu = new JMenu("Stats");
        subMenu.setMnemonic(KeyEvent.VK_S);
        menu.add(subMenu);

        mmStatFormatMenus = new HashMap<ValueAndTextFormat, JMenuItem>(
                ValueAndTextFormat.values().length);
        createFormatSubMenu(subMenu, "Stat", mmStatFormatMenus);

        subMenu = new JMenu("Potentials");
        subMenu.setMnemonic(KeyEvent.VK_T);
        menu.add(subMenu);

        mmPotFormatMenus = new HashMap<ValueAndTextFormat, JMenuItem>(
                ValueAndTextFormat.values().length);
        createFormatSubMenu(subMenu, "Potential", mmPotFormatMenus);

        subMenu = new JMenu("Skill Levels");
        subMenu.setMnemonic(KeyEvent.VK_K);
        menu.add(subMenu);

        mmLevelFormatMenus = new HashMap<ValueAndTextFormat, JMenuItem>(
                ValueAndTextFormat.values().length);
        createFormatSubMenu(subMenu, "Skill Level", mmLevelFormatMenus);

        // Views----------------------------------------------------------------
        menu = new JMenu("View");
        menu.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menu);
        //System.out.println("Menu count: " + menuBar.getMenuCount());

        group = new ButtonGroup();
        for (String key : moViews.keySet()) {
            final GridView view = moViews.get(key);
            item = new JRadioButtonMenuItem(view.getName());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setView(view.getName());
                }
            });
            item.setSelected(view.getName().equals(DEFAULT_VIEW_NAME));
            group.add(item);
            menu.add(item);
        }

        menu.add(new JSeparator());
        item = new JMenuItem("Manage Views..."); // TODO
        item.setToolTipText("Coming soon"); // TODO
        item.setEnabled(false); // TODO
        menu.add(item);

        return menuBar;
    }

    private Object[] getHideableColumns() {
        Vector<Object> vAllColumns = moViewHandler.getAllColumns();
        ArrayList<Object> alAlwaysShow
                = new ArrayList<Object>(Arrays.asList(
                moViewHandler.getColumnGroupMap().get(
                ViewHandler.COL_GROUP_ALWAYS_SHOW).getColumns()));
        int size = vAllColumns.size() - alAlwaysShow.size();
        Object[] hideableColumns = new Object[size];
        
        int iCount = 0;
        for (Object col : vAllColumns) {
            if (! alAlwaysShow.contains(col)) {
                hideableColumns[iCount] = col;
                iCount++;
            }
        }
        return hideableColumns;
    }
    
    private ActionListener createAllVisActionListener(
            final Object[] affectedColumns, final boolean visible) {
        
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (moColMenus != null) {
                    moTableVis.incrementHidden();
                    
                    // Show or hide columns
                    setColumnsVisible(affectedColumns, visible
                            , moFreezerColModel);
                    
                    // Set menu item states
                    for (String key : moColMenus.keySet()) {
                        JCheckBoxMenuItem menuItem = moColMenus.get(key);
                        if (menuItem.isSelected() != visible) {
                            //menuItem.doClick(0);
                            menuItem.setSelected(visible);
                        }
                    }
                    
                    moTableVis.incrementShown();
                }
            }
        };        
    }
    
    private void createFormatSubMenu(JMenu subMenu, final String which
            , Map<ValueAndTextFormat, JMenuItem> map) {
        JMenuItem item;

        ButtonGroup group = new ButtonGroup();
        for (final ValueAndTextFormat statFormat : ValueAndTextFormat.values()) {
            String humanText = statFormat.getHumanText();
            item = new JRadioButtonMenuItem(humanText);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setValueAndTextFormat(which, statFormat);
                }
            });
            //statFormat.setMenuItem(item);
            map.put(statFormat, item);
            group.add(item);
            subMenu.add(item);
        }
    }

    private void setValueAndTextFormat(String which, ValueAndTextFormat newFormat) {

        moTableVis.incrementHidden();

        if (which.equals("Stat")) {
            currentStatFormat = newFormat;
        }
        else if (which.equals("Potential")) {
            currentPotFormat = newFormat;
        }
        else if (which.equals("Skill Level")) {
            currentLevelFormat = newFormat;
        }
        else {
            System.err.println("Unknown format option: " + which);
        }
        resizeColumns(which);   // Resize relevant columns

        moTableVis.incrementShown();
    }

    // Auto-resizes the proper columns
    private void resizeColumns(String which) {
        final String STATS = ViewHandler.COL_GROUP_STATS;

        //JTable table;
        Map<String, ColumnGroup> colMap = moViewHandler.getColumnGroupMap();

        if (which.equals("Stat")) {
            // Don't resize all stats - some might be hidden by View
            //MyHandyTable.autoResizeTableColumns(moTable
            //    , colMap.get(ViewHandler.COL_GROUP_STATS).getColumns());
            for (String id : colMap.get(STATS).getColumns()) {
                moFreezer.autoResizeTableColumn(id);
            }
        }
        else if (which.equals("Potential") || which.equals("Skill Level")) {
            ColumnNameGetter cng;
            Map[] skillMaps;

            if (which.equals("Potential")) {
                skillMaps = new Map[] { mhtSkills, mhtMetaSkills };
                cng = new ColumnNameGetter() {
                    @Override
                    public String getColumnName(String key) {
                        return getColumnNameForSkill(key);
                    }
                };
            }
            else { // if (which.equals("Skill Level"))
                skillMaps = new Map[] { mhtSkills };    // Metaskills have no skill levels
                cng = new ColumnNameGetter() {
                    @Override
                    public String getColumnName(String key) {
                        return getColumnNameForSkillLevel(key);
                    }
                };
            }

            for (Map map : skillMaps) {
                Map<String, GenericSkill> skillMap
                        = (Map<String, GenericSkill>) map;
                for (String key : skillMap.keySet()) {
                    moFreezer.autoResizeTableColumn(cng.getColumnName(key));
                }
            }
        }
        else
            System.err.println("Unknown format option: " + which);
    }
    private interface ColumnNameGetter {
        public String getColumnName(String key);
    }

    // Sets the display for number of included dwarves
    private void updateSelectedLabel() {
        int sum = 0;

        Vector<DwarfListItem> vRows = moModel.getRowData();
        for (DwarfListItem item : vRows) {
            if (item.isIncluded())
                sum++;
        }

        /*for (int row = 0; row < moModel.getRowCount(); row++)
            if ((Boolean) moModel.getValueAt(row, INCLUDE_COLUMN))
                sum++; */

        mlblSelected.setText(getNumSelectedText(sum));
    }

    private String getNumSelectedText(int numDwarves) {
         return numDwarves + " dwarves selected for optimization";
    }

    private void updateExclusionsLabel(Collection<Exclusion> vExclusions) {
        int sum = 0;
        for (Exclusion excl : vExclusions) {
            if (excl.isActive())
                sum++;
        }
        mlblExclusions.setText(getNumExclusionsText(sum));
    }
    private String getNumExclusionsText(int numExclusions) {
        return numExclusions + " active exclusion rule(s)";
    }

    // Returns a vector of Included dwarves
    protected Vector<Dwarf> getIncludedDwarves() {

        Vector<Dwarf> vIncluded = new Vector<Dwarf>();

        for (Dwarf dwarf : mvDwarves)
            if (isDwarfIncluded(dwarf.getName()))
                vIncluded.add(dwarf);

        return vIncluded;
    }

    // Returns true if the dwarf with the given name is included
    private boolean isDwarfIncluded(String dwarfName) {

        //final int NAME_COLUMN = 1;

        Vector<DwarfListItem> vRows = moModel.getRowData();
        for (DwarfListItem item : vRows) {
            if (item.getDwarf().getName().equals(dwarfName)
                    && item.isIncluded())
                return true;
        }
        return false;

        /*for (int row = 0; row < moModel.getRowCount(); row++)
            if (moModel.getValueAt(row, NAME_COLUMN).equals(dwarfName)
                && ((Boolean) moModel.getValueAt(row, INCLUDE_COLUMN)))
                return true;
        return false; */

    }

    private void setColumnsVisible(Object[] cols, boolean visible
            , HideableTableColumnModel hideableModel) {

        // Hide the table, save the current divider location, and move
        // the divider to its default location.
        moTableVis.incrementHidden();

        for (Object col : cols) {
            hideableModel.setColumnVisible(col, visible);
        }

        // Resize the columns on visible=true, in case the format was
        // changed while hidden
        if (visible) {
            moFreezer.autoResizeTableColumns(cols);
        }

        // Put the divider back where it was, and show the table
        //handler.increment();
        //System.out.println("----------------------");
        moTableVis.incrementShown();
    }

    // Returns the column title for potential in the given skill
    private String getColumnNameForSkill(String skillName) {
         return skillName + " Potential";
    }
    // Returns the column title for current skill level in the given skill
    private String getColumnNameForSkillLevel(String skillName) {
         return skillName + " Lvl";
    }

    // Print stat groups, for debugging
    private void printStatGroups(Hashtable<String, Vector<Stat>> statGroups) {
        System.out.println("Printing " + statGroups.size() + " stat groups:");
        for (String key : statGroups.keySet()) {
            System.out.println(key + ":");
            printStatGroup(statGroups.get(key));
        }
        System.out.println("Done printing stat groups.");
    }
    private void printStatGroup(Vector<Stat> statGroup) {
        for (Stat stat : statGroup) {
            System.out.println("    " + stat.getName());
        }
    }

    // One-shot function to move hard-coded skill stats to file
    private void writeSkillsToXML() {
        FileWriter fstream = null;
        try {
            String strPath = "c:\\labor-skills.xml";
            fstream = new FileWriter(strPath);
            BufferedWriter out = new BufferedWriter(fstream);

            // \u201c is left opening quotation mark
            // \u201d is right closing quotation mark
            // According to fileformat.info these are the preferred paired
            // Unicode quotation marks for English. However, my Firefox
            // XML parser can't read these.
            // ... Oops, and then the normal \u0022 confuses the compiler...
            // \" is the way to do it.
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            out.newLine();

            out.write("<root>");
            out.newLine();

            // (Skip stat groups - paste in from file)

            // Labor skills
            out.write("<Skills>");
            out.newLine();
            out.flush();

            for (Skill skill : mhtSkills.values()) {
                if (skill.getClass() == Skill.class)
                    writeSkill(skill, out, "Skill");
            }
            out.write("</Skills>");
            out.newLine();

            // Secondary skills
            out.write("<SecondarySkills>");
            out.newLine();
            out.flush();

            for (Skill skill : mhtSkills.values()) {
                if (skill instanceof SecondarySkill && ! (skill instanceof SocialSkill))
                    writeSkill(skill, out, "SecondarySkill");
            }
            out.write("</SecondarySkills>");
            out.newLine();

            // Social skills
            out.write("<SocialSkills>");
            out.newLine();
            out.flush();

            for (Skill skill : mhtSkills.values()) {
                if (skill instanceof SocialSkill)
                    writeSkill(skill, out, "SocialSkill");
            }
            out.write("</SocialSkills>");
            out.newLine();
            out.flush();

            out.write("</root>");
            out.newLine();

            out.flush();

        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(DwarfListWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fstream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                Logger.getLogger(DwarfListWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    private void writeSkill(Skill skill, BufferedWriter out, String tagName)
            throws IOException {

        out.write("<" + tagName + ">");

        out.newLine();
        out.write("    <Name>" + skill.getName() + "</Name>");
        out.newLine();
        out.write("    <Stats>");
        out.newLine();

        for (Stat stat : skill.getStats()) {
            out.write("        <Stat>" + stat.getName() + "</Stat>");
            out.newLine();
        }
        out.write("    </Stats>");
        out.newLine();

        if (skill instanceof SocialSkill) {
            SocialSkill social = (SocialSkill) skill;
            out.write("    <PreventedBy>");
            out.newLine();
            out.write("        <Trait>" + social.noStatName + "</Trait>");
            out.newLine();
            out.write("        <Min>" + social.noStatMin + "</Min>");
            out.newLine();
            out.write("        <Max>" + social.noStatMax + "</Max>");
            out.newLine();
            out.write("    </PreventedBy>");
            out.newLine();

        }

        out.write("</" + tagName + ">");
        out.newLine();

        out.flush();
    }

    // Formats the exclusion into the list of exclusions for display in the Dwarf List
    private String formatExclusion(String list, String exclusion) {
        String strReturn = list;
        String strExclName;

        if (! list.equals(""))
            strReturn += ", ";
        else
            strReturn = list;

        if (exclusion.equals(""))
            strExclName = "[Unnamed]";
        else
            strExclName = exclusion;

        return strReturn + strExclName;
    }

    protected class DwarfListItem implements MyPropertyGetter, MyPropertySetter {

        private boolean include;
        private Dwarf dwarf;
        private String juvenile;
        private String activeExclusions;
        private String inactiveExclusions;
        private String closeCombatLevels;
        private String rangedCombatLevels;

        public DwarfListItem(boolean include, Dwarf dwarf, String juvenile
                , String activeExclusions, String inactiveExclusions
                , String closeCombatLevels, String rangedCombatLevels) {
            super();
            this.include = include;
            this.dwarf = dwarf;
            this.juvenile = juvenile;
            this.activeExclusions = activeExclusions;
            this.inactiveExclusions = inactiveExclusions;
            this.closeCombatLevels = closeCombatLevels;
            this.rangedCombatLevels = rangedCombatLevels;
        }

        public String getActiveExclusions() {
            return activeExclusions;
        }

        public void setActiveExclusions(String activeExclusions) {
            this.activeExclusions = activeExclusions;
        }

        public Dwarf getDwarf() {
            return dwarf;
        }

        public void setDwarf(Dwarf dwarf) {
            this.dwarf = dwarf;
        }

        public String getInactiveExclusions() {
            return inactiveExclusions;
        }

        public void setInactiveExclusions(String inactiveExclusions) {
            this.inactiveExclusions = inactiveExclusions;
        }

        public boolean isIncluded() {
            return include;
        }

        public void setIncluded(boolean include) {
            this.include = include;
        }

        public String getJuvenile() {
            return juvenile;
        }

        public void setJuvenile(String juvenile) {
            this.juvenile = juvenile;
        }

        public String getCloseCombatLevels() {
            return closeCombatLevels;
        }

        public void setCloseCombatLevels(String closeCombatLevels) {
            this.closeCombatLevels = closeCombatLevels;
        }

        public String getRangedCombatLevels() {
            return rangedCombatLevels;
        }

        public void setRangedCombatLevels(String rangedCombatLevels) {
            this.rangedCombatLevels = rangedCombatLevels;
        }

        @Override
        public Object getProperty(String propName, boolean humanReadable) {
            String prop = propName.toLowerCase();
            if (prop.equals("include"))
                return isIncluded();
            else if (prop.startsWith("dwarf."))
                return dwarf.getProperty(propName.replace("dwarf.", ""), humanReadable); // don't lowercase
            else if (prop.equals("juvenile"))
                return getJuvenile();
            else if (prop.equals("activeexclusions"))
                return getActiveExclusions();
            else if (prop.equals("inactiveexclusions"))
                return getInactiveExclusions();
            else if (prop.equals("rangedcombatlevels"))
                return getRangedCombatLevels();
            else if (prop.equals("closecombatlevels"))
                return getCloseCombatLevels();
            else
                return "[DwarfListItem] Unknown getProperty: " + propName;
        }

        @Override
        public long getKey() {
            return 0;
        }

        @Override
        public void setProperty(String propName, Object value) {
            String prop = propName.toLowerCase();
            try {
               if (prop.equals("include"))
                    setIncluded((Boolean) value);
               else if (prop.equals("juvenile"))
                   setJuvenile(value.toString());
               else if (prop.equals("activeexclusions"))
                   setActiveExclusions(value.toString());
               else if (prop.equals("inactiveexclusions"))
                   setInactiveExclusions(value.toString());
               else if (prop.equals("rangedcombatlevels"))
                   setRangedCombatLevels(value.toString());
               else if (prop.equals("closecombatlevels"))
                   setCloseCombatLevels(value.toString());
               else
                   System.err.println("[DwarfListItem] Unknown setProperty: "
                           + propName);

            } catch (Exception e) {
                System.err.println("[DwarfListItem] Failed to set property "
                        + propName);
            }
        }
    }

    private String formatJuvenile(Dwarf citizen) {
        if (citizen.getAge() < 1)
            return "Baby";
        else if (citizen.isJuvenile())
            return "Child";
        else
            return "";
    }
    private String formatExclusions(Dwarf citizen, boolean active
            , Collection<Exclusion> colExclusions) {
        String strReturn = "";

        for (Exclusion excl : colExclusions) {
            if (excl.isActive() == active) {
                if (excl.appliesTo(citizen)) {
                    strReturn = formatExclusion(strReturn, excl.getName());
                }
            }
        }
        return strReturn;
    }

    //TODO: Figure out what to do with general combat skills and equipment skills
    // (DF wiki article: http://dwarffortresswiki.org/index.php/DF2012:Combat_skill)
    private String listCombatLevels(String rangedOrClose
            , Hashtable<String, Long> skillLevels) {

        // TODO: Verify *all* of these are the exact strings used in Dwarves.xml
        String[] rangedSkills = new String[] { "Blowgun", "Bow", "Crossbow"
                , "Thrower" };
        String[] closeSkills = new String[] { "Axe", "Hammer", "Knife", "Lasher"
                , "Mace", "Misc. Object User", "Pike", "Spear", "Sword" };
        String[] skills;
        String strReturn = "";

        if (rangedOrClose.equals("Ranged"))
            skills = rangedSkills;
        else if (rangedOrClose.equals("Close"))
            skills = closeSkills;
        else
            return "Unknown combat type: " + rangedOrClose;

        for (String skill : skills) {
            if (null != skillLevels.get(skill)) {
                if (! strReturn.equals(""))
                    strReturn += ", ";
                strReturn += skillLevels.get(skill) + " " + skill;
            }
        }
        return strReturn;
    }

    private String describeAttribute(String[] descriptions, int[] values
            , int attribute) {
        return descriptions[getAttributeBracket(values, attribute)];
    }
    private int getAttributeBracket(int[] values, int attribute) {
        for (int iCount = values.length - 1; iCount >= 0; iCount--)
            if (attribute >= values[iCount])
                return iCount + 1;
        return 0;
    }

/*    private String describeAttribute(String[] descriptions, int[] values
            , int attribute) {
        return descriptions[getAttributeBracket(values, attribute)];
    }
    private int getAttributeBracket(int[] values, int attribute) {
        for (int iCount = 0; iCount < values.length; iCount++)
            if (attribute >= values[iCount])
                return iCount;
        return values.length;
    }
    private int getBracket(int[] values, int attribute) {
        return values.length - 1 - getAttributeBracket(values, attribute);
    } */

    private String getPlusPlusDesc(String[] desc, int bracket) {
        return desc[bracket];
    }

    private void applyExclusions(Collection<Exclusion> colExclusion) {
        Vector<DwarfListItem> vRows = moModel.getRowData();
        for (DwarfListItem item : vRows) {
            for (Exclusion excl : colExclusion) {
                if (excl.isActive() && excl.appliesTo(item.getDwarf())) {
                    item.setIncluded(false);
                    break;
                }
                item.setIncluded(true); // Default
            }
            item.setActiveExclusions(formatExclusions(item.getDwarf(), true
                    , colExclusion));
            item.setInactiveExclusions(formatExclusions(item.getDwarf(), false
                    , colExclusion));
        }

        // Update statistic labels
        updateSelectedLabel();
        updateExclusionsLabel(colExclusion);
    }

    @Override
    public void broadcast(BroadcastMessage message) {
        if (message.getSource().equals("ExclusionsApplied")) {
            try {
                //System.out.println("Exclusions applied");
                Collection<Exclusion> lstExclusion = (Collection<Exclusion>) message.getTarget();
                applyExclusions(lstExclusion);
            } catch (Exception e) {
                System.err.println(e.getMessage()
                        + " [Dwarf List]Failed to apply exclusions.");
            }
        }
        else
            System.err.println("[Dwarf List]Unknown broadcast message received: "
                    + message.getSource());
    }
}
