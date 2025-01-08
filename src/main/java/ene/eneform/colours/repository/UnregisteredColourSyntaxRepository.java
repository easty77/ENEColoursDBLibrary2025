package ene.eneform.colours.repository;

import ene.eneform.colours.domain.RacingColoursParse;
import ene.eneform.colours.domain.UnregisteredColourSyntax;
import org.springframework.data.repository.CrudRepository;

public interface UnregisteredColourSyntaxRepository extends CrudRepository<UnregisteredColourSyntax, String> {
UnregisteredColourSyntax findByIdColoursAndIdOrganisationAndIdYear(String colours, String organisation, int year);
}
