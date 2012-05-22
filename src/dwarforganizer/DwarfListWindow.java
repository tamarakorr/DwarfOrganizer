/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.broadcast.BroadcastListener;
import dwarforganizer.broadcast.BroadcastMessage;
import dwarforganizer.broadcast.Broadcaster;
import dwarforganizer.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
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

    private Map<String, Stat> mhtStats;
    private Map<String, Skill> mhtSkills;
    private List<Dwarf> mlstDwarves;
    private Map<String, MetaSkill> mhtMetaSkills;

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

    private List<Labor> mlstLabors; // Set in constructor

    //private List<GridView> mlstOrderedViews;    // Ordered views
    //private Map<String, GridView> moViews;      // Views keyed by view name
    private Views moViews;

    //private Map<String, List<String>> moViewColumnGroups; // Keyed by view name, values are lists of column group keys
    private ViewHandler moViewHandler;

    private JMenuBar moMenuBar;
    private Map<String, JCheckBoxMenuItem[]> moColGroupMenus;   // Columns by group
    private Map<Object, ArrayList<JCheckBoxMenuItem>> moColMenus;  // Columns by identifier (a column may have >1 menu item)
    private ButtonGroup moViewButtonGroup;
    private JMenu moViewMenu;

    private VisibilityHandler moTableVis;

    private ColumnFreezingTable moFreezer;
    private HideableTableColumnModel moFreezerColModel;
    private ClipboardTableHelper moClipboardHelper;

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

    private Broadcaster moBroadcaster;

    private int mintFixedViewMenuItems;

    private PrefsRecaller moPrefs;

    public DwarfListWindow(List<Labor> vLabors, Map<String, Stat> htStat
            , Map<String, Skill> htSkill, Map<String, MetaSkill> htMeta
            , List<LaborGroup> lstLaborGroups, List<GridView> lstViews) {

        // Parent constructor---------------------------------------------------
        super();

        // Load preferences-----------------------------------------------------
        moPrefs = new PrefsRecaller();
        moPrefs.loadPreferences();

        // Set local variables--------------------------------------------------
        mlstLabors = vLabors;
        mhtStats = htStat;
        mhtSkills = htSkill;
        mhtMetaSkills = htMeta;
        moViews = new Views(lstViews);
        //mlstOrderedViews = lstViews;
        //moViews = viewsToHashMap(lstViews); // Convert view list to hashmap

        // Create objects-------------------------------------------------------
        Map[] skillMaps = { mhtSkills, mhtMetaSkills };

        mlstDwarves = new ArrayList<Dwarf>(); //vDwarves;
        Collection<Exclusion> vExclusions = new ArrayList<Exclusion>();

        moBroadcaster = new Broadcaster();
        mbLoading = true;

        // Create view data-----------------------------------------------------
        moViewHandler = new ViewHandler(lstLaborGroups);
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

        //moViewHandler.createDwarfListViews();

        // Create the column freeze pane----------------------------------------
        // Should be done after setting up renderers and table
        moFreezerColModel = new HideableTableColumnModel();
        moFreezer = new ColumnFreezingTable(mspScrollPane, moFreezerColModel);
        moTable = new CompositeTable(new JTable[]
            { moFreezer.getFixedTable(), mainTable }, 1); // Order is important for view saving

        // Create the popup menu for mass including/excluding.
        // Must be done after composite table is created.
        createPopup();

        // Create the key listener for copying rows. Must be done after
        // composite table is created.
        moClipboardHelper = new ClipboardTableHelper(moTable.getTables()
                , true, false, false);
        mainTable.addKeyListener(moClipboardHelper.createKeyAdapter());

        moMenuBar = createMenu(lstViews);   // Must be done after creating views & composite table

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

        //setView(DEFAULT_VIEW_NAME);    // // "Military View"
        setView(moPrefs.getViewName());

        // Show some statistics-------------------------------------------------
        mlblPop = new JLabel("X total dwarves from XML"); // total adult
        mlblSelected = new JLabel(getNumSelectedText(mlstDwarves.size()));
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
    private class PrefsRecaller extends MyPrefs {
        private String mstrViewName;

        @Override
        public void savePrefs(Preferences prefs) {
            //System.out.println("Current view = " + getSelectedViewName());
            prefs.put("CurrentView", getSelectedViewName());
        }

        @Override
        public void loadPrefs(Preferences prefs) {
            mstrViewName = prefs.get("CurrentView", DEFAULT_VIEW_NAME);
        }
        public String getViewName() {
            return mstrViewName;
        }
    }
    // For using a list or map to access the collection of views--whichever
    // is more convenient.
    private class Views {

        private List<GridView> mlstOrderedViews;    // Ordered views
        private Map<String, GridView> mmViews;      // Views keyed by view name

        public Views(final List<GridView> views) {
            mlstOrderedViews = views;
            mmViews = viewsToHashMap(views); //setViews(views);
        }
        public void setViews(final List<GridView> views) {
            mlstOrderedViews = views;
            mmViews = viewsToHashMap(views);
        }
        public List<GridView> getOrderedList() {
            return mlstOrderedViews;
        }
        public Map<String, GridView> getMap() {
            return mmViews;
        }
        public GridView get(String key) {
            return mmViews.get(key);
        }
        public int size() {
            return mlstOrderedViews.size();
        }
        // Remove the view from the hash map and ordered list of views
        public void remove(String name) {
            mmViews.remove(name);
            for (GridView view : mlstOrderedViews) {
                if (view.getName().equals(name)) {
                    mlstOrderedViews.remove(view);
                    break;
                }
            }
        }
        // Add the view to the hash map and ordered list of views
        public void add(GridView view) {
            mmViews.put(view.getName(), view);
            mlstOrderedViews.add(view);
        }
        private HashMap<String, GridView> viewsToHashMap(List<GridView> views) {
            HashMap<String, GridView> hmReturn = new HashMap<String, GridView>(
                    views.size());
            for (GridView view : views)
                hmReturn.put(view.getName(), view);
            return hmReturn;
        }
    }
    public Broadcaster getBroadcaster() {
        return moBroadcaster;
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
        //tblReturn.addKeyListener(new ClipboardKeyAdapter(tblReturn, true, false
        //        , false));

        tblReturn.setUpdateSelectionOnSort(false); // For freezing columns

        return tblReturn;
    }
    // Sets the table columns/view
    private void setView(String viewName) {
        GridView view;

        if (moViews.getMap().containsKey(viewName)) {
            view = moViews.get(viewName);
        }
        else {
            // If the selected view doesn't exist, add a new view with
            // all columns
            view = new GridView("Everything", moViewHandler.getAllColumns());
            // (Do not add this temporary view to moViews)
        }

        view.applyToTable(moTable, moFreezer.getAllColumnsModel());

        // Set the selected column groups in the menu
        for (String groupKey : moColGroupMenus.keySet()) {
            for (JMenuItem menuItem : moColGroupMenus.get(groupKey)) {
                menuItem.setSelected(
                        isColumnGroupFullyInView(groupKey, view));
                        //moViewColumnGroups.get(viewName).contains(columnKey));
            }
        }

        // Set the selected columns in the menu
        for (Object identifier : moColMenus.keySet()) {
            boolean selected = isColumnInView(identifier, view);
            // (A single column may have multiple menu items)
            for (JCheckBoxMenuItem item : moColMenus.get(identifier)) {
                item.setSelected(selected);
            }
        }
    }
    // Returns true if all columns in the column group keyed by columnKey
    // are included in the given grid view.
    private boolean isColumnGroupFullyInView(String columnKey, GridView view) {
        List<String> lstGroupCols = Arrays.asList(
                moViewHandler.getColumnGroupMap().get(columnKey).getColumns());
        return view.getColOrder().containsAll(lstGroupCols);
    }
    private boolean isColumnInView(Object identifier, GridView view) {
        return view.getColOrder().contains(identifier);
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

        private List<Object> mlstColumns;
        private List<Class> mlstClasses;
        private List<String> mlstColProps;

        public ViewHandler(List<LaborGroup> vLaborGroups) {

            mhmColGroups = createColumnGroups(vLaborGroups);
            malColOrder = setColumnOrder(vLaborGroups);
            //System.out.println(mhmColGroups.size() + " column groups");
            //System.out.println(mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()[0]);

            // Create all the column data---------------------------------------
            mlstColumns = new ArrayList<Object>(Arrays.asList(
                    MyArrayUtils.concatAll(
                    mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()
                    , mhmColGroups.get(COL_GROUP_NICKNAME).getColumns()
                    , mhmColGroups.get(COL_GROUP_GENDER).getColumns()
                    , mhmColGroups.get(COL_GROUP_AGE).getColumns()
                    , mhmColGroups.get(COL_GROUP_EXCL).getColumns())));
            mlstClasses = new ArrayList<Class>(Arrays.asList(new Class[]
                { Boolean.class, String.class, String.class, String.class
                    , Integer.class, String.class, String.class }));
            mlstColProps = new ArrayList<String>(Arrays.asList(new String[]
                { "include", "dwarf.name", "dwarf.nickname", "dwarf.gender"
                          , "dwarf.age", "activeexclusions"
                          , "inactiveexclusions" }));

            // Stats
            String colName;
            for (String key : mhtStats.keySet()) {
                colName = mhtStats.get(key).getName();
                //msaStatCols[statCount++] = colName;
                mlstColumns.add(colName);
                mlstClasses.add(Long.class);   // Stats are Longs
                mlstColProps.add("dwarf.statvalues." + key);
            }

            // Skills
            for (String key : mhtSkills.keySet()) {
                colName = getColumnNameForSkill(mhtSkills.get(key).getName());
                mlstColumns.add(colName);       //  + " Potential"
                mlstClasses.add(Long.class);   // Skills are Longs
                mlstColProps.add("dwarf.skillpotentials." + key);

                // Print relevant skill levels for ranged/close combat
                // (Override dwarf.skilllevels.)
                colName = getColumnNameForSkillLevel(mhtSkills.get(key).getName());
                mlstColumns.add(colName);
                if (mhtSkills.get(key).getName().equals("Ranged Combat")) {
                    mlstClasses.add(String.class);
                    mlstColProps.add("rangedcombatlevels");
                }
                else if (mhtSkills.get(key).getName().equals("Close Combat")) {
                    mlstClasses.add(String.class);
                    mlstColProps.add("closecombatlevels");
                }
                else {
                    mlstClasses.add(Long.class);   // Skill levels are Longs
                    mlstColProps.add("dwarf.skilllevels." + key);
                }
            }
            for (String key : mhtMetaSkills.keySet()) {
                //System.out.println("Adding " + key);
                mlstColumns.add(getColumnNameForSkill(mhtMetaSkills.get(key).getName()));   //  + " Potential"
                mlstClasses.add(Long.class);
                mlstColProps.add("dwarf.skillpotentials." + key);
            }
            mlstColumns.addAll(Arrays.asList(mhmColGroups.get(
                    COL_GROUP_CUR_LABORS).getColumns())); // "Jobs"
            mlstClasses.add(String.class);
            mlstColProps.add("dwarf.jobtext");
        }

        protected ArrayList<ColumnGroup> getColumnGroups() {
            return malColOrder;
        }
        protected Map<String, ColumnGroup> getColumnGroupMap() {
            return mhmColGroups;
        }

        private Map<String, ColumnGroup> createColumnGroups(
                List<LaborGroup> vLaborGroups) {

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
            ArrayList<String> lstSecondaryCols;

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
            lstSecondaryCols = new ArrayList<String>();
            for (String key : mhtSkills.keySet()) {
                Skill skill = mhtSkills.get(key);
                if (skill instanceof SecondarySkill) {
                    String skillName = skill.getName();
                    lstSecondaryCols.add(getColumnNameForSkill(skillName));
                    lstSecondaryCols.add(getColumnNameForSkillLevel(skillName));
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
                    COL_GROUP_SECONDARY, lstSecondaryCols.toArray(
                    new String[lstSecondaryCols.size()]), false));
            for (LaborGroup group : vLaborGroups) {
                List<String> lstCols = getLaborColsForGroup(group.getName());
                String strName = getJobGroupKey(group.getName());
                hmReturn.put(strName, new ColumnGroup(strName
                        , lstCols.toArray(new String[lstCols.size()]), false));
            }
            hmReturn.put(COL_GROUP_CUR_LABORS, new ColumnGroup(
                    COL_GROUP_CUR_LABORS, asJobsCols, true));

            return hmReturn;
        }
        private ArrayList<ColumnGroup> setColumnOrder(
                List<LaborGroup> vLaborGroups) {

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

        private ArrayList<String> getLaborColsForGroup(String groupName) {
            final ArrayList<String> lstReturn = new ArrayList<String>();
            for (Labor labor : mlstLabors) {
                if (labor.getGroupName().equals(groupName)) {
                    lstReturn.add(getColumnNameForSkill(labor.getSkillName()));

                    // Meta skills don't have skill levels
                    if (! mhtMetaSkills.containsKey(labor.getSkillName()))
                        lstReturn.add(getColumnNameForSkillLevel(labor.getSkillName()));
                }
            }
            return lstReturn;
        }

        public MyEditableTableModel<DwarfListItem> createDwarfListModel(
                Collection<Exclusion> vExclusions) {

            MyEditableTableModel<DwarfListItem> mdlReturn;

            //Create the table model--------------------------------------------
            SortKeySwapper swapper = new SortKeySwapper();
            mdlReturn = new MyEditableTableModel<DwarfListItem>(mlstColumns
                    , mlstClasses
                    , mlstColProps, toDwarfListItems(mlstDwarves, vExclusions)
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

            return mdlReturn;
        }
        public List<Object> getAllColumns() {
            return mlstColumns;
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
            catch (Exception ignore) {
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
            catch (Exception ignore) {
                return false;
            }
        }
    }
    private interface CurrentFormatGetter {
        public ValueAndTextFormat getCurrentFormat();
    }
    private ArrayList<DwarfListItem> toDwarfListItems(List<Dwarf> vDwarves
            , Collection<Exclusion> colExclusions) {

        ArrayList<DwarfListItem> vReturn = new ArrayList<DwarfListItem>(
                vDwarves.size());

        for (Dwarf dwarf : vDwarves) {
            vReturn.add(new DwarfListItem(DwarfOrganizerIO.DEFAULT_EXCLUSION_ACTIVE
                    , dwarf, formatJuvenile(dwarf)
                    , formatExclusions(dwarf, true, colExclusions)
                    , formatExclusions(dwarf, false, colExclusions)
                    , listCombatLevels("Close", dwarf.getSkillLevels())
                    , listCombatLevels("Ranged", dwarf.getSkillLevels())));
        }

        return vReturn;
    }
    public void loadData(List<Dwarf> lstDwarves
            , Collection<Exclusion> vExclusions) {
        mbLoading = true;

        // Set locals-----------------------------------------------------------
        mlstDwarves = lstDwarves;

        // Set table contents---------------------------------------------------
        moModel.setRowData(toDwarfListItems(mlstDwarves, vExclusions));
        //MyHandyTable.autoResizeTableColumns(moTable.getTables());
        moFreezer.autoResizeTableColumns();

        // Update labels--------------------------------------------------------
        applyExclusions(vExclusions);
        updatePopulationLabel();

        mbLoading = false;
    }
    private void updatePopulationLabel() {
        mlblPop.setText(mlstDwarves.size() + " total dwarves from XML");
    }

    private void createPopup() {
        JPopupMenu popUp = new JPopupMenu();
        JMenuItem menuItem;
        final JMenuItem[] selectionNeededItems = new JMenuItem[2];

        // Include selected
        menuItem = new JMenuItem("Include Selected");
        //menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setIncluded(true);
            }
        });
        selectionNeededItems[0] = menuItem;
        popUp.add(menuItem);

        menuItem = new JMenuItem("Exclude Selected");
        //menuItem.setMnemonic(KeyEvent.VK_U);
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
        menuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moClipboardHelper.doCopy();
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
                moModel.setValueAt(included
                        , moTable.getMainTable().convertRowIndexToModel(row)
                        , moTable.getMainTable().convertColumnIndexToModel(
                        INCLUDE_COLUMN));
    }

    // Returns the desired menu for this panel.
    // Expected to be called by owner frame
    protected JMenuBar getMenu() {
        return moMenuBar;
    }
    private JMenuBar createMenu(List<GridView> lstView) {

        JMenu menu;
        JMenu subMenu;
        JMenuItem item;
        final Object[] hideableColumns = getHideableColumns();

        JMenuBar menuBar = new JMenuBar();

        // Columns--------------------------------------------------------------
        ArrayList<ColumnGroup> alColGroups = moViewHandler.getColumnGroups();
        int numGroups = alColGroups.size();
        moColGroupMenus = new HashMap<String, JCheckBoxMenuItem[]>(numGroups);
        moColMenus = new HashMap<Object, ArrayList<JCheckBoxMenuItem>>(
                hideableColumns.length);

        menu = new JMenu("Columns");
        //menu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(menu);

        item = new JMenuItem("Show All");
        //item.setMnemonic(KeyEvent.VK_S);
        item.addActionListener(CursorController.createListener(this
                , createAllVisActionListener(hideableColumns, true)));  // Hourglass  al
        menu.add(item);

        item = new JMenuItem("Hide All");
        //item.setMnemonic(KeyEvent.VK_I);
        item.addActionListener(CursorController.createListener(this
                , createAllVisActionListener(hideableColumns, false)));  // Hourglass  al
        menu.add(item);

        // -------------------------------
        menu.add(new JSeparator());

        for (ColumnGroup colGroup : alColGroups) {
            if (! colGroup.getName().equals("Always Show")) {
                List<JCheckBoxMenuItem> lstItem
                        = new ArrayList<JCheckBoxMenuItem>();
                final String[] arrColumns = colGroup.getColumns();

                // Show All, Hide All, and separator
                if (arrColumns.length > 1) {
                    subMenu = new JMenu(colGroup.getName());
                    menu.add(subMenu);

                    item = new JMenuItem("Show All " + colGroup.getName());
                    item.addActionListener(createAllVisActionListener(arrColumns
                            , true));
                    subMenu.add(item);

                    item = new JMenuItem("Hide All " + colGroup.getName());
                    item.addActionListener(createAllVisActionListener(arrColumns
                            , false));
                    subMenu.add(item);

                    // ---------------------------
                    subMenu.add(new JSeparator());
                }
                else {
                    subMenu = menu;
                }

                for (final String col : arrColumns) {
                    final JCheckBoxMenuItem colItem = new JCheckBoxMenuItem(
                            col, colGroup.isShownByDefault());
                    colItem.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setColumnVisible(col, colItem.isSelected()
                                    , moFreezerColModel);
                        }
                    });
                    subMenu.add(colItem);
                    lstItem.add(colItem);
                    addMenuForColumn(moColMenus, col, colItem);
                }
                moColGroupMenus.put(colGroup.getName(), lstItem.toArray(
                        new JCheckBoxMenuItem[lstItem.size()]));
            }
        }

        // Formats--------------------------------------------------------------
        menu = new JMenu("Format");
        //menu.setMnemonic(KeyEvent.VK_O);
        menuBar.add(menu);

        subMenu = new JMenu("Stats");
        //subMenu.setMnemonic(KeyEvent.VK_S);
        menu.add(subMenu);

        //mmStatFormatMenus = new HashMap<ValueAndTextFormat, JMenuItem>(
        //        ValueAndTextFormat.values().length);
        mmStatFormatMenus = new EnumMap<ValueAndTextFormat, JMenuItem>(
                ValueAndTextFormat.class);
        createFormatSubMenu(subMenu, "Stat", mmStatFormatMenus);

        subMenu = new JMenu("Potentials");
        //subMenu.setMnemonic(KeyEvent.VK_T);
        menu.add(subMenu);

        //mmPotFormatMenus = new HashMap<ValueAndTextFormat, JMenuItem>(
        //        ValueAndTextFormat.values().length);
        mmPotFormatMenus = new EnumMap<ValueAndTextFormat, JMenuItem>(
                ValueAndTextFormat.class);
        createFormatSubMenu(subMenu, "Potential", mmPotFormatMenus);

        subMenu = new JMenu("Skill Levels");
        //subMenu.setMnemonic(KeyEvent.VK_K);
        menu.add(subMenu);

        //mmLevelFormatMenus = new HashMap<ValueAndTextFormat, JMenuItem>(
        //        ValueAndTextFormat.values().length);
        mmLevelFormatMenus = new EnumMap<ValueAndTextFormat, JMenuItem>(
                ValueAndTextFormat.class);
        createFormatSubMenu(subMenu, "Skill Level", mmLevelFormatMenus);

        // Views----------------------------------------------------------------
        createViewMenu(lstView);
        menuBar.add(moViewMenu);

        return menuBar;
    }
    private void addMenuForColumn(Map<Object, ArrayList<JCheckBoxMenuItem>> map
            , Object identifier, JCheckBoxMenuItem item) {

        // Create a new list if there isn't an entry with this key yet
        if (! map.containsKey(identifier))
            map.put(identifier, new ArrayList<JCheckBoxMenuItem>());

        // Add the new item to list for the entry
        map.get(identifier).add(item);
    }
    private void addViewToMenu(String viewName, int index, JMenu menu
            , ButtonGroup group, boolean selected) {
        JMenuItem item = createViewMenuItem(viewName, false);
        group.add(item);
        menu.add(item, index);

        // Have to select after adding to button group
        if (selected)
            item.setSelected(selected);
    }
    private JMenuItem createViewMenuItem(final String viewName
            , boolean selected) {

        JMenuItem item = new JRadioButtonMenuItem(viewName);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setView(viewName);
            }
        });
        item.setSelected(selected);
        return item;
    }
    // Returns false if cancelled by user; true otherwise.
    private boolean saveCurrentView() {

        String name;

        Object response = JOptionPane.showInputDialog(this
                , "Enter a name for this view:", "Save View"
                , JOptionPane.PLAIN_MESSAGE);
        if (response == null) // User cancelled
            return false;
        name = response.toString();

        if (moViews.getMap().containsKey(name)) {
            int overwrite = JOptionPane.showConfirmDialog(this
                    , "Overwrite the current '" + name + "'?"
                    , "Confirm Overwrite", JOptionPane.OK_CANCEL_OPTION);
            if (JOptionPane.CANCEL_OPTION == overwrite
                    || JOptionPane.CLOSED_OPTION == overwrite)
                return false;
            else {
                System.out.println("overwrite = " + overwrite
                        + "; Deleting view '" + name + "'");
                moViews.remove(name);
            }
        }

        // Create a new view with the current columns
        moViews.add(createView(name));

        // Write to file
        moBroadcaster.notifyListeners(new BroadcastMessage("DwarfListSaveViews"
                , moViews.getOrderedList(), "")); // moViews

        // Add and select menu item
        addViewToMenu(name, moViews.size() - 1 + mintFixedViewMenuItems
                , moViewMenu, moViewButtonGroup
                , true);
        return true;
    }
    // Creates a new view from current columns
    private GridView createView(String name) {
        List<Object> lstOrder = new ArrayList<Object>(
                moTable.getTotalColumns());

        // (In the model, the columns aren't sorted in view order. So we have
        // to reference the actual tables.)
        for (int iCount = 0; iCount < moTable.getTables().length; iCount++) {
            JTable table = moTable.getTables()[iCount];
            for (int jCount = 0; jCount < table.getColumnCount(); jCount++) {
                lstOrder.add(table.getColumnName(jCount));
                //System.out.println(table.getColumnName(jCount));
            }
        }

        // "", GridView.KeyAxis.X_AXIS, false
        return new GridView(name, lstOrder);
    }
    // Called by MainWindow before application is closed
    // Returns false if the user cancels the operation
    public boolean exit() {

        // View-----------------------------------------------------------------
        GridView thisView = createView(GridView.UNSAVED_VIEW_NAME);
        GridView selView = getSelectedView();

        if (! thisView.equals(selView)) {
            int response = JOptionPane.showConfirmDialog(this
                    , "Would you like to save this view?"
                    , "Dwarf List View Changed"
                    , JOptionPane.YES_NO_CANCEL_OPTION);
            if (response == JOptionPane.CANCEL_OPTION
                    || response == JOptionPane.CLOSED_OPTION) {
                return false;
            }
            else if (response == JOptionPane.NO_OPTION) {
                // Do nothing
            }
            else if (response == JOptionPane.YES_OPTION) {
                if (! saveCurrentView()) // Same return values as this function
                    return false;
            }
            else {
                System.err.println("[DwarfListWindow.checkForChanges()]"
                        + " Unknown response from JOptionPane: " + response);
            }
        }

        // Preferences----------------------------------------------------------
        moPrefs.savePreferences();

        return true;
    }
    private Object[] getHideableColumns() {
        List<Object> vAllColumns = moViewHandler.getAllColumns();
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
                if (moColMenus != null) {  //moColGroupMenus
                    moTableVis.incrementHidden();

                    // Show or hide columns
                    setColumnsVisible(affectedColumns, visible
                            , moFreezerColModel);

                    // Set menu item states
                    for (Object identifier : affectedColumns) {
                        for (JCheckBoxMenuItem item : moColMenus.get(identifier))
                            item.setSelected(visible);
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

        final List<DwarfListItem> vRows = moModel.getRowData();
        for (DwarfListItem item : vRows) {
            if (item.isIncluded())
                sum++;
        }

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
    protected ArrayList<Dwarf> getIncludedDwarves() {

        ArrayList<Dwarf> lstIncluded = new ArrayList<Dwarf>();

        for (Dwarf dwarf : mlstDwarves)
            if (isDwarfIncluded(dwarf.getName()))
                lstIncluded.add(dwarf);

        return lstIncluded;
    }

    // Returns true if the dwarf with the given name is included
    private boolean isDwarfIncluded(String dwarfName) {
        final List<DwarfListItem> vRows = moModel.getRowData();
        for (DwarfListItem item : vRows) {
            if (item.getDwarf().getName().equals(dwarfName)
                    && item.isIncluded())
                return true;
        }
        return false;
    }

    private void setColumnsVisible(Object[] cols, boolean visible
            , HideableTableColumnModel hideableModel) {

        // Hide the table
        moTableVis.incrementHidden();

        for (Object col : cols) {
            hideableModel.setColumnVisible(col, visible);
        }

        // Resize the columns on visible=true, in case the format was
        // changed while hidden
        if (visible) {
            moFreezer.autoResizeTableColumns(cols);
        }

        // Show the table
        moTableVis.incrementShown();
    }
    // Same as setColumnsVisible, except for just one column
    private void setColumnVisible(Object col, boolean visible
            , HideableTableColumnModel hideableModel) {

        // Hide the table
        moTableVis.incrementHidden();

        hideableModel.setColumnVisible(col, visible);

        // Resize the column on visible=true, in case the format was
        // changed while hidden
        if (visible) {
            moFreezer.autoResizeTableColumn(col);
        }

        // Show the table
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
    private void printStatGroups(Map<String, List<Stat>> statGroups) {
        System.out.println("Printing " + statGroups.size() + " stat groups:");
        for (String key : statGroups.keySet()) {
            System.out.println(key + ":");
            printStatGroup(statGroups.get(key));
        }
        System.out.println("Done printing stat groups.");
    }
    private void printStatGroup(List<Stat> statGroup) {
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
                if (skill instanceof SecondarySkill && ! (skill instanceof
                        SocialSkill))

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
            ex.printStackTrace(System.out);
            Logger.getLogger(DwarfListWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        } finally {
            try {
                fstream.close();
            } catch (IOException ex) {
                ex.printStackTrace(System.out);
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
            out.write("        <Trait>" + social.getNoStatName() + "</Trait>");
            out.newLine();
            out.write("        <Min>" + social.getNoStatMin() + "</Min>");
            out.newLine();
            out.write("        <Max>" + social.getNoStatMax() + "</Max>");
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
            , Map<String, Long> skillLevels) {

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
        final List<DwarfListItem> vRows = moModel.getRowData();
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
    public void updateViews(List<GridView> newViews) {
        createDynamicViewItems(moViewMenu, moViewButtonGroup, newViews
                , moViews.size(), getSelectedViewName());
        moViews.setViews(newViews);
    }
    private String getSelectedViewName() {
        for (int iCount = 0; iCount < moViews.size(); iCount++) {
            JMenuItem item = moViewMenu.getItem(
                    iCount + mintFixedViewMenuItems);
            if (item.isSelected())
                return item.getText();
        }
        return DEFAULT_VIEW_NAME;
    }
    private GridView getSelectedView() {
        try {
            // The view may not exist
            return moViews.get(getSelectedViewName());
        } catch (Exception ignore) {
            return new GridView("Unknown View", new ArrayList<Object>());
        }
    }
    private void createDynamicViewItems(JMenu viewMenu, ButtonGroup viewGroup
            , List<GridView> views
            , int numOldViews, String selectedView) {

        // Remove any existing dynamic view menu items
        for (int iCount = numOldViews - 1; iCount >= 0; iCount--) {
            viewMenu.remove(iCount + mintFixedViewMenuItems);
        }

        // Add dynamic view menu items
        int index = 0;
        for (GridView view : views) {
            String name = view.getName();
            addViewToMenu(name, index + mintFixedViewMenuItems, viewMenu
                    , viewGroup, name.equals(selectedView));
            index++;
        }
    }
    // Creates moViewMenu and moViewButtonGroup
    private void createViewMenu(List<GridView> views) {
        JMenuItem item;
        moViewMenu = new JMenu("View");
        //moViewMenu.setMnemonic(KeyEvent.VK_V);

        // Save current view
        item = new JMenuItem("Save Current View As...");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean ignore = saveCurrentView();
            }
        });
        moViewMenu.add(item);

        // Manage views
        item = new JMenuItem("Manage Views...");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                moBroadcaster.notifyListeners(new BroadcastMessage(
                        "DwarfListManageViews", null, ""));
            }
        });
        moViewMenu.add(item);

        // -----------------------------
        moViewMenu.add(new JSeparator());
        mintFixedViewMenuItems = moViewMenu.getMenuComponentCount();

        // Dynamic views
        moViewButtonGroup = new ButtonGroup();
        createDynamicViewItems(moViewMenu, moViewButtonGroup, views, 0
                , moPrefs.getViewName()); // DEFAULT_VIEW_NAME
    }
}
