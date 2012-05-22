/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobList extends HashMap<String, List<String>> {

    public JobList() { super(); }

    public void print() {
        for (String key : this.keySet()) {
            System.out.println("Whitelist for " + key + ":");
            final List<String> list = this.get(key);
            for (String element : list)
                System.out.println("     " + element);
        }
    }

    // Adds a one-way entry to the job list, with the first job as the
    // key and the second as the value.
    public void addOneWayEntry(String keyJob, String valueJob) {
        if (this.containsKey(keyJob))
            this.get(keyJob).add(valueJob);
        else {
            final List<String> list = new ArrayList<String>();
            list.add(valueJob);
            this.put(keyJob, list);
        }
    }

    protected boolean areItemsListedTogether(String item1, String item2) {
        if (this.containsKey(item1)) {
            if (this.get(item1).contains(item2))
                return true;
        }

        if (this.containsKey(item2)) {
            if (this.get(item2).contains(item1))
                return true;
        }
        return false;
    }

}
