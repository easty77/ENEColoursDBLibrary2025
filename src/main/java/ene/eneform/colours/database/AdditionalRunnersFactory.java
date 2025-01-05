/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.utils.ENEStatement;

import java.sql.PreparedStatement;

/**
 *
 * @author Simon
 */
public class AdditionalRunnersFactory {

    public static int updateDistanceWon(ENEStatement statement, int nRace) {
        String strUpdate = "UPDATE additional_runners set distance_won = (select * from (select distance_beaten from additional_runners where race_id=" + nRace + " and finish_position=2) as t1) where race_id=" + nRace + " and finish_position=1";
        int nReturn = statement.executeUpdate(strUpdate);
        return nReturn;
    }

    public static int updateRunnerName(ENEStatement statement, String strDataType, String strName, int nPosition, int nRaceId) {
        String strUpdate;
        int nCount = 0;
        strUpdate = "insert into additional_runners (race_id, aru_source, finish_position, name, ard_timestamp) values (" + nRaceId + ", '" + strDataType + "', " + nPosition + ", '" + strName.replace("'", "''") + "', current_timestamp)";
        if (!"".equals(strUpdate)) {
            nCount += statement.executeUpdate(strUpdate);
        }
        return nCount;
    }

    public static int updateOwnerName(ENEStatement statement, String strDataType, String strOwner, String strName, int nRaceId) {
        String strUpdate;
        int nCount = 0;
        strUpdate = "update additional_runners set owner_name='" + strOwner.replace("'", "''") + "' where race_id= " + nRaceId + " and name = '" + strName.replace("'", "''") + "' and aru_source='" + strDataType + "'";
        nCount += statement.executeUpdate(strUpdate);
        strUpdate = "update additional_runners set owner_name='" + strOwner.replace("'", "''") + "' where name = '" + strName.replace("'", "''") + "' and owner_name='' and aru_source='" + strDataType + "'";
        nCount += statement.executeUpdate(strUpdate);
        return nCount;
    }

    public static int updateOwnerColours(ENEStatement statement, String strDataType, String strOwner, String strColours, String strName, int nRaceId) {
        String strUpdate;
        int nCount = 0;
        if ("SF".equals(strDataType)) {
            strUpdate = "update daily_runners set jockey_colours='" + strColours.replace("'", "''") + "', loaded_at=loaded_at where race_id= " + nRaceId + " and name = '" + strName.replace("'", "''") + "'";
            nCount += statement.executeUpdate(strUpdate);
            if (nCount == 0) {
                strUpdate = "insert into daily_runners (race_id, runner_id, name, owner_name, form_type, loaded_at, jockey_colours) ( select race_id, runner_id, name, owner_name, 'Inserted by SE','1970-01-01 12:00:00', '" + strColours.replace("'", "''") + "' from historic_runners where race_id= " + nRaceId + " and name = '" + strName.replace("'", "''") + "')";
                nCount += statement.executeUpdate(strUpdate);
            }
        } else {
            strUpdate = "update additional_runners set jockey_colours='" + strColours.replace("'", "''") + "' where race_id= " + nRaceId + " and name = '" + strName.replace("'", "''") + "' and aru_source='" + strDataType + "'";
            nCount += statement.executeUpdate(strUpdate);
        }
        if (strOwner != null)
        {
            strUpdate = "insert into owner_unique_colours (owner_name, jockey_colours) values ('" + strOwner.replace("'", "''") + "', '" + strColours.replace("'", "''") + "')";
            nCount += statement.executeUpdate(strUpdate);
        }
        return nCount;
    }
    
    public static int updateOwnerDetails(ENEStatement statement, long lRaceId, String strSource, int nClothNumber, String strName, String strOwner, String strColours, boolean bForce)
    {
        int nReturn = 0;
        if (strOwner == null)
            strOwner = "";
        String strUpdate = "";
        if (strSource.equals("SF"))
        {
            if ("".equals(strColours))
                return 0;
            
            strUpdate = "update daily_runners set jockey_colours=?, loaded_at=timestamp('1971-01-01') where race_id= ? and name = ?";
        }
        else if (bForce)
        {
            if ("".equals(strOwner))
            {
                strUpdate = "update additional_runners set cloth_number= ?, jockey_colours= ?, owner_name= case when 1 = 1 then owner_name else ? end where race_id= ? and aru_source=? and name = ?";
            }
            else
            {
                strUpdate = "update additional_runners set cloth_number= ?, jockey_colours= ?, owner_name= ? where race_id= ? and aru_source=? and name = ?";
            }
        }
        else
        {
            
            strUpdate = "update additional_runners set cloth_number= ?, jockey_colours=case when coalesce(jockey_colours,'')='' then ? else jockey_colours end, owner_name= case when coalesce(owner_name,'')='' then ? else owner_name end where race_id= ? and aru_source=? and name = ?";
        }
    
        PreparedStatement insert = null;
           try 
           {
               insert = statement.getPreparedStatement(strUpdate);
                if (strSource.equals("SF"))
                {
                    insert.setString(1, strColours.replace("'", "''"));
                    insert.setLong(2, lRaceId);
                    insert.setString(3, strName);
                }
                else
                {
                    if (nClothNumber > 0)
                        insert.setInt(1, nClothNumber);
                    else
                        insert.setNull(1, java.sql.Types.INTEGER);
                    insert.setString(2, strColours.replace("'", "''"));
                    insert.setString(3, strOwner.replace("'", "''"));
                    insert.setLong(4, lRaceId);
                    insert.setString(5, strSource);
                    insert.setString(6, strName);
                }
                    
                nReturn += insert.executeUpdate();
          }
           catch(Exception e)
           {
               System.out.println("updateOwnerDetails: " + e.getMessage());
           }
/*           finally
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
}
