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

    public MyEditableTableModel(Object[] cols, Class[] colClasses
            , String[] colProps
            , List<T> rows, SortKeySwapper sortKeySwapper) {
        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }
    public MyEditableTableModel(List<Object> cols, Class[] colClasses
            , String[] colProps, List<T> rows, SortKeySwapper sortKeySwapper) {
        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }
    public MyEditableTableModel(List<Object> cols, List<Class> colClasses
            , List<String> colProps, List<T> rows, SortKeySwapper sortKeySwapper) {
        super(cols, colClasses, colProps, rows, sortKeySwapper);
        initialize();
    }

    // Initialize variables
    private void initialize() {
        mbEditable = false;
        mlstEditableExceptionCols = new ArrayList<Integer>();
    }
    public void addEditableException(int col) {
        if (! mlstEditableExceptionCols.contains(col))
            mlstEditableExceptionCols.add(col);
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
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        T oRow = (T) mlstRowData.get(rowIndex);
        // Get the user readable version of the property:
        oRow.setProperty(mlstColumnPropertyNames.get(columnIndex).toString()
                , aValue);
        fireUpdated(rowIndex, rowIndex);
    }
}
