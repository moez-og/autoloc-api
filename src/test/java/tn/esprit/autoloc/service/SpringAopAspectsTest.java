package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import tn.esprit.autoloc.aspect.ExceptionHandlingAspect;
import tn.esprit.autoloc.aspect.LoggingAspect;
import tn.esprit.autoloc.aspect.PerformanceAspect;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringJUnitConfig(SpringAopAspectsTest.ConfigurationAop.class)
@ExtendWith(OutputCaptureExtension.class)
class SpringAopAspectsTest {
    @Autowired
    private ServiceDeTest service;

    @Test
    void journaliseUnAppelSansExposerLesTextes(CapturedOutput console) {
        AtomicInteger executions = new AtomicInteger();
        String resultat = service.reussir(42, "donnee-privee-en-entree", executions);

        assertThat(AopUtils.isAopProxy(service)).isTrue();
        assertThat(executions.get()).isEqualTo(1);
        assertThat(resultat).isEqualTo("donnee-privee-en-sortie");
        assertThat(console.getAll()).contains("Début méthode ServiceDeTest.reussir(..)", "42", "Fin de", "terminé en")
                .doesNotContain("donnee-privee-en-entree", "donnee-privee-en-sortie");
    }

    @Test
    void journaliseUnEchecEtPropageLaMemeException(CapturedOutput console) {
        AtomicInteger executions = new AtomicInteger();
        IllegalStateException echec = new IllegalStateException("Échec métier simulé");

        assertThatThrownBy(() -> service.echouer(echec, executions)).isSameAs(echec);

        assertThat(executions.get()).isEqualTo(1);
        assertThat(console.getAll()).contains("Début méthode ServiceDeTest.echouer(..)", "Échec de",
                "IllegalStateException", "Fin de", "terminé en");
    }

    @Test
    void avertitQuandUneMethodeDepasseUneSeconde(CapturedOutput console) throws InterruptedException {
        service.attendre(1050);

        assertThat(console.getAll()).contains("seuil de 1000 ms dépassé");
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableAspectJAutoProxy
    @Import({LoggingAspect.class, PerformanceAspect.class, ExceptionHandlingAspect.class})
    static class ConfigurationAop {
        @Bean
        ServiceDeTest serviceDeTest() {
            return new ServiceDeTest();
        }
    }

    @Service
    @TestComponent
    static class ServiceDeTest {
        public String reussir(int nombre, String texte, AtomicInteger executions) {
            executions.incrementAndGet();
            return "donnee-privee-en-sortie";
        }

        public void echouer(IllegalStateException echec, AtomicInteger executions) {
            executions.incrementAndGet();
            throw echec;
        }

        public void attendre(long millisecondes) throws InterruptedException {
            TimeUnit.MILLISECONDS.sleep(millisecondes);
        }
    }
}
