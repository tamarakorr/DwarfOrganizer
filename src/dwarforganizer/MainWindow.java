/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.broadcast.BroadcastListener;
import dwarforganizer.broadcast.BroadcastMessage;
import dwarforganizer.deepclone.DeepCloneUtils;
import dwarforganizer.dirty.DirtyForm;
import dwarforganizer.dirty.DirtyListener;
import dwarforganizer.swing.MenuCombiner;
import dwarforganizer.swing.MenuHelper;
import dwarforganizer.swing.MenuMnemonicSetter;
import dwarforganizer.swing.MyFileChooser;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileFilter;
import myutils.MyHandyOptionPane;
import myutils.MyHandyWindow;
import myutils.MySimpleLogDisplay;
import myutils.com.centerkey.utils.BareBonesBrowserLaunch;

/**
 *
 * @author Tamara Orr
 *
 * This software is provided under the MIT license.
 * See the included license.txt for details.
 */
public class MainWindow extends JFrame implements BroadcastListener { // implements DirtyListener

    private static final Logger logger = Logger.getLogger(
            MainWindow.class.getName());
    protected static final long MAX_SKILL_LEVEL = 20L;
    //private static final String DEFAULT_DWARVES_XML
    //        = "C://DwarfFortress//DwarfGuidanceCounselor//0.0.6//Dwarves.xml";
    private static final String DEFAULT_DWARVES_XML
            = "samples/dwarves/sample-7-dwarves.xml";
    private static final String TUTORIAL_FILE
            = "tutorial/ReadmeSlashTutorial.html";

    // Keys for internal frames
    private static final String INTERNAL_DWARF_LIST = "Dwarf List";
    private static final String INTERNAL_JOB_LIST = "Job List";
    private static final String INTERNAL_RULES_EDITOR = "Rules Editor";
    private static final String INTERNAL_EXCLUSIONS = "Exclusions";
    private static final String INTERNAL_VIEW_MANAGER = "View Manager";
    private static final String INTERNAL_LOG = "Log";
    private Map<String, MyAbstractInternalFrame> mhmInternalFrames;
    private JFrame moAboutScreen;
    private JDesktopPane moDesktop;

    private static final String DIRTY_TITLE = " (Unsaved Changes)";

    private static final int EXIT_MENU_PRIORITY = 100;
    private static final int FILE_MENU_MNEMONIC = KeyEvent.VK_F;

    private DwarfListWindow moDwarfListWindow;
    private JobListPanel moJobListPanel;
    private RulesEditorUI moRulesEditor;
    private ExclusionPanel moExclusionManager;
    private ViewManagerUI moViewManager;
    private MySimpleLogDisplay moLog;
    private static final int LOG_MAX_LINES = 500;
    private static final Level LOG_LEVEL = Level.INFO;

    private String mstrDwarvesXML = DEFAULT_DWARVES_XML;
    private File mfilLastFile;
    private static final String FILE_CHOOSER_SAVE = "Save"; // Keys for file choosers
    private static final String FILE_CHOOSER_OPEN = "Open";
    private static final String FILE_CHOOSER_DWARVES = "Dwarves";
    private Map<String, MyFileChooser> mmFileChoosers;

    private List<Labor> mlstLabors;
    private List<LaborGroup> mlstLaborGroups;
    private JobBlacklist moJobBlacklist;
    private JobList moJobWhitelist;
    private DwarfOrganizerIO moIO;
    private List<Dwarf> mlstDwarves;
    private List<Exclusion> mlstExclusions;
    private Map<Integer, Boolean> mmapActiveExclusions;
    private List<GridView> mlstViews; // TODO: This really shouldn't be messed with in MainWindow

    private static enum JobListMenuAccelerator {
        SAVE(KeyStroke.getKeyStroke("control S"))
            , SAVE_AS(KeyStroke.getKeyStroke("control shift S"))
            , OPEN(KeyStroke.getKeyStroke("control O"))
            , RESET(KeyStroke.getKeyStroke("control R"));

        private final KeyStroke keyStroke;
        private JobListMenuAccelerator(KeyStroke keyStroke) {
            this.keyStroke = keyStroke;
        }
        public KeyStroke getKeyStroke() { return keyStroke; }
    }

    private static final int DESKTOP_WIDTH = 800;
    private static final int DESKTOP_HEIGHT = 600;
    private static final String MAIN_TITLE = "Dwarf Organizer";

    public MainWindow() {
        super();

        FileData fileData = new FileData(new HashMap<String, Stat>()
                    , new HashMap<String, Skill>()
                    , new HashMap<String, MetaSkill>()); // dummy value

        initVariables();    // Initialize variables that must be created
        loadPreferences();  // Load user prefs

        // Read files, and don't necessarily crash if we fail
        try {
            fileData = readFiles();
        } catch (Exception e) {
            logger.log(Level.SEVERE
                    , "Failed to read at least one critical file.", e);
        }
        setExclusionsActive();      // Combine exclusions from user prefs with data from file
        prepareFrameUIs(fileData);  // Prepare data objects

        mmFileChoosers = createChoosers(); // (Must be done after initializing JobListPanel)

        // Use JobListPanel to create main menu
        final MenuCombiner.MenuInfo menuInfo = createMenu(moJobListPanel);
        final MenuCombiner combiner = new MenuCombiner(menuInfo); // Must be done after createMenu

        // Create frame maps and internal frames; must be done after
        // MenuCombiner is created
        mhmInternalFrames = createFrameMap();
        createFrames(mhmInternalFrames, combiner);

        setUpMainWindow(moDesktop, menuInfo);   // Set frame properties and show

        // Dwarf List on top (must be done after setting main window visible)
        frameToTop(INTERNAL_DWARF_LIST);
    }
    private class FileData {
        private Map<String, Stat> mapStat;
        private Map<String, Skill> mapSkill;
        private Map<String, MetaSkill> mapMetaSkill;

        public FileData(final Map<String, Stat> mapStat
                , final Map<String, Skill> mapSkill
                , final Map<String, MetaSkill> mapMetaSkill) {
            this.mapStat = mapStat;
            this.mapSkill = mapSkill;
            this.mapMetaSkill = mapMetaSkill;
        }

