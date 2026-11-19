package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tn.esprit.autoloc.domain.RoleEmploye;

public record EmployeDto(
        Long idEmploye,
        String nom,
        String prenom,
        RoleEmploye role,
        Long agenceId) {

    public record SaisieEmploye(
            @NotBlank @Size(max = 255) String nom,
            @NotBlank @Size(max = 255) String prenom,
            @NotNull RoleEmploye role,
            @Positive Long agenceId) {
    }
}
