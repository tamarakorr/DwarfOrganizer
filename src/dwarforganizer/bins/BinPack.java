/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.bins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
    public ArrayList<ArrayList<T>> binPack(final ArrayList<T> items
            , final int binSize, final BinRule<T> binRule) {

        final int MAX_BINS = items.size();
        final ArrayList<ArrayList<T>> lstReturn = new ArrayList<ArrayList<T>>();

        // Initialize used bin size to zero
        final ArrayList<Integer> lstBinSize = new ArrayList<Integer>(MAX_BINS);   // Stores used bin size
        for (int iCount = 0; iCount < MAX_BINS; iCount++)
            lstBinSize.add(0);

        // Create a sorted (descending) list of the items.
        final ArrayList<T> lstOrderedItems = (ArrayList<T>) items.clone();
        final Comparator comparator = Collections.reverseOrder();
        Collections.sort(lstOrderedItems, comparator);
        //System.out.println("Finding bins for items");

        // Find a bin for each item.
        for (final T item : lstOrderedItems) {

            final int intItemSize = (Integer) item.getProperty("size", false);
            //System.out.println("Finding bin for item of size " + intItemSize);

            // Find the item a bin
            for (int bin = 0; bin < MAX_BINS; bin++) {
                final int intBinSize = lstBinSize.get(bin);
                final int intSizeRemaining = binSize - intBinSize;
                //System.out.println("bin #" + bin + " current used size = " + intBinSize);

                // If the item fits in the bin
                if (intItemSize <= intSizeRemaining) {

                    // If placing this item in this bin does not violate a rule
                    final boolean bAccept;
                    if (lstReturn.size() >= (bin + 1))
                        bAccept = binAccepts(lstReturn.get(bin), item, binRule);
                    else
                        bAccept = true;

                    if (bAccept) {

                        // Create a bin if needed
                        if (lstReturn.size() < bin + 1) {
                            //System.out.println("Creating bin #" + (bin + 1));
                            lstReturn.add(new ArrayList<T>());
                        }
                        lstReturn.get(bin).add(item);

                        // Update the used bin size
                        lstBinSize.set(bin, intBinSize + intItemSize);
                        //System.out.println("Bin #" + bin + " +" + item);
                        break;
                    }
                }
            }

        }
        //System.out.println("Returning");
        return lstReturn;

    }

    // Returns true if the given bin items can be combined with the given item
    private boolean binAccepts(final List<T> bin, final T item
            , final BinRule binRule) {

        for (final T binnedItem : bin) {
            if (! binRule.canItemsBeBinned(binnedItem, item))
                return false;
        }
        return true;
    }
}
