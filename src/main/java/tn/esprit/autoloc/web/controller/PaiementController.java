package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.PaiementDto;
import tn.esprit.autoloc.web.dto.PaiementDto.SaisiePaiement;

@Tag(name = "Paiements")
@RestController
@RequestMapping("/api/paiements")
public class PaiementController extends CrudController<Paiement, SaisiePaiement, PaiementDto> {

    public PaiementController(CrudService crud) {
        super(crud, Paiement.class, Paiement::new, PaiementDto::idPaiement);
    }

    @Override
    protected void appliquer(Paiement paiement, SaisiePaiement saisie) {
        paiement.setMontant(saisie.montant());
        paiement.setDatePaiement(saisie.datePaiement());
        paiement.setModePaiement(saisie.modePaiement());
        affecterSiFourni(Contrat.class, saisie.contratId(), paiement::setContrat);
    }

    @Override
    protected PaiementDto reponse(Paiement paiement) {
        return new PaiementDto(paiement.getIdPaiement(), paiement.getMontant(), paiement.getDatePaiement(),
                paiement.getModePaiement(), idDe(paiement.getContrat(), Contrat::getIdContrat));
    }
}
