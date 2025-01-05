/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.smartform.bos.AdditionalRaceInstance;
import ene.eneform.utils.ENEStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author simon
 */
public class AdditionalRaceLinkFactory {

    public static int insertAdditionalRaceLink(ENEStatement statement, String strARDName, String strSource, int nRace) {
        int nReturn = 0;
        String strUpdate = "replace into additional_race_link (arl_source, arl_race_id, arl_name)";
        strUpdate += "values (?, ?, ?)";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setString(1, strSource);
            insert.setInt(2, nRace);
            insert.setString(3, strARDName);
            nReturn = insert.executeUpdate();
        } 
        catch (Exception e) 
        {
            System.out.println("insertAdditionalSLRaceLink Exception: " + e.getMessage());
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


    public static int insertSFAdditionalRaceLinks(ENEStatement statement) {
        String strUpdate = "insert into additional_race_link (arl_source, arl_race_id, arl_name)(select distinct arl_source, child_id, arl_name from additional_race_link arl inner join previous_race on arl_race_id=parent_id where arl_source='SF' and not exists (select * from additional_race_link a1 where child_id = a1.arl_race_id and arl_source='SF'))";
        int nInserts = statement.executeUpdate(strUpdate);
        return nInserts;
    }

    public static ArrayList<AdditionalRaceInstance> getAdditionalRaceLinks(ENEStatement statement, String strARDName) {
        ArrayList<AdditionalRaceInstance> alRaces = new ArrayList<AdditionalRaceInstance>();
        String strQuery = "select * from (";
        strQuery += "select arl_source, arl_race_id as race_id, meeting_date from additional_race_link inner join daily_races on race_id=arl_race_id where arl_source='SF' and arl_name='" + strARDName.replace("'", "''") + "'";
        strQuery += " union";
        strQuery += " select arl_source, arl_race_id, meeting_date from additional_race_link inner join additional_races on race_id=arl_race_id where arl_name='" + strARDName.replace("'", "''") + "' and arl_source=ara_source";
        strQuery += ") as t1 order by meeting_date desc";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                AdditionalRaceInstance arl = new AdditionalRaceInstance(rs.getString("arl_source"), rs.getInt("race_id"), rs.getDate("meeting_date"));
                alRaces.add(arl);
            }
            rs.close();
        } catch (SQLException e) {
        }
        return alRaces;
    }

    public static AdditionalRaceInstance getLatestAdditionalRaceLink(ENEStatement statement, String strDescription) {
        ArrayList<AdditionalRaceInstance> alAdditionalRaceLinks = getAdditionalRaceLinks(statement, strDescription);
        if (alAdditionalRaceLinks.size() > 0) {
            return alAdditionalRaceLinks.get(0);
        } else {
            return null;
        }
    }

    public static AdditionalRaceInstance getAdditionalRaceLink(ENEStatement statement, String strARDName, int nRaceId, String strSource) {
        String strQuery;
        
        if ("SF".equals(strSource))
        {
            strQuery = "select arl_race_id, arl_source, meeting_date, course, race_type, distance_yards, going, race_name, group_race, handicap, num_runners, direction, time(scheduled_time) as scheduled_time, name as winner from additional_race_link inner join historic_races a on race_id=arl_race_id inner join historic_runners u on a.race_id=u.race_id and finish_position=1 where arl_source='SF' and arl_name='" + strARDName.replace("'", "''") + "' and arl_race_id=" + nRaceId;
        }
        else
        {
            strQuery = "select arl_race_id, arl_source, meeting_date, course, race_type, distance_yards, going, race_name, group_race, handicap, num_runners, '' as direction, scheduled_time, name as winner from additional_race_link inner join additional_races a on race_id=arl_race_id inner join additional_runners u on a.race_id=u.race_id and finish_position=1 where arl_name='" + strARDName.replace("'", "''") + "' and arl_source=ara_source and arl_source='" + strSource + "' and arl_race_id=" + nRaceId;
        }
        System.out.println(strQuery);
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) 
            {
                AdditionalRaceInstance arl = new AdditionalRaceInstance(rs.getString("arl_source"), rs.getInt("arl_race_id"), rs.getDate("meeting_date"));
                arl.setTitle(rs.getString("race_name"));
                arl.setCourse(rs.getString("course"));
                arl.setRaceType(rs.getString("race_type"));
                arl.setGoing(rs.getString("going"));
                arl.setDirection(rs.getString("direction"));
                arl.setDistanceYards(rs.getInt("distance_yards"));
                arl.setNrRunners(rs.getInt("num_runners"));
                arl.setGroupRace(rs.getInt("group_race"));
                arl.setHandicap(rs.getInt("handicap") == 1);
                Time scheduledTime = rs.getTime("scheduled_time");
                if (scheduledTime != null)
                    arl.setScheduledTime(scheduledTime.getTime());
                arl.setWinner(rs.getString("winner"));
                rs.close();
                return arl;
            }
        } catch (SQLException e) {
        }
        return null;
    }

    public static AdditionalRaceInstance getYearAdditionalRaceLink(ENEStatement statement, String strARDName, int nYear) {
        String strQuery = "select * from (";
        strQuery += "select arl_race_id, arl_source, meeting_date, course, race_type, distance_yards, going, race_name, group_race, handicap, num_runners, direction, time(scheduled_time) as scheduled_time from additional_race_link, historic_races where arl_source='SF' and arl_name='" + strARDName.replace("'", "''") + "' and arl_race_id=race_id and year(meeting_date)=" + nYear;
        strQuery += " union";
        strQuery += " select arl_race_id, arl_source, meeting_date, course, race_type, distance_yards, going, race_name, group_race, handicap, num_runners, '' as direction, scheduled_time from additional_race_link inner join additional_races on race_id=arl_race_id where arl_name='" + strARDName.replace("'", "''") + "' and arl_source=ara_source and year(meeting_date)=" + nYear;
        strQuery += ") as t1 order by meeting_date desc";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) {
                AdditionalRaceInstance arl = new AdditionalRaceInstance(rs.getString("arl_source"), rs.getInt("arl_race_id"), rs.getDate("meeting_date"));
                arl.setTitle(rs.getString("race_name"));
                arl.setCourse(rs.getString("course"));
                arl.setRaceType(rs.getString("race_type"));
                arl.setGoing(rs.getString("going"));
                arl.setDirection(rs.getString("direction"));
                arl.setDistanceYards(rs.getInt("distance_yards"));
                arl.setNrRunners(rs.getInt("num_runners"));
                arl.setGroupRace(rs.getInt("group_race"));
                arl.setHandicap(rs.getInt("handicap") == 1);
                Time scheduledTime = rs.getTime("scheduled_time");
                if (scheduledTime != null)
                    arl.setScheduledTime(scheduledTime.getTime());
                rs.close();
                return arl;
            }
        } catch (SQLException e) {
        }
        return null;
    }


    public static void updateAdditionalRaceLink(ENEStatement statement, int nParentRace, String strDescription) {
        String strUpdate = "insert into additional_race_link (arl_source, arl_race_id, arl_name)";
        strUpdate += "(select 'SF', child_id, '" + strDescription.replace("'", "''") + "' from previous_race where parent_id = " + nParentRace;
        strUpdate += " and not exists (select * from additional_race_link where arl_race_id=child_id and arl_source='SF'))";
        statement.executeUpdate(strUpdate);
        AdditionalRaceDataFactory.insertAdditionalRaceData(statement, 2014, null);
        AdditionalRaceDataFactory.insertAdditionalRaceData(statement, 2013, strDescription);
    }
