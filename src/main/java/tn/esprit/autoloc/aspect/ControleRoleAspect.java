package tn.esprit.autoloc.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Aspect
@Component
public class ControleRoleAspect {
    private final HttpServletRequest requete;
    private final byte[] autorisationManager;

    public ControleRoleAspect(HttpServletRequest requete, @Value("${autoloc.manager.token:}") String jeton) {
        this.requete = requete;
        this.autorisationManager = jeton.isBlank() ? new byte[0]
                : ("Bearer " + jeton).getBytes(StandardCharsets.UTF_8);
    }

    @Before("@annotation(tn.esprit.autoloc.aspect.ManagerRequis)")
    public void verifierManager() {
        String valeur = requete.getHeader(HttpHeaders.AUTHORIZATION);
        if (autorisationManager.length == 0 || valeur == null
                || !MessageDigest.isEqual(autorisationManager, valeur.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Une autorisation de responsable est requise.");
        }
    }
}