        public Map<String, MetaSkill> getMetaSkillMap() {
            return mapMetaSkill;
        }
        public Map<String, Skill> getSkillMap() {
            return mapSkill;
        }
        public Map<String, Stat> getStatMap() {
            return mapStat;
        }
    }
    private void initVariables() {
        moJobBlacklist = new JobBlacklist();
        moJobWhitelist = new JobList();
        moIO = new DwarfOrganizerIO();
        moAboutScreen = new AboutScreen(this);
        mlstLabors = new ArrayList<Labor>();
        mlstLaborGroups = new ArrayList<LaborGroup>();
        moDesktop = new JDesktopPane();
        moDesktop.setPreferredSize(new Dimension(DESKTOP_WIDTH
                , DESKTOP_HEIGHT));
    }
    // Creates and returns the frame map, and creates the internal frames
    private void createFrames(final Map<String, MyAbstractInternalFrame> map
            , final MenuCombiner combiner) {

        for (final String key : map.keySet()) {
            final MyAbstractInternalFrame frame = map.get(key);
            if (frame instanceof MyAbstractMenuFrame) {
                final MyAbstractMenuFrame menuFrame
                        = (MyAbstractMenuFrame) frame;
                menuFrame.create(combiner);
            }
            else {
                frame.create();
            }
        }
    }
    private void setUpMainWindow(final JDesktopPane desktop
            , final MenuCombiner.MenuInfo menuInfo) {

        this.setLayout(new BorderLayout());
        this.add(desktop, BorderLayout.CENTER);
        this.pack();

        this.setJMenuBar(menuInfo.getMenuBar());
        this.addWindowListener(createExitListener());
        this.setTitle(MAIN_TITLE);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setVisible(true);
    }
    private void frameToTop(final String frameKey) {
        try {
            getInternalFrame(frameKey).setSelected(true);
        } catch (final PropertyVetoException ignore) {
            logger.log(Level.WARNING
                    ,"[MainWindow.frameToTop] Failed to activate frame {0}"
                    , frameKey);
        }
    }
    private WindowAdapter createExitListener() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                exit();
            }
        };
    }
    private void prepareFrameUIs(final FileData fileData) {
        moLog = new MySimpleLogDisplay(LOG_LEVEL, LOG_MAX_LINES);
        logger.addHandler(moLog);

        moRulesEditor = new RulesEditorUI(mlstLabors);    // Create rules editor
        moExclusionManager = new ExclusionPanel(moIO);  // Create exclusions manager
        moViewManager = new ViewManagerUI(); // Create view manager

        // Create dwarf list window
        try {
            moDwarfListWindow = createDwarfListWindow(mlstLabors, fileData
                    , mlstLaborGroups, mlstViews, mlstDwarves, mlstExclusions
                    , moExclusionManager);
        } catch (final NullPointerException e) {
            logger.log(Level.SEVERE, "Failed to create Dwarf List interface:"
                    + " NullPointerException", e);
        }

        // Display a grid of the jobs to assign
        try {
            moJobListPanel = new JobListPanel(mlstLabors
                , mlstLaborGroups, moJobBlacklist, moIO);
            moJobListPanel.initialize();
        } catch (final JobListPanel.CouldntProcessFileException e) {
            logger.log(Level.SEVERE, "JobListPanel.CouldntProcessFileException"
                    , e);
        }
    }
    // Create Dwarf List window
    private DwarfListWindow createDwarfListWindow(final List<Labor> vLabors
            , final FileData fileData, final List<LaborGroup> vLaborGroup
            , final List<GridView> vViews, final List<Dwarf> vDwarves
            , final List<Exclusion> vExclusions
            , final ExclusionPanel ePanel) {

        final DwarfListWindow win = new DwarfListWindow(vLabors
                , fileData.getStatMap()
                , fileData.getSkillMap(), fileData.getMetaSkillMap()
                , vLaborGroup, vViews);
        win.loadData(vDwarves, vExclusions);
        ePanel.getAppliedBroadcaster().addListener(win); // Listen for exclusions applied
        win.getBroadcaster().addListener(this);

        return win;
    }
    private HashMap<String, MyAbstractInternalFrame> createFrameMap() {
        final int NUM_FRAMES = 5;
        final HashMap<String, MyAbstractInternalFrame> map
                = new HashMap<String, MyAbstractInternalFrame>(NUM_FRAMES);

        map.put(INTERNAL_RULES_EDITOR, new RulesEditorFrame(this, moDesktop
                , moRulesEditor));
        map.put(INTERNAL_EXCLUSIONS, new ExclusionFrame(this, moDesktop
                , moExclusionManager));
        map.put(INTERNAL_VIEW_MANAGER, new ViewManagerFrame(this, moDesktop
                , moViewManager));
        map.put(INTERNAL_DWARF_LIST, new DwarfListFrame(this, moDesktop
                , moDwarfListWindow, "Dwarf List"));
        map.put(INTERNAL_JOB_LIST, new JobListFrame(this, moDesktop
                , moJobListPanel, "Job Settings"));
        map.put(INTERNAL_LOG, new LogFrame(this, moDesktop, moLog.createUI()
                , "Log"));
        return map;
    }
    private class AboutScreen extends JFrame {
        private final String TITLE = "Dwarf Organizer License";

        public AboutScreen(final MainWindow main) {
            final JLabel lblVersion = new JLabel("Dwarf Organizer version "
                    + DwarfOrganizer.VERSION);

            final JTextArea txtLicense = new JTextArea(moIO.getLicense());
            txtLicense.setEditable(false);
            txtLicense.setLineWrap(true);
            txtLicense.setWrapStyleWord(true);
            final JScrollPane sp = new JScrollPane(txtLicense);

            this.setTitle(TITLE);
            this.setPreferredSize(new Dimension(500, 400));
            this.setLayout(new BorderLayout());
            this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            this.add(lblVersion, BorderLayout.NORTH);
            this.add(sp, BorderLayout.CENTER);
            this.pack();

            this.setLocationRelativeTo(main);
        }
    }
    private interface ListenerAdder {
        public void addListeners();
    }

    // Takes care of confirmations to save, disposal of objects, and
    // saving preferences.
    // Disposes of the window if the user does not cancel the operation.
    // (The default close operation should be DO_NOTHING_ON_CLOSE in order
    // for this to work.)
    private void exit() {

        // Prompts to save changes
        // TODO: Is moDwarfListWindow.exit() running on EDT? It needs to...
        if (! moDwarfListWindow.exit()) {
            return;
        }
        final String[] windowList = new String[] { INTERNAL_VIEW_MANAGER
                , INTERNAL_RULES_EDITOR, INTERNAL_EXCLUSIONS };
        for (final String key : windowList) {
            MyHandyWindow.clickClose(getInternalFrame(key));
        }
        moAboutScreen.dispose();    // Destroy "about" screen
        mhmInternalFrames.get(INTERNAL_LOG).getInternalFrame().dispose(); // Dispose of log

        savePreferences();          // Save preferences
        this.dispose();
    }
    private JInternalFrame getInternalFrame(final String key) {
        return mhmInternalFrames.get(key).getInternalFrame();
    }

    // Returns a JMenuBar made of the menus in the first menu bar, followed
    // by the menus in the second menu bar.
    private JMenuBar appendMenuBar(final JMenuBar menuBar1
            , final JMenuBar menuBar2) {

        final JMenuBar jmbReturn = new JMenuBar();
        final int firstMenuCount = menuBar1.getMenuCount();

        // (We run the loops backwards because getMenuCount() decrements
        // each time we add a menu to jmbReturn.)
        for (int iCount = menuBar1.getMenuCount() - 1; iCount >= 0; iCount--) {
            jmbReturn.add(menuBar1.getMenu(iCount), 0);
        }
        for (int iCount = menuBar2.getMenuCount() - 1; iCount >= 0; iCount--) {
            //System.out.println("Menu count: " + menuBar2.getMenuCount());
            //System.out.println("Adding " + menuBar2.getMenu(iCount).getText());
            jmbReturn.add(menuBar2.getMenu(iCount), firstMenuCount);
        }
        return jmbReturn;
    }

    private abstract class AbstractFrameCreator {
        public abstract Container createUIObject();
        public abstract void addListeners();

        public JInternalFrame createInternalFrame(final JDesktopPane desktop
                , final String title, final boolean resizable
                , final boolean closable, final boolean maximizable
                , final boolean iconifiable, final int closeBehavior) {

            final Container uiObject = createUIObject();
            addListeners();

            return MyHandyWindow.createInternalFrame(desktop, title, resizable
                    , closable, maximizable, iconifiable, closeBehavior
                    , uiObject);
        }
    }
    private abstract class AbstractEditorFrameCreator
            extends AbstractFrameCreator {

        public abstract DirtyForm getDirtyForm();

        public JInternalFrame createInternalFrame(final JDesktopPane desktop
                , final String cleanTitle, final boolean resizable
                , final boolean closable, final boolean maximizable
                , final boolean iconifiable, final int closeBehavior
                , final String dirtyTitle, final FrameClosingFunction fcf) {

            final JInternalFrame frameToCreate = super.createInternalFrame(
                    desktop
                    , cleanTitle, resizable, closable
                    , maximizable, iconifiable, closeBehavior);

            // Update title when dirty state changes
            final DirtyListener dirtyListener = createDirtyListener(
                dirtyTitle, cleanTitle, frameToCreate);
            getDirtyForm().addDirtyListener(dirtyListener);
            frameToCreate.addInternalFrameListener(
                    new InternalFrameClosingAdapter(fcf));

            return frameToCreate;
        }
    }

    private static Point getCenteringPoint(final JDesktopPane desktop
            , final JInternalFrame frame) {

        final Dimension desktopSize = desktop.getSize();
        final Dimension frameSize = frame.getSize();
        return new Point((desktopSize.width - frameSize.width) / 2
                , (desktopSize.height - frameSize.height) / 2);
    }
    private void doViewMgrWindowClosing(final MainWindow mainWindow
            , final ViewManagerUI viewManagerUI) {

        doWindowClosing(mainWindow, viewManagerUI
                , new ConfirmFunction() {
            @Override
            public void doConfirm() {
                saveViewManagerViews();
            }
        }
                , "Save Views?"
                , "Would you like to save your changes to views?");
    }
    private void saveViews(final List<GridView> views) {
        moIO.writeViews(views);
        mlstViews = views;            // Update local copy
        // (Don't set anything clean here since we don't know which window
        // initiated the save.)
    }

    private interface FrameClosingFunction {
        public void doFrameClosing(InternalFrameEvent e);
    }
    private class InternalFrameClosingAdapter extends InternalFrameAdapter {
        private FrameClosingFunction f;
        public InternalFrameClosingAdapter(final FrameClosingFunction f) {
            this.f = f;
        }
        @Override
        public void internalFrameClosing(final InternalFrameEvent e) {
            f.doFrameClosing(e);
        }
    }

    private DirtyListener createDirtyListener(final String dirtyTitle
            , final String cleanTitle, final JInternalFrame frame) {
        return new DirtyListener(){
            @Override
            public void dirtyChanged(final boolean newDirtyState) {
                final String strTitle;

                // If dirty
                if (newDirtyState)
                    strTitle = dirtyTitle;
                else
                    strTitle = cleanTitle;

                frame.setTitle(strTitle);
            }
        };
    }

    private interface ConfirmFunction {
        public void doConfirm();
    }
    private void doWindowClosing(final MainWindow main, final DirtyForm editor
            , final ConfirmFunction cf, final String strMessageTitle
            , final String strQuestion) {
        if (editor.isDirty()) {
            final MyHandyOptionPane optionPane = new MyHandyOptionPane();
            final Object[] options = { "Yes", "No" };
            final Object result = optionPane.yesNoDialog(main
                    , options, "Yes", ""
                    , strQuestion
                    , strMessageTitle);
            if ("Yes".equals(result.toString())) {
                cf.doConfirm();
            }
            else {   // "No"
                // TODO this makes me uncomfortable. The form has dirty data on
                // it.
                // But if we don't set it to clean here, then the user could be
                // asked again on app shutdown whether to save.
                editor.setClean();
            }
        }
    }
    private void doRulesWindowClosing(final MainWindow main
            , final RulesEditorUI rulesEditor) {

        doWindowClosing(main, rulesEditor, new ConfirmFunction() {
            @Override
            public void doConfirm() {
                saveRuleFile(rulesEditor);
            }
        }, "Save rules?", "Would you like to save your changes to rules?");
    }
    private void doExclWindowClosing(final MainWindow main
            , final ExclusionPanel exclMgr) {

        doWindowClosing(main, exclMgr, new ConfirmFunction() {
            @Override
            public void doConfirm() {
                saveExclusions(exclMgr);
            }
        }, "Save exclusions?", "Would you like to save and apply your changes"
                + " to exclusions?");
    }
    private void saveExclusions(final ExclusionPanel exclMgr) {
        exclMgr.saveExclusions(); // Also notifies Dwarf List, and sets clean
    }

    private void saveRuleFile(final RulesEditorUI rulesEditor) {
        moIO.writeRuleFile(rulesEditor.getCurrentFile());
        rulesEditor.setClean();

        // Recreate local blacklist structures & resend blacklist to Job
        // Settings screen
        setBlacklistStructures();
        moJobListPanel.setBlacklist(moJobBlacklist);
    }

    private void closeRules(final RulesEditorUI rulesEditor) {
        doRulesWindowClosing(this, rulesEditor);
        getInternalFrame(INTERNAL_RULES_EDITOR).setVisible(false);
    }

    private void closeExclusions() {
        doExclWindowClosing(this, moExclusionManager);
        getInternalFrame(INTERNAL_EXCLUSIONS).setVisible(false);
    }
    private void closeViewManager() {
        doViewMgrWindowClosing(this, moViewManager);
        getInternalFrame(INTERNAL_VIEW_MANAGER).setVisible(false);
    }

    // Creates file choosers for save and load operations. This is a time-consuming
    // operation and only needs to be done once.
    private Map<String, MyFileChooser> createChoosers() {
        final HashMap<String, MyFileChooser> map
                = new HashMap<String, MyFileChooser>(3);

        // File chooser for Job Settings->Save
        MyFileChooser chooser = new MyFileChooser(this);
        chooser.setDialogTitle("Save Job Settings");
        chooser.setDialogType(MyFileChooser.SAVE_DIALOG);
        final FileFilter ffText = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                final String ext = getExtension(pathname.getName());
                return ext.toUpperCase().equals("TXT");
            }
            @Override
            public String getDescription() {
                return "Dwarf Organizer text files (.txt)";
            }
        };
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(ffText);
        chooser.setCurrentDirectory(moJobListPanel.getDirectory());
        map.put(FILE_CHOOSER_SAVE, chooser);

        // File chooser for Job Settings->Open...
        chooser = new MyFileChooser(this);
        chooser.setDialogTitle("Load Job Settings");
        chooser.setDialogType(MyFileChooser.OPEN_DIALOG);
        chooser.setCurrentDirectory(moJobListPanel.getDirectory());
        map.put(FILE_CHOOSER_OPEN, chooser);

        // File chooser for Dwarves.xml
        final File file = new File(mstrDwarvesXML);
        chooser = new MyFileChooser(this);
        chooser.setDialogTitle("Select location of Dwarves.xml");
        chooser.setCurrentDirectory(file);
        chooser.setSelectedFile(file);
        map.put(FILE_CHOOSER_DWARVES, chooser);

        return map;
    }

    private FileData readFiles() {
        // Try to read group-list.txt, labor-list.txt, rules.txt, Dwarves.xml,
        // and exclusions
        final FileData fileData;

        mlstLaborGroups = moIO.readLaborGroups();
        mlstLabors = moIO.readLabors();           // Read labor-list.txt

        moIO.readRuleFile();
        setBlacklistStructures();

        fileData = readDwarves();

        mlstExclusions = moIO.readExclusions(mlstDwarves); // mhtActiveExclusions

        mlstViews = moIO.readViews();

        return fileData;
    }
    private void setBlacklistStructures() {
        moJobBlacklist = moIO.getBlacklist();
        moJobWhitelist = moIO.getWhitelist();

        // (Post-processing must be done after mvLabors is set)
        addWhitelistToBlacklist(moJobBlacklist, moJobWhitelist, mlstLabors);
    }
    private void showFrameByKey(final String frameKey) {
        final JInternalFrame frame = getInternalFrame(frameKey);

        frame.setVisible(true);
        try {
            frame.setIcon(false);
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    private ActionListener createShowListener(final String frameKey) {
        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                showFrameByKey(frameKey);
            }
        };
    }
    private ActionListener createShowOrLoadListener(final String frameKey
            , final DataLoader dataLoader) {

        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                showOrLoad(getInternalFrame(frameKey), dataLoader);
            }
        };
    }
    private SwingWorker createOptimizeWorker(final JobListPanel jobListPanel) {
        return new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    // Optimizer
                    final List<Job> lstJobs = jobListPanel.getJobs();
                    final List<Dwarf> lstDwarves = getDwarves();

                    setBalancedPotentials(lstDwarves, lstJobs);
                    final JobOptimizer opt = new JobOptimizer(lstJobs
                            , lstDwarves, moJobBlacklist, moLog); //, moDesktop);
                    return opt.optimize();
                }
            };
    }
    private ActionListener createOptimizeAL(final JMenuItem optimizeItem
            , final JobListPanel jobListPanel) {

        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Disable the menu item while running
                optimizeItem.setEnabled(false);
                showFrameByKey(INTERNAL_LOG);

                // Do this lengthy processing on a background thread, maintaining
                // UI responsiveness.
                final SwingWorker worker = createOptimizeWorker(jobListPanel);
                worker.execute();

                optimizeItem.setEnabled(true);
            }
        };
    }
    private ActionListener createExitAL() {
        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                exit();
            }
        };
    }
    private interface DataLoader {
        public void loadData();
    }
    private class RulesLoader implements DataLoader {
        @Override
        public void loadData() {
            // Use a clone for the table model. Otherwise
            // unsaved changes will persist when window is closed
            // and reopened.
            final List<LaborRule> vRulesClone = DeepCloneUtils.deepClone(
                    moIO.getRuleFileContents());
            moRulesEditor.loadData(vRulesClone);
        }
    }
    // (We need to deep-clone mvExclusions
    // and loadData on that, not on mvExclusions directly.
    // Otherwise the "active" checkbox states will carry over
    // between sessions)
    private class ExclLoader implements DataLoader {
        @Override
        public void loadData() {
            final List<Exclusion> exclDeepClone = DeepCloneUtils.deepClone(
                    mlstExclusions);
            moExclusionManager.loadData(exclDeepClone, mlstDwarves);
        }
    }
    private ActionListener createTutorialAL() {
        return new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    // TODO: Find out if this fix worked for users
                    // (for Java 6 Desktop non-implementation on some Linux builds)
                    //java.awt.Desktop.getDesktop().open(new File(TUTORIAL_FILE));
                    BareBonesBrowserLaunch.openURL(new File(
                            TUTORIAL_FILE).toURI().toURL().toString());
                } catch (final Exception ex) {
                    logger.log(Level.SEVERE, null, ex);
                    logger.severe("Failed to open " + TUTORIAL_FILE
                            + " with default browser.");
                }
            }
        };
    }
    private ActionListener createAboutAL() {
        return new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                moAboutScreen.setVisible(true);
            }
        };
    }
    private void addMenuItem(final JMenu toMenu, final JMenuItem menuItem
            , final ArrayList<Integer> priorityList, final int priority) {
        toMenu.add(menuItem);
        priorityList.add(priority);
    }
    private void addSeparator(final JMenu toMenu
            , final ArrayList<Integer> priorityList, final int priority) {
        toMenu.addSeparator();
        priorityList.add(priority);
    }
    private ArrayList<Integer> createProcessMenu(final JMenuBar menuBar
            , final JobListPanel jobListPanel) {
        final int OPTIMIZE_PRIO = 80;
        final int SEP_PRIO = 90;

        final ArrayList<Integer> lstReturn = new ArrayList<Integer>();

        final JMenu menu = MenuHelper.createMenu("File", FILE_MENU_MNEMONIC); // "Process", VK_P
        menuBar.add(menu);

        final JMenuItem optimizeItem = MenuHelper.createMenuItem(
                "Optimize Now!", KeyEvent.VK_O, KeyStroke.getKeyStroke("F5"));
        optimizeItem.addActionListener(createOptimizeAL(optimizeItem
                , jobListPanel));
        addMenuItem(menu, optimizeItem, lstReturn, OPTIMIZE_PRIO);
        // ---------------------------------------------------------------------
        addSeparator(menu, lstReturn, SEP_PRIO);

        final JMenuItem menuItem = MenuHelper.createMenuItem("Exit"
                , createExitAL(), KeyEvent.VK_X);
        addMenuItem(menu, menuItem, lstReturn, EXIT_MENU_PRIORITY);

        return lstReturn;
    }
    private ArrayList<Integer> createWindowMenu(final JMenuBar menuBar) {
        final ArrayList<Integer> lstReturn = new ArrayList<Integer>();
        final int PRIO_DWARF = 20;
        final int PRIO_JOB = 30;
        final int PRIO_RULES = 40;
        final int PRIO_EXCL = 50;
        final int PRIO_LOG = 10;

        final JMenu menu = MenuHelper.createMenu("Window", KeyEvent.VK_W);
        menuBar.add(menu);

        JMenuItem menuItem;
        menuItem = MenuHelper.createMenuItem("Log"
                , createShowListener(INTERNAL_LOG), KeyEvent.VK_L
                , KeyStroke.getKeyStroke("F4"));
        addMenuItem(menu, menuItem, lstReturn, PRIO_LOG);
        menuItem = MenuHelper.createMenuItem("Dwarf List"
                , createShowListener(INTERNAL_DWARF_LIST), KeyEvent.VK_D
                , KeyStroke.getKeyStroke("F6"));
        addMenuItem(menu, menuItem, lstReturn, PRIO_DWARF);
        menuItem = MenuHelper.createMenuItem("Job Settings"
                , createShowListener(INTERNAL_JOB_LIST), KeyEvent.VK_J
                , KeyStroke.getKeyStroke("F7"));
        addMenuItem(menu, menuItem, lstReturn, PRIO_JOB);
        menuItem = MenuHelper.createMenuItem("Rules Editor"
                , createShowOrLoadListener(INTERNAL_RULES_EDITOR
                , new RulesLoader()), KeyEvent.VK_R
                , KeyStroke.getKeyStroke("F8"));
        addMenuItem(menu, menuItem, lstReturn, PRIO_RULES);
        menuItem = MenuHelper.createMenuItem("Exclusion Manager"
                , createShowOrLoadListener(INTERNAL_EXCLUSIONS
                , new ExclLoader()), KeyEvent.VK_E
                , KeyStroke.getKeyStroke("F9"));
        addMenuItem(menu, menuItem, lstReturn, PRIO_EXCL);
        return lstReturn;
    }
    private ArrayList<Integer> createHelpMenu(final JMenuBar menuBar) {
        final ArrayList<Integer> lstReturn = new ArrayList<Integer>();
        final int PRIO_TUTORIAL = 10;
        final int PRIO_SEP = 20;
        final int PRIO_ABOUT = 30;

        final JMenu menu = MenuHelper.createMenu("Help", KeyEvent.VK_H);
        menuBar.add(menu);

        JMenuItem menuItem = MenuHelper.createMenuItem("Tutorial"
                , createTutorialAL(), KeyEvent.VK_T
                , KeyStroke.getKeyStroke("F1"));
        addMenuItem(menu, menuItem, lstReturn, PRIO_TUTORIAL);
        //----------------------------------------------------------------------
        addSeparator(menu, lstReturn, PRIO_SEP);

        menuItem = MenuHelper.createMenuItem("About", createAboutAL()
                , KeyEvent.VK_A);
        addMenuItem(menu, menuItem, lstReturn, PRIO_ABOUT);

        return lstReturn;
    }
    private MenuCombiner.MenuInfo createMenu(final JobListPanel jobListPanel) {
        final JMenuBar menuBar = new JMenuBar();

        final int[] menuPriority = { 10, 80, 90 };
        final ArrayList<Integer>[] menuItemPriority
                = new ArrayList[menuPriority.length];

        menuItemPriority[0] = createProcessMenu(menuBar, jobListPanel);
        menuItemPriority[1] = createWindowMenu(menuBar);
        menuItemPriority[2] = createHelpMenu(menuBar);

        return new MenuCombiner.MenuInfo(menuBar, menuPriority
                , menuItemPriority);
    }

    // Reload if invisible; show if visible.
    private void showOrLoad(final JInternalFrame frame
            , final DataLoader loader) {

        if (frame.isVisible() == false) {
            loader.loadData();
            frame.pack();
        }

        frame.setLocation(getCenteringPoint(moDesktop, frame)); // Center
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (final PropertyVetoException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    // Set dwarves.xml location & read it
    private void setDwarves() {
        final int response = setDwarvesLocation();
        if (response == MyFileChooser.APPROVE_OPTION) {
            try {
                readDwarves();
            } catch (final Exception e) {
                logger.log(Level.SEVERE, "Failed to read dwarves.xml.", e);
            }
            moDwarfListWindow.setVisible(false);
            moDwarfListWindow.loadData(mlstDwarves, mlstExclusions);
            moDwarfListWindow.setVisible(true);
        }
    }

    // Reset job settings to user defaults
    private void resetJobSettings(final JobListPanel jobPanel) {
        jobPanel.load(new File(JobListPanel.MY_DEFAULT_SETTINGS_FILE));
    }

    private void loadJobSettings(final JobListPanel jobListPanel) {
        final int input = getFileChooser(FILE_CHOOSER_OPEN).showOpenDialog(
                this);

        if (input == MyFileChooser.APPROVE_OPTION) {
            final File file = getFileChooser(
                    FILE_CHOOSER_OPEN).getSelectedFile();
            jobListPanel.load(file);
            updateCurrentJobSettings(file);
        }
    }

    // TODO Are comments below true? Cleanup needed if so
    // If saving as "DEFAULT SETTINGS", change name to "MY DEFAULT SETTINGS"
    // (That logic is now unused since .txt is added to file names, and we don't load from
    // default settings in this way anymore)
    private void updateCurrentJobSettings(File file) {
        String fileName = file.getName();
        if (fileName.equals("DEFAULT SETTINGS")) {
            fileName = "MY DEFAULT SETTINGS";
            file = new File(file.getParentFile(), fileName);
        }
        mfilLastFile = file;
        getInternalFrame(INTERNAL_JOB_LIST).setTitle(
                fileName.replace(".txt", "") + " Job Settings");
    }

    // Returns the text after (not including) the dot if the given file name
    // has an extension.
    // Returns the whole file name if there is no dot.
    private String getExtension(final String fileName) {
        final int dot = fileName.lastIndexOf(".");
        return fileName.substring(dot + 1);
    }
    private MyFileChooser getFileChooser(final String key) {
        return mmFileChoosers.get(key);
    }
    private void saveJobSettingsAs(final JobListPanel jobListPanel
            , final boolean quickSave) {

        boolean bConfirm = false;
        File file = mfilLastFile;

        if (quickSave && (mfilLastFile != null)) {
            bConfirm = true;
        }
        else {
            final int input = getFileChooser(FILE_CHOOSER_SAVE).showSaveDialog(
                    this);

            if (input == MyFileChooser.APPROVE_OPTION) {
                file = getFileChooser(FILE_CHOOSER_SAVE).getSelectedFile();
                // If the user enters no extension, append ".txt"
                if (! fileHasExtension(file))
                    file = addExtension(file, ".txt");

                bConfirm = true;
                if (file.exists()) {
                    final Object[] options = new Object[] { "Yes", "No" };
                    final Object result = new MyHandyOptionPane().yesNoDialog(
                            this, options, "No", ""
                            , "This file already exists. Overwrite it?"
                            , "Overwrite?");
                    bConfirm = (result.toString().equals("Yes"));
                }
            }
        }

        if (bConfirm) {
            jobListPanel.save(file);
            updateCurrentJobSettings(file);
        }
    }

    private boolean fileHasExtension(final File file) {
        final String strName = file.getName();
        //System.out.println("Extension is " + getExtension(file.getName()));
        //System.out.println(getExtension(strName).length() + " ?= " + strName.length());
        return (getExtension(strName).length() != strName.length());
    }

    private File addExtension(final File file, final String ext) {
        final String strNewName = file.getName() + ext;
        return new File(file.getParentFile(), strNewName);
    }

    // Returns the user's input to the file dialog
    private int setDwarvesLocation() {
        final MyFileChooser chooser = getFileChooser(FILE_CHOOSER_DWARVES);
        final int input = chooser.showOpenDialog(this);
        if (input == MyFileChooser.APPROVE_OPTION) {
            String strSelectedLoc = chooser.getCurrentDirectory() + "\\"
                + chooser.getSelectedFile().getName();
            strSelectedLoc = strSelectedLoc.replace("\\", "//");
            logger.log(Level.INFO, "Selected Dwarves.xml location: {0}"
                    , strSelectedLoc);
            //textField.setText(strSelectedLoc);
            mstrDwarvesXML = strSelectedLoc;
        }
        return input;
    }

    private void loadPreferences() {
        final Preferences prefs = Preferences.userNodeForPackage(
                this.getClass());
        mstrDwarvesXML = prefs.get("DwarvesXML", DEFAULT_DWARVES_XML);

        // Exclusions active
        final int maxID = prefs.getInt("MaxExclusionID", 0);
        mmapActiveExclusions = new HashMap<Integer, Boolean>();
        for (int iCount = 0; iCount <= maxID; iCount++) { // moIO.getMaxUsedExclusionID() <- not set yet!
            //System.out.println("ExclusionsActive_" + iCount + ", " + DwarfOrganizerIO.DEFAULT_EXCLUSION_ACTIVE);
            // (.active in the mvExclusions object is set later, after exclusions are read)
            final boolean value = prefs.getBoolean("ExclusionsActive_" + iCount
                    , DwarfOrganizerIO.DEFAULT_EXCLUSION_ACTIVE);
            //setExclusionActive(iCount, value);
            mmapActiveExclusions.put(iCount, value);
        }
    }
    private void savePreferences() {
        //System.out.println("Saving preferences...");
        final Preferences prefs = Preferences.userNodeForPackage(
                this.getClass());
        prefs.put("DwarvesXML", mstrDwarvesXML);

        // Exclusions active
        prefs.putInt("MaxExclusionID", moIO.getMaxUsedExclusionID());
        for (final int key : mmapActiveExclusions.keySet()) {
            //System.out.println("ExclusionsActive_" + key + " " + mhtActiveExclusions.get(key));
            prefs.putBoolean("ExclusionsActive_" + key
                    , mmapActiveExclusions.get(key));
        }
        //System.out.println("    Done saving preferences.");
    }

    // Updates the initial active state of the exclusions from mhtActiveExclusions
    // Must be called after loadPreferences() and after readFiles()
    private void setExclusionsActive() {
        for (final int key : mmapActiveExclusions.keySet()) {
            //System.out.println("Setting exclusion #" + key + " " + mhtActiveExclusions.get(key));
            setExclusionActive(key, mmapActiveExclusions.get(key));
        }
    }
    private void setExclusionActive(final int ID, final boolean active) {
        if (mlstExclusions == null) {
            logger.severe("Could not set active exclusion: exclusion list is"
                    + " null");
            return;
        }

        for (final Exclusion excl : mlstExclusions) {
            if (excl.getID() == ID) {
                excl.setActive(active);
                return;
            }
        }
        // (Do nothing if there is no exclusion with this ID)
    }

    private FileData readDwarves() {
        // Set dummy values:
        mlstDwarves = new ArrayList<Dwarf>();
        Map<String, Stat> mapStat;
        Map<String, Skill> mapSkill;
        Map<String, MetaSkill> mapMetaSkill;

        final DwarfOrganizerIO.DwarfIO dwarfIO = new DwarfOrganizerIO.DwarfIO();
        dwarfIO.readDwarves(mstrDwarvesXML);
        mlstDwarves = dwarfIO.getDwarves();
        mapStat = dwarfIO.getStats();
        mapSkill = dwarfIO.getSkills();
        mapMetaSkill = dwarfIO.getMetaSkills();

        return new FileData(mapStat, mapSkill, mapMetaSkill);
    }

    private ArrayList<Dwarf> getDwarves() {
        // Get included dwarves and reset all dwarf.time
        // TODO clone() probably isn't doing what it's supposed to
        final ArrayList<Dwarf> lstIncluded = (ArrayList<Dwarf>)
                moDwarfListWindow.getIncludedDwarves().clone();
        for (final Dwarf dwarf : lstIncluded) {
            dwarf.setTime(JobOptimizer.MAX_TIME);
        }
        return lstIncluded;
    }

    // Note that balanced potentials are keyed by job name, not skill name.
    private void setBalancedPotentials(final List<Dwarf> lstDwarves
            , final List<Job> lstJobs) {

        for (final Dwarf dwarf : lstDwarves) {
            for (final Job job : lstJobs) {

                final double dblCurrentSkillPct = ((double)
                        job.getCurrentSkillWeight()) / 100.0d;
                final double dblPotentialPct = 1.0d - dblCurrentSkillPct;
                long skillLevel = 0L;
                if (null != dwarf.getSkillLevels().get(job.getSkillName()))
                    skillLevel = dwarf.getSkillLevels().get(job.getSkillName());

                final double dblBalancedPotential = (dblCurrentSkillPct
                        * skillLevelToPercent(skillLevel))
                        + (dblPotentialPct
                        * ((double) dwarf.getSkillPotentials().get(
                        job.getSkillName())));

                dwarf.getBalancedPotentials().put(job.getName()
                        , Math.round(dblBalancedPotential));
            }
        }
    }
    private double skillLevelToPercent(final long skillLevel) {
        if (skillLevel >= MAX_SKILL_LEVEL)
            return 100.0d;
        else
            return ((double) skillLevel) * 100.0d / ((double) MAX_SKILL_LEVEL);
    }

    private void addWhitelistToBlacklist(final JobBlacklist blacklist
            , final JobList whitelist, final List<Labor> labors) {
        for (final String wlJobName : whitelist.keySet()) {
            for (final Labor labor : labors) {
                // Add all non-whitelisted jobs to the blacklist.
                if (! wlJobName.equals(labor.getName())) {
                    if (! whitelist.get(wlJobName).contains(labor.getName()))
                        blacklist.addOneWayEntry(wlJobName, labor.getName());
                }
            }
        }
    }
    private void handleViewManagerMessage(final BroadcastMessage message) {
        if (message.getSource().equals("ViewManagerDefaultButton")) {
            setDefaultButton(message, getInternalFrame(INTERNAL_VIEW_MANAGER));
        }
        else if (message.getSource().equals("ViewManagerSave")) {
            saveViewManagerViews();
        }
        else if (message.getSource().equals("ViewManagerClose"))
            closeViewManager();
        else if (message.getSource().equals("ViewManagerRequestFocus")) {
            //System.out.println("ViewMgrReqFoc");
            final JComponent comp = (JComponent) message.getTarget();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    comp.requestFocusInWindow();
                }
            });
        }
        else {
            DwarfOrganizer.showInfo(this
                    , "[MainWindow.handleViewManagerMessage]"
                    + " Unknown broadcast message received.", "Problem");
        }
    }
    private void handleDwarfListMessage(final BroadcastMessage message) {
        if (message.getSource().equals("DwarfListSaveViews")) {
            final List<GridView> views = (List<GridView>) message.getTarget();
            saveViews(new ArrayList(views));
        }
        else if (message.getSource().equals("DwarfListManageViews")) {
            loadViewManager();
        }
        else {
            DwarfOrganizer.showInfo(this, "[MainWindow.handleDwarfListMessage]"
                    + " Unknown broadcast message received", "Problem");
        }
    }
    private void handleExclusionMessage(final BroadcastMessage message) {
        if (message.getSource().equals("ExclusionPanelDefaultButton")) {
            setDefaultButton(message
                    , getInternalFrame(INTERNAL_EXCLUSIONS));
        }
        else if (message.getSource().equals("ExclusionPanelActiveExclusions")) {
            try {
                mmapActiveExclusions
                        = (HashMap<Integer, Boolean>) message.getTarget();
            } catch (Exception ignore) {
                logger.severe("Failed to set active exclusions");
            }
        }
        else if (message.getSource().equals("ExclusionPanelClose")) {
            closeExclusions();
        }
        else if (message.getSource().equals("ExclusionPanelApply")) {
            final ArrayList<Exclusion> lstExclusion
                    = (ArrayList<Exclusion>) message.getTarget();
            updateActiveExclusions(lstExclusion);
        }
        else {
            DwarfOrganizer.showInfo(this, "[MainWindow.handleExclusionMessage]"
                    + " Unknown broadcast message received", "Problem");
        }
    }
    private void handleRulesEditorMessage(final BroadcastMessage message) {
        if (message.getSource().equals("RulesEditorDefaultButton")) {
            setDefaultButton(message, getInternalFrame(INTERNAL_RULES_EDITOR));
        }
        else {
            DwarfOrganizer.showInfo(this
                    , "[MainWindow.handleRulesEditorMessage]"
                    + " Unknown broadcast message received", "Problem");
        }
    }
    // For receiving broadcast messages
    @Override
    public void broadcast(final BroadcastMessage message) {
        //System.out.println("Broadcast message received");
        if (message.getSource().startsWith("ViewManager")) {
            handleViewManagerMessage(message);
        }
        else if (message.getSource().startsWith("DwarfList")) {
            handleDwarfListMessage(message);
        }
        else if (message.getSource().startsWith("ExclusionPanel")) {
            handleExclusionMessage(message);
        }
        else if (message.getSource().startsWith("RulesEditor")) {
            handleRulesEditorMessage(message);
        }
        else {
            DwarfOrganizer.showInfo(this, "[MainWindow.broadcast]"
                    + " Unknown broadcast message received", "Problem");
        }
    }
    private void saveViewManagerViews() {
        final ArrayList<GridView> vView = moViewManager.getViews();
        saveViews(vView);
        moViewManager.setClean();
        moDwarfListWindow.updateViews(vView);
    }
    private void loadViewManager() {
        final DataLoader loader = new DataLoader() {
                    @Override
                    public void loadData() {
                        // Use a clone for the table model. Otherwise
                        // unsaved changes will persist when window is closed
                        // and reopened.
                        final List<GridView> vViewClone
                                = DeepCloneUtils.deepClone(mlstViews);
                        moViewManager.loadData(vViewClone);
                    }
                };
        showOrLoad(getInternalFrame(INTERNAL_VIEW_MANAGER), loader);
    }

    // Attempts to set the default button in the given internal frame
    // to the JButton object in the message target.
    private void setDefaultButton(final BroadcastMessage message
            , final JInternalFrame frame) {

        // Set default button
        try {
            if (message.getTarget() == null)
                frame.getRootPane().setDefaultButton(null);
            else {
                final JButton btn = (JButton) message.getTarget();
                if (! btn.equals(frame.getRootPane().getDefaultButton()))
                    setDefaultButton(btn, frame);
            }
        } catch (final Exception e) {
            logger.log(Level.SEVERE, "Failed to set default button", e);
        }
    }
    private void setDefaultButton(final JButton btn
            , final JInternalFrame frame) {

        frame.getRootPane().setDefaultButton(btn);
    }
    private void updateActiveExclusions(final List<Exclusion> vExclusion) {

        // Rebuild mhtActiveExclusions
        mmapActiveExclusions = new HashMap<Integer, Boolean>();
        for (final Exclusion excl : vExclusion) {
            mmapActiveExclusions.put(excl.getID(), excl.isActive());
        }
        mlstExclusions = vExclusion;
    }
    private abstract class MyAbstractInternalFrame {
        private JInternalFrame internalFrame;
        private MainWindow mainWindow;

        private MyAbstractInternalFrame(final MainWindow mainWindow) {
            this.mainWindow = mainWindow;
        }
        public JInternalFrame getInternalFrame() {
            return internalFrame;
        }

        public void setInternalFrame(final JInternalFrame internalFrame) {
            this.internalFrame = internalFrame;
        }

        public MainWindow getMainWindow() {
            return mainWindow;
        }

        public abstract JInternalFrame createFrame();

        public JInternalFrame create() {
            final JInternalFrame frame = createFrame();
            setInternalFrame(frame);
            return frame;
        }
        // TODO: Close, save(?), and load could be options
    }
    private abstract class MyAbstractMenuFrame extends MyAbstractInternalFrame {
        private MenuCombiner.MenuInfo menuInfo;

        private MyAbstractMenuFrame(final MainWindow mainWindow) {
            super(mainWindow);
        }

        public abstract MenuCombiner.MenuInfo createMenuInfo();

        public MenuCombiner.MenuInfo getMenuInfo() {
            return menuInfo;
        }

        public JInternalFrame create(final MenuCombiner combiner) {
            final JInternalFrame frame = super.create();
            menuInfo = createMenuInfo();
            addCombiner(frame, combiner);
            return frame;
        }

        // Called by create()
        private void addCombiner(final JInternalFrame frame
                , final MenuCombiner combiner) {

            if (menuInfo.getMenuBar().getMenuCount() > 0) {
                final InternalFrameListener listener
                        = combiner.createInternalFrameListener(getMainWindow()
                        , getMenuInfo());
                frame.addInternalFrameListener(listener);
            }
        }
    }
    private abstract class MyAbstractSimpleFrame extends MyAbstractMenuFrame {

        private JDesktopPane desktop;
        private Container ui;
        private String title;

        private MyAbstractSimpleFrame(final MainWindow main
                , final JDesktopPane desktop
                , final Container ui, final String title) {

            super(main);
            this.desktop = desktop;
            this.ui = ui;
            this.title = title;
        }

        @Override
        public JInternalFrame createFrame() {
            return createAlwaysShownFrame(desktop, ui, title); //, menuBar);
        }
        private JInternalFrame createAlwaysShownFrame(final JDesktopPane desktop
                , final Container ui, final String title) {

            final AbstractFrameCreator creator = new AbstractFrameCreator() {
                @Override
                public Container createUIObject() {
                    return ui;
                }
                @Override
                public void addListeners() { // Do nothing
                }
            };
            final JInternalFrame frame = creator.createInternalFrame(desktop
                    , title
                    , true, false, true, true
                    , WindowConstants.DISPOSE_ON_CLOSE); //, menuBar);
            frame.pack();
            frame.setVisible(true);
            return frame;
        }
    }
    private JInternalFrame createEditorFrame(final JDesktopPane desktop
            , final Container ui, final String cleanTitle
            , final DirtyForm dirtyForm, final ListenerAdder la
            , final String dirtyTitle, final FrameClosingFunction fcf) {

        final AbstractEditorFrameCreator creator
                = new AbstractEditorFrameCreator() {

            @Override
            public Container createUIObject() {
                return ui;
            }
            @Override
            public DirtyForm getDirtyForm() {
                return dirtyForm;
            }
            @Override
            public void addListeners() {
                la.addListeners();
            }
        };

        final JInternalFrame frame = creator.createInternalFrame(desktop
                , cleanTitle, true, true, true, true
                , WindowConstants.HIDE_ON_CLOSE
                , dirtyTitle, fcf);

        return frame;
    }
    private class ExclusionFrame extends MyAbstractMenuFrame {
        private static final String TITLE = "Manage Exclusions";
        private static final String TITLE_DIRTY = TITLE + DIRTY_TITLE;

        private JDesktopPane desktop;
        private ExclusionPanel exclusionPanel;

        public ExclusionFrame(final MainWindow main, final JDesktopPane desktop
                , final ExclusionPanel exclusionPanel) {
            super(main);
            this.desktop = desktop;
            this.exclusionPanel = exclusionPanel;
        }

        @Override
        public JInternalFrame createFrame() {
            final ListenerAdder la = new ListenerAdder() {
                @Override
                public void addListeners() {
                    exclusionPanel.getDefaultButtonBroadcaster().addListener(
                            getMainWindow());
                    exclusionPanel.getCloseBroadcaster().addListener(
                            getMainWindow());
                    exclusionPanel.getAppliedBroadcaster().addListener(
                            getMainWindow());
                }
            };
            final FrameClosingFunction fcf = new FrameClosingFunction() {
                @Override
                public void doFrameClosing(final InternalFrameEvent e) {
                    doExclWindowClosing(getMainWindow(), exclusionPanel);
                }
            };

            setInternalFrame(createEditorFrame(desktop, exclusionPanel
                    , TITLE, exclusionPanel, la, TITLE_DIRTY, fcf));
            return getInternalFrame();
        }
        private ActionListener createSaveAL() {
            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    exclusionPanel.saveExclusions();
                }
            };
        }
        private ActionListener createCloseAL() {
            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    closeExclusions();
                }
            };
        }
        private ArrayList<Integer> createFileMenu(final JMenuBar menuBar) {
            final int SAVE_PRIO = 31;
            final int SEP_PRIO = 62;
            final int EXIT_PRIO = EXIT_MENU_PRIORITY - 1;

            final ArrayList<Integer> aReturn = new ArrayList<Integer>();

            final JMenu menu = MenuHelper.createMenu("File"
                    , FILE_MENU_MNEMONIC);
            menuBar.add(menu);

            JMenuItem menuItem = MenuHelper.createMenuItem("Save and Apply"
                    , createSaveAL()
                    , KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"));
            addMenuItem(menu, menuItem, aReturn, SAVE_PRIO);
            //------------------------------------------------------------------
            addSeparator(menu, aReturn, SEP_PRIO);

            menuItem = MenuHelper.createMenuItem("Close Exclusion Manager"
                    , createCloseAL(), KeyEvent.VK_C);
            addMenuItem(menu, menuItem, aReturn, EXIT_PRIO);
            return aReturn;
        }

        @Override
        public MenuCombiner.MenuInfo createMenuInfo() {
            final int NUM_MENUS = 1;
            final int[] menuPriority = new int[] { 1 };
            final ArrayList<Integer>[] menuItemPriority
                    = new ArrayList[NUM_MENUS];

            final JMenuBar menuBar = new JMenuBar();

            menuItemPriority[0] = createFileMenu(menuBar);

            return new MenuCombiner.MenuInfo(menuBar, menuPriority
                    , menuItemPriority);
        }
    }
    private class RulesEditorFrame extends MyAbstractMenuFrame {
        private static final String RULES_EDITOR_TITLE_CLEAN = "Edit Rules";
        private static final String RULES_EDITOR_TITLE_DIRTY
                = RULES_EDITOR_TITLE_CLEAN + DIRTY_TITLE;
        private JDesktopPane desktop;
        private RulesEditorUI rulesEditor;

        public RulesEditorFrame(final MainWindow main
                , final JDesktopPane desktop, final RulesEditorUI rulesEditor) {
            super(main);
            this.desktop = desktop;
            this.rulesEditor = rulesEditor;
        }

        @Override
        public JInternalFrame createFrame() {
            final ListenerAdder la = new ListenerAdder() {
                @Override
                public void addListeners() {
                    rulesEditor.getDefaultButtonBroadcaster().addListener(
                            getMainWindow());
                }
            };
            final FrameClosingFunction fcf = new FrameClosingFunction() {
                @Override
                public void doFrameClosing(final InternalFrameEvent e) {
                    doRulesWindowClosing(getMainWindow(), rulesEditor);
                }
            };

            setInternalFrame(createEditorFrame(desktop, rulesEditor
                    , RULES_EDITOR_TITLE_CLEAN
                    , rulesEditor, la
                    , RULES_EDITOR_TITLE_DIRTY, fcf));
            return getInternalFrame();
        }
        private ActionListener createSaveAL() {
            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    saveRuleFile(rulesEditor);
                }
            };
        }
        private ActionListener createCloseAL() {
            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    closeRules(rulesEditor);
                }
            };
        }
        private ArrayList<Integer> createFileMenu(final JMenuBar menuBar) {
            final int SAVE_PRIO = 31;
            final int SEP_PRIO = 62;
            final int CLOSE_PRIO = EXIT_MENU_PRIORITY - 1;

            final ArrayList<Integer> lstPriority = new ArrayList<Integer>();

            final JMenu menu = MenuHelper.createMenu("File"
                    , FILE_MENU_MNEMONIC);
            menuBar.add(menu);

            JMenuItem menuItem = MenuHelper.createMenuItem("Save"
                    , createSaveAL()
                    , KeyEvent.VK_S, KeyStroke.getKeyStroke("control S"));
            addMenuItem(menu, menuItem, lstPriority, SAVE_PRIO);
            // -----------------------------------------------------------------
            addSeparator(menu, lstPriority, SEP_PRIO);

            // ----------------------------------
            menuItem = MenuHelper.createMenuItem("Close Rules Editor"
                    , createCloseAL(), KeyEvent.VK_C);
            addMenuItem(menu, menuItem, lstPriority, CLOSE_PRIO);

            return lstPriority;
        }
        @Override
        public MenuCombiner.MenuInfo createMenuInfo() {
            final int NUM_MENUS = 1;
            final int[] menuPriority = new int[] { 1 };
            final ArrayList<Integer>[] menuItemPriority
                    = new ArrayList[NUM_MENUS];

            final JMenuBar menuBar = new JMenuBar();

            menuItemPriority[0] = createFileMenu(menuBar);

            return new MenuCombiner.MenuInfo(menuBar, menuPriority
                    , menuItemPriority);
        }
    }
    private class ViewManagerFrame extends MyAbstractInternalFrame {
        private static final String VIEW_MGR_TITLE = "Manage Views";
        private static final String VIEW_MGR_TITLE_DIRTY = VIEW_MGR_TITLE
                + DIRTY_TITLE;

        private JDesktopPane desktop;
        private ViewManagerUI viewManagerUI;

        public ViewManagerFrame(final MainWindow mainWindow
                , final JDesktopPane desktop
                , final ViewManagerUI viewManagerUI) {

            super(mainWindow);
            this.desktop = desktop;
            this.viewManagerUI = viewManagerUI;
        }

        @Override
        public JInternalFrame createFrame() {
            final ListenerAdder la = new ListenerAdder() {
                @Override
                public void addListeners() {
                    viewManagerUI.getBroadcaster().addListener(getMainWindow());
                }
            };
            final FrameClosingFunction fcf = new FrameClosingFunction() {
                @Override
                public void doFrameClosing(final InternalFrameEvent e) {
                    doViewMgrWindowClosing(getMainWindow(), viewManagerUI);
                }
            };

            final JInternalFrame frame = createEditorFrame(desktop
                    , viewManagerUI.getUIPanel(), VIEW_MGR_TITLE
                    , viewManagerUI, la, VIEW_MGR_TITLE_DIRTY, fcf);

            setDefaultButton(viewManagerUI.getDefaultButton(), frame);
            setInternalFrame(frame);
            return getInternalFrame();
        }
    }
    private class DwarfListFrame extends MyAbstractSimpleFrame {
        private DwarfListWindow dwarfListWindow;

        public DwarfListFrame(final MainWindow mainWindow
                , final JDesktopPane desktop
                , final DwarfListWindow dwarfListWindow, final String title) {

            super(mainWindow, desktop, dwarfListWindow, title);

            this.dwarfListWindow = dwarfListWindow;
        }

        @Override
        public MenuCombiner.MenuInfo createMenuInfo() {
            JMenuBar menuBar = new JMenuBar();
            final int[] menuPriority = new int[] { 1, 13, 14, 31 }; // TODO hard-coded but dynamically created

            createFileMenu(menuBar);
            menuBar = appendMenuBar(menuBar, dwarfListWindow.getMenu());

            final ArrayList<Integer>[] menuItemPriority = createPriorities(
                    menuBar);

            MenuMnemonicSetter.setMnemonics(menuBar);

            return new MenuCombiner.MenuInfo(menuBar, menuPriority
                    , menuItemPriority);
        }
        private ArrayList<Integer>[] createPriorities(final JMenuBar menuBar) {
            final int NUM_MENUS = menuBar.getMenuCount();
            final ArrayList<Integer>[] menuItemPriority
                    = new ArrayList[NUM_MENUS];

            final int STARTING_PRIO = 51;
            for (int iCount = 0; iCount < NUM_MENUS; iCount++) {
                final JMenu menu = menuBar.getMenu(iCount);
                final int numItems = menu.getMenuComponentCount();
                final ArrayList<Integer> lstItem = new ArrayList<Integer>(
                        numItems);
                int priority = STARTING_PRIO;
                for (int jCount = 0; jCount < numItems; jCount++) {
                    lstItem.add(priority++);
                }
                menuItemPriority[iCount] = lstItem;
            }
            return menuItemPriority;
        }
        private void createFileMenu(final JMenuBar menuBar) {
            final JMenu menu = MenuHelper.createMenu("File"
                    , FILE_MENU_MNEMONIC);
            menuBar.add(menu);

            menu.add(MenuHelper.createMenuItem("Set Location of Dwarves.xml..."
                    , createSetLocListener()
                    , KeyStroke.getKeyStroke(KeyEvent.VK_L
                    , ActionEvent.CTRL_MASK)));
        }
        private ActionListener createSetLocListener() {
            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    setDwarves();
                }
            };
        }
    }
    private class JobListFrame extends MyAbstractSimpleFrame {
        private JobListPanel jobListPanel;

        public JobListFrame(final MainWindow mainWindow
                , final JDesktopPane desktop
                , final JobListPanel jobListPanel, final String title) {

            super(mainWindow, desktop, jobListPanel, title);

            this.jobListPanel = jobListPanel;
        }

        @Override
        public MenuCombiner.MenuInfo createMenuInfo() {
            final int NUM_MENUS = 2;
            final int[] menuPriority = new int[] { 1, 2 };
            final ArrayList<Integer>[] menuItemPriority
                    = new ArrayList[NUM_MENUS];

            final JMenuBar menuBar = new JMenuBar();

            menuItemPriority[0] = createFileMenu(menuBar);
            menuItemPriority[1] = createEditMenu(menuBar);

            return new MenuCombiner.MenuInfo(menuBar, menuPriority
                    , menuItemPriority);
        }
        private ActionListener createLoadActionListener() {
            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    loadJobSettings(jobListPanel);
                }
            };
        }
        private ActionListener createSaveActionListener(
                final boolean quickSave) {

            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    saveJobSettingsAs(jobListPanel, quickSave);
                }
            };
        }
        private ActionListener createResetActionListener() {
            return new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    resetJobSettings(jobListPanel);
                }
            };
        }
        private ArrayList<Integer> createFileMenu(final JMenuBar menuBar) {

            final int OPEN_PRIO = 24;
            final int SEP1_PRIO = 25;
            final int SAVE_PRIO = 26;
            final int SAVEAS_PRIO = 27;
            final int SEP2_PRIO = 28;
            final int RESET_PRIO = 29;

            final ArrayList<Integer> lstReturn = new ArrayList<Integer>();

            final JMenu menu = MenuHelper.createMenu("File"
                    , FILE_MENU_MNEMONIC);
            menuBar.add(menu);

            JMenuItem menuItem = MenuHelper.createMenuItem("Open..."
                    , createLoadActionListener(), KeyEvent.VK_O
                    , JobListMenuAccelerator.OPEN.getKeyStroke());
            addMenuItem(menu, menuItem, lstReturn, OPEN_PRIO);
            // -----------------------------------------------------------------
            addSeparator(menu, lstReturn, SEP1_PRIO);

            menuItem = MenuHelper.createMenuItem("Save"
                    , createSaveActionListener(true), KeyEvent.VK_S
                    , JobListMenuAccelerator.SAVE.getKeyStroke());
            addMenuItem(menu, menuItem, lstReturn, SAVE_PRIO);
            menuItem = MenuHelper.createMenuItem("Save As..."
                    , createSaveActionListener(false), KeyEvent.VK_A
                    , JobListMenuAccelerator.SAVE_AS.getKeyStroke());
            addMenuItem(menu, menuItem, lstReturn, SAVEAS_PRIO);
            // -----------------------------------------------------------------
            addSeparator(menu, lstReturn, SEP2_PRIO);

            menuItem = MenuHelper.createMenuItem("Reset to My Defaults"
                    , createResetActionListener(), KeyEvent.VK_R);
            addMenuItem(menu, menuItem, lstReturn, RESET_PRIO);

            return lstReturn;
        }
        private ArrayList<Integer> createEditMenu(final JMenuBar menuBar) {
            final ArrayList<Integer> lstReturn = new ArrayList<Integer>();

            JMenu menu = MenuHelper.createMenu("Edit", KeyEvent.VK_E);
            menuBar.add(menu);

            menu = jobListPanel.createEditMenuItems(menu);
            lstReturn.addAll(createMenuItemPriority(menu));

            return lstReturn;
        }
        private ArrayList<Integer> createMenuItemPriority(final JMenu menu) {
            final int NUM_ITEMS = menu.getMenuComponentCount();
            final ArrayList<Integer> list = new ArrayList<Integer>(NUM_ITEMS);

            int priority = 51;
            for (int iCount = 0; iCount < NUM_ITEMS; iCount++) {
                list.add(priority++);
            }
            return list;
        }
    }
    private class LogFrame extends MyAbstractInternalFrame {

        private JDesktopPane desktop;
        private Container ui;
        private String title;

        public LogFrame(final MainWindow mainWindow
                , final JDesktopPane desktop, final Container ui
                , final String title) {

            super(mainWindow);
            this.desktop = desktop;
            this.ui = ui;
            this.title = title;
        }

        @Override
        public JInternalFrame createFrame() {
            final JInternalFrame frame = MyHandyWindow.createInternalFrame(
                    desktop, title, true, true, true, true
                    , WindowConstants.HIDE_ON_CLOSE, ui);
            frame.setSize(700, 250);
            frame.setVisible(true);
            return frame;
        }
    }
}