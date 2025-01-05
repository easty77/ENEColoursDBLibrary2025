/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.web.rp;

/**
 *
 * @author Simon
 */
public class RacingPostCourse {
    
    private String m_strCode;

    private String m_strName;
    private String m_strCountry;
    private int m_nCourseNumber = 0;
    private String m_strSFCourseName = "";
    private String m_strATRCourseName = "";
     public RacingPostCourse(String strCode, String strName, String strCountry, int nCourseNumber, String strSFCourseName, String strATRCourseName)
    {
        m_strCode = strCode;
        m_strName = strName;
        m_strCountry = strCountry;
        m_nCourseNumber = nCourseNumber;
        m_strSFCourseName = strSFCourseName;
        m_strATRCourseName = strATRCourseName;
    }

     public String getCode() {
            return m_strCode;
        }

        public String getName() {
            return m_strName;
        }

        public String getCountry() {
            return m_strCountry;
        }
         public int getNumber() {
            return m_nCourseNumber;
        }

        public String getSFName() {
            return m_strSFCourseName;
        }
        public String getATRName() {
            return m_strATRCourseName;
        }
        public boolean isValid()
        {
            return (m_nCourseNumber > 0);
        }
}
