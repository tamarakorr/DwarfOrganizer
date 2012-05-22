/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.swing.HideableTableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableRowSorter;
import myutils.*;

/**
 *
 * GUI for displaying optimizer results
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 *
 */
public class ResultsView { //  implements ActionListener {

    private static final int LABOR_COLUMN = 3;      // Labors column index in the table
    private static final int REMINDER_COLUMN = 4;   // Reminder column index in the table

    private static final int MULTILINE_GAP = 2;

    private static final String COLOR_ADD = "339933"; //"Green";
    private static final String COLOR_REMOVE = "Red";
    private static final String COLOR_STAY_SAME = "Black";

    private static final String ACTION_CMD_ALL = "ALL";
    private static final String ACTION_CMD_NOBLE = "NOBLE";
    private static final String ACTION_CMD_REMINDER = "REMINDER";

    private static final int LOW_ALTRUISM_THRESHOLD = 39;

    // Table and filters
    private JTable moTable;
    private RowFilter mrfNoble;
    private RowFilter mrfAll;
    private RowFilter mrfHasReminder;

    // Local variables set via constructor
    private List<Dwarf> mlstDwarves;
    private boolean[][] mbSolution;
    private List<Job> mlstJobs;
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

    public ResultsView(final JobOptimizer.Solution solution) {

        mlstDwarves = solution.getDwarves();
        mbSolution = solution.dwarfjobmap;
        mlstJobs = solution.getJobs();
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
        final MySimpleTableModel oModel = createResultsModel();
/*        for (int iCount = 0; iCount < oModel.getColumnCount(); iCount++)
            System.out.println("Column " + iCount + " is of class "
                + oModel.getColumnClass(iCount).getName()); */
        moTable = new JTable(oModel, new HideableTableColumnModel());   // oModel
        moTable.createDefaultColumnsFromModel(); // Necessary when using HideableTableColumnModel

        // Renderers
        final MyTCRStripedHighlight normalRenderer
                = new MyTCRStripedHighlight(1);
        moTable.setDefaultRenderer(Object.class, normalRenderer);

        // Top-align the multi-line columns
        class MyTopAlignedRenderer extends MyTCRStripedHighlight {
            MyTopAlignedRenderer(int i) {
                super(i);
            }

            @Override
            public Component getTableCellRendererComponent(final JTable table
                    , final Object value, final boolean isSelected
                    , final boolean hasFocus, final int row, final int column) {

                final JLabel renderedLabel
                        = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                renderedLabel.setVerticalAlignment(SwingConstants.TOP);
                return renderedLabel;
            }
        }

        final MyTopAlignedRenderer topAlignedRenderer
                = new MyTopAlignedRenderer(1);
        moTable.getColumn("Job").setCellRenderer(topAlignedRenderer);
        moTable.getColumn("Reminder").setCellRenderer(topAlignedRenderer);

        // Hide nickname if necessary
        setNicknameVisible(DEFAULT_NICKNAME_VIS);

        final JScrollPane oSP = new JScrollPane(moTable);
        MyHandyTable.handyTable(moTable, oModel, true, true);
        MyHandyTable.adjustMultiLineRowHeight(moTable, MULTILINE_GAP);
        MyHandyTable.setPrefWidthToColWidth(moTable);

        //oSP.setPreferredSize(oTable.getPreferredScrollableViewportSize());

        // Create view filter buttons
        final JRadioButton btnViewAll = new JRadioButton("View All");
        btnViewAll.setSelected(true);
        btnViewAll.setActionCommand(ACTION_CMD_ALL);

        final JRadioButton btnViewNobles = new JRadioButton("Nobles Only");
        btnViewNobles.setActionCommand(ACTION_CMD_NOBLE);

        final JRadioButton btnViewReminders = new JRadioButton("Has Reminder");
        btnViewReminders.setActionCommand(ACTION_CMD_REMINDER);

        final ButtonGroup optView = new ButtonGroup();
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

        final JPanel panFilter = new JPanel();
        panFilter.setLayout(new FlowLayout());
        //panFilter.add(chkJobsToRemove);
        panFilter.add(btnViewAll);
        panFilter.add(btnViewNobles);
        panFilter.add(btnViewReminders);

        // Put the UI together
        final JPanel panAll = new JPanel();
        panAll.setLayout(new BorderLayout());
        panAll.add(oSP);
        panAll.add(panFilter, BorderLayout.PAGE_END);

        // Create and show a window containing the table.
        final JFrame frList = MyHandyWindow.createSimpleWindow("Optimized Jobs"
                , panAll, new BorderLayout());
        frList.setJMenuBar(createMenu());
        frList.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frList.setVisible(true);

        // Create the actionlistener for the buttons
        // Must be done after creating frList
        final ActionListener filterActionListener
                = createActionListener(frList);
        btnViewAll.addActionListener(filterActionListener);
        btnViewNobles.addActionListener(filterActionListener);
        btnViewReminders.addActionListener(filterActionListener);

    }
    private class DisplayableChange {

        private String text;
        private ChangeType changeType;

