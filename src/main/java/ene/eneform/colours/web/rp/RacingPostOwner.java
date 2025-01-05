/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

/**
 *
 * @author simon
 */
public class RacingPostOwner {
    
    private String m_strCode;
    private String m_strName;
    private String m_strColours;
     public RacingPostOwner(String strCode, String strName, String strColours)
    {
        m_strCode = strCode;
        m_strName = strName;
        m_strColours = strColours;
    }
     public String getCode() {
            return m_strCode;
        }

        public String getName() {
            return m_strName;
        }
        public String getColours() {
            return m_strColours;
        }
}
