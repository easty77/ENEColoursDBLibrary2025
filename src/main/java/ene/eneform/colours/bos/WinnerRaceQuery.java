/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.bos;

import ene.eneform.colours.web.rp.RacingPostHorse;
import ene.eneform.colours.web.rp.RacingPostHorseRaceSummary;
import ene.eneform.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 * The information needed to identify a given race
 * The course, winner (not including bred), year, month (a guide) keywords from the title
 * jockey also possible
*/
public class WinnerRaceQuery {
    private String m_strCourse;
    private String m_strWinner;
    private int m_nYear;
    private int m_nMonth;   // -1 if unknown
    private int m_nDay;   // -1 if unknown
    private String[] m_astrKeywords = {};
    
    public WinnerRaceQuery(String strCourse, String strWinner, int nYear, int nMonth, int nDay, String strKeywords)
    {
        m_strCourse = strCourse;
        m_strWinner = RacingPostHorse.convertName(strWinner);
        m_nYear = nYear;
        m_nMonth = nMonth;
        m_nDay = nDay;
        if (!"".equals(strKeywords.trim()))
            m_astrKeywords = strKeywords.trim().split("\\+");       // TO DO:  Allow | meaning OR in keywords (+ is current separator)
    }

    public String getCourse() {
        return m_strCourse; // use | to indicate OR
    }

    public String getWinner() {
        return m_strWinner;
    }

    public void setWinner(String strWinner) {
        m_strWinner = RacingPostHorse.convertName(strWinner);
    }

    public int getYear() {
        return m_nYear;
    }

    public int getMonth() {
        return m_nMonth;
    }

    public int getDay() {
        return m_nDay;
    }

    public String[] getKeywords() {
        return m_astrKeywords;
    }
/*    
    public long getRaceId(ArrayList<RacingPostHorseRaceSummary> alRaces)
    {
        RacingPostHorseRaceSummary summary = getRaceSummary(alRaces);
        if (summary == null)
            return 0;
        else
            return summary.getRaceID();
    } */
    public RacingPostHorseRaceSummary getRaceSummary(ArrayList<RacingPostHorseRaceSummary> alRaces)
    {
        // find best match and return id
        // the horse is always the Winner
        Iterator<RacingPostHorseRaceSummary> iter = alRaces.iterator();
        while(iter.hasNext())
        {
            RacingPostHorseRaceSummary summary = iter.next();
            if (summary.getPosition() == 1) // we know the horse won the race
            {
                int nSummaryYear = summary.getYear();
                int nSummaryMonth = summary.getMonth();  //  N.B Calendar month is indexed from 0
                int nSummaryDay = summary.getDay();
                if ((nSummaryYear == m_nYear) && (nSummaryMonth == m_nMonth) && (nSummaryDay == m_nDay))
                    return summary;     // exact match on date
            }
        }
        iter = alRaces.iterator();
        while(iter.hasNext())
        {
            RacingPostHorseRaceSummary summary = iter.next();
                int nSummaryYear = summary.getYear();
                int nSummaryMonth = summary.getMonth();  //  N.B Calendar month is indexed from 0
                if ((summary.getPosition() == 1) && (nSummaryYear == m_nYear))
                {
                    // check course
                    String[] astrCourses = m_strCourse.split("\\|");
                    if("".equals(m_strCourse) || ArrayUtils.contains(astrCourses, summary.getCourse().getCode(), true)) // can this be removed??
                    {
                        String strRaceTitle = summary.getTitle().toLowerCase();
                        int nMatch = 0;
                        for (int i = 0; i < m_astrKeywords.length; i++)
                        {
                            if (strRaceTitle.indexOf(m_astrKeywords[i].toLowerCase()) >= 0)
                                nMatch++;
                        }
                        if ((nMatch > 0) && (nMatch == m_astrKeywords.length))
                            return summary;
                       if (((m_nMonth <= 0) || (nSummaryMonth == m_nMonth)) && (m_astrKeywords.length==0)) // m_nMonth = -1 if unknown
                        {
                            return summary;
                            // check title against key words
                        }
                   }
                }
            }
         
        return null;
    }
    
}
