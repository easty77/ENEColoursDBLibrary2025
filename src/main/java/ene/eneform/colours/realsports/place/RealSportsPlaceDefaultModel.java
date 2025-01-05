/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.place;

/**
 *
 * @author Simon
 */
public class RealSportsPlaceDefaultModel extends RealSportsPlaceModel{
    
    private String m_strId;
    private int[] m_anPlaces;
    private int[] m_anFractions;
    
    public RealSportsPlaceDefaultModel(String strId, int[] anPlaces, int[] anFractions)
    {
        super(strId);
        m_anPlaces = anPlaces;
        m_anFractions = anFractions;
    }
    public @Override int getNrPlaces(int nRunners)
    {
        if ((nRunners >= 6) && (nRunners <= 8))
            return m_anPlaces[nRunners - 6];
        return 0;
    }
    public @Override double getPlaceDecimal(int nRunners)
    {
        if ((nRunners >= 6) && (nRunners <= 8))
            return (1.0/m_anFractions[nRunners - 6]);
        
        return 0.0;
    }
    
    public @Override double getRunnerProfit(int nRunners, int nPosition, double dSP)
    {
       if ((nRunners >= 6) && (nRunners <= 8))
       {
            if (nPosition <= getNrPlaces(nRunners))
            {
                return (1.0/m_anFractions[nRunners - 6]) * dSP;
            }

            return -1.0;
       }
       
       return 0.0;
    }
    public @Override String getDescription(int nRunners, double dSP)
    {
        return (getId() + " #Places " + getNrPlaces(nRunners) + " Odds " + getPlaceDecimal(nRunners) * dSP);        
    }
}
