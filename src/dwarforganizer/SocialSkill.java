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
public class SocialSkill extends SecondarySkill {
    // Social skills are defined as skills whose development can be prevented
    // completely by a trait.
    
    private String noStatName;
    private int noStatMin;
    private int noStatMax;

    public SocialSkill(String name, Vector<Stat> stats, String noStatName
            , int noStatMin, int noStatMax) {
        super(name, stats);
        this.noStatName = noStatName;
        this.noStatMin = noStatMin;
        this.noStatMax = noStatMax;
    }

    public int getNoStatMax() {
        return noStatMax;
    }

    public int getNoStatMin() {
        return noStatMin;
    }

    public String getNoStatName() {
        return noStatName;
    }

}
