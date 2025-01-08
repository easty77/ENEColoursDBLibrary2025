package ene.eneform.colours.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="racing_colours_parse")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class UnregisteredColourSyntax implements RacingColours {
    @EmbeddedId
    private final UnregisteredColourSyntaxId id;
     @Column(name = "ucs_jacket")
    private String jacket;
    @Column(name = "ucs_sleeves")
    private String sleeves;
    @Column(name = "ucs_cap")
    private String cap;
    @Column(name = "ucs_comments")
    private String comments;

    public static UnregisteredColourSyntax onCreate(String organisation, Integer year, String colours, String jacket, String sleeves, String cap, String comments) {
        return new UnregisteredColourSyntax(new UnregisteredColourSyntaxId(organisation, year, colours), jacket, sleeves, cap, comments);
    }

    public UnregisteredColourSyntax onUpdate(String jacket, String sleeves, String cap, String comments) {
        this.jacket = jacket;
        this.sleeves = sleeves;
        this.cap = cap;
        this.comments = comments;
        return this;
    }
    public String getUnresolved() {
        return "";
    }
}
