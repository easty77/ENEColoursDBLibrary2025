/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bet;

import ene.eneform.colours.realsports.RealSportsRunner;

import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public class RealSportsGroupBet extends RealSportsBet{
    protected String m_strGroupId;
    protected ArrayList<RealSportsRunner> m_runners;
   public RealSportsGroupBet(String strType, String strId, ArrayList<RealSportsRunner> runners)
    {
        super(strType, getProbability(runners));
        m_strGroupId = strId;
         m_runners = runners;
    }
    public static double getProbability(ArrayList<RealSportsRunner> runners)
    {
        double dProbability = 0.0d;
        for(int i = 0; i < runners.size(); i++)
        {
            dProbability += runners.get(i).getProbability();
        }
        return dProbability;
    }
   public @Override String getId()
    {
        return m_strGroupId;
    }
   public @Override String getName()
    {
        return m_strType;
    }
   public @Override boolean isWinner(int[] anResult)
    {
        for(int i = 0; i < m_runners.size(); i++)
        {
            if (m_runners.get(i).getStallNumber() == anResult[0])
                return true;
        }
        return false;
    }
   public @Override int getTotalBetCount(int nRunners){return 2;}   // odd/even high/low
}
