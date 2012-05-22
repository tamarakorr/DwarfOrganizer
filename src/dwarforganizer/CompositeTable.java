/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import myutils.MyHandyTable;

/**
 * Conveniently treats multiple JTables as one
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class CompositeTable {
    private JTable[] tables;
    private int mainIndex;

    public CompositeTable(final JTable[] tables, final int mainIndex) {
        this.tables = tables;
        this.mainIndex = mainIndex;
    }
    public JTable getMainTable() {
        return tables[mainIndex];
    }
    public JTable[] getTables() {
        return tables;
    }
    public TableColumn getColumn(final Object identifier) {
        for (final JTable table : tables) {
            try {
                return table.getColumn(identifier);
            } catch (IllegalArgumentException ignore) {
                // The column isn't in this one!
            }
        }
        throw new IllegalArgumentException();
    }
    public int getTotalColumns() {
        int sum = 0;
        for (JTable table : tables) {
            sum += table.getColumnCount();
        }
        return sum;
    }
    public void moveColumn(final JTable sourceTable, final int sourceIndex
            , final int destIndex) {

        int localDestIndex = 0;
        JTable destTable = null;
        int destCounter;

        destCounter = 0;
        for (final JTable table : tables) {
            localDestIndex = 0;
            for (int iCount = 0; iCount < table.getColumnCount(); iCount++) {
                if (destCounter == destIndex) {
                    destTable = table;
                    break;
                }
                destCounter++;
                localDestIndex++;
            }
            if (destTable != null)
                break;
        }

        if (sourceTable.equals(destTable)) {
            sourceTable.moveColumn(sourceIndex, localDestIndex);
        }
        else {
            MyHandyTable.moveColumn(
                sourceTable.getColumnModel().getColumn(sourceIndex)
                , sourceTable, destTable, sourceIndex);
        }
    }
}
