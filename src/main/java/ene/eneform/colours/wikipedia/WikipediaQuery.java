/*
 * Functions to retrieve data from Wikipedia via the generated HTML
 */
package ene.eneform.colours.wikipedia;

import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.AdditionalRaceWikipedia;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.bos.WinnerRaceQuery;
import ene.eneform.colours.database.AdditionalRaceDataFactory;
import ene.eneform.colours.database.AdditionalRaceLinkFactory;
import ene.eneform.colours.database.AdditionalRacesFactory;
import ene.eneform.colours.database.AdditionalRunnersFactory;
import ene.eneform.colours.web.rp.RacingPostCourse;
import ene.eneform.colours.web.rp.RacingPostHorseRaceSummary;
import ene.eneform.colours.web.rp.RacingPostRaceSummary;
import ene.eneform.smartform.bos.AdditionalRaceInstance;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import ene.eneform.utils.Pair;
import ene.eneform.utils.SetUtils;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class WikipediaQuery {
    private static String sm_strWikipediaBaseURL = "https://|LANGUAGE|.wikipedia.org/wiki/";
    private static String sm_strRaceURL = "https://$LANGUAGE$.wikipedia.org/wiki/";
    
    public static String getBaseURL()
    {
        return sm_strWikipediaBaseURL;
    }
    /*
    public static HashMap<Integer, Long> getWikipediaRacingPostReferences(String strLanguage, String strHRef) throws IOException
    {
        HashMap<Integer, Long> hmReferences = new HashMap<Integer, Long>();
        String strURL = sm_strRaceURL.replace("$LANGUAGE$", strLanguage) + strHRef;
        strURL = strURL.replace(" ", "_");
        try
        {
            TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");
            hmReferences = extractRacingPostReferences(root);
        }
         catch(Exception e)
         {
                System.out.println("Exception: getWikipediaWinners - " + strURL + "-" + e.getMessage());
         }
       return hmReferences;
    }
    public static HashMap<Integer, String> getWikipediaFullRacingPostReferences(String strLanguage, String strHRef) throws IOException
    {
        HashMap<Integer, String> hmReferences = new HashMap<Integer, String>();
        String strURL = sm_strRaceURL.replace("$LANGUAGE$", strLanguage) + strHRef;
        strURL = strURL.replace(" ", "_");
        try
        {
            TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");
            hmReferences = extractFullRacingPostReferences(root);
        }
         catch(Exception e)
         {
                System.out.println("Exception: getWikipediaWinners - " + strURL + "-" + e.getMessage());
         }
       return hmReferences;
    }*/
    public static HashMap<Integer, RacingPostRaceSummary> extractRacingPostSummaries(String strLanguage, String strHRef) throws IOException
    {
        HashMap<Integer, RacingPostRaceSummary> hmReferences = new HashMap<Integer, RacingPostRaceSummary>();
        String strURL = sm_strRaceURL.replace("$LANGUAGE$", strLanguage) + strHRef;
        strURL = strURL.replace(" ", "_");
        try
        {
            TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");
            hmReferences = extractRacingPostSummaries(root);
        }
         catch(Exception e)
         {
                System.out.println("Exception: getWikipediaWinners - " + strURL + "-" + e.getMessage());
         }
       return hmReferences;
    }
    public static HashMap<Integer, RacingPostRaceSummary> extractRacingPostSummaries(TagNode root)
    {
        HashMap<Integer, RacingPostRaceSummary> hmReferences = new HashMap<Integer, RacingPostRaceSummary>();
            TagNode[]  aRacingPost = root.getElementsByAttValue("title", "Racing Post", true, false);
            for(int k = 0; k < aRacingPost.length; k++)
            {
                if ("li".equalsIgnoreCase(aRacingPost[k].getParent().getName()))
                {
                    TagNode li = aRacingPost[k].getParent();
                    TagNode[] aUL = li.getElementsByName("ul", true);
                    if (aUL.length == 1)
                    {
                        TagNode ul = aUL[0];
                        TagNode[] aLI = ul.getElementsByName("li", true);
                        for(int i = 0; i < aLI.length; i++)
                        {
                            TagNode[] aA = aLI[i].getElementsByName("a", true);
                            for(int j = 0; j < aA.length; j++)
                            {
                                String strRPurl = aA[j].getAttributeByName("href");
                                System.out.println(strRPurl);
                                String strYear;
                                String strRaceId;
                                if (strRPurl.indexOf("racingpost") >= 0)
                                {
                                    try
                                    {
                                        if (strRPurl.indexOf("race_id") >= 0)   // old style
                                        {
                                            int nIndex1 = strRPurl.indexOf("race_id=") + 8;
                                            int nIndex2 = strRPurl.indexOf("&amp;");
                                            int nYearIndex1 = strRPurl.indexOf("r_date=") + 7;
                                            strRaceId = strRPurl.substring(nIndex1).substring(0, nIndex2 - nIndex1);
                                            strYear = strRPurl.substring(nYearIndex1).substring(0, 4);   //aA[j].getText().toString(); */
                                        }
                                        else
                                        {
                                            String[] aRPRefs = strRPurl.split("/");
                                            strRaceId = aRPRefs[7];
                                            String strDate = aRPRefs[6];
                                            String[] astrDate = strDate.split("-");
                                            strYear = astrDate[0];
                                        }
                                            long nRaceId = Long.valueOf(strRaceId);
                                        int nYear = Integer.valueOf(strYear);
                                        hmReferences.put(nYear, new RacingPostRaceSummary(strRPurl));
                                        //System.out.println("getWikipediaRacingPostReferences: " + nRaceId + "-" + nYear);
                                    }
                                    catch(NumberFormatException e)
                                    {
                                        System.out.println("getWikipediaRacingPostReferences: " + strRPurl + "-" + e.getMessage());
                                    }
                                    catch(Exception e)
                                    {
                                        System.out.println("getWikipediaRacingPostReferences: " + strRPurl + "-" + e.getMessage());
                                    }
                                 }
                            }
                        }
                    }
                }
            }
            return hmReferences;
        
    }
    /*
    public static HashMap<Integer, String> extractFullRacingPostReferences(TagNode root)
    {
        HashMap<Integer, String> hmReferences = new HashMap<Integer, String>();
            TagNode[]  aRacingPost = root.getElementsByAttValue("title", "Racing Post", true, false);
            for(int k = 0; k < aRacingPost.length; k++)
            {
                if ("li".equalsIgnoreCase(aRacingPost[k].getParent().getName()))
                {
                    TagNode li = aRacingPost[k].getParent();
                    TagNode[] aUL = li.getElementsByName("ul", true);
                    if (aUL.length == 1)
                    {
                        TagNode ul = aUL[0];
                        TagNode[] aLI = ul.getElementsByName("li", true);
                        for(int i = 0; i < aLI.length; i++)
                        {
                            TagNode[] aA = aLI[i].getElementsByName("a", true);
                            for(int j = 0; j < aA.length; j++)
                            {
                                String strRPurl = aA[j].getAttributeByName("href");
                                System.out.println(strRPurl);
                                String strYear;
                                String strRaceId;
                                if (strRPurl.indexOf("racingpost") >= 0)
                                {
                                    try
                                    {
                                        if (strRPurl.indexOf("race_id") >= 0)   // old style
                                        {
                                            int nIndex1 = strRPurl.indexOf("race_id=") + 8;
                                            int nIndex2 = strRPurl.indexOf("&amp;");
                                            int nYearIndex1 = strRPurl.indexOf("r_date=") + 7;
                                            strRaceId = strRPurl.substring(nIndex1).substring(0, nIndex2 - nIndex1);
                                            strYear = strRPurl.substring(nYearIndex1).substring(0, 4);   //aA[j].getText().toString(); 
                                        }
                                        else
                                        {
                                            String[] aRPRefs = strRPurl.split("/");
                                            strRaceId = aRPRefs[7];
                                            String strDate = aRPRefs[6];
                                            String[] astrDate = strDate.split("-");
                                            strYear = astrDate[0];
                                        }
                                            long nRaceId = Long.valueOf(strRaceId);
                                        int nYear = Integer.valueOf(strYear);
                                        hmReferences.put(nYear, strRPurl);
                                        //System.out.println("getWikipediaRacingPostReferences: " + nRaceId + "-" + nYear);
                                    }
                                    catch(NumberFormatException e)
                                    {
                                        System.out.println("getWikipediaRacingPostReferences: " + strRPurl + "-" + e.getMessage());
                                    }
                                    catch(Exception e)
                                    {
                                        System.out.println("getWikipediaRacingPostReferences: " + strRPurl + "-" + e.getMessage());
                                    }
                                 }
                            }
                        }
                    }
                }
            }
            return hmReferences;
    }
    public static HashMap<Integer, Long> extractRacingPostReferences(TagNode root)
    {
       HashMap<Integer, Long> hmReferences = new HashMap<Integer, Long>();
            TagNode[]  aRacingPost = root.getElementsByAttValue("title", "Racing Post", true, false);
            for(int k = 0; k < aRacingPost.length; k++)
            {
                if ("li".equalsIgnoreCase(aRacingPost[k].getParent().getName()))
                {
                    TagNode li = aRacingPost[k].getParent();
                    TagNode[] aUL = li.getElementsByName("ul", true);
                    if (aUL.length == 1)
                    {
                        TagNode ul = aUL[0];
                        TagNode[] aLI = ul.getElementsByName("li", true);
                        for(int i = 0; i < aLI.length; i++)
                        {
                            TagNode[] aA = aLI[i].getElementsByName("a", true);
                            for(int j = 0; j < aA.length; j++)
                            {
                                String strRPurl = aA[j].getAttributeByName("href");
                                //System.out.println(strRPurl);
                                String strYear;
                                String strRaceId;
                                if (strRPurl.indexOf("racingpost") >= 0)
                                {
                                    try
                                    {
                                        if (strRPurl.indexOf("race_id") >= 0)   // old style
                                        {
                                            int nIndex1 = strRPurl.indexOf("race_id=") + 8;
                                            int nIndex2 = strRPurl.indexOf("&amp;");
                                            int nYearIndex1 = strRPurl.indexOf("r_date=") + 7;
                                            strRaceId = strRPurl.substring(nIndex1).substring(0, nIndex2 - nIndex1);
                                            strYear = strRPurl.substring(nYearIndex1).substring(0, 4);   //aA[j].getText().toString(); 
                                        }
                                        else
                                        {
                                            String[] aRPRefs = strRPurl.split("/");
                                            strRaceId = aRPRefs[7];
                                            String strDate = aRPRefs[6];
                                            String[] astrDate = strDate.split("-");
                                            strYear = astrDate[0];
                                        }
                                            long nRaceId = Long.valueOf(strRaceId);
                                        int nYear = Integer.valueOf(strYear);
                                        hmReferences.put(nYear, nRaceId);
                                        //System.out.println("getWikipediaRacingPostReferences: " + nRaceId + "-" + nYear);
                                    }
                                    catch(NumberFormatException e)
                                    {
                                        System.out.println("getWikipediaRacingPostReferences: " + strRPurl + "-" + e.getMessage());
                                    }
                                    catch(Exception e)
                                    {
                                        System.out.println("getWikipediaRacingPostReferences: " + strRPurl + "-" + e.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return hmReferences;
      } */
   public static void loadJockeyColoursData(ENEStatement statement, String strLanguage) throws IOException
    {
        ArrayList<AdditionalRaceData> alRaces = AdditionalRaceDataFactory.createAdditionalRaceDataList(statement, "", "");
        Iterator<AdditionalRaceData> iter = alRaces.iterator();
        while(iter.hasNext())
        {
            AdditionalRaceData ard = iter.next();
            AdditionalRaceWikipedia wp = ard.getWikipedia(strLanguage);
            getJockeyColoursData(statement, strLanguage, wp);
        }
    }
   public static void getJockeyColoursData(ENEStatement statement, String strARDName, String strLanguage) throws IOException, ParseException
   {
       AdditionalRaceData racedata = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDName);
       if (racedata == null)
       {
           System.out.println("AdditionalRaceData  not found: " + strARDName);
           return;
       }
       getJockeyColoursData(statement, strLanguage, racedata.getWikipedia(strLanguage));
   }
    public static void getJockeyColoursData(ENEStatement statement,  String strLanguage, AdditionalRaceWikipedia wiki) throws IOException
    {
        String strHRef = wiki.getWikipediaRef();
        String strRaceName = wiki.getName();

        String strURL = sm_strRaceURL.replace("$LANGUAGE$", strLanguage) + strHRef;
        strURL = strURL.replace(" ", "_");
        try
        {
            TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");
            extractJockeyColoursData(statement, strRaceName, root);
        }
         catch(Exception e)
         {
                System.out.println("Exception: getJockeyColoursData - " + strURL + "-" + e.getMessage());
                e.printStackTrace();
         }
        
    }
    public static void extractJockeyColoursData(ENEStatement statement, String strRaceName, TagNode root)
    {
        // pull back data lost in hard disk crash!
        TagNode[]  aTables = root.getElementsByName("table", true);
        for(int i = 0; i < aTables.length; i++)
        {
            TagNode table = aTables[i];
            if (table.getElementsByAttValue("class", "jockey_colours_year_cell", true, false).length > 0)
            {
                // this is the table
                TagNode[]  aRows = table.getElementsByName("tr", true);
                int nRowIndex = 0;
                int nCurrentYear = 0;
                int nRaceId = 0;
                String[] aColours = new String[3];
                boolean bNewRace = false;
                for(int j = 0; j < aRows.length; j++)
                {
                    TagNode row = aRows[j];
                    TagNode[] aTD = row.getElementsByAttValue("class", "jockey_colours_year_cell", true, false);
                    if (aTD.length > 0)
                    {
                        String strYear = aTD[0].getText().toString();
                        nCurrentYear = Integer.valueOf(strYear);
                        nRowIndex = 0;
                        bNewRace = false;
                    }
                    
                    if (nCurrentYear >= 2003 && nCurrentYear <= 2008)
                    {
                        if (nRowIndex == 0)
                        {
                            AdditionalRaceInstance arl = AdditionalRaceLinkFactory.getYearAdditionalRaceLink(statement, strRaceName, nCurrentYear);
                            nRaceId = arl.getRaceId();
                            int nInsert = AdditionalRacesFactory.insertDailyRace(statement, nRaceId);
                            bNewRace = (nInsert > 0);
                            System.out.println(strRaceName + "-" + nCurrentYear + "-" + nRaceId);
                        }
                        else if (bNewRace && (nRowIndex == 1))
                        {
                            // td contains colours as title of a of class image
                            TagNode[] aImages = row.getElementsByAttValue("class", "image", true, false);
                            for(int l = 0; l < aImages.length; l++)
                            {
                                aColours[l] = aImages[l].getAttributeByName("title");
                                System.out.println("Colours: " + (l + 1) + "-" + aColours[l]);
                            }
                        }
                        else if (bNewRace && (nRowIndex == 2))
                        {
                            // td contains horse name as text
                            TagNode[]  aNames = row.getElementsByName("td", true);
                            for(int l = 0; l < aNames.length; l++)
                            {
                                String strHorse = aNames[l].getText().toString();
                                System.out.println("Horse: " + (l + 1) + "-" + strHorse);
                                String strColours = aColours[l];
                                if (strColours == null)
                                    strColours = "";
                                if ((strHorse != null) && (!"".equals(strHorse)))
                                    AdditionalRunnersFactory.updateOwnerColours(statement, "SF", null, strColours, strHorse, nRaceId);
                            }
                        }
                    }
                    nRowIndex++;
                }
           }
        }
    
    }
    public static HashMap<Integer, String> getWikipediaWinners(String strLanguage, AdditionalRaceData ard)
    {
        HashMap<Integer,String> hmWinners = WikipediaQuery.getWikipediaWinners(strLanguage, ard.getWikipedia(strLanguage).getWikipediaRef());
        return hmWinners;
    }    
    public static HashMap<Integer, String> getWikipediaWinners(String strLanguage, String strHRef)
    {
        HashMap<Integer, String> hmWinners = new HashMap<Integer, String>();
        String strURL = sm_strRaceURL.replace("$LANGUAGE$", strLanguage) + strHRef;
        strURL = strURL.replace(" ", "_");
        try
        {
            // UK and US use sortable table
            TagNode root = ExecuteURL.getRootNode(strURL, "utf-8");
            TagNode[] aSortable = root.getElementsByAttValue("class", "sortable", true, true);
            if (aSortable.length == 0)
            {
                aSortable = root.getElementsByAttValue("class", "wikitable sortable", true, true);
            }
            if (aSortable.length == 0)
            {
                aSortable = root.getElementsByAttValue("class", "sortable wikitable", true, true);
            }

            if (aSortable.length > 0)
            {
                for(int j = 0; j < aSortable.length; j++)
                {
                    TagNode sortable = aSortable[j];

                    TagNode[] rows = sortable.getElementsByName("tr", true);
                    for(int i = 1; i < rows.length; i++)    // Ignore headers
                    {
                        TagNode row = rows[i];
                        try
                        {
                            String strYear = row.getElementsByName("td", true)[0].getText().toString();
                            int nYear = Integer.parseInt(strYear);
                            String strWinner = "";
                            TagNode td = row.getElementsByName("td", true)[1];
                            TagNode[] aLinks = td.getElementsByName("a", true);
                            for (int k = 0; k <  aLinks.length; k++)
                            {
                                strWinner = aLinks[j].getAttributeByName("title");
                                if ((strWinner != null) && !"".equals(strWinner))
                                {
                                    strWinner = aLinks[j].getText().toString(); // retrieve the text (title sometimes has (horse) in brackets!
                                    break;
                                }
                            }
                            if ("".equals(strWinner))
                            {
                                ArrayList<String> alWinners = ExecuteURL.getNodeContentList(td);
                                strWinner = alWinners.get(alWinners.size() - 1);     // might be image
                                strWinner= strWinner.replace("é", "e").replace("è", "e");
                                if (strWinner.indexOf("*") >=0)
                                    strWinner= strWinner.substring(0, strWinner.indexOf("*")).trim();
                                if (strWinner.indexOf("[") >=0)
                                    strWinner= strWinner.substring(0, strWinner.indexOf("[")).trim();
                                if (strWinner.indexOf("(") >=0)
                                    strWinner= strWinner.substring(0, strWinner.indexOf("(")).trim();
                                if (strWinner.indexOf("†") >=0)
                                    strWinner= strWinner.substring(0, strWinner.indexOf("†")).trim();
                            }

                            hmWinners.put(nYear, strWinner);
                        }
                        catch(NumberFormatException e)
                        {
                            System.out.println("NumberFormatException: getWikipediaWinners row " + i + "-" + strURL + "-" + e.getMessage());
                        }
                       catch(Exception e)
                        {
                            System.out.println("Exception: getWikipediaWinners row " + i + "-" + strURL + "-" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            else
            {
                // Australia uses ul inside div 
                TagNode[] aDiv = root.getElementsByAttValue("class", "refbegin columns references-column-width", true, true);
                if (aDiv.length == 0)
                    aDiv = root.getElementsByAttValue("class", "refbegin references-column-width", true, true);
                if (aDiv.length >= 1)
                {
                    TagNode[] rows = aDiv[0].getElementsByName("li", true);
                    for(int i = 0; i < rows.length; i++)  
                    {
                        TagNode row = rows[i];
                        try
                        {
                            // want to extract year and winner name
                            ArrayList<String>  alItems = ExecuteURL.getNodeContentList(row);
                            String strYearWinner = alItems.get(0);
                            String strYear = strYearWinner.substring(0, 4);
                            int nYear = Integer.parseInt(strYear);
                            String strWinner = "";
                            if (strYearWinner.length() > 7)
                                strWinner = strYearWinner.substring(7).replace("†", "");
                            if ("".equals(strWinner))
                            {
                                TagNode[] aLinks = row.getElementsByName("a", true);
                                for (int j = 0; j <  aLinks.length; j++)
                                {
                                    strWinner = aLinks[j].getAttributeByName("title");
                                    if ((strWinner != null) && !"".equals(strWinner))
                                    {
                                        strWinner = aLinks[j].getText().toString(); // retrieve the text (title sometimes has (horse) in brackets!
                                        break;
                                    }
                                }
                                if (((strWinner == null) || ("".equals(strWinner))) && (alItems.size() > 1))
                                    strWinner = alItems.get(1);
                            }
/*                            ArrayList<String> alWinners = ExecuteURL.getNodeContentList(row.getElementsByName("td", true)[1]);
                            String strWinner = alWinners.get(alWinners.size() - 1);     // might be image
                            strWinner= strWinner.replace("é", "e").replace("è", "e");
                            if (strWinner.indexOf("*") >=0)
                                strWinner= strWinner.substring(0, strWinner.indexOf("*")).trim();
                            if (strWinner.indexOf("[") >=0)
                                strWinner= strWinner.substring(0, strWinner.indexOf("[")).trim();
                            if (strWinner.indexOf("(") >=0)
                                strWinner= strWinner.substring(0, strWinner.indexOf("(")).trim();
                            if (strWinner.indexOf("†") >=0)
                                strWinner= strWinner.substring(0, strWinner.indexOf("†")).trim();
*/                  
                            if (!"".equals(strWinner))
                            {
                                System.out.println("Year " + nYear + " Winner: " + strWinner);
                                hmWinners.put(nYear, strWinner);
                            }
                            else
                            {
                                System.out.println("Failed Winner: " + strYearWinner);
                            }
                        }
                        catch(NumberFormatException e)
                        {
                            System.out.println("NumberFormatException: getWikipediaWinners row " + i + "-" + strURL + "-" + e.getMessage());
                        }
                       catch(Exception e)
                        {
                            System.out.println("Exception: getWikipediaWinners row " + i + "-" + strURL + "-" + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                }
            }
         }
         catch(Exception e)
         {
                System.out.println("Exception: getWikipediaWinners - " + strURL + "-" + e.getMessage());
         }
        return hmWinners;
    }

    public static void loadWikipediaRacePages(ENEStatement statement, String strLanguage, String strWhere, String strOrder) throws IOException {
        ArrayList<AdditionalRaceData> alRaces = AdditionalRaceDataFactory.createAdditionalRaceDataList(statement, strWhere, strOrder);
        Iterator<AdditionalRaceData> iter = alRaces.iterator();
        int nCount = 0;
        while (iter.hasNext()) 
        {
            AdditionalRaceData ard = iter.next();
            System.out.println(++nCount + "-" + ard.getName());
            AdditionalRaceWikipedia wp = ard.getWikipedia(strLanguage);
            String strWikipediaURL = wp.getWikipediaRef().replace(" ", "_");
            if ((strWikipediaURL != null) && !"".equals(strWikipediaURL)) 
            {
                System.out.println(strWikipediaURL);
                TagNode rootNode = ExecuteURL.getRootNode(sm_strWikipediaBaseURL.replace("|LANGUAGE|", strLanguage) + strWikipediaURL, "utf-8");
                if (rootNode != null) 
                {
                    // JockeyColours entries
                    TagNode[] aCellTitles = rootNode.getElementsByAttValue("class", "jockey_colours_year_cell", true, true);
                    int nMax = 0;
                    int nCurrent = 0;
                    if (aCellTitles.length > 0) 
                    {
                        // Years are in descending order
                       for (int i = 0; i < aCellTitles.length; i++) 
                       {
                           try 
                           {
                               nCurrent = Integer.valueOf(aCellTitles[i].getText().toString().replace("\n", ""));
                               if (nMax == 0) 
                               {
                                   nMax = nCurrent;
                               }
                           } 
                           catch (NumberFormatException e) 
                           {
                               //ignore non-years , e.g. 2000 (November)
                           }
                       }
                    }
                    System.out.println("Jockey Colours table: " + ard.getName() + " Max: " + nMax + " Min: " + nCurrent);
                    // Winners Table
                    int nStartYear = 0;
                    int nYear = 0;
                    TagNode[] aWinnersTable = rootNode.getElementsByAttValue("class", "sortable", true, true);
                    if (aWinnersTable.length == 0)
                        aWinnersTable = rootNode.getElementsByAttValue("class", "wikitable sortable", true, true);
                    if (aWinnersTable.length == 0)
                        aWinnersTable = rootNode.getElementsByAttValue("class", "sortable wikitable", true, true);
                    if (aWinnersTable.length == 1) 
                    {
                        TagNode winnersTable = aWinnersTable[0];
                        TagNode[] aWinnersRows = winnersTable.getElementsByName("tr", true);
                        for (int i = 1; i < aWinnersRows.length; i++) // ignore first row, for headers
                        {
                            TagNode[] aWinnersCells = aWinnersRows[i].getElementsByName("td", true);
                            if (aWinnersCells.length >= 4) {
                                try 
                                {
                                    String strYear = "";
                                    ArrayList<String> alContent = ExecuteURL.getNodeContentList(aWinnersCells[0]);
                                    if (alContent.size() > 0)
                                    {
                                        strYear = alContent.get(alContent.size() - 1).split("\\,")[0].split("\\(")[0].trim();
                                        nYear = Integer.parseInt(strYear);
                                    }
                                } 
                                catch (NumberFormatException e) 
                                {
                                }
                                if (nStartYear == 0) {
                                    nStartYear = nYear;
                                }
                            }
                        }
                    }
                    int nEndYear = nYear;
                    System.out.println("Winners table: " + ard.getName() + " Max: " + nEndYear + " Min: " + nStartYear);
                    // Racing Post References
                    HashMap<Integer, RacingPostRaceSummary> hmReferences = extractRacingPostSummaries(rootNode);
                    Pair<Integer, Integer> pair = SetUtils.getMinMax(hmReferences.keySet());
                    System.out.println("Racing Post References: " + ard.getName() + " Min: " + pair.getElement0() + " Max: " + pair.getElement1());
                    AdditionalRaceDataFactory.updateAdditionalRaceDataWikipediaYears(statement, ard.getName(), strLanguage, nCurrent, nMax, nStartYear, nEndYear, pair.getElement0(), pair.getElement1());
                }
                else
                {
                    System.out.println("Null Wikipedia URL: " + strWikipediaURL);
                }
           }
            else
            {
                System.out.println("Empty Wikipedia URL: " + ard.getName());
            }
        }
    }

    public static void loadWikipediaGaloppSiegerReferences(ENEStatement statement, String strLanguage, String strWhere, String strOrder) throws IOException {
        ArrayList<AdditionalRaceData> alRaces = AdditionalRaceDataFactory.createAdditionalRaceDataList(statement, strWhere, strOrder);
        Iterator<AdditionalRaceData> iter = alRaces.iterator();
        while (iter.hasNext()) {
            AdditionalRaceData ard = iter.next();
            AdditionalRaceWikipedia wp = ard.getWikipedia(strLanguage);
            String strWikipediaURL = wp.getWikipediaRef().replace(" ", "_");
            String strGSRef = ard.getGSref();
            if ((strGSRef == null) || "".equals(strGSRef)) {
                if ((strWikipediaURL != null) && !"".equals(strWikipediaURL)) {
                    TagNode rootNode = ExecuteURL.getRootNode(WikipediaQuery.getBaseURL().replace("|LANGUAGE|", strLanguage) + strWikipediaURL, "utf-8");
                    if (rootNode != null) {
                        TagNode[] aListItems = rootNode.getElementsByName("a", true);
                        for (int i = 0; i < aListItems.length; i++) {
                            String strHRef = aListItems[i].getAttributeByName("href");
                            if ((strHRef != null) && strHRef.indexOf("http://www.galopp-sieger.de") >= 0) {
                                int nStart = strHRef.indexOf("rennkz=") + 7;
                                strHRef = strHRef.substring(nStart);
                                int nEnd = 0;
                                nEnd = strHRef.indexOf("&");
                                if (nEnd > 0) {
                                    strHRef = strHRef.substring(0, nEnd);
                                }
                                System.out.println("GaloppSiegerReference: " + ard.getName() + "-" + strHRef);
                                AdditionalRaceDataFactory.updateAdditionalRaceDataGSRef(statement, ard.getName(), strHRef);
                                int nKey = ard.getSEKey();
                                if (nKey <= 0) {
                                    AdditionalRaceDataFactory.updateAdditionalRaceDataSEKey(statement, ard.getName());
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
  public static RacingPostRaceSummary getRacingPostReferenceWinner(ENEStatement statement, AdditionalRaceData ard, int nYear, HashMap<Integer,RacingPostRaceSummary> hmWinners) throws IOException, ParseException, InterruptedException
   {
       // hmWinners is the product of a database query - have all info except race id
        RacingPostRaceSummary summary = hmWinners.get(nYear);
        if (summary != null)
        {
             if(summary.getRaceID() == 0)
             {
                 RacingPostHorseRaceSummary hrs = (RacingPostHorseRaceSummary) summary;
                 // strWinner = winner|date
                 String strDate = summary.getRPDate();
                 String[] astrDate = strDate.split("-");
                 String strWinner = ((RacingPostHorseRaceSummary) summary).getHorse();
                 summary = RacingPostRaceSummary.createSummary(new WinnerRaceQuery(summary.getCourse().getCode(), strWinner, nYear, Integer.parseInt(astrDate[1]), Integer.parseInt(astrDate[2]), ard.getKeywords()));
             }
        }
        else
            System.out.println("Missing year: " + nYear);

        return summary;
   } 
    public static RacingPostRaceSummary getRacingPostReferenceWinner(AdditionalRaceData ard, int nYear, String strDate, HashMap<Integer, String> hmWinners, RacingPostCourse course)
    { 
        // Two chances of retrieveing from Wikipedia - get from Racing Post References OR via Winners table
        String strCourse = ENEColoursDBEnvironment.getInstance().getRPCourseByName(ard.getCourse(), ard.getRaceType()).getCode();
        if (course != null) 
        {
            strCourse += ("|" + course.getCode());
        }
        RacingPostRaceSummary summary = null;
        String strWinner = hmWinners.get(nYear);
        if (strWinner != null) 
        {
            strWinner = strWinner.replace("\u00f6", "o").replace("\u00e9", "e").replace("\u00e8", "e").replace("\u00ea", "e").replace("\u00e0", "a").replace("\u00e7", "c").replace("\u00fc", "u");
            try
            {
                if (strDate != null) 
                {
                    String[] astrDate = strDate.split("\\-");
                    summary = RacingPostRaceSummary.createSummary(new WinnerRaceQuery(strCourse, strWinner, nYear, Integer.parseInt(astrDate[1]), Integer.parseInt(astrDate[2]), ard.getKeywords()));
                }
                else
                {
                   summary = RacingPostRaceSummary.createSummary(new WinnerRaceQuery(strCourse, strWinner, nYear, 0, 0, ard.getKeywords()));
                }
            }
             catch(IOException e)
             {
                 System.out.println("getRacingPostReferenceDatabase: " + e.getMessage());
             }
             catch(InterruptedException e)
             {
                 System.out.println("getRacingPostReferenceDatabase: " + e.getMessage());
             }
        }
        
        return summary;
    }
    public static ArrayList<RacingPostRaceSummary> getRacingPostReferences(AdditionalRaceData ard, String strLanguage, int nStartYear, int nEndYear) 
    {   
        ArrayList<RacingPostRaceSummary> alSummaries = new ArrayList<RacingPostRaceSummary>();
        try
        {
            // Two chances of retrieving from Wikipedia - get from Racing Post References OR via Winners table
            HashMap<Integer, RacingPostRaceSummary> hmFullReferences = WikipediaQuery.extractRacingPostSummaries(strLanguage, ard.getWikipedia(strLanguage).getWikipediaRef());
            Iterator<Integer> iter = hmFullReferences.keySet().iterator();
            while(iter.hasNext())
            {
                int nYear = iter.next();
                if ((nYear >= nStartYear) && (nYear <= nEndYear))
                    alSummaries.add(hmFullReferences.get(nYear));
            }
         }
        catch(IOException e)
        {
            System.out.println("getRacingPostReferenceWinner: " + e.getMessage());
        }
        return alSummaries;
    }
    public static RacingPostRaceSummary getRacingPostReference(AdditionalRaceData ard, int nYear) 
    {   
        RacingPostRaceSummary summary = null;
        try
        {
            // Two chances odf retrieveing from Wikipedia - get from Racing Post References OR via Winners table
            HashMap<Integer, RacingPostRaceSummary> hmFullReferences = WikipediaQuery.extractRacingPostSummaries("en", ard.getWikipedia("en").getWikipediaRef());
            summary = hmFullReferences.get(nYear);
            
         }
        catch(IOException e)
        {
            System.out.println("getRacingPostReferenceWinner: " + e.getMessage());
        }
        return summary;
    }
    /*
    static RacingPostRaceSummary getRacingPostReference(ENEStatement statementX, AdditionalRaceData ard, int nYear, String strDate, HashMap<Integer, String> hmWinners, RacingPostCourse course) {
        if (course == null) {
            course = ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(ard.getCourse(), ard.getRaceType());
        }
        RacingPostRaceSummary summary = null;
        try {
            if (strDate == null) {
                // retrieve RP Reference from Wikipedia page
                if (summary != null) {
                    return summary;
                }
            }
            // retrieve data from database
            if (summary == null) {
                // get winner data from Wikipedia
                String strWinner = hmWinners.get(nYear);
                if (strWinner != null) {
                    strWinner = strWinner.replace("\u00f6", "o").replace("\u00e9", "e").replace("\u00e8", "e").replace("\u00ea", "e").replace("\u00e0", "a").replace("\u00e7", "c").replace("\u00fc", "u");
                }
                // get Racing Post Reference data from Wikipedia (race id only)
                //HashMap<Integer, Long> hmReferences = WikipediaQuery.getWikipediaRacingPostReferences("en", ard.getWikipedia("en").getWikipediaRef());
                // long nRaceId = hmReferences.get(nYear);
                // get Full Racing Post Reference data from Wikipedia
                if (strDate != null) {
                    String[] astrDate = strDate.split("\\-");
                    String strCourse = course.getCode(); // don't just course in reference?
                    summary = RacingPostRaceSummary.createSummary(new WinnerRaceQuery(strCourse, strWinner, nYear, Integer.parseInt(astrDate[1]), Integer.parseInt(astrDate[2]), ard.getKeywords()));
                }
            }
         } catch (Exception e) {
            System.out.println("getRacingPostReference Exception: " + e.getMessage());
            e.printStackTrace();
        }
        return summary;
    } */
}
