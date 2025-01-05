/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

import org.json.JSONArray;
import org.json.JSONObject;
import ene.eneform.utils.ExecuteURL;
import ene.eneform.utils.JSONUtils;
import ene.eneform.utils.StringUtils;
import org.apache.commons.httpclient.methods.PostMethod;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Simon
 */
public class RacingPostHorse {

    private static String sm_strHorseURL = "https://www.racingpost.com/profile/horse/%ID%/%NAME%";

    private String m_strBred="";

    private String m_strURL;
    private String m_strName;
    private String m_strRPName;
    private int m_nYear = 0;
    private long m_lID;
    public RacingPostHorse(String strName, long lID)
    {
        m_strName = strName;
        m_strRPName = convertName(strName);
        //m_strBred = strBred;
        //m_nYear = nYear;
        m_lID = lID;
        m_strURL = sm_strHorseURL.replace("%ID%", String.valueOf(lID)).replace("%NAME%", m_strRPName).toLowerCase();
    }
        public String getBred() {
            return m_strBred;
        }

        public String getRPName() {
            return m_strRPName;
        }
        public String getName() {
            return m_strName;
        }
        public String getURL() {
            // https://www.racingpost.com/profile/horse/724457/maarek
            return m_strURL;
        }
       public String getAdjustedURL() {
           // https://www.racingpost.com/profile/horse/724457/maarek
           // becomes
           // https://www.racingpost.com/profile/horse/tabs/724457/maarek/form/horse/0/0/1/desktop
            //return m_strURL.replace(String.valueOf(m_lID), "tabs/" + m_lID) + "/form/horse/0/0/1/desktop";
           
          // 20170822 - RacingPost now returns data as JSON
           // https://www.racingpost.com/profile/tab/horse/724457/maarek/form
            return m_strURL.replace("horse/" + m_lID, "tab/horse/" + m_lID) + "/form";
        }
       public static String convertName(String strName)
       {
           return strName.replace(" ", "-").replace("'", "").replace("ñ", "n").replace(".", "");
       }
       public int getYear() {
            return m_nYear;
        }

        public long getID() {
            return m_lID;
        }
        public void setYear(int nYear)
        {
            m_nYear = nYear;
        }
        public void setBred(String strBred)
        {
            m_strBred = strBred;
        }
     public static ArrayList<Long> getHorseId(String strHorse) {
        return getHorseId(strHorse, 2050); // year in future
    }

    public static ArrayList<Long> getHorseId(String strHorse, int nYear) 
    {
        // without bred, could be multiple matches
        ArrayList<Long> alHorseIds = new ArrayList<Long>();
        ArrayList<RacingPostHorse> alHorses = getHorseList(strHorse);
        Iterator<RacingPostHorse> iter = alHorses.iterator();
        while (iter.hasNext()) {
            RacingPostHorse horse = iter.next();
            if ((horse.getYear() + 1 < nYear) && horse.getRPName().equalsIgnoreCase(convertName(strHorse))) 
            {
                alHorseIds.add(horse.getID());
            }
        }
        return alHorseIds;
    }

    public static long getHorseId(String strHorse, String strBred) {
        return getHorseId(strHorse, strBred, 2050); // year in future
    }

    public static long getHorseId(String strHorse, String strBred, int nRaceYear) 
    {
        RacingPostHorse horse = getHorse(strHorse, strBred, nRaceYear);
        if (horse == null)
            return -1;
        else
            return horse.getID();
    }
    public static RacingPostHorse getHorse(String strHorse, String strBred, int nRaceYear) 
    {
        ArrayList<RacingPostHorse> alHorses = getHorseList(strHorse);
        String strHorse1 = null;
        if (alHorses.isEmpty()) 
        {
            strHorse1 = strHorse.replace("-", " ");
            alHorses = getHorseList(strHorse1);
            if (alHorses.isEmpty()) {
                int nHyphen = strHorse.indexOf("-");
                if (nHyphen > 0) {
                    strHorse1 = strHorse.substring(0, nHyphen);
                    alHorses = getHorseList(strHorse1);
                }
            }
        }
        Iterator<RacingPostHorse> iter = alHorses.iterator();
        while (iter.hasNext()) 
        {
            RacingPostHorse horse = iter.next();
            if ((horse.getYear() + 1 < nRaceYear) && (horse.getName().replace("-", " ").replace("'", "").replace(" I", "").equalsIgnoreCase(strHorse.replace("-", " ").replace("'", "").replace(" I", "")) 
                    || horse.getName().equalsIgnoreCase(strHorse1)) && (horse.getBred().equalsIgnoreCase(strBred) 
                    || (("".equals(strBred) || "UK".equals(strBred) || "GB".equals(strBred)) && (horse.getBred().equalsIgnoreCase("UK") || horse.getBred().equalsIgnoreCase("GB"))))) 
            {
                System.out.println(horse.getID() + "-" + horse.getName() + "-" + horse.getBred());
                return horse;
            }
        }
        System.out.println("RP Horse not found: " + strHorse + "-" + strBred + "-" + nRaceYear);
        return null;
    }

