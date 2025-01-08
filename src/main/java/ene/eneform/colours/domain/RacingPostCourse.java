package ene.eneform.colours.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="racing_post_course")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class RacingPostCourse {
    @Id
    @Column(name="rp_course_code")
    String code;
    @Column(name="rp_course_name")
    String name;
    @Column(name="rp_country")
    String country;
}
