/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.bos;

import java.text.DateFormatSymbols;
import java.util.HashMap;

/**
 *
 * @author Simon
 */
public class AdditionalRaceData {
    
   protected String m_strName;
   protected String m_strTitle;
   protected int m_nMonth;
   protected int m_nDay;
   protected int m_nDOW;
    protected int m_nFences = 0;
    protected int m_nGroupRace = 0;

    protected String m_strCourse;
     protected int m_nDistanceYards;

    protected String m_strRaceClass;
    protected String m_strRaceType;
    protected String m_strTrackType;
  
    protected String m_strSex="";

    protected double m_dStandardTime = 0;
    protected String m_strStandardTime;

    protected String m_strDirection;  // Left Handed, Right Handed, Straight, NULL to do: derive for Daily Race
      
    protected boolean m_bHandicap;
    protected String m_strAgeRange;
    
    protected String m_strComments;
    protected String m_strCountry;

    protected int m_aMissingYears[];
     
    protected int m_nStartYear;
    protected int m_nEndYear;

    // conditions separated out
    protected String m_strConditions="";
    
   protected String m_strKeywords="";
   
    // from sporting_life_course
    protected int m_nCourse = 0;
    protected String m_strRPCourseCode = "";
    // from additional_race_wikipedia
    HashMap<String,AdditionalRaceWikipedia> m_hmWikipedia = new HashMap<String,AdditionalRaceWikipedia>();
    
    protected int m_nSEKey = 0;         // range of SE additional_races (x 10000)
    protected String m_strGSref = "";       // Gallop-Sieger code


    public AdditionalRaceData(String strName)
    {
        m_strName = strName;
    }
    public AdditionalRaceData(AdditionalRaceData data)
    {
        // to do: complete copy constructor
        m_strName = data.getName();
        m_strCourse = data.getCourse();
        m_strKeywords = data.getKeywords();
        m_strRPCourseCode = data.getRPCourseCode();
    }
    public String getName()
    {
        return m_strName;
    }
    public void setDirection(String strDirection)
    {
        m_strDirection = strDirection;
    }
    public void setNrFences(int nFences)
    {
        m_nFences = nFences;
    }
    public void setStandardTimeSeconds(double dStandardTime)
    {
        m_dStandardTime = dStandardTime;
    }
    public String getStandardTime()
    {
        return m_strStandardTime;
    }
    public double getStandardTimeSeconds()
    {
        return m_dStandardTime;
    }
   public int getNrFences()
    {
        return m_nFences;
    }
    public String getDirection()
    {
        return m_strDirection;
    }
    public void setAgeRange(String strAgeRange)
    {
        m_strAgeRange = strAgeRange;

     }

    public  void setTitle(String strTitle)
    {
        m_strTitle = strTitle;
/*       String strLowerTitle = strTitle.toLowerCase();
        if (strLowerTitle.indexOf("fillies") >= 0)
        {
            if (strLowerTitle.indexOf("mares") >= 0)
                m_strSex = "FM";
            else
                m_strSex = "F";
        }
        else if(strLowerTitle.indexOf("colts") >= 0)
        {
            if (strLowerTitle.indexOf("geldings") >= 0)
                m_strSex = "CG";
            else
                m_strSex = "C";
        }
        else if (strLowerTitle.indexOf("mares") >= 0)
        {
            m_strSex = "M";
        } */
    }
    
    public void setSex(String strSex)
    {
        m_strSex = strSex;
    }
    public void setTrackType(String strTrackType)
    {
        m_strTrackType = strTrackType;
    }
    public void setCourse(String strCourse)
    {
        m_strCourse = strCourse;
    }

