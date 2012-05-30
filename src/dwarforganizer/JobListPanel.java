/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.StateIncrementHandler.DefaultState;
import dwarforganizer.StateIncrementHandler.ThresholdFunctions;
import dwarforganizer.bins.BinPack;
import dwarforganizer.swing.CopyCutPastingTable;
import dwarforganizer.swing.MyTableTransferHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
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

    //private static final String CURRENT_JOB_SETTINGS_VERSION = "A";
    private static final int MAX_DWARF_TIME = 100;
    private static final int DEFAULT_QTY = 0;
    private static final int DEFAULT_TIME = MAX_DWARF_TIME;      // 1.0d
    private static final double DEFAULT_WT = 1.0d;
    private static final int DEFAULT_SKILL_WT = 50;
    protected static final String DEFAULT_REMINDER = "";
    //private static final String DEFAULT_FILE_TEXT = "[Enter a file name]";

    // DEFAULT SETTINGS shouldn't be used - it just exists as a read-only file
    // with the stock defaults
    private static final String DEFAULT_SETTINGS_FILE
            = "samples/jobs/DEFAULT SETTINGS";
    protected static final String MY_DEFAULT_SETTINGS_FILE
            = "samples/jobs/MY DEFAULT SETTINGS";

    // Column identifiers
    private static final String QTY_COL_IDENTIFIER = "Qty";
    private static final String TIME_COL_IDENTIFIER = "Time";
    private static final String JOB_PRIO_COL_IDENTIFIER = "Job Priority";
    private static final String CUR_SKILL_WT_COL_IDENTIFIER
            = "Current Skill Weight";
    private static final String REMINDER_COL_IDENTIFIER = "Reminder";

    private List<Labor> mlstLabors; // Set in constructor
    private List<LaborGroup> mlstLaborGroups; //Set in constructor    = new Vector<LaborGroup>();
    private List<Job> mlstLaborSettings;
    private JobBlacklist moBlacklist = new JobBlacklist();

    private SelectingTable moTable;
    private JLabel lblHours;

    private boolean mbLoading;
    private StateIncrementHandler moLoadingHandler;

    // keysToIgnore: A vector of keystrokes to be ignored by the JTable editor
    //               (i.e. keystrokes bound to menu items such as control S)
    // The problem with control S etc. activating the JTable editing session
    // is an outstanding bug in Java documented at
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4820794

    public JobListPanel(final List<Labor> vLabors
            , final List<LaborGroup> vLaborGroups
            , final JobBlacklist blacklist, final int heightToWorkWith)
            throws CouldntProcessFileException {

        moLoadingHandler = createLoadingHandler();
        moLoadingHandler.increment();

        mlstLaborGroups = vLaborGroups;
        mlstLabors = vLabors;
        moBlacklist = blacklist;

        // Create labor settings
        mlstLaborSettings = new ArrayList<Job>(vLabors.size());
        for (final Labor labor : vLabors) {
            mlstLaborSettings.add(new Job(labor.getName(), labor.getSkillName()
                    , DEFAULT_QTY //, 0
                    , DEFAULT_TIME, DEFAULT_WT, DEFAULT_SKILL_WT
                    , DEFAULT_REMINDER));
        }

        // Create job settings table
        final List<Color> lstBackgroundColors = new ArrayList<Color>(
                mlstLaborGroups.size());
        final List lstGroups = new ArrayList(mlstLaborGroups.size());
        for (final LaborGroup laborGroup : mlstLaborGroups) {
            lstBackgroundColors.add(getColor(laborGroup.getRed()
                    , laborGroup.getGreen(), laborGroup.getBlue()));
            lstGroups.add(laborGroup.getName());
        }

        // Hours label
        lblHours = new JLabel("Number of job hours: X");
        final JPanel panHours = new JPanel();
        panHours.setLayout(new BorderLayout());
        panHours.add(lblHours, BorderLayout.LINE_START);

        // Build UI
        final Object[] columns = { "Group", "Labor", QTY_COL_IDENTIFIER
                , TIME_COL_IDENTIFIER, JOB_PRIO_COL_IDENTIFIER    // "Time Weight"
                , CUR_SKILL_WT_COL_IDENTIFIER, REMINDER_COL_IDENTIFIER };
        final Class[] columnClass = { String.class, String.class, Integer.class
                , Integer.class
                , Double.class, Integer.class, String.class };  // No primitives allowed here in Java 6!!
        final MySimpleTableModel oModel = new MySimpleTableModel(columns
                , mlstLaborSettings.size(), columnClass);

        // Add the edit listener
        oModel.addTableModelListener(createTableModelListener(oModel));

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
                lstBackgroundColors, lstGroups, 0));

        // This didn't work: see solution in CopyCutPastingTable
        // JTable must ignore all menu accelerators associated with the job list
        // Otherwise it will start an editing session when Control+S or whatever is pressed
        //for (KeyStroke keyStroke : keysToIgnore) {
        /*for (MainWindow.JobListMenuAccelerator accel
                : MainWindow.JobListMenuAccelerator.values()) {

                //System.out.println(accel.getKeyStroke().toString());
                alwaysIgnoreKeyStroke(moTable, accel.getKeyStroke());
        } */

        final JScrollPane oSP = new JScrollPane(moTable);
        MyHandyTable.handyTable(moTable, oModel, false, true);
        MyHandyTable.setPrefWidthToColWidth(moTable);
        oSP.setPreferredSize(new Dimension(oSP.getPreferredSize().width
                , heightToWorkWith));

        // Create panel
        this.setLayout(new BorderLayout());
        //this.add(panFileInfo, BorderLayout.PAGE_START);
        this.add(oSP);
        this.add(panHours, BorderLayout.PAGE_END);

        moLoadingHandler.decrement();
    }
    // Handles incremental changes to mbLoading
    private StateIncrementHandler createLoadingHandler() {
        final StateIncrementHandler handler = new StateIncrementHandler(
                DefaultState.NEGATIVE_STATE);
        handler.initialize(new ThresholdFunctions() {
            @Override
            public void doAtNegativeThreshold() {
                mbLoading = false;
            }
            @Override
            public void doAtPositiveThreshold() {
                mbLoading = true;
            }
        });

        return handler;
    }
    private TableModelListener createTableModelListener(
            final MySimpleTableModel model) {

        return new TableModelListener() {

            @Override
            public void tableChanged(final TableModelEvent e) {
                //Logger.getLogger(JobListPanel.class.getName()).fine(
                //    "Table changed."); major spam
                if (! mbLoading) {
                    Logger.getLogger(JobListPanel.class.getName()).log(
                            Level.FINE
                            , "Updating labor setting (col={0}, row={1})"
                            , new Object[]{e.getColumn(), e.getFirstRow()});
                    updateLaborSetting(e.getFirstRow());
                }
            }

            private void updateLaborSetting(final int firstRow) {
                //TODO: In catch blocks, also set the table value to reflect the
                // default value (Will this be circular?)

                //double dblNewTime = Double.parseDouble(oModel.getValueAt(firstRow, 3).toString());
                int intNewTime;
                int intNewQty;
                try {
                    intNewTime = Integer.parseInt(model.getValueAt(firstRow, 3).toString());
                } catch (final NumberFormatException ignore) {
                    intNewTime = DEFAULT_TIME;
                }
                try {
                    intNewQty = Integer.parseInt(model.getValueAt(
                            firstRow, 2).toString());
                } catch (final NumberFormatException ignore) {
                    intNewQty = DEFAULT_QTY;
                }

                final Job job = mlstLaborSettings.get(firstRow);
                final boolean bTimeChanged = (job.getQtyDesired() != intNewQty
                        || job.getTime() != intNewTime);

                job.setQtyDesired(intNewQty);
                job.setTime(intNewTime);
                try {
                    job.setCandidateWeight(Double.parseDouble(model.getValueAt(
                            firstRow, 4).toString()));
                } catch (final NumberFormatException ignore) {
                    job.setCandidateWeight(DEFAULT_WT);
                }
                try {
                    job.setCurrentSkillWeight(Integer.parseInt(
                            model.getValueAt(firstRow, 5).toString()));
                } catch (final NumberFormatException ignore) {
                    job.setCurrentSkillWeight(DEFAULT_SKILL_WT);
                }
                job.setReminder(model.getValueAt(firstRow, 6).toString());

                if (bTimeChanged) {
                    //updateHours();
                    new HoursDisplayUpdater().execute();
                }
            }
        };
    }
    // A table cell editor that selects all text when we start to edit a cell:
    class SelectingEditor extends DefaultCellEditor {
        public SelectingEditor(final JTextField textField) {  // JTextField textField
            super(textField);    // textField
        }

        @Override
        public Component getTableCellEditorComponent(final JTable table
                , final Object value
                , final boolean isSelected, final int row, final int column) {

            final Component c = super.getTableCellEditorComponent(table, value
                    , isSelected, row, column);
            if (c instanceof JTextComponent) {
                final JTextField jtf = (JTextField) c;
                if (MyNumeric.isNumericClass(table.getColumnClass(column))) {
                    jtf.setHorizontalAlignment(JTextField.RIGHT);
                }
                //TODO: Override isCellEditable so that we can detect an edit
                // that starts with a double-click, and highlight in that case
                // as well
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
        public SelectingTable(final TableModel oModel) {
            super(oModel);
            final TableColumnModel model = super.getColumnModel();
            for (int iCount = 0; iCount < super.getColumnCount(); iCount++) {
                final TableColumn tc = model.getColumn(iCount);
                final JTextField txt = new JTextField();
                txt.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                tc.setCellEditor(new SelectingEditor(txt));    // new JTextField()
            }
        }
    }

    public class CouldntProcessFileException extends Exception {
        public CouldntProcessFileException() {
            super();
        }
    };

    // Updates the job hours display
    private class HoursDisplayUpdater extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            final long lngHours = sumHours();
            final long lngDwarves = getMinDwarvesNeeded();
            final String text = "Number of job hours: "
                    + NumberFormat.getInstance().format(lngHours)
                    + " (~" + lngDwarves + " dwarves)";
            lblHours.setText(text);
            return 0;
        }
    };

    private JPopupMenu createEditMenuPopup() {

        final JPopupMenu popUp = new JPopupMenu();
        //JMenu menu = new JMenu("Edit");

        moTable.createEditMenuItems(popUp);
        //popUp.add(menu);

        return popUp;
    }
    protected JMenu createEditMenuItems(final JMenu menu) {
        moTable.createEditMenuItems(menu);
        return menu;
    }

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

    private ArrayList<JobOpening> getJobOpenings() {
        int size = 0;
        for (final Job job : mlstLaborSettings) {
            size += job.getQtyDesired();
        }
        final ArrayList<JobOpening> lstReturn = new ArrayList<JobOpening>(size);

        for (final Job job : mlstLaborSettings) {
            for (int iCount = 0; iCount < job.getQtyDesired(); iCount++)
                lstReturn.add((JobOpening) job);
        }

        return lstReturn;
    }

    private long sumHours() {
        long lngReturn = 0L;
        for (final Job job : mlstLaborSettings)
            lngReturn += job.getQtyDesired() * job.getTime();
        return lngReturn;
    }

    // Updates internal blacklist and updates display
    protected void setBlacklist(final JobBlacklist newData) {
        moBlacklist = newData;
        new HoursDisplayUpdater().execute();
    }

    // Uses a bin packing algorithm to determine the number of dwarves.
    // This value is not simply the number of job hours divided by the number
    // of hours available per dwarf. The job blacklist/whitelist and the
    // fact that the user may have chosen numbers of hours that don't fit
    // perfectly together may both influence the number of dwarves needed.
    private long getMinDwarvesNeeded() {
        final BinPack<JobOpening> binPacker = new BinPack<JobOpening>();
        final ArrayList<ArrayList<JobOpening>> lstPackedBins
                = binPacker.binPack(getJobOpenings(), MAX_DWARF_TIME
                , moBlacklist);
        return lstPackedBins.size();
    }

    protected List<Job> getJobs() {
        return mlstLaborSettings;
    }
    protected void setJobs(final List<Job> jobList) {
        mlstLaborSettings = jobList;
    }

    private void loadLaborSettings() {
        moLoadingHandler.increment();

        final TableModel oModel = moTable.getModel();
        int row = 0;
        for (final Job job : mlstLaborSettings) {
            oModel.setValueAt(getGroupForLabor(job.getName()), row, 0);
            oModel.setValueAt(job.getName(), row, 1);
            oModel.setValueAt(job.getQtyDesired(), row, 2);
            //if (laborSetting.time != DEFAULT_TIME) System.out.println("Setting time to " + laborSetting.time);
            oModel.setValueAt(job.getTime(), row, 3);
            oModel.setValueAt(job.getCandidateWeight(), row, 4);
            oModel.setValueAt(job.getCurrentSkillWeight(), row, 5);
            oModel.setValueAt(job.getReminder(), row, 6);
            row++;
        }
        moLoadingHandler.decrement();
    }

    // Do when job settings are saved to file
    public void doOnSave() {
        MyHandyTable.stopEditing(moTable);      // Accept partial edits
    }

    // Do when job settings are loaded from file
    public void doOnLoad() {
        MyHandyTable.cancelEditing(moTable);    // Cancel partial edits

        //mbLoading = true;
        moLoadingHandler.increment();

        // Display the values in the table.
        loadLaborSettings();
        new HoursDisplayUpdater().execute();

        //mbLoading = false;
        moLoadingHandler.decrement();
    }

    // Print labor settings, for debugging
    private void printLaborSettings() {
        for (final Job job : mlstLaborSettings) {
            System.out.println(job.getName() + " " + job.getQtyDesired()
                    + " " + job.getTime() + " "
                    + job.getCandidateWeight() + " "
                    + job.getCurrentSkillWeight() + " "
                    + job.getReminder());
        }
    }

    private Color getColor(final String colorName) {

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
    // Crappy unused code remains to deter me from trying it again
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
    private String getGroupForLabor(final String laborName) {
        for (final Labor labor : mlstLabors)
            if (labor.getName().equals(laborName))
                return labor.getGroupName();

        return "";
    }
}
