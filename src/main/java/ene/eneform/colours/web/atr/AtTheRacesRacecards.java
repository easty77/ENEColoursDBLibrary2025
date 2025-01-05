/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.atr;

import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.database.AdditionalRunnersFactory;
import ene.eneform.colours.web.rp.RacingPostCourse;
import ene.eneform.smartform.bos.SmartformColoursRunner;
import ene.eneform.smartform.bos.SmartformDailyRace;
import ene.eneform.smartform.bos.SmartformRunnerContainer;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

/**
 *
 * @author simon
 */
public class AtTheRacesRacecards {
    
    // http://www.attheraces.com/racecard/Chantilly/02-October-2016/1635
    static String strATRRacecardURL = "https://www.attheraces.com/racecard/";
    
    public static int updateSmartformColours(ENEStatement statement, SmartformDailyRace race)
    {
        String strCourse = race.getCourse();
        if ("Royal_Ascot".equals(strCourse))
            strCourse="Ascot";
        RacingPostCourse course = ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(strCourse, race.getRaceType());
        AtTheRacesSlot slot = AtTheRacesFactory.getRaceSlot(statement, race.getRaceId());
        if (slot!= null && course != null)
            return loadRacecard(statement, course.getATRName(), course.getCountry(), slot.getDate(), slot.getScheduledTime(), race.getRaceId(), "SF", race, true);
        else
        {
            System.out.println("updateSmartformColours null slot: " + strCourse + "-" + race.getRaceId());
            return -1;
        }
    }
    public static int loadRacecard(ENEStatement statement, String strCourse, String strCountry, String strDate, String strScheduledTime, long nRace, String strSource, SmartformRunnerContainer race, boolean bUpdate)
    {
        String strRacecardURL = strATRRacecardURL + strCourse + "/" + strDate + "/" + strScheduledTime;
        System.out.println("AtTheRaces: " + strRacecardURL + "-" + race.getNrRunners());
        TagNode root = ExecuteURL.getRootNode(strRacecardURL, "utf-8");
        String strVersion = "v1";       // addition of v2 on 2018-12-18
        if (root == null)
        {
            System.out.println("AtTheRaces race not found");
            return -1;
        }
        TagNode[] tables = root.getElementsByName("table", true);
        TagNode[] aHorses = {};
        boolean bResult = true;
        if (tables.length > 0)
        {
            for(int j= 0; j < tables.length; j++)
            {
                TagNode[] aHorses1 =  tables[j].getElementsByAttValue("class", "horse", true, true);
                int nHorse = 0;
                for(int i = 0; i < aHorses1.length; i++)
                {
                    if ("td".equals(aHorses1[i].getName()))
                        nHorse++;
                }
                if (nHorse > 0)
                {
                    aHorses = aHorses1;
                    break;
                }
            }
        }
        if (aHorses.length == 0)
        {
            tables = root.getElementsByAttValue("class", "card", true, true);
            if (tables.length > 0)
            {
                aHorses = tables[0].getElementsByAttValue("class", "card-item", true, true);
                bResult = false;
            }
            if (aHorses.length == 0)
            {
                // v2
                tables = root.getElementsByAttValue("class", "card__content js-card__content", true, true);
                for(int i = 0; i < tables.length; i++)
                {
                    aHorses = tables[0].getElementsByAttValue("class", "horse", true, true);    // need to know that there are horse elements
                    if (aHorses.length > 0)
                    {
                        aHorses = tables[0].getElementsByAttValue("class", "card-entry", true, true);   // but really want the card-entry elements
                        strVersion = "v2";
                        break;
                    }
                }
            }

        }
        if (aHorses.length == 0)
        {
            // Neither format applies
            System.out.println("AtTheRaces no horses");
            return -1;
        }
        int nColourMatches = 0;
        System.out.println(bResult ? "AtTheRaces Result" : "AtTheRaces Racecard");
        int nUpdate = 0;
        for(int i = 0; i < aHorses.length; i++)
        {
            try
            {
                TagNode horse = aHorses[i];
                AtTheRacesRunner atrRunner = null;
                if (bResult)
                {
                    if ("v1".equals(strVersion))
                        atrRunner = loadRacecardResult_v1(horse, race);
                    else
                        atrRunner = loadRacecardResult(horse, race);
                }
                else
                {
                    atrRunner = loadRacecardRacecard(horse, race);                    
                }
                if (atrRunner != null)
                {
                    String strATRColours = convertColours(atrRunner.m_strColours, strCountry);  
                    if (strATRColours != null && !"".equals(strATRColours))
                    {
                        String strJockeyColours="";
                        if (race != null)
                        {
                            // can compare old and new values
                            SmartformColoursRunner runner = (SmartformColoursRunner)race.getRunnerByName(atrRunner.m_strName);
                            if (runner == null)
                                System.out.println("SmartformDailyRunner not found: " + atrRunner.m_strName);
                            else
                            {
                                strJockeyColours = runner.getJockeyColours();
                                strJockeyColours = strJockeyColours.trim().replaceAll(" +", " ");  // remove double spaces
                            }
                        }
                        System.out.println(atrRunner.m_nClothNumber + "-" + atrRunner.m_strName + "-" + atrRunner.m_strOwner + "-" + strATRColours + " current: " + strJockeyColours);
                        try
                        {
                            int nRPOwner = Integer.valueOf(strJockeyColours);
                            System.out.println("insert into rp_owner_colours (rpc_owner_id, rpc_owner_colours, rpc_suffix) values (" + strJockeyColours + ",'" + strATRColours + "', '');");
                        }
                        catch(NumberFormatException e)
                        {

                        }
                        if (!"".equals(strATRColours) && (!strJockeyColours.replace(" & ", " and ").toLowerCase().equals(strATRColours.replace(" & ", " and ").toLowerCase())))
                        {
                             if (bUpdate)
                             {
                                nUpdate = AdditionalRunnersFactory.updateOwnerDetails(statement, nRace, strSource, atrRunner.m_nClothNumber, atrRunner.m_strName, atrRunner.m_strOwner, strATRColours, true);
                             }
                        }
                        else
                        {
                            nColourMatches++;
                        }
                    }
                }
            }
            catch(Exception e)
            {
                System.out.println("loadRacecard: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Colour matches: " + nColourMatches);
        
        return nUpdate;
    }
  private static AtTheRacesRunner loadRacecardRacecard(TagNode horse, SmartformRunnerContainer race)
  {
       String strName=horse.getElementsByAttValue("class", "horse-tracker-link", true, true)[0].getAttributeByName("data-horsename");
       String strColoursOwner = "";
        TagNode[] aSilks = horse.getElementsByAttValue("class", "silk", true, true);
        if (aSilks.length > 0)
        {
            strColoursOwner = aSilks[0].getAttributeByName("data-tooltip");
        }
        int nClothNumber = 0;
        try
        {
            nClothNumber = Integer.parseInt(ExecuteURL.getFirstNodeContent(horse.getElementsByAttValue("class", "card-no-draw__no", true, true)[0]));
        }
        catch(Exception e)
        {
            // carry on
        }
                
     return new AtTheRacesRunner(nClothNumber, strName, strColoursOwner.replace("&#xA;", ""));
  }
  private static AtTheRacesRunner loadRacecardResult(TagNode horse, SmartformRunnerContainer race)
  {
        String strName="";
        int nClothNumber = 0;
        TagNode[] aData;
        TagNode[] astrName = horse.getElementsByAttValue("class", "h6 flush", true, true);
        if (astrName.length == 0)
            astrName = horse.getElementsByAttValue("class", "h6 horse__details flush", true, true);
        if (astrName.length == 1)
        {
            String strClothNumber = horse.getAttributeByName("data-number");      // h6 horse__details flush", true, true);
               aData = astrName[0].getElementsByName("span", true);
               if (aData.length > 0)
               {
                   if (strClothNumber == null)
                        strClothNumber = ExecuteURL.getFirstNodeContent(aData[0]);
                    try
                    {
                         nClothNumber = Integer.parseInt(strClothNumber.replace(".", ""));
                    }
                    catch(Exception e)
                    {
                         System.out.println("Cloth number not found: " + strClothNumber);
                    }
               }
               aData = astrName[0].getElementsByName("a", true);
                if (aData.length > 0)
                {
                    strName = ExecuteURL.getFirstNodeContent(aData[0]);
                    if ("".equals(strName))
                        strName = ExecuteURL.getNodeContentString(aData[0]);
                    if ("".equals(strName))
                        System.out.println("Name not found: " + aData[0].toString());
                    strName=strName.replace("&#39;", "'");
                }
                else
                    return null;
            
        }
        else
        {
            astrName = horse.getElementsByAttValue("class", "odds-grid-horse__name", true, true);
            if (astrName.length == 1)
              strName = ExecuteURL.getNodeContentString(astrName[0]);
            astrName = horse.getElementsByAttValue("class", "odds-grid-horse__no", true, true);
            if (astrName.length == 1)
            {
                    String strClothNumber = ExecuteURL.getFirstNodeContent(astrName[0]);
                    try
                    {
                         nClothNumber = Integer.parseInt(strClothNumber.replace(".", ""));
                    }
                    catch(Exception e)
                    {
                         // carry on
                    }
            }
         }
                
        TagNode[] aImages = horse.getElementsByName("img", true);
        String strColoursOwner = "";
        if (aImages.length > 0)
        {
            strColoursOwner = aImages[0].getAttributeByName("title");
        }

        return new AtTheRacesRunner(nClothNumber, strName, strColoursOwner.replace("&#xA;", ""));
    }
  private static AtTheRacesRunner loadRacecardResult_v1(TagNode horse, SmartformRunnerContainer race)
  {
        System.out.println("loadRacecardResult_v1: " + horse.toString());
        String strName="";
        TagNode[] aData = horse.getElementsHavingAttribute("data-horsename", true);
        if (aData.length == 0)
        {
            aData = horse.getElementsByName("horse", true);
            if (aData.length == 0)
            {
                aData = horse.getElementsByName("a", true);
                if (aData.length > 0)
                    strName = ExecuteURL.getFirstNodeContent(aData[0]);
                else
                    return null;
            }
            else
            {
                strName = ExecuteURL.getNodeContentString(aData[0]);
            }
        }
        else
        {
            strName = aData[0].getAttributeByName("data-horsename");
        }

        TagNode[] astrCloth = horse.getElementsByAttValue("class", "cloth-number", true, true);
        String strClothNumber="";
        if (astrCloth.length > 0)
        {
            strClothNumber= astrCloth[0].getText().toString().replace(".", "").trim();
        }
        int nClothNumber = 0;
        try
        {
            nClothNumber = Integer.parseInt(strClothNumber);
        }
        catch(Exception e)
        {
            // carry on
        }
        TagNode[] aImages = horse.getElementsByName("img", true);
        String strColoursOwner = "";
        if (aImages.length > 0)
        {
            strColoursOwner = aImages[0].getAttributeByName("alt");
        }
        else
        {
            TagNode[] aSilks = horse.getElementsByAttValue("class", "silk summary", true, true);
            if (aSilks.length > 0)
            {
                strColoursOwner = aSilks[0].getAttributeByName("data-tooltip");
            }
        }

        return new AtTheRacesRunner(nClothNumber, strName, strColoursOwner.replace("&#xA;", ""));
    }
    private static String convertColours(String strColours, String strCountry)
    {
        // pseudo French syntax
        String strColours1 = strColours;
        strColours1 = strColours1.replace(" &amp; ", " and ");
        if ("FR".equals(strCountry))
        {
            strColours1 = strColours1.replace(" body,", ",");
            strColours1 = strColours1.replace(" Arms,", " sleeves,");
            strColours1 = strColours1.replace(" arms,", " sleeves,");
            strColours1 = strColours1.replace(" halves sleeves", " halved sleeves");
            strColours1 = strColours1.replace("soft blue", "light blue");
            strColours1 = strColours1.replace("Soft Blue", "light blue");
            strColours1 = strColours1.replace("blue-light", "light blue");
            strColours1 = strColours1.replace("green-light", "light green");
            strColours1 = strColours1.replace("Green-light", "Light green");
            strColours1 = strColours1.replace("Blue-light", "Light blue");
            strColours1 = strColours1.replace("big-", "dark ");
            strColours1 = strColours1.replace("Big-", "Dark ");
            strColours1 = strColours1.replace("circles", "hoops");
            strColours1 = strColours1.replace("garnet", "maroon");
            strColours1 = strColours1.replace("Garnet", "Maroon");
            strColours1 = strColours1.replace("saint's cross andre", "cross-belts");
            strColours1 = strColours1.replace("saint andre's cross", "cross-belts");
            strColours1 = strColours1.replace("diaboloes", "diabolo");
            strColours1 = strColours1.replace("diablo sleeves", "diabolo on sleeves");
            strColours1 = strColours1.replace("Rose ", "Pink ");
            strColours1 = strColours1.replace("Rose, ", "Pink, ");
            strColours1 = strColours1.replace(", rose ", ", pink ");
            strColours1 = strColours1.replace("Blue, blue sleeves, blue cap ", "Royal blue");   // Godolphin
        }
        
        // soft blue
        // blue large spots
        //if (!strColours.equals(strColours1))
        //    System.out.println(strColours + " -> " + strColours1);
        return strColours1;
    }
    private static class AtTheRacesRunner
    {
        public int m_nClothNumber;
        public String m_strName;
        public String m_strOwner;
        public String m_strColours;
        
        public AtTheRacesRunner(int nClothNumber, String strName, String strColoursOwner)
        {
            m_nClothNumber = nClothNumber;
            m_strName = strName;
            m_strColours=strColoursOwner;
            // split on LAST ( which is used to indicate owner
            int nBracket = strColoursOwner.indexOf("(");
            if ((strColoursOwner.indexOf("(quartered)") > 0) || (strColoursOwner.indexOf("(halved)") > 0))
                nBracket = strColoursOwner.indexOf("(", strColoursOwner.indexOf("(") + 1);
            if (nBracket == 0)
            {
                // owner only
                m_strColours = "";
                m_strOwner = strColoursOwner.substring(1).replace(")", "").trim();
                if ("0".equals(m_strOwner))
                    m_strOwner="";
            }
            if (nBracket > 0)
            {
                m_strColours = strColoursOwner.substring(0, nBracket).replace("&amp;", "&").trim().replaceAll("[\\s]+", " ").trim();
                m_strOwner=strColoursOwner.substring(nBracket + 1).replace("&amp;", "&").replace("&#xA;", "").replace(")", "").trim();
            }
        }
    }
 }
