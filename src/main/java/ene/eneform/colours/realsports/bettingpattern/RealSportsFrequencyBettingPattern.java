/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bettingpattern;

import ene.eneform.colours.realsports.RealSportsRunner;
import ene.eneform.colours.realsports.bet.RealSportsBet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Simon
 */
public class RealSportsFrequencyBettingPattern  extends RealSportsBettingPattern{
    protected HashMap<Integer,boolean[][]> m_hmBets = new HashMap<>();      // indexed by nTotalBets
    protected boolean[][] m_aBets;
    protected int m_nRunners = 0;
    protected int m_nRaces = 0;
public RealSportsFrequencyBettingPattern(int nFrequency)
{
    super("F", nFrequency);
}
public @Override void generateBets(int nRaces, ArrayList<RealSportsRunner> alRunners)
{
    m_nRunners = alRunners.size();
    m_nRaces = nRaces;
    // to do: extend for Multiple bets
}
private boolean[][] generateBetArray(int nTotalBets)
{
    boolean[][] aBets = new boolean[m_nRaces][nTotalBets];
    for(int i = 0; i < m_nRaces; i++)
    {
        for(int j = 0; j < nTotalBets; j++)
        {
            boolean bPlaceBet = (m_nAdditionalData==1) ? true : (ThreadLocalRandom.current().nextInt(1, m_nAdditionalData+1) == 1);
            if (bPlaceBet)
            {
               aBets[i][j] = bPlaceBet;
                m_nBets++;
            }
        }
    }
    return aBets;
}
   public @Override boolean placeBet(int nRace, int nBet, RealSportsBet bet)
    {
        int nTotalBets = bet.getTotalBetCount(m_nRunners);
        if (!m_hmBets.containsKey(nTotalBets))
        {
            boolean[][] aBets = generateBetArray(nTotalBets);
            m_hmBets.put(nTotalBets, aBets);
        }
        boolean[][] aBets = m_hmBets.get(nTotalBets);
        return aBets[nRace][nBet];
    }
    public @Override int getBetCount(int nTotalBets)
    {
        int nBets = 0;
        if (!m_hmBets.containsKey(nTotalBets))
        {
            boolean[][] aBets = m_hmBets.get(nTotalBets);
            for(int i = 0; i < aBets.length; i++)
            {
                for(int j = 0; j < aBets[i].length; j++)
                {
                    if (aBets[i][j])
                        nBets++;
                }
            }
        }
        
        return nBets;
    }
}
