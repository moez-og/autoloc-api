package tn.esprit.autoloc.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class PerformanceAspect {
    private static final Logger LOG = LoggerFactory.getLogger(PerformanceAspect.class);

    @Around("execution(public * tn.esprit.autoloc.service.business.*Service.*(..))")
    public Object mesurer(ProceedingJoinPoint appel) throws Throwable {
        long debut = System.nanoTime();
        try {
            return appel.proceed();
        } finally {
            LOG.info("{} terminé en {} ms", appel.getSignature().toShortString(),
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - debut));
        }
    }
}
