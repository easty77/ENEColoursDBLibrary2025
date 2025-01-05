/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.pt;

import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.database.AdditionalRacesFactory;
import ene.eneform.smartform.bos.AdditionalRace;
import ene.eneform.smartform.bos.AdditionalRunner;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import ene.eneform.utils.StringUtils;
import org.htmlcleaner.BaseToken;
import org.htmlcleaner.TagNode;

import jakarta.xml.soap.SOAPException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Simon
 */
public class ParisTurfResults {
    private static String sm_strRaceDateURL="http://www.paris-turf.com/index.php/menu/courses/$YYYY-MM-DD$";
    private static String sm_strRaceURL="http://www.paris-turf.com/toutes-les-courses/reunion-du-$YYYY-MM-DD$-a-$COURSE$/$RACE_TITLE$-$RACE_ID$";
    private static SimpleDateFormat sm_dtRace = new SimpleDateFormat("yyyy-MM-dd");
    private static final String [] sm_astrUKCourses={"aintree","ascot","ayr","bangor","bath","beverley","brighton","carlisle","cartmel","catterick","cheltenham","chepstow","chester","clonmel","cork","curragh", "doncaster","downpatrick","down-royal","dundalk","epsom","exeter","fairyhouse","fakenham","ffos-las","folkestone","fontwell","galway","goodwood","gowran-park","hamilton","haydock","hereford","hexham","huntingdon","kelso","kempton","leicester","leopardstown","limerick","lingfield","ludlow","market-rasen","musselburgh","naas","navan","newbury","newcastle","newmarket","newton-abbott","nottingham","perth","phoenix-park", "plumpton","pontefract","punchestown","redcar","ripon","salisbury","sandown","sedgfield","southwell","stratford","taunton","thirsk","thurles","towcester","tramore","uttoxeter","warwick","wetherby","wexford","wincanton","windsor","wolverhampton","worcester","yarmouth","york"};
    private static final List sm_lstUKCourses = new ArrayList(Arrays.asList(sm_astrUKCourses));
    // added curragh 20150423 later phoenix park
     private static final SimpleDateFormat sm_fmtDate;

    static
    {
        sm_fmtDate = new SimpleDateFormat( "yyyy-MM-dd" );
     }

    public static void retrieveCalendar(ENEStatement statement, int nYear) throws FileNotFoundException, UnsupportedEncodingException, IOException, SOAPException, Exception
   {
       Calendar cal = Calendar.getInstance();
       cal.set(Calendar.YEAR, nYear);
       cal.set(Calendar.MONTH, 0);
       cal.set(Calendar.DATE, 1);
       while(cal.get(Calendar.YEAR) == nYear)
       //while(cal.get(Calendar.MONTH) < 3)
       {
           try
           {
                loadResultsDate(statement, sm_fmtDate.format(cal.getTime()));
           }
           catch(Exception e)
           {
               System.out.println("retrieveCalendar exception: " + sm_fmtDate.format(cal.getTime()) + "-" + e.getMessage());
           }
        cal.add(Calendar.DAY_OF_YEAR, 1);   // 5 days shown at a time
       }
   }

