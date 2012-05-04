/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.TableRowSorter;
import myutils.MyHTMLUtils;
import myutils.MyHandyTable;
import myutils.MyHandyWindow;
import myutils.MySimpleTableModel;
import myutils.MyTCRStripedHighlight;

/**
 *
 * GUI for displaying optimizer results
 * 
 * @author Tamara Orr
 * See MIT license in license.txt
 * 
 */
public class ResultsView implements ActionListener {
    
    private static final int LABOR_COLUMN = 3;      // Labors column index in the table
    private static final int REMINDER_COLUMN = 4;   // Reminder column index in the table

    private static final int MULTILINE_GAP = 2;    
    
    private static final String COLOR_ADD = "339933"; //"Green";
    private static final String COLOR_REMOVE = "Red";
    private static final String COLOR_STAY_SAME = "Black";    
    
    private static final String ACTION_CMD_ALL = "ALL";
    private static final String ACTION_CMD_NOBLE = "NOBLE";
    private static final String ACTION_CMD_REMINDER = "REMINDER";    
        
    // Table and filters
    private JTable moTable;
    private RowFilter mrfNoble;
    private RowFilter mrfAll;
    private RowFilter mrfHasReminder;    
    
    // Local variables set via constructor
    private Vector<Dwarf> mvDwarves;
    private boolean[][] mbSolution;
    private Vector<Job> mvJobs;
    private double[] mdblScores;
    
    // mbShowJobs index matches up with ChangeType
    private enum ChangeType {
        ADD(0)
        , REMOVE(1)
        , REMOVE_ALWAYS_SHOW(2)
        , STAY_SAME(3);
        
        private final int index;
        private ChangeType(int index) { this.index = index; }
        public int getIndex() { return index; }
        
    };
    // Default values for showing jobs. Indices match with ChangeTypes
    private boolean[] mbShowJobs = new boolean[] { true, true, true, true } ;
    //private static final boolean DEFAULT_SHOW_REMOVE_JOBS = true;
    //private boolean mbShowRemoveJobs = DEFAULT_SHOW_REMOVE_JOBS;

    private static final boolean DEFAULT_NICKNAME_VIS = false;
    
    private class DisplayableChange {
        
        private String text;
        private ChangeType changeType;
        
        public DisplayableChange(String text, ChangeType changeType) {
            this.text = text;
            this.changeType = changeType;
        }
        
        @Override
        public String toString() {
            if (this.changeType == ChangeType.STAY_SAME
                    && mbShowJobs[ChangeType.STAY_SAME.getIndex()]) {
                return getAddRemoveText("", this.text, true, "=", COLOR_STAY_SAME);
            }
            else if (this.changeType == ChangeType.ADD
                    && mbShowJobs[ChangeType.ADD.getIndex()]) {
                return getAddRemoveText("", this.text, true, "+", COLOR_ADD);
            }
            else if ((this.changeType == ChangeType.REMOVE_ALWAYS_SHOW
                    && mbShowJobs[ChangeType.REMOVE_ALWAYS_SHOW.getIndex()])
                    || ((this.changeType == ChangeType.REMOVE)
                    && mbShowJobs[ChangeType.REMOVE.getIndex()])) { //  mbShowRemoveJobs
             
                boolean bolden = (this.changeType == ChangeType.REMOVE_ALWAYS_SHOW);
                return getAddRemoveText("", this.text, bolden, "-", COLOR_REMOVE);
            }
            // Else if not shown
            else if (this.changeType == ChangeType.REMOVE
                    || this.changeType == ChangeType.ADD
                    || this.changeType == ChangeType.REMOVE_ALWAYS_SHOW
                    || this.changeType == ChangeType.STAY_SAME) {
                    return "";
            }
            else
                return "Undefined DisplayableChange";
        }
    }
    
    private class DisplayableChanges extends Vector<DisplayableChange> {
        
        @Override
        public String toString() {
            
            String strReturn = "";
            for (DisplayableChange displayableChange : this) {
                if (! strReturn.equals("") && ! displayableChange.toString().equals(""))
                    strReturn += MyHTMLUtils.LINE_BREAK;
                strReturn += displayableChange.toString();
            }
                        
            return MyHTMLUtils.toHTML(strReturn);
        }
    }    
    
