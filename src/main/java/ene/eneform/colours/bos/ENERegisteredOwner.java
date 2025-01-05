/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class ENERegisteredOwner {
    private int m_nItem;        // item number
    private String m_strOwnerName;
    private ArrayList<ENERegisteredColours> m_arColours = new ArrayList<ENERegisteredColours>();

    public ENERegisteredOwner(String strOwner)
    {
        m_strOwnerName = strOwner;
    }

    public String getOwnerName()
    {
        return m_strOwnerName;
    }
    public void addColours(int nColours, String strDescription, String strLanguage)
    {
        m_arColours.add(new ENERegisteredColours(nColours, strDescription, strLanguage));
    }
    public void setItemNumber(int nItem)
    {
        m_nItem = nItem;
    }
    public int getItemNumber()
    {
        return m_nItem;
    }
    public Iterator<ENERegisteredColours> getColourIterator()
    {
        return m_arColours.iterator();
    }
    public int getNrColours()
    {
        return m_arColours.size();
    }
}
