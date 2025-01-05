/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.attributes;

import ene.eneform.smartform.bos.SmartformHistoricRunner;
import ene.eneform.smartform.bos.SmartformRunner;

/**
 *
 * @author Simon
 */
public class ENEAttributeSmartformRunner implements ENEAttributeContainer {
    
    protected SmartformRunner m_runner;
    
    public ENEAttributeSmartformRunner(SmartformRunner runner)
    {
        m_runner = runner;
    }
    public String getAttribute(String strAttribute)
    {
        if ("name".equalsIgnoreCase(strAttribute))
            return m_runner.getName();
        else if ("jockey".equalsIgnoreCase(strAttribute))
            return m_runner.getJockeyName();
        else if ("trainer".equalsIgnoreCase(strAttribute))
            return m_runner.getTrainerName();
        else if ("owner".equalsIgnoreCase(strAttribute))
            return m_runner.getOwnerName();
       else if ("sire".equalsIgnoreCase(strAttribute))
            return m_runner.getHorse().getSire().getName();
       else if ("dam".equalsIgnoreCase(strAttribute))
            return m_runner.getHorse().getDam().getName();
       else if ("dam_sire".equalsIgnoreCase(strAttribute))
            return m_runner.getHorse().getDamSire().getName();
       else if ("age".equalsIgnoreCase(strAttribute))
            return m_runner.getAgeString();
       else if ("starting_price".equalsIgnoreCase(strAttribute))
            return ((SmartformHistoricRunner)m_runner).getFullStartingPrice();
       else if ("winning_distance".equalsIgnoreCase(strAttribute))
            return ((SmartformHistoricRunner)m_runner).getDistanceWonString();
       else if ("weight".equalsIgnoreCase(strAttribute))
            return SmartformRunner.getWeightString(m_runner.getWeightPounds());
       else if ("weight_headgear".equalsIgnoreCase(strAttribute))
       {
            String strWeight = SmartformRunner.getWeightString(m_runner.getWeightPounds());
            String strHeadgear = m_runner.getTackString();
            if (!"".equals(strHeadgear))
                strWeight += (" " + strHeadgear);
            return strWeight;
       }
        return strAttribute;
    }
}
