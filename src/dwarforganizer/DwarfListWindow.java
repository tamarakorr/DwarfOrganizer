/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.Stat.StatHint;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import myutils.MyHandyTable;
import myutils.MySimpleTableModel;
import myutils.MyTCRStripedHighlight;
import myutils.MyTCRStripedHighlightCheckBox;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public class DwarfListWindow extends JPanel {
    
    // Note that this XML file is read differently from other files in this
    // application. We have to provide src/data as part of the path because
    // we are having to locate it from the root of the .jar, since we cannot use
    // getResourceAsStream() to read an XML file.
    //private static final String LABOR_SKILLS_XML_FILE_NAME = "src/data/labor-skills.xml";
    //private static final String LABOR_SKILLS_XML_FILE_NAME = "/data/labor-skills.xml";
    private static final String LABOR_SKILLS_XML_FILE_NAME = "config/labor-skills.xml";
    
    private static final int INCLUDE_COLUMN = 0;  // Index of the "Include" column in the table
    
    // Secondary skills are a convenience for filtering the list. They're
    // the same as a Skill.
    class SecondarySkill extends Skill {
        public SecondarySkill(String name, Vector<Stat> stats) {
            super(name, stats);
        }
    }
    
    // Social skills defined here as skills whose development can be prevented completely by
    // a trait.
    class SocialSkill extends SecondarySkill {
        protected String noStatName;
        protected int noStatMin;
        protected int noStatMax;
        
        public SocialSkill(String name, Vector<Stat> stats, String noStatName
                , int noStatMin, int noStatMax) {
            super(name, stats);
            this.noStatName = noStatName;
            this.noStatMin = noStatMin;
            this.noStatMax = noStatMax;
        }
    }    
    
    // Table cell renderer for number-formatted column with null values
    // displayed as blanks
    static class NumberOrNullRenderer extends MyTCRStripedHighlight {
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
    
    //private static final String TRAITS_FILE_NAME = "/data/trait-hints.txt";
    private static final String TRAITS_FILE_NAME = "config/trait-hints.txt";
    private static final int MAX_DWARF_TIME = 100;
    
    private static final String DEFAULT_DWARF_AGE = "999";
    private static final String DEFAULT_TRAIT_VALUE = "50";
    
    private String[] masSocialTraits = { "Friendliness", "Self_consciousness"
        , "Straightforwardness", "Cooperation", "Assertiveness" };
    
//    private static final long MAX_SKILL_LEVEL = 20l;    // That's an "L", not a one
    
    /*private int[] strengthValues = { 2250, 2000, 1750, 1500, 1001, 751, 501, 251, 0 };
    private int[] patienceValues = { 2250, 2000, 1750, 1500, 1001, 501, 251, 0 };
    private int[] kinsenseValues = { 2000, 1750, 1500, 1250, 751, 501, 251, 1, 0 };
    private int[] focusValues = { 2542, 2292, 2042, 1792, 1293, 1043, 793, 543, 0 };
    private int[] agiValues = {1900, 1650, 1400, 1150, 651, 401, 151, 0 };
    */
    
    private long[] plusplusRange = { 700, 1200, 1400, 1500, 1600, 1800, 2500 };
    private long[] plusRange = { 450, 950, 1150, 1250, 1350, 1550, 2250 };
    private long[] avgRange = { 200, 750, 900, 1000, 1100, 1300, 2000 };
    private long[] minusRange = { 150, 600, 800, 900, 1000, 1100, 1500 };
    private long[] socialRange = { 0, 10, 25, 61, 76, 91, 100 };
    
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
    private String[] plusplusDesc = { "ERROR", "1 Very Poor", "2 Poor"
            , "3 Below Average", "4 Above Average", "5 Good", "6 Very Good" };
    
    private Hashtable<String, Stat> mhtStats = new Hashtable<String, Stat>();
    private Hashtable<String, Skill> mhtSkills = new Hashtable<String, Skill>();
    private Vector<Dwarf> mvDwarves = new Vector<Dwarf>();
    private Hashtable<String, MetaSkill> mhtMetaSkills = new Hashtable<String, MetaSkill>();
    
    //JobListPanel moOptimizerSettings;
    
    private JTable moTable;
    private MySimpleTableModel moModel;
    private JLabel lblSelected;
    
    private boolean mbLoading = false;
    
    // Any character 0 or more times, followed by a [, followed by one or more
    // digits, followed by a ], followed by any character 0 or more times:
    private static Pattern mpatSkillLevel = Pattern.compile("(.*\\[)(\\d+)(\\].*)");
    
    private Vector<Labor> mvLabors; // Set in constructor
    
    public DwarfListWindow(NodeList nodes, Vector<Labor> vLabors
            , Vector<LaborGroup> vLaborGroups) {
        
        super();
        
        mvLabors = vLabors;
        
        mbLoading = true;
        
        //createMenu();
        
        createSkills();
        
        // Read trait-hints.txt
        readTraitHints();   //path
        
        // Read dwarves from the node list
        if (nodes != null)
            getDwarves(nodes);
        
        // Create table column headers.
        Vector<String> vColumns = new Vector<String>(Arrays.asList(new String[]
            { "Include", "Name", "Nickname", "Gender", "Juvenile"
                })); // , "Squad", "Squad Leader", "Strength", "Agility", "Focus"
                //, "Patience", "Kinesthetic Sense"
        for (String key : mhtStats.keySet())
            vColumns.add(mhtStats.get(key).name);
        for (String key : mhtSkills.keySet()) {
            vColumns.add(getColumnNameForSkill(mhtSkills.get(key).name));       //  + " Potential"
            vColumns.add(getColumnNameForSkillLevel(mhtSkills.get(key).name));
        }
        for (String key : mhtMetaSkills.keySet())
            vColumns.add(getColumnNameForSkill(mhtMetaSkills.get(key).name));   //  + " Potential"
        vColumns.add("Jobs");
        
        // Display data in a grid (JTable)
        moModel = new MySimpleTableModel(vColumns
                , mvDwarves.size());    //  nodes.getLength()
        moModel.addEditableException(0);     // First column editable.
        moModel.addTableModelListener(new TableModelListener() {
        
            @Override
            public void tableChanged(TableModelEvent e) {
                if (! mbLoading)
                    updateSelectedLabel();
            }
            
        });
        setModelData(moModel);       // , nodes
        
        // Create the dwarf data table
        moTable = new JTable(moModel, new HideableTableColumnModel());
        moTable.createDefaultColumnsFromModel(); // Necessary when using HideableTableColumnModel
        
        // Set column renderer for skill levels (numeric format, right-justified, nulls blank)
        for (String key : mhtSkills.keySet()) {
            String id = getColumnNameForSkillLevel(mhtSkills.get(key).name);
            moTable.getColumn(id).setCellRenderer(new NumberOrNullRenderer());
        }
        
        moTable.setDefaultRenderer(Boolean.class, new MyTCRStripedHighlightCheckBox());
        moTable.setDefaultRenderer(Object.class, new MyTCRStripedHighlight());        

        JScrollPane oSP = new JScrollPane(moTable);
        //setViewportView(oTable);
        
        // Sort by name
        MyHandyTable.handyTable(moTable, oSP, moModel, true, 1, true);   // this
        
        // Create the popup menu for mass including/excluding
        createPopup();
        
        // Hide columns for labor groups and secondary skills by default
        for (Labor labor : mvLabors)
            setLaborGroupVisible(labor.groupName, false);
        setSecondaryColumnsVisible(false);
        
        //setPreferredSize(oTable.getPreferredScrollableViewportSize());
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(oSP);
        tablePanel.setPreferredSize(new Dimension(750, 250));
        
        // Show some statistics
        JLabel lblPop = new JLabel(mvDwarves.size() + " total dwarves from XML"); // total adult
        lblSelected = new JLabel(getNumSelectedText(mvDwarves.size()));
        updateSelectedLabel();
        
        JPanel panInfo = new JPanel();
        panInfo.setLayout(new BorderLayout());
        panInfo.add(lblPop, BorderLayout.PAGE_START);
        panInfo.add(lblSelected, BorderLayout.PAGE_END);
        
        // Create checkboxes for table column groups.
        //JPanel panColumns = new JPanel();
        //panColumns.setLayout(new BoxLayout(panColumns, BoxLayout.PAGE_AXIS));
        
        JPanel panDwarfList = new JPanel();
        //panDwarfList.setLayout(new BoxLayout(panDwarfList, BoxLayout.PAGE_AXIS));
        panDwarfList.setLayout(new BorderLayout());
        panDwarfList.add(tablePanel, BorderLayout.CENTER);
        panDwarfList.add(panInfo, BorderLayout.SOUTH);
        //panDwarfList.add(panColumns, BorderLayout.SOUTH);
        //panDwarfList.add(buttonPanel);
        
        this.setLayout(new BorderLayout());
        this.add(panDwarfList); // , BorderLayout.LINE_START
        //this.add(moOptimizerSettings, BorderLayout.LINE_END);
        
        mbLoading = false;
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
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Columns");
        menu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(menu);
        
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
                    "Job Group: " + group.name, false);
            jobGroupItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLaborGroupVisible(group.name, jobGroupItem.isSelected());
                }
            });
            menu.add(jobGroupItem);
        }

        return menuBar;
    }
    
    // Sets the display for number of included dwarves
    private void updateSelectedLabel() {
        int sum = 0;
        
        for (int row = 0; row < moModel.getRowCount(); row++)
            if ((Boolean) moModel.getValueAt(row, INCLUDE_COLUMN))
                sum++;
        lblSelected.setText(getNumSelectedText(sum));
    }
    
    private String getNumSelectedText(int numDwarves) {
         return numDwarves + " dwarves selected for optimization";
    }
    
    // Returns a vector of Included dwarves
    protected Vector<Dwarf> getIncludedDwarves() {
        
        Vector<Dwarf> vIncluded = new Vector<Dwarf>();
        
        for (Dwarf dwarf : mvDwarves)
            if (isDwarfIncluded(dwarf.name))
                vIncluded.add(dwarf);
        
        return vIncluded;
    }
    
    // Returns true if the dwarf with the given name is included
    private boolean isDwarfIncluded(String dwarfName) {
        
        final int NAME_COLUMN = 1;
        
        for (int row = 0; row < moModel.getRowCount(); row++)
            if (moModel.getValueAt(row, NAME_COLUMN).equals(dwarfName)
                && ((Boolean) moModel.getValueAt(row, INCLUDE_COLUMN)))
                return true;
        return false;
        
    }
    
    // Sets the visibility of the columns associated with the given labor group
    private void setLaborGroupVisible(String group, boolean visible) {
        for (Labor labor : mvLabors) {
            if (labor.groupName.equals(group)) {
                //System.out.println("Setting visibility of " + labor.skillName);
                setColumnVisible(getColumnNameForSkill(labor.skillName), visible);
                setColumnVisible(getColumnNameForSkillLevel(labor.skillName), visible);
            }
        }
    }
    
    // Sets the visibility of the columns associated with the secondary labors
    private void setSecondaryColumnsVisible(boolean visible) {
        for (Skill skill : mhtSkills.values()) {
            if (skill instanceof SecondarySkill) {
                setColumnVisible(getColumnNameForSkill(skill.name), visible);
                setColumnVisible(getColumnNameForSkillLevel(skill.name), visible);
            }
        }
    }
    
    // Sets the visibility of stat columns
    private void setStatColumnsVisible(boolean visible) {
        for (Stat stat : mhtStats.values())
            setColumnVisible(stat.name, visible);
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
    
    public Vector<Dwarf> getDwarves() {
        return mvDwarves;
    }
    
    // Translates XML data to dwarf objects
    private void getDwarves(NodeList nodes) {
        for (int iCount = 0; iCount < nodes.getLength(); iCount++) {
            
            Element thisCreature = (Element) nodes.item(iCount);

            int age = Integer.parseInt(getTagValue(thisCreature, "Age"
                    , DEFAULT_DWARF_AGE));
            
            // Stopped skipping juveniles for DF 34.05 due to age bugs
            //if (! isJuvenile(age)) {

                // Read stat values and get percentiles.
                Hashtable<String, Long> statValues = new Hashtable<String, Long>();
                Hashtable<String, Long> htPercents = new Hashtable<String, Long>();
                for (String key : mhtStats.keySet()) {
                    //System.out.println("Getting " + mhtStats.get(key).name);

                    long value;
                    if (mhtStats.get(key).xmlName != null)
                        value = Long.parseLong(getTagValue(thisCreature
                            , mhtStats.get(key).xmlName, "0"));
                    
                    else {  // Look under Traits if there is no attribute XML name
                                      
                        // TODO: get dwarven personality average to use as default                        
                        
                        // (DFHack style XML) If the trait has a named entry,
                        // then get the value
                        Element traits = (Element) thisCreature.getElementsByTagName(
                                "Traits").item(0);
                        value = Long.parseLong(getXMLValueByKey(traits, "Trait"
                                , "name", mhtStats.get(key).name, "value"
                                , "-1"));   // DEFAULT_TRAIT_VALUE
                        
                        // If we could not get the exact trait value, perhaps
                        // this is a Runesmith XML file. Check for trait hints.
                        if (value == -1) {
                            value = Long.parseLong(DEFAULT_TRAIT_VALUE);
                            for (StatHint hint : mhtStats.get(key).vStatHints) {
                                //if (traits contains hint)
                                if (getTagList(thisCreature, "Traits").contains(hint.hintText)) {
                                    value = (hint.hintMin + hint.hintMax) / 2;
                                    //System.out.println(mhtStats.get(key).name + " " + value);
                                    break;
                                }
                            }
                        }
                    }
                    //System.out.println("Value: " + value);
                    statValues.put(key, value);
                    htPercents.put(key, Math.round(
                            getPlusPlusPercent(mhtStats.get(key).range, value)));
                    //System.out.println(key + htPercents.get(key));
                }

                // Create a dwarf object
                Dwarf oDwarf = new Dwarf();

                oDwarf.name = getTagValue(thisCreature, "Name", "Error - Null Name");
                oDwarf.age = age;
                oDwarf.gender = getTagValue(thisCreature, "Sex", "Error - Null Sex");
                oDwarf.nickname = getTagValue(thisCreature, "Nickname", "");
                oDwarf.statPercents = htPercents;
                oDwarf.statValues = statValues;
                oDwarf.time = MAX_DWARF_TIME;
                oDwarf.jobText = getTagList(thisCreature, "Labours");
                String jobs[] = oDwarf.jobText.split("\n");
                //if (jobs.length <= 1)
                    //System.out.println("No labors enabled.");
                //else {    
                if (jobs.length > 1) {  // First and last entries in the labor list from XML are blank                
                    for (int jCount = 1; jCount < jobs.length - 1; jCount++) {
                        //System.out.println(oDwarf.name + ": labor " + jCount
                        //        + " enabled: " + jobs[jCount].trim());
                        oDwarf.labors.add(jobs[jCount].trim());
                    }
                }

                // Read current skill levels
                Element skills = (Element) thisCreature.getElementsByTagName("Skills").item(0);
                
                try {
                    NodeList children = skills.getElementsByTagName("Skill");
                    
                    for (int sCount = 0; sCount < children.getLength(); sCount++) {
                        Element eleSkill = (Element) children.item(sCount);
                        String strSkillName = getTagValue(eleSkill, "Name"
                                , "Error - Null skill name");
                        String strSkillLevel = getTagValue(eleSkill, "Level"
                                , "Error - Null skill level");
                        //System.out.println(oDwarf.name + " " + strSkillName + " "
                        //        + strSkillLevel);
                        
                        // If it is a DFHack dwarves.XML, the skill level will
                        // be just digits.
                        long skillValue = -1;
                        try {
                            skillValue = Long.parseLong(strSkillLevel);
                        } catch (NumberFormatException e) {
                            // Probably a Runesmith XML - convert the skill level
                            // to a numeric value if so
                            skillValue = skillDescToLevel(strSkillLevel);
                        }
                        oDwarf.skillLevels.put(strSkillName, skillValue);
                        
                        //oDwarf.skillLevels.put(strSkillName
                        //        , skillDescToLevel(strSkillLevel));
                    }
                    
                } catch (java.lang.NullPointerException e) {
                    System.err.println("Skills are not present in the given dwarves.xml file. "
                            + oDwarf.name + " will not have skill levels.");
                }
                                
                // Simple skill potentials
                for (String key : mhtSkills.keySet()) {
                    Skill oSkill = mhtSkills.get(key);
                    oDwarf.skillPotentials.put(oSkill.name
                            , getPotential(oDwarf, oSkill));
                }
                // Meta skill potentials
                for (String key : mhtMetaSkills.keySet()) {
                    MetaSkill meta = mhtMetaSkills.get(key);
                    double dblSum = 0.0d;

                    for (Skill oSkill : meta.vSkills)
                        dblSum += oDwarf.skillPotentials.get(oSkill.name);

                    oDwarf.skillPotentials.put(meta.name
                            , Math.round(dblSum / meta.vSkills.size()));
                }
                mvDwarves.add(oDwarf);
                
                /* testing
                Exclusion excl = new Exclusion("Juveniles", "age", "Less than", new Integer(13));
                if (excl.appliesTo(oDwarf))
                    System.out.println("'Juveniles' exclusion applies to " + oDwarf.name);
                 */
                
            //} // End if non-juvenile
        }
    }
    
    // Converts a Runesmith skill level description to long integer value
    private long skillDescToLevel(String skillLevelDesc) {
        
        Matcher matcher = mpatSkillLevel.matcher(skillLevelDesc);
        if (matcher.find())
            return Long.parseLong(matcher.group(2));
        else
            System.err.println("Pattern not matched.");
        
        return 0;
    }
    
    // Reads the trait hints file (needed for Runesmith style XML traits)
    private void readTraitHints() {
        String strLine;
        
        try {
            // Open the file
            //InputStream in =
            //        this.getClass().getResourceAsStream(TRAITS_FILE_NAME);
            FileInputStream in = new FileInputStream(TRAITS_FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            while ((strLine = br.readLine()) != null) {
                String[] data = strLine.split("\t");
                
                mhtStats.get(data[0]).addStatHint(data[3]
                        , Integer.parseInt(data[1]), Integer.parseInt(data[2]));
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error when reading file trait-hints.txt."
                    + " Dwarf traits may not have correct values.");
            e.printStackTrace();
        }
    }
    
    // Reads the labor skills XML data file
    // Commented code has been left intact so that I (hopefully) don't ever have
    // to research the problems I encountered here again
    private Hashtable<String, Skill> getLaborSkills() throws URISyntaxException
            , FileNotFoundException {
        
        Hashtable<String, Vector<Stat>> htStatGroup = new Hashtable<String
                , Vector<Stat>>();
        Hashtable<String, Skill> htReturn = new Hashtable<String, Skill>();
        Vector<Stat> vStat;
        Element thisStat;
        NodeList stat;
        
        //try {
            // The following two lines work perfectly when running from the development
            // environment in Netbeans, but they do not work in the distributed application.
            //URI oURI = new URI(
            //    this.getClass().getResource(LABOR_SKILLS_XML_FILE_NAME).getFile());  //getFile()
            //URI oURI = new URI(this.getClass().getClassLoader().getResource(
            //        LABOR_SKILLS_XML_FILE_NAME).getFile());
            //myXMLReader xmlFileReader = new myXMLReader(oURI.getPath());
            //System.out.println(getClass().getResource(LABOR_SKILLS_XML_FILE_NAME).toURI().toString());
        
            // UPDATE: The insanity trying to get this file to process is caused by
            // a bug-slash-feature in Java:
            // url.getFile() is full of '%20's standing in for spaces in path names.
            // See bug details and workaround at
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4466485
            // UPDATE: This was such a headache to deal with that myXMLReader was
            // modified to accept streams instead. Duhhhh
            //System.out.println(getClass().getResource(LABOR_SKILLS_XML_FILE_NAME));
            //URI uri = new URI(getClass().getResource(LABOR_SKILLS_XML_FILE_NAME).toString());
            //System.out.println(uri.getPath());
            //myXMLReader xmlFileReader = new myXMLReader(uri.getPath());
            
            //And what the heck was I doing using getResourceAsStream anyway...
            // I don't want this file zipped up in the JAR! FileInputStream for the win!
            //myXMLReader xmlFileReader = new myXMLReader(getClass()
            //        .getResourceAsStream(LABOR_SKILLS_XML_FILE_NAME));
            myXMLReader xmlFileReader = new myXMLReader(new FileInputStream(
                    LABOR_SKILLS_XML_FILE_NAME));
            
            // Read the stat group macros
            NodeList nlStatGroup = xmlFileReader.getDocument().getElementsByTagName(
                    "StatGroup");
            //Element ele = (Element) nlStatGroup.item(0);
            //System.out.println(nlStatGroup.getLength() + " stat groups");
            for (int iCount = 0; iCount < nlStatGroup.getLength(); iCount++) {
                vStat = new Vector<Stat>();

                Element thisStatGroup = (Element) nlStatGroup.item(iCount);
                String strStatGroupName = getTagValue(thisStatGroup, "Name"
                        , "Error - Null name in XML"); // thisStatGroup.getAttribute("Name");
                //System.out.println("Stat group name: " + strStatGroupName);
                stat = thisStatGroup.getElementsByTagName("Stat");
                for (int jCount = 0; jCount < stat.getLength(); jCount++) {
                    thisStat = (Element) stat.item(jCount);
                    String strStatName = thisStat.getAttribute("Name");
                    vStat.add(mhtStats.get(strStatName));
                }
                htStatGroup.put(strStatGroupName, vStat);
            }
            //printStatGroups(htStatGroup);

            // Read the skills (and secondary skills, and social skills) for labors
            Class[] classes = {Skill.class, SecondarySkill.class
                    , SocialSkill.class};
            for (Class classItem : classes) {
                NodeList nlLaborSkill = xmlFileReader.getDocument().getElementsByTagName(
                            classItem.getSimpleName());   // "Skill"
                for (int kCount = 0; kCount < nlLaborSkill.getLength(); kCount++) {
                    vStat = new Vector<Stat>();

                    Element thisLaborSkill = (Element) nlLaborSkill.item(kCount);
                    String strName = getTagValue(thisLaborSkill, "Name"
                            , "Error - Null labor skill name in XML");
                    //System.out.println(strName + " :");

                    // Stats and StatGroupRefs can be listed
                    // Add stats to stat list
                    stat = thisLaborSkill.getElementsByTagName("Stat");
                    //System.out.println(stat.getLength() + " stats");
                    for (int mCount = 0; mCount < stat.getLength(); mCount++) {
                        String strStatName = stat.item(mCount).getTextContent();
                        //System.out.println("    " + strStatName);   // thisStat.getNodeValue()
                        vStat.add(mhtStats.get(strStatName));   // thisStat.getNodeValue()
                    }

                    // Add StatGroupRefs to stat list
                    NodeList statGroup = thisLaborSkill.getElementsByTagName("StatGroupRef");
                    for (int nCount = 0; nCount < statGroup.getLength(); nCount++) {
                        Element thisStatGroup = (Element) statGroup.item(nCount);
                        //System.out.println(thisStatGroup.getTextContent());
                        //printStatGroup(htStatGroup.get(thisStatGroup.getTextContent()));
                        vStat.addAll(htStatGroup.get(thisStatGroup.getTextContent()));
                    }

                    // Prevented by trait, min, and max for social skills-------
                    String strTrait = "Error - No trait in XML";
                    int intMin = 0;
                    int intMax = 100;

                    if (classItem == SocialSkill.class) {
                        strTrait = thisLaborSkill
                                .getElementsByTagName("Trait").item(0).getTextContent();
                        intMin = Integer.parseInt( 
                                thisLaborSkill.getElementsByTagName("Min").item(0)
                                .getTextContent());
                        intMax = Integer.parseInt(
                                thisLaborSkill.getElementsByTagName("Max").item(0)
                                .getTextContent());
                    }
                    // ------------End special social skills processing---------
                    
                    // Add the skill to the hash table
                    if (classItem == Skill.class)
                        htReturn.put(strName, new Skill(strName, vStat));
                    else if (classItem == SecondarySkill.class)
                        htReturn.put(strName, new SecondarySkill(strName, vStat));
                    else if (classItem == SocialSkill.class)
                        htReturn.put(strName, new SocialSkill(strName, vStat
                                , strTrait, intMin, intMax));
                    else
                        System.err.println("classItem is not of a recognized type."
                                + " Ignoring skill " + strName);
                }
            }            
                
        //} catch (URISyntaxException e) { e.printStackTrace();
        //}
        
        return htReturn;
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
            System.out.println("    " + stat.name);
        }
    }
    
    // Reads labor-skills.XML
    private void createSkills() {
        createStats();
        
        // Read skills from XML.
        try {
            mhtSkills = getLaborSkills();
        } catch (URISyntaxException e) {
            System.err.println("URI syntax exception: could not read labor-skills.XML");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            System.err.println("Labor skills file not found");
            e.printStackTrace();
        }
        
        // TODO: Remove this hard-coding. Put meta skills in XML file.
        // Meta skills: Broker and manager
        mhtMetaSkills.put("Broker", new MetaSkill("Broker"
            , new Vector<Skill>(Arrays.asList(new Skill[] { mhtSkills.get("Appraisal")
            , mhtSkills.get("Judging Intent"), mhtSkills.get("Conversation")
            , mhtSkills.get("Comedy"), mhtSkills.get("Flattery")
            , mhtSkills.get("Lying")
            , mhtSkills.get("Intimidation"), mhtSkills.get("Persuasion")
            , mhtSkills.get("Negotiation"), mhtSkills.get("Consoling")
            , mhtSkills.get("Pacification") }))));
        mhtMetaSkills.put("Manager", new MetaSkill("Manager"
                , new Vector<Skill>(Arrays.asList(new Skill[] {
                mhtSkills.get("Organization")
                , mhtSkills.get("Consoling"), mhtSkills.get("Pacification") }))));
        
        //(For dumping hard-coded data to XML)
        //writeSkillsToXML();
        //System.out.println("mhtSkills contains " + mhtSkills.size() + " entries.");
        //System.out.println("laborSkills contains " + getLaborSkills().size() + " entries.");
        
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
        out.write("    <Name>" + skill.name + "</Name>");
        out.newLine();
        out.write("    <Stats>");
        out.newLine();

        for (Stat stat : skill.getStats()) {
            out.write("        <Stat>" + stat.name + "</Stat>");
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
    
    // Creates mhtStats.
    // TODO Remove hard-coding
    private void createStats() {
        String[] plusplusStats = { "Focus", "Spatial Sense" };
        String[] plusStats = { "Strength", "Toughness", "Analytical Ability"
            , "Creativity", "Patience", "Memory" };
        String[] avgStats = { "Endurance", "Disease Resistance", "Recuperation"
                , "Intuition", "Willpower", "Kinesthetic Sense", "Linguistic Ability"
                , "Musicality", "Empathy", "Social Awareness", "Altruism" };
        String[] minusStats = { "Agility" };
        
        createStats(plusplusStats, plusplusRange);
        createStats(plusStats, plusRange);
        createStats(avgStats, avgRange);
        createStats(minusStats, minusRange);
        createStats(masSocialTraits, socialRange);
        
        // Set XML names for stats that don't need to be hinted
        mhtStats.get("Focus").xmlName = "Focus";
        mhtStats.get("Spatial Sense").xmlName = "SpatialSense";
        mhtStats.get("Strength").xmlName = "Strength";
        mhtStats.get("Toughness").xmlName = "Toughness";
        mhtStats.get("Analytical Ability").xmlName = "AnalyticalAbility";
        mhtStats.get("Creativity").xmlName = "Creatvity";   // Yes, it's missing an "i".
        mhtStats.get("Patience").xmlName = "Patience";
        mhtStats.get("Memory").xmlName = "Memory";
        mhtStats.get("Endurance").xmlName = "Endurance";
        mhtStats.get("Disease Resistance").xmlName = "DiseaseResistance";
        mhtStats.get("Recuperation").xmlName = "Recuperation";
        mhtStats.get("Intuition").xmlName = "Intuition";
        mhtStats.get("Willpower").xmlName = "Willpower";
        mhtStats.get("Kinesthetic Sense").xmlName = "KinaestheticSense";
        mhtStats.get("Linguistic Ability").xmlName = "LinguisticAbility";
        mhtStats.get("Musicality").xmlName = "Musicality";
        mhtStats.get("Empathy").xmlName = "Empathy";
        mhtStats.get("Social Awareness").xmlName = "SocialAwareness";
        mhtStats.get("Agility").xmlName = "Agility";       
        
        // TODO ... Figure out why I wrote TODO here and what this is/was for
        //mhtStats.get("Friendliness").xmlName = 
        //mhtStats.get("Self-consciousness").xmlName = 
        //mhtStats.get("Straightforwardness").xmlName = 
        //mhtStats.get("Cooperation").xmlName = 
        //mhtStats.get("Assertiveness").xmlName = 
    }
    
    private void createStats(String[] statName, long[] statRange) {
        for (int iCount = 0; iCount < statName.length; iCount++)
            mhtStats.put(statName[iCount], new Stat(statName[iCount], statRange));
    }
    
    // Loads the big JTable o' dwarves (Dwarf List)
    private void setModelData(MySimpleTableModel oModel) {
        // , NodeList nodes
        
        int iCount = 0; // row count
        for (Dwarf oDwarf : mvDwarves) {
            
            // Default Include to false for juveniles
            oModel.setValueAt(! oDwarf.isJuvenile(), iCount, 0); // "Include"    true
            oModel.setValueAt(oDwarf.name, iCount, 1);   //getTagValue(thisCreature, "Name")
            oModel.setValueAt(oDwarf.nickname, iCount, 2); // getTagValue(thisCreature, "Nickname")
            //oModel.setValueAt("Unknown", iCount, 2);
            oModel.setValueAt(oDwarf.gender, iCount, 3); //getTagValue(thisCreature, "Sex")
            
            String strJuvenile = "";
            int age = oDwarf.age; // Integer.parseInt(getTagValue(thisCreature, "Age"));
            if (age < 1)
                strJuvenile = "Baby";
            else if (oDwarf.isJuvenile())
                strJuvenile = "Child";
            oModel.setValueAt(strJuvenile, iCount, 4);
            
            // Too bad these aren't in exported dwarves.XML...
            //oModel.setValueAt("Unknown", iCount, 5);  // Squad
            //oModel.setValueAt("Unknown", iCount, 6);  // Squad leader
            
            // Stats
            int columnIndex = 5;  // 7;
            for (Stat stat : mhtStats.values()) {
                oModel.setValueAt(oDwarf.statValues.get(stat.name)
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
            oModel.setValueAt(oDwarf.jobText //getTagList(thisCreature, "Labours")   // , "Labour"
                , iCount, columnIndex);
            
            iCount++;
        }
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
    
    // Calculates the dwarf's "potential" for the given skill
    private long getPotential(Dwarf oDwarf, Skill oSkill) {
        
        double dblSum = 0.0d;
        Vector<Stat> vStats = oSkill.getStats();
        double numStats = (double) vStats.size();
        
        for (int kCount = 0; kCount < numStats; kCount++) {
            double addValue = oDwarf.statPercents.get(vStats.get(kCount).name);

            // If the dwarf cannot gain skill because of a personality trait
            if (oSkill.getClass() == SocialSkill.class) {
                SocialSkill sSkill = (SocialSkill) oSkill;
                long noValue = oDwarf.statValues.get(sSkill.noStatName);
                if (noValue >= sSkill.noStatMin && noValue <= sSkill.noStatMax)
                    addValue = 0;
            }

            dblSum += addValue;
        }
        
        return Math.round(dblSum / numStats);
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
    
    private int getPlusPlusBracket(long[] range, long attribute) {
        long minValue = range[0];
        for (int iCount = 1; iCount < range.length; iCount++)
            if (attribute <= range[iCount])
                return iCount;
        return 0;
    }
    private String getPlusPlusDesc(String[] desc, int bracket) {
        return desc[bracket];
    }
    private double getPlusPlusPercent(long[] range, long attribute) {
        double chanceToBeInBracket = 1.0d / (range.length - 1);
        int bracket = getPlusPlusBracket(range, attribute);
        
        if (bracket > 0) {
            int numBracketsBelow = bracket - 1;
            double bracketSize = range[bracket] - range[bracket - 1];
            double inBracketPercent = (attribute - range[bracket - 1]) / bracketSize;
            
            return 100.0d * chanceToBeInBracket
                    * (inBracketPercent + numBracketsBelow);            
        }
        else    // Not in a bracket: better than 100% of dwarves
            return 100.0d;
        
    }
    
    private Element getElement(Element ele, String tagName) {
        return (Element) ele.getElementsByTagName(tagName);
    }
    
    // Returns the value for the XML tag for this creature,
    // or nullValue if the tag does not exist.
    private String getTagValue(Element creature, String tagName, String nullValue) {
        Element ele = (Element) creature.getElementsByTagName(tagName).item(0);
        if (null == ele)
            return nullValue;
        else if (null == ele.getChildNodes().item(0))
            return nullValue;
        else
            return ((Node) ele.getChildNodes().item(0)).getNodeValue().trim();
    }
    // Gets...a list of tags
    // Some non-working code is allowed to remain. This was such a headache.
    private String getTagList(Element creature, String tagName) {
        //, String tagNameChild
        //String strList = "";
        
        NodeList parent = creature.getElementsByTagName(tagName);
        
        if (null == parent.item(0))
            return "Error - Null tag list";
        else
            return parent.item(0).getTextContent();
/*        NodeList children = parent.item(0).getChildNodes();
        
        System.out.println("There are " + children.getLength() + " node children.");
        for (int iCount = 0; iCount < children.getLength(); iCount++) {

            if (null != children.item(iCount))
                strList = strList = ", "
                    + ((Node) children.item(iCount)).getNodeValue().trim();
        }
      
        return strList; */
    }
    
    // Gets the value of an XML tag in a keyed list. The Traits section of
    // dwarves.xml from DFHack is an example of the expected format.
    // parent   : The parent element (for example, the Traits element)
    // tagName  : The name of the list-formatted element (example: "Trait")
    // keyName  : The name of the element containing the key (example: "Name")
    // keyValue : The key to search for (example: "ASSERTIVENESS")
    // valueName: The name of the element for the value to retrieve (example: "value")
    // nullValue: The value to return if an error is encountered, or if the expected
    //            item is not found. (Example: "50")
    private String getXMLValueByKey(Element parent, String tagName, String keyName
            , String keyValue, String valueName, String nullValue) {
        
        try {
            NodeList children = parent.getElementsByTagName(tagName);

            //System.out.println("Number of children: " + children.getLength());
            for (int iCount = 0; iCount < children.getLength(); iCount++) {
                Element eleItem = (Element) children.item(iCount);
                //System.out.println("Key name: " + eleItem.getAttribute(keyName));
                if (eleItem.getAttribute(keyName).toUpperCase().equals(keyValue.toUpperCase()))
                    return eleItem.getAttribute(valueName);
            }

        } catch (java.lang.NullPointerException e) {
            System.err.println("Error encountered when retrieving XML value "
                    + keyValue + " by key.");
            return nullValue;
        }
        
        System.err.println(tagName + ":" + keyName + ":" + valueName
                + " was not found in dwarves.xml");
        return nullValue;
    }
}
