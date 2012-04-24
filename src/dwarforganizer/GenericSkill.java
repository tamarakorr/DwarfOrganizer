/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 */
public class GenericSkill implements NamedThing {

    private String name;
    
    public GenericSkill(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

}
