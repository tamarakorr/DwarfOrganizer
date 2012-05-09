/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import dwarforganizer.*;
import java.util.Arrays;
import java.util.Vector;
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
    private Vector<Class> mvColumnClass;
    protected Vector<String> mvColumnPropertyNames;
    protected Vector<T> mvRowData;
    private SortKeySwapper moSortKeySwapper;    // For preventing API bug with fireTableRowsInserted() on sorted table

    public MyTableModel(Object[] cols, Class[] colClasses, String[] colProps
            , Vector<T> rows, SortKeySwapper sortKeySwapper) {
        //super();
        maoColumnHeadings = cols;
        mvColumnClass = new Vector<Class>(Arrays.asList(colClasses));
        mvColumnPropertyNames = new Vector<String>(Arrays.asList(colProps));
        mvRowData = rows;
        moSortKeySwapper = sortKeySwapper;

        //initialize();
    }
    public MyTableModel(Vector<Object> cols, Class[] colClasses
            , String[] colProps, Vector<T> rows, SortKeySwapper sortKeySwapper) {
        //super();
        maoColumnHeadings = cols.toArray();
        mvColumnClass = new Vector<Class>(Arrays.asList(colClasses));
        mvColumnPropertyNames = new Vector<String>(Arrays.asList(colProps));
        mvRowData = rows;
        moSortKeySwapper = sortKeySwapper;

        //initialize();
    }
    public MyTableModel(Vector<Object> cols, Vector<Class> colClasses
            , Vector<String> colProps, Vector<T> rows, SortKeySwapper sortKeySwapper) {
        maoColumnHeadings = cols.toArray();
        mvColumnClass = colClasses;
        mvColumnPropertyNames = colProps;
        mvRowData = rows;
        moSortKeySwapper = sortKeySwapper;
    }

    @Override
    public int getRowCount() {
        return mvRowData.size();
    }

    @Override
    public int getColumnCount() {
        return maoColumnHeadings.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T oRow = mvRowData.get(rowIndex);
        // Get the user readable version of the property:
        return oRow.getProperty(mvColumnPropertyNames.get(columnIndex).toString()
                , true);
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

    public void addRow(T newRow) {
        int index = mvRowData.size();
        mvRowData.add(newRow);
        fireInserted(index, index);
    }

    public void addRows(Vector<T> newRows) {
        final int firstIndex = mvRowData.size();
        final int lastIndex = firstIndex + newRows.size() - 1;
        //System.out.println("Rows added in range " + firstIndex + " to " + lastIndex);
        mvRowData.addAll(newRows);

        if (newRows.size() > 0) fireInserted(firstIndex, lastIndex);
    }

    // Make sure fireTableRowsInserted gets executed on EDT
    private void fireInserted(final int firstIndex, final int lastIndex) {
        runFireOnEDT(new FiringFunction() {
            @Override
            public void fireIt() {
                moSortKeySwapper.swapSortKeys();        // TODO: This swap isn't really safe
                fireTableRowsInserted(firstIndex, lastIndex);
                moSortKeySwapper.swapSortKeys();
            }
        });
    }
    private void runSynchronousOnEDT(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread())
            runnable.run();
        else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception e) {
                System.err.println(e.getMessage() + "Failed to update table correctly");
            }
        }
    }

    public boolean containsKey(long key) {
        boolean bReturn = false;
        if (mvRowData != null)
            for (T obj : mvRowData)
                if (obj.getKey() == key) {
                    bReturn = true;
                    break;
                }
        return bReturn;
    }

    public void removeRow(int rowIndex) {
        mvRowData.remove(rowIndex);
        //this.fireTableRowsDeleted(rowIndex, rowIndex);
        fireDeleted(rowIndex, rowIndex);
    }

    // For using closures to run function on EDT
    interface FiringFunction {
        void fireIt();
    }
    private void runFireOnEDT(final FiringFunction fwf) {
        Runnable runnable = new Runnable() {
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

    public long getKeyForRow(int rowIndex) {
        return mvRowData.get(rowIndex).getKey();
    }

    // Updates the first row found that has the given key.
    public void updateRowByKey(long key, T newData) {

        for (int iCount = 0; iCount < mvRowData.size(); iCount++) {
            if (key == getKeyForRow(iCount)) {
                updateRow(iCount, newData);
                break;
            }
        }
    }

    public void updateRow(int rowIndex, T newData) {
        mvRowData.remove(rowIndex);
        mvRowData.insertElementAt(newData, rowIndex);
        //this.fireTableRowsUpdated(rowIndex, rowIndex);
        fireUpdated(rowIndex, rowIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return maoColumnHeadings[columnIndex].toString();
    }

    public Vector<T> getRowData() {
        return mvRowData;
    }
    public void setRowData(Vector<T> newRowData) {
        mvRowData = newRowData;
        this.fireTableDataChanged();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        try {

            // If column class array is set, use its value
            if (mvColumnClass != null) {
                //System.out.println("Returning " + maColumnClass[columnIndex].getSimpleName());
                return mvColumnClass.get(columnIndex);
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
        } catch (Exception ignore) {
            return Object.class;        // On error, guess Object
        }
    }

    // Calls for a repaints when the underlying object was changed
    public void fireUpdateForKey(long key) {
        for (int iCount = 0; iCount < mvRowData.size(); iCount++) {
            if (key == getKeyForRow(iCount)) {
                fireUpdated(iCount, iCount);
                break;
            }
        }
    }
}
