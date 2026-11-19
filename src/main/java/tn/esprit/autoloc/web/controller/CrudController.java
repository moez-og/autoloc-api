package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.esprit.autoloc.service.CrudService;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CrudController<E, S, D> {

    private final CrudService crud;
    private final Class<E> type;
    private final Supplier<E> constructeur;
    private final Function<D, Long> identifiant;

    protected CrudController(CrudService crud, Class<E> type, Supplier<E> constructeur, Function<D, Long> identifiant) {
        this.crud = crud;
        this.type = type;
        this.constructeur = constructeur;
        this.identifiant = identifiant;
    }

    @GetMapping
    @Operation(summary = "Lister les ressources")
    public List<D> lister() {
        return crud.lister(type, this::reponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lire une ressource")
    public D obtenir(@PathVariable @Positive Long id) {
        return crud.obtenir(type, id, this::reponse);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une ressource")
    public ResponseEntity<D> creer(@Valid @RequestBody S saisie) {
        D resultat = crud.creer(type, () -> {
            E entite = constructeur.get();
            appliquer(entite, saisie);
            return entite;
        }, this::reponse);
        URI adresse = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(identifiant.apply(resultat))
                .toUri();
        return ResponseEntity.created(adresse).body(resultat);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une ressource",
            description = "Les champs principaux sont obligatoires. Les associations omises sont conservées.")
    public D modifier(@PathVariable @Positive Long id, @Valid @RequestBody S saisie) {
        return crud.modifier(type, id, entite -> appliquer(entite, saisie), this::reponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une ressource")
    public void supprimer(@PathVariable @Positive Long id) {
        crud.supprimer(type, id);
    }

    protected abstract void appliquer(E entite, S saisie);

    protected abstract D reponse(E entite);

    protected <R> R reference(Class<R> typeReference, Long id) {
        return crud.exiger(typeReference, id);
    }

    protected <R> void affecterSiFourni(Class<R> typeReference, Long id, Consumer<R> affecter) {
        if (id != null) {
            affecter.accept(reference(typeReference, id));
        }
    }

    protected static <R> Long idDe(R reference, Function<R, Long> lireId) {
        return reference == null ? null : lireId.apply(reference);
    }

    protected static <R> List<Long> idsDe(Collection<R> references, Function<R, Long> lireId) {
        return references.stream().map(lireId).sorted().toList();
    }
}
