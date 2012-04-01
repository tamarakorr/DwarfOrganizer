/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import myutils.MyFileUtils;
import org.w3c.dom.NodeList;

/**
 *
 * @author Owner
 */
public class MyIO {
    
    private static final String GROUP_LIST_FILE_NAME = "config/group-list.txt";
    private static final String LABOR_LIST_FILE_NAME = "config/labor-list.txt";
    private static final String RULE_FILE_NAME = "config/rules.txt";
    
    private static final String RULES_NOTE = "This file lists jobs that aren't allowed"
            + " together (BLACKLIST), or aren't allowed with other jobs"
            + " (WHITELIST). Put each new pair on a different line, and separate"
            + " the pair by a a Tab. The names must exactly match the XML 'Labour' names,"
            + " which can be found in labor-list.txt. Comments can be added after the last"
            + " column.";        
    
    private JobBlacklist moBlacklist;
    private JobList moWhitelist;
    private Vector<String[]> mvRuleFile;
    
    public MyIO() {
        super();
    }
    
    protected Vector<LaborGroup> readLaborGroups() throws Exception {
        final int EXPECTED_COLUMNS = 5;
        Vector<LaborGroup> vReturn = new Vector<LaborGroup>();
        
        try {
            FileInputStream in = new FileInputStream(GROUP_LIST_FILE_NAME);
            Vector<String[]> vData = MyFileUtils.readDelimitedLineByLine(in, "\t", 1);
            for (String[] array : vData) {
                if (array.length != EXPECTED_COLUMNS)
                    System.err.println("A line in group-list.txt contains an inappropriate"
                            + " number of columns: " + array.length);
                else
                    vReturn.add(new LaborGroup(array[0], array[1]
                            , Integer.parseInt(array[2]), Integer.parseInt(array[3])
                            , Integer.parseInt(array[4])));
            }

            in.close();
            
        } catch (Exception ex) {
            System.err.println("Could not process group-list.txt. All results are invalid.");
            throw ex;
        }
        
        return vReturn;
    }

    protected Vector<Labor> readLabors() throws Exception {
        final int EXPECTED_COLUMNS = 3;
        Vector<Labor> vReturn = new Vector<Labor>();
        
        try {
            FileInputStream in = new FileInputStream(LABOR_LIST_FILE_NAME);
            Vector<String[]> vData = MyFileUtils.readDelimitedLineByLine(in, "\t", 1);
            
            for (String[] array : vData) {
                if (array.length != EXPECTED_COLUMNS)
                    System.err.println("A line in " + LABOR_LIST_FILE_NAME
                            + " contains an inappropriate"
                            + " number of columns: " + array.length);
                else
                    vReturn.add(new Labor(array[0], array[1], array[2]));
            }

            in.close();
        } catch (Exception ex) {
            System.err.println("Could not read labor-list.txt. All results will be invalid.");
            throw ex;
        }
        return vReturn;
    }
    
