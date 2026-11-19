package tn.esprit.autoloc.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Employe;
import tn.esprit.autoloc.domain.Equipement;
import tn.esprit.autoloc.domain.Maintenance;
import tn.esprit.autoloc.domain.Paiement;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.repository.AgenceRepository;
import tn.esprit.autoloc.repository.ClientRepository;
import tn.esprit.autoloc.repository.ContratRepository;
import tn.esprit.autoloc.repository.EmployeRepository;
import tn.esprit.autoloc.repository.EquipementRepository;
import tn.esprit.autoloc.repository.MaintenanceRepository;
import tn.esprit.autoloc.repository.PaiementRepository;
import tn.esprit.autoloc.repository.ReservationRepository;
import tn.esprit.autoloc.repository.VehiculeRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@Transactional(readOnly = true)
public class CrudService {

    private final Map<Class<?>, JpaRepository<?, Long>> repositories;

    public CrudService(
            AgenceRepository agences, ClientRepository clients, ContratRepository contrats,
            EmployeRepository employes, EquipementRepository equipements, MaintenanceRepository maintenances,
            PaiementRepository paiements, ReservationRepository reservations, VehiculeRepository vehicules) {
        this.repositories = Map.of(
                Agence.class, agences,
                Client.class, clients,
                Contrat.class, contrats,
                Employe.class, employes,
                Equipement.class, equipements,
                Maintenance.class, maintenances,
                Paiement.class, paiements,
                Reservation.class, reservations,
                Vehicule.class, vehicules);
    }

    public <E, D> List<D> lister(Class<E> type, Function<E, D> mapper) {
        return repository(type).findAll().stream().map(mapper).toList();
    }

    public <E, D> D obtenir(Class<E> type, Long id, Function<E, D> mapper) {
        return mapper.apply(exiger(type, id));
    }

    @Transactional
    public <E, D> D creer(Class<E> type, Supplier<E> constructeur, Function<E, D> mapper) {
        E entite = constructeur.get();
        valider(entite);
        return mapper.apply(repository(type).saveAndFlush(entite));
    }

    @Transactional
    public <E, D> D modifier(Class<E> type, Long id, Consumer<E> modification, Function<E, D> mapper) {
        E entite = exiger(type, id);
        modification.accept(entite);
        valider(entite);
        repository(type).flush();
        return mapper.apply(entite);
    }

    @Transactional
    public <E> void supprimer(Class<E> type, Long id) {
        E entite = exiger(type, id);
        detacherAvantSuppression(entite);
        JpaRepository<E, Long> repository = repository(type);
        repository.delete(entite);
        repository.flush();
    }

    public <E> E exiger(Class<E> type, Long id) {
        return repository(type).findById(id).orElseThrow(() -> new RessourceIntrouvableException(type, id));
    }

    private void detacherAvantSuppression(Object entite) {
        if (entite instanceof Contrat contrat && contrat.getReservation() != null) {
            contrat.getReservation().setContrat(null);
        } else if (entite instanceof Paiement paiement && paiement.getContrat() != null) {
            paiement.getContrat().getPaiements().remove(paiement);
        } else if (entite instanceof Reservation reservation && reservation.getClient() != null) {
            reservation.getClient().getReservations().remove(reservation);
        }
    }

    private void valider(Object entite) {
        if (entite instanceof Reservation reservation
                && reservation.getDateDebut() != null
                && reservation.getDateFin() != null
                && !reservation.getDateFin().isAfter(reservation.getDateDebut())) {
            throw new IllegalArgumentException("La fin de la réservation doit suivre son début.");
        }
    }

    @SuppressWarnings("unchecked")
    private <E> JpaRepository<E, Long> repository(Class<E> type) {
        JpaRepository<?, Long> repository = repositories.get(type);
        if (repository == null) {
            throw new IllegalArgumentException("Type d'entité inconnu.");
        }
        return (JpaRepository<E, Long>) repository;
    }
}
