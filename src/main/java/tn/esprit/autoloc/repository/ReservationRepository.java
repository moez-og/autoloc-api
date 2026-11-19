package tn.esprit.autoloc.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.autoloc.domain.Reservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClient_IdClientOrderByDateDebutDescIdReservationDesc(Long clientId, Pageable page);
}
