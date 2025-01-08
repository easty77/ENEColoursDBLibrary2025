package ene.eneform.colours.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="racing_colours_parse")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RacingColoursParse implements RacingColours {
    @EmbeddedId
    private final RacingColoursParseId id;
     @Column(name = "rcp_jacket")
    private String jacket;
    @Column(name = "rcp_sleeves")
    private String sleeves;
    @Column(name = "rcp_cap")
    private String cap;
    @Column(name = "rcp_expanded")
    private String expanded;
    @Column(name = "rcp_unresolved")
    private String unresolved;
    @Column(name = "rcp_syntax")
    private String syntax;
    public static RacingColoursParse onCreate(String description, String version, String jacket, String sleeves, String cap, String expanded, String unresolved, String syntax) {
        return new RacingColoursParse(new RacingColoursParseId(description, version), jacket, sleeves, cap, expanded, unresolved, syntax);
    }
    public RacingColoursParse onUpdate(String jacket, String sleeves, String cap, String expanded, String unresolved, String syntax) {
        this.jacket = jacket;
        this.sleeves = sleeves;
        this.cap = cap;
        this.expanded = expanded;
        this.unresolved = unresolved;
        this.syntax = syntax;
        return this;
    }
}
