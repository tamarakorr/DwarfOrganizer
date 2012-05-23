/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer;

import java.awt.Component;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import myutils.MyNimbus;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class DwarfOrganizer {
    public static final String VERSION = "1.3";

    private static final Logger logger = Logger.getLogger(
            DwarfOrganizer.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        MyNimbus.setNimbus();
        final MainWindow ignore = new MainWindow();
    }
    // Voluntarily crash the program
    public static void crash(final String message, final Exception e) {
        logger.log(Level.SEVERE, message, e);
        showDialog(null, message, "Application Failure"
                , JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }
    public static void warn(final String message, final Exception e) {
        String errMessage;

        logger.log(Level.SEVERE, message, e);
        if (e.getMessage() != null)
            errMessage = e.getMessage() + "\n";
        else
            errMessage = "";
        if (message != null)
            errMessage += message;

        showDialog(null, errMessage, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    public static void showInfo(final Component parentComp, final String message
            , final String title) {

        showDialog(parentComp, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    private static void showDialog(final Component parentComp
            , final String message, final String title, final int msgType) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(parentComp, message, title
                        , msgType);
            }
        });
    }
}
