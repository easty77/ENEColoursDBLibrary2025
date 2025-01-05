/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.bos.HorseOwnerQuery;
import ene.eneform.colours.bos.WinnerRaceQuery;
import ene.eneform.colours.database.AdditionalRaceLinkFactory;
import ene.eneform.colours.database.AdditionalRacesFactory;
import ene.eneform.colours.database.WikipediaFactory;
import ene.eneform.colours.web.atr.AtTheRacesRacecards;
import ene.eneform.colours.wikipedia.WikipediaQuery;
import ene.eneform.smartform.bos.AdditionalRace;
import ene.eneform.smartform.bos.AdditionalRunner;
import ene.eneform.smartform.bos.SmartformRace;
import ene.eneform.smartform.bos.SmartformTack;
import ene.eneform.smartform.factory.SmartformRaceFactory;
import ene.eneform.utils.ArrayUtils;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.BaseToken;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Simon
 */
public class RacingPostRacecards {
    
    private static String sm_strRacecardURL="http://www.racingpost.com/horses2/cards/home.sd?r_date=";
    // /results/257/santa-anita/2016-11-05/662271
 private static Pattern sm_patternDetails = Pattern.compile("([\\w\\s\\d\\.,]+) \\(([\\w\\s\\d/\\.,]+)\\)");
 private static Pattern sm_raceDetails = Pattern.compile("\\n[\\s]*[\\(]*([\\w\\s\\d]*)[\\)]*[\\s]*\\n[\\s]*[\\(]*([\\w\\s\\+\\-,]*)[\\)]*[\\s]*\\n[\\s]*([\\w\\d]+)[\\s]*([\\w\\s]*)"); // (Class) (Age Range) Distance Going
 private static Pattern sm_raceDetails2 = Pattern.compile("\\n[\\s]*[\\(]*([\\w\\s\\d]*)[\\)]*[\\s]*\\n[\\s]*[\\(]*([\\w\\s\\+\\-]*)\\,([\\w\\s\\+\\-]*)[\\)]*[\\s]*[\\(]*([\\w\\d]+)[\\)]*[\\s\\n]*[\\w\\d&;]+\\s([\\w\\s]*)"); // (Class) (Rating Range, Age Range) (Distance) Distance Going
 private static Pattern sm_raceDetails3 = Pattern.compile("\\n[\\s]*[\\(]*([\\w\\s\\d]*)[\\)]*[\\s]*\\n[\\s]*[\\(]*([\\w\\s\\+\\-]*)[\\)]*[\\s]*[\\(]*([\\w\\d]+)[\\)]*[\\s\\n]*[\\w\\d&;]+\\s([\\w\\s]*)"); // (Class) (Age Range) (Distance) Distance Going
 private static Pattern sm_raceDistance = Pattern.compile("\\(*(\\d+m\\s*)?(\\d+f\\s*)?(\\d+y)ds?\\)*");
private static Pattern sm_raceLink = Pattern.compile("race_id=(\\d+)");


public static void loadRacingPostDailyHorses(ENEStatement statement, String strARDName)
{
    ArrayList<String> alHorses = new ArrayList<String>();
    String strQuery="select name, bred, a.race_id, DATE_FORMAT(meeting_date,'%d %b %Y'), year(meeting_date) as year from daily_races a, additional_race_link, daily_runners u where arl_name='" + strARDName + "' and arl_source='SF' and arl_race_id=a.race_id and a.race_id=u.race_id";
    strQuery += " and year(meeting_date) =2017";
    ResultSet rs = statement.executeQuery(strQuery);
    if (rs != null)
    {
        try
        {
            while (rs.next())   // 1 record
            {
                alHorses.add(rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getInt(5) + "|" + rs.getInt(3) +  "|" + rs.getString(4));
            }
            rs.close();
        }
        catch(SQLException e)
        {
            System.out.println("SQLException: " + e.getMessage());
        }
         loadRacingPostHorses(statement, "SF", alHorses, true);
    } 
}
public static void loadRacingPostHorses(ENEStatement statement, String strARDName, String strSource, boolean bRaces)
{
    ArrayList<String> alHorses = new ArrayList<String>();
    String strQuery="";
    AdditionalRaceData ard = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDName);
    
