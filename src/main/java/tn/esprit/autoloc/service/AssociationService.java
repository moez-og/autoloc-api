package tn.esprit.autoloc.service;

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
import tn.esprit.autoloc.repository.ContratRepository;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
@Transactional
public class AssociationService {

    private final CrudService crud;
    private final ContratRepository contrats;

    public AssociationService(CrudService crud, ContratRepository contrats) {
        this.crud = crud;
        this.contrats = contrats;
    }

    public void affecter(String association, Long id, Long cibleId) {
        modifier(association, id, cibleId, true);
    }

    public void desaffecter(String association, Long id, Long cibleId) {
        modifier(association, id, cibleId, false);
    }

    private void modifier(String association, Long id, Long cibleId, boolean affecter) {
        switch (association) {
            case "vehicule-agence" -> modifierParent(
                    crud.exiger(Vehicule.class, id), crud.exiger(Agence.class, cibleId), affecter,
                    Vehicule::getAgence, Vehicule::setAgence, Agence::getIdAgence, Agence::getVehicules);
            case "employe-agence" -> modifierParent(
                    crud.exiger(Employe.class, id), crud.exiger(Agence.class, cibleId), affecter,
                    Employe::getAgence, Employe::setAgence, Agence::getIdAgence, Agence::getEmployes);
            case "reservation-client" -> modifierParent(
                    crud.exiger(Reservation.class, id), crud.exiger(Client.class, cibleId), affecter,
                    Reservation::getClient, Reservation::setClient, Client::getIdClient, Client::getReservations);
            case "reservation-vehicule" -> modifierParent(
                    crud.exiger(Reservation.class, id), crud.exiger(Vehicule.class, cibleId), affecter,
                    Reservation::getVehicule, Reservation::setVehicule, Vehicule::getIdVehicule, null);
            case "paiement-contrat" -> modifierParent(
                    crud.exiger(Paiement.class, id), crud.exiger(Contrat.class, cibleId), affecter,
                    Paiement::getContrat, Paiement::setContrat, Contrat::getIdContrat, Contrat::getPaiements);
            case "maintenance-vehicule" -> modifierParent(
                    crud.exiger(Maintenance.class, id), crud.exiger(Vehicule.class, cibleId), affecter,
                    Maintenance::getVehicule, Maintenance::setVehicule, Vehicule::getIdVehicule, null);
            case "vehicule-equipement" -> modifierEquipement(
                    crud.exiger(Vehicule.class, id), crud.exiger(Equipement.class, cibleId), affecter);
            case "contrat-reservation" -> modifierReservation(
                    crud.exiger(Contrat.class, id), crud.exiger(Reservation.class, cibleId), affecter);
            default -> throw new IllegalArgumentException("Association inconnue.");
        }
    }

    private <E, P> void modifierParent(
            E entite, P cible, boolean affecter, Function<E, P> lireParent, BiConsumer<E, P> definirParent,
            Function<P, Long> lireId, Function<P, List<E>> lireEnfants) {
        P ancienParent = lireParent.apply(entite);
        boolean dejaAffecte = ancienParent != null && Objects.equals(lireId.apply(ancienParent), lireId.apply(cible));
        if (dejaAffecte == affecter) {
            return;
        }

        if (ancienParent != null && lireEnfants != null) {
            lireEnfants.apply(ancienParent).remove(entite);
        }
        definirParent.accept(entite, affecter ? cible : null);
        if (affecter && lireEnfants != null) {
            List<E> enfants = lireEnfants.apply(cible);
            if (!enfants.contains(entite)) {
                enfants.add(entite);
            }
        }
    }

    private void modifierEquipement(Vehicule vehicule, Equipement equipement, boolean affecter) {
        if (affecter) {
            vehicule.getEquipements().add(equipement);
            equipement.getVehicules().add(vehicule);
        } else {
            vehicule.getEquipements().remove(equipement);
            equipement.getVehicules().remove(vehicule);
        }
    }

    private void modifierReservation(Contrat contrat, Reservation reservation, boolean affecter) {
        Reservation ancienneReservation = contrat.getReservation();
        boolean dejaAffecte = ancienneReservation != null
                && Objects.equals(ancienneReservation.getIdReservation(), reservation.getIdReservation());
        if (dejaAffecte == affecter) {
            return;
        }

        Contrat contratActuel = reservation.getContrat();
        if (affecter && contratActuel != null && !Objects.equals(contratActuel.getIdContrat(), contrat.getIdContrat())) {
            throw new ConflitMetierException("La réservation est déjà liée à un contrat.");
        }
        if (ancienneReservation != null) {
            ancienneReservation.setContrat(null);
        }
        contrat.setReservation(affecter ? reservation : null);
        if (affecter) {
            reservation.setContrat(contrat);
        }
        contrats.flush();
    }
}
