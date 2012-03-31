/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public class Dwarf {

    protected String name;
    protected String nickname;
    protected String gender;
    protected int age;
    protected Hashtable<String, Long> statValues;
    protected Hashtable<String, Long> statPercents;
    protected int time;
    protected Hashtable<String, Long> skillPotentials
            = new Hashtable<String, Long>();
    protected Hashtable<String, Long> skillLevels = new Hashtable<String, Long>();
    protected Hashtable<String, Long> balancedPotentials = new Hashtable<String, Long>();
    protected String jobText;
    protected Vector<String> labors = new Vector<String>(); 
    
    public boolean isJuvenile() {
        return (age < 13);
    }
    
}
