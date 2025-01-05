/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.attributes;

import ene.eneform.colours.bos.ENETopRace;

/**
 *
 * @author Simon
 */
public class ENEAttributeTopRace implements ENEAttributeContainer {
    
    protected ENETopRace m_race;
    
    public ENEAttributeTopRace(ENETopRace race)
    {
        m_race = race;
    }
    public String getAttribute(String strAttribute)
    {
        if ("title".equalsIgnoreCase(strAttribute))
            return m_race.getTitle();
        if ("course".equalsIgnoreCase(strAttribute))
            return m_race.getCourse();
        else if ("country".equalsIgnoreCase(strAttribute))
            return m_race.getCountry();
        else if ("conditions".equalsIgnoreCase(strAttribute))
            return m_race.getConditions();
        else if ("age_range".equalsIgnoreCase(strAttribute))
            return m_race.getAgeRange();
        else if ("date".equalsIgnoreCase(strAttribute))
            return m_race.getDateDescription();
        else if ("distance".equalsIgnoreCase(strAttribute))
            return String.valueOf(m_race.getDistance()) + " " + m_race.getDistanceUnits();

        return strAttribute;
    }
}
