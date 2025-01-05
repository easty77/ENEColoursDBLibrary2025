/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

/**
 *
 * @author Simon
 */
public class ENEColoursRunner{

    private String m_strId;
    private String m_strColours;
    private String m_strOwner = "";

    public ENEColoursRunner(int nRunner, String strColours)
    {
        m_strId = String.valueOf(nRunner);
        m_strColours = strColours;
    }

    public ENEColoursRunner(int nRunner, String strOwner, String strColours)
    {
        m_strId = String.valueOf(nRunner);
        m_strColours = strColours;
        m_strOwner = strOwner;
    }
    public ENEColoursRunner(String strOwner, String strColours)
    {
        m_strId = strOwner;
        m_strColours = strColours;
        m_strOwner = strOwner;
    }

    public String getOwnerName()
    {
        return m_strOwner;
    }

    public String getID()
    {
        return m_strId;
    }

    public String getColours()
    {
        return m_strColours;
    }
}