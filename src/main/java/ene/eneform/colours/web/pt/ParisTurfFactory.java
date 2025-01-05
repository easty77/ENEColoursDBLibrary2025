/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.pt;

import ene.eneform.utils.ENEStatement;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Simon
 */
public class ParisTurfFactory {
    
public static int updateParisTurfWinner(ENEStatement statement, ParisTurfRace race)
{
    int nReturn = -1;
    String strUpdate = "update paris_turf_race set pt_winner= ? where pt_race_id = ? and pt_date = ? and pt_course = ? and pt_title = ?";
    
    PreparedStatement insert = null;
    try 
    {
        insert = statement.getPreparedStatement(strUpdate);
      
        insert.setString(1, race.getWinner());
        insert.setInt(2, race.getId());
        insert.setDate(3, new Date(race.getRaceDate().getTime()));
        insert.setString(4, race.getCourse());
        insert.setString(5, race.getTitle());
  
        nReturn  = insert.executeUpdate();
        insert.close();
    }
   catch(Exception e)
    {
        System.out.println("updateParisTurfWinner Exception: " + e.getMessage());
    }
    
    return nReturn;
}
public static boolean existsParisTurfRace(ENEStatement statement, int nRaceId)
{
    boolean bExists = false;
     String strSelect="select pt_race_id from paris_turf_race where pt_race_id=" + nRaceId;
    
   try
    {
        ResultSet rs = statement.executeQuery(strSelect);
        while (rs.next())
        {
            bExists = true;
         }
        rs.close();
    }
    catch(SQLException e)
    {
        
    }
   return bExists;
}
public static int insertParisTurfRace(ENEStatement statement, ParisTurfRace race)
{
    int nReturn = -1;
    String strInsert = "insert INTO paris_turf_race (pt_race_id, pt_date, pt_course, pt_title, pt_winner) values (?, ?, ?, ?, ?)";
    
    PreparedStatement insert = null;
    try 
    {
        insert = statement.getPreparedStatement(strInsert);

        insert.setInt(1, race.getId());
        insert.setDate(2, new Date(race.getRaceDate().getTime()));
        insert.setString(3, race.getCourse());
        insert.setString(4, race.getTitle());
        insert.setString(5, race.getWinner());
  
        nReturn  = insert.executeUpdate();
        insert.close();
    }
    catch(Exception e)
    {
        System.out.println("insertParisTurfRace Exception: " + e.getMessage());
    }
    
    return nReturn;
}
public static List<ParisTurfRace> selectParisTurfMissingRaces(ENEStatement statement)
{
    List<ParisTurfRace> lstRaces = new ArrayList<ParisTurfRace>();
    String strSelect="select pt_race_id, pt_date, pt_course, pt_title, pt_winner, pt_ard_name from paris_turf_race left outer join additional_race_link on arl_race_id=pt_race_id and arl_source='PT' where pt_ard_name is not null and pt_ard_name != '' and year(pt_date) < 1988 and arl_race_id is null order by pt_ard_name, pt_date";
    
   try
    {
        ResultSet rs = statement.executeQuery(strSelect);
        while (rs.next())
        {
            int nId = rs.getInt(1);
            Date dtRace = rs.getDate(2);
            String strCourse = rs.getString(3);
            String strTitle = rs.getString(4);
            String strWinner = rs.getString(5);
            ParisTurfRace ptr = new ParisTurfRace(dtRace, strCourse, strTitle, nId, strWinner);
            ptr.setARDName(rs.getString(6));
            lstRaces.add(ptr);
         }
        rs.close();
    }
    catch(SQLException e)
    {
        
    }
    return lstRaces;
}
public static int updateParisTurfRaces(ENEStatement statement, String strARDName, String strCourse, String strTitle, int nYear, int nMonth, boolean bUnassignedOnly)
{
     int nCount = 0;
     String strUpdate="update paris_turf_race set pt_ard_name= '" + strARDName + "' where 1=1 ";
     if (nYear > 0)
         strUpdate += " and year(pt_date) < " + nYear;
     if (nMonth > 0)
         strUpdate += " and month(pt_date) = " + nMonth;
     if (!"".equals(strCourse))
        strUpdate += " and pt_course like '%" + strCourse + "%'";
     if (!"".equals(strTitle))
        strUpdate += " and pt_title like '%" + strTitle + "%'";
     if (bUnassignedOnly)
        strUpdate += " and (pt_ard_name is null or pt_ard_name='')";
    
     nCount += statement.executeUpdate(strUpdate);
    
    return nCount;
}
public static ParisTurfRace selectParisTurfRace(ENEStatement statement, String strARDName, int nYear)
{
    String strSelect="select pt_race_id, pt_date, pt_course, pt_title, pt_winner from paris_turf_race";
    strSelect += (" where pt_ard_name = '" + strARDName + "' and year(pt_date) = " + nYear);
    try
    {
        ResultSet rs = statement.executeQuery(strSelect);
        int nCount=0;
        if (rs.next())
        {
            int nId = rs.getInt(1);
            Date dtRace = rs.getDate(2);
            String strCourse = rs.getString(3);
            String strTitle = rs.getString(4);
            String strWinner = rs.getString(5);
            rs.close();
            return new ParisTurfRace(dtRace, strCourse, strTitle, nId, strWinner);
         }
        rs.close();
    }
    catch(SQLException e)
    {
        
    }
    return null;
}
public static HashMap<Integer, ParisTurfRace> selectParisTurfARDRaces(ENEStatement statement, String strARDName)
{
    
    HashMap<Integer, ParisTurfRace> hmRaces = new HashMap<Integer, ParisTurfRace>();
    String strSelect="select pt_race_id, pt_date, pt_course, pt_title, pt_winner, year(pt_date) from paris_turf_race";
    strSelect += (" where pt_ard_name='" + strARDName + "'");
    try
    {
        ResultSet rs = statement.executeQuery(strSelect);
        int nCount=0;
        while (rs.next())
        {
            int nId = rs.getInt(1);
            Date dtRace = rs.getDate(2);
            String strCourse = rs.getString(3);
            String strTitle = rs.getString(4);
            String strWinner = rs.getString(5);
            int nYear = rs.getInt(6);
           hmRaces.put(nYear, new ParisTurfRace(dtRace, strCourse, strTitle, nId, strWinner));
         }
        rs.close();
    }
    catch(SQLException e)
    {
        
    }
    return hmRaces;
}
public static List<ParisTurfRace> selectParisTurfRaces(ENEStatement statement, String strWhere)
{
    List<ParisTurfRace> lstRaces = new ArrayList<ParisTurfRace>();
    String strSelect="select pt_race_id, pt_date, pt_course, pt_title, pt_winner from paris_turf_race";
    if (!"".equals(strWhere))
        strSelect += (" where " + strWhere);
    try
    {
        ResultSet rs = statement.executeQuery(strSelect);
        int nCount=0;
        while (rs.next())
        {
            int nId = rs.getInt(1);
            Date dtRace = rs.getDate(2);
            String strCourse = rs.getString(3);
            String strTitle = rs.getString(4);
            String strWinner = rs.getString(5);
            lstRaces.add(new ParisTurfRace(dtRace, strCourse, strTitle, nId, strWinner));
         }
        rs.close();
    }
    catch(SQLException e)
    {
        
    }
    return lstRaces;
}
public static String selectParisTurfReferences(ENEStatement statement, String strARDName)
{
    String strContent="* Paris-Turf:\n**";
    String strSelect="select concat('{{Paris-Turf|', year(pt_date), '|', lpad(cast(month(pt_date) as char(2)), 2, '0'), '-', lpad(cast(day(pt_date) as char(2)), 2, '0'), '|', pt_course, '|', pt_title, '|', pt_race_id, '}}') as pt from paris_turf_race where pt_ard_name='" + strARDName + "' and year(pt_date) <= 1987 order by pt_date";
   
    try
    {
        ResultSet rs = statement.executeQuery(strSelect);
        int nCount=0;
        while (rs.next())
        {
            String strRow = rs.getString("pt");
            if (nCount > 0)
                strContent += ", ";
            strContent+=strRow;
            nCount++;
        }
        rs.close();
    }
    catch(SQLException e)
    {
        
    }
    return strContent;
}}
