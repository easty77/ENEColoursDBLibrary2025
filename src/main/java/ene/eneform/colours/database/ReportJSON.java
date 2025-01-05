/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import org.json.JSONObject;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.JSONUtils;

/**
 *
 * @author simon
 */
public class ReportJSON {
/*    
public static void selectJSONRaceNameFileYear(ENEStatement statement, String strFileName, String strDirectory, String strRaceName, int nYear)
{
    JSONObject params = new JSONObject();
    params.put("race_name", strRaceName);
    params.put("where", " and year(meeting_date) = " + nYear);
    String strInputFile = strFileName+ ".sql";
    String strOutputFile = strDirectory + "/" + strRaceName.replace(" ", "_") + nYear + ".json";
        JSONUtils.selectJSONFile(statement, 
                ENEColoursEnvironment.getInstance().getVariable("QUERY_DIRECTORY") + strInputFile, 
                ENEColoursEnvironment.getInstance().getVariable("JSON_OUTPUT_DIRECTORY") + strOutputFile, 
                params, -1, false);   // retrieve all records, omit attribute if value is null
}
public static void selectJSONRaceNameFiles(ENEStatement statement, String strFileName, String strDirectory, String[] astrRaceName)
{
    for(int i = 0; i < astrRaceName.length; i++)
    {
        selectJSONRaceNameFile(statement, strFileName, strDirectory, astrRaceName[i]);
    }
}
public static void selectJSONRaceNameFile(ENEStatement statement, String strFileName, String strDirectory, String strRaceName)
{
    JSONObject params = new JSONObject();
    params.put("race_name", strRaceName);
    params.put("where", "");
    String strInputFile = strFileName + ".sql";
    String strOutputFile = strDirectory + "/" + strRaceName.replace(" ", "_") + ".json";
        JSONUtils.selectJSONFile(statement, 
                ENEColoursEnvironment.getInstance().getVariable("QUERY_DIRECTORY") + strInputFile, 
                ENEColoursEnvironment.getInstance().getVariable("JSON_OUTPUT_DIRECTORY") + strOutputFile, 
                params, -1, false);   // retrieve all records, omit attribute if value is null
}
*/
public static void reportJSONRaceNameFile(ENEStatement statement, String strFileName, String strDirectory, String strRaceName)
{
    JSONObject params = new JSONObject();
    params.put("race_name", strRaceName);
    params.put("where", "");
    String strInputFile = strFileName + ".sql";
    String strOutputFile = strDirectory + "/" + strRaceName.replace(" ", "_") + ".json";
    String strQuery = JSONUtils.processQueryFile(ENEColoursEnvironment.getInstance().getVariable("QUERY_DIRECTORY") + strInputFile, true, params, null);
    JSONUtils.reportJSON2File(statement, strQuery,
                ENEColoursEnvironment.getInstance().getVariable("JSON_OUTPUT_DIRECTORY") + strOutputFile, 
                -1, JSONUtils.DATATABLE);   // retrieve all records, omit attribute if value is null
}

}