/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.wikipedia;

import ene.eneform.colours.database.*;
import ene.eneform.colours.service.WikipediaService;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import ene.eneform.colours.bos.AdditionalRaceData;
import ene.eneform.colours.bos.AdditionalRaceWikipedia;
import ene.eneform.colours.bos.ENEColoursDBEnvironment;
import ene.eneform.colours.web.atr.AtTheRacesRacecards;
import ene.eneform.colours.web.rp.RacingPostCourse;
import ene.eneform.colours.web.rp.RacingPostRaceSummary;
import ene.eneform.colours.web.rp.RacingPostRacecards;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.smartform.bos.AdditionalRaceInstance;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.springframework.stereotype.Service;
import org.wikipedia.*;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Simon
 */
@Service
@RequiredArgsConstructor
public class WikipediaUpdate {
    private final WikipediaService wikipediaService;
private Pattern sm_refRP = Pattern.compile("\\{\\{Racing Post[a-z0-9\\|\\-\\s]+\\}\\}");
private Pattern sm_refRPDate = Pattern.compile("\\/[\\d]+\\-[\\d]+\\-[\\d]+\\/");
    private String sm_strUser = "JockeyColours";
    private String sm_strPassword = "thi1mat2";
    private String sm_strLicense = "{{self|cc-by-sa-4.0}}";
    private String[] sm_astrCategories = {"Racing silks"};
    private DateFormat sm_dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    //private String sm_strDirectory="D:\\Program Files\\apache-tomcat-7.0.30-windows-x64\\apache-tomcat-7.0.30\\webapps\\ROOT\\images\\colours\\svg\\wikipedia\\owners";
    
