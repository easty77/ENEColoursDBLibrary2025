/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import org.json.JSONArray;
import org.json.JSONObject;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.JSONUtils;
import ene.eneform.utils.Pair;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Simon
 */
public class RacingPostFactory {
/*
    public static HashMap<String, RacingPostCourse> getRPCourses(ENEStatement statement) {
        HashMap<String, RacingPostCourse> hmCourses = new HashMap<String, RacingPostCourse>();
        String strQuery = "select rp_course_code, rp_course_name, rp_country from racing_post_course";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                String strCode = rs.getString("rp_course_code");
                RacingPostCourse course = new RacingPostCourse(strCode, rs.getString("rp_course_name"), rs.getString("rp_country"));
                hmCourses.put(strCode, course);
            }
        } catch (SQLException e) {
        }
        return hmCourses;
    }
*/
    public static void loadRacingPostCourses(ENEStatement statement, HashMap<String, RacingPostCourse> hmCourses, HashMap<String, RacingPostCourse> hmRPCourses, HashMap<String, RacingPostCourse> hmSFCourses) 
    {
        String strQuery = "select rp_course_id, rp_course_code, rp_course_name, rp_country, sf_course_name, atr_course_name from racing_post_course_2017";
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                String strCode = rs.getString("rp_course_code");
                String strName = rs.getString("rp_course_name");
                String strSFName = rs.getString("sf_course_name");
                RacingPostCourse course = new RacingPostCourse(strCode, strName, rs.getString("rp_country"), rs.getInt("rp_course_id"), strSFName, rs.getString("atr_course_name"));
                hmCourses.put(strCode, course);
                hmRPCourses.put(strName, course);
                if (!"".equals(strSFName))
                    hmSFCourses.put(strSFName, course);
            }
            rs.close();
        } 
        catch (SQLException e) {
        }
    }
    public static void loadRacingPostOwners(ENEStatement statement, HashMap<String, RacingPostOwner> hmOwners) 
    {
        String strQuery = "select concat(rp_owner_id, coalesce(rpc_suffix, '')) as owner_id, rp_owner_name, rpc_owner_colours from rp_owners left outer join rp_owner_colours on rp_owner_id=rpc_owner_id";
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                String strCode = rs.getString("owner_id");
                String strName = rs.getString("rp_owner_name");
                String strColours = rs.getString("rpc_owner_colours");
                RacingPostOwner owner = new RacingPostOwner(strCode, strName, strColours);
                hmOwners.put(strCode, owner);
            }
            rs.close();
        } 
        catch (SQLException e) {
        }
    }
    public static List<Integer> getExistingOwnerIds(ENEStatement statement, int nMinOwnerId, int nMaxOwnerId)
    {
        List<Integer> lstOwnerIds = new ArrayList<Integer>();
        
        String strSelect = "select rp_owner_id from rp_owners where rp_owner_id >= " + nMinOwnerId + " and rp_owner_id <= " + nMaxOwnerId + " order by rp_owner_id";
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
                while(rs.next()) {
                    lstOwnerIds.add(rs.getInt(1));
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("getExistingOwnerIds SQLException: " + e.getMessage());
            }
        }
        return lstOwnerIds;
    }
    public static boolean existsRacingPostOwner(ENEStatement statement, int nOwnerId) {
        String strSelect = "select rp_owner_id from rp_owners where rp_owner_id = " + nOwnerId;
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
                if (rs.next()) {
                    rs.close();
                    return true;
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("existsRacingPostOwner SQLException: " + e.getMessage());
            }
        }
        return false;
    }

    public static int insertRPOwnerTypeYear(ENEStatement statement, int nOwnerId, String strYear, String strType, String strRuns, String strWins, String strWinPounds, String strTotalPounds) {
        String strUpdate = "REPLACE INTO rp_owners_type_year (rpty_owner_id, rpty_year, rpty_type, rpty_runs, rpty_wins, rpty_win_pounds, rpty_total_pounds, rpty_timestamp  ) VALUES (";
        strUpdate += nOwnerId + "," + strYear + ",'" + strType + "'," + strRuns + "," + strWins + "," + strWinPounds + "," + strTotalPounds + ", current_timestamp)";
        int nReturn = statement.executeUpdate(strUpdate);
        return nReturn;
    }

    public static int insertRPOwnerYear(ENEStatement statement, int nOwnerId, String strYear, String strRuns, String strWins, String strWinPounds, String strTotalPounds) {
        String strUpdate = "REPLACE INTO rp_owners_year (rpy_owner_id, rpy_year, rpy_runs, rpy_wins, rpy_win_pounds, rpy_total_pounds  ) VALUES (";
        strUpdate += nOwnerId + "," + strYear + "," + strRuns + "," + strWins + "," + strWinPounds + "," + strTotalPounds + ")";
        int nReturn = statement.executeUpdate(strUpdate);
        return nReturn;
    }

    public static int insertRPOwner(ENEStatement statement, int nOwnerId, String strOwner) {
        String strUpdate = "REPLACE INTO rp_owners (rp_owner_id, rp_owner_name, rp_timestamp) VALUES (";
        strUpdate += nOwnerId + ",'" + strOwner.replace("'", "''") + "', current_timestamp)";
        int nReturn = statement.executeUpdate(strUpdate);
        return nReturn;
    }

    public static HashMap<Integer, RacingPostRaceSummary> selectRacingPostReferenceData(ENEStatement statement, String strARDName) 
    {
        // retrieve information required to build a Racing Post Reference
        // based on historic race records found in the database
        HashMap<Integer, RacingPostRaceSummary> hmWinners = new HashMap<Integer, RacingPostRaceSummary>();
        String strQuery = "select * from ( ";
        strQuery += "select year(meeting_date) as year, case when hra.course = 'Royal_Ascot' then 'Ascot' else hra.course end as course, hra.race_type, hr.name as winner, 'SF' as source, hra.race_id as race_id, hra.meeting_date as meeting_date";
        strQuery += " from ((";
        strQuery += " additional_race_link inner join historic_races hra on arl_race_id=hra.race_id)";
        strQuery += " inner join historic_runners hr on hra.race_id=hr.race_id and coalesce(amended_position, finish_position, 100) = 1)";
        strQuery += " where arl_name= '" + strARDName.replace("'", "''") + "' and arl_source='SF'";
        strQuery += " union";
        strQuery += " select year(meeting_date) as year, case when hra.course = 'Newmarket' and conditions like 'July%' then 'Newmarket (July)' else hra.course end, hra.race_type, hr.name as winner, arl_source, hra.race_id, hra.meeting_date";
        strQuery += " from((";
        strQuery += " additional_race_link inner join additional_races hra on arl_race_id=hra.race_id and ara_source=arl_source)";
        strQuery += " inner join additional_runners hr on hra.race_id=hr.race_id and aru_source=arl_source and coalesce(amended_position, finish_position, 100) = 1)";
        strQuery += " where arl_name= '" + strARDName.replace("'", "''") + "' and arl_source!='SF'";
        strQuery += " )as T1 order by year desc";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                int nYear = rs.getInt("year");
                int nId = rs.getInt("race_id");
                String strSource = rs.getString("source");
                String strCourse = rs.getString("course");
                String strWinner = rs.getString("winner");
                String strRaceType = rs.getString("race_type");
                Date date = rs.getDate("meeting_date");
                Calendar dtRace = Calendar.getInstance();
                dtRace.setTimeInMillis(date.getTime());
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = format1.format(dtRace.getTime());
                if ("RP".equals(strSource)) 
                {
                    RacingPostCourse course = ENEColoursDBEnvironment.getInstance().getRPCourseByName(strCourse, strRaceType);
                    RacingPostRaceSummary summary = new RacingPostRaceSummary(nId, course, strDate);
                    String strRef = summary.getWikipediaReference();
                    hmWinners.put(nYear, summary);
                } 
                else if (!hmWinners.containsKey(nYear)) 
                {
                    RacingPostCourse course = ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(strCourse, strRaceType);
                    hmWinners.put(nYear, new RacingPostHorseRaceSummary(0, course, strDate, strWinner));
                }
            }
            rs.close();
        } catch (SQLException e) {
        }
        return hmWinners;
    }
    public static void convertSportingLifeOnlyReferenceData(ENEStatement statement)
    {
       ArrayList<Pair<String,RacingPostHorseRaceSummary>> alRaces = RacingPostFactory.selectSportingLifeOnlyReferenceData(statement);
       Iterator<Pair<String,RacingPostHorseRaceSummary>> iter = alRaces.iterator();
       while(iter.hasNext())
       {
           Pair<String,RacingPostHorseRaceSummary> cur = iter.next();
           try
           {
                RacingPostRacecards.insertFullRaceResult(statement, cur.getElement1(), ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(cur.getElement0()), true, false, true);
           }
           catch(Exception e)
           {
             System.out.println("convertSportingLifeOnlyReferenceData: " + e.getMessage());
             e.printStackTrace();
           }
       }
    }
    private static ArrayList<Pair<String,RacingPostHorseRaceSummary>> selectSportingLifeOnlyReferenceData(ENEStatement statement) 
    {
        // races that exist as SL but not RP
        ArrayList<Pair<String,RacingPostHorseRaceSummary>> alRaces =  new ArrayList<Pair<String,RacingPostHorseRaceSummary>>();
        String strQuery="select ard_course, arl1.arl_name, ar1.race_type, ar1.meeting_date, name as winner, ard_name";
        strQuery += " from additional_race_link arl1 inner join additional_races ar1 on arl1.arl_race_id = ar1.race_id and ar1.ara_source=arl1.arl_source";
        strQuery += " inner join additional_race_data on arl_name=ard_name and ((year(meeting_date) < 2003) or (ard_country = 'France'))";
        strQuery += " inner join additional_runners aru on aru_source=ara_source and ar1.race_id=aru.race_id and coalesce(amended_position, finish_position)=1";
        strQuery += " left outer join (select arl_name, year(meeting_date) as year";
        strQuery += " from additional_race_link arl2 inner join additional_races ar2 on arl2.arl_race_id = ar2.race_id and ar2.ara_source=arl2.arl_source";
        strQuery += " where arl2.arl_source='RP' ) d1";
        strQuery += " on arl1.arl_name=d1.arl_name and year(ar1.meeting_date)=d1.year";
        strQuery += " where arl1.arl_source='SL' and d1.arl_name is null";        
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                String strCourse = rs.getString("ard_course");
                String strWinner = rs.getString("winner");
                String strRaceType = rs.getString("race_type");
                String strARDName = rs.getString("ard_name");
                Date date = rs.getDate("meeting_date");
                Calendar dtRace = Calendar.getInstance();
                dtRace.setTimeInMillis(date.getTime());
                SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                String strDate = format1.format(dtRace.getTime());
                RacingPostCourse course = ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(strCourse, strRaceType);
                alRaces.add(new Pair(strARDName, new RacingPostHorseRaceSummary(0, course, strDate, strWinner)));
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("selectSportingLifeOnlyReferenceData: " + e.getMessage());
        }
        return alRaces;
    }

    /*
    public static RacingPostRaceSummary getRacingPostReference(ENEStatement statement, AdditionalRaceData ard, int nYear, String strDate, RacingPostCourse course)
    {
    HashMap<Integer, String> hmWinners = WikipediaQuery.getWikipediaWinners("en", ard);
    return getRacingPostReference(statement, ard, nYear, strDate, hmWinners, course);
    }
     */
    static RacingPostRaceSummary getRacingPostReferenceDatabase(ENEStatement statement, AdditionalRaceData ard, int nYear) {
        RacingPostRaceSummary summary = null;

        HashMap<Integer, RacingPostRaceSummary> hmSummaries = selectRacingPostReferenceData(statement, ard.getName());
        
        summary = hmSummaries.get(nYear);
        
        return summary;
    }
    
