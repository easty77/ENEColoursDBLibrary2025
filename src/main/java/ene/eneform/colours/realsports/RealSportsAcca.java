/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports;

import ene.eneform.colours.realsports.bet.RealSportsBet;
import ene.eneform.colours.realsports.bet.RealSportsWin;
import ene.eneform.colours.realsports.results.RealSportsResultGenerator;
import ene.eneform.utils.HorseRacingUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author simon
 */
public class RealSportsAcca extends RealSportsBet {
    
    RealSportsWin[] m_aSelections;
    public RealSportsAcca(RealSportsWin[] aSelections)
    {
        super("A", 0);      // don't know probability untill all selections are assigned
        m_aSelections = aSelections;
        m_dProbability = m_aSelections[0].getProbability();
        if (m_aSelections.length > 1)
        {
            for(int i = 1; i < m_aSelections.length; i++)
            {
                m_dProbability = m_dProbability * m_aSelections[i].getProbability();
            }
        }
        m_dSP = 1/m_dProbability - 1;

    }
    public @Override String getName()
    {
        String strName = "Acca ";
        for (int i = 0; i < m_aSelections.length; i++)
        {
            RealSportsWin win = m_aSelections[i];
            strName += (win.getName() + " " + win.getSP() + " "); 
        }
        return strName;
    }
    public @Override String getId(){return "Acca";}
    public @Override int getTotalBetCount(int nRunners)
    {
        return (int) Math.pow(nRunners, m_aSelections.length);
    }
    public boolean isWinner(int[] aResult)
    {
        // aResult is the WINNER of each of the races
        for (int i = 0; i < m_aSelections.length; i++)
        {
            if (!m_aSelections[i].isWinner(new int[]{aResult[i], 0, 0}))       // all best must win
                return false;
        }
        return true;
    }
    public int getNrBets()
    {
        return 1;
    }
    public String getSP() {
        return HorseRacingUtils.convertDecimalToSP((1.0d/m_dProbability) - 1);
    }
    
    public static List<RealSportsAcca> createAccaList(List<RealSportsRunner> alRunners, int nRaces)
    {
        List<RealSportsAcca> alAccas = new ArrayList<RealSportsAcca>();
        List<RealSportsWin[]> alWinners = new ArrayList<RealSportsWin[]>();
        for (int nRace = 1; nRace <= nRaces; nRace++)
        {
            alWinners = generateWinnerArrayLevel(alWinners, alRunners);
        }
        for(int i = 0; i < alWinners.size(); i++)
        {
            RealSportsAcca acca = new RealSportsAcca(alWinners.get(i));
            alAccas.add(acca);
        }
        return alAccas;
    }
    private static  List<RealSportsWin[]> generateWinnerArrayLevel(List<RealSportsWin[]> alWinners, List<RealSportsRunner> alRunners)
    {
        List<RealSportsWin[]> alNewWinners = new ArrayList<RealSportsWin[]>();
        if (alWinners.size() == 0)
        {
            // this is first level
            for (int j = 0; j < alRunners.size(); j++)
            {
                RealSportsWin win = new RealSportsWin(alRunners.get(j));
                alNewWinners.add(new RealSportsWin[]{win});
            }
        }
        else
        {
            for (int i = 0; i < alWinners.size(); i++)
            {
                RealSportsWin[] current = alWinners.get(i);
                for (int j = 0; j < alRunners.size(); j++)
                {
                    RealSportsWin[] win = new RealSportsWin[current.length + 1];
                    for(int k = 0; k < current.length; k++)
                    {
                        win[k] = current[k];
                    }
                    win[current.length] = new RealSportsWin(alRunners.get(j));
                    alNewWinners.add(win);
                }
            }
        }
        return alNewWinners;
    }
      public static void GenerateAcca(RealSportsBook book, List<RealSportsRunner> alRunners, int nRaces, long nRepeats)
       {
            double dEnhanced = 1.0;
            if (nRaces >= 4)
                dEnhanced = 1.5;
            else if (nRaces >= 3)
                dEnhanced= 1.3;
            else if (nRaces >= 2)
                dEnhanced = 1.1;
           List<RealSportsAcca> alAccas = RealSportsAcca.createAccaList(alRunners, nRaces);
           RealSportsResultGenerator generator = new RealSportsResultGenerator(0, book);
           
           long nOverallProfit = 0;
           long nOverallEnhancedProfit = 0;
           
           for(int nRepeat = 0; nRepeat < nRepeats; nRepeat++)
           {
                
                int[] aWinners = new int[nRaces];
                for(int nRace = 0; nRace < nRaces; nRace++)
                {
                     int[] aResult = generator.generateResult();     // tricast
                     aWinners[nRace]= aResult[0];           // only interested in the winner
                }
                //System.out.println("Results: " + Arrays.toString(aWinners));
                for (int nAcca = 0; nAcca < alAccas.size(); nAcca++)
                {
                    RealSportsAcca acca = alAccas.get(nAcca);
                    if (acca.isWinner(aWinners))
                    {
                        //System.out.println("Winner: " + acca.getName() + acca.getDecimalSP());
                        nOverallProfit += acca.getDecimalSP();
                        nOverallEnhancedProfit += (acca.getDecimalSP() * dEnhanced);
                    }
                    else
                    {
                        nOverallProfit--;
                        nOverallEnhancedProfit--;
                    }
                }
           }
           System.out.println("Overall: " + nRaces + " Combinations " + alAccas.size() + " Total bets " +  (nRepeats * alAccas.size()) + " Overall Profit  " + nOverallProfit + "   " + (100 * nOverallProfit)/(nRepeats * alAccas.size()) + "%  Enhanced " + ((int)(dEnhanced * 100)) + "% " + nOverallEnhancedProfit + "   " + (nOverallEnhancedProfit * 100)/(nRepeats * alAccas.size()) + "%");
       }
}
