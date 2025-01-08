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
public class UnregisteredColourSyntaxId implements Serializable {
    @Column(name = "ucs_organisation")
    public final String organisation;
    @Column(name = "ucs_year")
    public final Integer year;
    @Column(name = "ucs_colours")
    public final String colours;
}