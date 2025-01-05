/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.bos;

import ene.eneform.smartform.bos.SmartformHistoricRunner;
import ene.eneform.smartform.bos.SmartformRace;
import ene.eneform.smartform.bos.SmartformRace123;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
public class WikipediaRace {
    
    private AdditionalRaceData m_ard;
    private ArrayList<SmartformRace123> m_alRaces;
    private ArrayList<String> m_alKeyWords = null;
    
    public WikipediaRace(AdditionalRaceData ard, ArrayList<SmartformRace123> alRaces)
    {
        m_ard = ard;
        m_alRaces = alRaces;
    }
    
    public AdditionalRaceData getAdditionalRaceData()
    {
        return m_ard;
    }
    public Iterator<SmartformRace123> getRaceIterator()
    {
        return m_alRaces.iterator();
    }
    public ArrayList<String> getKeyWords()
    {
        if (m_alKeyWords == null)
        {
            m_alKeyWords = new ArrayList<String>();
            addKeyWord(m_ard.getName());
            addKeyWord(m_ard.getTitle());
            Iterator<SmartformRace123> iter = m_alRaces.iterator();
            while (iter.hasNext())
            {
                SmartformRace123 race123 = iter.next();
                SmartformRace race = race123.getRace();
                addKeyWord(race.getTitle());
                Iterator<SmartformHistoricRunner> iter1= race123.getRunnerIterator();
                while(iter1.hasNext())
                {
                    SmartformHistoricRunner runner = iter1.next();
                    addKeyWord(runner.getName());
                    addKeyWord(runner.getOwnerName());
                }
            }
        }
        return m_alKeyWords;
    }
    private void addKeyWord(String strKey)
    {
        if (!m_alKeyWords.contains(strKey))
            m_alKeyWords.add(strKey);
    }
    public int getNumberOfRaces()
    {
        return m_alRaces.size();
    }
}
