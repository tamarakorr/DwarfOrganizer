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

    private String name;
    private String skillName;
    private String groupName;

    public Labor(String name, String skillName, String groupName) {
        this.name = name;
        this.skillName = skillName;
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getName() {
        return name;
    }

    public String getSkillName() {
        return skillName;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
