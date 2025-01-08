/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.wikipedia;

import ene.eneform.colours.service.WikipediaService;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.utils.ENEStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Simon
 */
@Service
@RequiredArgsConstructor
public class WikipediaData {
    private final WikipediaService wikipediaService;
    private String sm_strTranscript = "";
    public void runWP(ENEStatement statement) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
       sm_strTranscript = "";
       
       //generateRacePages(statement, "", true);      // ard_name='2000 Guineas'"
       generateWikipediaData(statement);
       //parseWikipediaColours(statement, "smx_group_races");
       //parseWikipediaColours(statement, "smx_yesterday_races");
        //parseWikipediaDailyRaceColours(statement, 501119);
       
       if (!"".equals(sm_strTranscript))
       {
            String strFullDirectory = ENEColoursEnvironment.getInstance().getVariable("SVG_OUTPUT_DIRECTORY") + ENEColoursEnvironment.getInstance().getVariable("SVG_IMAGE_PATH") + "wikipedia";
            String strFileName = strFullDirectory + "/transcript.txt";
            FileOutputStream fos = new FileOutputStream(strFileName, true);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            writer.append("\r\n\r\n--" + new SimpleDateFormat("yyyyMMddhhmm").format(new Date()) + "\r\n");
            writer.append(sm_strTranscript);
            writer.close();
       }
   } 
   public void addToTranscript(String strTranscript)
   {
       sm_strTranscript += strTranscript + "\r\n\r\n";
   }

    public void generateWikipediaData(ENEStatement statement) throws FileNotFoundException, UnsupportedEncodingException, IOException
   {
         //generateWikipediaBreedersCup(statement);
       //Wikipedia.generateWikipediaOwner(statement, "Kenneth L and Sarah K Ramsey", "white:'r'-red", "red", "red:'r'-white", "White, red 'r', red sleeves, white armlet, red cap, white 'r'");
       //generateWikipediaCheltenham(statement);
       //generateWikipediaAintree(statement);
       //generateWikipediaPunchestown(statement);
      //generateWikipedia(statement);
       //generateWikipediaFlat14(statement);
      //generateLastWeek(statement);
      // 516919, 516920
        
      //generateRace(statement, "Hilary Needler Trophy", 2007);   // same colours in WI leads to repeat
      generateRace(statement, "St Leger", 2001);   // Mel Brittain red cap
       
       //generateRaceSequence(statement, "St Leger");
      //generateRace(statement, "World Trophy");
              
      
      // Retrieve info from Wikipedia about which years have been uploaded
       //loadWikipediaRacePages(statement, "en", "ard_wikipedia_start_year is null", "ard_country, ard_group_race, ard_name");
       //loadWikipediaRacePages(statement, "en", "ard_country in ('England', 'Scotland', 'Wales', 'France') and ard_race_type='Flat' and ard_group_race is not null and ard_group_race=1", "ard_country, ard_group_race");
       //loadWikipediaRacePages(statement, "fr", "ard_country in ('England', 'Scotland', 'Wales', 'France') and ard_race_type='Flat' and ard_group_race is not null and ard_group_race=1", "ard_country, ard_group_race");
       //loadWikipediaRacePages(statement, "en", "ard_race_type='Flat' and ard_name in (select ary_name from additional_race_year where year(ary_date)=2014)", "");
       //loadWikipediaRacePages(statement, "en", "ard_country in ('England', 'Scotland', 'Wales') and ard_race_type='Flat' and ard_group_race is null and en.arw_start_year is null", "ard_country, ard_group_race");
  
      
        //generateHorseSequence(statement, "Kauto Star"); 
        //generateRaceSequence(statement, "Galway Hurdle"); 
        
    /*    ArrayList<AdditionalRaceData> alARD = SmartformRaceFactory.createAdditionalRaceDataList(statement, "ard_country='Eire' and ard_race_type='Flat' and ard_group_race is not null and ard_group_race <= 2", "ard_group_race, ard_name");
        for(int i = 0; i < alARD.size(); i++)
        {
            generateRaceSequence(statement, alARD.get(i).getName());
        } */
       //generateRace(statement, "Phoenix Sprint", 2015);

      //generateWikipediaAscot(statement);
       //Wikipedia.generateOwnerColours(statement, new String[]{ "Zayat Stables Ltd"}, false);   // 2nd param is BackView
       //generateRace123SeparateWikipediaTranscript(statement, 502137,	451115,	388652,	332425,	279658,	224981,	184241});      // William Hill Lincoln
       //generateRace123SeparateWikipediaTranscript(statement, 509560,	449899,	394504,	338298,	281877,	224978,	184238});      // Brocklesby Conditions Stakes
       //generateYesterday(statement, true);
       //generateWikipediaGroup2013(statement);
      
       //generateWikipediaIreland2014(statement);
   }   
    private void generateHorseSequence(ENEStatement statement, String strHorse, String strBred) {
        addToTranscript(wikipediaService.generateHorseSequence(strHorse, strBred, ENEColoursEnvironment.DEFAULT_LANGUAGE, "\r\n"));
    }

    private void generateRaceSequence(ENEStatement statement, String strDescription) {
        addToTranscript(wikipediaService.generateRaceSequence(strDescription, ENEColoursEnvironment.DEFAULT_LANGUAGE, "\r\n"));
    }

    private void generateRace(ENEStatement statement, String strDescription) {
         addToTranscript(wikipediaService.generateRace(strDescription, ENEColoursEnvironment.DEFAULT_LANGUAGE, "\r\n"));
    }
    private void generateRace(ENEStatement statement, int nRace) {
         addToTranscript(wikipediaService.generateRace(nRace, "SF", ENEColoursEnvironment.DEFAULT_LANGUAGE, "\r\n"));   // Assume Smartform
    }
    private void generateRace(ENEStatement statement, String strDescription, int nYear) {
         addToTranscript(wikipediaService.generateRace(strDescription, nYear, ENEColoursEnvironment.DEFAULT_LANGUAGE, "\r\n"));
    }

    private void generateRace(ENEStatement statement, int nRace, String strDescription) {
         addToTranscript(wikipediaService.generateRace(nRace, strDescription, ENEColoursEnvironment.DEFAULT_LANGUAGE, "\r\n"));
    }
 }
