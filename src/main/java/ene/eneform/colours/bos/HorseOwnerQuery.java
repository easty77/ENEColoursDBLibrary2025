/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.bos;

import ene.eneform.colours.web.rp.RacingPostHorseOwner;

import java.util.Calendar;

/**
 *
 * @author Simon
 * The information needed to retrieve an owner for a given horse in a given race
 * The horse (including bred) and the date of the race
 */
public class HorseOwnerQuery {
    
    private String m_strName;
    private String m_strBred;
    private Calendar m_dtRace;
     
    public HorseOwnerQuery(String strName, String strBred, Calendar dtRace)
    {
        m_strName = strName;
        m_strBred = strBred;
        m_dtRace = dtRace;
    }
 
        public String getName() {
            return m_strName;
        }
        public String getBred() {
            return m_strBred;
        }
        public Calendar getRaceDate() {
            return m_dtRace;
        }
        
        public String getOwner(RacingPostHorseOwner owner)
        {
            return owner.getOwnerOnDate(m_dtRace);
        }
}
