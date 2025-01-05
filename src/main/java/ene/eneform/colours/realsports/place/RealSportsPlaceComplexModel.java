/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.place;

/**
 *
 * @author Simon
 */
// {{4,4,5},{4,4,5},{4,4,4}}
public class RealSportsPlaceComplexModel extends RealSportsPlaceModel{
    
    private int[][] m_anPlaces;
    
    public RealSportsPlaceComplexModel(String strId, int[][] anPlaces)
    {
        super(strId);
        m_anPlaces = anPlaces;
    }
    public @Override double getRunnerProfit(int nRunners, int nPosition, double dSP)
    {
       if ((nRunners >= 6) && (nRunners <= 8))
       {
           int[] anFractions = m_anPlaces[nRunners - 6];
            if (nPosition <= anFractions.length)
            {
                return (1.0/anFractions[nPosition - 1]) * dSP;
            }

            return -1.0;
       }
       
       return 0.0;
    }
    public @Override int getNrPlaces(int nRunners)
    {
        if ((nRunners >= 6) && (nRunners <= 8))
            return m_anPlaces[nRunners - 6].length;
        return 0;
    }
    public @Override double getPlaceDecimal(int nRunners)
    {
        if ((nRunners >= 6) && (nRunners <= 8))
            return (1.0d/m_anPlaces[nRunners - 6][0]);
        
        return 0.0d;
    }
    public String getPlaceDecimalString(int nRunners)
    {
        if ((nRunners >= 6) && (nRunners <= 8))
            return String.valueOf(m_anPlaces[nRunners - 6]);
        
        return "";
    }
    public @Override String getDescription(int nRunners, double dSP)
    {
        return (getId() + " #Places " + getNrPlaces(nRunners) + " Odds " + getPlaceDecimal(nRunners) + " @ " +  dSP);        
    }
}
