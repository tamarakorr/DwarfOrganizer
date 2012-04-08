/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.List;

/**
 *
 * @author Owner
 */
public class ExclusionList extends Exclusion {

    private List<Dwarf> moCitizenList;

    public ExclusionList(Integer ID, String name, List<Dwarf> citizenList) {
        super(ID, name);
        this.moCitizenList = citizenList;
    }
    public List<Dwarf> getCitizenList() {
        return moCitizenList;
    }
    public void setCitizenList(List<Dwarf> moCitizenList) {
        this.moCitizenList = moCitizenList;
    }
    
    @Override
    public boolean appliesTo(MyPropertyGetter citizen) {
        if (citizen.getClass().equals(Dwarf.class))
            return moCitizenList.contains(citizen);
        else {
            System.out.println("[ExclusionList] Class of object is not Dwarf");
            return false;
        }
    }
}
