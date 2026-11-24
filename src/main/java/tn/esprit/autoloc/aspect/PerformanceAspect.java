package tn.esprit.autoloc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class PerformanceAspect {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceAspect.class);
    private static final long SEUIL_MS = 1000;

    @Around("tn.esprit.autoloc.aspect.LoggingAspect.methodesServices()")
    public Object mesurer(ProceedingJoinPoint appel) throws Throwable {
        long debut = System.nanoTime();
        try {
            return appel.proceed();
        } finally {
            long dureeNanos = System.nanoTime() - debut;
            long dureeMs = TimeUnit.NANOSECONDS.toMillis(dureeNanos);
            String methode = appel.getSignature().toShortString();
            if (dureeNanos > TimeUnit.MILLISECONDS.toNanos(SEUIL_MS)) {
                LOG.warn("{} terminé en {} ms (seuil de {} ms dépassé)", methode, dureeMs, SEUIL_MS);
            } else {
                LOG.info("{} terminé en {} ms", methode, dureeMs);
            }
        }
    }
}
