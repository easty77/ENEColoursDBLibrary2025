/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.bos;

/**
 *
 * @author Simon
 */
public class ENEWebsiteURL {
    protected String m_strTitle;
    protected String m_strType;
    protected String m_strSubType;
    protected String m_strURL;
    
    public ENEWebsiteURL(String strType, String strSubType, String strTitle, String strURL)
    {
        m_strType = strType;
        m_strSubType = strSubType;
        m_strTitle = strTitle;
        m_strURL = strURL;
    }
    public String getType()
    {
        return m_strType;
    }
    public String getSubType()
    {
        return m_strSubType;
    }
    public String getTitle()
    {
        return m_strTitle;
    }
    public String getURL()
    {
        return m_strURL;
    }
}
