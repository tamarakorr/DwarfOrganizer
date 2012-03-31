/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * MIT license: Refer to license.txt
 */
public class BinPack<T extends Binnable> {

    // Packs the given items into bins of the given size using a first-fit
    // decreasing (FFD) algorithm.
    // Returns a vector whose size is the number of bins used. Each entry
    // in the vector is another vector containing the list of items in that bin.
    public Vector<Vector<T>> binPack(Vector<T> items, int binSize
            , BinRule<T> binRule) {
        
        final int MAX_BINS = items.size();
        Vector<Vector<T>> vReturn = new Vector<Vector<T>>();
        
        // Initialize used bin size to zero
        Vector<Integer> vBinSize = new Vector<Integer>(MAX_BINS);   // Stores used bin size
        for (int iCount = 0; iCount < MAX_BINS; iCount++)
            vBinSize.add(0);
        
        // Create a sorted (descending) list of the items.
        Vector<T> vOrderedItems = (Vector<T>) items.clone();
        Comparator comparator = Collections.reverseOrder();
        Collections.sort(vOrderedItems, comparator);
        //System.out.println("Finding bins for items");
        
        // Find a bin for each item.
        for (T item : vOrderedItems) {

            int intItemSize = (Integer) item.getProperty("size");
            //System.out.println("Finding bin for item of size " + intItemSize); 
            
            // Find the item a bin            
            for (int bin = 0; bin < MAX_BINS; bin++) {
                int intBinSize = vBinSize.get(bin);
                int intSizeRemaining = binSize - intBinSize;
                //System.out.println("bin #" + bin + " current used size = " + intBinSize);
                
                // If the item fits in the bin
                if (intItemSize <= intSizeRemaining) {
                    
                    // If placing this item in this bin does not violate a rule
                    boolean bAccept;
                    if (vReturn.size() >= (bin + 1))
                        bAccept = binAccepts(vReturn.get(bin), item, binRule);
                    else
                        bAccept = true;
                    
                    if (bAccept) {
                    
                        // Create a bin if needed
                        if (vReturn.size() < bin + 1) {
                            //System.out.println("Creating bin #" + (bin + 1));                        
                            vReturn.add(new Vector<T>());
                        }
                        vReturn.get(bin).add(item);

                        // Update the used bin size
                        vBinSize.set(bin, intBinSize + intItemSize);
                        //System.out.println("Bin #" + bin + " +" + item);
                        break;
                    }
                }
            }
                
        }
        //System.out.println("Returning");        
        return vReturn;
        
    }
    
    // Returns true if the given bin items can be combined with the given item
    private boolean binAccepts(Vector<T> bin, T item, BinRule binRule) {
        
        for (T binnedItem : bin) {
            if (! binRule.canItemsBeBinned(binnedItem, item))
                return false;
        }
        return true;
    }
    
}
