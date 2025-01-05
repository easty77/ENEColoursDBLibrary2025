/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bettingpattern;

import ene.eneform.colours.realsports.RealSportsRunner;
import ene.eneform.colours.realsports.bet.RealSportsBet;

import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public abstract class RealSportsBettingPattern {
    
    protected String m_strId;
    protected String m_strPattern;
    protected int m_nAdditionalData;       // m_nFrequency = 2 means only place bet on half the runners, 3 = third etc
    protected int m_nBets;
    public RealSportsBettingPattern(String strPattern, int nAdditionalData)
    {
        m_strPattern = strPattern;
        m_nAdditionalData = nAdditionalData;
        m_strId = m_strPattern + m_nAdditionalData;
}
  public abstract boolean placeBet(int nRace, int nBet, RealSportsBet bet);
  public abstract void generateBets(int nRaces, ArrayList<RealSportsRunner> alRunners);
  public boolean supportsType(String strType){return true;} 
 
    public String getId()
    {
        return m_strId;
    }
    public int getAdditionalData()
    {
        return m_nAdditionalData;
    }
    public int getBetCount(int nTotalBets)
    {
        return m_nBets;
    }
}
