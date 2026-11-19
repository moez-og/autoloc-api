package tn.esprit.autoloc.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tn.esprit.autoloc.service.business.ClotureLocationsService;

@Component
public class ContratEcheanceScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(ContratEcheanceScheduler.class);

    private final ClotureLocationsService cloture;

    public ContratEcheanceScheduler(ClotureLocationsService cloture) {
        this.cloture = cloture;
    }

    @Scheduled(cron = "${autoloc.scheduler.cron:0 0 1 * * *}", zone = "Africa/Tunis")
    public void actualiserLesLocations() {
        LOG.info("Bilan planifié des locations {}", cloture.actualiser());
    }
}
