package ene.eneform.colours.database;

import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.mero.parse.ENEColoursParser;
import ene.eneform.smartform.bos.SmartformColoursRunner;
import ene.eneform.smartform.bos.UnregisteredColourSyntax;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import ene.eneform.utils.ENEStatement;

public class ENERacingColoursFactory
{

     
    public static ENERacingColours createRunnerColours(String strLanguage, SmartformColoursRunner runner)
    {
        return createRunnerColours(strLanguage, runner, SmartformRunnerFactory.sm_RCPVersion);
    }
    public static ENERacingColours createRunnerColours(String strLanguage, SmartformColoursRunner runner, String strVersion)
    {
        ENERacingColours colours = null;
        if (runner != null)
        {
            colours = createRacingColours(null, runner.getUnregisteredColourSyntax(), strLanguage, runner.getJockeyColours(), strVersion);
         }   
        
        return colours;
    }
    public static ENERacingColours createColours(ENEStatement statement, String strLanguage, String strJockeyColours)
    {
        return createColours(statement, strLanguage, strJockeyColours, SmartformRunnerFactory.sm_RCPVersion);
    }
    public static ENERacingColours createColours(ENEStatement statement, String strLanguage, String strJockeyColours, String strVersion)
    {
        return createRacingColours(statement, WikipediaFactory.createUnregisteredColourSyntax(statement, strJockeyColours), strLanguage, strJockeyColours, strVersion);
    }

    public static ENERacingColours createRacingColours(ENEStatement statement, UnregisteredColourSyntax ucs, String strLanguage, String strJockeyColours, String strVersion)
    {
        ENERacingColours colours = null;
        if (ucs != null)
        {
            colours = ENEColoursEnvironment.getInstance().createRacingColours(strLanguage, ENEColoursEnvironment.getInstance().createJacket(strLanguage, ucs.getJacket()), 
                    ENEColoursEnvironment.getInstance().createSleeves(strLanguage,ucs.getSleeves()), 
                    ENEColoursEnvironment.getInstance().createCap(strLanguage,ucs.getCap()));
            colours.setDescription(strJockeyColours);
            if (statement != null)
                insertJockeyColoursParse(statement, strVersion, colours, "", "", "");
        }
        else
        {
            ENEColoursParser parser = new ENEColoursParser(strLanguage, strJockeyColours, "");
            colours = parser.parse();
            if (statement != null)
                insertJockeyColoursParse(statement, strVersion, colours, parser.getRemainder(), parser.getExpanded(), parser.getSyntax());
        }
         
        return colours;
       
    }
private static int insertJockeyColoursParse(ENEStatement statement, String strVersion, ENERacingColours colours, String strUnresolved, String strExpanded, String strSyntax)
{
    int nReturn = 0;

    String strJacket = colours.getJacket().toString();
    String strSleeves = colours.getSleeves().toString();
    String strCap = colours.getCap().toString();
    String strDescription = colours.getDescription();

    String strInsert = "replace into racing_colours_parse (rcp_description, rcp_expanded, rcp_version, rcp_jacket, rcp_sleeves, rcp_cap, rcp_unresolved, rcp_syntax) values (\"";
    strInsert += (strDescription + "\",\"");
    strInsert += (strExpanded + "\",\"");
    strInsert += (strVersion + "\",\"");
    strInsert += (strJacket + "\",\"");
    strInsert += (strSleeves + "\",\"");
    strInsert += (strCap + "\",\"");
    strInsert += (strUnresolved + "\",\"");
    strInsert += (strSyntax + "\")");

    nReturn  = statement.executeUpdate(strInsert);
 
    return nReturn;
  }
}
