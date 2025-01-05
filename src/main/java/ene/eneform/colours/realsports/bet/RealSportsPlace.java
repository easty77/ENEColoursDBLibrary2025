/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bet;

import ene.eneform.colours.realsports.RealSportsRunner;
import ene.eneform.colours.realsports.place.RealSportsPlaceModel;

/**
 *
 * @author Simon
 */
public class RealSportsPlace extends RealSportsSingle{
    
    private RealSportsPlaceModel m_model;
    private int m_nRunners;

    public RealSportsPlace(RealSportsRunner runner, RealSportsPlaceModel model, int nRunners)
    {
        super(model.getId(), runner);
        // nPlaceFraction 5 = 1/5, 4 = 1/4 etc
        m_model = model;
        m_nRunners = nRunners;
    }
    protected @Override double processResult(int[] anResult)
    {
        return m_model.getRunnerProfit(m_nRunners, getPosition(anResult), getDecimalSP());
    }
    private int getPosition(int[] anResult)
    {
        if (m_runner.getStallNumber() == anResult[0])
            return 1;
        else if (m_runner.getStallNumber() == anResult[1])
            return 2;
        else if (m_runner.getStallNumber() == anResult[2])
            return 3;
        
        return 4;
    }
    public @Override boolean isWinner(int[] anResult)
    {
        return (getPosition(anResult) <= m_model.getNrPlaces(m_nRunners));
    }

    public @Override String getName()
    {
        return ("Place : " + m_runner.getStallNumber() + " " + m_model.getDescription(m_nRunners, getDecimalSP()));
    }
    public @Override double getProbability() {
        double dProbability = 1/(1 + (m_dSP * m_model.getPlaceDecimal(m_nRunners)));
        return dProbability  / m_model.getNrPlaces(m_nRunners);
    }
}
