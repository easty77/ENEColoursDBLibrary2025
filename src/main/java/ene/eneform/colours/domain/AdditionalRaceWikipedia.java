package ene.eneform.colours.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="additional_race_wikipedia")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AdditionalRaceWikipedia {
    @EmbeddedId
    private final AdditionalRaceWikipediaId id;
    @Column(name="arw_wikipedia_ref")
    String href;
}
