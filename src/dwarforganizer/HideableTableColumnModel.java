/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Enumeration;
import java.util.Vector;
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

    protected Vector<TableColumn> mvAllTableColumns;
    
    public HideableTableColumnModel() {
        super();
        
        mvAllTableColumns = new Vector<TableColumn>();
    }
    
    // Method to use column identifier instead of TableColumn
    // Beware; getColumnIndex() isn't incredibly fast for large tables
    public void setColumnVisible(Object colIdentifier, boolean visible) {
        setColumnVisible(this.getColumn(this.getColumnIndex(colIdentifier
                , false), false), visible);
    }
    
    public void setColumnVisible(TableColumn column, boolean visible) {
        if(!visible) {
            super.removeColumn(column);
        }
        else {
            // find the visible index of the column:
            // iterate through both collections of visible and all columns, counting
            // visible columns up to the one that's about to be shown again
            int noVisibleColumns = tableColumns.size();
            int noInvisibleColumns = mvAllTableColumns.size();
            int visibleIndex = 0;
            
            for(int invisibleIndex = 0; invisibleIndex < noInvisibleColumns; ++invisibleIndex) {
                TableColumn visibleColumn = (visibleIndex < noVisibleColumns ? tableColumns.get(visibleIndex) : null); // (TableColumn)
                TableColumn testColumn = mvAllTableColumns.get(invisibleIndex); // (TableColumn)
                
                if(testColumn == column) {
                    if(visibleColumn != column) {
                        super.addColumn(column);
                        super.moveColumn(tableColumns.size() - 1, visibleIndex);
                    }
                    return;
                }
                if(testColumn == visibleColumn) {
                    ++visibleIndex;
                }
            }
        }
    }
    
    public boolean isColumnVisible(TableColumn aColumn) {
        return (tableColumns.indexOf(aColumn) >= 0);
    }
    
    public TableColumn getColumnModelIndex(int modelColumnIndex) {
        for (int cCount = 0; cCount < mvAllTableColumns.size(); ++cCount) {
            TableColumn col = mvAllTableColumns.elementAt(cCount); //(TableColumn)
            if (col.getModelIndex() == modelColumnIndex) {
                return col;
            }
        }
        return null;
    }
    
    public void setAllColumnsVisible() {
        int noColumns = mvAllTableColumns.size();
        for (int columnIndex = 0; columnIndex < noColumns; ++columnIndex) {
            TableColumn visibleColumn = (columnIndex < tableColumns.size()
                    ? tableColumns.get(columnIndex)
                    : null); // (TableColumn)
            TableColumn invisibleColumn = mvAllTableColumns.get(columnIndex); // (TableColumn)
            
            if (visibleColumn != invisibleColumn) {
                super.addColumn(invisibleColumn);
                super.moveColumn(tableColumns.size() - 1, columnIndex);
            }
        }
    }
    
    @Override
    public void addColumn(TableColumn column) {
        mvAllTableColumns.addElement(column);
        super.addColumn(column);
    }
    
    @Override
    public void removeColumn(TableColumn column) {
        int allColumnsIndex = mvAllTableColumns.indexOf(column);
        if (allColumnsIndex != -1)
            mvAllTableColumns.removeElementAt(allColumnsIndex);
        super.removeColumn(column);
        //mvAllTableColumns.remove(column);
    }
    
    @Override
    public void moveColumn(int oldIndex, int newIndex) {
	if ((oldIndex < 0) || (oldIndex >= getColumnCount()) ||
	    (newIndex < 0) || (newIndex >= getColumnCount()))
	    throw new IllegalArgumentException("moveColumn() - Index out of range");
        
        TableColumn fromColumn = tableColumns.get(oldIndex); // (TableColumn)
        TableColumn toColumn = tableColumns.get(newIndex); // (TableColumn) 
        
        int allColumnsOldIndex = mvAllTableColumns.indexOf(fromColumn);
        int allColumnsNewIndex = mvAllTableColumns.indexOf(toColumn);

        if(oldIndex != newIndex) {
            mvAllTableColumns.removeElementAt(allColumnsOldIndex);
            mvAllTableColumns.insertElementAt(fromColumn, allColumnsNewIndex);
        }
        
        super.moveColumn(oldIndex, newIndex);
    }

    public int getColumnCount(boolean onlyVisible) {
        Vector columns = (onlyVisible ? tableColumns : mvAllTableColumns);
	return columns.size();
    }

    public Enumeration getColumns(boolean onlyVisible) {
        Vector columns = (onlyVisible ? tableColumns : mvAllTableColumns);
        
	return columns.elements();
    }

    public int getColumnIndex(Object identifier, boolean onlyVisible) {
	if (identifier == null) {
	    throw new IllegalArgumentException("Identifier is null");
	}

        Vector<TableColumn> columns = (onlyVisible
                ? tableColumns : mvAllTableColumns);
        int numCols = columns.size();
        TableColumn column;
        
        for (int columnIndex = 0; columnIndex < numCols; ++columnIndex) {
	    column = columns.get(columnIndex); // (TableColumn)
            if (identifier.equals(column.getIdentifier()))
		return columnIndex;
        }
        
	throw new IllegalArgumentException("Identifier not found (" + identifier
                + ")");
    }

    public TableColumn getColumn(int columnIndex, boolean onlyVisible) {
	return  mvAllTableColumns.elementAt(columnIndex); // (TableColumn)
    }    
    
}
