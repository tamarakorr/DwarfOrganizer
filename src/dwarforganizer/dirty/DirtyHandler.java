/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.dirty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class DirtyHandler {

    private boolean mbDirty;
    private List<DirtyListener> mlstDirtyListener;

    public DirtyHandler() {
        super();

        mbDirty = false;
        mlstDirtyListener = new ArrayList<DirtyListener>();
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
        //Vector<DirtyListener> v = (Vector<DirtyListener>) mlstDirtyListener.clone();
        final List<DirtyListener> list = Collections.synchronizedList(
                mlstDirtyListener);
        synchronized(list) {
            for (final DirtyListener listener : list)
                listener.dirtyChanged(mbDirty);
        }
    }

    public void addDirtyListener(DirtyListener listener) {
        //Vector<DirtyListener> v = (Vector<DirtyListener>) mlstDirtyListener.clone();
        final List<DirtyListener> list = Collections.synchronizedList(
                mlstDirtyListener);
        synchronized(list) {
            if (! list.contains(listener))
                list.add(listener);
        }
    }
}
