/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.List;
import java.util.Vector;

/**
 * For converting a list of (or single) MyPropertyGetter(s) to a data vector
 * (as used in JTable).
 * colProperties should contain objects that match up with MyPropertyGetter
 * properties. 
 * @author Tamara Orr
 */
public class DataVectorMaker<T extends MyPropertyGetter> {
    public DataVectorMaker() {
        super();
    }
    
    public Vector<Vector<Object>> toDataVector(List<T> list
            , Vector<Object> vColProperties, Vector<Integer> vColIndices
            , boolean humanReadable, int numCols) {
        Vector<Vector<Object>> vReturn = new Vector<Vector<Object>>();
        
        for (MyPropertyGetter item : list) {
            vReturn.add(toRowData(item, vColProperties, vColIndices
                    , humanReadable, numCols));
        }
        
        return vReturn;
    }
    public Vector<Object> toRowData(MyPropertyGetter rowItem, Vector<Object> vColProperties
            , Vector<Integer> vColIndices, boolean humanReadable, int numCols) {
        Vector<Object> vReturn = new Vector<Object>();
        vReturn.setSize(numCols);
        for (int index = 0; index < vColProperties.size(); index++) {   // for (Object col : vColProperties) 
            Object col = vColProperties.get(index);
            Integer colIndex = vColIndices.get(index);
            vReturn.set(colIndex
                    , rowItem.getProperty(col.toString(), humanReadable));
        }
        return vReturn;
    }
}
