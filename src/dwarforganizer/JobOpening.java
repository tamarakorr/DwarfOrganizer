/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.bins.Binnable;

/**
 * Represents a single job opening
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobOpening extends Binnable {

    private static String[] SUPPORTED_PROPERTIES = new String[] { "size" };
    
    private String name;
    private String skillName;
    private int time;
    private double candidateWeight;
    private int currentSkillWeight;
    private String reminder;

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
        if (this.time < j.getTime())
            return -1;
        else if (this.time == j.getTime())
            return 0;
        else
            return 1;
    }

    @Override
    public Object getProperty(String propName, boolean humanReadable) {
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

    @Override
    public long getKey() {
        //TODO
        return 0;
    }

    public double getCandidateWeight() {
        return candidateWeight;
    }

    public int getCurrentSkillWeight() {
        return currentSkillWeight;
    }

    public String getName() {
        return name;
    }

    public String getReminder() {
        return reminder;
    }

    public String getSkillName() {
        return skillName;
    }

    public int getTime() {
        return time;
    }

    public void setCandidateWeight(double candidateWeight) {
        this.candidateWeight = candidateWeight;
    }

    public void setCurrentSkillWeight(int currentSkillWeight) {
        this.currentSkillWeight = currentSkillWeight;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public void setTime(int time) {
        this.time = time;
    }

/*    @Override
    public Class getPropertyClass(String propName, boolean humanReadable) {
        throw new UnsupportedOperationException("Not supported yet.");
    } */
    
}
