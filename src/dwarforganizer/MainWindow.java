/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.dirty.DirtyListener;
import dwarforganizer.dirty.DirtyForm;
import dwarforganizer.swing.MenuMnemonicSetter;
import dwarforganizer.swing.MyFileChooser;
import dwarforganizer.broadcast.BroadcastMessage;
import dwarforganizer.broadcast.BroadcastListener;
import dwarforganizer.deepclone.DeepCloneUtils;
import dwarforganizer.swing.MenuCombiner;
import dwarforganizer.swing.MenuHelper;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileFilter;
import myutils.MyHandyOptionPane;
import myutils.MyHandyWindow;
import myutils.com.centerkey.utils.BareBonesBrowserLaunch;

/**
 *
 * @author Tamara Orr
 *
 * This software is provided under the MIT license.
 * See the included license.txt for details.
 */
public class MainWindow extends JFrame implements BroadcastListener { // implements DirtyListener

    private static final String VERSION = "1.21";

    protected static final long MAX_SKILL_LEVEL = 20l;    // That's an "L", not a one
    //private static final String DEFAULT_DWARVES_XML
    //        = "C://DwarfFortress//DwarfGuidanceCounselor//0.0.6//Dwarves.xml";
    private static final String DEFAULT_DWARVES_XML = "samples/dwarves/sample-7-dwarves.xml";
    private static final String TUTORIAL_FILE = "tutorial/ReadmeSlashTutorial.html";

    // Keys for internal frames
    private static final String INTERNAL_DWARF_LIST = "Dwarf List";
    private static final String INTERNAL_JOB_LIST = "Job List";
    private static final String INTERNAL_RULES_EDITOR = "Rules Editor";
    private static final String INTERNAL_EXCLUSIONS = "Exclusions";
    private static final String INTERNAL_VIEW_MANAGER = "View Manager";
    private static final int NUM_INTERNAL_FRAMES = 5;

    private static final int EXIT_MENU_PRIORITY = 100;
    private static final int FILE_MENU_MNEMONIC = KeyEvent.VK_F;

    private Map<String, MyAbstractInternalFrame> mhmInternalFrames;

    private DwarfListWindow moDwarfListWindow;
    private JobListPanel moJobListPanel;
    private RulesEditorUI moRulesEditor;

    private String mstrDwarvesXML = DEFAULT_DWARVES_XML;
    private File mfilLastFile;
    private MyFileChooser mjfcSave; //= new JFileChooser(); JFileChooser
    private MyFileChooser mjfcOpen; //= new JFileChooser();
    private MyFileChooser mjfcDwarves;

    private Vector<Labor> mvLabors;
    private Vector<LaborGroup> mvLaborGroups;

    private static final String DIRTY_TITLE = " (Unsaved Changes)";

    private static final String RULES_EDITOR_TITLE_CLEAN = "Edit Rules";
    private static final String RULES_EDITOR_TITLE_DIRTY
            = RULES_EDITOR_TITLE_CLEAN + DIRTY_TITLE;
    private JobBlacklist moJobBlacklist = new JobBlacklist();
    private JobList moJobWhitelist = new JobList();

    private DwarfOrganizerIO moIO = new DwarfOrganizerIO();
    private Vector<Dwarf> mvDwarves;
    private Vector<Exclusion> mvExclusions;
    private Hashtable<Integer, Boolean> mhtActiveExclusions;

    private JFrame moAboutScreen = new AboutScreen(this);

    private static final String EXCLUSIONS_TITLE = "Manage Exclusions";
    private static final String EXCLUSIONS_TITLE_DIRTY = EXCLUSIONS_TITLE + DIRTY_TITLE;
    private static final String VIEW_MGR_TITLE = "Manage Views";
    private static final String VIEW_MGR_TITLE_DIRTY = VIEW_MGR_TITLE + DIRTY_TITLE;

    private ExclusionPanel moExclusionManager;

    private Hashtable<String, Stat> mhtStat;
    private Hashtable<String, Skill> mhtSkill;
    private Hashtable<String, MetaSkill> mhtMetaSkill;

    private Vector<GridView> mvViews; // TODO: This really shouldn't be messed with in MainWindow

    private ViewManagerUI moViewManager;
    private JDesktopPane moDesktop;

    private MenuCombiner moCombiner;

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

