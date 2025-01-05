/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports;

import ene.eneform.colours.realsports.bet.*;

import java.util.*;

/**
 *
 * @author Simon
 */
public class RealSportsBook {
    private int m_nRunners;
    private String m_strSPs = "";       // unique identifier, assumes SPs are in ascending order

    private ArrayList<RealSportsRunner> m_alRunners = new ArrayList<>();
    private HashMap<String,RealSportsMarket> m_hmMarkets = new HashMap<>();
 
    public RealSportsBook(String[] astrSPs, boolean bShuffle, String[] astrBetTypes)
    {
        m_nRunners = astrSPs.length;
        for(int i = 0; i < m_nRunners; i++)
        {
            RealSportsRunner runner = new RealSportsRunner(astrSPs[i]);
            m_alRunners.add(runner);
            if (!"".equals(m_strSPs))
                m_strSPs += ", ";
            m_strSPs += astrSPs[i];
        }
        if (bShuffle)
        {
            long seed = System.nanoTime();
            Collections.shuffle(m_alRunners, new Random(seed));
        }
        for(int i = 0; i < m_nRunners; i++)
        {
            RealSportsRunner runner = m_alRunners.get(i);
            runner.setStallNumber(i+1);
        }
        for(int i = 0; i < astrBetTypes.length; i++)
        {
            RealSportsMarket market = createMarket(astrBetTypes[i]);
            m_hmMarkets.put(astrBetTypes[i], market);
        }
     }
    public String trace(boolean bDetail)
    {
        String strOutput="";
        Iterator<RealSportsRunner> iterrun = m_alRunners.iterator();
        if (bDetail)
        {
            while (iterrun.hasNext())
            {
                RealSportsRunner runner = iterrun.next();
                strOutput += (runner.toString() + "\n");
            }
        }
       Iterator<String> iter = getMarketTypes();
       while (iter.hasNext())
       {
            RealSportsMarket market = m_hmMarkets.get(iter.next());
            strOutput += (market.trace(bDetail) + "\n");
        }
        
        return strOutput;
    }
    public ArrayList<RealSportsRunner> getRunners()
    {
        return m_alRunners;
    }
   public RealSportsRunner getRunner(int nRunner)
    {
        if ((nRunner >= 0) && (nRunner < m_alRunners.size()))
            return m_alRunners.get(nRunner);
        return null;
    }
     public String getSPs()
    {
        return m_strSPs;
    }
    public int getNrRunners(){return m_alRunners.size();}
    public Iterator<String> getMarketTypes()
    {
        return m_hmMarkets.keySet().iterator();
    }
    public RealSportsMarket getMarket(String strType)
    {
        return m_hmMarkets.get(strType);
    }
    private RealSportsMarket createMarket(String strType)
    {
        RealSportsMarket market = new RealSportsMarket(strType);
        if ("W".equals(strType))
        {
            for(int i = 0; i < m_nRunners; i++)
            {
                market.addBet(new RealSportsWin(getRunner(i)));
            }
        }
        else if ("P".equals(strType))
        {
            
            for(int i = 0; i < m_nRunners; i++)
            {
                market.addBet(new RealSportsPlace(getRunner(i), RealSportsScenario.sm_placeModel, m_nRunners));
            }
        }
        else if ("F".equals(strType))
        {
            for(int i = 0; i < m_nRunners; i++)
            {
                for(int j = 0; j < m_nRunners; j++)
                {
                    if (i != j)
                    {
                        market.addBet(new RealSportsForecast(getRunner(i), getRunner(j)));
                     }
                }
            }
        }
        else if ("T".equals(strType))
        {
            for(int i = 0; i < m_nRunners; i++)
            {
                for(int j = 0; j < m_nRunners; j++)
                {
                    if (i != j)
                    {
                        for(int k = 0; k < m_nRunners; k++)
                        {
                            if ((k != i) && (k != j))
                            {
                                market.addBet(new RealSportsTricast(getRunner(i), getRunner(j), getRunner(k)));
                            }
                         }
                    }
                }
            }
        }
        else if ("O".equals(strType))
        {
            // odd/even
            ArrayList<RealSportsRunner> odd = new ArrayList<>();
            ArrayList<RealSportsRunner> even = new ArrayList<>();
            
            for(int i = 0; i < m_nRunners; i++)
            {
                if ((i+1)%2 == 0)
                    even.add(getRunner(i));
                else
                    odd.add(getRunner(i));
            }
            market.addBet(new RealSportsGroupBet(strType, "Odd", odd));
            market.addBet(new RealSportsGroupBet(strType, "Even", even));
        }
       
        else if ("H".equals(strType))
        {
            // high/low
            ArrayList<RealSportsRunner> high = new ArrayList<>();
            ArrayList<RealSportsRunner> low = new ArrayList<>();
            
            for(int i = 0; i < m_nRunners; i++)
            {
                if ((i+1)*2 > m_nRunners)
                    high.add(getRunner(i));
                else
                    low.add(getRunner(i));
            }
            market.addBet(new RealSportsGroupBet(strType, "High", high));
            market.addBet(new RealSportsGroupBet(strType, "Low", low));
        }
       
        
        return market;
    }
}
