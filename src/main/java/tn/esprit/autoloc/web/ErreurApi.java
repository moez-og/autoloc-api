package tn.esprit.autoloc.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tn.esprit.autoloc.service.ConflitMetierException;
import tn.esprit.autoloc.service.RessourceIntrouvableException;

import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class ErreurApi {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail champsInvalides(MethodArgumentNotValidException erreur) {
        Map<String, String> champs = new TreeMap<>();
        erreur.getBindingResult().getFieldErrors().forEach(champ ->
                champs.putIfAbsent(champ.getField(), champ.getDefaultMessage()));
        ProblemDetail resultat = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Les données de la requête sont invalides.");
        resultat.setProperty("erreurs", champs);
        return resultat;
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class, ConstraintViolationException.class
    })
    public ProblemDetail requeteInvalide() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La requête contient une valeur invalide.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentInvalide(IllegalArgumentException erreur) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, erreur.getMessage());
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ProblemDetail introuvable(RessourceIntrouvableException erreur) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, erreur.getMessage());
    }

    @ExceptionHandler(ConflitMetierException.class)
    public ProblemDetail conflit(ConflitMetierException erreur) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, erreur.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integrite() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "L'opération est bloquée par une contrainte d'unicité ou par une ressource associée.");
    }
}
