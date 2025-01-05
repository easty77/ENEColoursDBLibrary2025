/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports;

import ene.eneform.colours.realsports.bet.RealSportsBet;
import ene.eneform.utils.ArithmeticUtils;

import java.util.ArrayList;

/**
 *
 * @author Simon
 */
public class RealSportsMarket {
    
    private String m_strType;
    private ArrayList<RealSportsBet> m_alBets = new ArrayList<>();
    //private double m_dTotalProbability = 0.0d;
    private double m_dOverround =0.0d;
    private double m_dMargin = 0.0d;
    private double m_dEnhancedOverround =0.0d;
    public RealSportsMarket(String strType)
    {
        m_strType = strType;    // "W", "P"
    }
    public ArrayList<RealSportsBet> getBets()
    {
        return m_alBets;
    }
    public void addBet(RealSportsBet bet)
    {
        m_dOverround += bet.getProbability();
        m_alBets.add(bet);
    }
    public double getOverround()
    {
        return m_dOverround;
    }
    public double getMargin()
    {
        return m_dMargin;
    }
    public String getType()
    {
        return m_strType;
    }
    public void setMargin(double dMargin)
    {
        m_dMargin = dMargin;
        m_dEnhancedOverround = 0.0d;
        for(int i = 0; i < m_alBets.size(); i++)
        {
            RealSportsBet bet = m_alBets.get(i);
            // TO DO: Offer enhanced and normal SP separately not either/or
            //bet.setEnhanced(m_dOverround/m_dMargin);
            
            //m_dEnhancedOverround += bet.getEnhancedProbability();
        }
    }
   public String trace(boolean bDetail)
    {
        String strAnalyse = "";
        if (bDetail)
        {
            for(int i = 0; i < m_alBets.size(); i++)
            {
                RealSportsBet bet = m_alBets.get(i);
                strAnalyse += bet.toString() + "\n";
            }
        }
        strAnalyse  += (m_strType + " Overround: " + (int)(m_dOverround * 100.0) + "%" + " Margin " + ArithmeticUtils.round(getMargin(), 2) + " Revised overround " + ArithmeticUtils.round(getMargin(), 3));
        return strAnalyse;
    }
}
