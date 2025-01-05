/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports;

import ene.eneform.utils.HorseRacingUtils;

/**
 *
 * @author Simon
 */
public class RealSportsRunner {
    
    protected String m_strSP="";
    protected double m_dProbability = 0.0d;
    protected double m_dSP = 0;

    //private int m_nWeighting = 0;
    private int m_nStallNumber = 0;
    
    public RealSportsRunner(String strSP)
    {
        m_strSP = strSP;
        m_dSP = HorseRacingUtils.convertSP(strSP);
        m_dProbability = 1/(1 + m_dSP);
    }

    public String getSP() {
        return m_strSP;
    }
   public double getProbability() {
        return m_dProbability;
    }
  public double getDecimalSP() {
        return m_dSP;
    }

    public int getStallNumber() {
        return m_nStallNumber;
    }

    public void setStallNumber(int nStallNumber) {
        m_nStallNumber = nStallNumber;
    }
   public String analyse(int nRaces)
    {
        return (m_nStallNumber +". " + m_strSP);
    }
    public @Override String toString()
    {
        return (m_nStallNumber +". " + m_strSP);
    }
}