    if ("SF".equals(strSource))
        strQuery="select name, bred, a.race_id, DATE_FORMAT(meeting_date,'%d %b %Y'), year(meeting_date) as year from historic_races a, additional_race_link, historic_runners u where arl_name='" + strARDName + "' and arl_source='SF' and arl_race_id=a.race_id and a.race_id=u.race_id";
    else
    {
        strQuery = "select name, bred, a.race_id, DATE_FORMAT(meeting_date,'%d %b %Y'), year(meeting_date) as year from additional_races a, additional_race_link, additional_runners u where arl_name='" + strARDName + "' and arl_source = ara_source and ara_source=aru_source and arl_race_id=a.race_id and a.race_id=u.race_id and year(meeting_date) >= 1989";
        String strCountry = ard.getCountry();
        if (("England".equals(strCountry) || "Scotland".equals(strCountry) || "Wales".equals(strCountry) || "Eire".equals(strCountry)) || "Northern ireland".equals(strCountry))
             strQuery += " and year(meeting_date) <= 2002";
    }
    //strQuery += " and year(meeting_date) <= 2002 and year(meeting_date) >= 1989";
    //strQuery += " and (coalesce(amended_position, finish_position) > 3 or unfinished is not null)";
    //strQuery += " and (owner_name='' or owner_name is null)";
    //strQuery += " and (dam_sire_name='' or dam_sire_name is null)";
    //strQuery += " and name in ('Hurricane Floyd')";
    strQuery += " order by name";
    ResultSet rs = statement.executeQuery(strQuery);
    if (rs != null)
    {
        try
        {
            while (rs.next())   // 1 record
            {
                alHorses.add(rs.getString(1) + "|" + rs.getString(2) + "|" + rs.getInt(5) + "|" + rs.getInt(3) +  "|" + rs.getString(4));
            }
            rs.close();
        }
        catch(SQLException e)
        {
            System.out.println("SQLException: " + e.getMessage());
        }
        loadRacingPostHorses(statement, strSource, alHorses, bRaces);
    }
}
private static void loadRacingPostHorses(ENEStatement statement, String strSource, ArrayList<String> alHorses, boolean bRaces)
{
        for(int i = 0; i < alHorses.size(); i++)
        {
            String strHorse = alHorses.get(i);
            String[] aHorses = strHorse.split("\\|");
            String strBred = "";
            String strName = aHorses[0];
            String strMeetingDate = null;
            int nRace = 0;
            int nYear = 0;
            if (aHorses.length > 1)
                strBred = aHorses[1];
            if (aHorses.length > 2)
            {
                try
                {
                     nYear = Integer.parseInt(aHorses[2]);
                }
                catch(NumberFormatException e)
                {
                    
                }
            }
            if (aHorses.length > 3)
            {
                try
                {
                     nRace = Integer.parseInt(aHorses[3]);
                }
                catch(NumberFormatException e)
                {
                    
                }
            }
            if (aHorses.length > 4)
            {
                strMeetingDate = aHorses[4];
            }               
            try
             {
                 System.out.println("Processing: " + strName + "-" + strBred);
                 RacingPostHorse horse = RacingPostHorse.getHorse(strName, strBred, nYear);
                 if (horse != null)
                 {
                    if (bRaces)
                        RacingPostRacecards.insertHorseRaces(statement, strName, horse.getID(), nYear, false, false, true);
                    else
                    {
                       String strUpdate = horse.updateHorseDetails(strName, strSource, nRace, strMeetingDate);
                       statement.executeUpdate(strUpdate);
                       //System.out.println(strUpdate);
                    }
                 }
                 else
                     System.out.println("Horse not found: " + strName + "-" + strBred);
             }
             catch(IOException e)
             {
                 System.out.println("IOException: " + strName + "-" + e.getMessage());
             }
             catch(Exception e)
             {
                 System.out.println("Exception: " + strName + "-" + e.getMessage());
                 e.printStackTrace();
             }
        }
}
public static void loadRacingPostHorses(ENEStatement statement, String[] astrHorses, boolean bRaces)
{
        for(int i = 0; i < astrHorses.length; i++)
        {
            String strHorse = astrHorses[i];
            String[] aHorses = strHorse.split("\\|");
            String strName = aHorses[0];
            long nHorseId = 0;
            if (aHorses.length > 1)
            {
                try
                {
                    nHorseId = Integer.parseInt(aHorses[1]);
                    RacingPostHorse horse = new RacingPostHorse(strName, nHorseId);
                    if (horse != null)
                    {
                       if (bRaces)
                           RacingPostRacecards.insertHorseRaces(statement, strName, horse.getID(), 0, false, false, true);
                       else
                       {
                          String strUpdate = horse.updateHorseDetails(strName, "", 0, null);
                          System.out.println(strUpdate);
                       }
                    }
                }
                catch(IOException e)
                {
                    System.out.println("IOException: " + strName + "-" + e.getMessage());
                }
                catch(NumberFormatException e)
                {
                    System.out.println("IOException: " + strName + "-" + e.getMessage());
                }
                catch(InterruptedException e)
                {
                    System.out.println("InterruptedException: " + strName + "-" + e.getMessage());
                }
            }
        }
}
   public static void loadRacingPostRacesByWinner(ENEStatement statement, ArrayList<AdditionalRaceData> lstARD, String strLanguage, int nStartYear, int nEndYear, RacingPostCourse course) throws IOException, ParseException, InterruptedException
   {
    for(int i = 0; i < lstARD.size(); i++)
    {
        AdditionalRaceData ard = lstARD.get(i);
        System.out.println(ard.getName());
       List<RacingPostRaceSummary> lstSummaries = getWikipediaRaceSummaries(statement, strLanguage, ard, nStartYear, nEndYear, course);
        System.out.println(ard.getName() + "-" + lstSummaries.size());
       for(int j = 0; j < lstSummaries.size(); j++)
       {
           insertFullRaceResult(statement, lstSummaries.get(j), ard, true, false, true);
       }
    }
   }
   public static void loadRacingPostRacesByWinner(ENEStatement statement, AdditionalRaceData ard, String strLanguage, int nStartYear, int nEndYear, RacingPostCourse course) throws IOException, ParseException, InterruptedException
   {
       List<RacingPostRaceSummary> lstSummaries = getWikipediaRaceSummaries(statement, strLanguage, ard, nStartYear, nEndYear, course);
       for(int i = 0; i < lstSummaries.size(); i++)
       {
           insertFullRaceResult(statement, lstSummaries.get(i), ard, true, false, true);
       }
   }
    public static void loadRacingPostRacesByReference(ENEStatement statement, AdditionalRaceData ard, String strLanguage, int nStartYear, int nEndYear)
    {
        ArrayList<RacingPostRaceSummary> alSummaries = WikipediaQuery.getRacingPostReferences(ard, strLanguage, nStartYear, nEndYear);
        Iterator<RacingPostRaceSummary> iter = alSummaries.iterator();
        while(iter.hasNext())
        {
            RacingPostRaceSummary summary = iter.next();
            try
            {
                insertFullRaceResult(statement, summary, ard, true, false, true);
            }
            catch(Exception e)
            {
                System.out.println("loadRacingPostRaces: " + summary.getRaceURL() + "-" + e.getMessage());
            }
        }
    }
   public static void insertHorseRaces(ENEStatement statement, String strHorse, long lHorseID, int nMaxYear, boolean bCheckOnly, boolean bReplace, boolean bATRUpdate) throws IOException, InterruptedException
   {
       System.out.println("insertHorseRaces: " + strHorse);
       RacingPostHorse horse = new RacingPostHorse(strHorse, lHorseID);
       ArrayList<RacingPostHorseRaceSummary> alRaces = horse.getHorseRacesJSON();
 
        // bCheckOnly allows to make sure all courses are in racing_post_course, other wise check on abroad will not perform correctly
        boolean bAbroadOnly = true;
        boolean bSleep = false;
        for(int i= 0; i < alRaces.size(); i++)
        {
            if (bSleep)
            {
                TimeUnit.SECONDS.sleep(5);          // Sleep - too many rapid calls to RP leads to  HTTP response code: 403
                bSleep = false;
            }
            RacingPostHorseRaceSummary race = alRaces.get(i);
            
            // TO REMOVE - only process Martin Foley winners
            //System.out.println(race.getJockey() + "-" + race.getPosition());
            //if (race.getJockey() == null || (!race.getJockey().equals("Marcus Foley")) || race.getPosition() > 1)
            //    continue;
            
            long lRaceId = race.getRaceID();
            RacingPostCourse course = race.getCourse();
            String strCourse = course.getCode();
            if (!course.isValid())
            {
                System.out.println("RacingPostCourse not found:" + strCourse + "-" + race.getRaceID());
                continue;
            }
            int nYear = race.getYear();
            if ((nMaxYear == 0) || (nYear <= nMaxYear))
            {
                if ((nYear >= 2003) && bAbroadOnly)
                {
                    if (("GB".equalsIgnoreCase(course.getCountry()) || "IRE".equalsIgnoreCase(course.getCountry())))
                    {
                        if (bCheckOnly)
                            System.out.println("RacingPostCourse UK course:" + strCourse);
                        continue;
                    }
                    else
                    {
                       if (bCheckOnly)
                            System.out.println("RacingPostCourse Overseas course:" + strCourse);
                    }
                }
                try
                {
                    if (!bCheckOnly)
                    {
                        RacingPostRaceSummary summary = AdditionalRacesFactory.getRacingPostRaceSummary(statement, race.getRaceID());
                        if (bReplace || summary == null)
                        {
                            insertFullRaceResult(statement, race, null, true, bReplace, bATRUpdate);
                            bSleep = true;
                        }
                        else 
                        {
                            System.out.println("Exists: " + summary.getCourse().getName() + "-" + summary.getYear());
                            if (summary.getYear() >= 2005)
                            {
                                int nUpdates = AtTheRacesRacecards.loadRacecard(statement, course.getATRName(), course.getCountry(), summary.getATRDate(), summary.getScheduledTime().replace(":", ""), lRaceId, "RP", null, true);
                            }           
                        }
                    }
                }
                catch(IOException e)
                {
                    System.out.println("getAllHorseRaces IOException: " + lRaceId + "-" + e.getMessage());
                } 
                catch(ParseException e)
                {
                    System.out.println("getAllHorseRaces ParseException: " + lRaceId + "-" + e.getMessage());
                } 
            } 
        }  
        
   } 
   public static String insertFullRaceResult(ENEStatement statement, RacingPostRaceSummary summary, boolean bReplace, boolean bATRUpdate) throws IOException, ParseException, InterruptedException
   {
       String strReturn = insertFullRaceResult(statement, summary, null, true, bReplace, bATRUpdate);
       return strReturn;                
   }
   public static String insertFullRaceResult(ENEStatement statement, RacingPostRaceSummary summary, AdditionalRaceData ard, boolean bRunners, boolean bReplace, boolean bATRUpdate) throws IOException, ParseException, InterruptedException
   {
       long lRaceId = summary.getRaceID();
       if (lRaceId == 0)
       {
           // this is not a real RacingPostSummary
            summary = RacingPostRaceSummary.createSummary(new WinnerRaceQuery(summary.getCourse().getCode(), ((RacingPostHorseRaceSummary) summary).getHorse(),
                                summary.getYear(), summary.getMonth(), summary.getDay(), ""));
       }
       
       if (summary == null)
       {
          System.out.println("Summary not found: " + lRaceId);
           return "";
       }
       lRaceId = summary.getRaceID();
       // return next race id (0 if last race of day, -1 if error)
       TagNode node = summary.retrieveFullRaceResult();
       if (node == null)
       {
           System.out.println("Node not found: " + lRaceId);
           return null;
       }
       TagNode[] aTables = node.getElementsByName("table", true);
       if (aTables.length == 0)
       {
           System.out.println("Table not found: " + lRaceId);
           return null;
       }
       ArrayList<AdditionalRunner> alRunners = new ArrayList<AdditionalRunner>();
       if (bRunners)
       {    
            TagNode runnersTable = aTables[0];
            TagNode[] aRows = runnersTable.getElementsByName("tr", true);
            TagNode headerRow = aRows[0];
            // New column names: Pos., Horse, Jockey, Age, WGT, OR, TS, RPR, MR, blank
            HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(headerRow, "th");
            //hmColumns.put("POSITION", 1);
            //hmColumns.put("DISTANCE_BEATEN", 2);

            TagNode[] aTBody = runnersTable.getElementsByName("tbody", true);    // 4 rows grouped in a tbody for each runner
            // NB - HashMap doesn't cope with dead heat as overwrites
            //HashMap<String, AdditionalRaceRunner> hmRunners = new HashMap<String,AdditionalRaceRunner>();

            alRunners = getRaceDetailsRunners(statement, aTBody[0], lRaceId);
       }
        String strScheduledTime = ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName__time");    ////raceScheduledTime.getText().toString().replace("&laquo;", "").replace("&raquo;", "").replace("\n", "").trim();
        String strDistance = ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName_distance");
        String strDistanceFull = ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName_distanceFull");
        if (strDistanceFull != null)
        {
            strDistance = matchDistance(strDistanceFull.replace("(", "").replace(")", ""));
        }
        String strGoing = ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName_condition"); 
        String strTitle = ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName__title");
        String strCourse = summary.getCourse().getName(); //getRowAttribute(node, "class", "js-popupLink rp-raceTimeCourseName__name");
        String strAgeRange = ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName_ratingBandAndAgesAllowed", "").replace("(", "").replace(")", ""); 
        String strPrizeMoney = ExecuteURL.getRowAttribute(node, "data-test-selector", "text-prizeMoney");
        TagNode raceInfo = ExecuteURL.getRowAttributeNode(node, "class", "rp-raceInfo");
        String strConditions=ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName_distanceDetail", "") + " ";
        strConditions+=ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName_hurdles", "").trim();
       
        String strClass=ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName_class", "");
        String strNextRaceURL=node.getElementsByAttValue("data-test-selector", "link-nextRace", true, true)[0].getAttributeByName("href");
        int nRunners = 0;
        String strWinningTime="";
        String strTotalSP="";
        String strBreeder="";
        int nOwner1=0;
        int nOwner2=0;
        int nOwner3=0;
       TagNode[] raceInfoItems = raceInfo.getElementsByName("li", true);
       for(int i = 0; i < raceInfoItems.length; i++)
       {
           TagNode raceinfoitem = raceInfoItems[i];
           List lstTags = raceinfoitem.getAllChildren();
           for(int j = 0; j < lstTags.size(); j++)
           {
               String strInfo = ExecuteURL.getText((BaseToken)lstTags.get(j)).trim();
               if (strInfo.indexOf(" ran") > 0)
                   nRunners = Integer.parseInt(strInfo.replace(" ran", ""));
               else if ("Winning time:".equals(strInfo))
               {
                   strWinningTime = ExecuteURL.getText((BaseToken)lstTags.get(++j)).trim();
                    if (strWinningTime.indexOf("(") > 0)
                        strWinningTime=strWinningTime.substring(0, strWinningTime.indexOf("(")).trim();
               }
               else if ("Total SP:".equals(strInfo))
                   strTotalSP = ExecuteURL.getText((BaseToken)lstTags.get(++j)).trim();
               else if ("1st ownerXX:".equals(strInfo))
               {
                   // 20181020 - now pulling owner for each horse via image URL
                   nOwner1++;       // to cope with dead-heats
                   String strOwner1 = ExecuteURL.getText((BaseToken)lstTags.get(++j)).trim();
                    if (strOwner1.indexOf("(") == 0)
                        strOwner1 = "";     // just the horse name repeated
                    else if (strOwner1.indexOf("(") > 0)
                    {
                        strOwner1=strOwner1.substring(0, strOwner1.indexOf("("));
                    }
                    if (bRunners)
                        getRunnerByPosition(alRunners, 1, nOwner1).setOwner(strOwner1);
               }
               else if ("1st breeder:".equals(strInfo))
                   strBreeder = ExecuteURL.getText((BaseToken)lstTags.get(++j)).trim();
               else if ("2nd ownerXX:".equals(strInfo))
               {
                   nOwner2++;   // to cope with dead-heats
                   String strOwner2 = ExecuteURL.getText((BaseToken)lstTags.get(++j)).trim();
                    if (strOwner2.indexOf("(") == 0)
                        strOwner2 = "";     // just the horse name repeated
                    else if (strOwner2.indexOf("(") > 0)
                         strOwner2=strOwner2.substring(0, strOwner2.indexOf("("));
                    if (bRunners)
                        getRunnerByPosition(alRunners, 2, nOwner2).setOwner(strOwner2);
               }
               else if ("3rd ownerXX:".equals(strInfo))
               {
                   nOwner3++;   // to cope with dead-heats
                   String strOwner3 = ExecuteURL.getText((BaseToken)lstTags.get(++j)).trim();
                    if (strOwner3.indexOf("(") == 0)
                        strOwner3 = "";     // just the horse name repeated
                    else if (strOwner3.indexOf("(") > 0)
                        strOwner3=strOwner3.substring(0, strOwner3.indexOf("("));
                    if (bRunners)
                        getRunnerByPosition(alRunners, 3, nOwner3).setOwner(strOwner3);
               }
           }
       }

        String strARDName = "";
        String strRaceType = "Flat";  
        if (strTitle.toLowerCase().indexOf("hurdle") >= 0)
            strRaceType = "Hurdle";
        else if (strTitle.toLowerCase().indexOf("chase") >= 0)
            strRaceType = "Chase";
        else if ((strGoing != null) && (strGoing.toLowerCase().indexOf("standard") >=0) || "Fast".equals(strGoing))
            strRaceType = "A_W_Flat";
        else if (ard != null)
        {
            strARDName = ard.getName();
            strRaceType = ard.getRaceType();
            if ("All Weather Flat".equals(strRaceType) && (strGoing.toLowerCase().indexOf("standard") < 0)&& (strGoing.toLowerCase().indexOf("fast") < 0))
                strRaceType = "Flat";
                
        }

        RacingPostCourse course = summary.getCourse();
        String strCountry = course.getCountry();
        String astrWorld[]={"USA", "UAE", "HK", "JPN", "AUS"};
        boolean bEurope = true;
        if (ArrayUtils.contains(astrWorld, strCountry))
           bEurope = false;

        AdditionalRace arace = new AdditionalRace("RP", (int)lRaceId, strARDName, strTitle, strCourse, summary.getRaceTime(), strRaceType, strAgeRange, strClass, nRunners, strGoing);
       int nGroupRace = SmartformRace.getGroupFromTitle(strTitle);
       arace.setGroupRace(nGroupRace);
        arace.setWinningTime(strWinningTime);
       if (strDistance != null && !"".equals(strDistance))
            arace.setDistance(strDistance);  
       arace.setConditions(strConditions);
       //arace.convertGoing2Conditions();
       arace.setScheduledTime(strScheduledTime, false);     // set raw time

       
       AdditionalRacesFactory.insertAdditionalRaceInstance(statement, strARDName, arace, true);
       
       if (bRunners)
       {
            AdditionalRacesFactory.insertAdditionalRunners(statement, alRunners, bReplace);
            Iterator<AdditionalRunner> iter = alRunners.iterator();
            while(iter.hasNext())
            {
                AdditionalRunner arunner = iter.next();
                 AdditionalRacesFactory.updateAdditionalRunnerResult(statement, arunner);
                 arace.addRunner(arunner);
            }
       }
       
       if (ard != null)
       {
           AdditionalRaceLinkFactory.insertAdditionalRaceLink(statement, ard.getName(), "RP", (int) lRaceId);
       }
       if ((summary.getYear() >= 2000) && bRunners)
       {
           strScheduledTime = summary.getScheduledTime();    // take from database as may have been corrected
            int nUpdates = AtTheRacesRacecards.loadRacecard(statement, course.getATRName(), course.getCountry(), summary.getATRDate(), strScheduledTime.replace(":", ""), lRaceId, "RP", arace, bATRUpdate);
            if (nUpdates < 0)   // not found
            {
                 arace.setScheduledTime(strScheduledTime, true);    // adjust to 12 hour clock
                 System.out.println("Trying with 12 hour clock: " + arace.getScheduledTime());
                 nUpdates = AtTheRacesRacecards.loadRacecard(statement, course.getATRName(), course.getCountry(), summary.getATRDate(), arace.getScheduledTime().replace(":", ""), lRaceId, "RP", arace, bATRUpdate);
            }
       }
       
       RacingPostOwnerFactory.loadMissingRPOwners(statement, lRaceId);

       return strNextRaceURL;
   }

