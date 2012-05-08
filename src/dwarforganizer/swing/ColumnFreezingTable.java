/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import myutils.MyHandyTable;
import myutils.MyTableWidthAdjuster;

/**
 * Allows a left-to-right table to appear with columns frozen:
 *
 * Requirements
 * The horizontal scrollbar policy of the main table must be
 * ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS or
 * ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER. Results are unpredictable
 * otherwise.
 *
 * table.getUpdateSelectionOnSort() must be false for the main table.
 * Otherwise the selection will not maintain properly when the tables are
 * sorted.
 *
 * All changes to the table column model should be applied to the master column
 * model. (Examples: hiding columns, removing columns, moving columns, adding
 * columns) A reference to the model is obtainable via getAllColumnsModel().
 *
 * The frozen table and main table share a model.
 * Changes to the model and selection update the frozen table.
 * The divider snaps to fit exactly between columns.
 * Sorting works and maintains the selected row(s).
 * Scrolling the main table updates the frozen one. The frozen one can't be
 * scrolled.
 *
 * @author Tamara Orr
 */
public class ColumnFreezingTable implements RowSorterListener {

    private static final int DEFAULT_SNAP = 5;
    private static final int DIVIDER_SIZE = 6;

    private boolean snapping = false;
    private int[] selectedModelRows = new int[0];

    private JScrollPane leftPane;
    private JTable leftTable;
    private JScrollPane rightPane;
    private JTable rightTable;
    private JSplitPane split;

    TableColumnModel mcmAllColumns;

