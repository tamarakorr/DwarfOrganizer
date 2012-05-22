/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import dwarforganizer.MyPropertyGetter;
import dwarforganizer.MyPropertySetter;
import java.util.ArrayList;
import java.util.List;

/**
 * Editable (by column exception) version of MyTableModel.
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class MyEditableTableModel<T extends MyPropertyGetter & MyPropertySetter>
    extends MyTableModel<T> {

    private List<Integer> mlstEditableExceptionCols;
    private boolean mbEditable;

    public MyEditableTableModel(final Object[] cols, final Class[] colClasses
            , final String[] colProps
            , final List<T> rows, final SortKeySwapper sortKeySwapper) {

        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }
    public MyEditableTableModel(final List<Object> cols
            , final Class[] colClasses, final String[] colProps
            , final List<T> rows, final SortKeySwapper sortKeySwapper) {

        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }
    public MyEditableTableModel(final List<Object> cols
            , final List<Class> colClasses
            , final List<String> colProps, final List<T> rows
            , final SortKeySwapper sortKeySwapper) {

        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }

    // Initialize variables
    private void initialize() {
        mbEditable = false;
        mlstEditableExceptionCols = new ArrayList<Integer>();
    }
    public void addEditableException(final int col) {
        if (! mlstEditableExceptionCols.contains(col))
            mlstEditableExceptionCols.add(col);
    }
    // Adds an editable exception for the first column with the given identifier
    public void addEditableException(final Object colIdentifier) {
        for (int iCount = 0; iCount < this.getColumnCount(); iCount++) {
            if (colIdentifier.toString().equals(this.getColumnName(iCount))) {
                addEditableException(iCount);
                return;
            }
        }
    }
    @Override
    public boolean isCellEditable(final int row, final int column) {
        //System.out.println("isCellEditable(" + row + ", " + column + ")");
        if (mlstEditableExceptionCols.contains(column)) {
            //System.out.println("Returning " + ! mbEditable);
            return ! mbEditable;
        }
        else {
            //System.out.println("Returning " + mbEditable);
            return mbEditable;
        }
    }
    @Override
    public void setValueAt(final Object aValue, final int rowIndex
            , final int columnIndex) {

        final T oRow = (T) getRowData().get(rowIndex);
        // Get the user readable version of the property:
        oRow.setProperty(getColumnPropertyNames().get(columnIndex).toString()
                , aValue);
        fireUpdated(rowIndex, rowIndex);
    }
}