    private static ArrayList<RacingPostHorse> getHorseList(String strName) 
    {
        ArrayList<RacingPostHorse> alHorses = new ArrayList<RacingPostHorse>();
        //String strURL = "http://www.racingpost.com/public_gateway/db_search_interface.sd";
        String strURL = "https://api.swiftype.com/api/v1/public/engines/search.json";
        HashMap<String, String> hmParameters = new HashMap<String, String>();
        //hmParameters.put("search", strName);       // don't include bred in initial search
        hmParameters.put("q", convertName(strName)); // don't include bred in initial search
        hmParameters.put("engine_key", "8LYr3pPCWtAL6iEHsG1C"); 
        hmParameters.put("per_page", "25");
        //hmParameters.put("category", "2");
        //hmParameters.put("edition", "4");
        PostMethod postMethod = ExecuteURL.createPostMethod(strURL, hmParameters);
        JSONObject json = ExecuteURL.executePostMethodJSON(postMethod, false);
        JSONObject records = (JSONObject) json.get("records");
        JSONArray page = (JSONArray) records.get("page");
        for (int i = 0; i < page.length(); i++) {
            JSONObject el = (JSONObject) page.get(i);
            String strProfilesType = (String) el.get("profiles_type");
            if ("horses_profiles".equals(strProfilesType)) {
                String strCountry = (String) el.get("profiles_country_code");
                String strHorseURL = (String) el.get("url");
                String[] astrElements = strHorseURL.split("/");
                long lId = 0L;
                String strId = astrElements[astrElements.length - 2];
                try {
                    lId = Long.parseLong(strId);
                } catch (NumberFormatException e) {
                }
                //System.out.println(strHorseURL);
                RacingPostHorse horse = new RacingPostHorse((String) el.get("profiles_name"), lId);
                horse.setYear((int)(long) el.get("profiles_year_int"));
                horse.setBred(strCountry);
                alHorses.add(horse);
            }
        }

        return alHorses;
    }
/*
    public static String getHorseURL(String strHorse, String strBred) {
        return getHorseURL(strHorse, strBred, 2050); // year in future
    }

    public static String getHorseURL(String strHorse, String strBred, int nRaceYear) 
    {
        String strUrl = null;
        ArrayList<RacingPostHorse> alHorses = getHorseList(strHorse);
        String strHorse1 = null;
        if (alHorses.isEmpty()) {
            strHorse1 = strHorse.replace("-", " ");
            alHorses = getHorseList(strHorse1);
            if (alHorses.isEmpty()) {
                int nHyphen = strHorse.indexOf("-");
                if (nHyphen > 0) {
                    strHorse1 = strHorse.substring(0, nHyphen);
                    alHorses = getHorseList(strHorse1);
                }
            }
        }
        Iterator<RacingPostHorse> iter = alHorses.iterator();
        while (iter.hasNext()) 
        {
            RacingPostHorse horse = iter.next();
            if ((horse.getYear() + 1 < nRaceYear) && (horse.getName().equalsIgnoreCase(strHorse) || horse.getName().equalsIgnoreCase(strHorse1)) && (horse.getBred().equalsIgnoreCase(strBred) || ("".equals(strBred) && (horse.getBred().equalsIgnoreCase("UK") || horse.getBred().equalsIgnoreCase("GB"))))) {
                return horse.getAdjustedURL();
            }
        }
        //System.out.println(strUrl);
        return strUrl;
    } */
    public TagNode retrieveHorseDetails() throws IOException 
    {
        // 20170401:  https://www.racingpost.com/profile/horse/898073/marsha
        // now need id and name
        //String strURL  = "http://www.racingpost.com/horses/horse_home.sd?horse_id=";
        TagNode root = ExecuteURL.getRootNode(getURL(), "utf-8");
        return root;
    }
    private JSONObject getHorseDetails()
    {
        // 20170822 Data now only available via JSON embedded in HTML page
        // 20170401:  https://www.racingpost.com/profile/horse/898073/marsha
        // now need id and name
        //String strURL  = "http://www.racingpost.com/horses/horse_home.sd?horse_id=";
        TagNode root = ExecuteURL.getRootNode(getURL(), "utf-8");
        String strJSON = null;
        if (root != null)
        {
            TagNode[] scripts = root.getElementsByName("script", true);
            for(int i = 0; i < scripts.length; i++)
            {
                TagNode script = scripts[i];
                String strScriptContent = ExecuteURL.getNodeContentString(script);
                int nPreload = strScriptContent.indexOf("window.PRELOADED_STATE = ");
                if (nPreload >= 0)
                {
                    strJSON = strScriptContent.substring(nPreload + "window.PRELOADED_STATE = ".length()).replace(")();", "").replaceAll("window.adsTargeting = [a-z\"]+", "").trim().replaceAll(";[\\s]+}", "").replaceAll(";[\\s]+", "");
                    break;
                }
            }
            
            if (strJSON == null)
                return null;
            
            JSONObject json = JSONUtils.parse(strJSON);
            if (json == null)
            {
                System.out.println("Invalid JSON: " + strJSON);
                return null;
            }
            if (!json.has("profile"))
                return null;
            
            JSONObject jsonProfile  = (JSONObject) json.get("profile");
        
            return jsonProfile;
        }
        
        return null;
    }
    public String updateCareerDetails() throws IOException 
    {
        JSONObject jsonProfile = getHorseDetails();
        if (jsonProfile != null)
        {    
            String strSire = JSONUtils.getJSONAttributeString(jsonProfile, "sireHorseName");
            String strDam = JSONUtils.getJSONAttributeString(jsonProfile, "damHorseName");
            String strDamSire = JSONUtils.getJSONAttributeString(jsonProfile, "damSireHorseName");
            String strCurrentOwner = JSONUtils.getJSONAttributeString(jsonProfile, "ownerName");
            String strOwner = strCurrentOwner;
            String strFoalingDate = JSONUtils.getJSONAttributeString(jsonProfile, "horseDateOfBirth");
            if (strFoalingDate !=null)
               strFoalingDate = strFoalingDate.substring(0, 10);
            
            if (strFoalingDate !=null)
                return "update career_horses set foaling_date='" + strFoalingDate + "', dam_sire_name='" + strDamSire.replace("'", "''") + "' where horse_name='" + m_strName.replace("'", "''") + "';";
        }
        
        return "";
    }
    public String updateHorseDetails(String strName, String strSource, int nRace, String strMeetingDate) throws IOException 
    {
        JSONObject jsonProfile = getHorseDetails();
        if (jsonProfile != null)
        {    
            String strSire = JSONUtils.getJSONAttributeString(jsonProfile, "sireHorseName");
            String strDam = JSONUtils.getJSONAttributeString(jsonProfile, "damHorseName");
            String strDamSire = JSONUtils.getJSONAttributeString(jsonProfile, "damSireHorseName");
            String strCurrentOwner = JSONUtils.getJSONAttributeString(jsonProfile, "ownerName");
            String strOwner = strCurrentOwner;
            String strFoalingDate = JSONUtils.getJSONAttributeString(jsonProfile, "horseDateOfBirth").substring(0, 10);

            JSONArray previousOwners = null;

            if (jsonProfile.has("previousOwners"))
                previousOwners = (JSONArray)jsonProfile.get("previousOwners");
            if ((previousOwners != null) && (previousOwners.length() > 0))
            {
                try
                {
                    RacingPostHorseOwner owner = new RacingPostHorseOwner(strCurrentOwner);
                    GregorianCalendar cal = new GregorianCalendar();
                    SimpleDateFormat rpDate = new SimpleDateFormat("yyyy-MM-dd");
                    for(int i = 0; i < previousOwners.length(); i++)
                    {
                            JSONObject previous = (JSONObject) previousOwners.get(i);
                            String strChangeDate = JSONUtils.getJSONAttributeString(previous, "ownerChangeDate").substring(0, 10);
                            Date dtChange = rpDate.parse(strChangeDate);
                            cal.setTimeInMillis(dtChange.getTime());
                            owner.addPreviousOwner(JSONUtils.getJSONAttributeString(previous, "ownerStyleName"), cal);
                    }
                    Date dtRace = (new SimpleDateFormat("dd MMM yyyy")).parse(strMeetingDate);
                    GregorianCalendar calRace = new GregorianCalendar();
                    calRace.setTimeInMillis(dtRace.getTime());
                    strOwner = owner.getOwnerOnDate(calRace);
                    System.out.println("Owner on date: " + strMeetingDate + "-" + strOwner); 
                } 
                catch (ParseException e) 
                {
                    System.out.println("ParseException getOwner: " + strCurrentOwner);
                }
            }
    /*                
                String strSire = ExecuteURL.getRowAttribute(root, "data-test-selector", "details-sire");
                String strDam = ExecuteURL.getRowAttribute(root, "data-test-selector", "details-dam");
                String strDamSire = ExecuteURL.getRowAttribute(root, "data-test-selector", "details-dam-sire");
                String strCurrentOwner = ExecuteURL.getRowAttribute(root, "data-test-selector", "details-owner");
                String strOwner = strCurrentOwner;
                 try 
                {
                    RacingPostOwner owner = new RacingPostOwner(strCurrentOwner);
                    SimpleDateFormat rpDate = new SimpleDateFormat("dd MMM yyyy");
                    System.out.println("Current owner: " + strCurrentOwner);
                    TagNode[] aPreviousOwners = root.getElementsByAttValue("class", "hp-horseDetail__listItem hp-horseDetail__listItemPrevOwners", true, true);
                    if (aPreviousOwners.length > 0)
                    {
                        aPreviousOwners = aPreviousOwners[0].getElementsByName("div", true);
                        for (int j = 0; j < aPreviousOwners.length; j++) 
                        {
                            String strPreviousOwner = aPreviousOwners[j].getText().toString().replace("\n", "").trim();
                            System.out.println(strPreviousOwner); 
                            String[] aItems = strPreviousOwner.split(" until ");
                            Date dtRace = rpDate.parse(aItems[1]);
                            GregorianCalendar cal = new GregorianCalendar();
                            cal.setTimeInMillis(dtRace.getTime());
                            owner.addPreviousOwner(aItems[0], cal);
                        }
                    }
                    Date dtRace = rpDate.parse(strMeetingDate);
                    GregorianCalendar calRace = new GregorianCalendar();
                    calRace.setTimeInMillis(dtRace.getTime());
                    strOwner = owner.getOwnerOnDate(calRace);
                    System.out.println("Owner on date: " + strMeetingDate + "-" + strOwner); 
                } 
                catch (ParseException e) 
                {
                    System.out.println("ParseException getOwner: " + strCurrentOwner);
                }
                TagNode[] dd = root.getElementsByName("dd", true);
                String strFoalingDate = "";
                for(int i = 0; i < dd.length; i++)
                {
                    String strClass = dd[i].getAttributeByName("class");
                    // "hp-horseDetail_listDescription hp-horseDetail__listItemAge_value"
                    if (strClass.indexOf("hp-horseDetail__listItemAge_value")>= 0)
                        strFoalingDate = dd[i].getText().toString().trim().split("\\ ")[0].replace("(", "").replace(")", "");
                } */
    
            if ("SF".equals(strSource))
            {
                return "update historic_runners set loaded_at=loaded_at, foaling_date='" + strFoalingDate + "', dam_sire_name='" + strDamSire.replace("'", "''") + "', owner_name = '" + strOwner.replace("'", "''") + "' where name='" + strName.replace("'", "''") + "' and aru_source='RP' and race_id=" + nRace + ";";
//                return "update historic_runners set loaded_at=loaded_at, foaling_date=DATE(STR_TO_DATE('" + strFoalingDate + "', '%d%b%y')), dam_sire_name='" + strDamSire.replace("'", "''") + "', owner_name = '" + strOwner.replace("'", "''") + "' where name='" + strName.replace("'", "''") + "' and aru_source='RP' and race_id=" + nRace + ";";
            }
            else
            {
                return "update additional_runners set foaling_date='" + strFoalingDate + "', dam_sire_name='" + strDamSire.replace("'", "''") + "', owner_name = " + ((strOwner == null) ? "owner_name" : ("'" + strOwner.replace("'", "''") + "'" )) + " where name='" + strName.replace("'", "''") + "' and aru_source='RP' and race_id=" + nRace + ";";
//                return "update additional_runners set foaling_date=DATE(STR_TO_DATE('" + strFoalingDate + "', '%d%b%y')), dam_sire_name='" + strDamSire.replace("'", "''") + "', owner_name = " + ((strOwner == null) ? "owner_name" : ("'" + strOwner.replace("'", "''") + "'" )) + " where name='" + strName.replace("'", "''") + "' and aru_source='RP' and race_id=" + nRace + ";";
            }
        }
        else
            return "";
    }

