/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;
import myutils.MyHandyTable;
import myutils.MyNumeric;
import myutils.MySimpleTableModel;
import myutils.MyTCRStripedHighlight;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobListPanel extends JPanel {
    
    private static final int MAX_DWARF_TIME = 100;
    
    private static final int DEFAULT_QTY = 0;
    private static final int DEFAULT_TIME = MAX_DWARF_TIME;      // 1.0d
    private static final double DEFAULT_WT = 1.0d;
    private static final int DEFAULT_SKILL_WT = 50;
    private static final String DEFAULT_REMINDER = "";
    
    private static final String DEFAULT_FILE_TEXT = "[Enter a file name]";
    
    // DEFAULT SETTINGS shouldn't be used - it just exists as a read-only file
    // with the stock defaults
    private static final String DEFAULT_SETTINGS_FILE = "samples/jobs/DEFAULT SETTINGS";
    protected static final String MY_DEFAULT_SETTINGS_FILE = "samples/jobs/MY DEFAULT SETTINGS";
    
    // Column identifiers
    private static final String QTY_COL_IDENTIFIER = "Qty";
    private static final String TIME_COL_IDENTIFIER = "Time";
    private static final String JOB_PRIO_COL_IDENTIFIER = "Job Priority";
    private static final String CUR_SKILL_WT_COL_IDENTIFIER = "Current Skill Weight";
    private static final String REMINDER_COL_IDENTIFIER = "Reminder";
    
    private Vector<Labor> mvLabors; // Set in constructor
    private Vector<LaborGroup> mvLaborGroups; //Set in constructor    = new Vector<LaborGroup>();
    private Vector<Job> mvLaborSettings;
    //private Vector<String> mvstrGroups = new Vector<String>();
    
    //private JTextField txtName;
    private SelectingTable moTable;
    private JLabel lblHours;
    
    private boolean mbLoading = true;
    
    private static final String CURRENT_JOB_SETTINGS_VERSION = "A";
    
    private JobBlacklist moBlacklist = new JobBlacklist();    
    
    private DwarfOrganizerIO moIO;
    
    // A table cell editor that selects all text when we start to edit a cell:
    class SelectingEditor extends DefaultCellEditor {
        public SelectingEditor(JTextField textField) {  // JTextField textField
            super(textField);    // textField
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value
                , boolean isSelected, int row, int column) {
            
            Component c = super.getTableCellEditorComponent(table, value
                    , isSelected, row, column);
            if (c instanceof JTextComponent) {
                final JTextField jtf = (JTextField) c;
                if (MyNumeric.isNumericClass(table.getColumnClass(column))) {
                    jtf.setHorizontalAlignment(JTextField.RIGHT);
                }
                //TODO: Override isCellEditable so that we can detect an edit that
                // starts with a double-click, and highlight in that case as well
                jtf.selectAll();    
                
/*              Internet solution that doesn't work correctly:
                (first character typed is lost in most circumstances) :
                jtf.requestFocus();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jtf.selectAll();
                    }
                });         */
            }
            return c;
        }
    }
    
    // A JTable that uses SelectingEditors for column edits
    class SelectingTable extends CopyCutPastingTable {
        public SelectingTable(TableModel oModel) {
            super(oModel);
            TableColumnModel model = super.getColumnModel();
            for (int iCount = 0; iCount < super.getColumnCount(); iCount++) {
                TableColumn tc = model.getColumn(iCount);
                JTextField txt = new JTextField();
                txt.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                tc.setCellEditor(new SelectingEditor(txt));    // new JTextField()
            }
        }
    }
    
    private class CouldntProcessFileException extends Exception { 
        public CouldntProcessFileException() { super(); }
    };
    
    // Updates the job hours display
    private class HoursDisplayUpdater extends SwingWorker {
        
        @Override
        protected Object doInBackground() throws Exception {
            long lngHours = sumHours();
            long lngDwarves = getMinDwarvesNeeded();
            String text = "Number of job hours: "
                    + NumberFormat.getInstance().format(lngHours)
                    + " (~" + lngDwarves + " dwarves)";
            lblHours.setText(text);
            return 0;
        }        
    };
    
    // keysToIgnore: A vector of keystrokes to be ignored by the JTable editor
    //               (i.e. keystrokes bound to menu items such as control S)
    // The problem with control S etc. activating the JTable editing session
    // is an outstanding bug in Java documented at
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4820794
    public JobListPanel(Vector<Labor> vLabors, Vector<LaborGroup> vLaborGroups
            , JobBlacklist blacklist, DwarfOrganizerIO io)   // , Vector<KeyStroke> keysToIgnore
            throws CouldntProcessFileException {
        
        mvLaborGroups = vLaborGroups;
        mvLabors = vLabors;
        moBlacklist = blacklist;
        moIO = io;
        
        // Create labor settings
        mvLaborSettings = new Vector<Job>(vLabors.size());
        for (Labor labor : vLabors) {
            mvLaborSettings.add(new Job(labor.getName(), labor.getSkillName()
                    , DEFAULT_QTY //, 0
                    , DEFAULT_TIME, DEFAULT_WT, DEFAULT_SKILL_WT
                    , DEFAULT_REMINDER));
        }  //  getSkillNameForJob(labor.name)
        
        // Create job settings table
        Vector<Color> vBackgroundColors = new Vector<Color>(mvLaborGroups.size());
        Vector vGroups = new Vector(mvLaborGroups.size());
        for (LaborGroup laborGroup : mvLaborGroups) {
            //vBackgroundColors.add(getColor(laborGroup.color));
            vBackgroundColors.add(getColor(laborGroup.getRed()
                    , laborGroup.getGreen(), laborGroup.getBlue()));
            vGroups.add(laborGroup.getName());
        }
        
        // Hours label
        lblHours = new JLabel("Number of job hours: X");
        JPanel panHours = new JPanel();
        panHours.setLayout(new BorderLayout());
        panHours.add(lblHours, BorderLayout.LINE_START);
        
        // Build UI
        Object[] columns = { "Group", "Labor", QTY_COL_IDENTIFIER
                , TIME_COL_IDENTIFIER, JOB_PRIO_COL_IDENTIFIER    // "Time Weight"
                , CUR_SKILL_WT_COL_IDENTIFIER, REMINDER_COL_IDENTIFIER };
        Class[] columnClass = { String.class, String.class, Integer.class, Integer.class
                , Double.class, Integer.class, String.class };  // No primitives allowed here in Java 6!!
        final MySimpleTableModel oModel = new MySimpleTableModel(columns
                , mvLaborSettings.size(), columnClass);
        
        // Add the edit listener
        oModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent e) {
                //System.out.println("Table changed.");
                if (! mbLoading) updateLaborSetting(e.getFirstRow());
            }

            private void updateLaborSetting(int firstRow) {
                //TODO: In catch blocks, also set the table value to reflect the default value
                // (Will this be circular?)
                
                //double dblNewTime = Double.parseDouble(oModel.getValueAt(firstRow, 3).toString());
                int intNewTime;
                int intNewQty;
                try {
                    intNewTime = Integer.parseInt(oModel.getValueAt(firstRow, 3).toString());
                } catch (NumberFormatException e) {
                    intNewTime = DEFAULT_TIME;
                }
                try {
                    intNewQty = Integer.parseInt(oModel.getValueAt(firstRow, 2).toString());
                } catch (NumberFormatException e) {
                    intNewQty = DEFAULT_QTY;
                }
                
                Job job = mvLaborSettings.get(firstRow);
                boolean bTimeChanged = (job.qtyDesired != intNewQty
                        || job.time != intNewTime);
                
                job.qtyDesired = intNewQty;
                job.time = intNewTime;
                try {
                    job.candidateWeight = Double.parseDouble(oModel.getValueAt(firstRow, 4).toString());
                } catch (NumberFormatException e) {
                    job.candidateWeight = DEFAULT_WT;
                }
                try {
                    job.currentSkillWeight = Integer.parseInt(oModel.getValueAt(firstRow, 5).toString());
                } catch (NumberFormatException e) {
                    job.currentSkillWeight = DEFAULT_SKILL_WT;
                }
                job.reminder = oModel.getValueAt(firstRow, 6).toString();
                
                if (bTimeChanged) {
                    //updateHours();
                    new HoursDisplayUpdater().execute();
                }
            }
        });        

        // Quantity, time, weights, and reminder editable
        oModel.addEditableException(QTY_COL_IDENTIFIER); // 2
        oModel.addEditableException(TIME_COL_IDENTIFIER); // 3
        oModel.addEditableException(JOB_PRIO_COL_IDENTIFIER); // 4
        oModel.addEditableException(CUR_SKILL_WT_COL_IDENTIFIER); // 5
        oModel.addEditableException(REMINDER_COL_IDENTIFIER); // 6
        
        moTable = new SelectingTable(oModel);
        moTable.setTransferHandler(new MyTableTransferHandler());   // Allows single-cell cut copy paste
        moTable.setComponentPopupMenu(createEditMenuPopup());
        moTable.setRowSelectionAllowed(false);
        
        loadLaborSettings();
        
        moTable.setDefaultRenderer(Object.class, new MyTCRStripedHighlight(
                vBackgroundColors, vGroups, 0));
        
        // This didn't work: see solution in MyJTable
        // JTable must ignore all menu accelerators associated with the job list
        // Otherwise it will start an editing session when Control+S or whatever is pressed
        //for (KeyStroke keyStroke : keysToIgnore) {
        /*for (MainWindow.JobListMenuAccelerator accel
                : MainWindow.JobListMenuAccelerator.values()) {
                
                //System.out.println(accel.getKeyStroke().toString());
                alwaysIgnoreKeyStroke(moTable, accel.getKeyStroke());
        } */
        
        JScrollPane oSP = new JScrollPane(moTable);
        MyHandyTable.handyTable(moTable, oSP, oModel, false, true);
        MyHandyTable.setPrefWidthToColWidth(moTable);
        
        // Create panel
        this.setLayout(new BorderLayout());
        //this.add(panFileInfo, BorderLayout.PAGE_START);
        this.add(oSP);
        this.add(panHours, BorderLayout.PAGE_END);
        
        // Load any saved settings
        //load(DEFAULT_FILE_TEXT);    //txtName.getText()
        mbLoading = false;
        load(new File(MY_DEFAULT_SETTINGS_FILE)); // Takes care of mbLoading itself
        
        
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //this.pack();
        //this.setVisible(true);
    }
    
    private JPopupMenu createEditMenuPopup() {
        
        JPopupMenu popUp = new JPopupMenu();
        //JMenu menu = new JMenu("Edit");
        
        moTable.createEditMenuItems(popUp);
        //popUp.add(menu);
        
        return popUp;
    }
    protected JComponent createEditMenu(JComponent menu) {
        moTable.createEditMenuItems(menu);
        return menu;
    }
    
