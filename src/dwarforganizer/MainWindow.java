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
import java.awt.event.WindowListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
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
import myutils.MyHandyOptionPane;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tamara Orr
 * 
 * This software is provided under the MIT license.
 * See the included license.txt for details.
 */
public class MainWindow extends JFrame implements DirtyListener {

    private static final long MAX_SKILL_LEVEL = 20l;    // That's an "L", not a one    
    //private static final String DEFAULT_DWARVES_XML
    //        = "C://DwarfFortress//DwarfGuidanceCounselor//0.0.6//Dwarves.xml";
    private static final String DEFAULT_DWARVES_XML = "samples/dwarves/sample-7-dwarves.xml";
    private static final String TUTORIAL_FILE = "tutorial/ReadmeSlashTutorial.html";
    
    private JInternalFrame mitlJobList;
    private JInternalFrame mitlDwarfList;
    private JInternalFrame mitlRulesEditor;
    
    private DwarfListWindow moDwarfListWindow;
    private JobListPanel moJobListPanel;
    
    private String mstrDwarvesXML = DEFAULT_DWARVES_XML;
    private File mfilLastFile;
    private JFileChooser mjfcSave; //= new JFileChooser();
    private JFileChooser mjfcOpen; //= new JFileChooser();
    private JFileChooser mjfcDwarves;
    
    private Vector<Labor> mvLabors = new Vector<Labor>();
    private Vector<LaborGroup> mvLaborGroups = new Vector<LaborGroup>();
    
    private static final String RULES_EDITOR_TITLE_CLEAN = "Edit Rules";
    private static final String RULES_EDITOR_TITLE_DIRTY = "Edit Rules (Unsaved Changes)";
    private JobBlacklist moJobBlacklist = new JobBlacklist();
    private JobList moJobWhitelist = new JobList();    
    
    private MyIO moIO = new MyIO();
    
    private JFrame moAboutScreen = new AboutScreen(this);
    
    private class AboutScreen extends JFrame {
        public AboutScreen(MainWindow main) {
            JTextArea txtLicense = new JTextArea(moIO.getLicense());
            txtLicense.setEditable(false);
            txtLicense.setLineWrap(true);
            txtLicense.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(txtLicense);
            
            this.setTitle("Dwarf Organizer License");
            this.setPreferredSize(new Dimension(500, 400));
            this.setLayout(new BorderLayout());
            this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
            
            this.add(sp);
            this.pack();
            
            this.setLocationRelativeTo(main);
        }
    }
    
    public MainWindow() {
        super();
        
        JDesktopPane desktop = new JDesktopPane();
        //this.getContentPane().add(desktop);
        
        loadPreferences();
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }
            @Override
            public void windowClosing(WindowEvent e) {
                moAboutScreen.dispose();
                savePreferences();
            }
            @Override
            public void windowClosed(WindowEvent e) {
            }
            @Override
            public void windowIconified(WindowEvent e) {
            }
            @Override
            public void windowDeiconified(WindowEvent e) {
            }
            @Override
            public void windowActivated(WindowEvent e) {
            }
            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
        
        try {
            readFiles();
        } catch (Exception e) {
            //e.printStackTrace();
            System.err.println("Failed to read at least one critical file.");
        }
        
        // Create rules editor (hidden until shown)
        createRulesEditorScreen(desktop);
        
