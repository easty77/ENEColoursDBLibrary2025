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
@Table(name="sporting_life_course")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class SportingLifeCourse {
    @Id
    @Column(name="sl_course_name")
    String name;
    @Column(name="sl_course_id")
    Integer id;
    @Column(name="sl_country")
    String country;
}
