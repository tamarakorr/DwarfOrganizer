/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import dwarforganizer.*;
import java.util.Vector;

/**
 * Editable (by column exception) version of MyTableModel.
 * 
 * @author Tamara Orr
 */
public class MyEditableTableModel<T extends MyPropertyGetter & MyPropertySetter>
    extends MyTableModel<T> {

    private Vector<Integer> mvEditableExceptionCols;
    private boolean mbEditable;    
    
    public MyEditableTableModel(Object[] cols, Class[] colClasses, String[] colProps
            , Vector<T> rows, SortKeySwapper sortKeySwapper) {
        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }
    public MyEditableTableModel(Vector<Object> cols, Class[] colClasses
            , String[] colProps, Vector<T> rows, SortKeySwapper sortKeySwapper) {
        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }
    public MyEditableTableModel(Vector<Object> cols, Vector<Class> colClasses
            , Vector<String> colProps, Vector<T> rows, SortKeySwapper sortKeySwapper) {
        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }
    
    // Initialize variables
    private void initialize() {
        mbEditable = false;
        mvEditableExceptionCols = new Vector<Integer>();
    }
    public void addEditableException(int col) {
        if (! mvEditableExceptionCols.contains(col))
            mvEditableExceptionCols.add(col);
    }
    // Adds an editable exception for the first column with the given identifier
    public void addEditableException(Object colIdentifier) {
        for (int iCount = 0; iCount < this.getColumnCount(); iCount++) {
            if (colIdentifier.toString().equals(this.getColumnName(iCount))) {
                addEditableException(iCount);
                return;
            }
        }
    }
    @Override
    public boolean isCellEditable(int row, int column) {
        //System.out.println("isCellEditable(" + row + ", " + column + ")");
        if (mvEditableExceptionCols.contains(column)) {
            //System.out.println("Returning " + ! mbEditable);
            return ! mbEditable;
        }
        else {
            //System.out.println("Returning " + mbEditable);
            return mbEditable;
        }
    }
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        T oRow = (T) mvRowData.get(rowIndex);
        // Get the user readable version of the property:
        oRow.setProperty(mvColumnPropertyNames.get(columnIndex).toString(), aValue);
        fireUpdated(rowIndex, rowIndex);
    }
}
