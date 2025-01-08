package ene.eneform.colours.domain;

import ene.eneform.utils.ENEStatement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="wikipedia_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class WikipediaImage implements OwnerColours {
    @Id
    @Column(name = "wi_owner")
    private final String owner;
    @Column(name = "wi_jacket")
    private String jacket;
    @Column(name = "wi_sleeves")
    private String sleeves;
    @Column(name = "wi_cap")
    private String cap;
    @Column(name = "wi_description")
    private String colours;
    @Column(name = "wi_comments")
    private String comments;
    @Column(name = "wi_version")
    private Integer version;
    @UpdateTimestamp
    @Column(name = "wi_timestamp")
    private LocalDateTime timestamp;

    public static WikipediaImage onCreate(String owner, String jacket, String sleeves, String cap, String colours, String comments) {
        return new WikipediaImage(owner, jacket, sleeves, cap, colours, comments);
    }

    protected WikipediaImage(String owner, String jacket, String sleeves, String cap, String colours, String comments) {
this.owner = owner;
this.jacket = jacket;
this.sleeves = sleeves;
this.cap = cap;
this.colours = colours;
this.comments = comments;
this.version = 0;
    }
    public WikipediaImage onUpdate(String jacket, String sleeves, String cap, String colours, String comments) {
        this.jacket = jacket;
        this.sleeves = sleeves;
        this.cap = cap;
        this.colours = colours;
        this.comments = comments;
        this.version++;
        return this;
    }
}