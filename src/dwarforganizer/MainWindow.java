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
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.filechooser.FileFilter;
import myutils.Adapters.WindowClosingAdapter;
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

    private JInternalFrame mitlJobList;
    private JInternalFrame mitlDwarfList;
    private JInternalFrame mitlRulesEditor;
    private JInternalFrame mitlExclusions;

    private DwarfListWindow moDwarfListWindow;
    private JobListPanel moJobListPanel;
    private RulesEditorUI moRulesEditor;
    //private Vector<KeyStroke> mvJobListAccelerators;

    protected static enum JobListMenuAccelerator {
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

    private String mstrDwarvesXML = DEFAULT_DWARVES_XML;
    private File mfilLastFile;
    private MyFileChooser mjfcSave; //= new JFileChooser(); JFileChooser
    private MyFileChooser mjfcOpen; //= new JFileChooser();
    private MyFileChooser mjfcDwarves;

    private Vector<Labor> mvLabors;
    private Vector<LaborGroup> mvLaborGroups;

    private static final String RULES_EDITOR_TITLE_CLEAN = "Edit Rules";
    private static final String RULES_EDITOR_TITLE_DIRTY = "Edit Rules (Unsaved Changes)";
    private JobBlacklist moJobBlacklist = new JobBlacklist();
    private JobList moJobWhitelist = new JobList();
    //private DirtyListener moRuleDirtyListener;

    private DwarfOrganizerIO moIO = new DwarfOrganizerIO();
    private Vector<Dwarf> mvDwarves;
    private DeepCloneableVector<Exclusion> mvExclusions;
    private Hashtable<Integer, Boolean> mhtActiveExclusions;

    private JFrame moAboutScreen = new AboutScreen(this);

    private static final String EXCLUSIONS_TITLE = "Manage Exclusions";
    private static final String EXCLUSIONS_TITLE_DIRTY = EXCLUSIONS_TITLE + " (Unsaved Changes)";

    private ExclusionPanel moExclusionManager;

    private Hashtable<String, Stat> mhtStat;
    private Hashtable<String, Skill> mhtSkill;
    private Hashtable<String, MetaSkill> mhtMetaSkill;

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

    public MainWindow() {
        super();

        mvLabors = new Vector<Labor>();
        mvLaborGroups = new Vector<LaborGroup>();

        JDesktopPane desktop = new JDesktopPane();
        //this.getContentPane().add(desktop);

        loadPreferences();
        this.addWindowListener(new WindowClosingAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Prompts to save changes
                MyHandyWindow.clickClose(mitlRulesEditor);
                MyHandyWindow.clickClose(mitlExclusions);

                // Destroy "about" screen
                moAboutScreen.dispose();

                // Save preferences
                savePreferences();
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
        createRulesEditorScreen(desktop);

        // Create exclusions manager (hidden until shown)
        createExclusionScreen(desktop);

        // Create dwarf list window
        moDwarfListWindow = new DwarfListWindow(mvLabors, mhtStat, mhtSkill
                , mhtMetaSkill, mvLaborGroups);
        //if (mvDwarves == null) System.out.println("mvDwarves is null");
        moDwarfListWindow.loadData(mvDwarves, mvExclusions);
        moExclusionManager.getAppliedBroadcaster().addListener(moDwarfListWindow); // Listen for exclusions applied

        try {
            // Display a grid of the dwarves
            mitlDwarfList = new JInternalFrame("Dwarf List", true
                    , false, true, true);
            mitlDwarfList.setLayout(new BorderLayout());
            mitlDwarfList.add(moDwarfListWindow);
            updateDwarfListMenu();

            mitlDwarfList.pack();
            mitlDwarfList.setVisible(true);
            desktop.add(mitlDwarfList);

            // Display a grid of the jobs to assign
            moJobListPanel = new JobListPanel(mvLabors
                    , mvLaborGroups, moJobBlacklist, moIO);   // final JobListPanel jobListPanel
            createChoosers();   // (Must be done after initializing JobListPanel)
            mitlJobList = new JInternalFrame("Job Settings", true, false, true, true);
            mitlJobList.setJMenuBar(createJobListMenu(moJobListPanel));
            mitlJobList.setLayout(new BorderLayout());
            mitlJobList.add(moJobListPanel);
            mitlJobList.pack();
            mitlJobList.setVisible(true);
            desktop.add(mitlJobList);
            int width = (int) (moJobListPanel.getPreferredSize().getWidth() * 1.2);
            int height = (int) (moJobListPanel.getPreferredSize().getHeight() * 1.42);
            desktop.setPreferredSize(new Dimension(width, height));

            this.setJMenuBar(createMenu(moJobListPanel));
            //MenuMnemonicsSetter.setMnemonics(this.getJMenuBar());

            this.setLayout(new BorderLayout());
            this.add(desktop);

            this.setTitle("Dwarf Organizer");
            this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            this.pack();
            this.setVisible(true);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateDwarfListMenu() {
        mitlDwarfList.setJMenuBar(appendMenuBar(createDwarfListMenuBar()
            , moDwarfListWindow.getMenu())); // mvLaborGroups
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

    private void createExclusionScreen(JDesktopPane desktop) {

        final MainWindow main = this;

        // Update title of Rules Editor when dirty state changes
        DirtyListener dirtyListener = createDirtyListener(
                EXCLUSIONS_TITLE_DIRTY, EXCLUSIONS_TITLE
                , new FrameTitleSetter() {
            @Override
            public void setFrameTitle(String title) {
                if (! title.equals(mitlExclusions.getTitle()))
                    mitlExclusions.setTitle(title);
            }
        });

        moExclusionManager = new ExclusionPanel(moIO); // moDwarfListWindow.getDwarves() // mvExclusions, mvDwarves, moIO, mhtActiveExclusions

        moExclusionManager.getDefaultButtonBroadcaster().addListener(this);
        //moExclusionManager.getExclusionActiveBroadcaster().addListener(this);
        moExclusionManager.getCloseBroadcaster().addListener(this);
        moExclusionManager.getAppliedBroadcaster().addListener(this);

        mitlExclusions = new JInternalFrame(EXCLUSIONS_TITLE, true, true, true, true);
        mitlExclusions.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        mitlExclusions.setJMenuBar(createExclusionMgrMenu());
        mitlExclusions.setLayout(new BorderLayout());
        mitlExclusions.add(moExclusionManager);
        //mitlExclusions.pack();  <-Done by Window menu now

        moExclusionManager.addDirtyListener(dirtyListener);
        mitlExclusions.addInternalFrameListener(new InternalFrameClosingAdapter(
                new FrameClosingFunction() {
            @Override
            public void doFrameClosing(InternalFrameEvent e) {
                doExclWindowClosing(main, moExclusionManager);
            }
        }));
        desktop.add(mitlExclusions);
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

    private interface FrameTitleSetter {
        public void setFrameTitle(String title);
    }
    private DirtyListener createDirtyListener(final String dirtyTitle
            , final String cleanTitle, final FrameTitleSetter fts) {
        return new DirtyListener(){
            @Override
            public void dirtyChanged(boolean newDirtyState) {
                String strTitle;

                // If dirty
                if (newDirtyState)
                    strTitle = dirtyTitle;
                else
                    strTitle = cleanTitle;

                fts.setFrameTitle(strTitle);
            }
        };
    }

    private void createRulesEditorScreen(JDesktopPane desktop) {

        final MainWindow main = this;
        moRulesEditor = new RulesEditorUI(mvLabors);
        moRulesEditor.getDefaultButtonBroadcaster().addListener(this);
        // Update title of Rules Editor when dirty state changes
        DirtyListener dirtyListener = createDirtyListener(
                RULES_EDITOR_TITLE_DIRTY, RULES_EDITOR_TITLE_CLEAN
                , new FrameTitleSetter() {

            @Override
            public void setFrameTitle(String title) {
                if (! title.equals(mitlRulesEditor.getTitle()))
                    mitlRulesEditor.setTitle(title);
            }
        });

        mitlRulesEditor = new JInternalFrame(RULES_EDITOR_TITLE_CLEAN, true, true, true
                , true);
        mitlRulesEditor.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        mitlRulesEditor.setJMenuBar(createRulesEditorMenu(moRulesEditor));
        //DefaultFocus.alwaysFocusOnActivate(mitlRulesEditor
        //        , moRulesEditor.getDefaultFocusComp());

        mitlRulesEditor.setLayout(new BorderLayout());
        mitlRulesEditor.add(moRulesEditor);
        mitlRulesEditor.pack();

        moRulesEditor.addDirtyListener(dirtyListener);
        mitlRulesEditor.addInternalFrameListener(new InternalFrameClosingAdapter(
            new FrameClosingFunction() {
            @Override
            public void doFrameClosing(InternalFrameEvent e) {
                doRulesWindowClosing(main, moRulesEditor);
            }
        }));

        desktop.add(mitlRulesEditor);
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
    private void doRulesWindowClosing(MainWindow main, final RulesEditorUI rulesEditor) {
        doWindowClosing(main, rulesEditor, new ConfirmFunction() {
            @Override
            public void doConfirm() {
                saveRuleFile(rulesEditor);
            }
        }, "Save rules?", "Would you like to save your changes?");
    }
    private void doExclWindowClosing(MainWindow main, final ExclusionPanel exclMgr) {
        doWindowClosing(main, exclMgr, new ConfirmFunction() {
            @Override
            public void doConfirm() {
                saveExclusions(exclMgr);
            }
        }, "Save exclusions?", "Would you like to save and apply your changes?");
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
    private JMenuBar createRulesEditorMenu(final RulesEditorUI rulesEditor) {
        final MainWindow main = this;
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Save", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveRuleFile(rulesEditor);
            }
        });
        menu.add(menuItem);

        // ----------------------------------
        menu.add(new JSeparator());

        // ----------------------------------
        menuItem = new JMenuItem("Close", KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeRules(rulesEditor);
            }
        });
        menu.add(menuItem);

        return menuBar;
    }

    private void closeRules(RulesEditorUI rulesEditor) {
        doRulesWindowClosing(this, rulesEditor);
        mitlRulesEditor.setVisible(false);
    }

    private JMenuBar createExclusionMgrMenu() {
        final MainWindow main = this;

        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Save and Apply");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moExclusionManager.saveExclusions();
            }
        });
        menu.add(menuItem);

        menu.add(new JSeparator());

        menuItem = new JMenuItem("Close");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                closeExclusions();
            }
        });
        menu.add(menuItem);

        return menuBar;
    }
    private void closeExclusions() {
        doExclWindowClosing(this, moExclusionManager);
        mitlExclusions.setVisible(false);
    }

    private JMenuBar createDwarfListMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Set Location of Dwarves.xml..."
                , KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDwarves();
            }
        });
        menu.add(menuItem);
        return menuBar;
    }

    private JMenuBar createJobListMenu(final JobListPanel jobListPanel) {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Open...", KeyEvent.VK_O);
        menuItem.setAccelerator(JobListMenuAccelerator.OPEN.getKeyStroke()); //  KeyStroke.getKeyStroke(
            //KeyEvent.VK_O, ActionEvent.CTRL_MASK)
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadJobSettings(jobListPanel);
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Save", KeyEvent.VK_S);
        menuItem.setAccelerator(JobListMenuAccelerator.SAVE.getKeyStroke()); // KeyStroke.getKeyStroke(
                //KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveJobSettingsAs(jobListPanel, true);
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Save As...", KeyEvent.VK_A);
        menuItem.setAccelerator(JobListMenuAccelerator.SAVE_AS.getKeyStroke()); // KeyStroke.getKeyStroke(
                //KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveJobSettingsAs(jobListPanel, false);
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Reset to My Defaults", KeyEvent.VK_R);
        //menuItem.setAccelerator(JobListMenuAccelerator.RESET.getKeyStroke()); // KeyStroke.getKeyStroke(
                //KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetJobSettings(jobListPanel);
            }
        });
        menu.add(menuItem);

        // -------------------------------------
        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);

        jobListPanel.createEditMenu(menu);

        return menuBar;

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
    private JMenuBar createMenu(final JobListPanel jobListPanel) {
        JMenuBar menuBar = new JMenuBar();

        JMenu menu = new JMenu("Process");
        menu.setMnemonic(KeyEvent.VK_P);
        menuBar.add(menu);

        final JMenuItem optimizeItem = new JMenuItem("Optimize Now!", KeyEvent.VK_O);
        optimizeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Disable the menu item while running
                optimizeItem.setEnabled(false);

                // Do this lengthy processing on a background thread, maintaining
                // UI responsiveness.
                final SwingWorker worker = new SwingWorker() {
                    @Override
                    protected Object doInBackground() throws Exception {
                        // Optimizer
                        Vector<Job> vJobs = jobListPanel.getJobs();
                        Vector<Dwarf> vDwarves = getDwarves();
                        //JobBlacklist blacklist = jobListPanel.getBlacklist();

                        setBalancedPotentials(vDwarves, vJobs);
                        JobOptimizer opt = new JobOptimizer(vJobs, vDwarves
                                , moJobBlacklist);
                        return opt.optimize();
                    }
                };
                worker.execute();

                optimizeItem.setEnabled(true);            }

        });
        menu.add(optimizeItem);

        menu.addSeparator();
        JMenuItem menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                System.exit(0);
            }
        });
        menu.add(menuItem);

        // -------------------------------
        menu = new JMenu("Window");
        menu.setMnemonic(KeyEvent.VK_W);
        menuBar.add(menu);

        // -------------------------------
        menuItem = new JMenuItem("Dwarf List", KeyEvent.VK_D);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mitlDwarfList.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu.add(menuItem);

        // -------------------------------
        menuItem = new JMenuItem("Job Settings", KeyEvent.VK_J);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    mitlJobList.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        menu.add(menuItem);

        // -------------------------------
        menuItem = new JMenuItem("Rules Editor", KeyEvent.VK_R);
        final DataLoader rulesLoader = new DataLoader() {
                    @Override
                    public void loadData() {
                        // Use a clone for the table model. Otherwise
                        // unsaved changes will persist when window is closed
                        // and reopened.
                        DeepCloneableVector<LaborRule> vRulesClone
                                = (DeepCloneableVector<LaborRule>)
                                new DeepCloneableVector<LaborRule>(
                                moIO.getRuleFileContents()).deepClone();
                        moRulesEditor.loadData(vRulesClone);
                    }
                };
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOrLoad(mitlRulesEditor, rulesLoader);
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Exclusion Manager", KeyEvent.VK_E);
        // (We need to deep-clone mvExclusions
        // and loadData on that, not on mvExclusions directly.
        // Otherwise the "active" checkbox states will carry over
        // between sessions)
        final DataLoader exclLoader = new DataLoader() {
                    @Override
                    public void loadData() {
                        DeepCloneableVector<Exclusion> exclDeepClone
                                = (DeepCloneableVector<Exclusion>)
                                mvExclusions.deepClone();
                        moExclusionManager.loadData(exclDeepClone, mvDwarves);
                    }
                };
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOrLoad(mitlExclusions, exclLoader);
            }
        });
        menu.add(menuItem);

        // -------------------------------
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(menu);

        menuItem = new JMenuItem("Tutorial", KeyEvent.VK_T);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // TODO: Find out if this fix worked for users
                    // (for Java 6 Desktop non-implementation on some Linux builds)
                    //java.awt.Desktop.getDesktop().open(new File(TUTORIAL_FILE));
                    BareBonesBrowserLaunch.openURL(new File(
                            TUTORIAL_FILE).toURI().toURL().toString());
                } catch (Exception ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                    System.err.println("Failed to open " + TUTORIAL_FILE
                            + " with default browser.");
                }
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("About", KeyEvent.VK_A);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                moAboutScreen.setVisible(true);
            }

        });
        menu.add(menuItem);

        return menuBar;
    }

    private interface DataLoader {
        public void loadData();
    }
    
    // Reload if invisible; show if visible.
    private void showOrLoad(JInternalFrame frame, DataLoader loader) {    
        if (frame.isVisible() == false) {
            loader.loadData();
            frame.pack();
        }

        frame.setVisible(true);
        try {
            frame.setSelected(true);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null
                    , ex);
        }
    }

    // More non-functional internet garbage (slow and only works for Serializable objects)
    private Object deepClone(Object source) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(source);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object deepCopy = ois.readObject();
            return deepCopy;
        } catch (IOException e) {
            System.err.println(e.getMessage() + " [MainWindow] Failed to deepClone object");
            e.printStackTrace();
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage() + " [MainWindow] Failed to deepClone object");
            e.printStackTrace();
            return null;
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
        mitlJobList.setTitle(fileName.replace(".txt", "") + " Job Settings");
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
        Vector<Dwarf> vIncluded = (Vector<Dwarf>) moDwarfListWindow.getIncludedDwarves().clone();
        for (Dwarf dwarf : vIncluded) {
            dwarf.setTime(JobOptimizer.MAX_TIME);
        }
        return vIncluded;
    }

    // Note that balanced potentials are keyed by job name, not skill name.
    private void setBalancedPotentials(Vector<Dwarf> vDwarves, Vector<Job> vJobs) {
        for (Dwarf dwarf : vDwarves) {
            for (Job job : vJobs) {

                double dblCurrentSkillPct = ((double) job.currentSkillWeight) / 100.0d;
                double dblPotentialPct = 1.0d - dblCurrentSkillPct;
                long skillLevel = 0l;
                if (null != dwarf.skillLevels.get(job.skillName))
                    skillLevel = dwarf.skillLevels.get(job.skillName);

                double dblBalancedPotential = (dblCurrentSkillPct
                        * skillLevelToPercent(skillLevel))
                        + (dblPotentialPct
                        * ((double) dwarf.skillPotentials.get(job.skillName)));

                dwarf.balancedPotentials.put(job.name
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
            setDefaultButton(message, mitlExclusions);
        }
        else if (message.getSource().equals("RulesEditorDefaultButton")) {
            setDefaultButton(message, mitlRulesEditor);
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
            DeepCloneableVector<Exclusion> colExclusion
                    = (DeepCloneableVector<Exclusion>) message.getTarget();
            updateActiveExclusions(colExclusion);
        }
        else
            System.out.println("[MainWindow] Unknown broadcast message received");
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
                    frame.getRootPane().setDefaultButton(btn);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage() + " Failed to set default button");
        }
    }
    private void updateActiveExclusions(DeepCloneableVector<Exclusion> colExclusion) {
        // Rebuild mhtActiveExclusions
        mhtActiveExclusions = new Hashtable<Integer, Boolean>();
        for (Exclusion excl : colExclusion) {
            mhtActiveExclusions.put(excl.getID(), excl.isActive());
        }
        mvExclusions = colExclusion;
    }
}