    // Reads the rule file
    // Results can be obtained with getWhitelist() and getBlacklist()
    // Raw file data is stored in mvRuleFile
    protected void readRuleFile() throws Exception {
        String line;
        String strComment = "";
        moBlacklist = new JobBlacklist();
        moWhitelist = new JobList();
        mvRuleFile = new Vector<String[]>();
        
        try {
            // Open the file
            FileInputStream in = new FileInputStream(RULE_FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            // Throw out the first line
            br.readLine();
            
            while ((line = br.readLine()) != null) {
                String[] jobs = line.split("\t");
                
                // Convert old "COMMENT" entries to end-of-line comments
                // COMMENT entries at the very end of the file are handled out of loop
                if (jobs[0].equals("COMMENT")) {
                    strComment += jobs[1];
                }
                else if (! strComment.equals("")) {
                    jobs = fixOldStyleComment(jobs, strComment);
                    strComment = "";
                }

                if (! jobs[0].equals("COMMENT")) {
                    //System.out.println(jobs.length);
                    mvRuleFile.add(jobs);
                }
            }
            
            // Comments from the end of the file - append to last non-COMMENT entry
            if (! strComment.equals("")) {
                int lastIndex = mvRuleFile.size() - 1;
                mvRuleFile.set(lastIndex
                        , fixOldStyleComment(mvRuleFile.get(lastIndex), strComment));
            }
            
            br.close();
            in.close();
            
            // Create the rule structures
            createRuleStructures(mvRuleFile);
            
            // Post-process the whitelist
            //addWhitelistToBlacklist(moWhitelist, moBlacklist, jobSettings);
            
        } catch (Exception e) {
            System.err.println("Error when processing rule file."
                    + " Job blacklist/whitelist will not be correct.");
            throw e;
        }
    }
    protected JobBlacklist getBlacklist() { return moBlacklist; }
    protected JobList getWhitelist() { return moWhitelist; }
    protected Vector<String[]> getRuleFileContents() { return mvRuleFile; }
    
    // Appends old COMMENT entries (comment) to the given non-comment line (jobs)
    private String[] fixOldStyleComment(String[] jobs, String comment) {
        // Append comment to existing comment
        if (jobs.length > 3)
            jobs[3] = jobs[3] + "; " + comment;
        // Create comment on next non-COMMENT line
        else {
            String[] clone = (String[]) jobs.clone();
            jobs = new String[4];
            for (int iCount = 0; iCount < 3; iCount++)
                jobs[iCount] = clone[iCount];
            jobs[3] = comment;
        }
        return jobs;
    }
    
    // Creates moBlacklist and moWhitelist from the given file data
    private void createRuleStructures(Vector<String[]> vData) {
        for (String[] fields : vData) {
            if (fields[0].equals("BLACKLIST")) {
                moBlacklist.addOneWayEntry(fields[1], fields[2]);
                moBlacklist.addOneWayEntry(fields[2], fields[1]);
                //System.out.println("RULE: Blacklist: One dwarf may not do " + fields[1]
                //    + " and " + fields[2] + " simultaneously.");                        
            }
            else  { // fields[0].equals("WHITELIST")
                moWhitelist.addOneWayEntry(fields[1], fields[2]);
                //System.out.println("RULE: Whitelist: " + fields[1] + " may do " 
                //    + fields[2]);
            }
        }
    }
    
    // Writes the given data to the rule file.
    // Recreates the whitelist and blacklist data structures.
    protected void writeRuleFile(Vector<String[]> vData) {

        moBlacklist = new JobBlacklist();
        moWhitelist = new JobList();
        
        // Open the file
        try {
            FileWriter fstream = new FileWriter(RULE_FILE_NAME);
            BufferedWriter out = new BufferedWriter(fstream);
            System.out.println("Writing " + RULE_FILE_NAME);
            
            // Write the first line
            out.write(RULES_NOTE);
            out.newLine();
            out.flush();
            
            // Write all subsequent lines
            for (String[] line : vData) {
                boolean bFirst = true;
                for (String field : line) {
                    if (field != null) {
                        if (! bFirst) out.write("\t");
                        out.write(field);
                        bFirst = false;
                    }
                }
                out.newLine();
                out.flush();
            }
            
            out.close();
            fstream.close();
            
            // Success: update mvRuleFile with the new data
            mvRuleFile = vData;
            createRuleStructures(mvRuleFile);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to write " + RULE_FILE_NAME);
        }

    }
    
    
    protected NodeList readDwarves(String filePath) throws Exception {
        try {
            myXMLReader xmlFileReader = new myXMLReader(filePath);
            return xmlFileReader.getDocument().getElementsByTagName("Creature");
        } catch (Exception e) {
            System.err.println("Failed to read dwarves.XML");
            throw e;
        }
    }
    
    protected String getLicense() {
        String strReturn = "";
        String line;
        try {
            FileInputStream in = new FileInputStream("./license.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            while ((line = br.readLine()) != null)
                strReturn += line + "\n";
            
            br.close();
            in.close();
                
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MyIO.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            strReturn += "Error: License not found.";
        } catch (IOException ex) {
            Logger.getLogger(MyIO.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            strReturn += "Error: Failed to read license.";
        }
        return strReturn;
    }
    
    // Loads job settings from file
    public void readJobSettings(File file, Vector<Job> vLaborSettings
            , String defaultReminder) {
        
        try {
            FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            DataInputStream in = new DataInputStream(fstream);            

            Hashtable<String, Job> htJobs = hashJobs(MyFileUtils.readDelimitedLineByLine(
                    in, "\t", 0), defaultReminder);

            // Update the current labor settings with the file data.
            for (Job job : vLaborSettings) {
                Job jobFromFile = htJobs.get(job.name);
                if (jobFromFile != null) {
                    job.qtyDesired = jobFromFile.qtyDesired;
                    job.candidateWeight = jobFromFile.candidateWeight;
                    job.currentSkillWeight = jobFromFile.currentSkillWeight;
                    job.time = jobFromFile.time;
                    job.reminder = jobFromFile.reminder;
                }

                else
                    System.err.println("WARNING: Job '" + job.name + "' was not found"
                            + " in the file. Its settings will be the defaults.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load job file.");
            e.printStackTrace();
        }
    }
    
    // Loads the job data into a hash table
    private Hashtable<String, Job> hashJobs(Vector<String[]> vJobs
            , String defaultReminder) {
        
        Hashtable<String, Job> htReturn = new Hashtable<String, Job>();
        
        String strReminder;
        
        for (String[] jobData : vJobs) {
            if (jobData.length == 1) {
                // Version data
            }
            else {
                if (jobData.length < 6)
                    strReminder = defaultReminder;
                else
                    strReminder = jobData[5];

                htReturn.put(jobData[0], new Job(jobData[0], "Unknown"
                    , Integer.parseInt(jobData[1])
                    , Integer.parseInt(jobData[2]), Double.parseDouble(jobData[3])
                    , Integer.parseInt(jobData[4]), strReminder));
            }
        }
        return htReturn;
    }
}
