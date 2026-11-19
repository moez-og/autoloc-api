package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.MaintenanceDto;
import tn.esprit.autoloc.web.dto.MaintenanceDto.SaisieMaintenance;

@Tag(name = "Maintenances")
@RestController
@RequestMapping("/api/maintenances")
public class MaintenanceController extends CrudController<Maintenance, SaisieMaintenance, MaintenanceDto> {

    public MaintenanceController(CrudService crud) {
        super(crud, Maintenance.class, Maintenance::new, MaintenanceDto::idMaintenance);
    }

    @Override
    protected void appliquer(Maintenance maintenance, SaisieMaintenance saisie) {
        maintenance.setDateDebut(saisie.dateDebut());
        maintenance.setDateFin(saisie.dateFin());
        maintenance.setDescription(saisie.description());
        affecterSiFourni(Vehicule.class, saisie.vehiculeId(), maintenance::setVehicule);
    }

    @Override
    protected MaintenanceDto reponse(Maintenance maintenance) {
        return new MaintenanceDto(maintenance.getIdMaintenance(), maintenance.getDateDebut(), maintenance.getDateFin(),
                maintenance.getDescription(), idDe(maintenance.getVehicule(), Vehicule::getIdVehicule));
    }
}
