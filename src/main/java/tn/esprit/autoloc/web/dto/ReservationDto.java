package tn.esprit.autoloc.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tn.esprit.autoloc.domain.StatutReservation;

import java.time.LocalDate;

public record ReservationDto(
        Long idReservation,
        LocalDate dateDebut,
        LocalDate dateFin,
        StatutReservation statut,
        Long clientId,
        Long vehiculeId,
        Long contratId) {

    public record SaisieReservation(
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin,
            @NotNull StatutReservation statut,
            @Positive Long clientId,
            @Positive Long vehiculeId) {
        @AssertTrue(message = "La date de fin doit suivre la date de début.")
        @JsonIgnore
        public boolean isPeriodeValide() {
            return dateDebut == null || dateFin == null || dateFin.isAfter(dateDebut);
        }
    }
}
