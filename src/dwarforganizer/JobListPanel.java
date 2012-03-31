/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
import myutils.MyFileUtils;
import myutils.MyHandyTable;
import myutils.MySimpleTableModel;
import myutils.MyTCRStripedHighlight;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobListPanel extends JPanel {
       
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
                if (isClassNumeric(table.getColumnClass(column))) {
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
        private boolean isClassNumeric(Class c) {
            return (c == Integer.class || c == Long.class || c == Float.class
                    || c == Double.class);
        }
    }
    
    // A JTable that uses SelectingEditors for column edits
    class SelectingTable extends JTable {
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
    
    private static final int MAX_DWARF_TIME = 100;
    
    private static final int DEFAULT_QTY = 1;
    private static final int DEFAULT_TIME = MAX_DWARF_TIME;      // 1.0d
    private static final double DEFAULT_WT = 1.0d;
    private static final int DEFAULT_SKILL_WT = 0;
    private static final String DEFAULT_REMINDER = "";
    
    private static final String DEFAULT_FILE_TEXT = "[Enter a file name]";
        
    private Vector<Labor> mvLabors; // Set in constructor
    private Vector<LaborGroup> mvLaborGroups; //Set in constructor    = new Vector<LaborGroup>();
    private Vector<Job> mvLaborSettings = new Vector<Job>();
    //private Vector<String> mvstrGroups = new Vector<String>();
    
    //private JTextField txtName;
    private SelectingTable moTable;
    private JLabel lblHours;
    
    private boolean mbLoading = true;
    
    private static final String CURRENT_JOB_SETTINGS_VERSION = "A";
    
    private JobBlacklist moBlacklist = new JobBlacklist();
    
    public JobListPanel(Vector<Labor> vLabors, Vector<LaborGroup> vLaborGroups
            , JobBlacklist blacklist)
            throws CouldntProcessFileException {
        
        mvLaborGroups = vLaborGroups;
        mvLabors = vLabors;
        moBlacklist = blacklist;
        
/*        // Read rule file
        try {
            readRuleFile();
        } catch (Exception ex) {
            System.err.println("Failed to process rules.txt. All results are invalid.");
            ex.printStackTrace();
            throw new CouldntProcessFileException();
        } */
        
        // Create labor settings
        for (Labor labor : vLabors) {
            mvLaborSettings.add(new Job(labor.name, labor.skillName
                    , DEFAULT_QTY //, 0
                    , DEFAULT_TIME, DEFAULT_WT, DEFAULT_SKILL_WT
                    , DEFAULT_REMINDER));
        }  //  getSkillNameForJob(labor.name)
        
        // Create job settings table
        Vector<Color> vBackgroundColors = new Vector<Color>();
        Vector vGroups = new Vector();
        for (LaborGroup laborGroup : mvLaborGroups) {
            //vBackgroundColors.add(getColor(laborGroup.color));
            vBackgroundColors.add(getColor(laborGroup.R, laborGroup.G, laborGroup.B));
            vGroups.add(laborGroup.name);
        }
        
        // Hours label
        lblHours = new JLabel("Number of job hours: X");
        JPanel panHours = new JPanel();
        panHours.setLayout(new BorderLayout());
        panHours.add(lblHours, BorderLayout.LINE_START);
        
        // Build UI
        String[] columns = { "Group", "Labor", "Qty", "Time", "Job Priority"    // "Time Weight"
                , "Current Skill Weight", "Reminder" };
        final MySimpleTableModel oModel = new MySimpleTableModel(columns
                , mvLaborSettings.size());
                
        // Add the edit listener
        oModel.addTableModelListener(new TableModelListener() {

            @Override       // asked me to add @Override when I compiled Java 6 binary...
            public void tableChanged(TableModelEvent e) {
                //System.out.println("Table changed.");
                if (! mbLoading) updateLaborSetting(e.getFirstRow());
            }

            private void updateLaborSetting(int firstRow) {
                
                //double dblNewTime = Double.parseDouble(oModel.getValueAt(firstRow, 3).toString());
                int intNewTime = Integer.parseInt(oModel.getValueAt(firstRow, 3).toString());
                int intNewQty = Integer.parseInt(oModel.getValueAt(firstRow, 2).toString());
                
                Job job = mvLaborSettings.get(firstRow);
                boolean bTimeChanged = (job.qtyDesired != intNewQty
                        || job.time != intNewTime);
                
                job.qtyDesired = intNewQty;
                job.time = intNewTime;
                job.candidateWeight = Double.parseDouble(oModel.getValueAt(firstRow, 4).toString());
                job.currentSkillWeight = Integer.parseInt(oModel.getValueAt(firstRow, 5).toString());
                job.reminder = oModel.getValueAt(firstRow, 6).toString();
                
                if (bTimeChanged) {
                    //updateHours();
                    new HoursDisplayUpdater().execute();
                }
            }
        });        

        // TODO: Darned hard coding
        // Quantity, time, weights, and reminder editable
        oModel.addEditableException(2);
        oModel.addEditableException(3);
        oModel.addEditableException(4);
        oModel.addEditableException(5);
        oModel.addEditableException(6);
        
        moTable = new SelectingTable(oModel);       // SelectingTable
        moTable.setRowSelectionAllowed(false);
        
        loadLaborSettings();
        
        moTable.setDefaultRenderer(Object.class, new MyTCRStripedHighlight(
                vBackgroundColors, vGroups, 0));
        
        JScrollPane oSP = new JScrollPane(moTable);
        MyHandyTable.handyTable(moTable, oSP, oModel, false, true);
        MyHandyTable.setPrefWidthToColWidth(moTable);
        
        // Create panel
        this.setLayout(new BorderLayout());
        //this.add(panFileInfo, BorderLayout.PAGE_START);
        this.add(oSP);
        this.add(panHours, BorderLayout.PAGE_END);
        
        // Load any saved settings
        load(DEFAULT_FILE_TEXT);    //txtName.getText()
        mbLoading = false;
        
        //this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        //this.pack();
        //this.setVisible(true);
    }
    
/*    protected JobBlacklist getBlacklist() { return moBlacklist; }
    protected void setBlacklist(JobBlacklist newBlacklist) {
        moBlacklist = newBlacklist;
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
        // TODO: My Documents is nice but multiplatform makes me incredibly nervous.
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
        // Show file name in window title
        //this.setTitle(file.getName().replace(".txt", "") + " Job Settings");
        
        try {
            FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            DataInputStream in = new DataInputStream(fstream);            

            Hashtable<String, Job> htJobs = hashJobs(MyFileUtils.readDelimitedLineByLine(
                    in, "\t", 0));

            // Update the current labor settings with the file data.
            for (Job job : mvLaborSettings) {
                Job jobFromFile = htJobs.get(job.name);
                if (jobFromFile != null) {
                    job.qtyDesired = jobFromFile.qtyDesired;
                    job.candidateWeight = jobFromFile.candidateWeight;
                    job.currentSkillWeight = jobFromFile.currentSkillWeight;
                    job.time = jobFromFile.time;
                    job.reminder = jobFromFile.reminder;
                }

                else
                    System.err.println("WARNING: Job '" + job.name + "' was not found"
                            + " in the file. Its settings will be the defaults.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load job file.");
            e.printStackTrace();
        }
        
        // Display the values in the table.
        loadLaborSettings();
        new HoursDisplayUpdater().execute();
        
    }
    
    // Loads the file with the given name
    private void load(String fileName) {
        
        // Defaults
        if (fileName.equals(DEFAULT_FILE_TEXT)) {
            //this.setTitle("Default Job Settings");
            
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
            if (labor.name.equals(laborName))
                return labor.groupName;
        
        return "";
    }
    
    // Loads the job data into a hash table
    private Hashtable<String, Job> hashJobs(Vector<String[]> vJobs) {
        Hashtable<String, Job> htReturn = new Hashtable<String, Job>();
        
        String strReminder;
        
        for (String[] jobData : vJobs) {
            if (jobData.length == 1) {
                // Version data
            }
            else {
                if (jobData.length < 6)
                    strReminder = DEFAULT_REMINDER;
                else
                    strReminder = jobData[5];

                htReturn.put(jobData[0], new Job(jobData[0], "Unknown"
                    , Integer.parseInt(jobData[1])
                    , Integer.parseInt(jobData[2]), Double.parseDouble(jobData[3])
                    , Integer.parseInt(jobData[4]), strReminder));
            }
        }
        return htReturn;
        
    }
}
