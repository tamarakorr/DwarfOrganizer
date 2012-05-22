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
public class Skill extends GenericSkill implements NamedThing {
    private List<Stat> mvStats;

    public Skill(final String name, final List<Stat> stats) {
        super(name);
        mvStats = stats;
    }
    public List<Stat> getStats() {
        return mvStats;
    }

}