    public String getAgeRange()
    {
        return m_strAgeRange;
    }
  public int getMonth()
    {
     return m_nMonth;
    }
    public void setMonth(int nMonth)
    {
        m_nMonth = nMonth;
    }
   public String getMonthString()
    {
     return new DateFormatSymbols().getMonths()[m_nMonth-1];
    }
     public int getDay()
    {
        return m_nDay;  // day number in 2014 - not to be used literally, just an indicator for start/end of month
    }
    public void setDay(int nDay)
    {
        m_nDay = nDay;
    }
    public int getDOW()
    {
        return m_nDOW;
    }
    public void setDOW(int nDOW)
    {
        m_nDOW = nDOW;
    }
    public String getDOWString()
    {
        return new DateFormatSymbols().getWeekdays()[m_nDOW];
    }
    public void setRaceClass(String strRaceClass)
    {
        m_strRaceClass = strRaceClass;
    }
    public void setRaceType(String strRaceType)
    {
        m_strRaceType = strRaceType;
    }
    public void setDistanceYards(int nDistanceYards)
    {
        m_nDistanceYards = nDistanceYards;
    }
    public void setHandicap(boolean bHandicap)
    {
        m_bHandicap = bHandicap;
   }
    public String getRaceClass()
    {
        return m_strRaceClass;
    }
    public String getRaceType()
    {
        return m_strRaceType;
    }
   public boolean isHandicap()
   {
        return m_bHandicap;
    }
    public int getDistanceYards()
    {
        return m_nDistanceYards;
    }
    public int getDistanceFurlongs()
    {
        return (int) (((double)m_nDistanceYards)/220.0 + 0.5);
    }
    public double getDecimalDistance()
    {
        int nDistance10 = (int)((m_nDistanceYards * 10.0)/220.0);
        return nDistance10/10.0; // scale it
    }
    public String getFormattedDistance()
    {
        int nMiles = m_nDistanceYards/(220 * 8);
        int nRemainder = (m_nDistanceYards - (nMiles * 220 * 8));
        int nFurlongs = nRemainder/220;
        nRemainder = (m_nDistanceYards - (nMiles * 220 * 8) - (nFurlongs * 220));
        int nYards = nRemainder;

        String strDistance = "";
        if (nMiles > 0)
            strDistance += (nMiles + "m ");

        if (nFurlongs > 0)
            strDistance += (nFurlongs + "f ");

        if (nYards >  0)
            strDistance += (nYards + "y");

        return strDistance;
    }
    public String getLongFormattedDistance()
    {
        int nMiles = m_nDistanceYards/(220 * 8);
        int nRemainder = (m_nDistanceYards - (nMiles * 220 * 8));
        int nFurlongs = nRemainder/220;
        nRemainder = (m_nDistanceYards - (nMiles * 220 * 8) - (nFurlongs * 220));
        int nYards = nRemainder;

        String strDistance = "";
        if (nMiles > 0)
            strDistance += (nMiles + " mile");
        if (nMiles > 1)
            strDistance += "s";
        strDistance += " ";

        if (nFurlongs > 0)
            strDistance += (nFurlongs + " furlong");
        if (nFurlongs > 1)
            strDistance += "s";
        strDistance += " ";

        if (nYards >  0)
            strDistance += (nYards + " yds");

        return strDistance;
    }
    public int getGroupRace()
    {
        return m_nGroupRace;
    }
   public String getTrackType()
    {
        return m_strTrackType;
    }
    public String getTitle()
    {
        return m_strTitle;
    }
   public String getSex()
    {
        return m_strSex;
    }
   public String getSexString()
    {
        if ("FM".equals(m_strSex))
            return "Fillies and Mares only";
        else if ("CG".equals(m_strSex))
            return "Colts and Geldings only";
        else if ("F".equals(m_strSex))
            return "Fillies only";
        else if ("M".equals(m_strSex))
            return "Mares only";
        else if ("C".equals(m_strSex))
            return "Colts only";
        
        return "";
    }
   public String getConditions()
    {
        return m_strConditions;
    }
   public String getComments()
    {
        return m_strComments;
    }
   public void setComments(String strComments)
    {
        m_strComments = strComments;
    }
    public void setConditions(String strConditions)
    {
        m_strConditions = strConditions;
    }
  public String getCountry()
    {
        return m_strCountry;
    }
  public String getCourse()
    {
        return m_strCourse;
    }
   public void setCountry(String strCountry)
    {
        m_strCountry = strCountry;
    }
    public void setGroupRace(int nGroupRace)
    {
        m_nGroupRace = nGroupRace;
    }
    public void setStandardTime(String strStandardTime)
    {
        m_strStandardTime = strStandardTime;
    }
    public int getStartYear()
    {
        return m_nStartYear;
    }
    public void setStartYear(int nStartYear)
    {
        m_nStartYear = nStartYear;
    }
    public int getEndYear()
    {
        return m_nEndYear;
    }
    public void setEndYear(int nEndYear)
    {
        m_nEndYear = nEndYear;
    }
   public AdditionalRaceWikipedia getWikipedia(String strLanguage)
    {
        return m_hmWikipedia.get(strLanguage);
    }
   public void setWikipedia(AdditionalRaceWikipedia wp)
    {
        m_hmWikipedia.put(wp.getLanguage(), wp);
    }
public int[] getMissingYears()
    {
        return m_aMissingYears;
    }
   public void setMissingYears(int[] aMissingYears)
    {
        m_aMissingYears = aMissingYears;
    }
    public int getCourseId()
    {
        return m_nCourse;
    }
    public void setCourseId(int nCourse)
    {
        m_nCourse = nCourse;
    }

    public String getKeywords() {
        return m_strKeywords;
    }

    public void setKeywords(String strKeywords) {
        this.m_strKeywords = strKeywords;
    }

    public String getRPCourseCode() {
        return m_strRPCourseCode;
    }

    public void setRPCourseCode(String strRPCourseCode) {
        this.m_strRPCourseCode = strRPCourseCode;
    }
    
    public int getSEKey() {
        return m_nSEKey;
    }

    public void setSEKey(int nSEKey) {
        this.m_nSEKey = nSEKey;
    }

    public String getGSref() {
        return m_strGSref;
    }

    public void setGSref(String strGSref) {
        this.m_strGSref = strGSref;
    }
}