        public DisplayableChange(final String text
                , final ChangeType changeType) {

            this.text = text;
            this.changeType = changeType;
        }

        @Override
        public String toString() {
            if (this.changeType == ChangeType.STAY_SAME
                    && mbShowJobs[ChangeType.STAY_SAME.getIndex()]) {
                return getAddRemoveText("", this.text, true, "="
                        , COLOR_STAY_SAME);
            }
            else if (this.changeType == ChangeType.ADD
                    && mbShowJobs[ChangeType.ADD.getIndex()]) {
                return getAddRemoveText("", this.text, true, "+", COLOR_ADD);
            }
            else if ((this.changeType == ChangeType.REMOVE_ALWAYS_SHOW
                    && mbShowJobs[ChangeType.REMOVE_ALWAYS_SHOW.getIndex()])
                    || ((this.changeType == ChangeType.REMOVE)
                    && mbShowJobs[ChangeType.REMOVE.getIndex()])) { //  mbShowRemoveJobs

                final boolean bold
                        = (this.changeType == ChangeType.REMOVE_ALWAYS_SHOW);
                return getAddRemoveText("", this.text, bold, "-", COLOR_REMOVE);
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

    private class DisplayableChanges extends ArrayList<DisplayableChange> {

        @Override
        public String toString() {

            String strReturn = "";
            for (final DisplayableChange displayableChange : this) {
                if (! strReturn.equals("")
                        && ! displayableChange.toString().equals(""))
                    strReturn += MyHTMLUtils.LINE_BREAK;
                strReturn += displayableChange.toString();
            }

            return MyHTMLUtils.toHTML(strReturn);
        }
    }

    private void setNicknameVisible(final boolean visible) {
        final HideableTableColumnModel hideableModel
                = (HideableTableColumnModel) moTable.getColumnModel();
        hideableModel.setColumnVisible("Nickname", visible);
    }

    private JMenuBar createMenu() {

        final JMenuBar menuBar = new JMenuBar();

        final JMenu menu = new JMenu("Display");
        menu.setMnemonic(KeyEvent.VK_D);
        menuBar.add(menu);

        // ----- Dwarf name format ---
        final JCheckBoxMenuItem checkMenuItem = new JCheckBoxMenuItem(
                "Nicknames", false);
        checkMenuItem.setMnemonic(KeyEvent.VK_N);
        checkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
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
    private JCheckBoxMenuItem createJobMenuItem(final String title
            , final ChangeType changeType, final int keyEvent) {

        final JCheckBoxMenuItem checkMenuItem = new JCheckBoxMenuItem(
                title, mbShowJobs[changeType.getIndex()]);
        checkMenuItem.setMnemonic(keyEvent);
        checkMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                mbShowJobs[changeType.getIndex()] = checkMenuItem.isSelected();
                updateShowRemoveJobs();
            }
        });

        return checkMenuItem;
    }

