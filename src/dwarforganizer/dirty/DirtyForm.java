/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.dirty;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public interface DirtyForm {
    // These are just the DirtyHandler methods we want to expose to clients
    public void addDirtyListener(DirtyListener listener);
    public boolean isDirty();
    public void setClean();
}
