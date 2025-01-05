/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.fg;

import ene.eneform.utils.ENEStatement;
import org.apache.commons.httpclient.methods.PostMethod;

import jakarta.xml.soap.SOAPException;
import java.io.*;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Simon
 */
public abstract class FranceGalopQuery {
    
    private static String sm_strAlphabet="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    private static final SimpleDateFormat sm_fmtDate;

    static
    {
        sm_fmtDate = new SimpleDateFormat( "dd/MM/yyyy" );
     }

    protected PostMethod m_postMethod = null;
    protected Map<String,String> m_formFields = null;
    protected ArrayList<Integer> m_alPages = new ArrayList<Integer>();
    protected ArrayList<Integer> m_alProcessedPages = new ArrayList<Integer>();
    protected ENEStatement m_statement;
    protected String m_strURL;
    protected String m_strSearch;
    
    
    public FranceGalopQuery(ENEStatement statement, String strURL, String strSearch)
    {
        m_statement = statement;
        m_strURL = strURL;
        m_strSearch = strSearch;
    }
    public static void retrieveAllColours(ENEStatement statement) throws FileNotFoundException, UnsupportedEncodingException, IOException, SOAPException, Exception
    {
   for(int a = 0; a < 26; a++)
    {
        for(int b = 0; b < 26; b++)
        {
            for(int c = 0; c < 26; c++)
            {
                FranceGalopQuery fgc = new ColoursQuery(statement, String.format("%c%c%c", sm_strAlphabet.charAt(a),sm_strAlphabet.charAt(b),sm_strAlphabet.charAt(c)));
                fgc.retrieveData();
            }
        }
    }
    }
       
   public static void retrieveHorse(ENEStatement statement, String strHorse) throws FileNotFoundException, UnsupportedEncodingException, IOException, SOAPException, Exception
   {
      FranceGalopQuery fgc = new HorseQuery(statement, strHorse);
      fgc.retrieveData();
   }
   public static void retrieveCalendar(ENEStatement statement, String strDate, String strCourse) throws FileNotFoundException, UnsupportedEncodingException, IOException, SOAPException, Exception
   {
      FranceGalopQuery fgc = new CalendarQuery(statement, strDate);
      fgc.retrieveData();
   }
   public static void retrieveCalendar(ENEStatement statement, int nYear) throws FileNotFoundException, UnsupportedEncodingException, IOException, SOAPException, Exception
   {
       Calendar cal = Calendar.getInstance();
       cal.set(Calendar.YEAR, nYear);
       cal.set(Calendar.MONTH, 0);
       cal.set(Calendar.DATE, 1);
       while(cal.get(Calendar.YEAR) == nYear)
       {
        FranceGalopQuery fgc = new CalendarQuery(statement, sm_fmtDate.format(cal.getTime()));
        fgc.retrieveData();
        cal.add(Calendar.DAY_OF_YEAR, 5);   // 5 days shown at a time
       }
   }
    public  void retrieveData() throws FileNotFoundException, UnsupportedEncodingException, IOException, SOAPException, Exception
    {
                String strTranscript = sendPostRequest();
                if ((strTranscript != null) && !"".equals(strTranscript))
                    writeToFile("colours/France/fg_colours_20201022.txt", strTranscript);
        
    }
abstract protected String sendPostRequest() throws MalformedURLException, IOException, SOAPException, Exception;

protected void writeToFile(String strFileName, String strTranscript) throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
             String strFullDirectory = "c:/Users/Simon/Documents/horses";
            String strFullFileName = strFullDirectory + "/" + strFileName;
            FileOutputStream fos = new FileOutputStream(strFullFileName, true);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            writer.append("\r\n\r\n--" + new SimpleDateFormat("yyyyMMddhhmm").format(new Date()) + "\r\n");
            writer.append(strTranscript);
            writer.close();
    }

protected String convertDate(String strDate)
{
    // DD/MM/YYYY  becomes ISO YYYY-MM-DD
    String[] aDate = strDate.split("/");
    String strOutput = aDate[2] + "-" + aDate[1] + "-" + aDate[0];
    return strOutput;
}
}
