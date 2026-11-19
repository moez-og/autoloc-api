package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.VehiculeDto;
import tn.esprit.autoloc.web.dto.VehiculeDto.SaisieVehicule;

import java.util.List;

@Tag(name = "Véhicules")
@RestController
@RequestMapping("/api/vehicules")
public class VehiculeController extends CrudController<Vehicule, SaisieVehicule, VehiculeDto> {

    public VehiculeController(CrudService crud) {
        super(crud, Vehicule.class, Vehicule::new, VehiculeDto::idVehicule);
    }

    @Override
    protected void appliquer(Vehicule vehicule, SaisieVehicule saisie) {
        vehicule.setImmatriculation(saisie.immatriculation());
        vehicule.setMarque(saisie.marque());
        vehicule.setModele(saisie.modele());
        vehicule.setCategorie(saisie.categorie());
        vehicule.setTarifJournalier(saisie.tarifJournalier());
        vehicule.setStatut(saisie.statut());
        affecterSiFourni(Agence.class, saisie.agenceId(), vehicule::setAgence);
        if (saisie.equipementIds() != null) {
            List<Equipement> equipements = saisie.equipementIds().stream()
                    .map(id -> reference(Equipement.class, id))
                    .toList();
            vehicule.getEquipements().clear();
            vehicule.getEquipements().addAll(equipements);
        }
    }

    @Override
    protected VehiculeDto reponse(Vehicule vehicule) {
        return new VehiculeDto(vehicule.getIdVehicule(), vehicule.getImmatriculation(), vehicule.getMarque(),
                vehicule.getModele(), vehicule.getCategorie(), vehicule.getTarifJournalier(), vehicule.getStatut(),
                idDe(vehicule.getAgence(), Agence::getIdAgence),
                idsDe(vehicule.getEquipements(), Equipement::getIdEquipement));
    }
}
