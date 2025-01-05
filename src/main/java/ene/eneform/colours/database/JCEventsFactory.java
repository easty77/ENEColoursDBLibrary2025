/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.utils.ENEStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 *
 * @author simon
 */
public class JCEventsFactory {
    
    public static String getEventTimestamp(ENEStatement statement,  String strEvent)
    {
        String strQuery = "select jc_event_timestamp from jc_events where jc_event_name='" + strEvent + "'";
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) 
            {
                Timestamp ts = rs.getTimestamp(1);
                return ts.toGMTString();
            }
            rs.close();
        } catch (SQLException e) {
        }

        return null;

    }
    public static String getEventDate(ENEStatement statement,  String strEvent)
    {
        String strQuery = "select jc_event_date from jc_events where jc_event_name='" + strEvent + "'";
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) 
            {
                String strDate = rs.getString(1);
                return strDate;
            }
            rs.close();
        } catch (SQLException e) {
        }

        return null;

    }
    public static int getEventDayInterval(ENEStatement statement,  String strEvent)
    {
        String strQuery = "select datediff(current_date, jc_event_date) from jc_events where jc_event_name='" + strEvent + "'";
        try 
        {
            ResultSet rs = statement.executeQuery(strQuery);
            if (rs.next()) 
            {
                int nDays = rs.getInt(1);
                return nDays;
            }
            rs.close();
        } catch (SQLException e) {
        }

        return -1;

    }
    public static int updateEventDate(ENEStatement statement,  String strEvent)
    {
        String strQuery = "update jc_events set jc_event_date=current_date where jc_event_name='" + strEvent + "'";
        int nRecords = statement.executeUpdate(strQuery);
        
        return nRecords;
    }
}
