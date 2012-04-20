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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import myutils.MyArrayUtils;
import myutils.MyHandyTable;
import myutils.MySimpleTableModel;
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

    private Hashtable<String, Stat> mhtStats;
    private Hashtable<String, Skill> mhtSkills;
    private Vector<Dwarf> mvDwarves;
    private Hashtable<String, MetaSkill> mhtMetaSkills;

    //JobListPanel moOptimizerSettings;

    private JTable moTable;
    //private MySimpleTableModel moModel;
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

    private Map<String, GridView> moViews;
    private ViewHandler moViewHandler;
    
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
        mvDwarves = new Vector<Dwarf>(); //vDwarves;
        Collection<Exclusion> vExclusions = new Vector<Exclusion>();

        mbLoading = true;

        // Create table column data---------------------------------------------
        moViewHandler = new ViewHandler(vLaborGroups);
        moModel = moViewHandler.createDwarfListModel(vExclusions);

        // Create the dwarf data table------------------------------------------
        moTable = new JTable(moModel, new HideableTableColumnModel());
        moTable.createDefaultColumnsFromModel(); // Necessary when using HideableTableColumnModel

        // Set column renderer for skill levels (numeric format, right-justified,
        // nulls blank)
        for (String key : mhtSkills.keySet()) {
            String id = getColumnNameForSkillLevel(mhtSkills.get(key).getName());
            moTable.getColumn(id).setCellRenderer(new NumberOrNullRenderer());
        }

        moTable.setDefaultRenderer(Boolean.class, new MyTCRStripedHighlightCheckBox());
        moTable.setDefaultRenderer(Object.class, new MyTCRStripedHighlight());

        // For cleaner copy/paste to spreadsheets
        moTable.setColumnSelectionAllowed(false);
        moTable.setRowSelectionAllowed(true);
        moTable.addKeyListener(new ClipboardKeyAdapter(moTable, true, false, false));
        
        mspScrollPane = new JScrollPane(moTable);

        // Sort by name
        MyHandyTable.handyTable(moTable, mspScrollPane, moModel, true, 1, true);   // this

        // Create the popup menu for mass including/excluding
        createPopup();

