/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobBlacklist extends JobList implements BinRule<JobOpening> {

    @Override
    public boolean canItemsBeBinned(JobOpening job1, JobOpening job2) {
        if (job1.name.equals(job2.name))
            return false;
        else
            return (! this.areItemsListedTogether(job1.name, job2.name));
    }

}
