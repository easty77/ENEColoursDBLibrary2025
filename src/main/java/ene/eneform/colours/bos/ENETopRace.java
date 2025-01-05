/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ene.eneform.colours.bos;

/**
 *
 * @author Simon
 */
public class ENETopRace {

    private int m_nRace;
    private String m_strTitle;
    private String m_strCountry;
    private String m_strCourse;
    private String m_strDateDescription;
    private String m_strConditions;
    private String m_strAgeRange;
    private int m_nDistance;
    private String m_strDistanceUnits;

    public ENETopRace(int nRace)
    {
       this.m_nRace = nRace;
    }

    public int getDistance()
    {
        return m_nDistance;
    }

    public void setDistance(int nDistance)
    {
        this.m_nDistance = nDistance;
    }

    public int getRaceId()
    {
        return m_nRace;
    }

    public String getAgeRange()
    {
        return m_strAgeRange;
    }

    public void setAgeRange(String strAgeRange)
    {
        this.m_strAgeRange = strAgeRange;
    }

    public String getConditions()
    {
        return m_strConditions;
    }

    public void setConditions(String strConditions)
    {
        this.m_strConditions = strConditions;
    }

    public String getCountry()
    {
        return m_strCountry;
    }

    public void setCountry(String strCountry)
    {
        this.m_strCountry = strCountry;
    }

    public String getCourse()
    {
        return m_strCourse;
    }

    public void setCourse(String strCourse)
    {
        this.m_strCourse = strCourse;
    }

    public String getDateDescription()
    {
        return m_strDateDescription;
    }

    public void setDateDescription(String strDateDescription)
    {
        this.m_strDateDescription = strDateDescription;
    }

    public String getDistanceUnits()
    {
        return m_strDistanceUnits;
    }

    public void setDistanceUnits(String strDistanceUnits)
    {
        this.m_strDistanceUnits = strDistanceUnits;
    }

    public String getTitle()
    {
        return m_strTitle;
    }

    public void setTitle(String strTitle)
    {
        this.m_strTitle = strTitle;
    }
}
