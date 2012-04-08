/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Vector;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * A less simple table model.
 * Allows storage of objects instead of just attributes
 * 
 * @author Tamara Orr
 */
public class MyTableModel<T extends MyPropertyGetter>
        extends AbstractTableModel {

    private Object[] maoColumnHeadings;
    private Class[] maColumnClass;
    private String[] masColumnPropertyNames;
    private Vector<T> mvRowData;
    private Vector<Integer> mvEditableExceptionCols = new Vector<Integer>();
    private boolean mbEditable = false;
    private SortKeySwapper moSortKeySwapper;    // For preventing API bug with fireTableRowsInserted() on sorted table
    
    //public MyTableModel() { super(); }
/*    public MyTableModel(Object[] cols, int rowCount, Vector<T> rows) {
        super(cols, rowCount);
        maoColumnHeadings = cols;
        mvRowData = rows;
    }
    public MyTableModel(Object[] cols, int rowCount, Class[] columnClass
            , Vector<T> rows) {
        super(cols, rowCount, columnClass);
        maoColumnHeadings = cols;
        mvRowData = rows;
    }
    public MyTableModel(Vector cols, int rowCount, Vector<T> rows) {
        super(cols, rowCount);
        maoColumnHeadings = cols.toArray();
        mvRowData = rows;
    }
    public MyTableModel(Vector cols, int rowCount, Class[] columnClass
            , Vector<T> rows) {
        super(cols, rowCount, columnClass);
        maoColumnHeadings = cols.toArray();
        mvRowData = rows;
    } */
    //public MyTableModel(boolean editable) {
    //    super(editable);
    //}
    public MyTableModel(Object[] cols, Class[] colClasses, String[] colProps
            , Vector<T> rows, SortKeySwapper sortKeySwapper) {
        //super();
        maoColumnHeadings = cols;
        maColumnClass = colClasses;
        masColumnPropertyNames = colProps;
        mvRowData = rows;
        moSortKeySwapper = sortKeySwapper;
    }
    public MyTableModel(Vector<Object> cols, Class[] colClasses
            , String[] colProps, Vector<T> rows, SortKeySwapper sortKeySwapper) {
        //super();
        maoColumnHeadings = cols.toArray();
        maColumnClass = colClasses;
        masColumnPropertyNames = colProps;
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
        return oRow.getProperty(masColumnPropertyNames[columnIndex].toString()
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
        
/*        Runnable runnable = new Runnable() {            
            @Override
            public void run() {
                moSortKeySwapper.swapSortKeys();        // TODO: This swap isn't really safe
                fireTableRowsInserted(firstIndex, lastIndex);
                moSortKeySwapper.swapSortKeys();
            }
        };

        runSynchronousOnEDT(runnable); */
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
    private void fireUpdated(final int firstIndex, final int lastIndex) {
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
    
    @Override
    public Class getColumnClass(int columnIndex) {
        try {
            
            // If column class array is set, use its value
            if (maColumnClass != null) {
                //System.out.println("Returning " + maColumnClass[columnIndex].getSimpleName());
                return maColumnClass[columnIndex];
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
    
    public void addEditableException(int col) {
        if (! mvEditableExceptionCols.contains(col))
            mvEditableExceptionCols.add(col);
    }
    @Override
    public boolean isCellEditable(int row, int column) {
        if (mvEditableExceptionCols.contains(column))
            return ! mbEditable;
        else
            return mbEditable;
    }    
}
