/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class RacingPostHorseOwner {
    
    private String m_strCurrentOwner;

    private ArrayList<RacingPostPreviousOwner> m_alPreviousOwners = new ArrayList<RacingPostPreviousOwner>();
    public RacingPostHorseOwner(String strCurrentOwner)
    {
        m_strCurrentOwner = strCurrentOwner;
    }
    public void addPreviousOwner(String strOwner, Calendar untilDate)
    {
        m_alPreviousOwners.add(new RacingPostPreviousOwner(strOwner, untilDate));
    }
    
    public String getCurrentOwner() {
        return m_strCurrentOwner;
    }

    public String getOwnerOnDate(Calendar dtQuery)
    {
        String strOwner = m_strCurrentOwner;
        Iterator<RacingPostPreviousOwner> iter = m_alPreviousOwners.iterator();
        while(iter.hasNext())
        {
            RacingPostPreviousOwner prev = iter.next();
            if (prev.getUntilDate().compareTo(dtQuery) < 0)
                break;
            strOwner = prev.getOwner();
        }
        
        return strOwner;
    }
private class RacingPostPreviousOwner
{
    private String m_strOwner;
    private Calendar m_untilDate;

    public RacingPostPreviousOwner(String strOwner, Calendar untilDate)
    {
        m_strOwner = strOwner;
        m_untilDate = untilDate;
    }
        public String getOwner() {
            return m_strOwner;
        }

        public Calendar getUntilDate() {
            return m_untilDate;
        }
}
}
