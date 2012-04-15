/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import myutils.MySimpleLogDisplay;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobOptimizer { // implements ActionListener 
    
    public static final int MAX_TIME = 100;  // Maximum time units allowed to be spent scheduled for work
        
    private static int NUM_JOBS;
    private static int NUM_DWARVES;
    
    private boolean[][] mbSolution;
    private double [] mdblScores; // Individual scores are just tracked for reporting
    
    private Hashtable<String, Integer> mhtJobNameToIndex = new Hashtable<String, Integer>();
    
    private Vector<Job> mvJobs;
    private Vector<Dwarf> mvDwarves;
    private JobBlacklist mhtJobBlacklist;
    
    private MySimpleLogDisplay moLog = new MySimpleLogDisplay();    
    
    protected class Solution {
        protected Vector<Job> jobs;
        protected Vector<Dwarf> dwarves;
        protected boolean[][] dwarfjobmap;
        protected double[] dwarfscores;
        
        public Solution(Vector<Job> jobs, Vector<Dwarf> dwarves
                , boolean[][] solution, double[] dwarfscores) {
            this.jobs = jobs;
            this.dwarves = dwarves;
            this.dwarfjobmap = solution;
            this.dwarfscores = dwarfscores;
        }
    }
    
    private class SolutionImpossibleException extends Exception {
        public SolutionImpossibleException() { super(); }
    } ;
                
    public JobOptimizer(Vector<Job> vJobs, Vector<Dwarf> vDwarves
            , JobBlacklist htBlacklist) {
        
        mvJobs = vJobs;
        mvDwarves = vDwarves;
        mhtJobBlacklist = htBlacklist;
    }
    
    public int optimize() {
        
        NUM_JOBS = mvJobs.size();
        moLog.addEvent("There are " + NUM_JOBS + " jobs for assignment");
        NUM_DWARVES = mvDwarves.size();
        moLog.addEvent("There are " + NUM_DWARVES + " dwarves for assignment");
        
        // Create job name->index lookup table
        createJobIndex();
        
        // Summarize
        long jobHours = 0l;
        for (Job job : mvJobs)
            jobHours += job.time * job.qtyDesired;
        long dwarfHours = 0l;
        for (Dwarf dwarf : mvDwarves)
            dwarfHours += dwarf.getTime();
        moLog.addEvent(jobHours + " job hours to be matched with " + dwarfHours // mvDwarves.size()
                + " dwarf hours");
        
        if (jobHours > dwarfHours) {
            System.err.println("A solution is impossible: there are not enough dwarves. Aborting.");
            moLog.addEvent("A solution is impossible: there are not enough dwarves. Aborting.");
        }
        else {
            // Process jobs
            try {
                processJobs();

                // Display results
                displayResults();

            } catch (SolutionImpossibleException ignore) {
                System.err.println("(A solution is impossible. Aborting.)");
                moLog.addEvent("(A solution is impossible. Aborting.)");
            } catch (Exception e) {
                e.printStackTrace();
                moLog.addEvent("An error was encountered.");
            }
        }
        
        return 0;
    }
    
    // Hashes job names to vector indices
    private void createJobIndex() {
        for (int iCount = 0; iCount < NUM_JOBS; iCount++) {
            System.out.println(iCount + " " + mvJobs.get(iCount).name);
            mhtJobNameToIndex.put(mvJobs.get(iCount).name, iCount);    // Index it for matching
        }
    }
        
    // maxTime = The maximum time the jobs are allowed to take per dwarf.
    Vector<Long> getValidJobCombos(Vector<Integer> vJobs, int intMaxTime) {

        int intJobs = vJobs.size();
        long maxCombos = Math.round(Math.pow(2d, intJobs));
        Vector<Long> vlngCombos = new Vector<Long>();
        
        System.out.println("  Calculating " + maxCombos + " job combinations...");
        
        // Find all valid combos by time total
        // We only need to go through half the combos since we are
        // inverting each one as well.
        for (long mCount = 0; mCount <= (maxCombos / 2); mCount++) {
            // Get the total time for this combo.
            int intTotalTime1 = getTimeForCombo(vJobs, mCount);
            
            // Check the time total for the inverse of this combo as well.
            int intTotalTime2 = getTimeForCombo(vJobs, ~ mCount);
                        
            // If time total is ok, add these jobs for now.
            if (intTotalTime1 <= intMaxTime && intTotalTime2 <= intMaxTime) {

                vlngCombos.add(mCount);
                /*System.out.println("Combo #" + mCount + ") "
                        + " Time: " + dblTotalTime1
                        + " Inverse combo time: " + dblTotalTime2
                        + " Jobs: " + jobComboToString(mCount, vJobs)
                        + " (Inverse: " + jobComboToString(~ mCount, vJobs) + ")"); */
            }
        }
        System.out.println("  (" + vlngCombos.size() + " valid combos by time)");
        
        // Prune out any jobs that are disallowed together.
        Vector<Long> vReturn = getNonBlacklistedJobs(vlngCombos, vJobs);
        
        return vReturn;
    }
    
    // Returns true if the given combo indexed by the given list of relevant
    // jobs is blacklisted; false otherwise.
    private boolean isComboBlacklisted(long combo, Vector<Integer> relevantJobs) {
        
        int intSize = relevantJobs.size();
        
        for (String job1Name : mhtJobBlacklist.keySet()) {
            int job1Index = mhtJobNameToIndex.get(job1Name);
            
            if (relevantJobs.contains(job1Index)) {
                int job1RelevantIndex = getIndexOfItemInVector(job1Index, relevantJobs);

                if (isJobIncludedInCombo(job1RelevantIndex, combo, intSize)) {

                    for (String job2Name : mhtJobBlacklist.get(job1Name)) {
                        int job2Index = mhtJobNameToIndex.get(job2Name);
                        
                        if (relevantJobs.contains(job2Index)) {
                            int job2RelevantIndex = getIndexOfItemInVector(job2Index
                                    , relevantJobs);

                            if (isJobIncludedInCombo(job2RelevantIndex, combo, intSize)) {
                                //System.out.println("  Eliminating blacklisted combo ("
                                //        + job1Name + " and " + job2Name + ")");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    private int getIndexOfItemInVector(int item, Vector<Integer> vec) {
        for (int iCount = 0; iCount < vec.size(); iCount++)
            if (vec.get(iCount) == item)
                return iCount;
        return -1;
    }
    
    // Returns true if the given list of relevant jobs contains any pairs
    // of blacklisted jobs; false otherwise.
    private boolean containsAnyBlacklisted(Vector<Integer> relevantJobs) {
        
        for (String job1Name : mhtJobBlacklist.keySet()) {
            int job1Index = mhtJobNameToIndex.get(job1Name);
            if (relevantJobs.contains(job1Index))
                
                for (String job2Name : mhtJobBlacklist.get(job1Name)) {
                    int job2Index = mhtJobNameToIndex.get(job2Name);
                    if (relevantJobs.contains(job2Index))
                        return true;
                }
                
        }
        return false;
    }
    
    // Returns a subset of the given combos. The returned combos follow
    // the blacklist and whitelist rules. (Also checks and eliminates disallowed
    // inverted combos.)
    private Vector<Long> getNonBlacklistedJobs(Vector<Long> combos
            , Vector<Integer> relevantJobs) {
        
        Vector<Long> vNonBlacklist;     // Subset of combos, that aren't blacklisted
        
        // Prune out blacklisted jobs first-------------------------------------
        if (containsAnyBlacklisted(relevantJobs)) {
            //System.out.println("(Contains blacklisted job pair(s))");
            vNonBlacklist = new Vector<Long>();
            
            for (long combo : combos) {
                if (! isComboBlacklisted(combo, relevantJobs)
                        && ! isComboBlacklisted(~combo, relevantJobs))
                    vNonBlacklist.add(combo);
            }
        }
        else
            vNonBlacklist = (Vector<Long>) combos.clone();
        System.out.println("  (" + vNonBlacklist.size() + " valid combos by blacklist)");
        
        return vNonBlacklist;       // vReturn
    }
    
    // Returns the time required for the given job combo.
    // If the time is greater than MAX_TIME, a number greater
    // than MAX_TIME, which is not necessarily the total time, will be returned.
    private int getTimeForCombo(Vector<Integer> vJobs, long combo) {
        
        int intJobs = vJobs.size();
        int intTotalTime = 0;
        
        for (int iCount = 0; iCount < intJobs; iCount++) {

            // If the job is selected in this combination
            if (isJobIncludedInCombo(iCount, combo, intJobs)) {
                //dblTotalTime += mvdJobTime.get(vJobs.get(iCount));
                intTotalTime += mvJobs.get(vJobs.get(iCount)).time;
                if (intTotalTime > MAX_TIME) break;
            }
        }
        return intTotalTime;
    }
    
    private boolean isJobIncludedInCombo(int jobIndex, long jobCombo, int numJobs) {
         return (jobCombo
                 & Math.round(Math.pow(2d, numJobs - jobIndex - 1)))
                 > 0;
    }
    
    private Vector<JobOpening> getJobOpenings() {
        Vector<JobOpening> vReturn = new Vector<JobOpening>();
        
        for (Job job : mvJobs) {
            for (int iCount = 0; iCount < job.qtyDesired; iCount++)
                vReturn.add((JobOpening) job);
        }
        
        return vReturn;
    }
    
    private double createInitialSolution() throws SolutionImpossibleException {
        
        BinPack<JobOpening> binPacker = new BinPack<JobOpening>();
        Vector<Vector<JobOpening>> vPackedBins = binPacker.binPack(
                getJobOpenings()
                , MAX_TIME, mhtJobBlacklist);
        
        if (vPackedBins.size() > NUM_DWARVES) {
            System.err.println("The bin packing algorithm requires a minimum of "
                + vPackedBins.size() + " dwarves for this labor assignment.");
            System.err.println("Increase the labor force, loosen restrictions,"
                        + " or decrease the number of jobs and try again.");
            throw new SolutionImpossibleException();
        }
        
        // If bin packing worked out, initialize the solution array and dwarf time.
        mbSolution = new boolean[NUM_JOBS][NUM_DWARVES];
        mdblScores = new double[NUM_DWARVES];           // Initialize scores
        for (Dwarf dwarf : mvDwarves)
            dwarf.setTime(MAX_TIME);
        
        // Assign the bin-packed solution to the dwarves.
        for (int dCount = 0; dCount < vPackedBins.size(); dCount++) {
            Dwarf dwarf = mvDwarves.get(dCount);
            Vector<JobOpening> bin = vPackedBins.get(dCount);
            for (int jCount = 0; jCount < bin.size(); jCount++) {
                JobOpening job = bin.get(jCount);
                int jobIndex = mhtJobNameToIndex.get(job.name);
                mbSolution[jobIndex][dCount] = true;
                
                // Decrease the dwarf's available time
                dwarf.setTime(dwarf.getTime() - job.time);
                
                System.out.println("Selected initial job for "
                        + dwarf.getName() + ": " + job.name);
            }
        }
                
        double skillSum = getSkillSum();
        moLog.addEvent("Initial skill sum: " + skillSum);
        return skillSum;
    }
    
    private void processJobs() throws SolutionImpossibleException {

        double skillSum;        //  = 0
        double oldSkillSum = 0;
        
        skillSum = createInitialSolution();

        int iteration = 0;

        // Trade jobs to improve the sum.
        while (skillSum > oldSkillSum) {

            // Iteration counter and tracking
            iteration++;
            moLog.addEvent("================================");
            moLog.addEvent("Optimizing: Iteration " + iteration + "...");
            //moLog.addEvent("================================");
            
            oldSkillSum = skillSum;

            // Reset scores each iteration
            Arrays.fill(mdblScores, 0);
            
            for (int dwarf = 0; dwarf < mbSolution[0].length; dwarf++) {
                System.out.println("Examining " + mvDwarves.get(dwarf).getName()
                        + "'s jobs...");
                for (int job = 0; job < mbSolution.length; job++) {

                    boolean bReassigned = false;

                    // If this dwarf+job is included in the solution
                    if (mbSolution[job][dwarf]) {

                        // Is another dwarf that is not assigned this job,
                        // better at this job?
                        for (int otherDwarf = 0; otherDwarf < NUM_DWARVES; otherDwarf++) {
                            String jobName = mvJobs.get(job).name;
                            double otherDwarfSkill = mvDwarves.get(otherDwarf).balancedPotentials.get(jobName);
                            double dwarfSkill = mvDwarves.get(dwarf).balancedPotentials.get(jobName);

                            if (! mbSolution[job][otherDwarf]
                                && otherDwarfSkill > dwarfSkill) {     

                                System.out.println(" ("
                                        + mvDwarves.get(otherDwarf).getName()
                                        + " is better at "
                                        + mvJobs.get(job).name + ")");

                                // Now swap them if necessary
                                bReassigned = checkForJobSwap(dwarf, otherDwarf);
                                if (! bReassigned)
                                    System.out.println("  (Jobs are fine as they are.)");
                                else {
                                    System.out.println("  Jobs were swapped."); 
                                    break;
                                }
                            }
                        }
                        if (bReassigned) break;
                    }
                }
            }
            skillSum = getSkillSum();
            if (skillSum != oldSkillSum)
                moLog.addEvent("     New skill sum: " + skillSum);
            else
                moLog.addEvent("Finished optimizing (skill sum " + skillSum + ").");
        }
        updateNewDwarfTime();

    }
    
    // Updates dwarf free time by current solution
    private void updateNewDwarfTime() {
        for (int dwarf = 0; dwarf < mvDwarves.size(); dwarf++) {
            int intTime = MAX_TIME;
            for (int job = 0; job < NUM_JOBS; job++)
                if (mbSolution[job][dwarf])
                    intTime -= mvJobs.get(job).time;
            //mvdDwarfTimeNew.set(dwarf, time);
            mvDwarves.get(dwarf).setTime(intTime);
        }
    }
    
    private boolean checkForJobSwap(int dwarf1, int dwarf2)
            throws SolutionImpossibleException {
        
        //double dblFreeTime1 = mvdDwarfTimeNew.get(dwarf1);
        //double dblFreeTime2 = mvdDwarfTimeNew.get(dwarf2);
        
        int intMaxFreeTime = MAX_TIME;
        
        Vector<Integer> vRelevantJobs = new Vector<Integer>();
        
        // Find all relevant jobs (those held by exactly one of the two dwarves).
        for (int jCount = 0; jCount < NUM_JOBS; jCount++)
            // (Exclusive or -> don't check for swaps if neither or both
            // dwarves have the job.)
            if (mbSolution[jCount][dwarf1] ^ mbSolution[jCount][dwarf2])
                vRelevantJobs.add(jCount);
            // If both dwarves have the job, remove the time taken by the
            // job from the available time.
            else if (mbSolution[jCount][dwarf1] && mbSolution[jCount][dwarf2])
                intMaxFreeTime -= mvJobs.get(jCount).time;
        
        // Find and rate every possible combination of these dwarves' jobs.
        Vector<Long> vCombos = getValidJobCombos(vRelevantJobs, intMaxFreeTime);
        
        if (vCombos.size() == 0) {
            System.err.println("...ERROR: *NO* valid combinations were found,"
                    + " including the currently selected jobs."
                    + " All results beyond this line are invalid.");
            throw new SolutionImpossibleException();
        }
        else
            System.out.println("  ... " + (vCombos.size() * 2) + " valid combinations were found.");
        
        // Rate combos
        int numJobs = vRelevantJobs.size();
        long bestCombo = -1;
        mdblScores[dwarf1] = getSkillSum(dwarf1);   // Record scores for reporting
        mdblScores[dwarf2] = getSkillSum(dwarf2);   
        double currentScore = mdblScores[dwarf1] + mdblScores[dwarf2]; // getSkillSum(dwarf1) + getSkillSum(dwarf2);
        double bestScore = currentScore; //0;
        boolean invertBest = false;
        
        for (int iCount = 0; iCount < vCombos.size(); iCount++) {
            
            double score1 = 0; // First dwarf with 1's, second with 0's
            double score2 = 0; // First dwarf with 0's, second with 1's
            
            long combo = vCombos.get(iCount);
            
            for (int jCount = 0; jCount < vRelevantJobs.size(); jCount++) {
                
                int jobIndex = vRelevantJobs.get(jCount);
                if (isJobIncludedInCombo(jCount, combo, numJobs)) {
                    score1 += getWeightedScore(dwarf1, jobIndex);
                    score2 += getWeightedScore(dwarf2, jobIndex); //mvJobSkill.get(dwarf2).get(jobIndex);
                }
                else {  // Inverse
                    score1 += getWeightedScore(dwarf2, jobIndex); //mvJobSkill.get(dwarf2).get(jobIndex);
                    score2 += getWeightedScore(dwarf1, jobIndex); //mvJobSkill.get(dwarf1).get(jobIndex);
                }
            }
            
            // If any score is better:
            if (score1 > bestScore) {
                bestScore = score1;
                bestCombo = combo;
                invertBest = false;
            }
            if (score2 > bestScore) {   // Inverse of combo is better
                bestScore = score2;
                bestCombo = combo;
                invertBest = true;                
            }
        }
        if ((bestCombo) != -1) {
            if (invertBest) System.out.println("INVERT:");
            System.out.println("Best combo found: #" + bestCombo + ", score " + bestScore);
        }
        
        // Is the best combo different from the current job allocation?
        if (invertBest)
            bestCombo = ~ bestCombo;
        
        // Set the new job allocation if necessary (only if score improves).
        boolean bDifferent = bestScore > currentScore; //false;
        //if (bDifferent) {
            //bDifferent = isJobAllocationDifferent(vRelevantJobs, bestCombo, dwarf1);
            if (bDifferent) {
                setDwarfJobs(dwarf1, bestCombo, vRelevantJobs);
                setDwarfJobs(dwarf2, ~ bestCombo, vRelevantJobs);
            }
        //}        
        return bDifferent;
        
    }
    
    private long getWeightedScore(int dwarfIndex, int jobIndex)
            throws SolutionImpossibleException {
        
        Job thisJob = mvJobs.get(jobIndex);
        Dwarf oDwarf = mvDwarves.get(dwarfIndex);
        
        long potential = -1;
        
        //if (oDwarf.skillPotentials.get(thisJob.skillName) == null)
        if (oDwarf.balancedPotentials.get(thisJob.name) == null) {
            System.err.println("ERROR: Potential for job '"
                    + thisJob.name + "' not found"
                    + " for dwarf " + oDwarf.getName() + "."
                    + " All results are invalid. (Dwarf has "
                    + oDwarf.balancedPotentials.size() + " valid job potentials.)");
            throw new SolutionImpossibleException();
        }
        else {
            //potential = oDwarf.skillPotentials.get(thisJob.skillName);
            potential = oDwarf.balancedPotentials.get(thisJob.name);
        }
        
        return Math.round(thisJob.candidateWeight
                * potential);
    }
    
    private void setDwarfJobs(int dwarfIndex, long jobCombo, Vector<Integer> vJobs) {
        
        int intNumJobs = vJobs.size();
        
        for (int iCount = 0; iCount < intNumJobs; iCount++) {
            int job = vJobs.get(iCount);
            
            boolean bOld = mbSolution[job][dwarfIndex];
            boolean bNew = isJobIncludedInCombo(iCount, jobCombo, intNumJobs); 
            
            if (! bOld && bNew)
                System.out.println(mvDwarves.get(dwarfIndex).getName() + " +"
                        + mvJobs.get(job).name + " (" + mvJobs.get(job).time
                        + " time units)");
            else if (bOld && ! bNew)
                System.out.println(mvDwarves.get(dwarfIndex).getName()
                        + " -" + mvJobs.get(job).name
                        + " (" + mvJobs.get(job).time + " time units"
                        + ")");
            
            mbSolution[job][dwarfIndex] = bNew;
        }
        
    }
    
    private double getSkillSum() throws SolutionImpossibleException {
        
        double sum = 0;
        
        for (int dCount = 0; dCount < mvDwarves.size(); dCount++)
            sum += getSkillSum(dCount);
        
        return sum;
        
        /*for (int jCount = 0; jCount < NUM_JOBS; jCount++)
            for (int dCount = 0; dCount < mvstrDwarfNames.size(); dCount++) {
                if (mbSolution[jCount][dCount])
                    sum += mvJobSkill.get(dCount).get(jCount);
            }
                
        return sum; */
        
    }
    
    private double getSkillSum(int dwarf) throws SolutionImpossibleException {
        
        double sum = 0;
        //Vector<Integer> dwarfSkill = mvJobSkill.get(dwarf);
        
        for (int jCount = 0; jCount < NUM_JOBS; jCount++)
            if (mbSolution[jCount][dwarf])
                sum += getWeightedScore(dwarf, jCount); //dwarfSkill.get(jCount);
        
        return sum;
    }
    
    private boolean isJobLegalForDwarf(int job, int dwarf) {
        for (int iCount = 0; iCount < NUM_JOBS; iCount++) {
            if (mbSolution[iCount][dwarf]) {
                if (mhtJobBlacklist.areItemsListedTogether(mvJobs.get(iCount).name
                        , mvJobs.get(job).name))
                    return false;
/*                if (! areJobsAllowedTogether(mvJobs.get(iCount).name
                    , mvJobs.get(job).name))
                    return false; */
            }
        }
        return true;
    }
    
    private boolean areJobsAllowedTogether(String job1, String job2) {
        if (mhtJobBlacklist.containsKey(job1)) { 
            //System.out.println("CONTAINS");
            if (mhtJobBlacklist.get(job1).contains(job2))
                return false;
        }
        
        if (mhtJobBlacklist.containsKey(job2)) {
            //System.out.println("CONTAINS");
            if (mhtJobBlacklist.get(job2).contains(job1))
                return false;
        }
        
        return true;
    }
    
    private void displayResults() { // throws SolutionImpossibleException 
        new ResultsView(new Solution(mvJobs, mvDwarves, mbSolution, mdblScores));
    }
}
