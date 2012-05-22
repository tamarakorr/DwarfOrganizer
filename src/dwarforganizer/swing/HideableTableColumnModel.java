/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer.swing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

/**
 * See MIT license in license.txt
 *
 * NOTE: Remember to call table.createDefaultColumnsFromModel() after setting
 * this as the column model. Otherwise all table columns will be invisible.
 *
 * /**
 * <code>HideableTableColumnModel</code> extends the DefaultTableColumnModel .
 * It provides a comfortable way to hide/show columns.
 * Columns keep their positions when hidden and shown again.
 *
 * In order to work with JTable it cannot add any events to <code>TableColumnModelListener</code>.
 * Therefore hiding a column will result in <code>columnRemoved</code> event and showing it
 * again will notify listeners of a <code>columnAdded</code>, and possibly a <code>columnMoved</code> event.
 * For the same reason the following methods still deal with visible columns only:
 * getColumnCount(), getColumns(), getColumnIndex(), getColumn()
 * There are overloaded versions of these methods that take a parameter <code>onlyVisible</code> which lets
 * you specify whether you want invisible columns taken into account.
 *
 * @author Tamara Orr
 * @see DefaultTableColumnModel
 */
public class HideableTableColumnModel extends DefaultTableColumnModel {

    protected List<TableColumn> mlstAllTableColumns;

    public HideableTableColumnModel() {
        super();

        mlstAllTableColumns = new ArrayList<TableColumn>();
    }

    // Method to use column identifier instead of TableColumn
    // Beware; getColumnIndex() isn't incredibly fast for large tables
    public void setColumnVisible(Object colIdentifier, boolean visible) {
        int index = this.getColumnIndex(colIdentifier, false);
        setColumnVisible(this.getColumn(index, false), visible);
    }

    public void setColumnVisible(TableColumn column, boolean visible) {
        if (! visible) {
            super.removeColumn(column);
        }
        else {
            // find the visible index of the column:
            // iterate through both collections of visible and all columns, counting
            // visible columns up to the one that's about to be shown again
            int numVisible = tableColumns.size();
            int numInvisible = mlstAllTableColumns.size();
            int visibleIndex = 0;
            for (int invisibleIndex = 0; invisibleIndex < numInvisible; ++invisibleIndex) {
                TableColumn visibleColumn = (visibleIndex < numVisible ? tableColumns.get(visibleIndex) : null); // (TableColumn)
                TableColumn testColumn
                        = mlstAllTableColumns.get(invisibleIndex);
                if (testColumn == column) {
                    if (visibleColumn != column) {
                        super.addColumn(column);
                        super.moveColumn(tableColumns.size() - 1, visibleIndex);
                    }
                    return;
                }
                if (testColumn == visibleColumn) {
                    ++visibleIndex;
                }
            }
        }
    }
    // Debugging
    // The vector seems to get out of order when involved with the ColumnFreezingTable
    private void printAll(int howMany, String prefix) {
        for (int iCount = 0; iCount < howMany; iCount++)
            System.out.println(prefix + " " + iCount + " "
                    + mlstAllTableColumns.get(iCount).getIdentifier());
    }
    public boolean isColumnVisible(TableColumn aColumn) {
        return (tableColumns.indexOf(aColumn) >= 0);
    }

    public TableColumn getColumnModelIndex(int modelColumnIndex) {
        for (int cCount = 0; cCount < mlstAllTableColumns.size(); ++cCount) {
            TableColumn col = mlstAllTableColumns.get(cCount); //(TableColumn)
            if (col.getModelIndex() == modelColumnIndex) {
                return col;
            }
        }
        return null;
    }

    public void setAllColumnsVisible() {
        int noColumns = mlstAllTableColumns.size();
        for (int columnIndex = 0; columnIndex < noColumns; ++columnIndex) {
            TableColumn visibleColumn = (columnIndex < tableColumns.size()
                    ? tableColumns.get(columnIndex)
                    : null); // (TableColumn)
            TableColumn invisibleColumn = mlstAllTableColumns.get(columnIndex); // (TableColumn)

            if (visibleColumn != invisibleColumn) {
                super.addColumn(invisibleColumn);
                super.moveColumn(tableColumns.size() - 1, columnIndex);
            }
        }
    }

    @Override
    public void addColumn(TableColumn column) {
        //System.out.println("Adding " + column.getIdentifier());
        mlstAllTableColumns.add(column);
        super.addColumn(column);
    }

    @Override
    public void removeColumn(TableColumn column) {
        //System.out.println("Removing column " + column.getIdentifier());
        int allColumnsIndex = mlstAllTableColumns.indexOf(column);
        if (allColumnsIndex != -1) {
            mlstAllTableColumns.remove(allColumnsIndex);
        }
        super.removeColumn(column);
    //mvAllTableColumns.remove(column);
    }

    @Override
    public void moveColumn(int oldIndex, int newIndex) {
        if ((oldIndex < 0) || (oldIndex >= getColumnCount()) ||
                (newIndex < 0) || (newIndex >= getColumnCount())) {
            throw new IllegalArgumentException("moveColumn() - Index out of range");
        }
        TableColumn fromColumn = tableColumns.get(oldIndex);
        TableColumn toColumn = tableColumns.get(newIndex);
        int allColumnsOldIndex = mlstAllTableColumns.indexOf(fromColumn);
        int allColumnsNewIndex = mlstAllTableColumns.indexOf(toColumn);
//System.out.println("Moving " + fromColumn.getIdentifier() + " from " + allColumnsOldIndex + " to " + allColumnsNewIndex + " (inserting at " + toColumn.getIdentifier() + ")");
//printAll(3, "Before move:");
        if (oldIndex != newIndex) {
            mlstAllTableColumns.remove(allColumnsOldIndex);
            mlstAllTableColumns.add(allColumnsNewIndex, fromColumn);
        }
        super.moveColumn(oldIndex, newIndex);
//printAll(3, "After move:");
    }

    public int getColumnCount(boolean onlyVisible) {
        final List columns = (onlyVisible ? tableColumns : mlstAllTableColumns);
        return columns.size();
    }

    public Enumeration getColumns(boolean onlyVisible) {
        final List columns = (onlyVisible ? tableColumns : mlstAllTableColumns);
        return Collections.enumeration(columns);
        //return columns.elements();
    }

    public int getColumnIndex(Object identifier, boolean onlyVisible) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier is null");
        }

        final List<TableColumn> columns = (onlyVisible
                ? tableColumns : mlstAllTableColumns);
        final int numCols = columns.size();
        TableColumn column;

        for (int columnIndex = 0; columnIndex < numCols; ++columnIndex) {
            column = columns.get(columnIndex); // (TableColumn)
            if (identifier.equals(column.getIdentifier())) {
                return columnIndex;
            }
        }

        throw new IllegalArgumentException("Identifier not found (" + identifier
                + ")");
    }

    public TableColumn getColumn(int columnIndex, boolean onlyVisible) {
        return mlstAllTableColumns.get(columnIndex); // (TableColumn)
    }
}
