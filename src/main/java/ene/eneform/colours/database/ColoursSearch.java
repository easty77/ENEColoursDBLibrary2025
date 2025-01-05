/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.database;

import org.json.JSONArray;
import org.json.JSONObject;
import ene.eneform.colours.bos.ENEOwnerColours;
import ene.eneform.colours.bos.ENETopRaceWinner;
import ene.eneform.utils.DbUtils;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class ColoursSearch {
private static String sm_strDisplayNameDefn = "case when ro_display_name is null or ro_display_name='' then replace(trim(concat(ro_title, ' ', ro_first_name, ' ', ro_family_name, ' ', ro_suffix)),'  ', ' ') else ro_display_name end";

public static JSONObject searchTopRacesOwnersJSON(ENEStatement statement, String strOwnerAttribute, String strWhere, String strOrder, int nPageSize, int nMatches, int nFirst, boolean bAscending, boolean bOwners)
{
    int nLast = 0;
    ArrayList<ENETopRaceWinner> list = null;
    if (nMatches == -1)
    {
        if (bOwners)
             nMatches = ENETopRacesOwnerFactory.countOwnerColours(statement, strWhere);
        else
            nMatches = ENETopRacesFactory.countTopRaceWinners(statement, strWhere);
        
         if (nMatches > 0)
         {
            if (bOwners)
                list = ENETopRacesOwnerFactory.findOwnerColours(statement, strWhere, 0, nPageSize);
            else
                list = ENETopRacesFactory.getTopRaceWinners(statement, strWhere, bAscending, 0, nPageSize);
            nLast = (nMatches < nFirst + nPageSize) ? nMatches : nFirst + nPageSize - 1;
         }
         else
         {
             nFirst = 0;
             nLast = 0;
             list = new ArrayList<ENETopRaceWinner>();
         }
    }
    else
    {
        // this is a next or previous, so retrieve first
        if (bOwners)
            list = ENETopRacesOwnerFactory.findOwnerColours(statement, strWhere, nFirst - 1, nPageSize);
        else
            list = ENETopRacesFactory.getTopRaceWinners(statement, strWhere, bAscending, nFirst - 1, nPageSize);
        nLast = (nMatches < nFirst + nPageSize) ? nMatches : nFirst + nPageSize - 1;
    }

    JSONObject objSearch = new JSONObject();
    objSearch.put("search_type", bOwners ? "top_races_owner" : "top_race_winner");
    objSearch.put("data_type", "race");
    objSearch.put("first", nFirst);
    objSearch.put("last", nLast);
    objSearch.put("matches", nMatches);
    if (list.size() > 0)
    {
        JSONArray array = new JSONArray();
        Iterator<ENETopRaceWinner> iter = list.iterator();
        while(iter.hasNext())
        {
            ENETopRaceWinner toprace = iter.next();
            JSONObject objColours = new JSONObject();
            objColours.put("race_id", toprace.getRaceId());
            objColours.put("race_name", toprace.getRaceTitle());
            objColours.put("year", toprace.getYear());
            objColours.put("horse_name", toprace.getHorse());
            objColours.put("owner_name", toprace.getOwner().replace("&", "&amp;"));
            objColours.put("jockey_name", toprace.getJockey());
            objColours.put("colours", toprace.getColours());
            array.put(objColours);
        }

        objSearch.put("colours", array);
    }
    
    return objSearch;
}
public static JSONObject searchOwnersJSON(ENEStatement statement, String strOwnerAttribute, String strWhere, String strOrder, int nPageSize, int nMatches, int nFirst)
{
    int nLast = 0;
    ArrayList<ENEOwnerColours> list = null;
    if (nMatches == -1)
    {
         nMatches = ColoursSearch.countOwnerColours(statement, strWhere);
        
         if (nMatches > 0)
         {
            list = ColoursSearch.findOwnerColours(statement, strWhere, strOwnerAttribute, strOrder, 0, nPageSize);
            nLast = (nMatches < nFirst + nPageSize) ? nMatches : nFirst + nPageSize - 1;
         }
         else
         {
             nFirst = 0;
             nLast = 0;
             list = new ArrayList<ENEOwnerColours>();
         }
    }
    else
    {
        // this is a next or previous, so retrieve first
        list = ColoursSearch.findOwnerColours(statement, strWhere, strOwnerAttribute, strOrder, nFirst - 1, nPageSize);
        nLast = (nMatches < nFirst + nPageSize) ? nMatches : nFirst + nPageSize - 1;
    }

    JSONObject objSearch = new JSONObject();
    objSearch.put("data_type", "organisation");
    objSearch.put("first", nFirst);
    objSearch.put("last", nLast);
    objSearch.put("matches", nMatches);
    if (list.size() > 0)
    {
        JSONArray array = new JSONArray();
        Iterator<ENEOwnerColours> iter = list.iterator();
        while(iter.hasNext())
        {
            ENEOwnerColours colours = iter.next();
            JSONObject objColours = new JSONObject();
            int nColours = colours.getColoursNr();
            if(nColours > 1)
            {
                objColours.put("colours_nr", StringUtils.getOrdinalString(nColours));
            }
            objColours.put("colours", colours.getColours());
            objColours.put("organisation", colours.getOrganisation());
            objColours.put("year", colours.getYear());
            objColours.put("filename", colours.getLabel());
            objColours.put("owner_name", colours.getOwnerName().replace("&", "&amp;"));
            array.put(objColours);
        }

        objSearch.put("colours", array);
    }
    
    return objSearch;
}
public static JSONObject getColourSearchJSON(ENEStatement statement, String strOwnerAttribute, String strOwnerSearch, String ss_op, String strColoursSearch, String cs_op, String strOrganisation, String strOrgType, int nYear, int nPageSize, int nMatches, int nFirst, int nRace, boolean bAscending)
{
    if ((strOwnerAttribute == null) || ("".equals(strOwnerAttribute)))
        strOwnerAttribute = "ro_display_name";
    if (strOwnerSearch == null)
        strOwnerSearch = "";
    else
    {
        if ("contains".equalsIgnoreCase(ss_op))
            strOwnerSearch = ('%' + strOwnerSearch + '%');
        else if ("ends".equalsIgnoreCase(ss_op))
            strOwnerSearch = ('%' + strOwnerSearch);
        else if ("starts".equalsIgnoreCase(ss_op))
            strOwnerSearch = (strOwnerSearch += '%');
    }

    if (strColoursSearch == null)
        strColoursSearch = "";
    else
    {
        if ("contains".equalsIgnoreCase(cs_op))
            strColoursSearch = ('%' + strColoursSearch + '%');
        else if ("ends".equalsIgnoreCase(cs_op))
            strColoursSearch = ('%' + strColoursSearch);
        else if ("starts".equalsIgnoreCase(cs_op))
            strColoursSearch = (strColoursSearch += '%');
    }

    if (strOrganisation == null)
        strOrganisation = "";
 
    if ("top_races_owners".equals(strOrganisation))
    {
        String strWhere = ENETopRacesOwnerFactory.getOwnerColoursWhereClause(strOwnerSearch, strColoursSearch);

       return searchTopRacesOwnersJSON(statement, strOwnerAttribute, strWhere, "", nPageSize, nMatches, nFirst, bAscending, true);
    }
    else if ("top_race_winner".equals(strOrganisation))
    {
        String strWhere = ENETopRacesFactory.getTopRaceWinnersWhereClause(nRace);
        // need bAscending
        return searchTopRacesOwnersJSON(statement, strOwnerAttribute, strWhere, "", nPageSize, nMatches, nFirst, bAscending, false);
    }
    else
    {
    String strWhere = ColoursSearch.getOwnerColoursWhereClause(
            strOwnerAttribute,
            strOwnerSearch,
            strColoursSearch,
            strOrganisation,
            strOrgType,
            nYear);

   String strOrder = ColoursSearch.getOwnerColoursOrderClause(
            strOwnerAttribute,
            strOwnerSearch,
            strColoursSearch,
            strOrganisation,
            strOrgType,
            nYear);
        return searchOwnersJSON(statement, strOwnerAttribute, strWhere, strOrder, nPageSize, nMatches, nFirst);
    }
}
    public static String getOwnerColoursOrderClause(String strOwnerAttribute, String strOwner, String strColours, String strOrganisation, String strOrgType, int nYear)
    {
       if ("ro_display_name".equals(strOwnerAttribute))
       {
            strOwnerAttribute = sm_strDisplayNameDefn;
       }

        if (strOwner.indexOf("'") >= 0)
            strOwner = strOwner.replace("'", "''");
            
        if (strColours.indexOf("'") >= 0)
            strColours = strColours.replace("'", "''");

       String strOrder="";
       if (nYear < 1000)        // not a specific year
       {
          strOrder = "ro_year";
       }
       if (!"".equals(strOrder))
           strOrder += ", ";

       strOrder += (strOwnerAttribute + ", " + sm_strDisplayNameDefn + ", rc_colours_nr, rc_colours");

       return strOrder;
    }
    public static String getOwnerColoursWhereClause(String strOwnerAttribute, String strOwner, String strColours, String strOrganisation, String strOrgType, int nYear)
    {
        if (strOwner.indexOf("'") >= 0)
            strOwner = strOwner.replace("'", "''");
            
        if (strColours.indexOf("'") >= 0)
            strColours = strColours.replace("'", "''");

       String strWhere="";
       if ("ro_display_name".equals(strOwnerAttribute))
       {
            strOwnerAttribute = sm_strDisplayNameDefn;
       }

       if ("".equals(strOwner))
       {

       }
       else if(strOwner.indexOf("%") >= 0)
       {
           strWhere = strOwnerAttribute + " like '" + strOwner +"'";
       }
       else
       {
            strWhere = strOwnerAttribute + " = '" + strOwner +"'";
       }
       if ("".equals(strColours))
       {
       }
       else if(strColours.indexOf("%") >= 0)
       {
           if (!"".equals(strWhere))
               strWhere += " and";
           strWhere += " rc_colours like '" + strColours +"'";
       }
       else
       {
            if (!"".equals(strWhere))
               strWhere += " and";
           strWhere += " rc_colours = '" + strColours +"'";
       }
       if (!"".equals(strOrganisation))
       {
           if (!"".equals(strWhere))
               strWhere += " and";
           strWhere += (" ro_organisation = '" + strOrganisation + "'");
       }

       if (strOrgType != null)
       {
            if (!"".equals(strWhere))
                strWhere += " and";
            strWhere += (" ro_orgtype = '" + strOrgType + "'");
       }
       
       if (nYear > 0)
       {
          if (!"".equals(strWhere))
               strWhere += " and";
          if (nYear < 100)      // century
            strWhere += (" floor(ro_year/100) = " + nYear + "");
          else
            strWhere += (" ro_year = " + nYear + "");
       }

       if (!"".equals(strWhere))
           strWhere = " and " + strWhere;   // prefix where with and - all statements join tables so already have where elements

       return strWhere;
    }
    public static int countOwnerColours(ENEStatement statement, String strWhere)
    {
        // two possibilities: 1) a name 3) a wildcard search
        ArrayList<ENEOwnerColours> list = new ArrayList<ENEOwnerColours>();

        String strQuery = "select count(*) from registered_owners, registered_colours where rc_item=ro_item and rc_organisation=ro_organisation and rc_orgtype = ro_orgtype and rc_year = ro_year " + strWhere;

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

                rs.close();
            }
            catch(SQLException e)
            {
                    System.out.println("EXCEPTION countOwnerColoursStatement: " + e.getMessage());
            }
        }

         return nCount;
    }
    public static ArrayList<ENEOwnerColours> findOwnerColours(ENEStatement statement, String strWhere, String strOwnerAttribute, String strOrder, int nStart, int nMaxLimit)
    {
        return findOwnerColours(statement, strWhere, strOwnerAttribute, strOrder, nStart, nMaxLimit, false);
    }

   public static ArrayList<ENEOwnerColours> findOwnerColours(ENEStatement statement, String strWhere, String strOwnerAttribute, String strOrder, int nStart, int nMaxLimit, boolean bSyntaxOnly)
    {
        // 20120917 - Add join to Syntax table - make inner join for now (testing only)
        // two possibilities: 1) a name 3) a wildcard search
        ArrayList<ENEOwnerColours> list = new ArrayList<ENEOwnerColours>();

        String strJoin = bSyntaxOnly ? "inner" : "left outer";
        String strQuery = "select ro_item, " + sm_strDisplayNameDefn + " as ro_display_name, rc_colours, rc_colours_nr, rc_language, ro_organisation, ro_year, rcs_jacket, rcs_sleeves, rcs_cap from ((registered_owners inner join registered_colours on rc_item=ro_item and rc_organisation=ro_organisation and rc_year = ro_year and rc_orgtype = ro_orgtype) " + strJoin + " join registered_colour_syntax on ro_organisation=rcs_organisation and ro_year=rcs_year and rc_item=rcs_item and rc_colours_nr=rcs_colours_nr) ";
        strQuery += ("where 1 = 1 " + strWhere);
        strQuery += " order by " + strOrder;

        if (nMaxLimit > 0)
            strQuery += " LIMIT " + nStart + "," + nMaxLimit;

        System.out.print("findOwnerColoursStatement: " + strQuery);
        ResultSet rs = statement.executeQuery(strQuery);
        if (rs != null)
        {
            try
            {
                int nCount = 0;
                while (rs.next())
                {
                    nCount++;
                    ENEOwnerColours colours = new ENEOwnerColours(DbUtils.getDBString(rs, "ro_display_name"), DbUtils.getDBString(rs, "rc_colours"), rs.getInt("rc_colours_nr"),
                            DbUtils.getDBString(rs, "ro_organisation"), rs.getInt("ro_year"), DbUtils.getDBString(rs, "rc_language"));

                    String strJacketSyntax = rs.getString("rcs_jacket");
                    if ((strJacketSyntax != null) && !"".equals(strJacketSyntax))
                    {
                        colours.setJacketSyntax(strJacketSyntax);
                        colours.setSleevesSyntax(rs.getString("rcs_sleeves"));
                        colours.setCapSyntax(rs.getString("rcs_cap"));
                    }
                    colours.setLabel(rs.getInt("ro_item") + "_" + rs.getInt("rc_colours_nr"));
                    list.add(colours);
                }
            
                rs.close();
            }
            catch(SQLException e)
            {
                System.out.println("EXCEPTION findOwnerColoursStatement: " + e.getMessage());
            }
        }
        
  
        System.out.print("findOwnerColoursStatement: " + list.size());
        return list;
    }
}
