/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.Stat.StatHint;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import myutils.MyFileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Owner
 */
public class DwarfOrganizerIO {
    
    public static final boolean DEFAULT_EXCLUSION_ACTIVE = true;
    
    private static final String GROUP_LIST_FILE_NAME = "config/group-list.txt";
    private static final String LABOR_LIST_FILE_NAME = "config/labor-list.txt";
    private static final String RULE_FILE_NAME = "config/rules.txt";
    private static final String EXCLUSION_FILE_NAME = "config/exclusion-list.xml";
    private static final String LABOR_SKILLS_XML_FILE_NAME = "config/labor-skills.xml";
    private static final String TRAITS_FILE_NAME = "config/trait-hints.txt";
    
    private static final String CURRENT_EXCLUSIONS_VERSION = "B";
    
    private static final String RULES_NOTE = "This file lists jobs that aren't allowed"
            + " together (BLACKLIST), or aren't allowed with other jobs"
            + " (WHITELIST). Put each new pair on a different line, and separate"
            + " the pair by a a Tab. The names must exactly match the XML 'Labour' names,"
            + " which can be found in labor-list.txt. Comments can be added after the last"
            + " column.";        
    
    private JobBlacklist moBlacklist;
    private JobList moWhitelist;
    private Vector<String[]> mvRuleFile;
    
    private Integer mintMaxExclID = 0;
    
