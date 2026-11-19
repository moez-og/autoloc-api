package tn.esprit.autoloc.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EquipementDto(
        Long idEquipement,
        String libelle,
        List<Long> vehiculeIds) {

    public record SaisieEquipement(
            @NotBlank @Size(max = 255) String libelle) {
    }
}
