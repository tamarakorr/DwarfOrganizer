/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;
import myutils.MyHandyTable;
import myutils.MySimpleTableModel;

/**
 *
 * @author Tamara Orr
 */
public class ExclusionPanel extends JPanel {

    public ExclusionPanel(List<Exclusion> lstExclusion, List<Dwarf> lstCitizen) {
        super();
        
        JPanel ruleEntryPanel = new JPanel();
        ruleEntryPanel.setLayout(new FlowLayout());
        ruleEntryPanel.setBorder(BorderFactory.createEtchedBorder(
                EtchedBorder.LOWERED));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Rule Name"), BorderLayout.NORTH);
        String strPlaceholder = "Enter a name for the rule (optional)";
        JTextField txt = new PlaceholderTextField(20
                , strPlaceholder, true);        // strPlaceholder.length()
        panel.add(txt, BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Attribute"), BorderLayout.NORTH);
        panel.add(new JComboBox(Dwarf.getSupportedProperties()), BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Comparison"), BorderLayout.NORTH);
        panel.add(new JComboBox(), BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("Value"), BorderLayout.NORTH);
        panel.add(new PlaceholderTextField(15, "Value to be compared", true)
                , BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JButton("Add New"), BorderLayout.NORTH);
        panel.add(new JButton("Update"), BorderLayout.SOUTH);
        ruleEntryPanel.add(panel);
        
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JButton("Edit"), BorderLayout.WEST);
        panel.add(new JButton("Delete"), BorderLayout.EAST);
        tablePanel.add(panel, BorderLayout.NORTH);        
        
        tablePanel.add(createRuleExclusionTable(lstExclusion)
                , BorderLayout.CENTER);
        
        JPanel rulePanel = new JPanel();
        rulePanel.setLayout(new BorderLayout());
        rulePanel.add(ruleEntryPanel, BorderLayout.NORTH);
        rulePanel.add(tablePanel, BorderLayout.SOUTH);
        rulePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createRaisedBevelBorder()
                , "Exclusions by rule"));
        
        // --------------------
        JPanel listOfLists = new JPanel();
        listOfLists.setLayout(new BorderLayout());
        
