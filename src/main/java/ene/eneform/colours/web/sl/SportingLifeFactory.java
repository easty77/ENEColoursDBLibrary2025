/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.sl;

import ene.eneform.colours.database.ENEColoursRunnerFactory;
import ene.eneform.colours.database.ENERacingColoursFactory;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.smartform.bos.RacingColoursParse;
import ene.eneform.smartform.bos.SportingLifeSearch;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import ene.eneform.utils.ENEStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public class SportingLifeFactory {

    public static int getSLCourseId(ENEStatement statement, String strCourse) {
        String strQuery = "select sl_course_id from sporting_life_course where replace(sl_course_name, '-', ' ')='" + strCourse.replace('-', ' ') + "'";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) {
                int nReturn = rs.getInt(1);
                rs.close();
                return nReturn;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int updateSportingLifeOwnerColours(ENEStatement statement, String strOwner, int nOwner, String strCountry, String strDescription) {
        int nReturn = 0;
        String strUpdate = "update sporting_life_owners set sl_jockey_colours =? where sl_owner_name = ? and sl_owner_nr = ? and sl_country = ?";
        strUpdate += "values (?, ?, ?, ?)";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setString(1, strDescription);
            insert.setString(2, strOwner);
            insert.setInt(3, nOwner);
            insert.setString(4, strCountry);
            nReturn = insert.executeUpdate();
             // SE 20181208 - don't close statement
            //insert.close();
        } catch (Exception e) {
            System.out.println("updateSportingLifeOwnerColours Exception: " + e.getMessage());
        }
        return nReturn;
    }

    public static ArrayList<SportingLifeSearch> createSportingLifeSearch(ENEStatement statement, String strWhere) {
        ArrayList<SportingLifeSearch> alSearch = new ArrayList<SportingLifeSearch>();
        String strQuery = "select case when locate('(', race_name) > 1 then substr(race_name, 1, locate('(', race_name) - 1) else race_name end as name, sl_course_id, case when course = '' then ard_course else course end as course, min(year(meeting_date))  as min_year, max(year(meeting_date)) as max_year";
        strQuery += " from additional_races, sporting_life_course, additional_race_link, additional_race_data where arl_source != 'SF' and sl_course_name=case when course = '' then ard_course else course end and arl_race_id=race_id and arl_name=ard_name and arl_source=ara_source";
        if (!"".equals(strWhere)) {
            strQuery += " and " + strWhere;
        }
        strQuery += " group by case when locate('(', race_name) > 1 then substr(race_name, 1, locate('(', race_name) - 1) else race_name end, sl_course_id";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            while (rs.next()) {
                int nCourseId = rs.getInt("sl_course_id");
                String strName = rs.getString("name");
                int nMinYear = rs.getInt("min_year");
                int nMaxYear = rs.getInt("max_year");
                String strCourse = rs.getString("course");
                SportingLifeSearch sls = new SportingLifeSearch(nCourseId, strCourse, strName.replace(" ", "+"), nMinYear, nMaxYear);
                alSearch.add(sls);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alSearch;
    }

    public static String getSLCourseCountry(ENEStatement statement, String strCourse) {
        String strQuery = "select sl_country from sporting_life_course where sl_course_name='" + strCourse + "'";
        try {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) {
                String strReturn = rs.getString(1);
                rs.close();
                return strReturn;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int insertSportingLifeOwnerYear(ENEStatement statement, String strOwner, int nOwner, String strCountry, int nYear) {
        int nReturn = 0;
        String strUpdate = "insert INTO sporting_life_owner_year (sly_owner_name, sly_owner_nr, sly_country, sly_year)";
        strUpdate += "values (?, ?, ?, ?)";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setString(1, strOwner);
            insert.setInt(2, nOwner);
            insert.setString(3, strCountry);
            insert.setInt(4, nYear);
            nReturn = insert.executeUpdate();
            // SE 20181208 - don't close statement
            //insert.close();
        } catch (Exception e) {
            System.out.println("insertSportingLifeOwnerYear Exception: " + e.getMessage());
        }
        return nReturn;
    }

    public static int nomatchSportingLifeOwner(ENEStatement statement, String strOwner, String strCountry, String strJockeyColours, int nYear, int nColours) {
        // returns negative number if owner found and colours match, so no need to add
        // if value is 0 or positive then next value should be n + 1
        int nOwnerNr = 0;
        RacingColoursParse rcp = ENEColoursRunnerFactory.getParsedJockeyColours(statement, strJockeyColours);
        if (rcp == null) {
            ENERacingColoursFactory.createColours(statement, ENEColoursEnvironment.DEFAULT_LANGUAGE, strJockeyColours);
            rcp = ENEColoursRunnerFactory.getParsedJockeyColours(statement, strJockeyColours);
        }
        String strSelect = "select sl_colours_id, sl_jockey_colours, sl_owner_nr, sly_year, rcp_jacket, rcp_sleeves, rcp_cap, rcp_unresolved from sporting_life_owners left outer join sporting_life_owner_year on sl_owner_name = sly_owner_name and sl_country = sly_country and sl_owner_nr = sly_owner_nr and sly_year = " + nYear;
        strSelect += " left outer join racing_colours_parse on sl_jockey_colours=rcp_description and rcp_version='" + SmartformRunnerFactory.sm_RCPVersion + "'";
        strSelect += (" where sl_owner_name = '" + strOwner.replace("'", "''") + "' and sl_country = '" + strCountry + "' order by sl_owner_nr");
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
                while (rs.next()) {
                    int nDBColours = rs.getInt(1);
                    String strDBColours = rs.getString(2);
                    int nDBYear = rs.getInt(4);
                    boolean bYearExists = !rs.wasNull();
                    nOwnerNr = rs.getInt(3);
                    RacingColoursParse rcpDB = null;
                    String strJacket = rs.getString("rcp_jacket");
                    if (strJacket != null)
                        rcpDB = new RacingColoursParse(strJacket, rs.getString("rcp_sleeves"), rs.getString("rcp_cap"), rs.getString("rcp_unresolved"));
                    if ((rcpDB != null) && rcpDB.equals(rcp)) {
                        if (nColours == nDBColours) {
                            if (bYearExists) {
                                return 0; // nothing to do colours and year already exist
                            } else {
                                return -nOwnerNr;
                            }
                        }
                    } else if (nColours == nDBColours) {
                        if (strJockeyColours.indexOf(strDBColours) == 0) {
                            updateSportingLifeOwnerColours(statement, strOwner, nOwnerNr, strCountry, strJockeyColours);
                        }
                        if (bYearExists) {
                            return 0; // nothing to do colours and year already exist
                        } else {
                            return -nOwnerNr;
                        }
                    }
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("selectSportingLifeOwnerId SQLException: " + e.getMessage());
            }
        }
        return nOwnerNr + 1;
    }

    public static int insertSportingLifeOwner(ENEStatement statement, String strOwner, int nOwner, String strCountry, int nColoursId, String strDescription) {
        int nReturn = 0;
        String strUpdate = "INSERT INTO sporting_life_owners (sl_owner_name, sl_owner_nr, sl_country, sl_colours_id, sl_jockey_colours)";
        strUpdate += "values (?, ?, ?, ?, ?)";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setString(1, strOwner);
            insert.setInt(2, nOwner);
            insert.setString(3, strCountry);
            insert.setLong(4, nColoursId);
            insert.setString(5, strDescription);
            nReturn = insert.executeUpdate();
            // SE 20181208 - don't close statement
            //insert.close();
        } catch (Exception e) {
            System.out.println("insertSportingLifeOwner Exception: " + e.getMessage());
        }
        return nReturn;
    }

    public static int matchSportingLifeOwner(ENEStatement statement, String strOwner, String strCountry, String strJockeyColours, int nYear, int nColours) {
        String strSelect = "select sl_owner_nr, sly_year from sporting_life_owners left outer join sporting_life_owner_year on sl_owner_name = sly_owner_name and sl_country = sly_country and sl_owner_nr = sly_owner_nr and sly_year = " + nYear;
        strSelect += (" where sl_owner_name = '" + strOwner.replace("'", "''") + "' and sl_jockey_colours = '" + strJockeyColours + "' and sl_country = '" + strCountry + "' and sl_colours_id=" + nColours);
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
                if (rs.next()) {
                    int nOwner = rs.getInt(1);
                    int nDBYear = rs.getInt(2);
                    boolean bYearExists = !rs.wasNull();
                    rs.close();
                    if (bYearExists) {
                        return 0; // nothing to do colours and year already exist
                    } else {
                        return nOwner;
                    }
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("selectSportingLifeOwnerId SQLException: " + e.getMessage());
            }
        }
        return -1; // not found
    }

    public static int selectSportingLifeOwnerId(ENEStatement statement, String strOwner, String strCountry, String strJockeyColours) {
        int nOwnerId = 0;
        String strSelect = "select sl_colours_id from sporting_life_owners where sl_owner_name = '" + strOwner.replace("'", "''") + "' and sl_country = '" + strCountry + "'";
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
                while (rs.next()) {
                    nOwnerId = rs.getInt(1);
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("selectSportingLifeOwnerId SQLException: " + e.getMessage());
            }
        }
        return nOwnerId;
    }
    
}
