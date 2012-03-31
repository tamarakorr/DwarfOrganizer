/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.event.KeyEvent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

/**
 * Fixes a very old bug in the JVM which causes JTable to start editing when a
 * mnemonic key or function key is pressed.
 * 
 * @author Tamara Orr
 */

public class MyJTable extends JTable {
    public MyJTable(TableModel tableModel) {
        super(tableModel);
    }

    // Starting an edit session is horribly bugged so we are preventing
    // Control, Alt, and Meta from starting the edit session.
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e
            , int condition, boolean pressed) {

        // We cannot just ignore every key that isn't in the input map
        // these days, because in modern Java EVERY DARN KEY is in the
        // JTable input map.

        // So: We stop Alt, Ctrl, and Meta from starting an edit
        if (e.isAltDown() || e.isControlDown() || e.isMetaDown())
            return false;
        else
            return super.processKeyBinding(ks, e, condition, pressed);
    }
}
