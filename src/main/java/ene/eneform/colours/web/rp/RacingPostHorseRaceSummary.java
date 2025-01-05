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
public class RacingPostHorseRaceSummary extends RacingPostRaceSummary
{
    private int m_nPosition;
    private String m_strJockey;
    private String m_strHorse;
 
    public  RacingPostHorseRaceSummary(String strRaceURL, String strHorse, int nPosition, String strJockey)
    {
        super(strRaceURL);
       m_nPosition = nPosition;
       m_strJockey = strJockey;
       m_strHorse = strHorse;
    }
    public RacingPostHorseRaceSummary(long lRaceId, RacingPostCourse course, String strDate, String strWinner) 
    {
        this(lRaceId, course, strDate, strWinner, 1, "");
    }
    public RacingPostHorseRaceSummary(long lRaceId, RacingPostCourse course, String strDate, String strHorse, int nPosition, String strJockey) 
    {
        super(lRaceId, course, strDate);
       m_nPosition = nPosition;
       m_strJockey = strJockey;
       m_strHorse = strHorse;
    }
    public int getPosition() {
        return m_nPosition;
    }

    public String getHorse() {
        return m_strHorse;
    }

    public String getJockey() {
        return m_strJockey;
    }
}
