/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class ExclusionList extends Exclusion
    implements DeepCloneable<Exclusion> {

    private List<String> mvCitizenNames;

    public ExclusionList(final Integer ID, final String name
            , final boolean active, final List<String> citizenList) {

        super(ID, name, active);
        this.mvCitizenNames = citizenList;
    }
    public List<String> getCitizenList() {
        return mvCitizenNames;
    }
    public void setCitizenList(final List<String> vCitizenNames) {
        this.mvCitizenNames = vCitizenNames;
    }

    @Override
    public boolean appliesTo(final MyPropertyGetter citizen) {
        if (citizen.getClass().equals(Dwarf.class)) {
            final Dwarf dwarf = (Dwarf) citizen;
            return mvCitizenNames.contains(dwarf.getName());
        }
        else {
            System.out.println("[ExclusionList] Class of object is not Dwarf");
            return false;
        }
    }

    @Override
    public ExclusionList deepClone() {
        final ArrayList<String> vNames = new ArrayList<String>(
                mvCitizenNames.size());
        for (final String name : mvCitizenNames) {
            vNames.add(name);
        }
        return new ExclusionList(this.getID(), this.getName(), this.isActive()
                , vNames);
    }
}
