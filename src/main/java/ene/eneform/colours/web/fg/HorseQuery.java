/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.fg;

import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.ExecuteURL;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import jakarta.xml.soap.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Simon
 */
public class HorseQuery extends FranceGalopQuery{
    
     private static String sm_strHorseURL = "http://www9.france-galop.com/fgweb/domaines/chevaux/cheval_perf.aspx";
    //private static String sm_strHorseListURL = "http://www9.france-galop.com/fgweb/WebService/cheval.asmx/ListeChevaux";
   public HorseQuery(ENEStatement statement, String strSearch)
    {
        super(statement, sm_strHorseURL, strSearch);
    }
protected  String sendPostRequest() throws MalformedURLException, IOException, SOAPException, Exception
{
    List<String> astrHorses = getHorseList(m_strSearch);
    String strContent = "";
    
        TagNode node = ExecuteURL.getRootNode(m_strURL, "utf-8");
        m_formFields = ExecuteURL.getViewstate(node);
        m_postMethod  = ExecuteURL.createPostMethod(m_strURL, m_formFields);
        
        m_postMethod.setParameter("ctl00$cphContenuCentral$recherche_cheval$txtRechercheCheval", astrHorses.get(0)); 
        //m_postMethod.setParameter("txtRechercheCheval", astrHorses.get(0)); 
        //m_postMethod.setParameter("ctl00$txtRecherche", astrHorses.get(0));
        //m_postMethod.setParameter("ctl00$txtParam", astrHorses.get(0));
        //m_postMethod.setParameter("ctl00$cphContenuCentral$cbSpecialite", "P/");
        //m_postMethod.setParameter("hiddenInputToUpdateATBuffer_CommonToolkitScripts", "1");
        //m_postMethod.setParameter("__EVENTTARGET", "ctl00$cphContenuCentral$cbSpecialite"); 
        //m_postMethod.setParameter("__EVENTARGUMENT", "");
       
        node = ExecuteURL.executePostMethod(m_postMethod);
        m_formFields = ExecuteURL.getViewstate(node);
        
        TagNode[] aTables = node.getElementsByAttValue("id", "ctl00_cphContenuCentral_gvPerf", true, true);
        // <th class="start" scope="col">Date</th><th scope="col">Propriétaire</th><th scope="col">Place</th><th scope="col">Gains</th><th scope="col">Prim. Prop</th><th scope="col">Prim. Elev.</th><th scope="col">Poids Porté</th><th scope="col">Jockey</th><th scope="col">Hippodrome</th><th scope="col">Dist.</th><th scope="col">Disc.</th><th scope="col">Catég.</th><th scope="col">*&nbsp;</th><th scope="col">Alloc. tot.</th><th scope="col">Entraîneur</th><th scope="col">Val.</th><th class="end" scope="col">Vidéo</th>
        TagNode[] aRows = aTables[0].getElementsByName("tr", true);    
        int nRows = aRows.length;
        TagNode headerRow = aRows[0];
        HashMap<String,Integer> hmColumns = ExecuteURL.getColumnNumbers(headerRow, "th");
        for(int i = 1; i < nRows; i++)
        {
            TagNode[] aCells = aRows[i].getElementsByName("td", true);  
            TagNode datenode = aCells[hmColumns.get("Date")];
            String strDate = datenode.getText().toString().trim();
            String strRaceURL="";
            TagNode[] aA = datenode.getElementsByName("a", true);
            if(aA.length > 0)
            {
                strRaceURL = aA[0].getAttributeByName("href");  // javascript:popup('URL')
                strRaceURL=strRaceURL.substring(18, strRaceURL.length() - 2).replace("&amp;", "&");
                ResultQuery rq = new ResultQuery(m_statement, strRaceURL);
                rq.retrieveData();
            }
            String strDistance = aCells[hmColumns.get("Dist.")].getText().toString().trim();
            String strPosition = aCells[hmColumns.get("Place")].getText().toString().trim();
            String strGrade = aCells[hmColumns.get("Cat.")].getText().toString().trim();
            String strCourse = aCells[hmColumns.get("Racecourse")].getText().toString().trim();
/*            int nCells = aCells.length;
            for(int j = 0; j < nCells; j++)
            {
                strContent += aCells[j].getText();
            }
            System.out.println(i + "-" + strContent);
            strContent=""; */
        }

        return strContent;
}
public List<String> getHorseList(String strSearch) throws SOAPException, Exception
{
        ArrayList<String> astrHorses = new ArrayList<String>();
        SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        // Send SOAP Message to SOAP Server
        String url = "http://www9.france-galop.com/fgweb/WebService/cheval.asmx";
        
        // get SOAP syntax using http://www9.france-galop.com/fgweb/WebService/cheval.asmx?wsdl
        
        SOAPMessage soapResponse = soapConnection.call(createListeChevauxSOAPRequest(strSearch), url);
       
        //System.out.print("Response SOAP Message:");
        //soapResponse.writeTo(System.out);
        SOAPBody body = soapResponse.getSOAPBody();
        NodeList nodelist = body.getElementsByTagName("string");
        int nNodes = nodelist.getLength();
        for(int i = 0; i < nNodes; i++)
        {
            Node node  = nodelist.item(i);
            String strNode = node.getTextContent();
            astrHorses.add(strNode);
        }

        soapConnection.close();
        
        return astrHorses;
}
    private static SOAPMessage createListeChevauxSOAPRequest(String strSearch) throws Exception {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPPart soapPart = soapMessage.getSOAPPart();
        String serverURI = "http://tempuri.org/";

        // SOAP Envelope
        SOAPEnvelope envelope = soapPart.getEnvelope();
        envelope.addNamespaceDeclaration("example", serverURI);

        // SOAP Body
        SOAPBody soapBody = envelope.getBody();
        SOAPElement soapBodyElem = soapBody.addChildElement("ListeChevaux", "example");
        SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("prefixText", "example");
        soapBodyElem1.addTextNode(strSearch);
        SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("count", "example");
        soapBodyElem2.addTextNode("30");

        MimeHeaders headers = soapMessage.getMimeHeaders();
        headers.addHeader("SOAPAction", serverURI  + "ListeChevaux");

        soapMessage.saveChanges();

        /* Print the request message */
        System.out.print("Request SOAP Message:");
        soapMessage.writeTo(System.out);
        System.out.println();

        return soapMessage;
    }
}
