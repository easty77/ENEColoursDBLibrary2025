/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web;

import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author Simon
 */
public class IrishDerby {
private static String sm_IrishDerbyURL= "http://www.curragh.ie/derby150/irish-derby-results.html";

 private static SimpleDateFormat sm_dtFormatter1 =  new SimpleDateFormat("yyyy: EEEEE dd MMMM", Locale.US); 
 private static SimpleDateFormat sm_dtFormatter2 =  new SimpleDateFormat("yyyy EEEEE dd MMMM", Locale.US); 
private static SimpleDateFormat sm_dtIsoFormatter =  new SimpleDateFormat("yyyy-MM-dd"); 

   public static void loadResults(ENEStatement statement)
    {
        // retreive from ARD
        try
        {
            TagNode rootNode = ExecuteURL.getRootNode(sm_IrishDerbyURL, "utf-8");
        
            
            TagNode[] top = rootNode.getElementsByAttValue("class", "col-md-9", true, true);
            TagNode[] aNodes = top[0].getAllElements(false);
            
            //TagNode[] aRaces = rootNode.getElementsByAttValue("class", "year-title", true, true);
            for(int i = 0; i < aNodes.length; i++)
            {
                Date dt = null;
                if ("year-title".equals(aNodes[i].getAttributeByName("class")))
                {
                    TagNode race = aNodes[i];
                    TagNode[] ps = race.getElementsByName("p", true);
                    for(int j = 0; j < ps.length; j++)
                    {
                        if (j == 0)
                        {
                            String strDate = ExecuteURL.getFirstNodeContent(ps[j]);
                            if (strDate.indexOf(":") > 0)
                                dt = sm_dtFormatter1.parse(strDate);
                            else
                                dt = sm_dtFormatter2.parse(strDate);
                            //System.out.println(strDate +"-" + sm_dtIsoFormatter.format(dt));
                        }
                        else
                        {
                            //System.out.println( j + "-" + ExecuteURL.getNodeContentString(ps[j], "", "|"));
                        }
                    }
                    // next element is a table
                    // 1963 & 1964 have one row per horse, later two rows per horse
                    TagNode table = aNodes[i+1];
                    TagNode[] rows = table.getElementsByName("tr", true);
                    for(int j = 0; j < rows.length; j++)
                    {
                        String strOwner="";
                        String strColours = "";
                        TagNode[] cells = rows[j].getElementsByName("td", true);
                        if (dt.getYear() <= 64)
                        {
                            String strCell = cells[1].getText().toString();
                            int nLastBracket = strCell.lastIndexOf("(");
                            strOwner = strCell.substring(0, nLastBracket - 1).trim();
                            strColours = strCell.substring(nLastBracket + 1, strCell.length() - 1);
                        }    
                        else
                        {
                            strOwner = cells[1].getText().toString();
                            cells = rows[j+1].getElementsByName("td", true);
                            strColours = cells[1].getText().toString();
                            strColours = strColours.substring(1, strColours.length()-1);   // remove brackets
                            j++;
                        }
                        strOwner = strOwner.replace("&amp;" , "&");
                        strColours = strColours.replace(" &amp; " , " and ").replace(" lt " , " light ").replace("hlvd", "halved").replace("hpd" , "hooped").replace("slvs", "sleeves").replace("qrtd", "quartered");
                        if (!"nbsp".equals(strColours))
                            System.out.println(strOwner + "|" + strColours + "|" + (dt.getYear()+ 1900));
                    }
                    //System.out.println(ExecuteURL.getNodeContentString(table));
                    i++;
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("loadGSResults: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
