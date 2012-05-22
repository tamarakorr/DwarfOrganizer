/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.broadcast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class Broadcaster {

    private List<BroadcastListener> mlstListener;

    public Broadcaster() {
        super();
        mlstListener = new ArrayList<BroadcastListener>();
    }

    public void notifyListeners(final BroadcastMessage message) {
        /*final Vector<BroadcastListener> clone
                = (Vector<BroadcastListener>) mlstListener.clone(); */
        final List<BroadcastListener> list
                = Collections.synchronizedList(mlstListener);
        synchronized(list) {
            for (final BroadcastListener listener : list)
                listener.broadcast(message);
        }
    }

    public void addListener(final BroadcastListener listener) {
        /*final Vector<BroadcastListener> clone
                = (Vector<BroadcastListener>) mlstListener.clone(); */
        final List<BroadcastListener> list = Collections.synchronizedList(
                mlstListener);

        synchronized(list) {
            if (! list.contains(listener)) {
                //mlstListener.add(listener);
                list.add(listener);
            }
        }
    }
}
