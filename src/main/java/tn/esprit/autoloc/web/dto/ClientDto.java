package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ClientDto(
        Long idClient,
        String nom,
        String prenom,
        String email,
        String telephone,
        String numPermis,
        LocalDate dateInscription,
        List<Long> reservationIds) {

    public record SaisieClient(
            @NotBlank @Size(max = 255) String nom,
            @NotBlank @Size(max = 255) String prenom,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 255) String telephone,
            @NotBlank @Size(max = 255) String numPermis,
            @NotNull LocalDate dateInscription) {
    }
}
