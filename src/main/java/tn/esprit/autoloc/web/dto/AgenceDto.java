package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AgenceDto(
        Long idAgence,
        String nom,
        String ville,
        String adresse,
        String telephone,
        List<Long> vehiculeIds,
        List<Long> employeIds) {

    public record SaisieAgence(
            @NotBlank @Size(max = 255) String nom,
            @NotBlank @Size(max = 255) String ville,
            @NotBlank @Size(max = 255) String adresse,
            @NotBlank @Size(max = 255) String telephone) {
    }
}
