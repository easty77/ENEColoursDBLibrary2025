/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

import ene.eneform.utils.StringUtils;

/**
 *
 * @author Simon
 */
public class ENEOwnerColours {

    private String m_strOwnerName;
    private String m_strColours;
    private int m_nColours;
    private String m_strOrganisation;
    private int m_nYear;
    
    private String m_strJacketSyntax="";
    private String m_strSleevesSyntax="";
    private String m_strCapSyntax="";

    private String m_strLabel="";   // used for filename
    private String m_strLanguage;
   
    
    public ENEOwnerColours(String strOwnerName, String strColours, int nColours, String strOrganisation, int nYear, String strLanguage)
    {
        m_strOwnerName = strOwnerName;
        m_strColours = strColours;
        m_nColours = nColours;
        m_strOrganisation = strOrganisation;
        m_nYear = nYear;
        m_strLanguage = strLanguage;
   }

    public String getOwnerName()
    {
        return m_strOwnerName;
    }
    public String getLanguage()
    {
        return m_strLanguage;
    }
    public String getColours()
    {
        return m_strColours;
    }
    public int getColoursNr()
    {
        return m_nColours;
    }
    public String getOrganisation()
    {
        return m_strOrganisation;
    }
    public int getYear()
    {
        return m_nYear;
    }
    public void setJacketSyntax(String strJacketSyntax)
    {
        m_strJacketSyntax = strJacketSyntax;
    }
    public void setSleevesSyntax(String strSleevesSyntax)
    {
        m_strSleevesSyntax = strSleevesSyntax;
    }
    public void setCapSyntax(String strCapSyntax)
    {
        m_strCapSyntax = strCapSyntax;
    }
    public String getJacketSyntax()
    {
        return m_strJacketSyntax;
    }
    public String getSleevesSyntax()
    {
        return m_strSleevesSyntax;
    }
    public String getCapSyntax()
    {
        return m_strCapSyntax;
    }
   public void setLabel(String strLabel)
    {
        m_strLabel = strLabel;
    }
    public String getLabel()
    {
        return m_strLabel;
    }
    public String getFullOwnerName()
    {
        String strTitle;
        strTitle = m_strOwnerName;
            if (m_nColours > 1)
                strTitle += (" - " + StringUtils.getOrdinalString(m_nColours) + " colours");
        
        return strTitle;
    }
}
