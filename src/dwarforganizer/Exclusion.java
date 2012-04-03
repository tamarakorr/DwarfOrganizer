/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 */
public class Exclusion implements MyPropertyGetter {
    
    private static final String[] SUPPORTED_PROPERTIES = new String[] {
        "name", "propertyname", "comparator", "value" };
    
    protected static final String[] maStringComparators = new String[] {
        "Contains", "Does not contain"
    };
    protected static final String[] maNumericComparators = new String[] {
        "Less than", "Less than or equal to", "Equals", "Greater than"
                , "Greater than or equal to", "Not equal to"
    };
    
    private String name;
    private String propertyName;
    private String comparator;
    private Comparable value;
    
    // Example: new Exclusion("Juveniles", "Age", "<", 13) excludes juveniles
    public Exclusion(String name, String propertyName, String comparator
            , Comparable value) {
        this.name = name;
        this.propertyName = propertyName;
        this.comparator = comparator;
        this.value = value;
    }
    
    public String getComparator() {
        return comparator;
    }

    public String getName() {
        return name;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Comparable getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return "Exclusion: Name = " + name + ", Property = " + propertyName
                + ", Comparator = " + comparator + ", Value = " + value.toString();
    }
    
    public boolean appliesTo(MyPropertyGetter obj) {
        
        return compareWith(obj.getProperty(this.propertyName, false)
                , this.value);
        
    }
    private boolean compareWith(Comparable value1, Comparable value2) {
        //System.out.println(value2.getClass().toString());
                
        if (value2.getClass().equals(String.class)
                && value1.getClass().equals(String.class)) {
            //comparators = maStringComparators;
        }
        else if (value2.getClass().equals(Number.class)
                && (value1.getClass().equals(Number.class))) {
            //comparators = maNumericComparators;
        }
        else {
            System.out.println("Unknown value classes: " + value1.getClass().getSimpleName()
                    + ", " + value2.getClass().getSimpleName());
            return false;
        }
        
        if (this.comparator.equals("Less than")) {
            return (value1.compareTo(value2) == -1);
        }
        else if (this.comparator.equals("Contains")) {
            return (value1.toString().contains(value2.toString()));
        }
        else {
            System.out.println("Unsupported comparator: " + this.comparator);
            return false;
        }

    }

    @Override
    public Comparable getProperty(String propName, boolean humanReadable) {
        if (propName.equals("name"))
            return this.getName();
        else if (propName.equals("propertyname"))
            return this.getPropertyName();
        else if (propName.equals("comparator"))
            return this.getComparator();
        else if (propName.equals("value"))
            return this.getValue();
        else
            return "Unknown property: " + propName;
    }

    //@Override
    public static String[] getSupportedProperties() {
        return SUPPORTED_PROPERTIES;
    }

}
