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
public abstract class RealSportsSingle extends RealSportsBet {
    
    protected RealSportsRunner m_runner;
   public RealSportsSingle(String strType, RealSportsRunner runner)
    {
        super(strType, runner.getSP());
         m_runner = runner;
    }
   public @Override String getId()
    {
        return (String.valueOf(m_runner.getStallNumber()));
    }
   public @Override boolean isSingle(){return true;}
   public @Override int getTotalBetCount(int nRunners){return nRunners;}

}
