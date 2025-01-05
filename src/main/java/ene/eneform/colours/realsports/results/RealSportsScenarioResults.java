/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.results;

import ene.eneform.colours.realsports.RealSportsBook;
import ene.eneform.colours.realsports.RealSportsRunner;

/**
 *
 * @author Simon
 */
public class RealSportsScenarioResults
    {
        private int m_nGeneratorId;
        private int m_anResult[][];
        private int[][] m_anPositionCount;    // Unplaced, 1st, 2nd, 3rd  for each runner
        private int m_nRaces;
    
      public RealSportsScenarioResults(RealSportsResultGenerator rg, int nRaces)
      {
          m_nGeneratorId = rg.getId();
        m_nRaces = nRaces;
        int nRunners = rg.getNrRunners();
        m_anResult = new int[m_nRaces][3];
        m_anPositionCount = new int[nRunners][4];
        for (int nRace = 0; nRace < m_nRaces; nRace++)
        {
            m_anResult[nRace] = rg.generateResult();
            for(int nRunner = 1; nRunner <= nRunners; nRunner++)
            {
                if (nRunner == m_anResult[nRace][0])
                {
                    // win and place
                    m_anPositionCount[nRunner-1][1]++;
                 }
                else if (nRunner == m_anResult[nRace][1])
                {
                    // win and place
                    m_anPositionCount[nRunner-1][2]++;
                }
                else if (nRunner == m_anResult[nRace][2])
                {
                    // win and place
                    m_anPositionCount[nRunner-1][3]++;
                }
                else
                {
                    // win and place
                    m_anPositionCount[nRunner-1][0]++;
                 }
            }
        }
      }
public void analyze(RealSportsBook book, boolean bTrace)
 {
     for (int nRunner = 0; nRunner < book.getNrRunners(); nRunner++)
     {
         RealSportsRunner runner = book.getRunner(nRunner);
         String strRunner = (getGeneratorId() + "-" + runner.analyse(m_nRaces));
        strRunner += (" First: " + m_anPositionCount[nRunner][1] + " Second: " + m_anPositionCount[nRunner][2] + " Third: " + m_anPositionCount[nRunner][3] + " Unplaced: " + m_anPositionCount[nRunner][0]);
         System.out.println(strRunner);
      }
 }
public int[] getResult(int nRace)
   {
       return m_anResult[nRace];
   }
   public int getNrRaces()
   {
       return m_nRaces;
   }
   public int getGeneratorId()
   {
       return m_nGeneratorId;
   }
}