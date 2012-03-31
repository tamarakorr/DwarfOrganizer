/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 * Represents a quantity of job openings. Quantity may be zero.
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class Job extends JobOpening {
    protected int qtyDesired;
    
    public Job(String name, String skillName, int qtyDesired, int time
            , double candidateWeight, int currentSkillWeight, String reminder) {
        
        super(name, skillName, time, candidateWeight, currentSkillWeight
                , reminder);
        
        this.qtyDesired = qtyDesired;
    }
}
