/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bet;

import ene.eneform.colours.realsports.bettingpattern.RealSportsBettingPattern;
import ene.eneform.colours.realsports.results.RealSportsScenarioResults;
import ene.eneform.utils.ArithmeticUtils;

/**
 *
 * @author Simon
 */
public class RealSportsBetContainer {
 
    private RealSportsBet m_bet;
    private int m_nBet;     // the bet number, for referencing the pattern e.g. Runner 1 has nBet = 0 for the Winner/Place bet
    private int m_nRaces;
   
    protected double m_dProfit = 0.0d;
    protected int m_nWins = 0;
    protected int m_nBetsPlaced = 0;
    
    public RealSportsBetContainer(RealSportsBet bet, int nBet, int nRaces)
    {
        m_bet = bet;
        m_nBet = nBet;
        m_nRaces = nRaces;
    }
    
    public void processBets(RealSportsBettingPattern pattern, RealSportsScenarioResults results)
    {
        for(int nRace = 0; nRace < m_nRaces; nRace++)
        {
            if (pattern.placeBet(nRace, m_nBet, m_bet))
                placeBet(results.getResult(nRace));
        }
    }
    public double getProfit() {
        return m_dProfit;
    }
    public double getProbability() {
        return m_bet.getProbability();
    }
   public String getId() {
        return m_bet.getId();
    }
    
    public void placeBet(int[] anTricast)
    {
        m_nBetsPlaced++;
        double dProfit = m_bet.processResult(anTricast);
        if (dProfit > -1)
            m_nWins++;
        m_dProfit += dProfit;
    }
    public int getWinCount() {
        return m_nWins;
    }
    public int getBetCount() {
        return m_nBetsPlaced;
    }
    /*
    public void setEnhanced(double dEnhanced) {
        m_bet.setEnhanced(dEnhanced);
    } */
   public String analyse()
    {
         return(m_bet.getName() + " @" + m_bet.getSP() + "- #Bets: " + m_nBetsPlaced + " #Wins: " + m_nWins + " Profit: " + (int)((m_dProfit * 100)/ (double) m_nBetsPlaced) + "% (" + ArithmeticUtils.round(m_dProfit, 2) + ")");
    }
}
