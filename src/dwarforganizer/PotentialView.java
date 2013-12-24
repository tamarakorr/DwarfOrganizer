/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer;

import dwarforganizer.swing.ColumnFreezingTable;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import myutils.MyHandyTable;
import myutils.MySimpleTableModel;
import myutils.MyWindowUtils;

/**
 *
 * @author Tamara Orr
 */
public class PotentialView {
    private JSplitPane splitPane;
    private ColumnFreezingTable cft;

    public PotentialView(final List<Dwarf> dwarfList
            , final Map<String, Skill> skillMap) {

        initialize(dwarfList, skillMap);
    }
    public PotentialView(final Dwarf dwarf, final Map<String, Skill> skillMap) {
        final List<Dwarf> list = new ArrayList<Dwarf>(1);

        list.add(dwarf);
        initialize(list, skillMap);
    }
    private void initialize(final List<Dwarf> dwarfList
            , final Map<String, Skill> skillMap) {

        final MySimpleTableModel model = createModel(dwarfList, skillMap);
        final TableColumnModel cm = new DefaultTableColumnModel();

        final JTable tbl = new JTable(model);
        tbl.setUpdateSelectionOnSort(false);    // Required for freezing table
        MyHandyTable.handyTable(tbl, model, true, 1, SortOrder.DESCENDING
                , true);
        MyHandyTable.autoResizeTableColumns(tbl);
        final JScrollPane sp = new JScrollPane(tbl);
        sp.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);   // Required for freezing table

        cft = new ColumnFreezingTable(sp, cm);
        splitPane = cft.getSplitPane();
    }
    public void createFrame() {

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(splitPane);

        MyWindowUtils.createSimpleWindow("Potential View", panel
                , new BorderLayout());
        cft.setDividerAfterCol(0);      // Table must be visible already
    }
    private MySimpleTableModel createModel(final List<Dwarf> dwarfList
            , final Map<String, Skill> skillMap) {

        // Create columns
        final int numDwarves = dwarfList.size();
        final Object[] cols = new Object[numDwarves + 1];
        cols[0] = "Potential";
        for (int iCount = 0; iCount < numDwarves; iCount++) {
            cols[iCount + 1] = dwarfList.get(iCount).getName();
        }

        // Create model and skill name headings
        final MySimpleTableModel model = new MySimpleTableModel(cols
                , skillMap.size());
        int keyCount = 0;
        for (final String key : skillMap.keySet()) {
            final String skillName = skillMap.get(key).getName();
            model.setValueAt(skillName, keyCount, 0);

            // Set potentials
            for (int iCount = 0; iCount < numDwarves; iCount++) {
                final Dwarf dwarf = dwarfList.get(iCount);
                model.setValueAt(dwarf.getSkillPotentials().get(skillName)
                        , keyCount, iCount + 1);
            }
            keyCount++;
        }
        return model;
    }
}
