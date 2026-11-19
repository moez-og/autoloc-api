package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.ContratDto;
import tn.esprit.autoloc.web.dto.ContratDto.SaisieContrat;

@Tag(name = "Contrats")
@RestController
@RequestMapping("/api/contrats")
public class ContratController extends CrudController<Contrat, SaisieContrat, ContratDto> {

    public ContratController(CrudService crud) {
        super(crud, Contrat.class, Contrat::new, ContratDto::idContrat);
    }

    @Override
    protected void appliquer(Contrat contrat, SaisieContrat saisie) {
        contrat.setDateSignature(saisie.dateSignature());
        contrat.setMontantTotal(saisie.montantTotal());
        contrat.setValide(saisie.valide());
        affecterSiFourni(Reservation.class, saisie.reservationId(), contrat::setReservation);
    }

    @Override
    protected ContratDto reponse(Contrat contrat) {
        return new ContratDto(contrat.getIdContrat(), contrat.getDateSignature(), contrat.getMontantTotal(),
                contrat.isValide(), idDe(contrat.getReservation(), Reservation::getIdReservation),
                idsDe(contrat.getPaiements(), Paiement::getIdPaiement));
    }
}
