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
public class MetaSkill {
    protected String name;
    protected Vector<Skill> vSkills;
    public MetaSkill(String name, Vector<Skill> skills) {
        this.name = name;
        this.vSkills = skills;
    }
}
