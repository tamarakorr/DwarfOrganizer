/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.deepclone;

/**
 * Defines an object with the function deepClone(), returning the given type.
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public interface DeepCloneable<T> {
    public T deepClone();
}