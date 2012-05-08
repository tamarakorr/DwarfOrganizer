/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.bins;

/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public interface BinRule<T extends Binnable> {
    public boolean canItemsBeBinned(T item1, T item2);
}
