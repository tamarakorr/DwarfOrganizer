/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.List;
import java.util.Vector;

/**
 * For converting a list of MyPropertyGetters to a data vector (as used in
 * JTable).
 * colProperties should contain objects that match up with MyPropertyGetter
 * properties. 
 * @author Tamara Orr
 */
public class DataVectorMaker<T extends MyPropertyGetter> {
    public DataVectorMaker() {
        super();
    }
    
    public Vector toDataVector(List<T> list
            , Vector<Object> vColProperties, Vector<Integer> vColIndices
            , boolean humanReadable, int numCols) {
        Vector<Vector<Object>> vReturn = new Vector<Vector<Object>>();
        
        for (MyPropertyGetter item : list) {
            Vector<Object> vOneItem = new Vector<Object>();
            vOneItem.setSize(numCols);
            for (int index = 0; index < vColProperties.size(); index++) {   // for (Object col : vColProperties) 
                Object col = vColProperties.get(index);
                Integer colIndex = vColIndices.get(index);
                vOneItem.set(colIndex
                        , item.getProperty(col.toString(), humanReadable));
            }
            vReturn.add(vOneItem);
        }
        
        return vReturn;
    }
}
