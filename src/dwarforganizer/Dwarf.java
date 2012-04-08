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
public class Dwarf implements MyPropertyGetter, Comparable {
    
    private static final String[] SUPPORTED_PROPERTIES = new String[] {
            "Name", "Nickname", "Gender", "Age", "JobText" };
    private static final Class[] SUPPORTED_PROP_CLASSES = new Class[] {
        String.class, String.class, String.class, Integer.class, String.class
    };
    
    protected String name;
    protected String nickname;
    protected String gender;
    protected int age;
    protected Hashtable<String, Long> statValues;
    protected Hashtable<String, Long> statPercents;
    protected int time;
    protected Hashtable<String, Long> skillPotentials
            = new Hashtable<String, Long>();
    protected Hashtable<String, Long> skillLevels = new Hashtable<String, Long>();
    protected Hashtable<String, Long> balancedPotentials = new Hashtable<String, Long>();
    protected String jobText;
    protected Vector<String> labors = new Vector<String>(); 
    
    public boolean isJuvenile() {
        return (age < 13);
    }

    @Override
    public Object getProperty(String propName, boolean humanReadable) {
        String prop = propName.toLowerCase();
        if (prop.equals("name"))
            return this.name;
        else if (prop.equals("nickname"))
            return this.nickname;
        else if (prop.equals("gender"))
            return this.gender;
        else if (prop.equals("age"))
            return this.age;
        else if (prop.equals("jobtext"))
            return this.jobText;
        else
            return "Undefined property: " + propName;
    }

/*    @Override
    public String[] getSupportedProperties() {
        return SUPPORTED_PROPERTIES;      
    } */
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
    /*
    @Override
    public Class getPropertyClass(String propName, boolean humanReadable) {
        // TODO: Does this work? if it does, remove it from the interface (as it's unnecessary)
        // If not, fill it in!
        return getProperty(propName, humanReadable).getClass();
    } */
    
}
