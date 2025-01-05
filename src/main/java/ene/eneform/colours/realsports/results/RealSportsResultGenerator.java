/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.results;

import ene.eneform.colours.realsports.RealSportsBook;
import ene.eneform.colours.realsports.RealSportsRunner;
import ene.eneform.utils.ArithmeticUtils;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Simon
 */
public class RealSportsResultGenerator {
    
    int m_nId;
    boolean m_bRecalibrate;
    int m_nModel = 3;   // All Result Generators using 1000 for weighting
    private int m_anWeightings[];
    private double m_dTotalProbability;
    private ArrayList<RealSportsRunner> m_alRunners;
    
    public RealSportsResultGenerator(int nId, RealSportsBook book)
    {
        m_nId = nId;
        m_alRunners = book.getRunners();
        m_dTotalProbability = book.getMarket("W").getOverround();        // what is this?  Why Win market?
        m_bRecalibrate = (nId == 1);
        m_anWeightings = new int[m_alRunners.size()];
        initialise();
    }
    private void initialise()
    {
        if (m_nId <= 1)
        {
            for(int i = 0; i < m_alRunners.size(); i++)
            {
                RealSportsRunner runner = m_alRunners.get(i);
                int nWeighting;
                if (m_bRecalibrate)
                    nWeighting = (int)((Math.pow(10, m_nModel) * ArithmeticUtils.round(runner.getProbability()/m_dTotalProbability, m_nModel)));
                else
                    nWeighting = (int)((Math.pow(10, m_nModel) * ArithmeticUtils.round(runner.getProbability(), m_nModel)));
                m_anWeightings[i] = nWeighting;
            }
        }
        else
        {
            for(int i = 0; i < m_alRunners.size(); i++)
            {
                m_anWeightings[i] = 1;      // assign equal probability to all runners
            }
        }
        
     }
    public int getNrRunners(){return m_alRunners.size();}
    
   public int[] generateResult()
    {
        int[] anWeighting = new int[m_alRunners.size()];
        System.arraycopy( m_anWeightings, 0, anWeighting, 0, m_anWeightings.length );
        int[] anTricast = new int[3];
        for (int k = 0; k < 3; k++)
        {
            int[] anExpanded = expandWeightingArray(anWeighting);
            //System.out.println((k + 1) + ". Array of length: " + anExpanded.length);

            int nRandom = ThreadLocalRandom.current().nextInt(0, anExpanded.length);
            //System.out.println((k + 1) + ". Random: " + nRandom);

            anTricast[k] = anExpanded[nRandom];
            //System.out.println((k + 1) + ". RNG: " + anTricast[k]);

           //System.out.println((k + 1) + ". Reset: " + anWeighting[anTricast[k] - 1]);
           anWeighting[anTricast[k] - 1] = 0;
        }
        
        return anTricast;
    }
    private int[] expandWeightingArray(int[] anWeighting)
    {
        int nTotal = 0;
        for(int i = 0; i < anWeighting.length; i++)
        {
            nTotal += anWeighting[i];
        }
        int [] anExpanded = new int[nTotal];
        int nCount = 0;
        for(int i = 0; i < anWeighting.length; i++)
        {
            for(int j = 0; j < anWeighting[i]; j++)
            {
                anExpanded[nCount++] = i+1;
            }
        }
        
        return anExpanded;
    }
    public int getId()
    {
        return m_nId;
    }
}
