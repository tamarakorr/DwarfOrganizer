/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.deepclone;

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class DeepCloneableVector<T extends DeepCloneable> extends Vector<T>
        implements DeepCloneable, List<T>
        , RandomAccess, Cloneable, java.io.Serializable {

    public DeepCloneableVector() {
        super();
    }
    public DeepCloneableVector(Collection<? extends T> c) {
        super(c);
    }
    public DeepCloneableVector(int initialCapacity) {
        super(initialCapacity);
    }
    public DeepCloneableVector(int initialCapacity, int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }

    @Override
    public Object deepClone() {
        DeepCloneableVector<T> vReturn = new DeepCloneableVector<T>();
        for (DeepCloneable<T> item : this) { // T
            vReturn.add(item.deepClone()); // (T)
        }
        return vReturn;
    }

}
