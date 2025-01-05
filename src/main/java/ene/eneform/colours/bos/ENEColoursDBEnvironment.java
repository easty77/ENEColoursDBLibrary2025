/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.bos;

import ene.eneform.colours.database.AdditionalRaceDataFactory;
import ene.eneform.colours.web.rp.RacingPostCourse;
import ene.eneform.colours.web.rp.RacingPostFactory;
import ene.eneform.colours.web.rp.RacingPostOwner;
import ene.eneform.smartform.bos.AdditionalRaceInstance;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.SmartformConnectionPool;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author simon
 */
public class ENEColoursDBEnvironment {
    
    protected static ENEColoursDBEnvironment sm_coloursDBEnvironment = null;
    
    private HashMap<String, AdditionalRaceData> m_hmARD = new HashMap<String, AdditionalRaceData>();

    private HashMap<String, RacingPostCourse> m_hmCourses = new HashMap<String, RacingPostCourse>();  // index by RP code
    private HashMap<String, RacingPostCourse> m_hmRPCourses = new HashMap<String, RacingPostCourse>();  // index by RP name
    private HashMap<String, RacingPostCourse> m_hmSFCourses = new HashMap<String, RacingPostCourse>();
    private HashMap<String, RacingPostOwner> m_hmOwners = new HashMap<String, RacingPostOwner>();  // index by RP code
    
    public static synchronized ENEColoursDBEnvironment getInstance()
    {
        if (sm_coloursDBEnvironment == null)
        {
            sm_coloursDBEnvironment = new ENEColoursDBEnvironment();
            sm_coloursDBEnvironment.initialise();
        }

        return sm_coloursDBEnvironment;
    }
    public void initialise()
    {
        ENEStatement statement = SmartformConnectionPool.getInstance().getENEStatement();
        RacingPostFactory.loadRacingPostCourses(statement, m_hmCourses, m_hmRPCourses, m_hmSFCourses);
        RacingPostFactory.loadRacingPostOwners(statement, m_hmOwners);
        AdditionalRaceDataFactory.loadAdditionalRaceData(statement, m_hmARD);
    }
   public void reset()
    {
        m_hmCourses = new HashMap<String, RacingPostCourse>();
        m_hmRPCourses = new HashMap<String, RacingPostCourse>();
        m_hmSFCourses = new HashMap<String, RacingPostCourse>();
        initialise();
    }
    public AdditionalRaceData getAdditionalRaceData(String strARDName)
    {
        return m_hmARD.get(strARDName);
    }
    public Iterator<String> getAdditionalRaceDataIterator()
    {
        return m_hmARD.keySet().iterator();
    }
    public String getRaceCountry(AdditionalRaceInstance ari)
    {
        RacingPostCourse rpc = null;
        if ("SF".equals(ari.getSource()))
            rpc = getRPCourseBySFName(ari.getCourse(), ari.getRaceType());
        else
            rpc = getRPCourseByName(ari.getCourse(), ari.getRaceType());
        
        if (rpc != null)
        {
            String strCountry = rpc.getCountry();
            if ("GB".equals(strCountry))
                strCountry = "UK";
            else if ("FR".equals(strCountry))
                strCountry = "France";
            else if ("IRE".equals(strCountry))
                strCountry = "Eire";
            
            return strCountry;
        }
        
        return "";
    }
    public RacingPostOwner getRPOwner(String strCode)
    {
        RacingPostOwner owner =  m_hmOwners.get(strCode);
        
        return owner;
    }
    public RacingPostCourse getRPCourse(String strCode)
    {
        System.out.println("getRPCourse: " + strCode);
        RacingPostCourse course =  m_hmCourses.get(strCode.replace("'", ""));
        if (course == null)
            course = new RacingPostCourse(strCode, strCode, strCode, 0, strCode, strCode);
        
        return course;
    }
    public RacingPostCourse getRPCourseByName(String strRPName, String strRaceType)
    {
        RacingPostCourse course =  m_hmRPCourses.get(strRPName);
        if (course == null)
            course = new RacingPostCourse(strRPName, strRPName, strRPName, 0, strRPName, strRPName);
        else if ((course.getCode().indexOf("-aw") < 0) && ("A_W_Flat".equals(strRaceType) || "All Weather Flat".equals(strRaceType)))
        {
            RacingPostCourse course1 = m_hmCourses.get(course.getCode() + "-aw");
            if (course1 != null)
                course = course1;
        }
        
        return course;
    }
    public RacingPostCourse getRPCourseBySFName(String strSFName, String strRaceType)
    {
        RacingPostCourse course =  m_hmSFCourses.get(strSFName);
        if (course == null)
            course = new RacingPostCourse(strSFName, strSFName, strSFName, 0, strSFName, strSFName);
        else if ((course.getCode().indexOf("-aw") < 0) && ("A_W_Flat".equals(strRaceType) || "All Weather Flat".equals(strRaceType)))
        {
            RacingPostCourse course1 = m_hmCourses.get(course.getCode() + "-aw");
            if (course1 != null)
                course = course1;
        }
        
        return course;
    }
}
