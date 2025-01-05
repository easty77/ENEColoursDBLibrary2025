/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.geny;

import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Simon
 */
public class Geny {
    private static final String sm_strTodaysRacesURL="/courses";
    private static final String sm_strSiteURL = "http://www.geny.com";
    private static final String[] sm_astrCountries = {"afrique-du-sud", "allemagne", "australie", "autriche", "belgique", "cheltenham", "chili", "dubai", "emirats-arabes-unis", "espagne", "etats-unis", "fairyhouse", "grande-bretagne", "mons", "singapour", "suisse", "tasmanie", "uruguay"};
    private static final String[] sm_astrCountryCodes = {"SA", "GER", "AUS", "AUT", "BEL", "UK", "CHL", "UAE", "UAE", "SPA", "USA", "UK", "UK", "BEL", "SIN", "CHE", "AUS", "URY"};
    
    private static HashMap<String,HashMap<String,String>> m_hmCountryOwnerColours = new HashMap<String,HashMap<String,String>>();
     // Races today
    // 
    // <div class="nomReunion">	Vincennes&nbsp;(R1)</div>,  Vaal [Afrique du Sud]
    // all href starting /partants - retrieve country from URL?
    // all links beginning /cheval/ - associate with country of race
    
    // Horses running today
    // http://www.geny.com/cheval
    
    // all links beginning /cheval/
    
    // On horse page
    // 1) Image: <div class="yui-u first"><img src="http://media.geny.com/web/image/casaque/23/1105223_2.png" height="65" width="55" alt=""></div>

    // 2) Owner name: <div class="yui-gd"><div class="yui-u first"><em>Propriétaire&nbsp;:</em>&nbsp;</div>
	//<div class="yui-u">R S Napier, Mrs B A Napier</div>
    // </div></div>

    // 3) Location of today's race
    // <form name="newPartant" method="get" action="/cheval">
	//	<div style="width: 54.55em;">
	//	<div class="yui-g">
	//	<div class="yui-u first">jeudi : <strong>Vaal</strong><br>

