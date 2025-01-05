/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.sl;

import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Simon
 */
public class SportingLifeOwners {
    
    private static String sm_strBaseURL="http://www.sportinglife.com";
    private static String sm_strRacecardsURL="http://www.sportinglife.com/racing/racecards/";
    private static DateFormat sm_SLformatter = new SimpleDateFormat("dd-MM-yyyy");
    private static DateFormat sm_ISOformatter = new SimpleDateFormat("yyyy-MM-dd");
    
    private static final String[] sm_astrIgnoreCourses = {"will rogers downs"};     // wear red, blue etc rather than owners' colours
    public static void loadDays(ENEStatement statement, String strDate, int nDays, boolean bUK, boolean bOverseas)
    {
        try
        {
            Date date = sm_ISOformatter.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            
            for (int i = 0; i < nDays; i++)
            {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                loadDayImpl(statement, calendar, bUK, bOverseas);
            }
        }
        catch(Exception e)
        {
            
        }
        
    }
    public static void loadDay(ENEStatement statement, String strDate, boolean bUK, boolean bOverseas)
    {
        try
        {
            Date date = sm_ISOformatter.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            loadDayImpl(statement, calendar, bUK, bOverseas);
        }
        catch(Exception e)
        {
            
        }
    }
    private static void loadDayImpl(ENEStatement statement, Calendar calendar, boolean bUK, boolean bOverseas)
    {
        Date date = calendar.getTime();        
        try
        {
            if (date.compareTo(sm_ISOformatter.parse("2008-03-01")) >= 0)            
                bUK = false;
            String strDate = sm_SLformatter.format(date);
            TagNode rootNode = ExecuteURL.getRootNode(sm_strRacecardsURL + strDate, "utf-8");
        
            TagNode[] aRaceURLs = rootNode.getElementsByAttValue("class", "ixa", true, false);
            String strCourse = "";
            String strCountry = "";
            boolean bProcess = false;
            for(int i = 0; i < aRaceURLs.length; i++)
            {
                TagNode a = aRaceURLs[i];
                String strRaceURL = a.getAttributeByName("href");
                if (strRaceURL.indexOf("/meeting/") > 0)
                {
                    // /racing/meeting/cheltenham/15-03-2016
                    strRaceURL = strRaceURL.substring(16);
                    strCourse = strRaceURL.substring(0, strRaceURL.indexOf("/"));
                    strCourse = strCourse.replace("-", " ");
                    strCountry = SportingLifeFactory.getSLCourseCountry(statement, strCourse); 
                     boolean bCountryUK = false;
                    
                    if (strCountry != null)
                    {
                        if (strCountry.equals("UK") || strCountry.equals("IRE") || strCountry.equals("NI"))
                            bCountryUK = true;
                        if (bUK && bCountryUK)
                            bProcess = true;
                        else if (ignoreCourse(strCourse))   // overseas course where wear standard colours
                            bProcess = false;
                        else if ((!bCountryUK) && bOverseas)
                            bProcess = true;
                        else
                            bProcess = false;
                    }
                    else
                    {
                        System.out.println("Unknown course: " + strCourse);
                        bProcess = false;
                    }
                }

                if (bProcess && (strRaceURL.indexOf("/racecard/") > 0))
                {
                    System.out.println(strRaceURL);
                    loadRacecard(statement, sm_strBaseURL + strRaceURL, strCountry, calendar.get(Calendar.YEAR));
                }
            } 
        }
        catch(Exception e)
        {
            System.out.println("Geny.loadResults: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void loadRacecard(ENEStatement statement, String strRacecardURL, String strCountry, int nYear)
    {
        // sleep 1 second
        ExecuteURL.sleep(1);
        try
        {
            TagNode rootNode = ExecuteURL.getRootNode(strRacecardURL, "utf-8");
        
            if (rootNode == null)
                return;
            
            TagNode[] aHorses = rootNode.getElementsByAttValue("class", "horse-dtl", true, false);
            for(int i = 0; i < aHorses.length; i++)
            {
                TagNode horse = aHorses[i];
                TagNode[] aImages = horse.getElementsByName("img", true);
                if (aImages.length > 0)
                {
                    String strImage = aImages[0].getAttributeByName("src");
                    strImage = strImage.replace("http://images.sportinglife.com/racing/tsilks/", "").replace(".png", "");
                    String strDescription = aImages[0].getAttributeByName("alt");

                    TagNode[] aA = horse.getElementsByAttValue("class", "enable-tooltip", true, true);
                    if (aA.length > 0)
                    {
                        String strOwner = aA[0].getAttributeByName("title");
                        int nOwnerLocation = strOwner.indexOf(" Owned by ");
                        if (nOwnerLocation >= 0)
                        {
                            strOwner = strOwner.substring(nOwnerLocation + 10);
                            if ((!strDescription.equals("RED")) && (!strDescription.equals("White")) && (!strDescription.equals("Blue"))
                                    && (!strDescription.equals("Yellow")) && (!strDescription.equals("Green")) && (!strDescription.equals("Black"))
                                    && (!strDescription.equals("Orange")) && (!strDescription.equals("Pink"))
                                    && (!"My Stable".equalsIgnoreCase(strDescription)))
                            {
                                strDescription = strDescription.replace(" & ", " and ");
                                                                int nImage = 0;
                                try
                                {
                                    nImage = Integer.valueOf(strImage);
                                }
                                catch(NumberFormatException e)
                                {
                                    // nImage = 0
                                    System.out.println("Invalid image for: " + strOwner);
                                }
                                loadColoursDescription(statement, strDescription, strOwner, strCountry, nImage, nYear);
                                // negative value means owner+colour already exist, so no need to add (but always add for year)
                            }
                         }
                    }
                }
            }
        }
        catch(Exception e) 
        {
            System.out.println("Geny.loadRacecard: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static int loadColoursDescription(ENEStatement statement, String strDescription, String strOwner, String strCountry, int nImage, int nYear)
    {
        System.out.print(strCountry + ":" + strOwner + "-" + strDescription + "-" + nImage + ": ");
        int nOwner = SportingLifeFactory.matchSportingLifeOwner(statement, strOwner, strCountry, strDescription, nYear, nImage);
        // nOwner = 0 means nothing to do (owner and year already defined)
        if (nOwner > 0)
        {
            // insert year
            SportingLifeFactory.insertSportingLifeOwnerYear(statement, strOwner, nOwner, strCountry, nYear);
            System.out.println("Exact match year");
            return 1;
        }
        else if (nOwner < 0)
        {
            // not found
            nOwner = SportingLifeFactory.nomatchSportingLifeOwner(statement, strOwner, strCountry, strDescription, nYear, nImage);
            // nOwner = 0 means nothing to do (owner and year already defined)
            if (nOwner > 0)
            {
                // insert owner and year
                SportingLifeFactory.insertSportingLifeOwner(statement, strOwner, nOwner, strCountry, nImage, strDescription);
                SportingLifeFactory.insertSportingLifeOwnerYear(statement, strOwner, nOwner, strCountry, nYear);
                System.out.println("No match");
                return 2;
            }
            else if (nOwner < 0)
            {
                // insert owner and year
                nOwner = -nOwner;
                SportingLifeFactory.insertSportingLifeOwnerYear(statement, strOwner, nOwner, strCountry, nYear);
                System.out.println("Inexact match year");
                return 1;
            }
            else
            {
                System.out.println("Inexact match");
                return -1;
            }
        }
        else
        {
            System.out.println("Exact match");
            return 0;
        }
    }
     private static boolean ignoreCourse(String strCourse)
     {
         for(int i = 0; i < sm_astrIgnoreCourses.length; i++)
         {
             if (sm_astrIgnoreCourses[i].equals(strCourse))
                 return true;
         }
         
         return false;
     }
}
