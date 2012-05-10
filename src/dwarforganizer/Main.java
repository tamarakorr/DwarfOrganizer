/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import javax.swing.JOptionPane;
import myutils.MyNimbus;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MyNimbus.setNimbus();
        new MainWindow();
    }
    // Voluntarily crash the program
    public static void crash(final String message, final Exception e) {
        System.err.println(message);
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, message, "Application Failure"
                , JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
    public static void warn(final String message, final Exception e) {
        final String errMessage;

        System.err.println(message);
        if (e.getMessage() != null)
            errMessage = e.getMessage() + "\n";
        else
            errMessage = "";
        JOptionPane.showMessageDialog(null, errMessage + message, "Warning"
                , JOptionPane.WARNING_MESSAGE);
    }
}
