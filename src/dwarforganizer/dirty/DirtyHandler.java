/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.dirty;

import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class DirtyHandler {

    private boolean mbDirty;
    private Vector<DirtyListener> mvDirtyListener;

    public DirtyHandler() {
        super();

        mbDirty = false;
        mvDirtyListener = new Vector<DirtyListener>();
    }
    public boolean isDirty() {
        return mbDirty;
    }

    public void setClean() {
        setDirty(false);
    }

    public void setDirty(boolean newValue) {
        if (mbDirty != newValue) {
            mbDirty = newValue;
            notifyDirtyListeners();
        }
    }

    public void notifyDirtyListeners() {
        Vector<DirtyListener> v = (Vector<DirtyListener>) mvDirtyListener.clone();
        for (DirtyListener listener : v)
            listener.dirtyChanged(mbDirty);
    }

    public void addDirtyListener(DirtyListener listener) {
        Vector<DirtyListener> v = (Vector<DirtyListener>) mvDirtyListener.clone();
        if (! v.contains(listener))
            mvDirtyListener.add(listener);
    }
}