        JPanel panListNameEdit = new JPanel();
        panListNameEdit.setLayout(new BorderLayout());
        panListNameEdit.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JLabel("List Name"), BorderLayout.NORTH);
        panel.add(new PlaceholderTextField(20, "Enter a list name (optional)", true)
                , BorderLayout.SOUTH);
        panListNameEdit.add(panel, BorderLayout.CENTER);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JButton("Add New"), BorderLayout.NORTH);
        panel.add(new JButton("Update"), BorderLayout.SOUTH);
        panListNameEdit.add(panel, BorderLayout.EAST);
        listOfLists.add(panListNameEdit, BorderLayout.NORTH);
        
        JPanel panEditDelete = new JPanel();
        panEditDelete.setLayout(new BorderLayout());
        panEditDelete.add(new JButton("Edit"), BorderLayout.WEST);
        panEditDelete.add(new JButton("Delete"), BorderLayout.EAST);
        
        panel = new JPanel();
        panel.add(panEditDelete, BorderLayout.WEST);
        
        listOfLists.add(panel, BorderLayout.CENTER);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());        
        panel.add(createListExclusionList(), BorderLayout.CENTER);
        listOfLists.add(panel, BorderLayout.SOUTH);
                
        JPanel panCitizenList = new JPanel();
        panCitizenList.setLayout(new BorderLayout());
        panCitizenList.add(createCitizenList(), BorderLayout.CENTER);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JButton("Remove"), BorderLayout.WEST);
        panCitizenList.add(panel, BorderLayout.NORTH);
        
        JPanel panCitizenSelect = new JPanel();
        panCitizenSelect.setLayout(new BorderLayout());
        panCitizenSelect.add(new JLabel("Citizen"), BorderLayout.NORTH);
        panCitizenSelect.add(new JComboBox(getCitizenNames(lstCitizen))
                , BorderLayout.SOUTH);
        
        JPanel panCitizenAdd = new JPanel();
        panCitizenAdd.setLayout(new BorderLayout());
        panCitizenAdd.setBorder(BorderFactory.createEtchedBorder());
        panCitizenAdd.add(panCitizenSelect, BorderLayout.CENTER);
        panCitizenAdd.add(new JButton("Add"), BorderLayout.EAST);
        
        JPanel panCitizens = new JPanel();
        panCitizens.setLayout(new BorderLayout());
        panCitizens.add(panCitizenAdd, BorderLayout.NORTH);
        panCitizens.add(panCitizenList, BorderLayout.SOUTH);
        
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createRaisedBevelBorder()
                , "Exclusions by list"));
        listPanel.add(listOfLists, BorderLayout.WEST);
        listPanel.add(panCitizens, BorderLayout.EAST);
        
        // ---Build UI---------
        this.setLayout(new BorderLayout());
        this.add(rulePanel, BorderLayout.NORTH);
        this.add(listPanel, BorderLayout.CENTER);
        
    }
    
    protected JMenuBar createMenuBar() {
        // TODO: Let the owner do this
        JMenuBar menuBar = new JMenuBar();
        
        JMenu menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        
        JMenuItem menuItem = new JMenuItem("Save");
        menuItem.setMnemonic(KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveExclusions();
            }
        });
        menu.add(menuItem);
        
        menu.add(new JSeparator());
        
        menuItem = new JMenuItem("Close");
        menuItem.setMnemonic(KeyEvent.VK_C);
        menuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        
        return menuBar;
    }
    
    private void saveExclusions() {
        //TODO
    }
    private JScrollPane createRuleExclusionTable(List<Exclusion> lstExclusion) {
        
        Vector<Object> cols = new Vector<Object>(Arrays.asList(
                new Object[] { "Active", "Name", "Attribute", "Comparison"
                , "Value" }));  // Column identifiers
        Class[] aClasses = new Class[] {Boolean.class, String.class
                , String.class, String.class, Object.class };   // Column classes
        Vector<Object> colProps = new Vector<Object>(Arrays.asList(
                new Object[] { "name", "propertyname"
                , "comparator", "value"}));     // MyPropertyGetter properties
        Vector<Integer> vColIndices = new Vector<Integer> (Arrays.asList(
                new Integer[] { 1, 2, 3, 4 })); // Column indices for MyPropertyGetter properties
        
        MySimpleTableModel model = new MySimpleTableModel(cols
                , lstExclusion.size(), aClasses);
        DataVectorMaker dvm = new DataVectorMaker<Exclusion>();
        model.setDataVector(dvm.toDataVector(lstExclusion
                , colProps, vColIndices, true, cols.size()), cols);
        model.addEditableException(0);      // Active checkbox editable
        
        JTable tblReturn = new JTable(model);
        
        JScrollPane spReturn = new JScrollPane(tblReturn);
        spReturn.setPreferredSize(new Dimension(275, 100)); // w, h
        
        MyHandyTable.autoResizeTableColumns(tblReturn, spReturn);
        MyHandyTable.autoSortTable(model, tblReturn);
        
        return spReturn;
    }
    private JScrollPane createListExclusionList() {
        JScrollPane spReturn = new JScrollPane(new JTable(new MySimpleTableModel(
                new Object[] { "List", "Citizen Count" }, 2
                , new Class[] { String.class, int.class })));
        spReturn.setPreferredSize(new Dimension(350, 100)); // w, h
        return spReturn;
    }
    private JScrollPane createCitizenList() {
        JScrollPane spReturn = new JScrollPane(new JTable(new MySimpleTableModel(
                new Object[] { "Citizen" }, 2, new Class[] { String.class })));
        spReturn.setPreferredSize(new Dimension(250, 100)); // w, h
        return spReturn;    
    }
    
    private Vector<String> getCitizenNames(List<Dwarf> list) {
        Vector<String> vReturn = new Vector<String>();
        for (Dwarf dwarf : list) {
            vReturn.add(dwarf.name);
        }
        return vReturn;
    }
}
