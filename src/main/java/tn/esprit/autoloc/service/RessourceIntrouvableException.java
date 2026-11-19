package tn.esprit.autoloc.service;

public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(Class<?> type, Long id) {
        super(type.getSimpleName() + " " + id + " introuvable.");
    }
}
