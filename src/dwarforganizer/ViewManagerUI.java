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
import java.util.logging.Logger;
import javax.swing.*;

/**
 * Allows the user to rename and delete views
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class ViewManagerUI extends AbstractEditor<GridView>
        implements DirtyForm {

    private static final Logger logger = Logger.getLogger(
            ViewManagerUI.class.getName());

    private JPanel uiPanel;
    private JTextField txtName;
    private ArrayList<GridView> mlstViews;
    private MyTableModel model;
    private Broadcaster broadcaster;
    private JButton btnUpdate;

    public ViewManagerUI() {
        super();

        broadcaster = new Broadcaster();

        final Object[] cols = new Object[] { "All Views" };
        final Class[] colClass = new Class[] { String.class };
        final String[] colProps = new String[] { "name" };
        mlstViews = new ArrayList<GridView>();
        final SortKeySwapper swapper = new SortKeySwapper();

        model = new MyTableModel(cols, colClass, colProps, mlstViews, swapper);
        final JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(220, 175));

        swapper.setTable(table);
        final JScrollPane pane = new JScrollPane(table);
        btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                updateRecord();
            }
        });
        txtName = new PlaceholderTextField(20, "Rename a view and press [Enter]"
                , true);

        final JButton btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                deleteRow();
            }
        });

        final JButton btnEdit = new JButton("Edit");
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                editRow();
            }
        });

        final JButton btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                saveViews();
            }
        });

        final JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                closeWindow();
            }
        });

        // Set up default buttons
        final JComponent[] nullComps = new JComponent[] { table };
        final JComponent[] updateComps = new JComponent[] { btnUpdate, txtName
                , btnDelete, btnEdit, btnSave, btnClose };
        setUpDefaultButtons(createDefaultButtonMap(nullComps, null)
                , createDefaultButtonMap(updateComps, btnUpdate)); // (varargs)

        // Build UI-------------------------------------------------------------
        final JPanel tablePanel = new JPanel(new BorderLayout());
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
        for (final GridView view : mlstViews)
            if (view.getName().equals(txtName.getText())) {
                DwarfOrganizer.showInfo(uiPanel
                        , "There is already a view with that name."
                        , "Cannot Update");
                return false;
            }
        return true;
    }

    @Override
    public GridView createRowData(final boolean isNew) {

        if (isNew)
            logger.severe("[ViewManagerUI] New views are unsupported");

        final String name = txtName.getText();

        final GridView view = mlstViews.get(super.getCurrentEditedRow());
        view.setName(name);
        return view;
    }

    @Override
    public boolean rowDataToInput(final GridView rowData) {
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
    private void requestDefaultButton(final JButton btn) {
        broadcaster.notifyListeners(new BroadcastMessage(
                "ViewManagerDefaultButton", btn, ""));
    }
    // Sets up focus listeners for components in default button maps
    // (Uses varargs)
    private void setUpDefaultButtons(final Map<JComponent, JButton>... maps) {
        for (final Map<JComponent, JButton> map : maps) {
            for (final JComponent comp : map.keySet()) {
                final JButton btn = map.get(comp);
                final FocusListener listener = new FocusListener() {
                    @Override
                    public void focusGained(final FocusEvent e) {
                        requestDefaultButton(btn);
                    }
                    @Override
                    public void focusLost(final FocusEvent e) { // Do nothing
                    }
                };
                comp.addFocusListener(listener);
            }
        }
    }
    // Creates a map for the given array of components to the given default
    // button.
    private HashMap<JComponent, JButton> createDefaultButtonMap(
            final JComponent[] comps, final JButton btn) {

        final HashMap<JComponent, JButton> map
                = new HashMap<JComponent, JButton>(comps.length);
        for (final JComponent comp : comps)
            map.put(comp, btn);
        return map;
    }
}