    public ResultsView(JobOptimizer.Solution solution) {
        
        mvDwarves = solution.dwarves;
        mbSolution = solution.dwarfjobmap;
        mvJobs = solution.jobs;
        mdblScores = solution.dwarfscores;
        
        // Create the table filters
        ArrayList<RowFilter<Object, Object>> filters
                = new ArrayList<RowFilter<Object, Object>>();
        filters.add(RowFilter.regexFilter(".*Manager.*", LABOR_COLUMN));
        filters.add(RowFilter.regexFilter(".*Chief Medical Dwarf.*", LABOR_COLUMN));
        filters.add(RowFilter.regexFilter(".*Broker.*", LABOR_COLUMN));
        filters.add(RowFilter.regexFilter(".*Bookkeeper.*", LABOR_COLUMN));
        mrfNoble = RowFilter.orFilter(filters);
        
        filters = new ArrayList<RowFilter<Object, Object>>();
        filters.add(RowFilter.regexFilter(".*\\-.*", REMINDER_COLUMN)); // Reminder cell contains a "-"
        filters.add(RowFilter.regexFilter(".*\\+.*", REMINDER_COLUMN)); // Reminder cell contains a "+"
        mrfHasReminder = RowFilter.orFilter(filters);
        mrfAll = null;
        
        // Create table, scroll pane, and sorter.
        MySimpleTableModel oModel = createResultsModel();
/*        for (int iCount = 0; iCount < oModel.getColumnCount(); iCount++)
            System.out.println("Column " + iCount + " is of class "
                + oModel.getColumnClass(iCount).getName()); */
        moTable = new JTable(oModel, new HideableTableColumnModel());   // oModel
        moTable.createDefaultColumnsFromModel(); // Necessary when using HideableTableColumnModel
        
        // Renderers
        MyTCRStripedHighlight normalRenderer = new MyTCRStripedHighlight(1);
        moTable.setDefaultRenderer(Object.class, normalRenderer);
        
        // Top-align the multi-line columns
        class MyTopAlignedRenderer extends MyTCRStripedHighlight {
            MyTopAlignedRenderer(int i) {
                super(i);
            }
            
            @Override
            public Component getTableCellRendererComponent(JTable table
                    , Object value, boolean isSelected, boolean hasFocus, int row
                    , int column) {
                JLabel renderedLabel = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                renderedLabel.setVerticalAlignment(SwingConstants.TOP);
                return renderedLabel;
            }
        }
        
        MyTopAlignedRenderer topAlignedRenderer = new MyTopAlignedRenderer(1);
        moTable.getColumn("Job").setCellRenderer(topAlignedRenderer);
        moTable.getColumn("Reminder").setCellRenderer(topAlignedRenderer); 
        
        // Hide nickname if necessary
        setNicknameVisible(DEFAULT_NICKNAME_VIS);
        
        JScrollPane oSP = new JScrollPane(moTable);
        MyHandyTable.handyTable(moTable, oModel, true, true);
        MyHandyTable.adjustMultiLineRowHeight(moTable, MULTILINE_GAP);
        MyHandyTable.setPrefWidthToColWidth(moTable);
        
        //oSP.setPreferredSize(oTable.getPreferredScrollableViewportSize());   
        
        // Create view filter buttons
        JRadioButton btnViewAll = new JRadioButton("View All");
        btnViewAll.setSelected(true);
        btnViewAll.setActionCommand(ACTION_CMD_ALL);
        btnViewAll.addActionListener(this);
        
        JRadioButton btnViewNobles = new JRadioButton("Nobles Only");
        btnViewNobles.setActionCommand(ACTION_CMD_NOBLE);
        btnViewNobles.addActionListener(this);
        
        JRadioButton btnViewReminders = new JRadioButton("Has Reminder");
        btnViewReminders.setActionCommand(ACTION_CMD_REMINDER);
        btnViewReminders.addActionListener(this);
        
        ButtonGroup optView = new ButtonGroup();
        optView.add(btnViewAll);
        optView.add(btnViewNobles);
        optView.add(btnViewReminders);

        // Create "show jobs to remove" filter checkbox
/*        JCheckBox chkJobsToRemove = new JCheckBox("Show jobs to remove"
                , mbShowJobs[ChangeType.REMOVE.getIndex()]);  //  mbShowRemoveJobs
        chkJobsToRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mbShowRemoveJobs = ! mbShowRemoveJobs;
                updateShowRemoveJobs();
            }
        }); */
        
        JPanel panFilter = new JPanel();
        panFilter.setLayout(new FlowLayout());
        //panFilter.add(chkJobsToRemove);
        panFilter.add(btnViewAll);
        panFilter.add(btnViewNobles);
        panFilter.add(btnViewReminders);
                
        // Put the UI together
        JPanel panAll = new JPanel();
        panAll.setLayout(new BorderLayout());
        panAll.add(oSP);
        panAll.add(panFilter, BorderLayout.PAGE_END);

        // Create and show a window containing the table.
        JFrame frList = MyHandyWindow.createSimpleWindow("Optimized Jobs"
                , panAll, new BorderLayout());
        frList.setJMenuBar(createMenu());
        frList.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frList.setVisible(true);
        
    }
    
