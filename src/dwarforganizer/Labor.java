/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 * Convenient data structure for the contents of labor-list.txt
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class Labor {
    
    protected String name;
    protected String skillName;
    protected String groupName;

    public Labor(String name, String skillName, String groupName) {
        this.name = name;
        this.skillName = skillName;
        this.groupName = groupName;
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}
