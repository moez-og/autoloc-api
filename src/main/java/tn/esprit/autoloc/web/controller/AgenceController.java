package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.AgenceDto;
import tn.esprit.autoloc.web.dto.AgenceDto.SaisieAgence;

@Tag(name = "Agences")
@RestController
@RequestMapping("/api/agences")
public class AgenceController extends CrudController<Agence, SaisieAgence, AgenceDto> {

    public AgenceController(CrudService crud) {
        super(crud, Agence.class, Agence::new, AgenceDto::idAgence);
    }

    @Override
    protected void appliquer(Agence agence, SaisieAgence saisie) {
        agence.setNom(saisie.nom());
        agence.setVille(saisie.ville());
        agence.setAdresse(saisie.adresse());
        agence.setTelephone(saisie.telephone());
    }

    @Override
    protected AgenceDto reponse(Agence agence) {
        return new AgenceDto(agence.getIdAgence(), agence.getNom(), agence.getVille(), agence.getAdresse(),
                agence.getTelephone(), idsDe(agence.getVehicules(), Vehicule::getIdVehicule),
                idsDe(agence.getEmployes(), Employe::getIdEmploye));
    }
}
