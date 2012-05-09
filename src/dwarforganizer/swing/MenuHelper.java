/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 * Menu creation helper functions
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class MenuHelper {
    public static JMenu createMenu(String text) {
        return new JMenu(text);
    }
    public static JMenu createMenu(String text, int mnemonic) {
        JMenu menu = createMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }
    public static JMenuItem createMenuItem(String text, int mnemonic) {
        return new JMenuItem(text, mnemonic);
    }
    public static JMenuItem createMenuItem(String text, ActionListener al) {
        JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(al);
        return menuItem;
    }
    public static JMenuItem createMenuItem(String text, ActionListener al
            , int mnemonic) {
        return createMenuItem(text, al, mnemonic, null);
    }
    public static JMenuItem createMenuItem(String text, ActionListener al
            , KeyStroke accelerator) {
        JMenuItem menuItem = createMenuItem(text, al);
        if (accelerator != null)
            menuItem.setAccelerator(accelerator);

        return menuItem;
    }
    public static JMenuItem createMenuItem(String text, ActionListener al
            , int mnemonic, KeyStroke accelerator) {
        JMenuItem menuItem = createMenuItem(text, al, accelerator);
        menuItem.setMnemonic(mnemonic);

        return menuItem;
    }
}
