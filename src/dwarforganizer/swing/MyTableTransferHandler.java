/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

/**
 * A transfer handler for JTables supporting copy/cut/paste of single cells
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class MyTableTransferHandler extends TransferHandler {

    public MyTableTransferHandler() {
        super();
    }

    @Override
    protected Transferable createTransferable(final JComponent comp) {
        if (! (comp instanceof JTable))
            return null;

        final JTable table = (JTable) comp;
        final int[] rows = table.getSelectedRows();
        final int[] cols = table.getSelectedColumns();
        if (rows == null || cols == null
                || rows.length != 1 || cols.length != 1)
            return null;

        final Object value = table.getValueAt(rows[0], cols[0]);
        if (value == null)
            return null;

        return new MyTransferable(value);
    }

    @Override
    public int getSourceActions(final JComponent comp) {
        return MyTableTransferHandler.COPY;
    }

    @Override
    public boolean importData(final JComponent comp
            , final Transferable transferable) {

        if (! (comp instanceof JTable))
            return false;

        final JTable table = (JTable) comp;
        return importCellData(table, transferable);
    }

    protected boolean importCellData(final JTable table
            , final Transferable transferable) {

        final int[] rows = table.getSelectedRows();
        final int[] cols = table.getSelectedColumns();

        if (rows == null || cols == null || rows.length != 1
                || cols.length != 1)
            return false;

        final int rowIndex = rows[0];
        final int colIndex = cols[0];

        if (table.isCellEditable(rowIndex, colIndex) == false)
            return false;
        if (importCellObject(table, rowIndex, colIndex, transferable))
            return true;

        final Class valueClass = table.getColumnClass(colIndex);
        final PropertyEditor editor = PropertyEditorManager.findEditor(
                valueClass);
        final DataFlavor stringFlavor = getStringFlavor(transferable);
        if (editor == null || stringFlavor == null)
            return false;

        try {
            //System.out.println("Converting String to " + valueClass.getSimpleName());
            editor.setAsText(
                    (String) transferable.getTransferData(stringFlavor));
            setCellValue(table, rowIndex, colIndex, editor.getValue());
            return true;
        } catch (final UnsupportedFlavorException ignore) {
        } catch (final IOException ignore) {
        } catch (final IllegalArgumentException ignore) {
            Toolkit.getDefaultToolkit().beep();
        }

        return false;
    }

    protected DataFlavor getStringFlavor(final Transferable transferable) {
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
            return DataFlavor.stringFlavor;

        final DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (final DataFlavor flavor : flavors) {
            if (flavor.getMimeType().startsWith("text/plain"))
                return flavor;
        }
        return null;
    }

    protected boolean importCellObject(final JTable table, final int row
            , final int col, final Transferable transferable) {

        final Class cls = table.getColumnClass(col);
        if (cls.equals(String.class))
            return false;

        for (final DataFlavor flavor : transferable.getTransferDataFlavors()) {
            if (flavor.getRepresentationClass().equals(cls)) {
                try {
                    //System.out.println("Importing " + flavor.getHumanPresentableName());
                    setCellValue(table, row, col
                            , transferable.getTransferData(flavor));
                    return true;
                } catch (final UnsupportedFlavorException ignore) {
                } catch (final IOException ignore) {
                }
            }
        }
        return false;
    }
    protected void setCellValue(final JTable table, final int row, final int col
            , final Object newValue) {

        // Uncomment to support a MyUndoTable
        //if (! (table instanceof MyUndoTable))
            table.setValueAt(newValue, row, col);
        //else {
        //    MyUndoTable undoTable = (MyUndoTable) table;
        //    undoTable.setValueAt(newValue, row, col, true, "Paste");
        //}
    }
}
