package tn.esprit.autoloc.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.autoloc.domain.Client;
import tn.esprit.autoloc.domain.Reservation;
import tn.esprit.autoloc.service.CrudService;
import tn.esprit.autoloc.web.dto.ClientDto;
import tn.esprit.autoloc.web.dto.ClientDto.SaisieClient;

@Tag(name = "Clients")
@RestController
@RequestMapping("/api/clients")
public class ClientController extends CrudController<Client, SaisieClient, ClientDto> {

    public ClientController(CrudService crud) {
        super(crud, Client.class, Client::new, ClientDto::idClient);
    }

    @Override
    protected void appliquer(Client client, SaisieClient saisie) {
        client.setNom(saisie.nom());
        client.setPrenom(saisie.prenom());
        client.setEmail(saisie.email());
        client.setTelephone(saisie.telephone());
        client.setNumPermis(saisie.numPermis());
        client.setDateInscription(saisie.dateInscription());
    }

    @Override
    protected ClientDto reponse(Client client) {
        return new ClientDto(client.getIdClient(), client.getNom(), client.getPrenom(), client.getEmail(),
                client.getTelephone(), client.getNumPermis(), client.getDateInscription(),
                idsDe(client.getReservations(), Reservation::getIdReservation));
    }
}
