/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.service;

import ene.eneform.colours.domain.*;
import ene.eneform.colours.repository.AdditionalRaceDataRepository;
import ene.eneform.smartform.bos.AdditionalRaceInstance;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.database.*;
import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.mero.factory.ENEMeroFactory;
import ene.eneform.mero.factory.SVGFactoryUtils;
import ene.eneform.mero.parse.ENEColoursParser;
import ene.eneform.smartform.bos.SmartformColoursRunner;
import ene.eneform.smartform.bos.SmartformPrimaryOwnerColours;
import ene.eneform.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
@Service
@RequiredArgsConstructor
@Slf4j
public class WikipediaService {

    private final WikipediaImageService wikipediaImageService;
    private final RacingColoursParseService rcpService;
    private final AdditionalRaceLinkService arlService;
    private final UnregisteredColourSyntaxService ucsService;
    private final BasicRaceService raceService;
    private final ColourRunnerService runnerService;
        private final AdditionalRaceDataRepository ardRepository;
    @Value("${ene.eneform.colours.svg_output_dir}")
    private static String SVG_OUTPUT_DIRECTORY;
 
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
    private final ColourRunnerService colourRunnerService;

    public void generateRacePages(String raceName, boolean bRecreate)
    {
        AdditionalRaceData ard = ardRepository.findByName(raceName);
        //List<AdditionalRaceData> alRaceData = ardRepository.findByName(raceName);
    //    for(int i = 0; i < alRaceData.size(); i++)
    //    {
           // AdditionalRaceData ard = alRaceData.get(i);
            String strRegion = "UK";
            String strCountry = ard.getCountry();
            if ("Eire".equalsIgnoreCase(strCountry) || "Northern Ireland".equalsIgnoreCase(strCountry))
                strRegion = "Ireland";
            String strType = "NH";
            String strRaceType = ard.getRaceType();
            if ("Flat".equalsIgnoreCase(strRaceType))
                strType = "Flat";
            WikipediaFactory.generateRacePage(strRegion, strType, ard.getName(), bRecreate);
 //       }
    }
      

