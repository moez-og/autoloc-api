package tn.esprit.autoloc.service.tarification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Component
public class TarifWeekend implements PolitiqueTarifaire {
    private static final BigDecimal COEFFICIENT = new BigDecimal("1.20");

    @Override
    public boolean accepte(DayOfWeek jour) {
        return jour == DayOfWeek.SATURDAY || jour == DayOfWeek.SUNDAY;
    }

    @Override
    public BigDecimal prixJournalier(BigDecimal base) {
        return base.multiply(COEFFICIENT);
    }
}