    private MySimpleTableModel createResultsModel() { // throws SolutionImpossibleException

        //Vector<String> columns = new Vector<String>();
        /*Vector<String> vColumns = new Vector<String>(
                Arrays.asList(new String[] { "Dwarf", "Nickname", "Scheduled"
                        , "Job", "Reminder", "Score"
        })); */
        final String[] columns = { "Dwarf", "Nickname", "Scheduled", "Job"
                , "Reminder", "Score"};
        final String strFeed = "Feed Patients/Prisoners";
        final String strRecover = "Recovering Wounded";

        // Find the maximum number of jobs held by a dwarf.
        int intMaxJobs = 0;
        for (int dCount = 0; dCount < mlstDwarves.size(); dCount++) {
            int intThisDwarfJobs = 0;
            for (int jCount = 0; jCount < mlstJobs.size(); jCount++)    // NUM_JOBS
                if (mbSolution[jCount][dCount])
                    intThisDwarfJobs++;
            if (intThisDwarfJobs > intMaxJobs)
                intMaxJobs = intThisDwarfJobs;
        }

        // Create table columns and model.
        final MySimpleTableModel oModel = new MySimpleTableModel(columns
                , mlstDwarves.size());

        // Fill in a row for each dwarf.
        for (int row = 0; row < mlstDwarves.size(); row++) {
            oModel.setValueAt(mlstDwarves.get(row).getName(), row, 0);
            oModel.setValueAt(mlstDwarves.get(row).getNickname(), row, 1);
            oModel.setValueAt( //NumberFormat.getInstance().format(
                    JobOptimizer.MAX_TIME - mlstDwarves.get(row).getTime(), row
                    , 2);   // getPercentInstance()
            oModel.setValueAt( //NumberFormat.getNumberInstance().format(
                    mdblScores[row], row, 5);  // getSkillSum(row)

            int jobCount = 0;
            final Dwarf thisDwarf = mlstDwarves.get(row);
            String strReminderText = "";
            final DisplayableChanges vChanges = new DisplayableChanges();

            for (int job = 0; job < mlstJobs.size(); job++) {   // NUM_JOBS
                final Job thisJob = mlstJobs.get(job);
                final boolean bHasReminder = ! thisJob.getReminder().equals("");

                if (mbSolution[job][row]) {

                    // Display any change from the current labors
                    if (thisDwarf.getLabors().contains(thisJob.getName())) {

                        vChanges.add(new DisplayableChange(
                                getJobAndPotentialText(thisJob.getName()
                                , thisDwarf.getBalancedPotentials().get(
                                thisJob.getName()))
                                , ChangeType.STAY_SAME));
                    }
                    else {

                        vChanges.add(new DisplayableChange(
                                getJobAndPotentialText(thisJob.getName()
                                , thisDwarf.getBalancedPotentials().get(
                                thisJob.getName()))
                                , ChangeType.ADD));


                        // Add reminder text if any is needed.
                        if (bHasReminder) {
                            strReminderText = addLineBreakIfNonEmpty(
                                    strReminderText);
                            strReminderText = getAddText(strReminderText
                                    , thisJob.getReminder() + " ("
                                    + thisJob.getName() + ")");
                        }
                    }
                    jobCount++;
                }

                // Check for printing any labors to remove
                else if (thisDwarf.getLabors().contains(thisJob.getName())) {
                    //intChangeCount++;

                    vChanges.add(new DisplayableChange(getJobAndPotentialText(
                                thisJob.getName()
                                , thisDwarf.getBalancedPotentials().get(
                                thisJob.getName()))
                                , ChangeType.REMOVE));

                    // Add reminder text if any is needed.
                    if (bHasReminder) {
                        strReminderText = addLineBreakIfNonEmpty(
                                strReminderText);
                        strReminderText = getRemoveText(strReminderText
                            , thisJob.getReminder() + " (" + thisJob.getName()
                            + ")");
                    }
                }
            }

            // If Recover Wounded or Feed Patients/Prisoners is enabled and
            // Altruism is low enough to give a bad thought, add these to the list.
            if (thisDwarf.getStatValues().get("Altruism") != null) {
                final boolean lowAltruism =
                        (thisDwarf.getStatValues().get("Altruism")
                        <= LOW_ALTRUISM_THRESHOLD);

                if (thisDwarf.getLabors().contains(strFeed) && lowAltruism) {
                    vChanges.add(new DisplayableChange(strFeed
                                , ChangeType.REMOVE_ALWAYS_SHOW));
                }

                if (thisDwarf.getLabors().contains(strRecover) && lowAltruism) {
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

    private ActionListener createActionListener(final Component parentComp) {
        final ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {

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
                        parentComp.setCursor(CursorController.BUSY_CURSOR);
                        return setCurrentTableFilter(moTable, rf);
                    }

                    // Adjust the multiline row height when done
                    @Override
                    protected void done() {
                        try {
                            MyHandyTable.adjustMultiLineRowHeight(moTable
                                    , MULTILINE_GAP);
                        } catch (Exception ignore) {
                        } finally {
                            parentComp.setCursor(
                                    CursorController.DEFAULT_CURSOR);
                        }
                    }
                };
                worker.execute();
            }
        };
        return al;
        //return CursorController.createListener(parentComp, al); Doesn't work
    }
    // Adjust multiline row height when this setting is changed
    private void updateShowRemoveJobs() {
        MyHandyTable.adjustMultiLineRowHeight(moTable, MULTILINE_GAP);
    }
    // Sets the current table filter and adjusts row height as necessary.
    private int setCurrentTableFilter(final JTable table, final RowFilter rf) {
        final TableRowSorter sorter = (TableRowSorter) table.getRowSorter(); //new TableRowSorter(oModel);
        sorter.setRowFilter(rf);
        table.setRowSorter(sorter);

        return 0;
    }
    private String getJobAndPotentialText(final String jobName
            , final long potential) {
        return jobName + " (" + potential + ")";
    }
    private String addLineBreakIfNonEmpty(final String text) {
        if (! text.equals(""))
            return text + MyHTMLUtils.LINE_BREAK;
        else
            return text;
    }
    private String getRemoveText(final String currentText
            , final String thingToAdd, final long value) {
        return getRemoveText(currentText, thingToAdd + " (" + value + ")");
    }
    private String getRemoveText(final String currentText
            , final String thingToAdd) {

        return getAddRemoveText(currentText, thingToAdd, false, "-", COLOR_REMOVE);
    }
    private String getAddText(final String currentText, final String thingToAdd
            , final long value) {

        return getAddText(currentText, thingToAdd + " (" + value + ")");
    }
    private String getAddText(final String currentText
            , final String thingToAdd) {

        return getAddRemoveText(currentText, thingToAdd, true, "+", COLOR_ADD);
    }
    private String getAddRemoveText(final String currentText
            , final String thingToAdd
            , final boolean bold, final String plusOrMinus
            , final String color) {

        String strReturn = currentText; //addLineBreakIfNonEmpty(currentText);
        String strNewText = plusOrMinus + thingToAdd;

        if (bold)
            strNewText = MyHTMLUtils.makeBold(strNewText);
        if (! color.equals(""))
            strNewText = MyHTMLUtils.makeColored(strNewText, color);
        strReturn += strNewText;
        return strReturn;
    }
}
