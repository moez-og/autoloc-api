package tn.esprit.autoloc.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(public * tn.esprit.autoloc.service..*.*(..))"
            + " && @within(org.springframework.stereotype.Service)")
    public void methodesServices() {
    }

    @Before("methodesServices()")
    public void journaliserDebut(JoinPoint appel) {
        var arguments = Arrays.stream(appel.getArgs()).map(LoggingAspect::pourJournal).toList();
        LOG.info("Début méthode {} avec paramètres {}", appel.getSignature().toShortString(), arguments);
    }

    @After("methodesServices()")
    public void journaliserFin(JoinPoint appel) {
        LOG.info("Fin de {}", appel.getSignature().toShortString());
    }

    @AfterReturning(pointcut = "methodesServices()", returning = "resultat")
    public void journaliserRetour(JoinPoint appel, Object resultat) {
        LOG.debug("Retour normal de {} ({})", appel.getSignature().toShortString(),
                resultat == null ? "sans valeur" : resultat.getClass().getSimpleName());
    }

    private static String pourJournal(Object argument) {
        if (argument == null) {
            return "null";
        }
        if (argument instanceof Class<?> type) {
            return type.getSimpleName();
        }
        if (argument instanceof Number || argument instanceof Boolean
                || argument instanceof Enum<?> || argument instanceof LocalDate) {
            return argument.toString();
        }
        if (argument instanceof Set<?> identifiants && identifiants.stream().allMatch(Long.class::isInstance)) {
            return identifiants.toString();
        }
        return "<masqué>";
    }
}
