package tn.esprit.autoloc.service.tarification;

import org.springframework.stereotype.Service;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Vehicule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TarificationService {
    private static final BigDecimal OPTION_PAR_JOUR = new BigDecimal("5.00");
    private static final BigDecimal MONTANT_MAX = new BigDecimal("99999999.99");

    private final List<PolitiqueTarifaire> politiques;

    public TarificationService(List<PolitiqueTarifaire> politiques) {
        this.politiques = politiques;
    }

    public record Devis(long jours, BigDecimal prixVehicule, BigDecimal prixOptions, BigDecimal montantTotal) {
    }

    public Devis calculer(Vehicule vehicule, LocalDate du, LocalDate au, Set<Long> equipementIds) {
        long jours = duree(du, au);
        Set<Long> options = equipementIds == null ? Set.of() : equipementIds;
        Set<Long> proposes = vehicule.getEquipements().stream()
                .map(Equipement::getIdEquipement).collect(Collectors.toSet());
        if (!proposes.containsAll(options)) {
            throw new IllegalArgumentException("Un des équipements choisis est indisponible pour ce véhicule.");
        }

        BigDecimal prixVehicule = BigDecimal.ZERO;
        for (DayOfWeek jour : DayOfWeek.values()) {
            int decalage = Math.floorMod(jour.getValue() - du.getDayOfWeek().getValue(), 7);
            long occurrences = jours / 7 + (decalage < jours % 7 ? 1 : 0);
            PolitiqueTarifaire politique = politiques.stream().filter(p -> p.accepte(jour)).findFirst().orElseThrow();
            prixVehicule = prixVehicule.add(politique.prixJournalier(vehicule.getTarifJournalier())
                    .multiply(BigDecimal.valueOf(occurrences)));
        }
        prixVehicule = prixVehicule.setScale(2, RoundingMode.HALF_UP);
        BigDecimal prixOptions = OPTION_PAR_JOUR.multiply(BigDecimal.valueOf(jours))
                .multiply(BigDecimal.valueOf(options.size()));
        BigDecimal total = prixVehicule.add(prixOptions);
        if (total.compareTo(MONTANT_MAX) > 0) {
            throw new IllegalArgumentException("Le montant dépasse la capacité de stockage du contrat.");
        }
        return new Devis(jours, prixVehicule, prixOptions, total);
    }

    public static long duree(LocalDate du, LocalDate au) {
        if (du == null || au == null || !du.isBefore(au)) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début.");
        }
        return ChronoUnit.DAYS.between(du, au);
    }
}
