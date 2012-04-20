/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import myutils.MyHandyTable;

/**
 *
 * @author Tamara Orr
 *
 * 
 * KeyAdapter to detect Windows standard cut, copy and paste keystrokes on a
 * JTable and put them to the clipboard 
 * in Excel friendly plain text format. Assumes that null represents an empty 
 * column for cut operations. 
 * Replaces line breaks and tabs in copied cells to spaces in the clipboard. 
 * 
 * @see java.awt.event.KeyAdapter 
 * @see javax.swing.JTable 
 */
public class ClipboardKeyAdapter extends KeyAdapter {

    private static final String LINE_BREAK = "\n";
    private static final String CELL_BREAK = "\t";
    private static final Clipboard CLIPBOARD
            = Toolkit.getDefaultToolkit().getSystemClipboard();
    private final JTable table;

    private boolean mbAllowCopy;
    private boolean mbAllowCut;
    private boolean mbAllowPaste;
    
    public ClipboardKeyAdapter(JTable table, boolean allowCopy, boolean allowCut
            , boolean allowPaste) {
        this.table = table;
        mbAllowCopy = allowCopy;
        mbAllowCut = allowCut;
        mbAllowPaste = allowPaste;
    }

    @Override
    public void keyReleased(KeyEvent event) {
        if (event.isControlDown()) {
            if ((event.getKeyCode() == KeyEvent.VK_C) && mbAllowCopy) { // Copy
                //System.out.println("Copying");
                MyHandyTable.cancelEditing(table);
                MyHandyTable.copyToClipboard(table, false);
            } else if ((event.getKeyCode() == KeyEvent.VK_X) && mbAllowCut) { // Cut 
                MyHandyTable.cancelEditing(table);
                MyHandyTable.copyToClipboard(table, true);
            } else if ((event.getKeyCode() == KeyEvent.VK_V) && mbAllowPaste) { // Paste 
                MyHandyTable.cancelEditing(table);
                pasteFromClipboard();
            }
        }
    }

    // TODO: Consider moving to MyHandyTable
    private void pasteFromClipboard() {
        int startRow = table.getSelectedRows()[0];
        int startCol = table.getSelectedColumns()[0];

        String pasteString = "";
        try {
            pasteString = (String) (CLIPBOARD.getContents(
                    this).getTransferData(DataFlavor.stringFlavor));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Invalid Paste Type"
                    , "Invalid Paste Type", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] lines = pasteString.split(LINE_BREAK);
        for (int i = 0; i < lines.length; i++) {
            String[] cells = lines[i].split(CELL_BREAK);
            for (int j = 0; j < cells.length; j++) {
                if (table.getRowCount() > startRow + i
                        && table.getColumnCount() > startCol + j) {
                    table.setValueAt(cells[j], startRow + i, startCol + j);
                }
            }
        }
    }
} 
