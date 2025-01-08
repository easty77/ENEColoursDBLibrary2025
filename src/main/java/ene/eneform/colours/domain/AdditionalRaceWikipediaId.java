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
public class AdditionalRaceWikipediaId implements Serializable {
    @Column(name = "arw_name")
    public final String name;
    @Column(name = "arw_language")
    public final String language;
}