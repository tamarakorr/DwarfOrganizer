/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public class Dwarf implements MyPropertyGetter, Comparable, DeepCloneable {
    
    private static final String[] SUPPORTED_PROPERTIES = new String[] {
            "Name", "Nickname", "Gender", "Age", "JobText" };
    private static final Class[] SUPPORTED_PROP_CLASSES = new Class[] {
        String.class, String.class, String.class, Integer.class, String.class
    };
    
    private String name;
    private String nickname;
    private String gender;
    private int age;
    protected Hashtable<String, Long> statValues;
    protected Hashtable<String, Long> statPercents;
    private int time;
    protected Hashtable<String, Long> skillPotentials
            = new Hashtable<String, Long>();
    protected Hashtable<String, Long> skillLevels = new Hashtable<String, Long>();
    protected Hashtable<String, Long> balancedPotentials = new Hashtable<String, Long>();
    private String jobText;
    protected Vector<String> labors = new Vector<String>(); 
    
    public Dwarf() {
        super();
    }
    public Dwarf(String name, String nickname, String gender, int age
            , Hashtable<String, Long> statValues, Hashtable<String, Long> statPercents
            , int time, Hashtable<String, Long> skillPotentials
            , Hashtable<String, Long> skillLevels, Hashtable<String, Long> balancedPotentials
            , String jobText, Vector<String> labors) {
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
    
    public boolean isJuvenile() {
        return (age < 13);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getJobText() {
        return jobText;
    }

    public void setJobText(String jobText) {
        this.jobText = jobText;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public Object getProperty(String propName, boolean humanReadable) {
        String prop = propName.toLowerCase();
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
    public int compareTo(Object o) {
        Dwarf otherDwarf = (Dwarf) o;
        return this.name.compareTo(otherDwarf.name);
    }

    @Override
    public Object deepClone() {
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
