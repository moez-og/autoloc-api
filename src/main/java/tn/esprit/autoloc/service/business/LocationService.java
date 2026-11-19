package tn.esprit.autoloc.service.business;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.CategorieVehicule;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.StatutReservation;
import tn.esprit.autoloc.domain.StatutVehicule;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.AutoLocReportingRepository;
import tn.esprit.autoloc.repository.ContratRepository;
import tn.esprit.autoloc.repository.ReservationRepository;
import tn.esprit.autoloc.service.ConflitMetierException;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.service.RessourceIntrouvableException;
import tn.esprit.autoloc.service.tarification.TarificationService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
public class LocationService {

    private final CrudService crud;
    private final AutoLocReportingRepository reporting;
    private final ReservationRepository reservations;
    private final ContratRepository contrats;
    private final TarificationService tarifs;
    private final FabriqueContrat fabrique;
    private final ApplicationEventPublisher evenements;

    public LocationService(CrudService crud, AutoLocReportingRepository reporting, ReservationRepository reservations,
                           ContratRepository contrats, TarificationService tarifs, FabriqueContrat fabrique,
                           ApplicationEventPublisher evenements) {
        this.crud = crud;
        this.reporting = reporting;
        this.reservations = reservations;
        this.contrats = contrats;
        this.tarifs = tarifs;
        this.fabrique = fabrique;
        this.evenements = evenements;
    }

    public record VehiculeDisponible(Long idVehicule, String immatriculation, String marque, String modele,
                                     CategorieVehicule categorie, BigDecimal tarifJournalier) {
        static VehiculeDisponible depuis(Vehicule vehicule) {
            return new VehiculeDisponible(vehicule.getIdVehicule(), vehicule.getImmatriculation(),
                    vehicule.getMarque(), vehicule.getModele(), vehicule.getCategorie(),
                    vehicule.getTarifJournalier());
        }
    }

    public record PageVehicules(List<VehiculeDisponible> contenu, int page, int taille, long total) {
    }

    public record ReservationCreee(Long idReservation, Long clientId, Long vehiculeId,
                                   LocalDate dateDebut, LocalDate dateFin, StatutReservation statut) {
    }

    public record ContratCree(Long idContrat, Long reservationId, LocalDate dateSignature, BigDecimal montantTotal) {
    }

    @Transactional(readOnly = true)
    public PageVehicules disponibles(LocalDate du, LocalDate au, String ville, CategorieVehicule categorie,
                                     int page, int taille) {
        TarificationService.duree(du, au);
        if (page < 0 || taille < 1 || taille > 100) {
            throw new IllegalArgumentException("La page commence à 0 et la taille doit être entre 1 et 100.");
        }
        var resultat = reporting.disponibles(du, au, ville == null || ville.isBlank() ? null : ville.trim(),
                categorie, null, PageRequest.of(page, taille, Sort.by("idVehicule")));
        return new PageVehicules(resultat.getContent().stream().map(VehiculeDisponible::depuis).toList(),
                page, taille, resultat.getTotalElements());
    }

    @Transactional(readOnly = true)
    public TarificationService.Devis devis(Long vehiculeId, LocalDate du, LocalDate au, Set<Long> options) {
        return tarifs.calculer(crud.exiger(Vehicule.class, vehiculeId), du, au, options);
    }

    @Transactional(readOnly = true)
    public List<ReservationCreee> reservationsDuClient(Long clientId, int page, int taille) {
        crud.exiger(Client.class, clientId);
        if (page < 0 || taille < 1 || taille > 100) {
            throw new IllegalArgumentException("La page commence à 0 et la taille doit être entre 1 et 100.");
        }
        return reservations.findByClient_IdClientOrderByDateDebutDescIdReservationDesc(clientId,
                        PageRequest.of(page, taille)).stream()
                .map(r -> new ReservationCreee(r.getIdReservation(), clientId,
                        r.getVehicule() == null ? null : r.getVehicule().getIdVehicule(),
                        r.getDateDebut(), r.getDateFin(), r.getStatut())).toList();
    }

    @Transactional(readOnly = true)
    public List<AutoLocReportingRepository.RecetteAgence> recettes() {
        return reporting.recettes();
    }

    @Transactional(readOnly = true)
    public List<AutoLocReportingRepository.EtatFlotte> flotte() {
        return reporting.flotte();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReservationCreee reserver(Long clientId, Long vehiculeId, LocalDate du, LocalDate au) {
        TarificationService.duree(du, au);
        if (du.isBefore(aujourdhui())) {
            throw new IllegalArgumentException("La date de début doit être aujourd'hui ou ultérieure.");
        }
        Vehicule vehicule = reporting.verrouillerVehicule(vehiculeId)
                .orElseThrow(() -> new RessourceIntrouvableException(Vehicule.class, vehiculeId));
        Client client = crud.exiger(Client.class, clientId);
        if (reporting.disponibilite(vehiculeId, du, au, null) == 0) {
            throw new ConflitMetierException("Le véhicule est indisponible sur cette période.");
        }

        Reservation reservation = new Reservation(null, du, au, StatutReservation.EN_ATTENTE, client);
        reservation.setVehicule(vehicule);
        reservations.save(reservation);
        return new ReservationCreee(reservation.getIdReservation(), clientId, vehiculeId, du, au,
                reservation.getStatut());
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ContratCree signer(Long reservationId, Set<Long> options) {
        Reservation reservation = reporting.verrouillerReservation(reservationId)
                .orElseThrow(() -> new RessourceIntrouvableException(Reservation.class, reservationId));
        LocalDate signature = aujourdhui();
        if (reservation.getContrat() != null) {
            throw new ConflitMetierException("Cette réservation possède déjà un contrat.");
        }
        if (reservation.getStatut() != StatutReservation.EN_ATTENTE
                && reservation.getStatut() != StatutReservation.CONFIRMEE) {
            throw new ConflitMetierException("La réservation doit être en attente ou confirmée.");
        }
        if (reservation.getClient() == null || reservation.getVehicule() == null
                || !signature.isBefore(reservation.getDateFin())) {
            throw new ConflitMetierException("La réservation doit avoir un client, un véhicule et une période en cours ou future.");
        }
        Vehicule vehicule = reporting.verrouillerVehicule(reservation.getVehicule().getIdVehicule())
                .orElseThrow();
        if (reporting.disponibilite(vehicule.getIdVehicule(), reservation.getDateDebut(),
                reservation.getDateFin(), reservationId) == 0) {
            throw new ConflitMetierException("Le véhicule est indisponible sur cette période.");
        }
        BigDecimal montant = tarifs.calculer(vehicule, reservation.getDateDebut(), reservation.getDateFin(), options)
                .montantTotal();
        Contrat contrat = contrats.save(fabrique.creer(reservation, signature, montant));
        reservation.setStatut(StatutReservation.CONFIRMEE);
        if (!signature.isBefore(reservation.getDateDebut()) && vehicule.getStatut() == StatutVehicule.DISPONIBLE) {
            vehicule.setStatut(StatutVehicule.LOUE);
        }
        evenements.publishEvent(new ContratSigne(contrat.getIdContrat(), reservation.getDateFin()));
        return new ContratCree(contrat.getIdContrat(), reservationId, signature, montant);
    }

    public static LocalDate aujourdhui() {
        return LocalDate.now(ZoneId.of("Africa/Tunis"));
    }
}
