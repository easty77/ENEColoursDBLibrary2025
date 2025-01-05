/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bet;

import ene.eneform.utils.ArithmeticUtils;
import ene.eneform.utils.HorseRacingUtils;

/**
 *
 * @author Simon
 */
public abstract class RealSportsBet {
    protected String m_strType;     // W, P, F, T, P1, P2 ...
    private String m_strSP="";
    protected double m_dProbability = 0.0d;
    protected double m_dSP = 0;
    private double m_dEnhanced = 1.0f;        // factor by which to enhance bet
     
    public RealSportsBet(String strType, double dProbability)
    {
        m_strType = strType;
        m_strSP = HorseRacingUtils.convertDecimalToSP((1.0d/dProbability) - 1);   // and then convert back
        m_dSP = HorseRacingUtils.convertSP(m_strSP);
        m_dProbability = 1.0d/(1.0d + m_dSP);
    }
    public RealSportsBet(String strType, String strSP)
    {
        m_strType = strType;
        m_strSP = strSP;
        m_dSP = HorseRacingUtils.convertSP(strSP);
        m_dProbability = 1/(1 + m_dSP);
    }

    public String getType() {
        return m_strType;
    }
     public String getSP() 
     {
        return m_strSP;
    }
     public double getDecimalSP() 
     {
        return (1.0d/m_dProbability) - 1;
    }
     public double getEnhancedDecimalSP(double dEnhanced) 
     {
       return (1.0d/getEnhancedProbability(dEnhanced)) - 1;
    }
    public double getProbability() 
    {
        return m_dProbability;
    }
    public double getEnhancedProbability(double dEnhanced) 
    {
        return m_dProbability/dEnhanced;
    }
    public String getEnhancedSP(double dEnhanced) 
    {
        return HorseRacingUtils.convertDecimalToSP((1.0d/getEnhancedProbability(dEnhanced)) - 1);
    }
    /*
    public void setEnhanced(double dEnhanced) {
        m_dEnhanced = dEnhanced;
    } */
    public boolean isSingle(){return false;}
    public abstract String getName();
    public abstract String getId();
    public abstract int getTotalBetCount(int nRunners);
    public abstract boolean isWinner(int[] anResult);
    protected double processResult(int[] anResult)
    {
        double dProfit;
        if (isWinner(anResult))
        {
            dProfit = getDecimalSP();       // ((1.0/m_dProbability) - 1);
        }
        else
        {
            dProfit = -1;
          }
        
        return dProfit;
    }
    public @Override String toString()
    {
        return (getId() + "-" + getName() + "-" + m_strSP + ", " + ArithmeticUtils.round(getProbability(), 2));
    }

 }
