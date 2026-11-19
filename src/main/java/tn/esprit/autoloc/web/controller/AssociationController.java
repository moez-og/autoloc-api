package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.service.AssociationService;

@Tag(name = "Affectations")
@RestController
@RequestMapping("/api/associations")
public class AssociationController {

    private final AssociationService associations;

    public AssociationController(AssociationService associations) {
        this.associations = associations;
    }

    @PutMapping("/{association}/{sourceId}/{cibleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Affecter une ressource à une autre")
    public void affecter(@PathVariable @Parameter(example = "vehicule-agence") String association,
                         @PathVariable @Positive Long sourceId, @PathVariable @Positive Long cibleId) {
        associations.affecter(association, sourceId, cibleId);
    }

    @DeleteMapping("/{association}/{sourceId}/{cibleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retirer une affectation")
    public void desaffecter(@PathVariable @Parameter(example = "vehicule-agence") String association,
                            @PathVariable @Positive Long sourceId, @PathVariable @Positive Long cibleId) {
        associations.desaffecter(association, sourceId, cibleId);
    }
}
