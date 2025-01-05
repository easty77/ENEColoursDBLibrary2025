/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.ebay;

import ene.eneform.utils.ENEStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author simon
 */
public class EBayRacecards {
    
    public static int updateRacecard(ENEStatement statement, String strArticleId, String strArticleType, String strCourse, String strMeeting, 
            String strMeetingDate, String strYear, int nRacecards)
    {
        System.out.println("updateRacecard: " + strArticleId);
        int nReturn = 0;
        String strUpdateArticle = "update ebay_articles set ebay_article_type=? where ebay_article_id=?";
        String strUpdateRacecard = "replace into ebay_racecards (ebay_course, ebay_meeting, ebay_meeting_date, ebay_year, ebay_nr_racecards, ebay_racecard_id) values (?, ?, ?, ?, ?, ?)";
     PreparedStatement insert = null;
    try 
    {
        insert = statement.getPreparedStatement(strUpdateArticle);;
            insert.setString(1, strArticleType);
            insert.setString(2, strArticleId);
            nReturn  = insert.executeUpdate();
            // SE 20181208 - don't close statement
            //insert.close();
            insert = statement.getPreparedStatement(strUpdateRacecard);
            
            insert.setString(1, strCourse);
            insert.setString(2, strMeeting);
            if ("".equals(strMeetingDate))
                insert.setNull(3, java.sql.Types.DATE);
            else
            {
                java.sql.Date date = new java.sql.Date(0000-00-00);
                insert.setDate(3, java.sql.Date.valueOf(strMeetingDate));
            }
            try
            {
                int nYear = Integer.parseInt(strYear);
                insert.setInt(4, nYear);
            }
            catch(NumberFormatException e)
            {
                insert.setNull(4, java.sql.Types.INTEGER);
            }
            insert.setInt(5, nRacecards);
            insert.setString(6, strArticleId);
            
            nReturn  = insert.executeUpdate();
            // SE 20181208 - don't close statement
            //insert.close();
            //connection.commit();
    }
    catch(SQLException e)
    {
        System.out.println("updateRacecard Exception: " + e.getMessage());
    }
       
        return nReturn;
    }
    public static String getActiveArticleURL(ENEStatement statement, int nArticle)
    {
        String strURL = null;
        String strQuery = "select ebay_url from ebay_items where ebay_status='Active' and ebay_article_id=" + nArticle;
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) {
                strURL = rs.getString("ebay_url");
            }
            rs.close();
        } 
        catch (SQLException e) {
        }
        
        return strURL;
    }
    public static int updateRacecardYearStatus(ENEStatement statement, String strARDName, int nYear, int nStatus)
    {
        String strUpdate="insert into ebay_racecard_race_year(erry_ard_name, erry_year, erry_status) values ('" + strARDName + "', " + nYear + ", " + nStatus + ")";
        strUpdate +=" on duplicate key update erry_status=" + nStatus;

        int nRecords = statement.executeUpdate(strUpdate);
        
        return nRecords;
    }
}
