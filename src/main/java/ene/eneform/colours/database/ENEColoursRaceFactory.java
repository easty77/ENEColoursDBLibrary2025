/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.database;

import ene.eneform.smartform.bos.SmartformRace;
import ene.eneform.smartform.factory.SmartformRaceFactory;
import ene.eneform.utils.ENEStatement;

import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public class ENEColoursRaceFactory {

public static ArrayList<SmartformRace> getSmartformMeetingRaces(ENEStatement statement, int nMeeting)
{
    ArrayList<SmartformRace> alRaces = null;
        alRaces = SmartformRaceFactory.createSmartformDailyRaces(statement, nMeeting);
         return alRaces;
    }
public static ArrayList<SmartformRace> getSmartformMeetingHistoricRaces(ENEStatement statement, int nMeeting)
{
    ArrayList<SmartformRace> alRaces = null;
        alRaces = SmartformRaceFactory.createSmartformDailyRaces(statement, nMeeting, true);
         return alRaces;
    }

public static ArrayList<SmartformRace> getSmartformCourseNameClassRaces(ENEStatement statement, String strCourse, String strName, String strClass, int nStart, int nEnd)
{
    ArrayList<SmartformRace> alRaces = null;
        alRaces = SmartformRaceFactory.createSmartformCourseNameClassRaces(statement, strCourse, strName, strClass, nStart, nEnd);
         return alRaces;
    }
public static ArrayList<SmartformRace> getSponsorRaces(ENEStatement statement, String strCourse, int nYear, String strSponsor)
{

    ArrayList<SmartformRace> alRaces = null;
        alRaces = SmartformRaceFactory.createSponsorRaces(statement, strCourse, nYear, strSponsor);
        return alRaces;
}
public static SmartformRace getRace(ENEStatement statement, int nRace)
{
    SmartformRace race = null;
    Statement stmt = null;
        race = SmartformRaceFactory.createSmartformRace(statement, nRace, "SF");
         return race;
}
/*
public static  List<Integer> generateRacecard(ENEStatement statement, String strLanguage, SmartformRacecardDefinition racecard)
{
    List<Integer> alRaces = SmartformRaceFactory.getRaceIds(statement, racecard);

    {
        String strDefaultTitle = SmartformRaceFactory.getRacecardTitle(statement, racecard);    
        if (strDefaultTitle != null)
            racecard.setTitle(strDefaultTitle);
    }
        
    return generateRacecard(statement, strLanguage, alRaces, racecard.getType(), racecard.getTitle(), racecard.getDayDiff());
}
public static List<Integer> generateRacecard(ENEStatement statement, String strLanguage, List<Integer> alRaces, String strType, String strTitle, int nDayDiff)
{
    if (alRaces.size() > 0)
    {
        ENEColoursFactory.processSVGRaceSequence(statement, strLanguage, alRaces, strType);
 
        WebsiteURLFactory.insertWebsiteURL(statement, "racecard", strType, strTitle, "/images/colours/svg/racecards/" + strType + "/r" + alRaces.get(0), nDayDiff);
    }
    
     return alRaces;
}
*/
}
