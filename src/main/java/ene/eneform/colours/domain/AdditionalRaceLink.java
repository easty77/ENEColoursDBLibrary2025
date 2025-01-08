package ene.eneform.colours.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="additional_race_link")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AdditionalRaceLink implements BasicRaceInfo {
    @AttributeOverrides({
            @AttributeOverride(name="raceId",
                    column=@Column(name="arl_race_id")),
            @AttributeOverride(name="source",
                    column=@Column(name="arl_source"))
    })
    @EmbeddedId
    private final BasicRaceId id;
     @Column(name = "arl_name")
    private String raceName;
    @Column(name = "arl_year")
    private Integer year;

    public static AdditionalRaceLink onCreate(String source, Integer raceId, String raceName, Integer year) {
        return new AdditionalRaceLink(new BasicRaceId(source, raceId), raceName, year);
    }
    public AdditionalRaceLink onUpdate(String raceName, Integer year) {
        this.raceName = raceName;
        this.year = year;
        return this;
    }

    @Override
    public String getSource() {
        return id.source;
    }
    @Override
    public Integer getRaceId() {
        return id.raceId;
    }
}
