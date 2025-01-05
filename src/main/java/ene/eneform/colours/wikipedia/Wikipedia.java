/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.wikipedia;

import org.json.JSONObject;
import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.database.*;
import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.mero.factory.ENEMeroFactory;
import ene.eneform.mero.factory.SVGFactoryUtils;
import ene.eneform.mero.parse.ENEColoursParser;
import ene.eneform.smartform.bos.AdditionalRaceInstance;
import ene.eneform.smartform.bos.SmartformColoursRunner;
import ene.eneform.smartform.bos.SmartformPrimaryOwnerColours;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.FileUtils;
import org.w3c.dom.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Simon
 */
public class Wikipedia {
 
    private static String sm_strOpenHeader= "{{Jockey colours header$LINE_BREAK$| name = %s}}$LINE_BREAK$";
    private static String sm_strOpenFullHeader= "{{Jockey colours full header$LINE_BREAK$| name = %s}}$LINE_BREAK$";
    private static String sm_strCloseHeader= "|}$LINE_BREAK$";
    private static String sm_strNoFooter= "{{Jockey colours no footer}}$LINE_BREAK$";
    private static String sm_strCollapsibleHeader= "{{Jockey colours collapsible header}}$LINE_BREAK$";
    private static String sm_strNamedCollapsibleHeader= "{{Jockey colours named collapsible header$LINE_BREAK$| name = %s }}$LINE_BREAK$";
    private static String sm_strRowHeader = "{{Jockey colours row$LINE_BREAK$| year = %s$LINE_BREAK$";
    private static String sm_strRowFooter="}}$LINE_BREAK$";
    private static String sm_strRunnerTemplate="| image%1$d = File:Owner %2$s.svg$LINE_BREAK$| alt%1$d = %3$s$LINE_BREAK$| caption%1$d = %4$s$LINE_BREAK$";
    private static String sm_strFooter= "{{Jockey colours footer}}$LINE_BREAK$";
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static void generateRacePages(ENEStatement statement, String strWhere, boolean bRecreate)
    {
        ArrayList<AdditionalRaceData> alRaceData = AdditionalRaceDataFactory.createAdditionalRaceDataList(statement, strWhere, null);
        for(int i = 0; i < alRaceData.size(); i++)
        {
            AdditionalRaceData ard = alRaceData.get(i);
            String strRegion = "UK";
            String strCountry = ard.getCountry();
            if ("Eire".equalsIgnoreCase(strCountry) || "Northern Ireland".equalsIgnoreCase(strCountry))
                strRegion = "Ireland";
            String strType = "NH";
            String strRaceType = ard.getRaceType();
            if ("Flat".equalsIgnoreCase(strRaceType))
                strType = "Flat";
            WikipediaFactory.generateRacePage(strRegion, strType, ard.getName(), bRecreate);
        }
    }
      