/*        // Hide columns for labor groups and secondary skills by default
        for (Labor labor : mvLabors)
            setLaborGroupVisible(labor.groupName, false);
        setSecondaryColumnsVisible(false);
*/
        moViewHandler.createDwarfListViews();
        setView(DEFAULT_VIEW_NAME);    // // "Military View"
        //FixedColumnTable frozen = new FixedColumnTable(2, mspScrollPane); TODO
        
        // Show some statistics-------------------------------------------------
        mlblPop = new JLabel("X total dwarves from XML"); // total adult
        mlblSelected = new JLabel(getNumSelectedText(mvDwarves.size()));
        mlblExclusions = new JLabel("X active exclusion rules/lists");
        applyExclusions(vExclusions); // Apply exclusions to data & update labels

        // Build the UI---------------------------------------------------------
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(mspScrollPane);
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
    private void setView(String viewName) {
        moViews.get(viewName).applyToTable(moTable);
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
        private static final String COL_GROUP_ALWAYS_SHOW = "Always Show";
        private static final String COL_GROUP_NICKNAME = "Nickname";
        private static final String COL_GROUP_GENDER = "Gender";
        private static final String COL_GROUP_AGE = "Age";
        private static final String COL_GROUP_EXCL = "Exclusion Info";
        private static final String COL_GROUP_STATS = "Stats";
        private static final String COL_GROUP_SECONDARY = "Secondary Skills";
        private static final String COL_GROUP_CUR_LABORS = "Current Labors";
        
        private Map<String, ColumnGroup> mhmColGroups;
        private ArrayList<ColumnGroup> malColOrder;
        
        private Vector<Object> mvColumns;
        private Vector<Class> mvClasses;
        private Vector<String> mvColProps;

        public ViewHandler(Vector<LaborGroup> vLaborGroups) {

            mhmColGroups = createColumnGroups(vLaborGroups);
            malColOrder = setColumnOrder(vLaborGroups);
            System.out.println(mhmColGroups.size() + " column groups");
            System.out.println(mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()[0]);
            
            // Create all the column data---------------------------------------
            mvColumns = new Vector<Object>(Arrays.asList(MyArrayUtils.concatAll(
                    mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()
                    , mhmColGroups.get(COL_GROUP_NICKNAME).getColumns()
                    , mhmColGroups.get(COL_GROUP_GENDER).getColumns()
                    , mhmColGroups.get(COL_GROUP_AGE).getColumns()
                    , mhmColGroups.get(COL_GROUP_EXCL).getColumns())));
    /*        Vector<Object> vColumns = new Vector<Object>(Arrays.asList(new String[]
                { "Include", "Name", "Nickname", "Gender", "Age", "Exclusion"
                          , "Inactive Lists"
                    })); */ // , "Squad", "Squad Leader", "Strength", "Agility", "Focus"
                    //, "Patience", "Kinesthetic Sense"
            mvClasses = new Vector<Class>(Arrays.asList(new Class[]
                { Boolean.class, String.class, String.class, String.class
                    , Integer.class, String.class, String.class }));
            mvColProps = new Vector<String>(Arrays.asList(new String[]
                { "include", "dwarf.name", "dwarf.nickname", "dwarf.gender"
                          , "dwarf.age", "activeexclusions"
                          , "inactiveexclusions" }));

            // Stats
            //msaStatCols = new String[mhtStats.size()];
            //int statCount = 0;
            String colName;
            for (String key : mhtStats.keySet()) {
                colName = mhtStats.get(key).getName();
                //msaStatCols[statCount++] = colName;
                mvColumns.add(colName);
                mvClasses.add(Long.class);   // Stats are Longs
                mvColProps.add("dwarf.statvalues." + key);
            }
            
            // Skills
            //mvSecondaryCols = new Vector<String>();
            for (String key : mhtSkills.keySet()) {
                colName = getColumnNameForSkill(mhtSkills.get(key).getName());
                //if (mhtSkills.get(key) instanceof SecondarySkill)
                //    mvSecondaryCols.add(colName);
                mvColumns.add(colName);       //  + " Potential"
                mvClasses.add(Long.class);   // Skills are Longs
                mvColProps.add("dwarf.skillpotentials." + key);

                // Print relevant skill levels for ranged/close combat
                // (Override dwarf.skilllevels.)
                colName = getColumnNameForSkillLevel(mhtSkills.get(key).getName());
                //if (mhtSkills.get(key) instanceof SecondarySkill)
                //    mvSecondaryCols.add(colName);
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
                mvColumns.add(getColumnNameForSkill(mhtMetaSkills.get(key).name));   //  + " Potential"
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
                String strName = "Job Group: " + group.getName();
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
                alReturn.add(mhmColGroups.get("Job Group: " + group.getName()));
            }
            alReturn.add(mhmColGroups.get(COL_GROUP_CUR_LABORS));
            return alReturn;
        }
        
        private Vector<String> getLaborColsForGroup(String groupName) {
            Vector<String> vReturn = new Vector<String>();
            for (Labor labor : mvLabors) {
                if (labor.getGroupName().equals(groupName)) {
                    vReturn.add(getColumnNameForSkill(labor.getSkillName()));
                    vReturn.add(getColumnNameForSkillLevel(labor.getSkillName()));
                }
            }
            return vReturn;
        }
        
        public void createDwarfListViews() {
            moViews = new HashMap<String, GridView>();

            // TODO: Read from file?
            // Default view-----------------------------------------------------
            List<Object> colOrder = new ArrayList<Object>();
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_ALWAYS_SHOW).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_NICKNAME).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_GENDER).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_AGE).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_EXCL).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_STATS).getColumns()));
            colOrder.addAll(Arrays.asList(mhmColGroups.get(COL_GROUP_CUR_LABORS).getColumns()));
            //System.out.println("colOrder has " + colOrder.size() + " entries");
            
            moViews.put(DEFAULT_VIEW_NAME, new GridView(DEFAULT_VIEW_NAME, "Name"
                    , GridView.KeyAxis.X_AXIS, false, colOrder));

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
            //moModel = new MySimpleTableModel(vColumns
            //        , mvDwarves.size());    //  nodes.getLength()
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
        MyHandyTable.autoResizeTableColumns(moTable, mspScrollPane);

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

        // Include selected
        menuItem = new JMenuItem("Include Selected");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setIncluded(true);
            }
        });
        popUp.add(menuItem);

        menuItem = new JMenuItem("Un-include Selected");
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setIncluded(false);
            }
        });
        popUp.add(menuItem);

        // ---------------------------------------------------------------------
        popUp.add(new JSeparator());
        
        // Copy selected rows to clipboard in spreadsheet-friendly format-----
        menuItem = new JMenuItem("Copy");
        menuItem.setAccelerator(KeyStroke.getKeyStroke("ctrl C"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MyHandyTable.cancelEditing(moTable);
                MyHandyTable.copyToClipboard(moTable, false);                
            }
        });
        popUp.add(menuItem);
        
        moTable.setComponentPopupMenu(popUp);
    }

    // Sets whether selected table rows are Included
    private void setIncluded(boolean included) {
        for (int row = 0; row < moTable.getRowCount(); row++)
            if (moTable.isRowSelected(row))
                moModel.setValueAt(included, moTable.convertRowIndexToModel(row)
                        , moTable.convertColumnIndexToModel(INCLUDE_COLUMN));
    }

    // Returns the desired menu for this panel.
    // Expected to be called by owner frame
    protected JMenuBar getMenu(Vector<LaborGroup> vLaborGroups) {

/*        final String[] vitalsCols = new String[] { "Gender", "Age" };
        final String[] exclCols = new String[] { "Exclusion", "Inactive Lists" };
        final String[] nickCol = new String[] { "Nickname" };
        final String[] jobsCol = new String[] { "Jobs" }; */

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Columns");
        menu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(menu);

        ArrayList<ColumnGroup> alColGroups = moViewHandler.getColumnGroups();        
        for (ColumnGroup group : alColGroups) {
            if (! group.getName().equals("Always Show")) {
                //ColumnGroup columnGroup = hmColGroups.get(key);
                final String[] arrColumns = group.getColumns();
                final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(
                        group.getName(), group.isShownByDefault());
                menuItem.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setColumnsVisible(arrColumns, menuItem.isSelected());
                    }
                });
                menu.add(menuItem);
            }
        }
        menu.add(new JSeparator());
        
