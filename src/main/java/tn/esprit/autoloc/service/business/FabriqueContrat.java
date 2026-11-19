package tn.esprit.autoloc.service.business;

import org.springframework.stereotype.Component;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class FabriqueContrat {
    public Contrat creer(Reservation reservation, LocalDate signature, BigDecimal montant) {
        Contrat contrat = new Contrat(null, signature, montant, true);
        contrat.setReservation(reservation);
        reservation.setContrat(contrat);
        return contrat;
    }
}
