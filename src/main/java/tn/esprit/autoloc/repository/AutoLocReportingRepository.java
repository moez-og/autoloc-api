package tn.esprit.autoloc.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AutoLocReportingRepository extends Repository<Vehicule, Long> {

    String DISPONIBILITE = """
            v.statut <> tn.esprit.autoloc.domain.StatutVehicule.MAINTENANCE
            and not exists (
                select r.idReservation from Reservation r
                where r.vehicule = v
                  and (:ignorer is null or r.idReservation <> :ignorer)
                  and r.statut in (tn.esprit.autoloc.domain.StatutReservation.EN_ATTENTE,
                                   tn.esprit.autoloc.domain.StatutReservation.CONFIRMEE)
                  and r.dateDebut < :au and r.dateFin > :du
            )
            and not exists (
                select m.idMaintenance from Maintenance m
                where m.vehicule = v and m.dateDebut < :au and m.dateFin >= :du
            )
            """;

    String LOCATION_ACTIVE = """
            select c.idContrat from Contrat c
            where c.valide = true and c.reservation.vehicule = v
              and c.reservation.statut = tn.esprit.autoloc.domain.StatutReservation.CONFIRMEE
              and c.reservation.dateDebut <= :jour and c.reservation.dateFin > :jour
            """;

    @Query("select v from Vehicule v left join v.agence a where " + DISPONIBILITE + """
            and (:ville is null or lower(a.ville) = lower(:ville))
            and (:categorie is null or v.categorie = :categorie)
            """)
    Page<Vehicule> disponibles(LocalDate du, LocalDate au, String ville, CategorieVehicule categorie,
                               Long ignorer, Pageable page);

    @Query("select count(v) from Vehicule v where v.idVehicule = :id and " + DISPONIBILITE)
    long disponibilite(Long id, LocalDate du, LocalDate au, Long ignorer);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vehicule v where v.idVehicule = :id")
    Optional<Vehicule> verrouillerVehicule(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.idReservation = :id")
    Optional<Reservation> verrouillerReservation(Long id);

    interface RecetteAgence {
        Long getAgenceId();
        String getAgence();
        BigDecimal getMontantEncaisse();
    }

    @Query("""
            select a.idAgence as agenceId, a.nom as agence, sum(p.montant) as montantEncaisse
            from Paiement p join p.contrat c join c.reservation r join r.vehicule v join v.agence a
            group by a.idAgence, a.nom order by a.idAgence
            """)
    List<RecetteAgence> recettes();

    interface EtatFlotte {
        Long getAgenceId();
        String getAgence();
        StatutVehicule getStatut();
        long getNombre();
    }

    @Query("""
            select a.idAgence as agenceId, a.nom as agence, v.statut as statut, count(v) as nombre
            from Vehicule v join v.agence a group by a.idAgence, a.nom, v.statut
            order by a.idAgence, v.statut
            """)
    List<EtatFlotte> flotte();

    @Modifying
    @Query("""
            update Reservation r set r.statut = tn.esprit.autoloc.domain.StatutReservation.TERMINEE
            where r.statut = tn.esprit.autoloc.domain.StatutReservation.CONFIRMEE
              and r.dateFin <= :jour
            """)
    int terminerReservations(LocalDate jour);

    @Modifying
    @Query("""
            update Vehicule v set v.statut = tn.esprit.autoloc.domain.StatutVehicule.DISPONIBLE
            where v.statut = tn.esprit.autoloc.domain.StatutVehicule.LOUE
              and exists (select c.idContrat from Contrat c where c.reservation.vehicule = v
                and c.reservation.statut = tn.esprit.autoloc.domain.StatutReservation.TERMINEE
                and c.reservation.dateFin <= :jour)
              and not exists (""" + LOCATION_ACTIVE + ")")
    int libererVehicules(LocalDate jour);

    @Modifying
    @Query("""
            update Vehicule v set v.statut = tn.esprit.autoloc.domain.StatutVehicule.LOUE
            where v.statut = tn.esprit.autoloc.domain.StatutVehicule.DISPONIBLE
              and exists (""" + LOCATION_ACTIVE + ")")
    int activerLocations(LocalDate jour);

    @Query("""
            select c.idContrat from Contrat c where c.valide = true
              and c.reservation.statut = tn.esprit.autoloc.domain.StatutReservation.CONFIRMEE
              and c.reservation.dateFin > :jour and c.reservation.dateFin <= :echeance
            order by c.reservation.dateFin, c.idContrat
            """)
    List<Long> contratsAEcheance(LocalDate jour, LocalDate echeance);
}
