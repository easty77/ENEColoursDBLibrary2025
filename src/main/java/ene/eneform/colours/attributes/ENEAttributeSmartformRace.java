/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.attributes;

import ene.eneform.smartform.bos.SmartformRace;

/**
 *
 * @author Simon
 */
public class ENEAttributeSmartformRace implements ENEAttributeContainer{
    
    protected SmartformRace m_race;
    
    public ENEAttributeSmartformRace(SmartformRace race)
    {
        m_race = race;
    }
    public String getAttribute(String strAttribute)
    {
        if ("date".equalsIgnoreCase(strAttribute))
            return m_race.getFormattedMeetingDate();
        if ("date_course".equalsIgnoreCase(strAttribute))
            return m_race.getFormattedMeetingDate() + " at " + m_race.getCourse().replace("_", " ");
        else if ("course".equalsIgnoreCase(strAttribute))
            return m_race.getCourse().replace("_", " ");
        else if ("title".equalsIgnoreCase(strAttribute))
            return m_race.getAbbreviatedTitle();
        else if ("going".equalsIgnoreCase(strAttribute))
            return m_race.getGoing();
        else if ("distance".equalsIgnoreCase(strAttribute))
            return m_race.getLongFormattedDistance(true);
        else if ("nr_runners".equalsIgnoreCase(strAttribute))
            return String.valueOf(m_race.getNrRunners()) + " Ran";

        return strAttribute;
    }
}
