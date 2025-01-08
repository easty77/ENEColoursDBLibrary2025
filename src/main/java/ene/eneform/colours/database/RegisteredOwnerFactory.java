/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.database;

import ene.eneform.colours.bos.ENEOwnerColours;
import ene.eneform.colours.bos.ENERegisteredColours;
import ene.eneform.colours.bos.ENERegisteredOwner;
import ene.eneform.colours.service.WikipediaService;
import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.mero.parse.ENEColoursParser;
import ene.eneform.smartform.bos.UnregisteredColourSyntax;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import ene.eneform.utils.ENEStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Simon
 */
@Service
@RequiredArgsConstructor
public class RegisteredOwnerFactory {
    private final WikipediaService wikipediaService;
    private final ColoursSearch coloursSearch;

    private String getRCPVersion (String strVersion, String strOrganisation, String strOrgType, int nYear)
    {
        return strVersion + strOrganisation + strOrgType + String.valueOf(nYear);
    }
    public void parseRegisteredOwnerColours(ENEStatement statement, String strVersion, String strOrganisation, String strOrgType, int nYear)
    {
        // getRegisteredOwnerColours for 2011, getRegisteredOwnerColours1 for 1968
       // V3 does not require entries in the registered_owners table
        ArrayList<ENERegisteredOwner> astrColourDescriptions = getRegisteredOwners(statement, strOrganisation, strOrgType, nYear, "", "");    // ro_family_name like 'ab%'  ro_item in (18377,  2010, 13925)
       System.out.println("parseRegisteredOwnerColours: " + astrColourDescriptions.size());
        Iterator<ENERegisteredOwner> iter = astrColourDescriptions.iterator();
    	while(iter.hasNext())
    	{
            ENERegisteredOwner owner = iter.next();
            Iterator<ENERegisteredColours> i1 = owner.getColourIterator();
            while(i1.hasNext())
            {
                ENERegisteredColours regcolours = i1.next();
                String strDescription = regcolours.getColours();
                ENERacingColoursFactory.createColours(statement, regcolours.getLanguage(), strDescription, getRCPVersion(strVersion, strOrganisation, strOrgType, nYear));
            }
    	}
     }
    public void parseRegisteredFrenchOwnerColours(ENEStatement statement, String strVersion, String strFilter)
    {
        // getRegisteredOwnerColours for 2011, getRegisteredOwnerColours1 for 1968
       // V3 does not require entries in the registered_owners table
        ArrayList<ENERegisteredOwner> astrColourDescriptions = getRegisteredFrenchOwnerColours(statement, "fr", strFilter, "");    // ro_display_name > 'E'
       System.out.println("parseRegisteredFrenchOwnerColours: " + astrColourDescriptions.size());
        Iterator<ENERegisteredOwner> iter = astrColourDescriptions.iterator();
    	while(iter.hasNext())
    	{
            ENERegisteredOwner owner = iter.next();
            Iterator<ENERegisteredColours> i1 = owner.getColourIterator();
            while(i1.hasNext())
            {
                ENERegisteredColours regcolours = i1.next();
                String strDescription = regcolours.getColours();
                ENERacingColoursFactory.createColours(statement, ENEColoursEnvironment.DEFAULT_LANGUAGE, strDescription, strVersion);
            }
    	}
     }
    public int generateSVGRegisteredOwnerFiles(ENEStatement statement, String strVersion, String strOrganisation, String strOrgType, int nYear, String strFormat)
    {
        int nCount = 0;
        String strRCPVersion = getRCPVersion(strVersion, strOrganisation, strOrgType, nYear);
        ArrayList<ENEOwnerColours> alColours = getENEColoursRegisteredOwnerList(statement, strOrganisation, strOrgType, nYear);

        System.out.println("generateSVGRegisteredOwnerFiles: " + strOrganisation + "-" + nYear + ", Number of colours: " + alColours.size());

        // Jacket/Sleeves/Cap definition will be contained in either registered_colour_syntax or racing_colours_parse
        String strQuery="select rc_item, rc_colours_nr, coalesce(rcs_jacket, rcp_jacket) as ucs_jacket, coalesce(rcs_sleeves, rcp_sleeves) as ucs_sleeves, coalesce(rcs_cap, rcp_cap) as ucs_cap";
        strQuery += " from registered_colours";
        strQuery += " left outer join registered_colour_syntax on rcs_item=rc_item and rcs_colours_nr=rc_colours_nr and rcs_organisation=rc_organisation and rcs_year=rc_year";
        strQuery += " left outer join racing_colours_parse on rcp_description=replace(rc_colours, ' & ', ' and ') and rcp_version='" + strRCPVersion + "'";
        strQuery += " where rc_organisation='" + strOrganisation + "' and rc_year=" + nYear;
        strQuery += " order by rc_item, rc_colours_nr";
        
        ResultSet rs = statement.executeQuery(strQuery);
        if (rs != null)
        {
            try
            {
                while (rs.next())
                {
                    String strOwnerId = rs.getString("rc_item") + "_" + rs.getInt("rc_colours_nr");
                    try
                    {
                        UnregisteredColourSyntax ucs = SmartformRunnerFactory.createUnregisteredColourSyntax(rs);
                        if (ucs != null)
                        {
                            ENERacingColours colours = ENEColoursEnvironment.getInstance().createRacingColours("en", ENEColoursEnvironment.getInstance().createJacket("en", ucs.getJacket()), 
                            ENEColoursEnvironment.getInstance().createSleeves("en",ucs.getSleeves()), 
                            ENEColoursEnvironment.getInstance().createCap("en",ucs.getCap()));
                            String strFileName = getOrganisationYearFileName(strOwnerId, strOrganisation, nYear);
                            wikipediaService.createImageFile(strFileName, colours, "en", true, true);
                        }
                        else
                        {
                            System.out.println("createUnregisteredColourSyntax returned NULL: " + strOwnerId);
                        }
                        nCount++;
                    }
                    catch(FileNotFoundException e)
                    {
                        System.out.println("createSVGOwnerColoursList FileNotFoundException: " + strOwnerId);
                         e.printStackTrace();
                   }
                    catch(IOException e)
                    {
                        System.out.println("createSVGOwnerColoursList IOException: " + strOwnerId);
                        e.printStackTrace();
                    }
                    catch(Exception e)
                    {
                        System.out.println("createSVGOwnerColoursList Exception: " + strOwnerId);
                        e.printStackTrace();
                    }
                }
                rs.close();
            }
            catch(SQLException e)
            {
                System.out.println("createUnregisteredColourSyntax SQLException: " + e.getMessage());
            }
        }  

       
         return nCount;
    }
    public ENERacingColours createRegisteredOwnerColours(ENEStatement statement, String strLanguage, String strJockeyColours, String strOrganisation, String strRCPVersion)
    {
        ENERacingColours colours = null;
        UnregisteredColourSyntax ucs = createRCPUnregisteredColourSyntax(statement, strJockeyColours, strOrganisation, strRCPVersion);
        if (ucs != null)
        {
            colours = ENEColoursEnvironment.getInstance().createRacingColours(strLanguage, ENEColoursEnvironment.getInstance().createJacket(strLanguage, ucs.getJacket()), 
                    ENEColoursEnvironment.getInstance().createSleeves(strLanguage,ucs.getSleeves()), 
                    ENEColoursEnvironment.getInstance().createCap(strLanguage,ucs.getCap()));
            colours.setDescription(strJockeyColours);
        }
        else
        {
            colours = new ENEColoursParser(strLanguage, strJockeyColours, "").parse();
        }
         
        return colours;
    }
public UnregisteredColourSyntax createRCPUnregisteredColourSyntax(ENEStatement statement, String strJockeyColours, String strOrganisation, String strRCPVersion)
{
    String strQuery = "select coalesce(wi2.wi_jacket, ucs_jacket) as ucs_jacket, coalesce(wi2.wi_sleeves, ucs_sleeves) as ucs_sleeves, coalesce(wi2.wi_cap, ucs_cap) as ucs_cap";
    strQuery += " from racing_colours_parse rcp";
    strQuery += " left outer join unregistered_colour_syntax on rcp_description=ucs_colours and ucs_organisation='" + strOrganisation + "'";
    strQuery += " left outer join wikipedia_images wi1 on coalesce(ucs_jacket,rcp_jacket)=wi1.wi_jacket and coalesce(ucs_sleeves,rcp_sleeves)=wi1.wi_sleeves and coalesce(ucs_cap,rcp_cap)=wi1.wi_cap and coalesce(rcp_unresolved, '') = '' ";
    strQuery += " left outer join wikipedia_images wi2 on rcp_description=wi2.wi_description";
    strQuery += " where rcp_description=replace('" + strJockeyColours + "', ' & ', ' and ') and rcp_version='" + strRCPVersion + "'";
    UnregisteredColourSyntax ucs = null;
    ResultSet rs = statement.executeQuery(strQuery);
    if (rs != null)
    {
        try
        {
            while (rs.next())
            {
                 ucs = SmartformRunnerFactory.createUnregisteredColourSyntax(rs);
            }
            rs.close();
        }
        catch(SQLException e)
        {
            System.out.println("createUnregisteredColourSyntax SQLException: " + e.getMessage());
        }
    }  
    
    return ucs;
}
public void generateRegisteredOwnersSVG(ENEStatement statement, String strVersion, String strFormat) 
    {
/*       ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1912, strFormat); 
     ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1762, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1771, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1775, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1777, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1785, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1787, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1796, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1920, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1936, strFormat);   
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1889, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "SC", 1869, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "SC", 1867, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1908, strFormat);  
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1917, strFormat); 
       ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1949, strFormat); 
      ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1895, strFormat);
       ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1945, strFormat);  
      ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1958, strFormat); 
      ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "SC", 1958, strFormat);  

           ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 1968, strFormat);  
 ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 2011, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 2012, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 2013, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 2014, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "SA", "", 2011, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "NSW", "", 2011, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "NSW", "", 2012, strFormat);
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "Ireland", "", 2008, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "Ireland", "", 2009, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "Ireland", "", 2010, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "Ireland", "", 2011, strFormat); 
        ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "Ireland", "", 2012, strFormat);  
       ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "France", "", 2014, strFormat);  */
 //ENEColoursFactory.generateSVGRegisteredOwnerFiles(statement, "UK", "", 2015, strFormat); 
 generateSVGRegisteredOwnerFiles(statement, strVersion, "UK", "", 2016, strFormat); 
    }
    private ENERacingColours createRunnerColours(ENEOwnerColours ownercolours)
    {
        String strLanguage = ownercolours.getLanguage();
            String strJockeyColours = ownercolours.getColours();
            ENERacingColours colours = null;
            String strJacketSyntax = ownercolours.getJacketSyntax();
            if ((strJacketSyntax != null) && !"".equals(strJacketSyntax))
            {
                System.out.println("Syntax: " + strJockeyColours + " - " + strJacketSyntax);
                colours = ENEColoursEnvironment.getInstance().createRacingColours(strLanguage, 
                        ENEColoursEnvironment.getInstance().createJacket(strLanguage,strJacketSyntax), 
                        ENEColoursEnvironment.getInstance().createSleeves(strLanguage,ownercolours.getSleevesSyntax()), 
                        ENEColoursEnvironment.getInstance().createCap(strLanguage,ownercolours.getCapSyntax()));
                colours.setDescription(strJockeyColours);
            }
            else
            {
                System.out.println("Parse: " + strJockeyColours);
                //strJockeyColours = strJockeyColours.toLowerCase();
                //strJockeyColours = strJockeyColours.substring(0, 1).toUpperCase() + strJockeyColours.substring(1);
                        
                 ENEColoursParser parser = new ENEColoursParser(strLanguage, strJockeyColours, "");   // don't want owner's name in SVG ownercolours.getOwnerName());
                 colours = parser.parse();
            }
        return colours;
    }
   public ArrayList<ENERegisteredOwner> getRegisteredOwners(ENEStatement statement, String strOrganisation, String strOrgType, int nYear, String strFirstFilter, String strLastFilter)
    {
       // 20121127- outer join with syntax table, don't need to check if can parse entries that have syntax specified
        String strLimit = ""; // " limit 100";
        String strQuery = "select distinct case when ro_display_name is null or ro_display_name = '' then trim(concat(ro_title, ' ', ro_first_name, ' ', ro_family_name, ' ', ro_suffix)) else ro_display_name end as owner_name, rc_colours_nr as colours_nr, rc_colours as owner_colours, ro_item, rc_language from registered_owners inner join registered_colours on rc_item=ro_item and ro_organisation=rc_organisation and ro_year=rc_year";
        String strWhere = " where ro_organisation='" + strOrganisation + "'";
        strWhere += " and ro_item < 100000";    // do not include Life Colours that have already been matched
        if (nYear > 0)
            strWhere += " and ro_year = " + nYear;
        if (!"".equals(strFirstFilter))
            strWhere += (" and " + strFirstFilter);
        if (!"".equals(strLastFilter))
            strWhere += (" and " + strLastFilter);
        strWhere += " and not exists (select * from registered_colour_syntax where rcs_item = ro_item and rcs_organisation=ro_organisation and rcs_year = ro_year and rcs_colours_nr=rc_colours_nr)";
        String strOrder = " order by ro_display_name, rc_colours_nr";
        System.out.println("parseRegisteredOwnerColours: " + strQuery + strWhere + strOrder + strLimit);
        return getRegisteredOwnerColoursStatement(statement, strQuery + strWhere + strOrder + strLimit);
    }
   public ArrayList<ENERegisteredOwner> getRegisteredFrenchOwnerColours(ENEStatement statement, String strLanguage, String strFirstFilter, String strLastFilter)
    {
       // 20121127- outer join with syntax table, don't need to check if can parse entries that have syntax specified
        String strLimit = ""; // " limit 100";
        String strQuery = "select distinct fo_display_name as owner_name, 1 as colours_nr, fc_jockey_colours as owner_colours, 1 as ro_item, fc_language as rc_language from (french_owners inner join french_colours on fo_owner_id=fc_owner_id and fc_language='" + strLanguage + "' and fc_jockey_colours != 'Par Derogation')";
        String strWhere = " where 1= 1";
        if (!"".equals(strFirstFilter))
            strWhere += (" and " + strFirstFilter);
        if (!"".equals(strLastFilter))
            strWhere += (" and " + strLastFilter);
        String strOrder = " order by fo_display_name";
        System.out.println("getRegisteredFrenchOwnerColours: " + strQuery + strWhere + strOrder + strLimit);
        return getRegisteredOwnerColoursStatement(statement, strQuery + strWhere + strOrder + strLimit);
    }
   public ArrayList<ENERegisteredOwner> getRegisteredOwnerColoursV3(ENEStatement statement, String strOrganisation, int nYear, String strFirstFilter, String strLastFilter)
    {
       // no join to registered_owners
        String strLimit = ""; // " limit 100";
        String strQuery = "select rc_item as owner_name, rc_colours_nr as colours_nr, rc_colours as owner_colours, rc_language from registered_colours where";
        String strWhere = " rc_organisation='" + strOrganisation + "' and rc_year = " + nYear;
        if (!"".equals(strFirstFilter))
            strWhere += (" and " + strFirstFilter);
        if (!"".equals(strLastFilter))
            strWhere += (" and " + strLastFilter);
        String strOrder = " order by rc_item, rc_colours_nr";
        System.out.println("parseRegisteredOwnerColours: " + strQuery + strWhere + strOrder + strLimit);
        return getRegisteredOwnerColoursStatement(statement, strQuery + strWhere + strOrder + strLimit);
    }
 