    public void updateWikipediaRaceRacingPostReferences(ENEStatement statement, List<AdditionalRaceData> lstARD, String strLanguage, boolean bUpdate) 
    {
        try
        {
            Wiki wiki = Wiki.newSession(strLanguage + ".wikipedia.org");
            wiki.login(sm_strUser, sm_strPassword);
           for(int i = 0; i < lstARD.size(); i++)
           {
               try
               {
                    updateWikipediaRaceRacingPostReferences(statement, wiki, lstARD.get(i), "en", bUpdate);
               }
               catch(Exception e)
               {
                   System.out.println("updateWikipediaRaceRacingPostReferences exception: " + lstARD.get(i).getName() + "-" + e.getMessage());
               }
               catch(UnknownError e)
               {
                   System.out.println("updateWikipediaRaceRacingPostReferences exception: " + lstARD.get(i).getName() + "-" + e.getMessage());
               }
           }
           wiki.logout();
        } 
        catch (IOException e) {
                System.out.append("updateWikipediaRace IOException: " + e.getMessage());
            } catch (FailedLoginException e) {
                System.out.append("updateWikipediaRace FailedLoginException: " + e.getMessage());
            }
    }
    private void updateWikipediaRaceRacingPostReferences(ENEStatement statement, Wiki wiki, AdditionalRaceData ard, String strLanguage, boolean bUpdate) 
            throws IOException, UnsupportedEncodingException, LoginException
    {
             String strPage = ard.getWikipedia(strLanguage).getWikipediaRef();
            if (!"".equals(strPage))
            {
                strPage = URLDecoder.decode(strPage, "utf-8");  // already encoded
                LinkedHashMap<String,String> hmSections = wiki.getSectionMap(strPage);
                Iterator<Map.Entry<String,String>> iter = hmSections.entrySet().iterator();
                int nSection = 0;
                while(iter.hasNext())
                {
                    nSection++;
                  Map.Entry<String,String> entry = iter.next();
                  if ("References".equals(entry.getValue()))
                  {
                        //nSection = Integer.parseInt(entry.getKey());
                      break;
                   }
                }
                // "User:JockeyColours/sandbox", 9
                String strContent = "";
                if (nSection > 0)
                    strContent = wiki.getSectionText(strPage, nSection);
                //System.out.println(strContent);
                Matcher m = sm_refRP.matcher(strContent);
                String strNewContent="";
                int nPreviousEnd = 0;
                while (m.find())
                {
                    int nStart = m.start();
                    int nEnd = m.end();
                    String strRef = strContent.substring(nStart, nEnd);
                    String strNewRef = strRef;
                    try
                    {
                        String[] astrRef = strRef.split("\\|");
                        String strDay="";
                        String strRaceID = astrRef[1];
                        int nRaceID = Integer.parseInt(strRaceID);
                        String strYear = astrRef[2];
                        int nYear = Integer.parseInt(strYear);
                        // Temporary
                        //if (nYear != 1998)
                        //    continue;
                        String strMonth = astrRef[3];
                        if (strMonth.length() == 1)
                            strMonth = "0" + strMonth;
                        strDay = astrRef[4].replace("}}", "");      // if only 5 params will have clocing }} included 
                        if (strDay.length() == 1)
                            strDay = "0" + strDay;
                        RacingPostRaceSummary summary = new RacingPostRaceSummary(nRaceID, ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(ard.getCourse(), ard.getRaceType()), 
                                            strYear + "-" + strMonth + "-" + strDay);
                        AdditionalRaceInstance arl = AdditionalRaceLinkFactory.getAdditionalRaceLink(statement, ard.getName(), nRaceID, "RP");
                        if (arl == null)
                        {
                            arl = AdditionalRaceLinkFactory.getYearAdditionalRaceLink(statement, ard.getName(), nYear);
                            if ((arl != null)  && "SL".equals(arl.getSource()))
                            {
                                // hope SL name is same as RP
                                summary.setCourse(ENEColoursDBEnvironment.getInstance().getRPCourseByName(arl.getCourse(), arl.getRaceType()));
                                System.out.println("SL remove");
                                arl = null;         // Will remove all Sporting Life references
                            }
                            else if (arl == null)
                            {
                                arl  = AdditionalRaceLinkFactory.getAdditionalRaceLinkObject(statement, nRaceID, "RP");
                                if (arl != null)
                                {
                                    // get object, just to find course
                                    summary.setCourse(ENEColoursDBEnvironment.getInstance().getRPCourseByName(arl.getCourse(), arl.getRaceType()));
                                    arl = null;
                                }
                            }
                        }
                        boolean bNew = false;
                        if (arl == null)
                        {
                            System.out.println("Race not found: " + ard.getName() + "-" + nYear);
                            if (astrRef.length > 6)
                                summary.setCourse(ENEColoursDBEnvironment.getInstance().getRPCourse(astrRef[6].replace("}}", "")));
                            RacingPostRacecards.insertFullRaceResult(statement, summary, ard, true, false, true);
                            arl = AdditionalRaceLinkFactory.getAdditionalRaceLink(statement, ard.getName(), nRaceID, "RP");
                            bNew = true;
                        }
                        if (arl != null)
                        {
                            String strSource = arl.getSource();
                            if ("RP".equals(strSource))
                                nRaceID = arl.getRaceId();      // race id in current reference may be wrong!
                            summary.setCourse(
                                ("SF".equals(strSource)) ? ENEColoursDBEnvironment.getInstance().getRPCourseBySFName(arl.getCourse(), arl.getRaceType())
                                : ENEColoursDBEnvironment.getInstance().getRPCourseByName(arl.getCourse(), arl.getRaceType()));
                            if (("RP".equals(strSource) && (nYear >= 2000))
                                            || ("SF".equals(strSource) && (nYear <= 2005)))
                            {
                                String strScheduledTime = arl.getScheduledTime();
                                if (strScheduledTime != null)
                                {
                                    // from database
                                    String[] aScheduled = strScheduledTime.split("\\.");
                                    int nHours = Integer.parseInt(aScheduled[0]);
                                    if (nHours < 12)
                                        nHours += 12;
                                    int nMins = Integer.parseInt(aScheduled[1]);
                                    strScheduledTime = String.format("%02d", nHours) + String.format("%02d", nMins);
                                }
                                else
                                {
                                    strScheduledTime = summary.parseScheduledTime();    // 12 hour clock
                                    String[] aScheduled = strScheduledTime.split(":");
                                    int nHours = Integer.parseInt(aScheduled[0]);
                                    if (nHours < 12)
                                        nHours += 12;
                                    int nMins = Integer.parseInt(aScheduled[1]);
                                    strScheduledTime = String.format("%02d", nHours) + String.format("%02d", nMins);
                                }
                                if (!bNew)    
                                    AtTheRacesRacecards.loadRacecard(statement, summary.getCourse().getATRName(), summary.getCourse().getCountry(), summary.getATRDate(), strScheduledTime, arl.getRaceId(), arl.getSource(), null, true);
                            }
                        }
                        
                        // referenced that we have calculated
                        //strNewRef = astrRef[0] + "|" + astrRef[1] + "|" + strYear + "|" + strMonth + "|" + strDay + "|" + course.getNumber() + "|" + course.getCode() + "}}";
                        strNewRef = summary.getWikipediaReference();

                        if (!summary.checkURL())
                            System.out.println("ERROR; Invalid Racing Post Reference: " + ard.getName() + "-" + strRef);
                    }
                    catch(Exception e)
                    {
                        strNewRef = strRef;     // something has gone wrong so leave unchanged
                        System.out.println("ERROR! Racing Post Reference Exception: " + strRef + "-" + e.getMessage());
                    }
                    strNewContent += (strContent.substring(nPreviousEnd, nStart) + strNewRef);
                    nPreviousEnd = nEnd;
                }
                strNewContent += (strContent.substring(nPreviousEnd));
                

                if (bUpdate && (!strNewContent.equals(strContent)))
                {
                    System.out.println ("Updating: " + ard.getWikipedia(strLanguage).getWikipediaRef());
                    wiki.edit(strPage, strNewContent, "Jockey Colours API", nSection);

                    ExecuteURL.openBrowser("http://" + strLanguage + ".wikipedia.org/wiki/" + strPage.replace(" ", "_"));
                } 
            }
     }
    public void checkWikipediaRaceRacingPostReferences(ENEStatement statement, List<AdditionalRaceData> lstARD, String strLanguage, RacingPostCourse course, boolean bUpdate) 
    {
        try
        {
            Wiki wiki = Wiki.newSession(strLanguage + ".wikipedia.org");
            wiki.login(sm_strUser, sm_strPassword);
           for(int i = 0; i < lstARD.size(); i++)
           {
               try
               {
                 checkWikipediaRaceRacingPostReferences(statement, wiki, lstARD.get(i), "en",  course, bUpdate);
               }
               catch(Exception e)
               {
                   System.out.println("checkWikipediaRaceRacingPostReferences exception: " + lstARD.get(i).getName() + "-" + e.getMessage());
                   e.printStackTrace();
               }
               catch(UnknownError e)
               {
                   System.out.println("checkWikipediaRaceRacingPostReferences exception: " + lstARD.get(i).getName() + "-" + e.getMessage());
               }
           }
           wiki.logout();
        } 
        catch (IOException e) {
                System.out.append("updateWikipediaRace IOException: " + e.getMessage());
            } catch (FailedLoginException e) {
                System.out.append("updateWikipediaRace FailedLoginException: " + e.getMessage());
            } 
    }
    public void checkWikipediaRaceRacingPostReferences(ENEStatement statement, Wiki wiki, AdditionalRaceData ard, String strLanguage, RacingPostCourse course, boolean bUpdate) 
            throws IOException, UnsupportedEncodingException, LoginException
    {
        String strPage = ard.getWikipedia(strLanguage).getWikipediaRef();
        HashMap<Integer, String> hmWinners = WikipediaQuery.getWikipediaWinners("en", ard);    
        if (!"".equals(strPage))
        {
            strPage = URLDecoder.decode(strPage, "utf-8");  // already encoded
            LinkedHashMap<String,String> hmSections = wiki.getSectionMap(strPage);
            Iterator<Map.Entry<String,String>> iter = hmSections.entrySet().iterator();
            int nSection = 0;
            while(iter.hasNext())
            {
                nSection++;
              Map.Entry<String,String> entry = iter.next();
              if ("References".equals(entry.getValue()))
              {
                    //nSection = Integer.parseInt(entry.getKey());
                  break;
               }
            }
            // "User:JockeyColours/sandbox", 9
            String strContent = "";
            if (nSection > 0)
                strContent = wiki.getSectionText(strPage, nSection);
            //System.out.println(strContent);
            Matcher m = sm_refRP.matcher(strContent);
            int nReferenceCount = 0;
            String strNewContent="";
            int nPreviousEnd = 0;
            while (m.find())
            {
                nReferenceCount++;
                int nStart = m.start();
                int nEnd = m.end();
                String strRef = strContent.substring(nStart, nEnd);
                String strNewRef = strRef;
                RacingPostRaceSummary summary = RacingPostRaceSummary.createSummaryWikipedia(strRef);
                if (!summary.checkURL())
                {
                    System.out.println("ERROR; Invalid Racing Post Reference: " + ard.getName() + "-" + strRef);
                    RacingPostRaceSummary summary1 = WikipediaQuery.getRacingPostReferenceWinner(ard, summary.getYear(), summary.getRPDate(), hmWinners, course);
                    boolean bNewCheck = false;
                    if (summary1 != null)
                    {
                        bNewCheck = summary1.checkURL();
                        if (bNewCheck)
                        {
                            System.out.println("Validated URL: " + summary1.getWikipediaReference());
                            strNewRef = summary1.getWikipediaReference();
                        }
                        else
                        {
                            System.out.println("GENERATED URL is invalid: " + summary1.getWikipediaReference() + "-" + ard.getName());
                            strNewRef = strRef;
                        }
                    }
                    else
                    {
                        System.out.println("UNABLE TO GENERATE URL is invalid: " + ard.getName() + "-" + strRef);
                        strNewRef = strRef;
                    }
                    if (!bNewCheck && (summary.getCourse() == null) && (ard != null))
                    {
                        System.out.println("TRYING DEFAULT COURSE: " + ard.getName() + "-" + strRef);
                        // only course was missing?
                        if (course == null)
                            course = ENEColoursDBEnvironment.getInstance().getRPCourseByName(ard.getCourse(), ard.getRaceType());
                        summary.setCourse(course);
                        if (summary.checkURL())
                        {
                            strNewRef = summary.getWikipediaReference();
                            System.out.println("VALIDATED DEFAULT COURSE: " + ard.getName() + "-" + strNewRef);
                        }
                    }
                }
                strNewContent += (strContent.substring(nPreviousEnd, nStart) + strNewRef);
                nPreviousEnd = nEnd;
            }
            strNewContent += (strContent.substring(nPreviousEnd));
            System.out.println("Racing Post Reference: " + ard.getName() + "-" + nReferenceCount);
            //System.out.println(strNewContent);
            if (!strNewContent.equals(strContent))
            {
                if (bUpdate)
                {
                    System.out.println ("Updating: " + ard.getWikipedia(strLanguage).getWikipediaRef());
                    wiki.edit(strPage, strNewContent, "Jockey Colours API", nSection);

                    ExecuteURL.openBrowser("http://" + strLanguage + ".wikipedia.org/wiki/" + strPage.replace(" ", "_"));
                }
            } 
            else
            {
                System.out.println("Content unchanged: " + ard.getName());
            }
        }
    }
    public void retrieveWikipediaRaceRacingPostReferences(ENEStatement statement, List<AdditionalRaceData> lstARD, String strLanguage) 
    {
        try
        {
            Wiki wiki = Wiki.newSession(strLanguage + ".wikipedia.org");
            wiki.login(sm_strUser, sm_strPassword);
           for(int i = 0; i < lstARD.size(); i++)
           {
               try
               {
                 retrieveWikipediaRaceRacingPostReferences(statement, wiki, lstARD.get(i), "en");
               }
               catch(Exception e)
               {
                   System.out.println("retrieveWikipediaRaceRacingPostReferences exception: " + lstARD.get(i).getName() + "-" + e.getMessage());
                   e.printStackTrace();
               }
               catch(UnknownError e)
               {
                   System.out.println("retrieveWikipediaRaceRacingPostReferences exception: " + lstARD.get(i).getName() + "-" + e.getMessage());
               }
           }
           wiki.logout();
        } 
        catch (IOException e) {
                System.out.append("retrieveWikipediaRace IOException: " + e.getMessage());
            } catch (FailedLoginException e) {
                System.out.append("retrieveWikipediaRace FailedLoginException: " + e.getMessage());
            } 
    }
    public void retrieveWikipediaRaceRacingPostReferences(ENEStatement statement, Wiki wiki, AdditionalRaceData ard, String strLanguage) 
            throws IOException, UnsupportedEncodingException, LoginException, ParseException
    {
            String strPage = ard.getWikipedia(strLanguage).getWikipediaRef();
            if (!"".equals(strPage))
            {
                strPage = URLDecoder.decode(strPage, "utf-8");  // already encoded
                LinkedHashMap<String,String> hmSections = wiki.getSectionMap(strPage);
                Iterator<Map.Entry<String,String>> iter = hmSections.entrySet().iterator();
                int nSection = 0;
                while(iter.hasNext())
                {
                    nSection++;
                  Map.Entry<String,String> entry = iter.next();
                  if ("References".equals(entry.getValue()))
                  {
                        //nSection = Integer.parseInt(entry.getKey());
                      break;
                   }
                }
                // "User:JockeyColours/sandbox", 9
                String strContent = "";
                if (nSection > 0)
                    strContent = wiki.getSectionText(strPage, nSection);
                //System.out.println(strContent);
                Matcher m = sm_refRP.matcher(strContent);
                while (m.find())
                {
                    int nStart = m.start();
                    int nEnd = m.end();
                    String strRef = strContent.substring(nStart, nEnd);
                    RacingPostRaceSummary summary = RacingPostRaceSummary.createSummaryWikipedia(strRef);
                    String strCountry = ard.getCountry();
                    int nYear = summary.getYear();
                    if ( ((!"England".equals(strCountry)) && (!"Scotland".equals(strCountry)) && (!"Wales".equals(strCountry)) && (!"Eire".equals(strCountry))&& (!"Northern ireland".equals(strCountry)))
                            || (nYear < 2003) )
                    {
                        try
                        {
                            if (summary.checkURL())
                            {
                                // Just for now - only get race details, not runners
                                RacingPostRacecards.insertFullRaceResult(statement, summary, ard, true, false, true);
                            }
                            else
                                System.out.println("retrieveWikipediaRaceRacingPostReferences Invalud Reference: " + ard.getName() + "-" + strRef);
                        }
                        catch(Exception e)
                        {
                            System.out.println("retrieveWikipediaRaceRacingPostReferences exception: " + ard.getName() + "-" + nYear);
                            e.printStackTrace();
                        }
                    }
                }
            }
    }
    public String updateWikipediaRace(ENEStatement statement, AdditionalRaceData ard, String strLanguage, boolean bUpdate) 
    {
        if (bUpdate)
        {
            return updateWikipediaRaceURL(statement, ard, strLanguage);
        }
        else    // just generate content (for testing)
        {
            System.out.println(wikipediaService.generateRace(ard.getName(), ENEColoursEnvironment.DEFAULT_LANGUAGE, "\n"));
            return null;    // no updated page
        }
    
    }
    public JSONObject updateWikipediaRaceURL(ENEStatement statement, JSONObject obj, boolean bZipped) {
        String strReturnURL = null;
        String strPage = obj.get("url").toString();
        String strLanguage = obj.get("language").toString();
        try {
            Wiki wiki = Wiki.newSession(strLanguage + ".wikipedia.org");
            
            if (!"".equals(strPage))
            {
                 strPage = URLDecoder.decode(strPage, "utf-8");  // already encoded
                //wiki.setUsingCompressedRequests(bZipped);
                wiki.login(sm_strUser, sm_strPassword);
                System.out.println("Wikipedia logged in");

                int nSection = 0;
                // "User:JockeyColours/sandbox", 9
                String strContent;
                if (nSection > 0)
                    strContent = wiki.getSectionText(strPage, nSection);
                else
                {
                   List<String> lstTitles = new ArrayList<String>();
                   lstTitles.add(strPage);
                   List<String> lstPages = wiki.getPageText(lstTitles);
                   strContent = lstPages.get(nSection);
                }

                //System.out.println(strContent);
                obj = insertNewJockeyColoursRace(statement, obj, strContent);
                String strNewContent = (String) obj.get("content");
                //System.out.println(strNewContent);
                if (strNewContent != null)
                {
                    if (nSection > 0)
                        wiki.edit(strPage, strNewContent, "Jockey Colours API", nSection);
                    else
                        wiki.edit(strPage, strNewContent, "Jockey Colours API");

                    //ExecuteURL.openBrowser("http://" + strLanguage + ".wikipedia.org/wiki/" + strPage.replace(" ", "_"));
                    strReturnURL = "http://" + strLanguage + ".wikipedia.org/wiki/" + strPage.replace(" ", "_");
                }
                wiki.logout();
            }
            } catch (IOException e) {
                obj.put("error", "updateWikipediaRace IOException: " + e.getMessage());
                e.printStackTrace(System.out);
            } catch (FailedLoginException e) {
                obj.put("error", "updateWikipediaRace FailedLoginException: " + e.getMessage());
            } catch (LoginException e) {
                obj.put("error", "updateWikipediaRace LoginException: " + e.getMessage());
            } catch (Exception e) {
                obj.put("error", "updateWikipediaRace Exception: " + e.getMessage());
            }
        if (strReturnURL != null)
            obj.put("return_url", strReturnURL);
        
        return obj;
        
    }
    private String updateWikipediaRaceURL(ENEStatement statement, AdditionalRaceData ard, String strLanguage) {
        String strReturnURL = null;
        try {
            Wiki wiki = Wiki.newSession(strLanguage + ".wikipedia.org");
            wiki.setResolveRedirects(true);
            AdditionalRaceWikipedia arw = ard.getWikipedia(strLanguage);
            if (arw == null)
                return strReturnURL;
            
            String strPage = arw.getWikipediaRef();
            if (!"".equals(strPage))
            {
                strPage = URLDecoder.decode(strPage, "utf-8");  // already encoded
                wiki.login(sm_strUser, sm_strPassword);

                int nSection = 0;
                // "User:JockeyColours/sandbox", 9
                String strContent;
                if (nSection > 0)
                    strContent = wiki.getSectionText(strPage, nSection);
                else
                {
                   List<String> lstTitles = new ArrayList<String>();
                   lstTitles.add(strPage);
                   List<String> lstPages = wiki.getPageText(lstTitles);
                   strContent = lstPages.get(nSection);
                }

                //System.out.println(strContent);
                String strNewContent = insertNewJockeyColoursRace(statement, ard, strContent);

                if (strNewContent != null)
                {
                    if (nSection > 0)
                        wiki.edit(strPage, strNewContent, "Jockey Colours API", nSection);
                    else
                        wiki.edit(strPage, strNewContent, "Jockey Colours API");

                    //ExecuteURL.openBrowser("http://" + strLanguage + ".wikipedia.org/wiki/" + strPage.replace(" ", "_"));
                    strReturnURL = "http://" + strLanguage + ".wikipedia.org/wiki/" + strPage.replace(" ", "_");
                }
                wiki.logout();
            }
            } catch (IOException e) {
                System.out.append("updateWikipediaRace IOException: " + e.getMessage());
            } catch (FailedLoginException e) {
                System.out.append("updateWikipediaRace FailedLoginException: " + e.getMessage());
            } catch (LoginException e) {
                System.out.append("updateWikipediaRace LoginException: " + e.getMessage());
            }
        
        return strReturnURL;
    }
    private JSONObject insertNewJockeyColoursRace(ENEStatement statement, JSONObject obj, String strContent)
    {
            //System.out.println(strSection);
            // HTMLCleaner is no use as simply stored as text
            int nJockeyColoursRowStart = strContent.indexOf("\n{{Jockey colours row");
            if (nJockeyColoursRowStart >= 0)
            {
                int nJockeyColoursRowEnd = strContent.substring(nJockeyColoursRowStart).indexOf("\n}}");

                String strHeader = strContent.substring(0, nJockeyColoursRowStart);
                String strRace = strContent.substring(nJockeyColoursRowStart, nJockeyColoursRowStart + nJockeyColoursRowEnd + 3);
                //System.out.println("race: " + strRace);

                int nCollapsible = strContent.indexOf("\n|}\n{{Jockey colours collapsible header}}");
                if (nCollapsible >= 0)
                {
                    String strNewRace = wikipediaService.generateRace(obj, ENEColoursEnvironment.DEFAULT_LANGUAGE, "\n");
                    strNewRace = strNewRace.substring(0, strNewRace.length() - 1);
                    // 20210820 - only check first 34 chars i.e. up to "year = 2021"
                    if (strContent.indexOf(strNewRace.substring(0, 34)) < 0)
                    {
                        //System.out.println("new race: " + strNewRace);
                        String strCollapsible = strContent.substring(nCollapsible, nCollapsible + 41);
                        String strLastDecade = "{{Jockey colours no footer}}\n{{Jockey colours named collapsible header\n| name = 2020-2011 }}";
                        String strRest = strContent.substring(nCollapsible + 42);
                        if (strRace.indexOf("2021") <= 0)
                        {
                            strCollapsible = "";    // no Previous Races section if no race in 2021
                        }
                        String strNewContent = strHeader + "\n" + strNewRace + strCollapsible + strRace + "\n"+ strLastDecade + "\n" + strRest;

                        obj.put("content", strNewContent);
                    }
                    else
                    {
                        if (strContent.indexOf(strNewRace) < 0)
                            obj.put("error", "Different content already present-" + obj.get("name"));
                        else
                            obj.put("error", "Identical content already present-" + obj.get("name"));
                    }
                }
            }
            else
            {
                obj.put("error", "JockeyColours content not found");
            }
            
            return obj;
    }
    private String insertNewJockeyColoursRace(ENEStatement statement, AdditionalRaceData ard, String strContent)
    {
            //System.out.println(strSection);
            // HTMLCleaner is no use as simply stored as text
            int nJockeyColoursRowStart = strContent.indexOf("\n{{Jockey colours row");
            if (nJockeyColoursRowStart >= 0)
            {
                int nJockeyColoursRowEnd = strContent.substring(nJockeyColoursRowStart).indexOf("\n}}");

                String strHeader = strContent.substring(0, nJockeyColoursRowStart);
                String strRace = strContent.substring(nJockeyColoursRowStart, nJockeyColoursRowStart + nJockeyColoursRowEnd + 3);
                //System.out.println("race: " + strRace);

                int nCollapsible = strContent.indexOf("\n|}\n{{Jockey colours collapsible header}}");
                if (nCollapsible >= 0)
                {
                    String strCollapsible = strContent.substring(nCollapsible, nCollapsible + 41);
                    String strRest = strContent.substring(nCollapsible + 42);
                    String strNewRace = wikipediaService.generateRace(ard.getName(), ENEColoursEnvironment.DEFAULT_LANGUAGE, "\n");
                    strNewRace = strNewRace.substring(0, strNewRace.length() - 1);
                    //System.out.println("new race: " + strNewRace);

                    String strNewContent = strHeader + "\n" + strNewRace + strCollapsible + strRace + "\n" + strRest;

                    return strNewContent;
                }
            }
            
            return null;
    }
   public void uploadWikimediaImage(String strOwner) {
       String[] astrOwners = new String[1];
       astrOwners[0] = strOwner;
       uploadWikimediaImages(astrOwners);
   }
   public String uploadWikimediaImagesDate(ENEStatement statement, int nDayOffset, String strStartTime)
   {
        ArrayList<String> alOwners = WikipediaImagesFactory.selectWikipediaOwnersByDate(statement, nDayOffset, strStartTime);
        String strContent = alOwners.size() + " owners";
        String astrOwners[]={};
        strContent += uploadWikimediaImages(alOwners.toArray(astrOwners));
       return strContent;
   }
   public String uploadWikimediaImagesLatest(ENEStatement statement)
   {
       String strTimestamp = JCEventsFactory.getEventTimestamp(statement, "latest_wikipedia_images");
        ArrayList<String> alOwners = WikipediaImagesFactory.selectWikipediaOwnersLatest(statement, strTimestamp);
        String strContent = alOwners.size() + " owners";
        String astrOwners[]={};
        strContent += uploadWikimediaImages(alOwners.toArray(astrOwners));
       return strContent;
   }
   public JSONObject updateWikipediaRacesDate(ENEStatement statement, int nDayOffset, String strWhere, boolean bUpdate)
   {
       JSONObject obj = new JSONObject();
       JSONArray pages = new JSONArray();
       if (nDayOffset <= 0)     // must be in the past
           return obj;
       
       String strLanguage="en";
        ArrayList<String> alRaces = WikipediaFactory.selectWikipediaRacesByDate(statement, nDayOffset, strWhere, strLanguage);
        for(int i = 0; i < alRaces.size(); i++)
        {
            AdditionalRaceData ard = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(alRaces.get(i));
            if (ard != null)
            {
                String strReturnURL = updateWikipediaRace(statement, ard, strLanguage, bUpdate);
                if (strReturnURL != null)
                    pages.put(strReturnURL);
                else if (!bUpdate) // just generate content (for testing)
                    System.out.println(wikipediaService.generateRace(ard.getName(), ENEColoursEnvironment.DEFAULT_LANGUAGE, "\n"));
            }
        }
        strLanguage="fr";
        alRaces = WikipediaFactory.selectWikipediaRacesByDate(statement, nDayOffset, strWhere, strLanguage);
        for(int i = 0; i < alRaces.size(); i++)
        {
            AdditionalRaceData ard = ENEColoursDBEnvironment.getInstance().getAdditionalRaceData(alRaces.get(i));
            if (ard != null)
            {
                    String strReturnURL = updateWikipediaRace(statement, ard, strLanguage, bUpdate);
                    if (strReturnURL != null)
                        pages.put(strReturnURL);
            }
        }
        if (pages.length() > 0)
        {
            obj.put("pages", pages);
        }
        return obj;
   }
   public String uploadWikimediaImages(String[] astrOwners) {
       String strContent = "";
        try {
            Wiki wiki = Wiki.newSession("commons.wikimedia.org");
            //wiki.setUsingCompressedRequests(false);        // suddenly seems necessary as of 1st April 2021!!
            wiki.login(sm_strUser, sm_strPassword);
            System.out.println("uploadWikimediaImages logged in");
            for(int i = 0; i < astrOwners.length; i++)
            {
                strContent += astrOwners[i] + "\n";
                String strFileName = "owner_" + astrOwners[i] + ".svg";
                String strDirectory = ENEColoursEnvironment.getInstance().getVariable("SVG_OUTPUT_DIRECTORY") + ENEColoursEnvironment.getInstance().getVariable("SVG_IMAGE_PATH") + "wikipedia/owners/";
                File file = new File(strDirectory + strFileName);
                Calendar cal = Calendar.getInstance();
                String strContent1 = getUploadTextCommons(strFileName, sm_strUser, sm_dateFormat.format(cal.getTime()));
                String strReason = "JockeyColours API";
                System.out.println("uploadWikimediaImages: " + strFileName + "-" + strContent1 + "-" + strReason);
                wiki.upload(file, strFileName, strContent1, strReason);
            }
            wiki.logout();
        } catch (IOException e) {
            System.out.append("uploadWikimediaImages IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (FailedLoginException e) {
            System.out.append("uploadWikimediaImages FailedLoginException: " + e.getMessage());
        } catch (LoginException e) {
            System.out.append("uploadWikimediaImages LoginException: " + e.getMessage());
        }
        return strContent;
    }

public String getUploadTextCommons(String strDescription, String strAuthor, String strDate) {
      String desc = "=={{int:filedesc}}==\n{{Information"
          + "\n|description = " + strDescription
          + "\n|date = " + strDate;

      desc += "\n|source = {{own}}\n|author = [[user:" + strAuthor + "|]]";

      desc += "\n|permission = \n|other_versions = \n}}";

      desc += "\n\n=={{int:license-header}}==\n";

      desc += sm_strLicense;

      desc += getUploadCategories(sm_astrCategories); //categories
      return desc;
    }

    public String getUploadCategories(String[] astrCategories) {
        String strCategories = "";
        for(int j=0;j<astrCategories.length;++j) {
                strCategories += "[[Category:" + astrCategories[j] + "]]\n";
            }
        
        return strCategories;
    }

}