    public ColumnFreezingTable(JScrollPane rightPane
            , TableColumnModel cmAllColumns) {

        this.rightPane = rightPane;

        // The horizontal scrollbars must either always appear, or never appear.
        // Otherwise a scrollbar shown in one pane but not the other
        // puts the row display out of sync
        if (rightPane.getHorizontalScrollBarPolicy()
                == ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
            System.err.println("[ColumnFreezingTable] Invalid horizontal"
                    + " scrollbar policy: The object will have unexpected"
                    + " behavior");

        rightTable = (JTable) rightPane.getViewport().getView();
        if (rightTable.getUpdateSelectionOnSort())
            System.err.println("[ColumnFreezingTable] Invalid value for"
                    + " table.getUpdateSelectionOnSort() : true. Selections"
                    + " will show unexpected behavior on sorting.");

        // .setAutoCreateColumnsFromModel(false); May be important

        // Set column model and create it from rightTable's columns
        mcmAllColumns = cmAllColumns;
        setAllColumns();
        mcmAllColumns.addColumnModelListener(createTableColumnModelListener());

        // Create split controls
        leftTable = new JTable();
        leftTable.setAutoCreateColumnsFromModel(false);
        leftTable.setUpdateSelectionOnSort(false);  // We need to do this manually since we're sharing a sorter
        leftTable.setFocusable(false);  // Prevents user scrolling out of sync, such as with arrow keys
        
        leftTable.setModel(rightTable.getModel());
        leftTable.setSelectionModel(rightTable.getSelectionModel());
        leftTable.setRowSorter(rightTable.getRowSorter());
        leftTable.setPreferredScrollableViewportSize(
                leftTable.getPreferredSize());
        leftTable.getRowSorter().addRowSorterListener(this);

        // Copy object cell renderers
        // There seems to be no easy way to enumerate installed renderers.
        // So we just copy the defined default renderers here:
        Class[] rendererClasses = { Object.class, Boolean.class, Number.class };
        for (Class cls : rendererClasses) {
            leftTable.setDefaultRenderer(cls
            , rightTable.getDefaultRenderer(cls));
        }

        // Don't show a vertical scrollbar for the left pane; copy horizontal
        // policy
        leftPane = new JScrollPane(leftTable);
        
        // Instead of setting the policy to NEVER (which breaks the mouse
        // wheel), just hide the vertical scrollbar (allowing default
        // mouse wheel behavior).
        //leftPane.setVerticalScrollBarPolicy(
        //        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        leftPane.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        leftPane.setHorizontalScrollBarPolicy(
                rightPane.getHorizontalScrollBarPolicy());
        MyHandyTable.autoResizeTableColumns(leftTable);
        
        // leftTable and rightTable
        rightTable.addPropertyChangeListener(createModelSynchronizer(rightTable
                , leftTable));
        
        // Synchronize scrolling
        this.rightPane.getViewport().addChangeListener(createScrollSynchronizer(
                leftPane));
        leftPane.getViewport().addChangeListener(createScrollSynchronizer(
                this.rightPane));
        
        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, leftPane
                , rightPane);
        split.setDividerSize(DIVIDER_SIZE);
        safeSnap(DEFAULT_SNAP);
        split.addPropertyChangeListener("dividerLocation"
                , new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!snapping) {
                    updateDividerLocation((Integer) evt.getOldValue()
                            , (Integer) evt.getNewValue());
                }
            }
        });

        // Calculate the maximum snap position
        //mintMaxSnap = calcMaxSnap();
    }
    
    public int getMaxSnap() {
        return calcMaxSnap();
    }

    // Returns the maximum value we want to allow the divider to ever move to.
    // We always want at least one column to be on the right side, to allow
    // for proper scrolling and focus.
    private int calcMaxSnap() {
        // Get the maximum number of columns we want to allow in the left
        // table. (If the right viewport isn't full, this is all columns.
        // Otherwise this is the total number of visible columns - 1.)
        int leftColumns = leftTable.getColumnCount();
        int lastVisibleColIndex = rightTable.columnAtPoint(new Point(
                rightPane.getViewport().getViewRect().width, 0));
        int maxLeftColumns;

        if (lastVisibleColIndex == -1)
            maxLeftColumns = mcmAllColumns.getColumnCount();
        else
            maxLeftColumns = leftColumns + lastVisibleColIndex; // (This is one less column than the total)
        //System.out.println("Max left columns: " + maxLeftColumns);

        // Get the total width of the maximum number of columns we want to allow
        // in the left table. We sum these widths from leftTable and rightTable
        // (instead of mcmAllColumns) because mcmAllColumns is not
        // width-adjusted.
        int totalWidth = 0;
        totalWidth += sumColumnWidth(leftTable);
        if (maxLeftColumns > leftColumns) {
            //System.out.println("Getting width of " + (maxLeftColumns - leftColumns) + " columns");
            totalWidth += sumColumnWidth(rightTable, maxLeftColumns - leftColumns);
        }
        return totalWidth;
    }

    private TableColumnModelListener createTableColumnModelListener() {
        return new TableColumnModelListener() {

            // This occurs after the column has already been added
            @Override
            public void columnAdded(TableColumnModelEvent e) {
                // Add the column to the proper table
                int index = e.getToIndex();
                //System.out.println("Adding column at " + index);
                TableColumn tableColumn = mcmAllColumns.getColumn(index);
                JTable table = getTableForIndex(index);
                int addAtIndex = convertAllIndexToLocal(index);
                boolean isLeftTable = table.equals(leftTable);

                //System.out.println(isLeftTable + ", " + table.equals(rightTable));

                table.addColumn(tableColumn);
                // TODO: This was throwing IllegalArgumentException from
                // HideableTableColumnModel sometimes:
                table.moveColumn(table.getColumnCount() - 1, addAtIndex);

                // Move the divider if needed
                if (isLeftTable) {
                    updateDividerLocation();
                    /*int location = split.getDividerLocation();
                    updateDividerLocation(location, location); */
                }
            }

            // This occurs after the column has already been removed
            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                int index = e.getFromIndex();
                JTable table = getTableForIndex(index);
                int removedIndex = convertAllIndexToLocal(index);
                boolean isLeftTable = table.equals(leftTable);

                TableColumn tableColumn = table.getColumnModel().getColumn(
                        removedIndex);
                //System.out.println("Column removed: index " + index + ", "
                //    + tableColumn.getIdentifier());
                table.removeColumn(tableColumn);

                // Move the divider if needed
                if (isLeftTable) {
                    int newWidth = sumColumnWidth(leftTable);
                    updateDividerLocation(newWidth, newWidth);
                }
            }

            // This occurs after the column has already been moved
            @Override
            public void columnMoved(TableColumnModelEvent e) {
                //System.out.println("Column moved");
                int srcIndex = e.getFromIndex();
                int destIndex = e.getToIndex();
                TableColumn tableColumn = mcmAllColumns.getColumn(destIndex);
                int newIndex = convertAllIndexToLocal(destIndex);
                JTable source = getTableForIndex(srcIndex);
                JTable dest = getTableForIndex(destIndex);

                MyHandyTable.moveColumn(tableColumn, source, dest, newIndex);

                // Move the divider if needed
                if (dest.equals(leftTable)) {
                    updateDividerLocation();
                    /*int location = split.getDividerLocation();
                    updateDividerLocation(location, location); */
                }
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) { // Do nothing
            }
            @Override
            public void columnSelectionChanged(ListSelectionEvent e) { // Do nothing
            }
        };
    }

    private int sumColumnWidth(JTable table) {
        return sumColumnWidth(table.getColumnModel(), table.getColumnCount());
    }
    private int sumColumnWidth(JTable table, int numColumns) {
        return sumColumnWidth(table.getColumnModel(), numColumns);
    }
    private int sumColumnWidth(TableColumnModel cm, int numColumns) {
        int total = 0;
        for (int iCount = 0; iCount < numColumns; iCount++) {
            total += cm.getColumn(iCount).getWidth();
        }
        return total;
    }
    private JTable getTableForIndex(int allColumnsIndex) {
        if (allColumnsIndex < leftTable.getColumnCount())
            return leftTable;
        else
            return rightTable;
    }
    private int convertAllIndexToLocal(int allColumnsIndex) {
        int leftColumns = leftTable.getColumnCount();
        if (allColumnsIndex < leftColumns)
            return allColumnsIndex;
        else
            return allColumnsIndex - leftColumns;
    }
    // Returns the fixed component
    public JTable getFixedTable() {
        return leftTable;
    }

    // Returns the splitpane component
    public JSplitPane getSplitPane() {
        return split;
    }

    // Holds a column movement instruction
    private class ColToMove {

        TableColumn col;
        JTable source;
        JTable dest;
        int position;

        public ColToMove(TableColumn col, JTable source, JTable dest
                , int position) {
            this.col = col;
            this.source = source;
            this.dest = dest;
            this.position = position;
        }
    }

    // Call this if columns may need to be moved to the other side of the divider
    // due to resizing.
    private void updateDividerLocation() {
        int location = split.getDividerLocation();
        updateDividerLocation(location, location);
    }
    // Moves columns between tables if needed, and snaps divider line
    // to a column boundary.
    // If the boundary is dragged past half the column's width,
    // moves that column.
    private void updateDividerLocation(int oldValue, int newValue) {
        int snap;
        int totalColWidth;
        int position;
        int thisColWidth;
        int halfThisColWidth;
        int maxSnap = calcMaxSnap();

        List<ColToMove> list = new ArrayList<ColToMove>();

        totalColWidth = 0;
        position = 0;
        snap = DEFAULT_SNAP;
        if (newValue > maxSnap) newValue = maxSnap;

        // Do any left table columns need to be moved to the right table?
        for (int iCount = 0; iCount < leftTable.getColumnCount(); iCount++) {
            TableColumn col = leftTable.getColumn(leftTable.getColumnName(iCount));
            thisColWidth = col.getWidth();
            halfThisColWidth = thisColWidth / 2;
            totalColWidth += thisColWidth;

            // If the divider was not dragged past the halfway point of this
            // column:
            if (totalColWidth - halfThisColWidth <= newValue) {
                snap = totalColWidth;
            }
            // Set this column to be moved:
            else { // if (width > newValue) {
                //System.out.println("Moving " + col.getIdentifier() + " to main table, position " + position);
                list.add(new ColToMove(col, leftTable, rightTable, position));
                position++;
            }
        }

        // Do any right table columns need to be moved to the left table?
        // (Allow all but the last column to be moved.)
        if (newValue > oldValue) {
            position = leftTable.getColumnCount();
            for (int iCount = 0; iCount < rightTable.getColumnCount() - 1; iCount++) {
                TableColumn col = rightTable.getColumn(rightTable.getColumnName(
                        iCount));
                thisColWidth = col.getWidth();
                totalColWidth += thisColWidth;
                halfThisColWidth = thisColWidth / 2;
                if (totalColWidth - halfThisColWidth <= newValue) {
                    //System.out.println("Moving " + col.getIdentifier() + " to fixed table, position " + position);
                    list.add(new ColToMove(col, rightTable, leftTable
                            , position));
                    position++;
                    snap = totalColWidth;
                }
                else {
                    break;
                }
            }
        }

        moveColumns(list);

        //System.out.println(newValue + " " + snap);
        safeSnap(snap);
    }
    // Snaps the divider without calling updateDividerLocation (does not check
    // to move any columns)
    private void safeSnap(int location) {
        // Add DIVIDER_SIZE + 1 pixels to avoid cutting off any columns on the
        // left of the divider
        snapping = true;
        split.setDividerLocation(location + DIVIDER_SIZE + 1);
        snapping = false;
    }
    public void snapToDefault() {
        split.setDividerLocation(DEFAULT_SNAP);
    }
    // Executes the list of column movement instructions
    private void moveColumns(List<ColToMove> list) {
        for (ColToMove moveIt : list) {
            //System.out.println("Requesting move " + moveIt.col.getIdentifier()
            //        + " to " + moveIt.position);
            MyHandyTable.moveColumn(moveIt.col, moveIt.source, moveIt.dest
                    , moveIt.position);
        }
    }

    //  Keep the left table in sync with the main table
    private PropertyChangeListener createModelSynchronizer(final JTable leader
            , final JTable follower) {
        
        return new PropertyChangeListener() {
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("selectionModel".equals(evt.getPropertyName())) {
                    follower.setSelectionModel(leader.getSelectionModel());
                }

                if ("model".equals(evt.getPropertyName())) {
                    follower.setModel(leader.getModel());
                }
            }
        };
    }

    // Keep scroll state up to date
    private ChangeListener createScrollSynchronizer(final JScrollPane follower) {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JViewport viewport = (JViewport) e.getSource();
                follower.getVerticalScrollBar().setValue(
                        viewport.getViewPosition().y);
            }
        };
    }
    // Copy selection in side-by-side tables to clipboard
    public void copyToClipboard(boolean isCut) {
        MyHandyTable.copyToClipboard(new JTable[] { leftTable, rightTable }
            , false);
    }

    // When the sorter does something, maintain the selected rows
    @Override
    public void sorterChanged(RowSorterEvent e) {
        // This only works if events are guaranteed to always
        // be received in order (SORT_ORDER_CHANGED, SORTED)...

        // Capture the current selected model row just before the sort
        if (e.getType().equals(RowSorterEvent.Type.SORT_ORDER_CHANGED)) {
            //System.out.println("Capturing");
            captureSelectedRows();
        }
        // Re-select the appropriate rows after the sort
        else if (e.getType().equals(RowSorterEvent.Type.SORTED)) {
            //System.out.println("Setting selected");
            setSelectedRows();
        }
    }
    // Saves the currently selected rows by model index
    private void captureSelectedRows() {
        int[] viewRows = leftTable.getSelectedRows();
        selectedModelRows = new int[viewRows.length];
        for (int iCount = 0; iCount < viewRows.length; iCount++) {
            selectedModelRows[iCount]
                    = leftTable.convertRowIndexToModel(viewRows[iCount]);
        }
        //print(selectedModelRows);
    }
    // Sets the currently selected rows from the saved model indices
    private void setSelectedRows() {
        int newRow;

        if (selectedModelRows.length > 0) {
            leftTable.clearSelection();
            for (int iCount = 0; iCount < selectedModelRows.length; iCount++) {
                newRow = leftTable.convertRowIndexToView(
                        selectedModelRows[iCount]);
                //System.out.println("Selecting [view] Row# " + newRow);
                leftTable.getSelectionModel().addSelectionInterval(
                        newRow, newRow);
            }
        }
    }
    private void print(int[] selectedRows) {
        for (int i : selectedRows)
            System.out.println("'Row " + (i + 1) + "'");
        System.out.println("-----");
    }
    private void setAllColumns() {
        TableColumn tableColumn;
        for (int iCount = 0; iCount < rightTable.getColumnCount(); iCount++) {
            tableColumn = rightTable.getColumnModel().getColumn(iCount);
            //System.out.println("Adding " + tableColumn.getIdentifier());
            mcmAllColumns.addColumn(tableColumn);
        }
    }
    public TableColumnModel getAllColumnsModel() {
        return mcmAllColumns;
    }
    public void autoResizeTableColumn(Object identifier) {
        autoResizeTableColumns(new Object[] { identifier });
    }
    // Autosize all
    public void autoResizeTableColumns() {
        MyHandyTable.autoResizeTableColumns(
                new JTable[] { leftTable, rightTable });
    }
    // Autosize the given columns
    // (We can't just call autoresize on the array because
    // we don't know which table each identifier is in)
    public void autoResizeTableColumns(Object[] identifiers) {
        MyTableWidthAdjuster adj;
        int col;
        JTable[] tables = new JTable[] { leftTable, rightTable };

        for (JTable table : tables) {
            adj = new MyTableWidthAdjuster(table);
            for (Object identifier : identifiers) {
                try {
                    col = table.getColumnModel().getColumnIndex(identifier);
                    MyHandyTable.autoResizeTableColumn(col, adj);
                    updateDividerLocation();
                } catch (IllegalArgumentException ignore) {

                }
            }
        }
    }
}
