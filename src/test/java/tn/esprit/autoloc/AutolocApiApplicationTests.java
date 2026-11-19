package tn.esprit.autoloc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.ModePaiement;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.RoleEmploye;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AutolocApiApplicationTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional
    void reliePlusieursVehiculesALaMemeAgence() {
        Agence agence = new Agence(null, "Agence de test", "Tunis", "Adresse de test", "00000000");
        entityManager.persist(agence);

        Vehicule clio = nouveauVehicule("Renault", "Clio", "95.50");
        Vehicule polo = nouveauVehicule("Volkswagen", "Polo", "90.00");
        clio.setAgence(agence);
        polo.setAgence(agence);
        entityManager.persist(clio);
        entityManager.persist(polo);
        entityManager.flush();
        entityManager.clear();

        Vehicule clioRelue = entityManager.find(Vehicule.class, clio.getIdVehicule());
        assertThat(clioRelue.getAgence().getIdAgence()).isEqualTo(agence.getIdAgence());

        Agence agenceRelue = entityManager.find(Agence.class, agence.getIdAgence());
        assertThat(agenceRelue.getVehicules()).extracting(Vehicule::getIdVehicule)
                .containsExactlyInAnyOrder(clio.getIdVehicule(), polo.getIdVehicule());
    }

    @Test
    @Transactional
    void reliePlusieursEmployesALaMemeAgence() {
        Agence agence = new Agence(null, "Agence de test", "Tunis", "Adresse de test", "00000000",
                new ArrayList<>());
        entityManager.persist(agence);

        Employe amira = new Employe(null, "Test", "Amira", RoleEmploye.AGENT);
        Employe sami = new Employe(null, "Test", "Sami", RoleEmploye.MANAGER);
        amira.setAgence(agence);
        sami.setAgence(agence);
        entityManager.persist(amira);
        entityManager.persist(sami);
        entityManager.flush();
        entityManager.clear();

        Employe amiraRelue = entityManager.find(Employe.class, amira.getIdEmploye());
        assertThat(amiraRelue.getAgence().getIdAgence()).isEqualTo(agence.getIdAgence());

        Agence agenceRelue = entityManager.find(Agence.class, agence.getIdAgence());
        assertThat(agenceRelue.getEmployes()).extracting(Employe::getIdEmploye)
                .containsExactlyInAnyOrder(amira.getIdEmploye(), sami.getIdEmploye());
    }

    @Test
    @Transactional
    void partageUnEquipementEtRetireUnSeulLien() {
        Equipement gps = new Equipement(null, "GPS");
        Equipement siege = new Equipement(null, "Siège bébé");
        entityManager.persist(gps);
        entityManager.persist(siege);

        Vehicule clio = nouveauVehicule("Renault", "Clio", "95.50");
        Vehicule polo = nouveauVehicule("Volkswagen", "Polo", "90.00");
        clio.getEquipements().add(gps);
        clio.getEquipements().add(siege);
        polo.getEquipements().add(gps);
        entityManager.persist(clio);
        entityManager.persist(polo);
        entityManager.flush();
        entityManager.clear();

        Vehicule clioRelue = entityManager.find(Vehicule.class, clio.getIdVehicule());
        assertThat(clioRelue.getEquipements()).extracting(Equipement::getIdEquipement)
                .containsExactlyInAnyOrder(gps.getIdEquipement(), siege.getIdEquipement());

        Equipement gpsRelu = entityManager.find(Equipement.class, gps.getIdEquipement());
        assertThat(gpsRelu.getVehicules()).extracting(Vehicule::getIdVehicule)
                .containsExactlyInAnyOrder(clio.getIdVehicule(), polo.getIdVehicule());

        clioRelue.getEquipements().remove(gpsRelu);
        entityManager.flush();
        entityManager.clear();

        Equipement gpsApresRetrait = entityManager.find(Equipement.class, gps.getIdEquipement());
        assertThat(gpsApresRetrait.getVehicules()).extracting(Vehicule::getIdVehicule)
                .containsExactly(polo.getIdVehicule());

        Vehicule clioApresRetrait = entityManager.find(Vehicule.class, clio.getIdVehicule());
        assertThat(clioApresRetrait.getEquipements()).extracting(Equipement::getIdEquipement)
                .containsExactly(siege.getIdEquipement());
    }

    @Test
    @Transactional
    void enregistreLesReservationsAvecUnNouveauClient() {
        LocalDate inscription = LocalDate.of(2026, 11, 19);
        Client lina = new Client(null, "Test", "Lina", "lina@example.test", "00000000",
                "PERMIS-TP2", inscription);
        Vehicule clio = nouveauVehicule("Renault", "Clio", "95.50");
        entityManager.persist(clio);

        Reservation premiere = new Reservation(null, inscription.plusDays(2), inscription.plusDays(4),
                StatutReservation.EN_ATTENTE);
        Reservation deuxieme = new Reservation(null, inscription.plusWeeks(1), inscription.plusWeeks(2),
                StatutReservation.EN_ATTENTE);

        premiere.setVehicule(clio);
        deuxieme.setVehicule(clio);
        premiere.setClient(lina);
        deuxieme.setClient(lina);
        lina.getReservations().add(premiere);
        lina.getReservations().add(deuxieme);
        entityManager.persist(lina);
        entityManager.flush();
        entityManager.clear();

        Client linaRelue = entityManager.find(Client.class, lina.getIdClient());
        assertThat(linaRelue.getReservations()).extracting(Reservation::getIdReservation)
                .containsExactlyInAnyOrder(premiere.getIdReservation(), deuxieme.getIdReservation());

        Reservation premiereRelue = entityManager.find(Reservation.class, premiere.getIdReservation());
        assertThat(premiereRelue.getClient().getIdClient()).isEqualTo(lina.getIdClient());
        assertThat(premiereRelue.getVehicule().getIdVehicule()).isEqualTo(clio.getIdVehicule());
    }

    @Test
    @Transactional
    void lieLeContratALaReservationEtPropageLaPersistanceEtLaSuppressionDesPaiements() {
        LocalDate signature = LocalDate.of(2026, 12, 5);
        Reservation reservation = new Reservation(null, signature, signature.plusDays(3),
                StatutReservation.CONFIRMEE);
        entityManager.persist(reservation);

        Contrat contrat = new Contrat(null, signature, new BigDecimal("180.00"), true);
        contrat.setReservation(reservation);
        Paiement acompte = new Paiement(null, new BigDecimal("60.00"), signature, ModePaiement.CARTE);
        Paiement solde = new Paiement(null, new BigDecimal("120.00"), signature.plusDays(3), ModePaiement.CARTE);
        acompte.setContrat(contrat);
        solde.setContrat(contrat);
        contrat.getPaiements().add(acompte);
        contrat.getPaiements().add(solde);
        entityManager.persist(contrat);
        entityManager.flush();
        entityManager.clear();

        Reservation reservationRelue = entityManager.find(Reservation.class, reservation.getIdReservation());
        Contrat contratRelu = reservationRelue.getContrat();
        assertThat(contratRelu.getIdContrat()).isEqualTo(contrat.getIdContrat());
        assertThat(contratRelu.getReservation().getIdReservation()).isEqualTo(reservation.getIdReservation());
        assertThat(contratRelu.getPaiements()).extracting(Paiement::getIdPaiement)
                .containsExactlyInAnyOrder(acompte.getIdPaiement(), solde.getIdPaiement());

        reservationRelue.setContrat(null);
        entityManager.remove(contratRelu);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Contrat.class, contrat.getIdContrat())).isNull();
        assertThat(entityManager.find(Paiement.class, acompte.getIdPaiement())).isNull();
        assertThat(entityManager.find(Paiement.class, solde.getIdPaiement())).isNull();
        assertThat(entityManager.find(Reservation.class, reservation.getIdReservation())).isNotNull();
    }

    @Test
    @Transactional
    void enregistreLeNouveauVehiculeAvecSaMaintenance() {
        LocalDate debut = LocalDate.of(2026, 12, 5);
        Vehicule clio = nouveauVehicule("Renault", "Clio", "95.50");
        Maintenance revision = new Maintenance(null, debut, debut.plusDays(1), "Révision");
        revision.setVehicule(clio);
        entityManager.persist(revision);
        entityManager.flush();
        entityManager.clear();

        Maintenance revisionRelue = entityManager.find(Maintenance.class, revision.getIdMaintenance());
        assertThat(revisionRelue.getVehicule().getIdVehicule()).isEqualTo(clio.getIdVehicule());

        entityManager.remove(revisionRelue);
        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(Maintenance.class, revision.getIdMaintenance())).isNull();
        assertThat(entityManager.find(Vehicule.class, clio.getIdVehicule())).isNotNull();
    }

    private Vehicule nouveauVehicule(String marque, String modele, String tarif) {
        String immatriculation = "TP2-" + UUID.randomUUID().toString().substring(0, 16);
        return new Vehicule(null, immatriculation, marque, modele,
                CategorieVehicule.CITADINE, new BigDecimal(tarif), StatutVehicule.DISPONIBLE);
    }
}
