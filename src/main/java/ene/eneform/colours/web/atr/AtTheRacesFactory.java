/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.atr;

import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.smartform.bos.SmartformDailyRace;
import ene.eneform.smartform.bos.SmartformDailyRunner;
import ene.eneform.smartform.bos.SmartformRace;
import ene.eneform.smartform.bos.day.SmartformDay;
import ene.eneform.smartform.bos.meeting.SmartformMeeting;
import ene.eneform.smartform.factory.SmartformRaceFactory;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import ene.eneform.utils.ENEStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author simon
 */
public class AtTheRacesFactory 
{
   public static int updateARDYear(ENEStatement statement, int nYear, String strStartDate)
    {
        int nUpdate = 0;
      ArrayList<SmartformRace> aRaces = SmartformRaceFactory.createSmartformARDYearRaces(statement, nYear, strStartDate);
          Iterator<SmartformRace> riter = aRaces.iterator();
          while(riter.hasNext())
          {
              SmartformRace race = riter.next();
             
              // construct ATR URL
              ArrayList<SmartformDailyRunner> aRunners = SmartformRunnerFactory.createDailyRaceRunners(statement, race.getRaceId());
              Iterator<SmartformDailyRunner> runiter = aRunners.iterator();
              while(runiter.hasNext())
              {
                  race.addRunner(runiter.next());
              }
              nUpdate += AtTheRacesRacecards.updateSmartformColours(statement, (SmartformDailyRace) race);
          }
      return nUpdate;
    }
    public static int updateToday(ENEStatement statement, int nDayOffset)
    {
      SmartformDay today = SmartformRaceFactory.createSmartformToday(statement, nDayOffset);
      Iterator<SmartformMeeting> miter = today.getMeetingIterator();
      int nUpdate = 0;
      while(miter.hasNext())
      {
          SmartformMeeting meeting = miter.next();
          String strCourse = meeting.getCourse();
          if ("Royal_Ascot".equals(strCourse))
              strCourse="Ascot";
          String strATRCourse = ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(strCourse, "").getATRName();
          Iterator<SmartformRace> riter = meeting.getRaceIterator();
          while(riter.hasNext())
          {
              SmartformRace race = riter.next();
             
              // construct ATR URL
              nUpdate += AtTheRacesRacecards.updateSmartformColours(statement, (SmartformDailyRace) race);
          }
      } 
      return nUpdate;
    }
   public static int updateDate(ENEStatement statement, Date raceDate)
    {
      SmartformDay today = SmartformRaceFactory.createSmartformDayByDate(statement, raceDate);
      Iterator<SmartformMeeting> miter = today.getMeetingIterator();
      int nUpdate = 0;
      while(miter.hasNext())
      {
          SmartformMeeting meeting = miter.next();
          String strATRCourse = ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(meeting.getCourse(), "").getATRName();
          Iterator<SmartformRace> riter = meeting.getRaceIterator();
          while(riter.hasNext())
          {
              SmartformRace race = riter.next();
             
              // construct ATR URL
              nUpdate += AtTheRacesRacecards.updateSmartformColours(statement, (SmartformDailyRace) race);
          }
      } 
      return nUpdate;
    }
   public static void updateDateRange(ENEStatement statement, String strStartDate, String strEndDate)
   {
       try
       {
        DateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calStart = Calendar.getInstance();
        calStart.setTime(dtFormat.parse(strStartDate));
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dtFormat.parse(strEndDate));

        while(calStart.before(calEnd))
        {
            //System.out.println(dtFormat.format(calStart.getTime()));
            AtTheRacesFactory.updateDate(statement, calStart.getTime());
            calStart.add(Calendar.DAY_OF_MONTH, 1);
        }
       }
       catch(ParseException e)
       {
           System.out.println("ParseException: " + e.getMessage());
       }
   }
    public static int updateRace(ENEStatement statement, int nRace)
    {
       SmartformRace race = SmartformRaceFactory.createSmartformRace(statement, nRace, "SF");
           
       // construct ATR URL
       int nUpdate = AtTheRacesRacecards.updateSmartformColours(statement, (SmartformDailyRace) race);
       return nUpdate;
    }
    public static AtTheRacesSlot getRaceSlot(ENEStatement statement, long lRaceId)
    {
        AtTheRacesSlot slot = null;
        String strQuery= "select DATE_FORMAT(meeting_date, \"%e-%M-%Y\"), time_format(scheduled_time, \"%H%i\"),  replace(course, \"_\" , \"-\") from daily_races where race_id=" + lRaceId;
        ArrayList<SmartformMeeting> list = new ArrayList<SmartformMeeting>();

        try
        {
             ResultSet rs = statement.executeQuery(strQuery);
             if (rs != null)
             {
                while (rs.next())
                {
                    slot  = new AtTheRacesSlot(rs.getString(1), rs.getString(2), rs.getString(3));

                }
                rs.close();
             }
             else
             {
                 System.out.println("AtTheRacesSlot null resultset: " + strQuery);
             }
        }
        catch(SQLException e)
        {
        }

    return slot;
    }
}
