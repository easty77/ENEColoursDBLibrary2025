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
public class ENECalendarMonth {
    private String m_strCountry;
    private int m_nYear;
    private int m_nMonth;

    private ArrayList<ENECalendarDate> m_alDates = new ArrayList<ENECalendarDate>();

    public ENECalendarMonth(String strCountry, int nYear, int nMonth)
    {
        m_strCountry = strCountry;
        m_nYear = nYear;
        m_nMonth = nMonth;
    }
    public void addDate(ENECalendarDate dtCalendar)
    {
        m_alDates.add(dtCalendar);
    }
    public String getCountry()
    {
        return m_strCountry;
    }
    public int getYear()
    {
        return m_nYear;
    }
    public int getMonth()
    {
        return m_nMonth;
    }

    public int getNrRacingDays()
    {
        return m_alDates.size();
    }

    public int getNrMeetings()
    {
        int nMeetings = 0;
        for(int i = 0; i < m_alDates.size(); i++)
        {
            ENECalendarDate dtCalendar = m_alDates.get(i);
            nMeetings += dtCalendar.getNrMeetings();
        }

        return nMeetings;
    }

    public int getNrRaces()
    {
        int nRaces = 0;
        for(int i = 0; i < m_alDates.size(); i++)
        {
            ENECalendarDate dtCalendar = m_alDates.get(i);
            nRaces += dtCalendar.getNrRaces();
        }

        return nRaces;

    }
    public Iterator<ENECalendarDate> getDateIterator()
    {
        return m_alDates.iterator();
    }
    public ENECalendarDate getDate(int nDayOfMonth)
    {
        for(int i = 0; i < m_alDates.size(); i++)
        {
            ENECalendarDate dtCalendar = m_alDates.get(i);
            if (dtCalendar.getDayOfMonth() == nDayOfMonth)
                return dtCalendar;
        }

        return null;
    }
}
