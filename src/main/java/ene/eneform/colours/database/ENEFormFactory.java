/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import org.json.JSONArray;
import org.json.JSONObject;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.JSONUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author simon
 */
public class ENEFormFactory {
  
    private static String TRIAL_FORM_DIR = "C:/Users/simon/git/ENETrialForm/";
    private static String TRIAL_OUTPUT_DIR = TRIAL_FORM_DIR + "public/data/";
    private static String TRIAL_JSON_FILE = TRIAL_FORM_DIR + "public/trials.json";
    private static String TRIAL_QUERY_FILE = TRIAL_FORM_DIR + "public/select_trial_performance.sql";
    private static String TARGET_QUERY_SUMMARY_FILE = TRIAL_FORM_DIR + "public/select_target_summary.sql";
    private static String TARGET_QUERY_DETAIL_FILE = TRIAL_FORM_DIR + "public/select_target_detail.sql";
    private static String TARGET_QUERY_TRIAL_FILE = TRIAL_FORM_DIR + "public/select_target_trials.sql";
    
    public static void generateTargetData(ENEStatement statement) throws FileNotFoundException, IOException
    {
    JSONArray paramNames = new JSONArray();
    paramNames.put("target");
    JSONObject params = new JSONObject();
    params.put("params", paramNames);
    JSONArray aTrialData = JSONUtils.parseJSONFileArray("C:/Users/simon/git/ENETrialForm/public/trials.json");
    for (int i= 0; i < aTrialData.length(); i++)
    {
        JSONObject target = (JSONObject)aTrialData.get(i);
        String strTarget = (String)target.get("target");
        if (!"Falmouth".equals(strTarget))
            continue;
        params.put("target", strTarget);
        String strFileName = generateTrialFileName(TRIAL_OUTPUT_DIR, strTarget, "summary", 0);
        System.out.println(buildTrialFile(statement, TARGET_QUERY_SUMMARY_FILE, strFileName, params));
        strFileName = generateTrialFileName(TRIAL_OUTPUT_DIR, strTarget, "detail", 0);
        System.out.println(buildTrialFile(statement, TARGET_QUERY_DETAIL_FILE, strFileName, params));
    }
    }
    public static void generateTrialsData(ENEStatement statement) throws FileNotFoundException, IOException
    {
    
    JSONArray paramNames = new JSONArray();
    paramNames.put("trial");
    paramNames.put("target");
    paramNames.put("year_offset");
    JSONObject params = new JSONObject();
    params.put("params", paramNames);
    JSONArray aTrialData = JSONUtils.parseJSONFileArray("C:/Users/simon/git/ENETrialForm/public/trials.json");
    for (int i= 0; i < aTrialData.length(); i++)
    {
        JSONObject target = (JSONObject)aTrialData.get(i);
        String strTarget = (String)target.get("target");
        if (!"Falmouth".equals(strTarget))
            continue;
        params.put("target", strTarget);
        JSONArray aTrials = getTargetTrials(statement, strTarget, 0);
        //JSONArray aTrials = (JSONArray) target.get("current");
        params.put("year_offset_nr", 0);
       for (int j= 0; j < aTrials.length(); j++)
        {
            String strTrial = (String) ((JSONObject)aTrials.get(j)).get("ard_name");
            params.put("trial", strTrial);
            String strFileName = generateTrialFileName(TRIAL_OUTPUT_DIR, strTarget, strTrial, 0);  
            System.out.println(buildTrialFile(statement, TRIAL_QUERY_FILE,  strFileName, params));
        } 
        aTrials = getTargetTrials(statement, strTarget, 1);
        //aTrials = (JSONArray) target.get("previous");
        params.put("year_offset_nr", 1);
        for (int j= 0; j < aTrials.length(); j++)
        {
            String strTrial = (String) ((JSONObject)aTrials.get(j)).get("ard_name");
           //if (!"Coventry".equals(strTrial))
           //     continue;
           params.put("trial", strTrial);
            String strFileName = generateTrialFileName(TRIAL_OUTPUT_DIR, strTarget, strTrial, 1);   
            System.out.println(buildTrialFile(statement, TRIAL_QUERY_FILE,  strFileName, params));
        }
    }
    } 
    private static String generateTrialFileName(String strDir, String strTarget, String strTrial, int nYearOffset)
    {
        strTarget = strTarget.replace(' ', '_');
        
        File directory = new File(String.valueOf(strDir + strTarget));

        if(!directory.exists())
        {
             directory.mkdir();
             File previous = new File(String.valueOf(strDir + strTarget + "/previous"));
             previous.mkdir();
             File current = new File(String.valueOf(strDir + strTarget + "/current"));
             current.mkdir();
        }
            
        if (!strTrial.equals("summary") && !strTrial.equals("detail"))
        {
            strTrial = strTrial.replace("1000 Guineas Trial", "Trial").replace("2000 Guineas Trial", "Trial").replace("Oaks Trial", "Trial").replace("Derby Trial", "Trial");
            strTrial = strTrial.replace(' ', '_');
            return strDir + strTarget + (nYearOffset==1 ? "/previous/" : "/current/") + strTrial + ".json";
        }
        else
            return strDir + strTarget + "/" + strTrial + ".json";
    }
    private static String buildTrialFile(ENEStatement statement, String strQueryFile, String strOutputFile, JSONObject params)
    {
         return JSONUtils.reportJSONFile2File(statement, strQueryFile, params, strOutputFile, 50000, JSONUtils.JSON, null);
    }
    private static JSONArray getTargetTrials(ENEStatement statement, String strTarget, int nYearOffset)
    {
     JSONArray paramNames = new JSONArray();
    paramNames.put("target");
    paramNames.put("year_offset");
    JSONObject params = new JSONObject();
    params.put("params", paramNames);
    params.put("target", strTarget);
    params.put("year_offset", nYearOffset);
   JSONObject jsonObj = JSONUtils.reportJSONFile(statement, 
                    TARGET_QUERY_TRIAL_FILE, 
                    params, 500, JSONUtils.JSON, null); 
   JSONArray array = (JSONArray) jsonObj.get("data");
   return array;
}
}
