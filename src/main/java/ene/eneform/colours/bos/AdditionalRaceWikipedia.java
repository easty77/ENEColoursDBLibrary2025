/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.bos;

/**
 *
 * @author Simon
 */
public class AdditionalRaceWikipedia {
 protected String m_strName;
 protected String m_strLanguage;
 protected String m_strWikipediaRef;
 protected int m_nStartYear;
 protected int m_nEndYear;

  public AdditionalRaceWikipedia(String strName, String strLanguage, String strWikipediaRef, int nStartYear, int nEndYear)
 {
     m_strName = strName;
     m_strLanguage = strLanguage;
     m_strWikipediaRef = strWikipediaRef;
     m_nStartYear = nStartYear;
     m_nEndYear = nEndYear;
 }
   public String getName() {
        return m_strName;
    }

    public String getLanguage() {
        return m_strLanguage;
    }

    public String getWikipediaRef() {
        return m_strWikipediaRef;
    }

    public int getStartYear() {
        return m_nStartYear;
    }

    public int getEndYear() {
        return m_nEndYear;
    }
}
