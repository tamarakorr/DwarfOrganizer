/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 */
public abstract class Exclusion implements MyPropertyGetter {
    
    private static final String[] SUPPORTED_PROPERTIES = new String[] {
        "id", "name" };
    
    private String name;
    private Integer ID;
    
    public abstract boolean appliesTo(MyPropertyGetter obj);
    
    
    public Exclusion(Integer ID, String name) {
        super();
        this.ID = ID;
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public Integer getID() {
        return ID;
    }
    
    @Override
    public String toString() {
        return "Exclusion: ID = " + ID + ", Name = " + name;
    }
    

    @Override
    public Object getProperty(String propName, boolean humanReadable) {
        String prop = propName.toLowerCase();
        if (prop.equals("name"))
            return this.getName();
        else if (prop.equals("id"))
            return this.getID();
        else
            return "Unknown property: " + propName;
    }

    public static String[] getSupportedProperties() {
        return SUPPORTED_PROPERTIES;
    }

    @Override
    public long getKey() {
        return (long) ID;
    }

}
