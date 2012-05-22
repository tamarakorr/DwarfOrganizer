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
    private int qtyDesired;

    public Job(final String name, final String skillName, final int qtyDesired
            , final int time
            , final double candidateWeight, final int currentSkillWeight
            , final String reminder) {

        super(name, skillName, time, candidateWeight, currentSkillWeight
                , reminder);

        this.qtyDesired = qtyDesired;
    }
    public int getQtyDesired() {
        return qtyDesired;
    }
    public void setQtyDesired(final int qtyDesired) {
        this.qtyDesired = qtyDesired;
    }
}
