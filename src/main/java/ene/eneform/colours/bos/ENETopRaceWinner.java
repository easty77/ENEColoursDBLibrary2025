/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

/**
 *
 * @author Simon
 */
public class ENETopRaceWinner {

    private int m_nRace;
    private int m_nYear;
    private String m_strHorse;
    private String m_strJockey;
    private String m_strTrainer;
    private String m_strOwner;
    private int m_nAge;
    private char m_cGender;
    private String m_strColours;
    private int m_nDeadHeat;        // normally 0, becomes 1 for second horse in Dead Heat
    private String m_strComments;
    private String m_strRaceTitle = "";

    private String m_strSire;
    private String m_strDam;
    private String m_strDamSire;
    private String m_strColour;
    private String m_strBred;
    private int m_nFoalingYear;


    public ENETopRaceWinner(int nRace, int nYear)
    {
        this.m_nRace = nRace;
        m_nYear = nYear;
    }

    public char getGender()
    {
        return m_cGender;
    }

    public void setGender(char cGender)
    {
        this.m_cGender = cGender;
    }

    public int getAge()
    {
        return m_nAge;
    }

    public void setAge(int nAge)
    {
        this.m_nAge = nAge;
    }

    public int getDeadHeat()
    {
        return m_nDeadHeat;
    }

    public void setDeadHeat(int nDeadHeat)
    {
        this.m_nDeadHeat = nDeadHeat;
    }

    public int getYear()
    {
        return m_nYear;
    }

    public String getColours()
    {
        return m_strColours;
    }

    public void setColours(String strColours)
    {
        this.m_strColours = strColours;
    }

    public String getComments()
    {
        return m_strComments;
    }

    public void setComments(String strComments)
    {
        this.m_strComments = strComments;
    }

    public String getHorse()
    {
        return m_strHorse;
    }

    public void setHorse(String strHorse)
    {
        this.m_strHorse = strHorse;
    }

    public String getJockey()
    {
        return m_strJockey;
    }

    public void setJockey(String strJockey)
    {
        this.m_strJockey = strJockey;
    }

    public String getOwner()
    {
        return m_strOwner;
    }

    public void setOwner(String strOwner)
    {
        this.m_strOwner = strOwner;
    }

    public String getTrainer()
    {
        return m_strTrainer;
    }

    public void setTrainer(String strTrainer)
    {
        this.m_strTrainer = strTrainer;
    }

    public int getRaceId()
    {
        return m_nRace;
    }
    public String getRaceTitle()
    {
        return m_strRaceTitle;
    }
    public void setRaceTitle(String strRaceTitle)
    {
        this.m_strRaceTitle = strRaceTitle;
    }

    public String getSire()
    {
        return m_strSire;
    }
    public void setSire(String strSire)
    {
        this.m_strSire = strSire;
    }
    public String getDam()
    {
        return m_strDam;
    }
    public void setDam(String strDam)
    {
        this.m_strDam = strDam;
    }
    public String getDamSire()
    {
        return m_strDamSire;
    }
    public void setDamSire(String strDamSire)
    {
        this.m_strDamSire = strDamSire;
    }
    public String getColour()
    {
        return m_strColour;
    }
    public void setColour(String strColour)
    {
        this.m_strColour = strColour;
    }
    public String getBred()
    {
        return m_strBred;
    }
    public void setBred(String strBred)
    {
        this.m_strBred = strBred;
    }
    public int getFoalingYear()
    {
        return m_nFoalingYear;
    }

    public void setFoalingYear(int nFoalingYear)
    {
        this.m_nFoalingYear = nFoalingYear;
    }
  }
