/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.atr;

/**
 *
 * @author simon
 */
public class AtTheRacesSlot {

    public String getCourse() {
        return m_strCourse;
    }

    public String getDate() {
        return m_strDate;
    }

    public String getScheduledTime() {
        return m_strScheduledTime;
    }
    
    private String m_strCourse;
    private String m_strDate;
    private String m_strScheduledTime;
    public AtTheRacesSlot(String strDate, String strScheduledTime, String strCourse)
    {
        m_strDate = strDate;
        m_strScheduledTime = strScheduledTime;
        m_strCourse = strCourse;
    }
}
