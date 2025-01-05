/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

import ene.eneform.smartform.bos.JockeyColoursBO;

/**
 *
 * @author Simon
 */
public class ENEColoursBO implements JockeyColoursBO
{
    private String m_strHorse;
    private String m_strColours;
    private String m_strJockey;
    private String m_strTrainer;
    private String m_strOwner;
    private String m_strDescription;

    public ENEColoursBO(String strDescription, String strHorse, String strColours, String strTrainer, String strJockey, String strOwner)
    {
        m_strDescription = strDescription;
        m_strHorse = strHorse;
        m_strColours = strColours;
        m_strJockey = strJockey;
        m_strTrainer = strTrainer;
        m_strOwner = strOwner;
    }

    public String getJockeyColours() {
        return m_strColours;
    }

    public void setJockeyColours(String strColours) {
        this.m_strColours = strColours;
    }

    public String getDescription() {
        return m_strDescription;
    }

    public void setDescription(String strDescription) {
        this.m_strDescription = strDescription;
    }

    public String getHorse() {
        return m_strHorse;
    }

    public void setHorse(String strHorse) {
        this.m_strHorse = strHorse;
    }

    public String getJockey() {
        return m_strJockey;
    }

    public void setJockey(String strJockey) {
        this.m_strJockey = strJockey;
    }

    public String getOwner() {
        return m_strOwner;
    }

    public void setOwner(String strOwner) {
        this.m_strOwner = strOwner;
    }

    public String getTrainer() {
        return m_strTrainer;
    }

    public void setTrainer(String strTrainer) {
        this.m_strTrainer = strTrainer;
    }

}
