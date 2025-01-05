/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.colours.bos.ENEWebsiteURL;
import ene.eneform.utils.ENEStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Simon
 */
public class WebsiteURLFactory {
    
    public static void insertWebsiteURL(ENEStatement statement, String strType, String strSubType, String strTitle, String strURL, int nDays)
    {
        String strUpdate = "replace into website_urls (wu_type, wu_subtype, wu_title, wu_url, wu_date) values (\"" + strType + "\", \"" + strSubType + "\",\"" + strTitle + "\",\"" + strURL + ".svg\", date_add(current_date, interval " + nDays + " day))"; 

        int nReturn  = statement.executeUpdate(strUpdate);

    }
    
    public static List<ENEWebsiteURL> getWebsiteURLs(ENEStatement statement, String strWhere)
    {
    String strQuery = "select wu_type, wu_subtype, wu_title, wu_url from website_urls where " + strWhere;
    strQuery += " order by wu_date desc";
    
    List<ENEWebsiteURL> alURLs = new ArrayList<ENEWebsiteURL>();
    
    try
    {
        ResultSet rs = statement.executeQuery(strQuery);
        if (rs != null)
        {
            while (rs.next())
            {
                ENEWebsiteURL wu = new ENEWebsiteURL(rs.getString("wu_type"), rs.getString("wu_subtype"), rs.getString("wu_title"), rs.getString("wu_url"));
                alURLs.add(wu);
            }
            rs.close();
        }
    }
    catch(SQLException e)
    {

    }

    return alURLs;
        
    }
}
