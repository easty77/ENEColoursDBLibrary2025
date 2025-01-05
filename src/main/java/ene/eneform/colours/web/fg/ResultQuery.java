/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.fg;

import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Simon
 */
public class ResultQuery  {
    private static String sm_strURL="http://www9.france-galop.com/fgweb/Domaines/Courses/course_detail.aspx?aaCrse=${YEAR}&spCrse=${RACE_TYPE}&numCrsePgm=${RACE_ID}";
   private int m_nYear = -1;
   private int m_nRaceId = -1;
   private char m_cRaceType=' ';
   private String m_strResultURL;
   
     public ResultQuery(ENEStatement statement, String strResultURL)
    {
        m_strResultURL = "http://www9.france-galop.com" + strResultURL;
    }
   public ResultQuery(ENEStatement statement, int nYear, char cRaceType, int nRaceId)
    {
        m_nYear = nYear;
        m_nRaceId = nRaceId;
        m_cRaceType = cRaceType;
        m_strResultURL=sm_strURL;
        m_strResultURL = m_strResultURL.replace("${YEAR}", String.valueOf(m_nYear));
        m_strResultURL = m_strResultURL.replace("${RACE_ID}", String.valueOf(m_nRaceId));
        m_strResultURL = m_strResultURL.replace("${RACE_TYPE}", String.valueOf(m_cRaceType));
    }
    
    public void retrieveData() throws IOException
    {
            TagNode rootNode = ExecuteURL.getRootNode(m_strResultURL, "utf-8");
            processResultTable(rootNode, "ctl00_cphContenuCentral_detailCourse_gvChevalPV");
    }
    public static void processResultTable(TagNode node, String strLabel)
    {
       TagNode[] aTables = node.getElementsByAttValue("id", strLabel, true, true);
        // <th class="start" scope="col">Place</th><th class="invis" scope="col">*&nbsp;</th><th scope="col">Cheval</th><th scope="col">N°</th><th scope="col" style="width:70px;">Père / Mère</th><th scope="col">Ecart<br/>au précédent</th><th scope="col">Propriétaire</th><th scope="col">Entraîneur</th><th scope="col">Jockey</th><th scope="col">Poids</th><th scope="col">Gain</th><th scope="col">Prim. Prop</th><th scope="col">Prim. Elev.</th><th scope="col">Suppl.</th><th scope="col">Œil.</th><th scope="col">Ecurie</th><th class="invis" scope="col">A réclamer</th><th class="invis" scope="col">par</th><th class="end" scope="col">Eleveurs</th>
        if(aTables.length == 0)
        {
            System.out.println("No result table");
            return;
        }   
        TagNode[] aRows = aTables[0].getElementsByName("tr", true);    
        int nRows = aRows.length;
        TagNode headerRow = aRows[0];
        HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(headerRow, "th");
        for(int i = 1; i < nRows; i++)
        {
            TagNode[] aCells = aRows[i].getElementsByName("td", true);  
            String strHorse = aCells[hmColumns.get("Horse")].getText().toString().trim();
            String strClothNumber = aCells[hmColumns.get("#")].getText().toString().trim();
            String strPosition = aCells[hmColumns.get("Place")].getText().toString().trim();
            String strOwner = aCells[hmColumns.get("Owner")].getText().toString().trim();
            String strTrainer = aCells[hmColumns.get("Trainer")].getText().toString().trim();
            String strJockey = aCells[hmColumns.get("Jockey")].getText().toString().trim();
            String strWeight = aCells[hmColumns.get("Weight")].getText().toString().trim();
            String strMargin = aCells[hmColumns.get("Margin")].getText().toString().trim();
            String strTack = aCells[hmColumns.get("Blink.")].getText().toString().trim();
            System.out.println(strHorse + "-" + strPosition);
        }
        
    }
}
