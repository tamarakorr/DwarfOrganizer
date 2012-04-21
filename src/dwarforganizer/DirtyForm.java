/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 */
public interface DirtyForm {
    // These are just the DirtyHandler methods we want to expose to clients
    public void addDirtyListener(DirtyListener listener);
    public boolean isDirty();
}
