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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public class DwarfListWindow extends JPanel {
    
    private static final int INCLUDE_COLUMN = 0;  // Index of the "Include" column in the table
        
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
    //private static Pattern mpatSkillLevel = Pattern.compile("(.*\\[)(\\d+)(\\].*)");
    
    private Vector<Labor> mvLabors; // Set in constructor
        
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
    
    // TODO: Normalize this constructor
    public DwarfListWindow(Vector<Dwarf> vDwarves, Vector<Labor> vLabors) {
            //, Vector<LaborGroup> vLaborGroups) { // NodeList nodes
        
        super();
        
        // Set local variables
        mvLabors = vLabors;
        mvDwarves = vDwarves;
        
        mbLoading = true;
        
        //createMenu();
                
        // Read dwarves from the node list
        //if (nodes != null)
        //    getDwarves(nodes);
        
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
        
        JPanel panDwarfList = new JPanel();
        panDwarfList.setLayout(new BorderLayout());
        panDwarfList.add(tablePanel, BorderLayout.CENTER);
        panDwarfList.add(panInfo, BorderLayout.SOUTH);
        
        this.setLayout(new BorderLayout());
        this.add(panDwarfList); // , BorderLayout.LINE_START
        
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
    
/*    //TODO: No longer necessary - get most recent copy of Dwarves from IO class
    public Vector<Dwarf> getDwarves() {
        return mvDwarves;
    } */
    
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
}
