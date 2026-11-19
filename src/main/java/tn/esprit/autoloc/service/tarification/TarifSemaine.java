package tn.esprit.autoloc.service.tarification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Component
public class TarifSemaine implements PolitiqueTarifaire {

    @Override
    public boolean accepte(DayOfWeek jour) {
        return jour.getValue() <= DayOfWeek.FRIDAY.getValue();
    }

    @Override
    public BigDecimal prixJournalier(BigDecimal base) {
        return base;
    }
}
