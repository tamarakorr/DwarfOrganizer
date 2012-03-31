/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 * Represents a single job opening
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobOpening extends Binnable {

    protected String name;
    protected String skillName;
    protected int time;
    protected double candidateWeight;
    protected int currentSkillWeight;
    protected String reminder;
    
    public JobOpening(String name, String skillName, int time
            , double candidateWeight, int currentSkillWeight, String reminder) {
        this.name = name;
        this.skillName = skillName;
        this.time = time;
        this.candidateWeight = candidateWeight;
        this.currentSkillWeight = currentSkillWeight;
        this.reminder = reminder;
    }
    
    public int compareTo(Object o) {
        Job j = (Job) o;
        if (this.time < j.time)
            return -1;
        else if (this.time == j.time)
            return 0;
        else
            return 1;
    }

    public Object getProperty(String propName) {
        if (propName.equals("size"))
            return this.time;
        else {
            System.err.println("Unknown property: clsJobOpening." + propName);
            return -1;
        }
    }

    @Override
    public String toString() {
        return this.name;
    }
    
}
