/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import javax.swing.JMenu;

/**
 * Represents a JMenu with a priority (such as for priority queueing)
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class CombinableMenu {
    private int order;
    private JMenu menu;
    public CombinableMenu(final JMenu menu, final int order) {
        this.menu = menu;
        this.order = order;
    }
    public JMenu getMenu() {
        return menu;
    }

    public int getOrder() {
        return order;
    }
}