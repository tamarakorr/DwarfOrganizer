/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.broadcast;

import java.util.Vector;

/**
 *
 * @author Tamara Orr
 */
public class Broadcaster {
    
    private Vector<BroadcastListener> mvListener;
    
    public Broadcaster() {
        super();
        
        mvListener = new Vector<BroadcastListener>();
    }
    
    public void notifyListeners(BroadcastMessage message) {
        Vector<BroadcastListener> v = (Vector<BroadcastListener>) mvListener.clone();
        for (BroadcastListener listener : v)
            listener.broadcast(message);
    }

    public void addListener(BroadcastListener listener) {
        Vector<BroadcastListener> v = (Vector<BroadcastListener>) mvListener.clone();
        if (! v.contains(listener))
            mvListener.add(listener);
    }

}
