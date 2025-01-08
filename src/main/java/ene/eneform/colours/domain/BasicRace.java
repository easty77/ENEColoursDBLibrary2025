package ene.eneform.colours.domain;

import ene.eneform.smartform.bos.SmartformEnvironment;
import jakarta.persistence.*;
import lombok.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;

@Entity
@Value
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class BasicRace implements BasicRaceInfo {
    @EmbeddedId
    private final BasicRaceId id;
    private LocalDate meetingDate;
    private String course;
    private String raceType;
    private Integer distanceYards;
    private String going;
    private String raceName;
    private Integer groupRace;
    private Integer handicap;
    private Integer numRunners;
    private String direction;

    @Override
    public String getSource() {
        return id.source;
    }
    @Override
    public Integer getRaceId() {
        return id.raceId;
    }
    @Override
    public Integer getYear() {
        return meetingDate.getYear();
    }
    public String getFormattedMeetingDate()
    {
        if(meetingDate != null)
            return SmartformEnvironment.getInstance().getDateFormat().format(meetingDate);
        else
            return "";
    }

    public String getFormattedMeetingDate(String strFormat)
    {
        if(meetingDate != null)
            return new SimpleDateFormat( strFormat ).format(meetingDate);
        else
            return "";
    }
    public String getYearString() {
        return getYear().toString();
    }
    public String getSeasonString()
    {
        String strSeason;
        if ("Flat".equals(raceType) || "All Weather Flat".equals(raceType))
            strSeason = getYearString();
        else if (meetingDate.getMonthValue() > 7)
        {
            strSeason = getYearString() + "-" + String.valueOf(meetingDate.getYear() + 1901).substring(2);
        }
        else
        {
            strSeason =  String.valueOf(meetingDate.getYear() + 1899) + "-" + getYearString().substring(2);
        }

        return strSeason;
    }
    public String getFormattedDistance() {
        return getFormattedDistance(distanceYards, true, false, false);
    }
    private String getFormattedDistance(int nDistanceYards, boolean bAbbrev, boolean bYards, boolean bHTML)
    {
        String strMileLabel = bAbbrev ? "m" : " mile";
        String strFurlongLabel = bAbbrev ? "f" : " furlong";
        String strYardsLabel = bAbbrev ? "y" : " yds";

        int nMiles = nDistanceYards/(220 * 8);
        int nRemainder = (nDistanceYards - (nMiles * 220 * 8));
        int nFurlongs = nRemainder/220;
        nRemainder = (nDistanceYards - (nMiles * 220 * 8) - (nFurlongs * 220));
        int nYards = nRemainder;
        if ((!bYards) && (nFurlongs == 7) && (nYards > 165))
        {
            nFurlongs = 0;
            nYards = 0;
            nMiles++;
        }
        String strDistance = "";
        if (nMiles > 0)
            strDistance += (nMiles + strMileLabel);
        if ((nMiles > 1) && (!bAbbrev))
            strDistance += "s";
        strDistance += " ";

        String strFurlongs = "";
        if (nFurlongs > 0)
        {
            strFurlongs = java.lang.String.valueOf(nFurlongs);
            if (!bYards)
            {
                if (nYards > 165)
                {
                    strFurlongs = java.lang.String.valueOf(nFurlongs + 1);
                }
                else if (nYards > 55)
                    strFurlongs += (bHTML ? "&frac12;" : "�");
            }

            strFurlongs += strFurlongLabel;

            if ((nFurlongs > 1) && (!bAbbrev))
                strFurlongs += "s";
        }
        else if (!bYards)
        {
            if (nYards > 165)
            {
                strFurlongs = "1" + strFurlongLabel;
            }
            else if (nYards > 55)
            {
                strFurlongs = (bHTML ? "&frac12;" : "�") + strFurlongLabel;
            }
        }
        if (!"".equals(strFurlongs))
        {
            strFurlongs += " ";
            strDistance += strFurlongs;
        }

        if (bYards && (nYards > 0))
        {
            strDistance += (nYards + strYardsLabel);
        }

        return strDistance.trim();
    }
    public String getAbbreviatedTitle() {
        return getAbbreviatedTitle(this.raceName);
    }
    private String getAbbreviatedTitle(String strTitle)
    {
        String[] astrFullName ={"Beginners' Chase","Beginners Chase", "3-Y-O Maiden Hurdle", "Maiden Hurdle", "Maiden Stakes", "Conditions Stakes", "'Newcomers' NH Flat Race",
                "Intermediate Open NH Flat Race", "Standard Open NH Flat Race", "Member Flat Race", "(Pro/Am) Flat Race"};
        for (int i = 0; i < astrFullName.length; i++)
        {
            if (strTitle.toLowerCase().indexOf(astrFullName[i].toLowerCase()) >= 0)
            {
                strTitle = astrFullName[i];
                strTitle = strTitle.replace("Beginners' Chase", "Beginners Chase");
                return strTitle;
            }
        }

        int nBracket = strTitle.indexOf("(");
        if (nBracket > 0)
            strTitle = strTitle.substring(0, nBracket - 1);

        int nSponsoredBy = strTitle.toLowerCase().indexOf(" sponsored by");
        if (nSponsoredBy > 0)
            strTitle = strTitle.substring(0, nSponsoredBy);

        int nEmpoweredBy = strTitle.toLowerCase().indexOf(" empowered by");
        if (nEmpoweredBy > 0)
            strTitle = strTitle.substring(0, nEmpoweredBy);

        int nPresentedBy = strTitle.toLowerCase().indexOf(" presented by");
        if (nPresentedBy > 0)
            strTitle = strTitle.substring(0, nPresentedBy);

        strTitle = strTitle.replace("Novices' Hurdle", "Novice Hurdle");
        strTitle = strTitle.replace("Novices' Chase", "Novice Chase");


        strTitle = removeSponsor(strTitle);

        strTitle=strTitle.replaceAll("Showcase Race", "");
        strTitle=strTitle.replaceAll("Grade 1", "");

        if (strTitle.length() > 1)
            strTitle = strTitle.substring(0, 1).toUpperCase() + strTitle.substring(1);

        return strTitle.trim();
    }
    private String removeSponsor(String strRaceTitle)
    {
        String[] astrNoReplace ={"Betfair Chase", "Betfair Hurdle", "Betfred Sprint Cup", "Bet365 Hurdle", "Bet365 Chase", "Racing Post Trophy", "Racing Post Chase", "Racing Post Novice Chase", "Ryanair Hurdle", "Ryanair Chase", "Royal Sunalliance Novices' Hurdle"};
        for (int i = 0; i < astrNoReplace.length; i++)
        {
            if (strRaceTitle.equalsIgnoreCase(astrNoReplace[i]))
                return strRaceTitle;
        }

        if ("Dobbins & Madigans At Punchestown Hurdle".equalsIgnoreCase(strRaceTitle))
            strRaceTitle =  "Dobbins & Madigans At Punchestown Morgiana Hurdle";;

        strRaceTitle = replaceSponsorString(strRaceTitle);

        return strRaceTitle.replace("Vi ", "VI ").replace("Rsa ", "RSA ");
    }
    private String replaceSponsorString(String strRaceTitle)
    {
        String[] astrReplace ={"William Hill", "John Smith's", "Keith Prowse Hospitality", "Coral-", "Crabbie's", "Racing Uk On Virgin 536", "Cathay Pacific",
                "A.P. Wins Sports Personality", "Rabobank", "Rewards4Racing", "Www.punchestown.com", "JLT", "888sport", "Duty Free", "Foster's", "Crowson",  "Garrard",
                "paddypower.com iPhone App", "Paddypower.com", "Paddy Power", "Evening Herald", "Dobbins & Madigans At Punchestown", "Juddmonte", "Qipco", "Market Slide", "QNB", "Willmott Dixon",
                "Martell", "Deloitte And Touche", "Duggan Brothers", "Doom Bar", "Qatar", "Investec", "williamhill.com", "Matalan", "JCB", "Racing Post", "Longines", "Coolmore", "Pearl Bloodstock",
                "Merewood Homes", "Merewood Group", "HBLB", "Quantel", "Grangewood", "JPMorgan Private Bank", "JPMorgan", "J. P. Morgan", "Weatherbys Insurance",
                "GNER", "Great North Eastern Railway", "Tooheys New", "Champagne Lanson", "Vodafone", "Jefferson Smurfit Memorial", "Kingspin", "Darley",
                "Commercial First", "Cantor Fitzgerald", "MBNA", "Dubai Duty Free Finest", "Vision.ae", "188Bet", "bet365", "32Red"};
        for (int i = 0; i < astrReplace.length; i++)
        {
            int nIndex = strRaceTitle.toLowerCase().indexOf(astrReplace[i].toLowerCase());
            if (nIndex == 0)
            {
                return strRaceTitle.substring(astrReplace[i].length()).trim();
            }
        }
        astrReplace = new String[]{"Avonmore Waterford", "Avonmore", "AIG Europe", "A.I.B. Agri-Business", "ABN Amro", "Andrex"};
        for (int i = 0; i < astrReplace.length; i++)
        {
            int nIndex = strRaceTitle.toLowerCase().indexOf(astrReplace[i].toLowerCase());
            if (nIndex == 0)
            {
                return strRaceTitle.substring(astrReplace[i].length()).trim();
            }
        }

        astrReplace = new String[]{"Betfred TV", "Betfair Price Rush", "betfred.com", "Bonusprint.com", "Betfred", "Betfair", "Bet365.com", "Bet365", "Bibby Financial Services Ireland", "Bathwick Tyres",
                "Byrne Group", "BGC Partners", "Bar One Racing", "BHP Insurances", "BHP Insurance", "BetVictor", "Betway", "Bovis Homes", "Bonusprint"};
        for (int i = 0; i < astrReplace.length; i++)
        {
            int nIndex = strRaceTitle.toLowerCase().indexOf(astrReplace[i].toLowerCase());
            if (nIndex == 0)
            {
                return strRaceTitle.substring(astrReplace[i].length()).trim();
            }
        }
        astrReplace = new String[]{"Ladbrokes", "Lough Derg"};
        for (int i = 0; i < astrReplace.length; i++)
        {
            int nIndex = strRaceTitle.toLowerCase().indexOf(astrReplace[i].toLowerCase());
            if (nIndex == 0)
            {
                return strRaceTitle.substring(astrReplace[i].length()).trim();
            }
        }
        astrReplace = new String[]{"StanJames.com", "Stan James", "Seasons Holidays", "Sportinglife.com", "Sportingbet.com", "Sportingbet", "Smurfit",
                "Stanley Cooker", "Shell", "Sodexho", "Sagitta", "Sky Bet", "Stella Artois"};
        for (int i = 0; i < astrReplace.length; i++)
        {
            int nIndex = strRaceTitle.toLowerCase().indexOf(astrReplace[i].toLowerCase());
            if (nIndex == 0)
            {
                return strRaceTitle.substring(astrReplace[i].length()).trim();
            }
        }
        astrReplace = new String[]{"Totesport.com", "Totesport", "Totescoop6", "Totepool.com", "Totepool", "Tattersalls Millions", "Bet Online With TheTote.com", "Thebettingsite.com"};
        for (int i = 0; i < astrReplace.length; i++)
        {
            int nIndex = strRaceTitle.toLowerCase().indexOf(astrReplace[i].toLowerCase());
            if (nIndex == 0)
            {
                return strRaceTitle.substring(astrReplace[i].length()).trim();
            }
        }
        return strRaceTitle;
    }
    public String getRaceType()
    {
        if ("N_H_Flat".equals(raceType))
            return "National Hunt Flat";
        else if ("A_W_Flat".equals(raceType))
            return "All Weather Flat";
        else
            return raceType;
    }
}
