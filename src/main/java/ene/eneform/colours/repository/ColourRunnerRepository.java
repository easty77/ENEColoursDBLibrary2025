package ene.eneform.colours.repository;

import ene.eneform.colours.domain.BasicRace;
import ene.eneform.colours.domain.ColourRunner;
import ene.eneform.smartform.bos.SmartformRunner;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ColourRunnerRepository extends Repository<ColourRunner, Integer> {
@NativeQuery("""
                select 'SF' as aru_source, ru.cloth_number, ru.name, ru.owner_name, jockey_colours, ru.trainer_name, ru.jockey_name, in_race_comment, starting_price, coalesce(amended_position, finish_position, 0) as finish_position, coalesce(finish_position, unfinished) as finish_position_string, distance_beaten, ru.stall_number, ru.weight_pounds, ru.age, ru.gender,
                ru.tack_hood, ru.tack_visor, ru.tack_blinkers, ru.tack_eye_shield, ru.tack_eye_cover, ru.tack_cheek_piece, ru.tack_pacifiers, ru.tack_tongue_strap
                ,case when position_in_betting = 1 then 1 else 0 end as favourite
                , coalesce(wi1.wi_jacket, wi2.wi_jacket, ucs_jacket) as jacket, coalesce(wi1.wi_sleeves, wi2.wi_sleeves, ucs_sleeves) as sleeves, coalesce(wi1.wi_cap, wi2.wi_cap, ucs_cap) as cap, coalesce(wi1.wi_owner, wi2.wi_owner,'') as primary_owner, coalesce(wix.wi_owner, '') as owner_clash
   , ru.official_rating, ru.penalty_weight, ru.over_weight, ru.days_since_ran, ru.distance_travelled, ru.distance_won, ru.position_in_betting, ru.starting_price_decimal
   from (((((((( historic_runners ru inner join historic_races ra on ra.race_id=ru.race_id)
   left outer join daily_races dra on ra.race_id=dra.race_id)
 left outer join daily_runners dru on dra.race_id=dru.race_id and ru.name=dru.name)
  left outer join unregistered_colour_syntax on replace(trim(dru.jockey_colours), ' & ', ' and ')=ucs_colours and (country=ucs_organisation or (country in ('England', 'Wales', 'Scotland', 'Northern Ireland', 'EireXX') and ucs_organisation = 'UK')) and (year(ra.meeting_date) = ucs_year or ucs_year = 0))
 left outer join racing_colours_parse rcp on rcp_description=trim(dru.jockey_colours) and rcp_version=:rcpVersion)
 left outer join wikipedia_images wi1 on coalesce(ucs_jacket,rcp_jacket)=wi1.wi_jacket and coalesce(ucs_sleeves,rcp_sleeves)=wi1.wi_sleeves and coalesce(ucs_cap,rcp_cap)=wi1.wi_cap and coalesce(rcp_unresolved, '') = '')
 left outer join wikipedia_images wi2 on replace(trim(dru.jockey_colours), ' & ', ' and ')=wi2.wi_description)
 left outer join wikipedia_images wix on replace(dru.owner_name, ' & ', ' and ')=wix.wi_owner and concat(rcp_jacket, rcp_sleeves, rcp_cap) != concat(wix.wi_jacket, wix.wi_sleeves, wix.wi_cap))
 where ru.race_id==:raceId
 and (
 (:numRunners = 0 and (unfinished is null or amended_position is not null))
 or (:numRunners>0 and coalesce(amended_position, finish_position, 99) <= :numRunners)
 or (:numRunners<0 and coalesce(unfinished, '') != 'Non-Runner')
 )
 order by coalesce(amended_position, finish_position, 99), num_fences_jumped desc
 """)
List<ColourRunner> findSmartformByRaceId(Integer raceId, Integer numRunners, String rcpVersion);

    @NativeQuery("""
                select :source as aru_source, ru.cloth_number, ru.name, ru.owner_name, jockey_colours, ru.trainer_name, ru.jockey_name, in_race_comment, starting_price, coalesce(amended_position, finish_position, 0) as finish_position, coalesce(finish_position, unfinished) as finish_position_string, distance_beaten, ru.stall_number, ru.weight_pounds, ru.age, ru.gender,
 ru.tack_hood, ru.tack_visor, ru.tack_blinkers, ru.tack_eye_shield, ru.tack_eye_cover, ru.tack_cheek_piece, ru.tack_pacifiers, ru.tack_tongue_strap
, favourite
, coalesce(wi1.wi_jacket, wi2.wi_jacket, ucs_jacket) as jacket, coalesce(wi1.wi_sleeves, wi2.wi_sleeves, ucs_sleeves) as sleeves, coalesce(wi1.wi_cap, wi2.wi_cap, ucs_cap) as cap, coalesce(wi1.wi_owner, wi2.wi_owner,'') as primary_owner, coalesce(wix.wi_owner, '') as owner_clash
,  0 as official_rating, ru.weight_penalty as penalty_weight, 0 as over_weight, null as days_since_ran, null as distance_travelled, null as distance_won, null as position_in_betting, starting_price_decimal
from (((((((( additional_runners ru inner join additional_races ra on aru_source=ara_source and ru.race_id=ra.race_id)
left outer join additional_race_link on aru_source = arl_source and ru.race_id=arl_race_id)
 left outer join additional_race_data on ard_name=arl_name)
left outer join unregistered_colour_syntax on replace(trim(ru.jockey_colours), ' & ', ' and ')=ucs_colours and (ard_country=ucs_organisation or (ard_country in ('England', 'Wales', 'Scotland', 'Northern Ireland', 'EireXX') and ucs_organisation = 'UK')) and (year(ra.meeting_date) = ucs_year or ucs_year = 0))
left outer join racing_colours_parse rcp on rcp_description=trim(ru.jockey_colours) and rcp_version==:rcpVersion)
left outer join wikipedia_images wi1 on coalesce(ucs_jacket,rcp_jacket)=wi1.wi_jacket and coalesce(ucs_sleeves,rcp_sleeves)=wi1.wi_sleeves and coalesce(ucs_cap,rcp_cap)=wi1.wi_cap and coalesce(rcp_unresolved, '') = '')
left outer join wikipedia_images wi2 on replace(trim(ru.jockey_colours), ' & ', ' and ')=wi2.wi_description)
left outer join wikipedia_images wix on replace(ru.owner_name, ' & ', ' and ')=wix.wi_owner and concat(rcp_jacket, rcp_sleeves, rcp_cap) != concat(wix.wi_jacket, wix.wi_sleeves, wix.wi_cap))
where ru.race_id=:raceId and aru_source=:source
and (
 (:numRunners = 0 and (unfinished is null or amended_position is not null))
 or (:numRunners>0 and coalesce(amended_position, finish_position, 99) <= :numRunners)
 or (:numRunners<0 and coalesce(unfinished, '') != 'Non-Runner')
 )
 order by coalesce(amended_position, finish_position, 99)
        """)
    List<ColourRunner> findAdditionalBySourceAndRaceId(String source, Integer raceId, Integer numRunners, String rcpVersion);
}
