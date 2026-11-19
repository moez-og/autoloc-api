package tn.esprit.autoloc.web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MaintenanceDto(
        Long idMaintenance,
        LocalDate dateDebut,
        LocalDate dateFin,
        String description,
        Long vehiculeId) {

    public record SaisieMaintenance(
            @NotNull LocalDate dateDebut,
            @NotNull LocalDate dateFin,
            @NotBlank @Size(max = 255) String description,
            @Positive Long vehiculeId) {
        @AssertTrue(message = "La date de fin doit être égale ou postérieure à la date de début.")
        @JsonIgnore
        public boolean isPeriodeValide() {
            return dateDebut == null || dateFin == null || !dateFin.isBefore(dateDebut);
        }
    }
}
