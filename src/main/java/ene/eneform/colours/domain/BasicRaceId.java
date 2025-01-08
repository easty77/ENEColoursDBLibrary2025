package ene.eneform.colours.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class BasicRaceId implements Serializable {
    @Column(name = "source")
    public final String source;
    @Column(name = "race_id")
    public final Integer raceId;
}