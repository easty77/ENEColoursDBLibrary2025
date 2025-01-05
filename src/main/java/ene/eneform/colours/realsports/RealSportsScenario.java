/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports;

import ene.eneform.colours.realsports.bet.RealSportsBet;
import ene.eneform.colours.realsports.bet.RealSportsBetContainer;
import ene.eneform.colours.realsports.bettingpattern.RealSportsBettingPattern;
import ene.eneform.colours.realsports.place.RealSportsPlaceComplexModel;
import ene.eneform.colours.realsports.place.RealSportsPlaceDefaultModel;
import ene.eneform.colours.realsports.place.RealSportsPlaceModel;
import ene.eneform.colours.realsports.results.RealSportsResultGenerator;
import ene.eneform.colours.realsports.results.RealSportsScenarioResults;
import ene.eneform.utils.ArithmeticUtils;
import ene.eneform.utils.ENEStatement;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
    public class RealSportsScenario
    {
    private ENEStatement m_statement;    
     private Timestamp m_timestamp;
     private RealSportsBook m_book;
     private int m_nRunners;
     private ArrayList<RealSportsResultGenerator> m_alResultGenerators = new ArrayList<>();
    private ArrayList<RealSportsBettingPattern> m_alBettingPatterns = new ArrayList<>();
    
              
     public static final RealSportsPlaceModel sm_placeModel = new RealSportsPlaceDefaultModel("P", new int[]{2,2,3}, new int[]{4, 4, 5});  // standard
     private static final RealSportsPlaceModel sm_placeModel3 = new RealSportsPlaceDefaultModel("P3", new int[]{3,3,3}, new int[]{4, 4, 5}); // 3rd place for 6+7 runner races
     private static final RealSportsPlaceModel sm_placeModel1a = new RealSportsPlaceComplexModel("P1A", new int[][]{{4,4},{4,4},{5,5,5}}); // different coding of standard
     private static final RealSportsPlaceModel sm_placeModel1b = new RealSportsPlaceComplexModel("P1B", new int[][]{{4,4,5},{4,4,5},{5,5,5}}); // 3rd place pays 1/5 for 6+7 runner races


    public RealSportsScenario(ENEStatement statement, RealSportsBook book, ArrayList<RealSportsResultGenerator> alResultGenerators, ArrayList<RealSportsBettingPattern> alBettingPatterns)
    {
        // One race
        // Multiple bets = types x patterns
        // Multiple result generators
        m_statement = statement;
        m_book = book;
        m_nRunners = book.getNrRunners();
        m_alResultGenerators = alResultGenerators;
        m_alBettingPatterns = alBettingPatterns;
        m_timestamp = new Timestamp((new Date()).getTime());
     }

    public void generateRaces(int nRaces, boolean bTrace)
    {
        System.out.println("#Races: " + nRaces);
        for(int nBettingPattern= 0; nBettingPattern < m_alBettingPatterns.size(); nBettingPattern++)
         {
             // to do: expand so that hash table of Betting Patterns for Single, Forecast and Tricast
             RealSportsBettingPattern pattern = m_alBettingPatterns.get(nBettingPattern);
             pattern.generateBets(nRaces, m_book.getRunners());
         }

        RealSportsFactory.insertScenario(m_statement, m_timestamp, m_book.getSPs(), m_nRunners, nRaces);
        for(int i = 0; i < m_alResultGenerators.size(); i++)
        {
            RealSportsResultGenerator generator = m_alResultGenerators.get(i);
            RealSportsScenarioResults results = new RealSportsScenarioResults(generator, nRaces);
            results.analyze(m_book, bTrace);
            Iterator<String> iter = m_book.getMarketTypes();
            while(iter.hasNext())
             {
                 
                 String strMarketType = iter.next();
                 RealSportsMarket market = m_book.getMarket(strMarketType);
                 for(int nBettingPattern= 0; nBettingPattern < m_alBettingPatterns.size(); nBettingPattern++)
                 {
                     RealSportsBettingPattern pattern = m_alBettingPatterns.get(nBettingPattern);
                     if (pattern.supportsType(strMarketType))
                     {
                        // combine bets with results
                        RealSportsScenarioModel model = new RealSportsScenarioModel(market, pattern, results);
                        model.processBets();
                        System.out.println(model.analyse());
                     }
                  }
             }
        }
     }

    
     private class RealSportsScenarioModel // bets of a specific type placed on the runners of a  race run multiple times
    {
    private RealSportsMarket m_market;
    private RealSportsBettingPattern m_pattern;
    private RealSportsScenarioResults m_results;
    private int m_nRaces;
    private ArrayList<RealSportsBetContainer> m_alBets = new ArrayList<>();
    private double m_dProfit =0.0d;
    private int m_nBetCount = 0;        // number of runners showing overall Win profit
    private int m_nWinCount = 0;        // number of runners showing overall Win profit
    
    public RealSportsScenarioModel(RealSportsMarket market, RealSportsBettingPattern pattern, RealSportsScenarioResults results)
    {
        m_market = market;
        m_pattern = pattern;
        m_results = results;
        m_nRaces = results.getNrRaces();
        ArrayList<RealSportsBet> bets = m_market.getBets();
        for(int nBet = 0; nBet < bets.size(); nBet++)
        {
            RealSportsBet bet = bets.get(nBet);
            m_alBets.add(new RealSportsBetContainer(bet, nBet, m_nRaces));
        }
     }

        public double getOverallProfit()
        {
            return m_dProfit;
        }
        public int getWinCount()
        {
             return m_nWinCount;
        }
        public int getBetCount()
        {
             return m_nBetCount;
        }
    public void processBets()
    {
        for (int nBet = 0; nBet < m_alBets.size(); nBet++)
        {
            RealSportsBetContainer bet = m_alBets.get(nBet);
            bet.processBets(m_pattern, m_results);
            System.out.println(bet.analyse());
            int nBets = bet.getBetCount();
            int nWins = bet.getWinCount();
            double dProfit = bet.getProfit();
            int nPctProfit = (int)(dProfit * 100)/nBets;
            RealSportsFactory.insertRunner(m_statement, m_timestamp, m_market.getType(), m_pattern.getId(), bet.getId(), nBets, nPctProfit, nWins, m_results.getGeneratorId());
            if (dProfit > -1)
                m_nWinCount++;
            m_dProfit += dProfit;
            m_nBetCount += bet.getBetCount();
        }
         
       int nPctProfit = 0;
       if (m_nBetCount > 0)
           nPctProfit = (int)(100 * m_dProfit)/m_nBetCount;
        RealSportsFactory.insertBet(m_statement, m_timestamp, m_market.getType(), m_pattern.getId(), getBetCount(), nPctProfit, getWinCount(), m_results.getGeneratorId());
    }
 
        public String analyse()
        {
           int nPctProfit = 0;
            if (m_nBetCount > 0)
                nPctProfit = (int)(getOverallProfit() * 100)/m_nBetCount;
            return ("Overall: " + m_results.getGeneratorId() +  "-" + m_pattern.getId() + "-" + m_market.getType()+ " Overround " + ArithmeticUtils.round(m_market.getOverround(), 2) + " Margin " + ArithmeticUtils.round(m_market.getMargin(), 2) + ": #Bets " + m_nBetCount + " Profit " + nPctProfit + "% (" + ArithmeticUtils.round(getOverallProfit(), 2) + ", " + getWinCount() + ")");
        }
    }
}

