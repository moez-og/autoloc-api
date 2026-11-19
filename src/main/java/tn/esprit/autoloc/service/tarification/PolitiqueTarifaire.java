package tn.esprit.autoloc.service.tarification;

import java.math.BigDecimal;
import java.time.DayOfWeek;

public interface PolitiqueTarifaire {
    boolean accepte(DayOfWeek jour);
    BigDecimal prixJournalier(BigDecimal base);
}
