/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.smartform.bos.AdditionalRace;
import ene.eneform.smartform.bos.AdditionalRunner;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.web.rp.RacingPostCourse;
import ene.eneform.colours.web.rp.RacingPostRaceSummary;
import ene.eneform.smartform.bos.SmartformTack;
import ene.eneform.utils.ENEStatement;

import java.sql.*;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.TimeZone;

/**
 *
 * @author Simon
 */
public class AdditionalRacesFactory {

    public static boolean raceExists(ENEStatement statement, long nRaceId, String strSource)
    {
        String strQuery;
        int nRecords = 0;
        if ("SF".equals(strSource))
        {
            strQuery = "select count(*) from historic_races where race_id=" + nRaceId;
        }
        else
        {
            strQuery = "select count(*) from additional_races where ara_source='" + strSource + "' and race_id=" + nRaceId;
        }
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) 
            {
                nRecords = rs.getInt(1);
            }
            rs.close();
        } catch (SQLException e) {
        }

        return (nRecords == 1);

    }
    public static RacingPostRaceSummary getRacingPostRaceSummary(ENEStatement statement, long nRaceId)
    {
        RacingPostRaceSummary summary = null;
        String strQuery = "select race_type, case when hra.course = 'Newmarket' and conditions like 'July%' then 'Newmarket (July)' else hra.course end as course, hra.meeting_date, scheduled_time";
        strQuery += " from additional_races hra";
        strQuery += " where ara_source='RP' and race_id=" + nRaceId;
        
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) 
            {
                String strCourse = rs.getString("course");
                String strRaceType = rs.getString("race_type");
                Date date = rs.getDate("meeting_date");
                Time time = rs.getTime("scheduled_time");
                if (time != null)
                {
                    Calendar dtRace = Calendar.getInstance();
                    TimeZone localTimezone = TimeZone.getDefault();   
                    dtRace.setTimeInMillis(date.getTime() + time.getTime() + localTimezone.getOffset(time.getTime()));
                    RacingPostCourse course = ENEColoursDBEnvironment.getInstance().getRPCourseByName(strCourse, strRaceType);
                    summary = new RacingPostRaceSummary(nRaceId, course, dtRace);
                }
             }
            rs.close();
        } catch (SQLException e) {
        }
        
        return summary;
   }
    public static int updateAdditionalRaceCourse(ENEStatement statement, int nRace, String strCourse) {
        return 0;
    }

    public static int insertAdditionalRunners(ENEStatement statement, Collection<AdditionalRunner> colRunners) {
        return insertAdditionalRunners(statement, colRunners, false);
    }
    public static int insertAdditionalRunners(ENEStatement statement, Collection<AdditionalRunner> colRunners, boolean bReplace) {
        int nReturn = 0;
        String strUpdate = (bReplace ? "replace" : "insert") + " into additional_runners (race_id, name, bred, colour, gender, cloth_number, stall_number, trainer_name, owner_name, jockey_name, dam_name, dam_bred, sire_name, sire_bred, forecast_price, weight_pounds, starting_price, finish_position, form_figures, age, days_since_ran, tack_hood, tack_visor, tack_blinkers, tack_eye_shield, tack_eye_cover, tack_cheek_piece, tack_pacifiers, tack_tongue_strap, distance_beaten, forecast_price_decimal, starting_price_decimal, favourite, jockey_claim, weight_penalty, jockey_colours, aru_source, ard_timestamp)";
        strUpdate += " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            Iterator<AdditionalRunner> iter = colRunners.iterator();
            AdditionalRunner runner = null;
            while (iter.hasNext()) {
                try 
                {
                    runner = iter.next();
                    insert.setInt(1, runner.getRaceId());
                    insert.setString(2, runner.getName());
                    insert.setString(3, runner.getBred());
                    insert.setString(4, runner.getColour());
                    insert.setString(5, String.valueOf(runner.getShortGender()));
                    if (runner.getClothNumber() < 0) {
                        insert.setNull(6, Types.INTEGER);
                    } else {
                        insert.setInt(6, runner.getClothNumber());
                    }
                    if (runner.getStallNumber() < 0) {
                        insert.setNull(7, Types.INTEGER);
                    } else {
                        insert.setInt(7, runner.getStallNumber());
                    }
                    insert.setString(8, runner.getTrainer());
                    insert.setString(9, runner.getOwner());
                    insert.setString(10, runner.getJockey());
                    insert.setString(11, runner.getDamName());
                    insert.setString(12, runner.getDamBred());
                    insert.setString(13, runner.getSireName());
                    insert.setString(14, runner.getSireBred());
                    insert.setString(15, runner.getForecastSPString());
                    if (runner.getWeightPounds() < 0) {
                        insert.setNull(16, Types.INTEGER);
                    } else {
                        insert.setInt(16, runner.getWeightPounds());
                    }
                    insert.setString(17, runner.getStartingPrice());
                    if (runner.getFinishPosition() <= 0) {
                        insert.setNull(18, Types.INTEGER);
                    } else {
                        insert.setInt(18, runner.getFinishPosition());
                    }
                    insert.setString(19, runner.getFormFigures());
                    if (runner.getAge() < 0) {
                        insert.setNull(20, Types.INTEGER);
                    } else {
                        insert.setInt(20, runner.getAge());
                    }
                    if (runner.getDaysSinceRan() < 0) {
                        insert.setNull(21, Types.INTEGER);
                    } else {
                        insert.setInt(21, runner.getDaysSinceRan());
                    }
                    
                    boolean bHood = false, bVisor = false, bBlinkers = false, bEyeShield = false, 
                            bEyeCover = false, bCheekPieces = false, bPacifiers = false, bTongueStrap = false;
                    SmartformTack tack = runner.getTack();
                    if (tack != null)
                    {
                        bHood = tack.hasHood();
                        bVisor = tack.hasVisor();
                        bBlinkers = tack.hasBlinkers();
                        bEyeShield = tack.hasEyeShield();
                        bEyeCover = tack.hasEyeCover();
                        bCheekPieces = tack.hasCheekPieces();
                        bPacifiers = tack.hasPacifiers();
                        bTongueStrap = tack.hasTongueStrap();
                    }
                    
                        insert.setInt(22, bHood ? 1 : 0);
                        insert.setInt(23, bVisor ? 1 : 0);
                        insert.setInt(24, bBlinkers? 1 : 0);
                        insert.setInt(25, bEyeShield ? 1 : 0);
                        insert.setInt(26, bEyeCover ? 1 : 0);
                        insert.setInt(27, bCheekPieces? 1 : 0);
                        insert.setInt(28, bPacifiers? 1 : 0);
                        insert.setInt(29, bTongueStrap ? 1 : 0);

                    if (runner.getDistanceBeaten() < 0) {
                        insert.setNull(30, Types.DECIMAL);
                    } else {
                        insert.setDouble(30, runner.getDistanceBeaten());
                    }
                    if (runner.getForecastSP() < 0) {
                        insert.setNull(31, Types.DECIMAL);
                    } else {
                        insert.setDouble(31, runner.getForecastSP());
                    }
                    if (runner.getStartingPriceDecimal() < 0) {
                        insert.setNull(32, Types.DECIMAL);
                    } else {
                        insert.setDouble(32, runner.getStartingPriceDecimal());
                    }
                    insert.setInt(33, runner.isFavourite() ? 1 : 0);
                    insert.setInt(34, runner.getJockeyClaim());
                    if (runner.getPenaltyWeight() < 0) {
                        insert.setNull(35, Types.INTEGER);
                    } else {
                        insert.setInt(35, runner.getPenaltyWeight());
                    }
                    insert.setString(36, runner.getJockeyColours());
                    insert.setString(37, runner.getSource());
                    nReturn += insert.executeUpdate();
                } catch (Exception e) {
                    System.out.println("insertAdditionalRunners Exception: " + runner.getName() + "-" + e.getMessage());
                }
            }
        } 
        catch (Exception e) 
        {
            System.out.println("insertAdditionalRunners Exception: " + e.getMessage());
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




    public static int insertDailyRace(ENEStatement statement, int nRace) {
        int nReturn = 0;
        // to do : derive Country
        String strUpdate = "insert into daily_races(race_id, meeting_id, meeting_date, course, race_title, race_type, class, handicap, distance_yards, country, scheduled_time, loaded_at)";
        strUpdate += "(select race_id, meeting_id, meeting_date, course, race_name, race_type, class, handicap, distance_yards, 'England', scheduled_time, '1970-01-01 12:00:00' from historic_races";
        strUpdate += " where race_id = ?)";
        
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setInt(1, nRace);
            nReturn = insert.executeUpdate();
        } catch (Exception e) 
        {
            System.out.println("insertDailyRace Exception: " + e.getMessage());
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


    public static int updateAdditionalRunnerResult(ENEStatement statement, AdditionalRunner runner) {
        int nClothNumber = runner.getClothNumber();
        String strRunnerMatch;
        if (nClothNumber > 0) {
            strRunnerMatch = " and cloth_number=" + nClothNumber;
        } else {
            strRunnerMatch = " and name='" + runner.getName().replace("'", "''") + "'";
        }
        String strUpdate;
        int nFinishPosition = runner.getFinishPosition();
        if (nFinishPosition > 0) {
            strUpdate = "UPDATE additional_runners set finish_position = " + nFinishPosition + " where aru_source='" + runner.getSource() + "' and race_id=" + runner.getRaceId() + strRunnerMatch;
            statement.executeUpdate(strUpdate);
        }
        String strUnfinished = runner.getUnfinished();
        if (strUnfinished != null) {
            strUpdate = "UPDATE additional_runners set unfinished = '" + strUnfinished + "' where aru_source='" + runner.getSource() + "' and race_id=" + runner.getRaceId() + strRunnerMatch;
            statement.executeUpdate(strUpdate);
        }
        strUpdate = "UPDATE additional_runners set starting_price = '" + runner.getStartingPrice() + "', starting_price_decimal=" + runner.getStartingPriceDecimal() + " where aru_source='" + runner.getSource() + "' and race_id=" + runner.getRaceId() + strRunnerMatch;
        statement.executeUpdate(strUpdate);
        strUpdate = "UPDATE additional_runners set betting_text = '" + runner.getBetting() + "', in_race_comment='" + runner.getInRaceComment().replaceAll("'", "''") + "', ard_timestamp=current_timestamp where aru_source='" + runner.getSource() + "' and race_id=" + runner.getRaceId() + strRunnerMatch;
        int nReturn = statement.executeUpdate(strUpdate);
        if (runner.getDistanceBeaten() >= 0) {
            strUpdate = "UPDATE additional_runners set distance_beaten = " + runner.getDistanceBeaten() + " where aru_source='" + runner.getSource() + "' and race_id=" + runner.getRaceId() + strRunnerMatch;
            statement.executeUpdate(strUpdate);
        }
        String strOwner = runner.getOwnerName();
        if (!"".equals(strOwner)) 
        {
            strUpdate = "UPDATE additional_runners set owner_name = case when owner_name = '' then '" + strOwner.replace("'", "''") + "' else owner_name end where aru_source='" + runner.getSource() + "' and race_id=" + runner.getRaceId() + strRunnerMatch;
            statement.executeUpdate(strUpdate);
        }
        String strColours = runner.getJockeyColours();
        if (!"".equals(strColours)) 
        {
            strUpdate = "UPDATE additional_runners set jockey_colours = case when jockey_colours = '' then '" + strColours.replace("'", "''") + "' else jockey_colours end where aru_source='" + runner.getSource() + "' and race_id=" + runner.getRaceId() + strRunnerMatch;
            statement.executeUpdate(strUpdate);
        }
        return nReturn;
    }

    public static int updateAdditionalRunnerEmptyOwner(ENEStatement statement, String strHorse, String strBred, String strOwner) {
        int nSuffix = strHorse.indexOf(" I");
        if ("".equals(strBred) && nSuffix > 0) {
            strHorse = strHorse.replace(" I", "");
            strBred = "I";
        }
        String strUpdate = "UPDATE additional_runners set owner_name = '" + strOwner.replace("'", "''") + "' where name = '" + strHorse.replace("'", "''") + "' and bred = '" + strBred + "' and (owner_name is null or owner_name='' or owner_name='Not Specified')";
        int nReturn = statement.executeUpdate(strUpdate);
        return nReturn;
    }


    public static int insertAdditionalRaceInstance(ENEStatement statement, String strARDName, AdditionalRace ari, boolean bLink) {
        int nReturn = 0;
        String strUpdate = "replace INTO additional_races (race_id, meeting_date, course, race_name, race_type, going, race_class, num_runners, distance_yards, age_range, handicap, winning_time_disp, winning_time_secs, ara_source, conditions, group_race, scheduled_time, ard_timestamp)";
        strUpdate += "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, current_timestamp)";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setInt(1, ari.getRaceId());
            java.util.Date meeting = ari.getMeetingDate();
            if (meeting != null)
                insert.setDate(2, new Date(meeting.getTime()));
            else
                insert.setNull(2, Types.DATE);
            insert.setString(3, ari.getCourse());
            insert.setString(4, ari.getTitle());
            insert.setString(5, ari.getRaceType());
            insert.setString(6, ari.getGoing());
            String strClass = ari.getRaceClass();
            insert.setString(7, strClass.length() > 1 ? " " : strClass);
            insert.setInt(8, ari.getNrRunners());
            insert.setInt(9, ari.getDistanceYards());
            insert.setString(10, ari.getAgeRange());
            insert.setInt(11, ari.isHandicap() ? 1 : 0);
            insert.setString(12, ari.getWinningTime());
            if (ari.getWinningTimeSeconds() < 0) {
                insert.setNull(13, Types.DECIMAL);
            } else {
                insert.setDouble(13, ari.getWinningTimeSeconds());
            }
            insert.setString(14, ari.getSource());
            insert.setString(15, ari.getConditions());
            int nGroupRace = ari.getGroupRace();
            if (nGroupRace == 0) {
                insert.setNull(16, Types.INTEGER);
            } else {
                insert.setInt(16, nGroupRace);
            }
            String strScheduledTime = ari.getScheduledTime();
            if ("".equals(strScheduledTime))
            {
                insert.setNull(17, Types.TIME);
            }
            else
            {
                insert.setString(17, strScheduledTime);
            }
            nReturn = insert.executeUpdate();
            if (bLink && !"".equals(strARDName)) {
                AdditionalRaceLinkFactory.insertAdditionalRaceLink(statement, strARDName, ari.getSource(), ari.getRaceId());
            }
        } 
        catch (Exception e) 
        {
            System.out.println("insertAdditionalRaceInstance Exception: " + e.getMessage());
            e.printStackTrace();
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

}
