/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.RowSorter.SortKey;

/**
 *
 * See MIT license in license.txt
 * @author Tamara Orr
 *      // This is an ugly workaround for a Swing bug that causes
        // AbstractTableModel.fireTableRowsInserted to always throw
        // an unintelligible exception about column out of bounds, when
        // a new row is added to a sorted table.
 *
 * You must call SortKeySwapper.setTable() once before ever adding any rows
 * to a sorted table via MyTableModel.
 *
        // @see MyTableModel.java
        // @see ExclusionPanel.java for an example of how it is used
 */
public class SortKeySwapper {

    private static final Logger logger = Logger.getLogger(
            SortKeySwapper.class.getName());

    private List<SortKey> moKeyHolder = null;
    private JTable moTable;

    public SortKeySwapper() {
        super();
    }
    public SortKeySwapper(final JTable table) {
        super();
        moTable = table;
    }

    public void setTable(final JTable oTable) {
        moTable = oTable;
    }

    // Swap the current table sort keys for null sort keys, or vice versa
    public void swapSortKeys() {
        // Don't mess with unsorted tables
        if (null != moTable.getRowSorter()) {
            if (null != moTable.getRowSorter().getSortKeys()) {

                try {
                    final List<SortKey> temp = moKeyHolder;
                    moKeyHolder = (List<SortKey>)
                            moTable.getRowSorter().getSortKeys();
                    moTable.getRowSorter().setSortKeys(temp);
                } catch (final NullPointerException e) {
                    logger.log(Level.SEVERE, "Failed to swap sort keys. Did you"
                            + " remember to do SortKeySwapper.setTable() before"
                            + " adding any rows?", e);
                }
            }
        }
    }
}
