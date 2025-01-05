/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class RacingPostOwnerFactory {
    
        private static String sm_strRPImageURL = "http://images.racingpost.com/png_silks/";
        private static String sm_strRPOwnerURL = "https://www.racingpost.com/horses/owner_home.sd?owner_id=";
        private static String sm_strRPStatsURL = "https://www.racingpost.com/horses/owner_stats_summary.sd?owner_id="; // add season=GB Flat (default) or UK Jumps or Irish Flat or Irish Jumps
    
   public static void loadRPOwnerRange(ENEStatement statement, int nStart, int nEnd)
    {
        System.out.println("loadRPOwnerRange" + nStart + "-" + nEnd);
        for (int nOwnerId=nStart; nOwnerId <= nEnd; nOwnerId++)
        {
            try
            {
                System.out.print(".");
                loadRPOwner(statement, nOwnerId, false);
            }
            catch(Exception e)
            {
                System.out.println("loadRPOwner exception:" + nOwnerId + "-" + e.getMessage());
            }
        }        
    }
    public static void loadRPOwnerThousand(ENEStatement statement, int nStartThousand, int nEndThousand, boolean bCheck)
    {
       System.out.println("loadRPOwnerThousand" + nStartThousand + "-" + nEndThousand);
           for(int thousand = nStartThousand; thousand <= nEndThousand; thousand++)
            {
                System.out.println("\n" + (thousand * 1000));
                for (int i=0; i < 10; i++)
                {
                    System.out.print(i + "." + ((i == 9) ? "\n" : ""));
                    for(int j=0; j < 10; j++)
                    {
                        System.out.print(".");
                        for(int k=0; k < 10; k++)
                        {
                            System.out.print(".");
                            int nOwnerId = (thousand * 1000 + i * 100 + j * 10 + k);
                            if (!bCheck || !RacingPostFactory.existsRacingPostOwner(statement, nOwnerId))
                            {
                                try
                                {
                                    loadRPOwner(statement, nOwnerId, false);
                                }
                                catch(Exception e)
                                {
                                    System.out.println("loadRPOwner exception:" + nOwnerId + "-" + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        
    }
    public static String loadRPOwner(ENEStatement statement, int nOwnerId, boolean bForce) throws IOException
    {
        String strOwnerId = String.format("%06d", nOwnerId);
        String strOwnerName = null;
            String strImageURL=sm_strRPImageURL + strOwnerId.substring(5, 6) + "/" + strOwnerId.substring(4, 5) + "/" + strOwnerId.substring(3, 4) +"/" + nOwnerId + ".png";
            boolean bExists = true;
            if (!bForce)
                bExists = ExecuteURL.exists(strImageURL);
            if (bExists)
            {
                System.out.print("y");
                String strURL = "https://www.racingpost.com/profile/owner/" + nOwnerId;
                TagNode rootNode = ExecuteURL.getRootNode(strURL, "utf-8");
                if (rootNode != null)
                {
                    strOwnerName = ExecuteURL.getTagNodeTitle(rootNode);
                    //System.out.println(strOwnerName);
                    if (strOwnerName.indexOf("|") >= 3) // reduced from 8 to 3 because short owner names were being omitted (why does this exist?) 
                    {
                        strOwnerName=strOwnerName.substring(0, strOwnerName.indexOf("|") - 1);
                        strOwnerName=strOwnerName.replace("&#039;", "''").replace("&amp;", "&");
                        System.out.println(nOwnerId + "=" + strOwnerName);
                        RacingPostFactory.insertRPOwner(statement, nOwnerId, strOwnerName);
                        TagNode[] aTables = rootNode.getElementsByName("table", true);
                        if (aTables.length > 0)
                        {
                            TagNode table = (TagNode) aTables[0];
                            TagNode[] aRows = table.getElementsByName("tr", true);
                            System.out.println("Rows: " + aRows.length);
                            if (false)          // don't bother  with year data
//                            if (aRows.length > 1)
                            {
                                // ignore table header
                                for(int n = 1; n < aRows.length; n++)
                                {
                                    TagNode row = (TagNode) aRows[n];
                                    TagNode[] aCells = row.getElementsByName("td", true);
                                    if (aCells.length == 10)
                                    {
                                        String strYear = ExecuteURL.getNodeContentString(aCells[0]);
                                        String strNrWins = ExecuteURL.getNodeContentString(aCells[1]);
                                        String strNrRuns = ExecuteURL.getNodeContentString(aCells[2]);
                                        String strWinPounds = ExecuteURL.getNodeContentString(aCells[7]).replace("&pound;", "").replace(",", "");
                                        String strTotalPounds = ExecuteURL.getNodeContentString(aCells[8]).replace("&pound;", "").replace(",", "");
                                        //System.out.println("RP_owners_year:" + strYear + "-" + strNrRuns + "-" + strNrWins + "-" + strWinPounds + "-" + strTotalPounds);
                                        RacingPostFactory.insertRPOwnerTypeYear(statement, nOwnerId, strYear, "UK Flat", strNrRuns, strNrWins, strWinPounds, strTotalPounds);
                                        System.out.println("Year: " + strYear + " runners: " + strNrRuns);
                                    }
                                }
                            }
                        }
                    }
                }
           }
         return strOwnerName;
    }
public static int  insertRPOwnerColours(ENEStatement statement, int nOwner, String strColours)
{
    return insertRPOwnerColours(statement, nOwner, strColours, "");
}
public static int insertRPOwnerColours(ENEStatement statement, int nOwner, String strColours, String strSuffix)
{
     String strInsert = "insert into rp_owner_colours (rpc_owner_id, rpc_owner_colours, rpc_suffix) values";
     strInsert += ("(" + nOwner + ", '" + strColours.replace("'", "''") + "', '" + strSuffix + "')");

     int nRecords = statement.executeUpdate(strInsert);
     
     return nRecords;
 }
public static void  insertRPOwnerStats(ENEStatement statement, String strWhere, String strType, boolean bMissing)
 {
     ArrayList<Integer> alOwnerIds = getRPOwnerIds(statement, strWhere, bMissing);
     Iterator<Integer> iter = alOwnerIds.iterator();
     while(iter.hasNext())
     {
         int nOwnerId = iter.next();
         try
         {
            loadRPOwnerStats(statement, strType, nOwnerId);
         }
         catch(IOException e)
         {
            System.out.println("insertRPOwnerStats exception:" + nOwnerId + "-" + e.getMessage());
         }
     }
 }
public static ArrayList<Integer> getRPOwnerIds(ENEStatement statement, String strWhere, boolean bMissing)
{
    ArrayList<Integer> alOwnerIds = new ArrayList<Integer>();
    String strQuery;
    if (bMissing)
    {
        strQuery = "select rp_owner_id from rp_owners left outer join rp_owners_type_year on rp_owner_id=rpty_owner_id where rpty_owner_id is null";
        if ((strWhere != null) && (!"".equals(strWhere)))
                strQuery += " and " + strWhere;
    }
    else
    {
        strQuery = "select rp_owner_id from rp_owners";
        if ((strWhere != null) && (!"".equals(strWhere)))
                strQuery += " where " + strWhere;
    }
 
    strQuery += " order by rp_owner_id";
   
    try
    {
        ResultSet rs = statement.executeQuery(strQuery);
        while (rs.next())
        {
          alOwnerIds.add(rs.getInt(1));
         }
        rs.close();
    }
    catch(SQLException e)
    {
        e.printStackTrace();
    }
    return alOwnerIds;
}

    public static int loadRPOwnerStats(ENEStatement statement, String strType, int nOwnerId) throws IOException
    {
        int nInserts = 0;
                 TagNode rootNode = ExecuteURL.getRootNode(sm_strRPStatsURL + nOwnerId + "&season=" + strType, "utf-8");
                     TagNode[] aTables = rootNode.getElementsByName("table", true);
                    if (aTables.length > 0)
                    {
                        TagNode table = aTables[0];
                        TagNode[] aRows = table.getElementsByName("tr", true);
                        if (aRows.length > 1)
                        {
                            // ignore table header
                            System.out.println("Rows: " + String.valueOf(aRows.length - 1));
                            for(int n = 1; n < aRows.length; n++)
                            {
                                TagNode row = aRows[n];
                                TagNode[] aCells = row.getElementsByName("td", true);
                                if (aCells.length == 10)
                                {
                                    String strYear = ExecuteURL.getNodeContentString(aCells[0]);
                                    if (strYear.indexOf("-") > 0)   // NH Season
                                        strYear = strYear.substring(0, 2) + strYear.substring(5);
                                    String strNrWins = ExecuteURL.getNodeContentString(aCells[1]);
                                    String strNrRuns = ExecuteURL.getNodeContentString(aCells[2]);
                                    String strWinPounds = ExecuteURL.getNodeContentString(aCells[7]).replace("&pound;", "").replace(",", "");
                                    String strTotalPounds = ExecuteURL.getNodeContentString(aCells[8]).replace("&pound;", "").replace(",", "");
                                    nInserts += RacingPostFactory.insertRPOwnerTypeYear(statement, nOwnerId, strYear, strType, strNrRuns, strNrWins, strWinPounds, strTotalPounds);
                                    System.out.println("Year: " + strYear + "-" + strNrRuns);
                                }
                            }
                        }
            }
          int nReturn = (nInserts > 0) ? 1 : 0;
          System.out.println("loadRPOwnerStats: " + nOwnerId + "-" + nInserts);
          return nReturn;
    }
    public static void loadMissingRPOwners(ENEStatement statement, long nRace) throws IOException
    {
        String strSelect = "select race_id, name, owner_name as rp_owner_id, rp_owner_name as owner_name, case when length(owner_name) = length('' + owner_name * 1) then '' else substr(owner_name, length(owner_name), 1) end as rpc_suffix, coalesce(rpc_owner_colours, '') as jockey_colours";
        strSelect += " from additional_runners left outer join rp_owners on owner_name*1=rp_owner_id";
        strSelect += " left outer join rp_owner_colours on owner_name*1=rpc_owner_id and rpc_suffix=case when length(owner_name) = length('' + owner_name * 1) then '' else substr(owner_name, length(owner_name), 1) end";
        strSelect += " where race_id=" + nRace + " and owner_name * 1 > 0";
   
        
    /* Runs ok in command window, but gives  EXCEPTION executeUpdate: Data truncation: Truncated incorrect DOUBLE value when run here:
        String strUpdate="update additional_runners a inner join ";
        strUpdate += "(" + strSelect + ")";
        strUpdate += " t2 ON a.race_id=t2.race_id and a.name=t2.name";
        strUpdate += " set a.owner_name=t2.owner_name, a.jockey_colours=t2.jockey_colours";

        statement.executeUpdate(strUpdate);
*/
        ArrayList<String> alOwners = new ArrayList();
        
        try
        {
            ResultSet rs = statement.executeQuery(strSelect);
            while (rs.next())
            {
              alOwners.add(rs.getString(2) + "|" + rs.getString(3) + "|" + rs.getString(4) + "|" + rs.getString(5) + "|" + rs.getString(6));
             }
            rs.close();
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        
        for(int i = 0; i < alOwners.size(); i++)
        {
            String strOwnerDetails = alOwners.get(i);
            System.out.println(strOwnerDetails);
            String[] alOwnerElements = strOwnerDetails.split("\\|");    // name, owner_id, owner_name, suffix, jockey_colours
            if (alOwnerElements.length == 5)
            {
                String strUpdate = "update additional_runners set owner_name = '" + alOwnerElements[2].replace("'", "''") + "', jockey_colours='" + alOwnerElements[4].replace("'", "''") + "' where name='" + alOwnerElements[0].replace("'", "''") + "' and race_id=" + nRace;
                System.out.println(strUpdate);
                statement.executeUpdate(strUpdate);
            }
            else
            {
        // load rp_owners table
        // boolean bOwner = loadRPOwner(statement, nOwner, true);  // force, don't mind if no image
            }
        }
         
                
    } 
}
