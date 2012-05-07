/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer;

import java.awt.Component;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

/**
 * This is a utility class which can be used to set the mnemonics of all the
 * items in a JMenuBar on the fly.
 *
 * Ignores case of menu text.
 *
 * @author Tamara Orr
 * License: See MIT license in license.txt
 */
public class MenuMnemonicSetter {
    //Set mnemonic of all the items in the menubar
    public static void setMnemonics(JMenuBar mb) {
        MenuElement[] MenuElements = mb.getSubElements();
        int num = MenuElements.length;
        if (0 == num) {
            return;
        }
        setMnemonicForTopLevel(mb);
        for (int i = 0; i < num; ++i) {
            if (!(MenuElements[i] instanceof JMenu)) {
                continue;
            }
            setMnemonic((JMenu) MenuElements[i]);
        }
    }

    // Set mnemonic of all the top level menus in the menubar
    private static void setMnemonicForTopLevel(JMenuBar mb) {
        MenuElement[] MenuElements = mb.getSubElements();
        int num = MenuElements.length;
        if (0 == num) {
            return;
        }
        char mnemonics[] = new char[num];

        for (int i = 0; i < num; ++i) {
            if (!(MenuElements[i] instanceof JMenu)) {
                continue;
            }
            JMenu menu = (JMenu) MenuElements[i];

            String text = menu.getText();
            char mnemonic = getMnemonic(text, mnemonics);
            if (' ' != mnemonic) {
                menu.setMnemonic(mnemonic);
                mnemonics[i] = mnemonic;
            }
        }
    }

    // Set mnemonic of the menuitems and submenus of a given menu.
    // To set a different mnemonic for each item, it first checks the first
    // character of all the words then the second, third, and so on until a
    // unique character is found.
    private static void setMnemonic(JMenu jm) {
        Component mcomps[] = jm.getMenuComponents();
        int num = mcomps.length;

        if (0 == num) {
            return;
        }
        char mnemonics[] = new char[num];

        for (int i = 0; i < num; ++i) {
            if (!(mcomps[i] instanceof JMenuItem)) {
                continue;
            }
            JMenuItem menuitem = (JMenuItem) mcomps[i];

            String text = menuitem.getText();
            char mnemonic = getMnemonic(text, mnemonics);
            if (' ' != mnemonic) {
                menuitem.setMnemonic(mnemonic);
                mnemonics[i] = mnemonic;
            }
            if (menuitem instanceof JMenu) {
                setMnemonic((JMenu) menuitem);
            }
        }
    }

    private static char getMnemonic(String text, char[] mnemonics) {
        Vector words = new Vector();
        StringTokenizer t = new StringTokenizer(text);
        int maxSize = 0;

        while (t.hasMoreTokens()) {
            String word = (String) t.nextToken();
            if (word.length() > maxSize) {
                maxSize = word.length();
            }
            words.addElement(word);
        }
        words.trimToSize();

        for (int i = 0; i < maxSize; ++i) {
            char mnemonic = getMnemonic(words, mnemonics, i);
            if (' ' != mnemonic) {
                return mnemonic;
            }
        }

        return ' ';
    }

    private static char getMnemonic(Vector words, char[] mnemonics, int index) {
        int numwords = words.size();

        for (int i = 0; i < numwords; ++i) {
            String word = ((String) words.elementAt(i)).toLowerCase();
            if (index >= word.length()) {
                continue;
            }
            char c = word.charAt(index);
            if (!isMnemonicExists(c, mnemonics)) {
                return c;
            }
        }
        return ' ';
    }

    private static boolean isMnemonicExists(char c, char[] mnemonics) {
        int num = mnemonics.length;
        for (int i = 0; i < num; ++i) {
            if (mnemonics[i] == c) {
                return true;
            }
        }
        return false;
    }
}