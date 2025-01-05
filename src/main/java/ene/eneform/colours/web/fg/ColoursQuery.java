/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.fg;

import ene.eneform.colours.database.WikipediaFactory;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 * @author Simon
 */
public class ColoursQuery extends FranceGalopQuery{
    
   private static String sm_strColoursURL = "https://www2.france-galop.com/couleurs/CatcoNPro.aspx";

   public ColoursQuery(ENEStatement statement, String strSearch)
    {
        super(statement, sm_strColoursURL, strSearch);
    }
protected  String sendPostRequest() throws MalformedURLException, IOException
{
    String strContent = "";
    
        TagNode node = ExecuteURL.getRootNode(m_strURL, "utf-8");
        //This reads the page line-by-line and extracts out all the values from hidden input fields
        m_formFields = ExecuteURL.getViewstate(node);
        m_postMethod = ExecuteURL.createASPPostMethod(m_strURL, m_formFields);
        
        m_postMethod.setParameter("txtMaRequete", m_strSearch); // specific to Colours
         
        node = ExecuteURL.executePostMethod(m_postMethod);
        m_formFields = ExecuteURL.getViewstate(node);
        
 TagNode[] aTables = node.getElementsByAttValue("id", "GridView1", true, true);
        TagNode[] aRows = aTables[0].getElementsByName("tr", true);    
        int nPages = 1;     // assume just this page
        
        // look in last but one row (in fact it's last row but with embedded table)  - if there is a table there then there are mutiple pages
        
        nPages = getPageNumbers(aRows);
        
        int nRowMax = aRows.length - 1;
        if (nPages > 1)
            nRowMax = nRowMax - 2;      // the last 3 rows do not contain owner data
        int nCurrentPage = 0;
        if (m_alProcessedPages.size() > 0)
            nCurrentPage = m_alProcessedPages.get(0);
        strContent += getCurrentPage(nCurrentPage, nRowMax);

        // now the other pages
        strContent += processPageList();
        // there may be some more pages
        while (m_alPages.size() > 0)
            strContent += processPageList();
     return strContent;
}
private  int getPageNumbers(TagNode[] aRows)
{
     int nPages = 0;
        if (aRows.length > 2)      // page size is 20
        {
            TagNode lastRow = aRows[aRows.length-2];
            TagNode[] aPageTables = lastRow.getElementsByName("table", true);
            if (aPageTables.length > 0)
            {
                TagNode[] aPageRows = aPageTables[0].getElementsByName("tr", true);
                if(aPageRows.length > 0)
                {
                    TagNode[] aPageCells = aPageRows[0].getElementsByName("td", true);
                    // there is one cell for each page
                    nPages = aPageCells.length;     // Only shows up to 11 pages and there may be more that don't see until on last page!
                    int nMaxPage = 0;
                    for(int j = 0; j < aPageCells.length; j++)
                    {
                        TagNode[] aA = aPageCells[j].getElementsByName("a", true);
                        if (aA.length == 0)
                        {
                            m_alProcessedPages.add(Integer.parseInt(aPageCells[j].getText().toString()));                            
                            System.out.println("This is page:" + aPageCells[j].getText().toString());
                        }
                        else
                        {
                            String strA = aA[0].getText().toString();
                            if (!"...".equals(strA))                    // indicates more than 10 pages
                            {
                                int nPage = Integer.parseInt(strA);
                                if (!m_alProcessedPages.contains(nPage))
                                {
                                    System.out.println("Adding page:" + nPage);
                                    m_alPages.add(nPage);
                                    nMaxPage = nPage;
                                }
                            }
                            else if (nMaxPage > 0)
                            {
                                // assume at end
                                m_alPages.add(++nMaxPage);
                                System.out.println("Adding page ... :" + nMaxPage);
                           }
                        }
                    }
                }
           }
        }
    return nPages;
}
private  String processPageList()
{
    TagNode node;
    TagNode[] aTables;
    TagNode[] aRows = {};
    String strContent="";
        for(int n = 0; n < m_alPages.size(); n++)
        {
            int nPage = m_alPages.get(n);
            m_postMethod = ExecuteURL.createASPPostMethod(m_strURL, m_formFields);
            System.out.println("Setting page: " + nPage);
            m_postMethod.setParameter("__EVENTARGUMENT", "Page$" + nPage);    // can't submit current page
            m_postMethod.setParameter("__EVENTTARGET", "GridView1");
            m_postMethod.setParameter("txtMaRequete", m_strSearch);
            node = ExecuteURL.executePostMethod(m_postMethod);
            aTables = node.getElementsByAttValue("id", "GridView1", true, true);
            aRows = aTables[0].getElementsByName("tr", true);
            m_formFields = ExecuteURL.getViewstate(node);
            int nRowMax = aRows.length - 3;      // we know there is a table
            System.out.println("Getting page: " + nPage);
            strContent += getCurrentPage(nPage, nRowMax);
            m_alProcessedPages.add(nPage);
        }
    
        // are there more pages?
        m_alPages.clear();     // new pages array
        int nPages = getPageNumbers(aRows); // keep old processed pages array
        
        return strContent;
}
private  String getCurrentPage(int nPage, int nRowMax)
{
    String strContent = "";
            for(int i = 0; i < nRowMax; i++)   // ignore last row
            {
                m_postMethod = ExecuteURL.createASPPostMethod(m_strURL, m_formFields);
                m_postMethod.setParameter("__EVENTARGUMENT", "Select$" + i);
                m_postMethod.setParameter("__EVENTTARGET", "GridView1");
                TagNode node1= ExecuteURL.executePostMethod(m_postMethod);
                if (node1 != null)
                {
                    try
                    {
                        String strOwner = node1.getElementsByAttValue("name", "txtMessResultat", true, true)[0].getAttributeByName("value");
                        String strDates = node1.getElementsByAttValue("name", "txtMessResultat2", true, true)[0].getAttributeByName("value");
                        String strColours = node1.getElementsByAttValue("name", "txtMessResultat4", true, true)[0].getAttributeByName("value");
                        if (strColours != null)
                            strColours = strColours.replaceAll("\\s+", " ");
                        String strColourDescription = ("Owner: " + strOwner
                                + " Dates: " + strDates
                                + " Colours: " + ((strColours == null) ? "NONE" : strColours)
                                + "\n");
                        System.out.print(strColourDescription);
                        String strTo="";
                        String strFrom = "";
                        if (strDates.indexOf("depuis le ") >= 0)
                            strFrom = convertDate(strDates.replace("depuis le ", ""));
                        else
                        {
                            // format du 05/08/1982 au 28/12/1989
                            strFrom = convertDate(strDates.substring(3, 13));
                            strTo = convertDate(strDates.substring(17, 27));
                        }
                        WikipediaFactory.insertFrenchOwner(m_statement, strOwner.replace("Couleurs Attribuées à ", ""),
                                strFrom, strTo, 
                                strColours
                                );
                        strContent += strColourDescription;
                    }
                    catch(Exception e)
                    {
                        System.out.append("Exception: " + m_strSearch + "-" + nPage + "-" + i + "-" + e.getMessage());
                    }
                }
                else
                    System.out.append("Null colours: " + m_strSearch + "-" + nPage + "-" + i);
            }
    return strContent;
}
}
