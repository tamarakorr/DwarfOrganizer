/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.bins.BinPack;
import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import myutils.MyString;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class JobOptimizer { // implements ActionListener

    private static final Logger logger = Logger.getLogger(
            JobOptimizer.class.getName());

    private static final int LOG_DIVIDER_LENGTH = 80;
    public static final int MAX_TIME = 100;  // Maximum time units allowed to be spent scheduled for work

    private int NUM_JOBS;
    private int NUM_DWARVES;

    private boolean[][] mbSolution;
    private double [] mdblScores; // Individual scores are just tracked for reporting

    private Map<String, Integer> mmapJobNameToIndex
            = new HashMap<String, Integer>();

    private List<Job> mlstJobs;
    private List<Dwarf> mlstDwarves;
    private JobBlacklist mhtJobBlacklist;

    public JobOptimizer(final List<Job> vJobs, final List<Dwarf> vDwarves
            , final JobBlacklist htBlacklist, final Handler handler) {

        mlstJobs = vJobs;
        mlstDwarves = vDwarves;
        mhtJobBlacklist = htBlacklist;

        addHandlerIfNew(handler);
    }
    private void addHandlerIfNew(final Handler newHandler) {
        for (final Handler handler : logger.getHandlers()) {
            if (handler.equals(newHandler))
                return;
        }
        logger.addHandler(newHandler);
    }
    protected class Solution {
        protected List<Job> jobs;
        protected List<Dwarf> dwarves;
        protected boolean[][] dwarfjobmap;
        protected double[] dwarfscores;

        public Solution(final List<Job> jobs, final List<Dwarf> dwarves
                , final boolean[][] solution, final double[] dwarfscores) {
            this.jobs = jobs;
            this.dwarves = dwarves;
            this.dwarfjobmap = solution;
            this.dwarfscores = dwarfscores;
        }

        public List<Dwarf> getDwarves() {
            return dwarves;
        }

        public List<Job> getJobs() {
            return jobs;
        }
    }

    private class SolutionImpossibleException extends Exception {
        public SolutionImpossibleException() { super(); }
    } ;

    public int optimize() {

        NUM_JOBS = mlstJobs.size();
        logger.log(Level.INFO, "Found {0} jobs for assignment...", NUM_JOBS);
        NUM_DWARVES = mlstDwarves.size();
        logger.log(Level.INFO, "Found {0} dwarves available for assignment..."
                , NUM_DWARVES);

        // Create job name->index lookup table
        createJobIndex();

        // Summarize
        long jobHours = 0L;
        for (final Job job : mlstJobs)
            jobHours += job.getTime() * job.getQtyDesired();
        long dwarfHours = 0L;
        for (final Dwarf dwarf : mlstDwarves)
            dwarfHours += dwarf.getTime();
        logger.log(Level.INFO
                , "{0} job hours to be matched with {1} dwarf hours."
                , new Object[]{jobHours, dwarfHours});

        if (jobHours > dwarfHours) {
            final String message = "A solution is impossible: there are not"
                    + " enough dwarves. Aborting.";
            logger.severe(message);
        }
        else {
            // Process jobs
            try {
                processJobs();

                // Display results
                displayResults();

            } catch (final SolutionImpossibleException ignore) {
                final String message = "(A solution is impossible. Aborting.)";
                logger.severe(message);
            } catch (final Exception e) {
                final String message = "An error was encountered.";
                logger.log(Level.SEVERE, message, e);
            }
        }
        return 0;
    }

    // Hashes job names to vector indices
    private void createJobIndex() {
        for (int iCount = 0; iCount < NUM_JOBS; iCount++) {
            logger.log(Level.FINE, "{0} {1}"
                    , new Object[]{iCount, mlstJobs.get(iCount).getName()});
            mmapJobNameToIndex.put(mlstJobs.get(iCount).getName(), iCount);    // Index it for matching
        }
    }

    // maxTime = The maximum time the jobs are allowed to take per dwarf.
    ArrayList<Long> getValidJobCombos(final ArrayList<Integer> lstJobs
            , final int intMaxTime) {

        final int intJobs = lstJobs.size();
        final long maxCombos = Math.round(Math.pow(2d, intJobs));
        final ArrayList<Long> lstCombos = new ArrayList<Long>();

        logger.log(Level.FINE, "  Calculating {0} job combinations..."
                , maxCombos);
        // Find all valid combos by time total
        // We only need to go through half the combos since we are
        // inverting each one as well.
        for (long mCount = 0; mCount <= (maxCombos / 2); mCount++) {
            // Get the total time for this combo.
            final int intTotalTime1 = getTimeForCombo(lstJobs, mCount);

            // Check the time total for the inverse of this combo as well.
            final int intTotalTime2 = getTimeForCombo(lstJobs, ~ mCount);

            // If time total is ok, add these jobs for now.
            if (intTotalTime1 <= intMaxTime && intTotalTime2 <= intMaxTime) {

                lstCombos.add(mCount);
                /*System.out.println("Combo #" + mCount + ") "
                        + " Time: " + dblTotalTime1
                        + " Inverse combo time: " + dblTotalTime2
                        + " Jobs: " + jobComboToString(mCount, vJobs)
                        + " (Inverse: " + jobComboToString(~ mCount, vJobs) + ")"); */
            }
        }
        logger.log(Level.FINE, "  ({0} valid combos by time)"
                , lstCombos.size());

        // Prune out any jobs that are disallowed together.
        final ArrayList<Long> lstReturn
                = getNonBlacklistedJobs(lstCombos, lstJobs);

        return lstReturn;
    }

    // Returns true if the given combo indexed by the given list of relevant
    // jobs is blacklisted; false otherwise.
    private boolean isComboBlacklisted(final long combo
            , final List<Integer> relevantJobs) {

        final int intSize = relevantJobs.size();

        for (final String job1Name : mhtJobBlacklist.keySet()) {
            final int job1Index = mmapJobNameToIndex.get(job1Name);

            if (relevantJobs.contains(job1Index)) {
                final int job1RelevantIndex = getIndexOfItemInList(job1Index
                        , relevantJobs);

                if (isJobIncludedInCombo(job1RelevantIndex, combo, intSize)) {

                    for (final String job2Name : mhtJobBlacklist.get(job1Name)) {
                        final int job2Index = mmapJobNameToIndex.get(job2Name);

                        if (relevantJobs.contains(job2Index)) {
                            final int job2RelevantIndex = getIndexOfItemInList(
                                    job2Index, relevantJobs);

                            if (isJobIncludedInCombo(job2RelevantIndex, combo
                                    , intSize)) {
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

    private int getIndexOfItemInList(final int item, final List<Integer> list) {
        for (int iCount = 0; iCount < list.size(); iCount++)
            if (list.get(iCount) == item)
                return iCount;
        return -1;
    }

    // Returns true if the given list of relevant jobs contains any pairs
    // of blacklisted jobs; false otherwise.
    private boolean containsAnyBlacklisted(final List<Integer> relevantJobs) {

        for (final String job1Name : mhtJobBlacklist.keySet()) {
            final int job1Index = mmapJobNameToIndex.get(job1Name);
            if (relevantJobs.contains(job1Index)) {
                for (final String job2Name : mhtJobBlacklist.get(job1Name)) {
                    final int job2Index = mmapJobNameToIndex.get(job2Name);
                    if (relevantJobs.contains(job2Index))
                        return true;
                }
            }
        }
        return false;
    }

    // Returns a subset of the given combos. The returned combos follow
    // the blacklist and whitelist rules. (Also checks and eliminates disallowed
    // inverted combos.)
    private ArrayList<Long> getNonBlacklistedJobs(final ArrayList<Long> combos
            , final List<Integer> relevantJobs) {

        ArrayList<Long> lstNonBlacklist;     // Subset of combos, that aren't blacklisted

        // Prune out blacklisted jobs first-------------------------------------
        if (containsAnyBlacklisted(relevantJobs)) {
            //System.out.println("(Contains blacklisted job pair(s))");
            lstNonBlacklist = new ArrayList<Long>();

            for (final long combo : combos) {
                if (! isComboBlacklisted(combo, relevantJobs)
                        && ! isComboBlacklisted(~combo, relevantJobs))
                    lstNonBlacklist.add(combo);
            }
        }
        else {
            lstNonBlacklist = (ArrayList<Long>) combos.clone();
        }
        logger.log(Level.FINE, "  ({0} valid combos by blacklist)"
                , lstNonBlacklist.size());
        return lstNonBlacklist;       // vReturn
    }

    // Returns the time required for the given job combo.
    // If the time is greater than MAX_TIME, a number greater
    // than MAX_TIME, which is not necessarily the total time, will be returned.
    private int getTimeForCombo(final List<Integer> vJobs, final long combo) {

        final int intJobs = vJobs.size();
        int intTotalTime = 0;

        for (int iCount = 0; iCount < intJobs; iCount++) {

            // If the job is selected in this combination
            if (isJobIncludedInCombo(iCount, combo, intJobs)) {
                //dblTotalTime += mvdJobTime.get(vJobs.get(iCount));
                intTotalTime += mlstJobs.get(vJobs.get(iCount)).getTime();
                if (intTotalTime > MAX_TIME) break;
            }
        }
        return intTotalTime;
    }

    private boolean isJobIncludedInCombo(final int jobIndex, final long jobCombo
            , final int numJobs) {

         return (jobCombo
                 & Math.round(Math.pow(2d, numJobs - jobIndex - 1)))
                 > 0;
    }

    private ArrayList<JobOpening> getJobOpenings() {
        final ArrayList<JobOpening> lstReturn = new ArrayList<JobOpening>();

        for (final Job job : mlstJobs) {
            for (int iCount = 0; iCount < job.getQtyDesired(); iCount++)
                lstReturn.add((JobOpening) job);
        }

        return lstReturn;
    }

    private double createInitialSolution() throws SolutionImpossibleException {

        final BinPack<JobOpening> binPacker = new BinPack<JobOpening>();
        final ArrayList<ArrayList<JobOpening>> vPackedBins = binPacker.binPack(
                getJobOpenings(), MAX_TIME, mhtJobBlacklist);

        if (vPackedBins.size() > NUM_DWARVES) {
            logger.log(Level.SEVERE, "The bin packing algorithm requires a"
                    + " minimum of {0} dwarves for this labor assignment."
                    , vPackedBins.size());
            logger.severe("Increase the labor force, loosen restrictions,"
                        + " or decrease the number of jobs before optimizing.");
            throw new SolutionImpossibleException();
        }

        // If bin packing worked out, initialize the solution array and dwarf time.
        mbSolution = new boolean[NUM_JOBS][NUM_DWARVES];
        mdblScores = new double[NUM_DWARVES];           // Initialize scores
        for (final Dwarf dwarf : mlstDwarves)
            dwarf.setTime(MAX_TIME);

        // Assign the bin-packed solution to the dwarves.
        for (int dCount = 0; dCount < vPackedBins.size(); dCount++) {
            final Dwarf dwarf = mlstDwarves.get(dCount);
            final List<JobOpening> bin = vPackedBins.get(dCount);
            for (int jCount = 0; jCount < bin.size(); jCount++) {
                final JobOpening job = bin.get(jCount);
                final int jobIndex = mmapJobNameToIndex.get(job.getName());
                mbSolution[jobIndex][dCount] = true;

                // Decrease the dwarf's available time
                dwarf.setTime(dwarf.getTime() - job.getTime());

                logger.log(Level.FINE, "Assigned an initial job for {0}: {1}"
                        , new Object[]{dwarf.getName(), job.getName()});
            }
        }

        final double skillSum = getSkillSum();
        logger.log(Level.INFO, "Initial skill sum: {0}", skillSum);
        return skillSum;
    }

    private void processJobs() throws SolutionImpossibleException {

        double oldSkillSum = 0;
        double skillSum = createInitialSolution();
        int iteration = 0;

        // Trade jobs to improve the sum.
        while (skillSum > oldSkillSum) {

            // Iteration counter and tracking
            iteration++;
            logger.info(MyString.repeat("-", LOG_DIVIDER_LENGTH));
            logger.log(Level.INFO, "Optimizing: Iteration {0}...", iteration);

            oldSkillSum = skillSum;

            // Reset scores each iteration
            Arrays.fill(mdblScores, 0);

            for (int dwarf = 0; dwarf < mbSolution[0].length; dwarf++) {
                logger.log(Level.FINE, "Examining {0}''s jobs..."
                        , mlstDwarves.get(dwarf).getName());
                for (int job = 0; job < mbSolution.length; job++) {

                    boolean bReassigned = false;

                    // If this dwarf+job is included in the solution
                    if (mbSolution[job][dwarf]) {

                        // Is another dwarf that is not assigned this job,
                        // better at this job?
                        for (int otherDwarf = 0; otherDwarf < NUM_DWARVES; otherDwarf++) {
                            final String jobName = mlstJobs.get(job).getName();
                            final double otherDwarfSkill = mlstDwarves.get(
                                    otherDwarf).getBalancedPotentials().get(
                                    jobName);
                            final double dwarfSkill = mlstDwarves.get(
                                    dwarf).getBalancedPotentials().get(jobName);

                            if (! mbSolution[job][otherDwarf]
                                && otherDwarfSkill > dwarfSkill) {

                                Logger.getLogger(
                                        JobOptimizer.class.getName()).log(
                                        Level.FINE, " ({0} is better at {1})"
                                        , new Object[]{mlstDwarves.get(
                                        otherDwarf).getName()
                                                , mlstJobs.get(job).getName()});

                                // Now swap them if necessary
                                bReassigned = checkForJobSwap(dwarf
                                        , otherDwarf);
                                if (! bReassigned) {
                                    Logger.getLogger(
                                            JobOptimizer.class.getName()).fine(
                                            "  (Jobs are fine as they are.)");
                                }
                                else {
                                    Logger.getLogger(
                                            JobOptimizer.class.getName()).fine(
                                            "  Jobs were swapped.");
                                    break;
                                }
                            }
                        }
                        if (bReassigned) break;
                    }
                }
            }
            skillSum = getSkillSum();
            if (skillSum != oldSkillSum) {
                logger.log(Level.INFO, "     New skill sum: {0}", skillSum);
            }
            else {
                logger.log(Level.INFO, "Finished optimizing (skill sum {0})."
                        , skillSum);
            }
        }
        updateNewDwarfTime();

        addFinishedLogEntry();
    }
    private void addFinishedLogEntry() {
        final String done = "(Done)";
        final String dashes = MyString.repeat("-"
                , (LOG_DIVIDER_LENGTH / 2) - (done.length() / 2));
        logger.log(Level.INFO, "{0}{1}{2}", new Object[]{dashes, done, dashes});
    }
    // Updates dwarf free time by current solution
    private void updateNewDwarfTime() {
        for (int dwarf = 0; dwarf < mlstDwarves.size(); dwarf++) {
            int intTime = MAX_TIME;
            for (int job = 0; job < NUM_JOBS; job++)
                if (mbSolution[job][dwarf])
                    intTime -= mlstJobs.get(job).getTime();
            //mvdDwarfTimeNew.set(dwarf, time);
            mlstDwarves.get(dwarf).setTime(intTime);
        }
    }

    private boolean checkForJobSwap(final int dwarf1, final int dwarf2)
            throws SolutionImpossibleException {

        //double dblFreeTime1 = mvdDwarfTimeNew.get(dwarf1);
        //double dblFreeTime2 = mvdDwarfTimeNew.get(dwarf2);

        int intMaxFreeTime = MAX_TIME;
        final ArrayList<Integer> lstRelevantJobs = new ArrayList<Integer>();

        // Find all relevant jobs (those held by exactly one of the two dwarves).
        for (int jCount = 0; jCount < NUM_JOBS; jCount++)
            // (Exclusive or -> don't check for swaps if neither or both
            // dwarves have the job.)
            if (mbSolution[jCount][dwarf1] ^ mbSolution[jCount][dwarf2])
                lstRelevantJobs.add(jCount);
            // If both dwarves have the job, remove the time taken by the
            // job from the available time.
            else if (mbSolution[jCount][dwarf1] && mbSolution[jCount][dwarf2])
                intMaxFreeTime -= mlstJobs.get(jCount).getTime();

        // Find and rate every possible combination of these dwarves' jobs.
        final ArrayList<Long> lstCombos = getValidJobCombos(lstRelevantJobs
                , intMaxFreeTime);

        if (lstCombos.isEmpty()) {
            logger.severe("...ERROR: *NO* valid combinations were found,"
                    + " including the currently selected jobs."
                    + " All results beyond this line are invalid.");
            throw new SolutionImpossibleException();
        }
        else {
            logger.log(Level.FINE, "  ... {0} valid combinations were found."
                    , (lstCombos.size() * 2));
        }

        // Rate combos
        final int numJobs = lstRelevantJobs.size();
        long bestCombo = -1;
        mdblScores[dwarf1] = getSkillSum(dwarf1);   // Record scores for reporting
        mdblScores[dwarf2] = getSkillSum(dwarf2);
        final double currentScore = mdblScores[dwarf1] + mdblScores[dwarf2]; // getSkillSum(dwarf1) + getSkillSum(dwarf2);
        double bestScore = currentScore; //0;
        boolean invertBest = false;

        for (int iCount = 0; iCount < lstCombos.size(); iCount++) {

            double score1 = 0; // First dwarf with 1's, second with 0's
            double score2 = 0; // First dwarf with 0's, second with 1's

            final long combo = lstCombos.get(iCount);

            for (int jCount = 0; jCount < lstRelevantJobs.size(); jCount++) {

                final int jobIndex = lstRelevantJobs.get(jCount);
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
            if (invertBest) {
                logger.fine("INVERT:");
            }
            logger.log(Level.FINE, "Best combo found: #{0}, score {1}"
                    , new Object[]{bestCombo, bestScore});
        }

        // Is the best combo different from the current job allocation?
        if (invertBest)
            bestCombo = ~ bestCombo;

        // Set the new job allocation if necessary (only if score improves).
        final boolean bDifferent = bestScore > currentScore; //false;
        //if (bDifferent) {
            //bDifferent = isJobAllocationDifferent(vRelevantJobs, bestCombo, dwarf1);
            if (bDifferent) {
                setDwarfJobs(dwarf1, bestCombo, lstRelevantJobs);
                setDwarfJobs(dwarf2, ~ bestCombo, lstRelevantJobs);
            }
        //}
        return bDifferent;

    }

    private long getWeightedScore(final int dwarfIndex, final int jobIndex)
            throws SolutionImpossibleException {

        final Job thisJob = mlstJobs.get(jobIndex);
        final Dwarf oDwarf = mlstDwarves.get(dwarfIndex);

        long potential = -1;

        //if (oDwarf.skillPotentials.get(thisJob.skillName) == null)
        if (oDwarf.getBalancedPotentials().get(thisJob.getName()) == null) {
            logger.log(Level.SEVERE,"ERROR: Potential for job ''{0}'' not found"
                    + " for dwarf {1}. All results are invalid. (Dwarf has {2}"
                    + " valid job potentials.)"
                    , new Object[]{thisJob.getName(), oDwarf.getName()
                            , oDwarf.getBalancedPotentials().size()});
            throw new SolutionImpossibleException();
        }
        else {
            //potential = oDwarf.skillPotentials.get(thisJob.skillName);
            potential = oDwarf.getBalancedPotentials().get(thisJob.getName());
        }

        return Math.round(thisJob.getCandidateWeight() * potential);
    }

    private void setDwarfJobs(final int dwarfIndex, final long jobCombo
            , final List<Integer> vJobs) {

        final int intNumJobs = vJobs.size();

        for (int iCount = 0; iCount < intNumJobs; iCount++) {
            final int job = vJobs.get(iCount);

            final boolean bOld = mbSolution[job][dwarfIndex];
            final boolean bNew = isJobIncludedInCombo(iCount, jobCombo
                    , intNumJobs);
            String strPlusMinus = "";

            if (! bOld && bNew) {
                strPlusMinus = "+";
            }
            else if (bOld && ! bNew) {
                strPlusMinus = "-";
            }
            if (bOld ^ bNew) { // XOR
                logger.log(Level.FINE, "{0} {3}{1} ({2} time units)"
                    , new Object[]{mlstDwarves.get(dwarfIndex).getName()
                            , mlstJobs.get(job).getName()
                            , mlstJobs.get(job).getTime(), strPlusMinus});
            }
            mbSolution[job][dwarfIndex] = bNew;
        }

    }

    private double getSkillSum() throws SolutionImpossibleException {

        double sum = 0;

        for (int dCount = 0; dCount < mlstDwarves.size(); dCount++)
            sum += getSkillSum(dCount);

        return sum;

        /*for (int jCount = 0; jCount < NUM_JOBS; jCount++)
            for (int dCount = 0; dCount < mvstrDwarfNames.size(); dCount++) {
                if (mbSolution[jCount][dCount])
                    sum += mvJobSkill.get(dCount).get(jCount);
            }

        return sum; */

    }

    private double getSkillSum(final int dwarf)
            throws SolutionImpossibleException {

        double sum = 0;
        //Vector<Integer> dwarfSkill = mvJobSkill.get(dwarf);

        for (int jCount = 0; jCount < NUM_JOBS; jCount++)
            if (mbSolution[jCount][dwarf])
                sum += getWeightedScore(dwarf, jCount); //dwarfSkill.get(jCount);

        return sum;
    }

    private boolean isJobLegalForDwarf(final int job, final int dwarf) {
        for (int iCount = 0; iCount < NUM_JOBS; iCount++) {
            if (mbSolution[iCount][dwarf]) {
                if (mhtJobBlacklist.areItemsListedTogether(
                        mlstJobs.get(iCount).getName()
                        , mlstJobs.get(job).getName()))
                    return false;
/*                if (! areJobsAllowedTogether(mvJobs.get(iCount).name
                    , mvJobs.get(job).name))
                    return false; */
            }
        }
        return true;
    }

    private boolean areJobsAllowedTogether(final String job1
            , final String job2) {

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
        final ResultsView ignore = new ResultsView(
                new Solution(mlstJobs, mlstDwarves, mbSolution, mdblScores));
    }
}
