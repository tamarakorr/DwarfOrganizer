/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import myutils.MyHandyTable;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
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
public class ClipboardTableHelper {

    private static final Logger logger = Logger.getLogger(
            ClipboardTableHelper.class.getName());

    private static final String PASTE_UNSUPPORTED_MSG = "Paste is currently"
            + " unsupported for multiple tables";

    private static final String LINE_BREAK = "\n";
    private static final String CELL_BREAK = "\t";
    private static final Clipboard CLIPBOARD
            = Toolkit.getDefaultToolkit().getSystemClipboard();
    private final JTable[] tables;

    private boolean mbAllowCopy;
    private boolean mbAllowCut;
    private boolean mbAllowPaste;

    public ClipboardTableHelper(final JTable table, final boolean allowCopy
            , final boolean allowCut, final boolean allowPaste) {

        this.tables = new JTable[] { table };
        mbAllowCopy = allowCopy;
        mbAllowCut = allowCut;
        mbAllowPaste = allowPaste;
    }
    public ClipboardTableHelper(final JTable[] table, final boolean allowCopy
            , final boolean allowCut, final boolean allowPaste) {

        this.tables = table;
        mbAllowCopy = allowCopy;
        mbAllowCut = allowCut;
        if (table.length > 1 && allowPaste) {
            logger.warning(PASTE_UNSUPPORTED_MSG);
            mbAllowPaste = false;
        }
        else
            mbAllowPaste = allowPaste;
    }
    public KeyAdapter createKeyAdapter() {
        return new KeyAdapter() {
            @Override
            public void keyReleased(final KeyEvent e) {
                if (e.isControlDown()) {
                    // Copy
                    if ((e.getKeyCode() == KeyEvent.VK_C) && mbAllowCopy) {
                        doCopy();
                    }
                    // Cut
                    else if ((e.getKeyCode() == KeyEvent.VK_X) && mbAllowCut) {
                        doCut();
                    }
                    // Paste
                    else if ((e.getKeyCode() == KeyEvent.VK_V)
                            && mbAllowPaste) {
                        doPaste();
                    }
                }
            }
        };
    }
    private void cancelEdit() {
        for (final JTable table : tables) {
            MyHandyTable.cancelEditing(table);
        }
    }
    public void doCopy() {
        cancelEdit();
        MyHandyTable.copyToClipboard(tables, false);
    }
    public void doCut() {
        cancelEdit();
        MyHandyTable.copyToClipboard(tables, true);
    }
    public void doPaste() {
        if (tables.length > 1) {
            logger.warning(PASTE_UNSUPPORTED_MSG);
        }
        cancelEdit();
        pasteFromClipboard();
    }
    private void warn(final String message, final Exception e) {
        logger.log(Level.SEVERE, message, e);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, message
                        , "ClipboardTableHelper Warning"
                        , JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    // TODO: Consider moving to MyHandyTable
    private void pasteFromClipboard() {
        final int startRow = tables[0].getSelectedRows()[0];
        final int startCol = tables[0].getSelectedColumns()[0];
        final String pasteString;

        try {
            pasteString = (String) (CLIPBOARD.getContents(
                    this).getTransferData(DataFlavor.stringFlavor));
        } catch (final Exception e) {
            warn("Invalid paste type", e);
            return;
        }

        final String[] lines = pasteString.split(LINE_BREAK);
        for (int i = 0; i < lines.length; i++) {
            final String[] cells = lines[i].split(CELL_BREAK);
            for (int j = 0; j < cells.length; j++) {
                if (tables[0].getRowCount() > startRow + i
                        && tables[0].getColumnCount() > startCol + j) {
                    tables[0].setValueAt(cells[j], startRow + i, startCol + j);
                }
            }
        }
    }
}
