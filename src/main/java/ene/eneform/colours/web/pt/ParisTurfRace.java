/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.pt;

import java.util.Date;

/**
 *
 * @author Simon
 */
public class ParisTurfRace {
 
    private Date m_dtRace;
    private String m_strCourse;
    private String m_strTitle;
    private int m_nId;
    private String m_strWinner;
    private String m_strARDName;


   public ParisTurfRace(Date dtRace, String strCourse, String strTitle, int nId, String strWinner)
    {
        m_dtRace = dtRace;
        m_strCourse = strCourse;
        m_strTitle = strTitle;
        m_nId = nId;
        m_strWinner = strWinner;
    }

   public Date getRaceDate() {
        return m_dtRace;
    }

    public String getCourse() {
        return m_strCourse;
    }

     public String getTitle() {
        return m_strTitle;
    }

     public String getWinner() {
        return m_strWinner;
    }
     public void setWinner(String strWinner) {
        m_strWinner = strWinner;
    }

    public int getId() {
        return m_nId;
    }

     public String getARDName() {
        return m_strARDName;
    }

    public void setARDName(String strARDName) {
        this.m_strARDName = strARDName;
    }
   
 }