      public ArrayList<ENERegisteredOwner> getRegisteredOwnerColoursStatement(ENEStatement statement, String strStatement)
    {
       ArrayList<ENERegisteredOwner> lst = new ArrayList<ENERegisteredOwner>();

 
        ResultSet rs  = statement.executeQuery(strStatement);
        String strPreviousOwner="";
        ENERegisteredOwner currentOwner=null;
        if (rs != null)
        {
            try
            {
               while (rs.next())
               {
                   String strOwnerName = rs.getString("owner_name");
                   System.out.println("Owner: " + strOwnerName);
                   int nColours = rs.getInt("colours_nr");
                   String strColours = rs.getString("owner_colours");
                   String strLanguage = rs.getString("rc_language");

                   if (!strPreviousOwner.equals(strOwnerName))
                   {
                       currentOwner = new ENERegisteredOwner(strOwnerName);
                       currentOwner.setItemNumber(rs.getInt("ro_item"));
                       lst.add(currentOwner);
                   }

                   if (currentOwner != null)
                        currentOwner.addColours(nColours, strColours, strLanguage);
                   strPreviousOwner= strOwnerName;
                   
               }
               rs.close();
            }
            catch(SQLException e)
            {
            }
        }
 
        return lst;
    }
   public ArrayList<ENEOwnerColours> getENEColoursRegisteredOwnerList(ENEStatement statement, String strOrganisation, String strOrgType, int nYear)
    {

        String strWhere = coloursSearch.getOwnerColoursWhereClause("ro_display_name", "", "", strOrganisation, strOrgType, nYear);
        String strOrder = coloursSearch.getOwnerColoursOrderClause("ro_item", "", "", strOrganisation, strOrgType, nYear);
        //strWhere += " and rc_item in (17657, 1930, 1328, 6950, 14686, 6944, 15533, 11041, 14168, 7644, 4103, 18529, 8803, 4587, 15841)";
        strWhere += " and ro_family_name like 'ab%'";
        // Those specified in syntax only FOR NOW
        return coloursSearch.findOwnerColours(statement, strWhere, "ro_display_name", strOrder, 1, 0, false);    // return all - no LIMIT
    }

    public String getOrganisationYearFileName(String strFileName, String strOrganisation, int nYear) {
        String strFullDirectory = ENEColoursEnvironment.getInstance().getVariable("SVG_OUTPUT_DIRECTORY") + ENEColoursEnvironment.getInstance().getVariable("SVG_IMAGE_PATH") + "organisations/" + strOrganisation + "/" + nYear + "/mero";
        String strFullFileName = strFullDirectory + "/" + strFileName + ".svg";
        return strFullFileName;
    }
}