/* These functions do not relate to the additional_race_link table and should be renamed! */
    public static AdditionalRaceInstance getAdditionalRaceLinkObject(ENEStatement statement, int nRace, String strSource) {
        String strQuery;
        if ("SF".equals(strSource)) {
            // it seems that historic_races distance_yards is unreliable (rounded to nearest furlong??)
            strQuery = "select 'SF' as arl_source, h.race_id, h.meeting_date, h.course, h.race_type, coalesce(d.distance_yards, h.distance_yards) as distance_yards, h.going, h.race_name, coalesce(h.group_race, case when h.race_name like '%listed%' then 4 else 0 end) as group_race, h.handicap, num_runners, h.direction, time(h.scheduled_time) as scheduled_time, ard_sex, ard_age_range, coalesce(rp_country, '') as rp_country, hru.name as winner from historic_races h left outer join daily_races d using (race_id) left outer join additional_race_link on arl_race_id=race_id and arl_source='SF' left outer join additional_race_data on ard_name=arl_name left outer join racing_post_course_2017 on sf_course_name=h.course left outer join historic_runners hru on h.race_id=hru.race_id and coalesce(amended_position, finish_position) = 1 where h.race_id=" + nRace;
        } else {
            strQuery = "select ara_source as arl_source, h.race_id, meeting_date, course, race_type, distance_yards, going, race_name, group_race, handicap, num_runners, '' as direction, scheduled_time, ard_sex, ard_age_range, coalesce(rp_country, '') as rp_country, hru.name as winner from additional_races h left outer join additional_race_link on arl_race_id=race_id and arl_source=ara_source left outer join additional_race_data on ard_name=arl_name left outer join racing_post_course_2017 on rp_course_name=h.course left outer join additional_runners hru on h.race_id=hru.race_id and coalesce(amended_position, finish_position) = 1 and aru_source=ara_source where h.race_id=" + nRace + " and ara_source='" + strSource + "'";
        }
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) {
                Date date = rs.getDate("meeting_date");
                TimeZone localTimezone = TimeZone.getDefault(); 
                Date localDate = new Date(date.getTime() + localTimezone.getOffset(date.getTime()));     // add 1 hour for BST
                AdditionalRaceInstance arl = new AdditionalRaceInstance(rs.getString("arl_source"), rs.getInt("race_id"), localDate);
                arl.setTitle(rs.getString("race_name"));
                arl.setCourse(rs.getString("course"));
                arl.setRaceType(rs.getString("race_type"));
                arl.setGoing(rs.getString("going"));
                arl.setDirection(rs.getString("direction"));
                arl.setDistanceYards(rs.getInt("distance_yards"));
                arl.setNrRunners(rs.getInt("num_runners"));
                arl.setGroupRace(rs.getInt("group_race"));
                arl.setHandicap(rs.getInt("handicap") == 1);
                arl.setCountry(rs.getString("rp_country"));
                arl.setWinner(rs.getString("winner"));
                Time scheduledTime = rs.getTime("scheduled_time");
                if (scheduledTime != null)
                    arl.setScheduledTime(scheduledTime.getTime());
                arl.setAgeRange(rs.getString("ard_age_range"));
                arl.setSex(rs.getString("ard_sex"));
                rs.close();
                return arl;
            }
        } catch (SQLException e) {
        }
        return null;
    }

    public static ArrayList<AdditionalRaceInstance> getAdditionalRaceHorseLinks(ENEStatement statement, String strHorse, String strBred) {
        return getAdditionalRaceHorseLinks(statement, strHorse, "");
    }

    public static ArrayList<AdditionalRaceInstance> getAdditionalRaceHorseLinks(ENEStatement statement, String strHorse, String strBred, String strWhere) {
        ArrayList<AdditionalRaceInstance> alRaces = new ArrayList<AdditionalRaceInstance>();
        String strQuery = "select * from (";
        strQuery += "select 'SF' as arl_source, race_id, meeting_date, course, race_type, distance_yards, going, race_name, group_race, handicap, num_runners, direction from historic_runners inner join historic_races using (race_id) where name='" + strHorse.replace("'", "''") + "' and (finish_position is not null or unfinished not in ('Non-Runner', 'Withdrawn', 'UNKNOWN'))";
        if ((strBred != null) && (!"".equals(strBred))) {
            strQuery += (" and bred = '" + strBred + "'");
        }
        strQuery += " union";
        strQuery += " select ara_source as arl_source, a.race_id, meeting_date, course, race_type, distance_yards, going, race_name, group_race, handicap, num_runners, '' as direction from additional_runners u inner join additional_races a on u.race_id=a.race_id and ara_source=aru_source where name='" + strHorse.replace("'", "''") + "'";
        if ((strBred != null) && (!"".equals(strBred))) {
            strQuery += (" and bred = '" + strBred + "'");
        }
        strQuery += ") as t1";
        if (!"".equals(strWhere)) {
            strQuery += (" where " + strWhere);
        }
        strQuery += " order by meeting_date asc";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                AdditionalRaceInstance arl = new AdditionalRaceInstance(rs.getString("arl_source"), rs.getInt("race_id"), rs.getDate("meeting_date"));
                arl.setTitle(rs.getString("race_name"));
                arl.setCourse(rs.getString("course"));
                arl.setRaceType(rs.getString("race_type"));
                arl.setGoing(rs.getString("going"));
                arl.setDirection(rs.getString("direction"));
                arl.setDistanceYards(rs.getInt("distance_yards"));
                arl.setNrRunners(rs.getInt("num_runners"));
                arl.setGroupRace(rs.getInt("group_race"));
                arl.setHandicap(rs.getInt("handicap") == 1);
                alRaces.add(arl);
            }
            rs.close();
        } catch (SQLException e) {
        }
        return alRaces;
    }
    
}
