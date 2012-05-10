/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.swing.HideableTableColumnModel;
import dwarforganizer.deepclone.DeepCloneable;
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
    public GridView(String name, List<Object> colOrder) {
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

    public void setName(String name) {
        this.mstrName = name;
    }

    public boolean isLabelY() {
        return mbLabelY;
    }

    public void setLabelY(boolean labelY) {
        this.mbLabelY = labelY;
    }

    public List<Object> getColOrder() {
        return mlstColOrder;
    }

    public void setColOrder(List<Object> colOrder) {
        this.mlstColOrder = colOrder;
    }

    public Object getKeyAttribute() {
        return moKey;
    }

    public void setKeyAttribute(Object key) {
        this.moKey = key;
    }

    public KeyAxis getKeyAxis() {
        return moKeyAxis;
    }

    public void setKeyAxis(KeyAxis keyAxis) {
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
        public AttributeColumn(Object attribute) {
            super();
            this.attribute = attribute;
        }

        @Override
        public boolean isInView(Object attribute) {
            return this.attribute.equals(attribute);
        }
    }
    public class ColumnGroup extends GridColumn {
        private List<Object> columnGroup;
        public ColumnGroup(List<Object> columnGroup) {
            super();
            this.columnGroup = columnGroup;
        }

        @Override
        public boolean isInView(Object attribute) {
            return this.columnGroup.contains(attribute);
        }
    }
    public void applyToTable(CompositeTable table, TableColumnModel columnModel) {
        // Hide all columns not in the column ordering list---------------------

        hideColumns(columnModel); //table.getMainTable()
        reorderTableColumns(columnModel, mlstColOrder); // table.getMainTable()

        //TODO: the rest
    }
    private void hideColumns(TableColumnModel columnModel) { //JTable table
        if (! HideableTableColumnModel.class.isAssignableFrom(
                columnModel.getClass())) {
            System.err.println("[GridView] Could not set column visibility: "
                    + "table must use HideableTableColumnModel.");
        }
        else {
            // Show or hide each column based on presence in view
            HideableTableColumnModel hideableModel = (HideableTableColumnModel)
                    columnModel;
            for (int iCount = 0; iCount < hideableModel.getColumnCount(false); iCount++) {
                Object identifier = hideableModel.getColumn(iCount
                        , false).getIdentifier();
                hideableModel.setColumnVisible(hideableModel.getColumn(iCount
                        , false), isColumnInView(identifier));
            }
        }

    }
    private void reorderTableColumns(TableColumnModel columnModel
            , List<Object> order) {

        int iCount = 0;
        for (Object identifier : order) {
            columnModel.moveColumn(columnModel.getColumnIndex(identifier)
                    , iCount++);
            //table.moveColumn(MyHandyTable.getColByName(table
            //        , identifier.toString()), iCount++);
        }
    }
    private void reorderTableColumns(CompositeTable compTable
            , List<Object> order) {

        int col;

        int iCount = 0;
        for (Object identifier : order) {
            for (JTable table : compTable.getTables()) {
                try {
                    col = table.getColumnModel().getColumnIndex(identifier);
                    moveColumn(compTable, table, col, iCount++);
                } catch (IllegalArgumentException ignore) {
                    // Column isn't in this table
                }
            }
        }
    }
    private void moveColumn(CompositeTable compTable, JTable source
            , int sourceIndex, int overallDestIndex) {
        compTable.moveColumn(source, sourceIndex, overallDestIndex);
    }
    private boolean isColumnInView(Object identifier) {
        //System.out.println(identifier.toString() + " is in view: "
        //        + mlstColOrder.contains(identifier));
        return mlstColOrder.contains(identifier);
    }

    @Override
    public Object getProperty(String propName, boolean humanReadable) {
        String prop = propName.toLowerCase();

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
    public boolean equals(GridView otherView) {

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
        List<Object> otherOrder = otherView.getColOrder();
        int size = otherOrder.size();

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
