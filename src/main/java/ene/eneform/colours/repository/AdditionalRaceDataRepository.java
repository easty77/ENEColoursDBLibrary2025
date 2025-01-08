package ene.eneform.colours.repository;

import ene.eneform.colours.domain.AdditionalRaceData;
import org.springframework.data.repository.CrudRepository;

public interface AdditionalRaceDataRepository extends CrudRepository<AdditionalRaceData, String> {
    public AdditionalRaceData findByName(String name);
}
