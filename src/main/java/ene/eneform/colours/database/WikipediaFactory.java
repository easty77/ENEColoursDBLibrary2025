/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.colours.bos.HorseOwnerQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import ene.eneform.smartform.bos.UnregisteredColourSyntax;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;

import java.io.*;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 *
 * @author Simon
 */
public class WikipediaFactory {

public static boolean insertOwnerUniqueColours(ENEStatement statement, String strOwner, String strColours)
{
    String strInsert = "insert into owner_unique_colours (owner_name, jockey_colours) values (";
    strInsert += ("'" + strOwner + "', '" + strColours + "')");

    int nRows = statement.executeUpdate(strInsert);
    
    return (nRows == 1);
}
public static String selectWikipediaOwnerByColours(ENEStatement stmt, String strDescription, String strCountry)
{
    if ((strCountry == null) || ("".equals(strCountry)) || ("GB".equals(strCountry)))
        strCountry = "UK";
        
    String strQuery = "select coalesce(wi1.wi_owner, wi2.wi_owner, '')";
    strQuery += " from racing_colours_parse rcp";
    strQuery += " left outer join unregistered_colour_syntax on '" + strDescription.replace("'", "''").replace(" & ", " and ")+ "'=ucs_colours and ucs_organisation='" + strCountry + "'";
    strQuery += " left outer join wikipedia_images wi1 on wi1.wi_description='"+ strDescription.replace("'", "''").replace(" & ", " and ") + "'";
    strQuery += " left outer join wikipedia_images wi2 on wi2.wi_jacket=coalesce(ucs_jacket, rcp_jacket) and wi2.wi_sleeves=coalesce(ucs_sleeves, rcp_sleeves) and wi2.wi_cap=coalesce(ucs_cap, rcp_cap)";
    strQuery += (" where replace(rcp_description, ' ', '')=replace('" + strDescription.replace("'", "''") + "', ' ', '') and rcp_version='" + SmartformRunnerFactory.sm_RCPVersion + "'");

    ResultSet rs = stmt.executeQuery(strQuery);
    String strOwner = "";
    if (rs != null)
    {
        try
        {
            if (rs.next())
            {
                 strOwner = rs.getString(1);
            }
            rs.close();
        }
        catch(SQLException e)
        {
            
        }

    }
    if ("".equals(strOwner))
        System.out.println("selectWikipediaOwnerByColours: " + strQuery);
    
    return strOwner;
}
public static JSONArray selectAllWikipediaRacesLatest(ENEStatement statement)
{
    int nDays = JCEventsFactory.getEventDayInterval(statement, "latest_wikipedia_races");
    JSONArray array = selectAllWikipediaRacesByDate(statement, nDays);
    JCEventsFactory.updateEventDate(statement, "latest_wikipedia_races");
    return array;
}
public static JSONArray selectAllWikipediaRacesByDate(ENEStatement stmt, int nDayOffset)
{
    JSONArray array = new JSONArray();
    String strQuery = "select distinct ary_name, ary_race_id, ary_source, ary_date, arw_language, arw_wikipedia_ref from additional_race_data, additional_race_year, additional_race_wikipedia";
    strQuery += " where ard_name=ary_name and ary_name=arw_name";
    strQuery += " and ary_date>=date_sub(current_date, interval " + nDayOffset + " day) and ary_date < current_date";

    ResultSet rs = stmt.executeQuery(strQuery);
    if (rs != null)
    {
        try
        {
            while (rs.next())
            {
                JSONObject race = new JSONObject();
                race.put("name", rs.getString("ary_name"));
                race.put("language", rs.getString("arw_language"));
                race.put("id", rs.getInt("ary_race_id"));
                race.put("source", rs.getString("ary_source"));
                race.put("date", rs.getString("ary_date"));
                race.put("url", rs.getString("arw_wikipedia_ref"));
                array.put(race);
             }
            rs.close();
        }
        catch(SQLException e)
        {
            
        }

    }
    return array;
}
public static ArrayList<String> selectWikipediaRacesByDate(ENEStatement stmt, int nDayOffset, String strWhere, String strLanguage)
{
    ArrayList<String> alOwners = new ArrayList<String>();
    String strQuery = "select distinct ary_name from additional_race_data, additional_race_year, additional_race_wikipedia";
    strQuery += " where ard_name=ary_name and ary_name=arw_name and arw_language='" + strLanguage + "'";
    strQuery += " and ary_date>=date_sub(current_date, interval " + nDayOffset + " day) and ary_date < current_date";
    if ((strWhere != null) && !"".equals(strWhere))
        strQuery += " and " + strWhere;

    ResultSet rs = stmt.executeQuery(strQuery);
    if (rs != null)
    {
        try
        {
            while (rs.next())
            {
                 alOwners.add(rs.getString(1));
             }
            rs.close();
        }
        catch(SQLException e)
        {
            
        }

    }
    return alOwners;
}
public static UnregisteredColourSyntax createUnregisteredColourSyntax(ENEStatement statement, String strJockeyColours)
{
    String strSelect = "select * from (select wi_jacket as ucs_jacket, wi_sleeves as ucs_sleeves, wi_cap as ucs_cap, 1 as ucs_order";
    strSelect += " from wikipedia_images";
    strSelect += " where wi_description='" + strJockeyColours.replace(" & ", " and ").replace("'","''") + "'";
    strSelect += "union select ucs_jacket, ucs_sleeves, ucs_cap, 2";
    strSelect += " from unregistered_colour_syntax";
    strSelect += " where ucs_colours='" + strJockeyColours.replace(" & ", " and ").replace("'","''") + "' and ucs_organisation in ('Eire', 'UK') and ucs_year=0) d1 order by ucs_order";
    UnregisteredColourSyntax ucs = null;
    ResultSet rs = statement.executeQuery(strSelect);
    if (rs != null)
    {
        try
        {
            if (rs.next())
            {
                // take first - wikipedia_images are most reliable
                 ucs = SmartformRunnerFactory.createUnregisteredColourSyntax(rs);
            }
            rs.close();
        }
        catch(SQLException e)
        {
            System.out.println("createUnregisteredColourSyntax SQLException: " + e.getMessage());
        }
    }  
    
    return ucs;
}


public static int insertFrenchOwner(ENEStatement statement, String strOwnerName, String strFromDate, String strToDate, String strFrenchColours)
{
    int nReturn = 0;
    String strUpdate = "replace into eneform_french_owners (efo_owner_name, efo_from_date, efo_to_date, efo_french_colours)";

    strUpdate += "values (?, ?, ?, ?)";


    PreparedStatement insert = null;
    try 
    {
        insert = statement.getPreparedStatement(strUpdate);

        insert.setString(1, strOwnerName);
        insert.setString(2, strFromDate);
        if ("".equals(strToDate))
            insert.setNull(3, java.sql.Types.DATE);
        else
            insert.setString(3, strToDate);
        if (strFrenchColours == null)    
            insert.setNull(4, java.sql.Types.VARCHAR);
        else
            insert.setString(4, strFrenchColours);

        nReturn  = insert.executeUpdate();
    }
    catch(Exception e)
    {
        System.out.println("insertFrenchOwner Exception: " + e.getMessage());
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

public static String getFullJockeyName(ENEStatement statement, String strName)
{
    return getFullName(statement, "Jockey", strName);
}
public static String getFullTrainerName(ENEStatement statement, String strName)
{
    return getFullName(statement, "Trainer", strName);
}
private static String getFullName(ENEStatement statement, String strType, String strName)
{
    String strFullName = strName;
    
    String strQuery = "select wp_fullname from wikipedia_links where wp_type='" + strType + "' and wp_name='" + strName.replace("'", "''") + "'";
    
    ResultSet rs = statement.executeQuery(strQuery);
    if (rs != null)
    {
        try
        {
            if (rs.next())
            {
                 strFullName = rs.getString(1);
            }
            rs.close();
        }
        catch(SQLException e)
        {
            System.out.println("getFullJockeyName SQLException: " + e.getMessage());
        }
    }  

    return strFullName;
}
public static ArrayList<HorseOwnerQuery> getMissingOwners(ENEStatement statement, String strWhere)
{
    ArrayList<HorseOwnerQuery> alQueries = new ArrayList<HorseOwnerQuery> ();
    
    String strQuery = "select distinct name, coalesce(bred, ''), a.meeting_date";
    strQuery += " from (((";
    strQuery += " additional_race_data inner join additional_race_link on ard_name=arl_name and arl_source != 'SF')";
    strQuery += " inner join additional_races a on a.race_id=arl_race_id and arl_source=ara_source)";
    strQuery += " inner join additional_runners u on a.race_id=u.race_id and arl_source=aru_source and finish_position <=3)";
    strQuery += " where (owner_name is null or owner_name='' or owner_name='Not Specified')";
    if (!"".equals(strWhere))
         strQuery += (" and " + strWhere);
    strQuery += " order by name";
    try
    {
        ResultSet rs = statement.executeQuery(strQuery);
        while (rs.next())
        {
          String strName = rs.getString(1).trim();
          String strBred = rs.getString(2).trim();
          if ("I".equals(strBred))
          {
              strName += " I";
              strBred = "";
          }
          java.sql.Date dtRace = rs.getDate(3);
          GregorianCalendar cal = new GregorianCalendar();
          cal.setTimeInMillis(dtRace.getTime());
          alQueries.add(new HorseOwnerQuery(strName, strBred, cal));
        }
        rs.close();
    }
    catch(SQLException e)
    {
        e.printStackTrace();
    }

    return alQueries;
}

    public static void generateRacePage(String strCountry, String strRaceType, String strRace, boolean bRecreate) {
        try {
            String strDirectory = strCountry + " " + strRaceType;
            String strURL = "http://localhost:58080/JockeyColours/historic123/display.jsp?race_name=" + URLEncoder.encode(strRace, "UTF-8");
            strURL += ("&country=" + strCountry);
            strURL += ("&race_type=" + strRaceType);
            String strFullDirectory = "d:/Program Files/apache-tomcat-7.0.30-windows-x64/apache-tomcat-7.0.30/webapps/ROOT" + "/wikipedia/";
            String strFileName = strFullDirectory + strDirectory + "/" + strRace + "/index.html";
            File f = new File(strFileName);
            f.getParentFile().mkdirs();
            if (bRecreate || !f.exists()) {
                FileOutputStream fos = new FileOutputStream(strFileName);
                OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
                InputStream isExternalAA = ExecuteURL.executeURLStream(strURL, "UTF-8");
                BufferedReader rdr = new BufferedReader(new InputStreamReader(isExternalAA));
                if (rdr != null) {
                    String strLine;
                    while ((strLine = rdr.readLine()) != null) {
                        writer.append(strLine);
                        writer.append('\n');
                    }
                    rdr.close();
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
       } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