    public static void loadResultsDate(ENEStatement statement, String strDate)
    {
        String strURL  = sm_strRaceDateURL.replace("$YYYY-MM-DD$", strDate);
        try
        {
        TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");
        TagNode[] aMedia = root.getElementsByAttValue("class", "odd race_number", true, true);  
        int nCount = extractRaceData(statement, strDate, aMedia);
        aMedia = root.getElementsByAttValue("class", "event race_number", true, true);   
        nCount += extractRaceData(statement, strDate, aMedia);
        if (nCount > 0)
        {
            System.out.println(strDate + ": " + nCount);
        }
        }
        catch(Exception e)
        {
            System.out.println("loadResultsDate: " + strDate + "-" + e.getMessage());
            e.printStackTrace();
        }
     }
    private static int extractRaceData(ENEStatement statement, String strDate, TagNode[] aMedia) throws ParseException
    {
        int nCount = 0;
        for(int i = 0; i < aMedia.length; i++)
        {
            TagNode[] aLink = aMedia[i].getElementsByName("a", true);
            for(int j = 0; j < aLink.length; j++)
            {
                String strRaceURL = aLink[j].getAttributeByName("href");
                strRaceURL = strRaceURL.replace("/index.php/toutes-les-courses/reunion-du-" + strDate + "-a-", "");
                strRaceURL = strRaceURL.replace("/programme-courses/" + strDate + "/reunion-", "");
                String[] aRaceURL = strRaceURL.split("/");
                String strCourse = aRaceURL[0];
                // aRaceURL[1] = resultats-rapports
                String[] aRaceURL1 = aRaceURL[2].split("-");
                int nItems = aRaceURL1.length;
                String strId = aRaceURL1[nItems - 1];
                String strTitle = aRaceURL[1].replace("-" + strId, "");
                System.out.println(strRaceURL + " - " + strCourse);
                if ("grande-bretagne".equals(strCourse) || "irlande".equals(strCourse) ||
                    sm_lstUKCourses.contains(strCourse))
                {
                    try
                    {
                        int nId = Integer.valueOf(strId);
                        if (ParisTurfFactory.existsParisTurfRace(statement, nId))
                            continue;

                        ParisTurfRace race = new ParisTurfRace(sm_fmtDate.parse(strDate), strCourse, strTitle, nId, "");
                        String strWinner = extractWinner(race);
                        race.setWinner(strWinner);
                        ParisTurfFactory.insertParisTurfRace(statement, race);
                        //System.out.println(strRaceURL + ": " + strDate +", " + strCourse + ", " + strTitle + ", " + strId);
                        nCount++;
                    }
                    catch(NumberFormatException e)
                    {
                        System.out.println("extractRaceData exception: " + strId);
                    }
                }
                
            }
        }
       return nCount; 
    }
    public static void updateRaceWinners(ENEStatement statement)
    {
        List<ParisTurfRace> lstRaces = ParisTurfFactory.selectParisTurfRaces(statement, "pt_winner is null or pt_winner=''");
        Iterator<ParisTurfRace> iter = lstRaces.iterator();
        while(iter.hasNext())
        {
            ParisTurfRace race = iter.next();
            String strWinner = extractWinner(race);
            race.setWinner(strWinner);
            ParisTurfFactory.updateParisTurfWinner(statement, race);
        }
    }
    public static void loadResults(ENEStatement statement, String strARDName) throws IOException, ParseException
    {
        loadResults(statement, strARDName, 1978, 1987);
    }
    public static void loadResults(ENEStatement statement, String strARDName, int nYear) throws IOException, ParseException
    {
        loadResults(statement, strARDName, nYear, nYear);
    }
    public static void loadResults(ENEStatement statement, String strARDName, int nStartYear, int nEndYear) throws IOException, ParseException
    {
       AdditionalRaceData racedata = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDName);
        List<ParisTurfRace> lstRaces = ParisTurfFactory.selectParisTurfRaces(statement, "pt_ard_name='" + strARDName + "' and year(pt_date) >=" + nStartYear + " and year(pt_date)<=" + nEndYear);
        Iterator<ParisTurfRace> iter = lstRaces.iterator();
        while(iter.hasNext())
        {
            ParisTurfRace race = iter.next();
            try
            {
                loadResults(statement, racedata, race);
            }
            catch(IOException e)
            {
                System.out.println("loadResults exception: " + e.getMessage());
            }
        }
    }
    public static void loadMissingResults(ENEStatement statement) throws IOException, ParseException
    {
        List<ParisTurfRace> lstRaces = ParisTurfFactory.selectParisTurfMissingRaces(statement);
        Iterator<ParisTurfRace> iter = lstRaces.iterator();
        String strPreviousARDName="";
        AdditionalRaceData racedata = null;
        while(iter.hasNext())
        {
            ParisTurfRace race = iter.next();
            try
            {
                String strARDName = race.getARDName();
                if (!strARDName.equals(strPreviousARDName))
                {
                    racedata = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDName);
                    strPreviousARDName = strARDName;
                }
                loadResults(statement, racedata, race);
            }
            catch(IOException e)
            {
                System.out.println("loadResults exception: " + e.getMessage());
            }
        }
    }
    private static void loadResults(ENEStatement statement, String strARDName, Date dtRace, String strRaceTitle, int nRaceID) throws IOException, ParseException
    {
        AdditionalRaceData racedata = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDName);
        String strCourse= racedata.getCourse();
        loadResults(statement, racedata, new ParisTurfRace(dtRace, strCourse, strRaceTitle, nRaceID, ""));
    }
    public static void loadResults(ENEStatement statement, AdditionalRaceData racedata, String strURL) throws IOException, ParseException
    {
        String[] aItems = strURL.split("/");
        String strDate=aItems[0];
        String strCourse = aItems[1].split("-")[1];
        String strTitle=aItems[3];
        int nIndex = strTitle.lastIndexOf("-");
        String strId = strTitle.substring(nIndex+1);
        strTitle=strTitle.substring(0, nIndex);
        SimpleDateFormat dtRace = new SimpleDateFormat("yyyy-MM-dd");
        loadResults(statement, racedata, new ParisTurfRace(dtRace.parse(strDate), strCourse, strTitle, Integer.valueOf(strId), null));
    }
    public static void loadResults(ENEStatement statement, AdditionalRaceData racedata, ParisTurfRace race) throws IOException, ParseException
    {
        // 20150908 format of PT web page changed slightly
        String strRaceDate = sm_fmtDate.format(race.getRaceDate());
        String strCourse = race.getCourse();
        String strRaceTitle = race.getTitle();
        int nRaceID = race.getId();
        String strURL  = sm_strRaceURL.replace("$YYYY-MM-DD$", strRaceDate).replace("$COURSE$", strCourse.toLowerCase()).replace("$RACE_TITLE$", strRaceTitle.toLowerCase().replace(" ", "-")).replace("$RACE_ID$", String.valueOf(nRaceID));
        System.out.println(strURL);
        TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");

        if (root == null)
            return;
        
        String strWinningTime="";
        String strGoing="";
        String strRaceValue="";
        int nGroup = 0;
        int nRunners = 0;
        int nMetres = 0;
        TagNode[] aResume = root.getElementsByAttValue("class", "row-fluid row-no-margin text-left", true, true);   // until 20181207: "first resume  alone"
        if (aResume.length > 0)
        {
            TagNode resume = aResume[0].getElementsByName("p", true)[0];    // until 20181207: "span"
            List lstResumeElements = resume.getAllChildren();
            for(int i = 0; i < lstResumeElements.size(); i++)
            {
                String strCurrent = ExecuteURL.getText((BaseToken)lstResumeElements.get(i));
                String[] astrItems = strCurrent.split("-");
                for(int j = 0; j < astrItems.length; j++)
                {
                    String strItem = astrItems[j].trim().toLowerCase();
                    if (strItem.indexOf("temps total") >= 0)
                    {
                        // winning time
                        strWinningTime = strItem.replace("temps total", "").replace("&#039;", "m ").replace("&quot;", ".").trim() + "s";
                    }
                    else if (strItem.indexOf("groupe") >= 0)
                    {
                        // group race
                        if (strItem.indexOf("iii") > 0)
                            nGroup = 3;
                        else if (strItem.indexOf("ii") > 0)
                            nGroup = 2;
                        else if (strItem.indexOf("i") > 0)
                            nGroup = 1;
                    }
                    else if (strItem.indexOf("listed") >= 0)
                    {
                        // group race
                        nGroup = 4;
                    }
                    else if (strItem.indexOf("£") >= 0)
                    {
                        // race value
                        strRaceValue = strItem;
                    }
                    else if (strItem.indexOf("terrain") >= 0)
                    {
                        // going
                        if (strItem.indexOf("lourd") >= 0)
                            strGoing = "Heavy";
                        else if (strItem.indexOf("assez souple") >= 0)
                            strGoing = "Good to Soft";
                        else if (strItem.indexOf("souple") >= 0 || strItem.indexOf("collant") >= 0)
                            strGoing = "Soft";
                        else if (strItem.indexOf("sec") >= 0)
                            strGoing = "Firm";
                        else if (strItem.indexOf("assez sec") >= 0 || strItem.indexOf("léger") >= 0)
                            strGoing = "Good to Firm";
                        else if (strItem.indexOf("bon") >= 0)
                            strGoing = "Good";
                        else
                            strGoing = strItem.replace("terrain", "");
                    }
                    else if (strItem.indexOf(" partants") >= 0)
                    {
                        // #Runners
                        String strNrRunners = strItem.replace(" partants", "").trim();
                        try
                        {
                            nRunners = Integer.parseInt(strNrRunners);
                        }
                        catch(NumberFormatException e)
                        {

                        }
                    }
                    else if (strItem.indexOf("mètres") >= 0)
                    {
                        // #Runners
                        String strNrMetres = strItem.replace("mètres", "").replace(".", "").trim();
                        try
                        {
                            nMetres = Integer.parseInt(strNrMetres);
                        }
                        catch(NumberFormatException e)
                        {

                        }
                    }
                }
            }
        }
            String strRaceType = "Flat";        // ParisTurf is mainly flat
            String strRaceName = "";
            if (racedata != null)   
            {
                strRaceType = racedata.getRaceType();
                strRaceName = racedata.getName();
            }

            AdditionalRace arace = new AdditionalRace("PT", nRaceID, strRaceName, strRaceTitle, strCourse, sm_dtRace.parse(strRaceDate), strRaceType, "", "", nRunners, strGoing);
            arace.setWinningTime(strWinningTime);
            arace.setGroupRace(nGroup);
            arace.setConditions(strRaceValue);
            arace.setDistanceMetres(nMetres);
            AdditionalRacesFactory.insertAdditionalRaceInstance(statement, strRaceName, arace, true);

            ArrayList<AdditionalRunner> alRunners = new ArrayList<AdditionalRunner>();
            TagNode[] aTables = root.getElementsByAttValue("id", "arrivees_" + String.valueOf(nRaceID), true, true);
            if (aTables.length > 0)
            {
                TagNode table = aTables[0];
                TagNode[] aRows = table.getElementsByName("tr", true);
                if (aRows.length > 1)
                {
                    TagNode headerRow = aRows[0];
                    HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(headerRow, "th");
                    for(int i = 1; i < aRows.length; i++)
                    {
                        TagNode row = aRows[i];
                        TagNode[] aCells = row.getElementsByName("td", true);
                        String strName=aCells[hmColumns.get("Cheval")].getText().toString();
                        strName = strName.replace("&#039;", "'").replace("è", "e").replace("é", "e").replace("ê", "e").replace("ë", "e").replace("ï", "i").replace("ç", "c");
                        int nIndex;
                        if ((nIndex = strName.indexOf(" USA")) > 0)
                            strName= strName.substring(0, nIndex);
                        else if ((nIndex = strName.indexOf(" IRE")) > 0)
                            strName= strName.substring(0, nIndex);
                        else if ((nIndex = strName.indexOf(" E1")) > 0)
                            strName= strName.substring(0, nIndex);
                        else if ((nIndex = strName.indexOf(" E2")) > 0)
                            strName= strName.substring(0, nIndex);
                        else if ((nIndex = strName.indexOf(" E3")) > 0)
                            strName= strName.substring(0, nIndex);
                        else if ((nIndex = strName.indexOf(" E4")) > 0)
                            strName= strName.substring(0, nIndex);
                        if (strName == null)
                        {
                            System.out.println("Row " + i + " Name is null");
                            continue;
                        }
                        String strOwner = "";
                        TagNode[] ownerNode  = aCells[1].getElementsByName("a", true);
                        if (ownerNode.length > 0)
                        {
                            strOwner = ownerNode[0].getAttributeByName("href").replace("/fiche-proprietaire/", "");
                            if (strOwner.indexOf("-") > 0)
                                strOwner = strOwner.substring(0, strOwner.lastIndexOf("-")).replaceAll("\\-", " ");
                        }
                        String strJockeyColours = "";
                        TagNode[] imageNode  = ownerNode[0].getElementsByName("img", true);
                        if (imageNode.length > 0)
                            strJockeyColours = imageNode[0].getAttributeByName("alt");
                        String strPosition=aCells[hmColumns.get("Cl.")].getText().toString(); // returns ordinal: 1er, 2e etc
                        String strClothNumber=aCells[hmColumns.get("N°")].getText().toString(); // cardinal - usually same as finish position!
                        String strWeight=aCells[hmColumns.get("Poids")].getText().toString(); // kg
                        String strTrainer=aCells[hmColumns.get("Entraîneur")].getText().toString(); 
                        String strSP="";
                        if (hmColumns.containsKey("Rapp.final PMU"))
                        {
                            int nCol = hmColumns.get("Rapp.final PMU");
                            if (aCells.length >= nCol)
                            {
                                CharSequence cs = aCells[nCol].getText();
                                if (cs != null)
                                    strSP=cs.toString(); // decimal, subtract 1 for SP
                            }
                        }
                        //String strDam=aCells[hmColumns.get("Mère")].getText().toString();  // e.g. Sweet Relations GB
                        //String strSire=aCells[hmColumns.get("Père")].getText().toString();  // e.g. Habitat USA or Mount Hagen
                        //String strDamSire=aCells[hmColumns.get("Père de la mère")].getText().toString();  // e.g. Habitat USA or Mount Hagen
                        String strDistanceBeaten=aCells[hmColumns.get("Ecart")].getText().toString();  
                        System.out.println("Distance beaten:" + strDistanceBeaten);
                        String strJockey=aCells[hmColumns.get("Jockey")].getText().toString();  
                        String strSexAge=aCells[hmColumns.get("S/A")].getText().toString();  
                        int nAge = StringUtils.getNumericEnd(strSexAge);
                        String strSex = strSexAge.replace(String.valueOf(nAge), "");
                        AdditionalRunner runner = new AdditionalRunner("PT", nRaceID, strName);
                        runner.setJockey(strJockey);
                        runner.setTrainer(strTrainer);
                        //runner.setDamName(strDam);
                        //runner.setSireName(strSire);
                        //runner.setDamSireName(strDamSire);
                        runner.setFinishPosition(strPosition);
                        runner.setOwner(strOwner);
                        runner.setDistanceBeaten(strDistanceBeaten);
                        runner.setAge(nAge);
                        runner.setGender(strSex);
                        runner.setToteStartingPrice(strSP);
                        runner.setWeightKilos(strWeight);
                        runner.setJockeyColours(strJockeyColours);
                        //System.out.println(runner.toString());
                        alRunners.add(runner);
                    }

                    AdditionalRacesFactory.insertAdditionalRunners(statement, alRunners, false);
                    for(int i = 0; i < alRunners.size(); i++)
                    {
                        AdditionalRunner runner = alRunners.get(i);
                        if (runner.getName() != null)
                            AdditionalRacesFactory.updateAdditionalRunnerResult(statement, runner);
                        else
                            System.out.println("Runner " + i + " Name is null");
                    } 
        
                    if (nRunners == 0)  // #runners not explicitly specified
                    {
                        arace.setNrRunners(alRunners.size());
                        AdditionalRacesFactory.insertAdditionalRaceInstance(statement, strRaceName, arace, true);
                    }
               }
            }
        else
        {
            System.out.println("Parse failed");
        }
        
    }
    public static String extractWinner(ParisTurfRace race)
    {
        String strWinner="";
        String strRaceDate = sm_fmtDate.format(race.getRaceDate());
        String strCourse = race.getCourse();
        String strRaceTitle = race.getTitle();
        int nRaceID = race.getId();
        String strURL  = sm_strRaceURL.replace("$YYYY-MM-DD$", strRaceDate).replace("$COURSE$", strCourse.toLowerCase()).replace("$RACE_TITLE$", strRaceTitle.toLowerCase().replace(" ", "-")).replace("$RACE_ID$", String.valueOf(nRaceID));
        try
        {
            TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");
            TagNode[] aResult = root.getElementsByAttValue("class", "race-result", true, true);
            if(aResult.length > 0)
            {
                TagNode table = aResult[0].getElementsByName("table", true)[0];
                TagNode tr = table.getElementsByName("tr", true)[0];
                TagNode td = tr.getElementsByAttValue("class", "nom", true, true)[0];
                TagNode a = td.getElementsByName("a", true)[0];
                strWinner = a.getText().toString().replace(" USA", "").replace(" UK", "").replace(" FR", "").replace(" GB", "").replace(" IRE", "").replace(" CAN", "");
            }
            else
            {
                System.out.println("No winner");
            }
        }
        catch(Exception e)
        {
            System.out.println("extractWinner: " + strRaceTitle + "-" + e.getMessage());
        }    
        return strWinner;
    }

}
