/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.bos.WinnerRaceQuery;
import ene.eneform.smartform.bos.SmartformEnvironment;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 * @author Simon
 */
public class RacingPostRaceSummary {

private static Pattern sm_patternRacingPostSummaryURL = Pattern.compile("/results/([\\d]+)/([\\w\\s\\-\\.,]+)/([\\d\\-]+)/([\\d]+)");



        private static SimpleDateFormat sm_sdfRPtime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH);
        private static SimpleDateFormat sm_sdfRP = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        private static SimpleDateFormat sm_sdfATR = new SimpleDateFormat("dd-MMMMM-yyyy", Locale.ENGLISH);

    protected long m_lRaceID = 0;
    protected RacingPostCourse m_course = null;

    protected int m_nFurlongs = 0;
    protected String m_strTitle="";
    protected String m_strGoing="";
    protected Calendar m_calendar=Calendar.getInstance();
    protected int m_nYear = 0;
     public RacingPostRaceSummary(long lRaceId, RacingPostCourse course, String strDate)
    {
        // https://www.racingpost.com/results/192/naas/2017-03-26/671553
        m_lRaceID = lRaceId;
        m_course = course;
        m_calendar.setTime(getDate(strDate));
    }
     public RacingPostRaceSummary(long lRaceId, RacingPostCourse course, Calendar cal)
    {
        // https://www.racingpost.com/results/192/naas/2017-03-26/671553
        m_lRaceID = lRaceId;
        m_course = course;
        m_calendar = cal;
    }
    public RacingPostRaceSummary(String strRaceURL)
    {
        // https://www.racingpost.com/results/513/wolverhampton-aw/1999-03-13/263274"
        int nOffset = 0;
        if (strRaceURL.indexOf("http") == 0)
            nOffset = 2;
        String[] astrRef = strRaceURL.split("\\/");
        m_course = ENEColoursDBEnvironment.getInstance().getRPCourse(astrRef[3+nOffset]);;
        m_lRaceID = Integer.parseInt(astrRef[5+nOffset]);
        m_calendar.setTime(getDate(astrRef[4+nOffset]));
    }
    public static RacingPostRaceSummary createSummary(WinnerRaceQuery query) throws IOException, InterruptedException {
        RacingPostRaceSummary summary = null;
        String strHorse = query.getWinner();
        ArrayList<Long> alHorseIds = RacingPostHorse.getHorseId(strHorse, query.getYear()); // need non-exact match
        for (int i = 0; i < alHorseIds.size(); i++) 
        {
            long lHorseID = alHorseIds.get(i);
            if (lHorseID > 0) 
            {
                RacingPostHorse horse = new RacingPostHorse(strHorse, lHorseID);
                ArrayList<RacingPostHorseRaceSummary> alRaces = horse.getHorseRacesJSON();
                summary = query.getRaceSummary(alRaces);
                if (summary != null) 
                {
                    return summary;
                }
            }
        }
        if (!strHorse.endsWith("I")) 
        {
            System.out.println("Race not found, trying I: " + strHorse + "-" + query.getCourse() + "-" + query.getKeywords().toString());
            query.setWinner(strHorse + " I");
            summary = createSummary(query);
        } 
        else 
        {
            System.out.println("Race not found: " + strHorse + "-" + query.getCourse() + "-" + query.getKeywords().toString());
        }
        
        return summary;
    }
    protected static Date getDate(String strDate)
    {
        Date dt = null;
        if (strDate != null)
        {
            try
            {
                dt = sm_sdfRPtime.parse(strDate);
            }
            catch(ParseException e)
            {
                try
                {
                    dt = sm_sdfRP.parse(strDate);
                }
                catch(ParseException e1)
                {
                    System.out.println("RacingPostRaceSummary: invalid date: " + strDate);
                }
            }
        }
        return dt;
    }
    public static RacingPostRaceSummary createSummaryWikipedia(String strWikipediaRef)
    {
        RacingPostCourse course = null;
        String[] astrRef = strWikipediaRef.split("\\|");
        String strDay="";
        String strRaceID = astrRef[1];
        int nRaceID = Integer.parseInt(strRaceID);
        String strYear = astrRef[2];
        int nYear = Integer.parseInt(strYear);
        String strMonth = astrRef[3];
        strDay = astrRef[4].replace("}}", "");;   
        String strDate = strYear + "-" + strMonth + "-"  + strDay;
        if (astrRef.length == 7)
        {    
            String strCourseNumber = astrRef[5];
            String strCourse = astrRef[6].replace("}}", "");
            course = ENEColoursDBEnvironment.getInstance().getRPCourse(strCourse);
        }
        return new RacingPostRaceSummary(nRaceID, course, strDate);
    }
    public long getRaceID() {
        return m_lRaceID;
    }
    public RacingPostCourse getCourse() {
        return m_course;
    }

    public void setCourse(RacingPostCourse course) {
        m_course = course;
    }

    public String getGoing() {
        return m_strGoing;  // an abbreviated value
    }
    private Calendar getDate() {
        return m_calendar; 
    }
    public int getDay() {
        if (m_calendar != null)
            return m_calendar.get(Calendar.DAY_OF_MONTH) + 1; 
        else
            return 0;
    }
    public int getMonth() {
        if (m_calendar != null)
            return m_calendar.get(Calendar.MONTH) + 1; 
        else
            return 0;
    }
    public int getYear() {
        if (m_calendar != null)
            return m_calendar.get(Calendar.YEAR); 
        else
            return m_nYear;
    }
    public String getRPDate() {
        String strDate = sm_sdfRP.format(m_calendar.getTime()); 
        return strDate;
    }
    public String getATRDate() {
        // 02-October-2016
        return sm_sdfATR.format(m_calendar.getTime());
    }

    public void setGoing(String strGoing) {
        m_strGoing = strGoing;
    }

   public void setYear(int nYear) {
        m_nYear = nYear;
    }

   public void setTitle(String strTitle) {
        m_strTitle = strTitle;
    }
   public void setFurlongs(int nFurlongs) {
       m_nFurlongs = nFurlongs;
    }
    public Date getRaceTime()
    {
       return m_calendar.getTime();
    }

    public int getFurlongs() {
        return m_nFurlongs;
    }

    public String getTitle() {
        return m_strTitle;
    }
    public String getRaceURL() {
        String strDate = sm_sdfRP.format(m_calendar.getTime());
        return ("https://www.racingpost.com" + "/results/" + m_course.getNumber() + "/" + m_course.getCode() + "/" + strDate + "/" + m_lRaceID);
    }
    public boolean checkURL()
    {
        if (m_course == null)
            return false;
        return ExecuteURL.exists(getRaceURL());
    }
    public String getWikipediaReference()
    {
        // format: {{Racing Post|612080|2014|10|16}}
        String strDate = sm_sdfRP.format(m_calendar.getTime());
        String[] aDates = strDate.split("\\-");
        if (m_course != null)
            return String.format("{{Racing Post|%d|%s|%s|%s|%d|%s}}", m_lRaceID, aDates[0], aDates[1], aDates[2], m_course.getNumber(), m_course.getCode());
        else
            return String.format("{{Racing Post|%d|%s|%s|%s}}", m_lRaceID, aDates[0], aDates[1], aDates[2]);    // old style, useless now!
    }
   public TagNode retrieveFullRaceResult() throws IOException
   {
       String strRaceURL = getRaceURL();
        //System.out.println(strRaceURL);
        TagNode root = ExecuteURL.getRootNode(strRaceURL, "utf-8");
        return root;
   }
