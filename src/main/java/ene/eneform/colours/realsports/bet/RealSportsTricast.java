/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bet;

import ene.eneform.colours.realsports.RealSportsRunner;

/**
 *
 * @author Simon
 */
public class RealSportsTricast extends RealSportsBet {
    
    private RealSportsRunner m_first;
    private RealSportsRunner m_second;
    private RealSportsRunner m_third;

    public RealSportsTricast(RealSportsRunner first, RealSportsRunner second, RealSportsRunner third)
    {
        super("T", getProbability(first, second, third));
        m_first = first;
        m_second = second;
        m_third = third;
     }

    public static double getProbability(RealSportsRunner first, RealSportsRunner second, RealSportsRunner third)
    {
        return first.getProbability()* (second.getProbability()/(1 - first.getProbability())) * (third.getProbability()/(1 - first.getProbability() - second.getProbability()));
    }
    public @Override boolean isWinner(int[] anResult)
    {
        return (m_first.getStallNumber() == anResult[0]) && (m_second.getStallNumber() == anResult[1]) && (m_third.getStallNumber() == anResult[2]);
    }
    public @Override String getName()
    {
        return ("Tricast: " + m_first.getStallNumber() + "-" + m_second.getStallNumber() + "-" + m_third.getStallNumber());
    }
    public @Override String getId()
    {
        return (m_first.getStallNumber() + "-" + m_second.getStallNumber() + "-" + m_third.getStallNumber());
    }
   public @Override int getTotalBetCount(int nRunners){return nRunners * (nRunners - 1)* (nRunners - 2);}
}