   public String generateWikipediaOwner(OwnerColours owner, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws FileNotFoundException, UnsupportedEncodingException, IOException
   {
        String strOwnerName = getOwnerName(owner.getOwner());
        String strFileName = getOwnerFileName(strOwnerName);
        ENERacingColours colours = ENEColoursEnvironment.getInstance().createRacingColours("en", ENEColoursEnvironment.getInstance().createJacket("en", owner.getJacket()),
                ENEColoursEnvironment.getInstance().createSleeves("en",owner.getSleeves()),
                ENEColoursEnvironment.getInstance().createCap("en",owner.getCap()));
        colours.setDescription(owner.getColours());        
        createWikipediaImageFile(strFileName, strOwnerName, colours, strComment, strLanguage, bCompress, bOverwrite);
        return strOwnerName;
   }
 
   public String generateWikipediaOwner(String strOwnerName, String strDescription, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws FileNotFoundException, UnsupportedEncodingException, IOException
   {
        strOwnerName = getOwnerName(strOwnerName);
        String strFileName = getOwnerFileName(strOwnerName);
        ENERacingColours colours = createColours(strLanguage, strDescription);
        createWikipediaImageFile(strFileName, strOwnerName, colours, strComment, strLanguage, bCompress, bOverwrite);
        return strOwnerName;
   } 
   public String generateWikipediaOwner(ColourRunner runner, String strLanguage) throws FileNotFoundException, UnsupportedEncodingException, IOException
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
                    ENERacingColours colours = createRunnerColours(ENEColoursEnvironment.DEFAULT_LANGUAGE, runner);
                    createWikipediaImageFile(strFileName, strOwnerName, colours, "", strLanguage, true, false);  // compress but don't overwrite
                }
            }
            System.out.println("generateWikipediaOwner New: " + strOwnerName + "-" + strJockeyColours);
            return strOwnerName;     
       }
       
       System.out.println("generateWikipediaOwner exists: " + strPrimaryOwner.toUpperCase());
       
       return strPrimaryOwner;
   }
   
   private String getOwnerName(String strOwnerName)
   {
       return strOwnerName.trim().replace(" & ", " and ").replace("& ", " and ").replace("&", " and ").replace(" / ", " and ").replace("/ ", " and ").replace("/", " and ").replace(":", "-");      // remove bad chars for file name
   }
   public String getOwnerFileName(String strOwnerName)
   {
        String strFileName = SVG_OUTPUT_DIRECTORY + "/owners/owner_" + strOwnerName + ".svg";
        
       return strFileName;
   }
    public void createImageFile(String strFileName, ENERacingColours colours, String strLanguage, boolean bCompress, boolean bOverwrite) throws IOException
    {
        Document document = (new ENEMeroFactory(colours, strLanguage)).generateSVGDocument("", 1, null);    // transparent background
        String strSVG = createImageContent(colours, strLanguage, bCompress);
        FileUtils.writeFile(strFileName, strSVG, StandardCharsets.ISO_8859_1, bOverwrite);
    }
    public String createImageContent(String strColours, String strLanguage, boolean bCompress) throws IOException
    {
        ENERacingColours colours = new ENEColoursParser("en", strColours, "").parse();
        return createImageContent(colours, strLanguage, bCompress);
    }
    public String createImageContent2(String strColours, String strLanguage, boolean bCompress) throws IOException
    {
        // use database conenction to parse description
        ENERacingColours colours = createColours(strLanguage, strColours);
        return createImageContent(colours, strLanguage, bCompress);
    }
    public String createImageContent(ENERacingColours colours, String strLanguage, boolean bCompress) throws IOException
    {
        Document document = (new ENEMeroFactory(colours, strLanguage)).generateSVGDocument("", 1, null);    // transparent background
        String strSVG = SVGFactoryUtils.convertSVGNode2String(document, bCompress);
        return strSVG;
    }
    public void createWikipediaImageFile(String strFileName, String strOwner, String strDescription, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws IOException
    {
        // specific for wikipedia owners - generate image and add to wikipedia_images table
        ENERacingColours colours = createColours(strLanguage, strDescription);
        createImageFile(strFileName, colours, strLanguage, bCompress, bOverwrite);

        if ((strOwner != null) && (!"".equals(strOwner)) && strOwner.indexOf("test") < 0) {
            wikipediaImageService.insertWikipediaImage(strOwner, colours.getJacket().getDefinition(), colours.getSleeves().getDefinition(), colours.getCap().getDefinition(), colours.getTitle(), strComment, true);
        }
    }
    public void createWikipediaImageFile(String strFileName, String strOwner, ENERacingColours colours, String strComment, String strLanguage, boolean bCompress, boolean bOverwrite) throws IOException
    {
        // specific for wikipedia owners - generate image and add to wikipedia_images table
        createImageFile(strFileName, colours, strLanguage, bCompress, bOverwrite);

        if ((strOwner != null) && (!"".equals(strOwner)) && strOwner.indexOf("test") < 0)
            wikipediaImageService.insertWikipediaImage(strOwner, colours.getJacket().getDefinition(), colours.getSleeves().getDefinition(), colours.getCap().getDefinition(), colours.getTitle(), strComment, true); // overwrite
    } 
    public int generateMultipleOwnerColours(String[] astrOwners, String strLanguage, boolean bCompress, boolean bReparse) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
         int nCount = 0;
           for(int i = 0; i < astrOwners.length; i++)
           {
               generateOwnerColours(astrOwners[i], strLanguage, bCompress, bReparse);
               nCount++;
            }
           return nCount;
    }
    public int generateOwnerColours(String[] astrOwners, String strLanguage, boolean bCompress, boolean bReparse) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        int nCount = 0;
        for(int i = 0; i < astrOwners.length; i++)
        {
            boolean bSuccess = generateOwnerColours(astrOwners[i], strLanguage, bCompress, bReparse);
            nCount += (bSuccess ? 1 : 0);
        }
        return nCount;
    }
    public boolean generateOwnerColours(String strOwner, String strLanguage, boolean bCompress, boolean bReparse) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        boolean bBackView = false;
           List<WikipediaImage> alOwners = wikipediaImageService.selectWikipediaOwners(new String[]{strOwner});
           if (alOwners.size() == 0)
            return false;

           OwnerColours owner = alOwners.get(0);
           String strOwnerName = owner.getOwner();
           String strDescription = owner.getColours();
           if (bReparse)        // don't accept WikipediaImages breakdown
           {
                strOwnerName = generateWikipediaOwner(strOwnerName, strDescription, "", strLanguage, bCompress, true);
           }
           else
           {
                strOwnerName = generateWikipediaOwner(owner, "", strLanguage, bCompress, true);
                if (strOwnerName != null)
                    wikipediaImageService.updateTimestamp(strOwnerName);
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
 
 
   public String generateHorseSequence(String strHorse, String strBred, String strLanguage, String strLineBreak) {
        // no id specified - retrieve from additional_race_link
        List<BasicRace> aRaces = raceService.findByHorseAndBred(strHorse, strBred);
        //WikipediaFactory.updateAdditionalRaceLink(aRaces.get(0).getRace(), strDescription);
         return generateHorseRaces123HTML(strHorse, aRaces, strLanguage, strLineBreak);
    }

   public String generateRaceSequence(String strDescription, String strLanguage, String strLineBreak) {
        // no id specified - retrieve from additional_race_link
        List<? extends BasicRaceInfo> aRaces = arlService.findByRaceName(strDescription);
        //WikipediaFactory.updateAdditionalRaceLink(aRaces.get(0).getRace(), strDescription);
        AdditionalRaceData ard = ardRepository.findByName(strDescription);
        return generateRaces123Wikipedia(ard.getTitle(), aRaces, strLanguage, strLineBreak);
    }
    
    public String generateRace(JSONObject obj, String strLanguage, String strLineBreak) {
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
        AdditionalRaceLink arl = AdditionalRaceLink.onCreate(obj.get("source").toString(),
                Integer.parseInt(obj.get("id").toString()), null, dt.getYear());
        return generateSingleRace123Wikipedia(arl, strLanguage, strLineBreak);
    }
    public String generateRace(String strDescription, String strLanguage, String strLineBreak) {
        // no id specified - retrieve latest from additional_race_link - looks in both SF and SL 
        AdditionalRaceLink arl = arlService.findLatestByRaceName(strDescription);
        return generateSingleRace123Wikipedia(arl, strLanguage, strLineBreak);
    }
    public String generateRace(int nRace, String strSource, String strLanguage, String strLineBreak) {
        BasicRace arl = raceService.findById(strSource, nRace);
       return generateSingleRace123Wikipedia(arl, strLanguage, strLineBreak);
    }
   public String generateRace(String strDescription, int nYear, String strLanguage, String strLineBreak) {
        // no id specified - retrieve from additiona_race_link for given year
       // SmartForm only
       String strRaceContent="";
        List<AdditionalRaceLink> arl = arlService.findByRaceNameAndYear(strDescription, nYear);
        if (arl.isEmpty()) {
            log.info("generateRace not found for: " + strDescription + "-" + nYear);
        } else {
            for (AdditionalRaceLink additionalRaceLink : arl) {
                strRaceContent += generateSingleRace123Wikipedia(additionalRaceLink, strLanguage, strLineBreak);
            }
        }

        return strRaceContent;
    }
   // Separate image for each runner
   
 
    public String generateRaceRunnerTriplet(List<ColourRunner> alRunners, String strTitle, String strLineBreak)
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
                 ColourRunner runner =  alRunners.get(i);
                 String strFileName = generateWikipediaOwner(runner, ENEColoursEnvironment.DEFAULT_LANGUAGE);
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
    public String generateRaces123Wikipedia(String strTitle, List<? extends BasicRaceInfo> aRaces, String strLanguage, String strLineBreak)
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
            BasicRaceInfo race = aRaces.get(i);
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
            
            strContent += generateRace123Wikipedia(race, String.valueOf(race.getYear()), strLanguage, strLineBreak);
            
            if (i == 0)
            {
                strContent += strCloseHeader;
            }
        }
        strContent += strFooter;
        
        return strContent;
    }
