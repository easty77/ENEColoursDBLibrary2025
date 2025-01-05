/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ene.eneform.colours.realsports;

import ene.eneform.colours.database.ENEColoursFactory;
import ene.eneform.colours.database.ENEColoursRunnerFactory;
import ene.eneform.colours.database.ENERacingColoursFactory;
import ene.eneform.colours.realsports.bettingpattern.RealSportsBettingPattern;
import ene.eneform.colours.realsports.bettingpattern.RealSportsFrequencyBettingPattern;
import ene.eneform.colours.realsports.results.RealSportsResultGenerator;
import ene.eneform.mero.Mero;
import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.smartform.bos.SmartformDailyRunner;
import ene.eneform.smartform.bos.SmartformRace;
import ene.eneform.utils.ENEStatement;
import ene.eneform.utils.HorseRacingUtils;

import java.awt.*;
import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Simon
 */
public class RealSportsFactory {
       private static final String[][] sm_astrPrices6a =  
       {
//{"6/1","5/1","6/4","5/1","3/1","6/1"},
{"6/1","5/1","2/1","5/1","3/1","6/1"},
       };    
       private static final String[][] sm_astrPrices6 =  
       {
{"1/2","7/2","8/1","14/1","16/1","40/1"},
{"1/2","5/1","13/2","10/1","20/1","22/1"},
{"1/1","7/2","6/1","6/1","10/1","20/1"},
{"6/4","2/1","5/1","7/1","10/1","33/1"},
{"2/1","5/2","5/2","6/1","12/1","33/1"},
{"2/1","5/2","4/1","9/2","17/2","20/1"},
{"2/1","2/1","3/1","9/2","28/1","66/1"},
{"2/1","5/2","6/1","13/2","13/2","7/1"},
{"2/1","3/1","3/1","5/1","11/1","14/1"},
{"2/1","7/2","4/1","9/2","7/1","10/1"},
{"2/1","9/2","9/2","11/2","11/2","6/1"},
{"5/2","4/1","9/2","9/2","9/2","15/2"},
{"5/2","3/1","3/1","7/2","17/2","25/1"},
{"3/1","3/1","7/2","4/1","7/1","9/1"},
{"3/1","3/1","4/1","4/1","11/2","9/1"},
{"3/1","3/1","3/1","7/2","13/2","20/1"},
{"3/1","3/1","3/1","9/2","9/2","25/1"}
};
       private static final String[][] sm_astrPrices7 =
       {
{"1/2","7/1","8/1","8/1","14/1","20/1","50/1"},
{"1/1","3/1","15/2","8/1","14/1","16/1","20/1"},
{"1/1","3/1","4/1","12/1","20/1","22/1","33/1"},
{"1/1","7/2","7/2","10/1","16/1","25/1","66/1"},
{"1/1","2/1","7/1","9/1","18/1","50/1","66/1"},
{"1/1","3/1","7/1","17/2","10/1","16/1","50/1"},
{"6/4","5/2","4/1","13/2","12/1","33/1","33/1"},
{"6/4","2/1","11/2","8/1","9/1","28/1","66/1"},
{"6/4","7/2","9/2","13/2","8/1","14/1","25/1"},
{"6/4","3/1","6/1","7/1","15/2","12/1","28/1"},
{"6/4","2/1","6/1","8/1","14/1","20/1","22/1"},
{"6/4","4/1","5/1","6/1","7/1","12/1","25/1"},
{"2/1","2/1","5/2","14/1","16/1","16/1","100/1"},
{"2/1","3/1","9/2","5/1","11/2","16/1","100/1"},
{"2/1","3/1","7/2","4/1","12/1","25/1","40/1"},
{"2/1","5/2","3/1","8/1","10/1","20/1","28/1"},
{"2/1","5/2","5/1","6/1","17/2","16/1","16/1"},
{"2/1","7/2","4/1","6/1","7/1","8/1","50/1"},
{"2/1","5/2","7/2","6/1","9/1","25/1","33/1"},
{"2/1","3/1","5/1","11/2","7/1","11/1","28/1"},
{"2/1","4/1","5/1","7/1","8/1","8/1","9/1"},
{"2/1","7/2","4/1","5/1","8/1","16/1","16/1"},
{"2/1","5/2","4/1","8/1","9/1","10/1","40/1"},
{"2/1","7/2","9/2","8/1","8/1","8/1","12/1"},
{"2/1","3/1","7/2","6/1","12/1","14/1","16/1"},
{"2/1","7/2","11/2","6/1","13/2","17/2","16/1"},
{"2/1","7/2","4/1","6/1","9/1","9/1","20/1"},
{"2/1","3/1","4/1","7/1","10/1","12/1","12/1"},
{"2/1","7/2","7/2","5/1","7/1","25/1","25/1"},
{"2/1","4/1","9/2","6/1","8/1","9/1","12/1"},
{"2/1","7/2","5/1","6/1","17/2","9/1","11/1"},
{"2/1","7/2","9/2","11/2","8/1","10/1","16/1"},
{"2/1","3/1","7/2","9/1","10/1","10/1","16/1"},
{"2/1","3/1","6/1","7/1","8/1","8/1","12/1"},
{"5/2","7/2","4/1","11/2","8/1","10/1","10/1"},
{"5/2","3/1","5/1","11/2","15/2","9/1","12/1"},
{"5/2","7/2","4/1","9/2","9/1","11/1","12/1"},
{"5/2","3/1","5/1","13/2","8/1","8/1","10/1"},
{"5/2","3/1","4/1","9/2","9/1","12/1","16/1"},
{"5/2","7/2","5/1","5/1","8/1","8/1","10/1"},
{"5/2","3/1","7/2","13/2","10/1","10/1","12/1"},
{"5/2","4/1","9/2","11/2","6/1","8/1","12/1"},
{"5/2","3/1","3/1","9/2","12/1","14/1","25/1"},
{"5/2","3/1","7/2","7/1","15/2","8/1","28/1"},
{"5/2","5/2","3/1","8/1","9/1","12/1","25/1"},
{"5/2","3/1","5/1","11/2","8/1","8/1","14/1"},
{"5/2","4/1","5/1","5/1","6/1","7/1","16/1"},
{"3/1","7/2","7/2","13/2","7/1","8/1","11/1"},
{"3/1","7/2","4/1","9/2","17/2","9/1","10/1"},
{"3/1","7/2","9/2","5/1","13/2","7/1","14/1"},
{"3/1","3/1","4/1","5/1","11/2","14/1","16/1"},
{"3/1","7/2","4/1","11/2","11/2","15/2","20/1"},
{"3/1","3/1","4/1","9/2","13/2","14/1","14/1"},
{"3/1","3/1","11/2","11/2","15/2","8/1","8/1"},
{"3/1","3/1","3/1","5/1","7/1","14/1","25/1"},
{"3/1","4/1","4/1","6/1","6/1","8/1","9/1"},
{"3/1","7/2","7/2","11/2","6/1","11/1","12/1"},
{"3/1","9/2","5/1","5/1","11/2","6/1","11/1"},
{"3/1","3/1","7/2","9/2","5/1","25/1","25/1"},
{"3/1","4/1","4/1","9/2","6/1","9/1","12/1"},
{"3/1","4/1","9/2","5/1","7/1","8/1","8/1"},
{"7/2","7/2","4/1","5/1","5/1","8/1","16/1"},
{"7/2","4/1","9/2","9/2","11/2","13/2","12/1"},
{"7/2","7/2","4/1","4/1","6/1","9/1","16/1"},
{"7/2","7/2","9/2","9/2","11/2","7/1","14/1"},
{"4/1","4/1","4/1","9/2","9/2","9/1","10/1"}
       };
       private static final String[][] sm_astrPrices8=
       {
{"1/2","13/2","7/1","10/1","20/1","33/1","33/1","33/1"},
{"1/2","7/1","8/1","10/1","11/1","25/1","50/1","50/1"},
{"1/1","4/1","6/1","15/2","11/1","14/1","33/1","80/1"},
{"1/1","9/2","6/1","9/1","10/1","12/1","25/1","66/1"},
{"1/1","5/1","7/1","17/2","9/1","14/1","20/1","25/1"},
{"1/1","7/2","8/1","8/1","12/1","16/1","20/1","40/1"},
{"1/1","11/2","8/1","8/1","12/1","12/1","16/1","16/1"},
{"1/1","3/1","5/1","10/1","16/1","33/1","33/1","33/1"},
{"1/1","7/2","6/1","7/1","14/1","25/1","25/1","50/1"},
{"1/1","5/1","6/1","10/1","12/1","16/1","16/1","16/1"},
{"6/4","7/2","6/1","13/2","11/1","12/1","14/1","40/1"},
{"6/4","5/2","6/1","12/1","12/1","14/1","16/1","22/1"},
{"6/4","3/1","4/1","7/1","12/1","20/1","22/1","100/1"},
{"6/4","3/1","4/1","8/1","10/1","20/1","33/1","50/1"},
{"6/4","9/2","13/2","8/1","9/1","9/1","12/1","20/1"},
{"6/4","9/2","5/1","7/1","8/1","14/1","16/1","25/1"},
{"6/4","7/2","13/2","7/1","8/1","16/1","16/1","25/1"},
{"6/4","6/1","6/1","13/2","7/1","9/1","11/1","50/1"},
{"6/4","2/1","11/2","12/1","14/1","16/1","20/1","100/1"},
{"6/4","4/1","6/1","7/1","15/2","8/1","22/1","150/1"},
{"6/4","7/2","4/1","5/1","14/1","25/1","25/1","66/1"},
{"6/4","2/1","7/2","12/1","16/1","33/1","50/1","100/1"},
{"6/4","7/2","5/1","6/1","12/1","16/1","20/1","25/1"},
{"6/4","7/2","5/1","6/1","10/1","16/1","20/1","50/1"},
{"2/1","9/2","5/1","6/1","8/1","10/1","12/1","22/1"},
{"2/1","5/1","5/1","6/1","15/2","9/1","12/1","20/1"},
{"2/1","7/2","4/1","7/1","7/1","11/1","33/1","33/1"},
{"2/1","2/1","13/2","7/1","15/2","16/1","33/1","66/1"},
{"2/1","5/1","5/1","11/2","8/1","10/1","11/1","20/1"},
{"2/1","5/1","6/1","7/1","7/1","9/1","10/1","14/1"},
{"2/1","7/2","11/2","11/2","15/2","11/1","20/1","25/1"},
{"2/1","2/1","9/2","8/1","20/1","20/1","20/1","20/1"},
{"2/1","3/1","6/1","7/1","8/1","12/1","14/1","20/1"},
{"2/1","9/2","5/1","6/1","6/1","10/1","20/1","20/1"},
{"2/1","3/1","5/1","6/1","12/1","14/1","14/1","20/1"},
{"2/1","4/1","9/2","5/1","6/1","16/1","20/1","50/1"},
{"2/1","6/1","6/1","7/1","8/1","8/1","10/1","10/1"},
{"2/1","5/1","6/1","7/1","7/1","8/1","12/1","14/1"},
{"2/1","3/1","3/1","8/1","14/1","16/1","16/1","40/1"},
{"2/1","4/1","9/2","11/2","8/1","12/1","16/1","25/1"},
{"2/1","3/1","3/1","5/1","16/1","20/1","25/1","150/1"},
{"2/1","3/1","4/1","13/2","7/1","16/1","33/1","50/1"},
{"2/1","9/2","5/1","13/2","8/1","10/1","10/1","25/1"},
{"2/1","9/2","9/2","6/1","6/1","12/1","20/1","20/1"},
{"2/1","9/2","9/2","6/1","7/1","12/1","16/1","18/1"},
{"2/1","4/1","7/1","7/1","8/1","17/2","12/1","12/1"},
{"2/1","4/1","5/1","11/2","8/1","11/1","16/1","25/1"},
{"2/1","7/2","5/1","11/2","8/1","11/1","14/1","66/1"},
{"2/1","3/1","5/1","15/2","9/1","10/1","14/1","33/1"},
{"2/1","7/2","7/2","9/2","10/1","25/1","25/1","50/1"},
{"2/1","3/1","9/2","6/1","14/1","14/1","16/1","20/1"},
{"2/1","3/1","5/1","11/2","10/1","14/1","14/1","50/1"},
{"5/2","7/2","5/1","13/2","13/2","9/1","16/1","20/1"},
{"5/2","7/2","4/1","6/1","13/2","12/1","18/1","25/1"},
{"5/2","3/1","4/1","15/2","9/1","10/1","12/1","33/1"},
{"5/2","7/2","7/2","11/2","17/2","14/1","16/1","28/1"},
{"5/2","4/1","9/2","5/1","6/1","12/1","16/1","25/1"},
{"5/2","7/2","4/1","13/2","7/1","12/1","14/1","25/1"},
{"5/2","3/1","4/1","4/1","8/1","20/1","33/1","33/1"},
{"5/2","7/2","5/1","13/2","7/1","8/1","12/1","33/1"},
{"5/2","7/2","4/1","5/1","8/1","14/1","14/1","28/1"},
{"5/2","7/2","5/1","13/2","7/1","15/2","16/1","25/1"},
{"5/2","5/2","7/1","7/1","8/1","10/1","14/1","16/1"},
{"5/2","5/2","4/1","6/1","9/1","18/1","20/1","25/1"},
{"5/2","7/2","4/1","5/1","5/1","16/1","33/1","50/1"},
{"5/2","4/1","5/1","6/1","7/1","9/1","10/1","25/1"},
{"5/2","3/1","5/1","11/2","13/2","9/1","25/1","40/1"},
{"5/2","5/1","5/1","6/1","7/1","8/1","10/1","16/1"},
{"5/2","7/2","7/2","11/2","7/1","14/1","18/1","50/1"},
{"5/2","5/2","5/1","7/1","8/1","12/1","16/1","25/1"},
{"5/2","7/2","5/1","6/1","7/1","8/1","16/1","25/1"},
{"5/2","7/2","4/1","5/1","13/2","10/1","33/1","40/1"},
{"5/2","3/1","5/1","11/2","10/1","12/1","12/1","18/1"},
{"5/2","3/1","5/1","6/1","10/1","12/1","14/1","14/1"},
{"5/2","3/1","6/1","6/1","8/1","11/1","12/1","16/1"},
{"5/2","5/2","5/1","11/2","17/2","18/1","20/1","20/1"},
{"5/2","5/2","11/2","7/1","8/1","8/1","20/1","33/1"},
{"5/2","5/2","4/1","13/2","10/1","14/1","20/1","25/1"},
{"5/2","7/2","4/1","5/1","10/1","12/1","12/1","33/1"},
{"5/2","3/1","4/1","13/2","8/1","14/1","16/1","20/1"},
{"5/2","7/2","5/1","6/1","7/1","8/1","14/1","33/1"},
{"5/2","7/2","6/1","13/2","7/1","9/1","9/1","25/1"},
{"5/2","4/1","11/2","11/2","6/1","9/1","10/1","40/1"},
{"5/2","7/2","4/1","6/1","8/1","10/1","16/1","25/1"},
{"5/2","7/2","9/2","11/2","10/1","10/1","14/1","16/1"},
{"5/2","7/2","5/1","8/1","9/1","9/1","11/1","12/1"},
{"5/2","3/1","4/1","6/1","8/1","10/1","20/1","40/1"},
{"5/2","4/1","9/2","9/2","7/1","11/1","20/1","20/1"},
{"5/2","7/2","4/1","6/1","8/1","9/1","16/1","33/1"},
{"5/2","4/1","4/1","9/2","7/1","14/1","20/1","20/1"},
{"5/2","5/1","11/2","11/2","7/1","15/2","17/2","25/1"},
{"3/1","7/2","4/1","6/1","6/1","9/1","16/1","33/1"},
{"3/1","7/2","9/2","5/1","8/1","9/1","16/1","16/1"},
{"3/1","7/2","5/1","5/1","7/1","7/1","16/1","25/1"},
{"3/1","4/1","9/2","5/1","6/1","8/1","20/1","20/1"},
{"3/1","7/2","7/2","6/1","6/1","9/1","20/1","50/1"},
{"3/1","7/2","4/1","9/2","15/2","10/1","16/1","33/1"},
{"3/1","7/2","4/1","4/1","15/2","17/2","28/1","40/1"},
{"3/1","4/1","11/2","6/1","8/1","8/1","9/1","12/1"},
{"3/1","3/1","7/2","4/1","10/1","11/1","33/1","40/1"},
{"3/1","3/1","7/2","7/1","15/2","8/1","20/1","40/1"},
{"3/1","7/2","4/1","7/1","7/1","8/1","12/1","25/1"},
{"3/1","7/2","11/2","7/1","15/2","8/1","9/1","14/1"},
{"3/1","7/2","4/1","7/1","8/1","8/1","10/1","22/1"},
{"3/1","4/1","4/1","9/2","7/1","10/1","16/1","25/1"},
{"3/1","4/1","5/1","6/1","7/1","8/1","9/1","18/1"},
{"3/1","7/2","9/2","5/1","11/2","7/1","33/1","50/1"},
{"3/1","7/2","5/1","11/2","8/1","8/1","12/1","16/1"},
{"3/1","4/1","5/1","5/1","11/2","10/1","12/1","20/1"},
{"3/1","7/2","7/2","9/2","8/1","17/2","33/1","33/1"},
{"3/1","4/1","9/2","11/2","6/1","8/1","10/1","66/1"},
{"3/1","3/1","4/1","5/1","8/1","12/1","20/1","22/1"},
{"3/1","4/1","5/1","7/1","7/1","9/1","10/1","10/1"},
{"3/1","3/1","4/1","6/1","7/1","12/1","16/1","20/1"},
{"3/1","7/2","4/1","9/2","7/1","10/1","20/1","33/1"},
{"3/1","3/1","7/2","4/1","9/1","14/1","33/1","33/1"},
{"3/1","5/1","11/2","13/2","7/1","7/1","15/2","12/1"},
{"3/1","3/1","9/2","9/2","15/2","16/1","16/1","20/1"},
{"3/1","3/1","5/1","7/1","8/1","9/1","9/1","20/1"},
{"3/1","4/1","9/2","6/1","6/1","8/1","16/1","16/1"},
{"3/1","5/1","5/1","11/2","11/2","7/1","9/1","33/1"},
{"3/1","3/1","11/2","11/2","6/1","12/1","12/1","20/1"},
{"3/1","7/2","11/2","7/1","8/1","8/1","9/1","12/1"},
{"3/1","7/2","9/2","8/1","9/1","9/1","9/1","11/1"},
{"3/1","3/1","5/1","6/1","7/1","8/1","12/1","40/1"},
{"3/1","3/1","7/2","7/1","10/1","10/1","12/1","20/1"},
{"3/1","4/1","9/2","11/2","6/1","9/1","10/1","33/1"},
{"3/1","7/2","11/2","6/1","7/1","7/1","9/1","28/1"},
{"3/1","4/1","9/2","5/1","9/1","10/1","10/1","14/1"},
{"3/1","7/2","4/1","13/2","9/1","9/1","12/1","14/1"},
{"3/1","4/1","9/2","5/1","8/1","9/1","10/1","20/1"},
{"3/1","3/1","7/2","9/1","10/1","10/1","11/1","16/1"},
{"3/1","4/1","9/2","13/2","7/1","7/1","8/1","40/1"},
{"3/1","7/2","4/1","5/1","8/1","8/1","20/1","25/1"},
{"3/1","9/2","6/1","13/2","7/1","7/1","17/2","10/1"},
{"3/1","4/1","9/2","6/1","8/1","8/1","10/1","16/1"},
{"3/1","3/1","7/2","11/2","17/2","10/1","18/1","33/1"},
{"3/1","7/2","4/1","5/1","15/2","11/1","16/1","18/1"},
{"3/1","7/2","9/2","5/1","8/1","10/1","12/1","20/1"},
{"7/2","4/1","5/1","5/1","13/2","9/1","10/1","14/1"},
{"7/2","4/1","9/2","6/1","6/1","8/1","11/1","14/1"},
{"7/2","7/2","5/1","6/1","13/2","8/1","12/1","12/1"},
{"7/2","7/2","9/2","9/2","11/2","12/1","16/1","20/1"},
{"7/2","7/2","9/2","11/2","13/2","8/1","14/1","16/1"},
{"7/2","7/2","4/1","5/1","8/1","8/1","14/1","18/1"},
{"7/2","9/2","5/1","11/2","6/1","7/1","8/1","20/1"},
{"7/2","9/2","9/2","11/2","6/1","17/2","11/1","12/1"},
{"7/2","9/2","5/1","6/1","6/1","7/1","8/1","16/1"},
{"7/2","7/2","4/1","13/2","7/1","9/1","12/1","14/1"},
{"7/2","7/2","5/1","5/1","7/1","8/1","10/1","22/1"},
{"7/2","7/2","5/1","11/2","8/1","9/1","9/1","12/1"},
{"7/2","7/2","7/2","5/1","8/1","8/1","16/1","28/1"},
{"7/2","7/2","9/2","9/2","5/1","12/1","18/1","20/1"},
{"7/2","7/2","7/2","13/2","8/1","10/1","10/1","16/1"},
{"7/2","4/1","9/2","6/1","6/1","13/2","10/1","25/1"},
{"7/2","4/1","9/2","5/1","15/2","8/1","10/1","16/1"},
{"7/2","7/2","9/2","11/2","6/1","15/2","12/1","33/1"},
{"7/2","4/1","4/1","4/1","6/1","8/1","25/1","25/1"},
{"7/2","7/2","5/1","5/1","11/2","7/1","14/1","33/1"},
{"7/2","7/2","4/1","9/2","8/1","11/1","14/1","16/1"},
{"7/2","4/1","9/2","13/2","13/2","7/1","9/1","16/1"},
{"7/2","4/1","5/1","11/2","6/1","15/2","12/1","14/1"},
{"4/1","4/1","9/2","6/1","13/2","15/2","9/1","12/1"},
{"4/1","4/1","4/1","5/1","11/2","15/2","12/1","25/1"},
{"4/1","4/1","4/1","4/1","8/1","9/1","10/1","20/1"},
{"4/1","4/1","9/2","11/2","11/2","15/2","12/1","14/1"},
{"4/1","4/1","4/1","5/1","11/2","10/1","12/1","16/1"},
{"4/1","4/1","4/1","9/2","11/2","7/1","14/1","40/1"},
{"9/2","9/2","9/2","5/1","7/1","8/1","9/1","9/1"}
       };
       
