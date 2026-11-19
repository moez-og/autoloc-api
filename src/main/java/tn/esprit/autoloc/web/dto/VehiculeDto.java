package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.StatutVehicule;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record VehiculeDto(
        Long idVehicule,
        String immatriculation,
        String marque,
        String modele,
        CategorieVehicule categorie,
        BigDecimal tarifJournalier,
        StatutVehicule statut,
        Long agenceId,
        List<Long> equipementIds) {

    public record SaisieVehicule(
            @NotBlank @Size(max = 20) String immatriculation,
            @NotBlank @Size(max = 50) String marque,
            @NotBlank @Size(max = 50) String modele,
            @NotNull CategorieVehicule categorie,
            @NotNull @PositiveOrZero @Digits(integer = 8, fraction = 2) BigDecimal tarifJournalier,
            @NotNull StatutVehicule statut,
            @Positive Long agenceId,
            Set<@NotNull @Positive Long> equipementIds) {
    }
}
