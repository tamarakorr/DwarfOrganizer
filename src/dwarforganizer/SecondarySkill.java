/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Vector;

/**
 *
 * @author Tamara Orr
 */
public class SecondarySkill extends Skill {
    // Secondary skills are a convenience for filtering the list. They
    // look just like a Skill otherwise.
    public SecondarySkill(String name, Vector<Stat> stats) {
        super(name, stats);
    }
}