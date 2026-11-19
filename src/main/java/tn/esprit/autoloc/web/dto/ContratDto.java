package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContratDto(
        Long idContrat,
        LocalDate dateSignature,
        BigDecimal montantTotal,
        boolean valide,
        Long reservationId,
        List<Long> paiementIds) {

    public record SaisieContrat(
            @NotNull LocalDate dateSignature,
            @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal montantTotal,
            @NotNull Boolean valide,
            @Positive Long reservationId) {
    }
}
