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
    public static JMenu createMenu(final String text) {
        return new JMenu(text);
    }
    public static JMenu createMenu(final String text, final int mnemonic) {
        JMenu menu = createMenu(text);
        menu.setMnemonic(mnemonic);
        return menu;
    }
    public static JMenuItem createMenuItem(final String text
            , final int mnemonic) {

        return new JMenuItem(text, mnemonic);
    }
    public static JMenuItem createMenuItem(final String text
            , final ActionListener al) {

        final JMenuItem menuItem = new JMenuItem(text);
        menuItem.addActionListener(al);
        return menuItem;
    }
    public static JMenuItem createMenuItem(final String text
            , final ActionListener al, final int mnemonic) {

        return createMenuItem(text, al, mnemonic, null);
    }
    public static JMenuItem createMenuItem(final String text
            , final ActionListener al, final KeyStroke accelerator) {
        final JMenuItem menuItem = createMenuItem(text, al);
        if (accelerator != null)
            menuItem.setAccelerator(accelerator);

        return menuItem;
    }
    public static JMenuItem createMenuItem(final String text
            , final ActionListener al, final int mnemonic
            , final KeyStroke accelerator) {

        final JMenuItem menuItem = createMenuItem(text, al, accelerator);
        menuItem.setMnemonic(mnemonic);

        return menuItem;
    }
    public static JMenuItem createMenuItem(final String text
            , final int mnemonic, final KeyStroke accelerator) {

        final JMenuItem menuItem = createMenuItem(text, mnemonic);
        menuItem.setAccelerator(accelerator);
        return menuItem;
    }
}
