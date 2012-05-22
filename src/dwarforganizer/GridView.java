/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;
import dwarforganizer.swing.HideableTableColumnModel;
import java.util.List;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class GridView implements MyPropertyGetter, DeepCloneable<GridView> {

    public static final String UNSAVED_VIEW_NAME = "Unsaved View";

    private String mstrName;

    private Object moKey;
    public enum KeyAxis { X_AXIS, Y_AXIS };
    private KeyAxis moKeyAxis;

    private boolean mbLabelY;
    //private List<GridFilter> mlstGridFilter;
    //private List<GridColumn> mlstGridColumn;
    //private List<GridColumn> mlstColOrder;
    private List<Object> mlstColOrder;

    // TODO: Object keyAttribute, KeyAxis axis, boolean labelY
    public GridView(final String name, final List<Object> colOrder) {
        super();

        mstrName = name;
        //moKey = keyAttribute;
        //moKeyAxis = axis;
        //mbLabelY = labelY;
        mlstColOrder = colOrder;
    }

    public String getName() {
        return mstrName;
    }

    public void setName(final String name) {
        this.mstrName = name;
    }

    public boolean isLabelY() {
        return mbLabelY;
    }

    public void setLabelY(final boolean labelY) {
        this.mbLabelY = labelY;
    }

    public List<Object> getColOrder() {
        return mlstColOrder;
    }

    public void setColOrder(final List<Object> colOrder) {
        this.mlstColOrder = colOrder;
    }

    public Object getKeyAttribute() {
        return moKey;
    }

    public void setKeyAttribute(final Object key) {
        this.moKey = key;
    }

    public KeyAxis getKeyAxis() {
        return moKeyAxis;
    }

    public void setKeyAxis(final KeyAxis keyAxis) {
        this.moKeyAxis = keyAxis;
    }

/*    public class GridAttribute {
        private Object attribute;
        public GridAttribute(Object attribute) {
            super();
            this.attribute = attribute;
        }
    } */
    public abstract class GridColumn {
        public GridColumn() {
            super();
        }
        public abstract boolean isInView(Object attribute);
    }
    public class AttributeColumn extends GridColumn {
        private Object attribute;
        public AttributeColumn(final Object attribute) {
            super();
            this.attribute = attribute;
        }

        @Override
        public boolean isInView(final Object attribute) {
            return this.attribute.equals(attribute);
        }
    }
    public class ColumnGroup extends GridColumn {
        private List<Object> columnGroup;
        public ColumnGroup(final List<Object> columnGroup) {
            super();
            this.columnGroup = columnGroup;
        }

        @Override
        public boolean isInView(final Object attribute) {
            return this.columnGroup.contains(attribute);
        }
    }
    public void applyToTable(final CompositeTable table
            , final TableColumnModel columnModel) {
        // Hide all columns not in the column ordering list---------------------

        hideColumns(columnModel); //table.getMainTable()
        reorderTableColumns(columnModel, mlstColOrder); // table.getMainTable()

        //TODO: the rest
    }
    private void hideColumns(final TableColumnModel columnModel) { //JTable table
        if (! HideableTableColumnModel.class.isAssignableFrom(
                columnModel.getClass())) {
            System.err.println("[GridView] Could not set column visibility: "
                    + "table must use HideableTableColumnModel.");
        }
        else {
            // Show or hide each column based on presence in view
            final HideableTableColumnModel hideableModel
                    = (HideableTableColumnModel) columnModel;
            for (int iCount = 0; iCount < hideableModel.getColumnCount(false); iCount++) {
                final Object identifier = hideableModel.getColumn(iCount
                        , false).getIdentifier();
                hideableModel.setColumnVisible(hideableModel.getColumn(iCount
                        , false), isColumnInView(identifier));
            }
        }

    }
    private void reorderTableColumns(final TableColumnModel columnModel
            , final List<Object> order) {

        int iCount = 0;
        for (final Object identifier : order) {
            columnModel.moveColumn(columnModel.getColumnIndex(identifier)
                    , iCount++);
            //table.moveColumn(MyHandyTable.getColByName(table
            //        , identifier.toString()), iCount++);
        }
    }
    private void reorderTableColumns(final CompositeTable compTable
            , final List<Object> order) {

        int col;

        int iCount = 0;
        for (final Object identifier : order) {
            for (final JTable table : compTable.getTables()) {
                try {
                    col = table.getColumnModel().getColumnIndex(identifier);
                    moveColumn(compTable, table, col, iCount++);
                } catch (IllegalArgumentException ignore) {
                    // Column isn't in this table
                }
            }
        }
    }
    private void moveColumn(final CompositeTable compTable, final JTable source
            , final int sourceIndex, final int overallDestIndex) {
        compTable.moveColumn(source, sourceIndex, overallDestIndex);
    }
    private boolean isColumnInView(final Object identifier) {
        //System.out.println(identifier.toString() + " is in view: "
        //        + mlstColOrder.contains(identifier));
        return mlstColOrder.contains(identifier);
    }

    @Override
    public Object getProperty(final String propName
            , final boolean humanReadable) {

        final String prop = propName.toLowerCase();

        if (prop.equals("name"))
            return getName();
        else {
            return "[GridView] Unsupported property: " + propName;
        }
    }

    @Override
    public long getKey() {
        // TODO
        return 0;
    }

    @Override
    public GridView deepClone() {
        // NOTE: mlstColOrder is not deep-cloned
        // moKey, moKeyAxis, mbLabelY,
        return new GridView(mstrName, mlstColOrder);
    }

    // Note: We do not check the name
    public boolean equals(final GridView otherView) {

        //System.out.println("equals()");

        // Not null
        if (otherView == null) {
            //System.out.println("Unequal: null");
            return false;
        }

        // Check name-----------------------------------------------------------
        // Name is ignored if otherView's name is UNSAVED_VIEW_NAME.

        if ((! otherView.getName().equals(UNSAVED_VIEW_NAME))
                && (! mstrName.equals(UNSAVED_VIEW_NAME))
                && (! otherView.getName().equals(mstrName))) {
            //System.out.println("Names unequal");
            return false;
        }

        // Check column ordering------------------------------------------------
        final List<Object> otherOrder = otherView.getColOrder();
        final int size = otherOrder.size();

        // If column ordering lists aren't the same size, unequal
        if (mlstColOrder.size() != size) {
            //System.out.println("Size unequal");
            return false;
        }

        // If any column ordering objects in the list are unequal, unequal
        for (int iCount = 0; iCount < size; iCount++) {
            if (! mlstColOrder.get(iCount).equals(otherOrder.get(iCount))) {
                //System.out.println(mlstColOrder.get(iCount).toString() + " != "
                //        + otherOrder.get(iCount).toString());
                return false;
            }
        }

        // Objects are equal
        //System.out.println("Equal");
        return true;
    }
}