private static ArrayList<AdditionalRunner> getRaceDetailsRunners(ENEStatement statement, TagNode tbody, long lRaceId)
{
      AdditionalRunner runner = null;

       ArrayList<AdditionalRunner> alRunners = new ArrayList<AdditionalRunner>();
       TagNode[] aRows = tbody.getElementsByName("tr", true);   
        for (int j = 0; j < aRows.length; j++)
        {
            TagNode row = aRows[j];    // omit row0
            String strRowClass = row.getAttributeByName("class");
            if (strRowClass.indexOf("rp-horseTable__mainRow") >= 0)
            {
                String strHorse = ExecuteURL.getRowAttribute(row, "data-test-selector", "link-horseName");
                String strPosition = ExecuteURL.getRowAttribute(row, "class", "rp-horseTable__pos__number");
                if (strPosition == null)
                    strPosition = ExecuteURL.getRowAttribute(row, "data-test-selector", "text-horsePosition");
                //System.out.println(strHorse + "-" + strPosition);
                int nClothNumber = -1;
                String strClothNumber = ExecuteURL.getRowAttribute(row, "class", "rp-horseTable__saddleClothNo").replace(".", "");
                if (strClothNumber != null && !"".equals(strClothNumber))
                {
                    try
                    {
                        nClothNumber = Integer.parseInt(strClothNumber);
                    }
                    catch(NumberFormatException e)
                    {
                    }
                }
                
                String strStallNumber = ExecuteURL.getRowAttribute(row, "class", "rp-horseTable__pos__draw");
                if (strStallNumber != null)
                    strPosition = strPosition.replace(strStallNumber, "");
                strStallNumber = strStallNumber.replace("&nbsp;", "").replace("(", "").replace(")", "");
                String strBred = ExecuteURL.getRowAttribute(row, "class", "rp-horseTable__horse__country", "").replace("(", "").replace(")", "");
                String strSP = ExecuteURL.getRowAttribute(row, "class", "rp-horseTable__horse__price");
                int nAge = ExecuteURL.getRowAttributeInt(row, "data-test-selector", "horse-age");
                String strJockey = ExecuteURL.getRowAttribute(row, "data-test-selector", "link-jockeyName");
                String strTrainer = ExecuteURL.getRowAttribute(row, "data-test-selector", "link-trainerName");
                String strTack = ExecuteURL.getRowAttribute(row, "class", "rp-horseTable__headGear", "");
                // OW and OH: rp-horseTable__extraData 
                int nStones = ExecuteURL.getRowAttributeInt(row, "data-test-selector", "horse-weight-st");
                int nPounds = ExecuteURL.getRowAttributeInt(row, "data-test-selector", "horse-weight-lb");
                
                TagNode tnOwner = ExecuteURL.getRowAttributeNode(row, "class", "rp-horseTable__silk");
                String strOwnerCode = null;
                String strOwnerName = "";
                String strColours="";

                // to be inserted here
                if (tnOwner != null) {
                    String strColoursImage = tnOwner.getAttributeByName("src");
                    strOwnerCode = strColoursImage.substring(strColoursImage.lastIndexOf("/") + 1).replace(".svg", "");
                }
                //System.out.println(strHorse + ":" + strOwnerCode);
                if (strOwnerCode != null) {
                    RacingPostOwner owner = ENEColoursDBEnvironment.getInstance().getRPOwner(strOwnerCode);

                    if (owner != null) 
                    {
                        strOwnerName = owner.getName();
                        if (owner.getColours() != null) {
                            strColours = owner.getColours();
                        } else {
                            strColours = strOwnerCode;
                        }
                    } 
                    else 
                    {
                        int nOwner = 0;
                        try 
                        {
                            nOwner = Integer.valueOf(strOwnerCode);
                            if (nOwner > 0) 
                            {
                                strOwnerName = RacingPostOwnerFactory.loadRPOwner(statement, nOwner, true);
                                if (strOwnerName == null) 
                                {
                                        strOwnerName = "";
                                        strColours = strOwnerCode;
                                } 
                                else 
                                {
                                        strColours = strOwnerCode;
                                }
                            }
                        } 
                        catch (Exception e) 
                        {
                            System.out.println(strHorse + ":" + e.toString());
                        }
                    }
                }

                // end
                String strDistanceBeaten = "";
                String strDistanceBehindWinner = "";
                TagNode distance = ExecuteURL.getRowAttributeNode(row, "class", "rp-horseTable__pos__length");
                if (distance != null)
                {
                    TagNode[] aDistance = distance.getElementsByName("span", true);
                    if (aDistance.length > 0)
                    {
                        strDistanceBeaten = aDistance[0].getText().toString().trim();
                        if (aDistance.length > 1)
                            strDistanceBehindWinner = aDistance[1].getText().toString().trim();
                    }
                }
                int nOfficialRating = ExecuteURL.getRowAttributeInt(row, "data-ending", "OR");

                runner = new AdditionalRunner("RP", (int) lRaceId, nClothNumber, ("".equals(strBred)) ? strHorse : (strHorse + " " + strBred));
                if (!"".equals(strStallNumber))
                     runner.setStallNumber(Integer.parseInt(strStallNumber));
                runner.setJockey(strJockey);
                runner.setTrainer(strTrainer);
                if (strTack != null)
                    runner.setTack(new SmartformTack(strTack));
                runner.setAge(nAge);
                runner.setWeightPounds((nStones * 14) + nPounds);
                runner.setStartingPrice(strSP);
                runner.setFinishPosition(strPosition);
                runner.setDistanceBeaten(strDistanceBeaten);
                runner.setOwner(strOwnerName);
                runner.setJockeyColours(strColours);
                alRunners.add(runner);
            }
            else if (strRowClass.indexOf("rp-horseTable__pedigreeRow") >= 0)
            {
                TagNode[] aPedigree = row.getElementsByName("a", true);
                String strSire = "";
                String strDam = "";
                String strDamSire = "";
                if (aPedigree.length > 0)
                {
                    strSire = aPedigree[0].getText().toString().trim();
                    if (aPedigree.length > 1)
                    {
                        strDam = aPedigree[1].getText().toString().trim();
                        if (aPedigree.length > 2)
                        {
                            strDamSire = aPedigree[2].getText().toString().trim().replace("(", "").replace(")", "");
                        }
                    }
                }
                String strColGen = ExecuteURL.getTrimmedText(row.getElementsByName("td", true)[0]);
                String[] astrPedigree = strColGen.split(" ");
                String strColour="";
                String strGender="";
                if (astrPedigree.length > 0)
                {
                    strColour = astrPedigree[0].trim();
                    if (astrPedigree.length > 1)
                    {
                        strGender=astrPedigree[1].trim();
                    }
                }
                runner.setColour(strColour);
                runner.setGender(strGender);
                runner.setSireName(strSire);
                runner.setDamName(strDam);
                runner.setDamSireName(strDamSire);
            }
            else if (strRowClass.indexOf("rp-horseTable__commentRow") >= 0)
            {
                String strInRunning = row.getElementsByName("td", true)[0].getText().toString().trim();
                String strBettingText = "";
                Matcher m1 = sm_patternDetails.matcher(strInRunning);
                if (m1.matches())
                {
                    strInRunning = m1.group(1);
                    strBettingText = m1.group(2);
                } 
                runner.setInRunning(strInRunning);
                runner.setBetting(strBettingText);
            }
        }

    return alRunners;
}

    private static SimpleDateFormat sm_dtRace2017 = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sm_dtRace = new SimpleDateFormat("dd MMM yyyy");
   public static void loadRPRacecard(ENEStatement statement, int nInterval) throws IOException
    {
            List<Integer> alRaces = new ArrayList<Integer>();
            String strDate = SmartformRaceFactory.getCurrentDateInterval(statement, nInterval);
            String strRacecardURL=sm_strRacecardURL + strDate;
            TagNode rootNode = ExecuteURL.getRootNode(strRacecardURL, "utf-8");
            
            TagNode[] aBlocks = rootNode.getElementsByAttValue("class", "crBlock", true, true);
            TagNode currentBlock = null;
            for(int i = 0; i < aBlocks.length; i++)
            {
                currentBlock = aBlocks[i];
                TagNode[] aH3 = currentBlock.getElementsByName("h3", true);
                if (aH3.length > 0)
                {
                    String strH3 = aH3[0].getText().toString().replace("\n", "").trim();
                    if ("RACES SHOWN ON TERRESTRIAL TV".equals(strH3))
                        break;
                }
                currentBlock = null;
            }
            if (currentBlock != null)
            {
                // This is the TV Racecard
                TagNode[] aGrid = currentBlock.getElementsByAttValue("class", "cardsGrid", true, true);
                if (aGrid.length > 0)
                {
                    TagNode grid = aGrid[0];
                    TagNode[] aRows = grid.getElementsByName("tr", true);
                    ArrayList<Integer> alRaceIds = new ArrayList<Integer>();
                    for(int j = 0; j < aRows.length; j++)
                    {
                        TagNode row = aRows[j];
                        TagNode[] aTH = row.getElementsByName("th", true);
                        if (aTH.length > 0)
                        {
                            String strTime = aTH[0].getText().toString().replace("\n", "").trim();
                            String[] aTime = strTime.split(":");
                            int nHour = Integer.parseInt(aTime[0]);
                            if (nHour < 11) // RP uses 12 hour clock for races that are generally p.m.
                                nHour += 12;
                            int nMinutes = Integer.parseInt(aTime[1]);
                            TagNode[] aTD = row.getElementsByName("td", true);
                            if (aTD.length > 0)
                            {
                                String strCourse = aTD[0].getText().toString().replace("\n", "").trim();
                                
                                // need to retrieve race_id using Date, Course and Scheduled Time
                                String strScheduledTime = String.format("%s %02d:%02d:00", strDate, nHour, nMinutes);
                                System.out.println("Course: " + strCourse + " Time: " + strScheduledTime);
                                int nRaceId = SmartformRaceFactory.getRPRaceId(statement, strCourse, strScheduledTime);
                                System.out.println("Smartform Race ID=" + nRaceId);
                                if (nRaceId > 0)
                                    alRaceIds.add(nRaceId);
                            }
                        }
                    }
// Commented out when new packages created - 20160408
//                    ENEColoursRaceFactory.generateRacecard(statement, ENEColoursEnvironment.DEFAULT_LANGUAGE, alRaceIds, "tv", "Televised Races for " + strDate, nInterval);
                }
            }
    }
   
