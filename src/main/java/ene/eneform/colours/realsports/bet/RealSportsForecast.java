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
public class RealSportsForecast extends RealSportsBet {
    private RealSportsRunner m_first;
    private RealSportsRunner m_second;

    public RealSportsForecast(RealSportsRunner first, RealSportsRunner second)
    {
        super("F", getProbability(first, second));
        m_first = first;
        m_second = second;
    }
    public static double getProbability(RealSportsRunner first, RealSportsRunner second)
    {
        return first.getProbability()* (second.getProbability()/(1 - first.getProbability()));
    }
    public @Override boolean isWinner(int[] anResult)
    {
        return (m_first.getStallNumber() == anResult[0]) && (m_second.getStallNumber() == anResult[1]);
    }
    public @Override String getName()
    {
        return ("Forecast: " + m_first.getStallNumber() + "-" + m_second.getStallNumber());
    }
    public @Override String getId()
    {
        return (m_first.getStallNumber() + "-" + m_second.getStallNumber());
    }
    public @Override int getTotalBetCount(int nRunners){return nRunners * (nRunners - 1);}
}