/*        final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Nickname", true);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setColumnsVisible(nickCol, menuItem.isSelected());
            }
        });
        menu.add(menuItem);

        final JCheckBoxMenuItem vitalsItem = new JCheckBoxMenuItem("Some Vitals", true);
        vitalsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setColumnsVisible(vitalsCols, vitalsItem.isSelected());
            }
        });
        menu.add(vitalsItem);

        final JCheckBoxMenuItem exclItem = new JCheckBoxMenuItem("Exclusion Info", true);
        exclItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setColumnsVisible(exclCols, exclItem.isSelected());
            }
        });
        menu.add(exclItem);

        final JCheckBoxMenuItem statItem = new JCheckBoxMenuItem("Stats", true);
        statItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStatColumnsVisible(statItem.isSelected());
            }
        });
        menu.add(statItem);

        final JCheckBoxMenuItem secondaryItem = new JCheckBoxMenuItem(
                "Secondary Skills", false);
        secondaryItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSecondaryColumnsVisible(secondaryItem.isSelected());
            }
        });
        menu.add(secondaryItem);

        for (final LaborGroup group : vLaborGroups) {
            final JCheckBoxMenuItem jobGroupItem = new JCheckBoxMenuItem(
                    "Job Group: " + group.getName(), false);
            jobGroupItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLaborGroupVisible(group.getName()
                            , jobGroupItem.isSelected());
                }
            });
            menu.add(jobGroupItem);
        }

        final JCheckBoxMenuItem jobItem = new JCheckBoxMenuItem("Current Labors", true);
        jobItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setColumnsVisible(jobsCol, jobItem.isSelected());
            }
        });
        menu.add(jobItem);
*/
        // Views----------------------------------------------------------------
        menu = new JMenu("View");
        menuBar.add(menu);
        //System.out.println("Menu count: " + menuBar.getMenuCount());

        JMenuItem item;
        ButtonGroup group = new ButtonGroup();
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
        item.setEnabled(false); // TODO
        menu.add(item);

        return menuBar;
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

    // Sets the visibility of the columns associated with the given labor group
    private void setLaborGroupVisible(String group, boolean visible) {
        for (Labor labor : mvLabors) {
            if (labor.getGroupName().equals(group)) {
                //System.out.println("Setting visibility of " + labor.skillName);
                setColumnVisible(getColumnNameForSkill(labor.getSkillName()), visible);
                setColumnVisible(getColumnNameForSkillLevel(labor.getSkillName()), visible);
            }
        }
    }

    // Sets the visibility of the columns associated with the secondary labors
    private void setSecondaryColumnsVisible(boolean visible) {
        for (Skill skill : mhtSkills.values()) {
            if (skill instanceof SecondarySkill) {
                setColumnVisible(getColumnNameForSkill(skill.getName()), visible);
                setColumnVisible(getColumnNameForSkillLevel(skill.getName()), visible);
            }
        }
    }

    // Sets the visibility of stat columns
    private void setStatColumnsVisible(boolean visible) {
        for (Stat stat : mhtStats.values())
            setColumnVisible(stat.getName(), visible);
    }
    private void setColumnsVisible(String[] cols, boolean visible) {
        for (String col : cols)
            setColumnVisible(col, visible);
    }

    // Sets visibility of the column with the given name in the hideable model
    private void setColumnVisible(String colName, boolean visible) {

        HideableTableColumnModel hideableModel
                = (HideableTableColumnModel) moTable.getColumnModel();

        hideableModel.setColumnVisible(colName, visible);

/*        for (int iCount = hideableModel.getColumnCount(false) - 1; iCount >= 0; iCount--) {
            //System.out.println("Number of columns: " + hideableModel.getColumnCount(false));
            Object oThisIdentifier = hideableModel.getColumn(iCount, false).getIdentifier();
            //System.out.println("Checking " + oThisIdentifier + " for " + colIdentifier);
            if (oThisIdentifier.equals(colName)) {
                //System.out.println("Match!");
                hideableModel.setColumnVisible(hideableModel.getColumn(iCount, false)
                        , visible);
            }
        }                */
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

    // Loads the big JTable o' dwarves (Dwarf List)
    private void setModelData(MySimpleTableModel oModel
            , Collection<Exclusion> vExclusions) {
        // , NodeList nodes

        int iCount = 0; // row count

        for (Dwarf oDwarf : mvDwarves) {

            // Default Include for now (exclusions are post-processed)
            oModel.setValueAt(DwarfOrganizerIO.DEFAULT_EXCLUSION_ACTIVE
                    , iCount, 0);  // "Include"

            oModel.setValueAt(oDwarf.getName(), iCount, 1);   //getTagValue(thisCreature, "Name")
            oModel.setValueAt(oDwarf.getNickname(), iCount, 2); // getTagValue(thisCreature, "Nickname")
            //oModel.setValueAt("Unknown", iCount, 2);
            oModel.setValueAt(oDwarf.getGender(), iCount, 3); //getTagValue(thisCreature, "Sex")
            //oModel.setValueAt(formatJuvenile(oDwarf), iCount, 4); //  strJuvenile
            oModel.setValueAt(oDwarf.getAge(), iCount, 4);

            // Too bad these aren't in exported dwarves.XML...
            //oModel.setValueAt("Unknown", iCount, 5);  // Squad
            //oModel.setValueAt("Unknown", iCount, 6);  // Squad leader

            // Exclusion lists
            oModel.setValueAt(formatExclusions(oDwarf, true, vExclusions)
                    , iCount, 5);       // Active exclusion lists
            oModel.setValueAt(formatExclusions(oDwarf, false, vExclusions)
                    , iCount, 6);  // Inactive exclusion lists

            // Stats
            int columnIndex = 7;  // 6; 5; 7;
            for (Stat stat : mhtStats.values()) {
                oModel.setValueAt(oDwarf.statValues.get(stat.getName())
                        , iCount, columnIndex);
                columnIndex++;
            }

            // Job potentials: simple jobs
            for (String key : mhtSkills.keySet()) {
                // Skill potential
                oModel.setValueAt(oDwarf.skillPotentials.get(key)   // getPotential(oDwarf, oSkill)
                        , iCount, columnIndex);
                columnIndex++;

                // Skill level for ranged and close combat
                if (key.equals("Ranged Combat"))
                    oModel.setValueAt(listCombatLevels("Ranged", oDwarf.skillLevels)
                            , iCount, columnIndex);
                else if (key.equals("Close Combat"))
                    oModel.setValueAt(listCombatLevels("Close", oDwarf.skillLevels)
                            , iCount, columnIndex);
                else
                    oModel.setValueAt(oDwarf.skillLevels.get(key), iCount, columnIndex);

                columnIndex++;
            }

            // Job potentials: meta jobs
            for (String key : mhtMetaSkills.keySet()) {
                oModel.setValueAt(oDwarf.skillPotentials.get(key), iCount, columnIndex);

                /*MetaSkill meta = mhtMetaSkills.get(key);
                Vector<Skill> vSkills = meta.vSkills;
                double numSkills = (double) vSkills.size();

                double dblSum = 0.0d;
                for (Skill oSkill : vSkills)
                    dblSum += oDwarf.jobPotentials.get(oSkill.name);

                oModel.setValueAt(Math.round(dblSum / numSkills), iCount, columnIndex); */
                columnIndex++;
            }

            // Jobs
            oModel.setValueAt(oDwarf.getJobText() //getTagList(thisCreature, "Labours")   // , "Labour"
                , iCount, columnIndex);

            iCount++;
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
        for (int iCount = 0; iCount < values.length; iCount++)
            if (attribute >= values[iCount])
                return iCount;
        return values.length;
    }
    private int getBracket(int[] values, int attribute) {
        return values.length - 1 - getAttributeBracket(values, attribute);
    }

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
