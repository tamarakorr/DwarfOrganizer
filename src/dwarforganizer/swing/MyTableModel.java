/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import dwarforganizer.MyPropertyGetter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * A less simple table model.
 * Allows storage of objects instead of just attributes
 *
 * @see MyEditableTableModel for editable version (T must extend
 * MyPropertySetter as well)
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class MyTableModel<T extends MyPropertyGetter>
        extends AbstractTableModel {

    private Object[] maoColumnHeadings;
    private List<Class> mlstColumnClass;
    private List<String> mlstColumnPropertyNames;
    private List<T> mlstRowData;
    private SortKeySwapper moSortKeySwapper;    // For preventing API bug with fireTableRowsInserted() on sorted table

    public MyTableModel(final Object[] cols, final Class[] colClasses
            , final String[] colProps
            , final List<T> rows, final SortKeySwapper sortKeySwapper) {

        //super();
        maoColumnHeadings = cols;
        mlstColumnClass = new ArrayList<Class>(Arrays.asList(colClasses));
        mlstColumnPropertyNames = new ArrayList<String>(Arrays.asList(
                colProps));
        mlstRowData = rows;
        moSortKeySwapper = sortKeySwapper;

        //initialize();
    }
    public MyTableModel(final List<Object> cols, final Class[] colClasses
            , final String[] colProps, final List<T> rows
            , final SortKeySwapper sortKeySwapper) {

        //super();
        maoColumnHeadings = cols.toArray();
        mlstColumnClass = new ArrayList<Class>(Arrays.asList(colClasses));
        mlstColumnPropertyNames = new ArrayList<String>(Arrays.asList(colProps));
        mlstRowData = rows;
        moSortKeySwapper = sortKeySwapper;

        //initialize();
    }
    public MyTableModel(final List<Object> cols, final List<Class> colClasses
            , final List<String> colProps, final List<T> rows
            , final SortKeySwapper sortKeySwapper) {

        maoColumnHeadings = cols.toArray();
        mlstColumnClass = colClasses;
        mlstColumnPropertyNames = colProps;
        mlstRowData = rows;
        moSortKeySwapper = sortKeySwapper;
    }
    public List<String> getColumnPropertyNames() {
        return mlstColumnPropertyNames;
    }

    @Override
    public int getRowCount() {
        return mlstRowData.size();
    }

    @Override
    public int getColumnCount() {
        return maoColumnHeadings.length;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final T oRow = mlstRowData.get(rowIndex);
        // Get the user readable version of the property:
        return oRow.getProperty(
                mlstColumnPropertyNames.get(columnIndex).toString(), true);
    }

    // Sets row data to the new vector
/*    public void setData(Vector<T> newRows) {
        int rowCount = mvRowData.size();
        if (rowCount > 0) this.fireTableRowsDeleted(1, rowCount);
        mvRowData = newRows;
        if (newRows.size() > 0) {
            this.fireTableRowsInserted(1, newRows.size());
        }
    } */

    public void addRow(final T newRow) {
        final int index = mlstRowData.size();
        mlstRowData.add(newRow);
        fireInserted(index, index);
    }

    public void addRows(final List<T> newRows) {
        final int firstIndex = mlstRowData.size();
        final int lastIndex = firstIndex + newRows.size() - 1;
        //System.out.println("Rows added in range " + firstIndex + " to " + lastIndex);
        mlstRowData.addAll(newRows);

        if (newRows.size() > 0) fireInserted(firstIndex, lastIndex);
    }

    // Make sure fireTableRowsInserted gets executed on EDT
    private void fireInserted(final int firstIndex, final int lastIndex) {
        runFireOnEDT(new FiringFunction() {
            @Override
            public void fireIt() {
                moSortKeySwapper.swapSortKeys(); // TODO: This swap isn't really safe
                fireTableRowsInserted(firstIndex, lastIndex);
                moSortKeySwapper.swapSortKeys();
            }
        });
    }
    private void runSynchronousOnEDT(final Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (final Exception e) {
                System.err.println(e.getMessage()
                        + "Failed to update table correctly");
            }
        }
    }

    public boolean containsKey(final long key) {
        boolean bReturn = false;
        if (mlstRowData != null) {
            for (final T rowData : mlstRowData) {
                if (rowData.getKey() == key) {
                    bReturn = true;
                    break;
                }
            }
        }
        return bReturn;
    }

    public void removeRow(final int rowIndex) {
        mlstRowData.remove(rowIndex);
        //this.fireTableRowsDeleted(rowIndex, rowIndex);
        fireDeleted(rowIndex, rowIndex);
    }

    // For using closures to run function on EDT
    interface FiringFunction {
        void fireIt();
    }
    private void runFireOnEDT(final FiringFunction fwf) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fwf.fireIt();
            }
        };
        runSynchronousOnEDT(runnable);
    }
    private void fireDeleted(final int firstIndex, final int lastIndex) {
        runFireOnEDT(new FiringFunction() {
            @Override
            public void fireIt() {
                fireTableRowsDeleted(firstIndex, lastIndex);
            }
        });

        /*
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fireTableRowsDeleted(firstIndex, lastIndex);
            }
        };

        runSynchronousOnEDT(runnable);        */
    }
    protected void fireUpdated(final int firstIndex, final int lastIndex) {
        runFireOnEDT(new FiringFunction() {
            @Override
            public void fireIt() {
                fireTableRowsUpdated(firstIndex, lastIndex);
            }
        });

        /*Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fireTableRowsUpdated(firstIndex, lastIndex);
            }
        };

        runSynchronousOnEDT(runnable); */
    }

    public long getKeyForRow(final int rowIndex) {
        return mlstRowData.get(rowIndex).getKey();
    }

    // Updates the first row found that has the given key.
    public void updateRowByKey(final long key, final T newData) {

        for (int iCount = 0; iCount < mlstRowData.size(); iCount++) {
            if (key == getKeyForRow(iCount)) {
                updateRow(iCount, newData);
                break;
            }
        }
    }

    public void updateRow(final int rowIndex, final T newData) {
        mlstRowData.remove(rowIndex);
        //mlstRowData.insertElementAt(newData, rowIndex);
        mlstRowData.add(rowIndex, newData);
        //this.fireTableRowsUpdated(rowIndex, rowIndex);
        fireUpdated(rowIndex, rowIndex);
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return maoColumnHeadings[columnIndex].toString();
    }

    public List<T> getRowData() {
        return mlstRowData;
    }
    public void setRowData(final List<T> newRowData) {
        mlstRowData = newRowData;
        this.fireTableDataChanged();
    }

    @Override
    public Class getColumnClass(final int columnIndex) {
        try {

            // If column class array is set, use its value
            if (mlstColumnClass != null) {
                //System.out.println("Returning " + maColumnClass[columnIndex].getSimpleName());
                return mlstColumnClass.get(columnIndex);
            }
            // If column class array is not set, guess the value by looking
            // for a non-null in the specified column
            else {
                for (int row = 0; row < this.getRowCount(); row++) {
                    if (getValueAt(row, columnIndex) != null)
                        return getValueAt(row, columnIndex).getClass();
                }
                return Object.class;    // If all null, guess Object
            }
        } catch (final Exception ignore) {
            return Object.class;        // On error, guess Object
        }
    }

    // Calls for a repaints when the underlying object was changed
    public void fireUpdateForKey(final long key) {
        for (int iCount = 0; iCount < mlstRowData.size(); iCount++) {
            if (key == getKeyForRow(iCount)) {
                fireUpdated(iCount, iCount);
                break;
            }
        }
    }
}
