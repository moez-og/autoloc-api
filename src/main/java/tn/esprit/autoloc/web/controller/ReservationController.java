package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Contrat;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.domain.Vehicule;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.ReservationDto;
import tn.esprit.autoloc.web.dto.ReservationDto.SaisieReservation;

@Tag(name = "Réservations")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController extends CrudController<Reservation, SaisieReservation, ReservationDto> {

    public ReservationController(CrudService crud) {
        super(crud, Reservation.class, Reservation::new, ReservationDto::idReservation);
    }

    @Override
    protected void appliquer(Reservation reservation, SaisieReservation saisie) {
        reservation.setDateDebut(saisie.dateDebut());
        reservation.setDateFin(saisie.dateFin());
        reservation.setStatut(saisie.statut());
        affecterSiFourni(Client.class, saisie.clientId(), reservation::setClient);
        affecterSiFourni(Vehicule.class, saisie.vehiculeId(), reservation::setVehicule);
    }

    @Override
    protected ReservationDto reponse(Reservation reservation) {
        return new ReservationDto(reservation.getIdReservation(), reservation.getDateDebut(), reservation.getDateFin(),
                reservation.getStatut(), idDe(reservation.getClient(), Client::getIdClient),
                idDe(reservation.getVehicule(), Vehicule::getIdVehicule),
                idDe(reservation.getContrat(), Contrat::getIdContrat));
    }
}
