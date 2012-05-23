/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.bins.Binnable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a single job opening
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobOpening extends Binnable {
    private static final Logger logger = Logger.getLogger(
            JobOpening.class.getName());
    private static String[] SUPPORTED_PROPERTIES = new String[] { "size" };

    private String name;
    private String skillName;
    private int time;
    private double candidateWeight;
    private int currentSkillWeight;
    private String reminder;

    public JobOpening(final String name, final String skillName, final int time
            , final double candidateWeight, final int currentSkillWeight
            , final String reminder) {

        this.name = name;
        this.skillName = skillName;
        this.time = time;
        this.candidateWeight = candidateWeight;
        this.currentSkillWeight = currentSkillWeight;
        this.reminder = reminder;
    }

    @Override
    public int compareTo(final Object o) {
        final Job j = (Job) o;
        if (this.time < j.getTime())
            return -1;
        if (this.time == j.getTime())
            return 0;
        return 1;
    }

    @Override
    public Object getProperty(final String propName
            , final boolean humanReadable) {

        if (propName.equals("size"))
            return this.time;
        else {
            logger.log(Level.SEVERE, "Unknown property: clsJobOpening.{0}"
                    , propName);
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

    public void setCandidateWeight(final double candidateWeight) {
        this.candidateWeight = candidateWeight;
    }

    public void setCurrentSkillWeight(final int currentSkillWeight) {
        this.currentSkillWeight = currentSkillWeight;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setReminder(final String reminder) {
        this.reminder = reminder;
    }

    public void setSkillName(final String skillName) {
        this.skillName = skillName;
    }

    public void setTime(final int time) {
        this.time = time;
    }

/*    @Override
    public Class getPropertyClass(String propName, boolean humanReadable) {
        throw new UnsupportedOperationException("Not supported yet.");
    } */

}
