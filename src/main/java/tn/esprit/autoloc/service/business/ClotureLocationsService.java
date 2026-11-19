package tn.esprit.autoloc.service.business;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.repository.AutoLocReportingRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClotureLocationsService {
    private final AutoLocReportingRepository reporting;

    public ClotureLocationsService(AutoLocReportingRepository reporting) {
        this.reporting = reporting;
    }

    public record Bilan(int reservationsTerminees, int vehiculesLiberes, int locationsActivees,
                        int contratsAEcheance, List<Long> contratIdsAEcheance) {
    }

    @Transactional
    public Bilan actualiser() {
        LocalDate jour = LocationService.aujourdhui();
        int terminees = reporting.terminerReservations(jour);
        int liberes = reporting.libererVehicules(jour);
        int actives = reporting.activerLocations(jour);
        List<Long> echeances = reporting.contratsAEcheance(jour, jour.plusDays(3));
        return new Bilan(terminees, liberes, actives, echeances.size(), echeances);
    }
}
