/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public abstract class Exclusion implements MyPropertyGetter, MyPropertySetter
    , DeepCloneable<Exclusion> {

    private static final String[] SUPPORTED_PROPERTIES = new String[] {
        "id", "name" };

    private String name;
    private Integer ID;
    private boolean active;

    public abstract boolean appliesTo(MyPropertyGetter obj);

    public Exclusion(Integer ID, String name, boolean active) {
        super();
        this.ID = ID;
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public Integer getID() {
        return ID;
    }

    public boolean isActive() {
        return active;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Exclusion: ID = " + ID + ", Name = " + name
                + ", Active = " + active;
    }


    @Override
    public Object getProperty(String propName, boolean humanReadable) {
        String prop = propName.toLowerCase();
        if (prop.equals("name"))
            return this.getName();
        else if (prop.equals("id"))
            return this.getID();
        else if (prop.equals("active"))
            return this.isActive();
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

    @Override
    public void setProperty(String propName, Object value) {
        String prop = propName.toLowerCase();

        try {
            if (prop.equals("name"))
                this.setName(value.toString());
            else if (prop.equals("id"))
                this.setID((Integer) value);
            else if (prop.equals("active"))
                this.setActive((Boolean) value);
            else
                System.err.println("Unknown Exclusion property: " + propName);
        } catch (Exception e) {
            System.err.println("Failed to set Exclusion property " + propName);
        }
    }

}
