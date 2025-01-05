/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.AdditionalRaceWikipedia;
import ene.eneform.utils.DbUtils;
import ene.eneform.utils.ENEStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author simon
 */
public class AdditionalRaceDataFactory {

    private static String sm_strARWColumnList = "en.arw_wikipedia_ref as en_wikipedia_ref, en.arw_start_year as en_start_year, en.arw_end_year as en_end_year, fr.arw_wikipedia_ref as fr_wikipedia_ref, fr.arw_start_year as fr_start_year, fr.arw_end_year as fr_end_year";
    private static String sm_strARDColumnList = "ard_name, ard_title, ard_race_type, ard_track_type, ard_direction, ard_country, ard_course, ard_month, ard_day, ard_dow, ard_handicap, ard_distance_yards, ard_group_race, ard_class, ard_age_range, ard_num_fences, ard_comments, ard_standard_time_disp, ard_standard_time_secs, ard_sex, ard_start_year, ard_end_year, ard_keywords, sl_course_id, rp_course_code, ard_se_key, ard_GS_ref";

    public static int updateAdditionalRaceDataGSRef(ENEStatement statement, String strARDName, String strGSRef) {
        int nReturn = 0;
        String strUpdate = "update additional_race_data set ard_gs_ref = ? where ard_name = ?";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setString(1, strGSRef);
            insert.setString(2, strARDName);
            nReturn = insert.executeUpdate();
        } 
        catch (Exception e) 
        {
            System.out.println("updateAdditionalRaceDateGSRef Exception: " + e.getMessage());
        }
/*        finally
        {
            if (insert != null)
            {
                try
                {
                    insert.close();
                }
                catch(SQLException e)
                {

                }
            }
        } */
        return nReturn;
    }

    public static void loadAdditionalRaceData(ENEStatement statement, HashMap<String, AdditionalRaceData> hmARD)
    {
        String strQuery = "select " + sm_strARDColumnList + ", " + sm_strARWColumnList;
        strQuery += " from ((((additional_race_data left outer join additional_race_wikipedia en on en.arw_name=ard_name and en.arw_language='en')";
        strQuery += " left outer join additional_race_wikipedia fr on fr.arw_name=ard_name and fr.arw_language='fr')";
        strQuery += " left outer join sporting_life_course on ard_course=sl_course_name)";
        strQuery += " left outer join racing_post_course on ard_course=rp_course_name)";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                AdditionalRaceData ard = createAdditionalRaceDataObject(rs);
                hmARD.put(ard.getName(), ard);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<AdditionalRaceData> createAdditionalRaceDataList(ENEStatement statement, String strWhere, String strOrder) {
        ArrayList<AdditionalRaceData> ardList = new ArrayList<AdditionalRaceData>();
        String strQuery = "select " + sm_strARDColumnList + ", " + sm_strARWColumnList;
        strQuery += " from ((((additional_race_data left outer join additional_race_wikipedia en on en.arw_name=ard_name and en.arw_language='en')";
        strQuery += " left outer join additional_race_wikipedia fr on fr.arw_name=ard_name and fr.arw_language='fr')";
        strQuery += " left outer join sporting_life_course on ard_course=sl_course_name)";
        strQuery += " left outer join racing_post_course on ard_course=rp_course_name)";
        if (strWhere != null && !"".equals(strWhere)) {
            strQuery += " where " + strWhere;
        }
        if (strOrder != null && !"".equals(strOrder)) {
            strQuery += " order by " + strOrder;
        }
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                AdditionalRaceData ard = createAdditionalRaceDataObject(rs);
                ardList.add(ard);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ardList;
    }
/*
    replaced by ENEColoursDBEnvironment.getAdditionalRaceData
    public static AdditionalRaceData createAdditionalRaceData(ENEStatement statement, String strName) {
        String strQuery = "select " + AdditionalRaceDataFactory.sm_strARDColumnList + ", " + AdditionalRaceDataFactory.sm_strARWColumnList;
        strQuery += " from ((((additional_race_data left outer join additional_race_wikipedia en on en.arw_name=ard_name and en.arw_language='en')";
        strQuery += " left outer join additional_race_wikipedia fr on fr.arw_name=ard_name and fr.arw_language='fr')";
        strQuery += " left outer join sporting_life_course on ard_course=sl_course_name)";
        strQuery += " left outer join racing_post_course on rp_course_name = case when ard_course='Royal_Ascot' then 'Ascot' when ard_course='Epsom' then 'Epsom_Downs' else ard_course end)";
        strQuery += " where ard_name='" + strName.replace("'", "''") + "'";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) {
                AdditionalRaceData ard = createAdditionalRaceDataObject(rs);
                return ard;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
*/
    public static AdditionalRaceData createAdditionalRaceData(ENEStatement statement, int nRace, String strSource) {
        String strQuery = "select " + sm_strARDColumnList + ", " + sm_strARWColumnList;
        strQuery += " from (((((additional_race_link inner join additional_race_data on arl_name=ard_name)";
        strQuery += " left outer join additional_race_wikipedia en on en.arw_name=ard_name and en.arw_language='en')";
        strQuery += " left outer join additional_race_wikipedia fr on fr.arw_name=ard_name and fr.arw_language='fr')";
        strQuery += " left outer join sporting_life_course on ard_course=sl_course_name)";
        strQuery += " left outer join racing_post_course on ard_course=rp_course_name)";
        strQuery += " where arl_source='" + strSource + "' and arl_race_id=" + nRace;
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) {
                AdditionalRaceData ard = createAdditionalRaceDataObject(rs);
                return ard;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void insertAdditionalRaceData(ENEStatement statement, int nYear, String strDescription) {
        String strUpdate = "insert into additional_race_data (ard_name, ard_title, ard_race_type, ard_track_type, ard_direction, ard_country, ard_month, ard_day, ard_course, ard_distance_yards, ard_group_race, ard_class, ard_age_range, ard_dow, ard_num_fences, ard_handicap, ard_comments, ard_standard_time_disp, ard_standard_time_secs, ard_sex)";
        strUpdate += "(select arl_name,";
        strUpdate += " case when locate('(', d.race_title) > 0 then substr(d.race_title, 1, locate('(', d.race_title) - 1) else d.race_title end,";
        strUpdate += " d.race_type, d.track_type, h.direction, d.country, month(d.meeting_date), day(d.meeting_date), d.course, d.distance_yards,h.group_race, h.class, d.age_range, dayofweek(d.meeting_date), h.num_fences, d.handicap, '', standard_time_disp, standard_time_secs,";
        strUpdate += " case when locate('fillies', d.race_title) then case when locate('mares', d.race_title) then 'FM' else 'F' end when locate('mares', d.race_title) then 'M' when locate('colts', d.race_title) then case when locate('geldings', d.race_title) then 'CG' else 'C' end else '' end";
        strUpdate += " from additional_race_link, daily_races d, historic_races h";
        strUpdate += " where arl_source='SF' and arl_race_id=d.race_id and d.race_id=h.race_id and year(d.meeting_date)=" + nYear;
        if (strDescription != null) {
            strUpdate += " and arl_name='" + strDescription + "'";
        }
        strUpdate += " and not exists (select * from additional_race_data where arl_name=ard_name))";
        statement.executeUpdate(strUpdate);
    }

    public static int updateAdditionalRaceDataSEKey(ENEStatement statement, String strARDName) {
        // set to next available value
        int nReturn = 0;
        String strUpdate = "UPDATE additional_race_data ard INNER JOIN (select max(ard_se_key) + 1 as new_key from additional_race_data) t2 ON 1=1 set ard_se_key=new_key where ard_name = ? ";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setString(1, strARDName);
            nReturn = insert.executeUpdate();
        } 
        catch (Exception e) 
        {
            System.out.println("updateAdditionalRaceDateGSRef Exception: " + e.getMessage());
        }
    /*    finally
        {
            if (insert != null)
            {
                try
                {
                    insert.close();
                }
                catch(SQLException e)
                {

                }
            }
        } */
        return nReturn;
    }

    public static int updateAdditionalRaceDataWikipediaYears(ENEStatement statement, String strARDName, String strLanguage, int nStartYear, int nEndYear, int nWinnersStartYear, int nWinnersEndYear, int nRPRefStartYear, int nRPRefEndYear) {
        int nReturn = 0;
        String strUpdate = "update additional_race_wikipedia set arw_start_year = ?, arw_end_year = ?, arw_winners_start_year = ?, arw_winners_end_year = ?, arw_rpref_start_year = ?, arw_rpref_end_year = ? where arw_name = ? and arw_language = ?";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setInt(1, nStartYear);
            insert.setInt(2, nEndYear);
            insert.setInt(3, nWinnersStartYear);
            insert.setInt(4, nWinnersEndYear);
            insert.setInt(5, nRPRefStartYear);
            insert.setInt(6, nRPRefEndYear);
            insert.setString(7, strARDName);
            insert.setString(8, strLanguage);
            nReturn = insert.executeUpdate();
        } 
        catch (Exception e) 
        {
            System.out.println("insertAdditionalSLRaceLink Exception: " + e.getMessage());
        }
  /*      finally
        {
            if (insert != null)
            {
                try
                {
                    insert.close();
                }
                catch(SQLException e)
                {

                }
            }
        } */
        return nReturn;
    }

    static AdditionalRaceData createAdditionalRaceDataObject(ResultSet rs) throws SQLException {
        String strName = DbUtils.getDBString(rs, "ard_name");
        AdditionalRaceData ard = new AdditionalRaceData(strName);
        ard.setTitle(DbUtils.getDBString(rs, "ard_title"));
        ard.setRaceType(DbUtils.getDBString(rs, "ard_race_type"));
        ard.setTrackType(DbUtils.getDBString(rs, "ard_track_type"));
        ard.setDirection(DbUtils.getDBString(rs, "ard_direction"));
        ard.setCountry(DbUtils.getDBString(rs, "ard_country"));
        ard.setCourse(DbUtils.getDBString(rs, "ard_course"));
        ard.setSex(DbUtils.getDBString(rs, "ard_sex"));
        ard.setMonth(rs.getInt("ard_month"));
        ard.setDay(rs.getInt("ard_day"));
        ard.setDOW(rs.getInt("ard_dow"));
        ard.setDistanceYards(rs.getInt("ard_distance_yards"));
        ard.setGroupRace(rs.getInt("ard_group_race"));
        ard.setRaceClass(DbUtils.getDBString(rs, "ard_class"));
        ard.setAgeRange(DbUtils.getDBString(rs, "ard_age_range"));
        ard.setNrFences(rs.getInt("ard_num_fences"));
        ard.setComments(DbUtils.getDBString(rs, "ard_comments"));
        ard.setStandardTimeSeconds(rs.getDouble("ard_standard_time_secs"));
        ard.setStandardTime(DbUtils.getDBString(rs, "ard_standard_time_disp"));
        int nHandicap = rs.getInt("ard_handicap");
        ard.setHandicap(nHandicap == 1);
        ard.setKeywords(DbUtils.getDBString(rs, "ard_keywords"));
        ard.setStartYear(rs.getInt("ard_start_year"));
        ard.setEndYear(rs.getInt("ard_end_year"));
        ard.setWikipedia(new AdditionalRaceWikipedia(strName, "en", DbUtils.getDBString(rs, "en_wikipedia_ref"), rs.getInt("en_start_year"), rs.getInt("en_end_year")));
        ard.setWikipedia(new AdditionalRaceWikipedia(strName, "fr", DbUtils.getDBString(rs, "fr_wikipedia_ref"), rs.getInt("fr_start_year"), rs.getInt("fr_end_year")));
        ard.setGSref(DbUtils.getDBString(rs, "ard_GS_ref"));
        int nSEKey = rs.getInt("ard_se_key");
        if (nSEKey > 0) {
            ard.setSEKey(nSEKey);
        }
        ard.setCourseId(rs.getInt("sl_course_id"));
        ard.setRPCourseCode(rs.getString("rp_course_code"));
        return ard;
    }
    
}
