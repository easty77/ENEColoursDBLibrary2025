/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.database;

import ene.eneform.colours.bos.ENETopRaceWinner;
import ene.eneform.utils.DbUtils;
import ene.eneform.utils.ENEStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public class ENETopRacesOwnerFactory {

    public static String getOwnerColoursWhereClause(String strOwner, String strColours)
    {
        // thw_race_id, thw_year, thw_winner, thw_jockey, thw_trainer, thw_owner, thw_age, thw_gender, thw_colours, thw_dead_heat, thw_comments
        // top_historic_winners
       String strWhere="";
       if ("".equals(strOwner))
       {

       }
       else if(strOwner.indexOf("%") >= 0)
       {
           strWhere = "thw_owner like '" + strOwner +"'";
       }
       else
       {
            strWhere = "thw_owner = '" + strOwner +"'";
       }
       if ("".equals(strColours))
       {
       }
       else if(strColours.indexOf("%") >= 0)
       {
           if (!"".equals(strWhere))
               strWhere += " and";
           strWhere += " thw_colours like '" + strColours +"'";
       }
       else
       {
            if (!"".equals(strWhere))
               strWhere += " and";
           strWhere += " thw_colours = '" + strColours +"'";
       }

       // do not prefix where with and - some staments do not have existing where content

       return strWhere;
    }
    public static int countOwnerColours(ENEStatement statement, String strWhere)
    {
        // two possibilities: 1) a name 3) a wildcard search
        int nReturn = 0;
        ArrayList<ENETopRaceWinner> list = new ArrayList<ENETopRaceWinner>();

        String strQuery = "select count(*) from top_historic_winners where " + strWhere;

        ResultSet rs = statement.executeQuery(strQuery);
        int nCount = 0;
        if (rs != null)
        {
            try
            {
                while (rs.next())
                {
                    nCount=  rs.getInt(1);
                }
            }
            catch(SQLException e)
            {
                
            }
        }

        return nCount;
    }
    public static ArrayList<ENETopRaceWinner> findOwnerColours(ENEStatement statement, String strWhere, int nStart, int nMaxLimit)
    {
        ArrayList<ENETopRaceWinner> list = new ArrayList<ENETopRaceWinner>();

        String strQuery = "select thr_title, thw_race_id, thw_year, thw_winner, thw_jockey, thw_trainer, thw_owner, thw_age, thw_gender, thw_colours, thw_dead_heat, thw_comments";
        strQuery += " from top_historic_winners, top_historic_races where thr_id = thw_race_id ";
        if (!"".equals(strWhere))
            strQuery += " and " + strWhere;
        strQuery += " order by thw_year, thr_title, thw_owner, thw_colours";

        if (nMaxLimit > 0)
            strQuery += " LIMIT " + nStart + "," + nMaxLimit;

        ResultSet rs = statement.executeQuery(strQuery);
        int nCount = 0;
        if(rs != null)
        {
            try
            {
                while (rs.next())
                {
                    nCount++;
                    ENETopRaceWinner winner = new ENETopRaceWinner(rs.getInt("thw_race_id"), rs.getInt("thw_year"));
                    winner.setRaceTitle(DbUtils.getDBString(rs, "thr_title").trim());
                    winner.setHorse(DbUtils.getDBString(rs, "thw_winner").trim());
                    winner.setJockey(DbUtils.getDBString(rs, "thw_jockey").trim());
                    winner.setTrainer(DbUtils.getDBString(rs, "thw_trainer").trim());
                    winner.setOwner(DbUtils.getDBString(rs, "thw_owner").trim());
                    winner.setAge(rs.getInt("thw_age"));
                    winner.setGender(DbUtils.getDBChar(rs, "thw_gender"));
                    winner.setColours(DbUtils.getDBString(rs, "thw_colours").trim());
                    winner.setDeadHeat(rs.getInt("thw_dead_heat"));
                    winner.setComments(DbUtils.getDBString(rs, "thw_comments"));
                    list.add(winner);
                }
            }
            catch(SQLException e)
            {
                
            }
        }
        return list;
    }

}
