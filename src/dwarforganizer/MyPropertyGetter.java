/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public interface MyPropertyGetter {
    public Object getProperty(String propName, boolean humanReadable);  // Object
    //public String[] getSupportedProperties();
    //public Class getPropertyClass(String propName, boolean humanReadable);
    //public String[] getProperties();
    public long getKey();
}
