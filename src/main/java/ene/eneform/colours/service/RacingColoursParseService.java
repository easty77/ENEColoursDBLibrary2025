package ene.eneform.colours.service;

import ene.eneform.colours.database.WikipediaFactory;
import ene.eneform.colours.domain.RacingColoursParse;
import ene.eneform.colours.domain.UnregisteredColourSyntax;
import ene.eneform.colours.domain.WikipediaImage;
import ene.eneform.colours.repository.RacingColoursParseRepository;
import ene.eneform.colours.repository.WikipediaImageRepository;
import ene.eneform.mero.colours.ENERacingColours;
import ene.eneform.mero.config.ENEColoursEnvironment;
import ene.eneform.mero.parse.ENEColoursParser;
import ene.eneform.smartform.bos.SmartformColoursRunner;
import ene.eneform.smartform.factory.SmartformRunnerFactory;
import ene.eneform.utils.ENEStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RacingColoursParseService {
    private final RacingColoursParseRepository rcpRepository;

    public void insertRacingColoursParse(String version, ENERacingColours colours, String unresolved, String expanded, String syntax) {
        rcpRepository.findById(colours.getDescription()).ifPresentOrElse(
                rcp -> {
                    RacingColoursParse save = rcp.onUpdate(colours.getJacket().toString(), colours.getSleeves().toString(), colours.getCap().toString(),
                                expanded, unresolved, syntax);
                        rcpRepository.save(save);

                },
                () -> {
                    RacingColoursParse rcp = RacingColoursParse.onCreate(colours.getDescription(), version, colours.getJacket().toString(), colours.getSleeves().toString(), colours.getCap().toString(),
                            expanded, unresolved, syntax);
        rcpRepository.save(rcp);
                }
        );
    }
}
