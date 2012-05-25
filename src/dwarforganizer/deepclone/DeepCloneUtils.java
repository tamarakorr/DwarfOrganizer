/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.deepclone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;

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
        for (final DeepCloneable<T> e : queueToClone) {
            queueReturn.add(e.deepClone());
        }
        return queueReturn;
    }
    // (We clone to an ArrayList instead of a Collection because we can't create
    // a new collection.) Deep clone a collection of DeepCloneable items
    public static <T extends DeepCloneable> ArrayList<T> deepClone(
            final Collection<T> colToClone) {
        final ArrayList<T> lstReturn = new ArrayList<T>(colToClone.size());
        for (final DeepCloneable<T> e : colToClone) {
            lstReturn.add(e.deepClone());
        }
        return lstReturn;
    }
    // (Vector "obsolete")
    // Deep clone a vector of DeepCloneable items
/*    public static <T extends DeepCloneable> Vector<T> deepClone(
            final Vector<T> vector) {
        final Vector<T> vReturn = new Vector<T>(vector.size());
        for (final DeepCloneable<T> e : vector) {
            vReturn.add(e.deepClone());
        }
        return vReturn;
    } */
    // Deep clone a list, returning it as an ArrayList
    public static <T extends DeepCloneable> ArrayList<T> deepClone(
            final List<T> listToClone) {

        final ArrayList<T> lstReturn = new ArrayList<T>(listToClone.size());
        for (final DeepCloneable<T> e : listToClone) {
            lstReturn.add(e.deepClone());
        }
        return lstReturn;
    }
}
