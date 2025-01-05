/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.database;

import ene.eneform.colours.bos.*;
import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.parse.ENEColoursParser;
import ene.eneform.smartform.bos.SmartformRace;
import ene.eneform.smartform.factory.SmartformRaceFactory;
import ene.eneform.utils.ENEStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public class ENEColoursFactory {
    
     public static ArrayList<String> getJockeyColoursList(ENEStatement statement, int nStart, int nEnd, int nMax)
    {
        String strLimit = "";
        String strQuery = "select distinct jockey_colours from daily_races inner join daily_runners using (race_id) where meeting_date>=date_sub(current_date, interval " + nStart + " day) and meeting_date<date_sub(current_date, interval " + nEnd + " day)";
        String strWhere = ""; // " and (jockey_colours like '% and %' or jockey_colours like '%&%')";
        if (nMax > 0)
            strLimit = " limit " + nMax;
        String strOrder = "  order by 1";
        ArrayList<String> lst = ENEColoursFactory.getStringList(statement, strQuery + strWhere + strOrder + strLimit);

        return lst;
    }
    public static SmartformRace getSmartformRace(ENEStatement statement, int nRace)
    {
        SmartformRace race = null;
 
        race = SmartformRaceFactory.createSmartformRace(statement, nRace, "SF");
        
        return race;
    }

    public static ArrayList<ENEColoursBO> getColoursBODailyRaceList(ENEStatement statement, int nRace)
    {
        String strLimit = "";
        String strQuery = "select name, jockey_colours, trainer_name, jockey_name, owner_name, cloth_number from daily_runners";
        String strWhere = " where race_id = " + nRace;
        String strOrder = " order by cloth_number";
        ArrayList<ENEColoursBO> lst = new ArrayList<ENEColoursBO>();

        ResultSet rs  = statement.executeQuery(strQuery + strWhere + strOrder + strLimit);
        if (rs != null)
        {
            try
            {
                while (rs.next())
                 {
                     lst.add(new ENEColoursBO(String.valueOf(rs.getInt("cloth_number")),
                             rs.getString("name"),
                             rs.getString("jockey_colours"),
                             rs.getString("trainer_name"),
                             rs.getString("jockey_name"),
                             rs.getString("owner_name")
                             )
                             );
                 }
                rs.close();
            }
            catch(SQLException e)
            {
            }
     }

        return lst;
    }

    public static ArrayList<ENEColoursRunner> getENEColoursRaceRunnerList(ENEStatement statement, int nRace)
    {
        String strQuery = "select runner_id, jockey_colours from daily_runners where race_id =" + nRace;

        return getRunnerList(statement, strQuery);
    }
    public static ArrayList<ENEColoursRunner> getENEColoursRunnerList(ENEStatement statement, int nStart, int nEnd, int nMax)
    {
        String strLimit = "";
        String strQuery = "select distinct runner_id, jockey_colours, owner_name from daily_races inner join daily_runners using (race_id) where meeting_date>=date_sub(current_date, interval " + nStart + " day) and meeting_date<date_sub(current_date, interval " + nEnd + " day)";
        String strWhere = ""; // " and (jockey_colours like '% and %' or jockey_colours like '%&%')";
        if (nMax > 0)
            strLimit = " limit " + nMax;
        String strOrder = "  order by race_id asc";

        return getRunnerList(statement, strQuery + strWhere + strLimit + strOrder);
    }
    public static ArrayList<ENEColoursRunner> getRunnerList(ENEStatement statement, String strQuery)
    {
        ArrayList<ENEColoursRunner> lst = new ArrayList<ENEColoursRunner>();


         ResultSet rs  = statement.executeQuery(strQuery);
         if (rs != null)
         {
        try
        {
            while (rs.next())
            {
                // ignore owner's name for now, as will include in image
                lst.add(new ENEColoursRunner(rs.getInt("runner_id"), rs.getString("jockey_colours")));
            }
            rs.close();
        }
        catch(SQLException e)
        {
        }
         }
    
        return lst;
    }

    public static ArrayList<String> getStringList(ENEStatement statement, String strQuery)
    {
        ArrayList<String> lst = new ArrayList<String>();

        ResultSet rs  = statement.executeQuery(strQuery);
        if (rs != null)
        {
            try
            {
            
                while (rs.next())
                {
                    lst.add(rs.getString(1));
                }
                rs.close();
            }
            catch(SQLException e)
            {
            }
        }

        return lst;
    }
    public static ENERacingColours createENEColours(String strLanguage, String strDescription, String strOwner)
    {
        ENEColoursParser parser = new ENEColoursParser(strLanguage, strDescription, strOwner);
        return parser.parse();
    }


    public static ArrayList<String> getRacingColours(ENEStatement statement)
    {
        String strLimit = ""; // " limit 100";
        String strQuery = "select distinct owner_colours from owner_colours";
        String strWhere = ""; // " where owner_colours = \"red\"";
        String strOrder = " order by owner_colours";

       ArrayList<String> lst = new ArrayList<String>();


         ResultSet rs  = statement.executeQuery(strQuery + strWhere + strOrder + strLimit);
         if (rs != null)
         {
            try
            {
               while (rs.next())
                {
                    lst.add(rs.getString(1));
                }
               rs.close();
            }
            catch(SQLException e)
            {
            }
         }
        return lst;
    }
 
    public static ENECalendarMonth getCalendarDates(ENEStatement statement, String strCountry, int nYear, int nMonth)
    {
        String strQuery = "select day(meeting_date), case when bh_date is not null then 1 else 0 end, m.meeting_id, meeting_type, course, evening_flag, race_title, grade, previous_year_id";
        strQuery += " from ((meetings m left outer join bank_holidays b on bh_date=m.meeting_date and m.meeting_year=bh_year and m.country=b.country)";
        strQuery += " left outer join meeting_races r on m.country = r.country and m.meeting_year = r.meeting_year and m.meeting_id = r.meeting_id)";
        String strWhere = " where m.country = '" + strCountry + "' and m.meeting_year = " + nYear + " and month(meeting_date) = " + nMonth;
        String strOrder = " order by meeting_date, course, race_title";

        ENECalendarMonth month = new ENECalendarMonth(strCountry, nYear, nMonth);

        ResultSet rs  = statement.executeQuery(strQuery + strWhere + strOrder);
        int nDayOfMonthPrevious = -1;
        int nPreviousMeeting = 0;
        ENECalendarDate caldate = null;
        ENECalendarMeeting calmeet = null;
        if (rs != null)
        {
            try
            {
                while (rs.next())
                {
                    int nDayOfMonth = rs.getInt(1);
                    int nBankHoliday = rs.getInt(2);
                    int nMeeting = rs.getInt(3);
                    String strMeetingType = rs.getString(4);
                    String strCourse = rs.getString(5);
                    int nEvening = rs.getInt(6);

                    if (nDayOfMonthPrevious != nDayOfMonth)
                    {
                        caldate = new ENECalendarDate(strCountry, nYear, nDayOfMonth, nBankHoliday == 1);
                        month.addDate(caldate);
                        nDayOfMonthPrevious = nDayOfMonth;
                    }

                    if (nMeeting != nPreviousMeeting)
                    {
                        calmeet = new ENECalendarMeeting(strCourse, strMeetingType, nEvening == 1);
                        caldate.addMeeting(calmeet);
                        nPreviousMeeting = nMeeting;
                    }

                    String strRaceTitle = rs.getString(7);
                    if (!rs.wasNull())
                    {
                        String strGrade = rs.getString(8);
                        int nPreviousRace = rs.getInt(9);
                        ENECalendarRace race = new ENECalendarRace(strRaceTitle, strGrade, nPreviousRace);
                        calmeet.addRace(race);
                    }
                }
                rs.close();
            }
            catch(SQLException e)
            {
                System.out.println("EXCEPTION getCalendarDates :" + e.getMessage());
                        
            }
        }

        return month;
    }

 


/*
    public static  void processSVGMeeting(ENEStatement statement, String strLanguage, int nMeeting)
    {
        ArrayList<SmartformRace> alRaces = ENEColoursRaceFactory.getSmartformMeetingRaces(statement, nMeeting);
        List lstRaces = new ArrayList();
        Iterator<SmartformRace> iter = alRaces.iterator();
        while (iter.hasNext())
        {
            lstRaces.add(iter.next().getRaceId());
        }
        iter = alRaces.iterator();  // reset iterator
        int nCount = 0;
        while (iter.hasNext())
        {
            SmartformRace race = iter.next();
            if (race != null)
            {
                processSVGRace(statement, strLanguage, race, lstRaces, "r", nCount++);
            }
        }
    }

    public static void processSVGRaceSequence(ENEStatement statement, String strLanguage, SmartformRacecardDefinition dctl, String strPrefix)
    {
        // convert to list of race ids
        List<Integer> alRaces = SmartformRaceFactory.getRaceIds(statement, dctl);
        
        processSVGRaceSequence(statement, strLanguage, alRaces, strPrefix);
    }
    public static void processSVGRaceSequence(ENEStatement statement, String strLanguage, List<Integer> lstRaces, String strPrefix)
    {
            Iterator<Integer> iter = lstRaces.iterator();

            
            int nCount = 0;
            while (iter.hasNext())
            {
                int nRace = iter.next().intValue();
                SmartformRace race = ENEColoursFactory.getSmartformRace(statement, nRace);
                if (race != null)
                {
                    processSVGRace(statement, strLanguage, race, lstRaces, strPrefix, nCount++);
                }
            }
     }
    private static void processSVGRace(ENEStatement statement, String strLanguage, SmartformRace race, List lstRaces, String strPrefix, int nPosition)
    {
            if (race !=  null)
            {
                ArrayList<SmartformDailyRunner> alRunners = ENEColoursRunnerFactory.getRacecardRunnerList(statement, race.getRaceId());

                System.out.println("Number of colours: " + race.getRaceId() + "-" + alRunners.size());

                try
                {
                    SVGRaceRunnerFactory.createSVGRaceRunners(strLanguage, race, alRunners, lstRaces, strPrefix, nPosition);
                }
                catch(IOException e)
                {

                }
            }

    } */
}