public static void updateMissingOwners(ENEStatement statement, String strWhere)
{
    ArrayList<HorseOwnerQuery> alQueries = WikipediaFactory.getMissingOwners(statement, strWhere);
    for(int i = 0; i < alQueries.size(); i++)
    {
        HorseOwnerQuery thq = alQueries.get(i);
        String strName = thq.getName();
        String strBred = thq.getBred();
        long lHorseId = RacingPostHorse.getHorseId(strName, strBred, thq.getRaceDate().get(Calendar.YEAR));
        if (lHorseId > 0)
        {
            try
            {
                RacingPostHorse horse = new RacingPostHorse(strName, lHorseId);
                String strOwner = horse.getOwnerName(thq.getRaceDate());
                if (strOwner != null)
                {
                    System.out.println("Owner: " + strOwner + "-" + strName + "-" + strBred + " on " + thq.getRaceDate().get(Calendar.YEAR) + "-" + thq.getRaceDate().get(Calendar.MONTH));
                    AdditionalRacesFactory.updateAdditionalRunnerEmptyOwner(statement, strName, strBred, strOwner.replaceAll("\\. ", " ").replace("&#39;", "'"));
                }
            }
            catch(IOException e)
            {
                System.out.println("updateMissingOwners Exception " + e.getMessage());
            }
        }
    }
}

   public static List<RacingPostRaceSummary> getWikipediaRaceSummaries(ENEStatement statement, String strLanguage, AdditionalRaceData ard, int nStartYear, int nEndYear, RacingPostCourse course) throws IOException, InterruptedException, ParseException
   {
       // Three possibilities
       // 1. Race is in database
       // 2. Racing Post Reference on Wikipedia
       // 3. Wikipedia Winners table lookup
       String strARDName = ard.getName();
       List<RacingPostRaceSummary> lstSummaries = new ArrayList<RacingPostRaceSummary>();

       // winners names retrieved from Wikipedia
       HashMap<Integer, String> hmWinners = WikipediaQuery.getWikipediaWinners(strLanguage, ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDName));
       
       // summaries retrieve by database query
       HashMap<Integer,RacingPostRaceSummary> hmWinnerSummaries = RacingPostFactory.selectRacingPostReferenceData(statement, strARDName);
       RacingPostCourse defaultCourse = ENEColoursDBEnvironment.getInstance().getRPCourseByName(ard.getCourse(), ard.getRaceType());
       for (int nYear = nStartYear; nYear <= nEndYear; nYear++)
       {
           RacingPostRaceSummary summary = WikipediaQuery.getRacingPostReferenceWinner(statement, ard, nYear, hmWinnerSummaries);
            if (summary == null)
            {
                System.out.println("Missing summary: " + nYear);
                String strWinner = hmWinners.get(nYear);
                if (strWinner != null)
                {
                    String strDate = null;  // don't know preceise date
                    summary = WikipediaQuery.getRacingPostReferenceWinner(ard, nYear, strDate, hmWinners, course);
        /*            if (course != null)
                    {
                        // try with specified course
                        summary = RacingPostRaceSummary.createSummary(new WinnerRaceQuery(course.getCode(), strWinner, nYear, ard.getMonth(), 0, ard.getKeywords()));
                    }
                    if (summary == null)
                    {
                        // still not found, so try with default course
                         summary = RacingPostRaceSummary.createSummary(new WinnerRaceQuery(defaultCourse.getCode(), strWinner, nYear, ard.getMonth(), 0, ard.getKeywords()));
                    }        */
                }
            }
            if (summary != null)
                lstSummaries.add(summary);
       }
       return lstSummaries;
   }
   private static int getFurlongs(String strDistance)
   {
        int nFurlongs = 0;
        Matcher m4 = sm_raceDistance.matcher(strDistance);
        if (m4.matches()) {
            if (m4.group(1) != null) {
                String strMiles = m4.group(1).trim();
                nFurlongs += (8 * Integer.parseInt(strMiles.substring(0, strMiles.length() - 1)));
            }
            if (m4.group(2) != null) {
                String strFurlongs = m4.group(2).trim();
                nFurlongs += Integer.parseInt(strFurlongs.substring(0, strFurlongs.length() - 1));
            }
            // ignore yards
        }
        return nFurlongs;
   }
   private static String matchDistance(String strDistance)
   {
        Matcher m4 = sm_raceDistance.matcher(strDistance);
        if (m4.matches())
        {
            int nEnd = m4.end();
            if (nEnd == strDistance.length())
            {
                // reassemble into full distance string
                strDistance = "";
                if (m4.group(1) != null)
                    strDistance += m4.group(1);
                if (m4.group(2) != null)
                    strDistance += m4.group(2);
                if (m4.group(3) != null)
                    strDistance += m4.group(3);
            }
        }
       return strDistance;
   }
   private static AdditionalRunner getRunnerByPosition(ArrayList<AdditionalRunner> alRunners, int nPosition, int nSubPosition)
   {
       int nSubCount = 0;
       for(int i = 0; i < alRunners.size(); i++)
       {
           AdditionalRunner runner = alRunners.get(i);
           if (runner.getFinishPosition() == nPosition)
           {
               nSubCount++;
               if(nSubCount == nSubPosition)
                   return runner;
           }
       }
       
       return null;
   }
public static String getWikipediaReferences(ENEStatement statement, String strLanguage, AdditionalRaceData ard, int nStartYear, int nEndYear, RacingPostCourse course) throws IOException, ParseException, InterruptedException
   {
       String strWikipediaRef = "";
       List<RacingPostRaceSummary> lstSummaries = getWikipediaRaceSummaries(statement, strLanguage, ard, nStartYear, nEndYear, course);
       if (lstSummaries.size() > 1)
           strWikipediaRef = "==References==\n*[[Racing Post]]:\n**" + strWikipediaRef;
       for(int i = 0; i < lstSummaries.size(); i++)
       {
           if (i > 0)
           {
               if (i%10 == 0)
                    strWikipediaRef += "\n** ";
               else
                   strWikipediaRef += ", ";
           }
           strWikipediaRef += lstSummaries.get(i).getWikipediaReference();
       }
       strWikipediaRef += "\n** ";

       return strWikipediaRef;
   }
}