   public static void loadResults(ENEStatement statement, String strStart)
    {
        // retreive from ARD
        try
        {
            TagNode rootNode = ExecuteURL.getRootNode(sm_strSiteURL + sm_strTodaysRacesURL, "utf-8");
        
             TagNode[] aMeetings = rootNode.getElementsByAttValue("class", "nomReunion", true, false);
            for(int i = 0; i < aMeetings.length; i++)
            {
                TagNode meeting = aMeetings[i];
                String strCountry = "France";
                String strMeeting = meeting.getText().toString().trim();
                strMeeting = strMeeting.substring(0, strMeeting.indexOf("\n"));      
                if (strMeeting.indexOf("[") > 0)
                {
                    strCountry = strMeeting.substring(strMeeting.indexOf("[") + 1);
                    strCountry = strCountry.substring(0, strCountry.length()- 1);
                    strMeeting = strMeeting.substring(0, strMeeting.indexOf("["));
                }
                System.out.println(strCountry + "-" + strMeeting);
            }
           
            String strPreviousCountry = null;
            int nRaceCount = 0;
            boolean bStarted = false;
            TagNode[] aNodes = rootNode.getElementsByName("a", true);
            for(int i = 0; i < aNodes.length; i++)      
            {
                TagNode a = aNodes[i];
                String strRaceURL = a.getAttributeByName("href");
                if (strRaceURL.indexOf("/partants") == 0)
                {
                    if (bStarted || (strStart == null) || (strRaceURL.indexOf(strStart) >= 0))  // start from a certain race in case of restart
                    {
                        bStarted = true;
                        System.out.println(strRaceURL);
                        String strCountry = getRaceCountry(strRaceURL);
                        if ((strPreviousCountry != null) && (!strPreviousCountry.equals(strCountry)))
                        {
                            writeToDatabase(statement);
                        }
                        strPreviousCountry = strCountry;
                        if (!"UK".equals(strCountry))    
                        {
                            loadGenyRace(statement, strCountry, strRaceURL);
                        }
                    }
                }
            } 

            writeToDatabase(statement);
         }
        catch(Exception e)
        {
            System.out.println("Geny.loadResults: " + e.getMessage());
            e.printStackTrace();
        }
    }
   private static void writeToDatabase(ENEStatement statement)
   {
    System.out.println("writeToDatabase");
     Set<String> countries = m_hmCountryOwnerColours.keySet();
     Iterator<String> iterc = countries.iterator();
     while(iterc.hasNext())
     {
         String strCountry = iterc.next();
         HashMap<String,String> hmOwnerColours = m_hmCountryOwnerColours.get(strCountry);
         Set<String> keys = hmOwnerColours.keySet();
         Iterator<String> iter = keys.iterator();
         while(iter.hasNext())
         {
             String strOwner = iter.next();
             String strImage = hmOwnerColours.get(strOwner);
             String strOwnerId = GenyFactory.selectGenyOwnerId(statement, strOwner);
             if (strOwnerId != null)
             {
                 if (!strOwnerId.equals(strImage))
                 {
                    System.out.println("Owner conflict:" + strCountry + ": " + strOwner + "-" + strOwnerId + "->" + strImage);
                 }
             }
             else
             {
                 String[] astrImage = strImage.split("_");
                 GenyFactory.insertGenyOwner(statement, strOwner, strCountry, Integer.valueOf(astrImage[0]), Integer.valueOf(astrImage[1]));
                 System.out.println("New owner:" + strCountry + ": " + strOwner + "-" + strImage);
             }
          }
     }
     m_hmCountryOwnerColours.clear();
   }
   private static void loadGenyRace(ENEStatement statement, String strCountry, String strRaceURL)
    {
        // sleep 1 second
        ExecuteURL.sleep(1);
        ArrayList<String> alHorses = new ArrayList<String>();
        try
        {
            TagNode rootNode = ExecuteURL.getRootNode(sm_strSiteURL + strRaceURL, "utf-8");
            if (rootNode == null)
                return;
            TagNode[] aNodes = rootNode.getElementsByName("a", true);
            for(int i = 0; i < aNodes.length; i++)
            {
                TagNode a = aNodes[i];
                String strHorseURL = a.getAttributeByName("href");
                if ((strHorseURL != null) && (strHorseURL.indexOf("/cheval/") == 0))
                {
                    // URL is of form /cheval/caduceus-des-baux-performance-2016-02-21-vincennes-vs-2016-03-03-vincennes_c783317_f785295_h2424247
                    // but seems that only the last bit (f785295_h2424247) is required, so remove this
                    int nHorseId = strHorseURL.lastIndexOf("_f");
                    if (nHorseId  < 0)
                        nHorseId = strHorseURL.lastIndexOf("_c");
                    if (nHorseId > 0)
                    {
                        String strHorseId = strHorseURL.substring(nHorseId+1);
                        //System.out.println(strHorseURL);
                        if (!alHorses.contains(strHorseId))
                        {
                            alHorses.add(strHorseId);
                            loadGenyHorse(statement, strCountry, "/cheval/" + strHorseId);
                        }
                    }
                    else
                        System.out.println("Invalid horse URL: " + strHorseURL);
                }
            } 
        }
        catch(Exception e)
        {
            System.out.println("Geny.loadRace: " + e.getMessage());
            e.printStackTrace();
        }
    }
   private static void loadGenyHorse(ENEStatement statement, String strCountry, String strHorseURL)
    {
        // sleep 1 second
        ExecuteURL.sleep(1);
        try
        {
            TagNode rootNode = ExecuteURL.getRootNode(sm_strSiteURL + strHorseURL, "utf-8");
        
            if (rootNode == null)
            {
                return;
            }
            System.out.println(strHorseURL);
            String strImage = "";
            TagNode[] aImages = rootNode.getElementsByName("img", true);
            for(int i = 0; i < aImages.length; i++)
            {
                String strSrc = aImages[i].getAttributeByName("src");
                if ((strSrc != null) && (strSrc.indexOf("/casaque/") > 0))
                {
                    strImage = strSrc;    
                    break;
                }
            }
                   
            String strOwner = "";        
            TagNode[] aNodes = rootNode.getElementsByAttValue("class", "yui-gd", true, false);
            for(int i = 0; i < aNodes.length; i++)
            {
                TagNode node = aNodes[i];
                TagNode[] aNodes1 = node.getElementsByAttValue("class", "yui-u", true, false);
                if (aNodes1.length >= 4)
                {
                    // 1= Jockey, 2 = Trainer, 3 = Owner
                    strOwner = aNodes1[3].getText().toString();
                }
            } 
            HashMap<String,String> hmOwnerColours = m_hmCountryOwnerColours.get(strCountry);
            if (hmOwnerColours == null)
            {
                hmOwnerColours = new HashMap<String,String>();
                m_hmCountryOwnerColours.put(strCountry, hmOwnerColours);
            }
            hmOwnerColours.put(convertOwnerName(strOwner), strImage.replace("http://media.geny.com/web/image/casaque/", "").replace(".png", "").substring(3));
            //System.out.println(strCountry + ": " + strOwner + "-" + strImage);
        }
        catch(Exception e)
        {
            System.out.println("Geny.loadHorse: " + e.getMessage());
            e.printStackTrace();
        }
    }

   private static String getRaceCountry(String strRaceURL)
   {
       for(int i = 0; i < sm_astrCountries.length; i++)
       {
           if (strRaceURL.indexOf("-" + sm_astrCountries[i] + "-") > 0)
               return sm_astrCountryCodes[i];
       }
       
       return "FR";
   }
   private static String convertOwnerName(String strOwnerName)
   {
       return  strOwnerName.replace("&amp;", "&").replace("&#039;", "'").replace('é', 'e').replace('è', 'e').replace('î', 'i').replace('ë', 'e').replace('ç', 'c').replace('ô', 'o').replace('ö', 'o').replace('ß', 'b').replace('ü', 'u');
   }
}

