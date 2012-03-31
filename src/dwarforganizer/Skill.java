/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class Skill {
    protected Vector<Stat> mvStats = new Vector<Stat>();
    protected String name;

    public Skill(String name, Vector<Stat> stats) {
        this.name = name;
        mvStats = stats;
    }
    public Vector<Stat> getStats() { return mvStats; }

}
