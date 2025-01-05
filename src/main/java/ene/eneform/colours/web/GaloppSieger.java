/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web;

import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.database.AdditionalRacesFactory;
import ene.eneform.colours.database.AdditionalRunnersFactory;
import ene.eneform.smartform.bos.AdditionalRace;
import ene.eneform.smartform.bos.AdditionalRunner;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Simon
 */
public class GaloppSieger {
    
    private static String sm_GaloppSiegerURL= "http://www.galopp-sieger.de/galoppsieger/en/sieger?rennkz=";   // Imperial Stakes
    // FAT        // Arc de Triomphe
    // IEO   // Irish Oaks
 private static SimpleDateFormat sm_dtFormatter1 = new SimpleDateFormat("dd/MM/yyyy"); 
 private static SimpleDateFormat sm_dtFormatter2 =  new SimpleDateFormat("dd MMM yyyy", Locale.GERMANY); 
   
    private static String[] sm_astrGermanMonths = {"Jan", "Feb", "Mrz", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"};
    
    public static void loadGSResults(ENEStatement statement, String strARDRaceName, int nStartYear, int nEndYear)
    {
        // retreive from ARD
        AdditionalRaceData ard = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strARDRaceName);
        String strCode=ard.getGSref();
        String strRaceType=ard.getRaceType();
        int nRaceKey = ard.getSEKey();
        if ("".equals(strCode) || (nRaceKey == 0))
        {
            System.out.println("Invalid Galopp-Sieger query: " + strARDRaceName + "-" + strCode + "-" + nRaceKey);
            return;
        }
        String strRaceURL = sm_GaloppSiegerURL + strCode;
        try
        {
            System.out.println(strRaceURL);
            TagNode rootNode = ExecuteURL.getRootNode(strRaceURL, "iso-8859-1");
        
            TagNode[] aTables = rootNode.getElementsByName("table", true);
            for(int i = 0; i < aTables.length; i++)
            {
                TagNode table = aTables[i];
                if ("sieger".equals(table.getAttributeByName("class")))
                {
                    parseGSResults(statement, table, ard, nRaceKey, strRaceType, nStartYear, nEndYear);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("loadGSResults: " + e.getMessage());
            e.printStackTrace();
        }
    }
  private static void parseGSResults(ENEStatement statement, TagNode results, AdditionalRaceData ard, int nRaceKey, String strRaceType, int nStartYear, int nEndYear)
    {
        // race attributes
         int nRace = 0;
         String strYear = "";
         int nYear = 0;
         String strMonth = "";
         String strGoing = "";
         String strRaceTitle = "";
         String strCourse = "";
         String strAgeRange = "";
         String strClass = "";
         String[] astrDistanceBeaten = {};
         ArrayList<AdditionalRunner> alRunners = new ArrayList<AdditionalRunner>();
        TagNode[] aRows = results.getElementsByName("tr", true);
        if (aRows.length > 1)
        {
            TagNode rowHeader = aRows[0];
            HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(rowHeader, "th");
            int nRaceRow = 0;
            for(int i = 1; i < aRows.length; i = i + 1)
            {
                // race covers 4 rows
                // 1st (rowspan=4) is a header
                // 2, 3, 4 are the placed horses (colspan=2)
                TagNode row = aRows[i];
                boolean bDeadHeat = false;
                TagNode[] aCells = row.getElementsByName("td", true);
                if (aCells.length > 0)
                {
                    TagNode cell0 = aCells[0];
                    String strCellClass = cell0.getAttributeByName("class");
                    if ("sieger_tot".equals(strCellClass))
                    {
                        System.out.println("Dead heat: " + strYear + " - " + nRaceRow);
                        bDeadHeat = true;
                    }
                    String strColSpan = cell0.getAttributeByName("colspan");
                    String strRowSpan = cell0.getAttributeByName("rowspan");
                    if ("4".equals(strRowSpan))
                    {
                        nRaceRow = 0;
                        // this is the first row of a race, so should write the runners from the previous race
                        if (((nStartYear == 0) || (nYear >= nStartYear))
                                && ((nEndYear == 0) || (nYear <= nEndYear)))
                        {
                            if (alRunners.size() > 0)
                            {
                                AdditionalRacesFactory.insertAdditionalRunners(statement, alRunners);
                                for(int k = 0; k < alRunners.size(); k++)
                                {
                                    System.out.println(alRunners.get(k).toString());
                                }
                            }
                        }
                        alRunners.clear();
                        List<String> lstTitleElements = ExecuteURL.getNodeContentList(cell0);
                        if (lstTitleElements.size() >= 2)
                        {
                            strYear = lstTitleElements.get(0);
                            nYear = Integer.parseInt(strYear);
                            nRace = (nRaceKey * 10000) + nYear;
                            strMonth = lstTitleElements.get(1);
                        }
                         
                        strRaceTitle = ExecuteURL.getFirstNodeContent(aCells[1]);
                        int nSlash = strRaceTitle.indexOf("/");
                        if (nSlash > 0)
                        {
                            strCourse = strRaceTitle.substring(nSlash + 1).trim();
                            strRaceTitle = strRaceTitle.substring(0, nSlash - 1).trim();
                        }
                        else
                        {
                            strRaceTitle = ard.getName();
                            strCourse = ard.getCourse();
                        }
                        strGoing = "";
                        String strDistancesBeaten = ExecuteURL.getSearchNodeContent(aCells[1], "Richterspruch:");
                        System.out.println(strDistancesBeaten);
                        int nGoing = strDistancesBeaten.indexOf("Boden");
                        if ((nGoing > 0) && ((nGoing + 6) < strDistancesBeaten.length()))
                        {
                            strGoing = strDistancesBeaten.substring(nGoing + 6).trim();
                            strDistancesBeaten = strDistancesBeaten.substring(0, nGoing - 1).trim();
                        }
                        strGoing = strGoing.replace(".", "");
                        if ("unbek".equalsIgnoreCase(strGoing) || "unebk".equalsIgnoreCase(strGoing))
                            strGoing = "";
                        else
                            strGoing = translateGoing(strGoing);
                        if (strDistancesBeaten.endsWith(";"))
                            strDistancesBeaten=strDistancesBeaten.substring(0, strDistancesBeaten.length() - 1);
                        astrDistanceBeaten = strDistancesBeaten.split("-");
                     }
                    else if ("2".equals(strColSpan))
                    {
                        if (!bDeadHeat)
                            nRaceRow++;
                        String strName = ExecuteURL.getFirstNodeContent(aCells[2]);
                        if (!"".equals(strName))
                        {
                            String strOwner = ExecuteURL.getSubsequentNodeContent(aCells[0], "own:");
                            if ("".equals(strOwner))
                                strOwner = ExecuteURL.getSubsequentNodeContent(aCells[0], "Bes:");
                            String strTrainer = ExecuteURL.getFirstNodeContent(aCells[7]);
                            String strJockey = ExecuteURL.getFirstNodeContent(aCells[8]);
                             AdditionalRunner runner = new AdditionalRunner("SE", nRace, strName);
                            runner.setOwner(strOwner);
                            runner.setTrainer(strTrainer);
                            runner.setJockey(strJockey);

                            String strAge = ExecuteURL.getFirstNodeContent(aCells[3]);
                            if (!"".equals(strAge))
                            {
                                try
                                {
                                    runner.setAge(Integer.parseInt(strAge));
                                }
                                catch(Exception e)
                                {
                                    // carry on
                                }
                            }
                            String strGenderGerman = ExecuteURL.getFirstNodeContent(aCells[4]);   // in German S=Filly H=Colt
                            String strGender = "C";
                            if ("S".equals(strGenderGerman))
                                strGender = "F";
                            runner.setGender(strGender);

                            runner.setFinishPosition(String.valueOf(nRaceRow));
                            if ((nRaceRow > 1) && (astrDistanceBeaten.length >= nRaceRow))
                                runner.setDistanceBeaten(astrDistanceBeaten[nRaceRow - 2]);
                            alRunners.add(runner);
                        
                            if (bDeadHeat)
                                nRaceRow++;
                        }
                        if (nRaceRow == 1)
                        {
                            
                             String strNrRunners = ExecuteURL.getSearchNodeContent(aCells[10], "starter:");
                             String strStatus = ExecuteURL.getSearchNodeContent(aCells[10], "status:");
                             int nGroupRace = 0;
                             if (strStatus.indexOf("GR ") == 0)
                             {
                                 nGroupRace = Integer.parseInt(strStatus.substring(3));
                             }
                             String strType = ExecuteURL.getSearchNodeContent(aCells[10], "type:");
                             if ("".equals(strType))
                                strType = ExecuteURL.getSearchNodeContent(aCells[10], "Art:");
                             String strTime = ExecuteURL.getSearchNodeContent(aCells[12], "time:");
                             if ("".equals(strTime))
                                strTime = ExecuteURL.getSearchNodeContent(aCells[12], "Zeit:");
                             String strDistance = ExecuteURL.getSearchNodeContent(aCells[12], "dist:");
                            // Also:
                            // 10 Track:
                            // prz: win: win %: tote:
                            // 12 RRNo: pace:
                             Date parsedDate = null;
                             if (!"".equals(strMonth))
                             {
                                try
                                {

                         /*        parsedDate = sm_dtFormatter1.parse("01/" + getMonthNumber(strMonth) + "/" + strYear);      */
                                   parsedDate = sm_dtFormatter2.parse("01 " + strMonth + " " + strYear);       
                                }
                                catch(ParseException e)
                                {
                                    System.out.println(e.getMessage());
                                }
                             }
                             else
                             {
                                 int nMonth = ard.getMonth() - 1;   // index from 0
                                 parsedDate = new Date(nYear - 1900, nMonth, 1);
                             }
                            AdditionalRace arace = new AdditionalRace("SE", nRace, 
                                 ard.getName(), strRaceTitle, strCourse, parsedDate,
                                 strRaceType, strAgeRange, 
                                 strClass, 
                                 ("".equals(strNrRunners)) ? 0 : Integer.parseInt(strNrRunners), 
                                 strGoing);
                            if (!"".equals(strDistance))
                            {
                                try
                                {
                                    arace.setDistanceMetres(Integer.parseInt(strDistance));
                                }
                                catch(NumberFormatException e)
                                {
                                    
                                }
                            }
                             arace.setWinningTime(strTime);
                             if (nGroupRace > 0)
                                arace.setGroupRace(nGroupRace);
                             if (((nStartYear == 0) || (nYear >= nStartYear))
                                     && ((nEndYear == 0) || (nYear <= nEndYear)))
                             {
                                AdditionalRacesFactory.insertAdditionalRaceInstance(statement, ard.getName(), arace, true);
                                AdditionalRunnersFactory.updateDistanceWon(statement, nRace);
                                System.out.println(arace.toString());
                             }
                       }
                    }
                    else
                    {
                        // this is an additional runner with finish position specified in col 1
                        String strName = ExecuteURL.getFirstNodeContent(aCells[4]).replace("&#039;", "'").replace("è", "e").replace("é", "e").replace("ê", "e").replace("ë", "e").replace("ï", "i").replace("ç", "c");
                        String strFinishPosition = ExecuteURL.getFirstNodeContent(aCells[1]).replace(".", "");
                        String strOwner = ExecuteURL.getSubsequentNodeContent(aCells[2], "own:");
                        if ("".equals(strOwner))
                            strOwner = ExecuteURL.getSubsequentNodeContent(aCells[2], "Bes:");
                        String strTrainer = ExecuteURL.getFirstNodeContent(aCells[9]);
                        String strJockey = ExecuteURL.getFirstNodeContent(aCells[10]);
                        AdditionalRunner runner = new AdditionalRunner("SE", nRace, strName);
                        runner.setOwner(strOwner);
                        runner.setTrainer(strTrainer);
                        runner.setJockey(strJockey);

                        String strAge = ExecuteURL.getFirstNodeContent(aCells[5]);
                        if (!"".equals(strAge))
                            runner.setAge(Integer.parseInt(strAge));
                        String strGenderGerman = ExecuteURL.getFirstNodeContent(aCells[6]);   // in German S=Filly H=Colt
                        String strGender = "C";
                        if ("S".equals(strGenderGerman))
                            strGender = "F";
                        runner.setGender(strGender);

                        runner.setFinishPosition(strFinishPosition);
                        int nFinishPosition = Integer.parseInt(strFinishPosition);
                        if (astrDistanceBeaten.length >= nFinishPosition)
                            runner.setDistanceBeaten(astrDistanceBeaten[nFinishPosition - 2]);
                        alRunners.add(runner);
                    }
                }
             }
        }
        if (alRunners.size() > 0)
        {
            if (((nStartYear == 0) || (nYear >= nStartYear))
                    && ((nEndYear == 0) || (nYear <= nEndYear)))
            {
                AdditionalRacesFactory.insertAdditionalRunners(statement, alRunners);
                for(int k = 0; k < alRunners.size(); k++)
                {
                    System.out.println(alRunners.get(k).toString());
                }
            }
            alRunners.clear();
        }
    }
  private static int getMonthNumber(String strMonth)
  {
      for(int i = 0; i < sm_astrGermanMonths.length; i++)
      {
          if (sm_astrGermanMonths[i].equalsIgnoreCase(strMonth))
              return i + 1;
      }
      return 0;
  }
  private static String translateGoing(String strGermanGoing)
  {
      String strGoing = strGermanGoing;
      if ("gut bis weich".equals(strGoing))
          strGoing = "Good to Soft";
      else if ("klebrig".equals(strGoing) || "gut bis klebrig".equals(strGoing) || "gut bis fest".equals(strGoing))
          strGoing = "Good to Firm";
      else if ("sehr schwer".equals(strGoing))
          strGoing = "Heavy";
      else if ("weich".equals(strGoing) || "sehr weich".equals(strGoing) || "schwer".equals(strGoing))
          strGoing = "Soft";
      else if ("sehr weich".equals(strGoing))
          strGoing = "Very Soft";
      else if ("gut".equals(strGoing))
          strGoing = "Good";
      else if ("fest".equals(strGoing))
          strGoing = "Firm";
      else if ("hart".equals(strGoing))
          strGoing = "Hard";
      else if (!"".equals(strGoing))
      {
          System.out.println("Unknown going: " + strGermanGoing);
      }
      return strGoing;
  }

}
