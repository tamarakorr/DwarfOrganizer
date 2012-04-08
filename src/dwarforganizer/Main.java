/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

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

}