public static void generateRacingPostRaces(ENEStatement statement, int nYear, String strRaceType, boolean bReplace) throws IOException, ParseException, InterruptedException
{
   JSONObject params = new JSONObject();
   params.put("YEAR", nYear);
   params.put("TYPE", strRaceType);  // NH or Flat
   JSONObject jsonObj = JSONUtils.reportJSONFile(statement, 
                    ENEColoursEnvironment.getInstance().getVariable("QUERY_DIRECTORY") +  "select_racing_post_races.sql", 
                    params, 500, JSONUtils.JSON, null); 
   JSONArray array = (JSONArray) jsonObj.get("data");
   System.out.println(array.toString());
   for(int i = 0; i < array.length(); i++)
   {
       JSONObject obj = (JSONObject)array.get(i);
       int nRace = (int) obj.get("race_id");
       RacingPostCourse course = new RacingPostCourse((String) obj.get("rp_course_code"), (String) obj.get("course"), (String) obj.get("rp_country"), (int) obj.get("rp_course_id"), (String) obj.get("sf_course_name"), (String) obj.get("atr_course_name"));
       RacingPostRaceSummary summary = new RacingPostRaceSummary(nRace, course, (String) obj.get("meeting_date"));
       String strRaceURL = summary.getRaceURL();
       System.out.println(strRaceURL);
       RacingPostRacecards.insertFullRaceResult(statement, summary, bReplace, false);
   }
}
public static void loadArdRaceUrls(ENEStatement statement, String strARDName, int nStartYear, int nEndYear, boolean bReplace)
{
    ArrayList<String> alRaceURLs = getArdRaceUrls(statement, strARDName, nStartYear, nEndYear);
    Iterator<String> iter = alRaceURLs.iterator();
    while(iter.hasNext())
    {
        String strURL = iter.next();
        System.out.println(strURL);
        RacingPostRaceSummary summary = new RacingPostRaceSummary(strURL);
        try
        {
            RacingPostRacecards.insertFullRaceResult(statement, summary, bReplace, false);
        }
        catch(Exception e)
        {
            System.out.println("loadArdRaceUrls exception: " + strURL);
        }
    }
}
    private static ArrayList<String> getArdRaceUrls(ENEStatement statement, String strARDName, int nStartYear, int nEndYear) 
    {
        // races that exist as SL but not RP
        ArrayList<String> alRaceURLs =  new ArrayList<String>();
        String strQuery="select concat('https://www.racingpost.com/results/', rp_course_id, '/', rp_course_code, '/', meeting_date, '/', race_id)";
	strQuery += " from additional_race_link inner join additional_races hra on arl_race_id=hra.race_id and ara_source=arl_source";
	strQuery += " inner join racing_post_course_2017 on course=rp_course_name";
	strQuery += " where arl_source='RP' and arl_name='"+ strARDName.replace("'", "''") + "'";
        if (nStartYear != 0)
            strQuery += " and year(meeting_date)>="+ nStartYear;
        if (nEndYear != 0)
            strQuery += " and year(meeting_date)<="+ nEndYear;
        strQuery += " order by meeting_date";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                String strURL = rs.getString(1);
                alRaceURLs.add(strURL);
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println("getArdRaceUrls: " + e.getMessage());
        }
        return alRaceURLs;
    }

}
