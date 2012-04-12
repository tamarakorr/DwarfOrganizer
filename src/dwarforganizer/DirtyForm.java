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
    public DirtyHandler getDirtyHandler();  // TODO Really we should be getting a dirty handler adapter
                                            // or better yet, just providing AddListener methods
}
