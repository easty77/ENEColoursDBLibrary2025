package ene.eneform.colours.service;

import ene.eneform.colours.domain.BasicRace;
import ene.eneform.colours.domain.BasicRaceId;
import ene.eneform.colours.domain.BasicRaceInfo;
import ene.eneform.colours.domain.ColourRunner;
import ene.eneform.colours.repository.ColourRunnerRepository;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColourRunnerService {
    private final ColourRunnerRepository runnerRepository;

    List<ColourRunner> findByRaceAndNumRunners(BasicRaceInfo race, int numRunners) {
        String rcpVersion = SmartformRunnerFactory.sm_RCPVersion;
      if ("SF".equals(race.getSource())) {
          return runnerRepository.findSmartformByRaceId(race.getRaceId(), numRunners, rcpVersion);
      } else {
          return runnerRepository.findAdditionalBySourceAndRaceId(race.getSource(), race.getRaceId(), numRunners, rcpVersion);
      }
     }

}
