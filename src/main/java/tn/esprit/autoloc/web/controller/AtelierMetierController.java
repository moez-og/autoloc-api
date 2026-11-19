package tn.esprit.autoloc.web.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.aspect.ManagerRequis;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.repository.AutoLocReportingRepository;
import tn.esprit.autoloc.service.business.ClotureLocationsService;
import tn.esprit.autoloc.service.business.LocationService;
import tn.esprit.autoloc.service.tarification.TarificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/metier")
public class AtelierMetierController {
    private final LocationService locations;
    private final ClotureLocationsService cloture;

    public AtelierMetierController(LocationService locations, ClotureLocationsService cloture) {
        this.locations = locations;
        this.cloture = cloture;
    }

    public record DevisRequest(@NotNull @Positive Long vehiculeId, @NotNull LocalDate dateDebut,
                               @NotNull LocalDate dateFin, Set<@NotNull @Positive Long> equipementIds) {
    }

    public record ReservationRequest(@NotNull @Positive Long clientId, @NotNull @Positive Long vehiculeId,
                                      @NotNull LocalDate dateDebut, @NotNull LocalDate dateFin) {
    }

    public record SignatureRequest(Set<@NotNull @Positive Long> equipementIds) {
    }

    @GetMapping("/vehicules-disponibles")
    public LocationService.PageVehicules disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) CategorieVehicule categorie,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int taille) {
        return locations.disponibles(du, au, ville, categorie, page, taille);
    }

    @PostMapping("/devis")
    public TarificationService.Devis devis(@Valid @RequestBody DevisRequest demande) {
        return locations.devis(demande.vehiculeId(), demande.dateDebut(), demande.dateFin(), demande.equipementIds());
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationService.ReservationCreee reserver(@Valid @RequestBody ReservationRequest demande) {
        return locations.reserver(demande.clientId(), demande.vehiculeId(), demande.dateDebut(), demande.dateFin());
    }

    @PostMapping("/reservations/{id}/signer")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationService.ContratCree signer(@PathVariable Long id,
                                              @Valid @RequestBody(required = false) SignatureRequest demande) {
        return locations.signer(id, demande == null ? Set.of() : demande.equipementIds());
    }

    @GetMapping("/statistiques/recettes")
    public List<AutoLocReportingRepository.RecetteAgence> recettes() {
        return locations.recettes();
    }

    @GetMapping("/statistiques/flotte")
    public List<AutoLocReportingRepository.EtatFlotte> flotte() {
        return locations.flotte();
    }

    @GetMapping("/clients/{id}/reservations")
    public List<LocationService.ReservationCreee> reservationsDuClient(
            @PathVariable Long id, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int taille) {
        return locations.reservationsDuClient(id, page, taille);
    }

    @ManagerRequis
    @SecurityRequirement(name = "manager")
    @PostMapping("/clotures")
    public ClotureLocationsService.Bilan cloturer() {
        return cloture.actualiser();
    }
}
