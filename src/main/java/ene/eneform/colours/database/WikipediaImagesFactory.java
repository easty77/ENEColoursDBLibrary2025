/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.smartform.bos.SmartformPrimaryOwnerColours;
import ene.eneform.utils.DbUtils;
import ene.eneform.utils.ENEStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public class WikipediaImagesFactory {

    public static boolean updateWikipediaImageTimestamp(ENEStatement statement, String strOwnerName) {
        String strUpdate = "UPDATE wikipedia_images set wi_timestamp=current_timestamp where wi_owner='" + strOwnerName.replaceAll("'", "''") + "'";
        int nReturn = statement.executeUpdate(strUpdate);
        return nReturn == 1;
    }

    public static int insertWikipediaImage(ENEStatement statement, String strOwner, String strJacket, String strSleeves, String strCap, String strDescription, String strComment, boolean bOverwrite) {
        String strUpdate = "INSERT INTO wikipedia_images (wi_owner, wi_jacket, wi_sleeves, wi_cap, wi_description, wi_comments, wi_timestamp, wi_version) VALUES ('";
        strUpdate += strOwner.replaceAll("'", "''") + "','" + strJacket.replaceAll("'", "''") + "','" + strSleeves.replaceAll("'", "''") + "','" + strCap.replaceAll("'", "''") + "','" + strDescription.replaceAll("'", "''") + "','" + strComment.replaceAll("'", "''") + "', current_timestamp, 1)";
        if (bOverwrite) {
            strUpdate += " ON DUPLICATE KEY UPDATE wi_timestamp = current_timestamp, wi_version=wi_version+1, wi_jacket=";
            strUpdate += "'" + strJacket.replaceAll("'", "''") + "', wi_sleeves='" + strSleeves.replaceAll("'", "''") + "', wi_cap='" + strCap.replaceAll("'", "''") + "', wi_description='" + strDescription.replaceAll("'", "''") + "'";
        }
        int nReturn = statement.executeUpdate(strUpdate);
        return nReturn;
    }

    public static ArrayList<SmartformPrimaryOwnerColours> selectWikipediaOwners(ENEStatement stmt, String[] astrOwners) {
        ArrayList<SmartformPrimaryOwnerColours> alOwners = new ArrayList<SmartformPrimaryOwnerColours>();
        String strOwnerList = "";
        for (int i = 0; i < astrOwners.length; i++) {
            if (i > 0) {
                strOwnerList += ", ";
            }
            strOwnerList += ("'" + astrOwners[i].replace("'", "''") + "'");
        }
        String strQuery = "select wi_owner, wi_jacket, wi_sleeves, wi_cap, wi_description from wikipedia_images";
        strQuery += (" where wi_owner in (" + strOwnerList + ")");
        ResultSet rs = stmt.executeQuery(strQuery);
        if (rs != null) {
            try {
                while (rs.next()) {
                    SmartformPrimaryOwnerColours owner = new SmartformPrimaryOwnerColours(rs.getString("wi_owner"));
                    owner.setJacketSyntax(rs.getString("wi_jacket"));
                    owner.setSleevesSyntax(rs.getString("wi_sleeves"));
                    owner.setCapSyntax(rs.getString("wi_cap"));
                    owner.setColours(DbUtils.getDBString(rs, "wi_description"));
                    alOwners.add(owner);
                }
                rs.close();
            } catch (SQLException e) {
            }
        }
        return alOwners;
    }

public static ArrayList<String> selectWikipediaImagesLatest(ENEStatement statement)
{
    String strTimestamp = JCEventsFactory.getEventTimestamp(statement, "latest_wikipedia_images");
    ArrayList<String> array = selectWikipediaOwnersLatest(statement, strTimestamp);
    JCEventsFactory.updateEventDate(statement, "latest_wikipedia_images");
    return array;
}
public static ArrayList<String> selectWikipediaOwnersByDate(ENEStatement stmt, int nDayOffset, String strStartTime) {
        ArrayList<String> alOwners = new ArrayList<String>();
        String strQuery = "select wi_owner from wikipedia_images";
        strQuery += (" where date(wi_timestamp) = date_sub(current_date, interval " + nDayOffset + " day)");
        if (strStartTime != null)
            strQuery += (" and time(wi_timestamp) >= '" + strStartTime + "'");
        ResultSet rs = stmt.executeQuery(strQuery);
        if (rs != null) {
            try {
                while (rs.next()) {
                    alOwners.add(rs.getString(1));
                }
                rs.close();
            } catch (SQLException e) {
            }
        }
        return alOwners;
    }
public static ArrayList<String> selectWikipediaOwnersLatest(ENEStatement stmt, String strTimestamp) {
        ArrayList<String> alOwners = new ArrayList<String>();
        String strQuery = "select wi_owner from wikipedia_images";
        strQuery += (" where wi_timestamp >= '" + strTimestamp + "'");
        ResultSet rs = stmt.executeQuery(strQuery);
        if (rs != null) {
            try {
                while (rs.next()) {
                    alOwners.add(rs.getString(1));
                }
                rs.close();
            } catch (SQLException e) {
            }
        }
        return alOwners;
    }
public static void insertUnregisteredColourSyntax(ENEStatement statement, String strCountry, String strColours, String strOwner)
{
    String strReplace="replace into unregistered_colour_syntax (ucs_organisation, ucs_year, ucs_colours, ucs_jacket, ucs_sleeves, ucs_cap, ucs_comments)";
    strReplace += "(select '" + strCountry + "', 0, '" + strColours.replaceAll("'", "''") + "', wi_jacket, wi_sleeves, wi_cap, wi_comments";
    strReplace += " from wikipedia_images where wi_owner = '" + strOwner.replaceAll("'", "''") + "')";
    statement.executeUpdate(strReplace);
    
    reloadRacingColoursParse(statement);
}

private static void reloadRacingColoursParse(ENEStatement statement)
{
    String strReplace1 = "replace into racing_colours_parse (rcp_description, rcp_jacket, rcp_sleeves, rcp_cap, rcp_version)";
    strReplace1 += "(select wi_description, wi_jacket, wi_sleeves, wi_cap, 'Summer2015' from wikipedia_images)";
    statement.executeUpdate(strReplace1);
 
    String strInsert1 = "insert into racing_colours_parse (rcp_description, rcp_jacket, rcp_sleeves, rcp_cap, rcp_version)";
    strInsert1 += "(select wi_description, wi_jacket, wi_sleeves, wi_cap, 'Summer2015' from wikipedia_images";
    strInsert1 += " where not exists (select * from racing_colours_parse where rcp_description=wi_description and rcp_version='Summer2015'))";
    statement.executeUpdate(strInsert1);

    String strReplace2="replace into racing_colours_parse (rcp_description, rcp_jacket, rcp_sleeves, rcp_cap, rcp_version)";
    strReplace2 += "(select ucs_colours, ucs_jacket, ucs_sleeves, ucs_cap, 'Summer2015' from unregistered_colour_syntax)";
    statement.executeUpdate(strReplace2);
}
}
