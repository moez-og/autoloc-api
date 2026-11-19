package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.EquipementDto;
import tn.esprit.autoloc.web.dto.EquipementDto.SaisieEquipement;

@Tag(name = "Équipements")
@RestController
@RequestMapping("/api/equipements")
public class EquipementController extends CrudController<Equipement, SaisieEquipement, EquipementDto> {

    public EquipementController(CrudService crud) {
        super(crud, Equipement.class, Equipement::new, EquipementDto::idEquipement);
    }

    @Override
    protected void appliquer(Equipement equipement, SaisieEquipement saisie) {
        equipement.setLibelle(saisie.libelle());
    }

    @Override
    protected EquipementDto reponse(Equipement equipement) {
        return new EquipementDto(equipement.getIdEquipement(), equipement.getLibelle(),
                idsDe(equipement.getVehicules(), Vehicule::getIdVehicule));
    }
}
