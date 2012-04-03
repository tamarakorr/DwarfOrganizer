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

    private static String[] SUPPORTED_PROPERTIES = new String[] { "size" };
    
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
    
    @Override
    public int compareTo(Object o) {
        Job j = (Job) o;
        if (this.time < j.time)
            return -1;
        else if (this.time == j.time)
            return 0;
        else
            return 1;
    }

    @Override
    public Comparable getProperty(String propName, boolean humanReadable) {
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

    //@Override
    public static String[] getSupportedProperties() {
        return SUPPORTED_PROPERTIES;
    }

/*    @Override
    public Class getPropertyClass(String propName, boolean humanReadable) {
        throw new UnsupportedOperationException("Not supported yet.");
    } */
    
}
