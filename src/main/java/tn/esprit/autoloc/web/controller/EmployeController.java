package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.EmployeDto;
import tn.esprit.autoloc.web.dto.EmployeDto.SaisieEmploye;

@Tag(name = "Employés")
@RestController
@RequestMapping("/api/employes")
public class EmployeController extends CrudController<Employe, SaisieEmploye, EmployeDto> {

    public EmployeController(CrudService crud) {
        super(crud, Employe.class, Employe::new, EmployeDto::idEmploye);
    }

    @Override
    protected void appliquer(Employe employe, SaisieEmploye saisie) {
        employe.setNom(saisie.nom());
        employe.setPrenom(saisie.prenom());
        employe.setRole(saisie.role());
        affecterSiFourni(Agence.class, saisie.agenceId(), employe::setAgence);
    }

    @Override
    protected EmployeDto reponse(Employe employe) {
        return new EmployeDto(employe.getIdEmploye(), employe.getNom(), employe.getPrenom(), employe.getRole(),
                idDe(employe.getAgence(), Agence::getIdAgence));
    }
}
