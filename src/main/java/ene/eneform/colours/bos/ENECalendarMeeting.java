/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class ENECalendarMeeting {

    private String m_strCourse;
    private String m_strMeetingType;
    private boolean m_bEvening;
    private ArrayList<ENECalendarRace> m_alRaces = new ArrayList<ENECalendarRace>();

    public ENECalendarMeeting(String strCourse, String strMeetingType, boolean bEvening)
    {
        m_strCourse = strCourse;
        m_strMeetingType = strMeetingType;
        m_bEvening = bEvening;
    }

    public void addRace(ENECalendarRace race)
    {
        m_alRaces.add(race);
    }

    public String getCourse()
    {
        return m_strCourse;
    }
    public String getMeetingType()
    {
        return m_strMeetingType;
    }
    public boolean isEvening()
    {
        return m_bEvening;
    }
    public int getNrRaces()
    {
        return m_alRaces.size();
    }
    public Iterator<ENECalendarRace> getRaceIterator()
    {
        return m_alRaces.iterator();
    }
}
