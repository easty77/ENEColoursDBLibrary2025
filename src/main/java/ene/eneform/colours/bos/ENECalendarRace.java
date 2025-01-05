/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

/**
 *
 * @author Simon
 */
public class ENECalendarRace {

    private String m_strTitle;
    private String m_strGrade;
    private int m_nPreviousRace;

    public ENECalendarRace(String strTitle, String strGrade, int nPreviousRace)
    {
        m_strTitle = strTitle;
        m_strGrade = strGrade;
        m_nPreviousRace = nPreviousRace;
    }

    public String getTitle()
    {
        return m_strTitle;
    }
    public String getGrade()
    {
        return m_strGrade;
    }
    public int getPreviousRace()
    {
        return m_nPreviousRace;
    }
}