   public static String generateWikipediaOwner(ENEStatement statement, SmartformPrimaryOwnerColours owner, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws FileNotFoundException, UnsupportedEncodingException, IOException
   {
        String strOwnerName = getOwnerName(owner.getOwnerName()); 
        String strFileName = getOwnerFileName(strOwnerName);
        ENERacingColours colours = ENEColoursEnvironment.getInstance().createRacingColours("en", ENEColoursEnvironment.getInstance().createJacket("en", owner.getJacketSyntax()), ENEColoursEnvironment.getInstance().createSleeves("en",owner.getSleevesSyntax()), ENEColoursEnvironment.getInstance().createCap("en",owner.getCapSyntax()));
        colours.setDescription(owner.getColours());        
        createWikipediaImageFile(statement, strFileName, strOwnerName, colours, strComment, strLanguage, bCompress, bOverwrite);
        return strOwnerName;
   }
 
   public static String generateWikipediaOwner(ENEStatement statement, String strOwnerName, String strDescription, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws FileNotFoundException, UnsupportedEncodingException, IOException
   {
        strOwnerName = getOwnerName(strOwnerName);
        String strFileName = getOwnerFileName(strOwnerName);
        ENERacingColours colours = ENERacingColoursFactory.createColours(statement, strLanguage, strDescription);
        createWikipediaImageFile(statement, strFileName, strOwnerName, colours, strComment, strLanguage, bCompress, bOverwrite);
        return strOwnerName;
   } 
   public static String generateWikipediaOwner(ENEStatement statement, SmartformColoursRunner runner, String strLanguage) throws FileNotFoundException, UnsupportedEncodingException, IOException
   {
       String strPrimaryOwner = runner.getPrimaryOwner();   // use primary owner if exists i.e. if already record in Wikipedia_Images
       if ("".equals(strPrimaryOwner))
       {
            String strOwnerName = getOwnerName(runner.getOwnerName());
            String strJockeyColours = runner.getJockeyColours();
            System.out.println("generateWikipediaOwner does not exist: " + strOwnerName + "-" + strJockeyColours);
            if ((!"".equals(strJockeyColours)) && (!"Not available".equalsIgnoreCase(strJockeyColours)))
            {
                // RacingPost colours are set to owner id until the owner is mapped in rp_owner_colours table
                int nJockeyColours = 0; 
                try
                {
                    nJockeyColours = Integer.valueOf(strJockeyColours);
                }
                catch(NumberFormatException e)
                {
                }
                if (nJockeyColours == 0)
                {
                    String strFileName = getOwnerFileName(strOwnerName);
                    ENERacingColours colours = ENERacingColoursFactory.createRunnerColours(ENEColoursEnvironment.DEFAULT_LANGUAGE, runner);
                    createWikipediaImageFile(statement, strFileName, strOwnerName, colours, "", strLanguage, true, false);  // compress but don't overwrite
                }
            }
            System.out.println("generateWikipediaOwner New: " + strOwnerName + "-" + strJockeyColours);
            return strOwnerName;     
       }
       
       System.out.println("generateWikipediaOwner exists: " + strPrimaryOwner.toUpperCase());
       
       return strPrimaryOwner;
   }
   
   private static String getOwnerName(String strOwnerName)
   {
       return strOwnerName.trim().replace(" & ", " and ").replace("& ", " and ").replace("&", " and ").replace(" / ", " and ").replace("/ ", " and ").replace("/", " and ").replace(":", "-");      // remove bad chars for file name
   }
   public static String getOwnerFileName(String strOwnerName)
   {
        String strFullDirectory = ENEColoursEnvironment.getInstance().getVariable("SVG_OUTPUT_DIRECTORY") + ENEColoursEnvironment.getInstance().getVariable("SVG_IMAGE_PATH") + "wikipedia";
        String strFileName = strFullDirectory + "/owners/owner_" + strOwnerName + ".svg";
        
       return strFileName;
   }
    public static void createImageFile(String strFileName, ENERacingColours colours, String strLanguage, boolean bCompress, boolean bOverwrite) throws IOException
    {
        Document document = (new ENEMeroFactory(colours, strLanguage)).generateSVGDocument("", 1, null);    // transparent background
        String strSVG = createImageContent(colours, strLanguage, bCompress);
        FileUtils.writeFile(strFileName, strSVG, StandardCharsets.ISO_8859_1, bOverwrite);
    }
    public static String createImageContent(String strColours, String strLanguage, boolean bCompress) throws IOException
    {
        ENERacingColours colours = new ENEColoursParser("en", strColours, "").parse();
        return createImageContent(colours, strLanguage, bCompress);
    }
    public static String createImageContent(ENEStatement statement, String strColours, String strLanguage, boolean bCompress) throws IOException
    {
        // use database conenction to parse description
        ENERacingColours colours = ENERacingColoursFactory.createColours(statement, strLanguage, strColours);
        return createImageContent(colours, strLanguage, bCompress);
    }
    public static String createImageContent(ENERacingColours colours, String strLanguage, boolean bCompress) throws IOException
    {
        Document document = (new ENEMeroFactory(colours, strLanguage)).generateSVGDocument("", 1, null);    // transparent background
        String strSVG = SVGFactoryUtils.convertSVGNode2String(document, bCompress);
        return strSVG;
    }
    public static void createWikipediaImageFile(ENEStatement statement, String strFileName, String strOwner, String strDescription, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws IOException
    {
        // specific for wikipedia owners - generate image and add to wikipedia_images table
        ENERacingColours colours = ENERacingColoursFactory.createColours(statement, strLanguage, strDescription);
        createImageFile(strFileName, colours, strLanguage, bCompress, bOverwrite);

        if ((strOwner != null) && (!"".equals(strOwner)) && strOwner.indexOf("test") < 0)
            WikipediaImagesFactory.insertWikipediaImage(statement, strOwner, colours.getJacket().getDefinition(), colours.getSleeves().getDefinition(), colours.getCap().getDefinition(), colours.getTitle(), strComment, true); // overwrite
    }
    public static void createWikipediaImageFile(ENEStatement statement, String strFileName, String strOwner, ENERacingColours colours, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws IOException
    {
        // specific for wikipedia owners - generate image and add to wikipedia_images table
        createImageFile(strFileName, colours, strLanguage, bCompress, bOverwrite);

        if ((strOwner != null) && (!"".equals(strOwner)) && strOwner.indexOf("test") < 0)
            WikipediaImagesFactory.insertWikipediaImage(statement, strOwner, colours.getJacket().getDefinition(), colours.getSleeves().getDefinition(), colours.getCap().getDefinition(), colours.getTitle(), strComment, true); // overwrite
    } 
    public static int generateMultipleOwnerColours(ENEStatement statement, String[] astrOwners, String strLanguage, boolean bCompress, boolean bReparse) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
         int nCount = 0;
           for(int i = 0; i < astrOwners.length; i++)
           {
               generateOwnerColours(statement, astrOwners[i], strLanguage, bCompress, bReparse);
               nCount++;
            }
           return nCount;
    }
    public static int generateOwnerColours(ENEStatement statement, String[] astrOwners, String strLanguage, boolean bCompress, boolean bReparse) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        int nCount = 0;
        for(int i = 0; i < astrOwners.length; i++)
        {
            boolean bSuccess = generateOwnerColours(statement, astrOwners[i], strLanguage, bCompress, bReparse);
            nCount += (bSuccess ? 1 : 0);
        }
        return nCount;
    }
    public static boolean generateOwnerColours(ENEStatement statement, String strOwner, String strLanguage, boolean bCompress, boolean bReparse) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        boolean bBackView = false;
           ArrayList<SmartformPrimaryOwnerColours> alOwners = WikipediaImagesFactory.selectWikipediaOwners(statement, new String[]{strOwner});
           if (alOwners.size() == 0)
            return false;

           SmartformPrimaryOwnerColours owner = alOwners.get(0);
           String strOwnerName = owner.getOwnerName();
           String strDescription = owner.getColours();
           if (bReparse)        // don't accept WikipediaImages breakdown
           {
                strOwnerName = generateWikipediaOwner(statement, strOwnerName, strDescription, "", strLanguage, bCompress, true);
           }
           else
           {
                strOwnerName = generateWikipediaOwner(statement, owner, "", strLanguage, bCompress, true);
                if (strOwnerName != null)
                    WikipediaImagesFactory.updateWikipediaImageTimestamp(statement, strOwnerName);
           }
/*                 OutputStreamWriter writer = createOwnerWriter(strOwnerName, true);  // overwrite svg
                if (writer != null)
                {
                   ENERacingColours colours = new ENERacingColours(ENEColoursEnvironment.DEFAULT_LANGUAGE, 
                            new ENEJacket(ENEColoursEnvironment.DEFAULT_LANGUAGE,owner.getJacketSyntax()), 
                            new ENESleeves(ENEColoursEnvironment.DEFAULT_LANGUAGE,owner.getSleevesSyntax()), 
                            new ENECap(ENEColoursEnvironment.DEFAULT_LANGUAGE,owner.getCapSyntax()));
                    String strDescription = owner.getColours();
                    if (!"".equals(strDescription))
                        colours.setDescription(strDescription);
                    RacingColoursDraw draw = new SingleWikipedia(colours, ENEColoursEnvironment.DEFAULT_LANGUAGE, false, bBackView);
                    String strTitle = ENEColoursSVGFactory.generateSVG(writer, draw); 
                }
                    */
                
                return true;
     }
 
 
   static public String generateHorseSequence(ENEStatement statement, String strHorse, String strBred, String strLanguage, String strLineBreak) {
        // no id specified - retrieve from additional_race_link
        List<AdditionalRaceInstance> aRaces = AdditionalRaceLinkFactory.getAdditionalRaceHorseLinks(statement, strHorse, strBred);
        //WikipediaFactory.updateAdditionalRaceLink(statement, aRaces.get(0).getRace(), strDescription);
         return generateHorseRaces123HTML(statement, strHorse, aRaces, strLanguage, strLineBreak);
    }

   static public String generateRaceSequence(ENEStatement statement, String strDescription, String strLanguage, String strLineBreak) {
        // no id specified - retrieve from additional_race_link
        List<AdditionalRaceInstance> aRaces = AdditionalRaceLinkFactory.getAdditionalRaceLinks(statement, strDescription);
        //WikipediaFactory.updateAdditionalRaceLink(statement, aRaces.get(0).getRace(), strDescription);
        AdditionalRaceData ard = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(strDescription);
        return generateRaces123Wikipedia(statement, ard.getTitle(), aRaces, strLanguage, strLineBreak);
    }
    
    static public String generateRace(ENEStatement statement, JSONObject obj, String strLanguage, String strLineBreak) {
        // JSONObject from web interface containing name, url, id, source, date attributes
        Date dt = null;
        try
        {
            String strDate = (String)obj.get("date");
            dt = DATE_FORMAT.parse(strDate);
        }
        catch(ParseException e)
        {
            return null;
        }
        AdditionalRaceInstance arl = new AdditionalRaceInstance(obj.get("source").toString(), Integer.parseInt(obj.get("id").toString()), dt);
        return generateSingleRace123Wikipedia(statement, arl, strLanguage, strLineBreak);
    }
    static public String generateRace(ENEStatement statement, String strDescription, String strLanguage, String strLineBreak) {
        // no id specified - retrieve latest from additional_race_link - looks in both SF and SL 
        AdditionalRaceInstance arl = AdditionalRaceLinkFactory.getLatestAdditionalRaceLink(statement, strDescription);
        return generateSingleRace123Wikipedia(statement, arl, strLanguage, strLineBreak);
    }
    static public String generateRace(ENEStatement statement, int nRace, String strSource, String strLanguage, String strLineBreak) {
        AdditionalRaceInstance arl = AdditionalRaceLinkFactory.getAdditionalRaceLinkObject(statement, nRace, strSource);
       AdditionalRaceData ard = AdditionalRaceDataFactory.createAdditionalRaceData(statement, nRace, strSource);
       return generateSingleRace123Wikipedia(statement, arl, strLanguage, strLineBreak);
    }
   static public String generateRace(ENEStatement statement, String strDescription, int nYear, String strLanguage, String strLineBreak) {
        // no id specified - retrieve from additiona_race_link for given year
       // SmartForm only
       String strRaceContent="";
        AdditionalRaceInstance arl = AdditionalRaceLinkFactory.getYearAdditionalRaceLink(statement, strDescription, nYear);
         if (arl != null)
            strRaceContent = generateSingleRace123Wikipedia(statement, arl, strLanguage, strLineBreak);
        else
            System.out.println("generateRace not found for: " + strDescription + "-" + nYear);
        
        return strRaceContent;
    }
   // Seperate image for each runner
   
 
    public static String generateRaceRunnerTriplet(ENEStatement statement, ArrayList<SmartformColoursRunner> alRunners, String strTitle, String strLineBreak)
    {
        String strContent="";
        String strRowHeader = sm_strRowHeader.replace("$LINE_BREAK$", strLineBreak);
        String strRowFooter = sm_strRowFooter.replace("$LINE_BREAK$", strLineBreak);
        String strRunnerTemplate = sm_strRunnerTemplate.replace("$LINE_BREAK$", strLineBreak);
        try
        {
             strContent += String.format(strRowHeader, strTitle);

            // to do: add "Only 2 finished/ran"
            int nPlaces = 3;
            if (alRunners.size() < 3)
                nPlaces = alRunners.size();
            for(int i = 0; i < nPlaces; i++)
            {
                 SmartformColoursRunner runner =  alRunners.get(i);
                 String strFileName = generateWikipediaOwner(statement, runner, ENEColoursEnvironment.DEFAULT_LANGUAGE);
                 String strName = runner.getName().trim();
                 if ((strName.length() >= 17) && (strName.indexOf(" ") <= 0))
                        strName = ("<small>" + strName + "</small>");
                 strContent += String.format(strRunnerTemplate, i+1, strFileName, runner.getJockeyColours().replace(" & ", " and "), strName);
             }
             strContent += strRowFooter;
        }
        catch(Exception e)
        {

        }
        return strContent;
    }
    public static String generateRaces123Wikipedia(ENEStatement statement, String strTitle, List<AdditionalRaceInstance> aRaces, String strLanguage, String strLineBreak)
    {
        String strContent="";
        String strOpenHeader = sm_strOpenHeader.replace("$LINE_BREAK$", strLineBreak);
        String strCloseHeader = sm_strCloseHeader.replace("$LINE_BREAK$", strLineBreak);
        String strCollapsibleHeader = sm_strCollapsibleHeader.replace("$LINE_BREAK$", strLineBreak);
        String strNamedCollapsibleHeader = sm_strNamedCollapsibleHeader.replace("$LINE_BREAK$", strLineBreak);
        String strNoFooter = sm_strNoFooter.replace("$LINE_BREAK$", strLineBreak);
        String strFooter = sm_strFooter.replace("$LINE_BREAK$", strLineBreak);
        int nRaces = aRaces.size();
        for(int i = 0; i < nRaces; i++)
        {
            AdditionalRaceInstance race = aRaces.get(i);
            String strYear = race.getYearString();
            int nYear = race.getYear();
            if (i == 0)
            {
                // first of a race sequence so need main header (if writing to file)
                strContent += String.format(strOpenHeader, strTitle);
            }
            else if (i == 1)
            {
                // second of a sequence, so need collapsible header
                 strContent += strCollapsibleHeader;
            }
            else if((nRaces > 15) && (nYear == 2010))
            {
                strContent += strNoFooter;
                strContent += String.format(strNamedCollapsibleHeader, "2010-2001");
            }
            else if((nRaces > 15) && (nYear == 2000))
            {
                strContent += strNoFooter;
                strContent += String.format(strNamedCollapsibleHeader, "2000-1991");
            }
            else if((nRaces > 15) && (nYear == 1990))
            {
                strContent += strNoFooter;
                strContent += String.format(strNamedCollapsibleHeader, "1990-1988");
            }
            
            strContent += generateRace123Wikipedia(statement, race, String.valueOf(race.getYear()), strLanguage, strLineBreak);
            
            if (i == 0)
            {
                strContent += strCloseHeader;
            }
        }
        strContent += strFooter;
        
        return strContent;
    }
public static String generateSingleRace123Wikipedia(ENEStatement statement, AdditionalRaceInstance race, String strLanguage, String strLineBreak)
{
       String strContent = "";
       //strContent += ("--" + racedata.getCourse() + " - " + racedata.getTitle()  + strLineBreak);
        
       strContent += generateRace123Wikipedia(statement, race, race.getYearString(), strLanguage, strLineBreak);
       
       return strContent;
}

public static String generateRace123Wikipedia(ENEStatement statement, AdditionalRaceInstance race, String strTitle, String strLanguage, String strLineBreak)
    {
    String strContent="";
    String strRowHeader = sm_strRowHeader.replace("$LINE_BREAK$", strLineBreak);
    String strRowFooter = sm_strRowFooter.replace("$LINE_BREAK$", strLineBreak);
    String strRunnerTemplate = sm_strRunnerTemplate.replace("$LINE_BREAK$", strLineBreak);
    try
    {
         strContent += String.format(strRowHeader, strTitle);
        ArrayList<SmartformColoursRunner> alDailyRunners = ENEColoursRunnerFactory.getSmartformRaceRunners(statement, race, 3);

        // to do: add "Only 2 finished/ran"
        int nPlaces = 3;
        if (alDailyRunners.size() < 3)
            nPlaces = alDailyRunners.size();
        for(int i = 0; i < nPlaces; i++)
        {
             SmartformColoursRunner runner =  alDailyRunners.get(i);
             String strFileName = generateWikipediaOwner(statement, runner, strLanguage);
             String strName = runner.getName().trim();
             if ((strName.length() >= 17) && (strName.indexOf(" ") <= 0))
                    strName = ("<small>" + strName + "</small>");
             strContent += String.format(strRunnerTemplate, i+1, strFileName, runner.getJockeyColours().replace(" & ", " and "), strName);
         }
         strContent += strRowFooter;
     }
    catch(Exception e)
    {
        ENEColoursEnvironment.getInstance().trace("generateDailyRaceTableWikipedia: " + e.getMessage());
        e.printStackTrace();
    }
    //System.out.println(strContent);
    return strContent;
}       
public static String generateHorseRaces123HTML(ENEStatement statement, String strTitle, List<AdditionalRaceInstance> aRaces, String strLanguage, String strLineBreak)
{
        
        String strHorseHeader="<h1>%s</h1>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
        String strHorseTable="<table cellpadding=\"0\" cellspacing=\"0\" style=\"clear:right; float:right; text-align:center; font-weight:bold;\" width=\"100%\">$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
        String strHorseTitle="<th colspan=\"7\">%s</th>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
        String strSeasonCell="<td>%s</td>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
        String strRowOpen="<tr>";
        String strRowClose="</tr>";
        String strTableClose="</table>";
        String strDivClear="<div class=\"clear\">$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);  // replace strRaceCell
        String strDivOpen="<div class=\"grid%d\">$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);  // replace strRaceCell
        String strDivClose="</div>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);    // replace strCellClose
        
        //String strContent = strHorseTable + strRowOpen + String.format(strHorseTitle, strTitle) + strRowClose;
        String strContent = String.format(strHorseHeader, strTitle);
        int nRaces = aRaces.size();
        String strCurrentSeason="";
        for(int i = 0; i < nRaces; i++)
        {
            AdditionalRaceInstance race = aRaces.get(i);
            String strSeason = race.getSeasonString();
            if(!strSeason.equals(strCurrentSeason))
            {
                if (i > 0)
                    strContent += strDivClear;          // strRowClose;
                
                //strContent += strRowOpen + String.format(strSeasonCell, strSeason);
                strContent += String.format(strDivOpen, 12) + strSeason + strDivClose;
                strCurrentSeason = strSeason;
            }
            
            strContent += generateRace123HTML(statement, race, race.getAbbreviatedTitle() + "<br />" + race.getCourse() + ", " + race.getFormattedDistance() + " - " + race.getFormattedMeetingDate("MMMM d") , strLanguage, strLineBreak, strTitle);
        }
        //strContent += strRowClose + strTableClose;
        
        return strContent;
}

public static String generateRace123HTML(ENEStatement statement, AdditionalRaceInstance race, String strTitle, String strLanguage, String strLineBreak, String strHorse)
{
    String strContent="";
    String strRaceTable="<table cellpadding=\"0\" cellspacing=\"0\" style=\"clear:right; float:right; text-align:center; font-weight:bold; width:100%;\">$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
    String strRaceTitleCell="<td class=\"jockey_colours_year_cell\" colspan=\"3\" style=\"border:1px solid black; border-top:2px solid black; border-bottom: none; background-color:%s;\">%s</td>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
    String strJockeyColoursCell1="<td style=\"border-left:1px solid black; border-right:1px solid; background-color:$BACKGROUND_COLOUR$;\" class=\"jockey_colours_silks\" name=\"%s\" title=\"%s\"></td>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
    String strJockeyColoursCell="<td style=\"border-right:1px solid black; background-color:$BACKGROUND_COLOUR$;\" class=\"jockey_colours_silks\" name=\"%s\" title=\"%s\"></td>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
    String strHorseCell1="<td style=\"width:33%%; border:1px solid black; border-top:none; font-size:%d%%; background-color:$BACKGROUND_COLOUR$;\">%s</td>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
    String strHorseCell="<td style=\"width:33%%; border-right:1px solid black; border-bottom:1px solid black; font-size:%d%%; background-color:$BACKGROUND_COLOUR$;\">%s</td>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);
    String strTableClose="</table>";
    String strHorseRowOpen="<tr style=\"line-height=0.8em;\">";
    String strRowOpen="<tr>";
    String strRowClose="</tr>";
    String strCellOpen="<td>";
    String strCellClose="</td>";
    String strDivOpen="<div class=\"grid%d\">$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);  // replace strRaceCell
    String strDivClose="</div>$LINE_BREAK$".replace("$LINE_BREAK$", strLineBreak);    // replace strCellClose
    String [] astrBackgroundColours = {"#B9CBCD", "#22FE3C", "#F8F45D", "#FEB511"};   // grey, green, yellow, orange
    try
    {
        int nGroupRace = race.getGroupRace();
        String strTitleBackgroundColour = astrBackgroundColours[nGroupRace];
        strContent += (String.format(strDivOpen, 7) + strRaceTable + strRowOpen + String.format(strRaceTitleCell, strTitleBackgroundColour, strTitle) + strRowClose);            // 1st Row
        ArrayList<SmartformColoursRunner> alDailyRunners = ENEColoursRunnerFactory.getSmartformRaceRunners(statement, race, 3);

        // to do: add "Only 2 finished/ran"
        int nPlaces = 3;
        if (alDailyRunners.size() < 3)
            nPlaces = alDailyRunners.size();
        
        strContent += strRowOpen;
        for(int i = 0; i < nPlaces; i++)        // 2nd Row
        {
            SmartformColoursRunner runner =  alDailyRunners.get(i);
            String strName = runner.getName().trim();
            String strBackgroundColour = strName.equalsIgnoreCase(strHorse) ? "#eee9e9" : "white";
             String strFileName = generateWikipediaOwner(statement, runner, strLanguage).replace(" & ", " and ");
             if (i == 0)
                strContent += String.format(strJockeyColoursCell1.replace("$BACKGROUND_COLOUR$", strBackgroundColour), strFileName, strFileName);
             else
                strContent += String.format(strJockeyColoursCell.replace("$BACKGROUND_COLOUR$", strBackgroundColour), strFileName, strFileName);
         }
        if (nPlaces == 2)   // always at least 2
            strContent += "<td style=\"border-right:1px solid black;\">&nbsp;</td>";
        
        strContent += strRowClose;
        strContent += strHorseRowOpen;
        for(int i = 0; i < nPlaces; i++)        // 3rd Row
        {
             SmartformColoursRunner runner =  alDailyRunners.get(i);
             String strName = runner.getName().trim();
             String strBackgroundColour = strName.equalsIgnoreCase(strHorse) ? "#eee9e9" : "white";
             int nSize = 85;
             if (strName.length() >= 17)
                    nSize = 65;
             else if (strName.length() >= 14)
                    nSize = 72;
             else if (strName.length() >= 12)
                    nSize = 75;
             if (i == 0)
	             strContent += String.format(strHorseCell1.replace("$BACKGROUND_COLOUR$", strBackgroundColour), nSize, strName);
             else
	             strContent += String.format(strHorseCell.replace("$BACKGROUND_COLOUR$", strBackgroundColour), nSize, strName);
         }
         if (nPlaces == 2)   // always at least 2
            strContent += String.format(strHorseCell.replace("$BACKGROUND_COLOUR$", "white"), 70, "Only 2 finished");
       
         strContent += (strTableClose + strDivClose);
     }
    catch(Exception e)
    {
        ENEColoursEnvironment.getInstance().trace("generateDailyRaceTableHTML: " + e.getMessage());
        e.printStackTrace();
    }
    //System.out.println(strContent);
    return strContent;
}       

 
}
