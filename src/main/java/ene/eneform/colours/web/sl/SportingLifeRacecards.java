/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.sl;

import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.database.AdditionalRacesFactory;
import ene.eneform.smartform.bos.AdditionalRace;
import ene.eneform.smartform.bos.AdditionalRunner;
import ene.eneform.smartform.bos.SmartformTack;
import ene.eneform.smartform.bos.SportingLifeSearch;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Simon
 */
public class SportingLifeRacecards {
    // http://www.sportinglife.com/ajax/racing/results-search?course=Epsom&course-id=16&racename=derby+vodafone

    // looking for results from 1st Jan 1994 to 31st Dec 2002
    private static String sm_strSLBaseURL = "http://www.sportinglife.com";
    private static String sm_strSLRaceListURL = "http://www.sportinglife.com/ajax/racing/results-search?date-range=period&fromDate=01%2F01%2F|START_YEAR|&toDate=31%2F12%2F|END_YEAR|&course-id=";    // add course_id and racename
   
    //private static String sm_strSLRacecardURL = "http://www.sportinglife.com/racing/racecards/25-06-2003/salisbury/racecard/65363/noel-cannon-memorial-trophy-rated-stakes-handicap";
    private static Pattern sm_owner = Pattern.compile("Owned by\\s([\\w &'\\-,\\')\\(/]+)");
    private static Pattern sm_breeding = Pattern.compile("([\\w_/\\.]+)\\s([\\w]*)\\s* by \\s*([\\w'\\-\\(\\) ]*)[\\.\\s]*");
    private static Pattern sm_breedingDam= Pattern.compile("([\\w_/\\. ]+)\\s([\\w]+)\\s* by \\s*([\\w'\\-\\(\\)\\s]*)\\sout of ([\\w'\\s\\(\\)\\-]+)[\\.\\s]*");

