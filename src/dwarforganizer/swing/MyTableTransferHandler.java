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
 */
public class MyTableTransferHandler extends TransferHandler {

    public MyTableTransferHandler() { super(); }
    
    @Override
    protected Transferable createTransferable(JComponent comp) {
        if (! (comp instanceof JTable))
            return null;
        
        JTable table = (JTable) comp;
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        if (rows == null || cols == null
                || rows.length != 1 || cols.length != 1)
            return null;
        
        Object value = table.getValueAt(rows[0], cols[0]);
        if (value == null)
            return null;
        
        return new MyTransferable(value);
    }
    
    @Override
    public int getSourceActions(JComponent comp) {
        return MyTableTransferHandler.COPY;
    }
    
    @Override
    public boolean importData(JComponent comp, Transferable transferable) {
        if (! (comp instanceof JTable))
            return false;
        
        JTable table = (JTable) comp;
        
        return importCellData(table, transferable);

    }
    
    protected boolean importCellData(JTable table, Transferable transferable) {
        
        int[] rows = table.getSelectedRows();
        int[] cols = table.getSelectedColumns();
        
        if (rows == null || cols == null || rows.length != 1 || cols.length != 1)
            return false;
        
        int rowIndex = rows[0];
        int colIndex = cols[0];
        
        if (table.isCellEditable(rowIndex, colIndex) == false)
            return false;
        else if (importCellObject(table, rowIndex, colIndex, transferable))
            return true;
        
        Class valueClass = table.getColumnClass(colIndex);
        PropertyEditor editor = PropertyEditorManager.findEditor(valueClass);
        DataFlavor stringFlavor = getStringFlavor(transferable);
        if (editor == null || stringFlavor == null)
            return false;
        
        try {
            //System.out.println("Converting String to " + valueClass.getSimpleName());
            editor.setAsText((String) transferable.getTransferData(stringFlavor));
            setCellValue(table, rowIndex, colIndex, editor.getValue());
            return true;
        } catch (UnsupportedFlavorException ignore) {
        } catch (IOException ignore) {
        } catch (IllegalArgumentException e) {
            Toolkit.getDefaultToolkit().beep();
        }
        
        return false;
    }
    
    protected DataFlavor getStringFlavor(Transferable transferable) {
        if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor))
            return DataFlavor.stringFlavor;
        
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (DataFlavor flavor : flavors) {
            if (flavor.getMimeType().startsWith("text/plain"))
                return flavor;
        }
        return null;
    }
    
    protected boolean importCellObject(JTable table, int row, int col
            , Transferable transferable) {
        Class cls = table.getColumnClass(col);
        if (cls.equals(String.class))
            return false;
        
        for (DataFlavor flavor : transferable.getTransferDataFlavors()) {
            if (flavor.getRepresentationClass().equals(cls)) {
                try {
                    //System.out.println("Importing " + flavor.getHumanPresentableName());
                    setCellValue(table, row, col, transferable.getTransferData(flavor));
                    return true;
                } catch (UnsupportedFlavorException e) {
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
    protected void setCellValue(JTable table, int row, int col, Object newValue) {
        // Uncomment to support a MyUndoTable
        //if (! (table instanceof MyUndoTable))
            table.setValueAt(newValue, row, col);
        //else {
        //    MyUndoTable undoTable = (MyUndoTable) table;
        //    undoTable.setValueAt(newValue, row, col, true, "Paste");
        //}
    }
}