/*    protected JobBlacklist getBlacklist() { return moBlacklist; }
    protected void setBlacklist(JobBlacklist newBlacklist) {
        moBlacklist = newBlacklist;
    } */
    
    // Doesn't really work. Left it here so I will remember that.
    // Causes the JComponent to always ignore the given keystroke.
/*    private void alwaysIgnoreKeyStroke(JComponent component, KeyStroke keyStroke) {
        
//        // Commented code causes the keystroke to be ignored by the entire
//        // application...sigh
//        // From Java SE Key Bindings tutorial
//        Action doNothing = new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                // Do nothing!
//            }
//        };
//        component.getInputMap().put(keyStroke, "doNothing");
//        component.getActionMap().put("doNothing", doNothing);
        
        // Just causes the component to ignore the keystroke, but doesn't seem to
        // work completely correctly with JTable
        component.getInputMap().put(keyStroke, "none");
        
    } */
    
    private Vector<JobOpening> getJobOpenings() {
        Vector<JobOpening> vReturn = new Vector<JobOpening>();
        
        for (Job job : mvLaborSettings) {
            for (int iCount = 0; iCount < job.qtyDesired; iCount++)
                vReturn.add((JobOpening) job);
        }
        
        return vReturn;
    }    
    
    private long sumHours() {
        long lngReturn = 0l;        // It's an L, not a one
        for (Job job : mvLaborSettings)
            lngReturn += job.qtyDesired * job.time;
        return lngReturn;
    }
    
    // Updates internal blacklist and updates display
    protected void setBlacklist(JobBlacklist newData) {
        moBlacklist = newData;
        new HoursDisplayUpdater().execute();
    }
    
    // Uses a bin packing algorithm to determine the number of dwarves.
    // This value is not simply the number of job hours divided by the number
    // of hours available per dwarf. The job blacklist/whitelist and the
    // fact that the user may have chosen numbers of hours that don't fit
    // perfectly together may both influence the number of dwarves needed.
    private long getMinDwarvesNeeded() {
        BinPack<JobOpening> binPacker = new BinPack<JobOpening>();
        Vector<Vector<JobOpening>> vPackedBins = binPacker.binPack(
                getJobOpenings()
                , MAX_DWARF_TIME, moBlacklist);
        return vPackedBins.size();        
    }
    
    protected Vector<Job> getJobs() {
        return mvLaborSettings;
    }
    
    private void loadLaborSettings() {
        mbLoading = true;
        
        TableModel oModel = moTable.getModel();
        int row = 0;
        for (Job job : mvLaborSettings) {
            oModel.setValueAt(getGroupForLabor(job.name), row, 0);
            oModel.setValueAt(job.name, row, 1);
            oModel.setValueAt(job.qtyDesired, row, 2);
            //if (laborSetting.time != DEFAULT_TIME) System.out.println("Setting time to " + laborSetting.time);
            oModel.setValueAt(job.time, row, 3);
            oModel.setValueAt(job.candidateWeight, row, 4);
            oModel.setValueAt(job.currentSkillWeight, row, 5);
            oModel.setValueAt(job.reminder, row, 6);
            row++;
        }
        mbLoading = false;
    }
    
    public File getDirectory() {
        // TODO: My Documents is nice but multiplatform makes me nervous.
        // Samples is not the right place either but using it for now...
        
        //String strDir = System.getProperty("user.home") + "/My Documents/";
        //return new File(strDir, "/DwarfOrganizer/");
        return new File("samples/jobs/");
    }
    private File getFile(File directory, String fileName) {
        return new File(directory, fileName + ".txt");
    }
    
    // Saves job settings to file
    public void save(File file) {
        MyHandyTable.stopEditing(moTable); // Accept partial edits
        
        File dir = getDirectory();
        FileWriter fstream;

        try {
            // Open the output file.

            System.out.println("Writing to file " + file.getAbsolutePath());
            dir.mkdirs();           // Create the directory if it does not exist.
            file.createNewFile();   // Create the file if it does not exist.

            fstream = new FileWriter(file.getAbsolutePath());
            BufferedWriter out = new BufferedWriter(fstream);

            out.write(CURRENT_JOB_SETTINGS_VERSION);
            out.newLine();
            for (Job job : mvLaborSettings) {
                out.write(job.name
                        + "\t" + job.qtyDesired
                        + "\t" + job.time
                        + "\t" + job.candidateWeight
                        + "\t" + job.currentSkillWeight
                        + "\t" + job.reminder);
                out.newLine();
                out.flush();
            }
            out.close();

        } catch (IOException ex) {
            //Logger.getLogger(JobListWindow.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println(ex.toString());
        }
        
    }
    
    // Loads job settings from file
    public void load(File file) {
        MyHandyTable.cancelEditing(moTable);    // Cancel partial edits
        
        mbLoading = true;
        
        moIO.readJobSettings(file, mvLaborSettings, DEFAULT_REMINDER);
        
        // Display the values in the table.
        loadLaborSettings();
        new HoursDisplayUpdater().execute();
        
        mbLoading = false;
    }
    
    // Loads the file with the given name
    private void load(String fileName) {
        
        // Defaults
        if (fileName.equals(DEFAULT_FILE_TEXT)) {
            
            // Update the current labor settings with the defaults.
            for (Job job : mvLaborSettings) {
                job.qtyDesired = DEFAULT_QTY;
                job.candidateWeight = DEFAULT_WT;
                job.currentSkillWeight = DEFAULT_SKILL_WT;
                job.time = DEFAULT_TIME;
                job.reminder = DEFAULT_REMINDER;
            }
        }
        
        // Read from file
        else {
            File file = getFile(getDirectory(), fileName);
            load(file);
        }
    }
    
    // Print labor settings, for debugging
    private void printLaborSettings() {
        for (Job job : mvLaborSettings) {
            System.out.println(job.name + " " + job.qtyDesired + " " + job.time + " "
                    + job.candidateWeight + " " + job.currentSkillWeight + " "
                    + job.reminder);
        }
    }
    
    private Color getColor(String colorName) {
        
        if (colorName.equals("Red"))
            return Color.RED;
        else if (colorName.equals("Green"))
            return Color.GREEN;
        else if (colorName.equals("Purple"))
            return Color.magenta;
        else if (colorName.equals("Gray"))
            return Color.GRAY;
        else if (colorName.equals("Yellow"))
            return Color.YELLOW;
        else if (colorName.equals("White"))
            return Color.WHITE;
        else if (colorName.equals("Brown"))
            return Color.ORANGE;
        else if (colorName.equals("Blue"))
            return Color.BLUE;
        else
            return Color.WHITE;
    }
    // Lightens and returns the given color
    // Crappy unused code remains so I don't decide to try it again
    private Color getColor(int R, int G, int B) {
        //Color clrReturn = new Color(R, G, B);
        /*
        float[] hsb = Color.RGBtoHSB(R, G, B, null);
        float hue = hsb[0];
        float saturation = hsb[1];
        float brightness = hsb[2];
        int rgb = Color.HSBtoRGB(hue, saturation, 0.999f);
        
        return new Color(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF);
         */
        
        // Make a 16-color gradient with white, and choose the color just past midway
        final int MAX_COLOR = 255;
        return new Color(Math.min(MAX_COLOR, 9 * (255 + R) / 16)
                    , Math.min(MAX_COLOR, 9 * (255 + G) / 16)
                    , Math.min(MAX_COLOR, 9 * (255 + B) / 16));
        
    }
    
    // Returns the labor group name for the given labor name
    private String getGroupForLabor(String laborName) {
        
        for (Labor labor : mvLabors)
            if (labor.getName().equals(laborName))
                return labor.getGroupName();
        
        return "";
    }    
}
