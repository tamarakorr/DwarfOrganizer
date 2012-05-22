/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.ArrayList;
import java.util.List;

/**
 * For converting a list of (or single) MyPropertyGetter(s) to a data vector
 * (as used in JTable).
 * colProperties should contain objects that match up with MyPropertyGetter
 * properties.
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class DataVectorMaker<T extends MyPropertyGetter> {
    public DataVectorMaker() {
        super();
    }

    public ArrayList<ArrayList<Object>> toDataVector(final List<T> list
            , final List<Object> lstColProperties
            , final List<Integer> lstColIndices
            , final boolean humanReadable, final int numCols) {

        final int size = list.size();
        final ArrayList<ArrayList<Object>> lstReturn
                = new ArrayList<ArrayList<Object>>(size);

        for (final MyPropertyGetter item : list) {
            lstReturn.add(toRowData(item, lstColProperties, lstColIndices
                    , humanReadable, numCols));
        }

        return lstReturn;
    }
    public ArrayList<Object> toRowData(final MyPropertyGetter rowItem
            , final List<Object> lstColProperties
            , final List<Integer> lstColIndices, final boolean humanReadable
            , final int numCols) {

        final ArrayList<Object> lstReturn = new ArrayList<Object>(numCols);
        for (int index = 0; index < lstColProperties.size(); index++) {   // for (Object col : vColProperties)
            final Object col = lstColProperties.get(index);
            final int colIndex = lstColIndices.get(index);
            lstReturn.set(colIndex
                    , rowItem.getProperty(col.toString(), humanReadable));
        }
        return lstReturn;
    }
}
