/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.Stat.StatHint;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
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
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class DwarfOrganizerIO {

    private static final Logger logger = Logger.getLogger(
            DwarfOrganizerIO.class.getName());

    public static final boolean DEFAULT_EXCLUSION_ACTIVE = true;
    private static final String USER_FILES_DIR = "config/";
    private static final String DEFAULT_FILES_DIR = "config/default/";

    public static final String DEFAULT_JOB_DIR = "samples/jobs/";

    private static final String GROUP_LIST_FILE_NAME = "group-list.txt";
    private static final String LABOR_LIST_FILE_NAME = "labor-list.txt";
    private static final String RULE_FILE_NAME = "rules.txt";
    private static final String EXCLUSION_FILE_NAME = "exclusion-list.xml";
    private static final String LABOR_SKILLS_XML_FILE_NAME = "labor-skills.xml";
    private static final String TRAITS_FILE_NAME = "trait-hints.txt";
    private static final String VIEW_FILE_NAME = "views.xml";

    private static final String CURRENT_EXCLUSIONS_VERSION = "B";
    private static final String CURRENT_JOB_SETTINGS_VERSION = "A";

    private static final String RULES_NOTE = "This file lists jobs that aren't allowed"
            + " together (BLACKLIST), or aren't allowed with other jobs"
            + " (WHITELIST). Put each new pair on a different line, and separate"
            + " the pair by a a Tab. The names must exactly match the XML 'Labour' names,"
            + " which can be found in labor-list.txt. Comments can be added after the last"
            + " column.";

    private static final int LABOR_RULE_INDEX_TYPE = 0;
    private static final int LABOR_RULE_INDEX_FIRST_LABOR = 1;
    private static final int LABOR_RULE_INDEX_SECOND_LABOR = 2;
    private static final int LABOR_RULE_INDEX_COMMENT = 3;
    private static final int LABOR_RULE_MIN_COLS = 3;
    private static final int LABOR_RULE_MAX_COLS = 4;

    private JobBlacklist moBlacklist;
    private JobList moWhitelist;
    private List<LaborRule> mvRuleFile;

    private Integer mintMaxExclID = 0;

    public DwarfOrganizerIO() {
        super();
    }

    // Prioritizes user files over defaults
    private static String getInputFile(final String fileName) {

        final String userFile = USER_FILES_DIR + fileName;
        if (new File(userFile).exists())
            return userFile;

        final String defaultFile = DEFAULT_FILES_DIR + fileName;
        if (new File(defaultFile).exists())
            return defaultFile;
        else {
            logger.log(Level.SEVERE
                    , "User file and default file for {0} do not exist"
                    , fileName);
            return fileName;
        }
    }
    // Write only to user files (never to default files)
    private static String getOutputFile(final String fileName) {
        return USER_FILES_DIR + fileName;
    }

    protected ArrayList<LaborGroup> readLaborGroups() {
        final int EXPECTED_COLUMNS = 5;
        final ArrayList<LaborGroup> vReturn = new ArrayList<LaborGroup>();

        try {
            final FileInputStream in = new FileInputStream(
                    getInputFile(GROUP_LIST_FILE_NAME));
            final List<String[]> vData = MyFileUtils.readDelimitedLineByLine(in
                    , "\t", 1);
            for (final String[] array : vData) {
                if (array.length != EXPECTED_COLUMNS)
                    logger.log(Level.SEVERE
                            , "A line in group-list.txt contains an"
                            + " inappropriate number of columns: {0}"
                            , array.length);
                else
                    vReturn.add(new LaborGroup(array[0], array[1]
                            , Integer.parseInt(array[2])
                            , Integer.parseInt(array[3])
                            , Integer.parseInt(array[4])));
            }

            in.close();
        } catch (final Exception ex) {
            final String message = "Failed to process group-list.txt."
                    + " The application will shut down.";
            DwarfOrganizer.crash(message, ex);
        }
        return vReturn;
    }

    protected ArrayList<Labor> readLabors() {
        final int EXPECTED_COLUMNS = 3;
        final ArrayList<Labor> vReturn = new ArrayList<Labor>();

        try {
            final FileInputStream in = new FileInputStream(getInputFile(
                    LABOR_LIST_FILE_NAME));
            final List<String[]> vData = MyFileUtils.readDelimitedLineByLine(in
                    , "\t", 1);

            for (final String[] array : vData) {
                if (array.length != EXPECTED_COLUMNS)
                    logger.log(Level.SEVERE,"A line in " + LABOR_LIST_FILE_NAME
                            + " contains an inappropriate number of columns:"
                            + " {0}", array.length);
                else
                    vReturn.add(new Labor(array[0], array[1], array[2]));
            }

            in.close();
        } catch (final Exception e) {
            final String message = "Failed to read the critical file"
                + " labor-list.txt."
                + " The application will terminate.";
            DwarfOrganizer.crash(message, e);
        }
        return vReturn;
    }
    // Reads the rule file
    // Results can be obtained with getWhitelist() and getBlacklist()
    // Raw file data is stored in mvRuleFile
    protected void readRuleFile() {
        final String ERROR_MESSAGE = "Failed to process rules.txt."
            + " Job whitelist/blacklist will be incorrect.";

        String line;
        String strCommentFix = "";
        String[] jobs = new String[LABOR_RULE_MAX_COLS]; // 4
        moBlacklist = new JobBlacklist();
        moWhitelist = new JobList();
        mvRuleFile = new ArrayList<LaborRule>();

        try {
            // Open the file
            final FileInputStream in = new FileInputStream(getInputFile(
                    RULE_FILE_NAME));
            final BufferedReader br = new BufferedReader(new InputStreamReader(
                    in));

            // Throw out the first line
            br.readLine();

            while ((line = br.readLine()) != null) {
                jobs = line.split("\t");

                // Convert old "COMMENT" entries to end-of-line comments
                // COMMENT entries at the very end of the file are handled out of loop
                if (jobs[LABOR_RULE_INDEX_TYPE].equals("COMMENT")) {
                    strCommentFix += jobs[1]; // Old comment index on comment lines
                }
                else if (! strCommentFix.equals("")) {
                    jobs = fixOldStyleComment(jobs, strCommentFix);
                    strCommentFix = "";
                }

                if (! jobs[LABOR_RULE_INDEX_TYPE].equals("COMMENT")) {
                    //System.out.println(jobs.length);
                    if (jobs.length < LABOR_RULE_MIN_COLS) {
                        logger.severe("Warning: A line in "
                                + RULE_FILE_NAME + " is improperly formatted.");
                    }
                    else {

                        // Use an empty string for the comment if there is no
                        // "Comment" column
                        String strComment = "";
                        if (jobs.length > LABOR_RULE_MIN_COLS)
                            strComment = jobs[LABOR_RULE_INDEX_COMMENT];

                        mvRuleFile.add(new LaborRule(jobs[LABOR_RULE_INDEX_TYPE]
                                , jobs[LABOR_RULE_INDEX_FIRST_LABOR]
                                , jobs[LABOR_RULE_INDEX_SECOND_LABOR]
                                , strComment));
                    }
                }
            }

            // Comments from the end of the file - append to last non-COMMENT entry
            if (! strCommentFix.equals("")) {
                final int lastIndex = mvRuleFile.size() - 1;
                //mvRuleFile.set(lastIndex
                //        , fixOldStyleComment(mvRuleFile.get(lastIndex), strComment));
                final String[] newData = fixOldStyleComment(jobs
                        , strCommentFix);
                mvRuleFile.get(lastIndex).setComment(
                        newData[LABOR_RULE_INDEX_COMMENT]);
            }

            br.close();
            in.close();

            // Create the rule structures
            createRuleStructures(mvRuleFile);

            // (Do not post-process the whitelist here)
        } catch (final FileNotFoundException e) {
            DwarfOrganizer.warn(ERROR_MESSAGE, e);
        } catch (final Exception e) {
            DwarfOrganizer.warn(ERROR_MESSAGE, e);
        }
    }
    protected JobBlacklist getBlacklist() {
        return moBlacklist;
    }
    protected JobList getWhitelist() {
        return moWhitelist;
    }
    protected List<LaborRule> getRuleFileContents() {
        return mvRuleFile;
    }

    // Appends old COMMENT entries (comment) to the given non-comment line
    // (jobs)
    private String[] fixOldStyleComment(String[] jobs
            , final String comment) {

        final int NO_COMMENT_SIZE = 3;
        final int COMMENT_SIZE = 4;
        final int COMMENT_INDEX = 3;

        // Append comment to existing comment
        if (jobs.length > NO_COMMENT_SIZE)
            jobs[COMMENT_INDEX] += "; " + comment;
        // Create comment on next non-COMMENT line
        else {
            final String[] clone = (String[]) jobs.clone();
            jobs = new String[COMMENT_SIZE];
            // Replaced with System.arraycopy:
            /*for (int iCount = 0; iCount < COMMENT_SIZE - 1; iCount++)
                jobs[iCount] = clone[iCount]; */
            System.arraycopy(clone, 0, jobs, 0, COMMENT_SIZE - 1);
            jobs[COMMENT_INDEX] = comment;
        }
        return jobs;
    }

    // Creates moBlacklist and moWhitelist from the given file data
    private void createRuleStructures(final List<LaborRule> vData) { // String[]
        //for (String[] fields : vData) {
        for (final LaborRule laborRule : vData) {
            if (laborRule.getType().equals("BLACKLIST")) { // fields[0]
                moBlacklist.addOneWayEntry(laborRule.getFirstLabor()
                        , laborRule.getSecondLabor()); //fields[1], fields[2]);
                moBlacklist.addOneWayEntry(laborRule.getSecondLabor()
                        , laborRule.getFirstLabor()); //fields[2], fields[1]);
                //System.out.println("RULE: Blacklist: One dwarf may not do " + fields[1]
                //    + " and " + fields[2] + " simultaneously.");
            }
            else if (laborRule.getType().equals("WHITELIST")) { // fields[0].equals("WHITELIST")
                moWhitelist.addOneWayEntry(laborRule.getFirstLabor()
                        , laborRule.getSecondLabor()); //fields[1], fields[2]);
                //System.out.println("RULE: Whitelist: " + fields[1] + " may do "
                //    + fields[2]);
            }
            else {
                logger.log(Level.SEVERE, "Warning: Unknown labor rule type {0}"
                        , laborRule.getType());
            }
        }
    }

    // Writes the given data to the rule file.
    // Recreates the whitelist and blacklist data structures.
    protected void writeRuleFile(final List<LaborRule> vData) { // String[]

        moBlacklist = new JobBlacklist();
        moWhitelist = new JobList();

        // Open the file
        try {
            final FileWriter fstream = new FileWriter(getOutputFile(
                    RULE_FILE_NAME));
            final BufferedWriter out = new BufferedWriter(fstream);
            logger.info("Writing " + RULE_FILE_NAME);

            // Write the first line
            out.write(RULES_NOTE);
            out.newLine();
            out.flush();

            // Write all subsequent lines
            for (final LaborRule line : vData) {
                out.write(line.getType() + "\t" + line.getFirstLabor() + "\t"
                        + line.getSecondLabor() + "\t" + line.getComment());
                out.newLine();
                out.flush();
            }

            out.close();
            fstream.close();

            // Success: update mvRuleFile with the new data
            mvRuleFile = vData;
            createRuleStructures(mvRuleFile);

        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to write " + RULE_FILE_NAME, e);
        }
    }

    // DwarfIO has been imported from DwarfListWindow and is a little more messy
    // than newer functions in this file
    public static class DwarfIO {

        private static final String DEFAULT_DWARF_AGE = "999";
        private static final String DEFAULT_TRAIT_VALUE = "50";
        private static final int MAX_DWARF_TIME = 100;
        private final Pattern SKILL_LEVEL_PATTERN; // Set by constructor

        private final int[] plusplusRange = { 700, 1200, 1400, 1500, 1600, 1800
                , 2500 };
        private final int[] plusRange = { 450, 950, 1150, 1250, 1350, 1550
                , 2250 };
        private final int[] avgRange = { 200, 750, 900, 1000, 1100, 1300
                , 2000 };
        private final int[] minusRange = { 150, 600, 800, 900, 1000, 1100
                , 1500 };

        // Fixed to include the neutral range  4/23/12
        //private final long[] socialRange = { 0, 10, 25, 61, 76, 91, 100 };
        private final int[] socialRange = { 0, 10, 25, 40, 61, 76, 91 };

        private final String[] SOCIAL_TRAITS = { "Friendliness"
                , "Self_consciousness", "Straightforwardness", "Cooperation"
                , "Assertiveness", "Altruism" };

        private Map<String, Stat> mhtStats;
        private Map<String, Skill> mhtSkills;
        private Map<String, MetaSkill> mhtMetaSkills;

        private ArrayList<Dwarf> mvDwarves;

        public DwarfIO() {
            // "Constants"
            SKILL_LEVEL_PATTERN = Pattern.compile("(.*\\[)(\\d+)(\\].*)");
        }

        public ArrayList<Dwarf> getDwarves() {
            return mvDwarves;
        }

        public Map<String, MetaSkill> getMetaSkills() {
            return mhtMetaSkills;
        }

        public Map<String, Skill> getSkills() {
            return mhtSkills;
        }

        public Map<String, Stat> getStats() {
            return mhtStats;
        }

        public void readDwarves(final String filePath) {

            mvDwarves = new ArrayList<Dwarf>(); // Initialize before reading anything

            try {

                // Skills
                createSkills();

                // Trait hints
                readTraitHints();

                // Dwarves.xml
                final MyXMLReader xmlFileReader = new MyXMLReader(filePath);
                final NodeList nodes
                        = xmlFileReader.getDocument().getElementsByTagName(
                        "Creature");
                logger.log(Level.INFO, "Dwarves.xml contains {0} creatures."
                        , nodes.getLength());
                parseDwarves(nodes);
            } catch (final Exception e) {
                DwarfOrganizer.warn("Failed to read the last dwarves.XML", e);
            }
        }

        // Reads labor-skills.XML
        private void createSkills() {

            mhtSkills = new HashMap<String, Skill>();
            mhtMetaSkills = new HashMap<String, MetaSkill>();

            createStats();

            // Read skills from XML.
            try {
                mhtSkills = getLaborSkills();
            } catch (final URISyntaxException e) {
                final String message = "URI syntax exception: failed to read"
                        + " labor-skills.XML";
                DwarfOrganizer.crash(message, e);
            } catch (final FileNotFoundException e) {
                final String message = "labor-skills.xml not found."
                        + " The application will shut down.";
                DwarfOrganizer.crash(message, e);
            }

            // TODO: Remove this hard-coding. Put meta skills in XML file.
            // Meta skills: Broker and manager
            mhtMetaSkills.put("Broker", new MetaSkill("Broker"
                , new ArrayList<Skill>(Arrays.asList(new Skill[] {
                  mhtSkills.get("Appraisal")
                , mhtSkills.get("Judging Intent"), mhtSkills.get("Conversation")
                , mhtSkills.get("Comedy"), mhtSkills.get("Flattery")
                , mhtSkills.get("Lying")
                , mhtSkills.get("Intimidation"), mhtSkills.get("Persuasion")
                , mhtSkills.get("Negotiation"), mhtSkills.get("Consoling")
                , mhtSkills.get("Pacification") }))));
            mhtMetaSkills.put("Manager", new MetaSkill("Manager"
                    , new ArrayList<Skill>(Arrays.asList(new Skill[] {
                    mhtSkills.get("Organization")
                    , mhtSkills.get("Consoling"), mhtSkills.get("Pacification")
                    }))));
        }

        // Creates mhtStats.
        // TODO Remove hard-coding
        private void createStats() {
            mhtStats = new HashMap<String, Stat>();

            final String[] plusplusStats = { "Focus", "Spatial Sense" };
            final String[] plusStats = { "Strength", "Toughness"
                    , "Analytical Ability", "Creativity", "Patience"
                    , "Memory" };
            final String[] avgStats = { "Endurance", "Disease Resistance"
                    , "Recuperation", "Intuition", "Willpower"
                    , "Kinesthetic Sense", "Linguistic Ability"
                    , "Musicality", "Empathy", "Social Awareness", }; // "Altruism"
            final String[] minusStats = { "Agility" };

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

        private void createStats(final String[] statName
                , final int[] statRange) {

            for (int iCount = 0; iCount < statName.length; iCount++) {
                mhtStats.put(statName[iCount], new Stat(statName[iCount]
                        , statRange));
            }
        }

        // Reads the trait hints file (needed for Runesmith style XML traits)
        private void readTraitHints() {
            final String ERROR_MESSAGE = "Error when reading file"
                    + " trait-hints.txt."
                    + " Dwarf traits may not have correct values.";
            String strLine;

            try {
                final FileInputStream in = new FileInputStream(getInputFile(
                        TRAITS_FILE_NAME));
                final BufferedReader br = new BufferedReader(
                        new InputStreamReader(in));

                while ((strLine = br.readLine()) != null) {
                    final String[] data = strLine.split("\t");

                    mhtStats.get(data[0]).addStatHint(data[3]
                            , Integer.parseInt(data[1])
                            , Integer.parseInt(data[2]));
                }
                in.close();
            } catch (final FileNotFoundException e) {
                DwarfOrganizer.warn(ERROR_MESSAGE, e);
            } catch (final Exception e) {
                DwarfOrganizer.warn(ERROR_MESSAGE, e);
            }
        }

        // Reads the labor skills XML data file
        // Commented code has been left intact so that I (hopefully) don't ever have
        // to research the problems I encountered here again
        private HashMap<String, Skill> getLaborSkills()
                throws URISyntaxException, FileNotFoundException {

            final HashMap<String, List<Stat>> htStatGroup
                    = new HashMap<String, List<Stat>>();
            final HashMap<String, Skill> htReturn
                    = new HashMap<String, Skill>();
            List<Stat> vStat;
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
                final MyXMLReader xmlFileReader = new MyXMLReader(
                        new FileInputStream(getInputFile(
                        LABOR_SKILLS_XML_FILE_NAME)));

                // Read the stat group macros
                final NodeList nlStatGroup
                        = xmlFileReader.getDocument().getElementsByTagName(
                        "StatGroup");
                //Element ele = (Element) nlStatGroup.item(0);
                //System.out.println(nlStatGroup.getLength() + " stat groups");
                for (int iCount = 0; iCount < nlStatGroup.getLength(); iCount++) {

                    final Element thisStatGroup
                            = (Element) nlStatGroup.item(iCount);
                    final String strStatGroupName = getTagValue(thisStatGroup
                            , "Name", "Error - Null name in XML"); // thisStatGroup.getAttribute("Name");
                    //System.out.println("Stat group name: " + strStatGroupName);
                    stat = thisStatGroup.getElementsByTagName("Stat");
                    vStat = new ArrayList<Stat>(stat.getLength());
                    for (int jCount = 0; jCount < stat.getLength(); jCount++) {
                        thisStat = (Element) stat.item(jCount);
                        final String name = thisStat.getAttribute("Name");
                        vStat.add(mhtStats.get(name));
                    }
                    htStatGroup.put(strStatGroupName, vStat);
                }
                //printStatGroups(htStatGroup);

                // Read the skills (and secondary skills, and social skills) for labors
                final Class[] classes = {Skill.class, SecondarySkill.class
                        , SocialSkill.class};
                for (final Class classItem : classes) {
                    final NodeList nlLaborSkill
                            = xmlFileReader.getDocument().getElementsByTagName(
                                classItem.getSimpleName());   // "Skill"
                    for (int kCount = 0; kCount < nlLaborSkill.getLength(); kCount++) {

                        final Element thisLaborSkill
                                = (Element) nlLaborSkill.item(kCount);
                        final String strName = getTagValue(thisLaborSkill
                                , "Name"
                                , "Error - Null labor skill name in XML");
                        //System.out.println(strName + " :");

                        // Stats and StatGroupRefs can be listed
                        // Add stats to stat list
                        stat = thisLaborSkill.getElementsByTagName("Stat");
                        vStat = new ArrayList<Stat>(stat.getLength());
                        //System.out.println(stat.getLength() + " stats");
                        for (int mCount = 0; mCount < stat.getLength(); mCount++) {
                            final String name
                                    = stat.item(mCount).getTextContent();
                            //System.out.println("    " + strStatName);   // thisStat.getNodeValue()
                            vStat.add(mhtStats.get(name));   // thisStat.getNodeValue()
                        }

                        // Add StatGroupRefs to stat list
                        final NodeList statGroup
                                = thisLaborSkill.getElementsByTagName(
                                "StatGroupRef");
                        for (int nCount = 0; nCount < statGroup.getLength(); nCount++) {
                            final Element thisStatGroup
                                    = (Element) statGroup.item(nCount);
                            //System.out.println(thisStatGroup.getTextContent());
                            //printStatGroup(htStatGroup.get(thisStatGroup.getTextContent()));
                            vStat.addAll(htStatGroup.get(
                                    thisStatGroup.getTextContent()));
                        }

                        // Prevented by trait, min, and max for social skills---
                        String strTrait = "Error - No trait in XML";
                        int intMin = 0;
                        int intMax = 100;

                        if (classItem == SocialSkill.class) {
                            strTrait = thisLaborSkill.getElementsByTagName(
                                    "Trait").item(0).getTextContent();
                            intMin = Integer.parseInt(
                                    thisLaborSkill.getElementsByTagName(
                                    "Min").item(0).getTextContent());
                            intMax = Integer.parseInt(
                                    thisLaborSkill.getElementsByTagName(
                                    "Max").item(0).getTextContent());
                        }
                        // ------------End special social skills processing---------

                        // Add the skill to the hash table
                        if (classItem == Skill.class)
                            htReturn.put(strName, new Skill(strName, vStat));
                        else if (classItem == SecondarySkill.class)
                            htReturn.put(strName, new SecondarySkill(strName, vStat));
                        else if (classItem == SocialSkill.class) {
                            htReturn.put(strName, new SocialSkill(strName, vStat
                                    , strTrait, intMin, intMax));
                        }
                        else
                            logger.log(Level.SEVERE,"classItem is not of a"
                                    + " recognized type. Ignoring skill {0}"
                                    , strName);
                    }
                }

            //} catch (URISyntaxException e) { e.printStackTrace();
            //}

            return htReturn;
        }

        // Translates XML data to dwarf objects
        private void parseDwarves(final NodeList nodes) {
            //mvDwarves = new Vector<Dwarf>();

            for (int iCount = 0; iCount < nodes.getLength(); iCount++) {

                final Element thisCreature = (Element) nodes.item(iCount);
                final int age = Integer.parseInt(getTagValue(thisCreature, "Age"
                        , DEFAULT_DWARF_AGE));

                // Stopped skipping juveniles for DF 34.05 due to age bugs
                //if (! isJuvenile(age)) {

                // Read stat values and get percentiles.
                final HashMap<String, Integer> statValues
                        = new HashMap<String, Integer>();
                final HashMap<String, Integer> htPercents
                        = new HashMap<String, Integer>();
                for (final String key : mhtStats.keySet()) {
                    //System.out.println("Getting " + mhtStats.get(key).name);

                    int value;
                    if (mhtStats.get(key).getXmlName() != null)
                        value = Integer.parseInt(getTagValue(thisCreature
                            , mhtStats.get(key).getXmlName(), "0"));

                    else {  // Look under Traits if there is no attribute XML name

                        // TODO: get dwarven personality average to use as default

                        // (DFHack style XML) If the trait has a named entry,
                        // then get the value
                        final Element traits
                                = (Element) thisCreature.getElementsByTagName(
                                "Traits").item(0);
                        value = Integer.parseInt(getXMLValueByKey(traits
                                , "Trait", "name", mhtStats.get(key).getName()
                                , "value", "-1"));   // DEFAULT_TRAIT_VALUE

                        // If we could not get the exact trait value, perhaps
                        // this is a Runesmith XML file. Check for trait hints.
                        if (value == -1) {
                            value = Integer.parseInt(DEFAULT_TRAIT_VALUE);
                            for (StatHint sh : mhtStats.get(key).getStatHints()) {
                                //if (traits contains hint)
                                if (getTagList(thisCreature, "Traits").contains(
                                        sh.hintText)) {

                                    value = (sh.hintMin + sh.hintMax) / 2;
                                    //System.out.println(htStats.get(key).name + " " + value);
                                    break;
                                }
                            }
                        }
                    }
                    //System.out.println("Value: " + value);
                    statValues.put(key, value);
                    htPercents.put(key, (int) Math.round(
                            getPlusPlusPercent(mhtStats.get(key).getRange()
                            , value)));
                    //System.out.println(key + htPercents.get(key));
                }

                // Create a dwarf object
                final Dwarf oDwarf = new Dwarf();

                oDwarf.setName(getTagValue(thisCreature, "Name"
                        , "Error - Null Name"));
                oDwarf.setAge(age);
                oDwarf.setGender(getTagValue(thisCreature, "Sex"
                        , "Error - Null Sex"));
                oDwarf.setNickname(getTagValue(thisCreature, "Nickname", ""));
                oDwarf.setStatPercents(htPercents);
                oDwarf.setStatValues(statValues);
                oDwarf.setTime(MAX_DWARF_TIME);
                oDwarf.setJobText(getTagList(thisCreature, "Labours"));
                final String jobs[] = oDwarf.getJobText().split("\n");
                //if (jobs.length <= 1)
                    //System.out.println("No labors enabled.");
                //else {
                if (jobs.length > 1) {  // First and last entries in the labor list from XML are blank
                    for (int jCount = 1; jCount < jobs.length - 1; jCount++) {
                        //System.out.println(oDwarf.name + ": labor " + jCount
                        //        + " enabled: " + jobs[jCount].trim());
                        oDwarf.getLabors().add(jobs[jCount].trim());
                    }
                }

                // Read current skill levels
                final Element skills
                        = (Element) thisCreature.getElementsByTagName(
                        "Skills").item(0);

                try {
                    final NodeList children = skills.getElementsByTagName(
                            "Skill");

                    for (int sCount = 0; sCount < children.getLength(); sCount++) {
                        final Element eleSkill = (Element) children.item(sCount);
                        final String strSkillName = getTagValue(eleSkill
                                , "Name", "Error - Null skill name");
                        final String strSkillLevel = getTagValue(eleSkill
                                , "Level", "Error - Null skill level");
                        //System.out.println(oDwarf.name + " " + strSkillName + " "
                        //        + strSkillLevel);

                        // If it is a DFHack dwarves.XML, the skill level will
                        // be just digits.
                        long skillValue; // = -1;
                        try {
                            skillValue = Long.parseLong(strSkillLevel);
                        } catch (final NumberFormatException ignore) {
                            // Probably a Runesmith XML - convert the skill level
                            // to a numeric value if so
                            skillValue = skillDescToLevel(strSkillLevel);
                        }
                        oDwarf.getSkillLevels().put(strSkillName, skillValue);

                        //oDwarf.skillLevels.put(strSkillName
                        //        , skillDescToLevel(strSkillLevel));
                    }

                } catch (final java.lang.NullPointerException ignore) {
                    logger.log(Level.SEVERE, "Skills are not present in the"
                            + " given dwarves.xml file. {0} will not have skill"
                            + " levels.", oDwarf.getName());
                }

                // Simple skill potentials
                for (final String key : mhtSkills.keySet()) {
                    final Skill oSkill = mhtSkills.get(key);
                    oDwarf.getSkillPotentials().put(oSkill.getName()
                            , getPotential(oDwarf, oSkill));
                }
                // Meta skill potentials
                for (final String key : mhtMetaSkills.keySet()) {
                    final MetaSkill meta = mhtMetaSkills.get(key);
                    double dblSum = 0.0d;

                    for (final Skill oSkill : meta.vSkills)
                        dblSum += oDwarf.getSkillPotentials().get(
                                oSkill.getName());

                    oDwarf.getSkillPotentials().put(meta.getName()
                            , Math.round(dblSum / meta.vSkills.size()));
                }
                mvDwarves.add(oDwarf);

            }
        }
        // Calculates the dwarf's "potential" for the given skill
        private long getPotential(final Dwarf oDwarf, final Skill oSkill) {

            double dblSum = 0.0d;
            final List<Stat> vStats = oSkill.getStats();
            final double numStats = (double) vStats.size();

            for (int kCount = 0; kCount < numStats; kCount++) {
                double addValue = oDwarf.getStatPercents().get(
                        vStats.get(kCount).getName());

                // If the dwarf cannot gain skill because of a personality trait
                if (oSkill.getClass() == SocialSkill.class) {
                    final SocialSkill sSkill = (SocialSkill) oSkill;
                    final long noValue = oDwarf.getStatValues().get(
                            sSkill.getNoStatName());
                    if (noValue >= sSkill.getNoStatMin()
                            && noValue <= sSkill.getNoStatMax())
                        addValue = 0;
                }

                dblSum += addValue;
            }

            return Math.round(dblSum / numStats);
        }

        // Converts a Runesmith skill level description to long integer value
        private long skillDescToLevel(final String skillLevelDesc) {

            final Matcher matcher = SKILL_LEVEL_PATTERN.matcher(skillLevelDesc);
            if (matcher.find())
                return Long.parseLong(matcher.group(2));
            else
                logger.severe("Pattern not matched.");

            return 0;
        }
        private double getPlusPlusPercent(final int[] range
                , final int attribute) {

            final double chanceToBeInBracket = 1.0d / (range.length - 1);
            final int bracket = getPlusPlusBracket(range, attribute);

            if (bracket > 0) {
                final int numBracketsBelow = bracket - 1;
                final double bracketSize = range[bracket] - range[bracket - 1];
                final double inBracketPercent = (attribute - range[bracket - 1])
                        / bracketSize;

                return 100.0d * chanceToBeInBracket
                        * (inBracketPercent + numBracketsBelow);
            }
            else    // Not in a bracket: better than 100% of dwarves
                return 100.0d;

        }
        private int getPlusPlusBracket(final int[] range, final int attribute) {
            //long minValue = range[0];
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
        private String getXMLValueByKey(final Element parent
                , final String tagName, final String keyName
                , final String keyValue, final String valueName
                , final String nullValue) {

            try {
                final NodeList children = parent.getElementsByTagName(tagName);

                //System.out.println("Number of children: " + children.getLength());
                for (int iCount = 0; iCount < children.getLength(); iCount++) {
                    final Element eleItem = (Element) children.item(iCount);
                    //System.out.println("Key name: " + eleItem.getAttribute(keyName));
                    if (eleItem.getAttribute(keyName).toUpperCase().equals(
                            keyValue.toUpperCase())) {
                        return eleItem.getAttribute(valueName);
                    }
                }

            } catch (final java.lang.NullPointerException ignore) {
                logger.log(Level.SEVERE, "Error encountered when retrieving XML"
                        + " value {0} by key.", keyValue);
                return nullValue;
            }

            logger.log(Level.SEVERE, "{0}:{1}:{2} was not found in xml data"
                    , new Object[]{tagName, keyName, valueName});
            return nullValue;
        }
        private String getTagList(final Element creature
                , final String tagName) {

            final NodeList parent = creature.getElementsByTagName(tagName);

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
            final FileInputStream in = new FileInputStream("./license.txt");
            final BufferedReader br = new BufferedReader(
                    new InputStreamReader(in));

            while ((line = br.readLine()) != null)
                strReturn += line + "\n";

            br.close();
            in.close();

        } catch (final FileNotFoundException ex) {
            final String message = "Error: License not found.";
            logger.log(Level.SEVERE, message, ex);
            strReturn += message;
        } catch (final IOException ex) {
            final String message = "Error: Failed to read license.";
            logger.log(Level.SEVERE, message, ex);
            strReturn += message;
        }
        return strReturn;
    }

    // Loads job settings from file
    public void readJobSettings(final File file, final List<Job> vLaborSettings
            , final String defaultReminder) {

        final String ERROR_MESSAGE = "Failed to load job file.";

        try {
            final FileInputStream fstream = new FileInputStream(
                    file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);

            final Map<String, Job> htJobs = hashJobs(
                    MyFileUtils.readDelimitedLineByLine(in, "\t", 0)
                    , defaultReminder);

            // Update the current labor settings with the file data.
            for (final Job job : vLaborSettings) {
                final Job jobFromFile = htJobs.get(job.getName());
                if (jobFromFile != null) {
                    job.setQtyDesired(jobFromFile.getQtyDesired());
                    job.setCandidateWeight(jobFromFile.getCandidateWeight());
                    job.setCurrentSkillWeight(
                            jobFromFile.getCurrentSkillWeight());
                    job.setTime(jobFromFile.getTime());
                    job.setReminder(jobFromFile.getReminder());
                }

                else
                    logger.log(Level.SEVERE, "WARNING: Job ''{0}"
                            + "'' was not found"
                            + " in the file. Its settings will be the"
                            + " defaults.", job.getName());
            }
        } catch (final FileNotFoundException e) {
            logger.log(Level.SEVERE, ERROR_MESSAGE, e);
        } catch (final Exception e) {
            logger.log(Level.SEVERE, ERROR_MESSAGE, e);
        }
    }

    // Loads the job data into a hash map
    private HashMap<String, Job> hashJobs(final List<String[]> vJobs
            , final String defaultReminder) {

        final HashMap<String, Job> htReturn = new HashMap<String, Job>();
        String strReminder;

        for (final String[] jobData : vJobs) {
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
                    , Integer.parseInt(jobData[2])
                    , Double.parseDouble(jobData[3])
                    , Integer.parseInt(jobData[4]), strReminder));
            }
        }
        return htReturn;
    }

    // Write views to XML
    public boolean writeViews(final List<GridView> lstView) {

        final DocumentBuilderFactory docFactory
                = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();

            // Root element
            final Element eleRoot = doc.createElement("Views");
            doc.appendChild(eleRoot);

            // Number of views
            final Element eleNum = doc.createElement("NumViews");
            eleRoot.appendChild(eleNum);
            eleNum.appendChild(doc.createTextNode(Integer.toString(
                    lstView.size())));

            // Body
            int iCount = 0;
            for (final GridView view : lstView) {
                //GridView view = mapView.get(key);
                final Element eleView = doc.createElement("View_" + iCount);
                eleRoot.appendChild(eleView);

                final Element eleName = doc.createElement("Name");
                eleView.appendChild(eleName);
                eleName.appendChild(doc.createTextNode(view.getName()));

                final Element eleOrder = doc.createElement("ColOrder");
                eleView.appendChild(eleOrder);
                final int size = view.getColOrder().size();
                final Element eleNumCols = doc.createElement("NumCols");
                eleOrder.appendChild(eleNumCols);
                eleNumCols.appendChild(doc.createTextNode(
                        Integer.toString(size)));

                for (int jCount = 0; jCount < size; jCount++) {
                    final Object col = view.getColOrder().get(jCount);
                    final Element eleCol = doc.createElement("Col_" + jCount);
                    eleOrder.appendChild(eleCol);
                    eleCol.appendChild(doc.createTextNode(col.toString()));
                }
                iCount++;   // View count
            }

            //-----------------------
            // Write the document to XML file
            final TransformerFactory transformerFactory
                    = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");
            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(new File(getOutputFile(
                    VIEW_FILE_NAME)));
            //StreamResult result = new StreamResult(System.out);  Uncomment for testing

            transformer.transform(source, result);

        } catch (final ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        } catch (final TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        } catch (final TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }

        return true;    // Success
    }
    public ArrayList<GridView> readViews() {

        final AbstractXMLReader<ArrayList<GridView>> reader
                = new AbstractXMLReader<ArrayList<GridView>>() {

            @Override
            public ArrayList<GridView> getDefaultReturnObject() {
                return new ArrayList<GridView>();
            }

            @Override
            public ArrayList<GridView> processDocument(final Document doc
                    , final ArrayList<GridView> returnObject) {

                ArrayList<Object> colOrder; // = new ArrayList<Object>();
                final int numViews = Integer.parseInt(doc.getElementsByTagName(
                        "NumViews").item(0).getTextContent());

                for (int iCount = 0; iCount < numViews; iCount++) {
                    final Node viewNode = doc.getElementsByTagName(
                            "View_" + iCount).item(0);
                    final Element ele = (Element) viewNode;
                    final String name = ele.getElementsByTagName(
                            "Name").item(0).getTextContent();

                    final int numCols = Integer.parseInt(
                            ele.getElementsByTagName(
                            "NumCols").item(0).getTextContent());
                    colOrder = new ArrayList<Object>(numCols);
                    for (int jCount = 0; jCount < numCols; jCount++) {
                        colOrder.add(ele.getElementsByTagName(
                                "Col_" + jCount).item(0).getTextContent());
                    }

                    // "", GridView.KeyAxis.X_AXIS, false,
                    final GridView view = new GridView(name, colOrder);
                    returnObject.add(view);
                }
                return returnObject;
            }

        };
        return reader.readFile(getInputFile(VIEW_FILE_NAME), "Failed to read "
                + VIEW_FILE_NAME + ". Cannot load Dwarf List view data.");
    }
    private abstract class AbstractXMLReader<T> {
        // Returns the initialized object to be processed and returned after
        // reading:
        public abstract T getDefaultReturnObject();

        // Processes the given document and returns the contents,
        // given the initialized return object
        public abstract T processDocument(Document doc, T initializedObject);

        public T readFile(final String fileName, final String errorMessage) {
            T returnObject = getDefaultReturnObject();

            final File file = new File(fileName);
            final DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();

            try {
                final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                final Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();

                returnObject = processDocument(doc, returnObject);

            } catch (final ParserConfigurationException ex) {
                DwarfOrganizer.warn(errorMessage, ex);
            } catch (final SAXException ex) {
                DwarfOrganizer.warn(errorMessage, ex);
            } catch (final IOException ex) {
                DwarfOrganizer.warn(errorMessage, ex);
            }

            return returnObject;
        }
    }

    // Writes exclusions to XML
    public boolean writeExclusions(final List<Exclusion> lstExclusion) {

        // Create XML document
        final DocumentBuilderFactory docFactory
                = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            final Document doc = docBuilder.newDocument();

            // Root element
            final Element eleRoot = doc.createElement("Exclusions");
            doc.appendChild(eleRoot);

            // File version
            final Element eleVer = doc.createElement("FileVersion");
            eleVer.setAttribute("Version", CURRENT_EXCLUSIONS_VERSION);
            eleRoot.appendChild(eleVer);

            // Start Exclusion rules
            final Element eleRules = doc.createElement("ExclusionRules");
            eleRoot.appendChild(eleRules);

            // Exclusion rules body
            for (final Exclusion exclusion : lstExclusion) {
                if (exclusion.getClass().equals(ExclusionRule.class)) {
                    final ExclusionRule rule = (ExclusionRule) exclusion;
                    final Element eleRule = doc.createElement("ExclusionRule");
                    eleRules.appendChild(eleRule);
                    eleRule.setAttribute("ID", rule.getID().toString());
                    eleRule.setAttribute("Name", rule.getName());
                    eleRule.setAttribute("Field", rule.getPropertyName());
                    eleRule.setAttribute("Comparator", rule.getComparator());
                    eleRule.setAttribute("Value", rule.getValue().toString());
                }
            }

            // Exclusion Lists
            final Element eleLists = doc.createElement("ExclusionLists");
            eleRoot.appendChild(eleLists);

            // Exclusion lists body
            for (final Exclusion exclusion : lstExclusion) {
                if (exclusion.getClass().equals(ExclusionList.class)) {
                    final ExclusionList list = (ExclusionList) exclusion;
                    final Element eleList = doc.createElement("ExclusionList");
                    eleLists.appendChild(eleList);
                    eleList.setAttribute("ID", list.getID().toString());
                    eleList.setAttribute("Name", list.getName());

                    final Element eleCitizens = doc.createElement("Citizens");
                    eleList.appendChild(eleCitizens);
                    for (final String citizen : list.getCitizenList()) {
                        final Element eleCitizen = doc.createElement("Citizen");
                        eleCitizens.appendChild(eleCitizen);
                        eleCitizen.setAttribute("Name", citizen);
                    }
                }
            }

            //-----------------------
            // Write the document to XML file
            final TransformerFactory transformerFactory
                    = TransformerFactory.newInstance();
            //transformerFactory.setAttribute("indent-number", new Integer(4));
            final Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            // Hooray for completely undocumented solutions:
            transformer.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount", "4");
            final DOMSource source = new DOMSource(doc);
            final StreamResult result = new StreamResult(new File(getOutputFile(
                    EXCLUSION_FILE_NAME)));
            //StreamResult result = new StreamResult(System.out);  Uncomment for testing

            transformer.transform(source, result);

        } catch (final ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        } catch (final TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        } catch (final TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
        return true;        // Success
    }

    private abstract class SectionReader {
        private String name;
        private Integer ID;
        public void setName(final String name) {
            this.name = name;
        }
        public void setID(final Integer ID) {
            this.ID = ID;
        }
        public Integer getID() {
            return ID;
        }
        public String getName() {
            return name;
        }
        public abstract void doVersionAPlusFunction(Element ele);
        public abstract void doVersionBPlusFunction(Element ele);
        public abstract Exclusion createExclusion();
    }

    private ArrayList<Exclusion> readSection(final SectionReader srf
            , final Document doc, final String tagName, final String version) {

        final ArrayList<Exclusion> lstReturn = new ArrayList<Exclusion>();
        Integer id = -1;
        final NodeList lstExclusionRules = doc.getElementsByTagName(tagName);

        for (int iCount = 0; iCount < lstExclusionRules.getLength(); iCount++) {
            final Node node = lstExclusionRules.item(iCount);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element ele = (Element) node;

                // TODO: Proper conversion between versions
                try {
                    if (version.compareTo("A") <= 0) {
                        id = iCount;
                        srf.setID(id);
                    }

                    if (version.compareTo("A") >= 0) {
                        final String name = ele.getAttribute("Name");
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
                    lstReturn.add(srf.createExclusion());

                } catch (final Exception e) {
                    logger.log(Level.SEVERE, "Failed to read a(n) {0}"
                            , tagName);
                }
            }
        }
        return lstReturn;
    }

    public ArrayList<Exclusion> readExclusions(final List<Dwarf> citizens) {

        final ArrayList<Exclusion> vReturn = new ArrayList<Exclusion>();

        final File file = new File(getInputFile(EXCLUSION_FILE_NAME));
        final DocumentBuilderFactory dbFactory
                = DocumentBuilderFactory.newInstance();

        try {
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            final NodeList lstFileVersion = doc.getElementsByTagName(
                    "FileVersion");
            final Node node = lstFileVersion.item(0);
            final Element ele = (Element) node;
            final String version = ele.getAttribute("Version");

            // Exclusion Rules-----------------
            final SectionReader exclusionRuleReader = new SectionReader() {
                private String field;
                private String comparator;
                private Object value;

                @Override
                public void doVersionAPlusFunction(final Element ele) {
                    field = ele.getAttribute("Field");
                    comparator = ele.getAttribute("Comparator");
                    value = ele.getAttribute("Value");
                }

                @Override
                public Exclusion createExclusion() {
                    return new ExclusionRule(this.getID(), this.getName()
                            , DEFAULT_EXCLUSION_ACTIVE
                            , this.field
                            , this.comparator, this.value); // isExclusionActive(this.ID, htActive)
                }

                @Override
                public void doVersionBPlusFunction(final Element ele) {
                    // Do nothing extra
                }
            };
            vReturn.addAll(readSection(exclusionRuleReader, doc, "ExclusionRule"
                    , version));

            // Exclusion Lists---------------
            final SectionReader exclusionListReader = new SectionReader() {
                private ArrayList<String> lstCitizenName;

                @Override
                public void doVersionAPlusFunction(final Element ele) {
                    // Do nothing
                }

                @Override
                public void doVersionBPlusFunction(final Element ele) {
                    lstCitizenName = new ArrayList<String>();
                    final NodeList nlist = ele.getElementsByTagName("Citizen");
                    for (int iCount = 0; iCount < nlist.getLength(); iCount++) {
                        final Node node = nlist.item(iCount);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            final Element eleCitizen = (Element) node;
                            lstCitizenName.add(eleCitizen.getAttribute("Name"));
                        }
                    }
                    //System.out.println("Citizens in list: " + vCitizenName.size() + ", citizen total list size = " + citizens.size());
                    //vCitizen = getCitizensFromNames(vCitizenName, citizens);
                    //System.out.println("    Found " + vCitizen.size() + " matching citizen objects");
                }

                @Override
                public Exclusion createExclusion() {
                    return new ExclusionList(this.getID(), this.getName()
                            , DEFAULT_EXCLUSION_ACTIVE, lstCitizenName); // isExclusionActive(this.ID, htActive)
                }

            };
            vReturn.addAll(readSection(exclusionListReader, doc, "ExclusionList"
                    , version));

        } catch (final ParserConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (final SAXException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (final IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        return vReturn;
    }
    private static String getTagValue(final String tag, final Element element) {
        final NodeList nodeList = element.getElementsByTagName(tag).item(
                0).getChildNodes();
        final Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }
    // Returns the value for the XML tag for this element,
    // or valueIfNull if the tag does not exist (from DwarfListWindow)
    private static String getTagValue(final Element element
            , final String tagName, final String valueIfNull) {

        final Element ele = (Element) element.getElementsByTagName(
                tagName).item(0);
        if (null == ele)
            return valueIfNull;
        if (null == ele.getChildNodes().item(0))
            return valueIfNull;

        return ((Node) ele.getChildNodes().item(0)).getNodeValue().trim();
    }

    // Returns the next exclusion ID to use, and increments the current maximum.
    public Integer incrementExclusionID() {
        mintMaxExclID++;
        return mintMaxExclID;
    }
    protected Integer getMaxUsedExclusionID() {
        return mintMaxExclID;
    }
    private boolean isExclusionActive(final int ID
            , final Map<Integer, Boolean> mapActive) {

        if (mapActive == null)
            return false;

        if (mapActive.containsKey(ID))
            return mapActive.get(ID);

        return false;
    }
    private ArrayList<Dwarf> getCitizensFromNames(final List<String> names
            , final List<Dwarf> citizens) {

        final ArrayList<Dwarf> vReturn = new ArrayList<Dwarf>();

        for (final String name : names) {
            //System.out.println("Looking for " + name);

            //Get the dwarf object for the name
            for (final Dwarf citizen : citizens) {
                //Dwarf citizen = (Dwarf) oCitizen;
                if (citizen.getName().equals(name)) {
                    vReturn.add(citizen);
                    break;
                }
            }
        }
        return vReturn;
    }
    public void writeJobSettings(final List<Job> jobList, final File toFile) {

        try {
            // Open the output file.
            logger.log(Level.INFO, "Writing to file {0}"
                    , toFile.getAbsolutePath());
            toFile.createNewFile();   // Create the file if it does not exist.

            final FileWriter fstream = new FileWriter(toFile.getAbsolutePath());
            final BufferedWriter out = new BufferedWriter(fstream);

            out.write(CURRENT_JOB_SETTINGS_VERSION);
            out.newLine();
            for (final Job job : jobList) {
                out.write(job.getName()
                        + "\t" + job.getQtyDesired()
                        + "\t" + job.getTime()
                        + "\t" + job.getCandidateWeight()
                        + "\t" + job.getCurrentSkillWeight()
                        + "\t" + job.getReminder());
                out.newLine();
                out.flush();
            }
            out.close();

        } catch (final IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
}