        private static Pattern sm_race6a =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\+\\-]+),\\sClass\\s([\\w]+),\\s([\\w\\s]+),\\s([\\d]+)\\srunners,\\sGoing\\s([\\w\\s\\-:;,&\\.'/\\(\\)]+)\\)");
        private static Pattern sm_race6b =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\+\\-]+),\\sClass\\s([\\w]+),\\s([\\w\\s]+),\\s([\\d]+)\\srunners,\\s([\\w\\s\\-:;,&\\.'/\\(\\)]+)\\)");
        private static Pattern sm_race5a =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\+\\-]+),\\s([\\w\\s]+),\\s([\\d]+)\\srunners,\\sGoing\\s([\\w\\s\\-:;,&\\.'/\\(\\)]+)\\)"); // No class
        private static Pattern sm_race5b =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\+\\-]+),\\sClass\\s([\\w]+),\\s([\\w\\s]+),\\sGoing\\s([\\w\\s\\-:;,&\\.'/\\(\\)]+)\\)"); // No runners
        private static Pattern sm_race5c =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\+\\-]+),\\s([\\w\\s]+),\\s([\\d]+)\\srunners,\\s([\\w\\s\\-:;,&\\.'/\\(\\)]+)\\)"); // No Going keyword
        private static Pattern sm_race4a =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\+\\-]+),\\s([\\w\\s]+),\\sGoing\\s([\\w\\s\\-:;,&\\.'/\\(\\)]+)\\)"); // No class or runners
        private static Pattern sm_race4b =Pattern.compile("\\(([\\w\\+\\-]+),\\s([\\w\\s]+),\\sClass\\s([\\w]+),\\s([\\d]+)\\srunners\\)"); // No type or going
        private static Pattern sm_race4c =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\+\\-]+),\\s([\\w\\s]+),\\s([\\d]+)\\srunners\\)"); // No class or going
        private static Pattern sm_race4d =Pattern.compile("\\(([\\w_/\\.]+),\\s([\\w\\s]+),\\s([\\d]+)\\srunners,\\sGoing\\s([\\w\\s\\-:;,&\\.'/\\(\\)]+)\\)"); // No class or age-range
        // (5yo+, 2m 4f 110y, Class 1, 7 runners)  - no Type, Going
        // (5yo+, 2m 4f 110y, 7 runners)   - no Type, Class, Going
    // createSportingLifeSearch
    public static void updateSLRaceList(ENEStatement statement, SportingLifeSearch search)
    {
        // These are updates by race id, so don't create link
        loadSLRaceList(statement, "", search.getSearch(), search.getCourseId(), search.getCourse(), search.getMinYear(), search.getMaxYear(), true);
    }
        
    public static void loadSLRaceList(ENEStatement statement, String strARDRaceName)
    {
        loadSLRaceList(statement, strARDRaceName, strARDRaceName.replace(' ', '+'), 1994, 2002);
    }
    public static void loadSLRaceList(ENEStatement statement, String strARDRaceName, String strRaceSearch, int nStartYear, int nEndYear)
    {
        AdditionalRaceData ard = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDRaceName);
        if (ard == null)
        {
            System.out.println("Additional race: " + strARDRaceName + " not found");
            return;
        }
        loadSLRaceList(statement, strARDRaceName, strRaceSearch, ard.getCourseId(), ard.getCourse(), nStartYear, nEndYear, false);
    }
    public static void loadSLRaceList(ENEStatement statement, String strARDRaceName, String strRaceSearch, String strCourse, int nStartYear, int nEndYear)
    {
        loadSLRaceList(statement, strARDRaceName, strRaceSearch, -1, strCourse, nStartYear, nEndYear, false);
    }
    public static void loadSLRaceList(ENEStatement statement, String strARDRaceName, String strRaceSearch, int nCourse, String strCourse, int nStartYear, int nEndYear, boolean bUpdateOnly)
    {
            if (nCourse < 0)
                nCourse = SportingLifeFactory.getSLCourseId(statement, strCourse);
            String strRaceListURL = sm_strSLRaceListURL.replace("|START_YEAR|", String.valueOf(nStartYear)).replace("|END_YEAR|", String.valueOf(nEndYear));
            strRaceListURL=strRaceListURL + nCourse + "&racename=" + strRaceSearch.replace(" ", "+");
            //strRaceListURL="http://localhost:58080/sporting_life/derby_search.html";
            System.out.println(strRaceListURL);
            TagNode rootNode = ExecuteURL.getRootNode(strRaceListURL, "utf-8");
            if (rootNode == null)
                return;
            TagNode[] aRaces = rootNode.getElementsByAttValue("class", "search-item t2", true, true);
            for(int i = 0; i < aRaces.length; i++)
            {
                TagNode race = aRaces[i];
                TagNode[] aA = race.getElementsByName("a", true);
                if (aA.length > 0)
                {
                    try
                    {
                        // essentials
                        TagNode a = aA[0];
                        String strHRef = a.getAttributeByName("href");
                        String strRaceId;
                        if (strHRef.indexOf("results/") > 0)
                            strRaceId = strHRef.substring(strHRef.indexOf("result/") + 7, strHRef.lastIndexOf("/"));
                        else
                            strRaceId = strHRef.substring(strHRef.indexOf("racecard/") + 9, strHRef.lastIndexOf("/"));

                        System.out.println(strRaceId + ":" + strHRef);
                        String strTitle = race.getElementsByName("h4", true)[0].getText().toString().trim();
                        String astrTitle[] = strTitle.split("\n");
                        String strMeetingDate = astrTitle[0].trim();
                        String strRaceTitle = astrTitle[4].trim();
                        String strDetails = race.getElementsByName("p", true)[0].getText().toString().trim();
                        // to do: use regexp to parse
                        String strGoing = null;
                        String strRaceType = null;
                        String strAgeRange = null;
                        String strClass = "";
                        String strDistance = null;
                        String strNrRunners = null;
                        // (Flat, 4yo+, Class B, 1m 2f, 19 runners, Going Soft, Heavy in places)
                        Matcher m1 = sm_race6a.matcher(strDetails);
                         if (m1.matches())
                         {
                              strRaceType= m1.group(1);
                              strAgeRange = m1.group(2);
                              strClass = m1.group(3);
                              strDistance = m1.group(4);
                              strNrRunners = m1.group(5);
                              strGoing = m1.group(6);
                         }
                         else
                         {
                            // (Flat, 4yo+, Class 1, 1m 2f, 9 runners, Turf) 
                            m1 = sm_race6b.matcher(strDetails);
                            if (m1.matches())
                            {
                              strRaceType= m1.group(1);
                              strAgeRange = m1.group(2);
                              strClass = m1.group(3);
                              strDistance = m1.group(4);
                              strNrRunners = m1.group(5);
                              strGoing = m1.group(6);
                            }
                             else
                            {
                             // (Flat, 4yo+, 1m 2f, 20 runners, Going Good)
                             m1 = sm_race5a.matcher(strDetails);
                             if (m1.matches())
                             {
                              strRaceType= m1.group(1);
                              strAgeRange = m1.group(2);
                              strDistance = m1.group(3);
                              strNrRunners = m1.group(4);
                              strGoing = m1.group(5);
                             }
                            else
                             {
                                 // (Hurdle, 4yo+, Class A, 2m 5f, Going None specified)
                                 m1 = sm_race5b.matcher(strDetails);
                                 if (m1.matches())
                                 {
                                    strRaceType= m1.group(1);
                                    strAgeRange = m1.group(2);
                                    strClass = m1.group(3);
                                    strDistance = m1.group(4);
                                    strGoing = m1.group(5);
                                   }
                                 else
                                 {
                                    // (Flat, 3yo, 7f 209y, 8 runners, Turf)
                                     m1 = sm_race5c.matcher(strDetails);
                                    if (m1.matches())
                                    {
                                    strRaceType= m1.group(1);
                                    strAgeRange = m1.group(2);
                                    strDistance = m1.group(3);
                                     strNrRunners = m1.group(4);
                                    strGoing = m1.group(5);
                                    }
                                 else
                                 {
                                     // (Flat, 4yo+, 1m, Going Soft)
                                    m1 = sm_race4a.matcher(strDetails);
                                    if (m1.matches())
                                    {
                                     strRaceType= m1.group(1);
                                     strAgeRange = m1.group(2);
                                     strDistance = m1.group(3);
                                     strGoing = m1.group(4);
                                    }
                                   else
                                    {
                                        // (4yo+, 1m 2f 96y, Class 1, 6 runners)
                                        m1 = sm_race4b.matcher(strDetails);
                                        if (m1.matches())
                                        {
                                         strAgeRange = m1.group(1);
                                         strDistance = m1.group(2);
                                         strClass = m1.group(3);
                                         strNrRunners = m1.group(4);
                                        }
                                        else
                                        {
                                            // (Chase, 5yo+, 3m 110y, 8 runners)
                                            m1 = sm_race4c.matcher(strDetails);
                                            if (m1.matches())
                                            {
                                             strRaceType= m1.group(1);
                                             strAgeRange = m1.group(2);
                                             strDistance = m1.group(3);
                                             strNrRunners = m1.group(4);
                                            }
                                            else
                                            {
                                                // (Flat, 1m 2f 96y, 8 runners, Going Good, Turf) NEW!
                                            m1 = sm_race4d.matcher(strDetails);
                                            if (m1.matches())
                                            {
                                             strRaceType= m1.group(1);
                                             strDistance = m1.group(2);
                                             strNrRunners = m1.group(3);
                                             strGoing = m1.group(4);
                                            }
                                            else
                                            {

                                                System.out.println("Invalid details: " + strDetails);
                                            }
                                            }
                                        }
                                        }
                                 }
                                 }
                                 }
                             }
                         }

                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                        Date parsedDate = formatter.parse(strMeetingDate);
                        int nRace = Integer.parseInt(strRaceId);
                        AdditionalRace arace = new AdditionalRace("SL", nRace, strARDRaceName, strRaceTitle, strCourse, parsedDate,
                            strRaceType, strAgeRange, 
                            strClass, 
                            (strNrRunners == null) ? 0 : Integer.parseInt(strNrRunners), 
                            strGoing);
                        arace.setDistance(strDistance);
                        String strWinningTime = null;
                        
                        // SE 20160609
                        // Always prefer RP results so just use this to find winner name, but fon't write to database
                        Calendar cal = new GregorianCalendar();
                        cal.setTime(arace.getMeetingDate());
                        int nYear = cal.get(Calendar.YEAR);
                        if (false && !bUpdateOnly)
                           strWinningTime = loadSLRacecard(statement, nRace, strHRef.replace("/results/", "/racecards/").replace("/result/", "/racecard/"));

                        strWinningTime = loadSLResult(statement, nRace, nYear, strHRef.replace("/racecards/", "/results/").replace("/racecard/", "/result/"));
                          
                        if (strWinningTime != null)
                            arace.setWinningTime(strWinningTime);
                        //System.out.println(arace.toString());
                        
                        // SE 20160609
                        // Always prefer RP results so just use this to find winner name, but fon't write to database
                    /*     AdditionalRacesFactory.insertAdditionalRaceInstance(statement, strARDRaceName, arace, !bUpdateOnly);
                        AdditionalRunnersFactory.updateDistanceWon(statement, nRace); */
                    }
                    catch(Exception e)
                    {
                        System.out.println("loadSLRaceList Exception: " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
              }
           }
          
    }
    public static String loadSLRacecard(ENEStatement statement, int nRace, String strRaceURL)
    {
        strRaceURL = sm_strSLBaseURL + strRaceURL;
        try
        {
            //strRaceURL="http://localhost:58080/sporting_life/" + nRace + ".html";
            TagNode rootNode = ExecuteURL.getRootNode(strRaceURL, "utf-8");
        
           // PART ONE - Racecard
            HashMap<Integer, AdditionalRunner> hmRunners = null;
            TagNode[] aRacecards = rootNode.getElementsByAttValue("id", "racecard", true, true);
            if (aRacecards.length > 0)
            {
                hmRunners =  parseSLRacecardRunners(aRacecards[0], nRace);
            
                if ((hmRunners.isEmpty()) && (aRacecards.length > 1))
                {
                        // try again with Non Runners!
                        System.out.println("No runners, trying non runners: " + nRace);
                        hmRunners =  parseSLRacecardRunners(aRacecards[1], nRace);
                }
                if (hmRunners.isEmpty())
                {
                    System.out.println("No runners found: " + nRace);
                    return null;
                }
            }
            // PART TWO - Result
            String strWinningTime = loadSLRacecardResult(rootNode, nRace, hmRunners);

            AdditionalRacesFactory.insertAdditionalRunners(statement, hmRunners.values());
            
            return strWinningTime;
        }
        catch(Exception e)
        {
            System.out.println("LoadSLRace: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    static public HashMap<Integer, AdditionalRunner> parseSLRacecardRunners(TagNode racecard, int nRace)
    {
            HashMap<Integer, AdditionalRunner> hmRunners = new HashMap<Integer, AdditionalRunner>();
                TagNode[] aRows = racecard.getElementsByName("tr", true);
                if (aRows.length > 1)
                {
                    for(int j= 1; j < aRows.length; j++)  // ignore 1st row which contains th elements
                    {
                        try
                        {
                            TagNode row = aRows[j];
                            TagNode[] aCells = row.getElementsByName("td", true);
                            if (aCells.length == 10)
                            {
                                    String strCell0 = aCells[0].getText().toString().trim();
                                    String[] astrCell0 = strCell0.split("\n");
                                    String strClothNumber = astrCell0[0].trim();
                                    int nClothNumber = Integer.parseInt(strClothNumber);
                                    TagNode cell2 = aCells[2];
                                    String strFullName = cell2.getText().toString().trim().split("\n")[0].trim();    // split into name and bred
                                    AdditionalRunner runner = new AdditionalRunner("SL", nRace, nClothNumber, strFullName);
                                    if (astrCell0.length > 1)
                                    {
                                        String strStallNumber = astrCell0[1].replace("(", "").replace(")", "").trim();
                                        runner.setStallNumber(Integer.parseInt(strStallNumber));
                                    }
                                    String strDaysSinceRan=null;
                                    TagNode[] aNote = cell2.getElementsByAttValue("class", "note", true, true);
                                    // if there is one note then it is Days Since Ran, if there are 2 then first is ex9, second is days
                                    if (aNote.length == 2)
                                    {
                                        String strPenaltyWeight = aNote[0].getText().toString();
                                        if (strPenaltyWeight.indexOf("ex") > 0)
                                        {
                                            strPenaltyWeight = strPenaltyWeight.replace(")", "").replace("(ex", "");
                                            runner.setPenaltyWeight(Integer.valueOf(strPenaltyWeight));
                                        }
                                        strDaysSinceRan = aNote[1].getText().toString();
                                    }
                                    else if (aNote.length == 1)
                                        strDaysSinceRan = aNote[0].getText().toString();

                                    TagNode a = cell2.getElementsByName("a", true)[0];
                                    String strOwnerDescription = a.getAttributeByName("title");
                                    //System.out.println(strOwnerDescription);
                                    String strColour = null;
                                    String strGender = null;
                                    String strSire = null;
                                    String strDam = null;
                                    String strOwner = null;
                                    int nOwner = strOwnerDescription.indexOf("Owned by");
                                    if (nOwner ==  0)
                                    {
                                        strOwner = strOwnerDescription.substring(8).trim();
                                        runner.setOwner(strOwner);
                                    }
                                    else
                                    {
                                        if (nOwner > 0)
                                        {
                                            strOwner = strOwnerDescription.substring(nOwner + 8).trim();
                                            runner.setOwner(strOwner);
                                            strOwnerDescription = strOwnerDescription.substring(0, nOwner);
                                        }
                                        if (strOwnerDescription.indexOf("out of") > 0)
                                        {
                                            Matcher m1 = sm_breedingDam.matcher(strOwnerDescription);
                                            if (m1.matches())
                                            {
                                                 strColour= m1.group(1);
                                                 runner.setColour(strColour);
                                                 strGender = m1.group(2);
                                                 runner.setGender(strGender);
                                                 strSire = m1.group(3);
                                                 runner.setSireName(strSire);
                                                 strDam = m1.group(4);
                                                 runner.setDamName(strDam);
                                            }
                                        }
                                        else
                                        {
                                            Matcher m1 = sm_breeding.matcher(strOwnerDescription);
                                            if (m1.matches())
                                            {
                                                 strColour= m1.group(1);
                                                 runner.setColour(strColour);
                                                 strGender = m1.group(2);
                                                 runner.setGender(strGender);
                                                 strSire = m1.group(3);
                                                 runner.setSireName(strSire);
                                          }
                                        }
                                    }
                                    if ((strOwner == null) || "".equals(strOwner))
                                        System.out.println("No owner found: " + strOwnerDescription);
                                    String strWeightPounds = aCells[4].getText().toString().trim().split(" ")[0].trim();
                                    try
                                    {
                                        runner.setWeightPounds(Integer.parseInt(strWeightPounds));
                                    }
                                    catch(NumberFormatException e)
                                    {
                                    }
                                     String strTrainer = aCells[5].getText().toString().trim();
                                    runner.setTrainer(strTrainer);
                                    String strJockey = aCells[6].getText().toString().trim();
                                    runner.setJockey(strJockey);
                                    String strForecastSP = aCells[9].getText().toString().trim();
                                    runner.setForecastSP(strForecastSP.trim());
                                    TagNode[] aData = row.getElementsByAttValue("class", "mobile-hdn", true, true);
                                    TagNode cell4 = aCells[4];
                                    String strTack="";
                                    TagNode[] aTack = cell4.getElementsByAttValue("class", "note", true, true);
                                    if (aTack.length > 0)
                                        strTack = aTack[0].getText().toString();
                                    else
                                    {
                                        String strWeightData = aData[2].getText().toString().replace("\n", "").replaceAll(" +", " ");
                                        String[] astrWD = strWeightData.split(" ");
                                        if (astrWD.length >= 4)
                                            strTack = astrWD[3].trim();
                                    }
                                    runner.setTack(new SmartformTack(strTack));
                                    runner.setFormFigures(aData[0].getText().toString());
                                    String strAge = aData[1].getText().toString();
                                    if(!"".equals(strAge))
                                    {
                                        try
                                        {
                                            runner.setAge(Integer.parseInt(strAge));
                                        }
                                        catch(NumberFormatException e)
                                        {
                                            
                                        }
                                    }
                                    if ((strDaysSinceRan != null) && (!"".equals(strDaysSinceRan)))
                                    {
                                        try
                                        {
                                            runner.setDaysSinceRan(Integer.parseInt(strDaysSinceRan));
                                        }
                                        catch(NumberFormatException e)
                                        {
                                            
                                        }
                                    }
                                    //System.out.println(runner.toString());
                                    hmRunners.put(nClothNumber, runner);
                            }
                          }
                           catch(Exception e)
                            {
                               System.out.println("loadSLRacecard runner: " + j + "-" + e.getMessage()); 
                                e.printStackTrace();
                            }
            }
            
        }
        return hmRunners;
}
    public static String loadSLRacecardResult(TagNode rootNode, int nRace, HashMap<Integer, AdditionalRunner> hmRunners)
    {
        String strWinningTime = null;
        try
        {
            TagNode[] aResults = rootNode.getElementsByAttValue("id", "racecard-result", true, true);
            if (aResults.length > 0)
            {
                TagNode result = aResults[0];
                TagNode[] aLIs = result.getElementsByName("li", true);
                if (aLIs.length > 0)
                {
                    strWinningTime = aLIs[0].getText().toString().trim();
                    if (strWinningTime.length() > 14)
                        strWinningTime = strWinningTime.substring(14);    // remove " Winning time: " from front
                    else
                    {
                        System.out.println("Invalid winning time: " + strWinningTime);
                        strWinningTime = null;
                    }
                }
                else
                    System.out.println("No Winning Time found");
                TagNode[] aTables = result.getElementsByName("table", true);
                if (aTables.length > 0)
                {
                    TagNode table = aTables[0];
                    TagNode[] aRows = table.getElementsByName("tr", true);
                    if (aRows.length > 1)
                    {
                        for(int j= 1; j < aRows.length; j++)
                        {
                            TagNode row = aRows[j];
                            TagNode[] aCells = row.getElementsByName("td", true);
                            if (aCells.length == 4)
                            {
                                String strPosition = ExecuteURL.getTagNodeArrayString(aCells, 0);  // to: remove st, nr, rd etc - meanwhile use j
                                String strNumberName = ExecuteURL.getTagNodeArrayString(aCells, 1);
                                String strDistanceBeaten = ExecuteURL.getTagNodeArrayString(aCells, 2);
                                String strSP = ExecuteURL.getTagNodeArrayString(aCells, 3);
                                String strClothNumber = strNumberName.substring(0, strNumberName.indexOf(" "));
                                int nClothNumber = Integer.parseInt(strClothNumber);
                                AdditionalRunner runner = hmRunners.get(nClothNumber);
                                String strName = strNumberName.substring(strNumberName.indexOf(" ")).trim();
                                runner.setHistoricData(j, strDistanceBeaten, strSP);
                            }
                        }
                    }
                }
            }
            return strWinningTime;
        }
        catch(Exception e)
        {
            System.out.println("loadSLResult: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    public static String loadSLResult(ENEStatement statement, int nRace, int nYear, String strRaceURL)
    {
        strRaceURL = sm_strSLBaseURL + strRaceURL;
        try
        {
            //strRaceURL="http://localhost:58080/sporting_life/result_" + nRace + ".html";
            TagNode rootNode = ExecuteURL.getRootNode(strRaceURL, "utf-8");
        
            TagNode[] aTables = rootNode.getElementsByName("table", true);
            if (aTables.length > 0)
            {
                parseSLResultRunners(statement, aTables[0], nRace, nYear);
            
            }
            // PART TWO - Result
            String strWinningTime = getResultWinningTime(rootNode);
            
            return strWinningTime;
        }
        catch(Exception e)
        {
            System.out.println("LoadSLRace: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    private static int parseSLResultRunners(ENEStatement statement, TagNode results, int nRace, int nYear)
    {
        int nCount = 0;
        TagNode[] aRows = results.getElementsByName("tr", true);
        if (aRows.length > 1)
        {
            for(int i = 1; i < aRows.length; i = i + 1)
            {
                // each runner covers 2 rows
                TagNode row1 = aRows[i];
                TagNode[] aCells1 = row1.getElementsByName("td", true);
                String strCell0 = aCells1[0].getText().toString().replaceAll("\n", "").trim();
                if (strCell0.indexOf("(") > 0)
                    strCell0 = strCell0.substring(0, strCell0.indexOf("(") - 1).trim();
                if ("NR".equals(strCell0) || "WD".equals(strCell0))
                    break;      // no need to process remaining lines after first NR
                
                String strCell2 = aCells1[2].getText().toString().replaceAll("\n", "").replaceAll("\r", "").trim().replaceAll(" +", " ");
                int nSpace = strCell2.indexOf(" ");
                String strClothNumber = strCell2.substring(0, nSpace);
                String strName = strCell2.substring(nSpace); 
                String strSP = aCells1[7].getText().toString().replaceAll("\n", "").trim();
                String strDistanceBeaten = aCells1[1].getText().toString().trim();                        
                
                //System.out.println("Position: " + strCell0  + " Name: " + strName + " Cloth Number: " + strClothNumber + " SP: " + strSP);
                String strBetting = "";
                String strDetails = "";
                if ( ((i + 1) < aRows.length) && ("note".equals(aRows[i+1].getAttributeByName("class"))) )
                {
                    TagNode row2 = aRows[i+1];
                    TagNode[] aCells2 = row2.getElementsByName("td", true);
                    if (aCells2.length > 0)     // one cell
                    {
                        TagNode cell = aCells2[0];
                        strDetails = cell.getText().toString().replaceAll("&#163;", "£").replaceAll("\n", " ").trim();
                        TagNode[] aStrong = cell.getElementsByName("strong", true);
                        if (aStrong.length > 0)
                            strDetails=strDetails.replace(aStrong[0].getText().toString(), "");     // remove SP from detail text
                        int nBetting = strDetails.indexOf(" opened ");
                        if (nBetting > 0)
                        {
                            strBetting = strDetails.substring(nBetting + 1);
                            strDetails = strDetails.substring(0, nBetting);
                        }
                        else
                        {
                           int nPounds = strDetails.indexOf("£");
                           if (nPounds > 0)
                           {
                                strBetting = strDetails.substring(nPounds);
                                strDetails = strDetails.substring(0, nPounds);
                           }
                        }
                        //System.out.println("In running: " + strDetails + " Betting: " + strBetting);
                        i = i + 1;
                    }
                }

                AdditionalRunner runner = new AdditionalRunner("SL", nRace, Integer.valueOf(strClothNumber), strName);
                runner.setFinishPosition(strCell0);
                runner.setBetting(strBetting);
                runner.setInRunning(strDetails);
                runner.setStartingPrice(strSP);
                if ((!"1st".equals(strCell0)) && !"".equals(strDistanceBeaten))
                    runner.setDistanceBeaten(strDistanceBeaten);
                // SE 20160609
                // Always prefer RP results so just use this to find winner name, but fon't write to database
                //AdditionalRacesFactory.updateAdditionalRunnerResult(statement, runner);
                if ("1st".equals(strCell0))
                        System.out.println(nYear + " " + runner.getName());
                nCount++;
            }
        }
        
        return nCount;
        
    }

    private static String getResultWinningTime(TagNode rootNode)
    {
        String strWinningTime = null;
        TagNode[] aStatus = rootNode.getElementsByAttValue("class", "racecard-status", true, true);
        if (aStatus.length > 0)
        {
            TagNode[] aULs = aStatus[0].getElementsByName("ul", true);
            if (aULs.length > 0)
            {
                TagNode[] aLIs = aULs[0].getElementsByName("li", true);
                if (aLIs.length > 0)
                    strWinningTime = aLIs[0].getText().toString().replace("Winning time: ", "").trim();
            }
        }
        return strWinningTime;
    }
    public static void loadData(ENEStatement statement)
    {
        // http://www.sportinglife.com/racing/results/27-12-1999/kempton-park/result/161133/pertemps-king-george-vi-chase-showcase-race
        // http://www.sportinglife.com/racing/results/02-10-2011/longchamp/result/463154/prix-de-la-foret-group-1 All France
        //loadSLResult(statement, 44724, "");
        //loadSLRaceList(statement, new SportingLifeSearch("Cheltenham", "Cathcart", 1994, 1994));
         //loadSLRaceList(statement, "King George", "king+george", "Sandown", 1995, 1996);
       //updateSLRaceList(statement, new SportingLifeSearch("Sandown", "king+george", 1995, 1996));
        //loadSLRaceList(statement, new SportingLifeSearch("Haydock", "henry+viii", 2000, 2000));
   /*    ArrayList<SportingLifeSearch> searches = WikipediaFactory.createSportingLifeSearch(statement, "ard_country = 'France'");
        Iterator<SportingLifeSearch> iter = searches.iterator();
        while(iter.hasNext())
        {
            SportingLifeSearch search = iter.next();
            updateSLRaceList(statement, search);
        } */  
        //ArrayList<AdditionalRaceData> alRaces = WikipediaFactory.createAdditionalRaceDataList(statement, "ard_country='England' and ard_race_type in ('Hurdle', 'Chase') and ard_group_race = 1", "ard_race_type, ard_name");
 /*    ArrayList<AdditionalRaceData> alRaces = WikipediaFactory.createAdditionalRaceDataList(statement, "ard_country='France' and ard_race_type in ('Hurdle', 'Chase')", "ard_name");
        Iterator<AdditionalRaceData> iter = alRaces.iterator();
        while(iter.hasNext())
        {
            AdditionalRaceData race = iter.next();
            loadSLRaceList(statement, race.getName(), race.getName().toLowerCase().replace(" la ", " ").replace("prix ", "").replace(" du ", " ").replace(" de ", " ").replace(" le ", " ").replace("d'", "").replace(" ", "+"), 1994, 2014);
        } 
   */    
       // French Races    Kings Stand
              //updateSLRaceList(statement, new SportingLifeSearch("Ascot", "prince+wales", 1994, 2002));
              //updateSLRaceList(statement, new SportingLifeSearch("Longchamp", "essai+poulains", 2005, 2014));

        //SportingLifeRacecards.loadSLRaceList(statement, "Grand Steeple-Chase de Paris", "steeple+paris", 1994, 2014);
        SportingLifeRacecards.loadSLRaceList(statement, "Prix Ferdinand Dufaure", "ferd+duf", 1994, 2014); 
        //SportingLifeRacecards.loadSLRaceList(statement, "Tercentenary", "new+stakes", 1994, 2002); 
  /*      SportingLifeRacecards.loadSLRaceList(statement, "Phoenix", "waterford+phoenix", "leopardstown", 1994, 2002);
        SportingLifeRacecards.loadSLRaceList(statement, "Phoenix", "heinz+phoenix", "leopardstown", 1994, 2002); */
        //SportingLifeRacecards.loadSLRaceList(statement, "Matron", "matron", "curragh", 1994, 2002);
        //SportingLifeRacecards.loadSLRaceList(statement, "National Stakes", "national+stakes", 1994, 1998);
       //SportingLifeRacecards.loadSLRaceList(statement, "Irish 2000 Guineas", "2,000+guineas", 1994, 2002); 
 /*       SportingLifeRacecards.loadSLRaceList(statement, "Arc de Triomphe", "arc+de+triomphe", 2005, 2014);
        SportingLifeRacecards.loadSLRaceList(statement, "Abbaye", "abbaye", 2005, 2014);
        SportingLifeRacecards.loadSLRaceList(statement, "Cadran", "cadran", 2005, 2014);
       SportingLifeRacecards.loadSLRaceList(statement, "Prix Rothschild", "rothschild", 2012, 2012);  */
     
        
      //SportingLifeRacecards.loadSLRaceList(statement, "Dominican Republic", "martell+handicap+hurdle", 2002, 2002);
      //loadSLRaceList(statement, "Dominican Republic", "barton+guestier", 1994, 1998);
     //SportingLifeRacecards.loadSLRaceList(statement, "Clarence House", "victor+chandler+handicap+chase", 54, "Warwick", 1994, 1994);    
     //SportingLifeRacecards.loadSLRaceList(statement, "Clarence House", "victor+chandler+handicap+chase", 27, "Kempton", 1997, 1997);    
     //SportingLifeRacecards.loadSLRaceList(statement, "Clarence House", "victor+chandler+handicap+chase", 27, "Kempton", 1999, 1999);    
     //SportingLifeRacecards.loadSLRaceList(statement, "Clarence House", "victor+chandler+handicap+chase", 1995, 1995);    
 /*     SportingLifeRacecards.loadSLRaceList(statement, "Ryanair Chase", "cathcart", 1994, 2002);     */
      //SportingLifeRacecards.loadSLRaceList(statement, "Betfred Bowl Chase", "martell+cup+chase", 1994, 2002);   // 
      //SportingLifeRacecards.loadSLRaceList(statement, "Ascot Chase", "comet+chase", 1994, 1994);   // 101138
 /*    SportingLifeRacecards.loadSLRaceList(statement, "Tolworth", "tolworth", 54, "Warwick", 1994, 2002); 
     SportingLifeRacecards.loadSLRaceList(statement, "Tolworth", "tolworth", 1, "Ascot", 1994, 2002); 
     SportingLifeRacecards.loadSLRaceList(statement, "Tolworth", "tolworth", 56, "Wincanton", 1994, 2002); 
     SportingLifeRacecards.loadSLRaceList(statement, "Tolworth", "tolworth", 1994, 2002); */
     //SportingLifeRacecards.loadSLRaceList(statement, "Henry VII Novices", "henry+viii", 22, "Haydock", 2000, 2000); 
       
/*    SportingLifeRacecards.loadSLRaceList(statement, "King George", "king+george", 45, "Sandown", 1995, 1995); 
     SportingLifeRacecards.loadSLRaceList(statement, "Henry VIII", "henry+vii", 22, "Haydock", 2000, 2000); 
     SportingLifeRacecards.loadSLRaceList(statement, "Challow", "challow", 10, "Cheltenham", 1994, 2002); 
     SportingLifeRacecards.loadSLRaceList(statement, "Challow", "challow", 1994, 2000); 
     SportingLifeRacecards.loadSLRaceList(statement, "RSA Chase", "alliance+chase", 1994, 2002);    
     SportingLifeRacecards.loadSLRaceList(statement, "Celebration Chase", "championship+chase", 2001, 2001);        // starts 2001
     SportingLifeRacecards.loadSLRaceList(statement, "World Hurdle", "stayers+hurdle", 1994, 2002);    
     SportingLifeRacecards.loadSLRaceList(statement, "Christmas Hurdle", "christmas+hurdle", 1994, 1996);   
     SportingLifeRacecards.loadSLRaceList(statement, "Aintree Stayers Hurdle", "long+distance", 1, "Ascot", 1994, 2002);  */
     
     //SportingLifeRacecards.loadSLRaceList(statement, "King George VI And Queen Elizabeth", "king+george+vi", 1994, 1997);
/*     SportingLifeRacecards.loadSLRaceList(statement, "Sussex", "sussex+stakes", 1994, 1995);
     SportingLifeRacecards.loadSLRaceList(statement, "Fillies Mile", "fillies+mile", 1, "Ascot", 2000, 2002);
     SportingLifeRacecards.loadSLRaceList(statement, "Champion Stakes", "champion+stakes", 36, "Newmarket", 2000, 2002); */
     // Fillies Mile + Champion Stakes
     //SportingLifeRacecards.loadSLRaceList(statement, "EBF Novice Final", "handicap+final", 1994, 2002);    // since 1998
     
 
/*       SportingLifeRacecards.loadSLRaceList(statement, "Diamond Jubilee", "cork+orrery", 1994, 2001);
       SportingLifeRacecards.loadSLRaceList(statement, "Diamond Jubilee", "golden+jubilee", 2002, 2002);
       SportingLifeRacecards.loadSLRaceList(statement, "Haydock Sprint Cup", "sprint+cup", 1998, 2002);
       SportingLifeRacecards.loadSLRaceList(statement, "Kings Stand", "king+stand", 1994, 2002);
       SportingLifeRacecards.loadSLRaceList(statement, "Ascot Gold Cup", "gold+cup", 1994, 1997);  */
/*     SportingLifeRacecards.loadSLRaceList(statement, "Victoria Cup", "victoria+cup", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Sandringham", "fern+hill", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Silver Bowl", "silver+bowl", 1994, 1994);
      SportingLifeRacecards.loadSLRaceList(statement, "Ascot Stakes", "ascot+stakes", 1994, 1995);
       SportingLifeRacecards.loadSLRaceList(statement, "Old Newton", "old+newton", 1994, 1994);
      SportingLifeRacecards.loadSLRaceList(statement, "Dick Poole", "dick+poole", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Windsor Castle", "windsor+castle", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Ascot Stakes", "ascot+stakes", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Britannia", "britannia", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Sandringham", "sandringham", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Zetland Gold Cup", "zetland+gold", 1994, 2002);
      SportingLifeRacecards.loadSLRaceList(statement, "Silver Bowl", "silver+bowl", 1994, 2002);
       SportingLifeRacecards.loadSLRaceList(statement, "Old Newton", "old+newton", 1994, 2002); */
/*       SportingLifeRacecards.loadSLRaceList(statement, "Grand National", "grand+national", 1994, 2002);
       SportingLifeRacecards.loadSLRaceList(statement, "2000 Guineas", "2000+guineas", 1994, 2002);
        SportingLifeRacecards.loadSLRaceList(statement, "1000 Guineas", "1000+guineas", 1994, 2002);
        SportingLifeRacecards.loadSLRaceList(statement, "St Leger", "teleconnection+st+leger", 1994, 1994);
       SportingLifeRacecards.loadSLRaceList(statement, "St Leger", "pertemps+st+leger", 1995, 1998);
         SportingLifeRacecards.loadSLRaceList(statement, "St Leger", "rothmans+st+leger", 1999, 2002);
       SportingLifeRacecards.loadSLRaceList(statement, "Epsom Oaks", "oaks", 1994, 2002);
        SportingLifeRacecards.loadSLRaceList(statement, "Royal Hunt Cup", "royal+hunt+cup", 1994, 2002);
        SportingLifeRacecards.loadSLRaceList(statement, "Wokingham", "wokingham", 1994, 2002);
        SportingLifeRacecards.loadSLRaceList(statement, "Derby", "derby", 1994, 1994);
        SportingLifeRacecards.loadSLRaceList(statement, "Derby", "vodafone+derby", 1994, 2002);
        SportingLifeRacecards.loadSLRaceList(statement, "Eclipse", "coral+eclipse+stakes", 1994, 2002); */
        
    }
}
