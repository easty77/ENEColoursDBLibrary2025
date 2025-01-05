/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.database;

import ene.eneform.smartform.bos.*;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import ene.eneform.utils.DbUtils;
import ene.eneform.utils.ENEStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class ENEColoursRunnerFactory {

    public static ArrayList<SmartformDailyRunner> getRacecardRunnerList(ENEStatement statement, int nRace)
    {
       // 20150817 write to racing_colours_parse BEFORE the big query
         ArrayList<String> aColours = createUnparsedJockeyColours(statement, new AdditionalRaceInstance("SF", nRace, null));
        Iterator<String> citer = aColours.iterator();
        while(citer.hasNext())
        {
            String strDescription = citer.next();
            ENERacingColoursFactory.createColours(statement, ENEColoursEnvironment.DEFAULT_LANGUAGE, strDescription);
         }
        
         ArrayList<SmartformDailyRunner> alRunners = SmartformRunnerFactory.createDailyRaceRunners(statement, nRace);
    
        return alRunners;
    }
    public static ArrayList<SmartformDailyRunner> getDayRunnerList(ENEStatement statement, int nDayDiff)
    {
         ArrayList<SmartformDailyRunner> alRunners = SmartformRunnerFactory.createDailyDayRunners(statement, nDayDiff);
  
        return alRunners;
    }
public static ArrayList<SmartformHistoricRunner> getRacesWinnerList(ENEStatement statement, ArrayList<SmartformRace> races)
{

    ArrayList<SmartformHistoricRunner> alRunners = SmartformRunnerFactory.getRacesWinnerList(statement, races);
 
          return alRunners;
}

public static SmartformHistoricRunner getRaceWinner(ENEStatement statement, int nRace)
{
    SmartformHistoricRunner runner = SmartformRunnerFactory.getRaceWinner(statement, nRace);

        return runner;
}

public static ArrayList<SmartformHistoricRunner> getRacesFirst5List(ENEStatement statement, ArrayList<SmartformRace> races, int nPlaces)
{

    ArrayList<SmartformHistoricRunner> alRunners = SmartformRunnerFactory.getRacesFirstNList(statement, races, nPlaces);
 
          return alRunners;
}

public static ArrayList<SmartformHistoricRunner> getMeetingWinnerList(ENEStatement statement, int nMeeting)
{
    ArrayList<SmartformHistoricRunner> alRunners = SmartformRunnerFactory.createMeetingWinners(statement, nMeeting);
        return alRunners;
    }
public static ArrayList<ArrayList<SmartformHistoricRunner>> getMeeting123List(ENEStatement statement, int nMeeting, int nPlaces)
{
    ArrayList<ArrayList<SmartformHistoricRunner>> alRaceRunners = SmartformRunnerFactory.createMeeting123(statement, nMeeting, nPlaces);
        return alRaceRunners;
    }

    
   public static ArrayList<SmartformColoursRunner> getSmartformRaceRunners(ENEStatement statement, AdditionalRaceInstance link, int nPlaces)
   {
       // 20150817 write to racing_colours_parse BEFORE the big query
         ArrayList<String> aColours = createUnparsedJockeyColours(statement, link);
        Iterator<String> citer = aColours.iterator();
        while(citer.hasNext())
        {
            String strDescription = citer.next();
            ENERacingColoursFactory.createColours(statement, ENEColoursEnvironment.DEFAULT_LANGUAGE, strDescription);
        }
        ArrayList<SmartformColoursRunner> aRunners = createAdditionalColoursRunners(statement, link.getRaceId(), link.getSource(), nPlaces);
           return aRunners;
    }
/*    public static int createJockeyColoursParse(ENEStatement statement, String strDescription)
    {
        ENEColoursParser parser = new ENEColoursParser(ENEColoursEnvironment.DEFAULT_LANGUAGE, strDescription, "");
        ENERacingColours colours = ENERacingColoursFactory.createColours(statement, ENEColoursEnvironment.DEFAULT_LANGUAGE, strDescription);
        String strRemainder = parser.getRemainder();
        if ("".equals(strRemainder))
            strRemainder = colours.getErrorString();
        return ENEColoursFactory.insertJockeyColoursParse(statement, SmartformRunnerFactory.sm_RCPVersion, colours, parser.getRemainder(), parser.getExpanded(), parser.getSyntax());
    }
*/
   public static SmartformColoursRunner getSmartformRaceRunnerName(ENEStatement statement, AdditionalRaceInstance link, String strHorse, boolean bFinishedOnly) {
        ArrayList<SmartformColoursRunner> alRunners = getSmartformRaceRunners(statement, link, bFinishedOnly ? 0 : -1);    // finished runners only
        for(int i = 0; i < alRunners.size(); i++)
        {
            SmartformColoursRunner runner = alRunners.get(i);
            if (strHorse.equalsIgnoreCase(runner.getName()))
                return runner;
        }
        
        return null;
    }

    public static RacingColoursParse getParsedJockeyColours(ENEStatement stmt, String strDescription) {
        ArrayList<String> alRunners = new ArrayList<String>();
        String strQuery = "select rcp_jacket, rcp_sleeves, rcp_cap, rcp_unresolved from racing_colours_parse where rcp_description = '" + strDescription.replace("'", "''") + "' and rcp_version='" + SmartformRunnerFactory.sm_RCPVersion + "'";
        ResultSet rs = stmt.executeQuery(strQuery);
        if (rs != null) {
            try {
                if (rs.next()) {
                    return new RacingColoursParse(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("checkParsedJockeyColours exception: " + strDescription + "-" + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }
    public static boolean isUnparsedJockeyColours(ENEStatement stmt, String strJockeyColours) {
        int nCount = 0;
        String strQuery = "select count(*) from racing_colours_parse where '" + strJockeyColours + "'=rcp_description and rcp_version='" + SmartformRunnerFactory.sm_RCPVersion + "'";
        
        ResultSet rs = stmt.executeQuery(strQuery);
        if (rs != null) {
            try {
                while (rs.next()) {
                    nCount = rs.getInt(1);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("isUnparsedJockeyColours exception: " + strJockeyColours + "-" + e.getMessage());
            }
        }
        return (nCount == 0);
    }
    public static ArrayList<String> createUnparsedJockeyColours(ENEStatement stmt, AdditionalRaceInstance link) {
        String strSource = link.getSource();
        ArrayList<String> alRunners = new ArrayList<String>();
        String strQuery = "select trim(jockey_colours) as jockey_colours from " + ("SF".equals(strSource) ? "daily_runners" : "additional_runners") + " left outer join racing_colours_parse on trim(jockey_colours)=rcp_description and rcp_version='" + SmartformRunnerFactory.sm_RCPVersion + "' where race_id = " + link.getRaceId() + " and jockey_colours not in ('', 'Not available') and rcp_description is null";
        if (!"SF".equals(strSource))
            strQuery +=  (" and aru_source='" + strSource + "'");
        System.out.println("createUnparsedJockeyColours: " + strQuery);
        ResultSet rs = stmt.executeQuery(strQuery);
        if (rs != null) {
            try {
                while (rs.next()) {
                    String strJockeyColours = rs.getString("jockey_colours");
                    alRunners.add(strJockeyColours);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("createUnparsedJockeyColours exception: " + link.getTitle() + "-" + link.getRaceId() + "-" + e.getMessage());
                e.printStackTrace();
            }
        }
        return alRunners;
    }
     public static UnregisteredColourSyntax createUnregisteredColourSyntax(ENEStatement statement, String strDescription)
     {
         UnregisteredColourSyntax ucs = null;
             String strSelect = "select ucs_jacket, ucs_sleeves, ucs_cap";
             strSelect += (" from unregistered_colour_syntax where ucs_colours='" + strDescription.replace("'", "''").replace(" & ", " and ") + "'");
             strSelect += " and ucs_year=0";    // and ucs_organisation='UK' 
       
       ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
               if (rs.next()) {
                    ucs = SmartformRunnerFactory.createUnregisteredColourSyntax(rs);
               }
               rs.close();
            }
            catch(SQLException e)
            {
                
            }
        }
        
        return ucs;
     }

    public static ArrayList<SmartformColoursRunner> createDailyColoursRunners(ENEStatement statement, int nRace) 
    {
        ArrayList<SmartformColoursRunner> alRunners = new ArrayList<SmartformColoursRunner>();
        String strRCPVersion = SmartformRunnerFactory.sm_RCPVersion;
        String strSelect = "select cloth_number, name, owner_name, jockey_colours, trainer_name, jockey_name, stall_number, weight_pounds,";
        strSelect += " tack_hood, tack_visor, tack_blinkers, tack_eye_shield, tack_eye_cover, tack_cheek_piece, tack_pacifiers, tack_tongue_strap";
        strSelect += ", coalesce(wi2.wi_jacket, ucs_jacket) as ucs_jacket, coalesce(wi2.wi_sleeves, ucs_sleeves) as ucs_sleeves, coalesce(wi2.wi_cap, ucs_cap) as ucs_cap, coalesce(wi1.wi_owner, wi2.wi_owner,'') as spc_primary_owner, coalesce(wix.wi_owner, '') as owner_clash";
        strSelect += " from ((((((daily_races dra inner join daily_runners dru on dra.race_id=dru.race_id)";
        strSelect += " left outer join unregistered_colour_syntax on replace(trim(dru.jockey_colours), ' & ', ' and ')=ucs_colours and (country=ucs_organisation or (country in ('England', 'Wales', 'Scotland', 'Northern Ireland', 'EireXX') and ucs_organisation = 'UK')) and (year(dra.meeting_date) = ucs_year or ucs_year = 0))";
        strSelect += " left outer join racing_colours_parse rcp on rcp_description=trim(dru.jockey_colours) and rcp_version='" + strRCPVersion + "')";
        strSelect += " left outer join wikipedia_images wi1 on coalesce(ucs_jacket,rcp_jacket)=wi1.wi_jacket and coalesce(ucs_sleeves,rcp_sleeves)=wi1.wi_sleeves and coalesce(ucs_cap,rcp_cap)=wi1.wi_cap and coalesce(rcp_unresolved, '') = '') ";
        strSelect += " left outer join wikipedia_images wi2 on replace(trim(dru.jockey_colours), ' & ', ' and ')=wi2.wi_description)";
        strSelect += " left outer join wikipedia_images wix on dru.owner_name=wix.wi_owner and concat(rcp_jacket, rcp_sleeves, rcp_cap) != concat(wix.wi_jacket, wix.wi_sleeves, wix.wi_cap))";
        strSelect += (" where dru.race_id=" + nRace);
        strSelect += " order by cloth_number";
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
                double dDistanceBehindWinner = 0.0;
                while (rs.next()) {
                    AdditionalRunner runner = new AdditionalRunner("SF", nRace, rs.getInt("cloth_number"), rs.getString("name").trim());
                    runner.setOwner(DbUtils.getDBString(rs, "owner_name"));
                    runner.setJockeyColours(DbUtils.getDBString(rs, "jockey_colours"));
                    String strPrimaryOwner = DbUtils.getDBString(rs, "spc_primary_owner");
                    runner.setPrimaryOwner(strPrimaryOwner);
                    runner.setTrainer(DbUtils.getDBString(rs, "trainer_name"));
                    runner.setJockey(DbUtils.getDBString(rs, "jockey_name"));
                    runner.setWeightPounds(rs.getInt("weight_pounds"));
                    runner.setTack(SmartformRunnerFactory.createSmartformTackObject(rs));
                    runner.setStallNumber(rs.getInt("cloth_number"));   // No cloth_number in AdditionalRaceRunner, so abuse stall number
                    UnregisteredColourSyntax ucs = SmartformRunnerFactory.createUnregisteredColourSyntax(rs);
                    if (ucs != null) {
                        runner.setUnregisteredColourSyntax(ucs);
                    }
                    alRunners.add(runner);
                    
                    String strOwnerClash = DbUtils.getDBString(rs, "owner_clash");
                    if ((!"".equals(strOwnerClash)) && "".equals(strPrimaryOwner))
                    {
                        runner.setPrimaryOwner(runner.getOwner() + "xxx");
                    }
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("createDailyColoursRunners SQLException: " + e.getMessage());
            }
        }
        return alRunners;
    }
    public static String getPrimaryOwner(ENEStatement statement, String strOwnerName, String strJockeyColours) {
        strJockeyColours = strJockeyColours.trim().replaceAll("&", " and ");
        strOwnerName = strOwnerName.trim().replaceAll("&", " and ");
        String strRCPVersion = SmartformRunnerFactory.sm_RCPVersion;
        String strPrimaryOwner = "";
        String strSelect = "select coalesce(wi1.wi_jacket, wi2.wi_jacket, ucs_jacket, rcp_jacket) as ucs_jacket, coalesce(wi1.wi_sleeves, wi2.wi_sleeves, ucs_sleeves, rcp_sleeves) as ucs_sleeves, coalesce(wi1.wi_cap, wi2.wi_cap, ucs_cap, rcp_cap) as ucs_cap, coalesce(wi1.wi_owner, wi2.wi_owner,'') as spc_primary_owner, coalesce(wix.wi_owner, '') as owner_clash";
        strSelect += " from";
            strSelect += " racing_colours_parse rcp";
            strSelect += " left outer join unregistered_colour_syntax on '" + strJockeyColours + "'=ucs_colours and ucs_organisation = 'UK' and ucs_year = 0";
            strSelect += " left outer join wikipedia_images wi1 on coalesce(ucs_jacket,rcp_jacket)=wi1.wi_jacket and coalesce(ucs_sleeves,rcp_sleeves)=wi1.wi_sleeves and coalesce(ucs_cap,rcp_cap)=wi1.wi_cap and coalesce(rcp_unresolved, '') = ''";
            strSelect += " left outer join wikipedia_images wi2 on '" + strJockeyColours + "'=wi2.wi_description";
            strSelect += " left outer join wikipedia_images wix on '" + strOwnerName + "'=wix.wi_owner and concat(rcp_jacket, rcp_sleeves, rcp_cap) != concat(wix.wi_jacket, wix.wi_sleeves, wix.wi_cap)";
            strSelect += " where rcp_description='" + strJockeyColours + "' and rcp_version='" + strRCPVersion + "'";
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try 
            {
                rs.next();      // only one record
                strPrimaryOwner = rs.getString("spc_primary_owner");
                String strOwnerClash = rs.getString("owner_clash");
                System.out.println(strOwnerName + "+" + strJockeyColours + "->" + strPrimaryOwner + "-" + strOwnerClash);
                UnregisteredColourSyntax ucs = SmartformRunnerFactory.createUnregisteredColourSyntax(rs);
                if (ucs != null)
                    System.out.println("UCS: " + ucs.getJacket() + "-" + ucs.getSleeves() + "+" + ucs.getCap());
                rs.close();
            } catch (SQLException e) {
                System.out.println("createAdditionalColoursRunners SQLException: " + e.getMessage());
            }
        }
        return strPrimaryOwner;
    }
    private static ArrayList<SmartformColoursRunner> createAdditionalColoursRunners(ENEStatement statement, int nRace, String strSource, int nRunners) {
        ArrayList<SmartformColoursRunner> alRunners = new ArrayList<SmartformColoursRunner>();
        String strRCPVersion = SmartformRunnerFactory.sm_RCPVersion;
        String strSelect = "select '" + strSource + "' as aru_source, ru.cloth_number, ru.name, ru.owner_name, jockey_colours, ru.trainer_name, ru.jockey_name, in_race_comment, starting_price, coalesce(amended_position, finish_position, 0) as finish_position, coalesce(finish_position, unfinished) as finish_position_string, distance_beaten, ru.stall_number, ru.weight_pounds, ru.age, ru.gender, ";
        strSelect += " ru.tack_hood, ru.tack_visor, ru.tack_blinkers, ru.tack_eye_shield, ru.tack_eye_cover, ru.tack_cheek_piece, ru.tack_pacifiers, ru.tack_tongue_strap";
        strSelect += ("SF".equals(strSource) ? ", case when position_in_betting = 1 then 1 else 0 end as favourite" : ", favourite");
        strSelect += ", coalesce(wi1.wi_jacket, wi2.wi_jacket, ucs_jacket) as ucs_jacket, coalesce(wi1.wi_sleeves, wi2.wi_sleeves, ucs_sleeves) as ucs_sleeves, coalesce(wi1.wi_cap, wi2.wi_cap, ucs_cap) as ucs_cap, coalesce(wi1.wi_owner, wi2.wi_owner,'') as spc_primary_owner, coalesce(wix.wi_owner, '') as owner_clash";
        if ("SF".equals(strSource)) {
            strSelect += ", ru.official_rating, ru.penalty_weight, ru.over_weight, ru.days_since_ran, ru.distance_travelled, ru.distance_won, ru.position_in_betting, ru.starting_price_decimal";
            strSelect += " from (((((((( historic_runners ru inner join historic_races ra on ra.race_id=ru.race_id)";
            strSelect += " left outer join daily_races dra on ra.race_id=dra.race_id)";
            strSelect += " left outer join daily_runners dru on dra.race_id=dru.race_id and ru.name=dru.name)";
            strSelect += " left outer join unregistered_colour_syntax on replace(trim(dru.jockey_colours), ' & ', ' and ')=ucs_colours and (country=ucs_organisation or (country in ('England', 'Wales', 'Scotland', 'Northern Ireland', 'EireXX') and ucs_organisation = 'UK')) and (year(ra.meeting_date) = ucs_year or ucs_year = 0))";
            strSelect += " left outer join racing_colours_parse rcp on rcp_description=trim(dru.jockey_colours) and rcp_version='" + strRCPVersion + "')";
            strSelect += " left outer join wikipedia_images wi1 on coalesce(ucs_jacket,rcp_jacket)=wi1.wi_jacket and coalesce(ucs_sleeves,rcp_sleeves)=wi1.wi_sleeves and coalesce(ucs_cap,rcp_cap)=wi1.wi_cap and coalesce(rcp_unresolved, '') = '') ";
            strSelect += " left outer join wikipedia_images wi2 on replace(trim(dru.jockey_colours), ' & ', ' and ')=wi2.wi_description)";
            strSelect += " left outer join wikipedia_images wix on replace(dru.owner_name, ' & ', ' and ')=wix.wi_owner and concat(rcp_jacket, rcp_sleeves, rcp_cap) != concat(wix.wi_jacket, wix.wi_sleeves, wix.wi_cap))";
            strSelect += (" where ru.race_id=" + nRace);
        } else {
            strSelect += ",  0 as official_rating, ru.weight_penalty as penalty_weight, 0 as over_weight, null as days_since_ran, null as distance_travelled, null as distance_won, null as position_in_betting, starting_price_decimal";
            strSelect += " from (((((((( additional_runners ru inner join additional_races ra on aru_source=ara_source and ru.race_id=ra.race_id)";
            strSelect += " left outer join additional_race_link on aru_source = arl_source and ru.race_id=arl_race_id)";
            strSelect += " left outer join additional_race_data on ard_name=arl_name)";
            strSelect += " left outer join unregistered_colour_syntax on replace(trim(ru.jockey_colours), ' & ', ' and ')=ucs_colours and (ard_country=ucs_organisation or (ard_country in ('England', 'Wales', 'Scotland', 'Northern Ireland', 'EireXX') and ucs_organisation = 'UK')) and (year(ra.meeting_date) = ucs_year or ucs_year = 0))";
            strSelect += " left outer join racing_colours_parse rcp on rcp_description=trim(ru.jockey_colours) and rcp_version='" + strRCPVersion + "')";
            strSelect += " left outer join wikipedia_images wi1 on coalesce(ucs_jacket,rcp_jacket)=wi1.wi_jacket and coalesce(ucs_sleeves,rcp_sleeves)=wi1.wi_sleeves and coalesce(ucs_cap,rcp_cap)=wi1.wi_cap and coalesce(rcp_unresolved, '') = '') ";
            strSelect += " left outer join wikipedia_images wi2 on replace(trim(ru.jockey_colours), ' & ', ' and ')=wi2.wi_description)";
            strSelect += " left outer join wikipedia_images wix on replace(ru.owner_name, ' & ', ' and ')=wix.wi_owner and concat(rcp_jacket, rcp_sleeves, rcp_cap) != concat(wix.wi_jacket, wix.wi_sleeves, wix.wi_cap))";
            strSelect += "where ru.race_id=" + nRace + " and aru_source='" + strSource + "'";
        }
        if (nRunners > 0) {
            strSelect += " and coalesce(amended_position, finish_position, 99) <= " + nRunners; // 20151017 remove finish_position is not null 
        } else if (nRunners == 0) {
            strSelect += " and (unfinished is null or amended_position is not null)";
        } else {
            strSelect += " and coalesce(unfinished, '') != 'Non-Runner'";
        }
        strSelect += " order by coalesce(amended_position, finish_position, 99)";
        if ("SF".equals(strSource)) {
            strSelect += ", num_fences_jumped desc";
        }
        System.out.println("createAdditionalColoursRunners: " + strSelect);
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try 
            {
                double dDistanceBehindWinner = 0.0;
                while (rs.next()) 
                {
                    AdditionalRunner runner = new AdditionalRunner(strSource, nRace, rs.getInt("cloth_number"), rs.getString("name").trim());
                    int nFinishPosition = rs.getInt("finish_position");
                    String strFinishPositionString = DbUtils.getDBString(rs, "finish_position_string");
                    runner.setOwner(DbUtils.getDBString(rs, "owner_name"));
                    runner.setJockeyColours(DbUtils.getDBString(rs, "jockey_colours"));
                    String strPrimaryOwner = DbUtils.getDBString(rs, "spc_primary_owner");
                    runner.setPrimaryOwner(strPrimaryOwner);
                    runner.setTrainer(DbUtils.getDBString(rs, "trainer_name"));
                    runner.setJockey(DbUtils.getDBString(rs, "jockey_name"));
                    runner.setInRunning(DbUtils.getDBString(rs, "in_race_comment"));
                    runner.setStartingPrice(DbUtils.getDBString(rs, "starting_price"));
                    runner.setFinishPosition(strFinishPositionString);
                    runner.setWeightPounds(rs.getInt("weight_pounds"));
                    runner.setOfficialRating(rs.getInt("official_rating"));
                    runner.setPenaltyWeight(rs.getInt("penalty_weight"));
                    runner.setDaysSinceRan(rs.getInt("days_since_ran"));
                    runner.setDistanceTravelled(rs.getInt("distance_travelled"));
                    runner.setTack(SmartformRunnerFactory.createSmartformTackObject(rs));
                    runner.setAge(rs.getInt("age"));
                    runner.setGender(rs.getString("gender"));
                    int nPositionInBetting = rs.getInt("position_in_betting");
                    if (nPositionInBetting > 0)
                        runner.setPositionInBetting(nPositionInBetting);
                    double dSP = rs.getDouble("starting_price_decimal");
                    if (dSP > 0)
                        runner.setStartingPriceDecimal(dSP);

                    int nFavourite = rs.getInt("favourite");
                    if (nFavourite == 1) {
                        runner.setFavourite();
                    }
                    double dDistanceBeaten = rs.getDouble("distance_beaten");
                    if (nFinishPosition > 1) {
                        dDistanceBehindWinner += dDistanceBeaten;
                        runner.setDistanceBeaten(dDistanceBeaten);
                        runner.setDistanceBehindWinner(dDistanceBehindWinner);
                    }
                    else
                    {
                        double dDistanceWon = rs.getDouble("distance_won");
                        runner.setDistanceWon(dDistanceWon);
                    }
                    runner.setStallNumber(rs.getInt("stall_number"));
                    UnregisteredColourSyntax ucs = SmartformRunnerFactory.createUnregisteredColourSyntax(rs);
                    if (ucs != null) {
                        runner.setUnregisteredColourSyntax(ucs);
                    }
                    alRunners.add(runner);
                    
                    String strOwnerClash = DbUtils.getDBString(rs, "owner_clash");
                    if ((!"".equals(strOwnerClash)) && "".equals(strPrimaryOwner))
                    {
                        runner.setPrimaryOwner(runner.getOwner() + "xxx");
                    }
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("createAdditionalColoursRunners SQLException: " + e.getMessage());
            }
        }
        return alRunners;
    }
}
