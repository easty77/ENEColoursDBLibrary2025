/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports.bet;

import ene.eneform.colours.realsports.RealSportsRunner;

/**
 *
 * @author Simon
 */
public class RealSportsWin extends RealSportsSingle {
    
    public RealSportsWin(RealSportsRunner runner)
    {
        super("W", runner);
    }
    public @Override boolean isWinner(int[] anResult)
    {
        return (m_runner.getStallNumber() == anResult[0]);
    }
    public @Override String getName()
    {
        return ("Win " + m_runner.getStallNumber());
    }
}
