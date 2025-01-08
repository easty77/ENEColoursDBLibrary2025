package ene.eneform.colours.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name="additional_race_data")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class AdditionalRaceData {
    @Id
    private final String id;
     @Column(name = "ard_name")
     private String name;
    @Column(name = "ard_title")
    private String title;
    @Column(name = "ard_course")
    private String course;
    @Column(name = "ard_race_type")
    private String raceType;
    @Column(name = "ard_track_type")
    private String trackType;
    @Column(name = "ard_direction")
    private String direction;
    @Column(name = "ard_country")
    private String country;
     // ard_country, ard_course, ard_month, ard_day, ard_dow, ard_handicap, ard_distance_yards,
    // ard_group_race, ard_class, ard_age_range, ard_num_fences, ard_comments,
    // ard_standard_time_disp, ard_standard_time_secs, ard_sex, ard_start_year,
    // ard_end_year, ard_keywords,
    // sl_course_id, rp_course_code, ard_se_key, ard_GS_ref



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ard_course", referencedColumnName="sl_course_name", insertable=false, updatable=false)
    private SportingLifeCourse sportingLifeCourse;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ard_course", referencedColumnName="rp_course_name", insertable=false, updatable=false)
     private RacingPostCourse racingPostCourse;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name="arw_name", referencedColumnName="ard_name", insertable=false, updatable=false)
     private List<AdditionalRaceWikipedia> additionalRaceWikipedia;
}