    public MainWindow() {
        super();

        mvLabors = new Vector<Labor>();
        mvLaborGroups = new Vector<LaborGroup>();
        moDesktop = new JDesktopPane();

        loadPreferences();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

        try {
            readFiles();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to read at least one critical file.");
        }

        // Combine exclusions from preferences with data from file
        setExclusionsActive();

        // Create rules editor (hidden until shown)
        moRulesEditor = new RulesEditorUI(mvLabors);

        // Create exclusions manager (hidden until shown)
        moExclusionManager = new ExclusionPanel(moIO);

        // Create view manager (hidden until shown)
        moViewManager = new ViewManagerUI();

        // Create dwarf list window
        moDwarfListWindow = new DwarfListWindow(mvLabors, mhtStat, mhtSkill
                , mhtMetaSkill, mvLaborGroups, mvViews);
        moDwarfListWindow.loadData(mvDwarves, mvExclusions);
        moExclusionManager.getAppliedBroadcaster().addListener(
                moDwarfListWindow); // Listen for exclusions applied
        moDwarfListWindow.getBroadcaster().addListener(this);

        try {
            // Display a grid of the jobs to assign
            moJobListPanel = new JobListPanel(mvLabors
                    , mvLaborGroups, moJobBlacklist, moIO);
            createChoosers();   // (Must be done after initializing JobListPanel)

            // Use JobListPanel to create main menu
            MenuCombiner.MenuInfo menuInfo = createMenu(moJobListPanel);
            this.setJMenuBar(menuInfo.getMenuBar());
            moCombiner = new MenuCombiner(menuInfo); // Must be done after createMenu
            //MenuMnemonicsSetter.setMnemonics(this.getJMenuBar());

            // Create frame maps and internal frames
            // Must be done after MenuCombiner is created
            mhmInternalFrames = createFrameMap();
            for (String key : mhmInternalFrames.keySet()) {
                MyAbstractInternalFrame frame = mhmInternalFrames.get(key);
                //System.out.println("Creating frame " + key);
                if (frame instanceof MyAbstractMenuFrame) {
                    MyAbstractMenuFrame menuFrame = (MyAbstractMenuFrame) frame;
                    menuFrame.create(moCombiner);
                }
                else {
                    frame.create();
                }
            }

            int width = (int) (moJobListPanel.getPreferredSize().getWidth() * 1.2);
            int height = (int) (moJobListPanel.getPreferredSize().getHeight() * 1.42);
            moDesktop.setPreferredSize(new Dimension(width, height));

            this.setLayout(new BorderLayout());
            this.add(moDesktop, BorderLayout.CENTER);

            this.setTitle("Dwarf Organizer");
            this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // WindowConstants.DISPOSE_ON_CLOSE
            this.pack();
            this.setVisible(true);

            // Dwarf List on top (must be done after setting main window visible)
            getInternalFrame(INTERNAL_DWARF_LIST).setSelected(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private HashMap<String, MyAbstractInternalFrame> createFrameMap() {
        HashMap<String, MyAbstractInternalFrame> map
                = new HashMap<String, MyAbstractInternalFrame>(
                NUM_INTERNAL_FRAMES);

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
        return map;
    }
    private class AboutScreen extends JFrame {
        public AboutScreen(MainWindow main) {
            JLabel lblVersion = new JLabel("Dwarf Organizer version " + VERSION);

            JTextArea txtLicense = new JTextArea(moIO.getLicense());
            txtLicense.setEditable(false);
            txtLicense.setLineWrap(true);
            txtLicense.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(txtLicense);

            this.setTitle("Dwarf Organizer License");
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
        if (! moDwarfListWindow.exit()) {
            return;
        }

        String[] windowList = new String[] { INTERNAL_VIEW_MANAGER
                , INTERNAL_RULES_EDITOR, INTERNAL_EXCLUSIONS };
        for (String key : windowList) {
            MyHandyWindow.clickClose(getInternalFrame(key));
        }

        // Destroy "about" screen
        moAboutScreen.dispose();

        // Save preferences
        savePreferences();

        this.dispose();
    }
    private JInternalFrame getInternalFrame(String key) {
        return mhmInternalFrames.get(key).getInternalFrame();
    }

    // Returns a JMenuBar made of the menus in the first menu bar, followed
    // by the menus in the second menu bar.
    private JMenuBar appendMenuBar(JMenuBar menuBar1, JMenuBar menuBar2) {
        JMenuBar jmbReturn = new JMenuBar();
        int firstMenuCount = menuBar1.getMenuCount();

        // (We run the loops backwards because getMenuCount() decrements
        // each time we add a menu to jmbReturn.)
        for (int iCount = menuBar1.getMenuCount() - 1; iCount >= 0; iCount--)
            jmbReturn.add(menuBar1.getMenu(iCount), 0);
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

        public JInternalFrame createInternalFrame(JDesktopPane desktop
                , String title
                , boolean resizable, boolean closable, boolean maximizable
                , boolean iconifiable, int closeBehavior) { //, JMenuBar jMenuBar) {

            final JInternalFrame frameToCreate;

            Container uiObject = createUIObject();
            addListeners();

            frameToCreate = new JInternalFrame(title, resizable, closable
                    , maximizable, iconifiable);
            frameToCreate.setDefaultCloseOperation(closeBehavior);
            //frameToCreate.setJMenuBar(jMenuBar);
            frameToCreate.setLayout(new BorderLayout());
            frameToCreate.add(uiObject);
            //frameToCreate.pack();  <-Done by Window menu

            desktop.add(frameToCreate);

            return frameToCreate;
        }
    }
    private abstract class AbstractEditorFrameCreator
            extends AbstractFrameCreator {

        public abstract DirtyForm getDirtyForm();

        public JInternalFrame createInternalFrame(JDesktopPane desktop
                , String cleanTitle
                , boolean resizable, boolean closable, boolean maximizable
                , boolean iconifiable, int closeBehavior
                , String dirtyTitle, FrameClosingFunction fcf) { //, JMenuBar jMenuBar

            JInternalFrame frameToCreate = super.createInternalFrame(desktop
                    , cleanTitle, resizable, closable
                    , maximizable, iconifiable, closeBehavior); //, jMenuBar);

            // Update title when dirty state changes
            DirtyListener dirtyListener = createDirtyListener(
                dirtyTitle, cleanTitle, frameToCreate);
            getDirtyForm().addDirtyListener(dirtyListener);
            frameToCreate.addInternalFrameListener(
                    new InternalFrameClosingAdapter(fcf));

            return frameToCreate;
        }
    }

    private Point getCenteringPoint(JDesktopPane desktop
            , JInternalFrame frame) {

        Dimension desktopSize = desktop.getSize();
        Dimension frameSize = frame.getSize();
        return new Point((desktopSize.width - frameSize.width) / 2
                , (desktopSize.height - frameSize.height) / 2);
    }
    private void doViewMgrWindowClosing(MainWindow mainWindow
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
    private void saveViews(Vector<GridView> views) {
        moIO.writeViews(views);
        mvViews = views;            // Update local copy
        // (Don't set anything clean here since we don't know which window
        // initiated the save.)
    }

    private interface FrameClosingFunction {
        public void doFrameClosing(InternalFrameEvent e);
    }
    private class InternalFrameClosingAdapter implements InternalFrameListener {
        private FrameClosingFunction f;
        public InternalFrameClosingAdapter(FrameClosingFunction f) {
            this.f = f;
        }
        @Override
        public void internalFrameClosing(InternalFrameEvent e) {
            f.doFrameClosing(e);
        }
        @Override
        public void internalFrameOpened(InternalFrameEvent e) {}
        @Override
        public void internalFrameClosed(InternalFrameEvent e) {}
        @Override
        public void internalFrameIconified(InternalFrameEvent e) {}
        @Override
        public void internalFrameDeiconified(InternalFrameEvent e) {}
        @Override
        public void internalFrameActivated(InternalFrameEvent e) {}
        @Override
        public void internalFrameDeactivated(InternalFrameEvent e) {}
    }

    private DirtyListener createDirtyListener(final String dirtyTitle
            , final String cleanTitle, final JInternalFrame frame) {
        return new DirtyListener(){
            @Override
            public void dirtyChanged(boolean newDirtyState) {
                String strTitle;

                // If dirty
                if (newDirtyState)
                    strTitle = dirtyTitle;
                else
                    strTitle = cleanTitle;

                frame.setTitle(strTitle);
            }
        };
    }

    private interface ConfirmFunction { public void doConfirm(); }
    private void doWindowClosing(MainWindow main, DirtyForm editor
            , ConfirmFunction cf, String strMessageTitle, String strQuestion) {
        if (editor.isDirty()) {
            MyHandyOptionPane optionPane = new MyHandyOptionPane();
            Object[] options = { "Yes", "No" };
            Object result = optionPane.yesNoDialog(main
                    , options, "Yes", ""
                    , strQuestion
                    , strMessageTitle);
            if ("Yes".equals(result.toString()))
                cf.doConfirm();
            else    // "No"
                // TODO this makes me uncomfortable. The form has dirty data on
                // it.
                // But if we don't set it to clean here, then the user could be
                // asked again on app shutdown whether to save.
                editor.setClean();
        }
    }
    private void doRulesWindowClosing(MainWindow main
            , final RulesEditorUI rulesEditor) {

        doWindowClosing(main, rulesEditor, new ConfirmFunction() {
            @Override
            public void doConfirm() {
                saveRuleFile(rulesEditor);
            }
        }, "Save rules?", "Would you like to save your changes to rules?");
    }
    private void doExclWindowClosing(MainWindow main
            , final ExclusionPanel exclMgr) {

        doWindowClosing(main, exclMgr, new ConfirmFunction() {
            @Override
            public void doConfirm() {
                saveExclusions(exclMgr);
            }
        }, "Save exclusions?", "Would you like to save and apply your changes"
                + " to exclusions?");
    }
    private void saveExclusions(ExclusionPanel exclMgr) {
        exclMgr.saveExclusions();       // Also notifies Dwarf List, and sets clean
    }

    private void saveRuleFile(RulesEditorUI rulesEditor) {
        moIO.writeRuleFile(rulesEditor.getCurrentFile());
        rulesEditor.setClean();

        // Recreate local blacklist structures & resend blacklist to Job Settings screen
        setBlacklistStructures();
        moJobListPanel.setBlacklist(moJobBlacklist);
    }

    private void closeRules(RulesEditorUI rulesEditor) {
        doRulesWindowClosing(this, rulesEditor);
        getInternalFrame(INTERNAL_RULES_EDITOR).setVisible(false);
    }

    private void closeExclusions() {
        doExclWindowClosing(this, moExclusionManager);
        //mitlExclusions.setVisible(false);
        getInternalFrame(INTERNAL_EXCLUSIONS).setVisible(false);
    }
    private void closeViewManager() {
        doViewMgrWindowClosing(this, moViewManager);
        getInternalFrame(INTERNAL_VIEW_MANAGER).setVisible(false);
        //mitlViewManager.setVisible(false);
    }

    // Creates file choosers for save and load operations. This is a time-consuming
    // operation and only needs to be done once.
    private void createChoosers() {
        // File chooser for Job Settings->Save
        mjfcSave = new MyFileChooser(this); //JFileChooser();
        mjfcSave.setDialogTitle("Save Job Settings");
        mjfcSave.setDialogType(MyFileChooser.SAVE_DIALOG);
        FileFilter ffText = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String ext = getExtension(pathname.getName());
                return ext.toUpperCase().equals("TXT");
            }
            @Override
            public String getDescription() {
                return "Dwarf Organizer text files (.txt)";
            }
        };
        mjfcSave.setAcceptAllFileFilterUsed(true);
        mjfcSave.setFileFilter(ffText);
        mjfcSave.setCurrentDirectory(moJobListPanel.getDirectory());

        //System.out.println(mjfcSave.getFocusTraversalPolicy().getFirstComponent(mjfcSave.getFocusCycleRootAncestor()).getName());
//        System.out.println(mjfcSave.getFocusTraversalPolicy().getInitialComponent(this));
        //new MyFileChooser(this, mjfcSave);
        //MyFileChooser fc = new MyFileChooser();
        //fc.focusFileNameWhenShown(this, mjfcSave);

        // File chooser for Job Settings->Open...
        mjfcOpen = new MyFileChooser(this);
        mjfcOpen.setDialogTitle("Load Job Settings");
        mjfcOpen.setDialogType(MyFileChooser.OPEN_DIALOG);
        mjfcOpen.setCurrentDirectory(moJobListPanel.getDirectory());

        // File chooser for Dwarves.xml
        File file = new File(mstrDwarvesXML);
        mjfcDwarves = new MyFileChooser(this);
        mjfcDwarves.setDialogTitle("Select location of Dwarves.xml");
        mjfcDwarves.setCurrentDirectory(file);
        mjfcDwarves.setSelectedFile(file);
    }

    private void readFiles() throws Exception {
        // Try to read group-list.txt, labor-list.txt, rules.txt, Dwarves.xml,
        // and exclusions
        try {
            mvLaborGroups = moIO.readLaborGroups();
            mvLabors = moIO.readLabors();           // Read labor-list.txt

            moIO.readRuleFile();
            setBlacklistStructures();

            readDwarves();

            mvExclusions = moIO.readExclusions(mvDwarves); // mhtActiveExclusions

            mvViews = moIO.readViews();
        } catch (Exception e) {
            throw e;
        }
    }
    private void setBlacklistStructures() {
        moJobBlacklist = moIO.getBlacklist();
        moJobWhitelist = moIO.getWhitelist();

        // (Post-processing must be done after mvLabors is set)
        addWhitelistToBlacklist(moJobBlacklist, moJobWhitelist, mvLabors);
    }
    private ActionListener createShowListener(final String frameKey) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getInternalFrame(frameKey).setSelected(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        };
    }
    private ActionListener createShowOrLoadListener(final String frameKey
            , final DataLoader dataLoader) {

        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOrLoad(getInternalFrame(frameKey), dataLoader);
            }
        };
    }
    private SwingWorker createOptimizeWorker(final JobListPanel jobListPanel) {
        return new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    // Optimizer
                    Vector<Job> vJobs = jobListPanel.getJobs();
                    Vector<Dwarf> vDwarves = getDwarves();

                    setBalancedPotentials(vDwarves, vJobs);
                    JobOptimizer opt = new JobOptimizer(vJobs, vDwarves
                            , moJobBlacklist);
                    return opt.optimize();
                }
            };
    }
    private ActionListener createOptimizeAL(final JMenuItem optimizeItem
            , final JobListPanel jobListPanel) {

        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Disable the menu item while running
                optimizeItem.setEnabled(false);

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
            public void actionPerformed(ActionEvent e) {
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
            Vector<LaborRule> vRulesClone = DeepCloneUtils.deepClone(
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
            Vector<Exclusion> exclDeepClone = DeepCloneUtils.deepClone(
                    mvExclusions);
            moExclusionManager.loadData(exclDeepClone, mvDwarves);
        }
    }
    private ActionListener createTutorialAL() {
        return new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // TODO: Find out if this fix worked for users
                    // (for Java 6 Desktop non-implementation on some Linux builds)
                    //java.awt.Desktop.getDesktop().open(new File(TUTORIAL_FILE));
                    BareBonesBrowserLaunch.openURL(new File(
                            TUTORIAL_FILE).toURI().toURL().toString());
                } catch (Exception ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(
                            Level.SEVERE, null, ex);
                    ex.printStackTrace();
                    System.err.println("Failed to open " + TUTORIAL_FILE
                            + " with default browser.");
                }
            }
        };
    }
    private ActionListener createAboutAL() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

        final ArrayList<Integer> lstReturn = new ArrayList<Integer>();

        final JMenu menu = MenuHelper.createMenu("File", FILE_MENU_MNEMONIC); // "Process", VK_P
        menuBar.add(menu);

        final JMenuItem optimizeItem = MenuHelper.createMenuItem(
                "Optimize Now!", KeyEvent.VK_O);
        optimizeItem.addActionListener(createOptimizeAL(optimizeItem
                , jobListPanel));
        addMenuItem(menu, optimizeItem, lstReturn, 80);
        // ---------------------------------------------------------------------
        addSeparator(menu, lstReturn, 90);

        JMenuItem menuItem = MenuHelper.createMenuItem("Exit", createExitAL()
                , KeyEvent.VK_X);
        addMenuItem(menu, menuItem, lstReturn, EXIT_MENU_PRIORITY);

        return lstReturn;
    }
    private ArrayList<Integer> createWindowMenu(JMenuBar menuBar) {
        final ArrayList<Integer> lstReturn = new ArrayList<Integer>();

        JMenu menu = MenuHelper.createMenu("Window", KeyEvent.VK_W);
        menuBar.add(menu);

        JMenuItem menuItem = MenuHelper.createMenuItem("Dwarf List"
                , createShowListener(INTERNAL_DWARF_LIST), KeyEvent.VK_D);
        addMenuItem(menu, menuItem, lstReturn, 10);
        menuItem = MenuHelper.createMenuItem("Job Settings"
                , createShowListener(INTERNAL_JOB_LIST), KeyEvent.VK_J);
        addMenuItem(menu, menuItem, lstReturn, 20);
        menuItem = MenuHelper.createMenuItem("Rules Editor"
                , createShowOrLoadListener(INTERNAL_RULES_EDITOR
                , new RulesLoader()), KeyEvent.VK_R);
        addMenuItem(menu, menuItem, lstReturn, 30);
        menuItem = MenuHelper.createMenuItem("Exclusion Manager"
                , createShowOrLoadListener(INTERNAL_EXCLUSIONS
                , new ExclLoader()), KeyEvent.VK_E);
        addMenuItem(menu, menuItem, lstReturn, 40);

        return lstReturn;
    }
    private ArrayList<Integer> createHelpMenu(JMenuBar menuBar) {
        final ArrayList<Integer> lstReturn = new ArrayList<Integer>();

        JMenu menu = MenuHelper.createMenu("Help", KeyEvent.VK_H);
        menuBar.add(menu);

        JMenuItem menuItem = MenuHelper.createMenuItem("Tutorial"
                , createTutorialAL(), KeyEvent.VK_T);
        addMenuItem(menu, menuItem, lstReturn, 10);
        //----------------------------------------------------------------------
        addSeparator(menu, lstReturn, 20);

        menuItem = MenuHelper.createMenuItem("About", createAboutAL()
                , KeyEvent.VK_A);
        addMenuItem(menu, menuItem, lstReturn, 30);

        return lstReturn;
    }
    private MenuCombiner.MenuInfo createMenu(final JobListPanel jobListPanel) {
        JMenuBar menuBar = new JMenuBar();

        int[] menuPriority = { 10, 80, 90 };
        ArrayList<Integer>[] menuItemPriority
                = new ArrayList[menuPriority.length];

        menuItemPriority[0] = createProcessMenu(menuBar, jobListPanel);
        menuItemPriority[1] = createWindowMenu(menuBar);
        menuItemPriority[2] = createHelpMenu(menuBar);

        return new MenuCombiner.MenuInfo(menuBar, menuPriority
                , menuItemPriority);
    }

    // Reload if invisible; show if visible.
    private void showOrLoad(JInternalFrame frame, DataLoader loader) {
        if (frame.isVisible() == false) {
            loader.loadData();
            frame.pack();
        }

        frame.setLocation(getCenteringPoint(moDesktop, frame)); // Center
        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null
                    , ex);
        }
    }

    // Set dwarves.xml location & read it
    private void setDwarves() {
        int response = setDwarvesLocation();
        if (response == MyFileChooser.APPROVE_OPTION) {
            try {
                readDwarves();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to read dwarves.xml.");
            }
            moDwarfListWindow.setVisible(false);
            moDwarfListWindow.loadData(mvDwarves, mvExclusions);
            moDwarfListWindow.setVisible(true);
        }
    }

    // Reset job settings to user defaults
    private void resetJobSettings(JobListPanel jobPanel) {
        jobPanel.load(new File(JobListPanel.MY_DEFAULT_SETTINGS_FILE));
    }

    private void loadJobSettings(JobListPanel jobListPanel) {
        int input = mjfcOpen.showOpenDialog(this);

        if (input == MyFileChooser.APPROVE_OPTION) {
            File file = mjfcOpen.getSelectedFile();
            jobListPanel.load(file);
            updateCurrentJobSettings(file);
        }
    }

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
                fileName.replace(".txt", "") + " Job Settings"); // mitlJobList
    }

    // Returns the text after (not including) the dot if the given file name
    // has an extension.
    // Returns the whole file name if there is no dot.
    private String getExtension(String fileName) {
        int dot = fileName.lastIndexOf(".");
        return fileName.substring(dot + 1);
    }

    private void saveJobSettingsAs(JobListPanel jobListPanel, boolean quickSave) {

        boolean bConfirm = false;
        File file = mfilLastFile;

        if (quickSave && (mfilLastFile != null)) {
            bConfirm = true;
        }
        else {

            int input = mjfcSave.showSaveDialog(this);

            if (input == MyFileChooser.APPROVE_OPTION) {
                file = mjfcSave.getSelectedFile();
                // If the user enters no extension, append ".txt"
                if (! fileHasExtension(file))
                    file = addExtension(file, ".txt");

                bConfirm = true;
                if (file.exists()) {
                    Object[] options = new Object[] { "Yes", "No" };
                    Object result = new MyHandyOptionPane().yesNoDialog(this
                            , options, "No", ""
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

    private boolean fileHasExtension(File file) {
        String strName = file.getName();
        //System.out.println("Extension is " + getExtension(file.getName()));
        //System.out.println(getExtension(strName).length() + " ?= " + strName.length());
        return (getExtension(strName).length() != strName.length());
    }

    private File addExtension(File file, String ext) {
        String strNewName = file.getName() + ext;
        return new File(file.getParentFile(), strNewName);
    }

    // Returns the user's input to the file dialog
    private int setDwarvesLocation() {
        int input = mjfcDwarves.showOpenDialog(this);
        if (input == MyFileChooser.APPROVE_OPTION) {
            String strSelectedLoc = mjfcDwarves.getCurrentDirectory() + "\\"
                + mjfcDwarves.getSelectedFile().getName();
            strSelectedLoc = strSelectedLoc.replace("\\", "//");
            System.out.println("Selected Dwarves.xml location: " +
                strSelectedLoc);
            //textField.setText(strSelectedLoc);
            mstrDwarvesXML = strSelectedLoc;
        }
        return input;
    }

    private void loadPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        mstrDwarvesXML = prefs.get("DwarvesXML", DEFAULT_DWARVES_XML);

        // Exclusions active
        int maxID = prefs.getInt("MaxExclusionID", 0);
        mhtActiveExclusions = new Hashtable<Integer, Boolean>();
        for (int iCount = 0; iCount <= maxID; iCount++) { // moIO.getMaxUsedExclusionID() <- not set yet!
            //System.out.println("ExclusionsActive_" + iCount + ", " + DwarfOrganizerIO.DEFAULT_EXCLUSION_ACTIVE);
            // (.active in the mvExclusions object is set later, after exclusions are read)
            boolean value = prefs.getBoolean("ExclusionsActive_" + iCount
                    , DwarfOrganizerIO.DEFAULT_EXCLUSION_ACTIVE);
            //setExclusionActive(iCount, value);
            mhtActiveExclusions.put(iCount, value);
        }
    }
    private void savePreferences() {
        //System.out.println("Saving preferences...");
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put("DwarvesXML", mstrDwarvesXML);

        // Exclusions active
        prefs.putInt("MaxExclusionID", moIO.getMaxUsedExclusionID());
        for (Integer key : mhtActiveExclusions.keySet()) {
            //System.out.println("ExclusionsActive_" + key + " " + mhtActiveExclusions.get(key));
            prefs.putBoolean("ExclusionsActive_" + key
                    , mhtActiveExclusions.get(key));
        }
        //System.out.println("    Done saving preferences.");
    }

    // Updates the initial active state of the exclusions from mhtActiveExclusions
    // Must be called after loadPreferences() and after readFiles()
    private void setExclusionsActive() {
        for (int key : mhtActiveExclusions.keySet()) {
            //System.out.println("Setting exclusion #" + key + " " + mhtActiveExclusions.get(key));
            setExclusionActive(key, mhtActiveExclusions.get(key));
        }
    }
    private void setExclusionActive(int ID, boolean active) {
        if (mvExclusions == null) {
            System.err.println("Could not set active exclusion: exclusion list is null");
            return;
        }

        for (Exclusion excl : mvExclusions) {
            if (excl.getID() == ID) {
                excl.setActive(active);
                return;
            }
        }
        // (Do nothing if there is no exclusion with this ID)
    }

    private void readDwarves() throws Exception {
        //NodeList nodes = null;
        mvDwarves = new Vector<Dwarf>();
        try {

            DwarfOrganizerIO.DwarfIO dwarfIO = new DwarfOrganizerIO.DwarfIO();
            dwarfIO.readDwarves(mstrDwarvesXML);
            mvDwarves = dwarfIO.getDwarves();
            mhtStat = dwarfIO.getStats();
            mhtSkill = dwarfIO.getSkills();
            mhtMetaSkill = dwarfIO.getMetaSkills();

        } catch (Exception e) {
            System.err.println("DwarfIO failed to read dwarves.xml");
            throw e;
        }

    }

    private Vector<Dwarf> getDwarves() {
        // Get included dwarves and reset all dwarf.time
        // TODO clone() probably isn't doing what it's supposed to
        Vector<Dwarf> vIncluded = (Vector<Dwarf>)
                moDwarfListWindow.getIncludedDwarves().clone();
        for (Dwarf dwarf : vIncluded) {
            dwarf.setTime(JobOptimizer.MAX_TIME);
        }
        return vIncluded;
    }

    // Note that balanced potentials are keyed by job name, not skill name.
    private void setBalancedPotentials(Vector<Dwarf> vDwarves, Vector<Job> vJobs) {
        for (Dwarf dwarf : vDwarves) {
            for (Job job : vJobs) {

                double dblCurrentSkillPct = ((double)
                        job.getCurrentSkillWeight()) / 100.0d;
                double dblPotentialPct = 1.0d - dblCurrentSkillPct;
                long skillLevel = 0l;
                if (null != dwarf.getSkillLevels().get(job.getSkillName()))
                    skillLevel = dwarf.getSkillLevels().get(job.getSkillName());

                double dblBalancedPotential = (dblCurrentSkillPct
                        * skillLevelToPercent(skillLevel))
                        + (dblPotentialPct
                        * ((double) dwarf.skillPotentials.get(
                        job.getSkillName())));

                dwarf.balancedPotentials.put(job.getName()
                        , Math.round(dblBalancedPotential));
            }
        }
    }
    private double skillLevelToPercent(long skillLevel) {

        if (skillLevel >= MAX_SKILL_LEVEL)
            return 100.0d;
        else
            return ((double) skillLevel) * 100.0d / ((double) MAX_SKILL_LEVEL);

    }

    private void addWhitelistToBlacklist(JobBlacklist blacklist
            , JobList whitelist, Vector<Labor> labors) {
        for (String wlJobName : whitelist.keySet()) {
            for (Labor labor : labors) {
                // Add all non-whitelisted jobs to the blacklist.
                if (! wlJobName.equals(labor.getName())) {
                    if (! whitelist.get(wlJobName).contains(labor.getName()))
                        blacklist.addOneWayEntry(wlJobName, labor.getName());
                }
            }
        }
    }

    // For receiving broadcast messages
    @Override
    public void broadcast(BroadcastMessage message) {
        //System.out.println("Broadcast message received");
        if (message.getSource().equals("ExclusionPanelDefaultButton")) {
            //setDefaultButton(message, mitlExclusions);
            setDefaultButton(message
                    , getInternalFrame(INTERNAL_EXCLUSIONS));
        }
        else if (message.getSource().equals("RulesEditorDefaultButton")) {
            //setDefaultButton(message, mitlRulesEditor);
            setDefaultButton(message, getInternalFrame(INTERNAL_RULES_EDITOR));
        }
        else if (message.getSource().equals("ViewManagerDefaultButton")) {
            setDefaultButton(message, getInternalFrame(INTERNAL_VIEW_MANAGER)); //mitlViewManager);
        }
        else if (message.getSource().equals("ExclusionPanelActiveExclusions")) {
            try {
                mhtActiveExclusions = (Hashtable<Integer, Boolean>) message.getTarget();
            } catch (Exception e) {
                System.err.println(e.getMessage() + " Failed to set active exclusions");
            }
        }
        else if (message.getSource().equals("CloseExclusions")) {
            closeExclusions();
        }
        else if (message.getSource().equals("ExclusionsApplied")) {
            Vector<Exclusion> colExclusion
                    = (Vector<Exclusion>) message.getTarget();
            updateActiveExclusions(colExclusion);
        }
        else if (message.getSource().equals("DwarfListSaveViews")) {
            List<GridView> views = (List<GridView>) message.getTarget();
            saveViews(new Vector(views));
        }
        else if (message.getSource().equals("DwarfListManageViews")) {
            loadViewManager();
        }
        else if (message.getSource().equals("ViewManagerSave")) {
            saveViewManagerViews();
        }
        else if (message.getSource().equals("ViewManagerClose"))
            closeViewManager();
        else if (message.getSource().equals("ViewManagerRequestFocus")) {
            System.out.println("ViewMgrReqFoc");
            final JComponent comp = (JComponent) message.getTarget();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Requesting focus");
                    comp.requestFocusInWindow();
                }
            });
        }
        else
            System.out.println("[MainWindow] Unknown broadcast message received");
    }
    private void saveViewManagerViews() {
        Vector<GridView> vView = moViewManager.getViews();
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
                        Vector<GridView> vViewClone = DeepCloneUtils.deepClone(
                                mvViews);
                        moViewManager.loadData(vViewClone);
                    }
                };
        //showOrLoad(mitlViewManager, loader);
        showOrLoad(getInternalFrame(INTERNAL_VIEW_MANAGER), loader);
    }

    // Attempts to set the default button in the given internal frame
    // to the JButton object in the message target.
    private void setDefaultButton(BroadcastMessage message
            , JInternalFrame frame) {

        // Set default button
        try {
            if (message.getTarget() == null)
                frame.getRootPane().setDefaultButton(null);
            else {
                JButton btn = (JButton) message.getTarget();
                if (! btn.equals(frame.getRootPane().getDefaultButton()))
                    setDefaultButton(btn, frame);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage() + " Failed to set default button");
        }
    }
    private void setDefaultButton(JButton btn, JInternalFrame frame) {
        frame.getRootPane().setDefaultButton(btn);
    }
    private void updateActiveExclusions(Vector<Exclusion> colExclusion) {

        // Rebuild mhtActiveExclusions
        mhtActiveExclusions = new Hashtable<Integer, Boolean>();
        for (Exclusion excl : colExclusion) {
            mhtActiveExclusions.put(excl.getID(), excl.isActive());
        }
        mvExclusions = colExclusion;
    }
    private abstract class MyAbstractInternalFrame {
        private JInternalFrame internalFrame;
        private MainWindow mainWindow;

        private MyAbstractInternalFrame(MainWindow mainWindow) {
            this.mainWindow = mainWindow;
        }
        public JInternalFrame getInternalFrame() {
            return internalFrame;
        }

        public void setInternalFrame(JInternalFrame internalFrame) {
            this.internalFrame = internalFrame;
        }

        public MainWindow getMainWindow() {
            return mainWindow;
        }

        public abstract JInternalFrame createFrame();

        public JInternalFrame create() {
            JInternalFrame frame = createFrame();
            setInternalFrame(frame);
            return frame;
        }
        // TODO: Close, save(?), and load could be options
    }
    private abstract class MyAbstractMenuFrame extends MyAbstractInternalFrame {
        private MenuCombiner.MenuInfo menuInfo;

        private MyAbstractMenuFrame(MainWindow mainWindow) {
            super(mainWindow);
        }

        public abstract MenuCombiner.MenuInfo createMenuInfo();

        public MenuCombiner.MenuInfo getMenuInfo() {
            return menuInfo;
        }

        public JInternalFrame create(MenuCombiner combiner) {
            JInternalFrame frame = super.create();
            this.menuInfo = createMenuInfo();
            addCombiner(frame, combiner);
            return frame;
        }

        // Called by create()
        private void addCombiner(JInternalFrame frame
                , MenuCombiner combiner) {

            if (menuInfo.getMenuBar().getMenuCount() > 0) {
                InternalFrameListener listener
                        = combiner.createInternalFrameListener(getMainWindow()
                        , getMenuInfo());
                frame.addInternalFrameListener(listener);
            }
        }
    }
    private abstract class MyAbstractSimpleFrame
            extends MyAbstractMenuFrame {

        private JDesktopPane desktop;
        private Container ui;
        private String title;

        private MyAbstractSimpleFrame(MainWindow main, JDesktopPane desktop
                , Container ui, String title) { // , JMenuBar menuBar

            super(main);
            this.desktop = desktop;
            this.ui = ui;
            this.title = title;
        }

        @Override
        public JInternalFrame createFrame() {
            return createAlwaysShownFrame(desktop, ui, title); //, menuBar);
        }
        private JInternalFrame createAlwaysShownFrame(JDesktopPane desktop
                , final Container ui, String title) { //, JMenuBar menuBar) {
            AbstractFrameCreator creator = new AbstractFrameCreator() {
                @Override
                public Container createUIObject() {
                    return ui;
                }

                @Override
                public void addListeners() { // Do nothing
                }
            };
            JInternalFrame frame = creator.createInternalFrame(desktop
                    , title
                    , true, false, true, true
                    , WindowConstants.DISPOSE_ON_CLOSE); //, menuBar);
            frame.pack();
            frame.setVisible(true);
            return frame;
        }
    }
    private JInternalFrame createEditorFrame(JDesktopPane desktop
            , final Container ui, String cleanTitle
            , final DirtyForm dirtyForm, final ListenerAdder la
            , String dirtyTitle, FrameClosingFunction fcf) {

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

        JInternalFrame frame = creator.createInternalFrame(desktop
                , cleanTitle, true, true, true, true
                , WindowConstants.HIDE_ON_CLOSE
                , dirtyTitle, fcf);

        return frame;
    }
    private class ExclusionFrame extends MyAbstractMenuFrame {
        private JDesktopPane desktop;
        private ExclusionPanel exclusionPanel;

        public ExclusionFrame(MainWindow main, JDesktopPane desktop
                , ExclusionPanel exclusionPanel) {
            super(main);
            this.desktop = desktop;
            this.exclusionPanel = exclusionPanel;
        }

        @Override
        public JInternalFrame createFrame() {
            ListenerAdder la = new ListenerAdder() {
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
            FrameClosingFunction fcf = new FrameClosingFunction() {
                @Override
                public void doFrameClosing(InternalFrameEvent e) {
                    doExclWindowClosing(getMainWindow(), exclusionPanel);
                }
            };

            setInternalFrame(createEditorFrame(desktop, exclusionPanel
                    , EXCLUSIONS_TITLE
                    , exclusionPanel, la, EXCLUSIONS_TITLE_DIRTY, fcf)); //, createExclusionMgrMenu()
            return getInternalFrame();
        }
        private ActionListener createSaveAL() {
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    exclusionPanel.saveExclusions();
                }
            };
        }
        private ActionListener createCloseAL() {
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    closeExclusions();
                }
            };
        }
        private ArrayList<Integer> createFileMenu(final JMenuBar menuBar) {
            final int SAVE_PRIO = 31;
            final int SEP_PRIO = 62;
            final int EXIT_PRIO = EXIT_MENU_PRIORITY - 1;

            final ArrayList<Integer> aReturn = new ArrayList<Integer>();

            JMenu menu = MenuHelper.createMenu("File", FILE_MENU_MNEMONIC);
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

            JMenuBar menuBar = new JMenuBar();

            menuItemPriority[0] = createFileMenu(menuBar);

            return new MenuCombiner.MenuInfo(menuBar, menuPriority
                    , menuItemPriority);
        }
    }
    private class RulesEditorFrame extends MyAbstractMenuFrame {
        private JDesktopPane desktop;
        private RulesEditorUI rulesEditor;

        public RulesEditorFrame(MainWindow main, JDesktopPane desktop
                , RulesEditorUI rulesEditor) {
            super(main);
            this.desktop = desktop;
            this.rulesEditor = rulesEditor;
        }

        @Override
        public JInternalFrame createFrame() {
            ListenerAdder la = new ListenerAdder() {
                @Override
                public void addListeners() {
                    rulesEditor.getDefaultButtonBroadcaster().addListener(
                            getMainWindow());
                }
            };
            FrameClosingFunction fcf = new FrameClosingFunction() {
                @Override
                public void doFrameClosing(InternalFrameEvent e) {
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
                public void actionPerformed(ActionEvent e) {
                    saveRuleFile(rulesEditor);
                }
            };
        }
        private ActionListener createCloseAL() {
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    closeRules(rulesEditor);
                }
            };
        }
        private ArrayList<Integer> createFileMenu(final JMenuBar menuBar) {
            final int SAVE_PRIO = 31;
            final int SEP_PRIO = 62;
            final int CLOSE_PRIO = EXIT_MENU_PRIORITY - 1;

            final ArrayList<Integer> lstPriority = new ArrayList<Integer>();

            final JMenu menu = MenuHelper.createMenu("File", FILE_MENU_MNEMONIC);
            menuBar.add(menu);

            JMenuItem menuItem = MenuHelper.createMenuItem("Save", createSaveAL()
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

            JMenuBar menuBar = new JMenuBar();

            menuItemPriority[0] = createFileMenu(menuBar);

            return new MenuCombiner.MenuInfo(menuBar, menuPriority
                    , menuItemPriority);
        }
    }
    private class ViewManagerFrame extends MyAbstractInternalFrame {
        private JDesktopPane desktop;
        private ViewManagerUI viewManagerUI;

        public ViewManagerFrame(MainWindow mainWindow, JDesktopPane desktop
                , ViewManagerUI viewManagerUI) {
            super(mainWindow);
            this.desktop = desktop;
            this.viewManagerUI = viewManagerUI;
        }

        @Override
        public JInternalFrame createFrame() {
            ListenerAdder la = new ListenerAdder() {
                @Override
                public void addListeners() {
                    viewManagerUI.getBroadcaster().addListener(getMainWindow());
                }
            };
            FrameClosingFunction fcf = new FrameClosingFunction() {
                @Override
                public void doFrameClosing(InternalFrameEvent e) {
                    doViewMgrWindowClosing(getMainWindow(), viewManagerUI);
                }
            };

            JInternalFrame frame = createEditorFrame(desktop
                    , viewManagerUI.getUIPanel(), VIEW_MGR_TITLE
                    , viewManagerUI, la, VIEW_MGR_TITLE_DIRTY, fcf);

            setDefaultButton(viewManagerUI.getDefaultButton(), frame);
            setInternalFrame(frame);
            return getInternalFrame();
        }
    }
    private class DwarfListFrame extends MyAbstractSimpleFrame {
        private DwarfListWindow dwarfListWindow;

        public DwarfListFrame(MainWindow mainWindow, JDesktopPane desktop
                , DwarfListWindow dwarfListWindow, String title) {

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
        private ArrayList<Integer>[] createPriorities(JMenuBar menuBar) {
            final int NUM_MENUS = menuBar.getMenuCount();
            ArrayList<Integer>[] menuItemPriority = new ArrayList[NUM_MENUS];

            final int STARTING_PRIO = 51;
            for (int iCount = 0; iCount < NUM_MENUS; iCount++) {
                final JMenu menu = menuBar.getMenu(iCount);
                final int numItems = menu.getMenuComponentCount();
                ArrayList<Integer> lstItem = new ArrayList<Integer>(numItems);
                int priority = STARTING_PRIO;
                for (int jCount = 0; jCount < numItems; jCount++) {
                    lstItem.add(priority++);
                }
                menuItemPriority[iCount] = lstItem;
            }
            return menuItemPriority;
        }
        private void createFileMenu(JMenuBar menuBar) {
            JMenu menu = MenuHelper.createMenu("File", FILE_MENU_MNEMONIC);
            menuBar.add(menu);

            menu.add(MenuHelper.createMenuItem("Set Location of Dwarves.xml..."
                    , createSetLocListener()
                    , KeyStroke.getKeyStroke(KeyEvent.VK_L
                    , ActionEvent.CTRL_MASK)));
        }
        private ActionListener createSetLocListener() {
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDwarves();
                }
            };
        }
    }
    private class JobListFrame extends MyAbstractSimpleFrame {
        private JobListPanel jobListPanel;

        public JobListFrame(MainWindow mainWindow, JDesktopPane desktop
                , JobListPanel jobListPanel
                , String title) {

            super(mainWindow, desktop, jobListPanel, title);

            this.jobListPanel = jobListPanel;
        }

        @Override
        public MenuCombiner.MenuInfo createMenuInfo() {
            final int NUM_MENUS = 2;
            int[] menuPriority = new int[] { 1, 2 };
            ArrayList<Integer>[] menuItemPriority = new ArrayList[NUM_MENUS];

            JMenuBar menuBar = new JMenuBar();

            menuItemPriority[0] = createFileMenu(menuBar);
            menuItemPriority[1] = createEditMenu(menuBar);

            return new MenuCombiner.MenuInfo(menuBar, menuPriority
                    , menuItemPriority);
        }
        private ActionListener createLoadActionListener() {
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loadJobSettings(jobListPanel);
                }
            };
        }
        private ActionListener createSaveActionListener(
                final boolean quickSave) {

            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveJobSettingsAs(jobListPanel, quickSave);
                }
            };
        }
        private ActionListener createResetActionListener() {
            return new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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
            ArrayList<Integer> lstReturn = new ArrayList<Integer>();

            JMenu menu = MenuHelper.createMenu("Edit", KeyEvent.VK_E);
            menuBar.add(menu);

            menu = jobListPanel.createEditMenuItems(menu);
            lstReturn.addAll(createMenuItemPriority(menu));

            return lstReturn;
        }
        private ArrayList<Integer> createMenuItemPriority(final JMenu menu) {
            final int NUM_ITEMS = menu.getMenuComponentCount();
            ArrayList<Integer> list = new ArrayList<Integer>(NUM_ITEMS);

            int priority = 51;
            for (int iCount = 0; iCount < NUM_ITEMS; iCount++) {
                list.add(priority++);
            }
            return list;
        }
    }
}