        try {
            // Display a grid of the dwarves
            /*JFrame frList = MyHandyWindow.createSimpleWindow("Dwarf List"
                    , moDwarfListWindow, new BorderLayout());
            frList.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frList.setVisible(true);            */
            mitlDwarfList = new JInternalFrame("Dwarf List", true
                    , false, true, true);
            mitlDwarfList.setLayout(new BorderLayout());
            mitlDwarfList.add(moDwarfListWindow);
            //JMenuBar dwarvesFileMenu = createDwarfListMenuBar();
            //JMenuBar desiredMenuBar = moDwarfListWindow.getMenu(mvLaborGroups);
            //mitlDwarfList.setJMenuBar(desiredMenuBar);
            updateDwarfListMenu();
            
            mitlDwarfList.pack();
            mitlDwarfList.setVisible(true);
            desktop.add(mitlDwarfList);
            
            // Display a grid of the jobs to assign
            moJobListPanel = new JobListPanel(mvLabors
                    , mvLaborGroups, moJobBlacklist);   // final JobListPanel jobListPanel
            createChoosers();   // (Must be done after initializing JobListPanel)
            //MyHandyWindow.createSimpleWindow("Job Settings"
            //            , jobListPanel, new BorderLayout());
            mitlJobList = new JInternalFrame("Job Settings", true, false, true, true);
            mitlJobList.setJMenuBar(createJobListMenu(moJobListPanel));
            mitlJobList.setLayout(new BorderLayout());
            mitlJobList.add(moJobListPanel);
            mitlJobList.pack();
            mitlJobList.setVisible(true);
            desktop.add(mitlJobList);
            int width = (int) (moJobListPanel.getPreferredSize().getWidth() * 1.2);
            int height = (int) (moJobListPanel.getPreferredSize().getHeight() * 1.2);
            desktop.setPreferredSize(new Dimension(width, height));
            
            this.setJMenuBar(createMenu(moJobListPanel));
            
            this.setLayout(new BorderLayout());
            //this.add(panOptions, BorderLayout.PAGE_START);
            //this.add(btnOptimize, BorderLayout.PAGE_START);
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
            , moDwarfListWindow.getMenu(mvLaborGroups)));        
    }
    
    // Returns a JMenuBar made of the menus in the first menu bar, followed
    // by the menus in the second menu bar.
    private JMenuBar appendMenuBar(JMenuBar menuBar1, JMenuBar menuBar2) {
        JMenuBar jmbReturn = new JMenuBar();
        for (int iCount = 0; iCount < menuBar1.getMenuCount(); iCount++)
            jmbReturn.add(menuBar1.getMenu(iCount));
        for (int iCount = 0; iCount < menuBar2.getMenuCount(); iCount++)
            jmbReturn.add(menuBar2.getMenu(iCount));
        return jmbReturn;
    }
    
    private void createRulesEditorScreen(JDesktopPane desktop) {

        final RulesEditor rulesEditor = new RulesEditor(
                moIO.getRuleFileContents(), mvLabors);
        rulesEditor.addDirtyListener(this);
        final MainWindow main = this;
        
        mitlRulesEditor = new JInternalFrame(RULES_EDITOR_TITLE_CLEAN, true, true, true
                , true);
        mitlRulesEditor.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        mitlRulesEditor.setJMenuBar(createRulesEditorMenu(rulesEditor));
        
        mitlRulesEditor.setLayout(new BorderLayout());
        mitlRulesEditor.add(rulesEditor);
        mitlRulesEditor.pack();        
        
        mitlRulesEditor.addInternalFrameListener(new InternalFrameListener() {

            @Override
            public void internalFrameOpened(InternalFrameEvent e) { // Do nothing
            }
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                doRulesWindowClosing(main, rulesEditor);
            }
            @Override
            public void internalFrameClosed(InternalFrameEvent e) { // Do nothing
            }
            @Override
            public void internalFrameIconified(InternalFrameEvent e) { // Do nothing
            }
            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) { // Do nothing
            }
            @Override
            public void internalFrameActivated(InternalFrameEvent e) { // Do nothing
            }
            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) { // Do nothing
            }
        });

        desktop.add(mitlRulesEditor);
    }
    private void doRulesWindowClosing(MainWindow main, RulesEditor rulesEditor) {
        if (rulesEditor.isDirty()) {
            MyHandyOptionPane optionPane = new MyHandyOptionPane();
            Object[] options = { "Yes", "No" };
            Object result = optionPane.yesNoDialog(main
                    , options, "Yes", ""
                    , "Would you like to save your changes?"
                    , "Save changes?");
            if ("Yes".equals(result.toString()))
                saveRuleFile(rulesEditor);
        }
    }
    private void saveRuleFile(RulesEditor rulesEditor) {
        moIO.writeRuleFile(rulesEditor.getCurrentFile());
        rulesEditor.setClean();
        
        // Recreate local blacklist structures & resend blacklist to Job Settings screen
        setBlacklistStructures();
        moJobListPanel.setBlacklist(moJobBlacklist);
    }
    private JMenuBar createRulesEditorMenu(final RulesEditor rulesEditor) {
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
                //try {
                doRulesWindowClosing(main, rulesEditor);
                mitlRulesEditor.setVisible(false);
/*                } catch (PropertyVetoException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();
                    System.err.println("Failed to close JInternalFrame");
                } */
            }
        });
        menu.add(menuItem);
        
        return menuBar;
    }
    
    private JMenuBar createDwarfListMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        
        JMenuItem menuItem = new JMenuItem("Set Location of Dwarves.xml...", KeyEvent.VK_L);
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
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
            KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadJobSettings(jobListPanel);
            }
        });
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Save", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveJobSettingsAs(jobListPanel, true);
            }
        });
        menu.add(menuItem);        
        
        menuItem = new JMenuItem("Save As...", KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.ALT_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveJobSettingsAs(jobListPanel, false);
            }
        });
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Reset to Default", KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetJobSettings();
            }
        });
        menuItem.setEnabled(false);     // TODO: Make this do something
        menu.add(menuItem);

        return menuBar;
        
    }
    
    // Creates file choosers for save and load operations. This is a time-consuming
    // operation and only needs to be done once.
    private void createChoosers() {
        // File chooser for Job Settings->Save
        mjfcSave = new JFileChooser();
        mjfcSave.setDialogTitle("Save Job Settings");
        mjfcSave.setDialogType(JFileChooser.SAVE_DIALOG);
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
        
        // File chooser for Job Settings->Open...
        mjfcOpen = new JFileChooser();
        mjfcOpen.setDialogTitle("Load Job Settings");
        mjfcOpen.setDialogType(JFileChooser.OPEN_DIALOG);
        mjfcOpen.setCurrentDirectory(moJobListPanel.getDirectory());
        
        // File chooser for Dwarves.xml
        File file = new File(mstrDwarvesXML);
        mjfcDwarves = new JFileChooser();
        mjfcDwarves.setDialogTitle("Select location of Dwarves.xml");
        mjfcDwarves.setCurrentDirectory(file);
        mjfcDwarves.setSelectedFile(file);

    }
    
    private void readFiles() throws Exception {
        // Try to read group-list.txt, labor-list.txt, rules.txt, and Dwarves.xml
        try {
            mvLaborGroups = moIO.readLaborGroups();
            mvLabors = moIO.readLabors();           // Read labor-list.txt

            moIO.readRuleFile();
            setBlacklistStructures();
            
            readDwarves();
            
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
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mitlRulesEditor.setVisible(true);
                try {
                    mitlRulesEditor.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
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
                    java.awt.Desktop.getDesktop().open(new File(TUTORIAL_FILE));
                } catch (IOException ex) {
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
    
    // Set dwarves.xml location & read it
    private void setDwarves() {
        int response = setDwarvesLocation();
        if (response == JFileChooser.APPROVE_OPTION) {
            
            // Hide the internal frame and remove the old panel, if any
            if (mitlDwarfList != null) {
                mitlDwarfList.setVisible(false);
                mitlDwarfList.remove(moDwarfListWindow);
            }
            try {
                readDwarves();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to read dwarves.xml.");
            }
            
            // Add the new panel to the internal frame, if an old panel was removed
            if (mitlDwarfList != null) {  
                mitlDwarfList.add(moDwarfListWindow);
                // TODO: The menu bar has to be set again here, or else
                // the menu items remain associated with the old table, and
                // won't appear to function. Should probably update this so that
                // the checked state of menu items is copied over. (Or, reload
                // the table more neatly.)
                //mitlDwarfList.setJMenuBar(moDwarfListWindow.getMenu(mvLaborGroups));
                updateDwarfListMenu();
                mitlDwarfList.pack();
                mitlDwarfList.setVisible(true);
            }
        }
    }
    
    private void resetJobSettings() {
        //TODO: Decide whether to load these from a file,
        // or from private load(DEFAULT_FILE_TEXT) in JobListPanel.
    }
    
    private void loadJobSettings(JobListPanel jobListPanel) {
        int input = mjfcOpen.showOpenDialog(this);
        
        if (input == JFileChooser.APPROVE_OPTION) {
            File file = mjfcOpen.getSelectedFile();
            jobListPanel.load(file);
            updateCurrentJobSettings(file);
        }
    }
    
    // If saving as "DEFAULT SETTINGS", change name to "MY DEFAULT SETTINGS"
    // TODO: This is not thought-through very well. DEFAULT SETTINGS should
    // be read-only (and created by the installation?) and the program should
    // suggest the file name MY DEFAULT SETTINGS.
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

            if (input == JFileChooser.APPROVE_OPTION) {
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
    
/*    private JPanel createOptionsPanel(final MainWindow mainWindow) {
        
        //final File filDwarvesXML = new File(mstrDwarvesXML);
        JPanel panOptions = new JPanel();
        panOptions.setLayout(new BorderLayout());
        panOptions.add(new JLabel("Dwarves.xml:  ")
                , BorderLayout.LINE_START);
        final JTextField txtDwarvesXML = new JTextField(mstrDwarvesXML);
        panOptions.add(txtDwarvesXML);

        JPanel panButtons = new JPanel();
        panButtons.setLayout(new BorderLayout());
        JButton btnFile = new JButton("Set Location...");
        btnFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDwarves();
            }
        });
        panButtons.add(btnFile, BorderLayout.LINE_START);
        JButton btnRead = new JButton("Re-read");
        btnRead.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mstrDwarvesXML = txtDwarvesXML.getText();
                readDwarves();
            }
        });
        panButtons.add(btnRead, BorderLayout.LINE_END);
        panOptions.add(panButtons, BorderLayout.LINE_END);
        
        return panOptions;
    } */
    
    // Returns the user's input to the file dialog
    private int setDwarvesLocation() {
        int input = mjfcDwarves.showOpenDialog(this);
        if (input == JFileChooser.APPROVE_OPTION) {
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
    }
    private void savePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        prefs.put("DwarvesXML", mstrDwarvesXML);
    }
    
    private void readDwarves() throws Exception {
        NodeList nodes = null;
        try {
            nodes = moIO.readDwarves(mstrDwarvesXML);
            /*myXMLReader xmlFileReader = new myXMLReader(mstrDwarvesXML);
            NodeList nodes = xmlFileReader.getDocument().getElementsByTagName("Creature"); */
            System.out.println("Dwarves.xml contains " + nodes.getLength() + " creatures.");            
        } catch (Exception e) {
            throw e;
        } finally {
            // Display a grid of the dwarves
            moDwarfListWindow = new DwarfListWindow(nodes, mvLabors, mvLaborGroups);
        }
        
    }
    
    private Vector<Dwarf> getDwarves() {
        return moDwarfListWindow.getIncludedDwarves();        
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
                if (! wlJobName.equals(labor.name)) {
                    if (! whitelist.get(wlJobName).contains(labor.name))
                        blacklist.addOneWayEntry(wlJobName, labor.name);
                }
            }
        }
    }

    // Updates the title of Rules Editor when the dirty state changes
    @Override
    public void dirtyChanged(boolean newDirtyState) {
        
        String strTitle;
        
        // If dirty
        if (newDirtyState)
            strTitle = RULES_EDITOR_TITLE_DIRTY;
        else
            strTitle = RULES_EDITOR_TITLE_CLEAN;
        
        if (! strTitle.equals(mitlRulesEditor.getTitle()))
            mitlRulesEditor.setTitle(strTitle);
    }
    
}
