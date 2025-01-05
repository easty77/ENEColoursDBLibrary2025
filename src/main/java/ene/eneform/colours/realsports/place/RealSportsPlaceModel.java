/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.place;

/**
 *
 * @author Simon
 */
public abstract class RealSportsPlaceModel {
    
    private String m_strId;
    
    public RealSportsPlaceModel(String strId)
    {
        m_strId = strId;
    }
    public String getId()
    {
        return m_strId;
    }
   
    public abstract String getDescription(int nRunners, double dSP);
    public abstract double getRunnerProfit(int nRunners, int nPosition, double dSP);
    public abstract int getNrPlaces(int nRunners);
    public abstract double getPlaceDecimal(int nRunners);
}
