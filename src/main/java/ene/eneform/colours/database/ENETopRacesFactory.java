/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.database;

import ene.eneform.colours.bos.ENETopRace;
import ene.eneform.colours.bos.ENETopRaceWinner;
import ene.eneform.colours.service.WikipediaService;
import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.mero.parse.ENEColoursParser;
import ene.eneform.utils.DbUtils;
import ene.eneform.utils.ENEStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Simon
 */
@Service
@RequiredArgsConstructor
public class ENETopRacesFactory {
private final WikipediaService wikipediaService;
public ENETopRace getTopRace(ENEStatement statement, int nRace)
{
    ENETopRace race = null;
    Statement stmt = null;
        String strQuery = "select thr_id, thr_title, thr_country, thr_course, thr_date_description, thr_conditions, thr_age_range, thr_distance, thr_distance_units";
        strQuery += " from top_historic_races";
        strQuery += (" where thr_id = " + nRace);

        ResultSet rs  = statement.executeQuery(strQuery);
        if (rs != null)
        {
            try
             {
                 while (rs.next())
                 {
                     race = new ENETopRace(rs.getInt("thr_id"));
                     race.setTitle(rs.getString("thr_title"));
                     race.setConditions(rs.getString("thr_conditions"));
                     race.setCountry(rs.getString("thr_country"));
                     race.setCourse(rs.getString("thr_course"));
                     race.setAgeRange(rs.getString("thr_age_range"));
                     race.setDistance(rs.getInt("thr_distance"));
                     race.setDistanceUnits(rs.getString("thr_distance_units"));
                     race.setDateDescription(rs.getString("thr_date_description"));
                 }
                 rs.close();
             }
             catch(SQLException e)
             {
             }
        }
        return race;
    }
public int countTopRaceWinners(ENEStatement statement, String strWhere)
{
    int nCount = -1;
         String strQuery = "select count(*) from top_historic_winners";
        strQuery += (" where " + strWhere);

        System.out.println("countTopRaceWinners: " + strQuery);
        ResultSet rs  = statement.executeQuery(strQuery);
        if (rs != null)
        {
            try
             {
                 while (rs.next())
                 {
                      nCount = rs.getInt(1);
                 }
                 rs.close();
             }
             catch(SQLException e)
             {
             }
    }
    System.out.println("countTopRaceWinners: " + nCount);
            return nCount;
    }
public ArrayList<ENETopRaceWinner> getTopRaceWinners(ENEStatement statement, String strWhere, boolean bAscending, int nStart, int nMaxLimit)
{
    ArrayList<ENETopRaceWinner> alWinners = new ArrayList<ENETopRaceWinner>();
        String strQuery = "select thw_race_id, thw_year, thw_winner, thw_jockey, thw_trainer, thw_owner, thw_age, thw_gender, thw_colours, thw_dead_heat, thw_comments, thw_sire, thw_dam, thw_dam_sire, thw_colour, thw_bred, thw_foaling_year";
        strQuery += " from top_historic_winners";
        strQuery += (" where " + strWhere);
        strQuery += (" order by thw_year " + (bAscending ? "asc" : "desc"));   // order is variable

        if (nMaxLimit > 0)
            strQuery += " LIMIT " + nStart + "," + nMaxLimit;

        System.out.println("getTopRaceWinners: " + strQuery);
        ResultSet rs  = statement.executeQuery(strQuery);
        if (rs != null)
        {
            try
            {
                while (rs.next())
                {
                    ENETopRaceWinner winner = new ENETopRaceWinner(rs.getInt("thw_race_id"), rs.getInt("thw_year"));
                    winner.setHorse(DbUtils.getDBString(rs, "thw_winner").trim());
                    winner.setJockey(DbUtils.getDBString(rs, "thw_jockey").trim());
                    winner.setTrainer(DbUtils.getDBString(rs, "thw_trainer").trim());
                    winner.setOwner(DbUtils.getDBString(rs, "thw_owner").trim());
                    winner.setAge(rs.getInt("thw_age"));
                    winner.setGender(DbUtils.getDBChar(rs, "thw_gender"));
                    winner.setColours(DbUtils.getDBString(rs, "thw_colours").trim());
                    winner.setDeadHeat(rs.getInt("thw_dead_heat"));
                    winner.setComments(DbUtils.getDBString(rs, "thw_comments"));
                    //thw_sire, thw_dam, thw_dam_sire, thw_colour, thw_bred, thw_foaling_year
                    winner.setSire(DbUtils.getDBString(rs, "thw_sire").trim());
                    winner.setDam(DbUtils.getDBString(rs, "thw_dam").trim());
                    winner.setDamSire(DbUtils.getDBString(rs, "thw_dam_sire").trim());
                    winner.setColour(DbUtils.getDBString(rs, "thw_colour").trim());
                    winner.setBred(DbUtils.getDBString(rs, "thw_bred").trim());
                    winner.setFoalingYear(rs.getInt("thw_foaling_year"));
                    alWinners.add(winner);

                }
                rs.close();
            }
            catch(SQLException e)
            {
            }

        }
        
        System.out.println("getTopRaceWinners: " + alWinners.size());
        return alWinners;
    }

    public void generateSVGTopRaceWinners(ENEStatement statement, int nRace, String strWhere)
    {
        String strWhere1 = getTopRaceWinnersWhereClause(nRace) + " " + strWhere;
        ArrayList<ENETopRaceWinner> alWinners = getTopRaceWinners(statement, strWhere1, true, 0, 0);
        System.out.println("generateSVGTopRaceWinners: " + nRace + " Number of colours: " + alWinners.size());
        // images should be named by race id and year
        //ENEColoursSVGFactory.createSVGTopRaceWinners(nRace, alWinners, ENEColoursEnvironment.DEFAULT_LANGUAGE);
        for(int i = 0; i < alWinners.size(); i++)
        {
            ENETopRaceWinner winner = alWinners.get(i);
            int nYear = winner.getYear();
            String strColours = winner.getColours();
            ENERacingColours colours = new ENEColoursParser("en", strColours, "").parse();
            try
            {
                String strFileName = getTopRaceFileName(String.valueOf(nYear), nRace);
                wikipediaService.createImageFile(strFileName, colours, "en", true, true);
            }
            catch(FileNotFoundException e)
            {

            }
            catch(IOException e)
            {

            }
        }

     }  
    public String getTopRaceWinnersWhereClause(int nRace)
    {
        return ("thw_race_id = " + nRace);
    }

    public void generateAllSVGTopRaces(ENEStatement statement)
    {
       for (int i = 1; i <= 10; i++)
        {
            generateSVGTopRaceWinners(statement, i, "and thw_year >= 2011"); //  and thw_year >= 2000");
        }
    }

    public String getTopRaceFileName(String strFileName, int nRace) {
        String strFullDirectory = ENEColoursEnvironment.getInstance().getVariable("SVG_OUTPUT_DIRECTORY") + ENEColoursEnvironment.getInstance().getVariable("SVG_IMAGE_PATH") + "races/" + nRace + "/mero";
        String strFullFileName = strFullDirectory + "/" + strFileName + ".svg";
        return strFullFileName;
    }
 
}
