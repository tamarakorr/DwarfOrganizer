/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public abstract class Exclusion implements MyPropertyGetter, MyPropertySetter
    , DeepCloneable<Exclusion> {

    private static final Logger logger = Logger.getLogger(
            Exclusion.class.getName());
    private static final String[] SUPPORTED_PROPERTIES = new String[] {
        "id", "name" };

    private String name;
    private Integer ID;
    private boolean active;

    public abstract boolean appliesTo(MyPropertyGetter obj);

    public Exclusion(final Integer ID, final String name
            , final boolean active) {

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

    public void setID(final Integer ID) {
        this.ID = ID;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Exclusion: ID = " + ID + ", Name = " + name
                + ", Active = " + active;
    }


    @Override
    public Object getProperty(final String propName
            , final boolean humanReadable) {

        final String prop = propName.toLowerCase();
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
    public void setProperty(final String propName, final Object value) {
        final String prop = propName.toLowerCase();

        try {
            if (prop.equals("name"))
                this.setName(value.toString());
            else if (prop.equals("id"))
                this.setID((Integer) value);
            else if (prop.equals("active"))
                this.setActive((Boolean) value);
            else {
                logger.log(Level.SEVERE, "Unknown Exclusion property: {0}"
                        , propName);
            }
        } catch (Exception ignore) {
            logger.log(Level.SEVERE, "Failed to set Exclusion property {0}"
                    , propName);
        }
    }
}
