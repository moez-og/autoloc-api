package tn.esprit.autoloc.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExceptionHandlingAspect {
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlingAspect.class);

    @AfterThrowing(pointcut = "tn.esprit.autoloc.aspect.LoggingAspect.methodesServices()", throwing = "exception")
    public void journaliserException(JoinPoint appel, Throwable exception) {
        LOG.error("Échec de {} ({})", appel.getSignature().toShortString(), exception.getClass().getSimpleName());
    }
}
