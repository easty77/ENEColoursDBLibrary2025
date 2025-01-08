package ene.eneform.colours.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Value
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class ColourRunner {
    @EmbeddedId
    BasicRaceId id;
    Integer clothNumber;
    String name;
    String ownerName;
    String jockeyColours;
    String trainerName;
    String jockeyName;
    String inRaceComment;
    String startingPrice;
    Integer finishPosition;
    String finishPositionString;
    Double distanceBeaten;
    Integer stallNumber;
    Integer weightPounds;
    Integer age;
    String gender;
    Integer tackHood;
    Integer tackVisor;
    Integer tackBlinkers;
    Integer tackEyeShield;
    Integer tackEyeCover;
    Integer tackCheekPiece;
    Integer tackPacifiers;
    Integer tackTongueStrap;
    Integer favourite;
    String jacket;
    String sleeves;
    String cap;
    String primaryOwner;
    String ownerClash;
       Integer officialRating;
       Integer penaltyWeight;
       Integer overWeight;
       Integer daysSinceRan;
       Integer distanceTravelled;
       Double distanceWon;
       Integer positionInBetting;
       Double startingPriceDecimal;

}
