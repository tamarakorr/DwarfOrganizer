/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public final class CursorController {

    public static final Cursor BUSY_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
    public static final Cursor DEFAULT_CURSOR
            = new Cursor(Cursor.DEFAULT_CURSOR);
    public static final int DELAY = 500; // in milliseconds

    private CursorController() {}

    public static ActionListener createListener(final Component component
            , final ActionListener mainActionListener) {

        final ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent ae) {

                final TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        component.setCursor(BUSY_CURSOR);
                    }
                };
                final Timer timer = new Timer();

                try {
                    timer.schedule(timerTask, DELAY);
                    mainActionListener.actionPerformed(ae);
                } finally {
                    timer.cancel();
                    component.setCursor(DEFAULT_CURSOR);
                }
            }
        };
        return actionListener;
    }

}