    public RacingPostHorseOwner getOwner() throws IOException 
    {
        RacingPostHorseOwner owner = null;
        TagNode root = ExecuteURL.getRootNode(getAdjustedURL(), "utf-8");
        TagNode[] aLIs = root.getElementsByName("li", true);
        for (int i = 0; i < aLIs.length; i++) {
            TagNode li = aLIs[i];
            String strContent = li.getText().toString();
            if (strContent.indexOf("Owner\n") == 0) {
                // this is the owner list item
                String strCurrentOwner = li.getElementsByName("a", true)[0].getText().toString();
                owner = new RacingPostHorseOwner(strCurrentOwner);
                if (!"".equals(strCurrentOwner)) {
                    System.out.println("Current owner: " + strCurrentOwner);
                    TagNode[] aPreviousOwners = li.getElementsByName("li", true);
                    for (int j = 0; j < aPreviousOwners.length; j++) {
                        String strPreviousOwner = aPreviousOwners[j].getText().toString().replace("\n", "").trim();
                        String[] aItems = strPreviousOwner.split(" until ");
                        SimpleDateFormat rpDate = new SimpleDateFormat("dd MMM yyyy");
                        try {
                            Date dtRace = rpDate.parse(aItems[1]);
                            GregorianCalendar cal = new GregorianCalendar();
                            cal.setTimeInMillis(dtRace.getTime());
                            owner.addPreviousOwner(aItems[0], cal);
                        } catch (ParseException e) {
                            System.out.println("ParseException getOwner: " + strPreviousOwner);
                        }
                    }
                }
                continue;
            }
        }
        return owner;
    }

