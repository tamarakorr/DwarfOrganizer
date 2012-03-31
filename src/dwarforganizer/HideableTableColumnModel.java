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
 *
 * Credit for this class to Stephen Kelvin (http://www.stephenkelvin.de/XTableColumnModel/)
 * (used with permission).
 * See MIT license in license.txt
 * 
 * NOTE: Remember to call table.createDefaultColumnsFromModel() after setting
 * this as the column model. Otherwise all table columns will be invisible.
 * 
 * /**
 * <code>XTableColumnModel</code> extends the DefaultTableColumnModel .
 * It provides a comfortable way to hide/show columns.
 * Columns keep their positions when hidden and shown again.
 *
 * In order to work with JTable it cannot add any events to <code>TableColumnModelListener</code>.
 * Therefore hiding a column will result in <code>columnRemoved</code> event and showing it
 * again will notify listeners of a <code>columnAdded</code>, and possibly a <code>columnMoved</code> event.
 * For the same reason the following methods still deal with visible columns only:
 * getColumnCount(), getColumns(), getColumnIndex(), getColumn()
 * There are overloaded versions of these methods that take a parameter <code>onlyVisible</code> which let's
 * you specify whether you want invisible columns taken into account.
 *
 * @version 0.9 04/03/01
 * @author Stephen Kelvin, mail@StephenKelvin.de
 * @see DefaultTableColumnModel
 */
public class HideableTableColumnModel extends DefaultTableColumnModel {

    protected Vector<TableColumn> mvAllTableColumns = new Vector<TableColumn>();
    
    public HideableTableColumnModel() {
        super();
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
                TableColumn visibleColumn = (visibleIndex < noVisibleColumns ? (TableColumn)tableColumns.get(visibleIndex) : null);
                TableColumn testColumn = (TableColumn) mvAllTableColumns.get(invisibleIndex);
                
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
            TableColumn col = (TableColumn) mvAllTableColumns.elementAt(cCount);
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
                    ? (TableColumn) tableColumns.get(columnIndex)
                    : null);
            TableColumn invisibleColumn = (TableColumn) mvAllTableColumns.get(columnIndex);
            
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
        
        TableColumn fromColumn  = (TableColumn) tableColumns.get(oldIndex);
        TableColumn toColumn    = (TableColumn) tableColumns.get(newIndex);
        
        int allColumnsOldIndex  = mvAllTableColumns.indexOf(fromColumn);
        int allColumnsNewIndex  = mvAllTableColumns.indexOf(toColumn);

        if(oldIndex != newIndex) {
            mvAllTableColumns.removeElementAt(allColumnsOldIndex);
            mvAllTableColumns.insertElementAt(fromColumn, allColumnsNewIndex);
        }
        
        super.moveColumn(oldIndex, newIndex);
    }

    /**
     * Returns the total number of columns in this model.
     *
     * @param   onlyVisible   if set only visible columns will be counted
     * @return	the number of columns in the <code>tableColumns</code> array
     * @see	#getColumns
     */
    public int getColumnCount(boolean onlyVisible) {
        Vector columns = (onlyVisible ? tableColumns : mvAllTableColumns);
	return columns.size();
    }

    /**
     * Returns an <code>Enumeration</code> of all the columns in the model.
     *
     * @param   onlyVisible   if set all invisible columns will be missing from the enumeration.
     * @return an <code>Enumeration</code> of the columns in the model
     */
    public Enumeration getColumns(boolean onlyVisible) {
        Vector columns = (onlyVisible ? tableColumns : mvAllTableColumns);
        
	return columns.elements();
    }

    /**
     * Returns the position of the first column whose identifier equals <code>identifier</code>.
     * Position is the the index in all visible columns if <code>onlyVisible</code> is true or
     * else the index in all columns.
     *
     * @param	identifier   the identifier object to search for
     * @param	onlyVisible  if set searches only visible columns
     *
     * @return		the index of the first column whose identifier
     *			equals <code>identifier</code>
     *
     * @exception       IllegalArgumentException  if <code>identifier</code>
     *				is <code>null</code>, or if no
     *				<code>TableColumn</code> has this
     *				<code>identifier</code>
     * @see		#getColumn
     */
    public int getColumnIndex(Object identifier, boolean onlyVisible) {
	if (identifier == null) {
	    throw new IllegalArgumentException("Identifier is null");
	}

        Vector      columns     = (onlyVisible ? tableColumns : mvAllTableColumns);
        int         noColumns   = columns.size();
        TableColumn column;
        
        for(int columnIndex = 0; columnIndex < noColumns; ++columnIndex) {
	    column = (TableColumn)columns.get(columnIndex);

            if(identifier.equals(column.getIdentifier()))
		return columnIndex;
        }
        
	throw new IllegalArgumentException("Identifier not found");
    }

    /**
     * Returns the <code>TableColumn</code> object for the column
     * at <code>columnIndex</code>.
     *
     * @param	columnIndex	the index of the column desired
     * @param	onlyVisible	if set columnIndex is meant to be relative to all visible columns only
     *                          else it is the index in all columns
     *
     * @return	the <code>TableColumn</code> object for the column
     *				at <code>columnIndex</code>
     */
    public TableColumn getColumn(int columnIndex, boolean onlyVisible) {
	return (TableColumn) mvAllTableColumns.elementAt(columnIndex);
    }    
    
}
