/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web;

import ene.eneform.colours.bos.HorseOwnerQuery;
import ene.eneform.colours.database.AdditionalRacesFactory;
import ene.eneform.colours.database.WikipediaFactory;
import ene.eneform.smartform.bos.AdditionalRunner;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import ene.eneform.utils.JSONUtils;
import org.apache.commons.httpclient.methods.PostMethod;
import org.htmlcleaner.TagNode;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Simon
 */
public class TimeformResults {
    
    // 70= Deauville, 80=Longchamp
    private static String sm_TimeformResultsURL="https://www.timeform.com/Racing/Results/Race?MeetingDate=$MEETING_DATE$&CourseId=$COURSE_ID$&RaceNumber=$RACE_NUMBER$";
    // MeetingDate=05-13-2007&CourseId=80&RaceNumber=4
    public static int loadTimeformResult(ENEStatement statement, int nRace, String strMeetingDate, int nCourseId)
    {
        // don't know race number, so keep trying until percent returned is high enough
        for(int i = 1; i <= 10; i++)
        {
            int nPercent = loadTimeformResult(statement, nRace, strMeetingDate, nCourseId, i);
            if (nPercent < -1)      // error
                return 0;
            else if (nPercent > 50)
            {
                System.out.println("Race number: " + i);
                return i;
            }
        }
        return 0;
    }
    public static int loadTimeformResult(ENEStatement statement, int nRace, String strMeetingDate, int nCourseId, int nRaceNumber)
    {
        int nPercent = -1;
        String strRaceURL = sm_TimeformResultsURL.replace("$MEETING_DATE$", strMeetingDate).replace("$COURSE_ID$", String.valueOf(nCourseId)).replace("$RACE_NUMBER$", String.valueOf(nRaceNumber));
        try
        {
            System.out.println(strRaceURL);
            TagNode rootNode = ExecuteURL.getRootNode(strRaceURL, "utf-8");
        
            TagNode[] aTables = rootNode.getElementsByAttValue("class", "fw tblCustom", true, true);
            if (aTables.length > 0)
            {
                nPercent = parseTimeformResultRunners(statement, aTables[0], nRace);
            
            }
        }
        catch(Exception e)
        {
            System.out.println("LoadTimeformRace: " + e.getMessage());
            e.printStackTrace();
        }
        
        return nPercent;
    }
   private static int parseTimeformResultRunners(ENEStatement statement, TagNode results, int nRace)
    {
        int nCount = 0;
        int nPercent = 0;
        TagNode[] aRows = results.getElementsByName("tr", true);
        if (aRows.length > 1)
        {
            TagNode rowHeader = aRows[0];
            HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(rowHeader, "td");
            for(int i = 1; i < aRows.length; i = i + 1)
            {
                // each runner covers 2 rows
                TagNode row = aRows[i];
                TagNode[] aCells = row.getElementsByName("td", true);
                // Drw, Age, Wgt, Eq, Trainer, Jockey, OR (handicaps only)
                String strPosition = aCells[hmColumns.get("Pos")].getText().toString().trim();
                String strDistanceBeaten = aCells[hmColumns.get("Btn")].getText().toString().trim();
                String strName = aCells[hmColumns.get("Horse")].getText().toString().replace("&#39;", "'").trim();
                String strSP = aCells[hmColumns.get("ISP")].getText().toString().trim();
                System.out.println("Position: " + strPosition + " Beaten: " + strDistanceBeaten + " Name: " + strName + " SP: " + strSP);

                AdditionalRunner runner = new AdditionalRunner("SL", nRace, strName);   // race was create from SL
                runner.setFinishPosition(strPosition);
                runner.setStartingPrice(strSP);
                if ((!"1".equals(strPosition)) && !"".equals(strDistanceBeaten))
                    runner.setDistanceBeaten(strDistanceBeaten);
                nCount += AdditionalRacesFactory.updateAdditionalRunnerResult(statement, runner);
             }
            nPercent = (nCount * 100)/(aRows.length - 1);
        }
        
        return nPercent; 
    }
   
//   https://www.timeform.com/racing/home/searchhorses?SearchText=high%20chaparral
public static String getHorseId(String strHorse, String strBred)
{
    String strURL = "https://www.timeform.com/racing/home/searchhorses";
    HashMap<String,String> hmParameters = new HashMap<String,String>();
    hmParameters.put("SearchText", strHorse);       // don't include bred in initial search
    PostMethod postMethod = ExecuteURL.createPostMethod(strURL, hmParameters);
    String strContent = ExecuteURL.executePostMethodString(postMethod);
    
    
    if ((strBred != null) && !"".equals(strBred))
        strHorse = strHorse.trim() + " (" + strBred + ")";
    try {
        JSONObject jsonData = JSONUtils.parseJSONStringObject(strContent);
       List alHorses = (List)jsonData.get("root");
    if (alHorses.size() == 1)
    {
        Map horse = (Map)alHorses.get(0);
        return (String) horse.get("horse_code");
    }
    else
    {
        for(int i = 0; i < alHorses.size(); i++)
        {
            Map horse = (Map)alHorses.get(i);
            String strName = (String) horse.get("horse_name");
            System.out.println(i + ": " + strName);
            if (strHorse.equalsIgnoreCase(strName))
                return (String) horse.get("horse_code");
        }
    }
    }
    catch(IOException e) {

    }
    return null;
}

public static String getOwnerName(String strHorseId) throws IOException
{
    String strURL = "https://www.timeform.com/Racing/Ledger/HorseLedger?HorseId=" + strHorseId;
    TagNode node = ExecuteURL.getRootNode(strURL, "utf-8");
    TagNode[] aTDs= node.getElementsByName("td", true);
    for(int i = 0; i < aTDs.length; i++)
    {
        TagNode td = aTDs[i];
        if ("Owner".equals(td.getText().toString().trim()))
            return aTDs[i+1].getText().toString().trim().replace("&amp;", "&");
    }
    return null;
}
public static void retrieveMissingResults(ENEStatement statement, String strWhere)
{
    ArrayList<TimeformResultQuery> alQueries = getMissingResults(statement, strWhere);
    for(int i = 0; i < alQueries.size(); i++)
    {
        TimeformResultQuery trq = alQueries.get(i);
        loadTimeformResult(statement, trq.getRace(), trq.getMeetingDate(), trq.getCourse());
    }
}
public static ArrayList<TimeformResultQuery> getMissingResults(ENEStatement statement, String strWhere)
{
    ArrayList<TimeformResultQuery> alQueries = new ArrayList<TimeformResultQuery> ();
    
    String strQuery = "select a.race_id, day(meeting_date) as day, month(meeting_date) as month, year(meeting_date) as year, tf_course_id";
    strQuery += " from ((((";
    strQuery += " additional_race_data inner join additional_race_link on ard_name=arl_name and arl_source != 'SF')";
    strQuery += " inner join additional_races a on a.race_id=arl_race_id and arl_source=ara_source)";
    strQuery += " left outer join additional_runners u on a.race_id=u.race_id and arl_source=aru_source and finish_position <=3)";
    strQuery += " left outer join timeform_course on tf_course_name=course)";
    if (!"".equals(strWhere))
         strQuery += (" where " + strWhere);
    strQuery += " group by a.race_id, race_name, meeting_date, course, tf_course_id";
    strQuery += " having count(*) < 3";
    try
    {
        ResultSet rs = statement.executeQuery(strQuery);
        while (rs.next())
        {
          int nRace = rs.getInt(1);
         int nDay = rs.getInt(2);
         int nMonth = rs.getInt(3);
         int nYear = rs.getInt(4);
          int nCourseId = rs.getInt(5);
          String strMeetingDate=String.format("%02d-%02d-%d", nMonth, nDay, nYear);
          alQueries.add(new TimeformResultQuery(nRace, nCourseId, strMeetingDate));
        }
        rs.close();
    }
    catch(SQLException e)
    {
        e.printStackTrace();
    }

    return alQueries;
}
// Use RacingPost version in preference, as can also pass date to determine owner AT THE TIME
private static void updateMissingOwners(ENEStatement statement, String strWhere)
{
    ArrayList<HorseOwnerQuery> alQueries = WikipediaFactory.getMissingOwners(statement, strWhere);
    for(int i = 0; i < alQueries.size(); i++)
    {
        HorseOwnerQuery thq = alQueries.get(i);
        String strName = thq.getName();
        String strBred = thq.getBred();
        String strHorseId = getHorseId(strName, strBred);
        if (strHorseId != null)
        {
            try
            {
                String strOwner = getOwnerName(strHorseId);
                if (strOwner != null)
                {
                    System.out.println("Owner: " + strOwner + "-" + strName + "-" + strBred);
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

public static class TimeformResultQuery
{
    private int m_nRace;
    private int m_nCourse;
    private String m_strMeetingDate;

     
    public TimeformResultQuery(int nRace, int nCourse, String strMeetingDate)
    {
        m_nRace = nRace;
        m_nCourse = nCourse;
        m_strMeetingDate = strMeetingDate;
    }
           public int getRace() {
            return m_nRace;
        }

        public int getCourse() {
            return m_nCourse;
        }

        public String getMeetingDate() {
            return m_strMeetingDate;
        }

}

}
