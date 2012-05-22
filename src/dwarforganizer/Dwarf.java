/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public class Dwarf implements MyPropertyGetter, Comparable
        , DeepCloneable<Dwarf> {

    private static final int ADULT_MINIMUM_AGE = 13;

    private static final String[] SUPPORTED_PROPERTIES = new String[] {
            "Name", "Nickname", "Gender", "Age", "JobText" };
    private static final Class[] SUPPORTED_PROP_CLASSES = new Class[] {
        String.class, String.class, String.class, Integer.class, String.class
    };

    private String name;
    private String nickname;
    private String gender;
    private int age;
    private Map<String, Integer> statValues;
    private Map<String, Integer> statPercents;
    private int time;
    private Map<String, Long> skillPotentials;
    private Map<String, Long> skillLevels;
    private Map<String, Long> balancedPotentials;
    private String jobText;
    private List<String> labors;

    public Dwarf() {
        super();
        skillPotentials = new HashMap<String, Long>();
        skillLevels = new HashMap<String, Long>();
        balancedPotentials = new HashMap<String, Long>();
        labors = new ArrayList<String>();
    }
    public Dwarf(final String name, final String nickname, final String gender
            , final int age, final Map<String, Integer> statValues
            , final Map<String, Integer> statPercents
            , final int time, final Map<String, Long> skillPotentials
            , final Map<String, Long> skillLevels
            , final Map<String, Long> balancedPotentials
            , final String jobText, final List<String> labors) {

        super();
        this.name = name;
        this.nickname = nickname;
        this.gender = gender;
        this.age = age;
        this.statValues = statValues;
        this.statPercents = statPercents;
        this.time = time;
        this.skillPotentials = skillPotentials;
        this.skillLevels = skillLevels;
        this.balancedPotentials = balancedPotentials;
        this.jobText = jobText;
        this.labors = labors;
    }

    public void setStatPercents(final Map<String, Integer> statPercents) {
        this.statPercents = statPercents;
    }

    public void setStatValues(final Map<String, Integer> statValues) {
        this.statValues = statValues;
    }

    public Map<String, Long> getBalancedPotentials() {
        return balancedPotentials;
    }

    public List<String> getLabors() {
        return labors;
    }

    public Map<String, Long> getSkillPotentials() {
        return skillPotentials;
    }

    public Map<String, Integer> getStatPercents() {
        return statPercents;
    }

    public Map<String, Integer> getStatValues() {
        return statValues;
    }

    public boolean isJuvenile() {
        return (age < ADULT_MINIMUM_AGE);
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    public String getJobText() {
        return jobText;
    }

    public void setJobText(final String jobText) {
        this.jobText = jobText;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(final String nickname) {
        this.nickname = nickname;
    }

    public int getTime() {
        return time;
    }

    public void setTime(final int time) {
        this.time = time;
    }

    public Map<String, Long> getSkillLevels() {
        return skillLevels;
    }

    @Override
    public Object getProperty(final String propName
            , final boolean humanReadable) {

        final String prop = propName.toLowerCase();
        if (prop.equals("name"))
            return getName().toString();
        else if (prop.equals("nickname"))
            return getNickname().toString();
        else if (prop.equals("gender"))
            return getGender().toString();
        else if (prop.equals("age"))
            return getAge();
        else if (prop.equals("jobtext"))
            return getJobText().toString();
        else if (prop.startsWith("statvalues."))
            return statValues.get(propName.replace("statvalues.", "")); // can't use lowercase
        else if (prop.startsWith("skillpotentials."))
            return skillPotentials.get(propName.replace("skillpotentials.", "")); // can't use lowercase
        else if (prop.startsWith("skilllevels."))
            return skillLevels.get(propName.replace("skilllevels.", "")); // can't use lowercase
        else
            return "[Dwarf] Undefined property: " + propName;
    }
    public static String[] getSupportedProperties() {
        return SUPPORTED_PROPERTIES;
    }
    public static Class[] getSupportedPropClasses() {
        return SUPPORTED_PROP_CLASSES;
    }

    @Override
    public long getKey() {
        //TODO
        return 0;
    }

    // Used for alphabetizing citizen lists. Not totally the correct way to do it, but easy
    @Override
    public int compareTo(final Object o) {
        final Dwarf otherDwarf = (Dwarf) o;
        return this.name.compareTo(otherDwarf.name);
    }

    @Override
    public Dwarf deepClone() {
        //NOTE: We are not deepcloning the following:
        // statValues, statPercents, skillPotentials, skillLevels, balancedPotentials,
        // and labors
        return new Dwarf(name, nickname, gender, age
            , statValues, statPercents
            , time, skillPotentials
            , skillLevels, balancedPotentials
            , jobText, labors);
    }
}
