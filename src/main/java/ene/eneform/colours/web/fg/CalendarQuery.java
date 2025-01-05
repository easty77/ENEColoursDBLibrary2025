/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.fg;

import ene.eneform.colours.database.AdditionalRacesFactory;
import ene.eneform.smartform.bos.AdditionalRace;
import ene.eneform.utils.DateUtils;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import jakarta.xml.soap.SOAPException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author Simon
 */
public class CalendarQuery  extends FranceGalopQuery{
 private static String sm_strCalendarURL = "http://www9.france-galop.com/fgweb/domaines/courses/courses_calendrier.aspx";
 
 private final static String[] sm_astrFlatCourses={"MAIS-LAFFITTE", "LONGCHAMP", "SAINT-CLOUD", "CHANTILLY", "DEAUVILLE"};
private final static String[] sm_astrJumpCourses={};    // "AUTEUIL", "ENGHIEN"};
 
  private final static List<String> sm_lstFlatCourses=new ArrayList<String>(Arrays.asList(sm_astrFlatCourses));
private final static List<String> sm_lstJumpCourses=new ArrayList<String>(Arrays.asList(sm_astrJumpCourses));

final static SimpleDateFormat sm_parseur = new SimpleDateFormat("dd-MMM-yy", Locale.FRENCH);
 private String m_strCookieHeader="";
 final static SimpleDateFormat sm_yrParser = new SimpleDateFormat("yyyy");
 