       public static void generate678x(ENEStatement statement)
       {
           HorseRacingUtils.convertDecimalToSP(9.51);
           HorseRacingUtils.convertDecimalToSP(19.51);
           HorseRacingUtils.convertDecimalToSP(7.51);
          HorseRacingUtils.convertDecimalToSP(3.51);
           HorseRacingUtils.convertDecimalToSP(2.51);
           HorseRacingUtils.convertDecimalToSP(4.51);
          HorseRacingUtils.convertDecimalToSP(2.71);
           HorseRacingUtils.convertDecimalToSP(2.81);
           HorseRacingUtils.convertDecimalToSP(2.31);
          HorseRacingUtils.convertDecimalToSP(1.71);
           HorseRacingUtils.convertDecimalToSP(1.81);
           HorseRacingUtils.convertDecimalToSP(1.31);
          HorseRacingUtils.convertDecimalToSP(1.41);
           HorseRacingUtils.convertDecimalToSP(1.21);
           HorseRacingUtils.convertDecimalToSP(1.01);
         HorseRacingUtils.convertDecimalToSP(1.5);
           HorseRacingUtils.convertDecimalToSP(1.25);
           HorseRacingUtils.convertDecimalToSP(1.20);

           HorseRacingUtils.convertDecimalToSP(0.9);
           HorseRacingUtils.convertDecimalToSP(0.85);
           HorseRacingUtils.convertDecimalToSP(0.825);
           HorseRacingUtils.convertDecimalToSP(0.8);
           HorseRacingUtils.convertDecimalToSP(0.7);
           HorseRacingUtils.convertDecimalToSP(0.6);
           HorseRacingUtils.convertDecimalToSP(0.57);
           HorseRacingUtils.convertDecimalToSP(0.55);
           HorseRacingUtils.convertDecimalToSP(0.52);
           HorseRacingUtils.convertDecimalToSP(0.5);
           HorseRacingUtils.convertDecimalToSP(0.4);
           HorseRacingUtils.convertDecimalToSP(0.3);
           HorseRacingUtils.convertDecimalToSP(0.22);
       }
       public static void generate678(ENEStatement statement)
       {
           String[] astrOdds =  //{"2/1","7/2","4/1","5/1","8/1","16/1","16/1"};
           {"6/1", "5/1", "6/4", "5/1", "3/1", "6/1"};

           String[] astrBetTypes = {"W"};
           RealSportsBook book = new RealSportsBook(astrOdds, false, astrBetTypes);
           List<RealSportsRunner> alRunners = book.getRunners();
           
           RealSportsAcca.GenerateAcca(book, alRunners, 1, 10000);
           RealSportsAcca.GenerateAcca(book, alRunners, 2, 10000);
           RealSportsAcca.GenerateAcca(book, alRunners, 3, 10000);
           RealSportsAcca.GenerateAcca(book, alRunners, 4, 10000);
           RealSportsAcca.GenerateAcca(book, alRunners, 5, 10000); 
           //RealSportsAcca.GenerateAcca(book, alRunners, 6, 100000);
       }
        public static void generate678v1(ENEStatement statement)
       {
           String[][][] aOdds = new String[1][][];
            aOdds[0] = sm_astrPrices7;
       //     aOdds[1] = sm_astrPrices7;
       //     aOdds[2] = sm_astrPrices8;
            int nKMax =   aOdds.length;
       String[] astrBetTypes =   {"F", "W"}; // , "T" , "O", "F", "H", "P", "T"};  // {"W", "P"};      // W, P, P3, F, T, P1A, P1B
       for(int k = 0; k < nKMax; k++)
       {
            int nIMax = aOdds[k].length;
        for(int i = 0; i < nIMax; i++)    // 
         {
            try
            {
              TimeUnit.SECONDS.sleep(1);      // guarantee unique timestamp - MySQL only goes below seconds in v5.7
              RealSportsBook book = new RealSportsBook(aOdds[k][i], false, astrBetTypes); // don't shuffle
              
              if (book.getMarket("F") != null)
                     book.getMarket("F").setMargin(1.25d);
             if (book.getMarket("T") != null)
                     book.getMarket("T").setMargin(1.25d);
   /*            if (book.getMarket("O") != null)
                     book.getMarket("O").setMargin(1.05d);
              if (book.getMarket("H") != null)
                     book.getMarket("H").setMargin(1.05d); */

              System.out.print(book.trace(false));
              
              if (false)
              {
                ArrayList<RealSportsResultGenerator> alResultsGenerators = new ArrayList<>();
                //alResultsGenerators.add(new RealSportsResultGenerator(1, book));      // recalibrate
                alResultsGenerators.add(new RealSportsResultGenerator(0, book));      // use full spread
                //alResultsGenerators.add(new RealSportsResultGenerator(2, book));      // even distribution

                ArrayList<RealSportsBettingPattern> alBettingPatterns = new ArrayList<>();
                alBettingPatterns.add(new RealSportsFrequencyBettingPattern(1));
      /*         alBettingPatterns.add(new RealSportsPriceBettingPattern("LTEQ", 1));
                alBettingPatterns.add(new RealSportsPriceBettingPattern("LTEQ", 3));
                alBettingPatterns.add(new RealSportsPriceBettingPattern("LTEQ", 5));
                 alBettingPatterns.add(new RealSportsPriceBettingPattern("LTEQ", 10));
               alBettingPatterns.add(new RealSportsPriceBettingPattern("GTEQ", 5));
               alBettingPatterns.add(new RealSportsPriceBettingPattern("GTEQ", 10));
                alBettingPatterns.add(new RealSportsPriceBettingPattern("GTEQ", 16));
               alBettingPatterns.add(new RealSportsPriceBettingPattern("GTEQ", 20)); */

                RealSportsScenario scenario = new RealSportsScenario(statement, book, alResultsGenerators, alBettingPatterns);
                scenario.generateRaces(100, false);
              }
            }
            catch(InterruptedException e)
            {
                
            }
          } 
       }
           
       }
    public static void insertRunner(ENEStatement statement, Timestamp timestamp, String strType, String strBettingPattern, String strRunnerId, int nBets, int nPctProfit, int nWins, int nResultModel)
    {
        int nReturn = 0;
        String strUpdate="insert ";
        strUpdate += "into realsport_scenario_runners (rsr_timestamp, rsr_result_model, rsr_type, rsr_betting_pattern, rsr_runner_id, rsr_nr_bets, rsr_pct_profit, rsr_nr_wins)";
        strUpdate += " values (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement insert = null;
        try 
        {
         insert = statement.getPreparedStatement(strUpdate);
         insert.setTimestamp(1, timestamp);
         insert.setInt(2, nResultModel);
         insert.setString(3, strType);
         insert.setString(4, strBettingPattern);
         insert.setString(5, strRunnerId);
         insert.setInt(6, nBets);
         insert.setInt(7, nPctProfit);
         insert.setInt(8, nWins);
         nReturn  = insert.executeUpdate();
        }
        catch(SQLException e)
        {
            System.out.println("insertRunner Exception: " + e.getMessage());
        }
/*        finally
        {
            if (insert != null)
            {
                try
                {
                    insert.close();
                }
                catch(SQLException e)
                {

                }
            }
        } */
    }
    public static void insertBet(ENEStatement statement, Timestamp timestamp, String strType, String strBettingPattern, int nBets, int nPctProfit, int nWins, int nResultModel)
    {
        int nReturn = 0;
        String strUpdate="insert ";
        strUpdate += "into realsport_scenario_bets (rsb_timestamp, rsb_result_model, rsb_type, rsb_betting_pattern, rsb_nr_bets, rsb_pct_profit, rsb_nr_win_combs)";
        strUpdate += " values (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement insert = null;
        try 
        {
         insert = statement.getPreparedStatement(strUpdate);
         insert.setTimestamp(1, timestamp);
         insert.setInt(2, nResultModel);
         insert.setString(3, strType);
         insert.setString(4, strBettingPattern);
         insert.setInt(5, nBets);
         insert.setInt(6, nPctProfit);
         insert.setInt(7, nWins);
         nReturn  = insert.executeUpdate();
        }
        catch(SQLException e)
        {
            System.out.println("insertBet Exception: " + e.getMessage());
        }
/*        finally
        {
            if (insert != null)
            {
                try
                {
                    insert.close();
                }
                catch(SQLException e)
                {

                }
            }
        } */
    }
public static void insertScenario(ENEStatement statement, Timestamp timestamp, String strSPs, int nRunners, int nRaces)
{
    int nReturn = 0;
    String strUpdate="insert into realsport_scenarios (rs_timestamp, rs_sps, rs_nr_runners, rs_nr_races)";
    strUpdate += " values (?, ?, ?, ?)";
    PreparedStatement insert = null;
    try 
    {
        insert = statement.getPreparedStatement(strUpdate);
     insert.setTimestamp(1, timestamp);
     insert.setString(2, strSPs);
     insert.setInt(3, nRunners);
     insert.setInt(4, nRaces);
     nReturn  = insert.executeUpdate();
    }
    catch(SQLException e)
    {
        System.out.println("insertScenario Exception: " + e.getMessage());
    }
/*    finally
    {
        if (insert != null)
        {
            try
            {
                insert.close();
            }
            catch(SQLException e)
            {
                
            }
        }
    } */
}

public static void generateImages(ENEStatement statement)
{
       int[] aRaces =  {534605, 536226, 522393, 600386, 534615, 539241, 602722, 614970, 484717, 540799, 587169, 481235, 501387, 538687, 604750};  // ArrayUtils.convertIntegers(getHDDRaces(statement, 4));
       //{660278}; 
       //  {470055};  // , 519119, 546329, 466782,464763,474588};
        for(int j = 0; j < aRaces.length; j++)
        {
            int nRace = aRaces[j];
            String strDirectory = ENEColoursEnvironment.getInstance().getVariable("SVG_OUTPUT_DIRECTORY") + ENEColoursEnvironment.getInstance().getVariable("SVG_IMAGE_PATH");
            SmartformRace race = ENEColoursFactory.getSmartformRace(statement, nRace);
            ArrayList<SmartformDailyRunner> alRunners = ENEColoursRunnerFactory.getRacecardRunnerList(statement, race.getRaceId());
            String strRaceDirectory = strDirectory + "realsports/hdd3";
            new File(strRaceDirectory).mkdirs();  
            int nRunners = race.getNrRunners();
            for(int i = 0; i < alRunners.size(); i++)
            {
                try
                {
                    ENERacingColours colours = ENERacingColoursFactory.createRunnerColours(ENEColoursEnvironment.DEFAULT_LANGUAGE, alRunners.get(i));
                    String strJockeyColours = colours.getDefinition();
                    //if (strJockeyColours.toLowerCase().indexOf("harlequin") >= 0)
                    //{
                        Mero.generateSVG(strJockeyColours, strRaceDirectory, nRace + "S-" + String.valueOf(i+1), null, new Point(0, -20), true);
                        Mero.generatePNG(strJockeyColours, strRaceDirectory, nRace + "S-" + String.valueOf(i+1), null, new Point(0, -20), true);
                        Mero.generateSVG(strJockeyColours, strRaceDirectory, nRace + "R-" + String.valueOf(nRunners - i), null, new Point(0, -20), true);
                        Mero.generatePNG(strJockeyColours, strRaceDirectory, nRace + "R-" + String.valueOf(nRunners - i), null, new Point(0, -20), true);
                        //Mero.generateSVG(strJockeyColours, strRaceDirectory + "/side", String.valueOf(i+1), "white", new Point(130, 90),true);  // cap at side
                    //}
                }
                catch(Exception e)
                {
                    System.out.println("Exception generateImages: " + nRace + "-" + (i+1));
                    e.printStackTrace();
                }
            } 
        }
    
}
public static ArrayList<Integer> getHDDRaces(ENEStatement statement, int nHDD)
{
    ArrayList<Integer> alRaces = new ArrayList<>();
     String strQuery = "select race_id from realsport_hdd_races where hdd=" + nHDD;
        ResultSet rs = statement.executeQuery(strQuery);
        if (rs != null)
        {
            try
            {
                while (rs.next())
                {
                    alRaces.add(rs.getInt(1));
                }
            
                rs.close();
            }
            catch(SQLException e)
            {
            }
        }
        
        return alRaces;
}
}
