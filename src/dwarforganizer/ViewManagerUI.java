/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.broadcast.BroadcastMessage;
import dwarforganizer.broadcast.Broadcaster;
import dwarforganizer.dirty.DirtyForm;
import dwarforganizer.swing.MyTableModel;
import dwarforganizer.swing.PlaceholderTextField;
import dwarforganizer.swing.SortKeySwapper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

/**
 * Allows the user to rename and delete views
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class ViewManagerUI extends AbstractEditor<GridView>
        implements DirtyForm {

    private JPanel uiPanel;
    private JTextField txtName;
    private ArrayList<GridView> mlstViews;
    private MyTableModel model;
    private Broadcaster broadcaster;
    private JButton btnUpdate;

    public ViewManagerUI() {
        super();

        broadcaster = new Broadcaster();

        Object[] cols = new Object[] { "All Views" };
        Class[] colClass = new Class[] { String.class };
        String[] colProps = new String[] { "name" };
        mlstViews = new ArrayList<GridView>();
        SortKeySwapper swapper = new SortKeySwapper();

        model = new MyTableModel(cols, colClass, colProps, mlstViews, swapper);
        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(220, 175));

        swapper.setTable(table);
        JScrollPane pane = new JScrollPane(table);
        btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRecord();
            }
        });
        txtName = new PlaceholderTextField(20, "Rename a view and press [Enter]"
                , true);

        JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteRow();
            }
        });

        JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editRow();
            }
        });

        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveViews();
            }
        });

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });

        // Set up default buttons
        JComponent[] nullComps = new JComponent[] { table };
        JComponent[] updateComps = new JComponent[] { btnUpdate, txtName
                , btnDelete, btnEdit, btnSave, btnClose };
        setUpDefaultButtons(createDefaultButtonMap(nullComps, null)
                , createDefaultButtonMap(updateComps, btnUpdate)); // (varargs)

        // Build UI-------------------------------------------------------------
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createEtchedBorder());
        tablePanel.add(pane, BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(txtName, BorderLayout.CENTER);
        panel.add(btnUpdate, BorderLayout.EAST);
        tablePanel.add(panel, BorderLayout.NORTH);

        panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        panel.add(btnDelete);
        panel.add(btnEdit);
        tablePanel.add(panel, BorderLayout.SOUTH);

        panel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        panel.add(btnSave);
        panel.add(btnClose);

        uiPanel = new JPanel(new BorderLayout());
        uiPanel.add(tablePanel, BorderLayout.CENTER);
        uiPanel.add(panel, BorderLayout.SOUTH);

        super.initialize(table, model, btnUpdate, false, true, true, true, true
                , true, true, txtName);
    }

    public void loadData(final Collection<GridView> colView) {
        // Clear input----------------------------------------------------------
        clearInput();

        // Set data-------------------------------------------------------------
        mlstViews = new ArrayList<GridView>(colView);
        model.setRowData(mlstViews);

        // Adjust components----------------------------------------------------
        uiPanel.validate();

        // Set clean state------------------------------------------------------
        setClean();
    }
    public JButton getDefaultButton() {
        return btnUpdate;
    }
    public ArrayList<GridView> getViews() {
        return mlstViews;
    }
    public JPanel getUIPanel() {
        return uiPanel;
    }

    @Override
    public void clearInput() {
        txtName.setText("");
    }

    @Override
    public boolean validateInput() {
        // Ensure no other view has the same name:
        for (GridView view : mlstViews)
            if (view.getName().equals(txtName.getText())) {
                JOptionPane.showMessageDialog(uiPanel
                        , "There is already a view with that name."
                        , "Cannot Update", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        return true;
    }

    @Override
    public GridView createRowData(boolean isNew) {

        if (isNew)
            System.err.println("[ViewManagerUI] New views are unsupported");

        String name = txtName.getText();

        GridView view = mlstViews.get(super.getCurrentEditedRow());
        view.setName(name);
        return view;
    }

    @Override
    public boolean rowDataToInput(GridView rowData) {
        txtName.setText(rowData.getName());
        return true;
    }

    public Broadcaster getBroadcaster() {
        return broadcaster;
    }
    private void saveViews() {
        broadcaster.notifyListeners(new BroadcastMessage("ViewManagerSave", null
                , ""));
    }
    private void closeWindow() {
        broadcaster.notifyListeners(new BroadcastMessage("ViewManagerClose"
                , null, ""));
    }
    private void requestDefaultButton(JButton btn) {
        broadcaster.notifyListeners(new BroadcastMessage(
                "ViewManagerDefaultButton", btn, ""));
    }
    // Sets up focus listeners for components in default button maps
    // (Uses varargs)
    private void setUpDefaultButtons(Map<JComponent, JButton>... maps) {
        for (Map<JComponent, JButton> map : maps) {
            for (JComponent comp : map.keySet()) {
                final JButton btn = map.get(comp);
                FocusListener listener = new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        requestDefaultButton(btn);
                    }
                    @Override
                    public void focusLost(FocusEvent e) { // Do nothing
                    }
                };
                comp.addFocusListener(listener);
            }
        }
    }
    // Creates a map for the given array of components to the given default
    // button.
    private HashMap<JComponent, JButton> createDefaultButtonMap(
            JComponent[] comps, JButton btn) {

        HashMap<JComponent, JButton> map = new HashMap<JComponent, JButton>(
                comps.length);
        for (JComponent comp : comps)
            map.put(comp, btn);
        return map;
    }
}
