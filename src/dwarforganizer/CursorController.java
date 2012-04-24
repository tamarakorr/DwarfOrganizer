/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Tamara Orr
 */
public final class CursorController {

    public static final Cursor BUSY_CURSOR = Cursor.getPredefinedCursor(
            Cursor.WAIT_CURSOR);
    public static final Cursor DEFAULT_CURSOR = Cursor.getDefaultCursor();

    private CursorController() {
    }

    public static ActionListener createListener(final Component component
            , final ActionListener mainActionListener) {
        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    component.setCursor(BUSY_CURSOR);
                    mainActionListener.actionPerformed(ae);
                } finally {
                    component.setCursor(DEFAULT_CURSOR);
                }
            }

        };
        return actionListener;
    }
}
