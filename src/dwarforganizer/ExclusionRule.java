/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 */
public class ExclusionRule extends Exclusion {

    private String propertyName;
    private String comparator;
    private Object value;

    protected static final String[] maStringComparators = new String[] {
        "Contains", "Does not contain", "Equals", "Not equal to"
    };
    protected static final String[] maNumericComparators = new String[] {
        "Less than", "Less than or equal to", "Equals", "Greater than"
                , "Greater than or equal to", "Not equal to"
    };    
    
    // Example: new Exclusion("Juveniles", "Age", "Less than", 13) excludes juveniles
    public ExclusionRule(Integer ID, String name, String propertyName, String comparator
            , Object value) {
        super(ID, name);
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
        return "Exclusion: ID = " + super.getID() + ", Name = " + super.getName()
                + ", Property = " + propertyName
                + ", Comparator = " + comparator + ", Value = " + value.toString();
    }
    @Override
    public boolean appliesTo(MyPropertyGetter obj) {
        
        return compareWith(obj.getProperty(this.propertyName, false)
                , this.value);
        
    }
    private boolean compareWith(Object valueObj, Object valueThis) {
        //System.out.println(value2.getClass().toString());
                
        if (valueObj.getClass().equals(String.class)) {
            valueThis = (Object) (String) valueThis;
            
            if (this.comparator.equals("Contains"))
                return (valueObj.toString().contains(valueThis.toString()));
            else if (this.comparator.equals("Does not contain"))
                return (! valueObj.toString().contains(valueThis.toString()));
            else if (this.comparator.equals("Equals"))
                return (valueObj.toString().equals(valueThis.toString()));
            else if (this.comparator.equals("Not equal to"))
                return (! valueObj.toString().equals(valueThis.toString()));
            
        }
        else if (Integer.class.equals(valueObj.getClass())) {
            Integer intThis = Integer.parseInt(valueThis.toString());
            Integer intObj = (Integer) valueObj;
            
            if (this.comparator.equals("Less than")) {
                return intObj < intThis;
            }
            else if (this.comparator.equals("Less than or equal to"))
                return intObj <= intThis;
            else if (this.comparator.equals("Equals"))
                return intObj == intThis;
            else if (this.comparator.equals("Greater than"))
                return intObj > intThis;
            else if (this.comparator.equals("Greater than or equal to"))
                return intObj >= intThis;
            else if (this.comparator.equals("Not equal to"))
                return intObj != intThis;
        }
        else {
            System.out.println("[Exclusion] Unknown value class: "
                    + valueObj.getClass().getSimpleName());
            System.out.println(valueObj + " " + valueThis);
            return false;
        }
        
        System.out.println("Unsupported comparator: " + this.comparator);
        return false;
    }

    @Override
    public Object getProperty(String propName, boolean humanReadable) {
        String prop = propName.toLowerCase();
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
        else
            return "Unknown property: " + propName;
    }
    
}
