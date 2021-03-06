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
public class ExclusionRule extends Exclusion
        implements DeepCloneable<Exclusion> {

    private String propertyName;
    private String comparator;
    private Object value;

    public static final String[] STRING_COMPARATORS = new String[] {
        "Contains", "Does not contain", "Equals", "Not equal to"
    };
    public static final String[] NUMERIC_COMPARATORS = new String[] {
        "Less than", "Less than or equal to", "Equals", "Greater than"
                , "Greater than or equal to", "Not equal to"
    };

    // Example: new Exclusion("Juveniles", "Age", "Less than", 13) excludes juveniles
    public ExclusionRule(final Integer ID, final String name
            , final boolean active
            , final String propertyName, final String comparator
            , final Object value) {

        super(ID, name, active);
        this.propertyName = propertyName;
        this.comparator = comparator;
        this.value = value;
    }

    public String getComparator() {
        return comparator;
    }
    public String getPropertyName() {
        return propertyName;
    }
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Exclusion: ID = " + super.getID() + ", Name = "
                + super.getName()
                + ", Property = " + propertyName
                + ", Comparator = " + comparator + ", Value = "
                + value.toString();
    }
    @Override
    public boolean appliesTo(final MyPropertyGetter obj) {
        return compareWith(obj.getProperty(this.propertyName, false)
                , this.value);
    }
    private boolean compareWith(final Object valueObj, Object valueThis) {
        //System.out.println(value2.getClass().toString());

        if (valueObj.getClass().equals(String.class)) {
            valueThis = (Object) (String) valueThis;

            if (this.comparator.equals("Contains"))
                return (valueObj.toString().contains(valueThis.toString()));
            if (this.comparator.equals("Does not contain"))
                return (! valueObj.toString().contains(valueThis.toString()));
            if (this.comparator.equals("Equals"))
                return (valueObj.toString().equals(valueThis.toString()));
            if (this.comparator.equals("Not equal to"))
                return (! valueObj.toString().equals(valueThis.toString()));

        }
        else if (Integer.class.equals(valueObj.getClass())) {
            final int intThis = Integer.parseInt(valueThis.toString());
            final int intObj = (Integer) valueObj;

            if (this.comparator.equals("Less than")) {
                return intObj < intThis;
            }
            if (this.comparator.equals("Less than or equal to"))
                return intObj <= intThis;
            if (this.comparator.equals("Equals"))
                return intObj == intThis;
            if (this.comparator.equals("Greater than"))
                return intObj > intThis;
            if (this.comparator.equals("Greater than or equal to"))
                return intObj >= intThis;
            if (this.comparator.equals("Not equal to"))
                return intObj != intThis;
        }
        else {
            final String message = "[Exclusion.compareWith]"
                    + " Unknown value class: "
                    + valueObj.getClass().getSimpleName()
                    + "\n" + valueObj + " " + valueThis;
            DwarfOrganizer.showInfo(null, message, "Problem");
            return false;
        }

        DwarfOrganizer.showInfo(null, "[ExclusionRule.compareWith]"
                + " Unsupported comparator: " + this.comparator, "Problem");
        return false;
    }

    @Override
    public Object getProperty(final String propName
            , final boolean humanReadable) {

        final String prop = propName.toLowerCase();
        if (prop.equals("name"))
            return this.getName();
        else if (prop.equals("propertyname"))
            return this.getPropertyName();
        else if (prop.equals("comparator"))
            return this.getComparator();
        else if (prop.equals("value"))
            return this.getValue();
        else if (prop.equals("id"))
            return this.getID();
        else if (prop.equals("active"))
            return this.isActive();
        else
            return "Unknown property: " + propName;
    }

    @Override
    public ExclusionRule deepClone() {
        return new ExclusionRule(this.getID(), this.getName(), this.isActive()
            , this.getPropertyName(), this.getComparator()
            , this.getValue());
    }

}
