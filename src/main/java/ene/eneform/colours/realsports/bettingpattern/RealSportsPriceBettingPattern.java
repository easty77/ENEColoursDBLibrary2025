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
public class RealSportsPriceBettingPattern extends RealSportsBettingPattern{
    protected boolean[] m_aBets;
public RealSportsPriceBettingPattern(String strOperator, int nPrice)
{
    super(strOperator, nPrice);
}
  public @Override boolean supportsType(String strType)
  {
      if ("W".equals(strType) || "P".equals(strType))
            return true;
      
      return false;
  } 

public @Override void generateBets(int nRaces, ArrayList<RealSportsRunner> alRunners)
{
    int nRunners = alRunners.size();
    m_aBets = new boolean[alRunners.size()];
        for(int nRunner= 0; nRunner < nRunners; nRunner++)
        {
            RealSportsRunner runner = alRunners.get(nRunner);
            double dOdds = runner.getDecimalSP();
            boolean bPlaceBet = false;
            if ("LT".equals(m_strPattern))
            {
                bPlaceBet = dOdds < m_nAdditionalData;
            }
            else if ("LTEQ".equals(m_strPattern))
            {
                bPlaceBet = dOdds <= m_nAdditionalData;
            }
            else if ("GT".equals(m_strPattern))
            {
                bPlaceBet = dOdds > m_nAdditionalData;
            }
            else if ("GTEQ".equals(m_strPattern))
            {
                bPlaceBet = dOdds >= m_nAdditionalData;
            }
            else if ("EQ".equals(m_strPattern))
            {
                bPlaceBet = dOdds == m_nAdditionalData;
            }
            else if (m_strPattern.startsWith("BT"))
            {
                bPlaceBet = dOdds == m_nAdditionalData;
            }
            if (bPlaceBet)
            {
                m_aBets[nRunner] = bPlaceBet;
                m_nBets++;
            }
    }
    m_nBets = m_nBets * nRaces;
}
   public @Override boolean placeBet(int nRace, int nRunner, RealSportsBet bet)
    {
        return m_aBets[nRunner];
    }
}