    public DwarfOrganizerIO() {
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
    
    // DwarfIO has been imported from DwarfListWindow and is a little more messy
    // than newer functions in this file
    public static class DwarfIO {
        
        private static final String DEFAULT_DWARF_AGE = "999";
        private static final String DEFAULT_TRAIT_VALUE = "50";
        private static final int MAX_DWARF_TIME = 100;
        private Pattern SKILL_LEVEL_PATTERN; // Set by constructor
        
        private final long[] plusplusRange = { 700, 1200, 1400, 1500, 1600, 1800, 2500 };
        private final long[] plusRange = { 450, 950, 1150, 1250, 1350, 1550, 2250 };
        private final long[] avgRange = { 200, 750, 900, 1000, 1100, 1300, 2000 };
        private final long[] minusRange = { 150, 600, 800, 900, 1000, 1100, 1500 };
        private final long[] socialRange = { 0, 10, 25, 61, 76, 91, 100 };        
        
        private final String[] SOCIAL_TRAITS = { "Friendliness"
                , "Self_consciousness", "Straightforwardness", "Cooperation"
                , "Assertiveness" };
        
        private Hashtable<String, Stat> mhtStats;
        private Hashtable<String, Skill> mhtSkills;
        private Hashtable<String, MetaSkill> mhtMetaSkills;
        
        private Vector<Dwarf> mvDwarves;
        
        public DwarfIO() {            
            // "Constants"
            SKILL_LEVEL_PATTERN = Pattern.compile("(.*\\[)(\\d+)(\\].*)");
        }
        
        public Vector<Dwarf> getDwarves() {
            return mvDwarves;
        }

        public Hashtable<String, MetaSkill> getMetaSkills() {
            return mhtMetaSkills;
        }

        public Hashtable<String, Skill> getSkills() {
            return mhtSkills;
        }

        public Hashtable<String, Stat> getStats() {
            return mhtStats;
        }
        
        public void readDwarves(String filePath) { //throws Exception { // NodeList
            
            mvDwarves = new Vector<Dwarf>(); // Initialize before reading anything
            
            try {
                
                // Skills
                createSkills();
                
                // Trait hints
                readTraitHints();
                
                // Dwarves.xml
                myXMLReader xmlFileReader = new myXMLReader(filePath);
                NodeList nodes = xmlFileReader.getDocument().getElementsByTagName(
                        "Creature");
                System.out.println("Dwarves.xml contains " + nodes.getLength()
                        + " creatures.");        
                parseDwarves(nodes);
            } catch (Exception e) {
                System.err.println(e.getMessage() + " Failed to read dwarves.XML");
            }
        }
        
        // Reads labor-skills.XML
        private void createSkills() {
            
            mhtSkills = new Hashtable<String, Skill>();
            mhtMetaSkills = new Hashtable<String, MetaSkill>();
            
            createStats();

            // Read skills from XML.
            try {
                mhtSkills = getLaborSkills();
            } catch (URISyntaxException e) {
                System.err.println("URI syntax exception: could not read labor-skills.XML");
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                System.err.println("Labor skills file not found");
                e.printStackTrace();
            }

            // TODO: Remove this hard-coding. Put meta skills in XML file.
            // Meta skills: Broker and manager
            mhtMetaSkills.put("Broker", new MetaSkill("Broker"
                , new Vector<Skill>(Arrays.asList(new Skill[] { mhtSkills.get("Appraisal")
                , mhtSkills.get("Judging Intent"), mhtSkills.get("Conversation")
                , mhtSkills.get("Comedy"), mhtSkills.get("Flattery")
                , mhtSkills.get("Lying")
                , mhtSkills.get("Intimidation"), mhtSkills.get("Persuasion")
                , mhtSkills.get("Negotiation"), mhtSkills.get("Consoling")
                , mhtSkills.get("Pacification") }))));
            mhtMetaSkills.put("Manager", new MetaSkill("Manager"
                    , new Vector<Skill>(Arrays.asList(new Skill[] {
                    mhtSkills.get("Organization")
                    , mhtSkills.get("Consoling"), mhtSkills.get("Pacification") }))));
        }

        // Creates mhtStats.
        // TODO Remove hard-coding
        private void createStats() {
            mhtStats = new Hashtable<String, Stat>();
            
            String[] plusplusStats = { "Focus", "Spatial Sense" };
            String[] plusStats = { "Strength", "Toughness", "Analytical Ability"
                , "Creativity", "Patience", "Memory" };
            String[] avgStats = { "Endurance", "Disease Resistance", "Recuperation"
                    , "Intuition", "Willpower", "Kinesthetic Sense", "Linguistic Ability"
                    , "Musicality", "Empathy", "Social Awareness", "Altruism" };
            String[] minusStats = { "Agility" };

            createStats(plusplusStats, plusplusRange);
            createStats(plusStats, plusRange);
            createStats(avgStats, avgRange);
            createStats(minusStats, minusRange);
            createStats(SOCIAL_TRAITS, socialRange);

            // Set XML names for stats that don't need to be hinted
            mhtStats.get("Focus").setXmlName("Focus");
            mhtStats.get("Spatial Sense").setXmlName("SpatialSense");
            mhtStats.get("Strength").setXmlName("Strength");
            mhtStats.get("Toughness").setXmlName("Toughness");
            mhtStats.get("Analytical Ability").setXmlName("AnalyticalAbility");
            mhtStats.get("Creativity").setXmlName("Creatvity");   // Yes, it's missing an "i".
            mhtStats.get("Patience").setXmlName("Patience");
            mhtStats.get("Memory").setXmlName("Memory");
            mhtStats.get("Endurance").setXmlName("Endurance");
            mhtStats.get("Disease Resistance").setXmlName("DiseaseResistance");
            mhtStats.get("Recuperation").setXmlName("Recuperation");
            mhtStats.get("Intuition").setXmlName("Intuition");
            mhtStats.get("Willpower").setXmlName("Willpower");
            mhtStats.get("Kinesthetic Sense").setXmlName("KinaestheticSense");
            mhtStats.get("Linguistic Ability").setXmlName("LinguisticAbility");
            mhtStats.get("Musicality").setXmlName("Musicality");
            mhtStats.get("Empathy").setXmlName("Empathy");
            mhtStats.get("Social Awareness").setXmlName("SocialAwareness");
            mhtStats.get("Agility").setXmlName("Agility");

            // TODO ... Figure out why I wrote TODO here and what this is/was for
            //mhtStats.get("Friendliness").xmlName = 
            //mhtStats.get("Self-consciousness").xmlName = 
            //mhtStats.get("Straightforwardness").xmlName = 
            //mhtStats.get("Cooperation").xmlName = 
            //mhtStats.get("Assertiveness").xmlName = 
        }

        private void createStats(String[] statName, long[] statRange) {
            for (int iCount = 0; iCount < statName.length; iCount++)
                mhtStats.put(statName[iCount], new Stat(statName[iCount], statRange));
        }
        
        // Reads the trait hints file (needed for Runesmith style XML traits)
        private void readTraitHints() {
            String strLine;

            try {
                // Open the file
                //InputStream in =
                //        this.getClass().getResourceAsStream(TRAITS_FILE_NAME);
                FileInputStream in = new FileInputStream(TRAITS_FILE_NAME);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                while ((strLine = br.readLine()) != null) {
                    String[] data = strLine.split("\t");

                    mhtStats.get(data[0]).addStatHint(data[3]
                            , Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                }
                in.close();
            } catch (Exception e) {
                System.err.println("Error when reading file trait-hints.txt."
                        + " Dwarf traits may not have correct values.");
                e.printStackTrace();
            }
        }
        
        // Reads the labor skills XML data file
        // Commented code has been left intact so that I (hopefully) don't ever have
        // to research the problems I encountered here again
        private Hashtable<String, Skill> getLaborSkills() throws URISyntaxException
                , FileNotFoundException {

            Hashtable<String, Vector<Stat>> htStatGroup = new Hashtable<String
                    , Vector<Stat>>();
            Hashtable<String, Skill> htReturn = new Hashtable<String, Skill>();
            Vector<Stat> vStat;
            Element thisStat;
            NodeList stat;

            //try {
                // The following two lines work perfectly when running from the development
                // environment in Netbeans, but they do not work in the distributed application.
                //URI oURI = new URI(
                //    this.getClass().getResource(LABOR_SKILLS_XML_FILE_NAME).getFile());  //getFile()
                //URI oURI = new URI(this.getClass().getClassLoader().getResource(
                //        LABOR_SKILLS_XML_FILE_NAME).getFile());
                //myXMLReader xmlFileReader = new myXMLReader(oURI.getPath());
                //System.out.println(getClass().getResource(LABOR_SKILLS_XML_FILE_NAME).toURI().toString());

                // UPDATE: The insanity trying to get this file to process is caused by
                // a bug-slash-feature in Java:
                // url.getFile() is full of '%20's standing in for spaces in path names.
                // See bug details and workaround at
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4466485
                // UPDATE: This was such a headache to deal with that myXMLReader was
                // modified to accept streams instead. Duhhhh
                //System.out.println(getClass().getResource(LABOR_SKILLS_XML_FILE_NAME));
                //URI uri = new URI(getClass().getResource(LABOR_SKILLS_XML_FILE_NAME).toString());
                //System.out.println(uri.getPath());
                //myXMLReader xmlFileReader = new myXMLReader(uri.getPath());

                //And what the heck was I doing using getResourceAsStream anyway...
                // I don't want this file zipped up in the JAR! FileInputStream for the win!
                //myXMLReader xmlFileReader = new myXMLReader(getClass()
                //        .getResourceAsStream(LABOR_SKILLS_XML_FILE_NAME));
                myXMLReader xmlFileReader = new myXMLReader(new FileInputStream(
                        LABOR_SKILLS_XML_FILE_NAME));

                // Read the stat group macros
                NodeList nlStatGroup = xmlFileReader.getDocument().getElementsByTagName(
                        "StatGroup");
                //Element ele = (Element) nlStatGroup.item(0);
                //System.out.println(nlStatGroup.getLength() + " stat groups");
                for (int iCount = 0; iCount < nlStatGroup.getLength(); iCount++) {

                    Element thisStatGroup = (Element) nlStatGroup.item(iCount);
                    String strStatGroupName = getTagValue(thisStatGroup, "Name"
                            , "Error - Null name in XML"); // thisStatGroup.getAttribute("Name");
                    //System.out.println("Stat group name: " + strStatGroupName);
                    stat = thisStatGroup.getElementsByTagName("Stat");
                    vStat = new Vector<Stat>(stat.getLength());
                    for (int jCount = 0; jCount < stat.getLength(); jCount++) {
                        thisStat = (Element) stat.item(jCount);
                        String strStatName = thisStat.getAttribute("Name");
                        vStat.add(mhtStats.get(strStatName));
                    }
                    htStatGroup.put(strStatGroupName, vStat);
                }
                //printStatGroups(htStatGroup);

                // Read the skills (and secondary skills, and social skills) for labors
                Class[] classes = {Skill.class, SecondarySkill.class
                        , SocialSkill.class};
                for (Class classItem : classes) {
                    NodeList nlLaborSkill = xmlFileReader.getDocument().getElementsByTagName(
                                classItem.getSimpleName());   // "Skill"
                    for (int kCount = 0; kCount < nlLaborSkill.getLength(); kCount++) {
                        
                        Element thisLaborSkill = (Element) nlLaborSkill.item(kCount);
                        String strName = getTagValue(thisLaborSkill, "Name"
                                , "Error - Null labor skill name in XML");
                        //System.out.println(strName + " :");

                        // Stats and StatGroupRefs can be listed
                        // Add stats to stat list
                        stat = thisLaborSkill.getElementsByTagName("Stat");
                        vStat = new Vector<Stat>(stat.getLength());
                        //System.out.println(stat.getLength() + " stats");
                        for (int mCount = 0; mCount < stat.getLength(); mCount++) {
                            String strStatName = stat.item(mCount).getTextContent();
                            //System.out.println("    " + strStatName);   // thisStat.getNodeValue()
                            vStat.add(mhtStats.get(strStatName));   // thisStat.getNodeValue()
                        }

                        // Add StatGroupRefs to stat list
                        NodeList statGroup = thisLaborSkill.getElementsByTagName("StatGroupRef");
                        for (int nCount = 0; nCount < statGroup.getLength(); nCount++) {
                            Element thisStatGroup = (Element) statGroup.item(nCount);
                            //System.out.println(thisStatGroup.getTextContent());
                            //printStatGroup(htStatGroup.get(thisStatGroup.getTextContent()));
                            vStat.addAll(htStatGroup.get(thisStatGroup.getTextContent()));
                        }

                        // Prevented by trait, min, and max for social skills-------
                        String strTrait = "Error - No trait in XML";
                        int intMin = 0;
                        int intMax = 100;

                        if (classItem == SocialSkill.class) {
                            strTrait = thisLaborSkill
                                    .getElementsByTagName("Trait").item(0).getTextContent();
                            intMin = Integer.parseInt( 
                                    thisLaborSkill.getElementsByTagName("Min").item(0)
                                    .getTextContent());
                            intMax = Integer.parseInt(
                                    thisLaborSkill.getElementsByTagName("Max").item(0)
                                    .getTextContent());
                        }
                        // ------------End special social skills processing---------

                        // Add the skill to the hash table
                        if (classItem == Skill.class)
                            htReturn.put(strName, new Skill(strName, vStat));
                        else if (classItem == SecondarySkill.class)
                            htReturn.put(strName, new SecondarySkill(strName, vStat));
                        else if (classItem == SocialSkill.class)
                            htReturn.put(strName, new SocialSkill(strName, vStat
                                    , strTrait, intMin, intMax));
                        else
                            System.err.println("classItem is not of a recognized type."
                                    + " Ignoring skill " + strName);
                    }
                }            

            //} catch (URISyntaxException e) { e.printStackTrace();
            //}

            return htReturn;
        }    
        
        // Translates XML data to dwarf objects
        private void parseDwarves(NodeList nodes) {
            //mvDwarves = new Vector<Dwarf>();
            
            for (int iCount = 0; iCount < nodes.getLength(); iCount++) {

                Element thisCreature = (Element) nodes.item(iCount);

                int age = Integer.parseInt(getTagValue(thisCreature, "Age"
                        , DEFAULT_DWARF_AGE));

                // Stopped skipping juveniles for DF 34.05 due to age bugs
                //if (! isJuvenile(age)) {

                // Read stat values and get percentiles.
                Hashtable<String, Long> statValues = new Hashtable<String, Long>();
                Hashtable<String, Long> htPercents = new Hashtable<String, Long>();
                for (String key : mhtStats.keySet()) {
                    //System.out.println("Getting " + mhtStats.get(key).name);

                    long value;
                    if (mhtStats.get(key).getXmlName() != null)
                        value = Long.parseLong(getTagValue(thisCreature
                            , mhtStats.get(key).getXmlName(), "0"));

                    else {  // Look under Traits if there is no attribute XML name

                        // TODO: get dwarven personality average to use as default                        

                        // (DFHack style XML) If the trait has a named entry,
                        // then get the value
                        Element traits = (Element) thisCreature.getElementsByTagName(
                                "Traits").item(0);
                        value = Long.parseLong(getXMLValueByKey(traits, "Trait"
                                , "name", mhtStats.get(key).getName(), "value"
                                , "-1"));   // DEFAULT_TRAIT_VALUE

                        // If we could not get the exact trait value, perhaps
                        // this is a Runesmith XML file. Check for trait hints.
                        if (value == -1) {
                            value = Long.parseLong(DEFAULT_TRAIT_VALUE);
                            for (StatHint hint : mhtStats.get(key).vStatHints) {
                                //if (traits contains hint)
                                if (getTagList(thisCreature, "Traits").contains(hint.hintText)) {
                                    value = (hint.hintMin + hint.hintMax) / 2;
                                    //System.out.println(htStats.get(key).name + " " + value);
                                    break;
                                }
                            }
                        }
                    }
                    //System.out.println("Value: " + value);
                    statValues.put(key, value);
                    htPercents.put(key, Math.round(
                            getPlusPlusPercent(mhtStats.get(key).range, value)));
                    //System.out.println(key + htPercents.get(key));
                }

                // Create a dwarf object
                Dwarf oDwarf = new Dwarf();

                oDwarf.setName(getTagValue(thisCreature, "Name", "Error - Null Name"));
                oDwarf.setAge(age);
                oDwarf.setGender(getTagValue(thisCreature, "Sex", "Error - Null Sex"));
                oDwarf.setNickname(getTagValue(thisCreature, "Nickname", ""));
                oDwarf.statPercents = htPercents;
                oDwarf.statValues = statValues;
                oDwarf.setTime(MAX_DWARF_TIME);
                oDwarf.setJobText(getTagList(thisCreature, "Labours"));
                String jobs[] = oDwarf.getJobText().split("\n");
                //if (jobs.length <= 1)
                    //System.out.println("No labors enabled.");
                //else {    
                if (jobs.length > 1) {  // First and last entries in the labor list from XML are blank                
                    for (int jCount = 1; jCount < jobs.length - 1; jCount++) {
                        //System.out.println(oDwarf.name + ": labor " + jCount
                        //        + " enabled: " + jobs[jCount].trim());
                        oDwarf.labors.add(jobs[jCount].trim());
                    }
                }

                // Read current skill levels
                Element skills = (Element) thisCreature.getElementsByTagName("Skills").item(0);

                try {
                    NodeList children = skills.getElementsByTagName("Skill");

                    for (int sCount = 0; sCount < children.getLength(); sCount++) {
                        Element eleSkill = (Element) children.item(sCount);
                        String strSkillName = getTagValue(eleSkill, "Name"
                                , "Error - Null skill name");
                        String strSkillLevel = getTagValue(eleSkill, "Level"
                                , "Error - Null skill level");
                        //System.out.println(oDwarf.name + " " + strSkillName + " "
                        //        + strSkillLevel);

                        // If it is a DFHack dwarves.XML, the skill level will
                        // be just digits.
                        long skillValue = -1;
                        try {
                            skillValue = Long.parseLong(strSkillLevel);
                        } catch (NumberFormatException e) {
                            // Probably a Runesmith XML - convert the skill level
                            // to a numeric value if so
                            skillValue = skillDescToLevel(strSkillLevel);
                        }
                        oDwarf.skillLevels.put(strSkillName, skillValue);

                        //oDwarf.skillLevels.put(strSkillName
                        //        , skillDescToLevel(strSkillLevel));
                    }

                } catch (java.lang.NullPointerException e) {
                    System.err.println("Skills are not present in the given dwarves.xml file. "
                            + oDwarf.getName() + " will not have skill levels.");
                }

                // Simple skill potentials
                for (String key : mhtSkills.keySet()) {
                    Skill oSkill = mhtSkills.get(key);
                    oDwarf.skillPotentials.put(oSkill.getName()
                            , getPotential(oDwarf, oSkill));
                }
                // Meta skill potentials
                for (String key : mhtMetaSkills.keySet()) {
                    MetaSkill meta = mhtMetaSkills.get(key);
                    double dblSum = 0.0d;

                    for (Skill oSkill : meta.vSkills)
                        dblSum += oDwarf.skillPotentials.get(oSkill.getName());

                    oDwarf.skillPotentials.put(meta.name
                            , Math.round(dblSum / meta.vSkills.size()));
                }
                mvDwarves.add(oDwarf);

            }
        }
        // Calculates the dwarf's "potential" for the given skill
        private long getPotential(Dwarf oDwarf, Skill oSkill) {

            double dblSum = 0.0d;
            Vector<Stat> vStats = oSkill.getStats();
            double numStats = (double) vStats.size();

            for (int kCount = 0; kCount < numStats; kCount++) {
                double addValue = oDwarf.statPercents.get(vStats.get(kCount).getName());

                // If the dwarf cannot gain skill because of a personality trait
                if (oSkill.getClass() == SocialSkill.class) {
                    SocialSkill sSkill = (SocialSkill) oSkill;
                    long noValue = oDwarf.statValues.get(sSkill.noStatName);
                    if (noValue >= sSkill.noStatMin && noValue <= sSkill.noStatMax)
                        addValue = 0;
                }

                dblSum += addValue;
            }

            return Math.round(dblSum / numStats);
        }

        // Converts a Runesmith skill level description to long integer value
        private long skillDescToLevel(String skillLevelDesc) {

            Matcher matcher = SKILL_LEVEL_PATTERN.matcher(skillLevelDesc);
            if (matcher.find())
                return Long.parseLong(matcher.group(2));
            else
                System.err.println("Pattern not matched.");

            return 0;
        }    
        private double getPlusPlusPercent(long[] range, long attribute) {
            double chanceToBeInBracket = 1.0d / (range.length - 1);
            int bracket = getPlusPlusBracket(range, attribute);

            if (bracket > 0) {
                int numBracketsBelow = bracket - 1;
                double bracketSize = range[bracket] - range[bracket - 1];
                double inBracketPercent = (attribute - range[bracket - 1]) / bracketSize;

                return 100.0d * chanceToBeInBracket
                        * (inBracketPercent + numBracketsBelow);            
            }
            else    // Not in a bracket: better than 100% of dwarves
                return 100.0d;

        }
        private int getPlusPlusBracket(long[] range, long attribute) {
            long minValue = range[0];
            for (int iCount = 1; iCount < range.length; iCount++)
                if (attribute <= range[iCount])
                    return iCount;
            return 0;
        }

        // Gets the value of an XML tag in a keyed list. The Traits section of
        // dwarves.xml from DFHack is an example of the expected format.
        // parent   : The parent element (for example, the Traits element)
        // tagName  : The name of the list-formatted element (example: "Trait")
        // keyName  : The name of the element containing the key (example: "Name")
        // keyValue : The key to search for (example: "ASSERTIVENESS")
        // valueName: The name of the element for the value to retrieve (example: "value")
        // nullValue: The value to return if an error is encountered, or if the expected
        //            item is not found. (Example: "50")
        private String getXMLValueByKey(Element parent, String tagName, String keyName
                , String keyValue, String valueName, String nullValue) {

            try {
                NodeList children = parent.getElementsByTagName(tagName);

                //System.out.println("Number of children: " + children.getLength());
                for (int iCount = 0; iCount < children.getLength(); iCount++) {
                    Element eleItem = (Element) children.item(iCount);
                    //System.out.println("Key name: " + eleItem.getAttribute(keyName));
                    if (eleItem.getAttribute(keyName).toUpperCase().equals(keyValue.toUpperCase()))
                        return eleItem.getAttribute(valueName);
                }

            } catch (java.lang.NullPointerException e) {
                System.err.println("Error encountered when retrieving XML value "
                        + keyValue + " by key.");
                return nullValue;
            }

            System.err.println(tagName + ":" + keyName + ":" + valueName
                    + " was not found in xml data");
            return nullValue;
        }
        private String getTagList(Element creature, String tagName) {
            NodeList parent = creature.getElementsByTagName(tagName);

            if (null == parent.item(0))
                return "Error - Null tag list";
            else
                return parent.item(0).getTextContent();
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
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            strReturn += "Error: License not found.";
        } catch (IOException ex) {
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
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
    
    // Writes exclusions to XML
    public boolean writeExclusions(List<Exclusion> lstExclusion) {
        
        // Create XML document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            
            // Root element
            Element eleRoot = doc.createElement("Exclusions");
            doc.appendChild(eleRoot);
            
            // File version
            Element eleVer = doc.createElement("FileVersion");
            eleVer.setAttribute("Version", CURRENT_EXCLUSIONS_VERSION);
            eleRoot.appendChild(eleVer);
            
            // Start Exclusion rules
            Element eleRules = doc.createElement("ExclusionRules");
            eleRoot.appendChild(eleRules);
            
            // Exclusion rules body
            for (Exclusion exclusion : lstExclusion) {
                if (exclusion.getClass().equals(ExclusionRule.class)) {
                    ExclusionRule rule = (ExclusionRule) exclusion;
                    Element eleRule = doc.createElement("ExclusionRule");
                    eleRules.appendChild(eleRule);
                    eleRule.setAttribute("ID", rule.getID().toString());
                    eleRule.setAttribute("Name", rule.getName());
                    eleRule.setAttribute("Field", rule.getPropertyName());
                    eleRule.setAttribute("Comparator", rule.getComparator());
                    eleRule.setAttribute("Value", rule.getValue().toString());
                }
            }
            
            // Exclusion Lists
            Element eleLists = doc.createElement("ExclusionLists");
            eleRoot.appendChild(eleLists);
            
            // Exclusion lists body
            for (Exclusion exclusion : lstExclusion) {
                if (exclusion.getClass().equals(ExclusionList.class)) {
                    ExclusionList list = (ExclusionList) exclusion;
                    Element eleList = doc.createElement("ExclusionList");
                    eleLists.appendChild(eleList);
                    eleList.setAttribute("ID", list.getID().toString());
                    eleList.setAttribute("Name", list.getName());
                    
                    Element eleCitizens = doc.createElement("Citizens");
                    eleList.appendChild(eleCitizens);
                    for (String citizen : list.getCitizenList()) {
                        Element eleCitizen = doc.createElement("Citizen");
                        eleCitizens.appendChild(eleCitizen);
                        eleCitizen.setAttribute("Name", citizen);
                    }
                }
            }
            
            //-----------------------
            // Write the document to XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            //transformerFactory.setAttribute("indent-number", new Integer(4));
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // Hooray for completely undocumented solutions:
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(EXCLUSION_FILE_NAME));
            //StreamResult result = new StreamResult(System.out);  Uncomment for testing

            transformer.transform(source, result);
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (TransformerException ex) {
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;        // Success
    }
    
    private abstract class SectionReader {
        String name;
        Integer ID;
        public void setName(String name) {
            this.name = name;
        }
        public void setID(Integer ID) {
            this.ID = ID;
        }
        public abstract void doVersionAPlusFunction(Element ele);
        public abstract void doVersionBPlusFunction(Element ele);
        public abstract Exclusion createExclusion();
    }
    
    private Vector<Exclusion> readSection(SectionReader srf
            , Document doc, String tagName, String version) {
        Node node;
        Element ele;
        Vector<Exclusion> vReturn = new Vector<Exclusion>();
        
        Integer id = -1;
        String name = "Exclusion rule name";        
        
        NodeList lstExclusionRules = doc.getElementsByTagName(tagName);
        for (int iCount = 0; iCount < lstExclusionRules.getLength(); iCount++) {
            node = lstExclusionRules.item(iCount);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ele = (Element) node;

                // TODO: Proper conversion between versions
                try {

                    if (version.compareTo("A") <= 0) {
                        id = iCount;
                        srf.setID(id);
                    }

                    if (version.compareTo("A") >= 0) {
                        name = ele.getAttribute("Name");
                        srf.setName(name);
                        srf.doVersionAPlusFunction(ele);
                    }

                    if (version.compareTo("B") >= 0) {
                        id = Integer.parseInt(ele.getAttribute("ID"));
                        srf.setID(id);
                        srf.doVersionBPlusFunction(ele);
                    }
                    //System.out.println("Reading exclusion #" + id);
                    mintMaxExclID = Math.max(mintMaxExclID, id);
                    vReturn.add(srf.createExclusion());
                    
                } catch (Exception e) {
                    System.err.println(e.getMessage() 
                            + " Failed to read a(n) " + tagName);
                }
            }
        }
        return vReturn;
    }
    
    public DeepCloneableVector<Exclusion> readExclusions(final Vector<Dwarf> citizens) {
            //, final Hashtable<Integer, Boolean> htActive) {
        Node node;
        Element ele;
        String version;
                
        DeepCloneableVector<Exclusion> vReturn = new DeepCloneableVector<Exclusion>();

        File file = new File(EXCLUSION_FILE_NAME);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList lstFileVersion = doc.getElementsByTagName("FileVersion");
            node = lstFileVersion.item(0);
            ele = (Element) node;
            version = ele.getAttribute("Version");
            
            // Exclusion Rules-----------------
            SectionReader exclusionRuleReader = new SectionReader() {
                private String field;
                private String comparator;
                private Object value;
                
                @Override
                public void doVersionAPlusFunction(Element ele) {
                    field = ele.getAttribute("Field");
                    comparator = ele.getAttribute("Comparator");
                    value = ele.getAttribute("Value");
                }

                @Override
                public Exclusion createExclusion() {
                    return new ExclusionRule(this.ID, this.name
                            , DEFAULT_EXCLUSION_ACTIVE
                            , this.field
                            , this.comparator, this.value); // isExclusionActive(this.ID, htActive)
                }

                @Override
                public void doVersionBPlusFunction(Element ele) {
                    // Do nothing extra
                }
            };
            vReturn.addAll(readSection(exclusionRuleReader, doc, "ExclusionRule"
                    , version));
            
            // Exclusion Lists---------------
            SectionReader exclusionListReader = new SectionReader() {
                //private DeepCloneableVector<Dwarf> vCitizen;
                Vector<String> vCitizenName;
                
                @Override
                public void doVersionAPlusFunction(Element ele) {
                    // Do nothing extra
                }

                @Override
                public void doVersionBPlusFunction(Element ele) {
                    vCitizenName = new Vector<String>();
                    NodeList nlist = ele.getElementsByTagName("Citizen");
                    for (int iCount = 0; iCount < nlist.getLength(); iCount++) {
                        Node node = nlist.item(iCount);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            Element eleCitizen = (Element) node;
                            vCitizenName.add(eleCitizen.getAttribute("Name"));
                        }
                    }
                    //System.out.println("Citizens in list: " + vCitizenName.size() + ", citizen total list size = " + citizens.size());
                    //vCitizen = getCitizensFromNames(vCitizenName, citizens);
                    //System.out.println("    Found " + vCitizen.size() + " matching citizen objects");
                }
                
                @Override
                public Exclusion createExclusion() {
                    return new ExclusionList(this.ID, this.name
                            , DEFAULT_EXCLUSION_ACTIVE, vCitizenName); // isExclusionActive(this.ID, htActive)
                }

            };
            vReturn.addAll(readSection(exclusionListReader, doc, "ExclusionList"
                    , version));            
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DwarfOrganizerIO.class.getName()).log(Level.SEVERE, null, ex);
        }

        return vReturn;
    }
    private static String getTagValue(String tag, Element element) {
        NodeList nList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node) nList.item(0);
        return nValue.getNodeValue();
    }
    // Returns the value for the XML tag for this element,
    // or valueIfNull if the tag does not exist (from DwarfListWindow)
    private static String getTagValue(Element element, String tagName
            , String valueIfNull) {
        Element ele = (Element) element.getElementsByTagName(tagName).item(0);
        if (null == ele)
            return valueIfNull;
        else if (null == ele.getChildNodes().item(0))
            return valueIfNull;
        else
            return ((Node) ele.getChildNodes().item(0)).getNodeValue().trim();
    }
    
    // Returns the next exclusion ID to use, and increments the current maximum.
    protected Integer incrementExclusionID() {
        mintMaxExclID++;
        return mintMaxExclID;
    }
    protected Integer getMaxUsedExclusionID() {
        return mintMaxExclID;
    }
    private boolean isExclusionActive(int ID
            , Hashtable<Integer, Boolean> htActive) {
        if (htActive == null)
            return false;
        else if (htActive.containsKey(ID))
            return htActive.get(ID);
        else
            return false;
    }
    private DeepCloneableVector<Dwarf> getCitizensFromNames(Vector<String> names
            , Vector<Dwarf> citizens) {
        
        DeepCloneableVector<Dwarf> vReturn = new DeepCloneableVector<Dwarf>();
        
        for (String name : names) {
            //System.out.println("Looking for " + name);
            
            //Get the dwarf object for the name
            for (Dwarf citizen : citizens) {
                //Dwarf citizen = (Dwarf) oCitizen;
                if (citizen.getName().equals(name)) {
                    vReturn.add(citizen);
                    break;
                }
            }
        }
        return vReturn;
    }
}
