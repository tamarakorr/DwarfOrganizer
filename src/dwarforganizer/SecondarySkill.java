/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.List;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class SecondarySkill extends Skill {
    // Secondary skills are a convenience for filtering the list. They
    // look just like a Skill otherwise.
    public SecondarySkill(final String name, final List<Stat> stats) {
        super(name, stats);
    }
}
