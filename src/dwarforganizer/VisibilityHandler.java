/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import javax.swing.JComponent;

/**
 * Allows incremented requests for showing/hiding a JComponent
 * 
 * @author Tamara Orr
 */
public class VisibilityHandler {
    private static final int HIDE_THRESHOLD = 0;
    private static final int SHOW_THRESHOLD = 1;        

    private JComponent comp;
    private int visibility;

    public VisibilityHandler(JComponent comp) {
        this.comp = comp;
        if (comp.isVisible())
            visibility = SHOW_THRESHOLD;
        else
            visibility = HIDE_THRESHOLD;
    }
    public void incrementHidden() {
        visibility--;
        if (visibility == HIDE_THRESHOLD) {
            //System.out.println("Hiding");
            comp.setVisible(false);
        }
    }
    public void incrementShown() {
        visibility++;
        if (visibility == SHOW_THRESHOLD) {
            //System.out.println("Showing");
            comp.setVisible(true);
        }
    }
}