    public String getOwnerName() throws IOException {
        RacingPostHorseOwner owner = getOwner();
        return owner.getCurrentOwner();
    }

    public String getOwnerName(Calendar dt) throws IOException {
        RacingPostHorseOwner owner = getOwner();
        if (owner != null) 
        {
            return owner.getOwnerOnDate(dt);
        }
        return null;
    }
    public ArrayList<RacingPostHorseRaceSummary> getHorseRacesJSON() throws InterruptedException, IOException
    {
        // 20170822 - RacingPost now returns data as JSON
        ArrayList<RacingPostHorseRaceSummary> alRaces = new ArrayList<RacingPostHorseRaceSummary>();
        JSONObject json = ExecuteURL.executeURLJSON(getAdjustedURL(), "utf-8");
        JSONObject jsonForm = (JSONObject) json.get("form");
        Set<String> raceKeys = jsonForm.keySet();
        Iterator<String> iter = raceKeys.iterator();
        while(iter.hasNext())
        {
            Map.Entry<String, JSONObject> current = (Map.Entry<String, JSONObject>) jsonForm.get(iter.next());
            String strRaceId = current.getKey();
            JSONObject obj = (JSONObject) current.getValue();
            String strCourseCode=JSONUtils.getJSONAttributeString(obj, "courseName").toLowerCase().replace(" ", "-").replace(".", "").replace("(", "").replace(")", "");
             String strRaceURL = "https://www.racingpost.com/results/" + obj.get("courseUid") + "/" + strCourseCode + "/" + JSONUtils.getJSONAttributeString(obj, "raceDatetime").substring(0, 10) + "/" + strRaceId; 
             String strHorse= this.getRPName();
             String strPosition = JSONUtils.getJSONAttributeString(obj, "raceOutcomeCode");
             int nPosition = StringUtils.getNumeric(strPosition);
             String strJockey = JSONUtils.getJSONAttributeString(obj, "jockeyStyleName");
             String strRaceTitle = JSONUtils.getJSONAttributeString(obj, "raceInstanceTitle");
            RacingPostHorseRaceSummary summary = new RacingPostHorseRaceSummary(strRaceURL, strHorse, nPosition, strJockey);
            summary.setTitle(strRaceTitle);
            alRaces.add(summary);
        }
        return alRaces;
    }
    private ArrayList<RacingPostHorseRaceSummary> getHorseRaces() throws IOException 
    {
        // do not use
        // replaced by getHorseRacesJSON
        TagNode root = ExecuteURL.getRootNode(getAdjustedURL(), "utf-8");
        if (root == null) {
            System.out.println("getHorseRaces No content: " + m_strName + "-" + m_lID);
            return new ArrayList<RacingPostHorseRaceSummary>(); // no races
        }
        TagNode[] aTables = root.getElementsByName("table", true);
        if (aTables.length == 0) {
            System.out.println("getHorseRaces No tables: " + m_strName + "-" + m_lID);
            return new ArrayList<RacingPostHorseRaceSummary>(); // no races
        }
        TagNode raceTable = aTables[1]; // first table is summary of horse, owner etc, second table contains races
        String strHorse = ExecuteURL.getRowAttribute(raceTable, "class", "hp-horseNameRow__name js-horseNameContainer");
        ArrayList<RacingPostHorseRaceSummary> alRaces = new ArrayList<RacingPostHorseRaceSummary>();
        TagNode[] aRows = raceTable.getElementsByName("tr", true);
        TagNode headerRow = aRows[0];
        HashMap<String, Integer> hmColumns = ExecuteURL.getColumnNumbers(headerRow, "th");
        for (int i = 1; i < aRows.length; i++) {
            TagNode row = aRows[i];
            //String strRaceID = row.getAttributeByName("id");
            //if (strRaceID != null)      // two rows for each race - ignore 2nd
            //{
            TagNode[] aCells = row.getElementsByName("td", true);
            try {
                // new column headers Jockey, Wgt, OR, MR, RPR, Dist., Pos., SP, Date, Conditions, Gng., TS
                // old column headers JOCKEY, WGT, OR, -, -, RACE CONDITIONS, RACE OUTCOME, DATE, RACE CONDITIONS, -, -
                TagNode tnDate = aCells[hmColumns.get("Date")];
                TagNode[] tnRaceData = tnDate.getElementsByName("a", true);
                if (tnRaceData.length > 0) {
                    TagNode dateNode = ExecuteURL.getRowAttributeNode(row, "data-test-selector", "item-table-date");
                    String strRaceTitle = dateNode.getAttributeByName("title");
                    // URL is of form:  /results/257/santa-anita/2016-11-05/662271
                    String strRaceURL = dateNode.getAttributeByName("href");
                    String strRaceID = "";
                    //String strDate = tnDate.getText().toString().trim().replace("&nbsp;", "");
                    String strRaceConditions = aCells[hmColumns.get("Conditions")].getText().toString().replace("\n", "").replaceAll("[\\s]+", " ").trim();
                    String strWeight = aCells[hmColumns.get("Wgt")].getText().toString().trim();
                    String strOfficialRating = "";
                    if (hmColumns.get("OR") > 0) {
                        strOfficialRating = aCells[hmColumns.get("OR")].getText().toString().trim();
                    }
                    String strRaceOutcome = aCells[hmColumns.get("Pos.")].getText().toString().trim().replace("\n", "").replaceAll("[\\s]+", " ").trim();
                    String strJockey = aCells[hmColumns.get("Jockey")].getText().toString().trim();
                    //System.out.println(strRaceID +"-" + strRaceTitle + "-" + strRaceConditions + "-" + strWeight + "-" + strOfficialRating + "-" + strRaceOutcome + "-" + strJockey);
                    String[] astrRaceConditions = strRaceConditions.split(" ");
                    String[] astrRaceOutcome = strRaceOutcome.split(" ");
                    String strGoing = ExecuteURL.getRowAttribute(row, "data-test-selector", "item-table-going");
                    //String strCourse = astrRaceConditions[0].toUpperCase();
                    String strDistance = ExecuteURL.getRowAttribute(row, "data-test-selector", "item-table-distance"); // format is 1m4f
                    int nPosition = StringUtils.getNumeric(astrRaceOutcome[0].split("/")[0]);
                    RacingPostCourse course = null;
                    long lRaceID = 0;
                    String strRPDate = "";
                    RacingPostHorseRaceSummary summary = new RacingPostHorseRaceSummary(strRaceURL, strHorse, nPosition, strJockey);
                    summary.setTitle(strRaceTitle);
                    alRaces.add(summary);
                }
            } catch (NumberFormatException e) {
                System.out.println("NumberFormatException: " + e.getMessage());
            }
            //}
        }
        return alRaces;
    }
}
