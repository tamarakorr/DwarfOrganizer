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
public class Skill extends GenericSkill implements NamedThing {
    private Vector<Stat> mvStats;

    public Skill(String name, Vector<Stat> stats) {
        super(name);
        mvStats = stats;
    }
    public Vector<Stat> getStats() { return mvStats; }

}