    private void setNicknameVisible(boolean visible) {
        HideableTableColumnModel hideableModel = (HideableTableColumnModel) 
                moTable.getColumnModel();
        hideableModel.setColumnVisible("Nickname", visible);
    }
    
    private JMenuBar createMenu() {
        
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("Display");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);

        // ----- Dwarf name format ---
        final JCheckBoxMenuItem checkMenuItem = new JCheckBoxMenuItem("Nicknames", false);
        checkMenuItem.setMnemonic(KeyEvent.VK_N);
        checkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNicknameVisible(checkMenuItem.isSelected());
            }
        });
        menu.add(checkMenuItem);
        
        // ----- Separator -----------
        menu.add(new JSeparator());
        
        // ----- Jobs to display -----
        menu.add(createJobMenuItem(MyHTMLUtils.toHTML("Jobs to "
                + MyHTMLUtils.makeColored("-Remove", COLOR_REMOVE))
                , ChangeType.REMOVE, KeyEvent.VK_R));
        
        menu.add(createJobMenuItem(MyHTMLUtils.toHTML("Jobs to "
                + MyHTMLUtils.makeBold("=Keep")), ChangeType.STAY_SAME
                , KeyEvent.VK_K));
        
        menu.add(createJobMenuItem(MyHTMLUtils.toHTML("Jobs to "
                + MyHTMLUtils.makeBold(MyHTMLUtils.makeColored("+Add", COLOR_ADD)))
                , ChangeType.ADD, KeyEvent.VK_A));

        return menuBar;
    }
    
    // Menu creation macro used by createMenu()
    private JCheckBoxMenuItem createJobMenuItem(String title
            , final ChangeType changeType, int keyEvent) {
        
        final JCheckBoxMenuItem checkMenuItem = new JCheckBoxMenuItem(
                title, mbShowJobs[changeType.getIndex()]);
        checkMenuItem.setMnemonic(keyEvent);
        checkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mbShowJobs[changeType.getIndex()] = checkMenuItem.isSelected();
                updateShowRemoveJobs();
            }
        });
        
        return checkMenuItem;
    }
    
    private MySimpleTableModel createResultsModel() { // throws SolutionImpossibleException 

        //Vector<String> columns = new Vector<String>();
        Vector<String> vColumns = new Vector<String>(Arrays.asList(new String[] {
            "Dwarf", "Nickname", "Scheduled", "Job", "Reminder", "Score"
        }));        
        String strFeed = "Feed Patients/Prisoners";
        String strRecover = "Recovering Wounded";
        
        // Find the maximum number of jobs held by a dwarf.
        int intMaxJobs = 0;
        for (int dCount = 0; dCount < mvDwarves.size(); dCount++) {
            int intThisDwarfJobs = 0;
            for (int jCount = 0; jCount < mvJobs.size(); jCount++)    // NUM_JOBS
                if (mbSolution[jCount][dCount])
                    intThisDwarfJobs++;
            if (intThisDwarfJobs > intMaxJobs)
                intMaxJobs = intThisDwarfJobs;
        }
        
        // Create table columns and model.
        MySimpleTableModel oModel = new MySimpleTableModel(vColumns
                , mvDwarves.size());
        
        // Fill in a row for each dwarf.
        for (int row = 0; row < mvDwarves.size(); row++) {
            oModel.setValueAt(mvDwarves.get(row).getName(), row, 0);
            oModel.setValueAt(mvDwarves.get(row).getNickname(), row, 1);
            oModel.setValueAt( //NumberFormat.getInstance().format(
                    JobOptimizer.MAX_TIME - mvDwarves.get(row).getTime(), row, 2);   // getPercentInstance()
            oModel.setValueAt( //NumberFormat.getNumberInstance().format(
                    mdblScores[row], row, 5);  // getSkillSum(row)
            
            int jobCount = 0;
            Dwarf thisDwarf = mvDwarves.get(row);
            String strReminderText = "";
            DisplayableChanges vChanges = new DisplayableChanges();
            
            for (int job = 0; job < mvJobs.size(); job++) {   // NUM_JOBS
                Job thisJob = mvJobs.get(job);
                boolean bHasReminder = ! thisJob.reminder.equals("");
                
                if (mbSolution[job][row]) {

                    // Display any change from the current labors
                    if (thisDwarf.labors.contains(thisJob.name)) {

                        vChanges.add(new DisplayableChange(getJobAndPotentialText(
                                thisJob.name
                                , thisDwarf.balancedPotentials.get(thisJob.name))
                                , ChangeType.STAY_SAME));
                    }
                    else {

                        vChanges.add(new DisplayableChange(getJobAndPotentialText(
                                thisJob.name
                                , thisDwarf.balancedPotentials.get(thisJob.name))
                                , ChangeType.ADD));                        
                        
                        
                        // Add reminder text if any is needed.
                        if (bHasReminder) {
                            strReminderText = addLineBreakIfNonEmpty(strReminderText);
                            strReminderText = getAddText(strReminderText
                                    , thisJob.reminder + " (" + thisJob.name + ")");
                        }
                    }
                    jobCount++;
                }
                
                // Check for printing any labors to remove
                else if (thisDwarf.labors.contains(thisJob.name)) {
                    //intChangeCount++;

                    vChanges.add(new DisplayableChange(getJobAndPotentialText(
                                thisJob.name
                                , thisDwarf.balancedPotentials.get(thisJob.name))
                                , ChangeType.REMOVE));
                    
                    // Add reminder text if any is needed.
                    if (bHasReminder) {
                        strReminderText = addLineBreakIfNonEmpty(strReminderText);
                        strReminderText = getRemoveText(strReminderText
                            , thisJob.reminder + " (" + thisJob.name + ")");
                    }
                }
            }
            
            // If Recover Wounded or Feed Patients/Prisoners is enabled and
            // Altruism is low enough to give a bad thought, add these to the list.
            if (thisDwarf.statValues.get("Altruism") != null) {
                boolean lowAltruism = (thisDwarf.statValues.get("Altruism") <= 39);

                if (thisDwarf.labors.contains(strFeed) && lowAltruism) {
                    vChanges.add(new DisplayableChange(strFeed
                                , ChangeType.REMOVE_ALWAYS_SHOW));                    
                }
                
                if (thisDwarf.labors.contains(strRecover) && lowAltruism) {
                    vChanges.add(new DisplayableChange(strRecover
                                , ChangeType.REMOVE_ALWAYS_SHOW));                    
                }
            }
            
            oModel.setValueAt(vChanges, row, LABOR_COLUMN);
            oModel.setValueAt(MyHTMLUtils.toHTML(strReminderText), row
                    , REMINDER_COLUMN);
        }
        return oModel;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        final RowFilter rf;
        if (e.getActionCommand().equals(ACTION_CMD_NOBLE))
            rf = mrfNoble;
        else if (e.getActionCommand().equals(ACTION_CMD_REMINDER))
            rf = mrfHasReminder;
        else
            rf = mrfAll;
        
        // Do this lengthy processing on a background thread, maintaining
        // some semblance of UI responsiveness.
        final SwingWorker worker = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                // Set the current table filter
                return setCurrentTableFilter(moTable, rf);
            }
            
            // Adjust the multiline row height when done
            @Override
            protected void done() {
                try { 
                    MyHandyTable.adjustMultiLineRowHeight(moTable, MULTILINE_GAP);
                } catch (Exception ignore) {
                }
            }
        };
        worker.execute();
    }
    // Adjust multiline row height when this setting is changed
    private void updateShowRemoveJobs() {
        MyHandyTable.adjustMultiLineRowHeight(moTable, MULTILINE_GAP);
    }
    // Sets the current table filter and adjusts row height as necessary.
    private int setCurrentTableFilter(JTable table, RowFilter rf) {
        TableRowSorter sorter = (TableRowSorter) table.getRowSorter(); //new TableRowSorter(oModel);  
        sorter.setRowFilter(rf);
        table.setRowSorter(sorter);
        
        return 0;
    }
    private String getJobAndPotentialText(String jobName, Long potential) {
        return jobName + " (" + potential + ")";
    }
    private String addLineBreakIfNonEmpty(String text) {
        if (! text.equals(""))
            return text + MyHTMLUtils.LINE_BREAK;
        else
            return text;
    }
    private String getRemoveText(String currentText, String thingToAdd
            , long value) {   
        return getRemoveText(currentText, thingToAdd + " (" + value + ")");
    }
    private String getRemoveText(String currentText, String thingToAdd) {
        return getAddRemoveText(currentText, thingToAdd, false, "-", COLOR_REMOVE);
    }
    private String getAddText(String currentText, String thingToAdd, long value) {
        return getAddText(currentText, thingToAdd + " (" + value + ")");
    }
    private String getAddText(String currentText, String thingToAdd) {
        return getAddRemoveText(currentText, thingToAdd, true, "+", COLOR_ADD);
    }
    private String getAddRemoveText(String currentText, String thingToAdd
            , boolean bold, String plusOrMinus, String color) {
        String strReturn = currentText; //addLineBreakIfNonEmpty(currentText);
        String strNewText = plusOrMinus + thingToAdd;
        if (bold) strNewText = MyHTMLUtils.makeBold(strNewText);
        if (! color.equals("")) strNewText = MyHTMLUtils.makeColored(strNewText, color);
        strReturn += strNewText;
        return strReturn;
    }
}
