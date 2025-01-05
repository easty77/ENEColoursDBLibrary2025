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
public class ENECalendarDate {
    private int m_nDayOfMonth;
    private boolean m_bBH;
    private ArrayList<ENECalendarMeeting> m_alMeetings = new ArrayList<ENECalendarMeeting>();

    public ENECalendarDate(String strCountry, int nYear, int nDayOfMonth, boolean bBH)
    {
        m_nDayOfMonth = nDayOfMonth;
        m_bBH = bBH;
    }

    public void addMeeting(ENECalendarMeeting meeting)
    {
        m_alMeetings.add(meeting);
    }

    public int getDayOfMonth()
    {
        return m_nDayOfMonth;
    }

    public boolean isBankHoliday()
    {
        return m_bBH;
    }

    public int getNrMeetings()
    {
        return m_alMeetings.size();
    }

    public int getNrRaces()
    {
        int nRaces = 0;
        for(int i = 0; i < m_alMeetings.size(); i++)
        {
            ENECalendarMeeting meeting = m_alMeetings.get(i);
            nRaces += meeting.getNrRaces();
        }

        return nRaces;
    }
    public Iterator<ENECalendarMeeting> getMeetingIterator()
    {
        return m_alMeetings.iterator();
    }
}
