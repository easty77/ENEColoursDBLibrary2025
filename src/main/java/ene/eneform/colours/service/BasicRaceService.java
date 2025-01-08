package ene.eneform.colours.service;

import ene.eneform.colours.domain.AdditionalRaceLink;
import ene.eneform.colours.domain.BasicRace;
import ene.eneform.colours.domain.BasicRaceId;
import ene.eneform.colours.repository.BasicRaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicRaceService {
    private final BasicRaceRepository raceRepository;

    List<BasicRace> findByHorseAndBred(String horseName, String bred) {
        return raceRepository.findByHorseAndBred(horseName, bred);
    }

    BasicRace findById(String source, int raceId) {
        return raceRepository.findById(new BasicRaceId(source, raceId));
    }

}
