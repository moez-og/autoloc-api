package tn.esprit.autoloc.service.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class JournalLocations {
    private static final Logger LOG = LoggerFactory.getLogger(JournalLocations.class);

    @TransactionalEventListener
    public void contratSigne(ContratSigne evenement) {
        LOG.info("Contrat {} signé, échéance le {}.", evenement.contratId(), evenement.fin());
    }
}
