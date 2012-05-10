/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;
import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class ExclusionList extends Exclusion
        implements DeepCloneable<Exclusion> {

    //private DeepCloneableVector<Dwarf> moCitizenList;
    private Vector<String> mvCitizenNames;

    public ExclusionList(Integer ID, String name, boolean active
            , Vector<String> citizenList) {
        super(ID, name, active);
        this.mvCitizenNames = citizenList;
    }
    public Vector<String> getCitizenList() {
        return mvCitizenNames;
    }
    public void setCitizenList(Vector<String> vCitizenNames) {
        this.mvCitizenNames = vCitizenNames;
    }

    @Override
    public boolean appliesTo(MyPropertyGetter citizen) {
        if (citizen.getClass().equals(Dwarf.class)) {
            Dwarf dwarf = (Dwarf) citizen;
            return mvCitizenNames.contains(dwarf.getName());
        }
        else {
            System.out.println("[ExclusionList] Class of object is not Dwarf");
            return false;
        }
    }

    @Override
    public ExclusionList deepClone() {
        Vector<String> vNames = new Vector<String>(mvCitizenNames.size());
        for (String name : mvCitizenNames) {
            vNames.add(name);
        }
        return new ExclusionList(this.getID(), this.getName(), this.isActive()
                , vNames);
    }
}
