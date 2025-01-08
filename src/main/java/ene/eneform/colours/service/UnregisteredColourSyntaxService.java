package ene.eneform.colours.service;

import ene.eneform.colours.domain.UnregisteredColourSyntax;
import ene.eneform.colours.repository.UnregisteredColourSyntaxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UnregisteredColourSyntaxService {
    private final UnregisteredColourSyntaxRepository ucsRepository;

    UnregisteredColourSyntax findByColours(String colours) {
        return ucsRepository.findByIdColoursAndIdOrganisationAndIdYear(colours, "UK", 0);
    }
}
