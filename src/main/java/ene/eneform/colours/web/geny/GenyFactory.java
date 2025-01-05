/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.geny;

import ene.eneform.utils.ENEStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Simon
 */
public class GenyFactory {

    public static int insertGenyOwner(ENEStatement statement, String strOwner, String strCountry, int nOwnerId, int nSuffix) {
        int nReturn = 0;
        String strUpdate = "REPLACE INTO geny_owners (geny_owner_name, geny_country, geny_colours_id, geny_colours_suffix)";
        strUpdate += "values (?, ?, ?, ?)";
        PreparedStatement insert = null;
        try 
        {
            insert = statement.getPreparedStatement(strUpdate);
            insert.setString(1, strOwner);
            insert.setString(2, strCountry);
            insert.setInt(3, nOwnerId);
            insert.setInt(4, nSuffix);
            nReturn = insert.executeUpdate();
            insert.close();
        } catch (Exception e) {
            System.out.println("insertGenyOwner Exception: " + e.getMessage());
        }
        return nReturn;
    }

    public static String selectGenyOwnerId(ENEStatement statement, String strOwner) {
        String strOwnerId = null;
        String strSelect = "select concat(cast(geny_colours_id as char), '_', cast(geny_colours_suffix as char)) from geny_owners where geny_owner_name = '" + strOwner.replace("'", "''") + "'";
        ResultSet rs = statement.executeQuery(strSelect);
        if (rs != null) {
            try {
                if (rs.next()) {
                    strOwnerId = rs.getString(1).trim();
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("createUnregisteredColourSyntax SQLException: " + e.getMessage());
            }
        }
        return strOwnerId;
    }
    
}