public String generateSingleRace123Wikipedia(BasicRaceInfo race, String strLanguage, String strLineBreak)
{
       String strContent = "";
       //strContent += ("--" + racedata.getCourse() + " - " + racedata.getTitle()  + strLineBreak);
        
       strContent += generateRace123Wikipedia(race, race.getYear().toString(), strLanguage, strLineBreak);
       
       return strContent;
}

public String generateRace123Wikipedia(BasicRaceInfo race, String strTitle, String strLanguage, String strLineBreak)
    {
    String strContent="";
    String strRowHeader = sm_strRowHeader.replace("$LINE_BREAK$", strLineBreak);
    String strRowFooter = sm_strRowFooter.replace("$LINE_BREAK$", strLineBreak);
    String strRunnerTemplate = sm_strRunnerTemplate.replace("$LINE_BREAK$", strLineBreak);
    try
    {
         strContent += String.format(strRowHeader, strTitle);
        List<ColourRunner> alDailyRunners = colourRunnerService.findByRaceAndNumRunners(race, 3);

        // to do: add "Only 2 finished/ran"
        int nPlaces = 3;
        if (alDailyRunners.size() < 3)
            nPlaces = alDailyRunners.size();
        for(int i = 0; i < nPlaces; i++)
        {
             ColourRunner runner =  alDailyRunners.get(i);
             String strFileName = generateWikipediaOwner(runner, strLanguage);
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
public String generateHorseRaces123HTML(String strTitle, List<BasicRace> aRaces, String strLanguage, String strLineBreak)
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
            BasicRace race = aRaces.get(i);
            String strSeason = race.getSeasonString();
            if(!strSeason.equals(strCurrentSeason))
            {
                if (i > 0)
                    strContent += strDivClear;          // strRowClose;
                
                //strContent += strRowOpen + String.format(strSeasonCell, strSeason);
                strContent += String.format(strDivOpen, 12) + strSeason + strDivClose;
                strCurrentSeason = strSeason;
            }
            
            strContent += generateRace123HTML(race, race.getAbbreviatedTitle() + "<br />" + race.getCourse() + ", " + race.getFormattedDistance() + " - " + race.getFormattedMeetingDate("MMMM d") , strLanguage, strLineBreak, strTitle);
        }
        //strContent += strRowClose + strTableClose;
        
        return strContent;
}

public String generateRace123HTML(BasicRace race, String strTitle, String strLanguage, String strLineBreak, String strHorse)
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
        List<ColourRunner> alDailyRunners = runnerService.findByRaceAndNumRunners(race, 3);

        // to do: add "Only 2 finished/ran"
        int nPlaces = 3;
        if (alDailyRunners.size() < 3)
            nPlaces = alDailyRunners.size();
        
        strContent += strRowOpen;
        for(int i = 0; i < nPlaces; i++)        // 2nd Row
        {
            ColourRunner runner =  alDailyRunners.get(i);
            String strName = runner.getName().trim();
            String strBackgroundColour = strName.equalsIgnoreCase(strHorse) ? "#eee9e9" : "white";
             String strFileName = generateWikipediaOwner(runner, strLanguage).replace(" & ", " and ");
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
             ColourRunner runner =  alDailyRunners.get(i);
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

 // new
 public ENERacingColours createRunnerColours(String strLanguage, ColourRunner runner)
 {
     return createRunnerColours(strLanguage, runner, SmartformRunnerFactory.sm_RCPVersion);
 }
    public ENERacingColours createRunnerColours(String strLanguage, ColourRunner runner, String strVersion)
    {
        ENERacingColours colours = null;
        if (runner != null)
        {
            // runner.getUnregisteredColourSyntax()
            colours = createRacingColours(null, strLanguage, runner.getJockeyColours(), strVersion);
        }

        return colours;
    }

    public ENERacingColours createColours(String strLanguage, String strJockeyColours)
    {
        return createColours(strLanguage, strJockeyColours, SmartformRunnerFactory.sm_RCPVersion);
    }
    public ENERacingColours createColours(String strLanguage, String strJockeyColours, String strVersion)
    {
        // first try wikipedia_images
        // then unregistered_colour_syntax
        UnregisteredColourSyntax ucs = ucsService.findByColours(strJockeyColours);
        return createRacingColours(ucs, strLanguage, strJockeyColours, strVersion);
    }

    public ENERacingColours createRacingColours(UnregisteredColourSyntax ucs, String strLanguage, String strJockeyColours, String strVersion) {
        ENERacingColours colours = null;
        if (ucs != null) {
            colours = ENEColoursEnvironment.getInstance().createRacingColours(strLanguage, ENEColoursEnvironment.getInstance().createJacket(strLanguage, ucs.getJacket()),
                    ENEColoursEnvironment.getInstance().createSleeves(strLanguage, ucs.getSleeves()),
                    ENEColoursEnvironment.getInstance().createCap(strLanguage, ucs.getCap()));
            colours.setDescription(strJockeyColours);
            rcpService.insertRacingColoursParse(strVersion, colours, "", "", "");
        } else {
            ENEColoursParser parser = new ENEColoursParser(strLanguage, strJockeyColours, "");
            colours = parser.parse();

            rcpService.insertRacingColoursParse(strVersion, colours, parser.getRemainder(), parser.getExpanded(), parser.getSyntax());
        }
        return colours;
    }

}
