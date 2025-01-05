/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

/**
 *
 * @author Simon
 */
public class ENERegisteredColours {

    private int m_nColours;
    private String m_strColours;
    private String m_strLanguage;

    public ENERegisteredColours(int nColours, String strColours, String strLanguage)
    {
        m_nColours = nColours;
        m_strColours = strColours;
        m_strLanguage = strLanguage;
    }

    public String getColours()
    {
        return m_strColours;
    }
    public int getColoursNumber()
    {
        return m_nColours;
    }
    public String getLanguage()
    {
        return m_strLanguage;
    }
}