/*
    public static long getRaceId(WinnerRaceQuery query) throws IOException {
        String strHorse = query.getWinner();
        ArrayList<Long> alHorseIds = RacingPostHorse.getHorseId(strHorse, query.getYear()); // need non-exact match
        for (int i = 0; i < alHorseIds.size(); i++) {
            long lHorseID = alHorseIds.get(i);
            if (lHorseID > 0) 
            {
                RacingPostHorse horse = new RacingPostHorse(strHorse, lHorseID);
                ArrayList<RacingPostHorseRaceSummary> alRaces = horse.getHorseRaces();
                long lRaceId = query.getRaceId(alRaces);
                if (lRaceId > 0) {
                    return lRaceId;
                }
            }
        }
        System.out.println("Race not found: " + strHorse + "-" + query.getCourse() + "-" + query.getKeywords().toString());
        return -1;
    }
*/
    public String parseScheduledTime() throws IOException, ParseException {
        // return next race id (0 if last race of day, -1 if error)
        TagNode node = retrieveFullRaceResult();
        String strScheduledTime = ExecuteURL.getRowAttribute(node, "class", "rp-raceTimeCourseName__time");
        return strScheduledTime;
    }
    public String getScheduledTime()
    {
        if(m_calendar != null)
            return SmartformEnvironment.getInstance().getTimeFormat().format(m_calendar.getTime());
        else
            return "";
    }
    public String setScheduledTime(String strScheduledTime)
    {
        if(m_calendar != null)
            return SmartformEnvironment.getInstance().getTimeFormat().format(m_calendar.getTime());
        else
            return "";
    }

}