   public CalendarQuery(ENEStatement statement, String strDate)
    {
        super(statement, sm_strCalendarURL, strDate);
    }
protected  String sendPostRequest() throws MalformedURLException, IOException, SOAPException, Exception
{
    String strContent = "";
    
    CleanerProperties props = new CleanerProperties();

    HtmlCleaner cleaner = new HtmlCleaner(props);
    HttpURLConnection conn = ExecuteURL.executeURLConnection(m_strURL, "utf-8");    // space to +, quote to ...

    // find the cookies in the response header from the first request
    m_strCookieHeader = ExecuteURL.getCookie(conn, "ASP.NET_SessionId");

    // build request cookie header to send on all subsequent requests
    InputStream is = conn.getInputStream();
    TagNode node = cleaner.clean(is, "utf-8");
    is.close();

    
        m_formFields = ExecuteURL.getViewstate(node);
        m_postMethod  = ExecuteURL.createPostMethod(m_strURL, m_formFields); // ASP is better, must not set ctl00$cphContenuCentral$btRechercher    
        
        m_postMethod.setRequestHeader("Cookie", "ASP.NET_SessionId=" + m_strCookieHeader);

        m_postMethod.setParameter("ctl00$cphContenuCentral$Calendrier$txtDate", m_strSearch);   // "01/07/1995"
        m_postMethod.setParameter("ctl00$cphContenuCentral$cbHippodrome", "");  // 0006
        m_postMethod.setParameter("ctl00$cphContenuCentral$cbClassement", "D-REU");   //   LB-HIPP-CRT
        m_postMethod.setParameter("__EVENTTARGET", "ctl00$cphContenuCentral$Calendrier"); // , ctl00$cphContenuCentral$btRechercher
        m_postMethod.setParameter("__EVENTARGUMENT", "");
        m_postMethod.setParameter("__SCROLLPOSITIONX", "0");
        m_postMethod.setParameter("__SCROLLPOSITIONY", "0");
        //m_postMethod.setParameter("ctl00$cphContenuCentral$cpeCalendrier_ClientState", "false");
        //m_postMethod.setParameter("ctl00$cphContenuCentral$cpeCourses_ClientState", "false");
       //m_postMethod.setParameter("ctl00$cphContenuCentral$btRechercher", "");   // stops working with no filter if set
       //m_postMethod.setParameter("ctl00$cphContenuCentral$txtSaisieExpert", "");
       
       
        node = ExecuteURL.executePostMethod(m_postMethod);
        if (node == null)
        {
            System.out.print("Node not found");
            return strContent;
        }   
        m_formFields = ExecuteURL.getViewstate(node);
        
        TagNode[] aTables = node.getElementsByAttValue("id", "ctl00_cphContenuCentral_gvCalendrier", true, true);
        // <th class="start" scope="col">Terrain</th><th scope="col">Date</th><th scope="col">Courses par course</th><th scope="col">Le programme <br/> (à partir des probables)</th><th scope="col">À noter</th><th scope="col">Début</th><th class="end" scope="col">Premium(*)</th>
        if (aTables.length == 0)
        {
            System.out.print("No content table");
            return strContent;
        }   
        TagNode[] aRows = aTables[0].getElementsByName("tr", true);    
        int nRows = aRows.length - 1;
        //System.out.println(nRows + " meetings found for " + m_strSearch);
        TagNode headerRow = aRows[0];
        HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(headerRow, "th");
        String strCurrentDate="";
        for(int i = 1; i <= nRows; i++)
        {
            TagNode[] aCells = aRows[i].getElementsByName("td", true);  
            TagNode coursenode = aCells[hmColumns.get("Race by race")];
            String strDate = aCells[hmColumns.get("Date")].getText().toString().trim();
            if ("''".equals(strDate))
            {
                strDate = strCurrentDate;
            }
            else
            {
                strCurrentDate = strDate;
            }
            String strCourse = coursenode.getText().toString().trim();
            if ((!sm_lstFlatCourses.contains(strCourse))
                    && (!sm_lstJumpCourses.contains(strCourse))
                    )
                continue;
            
            String strMeetingURL="";
            TagNode[] aA = coursenode.getElementsByName("a", true);
            if(aA.length > 0)
            {
                strMeetingURL = aA[0].getAttributeByName("href");  // javascript:popup('URL')
                strMeetingURL = strMeetingURL.replace("javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(&quot;", "");
                strMeetingURL = strMeetingURL.replace("&quot;, &quot;&quot;, true, &quot;&quot;, &quot;&quot;, false, true))", "");

                Date date= new Date();
                strDate = strDate.replace("avri-", "avr-").replace("octo-", "oct-").replace("nove-", "nov-").replace("déce-", "déc-");
                date=sm_parseur.parse(DateUtils.fixFrenchMonths(strDate));
        
                retrieveMeetingRaces(date, strCourse, strMeetingURL);
            }
            String strGoing = aCells[hmColumns.get("Going")].getText().toString().trim();
            String strTopRace = aCells[hmColumns.get("Notice")].getText().toString().trim();
            //System.out.println(strDate + "-" + strCourse +"-" + strTopRace + "-" + strMeetingURL);
        }
        //System.out.println("Latest date: " + strCurrentDate);

        return strContent;
}
private void retrieveMeetingRaces(Date date, String strCourse, String strMeetingId)
{
    System.out.println("retrieveMeetingRaces: " + date.toString() +"-" + strCourse);
    String strYear = sm_yrParser.format(date);
    int nYear = 0;
    try
    {
        nYear = Integer.valueOf(strYear);
    }
    catch(NumberFormatException e)
    {
        System.out.println("retrieveMeetingRaces Invalid year: " + strYear);
        return;
    }
    m_postMethod  = ExecuteURL.createPostMethod(m_strURL, m_formFields); // ASP is better, must not set ctl00$cphContenuCentral$btRechercher    
    m_postMethod.setParameter("__EVENTTARGET", strMeetingId); 
    m_postMethod.setRequestHeader("Cookie", "ASP.NET_SessionId=" + m_strCookieHeader);
    TagNode node = ExecuteURL.executePostMethod(m_postMethod);
    //m_formFields = ExecuteURL.getViewstate(node);

    TagNode[] aTables = node.getElementsByAttValue("id", "ctl00_cphContenuCentral_gvReunion", true, true);
    if (aTables.length == 0)
    {
        System.out.println("No meeting");
        return;
    }
    TagNode[] aRows = aTables[0].getElementsByName("tr", true);    
    int nRows = aRows.length;
    TagNode headerRow = aRows[0];
    HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(headerRow, "th");
    for(int i = 1; i < nRows; i++)
    {
        TagNode[] aCells = aRows[i].getElementsByName("td", true);  
        TagNode racenode = aCells[hmColumns.get("Race")];
        // O/ 1140-PRIX FIRE LIGHT II
        int nRaceId=0;
        String strCategory=aCells[hmColumns.get("Category")].getText().toString().trim();
        // only interested in Group races for now
        if (strCategory.indexOf("GR.") < 0)
            continue;
        String strRace = racenode.getText().toString().trim();
        String strCode=strRace.substring(0, 1);
        String strId=strRace.substring(3, strRace.indexOf("-"));
        String strTitle=strRace.substring(strRace.indexOf("-") + 1);
        try
        {
            nRaceId = Integer.valueOf(strId.trim());
                    
        }
        catch(NumberFormatException e)
        {
            System.out.println("retrieveMeetingRaces Invalid id: " + strId);
            continue;
        }
           String strRaceURL="";
            TagNode[] aA = racenode.getElementsByName("a", true);
            if(aA.length > 0)
            {
                strRaceURL = aA[0].getAttributeByName("href");  // javascript:popup('URL')
                strRaceURL = strRaceURL.replace("javascript:WebForm_DoPostBackWithOptions(new WebForm_PostBackOptions(&quot;", "");
                strRaceURL = strRaceURL.replace("&quot;, &quot;&quot;, true, &quot;&quot;, &quot;&quot;, false, true))", "");
/*
                m_postMethod  = ExecuteURL.createPostMethod(m_strURL, m_formFields); // ASP is better, must not set ctl00$cphContenuCentral$btRechercher    
                m_postMethod.setParameter("__EVENTTARGET", strRaceURL); 
                m_postMethod.setRequestHeader("Cookie", "ASP.NET_SessionId=" + m_strCookieHeader);
                TagNode resultnode = ExecuteURL.executePostMethod(m_postMethod);

                m_formFields = ExecuteURL.getViewstate(resultnode);
                ResultQuery.processResultTable(resultnode, "ctl00_cphContenuCentral_detail_course_gvChevalPV"); */
            }
            String strDiscipline = aCells[hmColumns.get("Discipline")].getText().toString().trim();
            String strRaceType1 = strDiscipline.substring(0, 1);
            String strRaceType = "";
            if ("P".equals(strRaceType1))
                strRaceType="Flat";
            else if ("S".equals(strRaceType1))
                strRaceType="Chase";
            else if ("H".equals(strRaceType1))
                strRaceType="Hurdle";
            else if ("C".equals(strRaceType1))
                strRaceType="Cross Country";
            String strAgeRange = strDiscipline.substring(strDiscipline.indexOf("(") + 1, strDiscipline.lastIndexOf(")")).replace(" &amp; +", "+");
            
            String strDistance = aCells[hmColumns.get("Distance")].getText().toString().trim();
            nRaceId += (nYear * 10000);
            AdditionalRace race = new AdditionalRace("F" + strCode, 
                    nRaceId, strTitle, strTitle, strCourse, date, 
                    strRaceType, strAgeRange, "", 0, "");  // no Class, Going or #Runners info
       race.setDistance(strDistance); 
       if ("HAND.".equals(strCategory))
          race.setHandicap(true);           
       else if ("RECL.".equals(strCategory))
            race.setConditions("Claimer");
       else if ("ANGLO".equals(strCategory))
            race.setConditions("Arab");
       else if ("GR.I".equals(strCategory))
            race.setGroupRace(1);
       else if ("GR.II".equals(strCategory))
            race.setGroupRace(2);
       else if ("GR.III".equals(strCategory))
            race.setGroupRace(3);
       
       AdditionalRacesFactory.insertAdditionalRaceInstance(m_statement, "", race, true);

            System.out.println(date.toString() +"-" + strCode + "," + strId + "," + strTitle + "-" + strRaceType + "," + strAgeRange + "-" + strDistance + "-" + strRaceURL);
    }
}
}
