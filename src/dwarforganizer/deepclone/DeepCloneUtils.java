/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.deepclone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * Convenient utility functions for operations on objects implementing
 * DeepCloneable.
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class DeepCloneUtils {
    // Deep clone a priority queue of DeepCloneable items
    public static <T extends DeepCloneable> PriorityQueue<T> deepClone(
            final PriorityQueue<T> queueToClone) {

        final PriorityQueue<T> queueReturn = new PriorityQueue<T>(
                queueToClone.size(), queueToClone.comparator());
        for (final T e : queueToClone) {
            queueReturn.add((T) e.deepClone());
        }
        return queueReturn;
    }
    // (We clone to an ArrayList instead of a Collection because we can't create
    // a new collection.) Deep clone a collection of DeepCloneable items
    public static <T extends DeepCloneable> ArrayList<T> deepClone(
            final Collection<T> colToClone) {
        final ArrayList<T> lstReturn = new ArrayList<T>(colToClone.size());
        for (final T e : colToClone) {
            lstReturn.add((T) e.deepClone());
        }
        return lstReturn;
    }
    // Deep clone a vector of DeepCloneable items
    public static <T extends DeepCloneable> Vector<T> deepClone(
            final Vector<T> vector) {
        final Vector<T> vReturn = new Vector<T>(vector.size());
        for (final T e : vector) {
            vReturn.add((T) e.deepClone());
        }
        return vReturn;
    }
}
