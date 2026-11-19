# AutoLoc

AutoLoc est le backend d'une entreprise de location de véhicules avec plusieurs agences. Il modélise la flotte, les clients, les réservations, les contrats et les paiements.

La [fiche de validation](VALIDATION.md) contient le récap des ateliers et des séances, la démonstration Swagger et les commandes SQL. La [collection Postman](AutoLoc-API.postman_collection.json) rejoue le parcours de démo.

| Acteur | Exemple de cas d'utilisation |
| --- | --- |
| Client | Consulter les véhicules et réserver pour une période. |
| Agent d'agence | Gérer les véhicules, établir un contrat et enregistrer un paiement. |
| Responsable d'agence | Gérer les employés et consulter les statistiques de son agence. |
| Administrateur | Gérer les agences et consulter les statistiques globales. |

## Démarrer le projet

1. Ouvre le dossier `autoloc-api` dans IntelliJ avec le JDK 17.
2. Démarre **MySQL** dans XAMPP. Ce bouton lance le serveur MariaDB de ton installation sur le port `3306`. Démarre aussi **Apache** pour utiliser phpMyAdmin.
3. Lance la classe `AutolocApiApplication` dans IntelliJ ou exécute cette commande PowerShell depuis `autoloc-api`.

```powershell
.\mvnw.cmd spring-boot:run
```

L'application écoute sur `8081`. Avec la configuration locale par défaut, le pilote MariaDB se connecte avec le compte `root` de XAMPP. Si ton compte a un mot de passe, fournis-le via la variable d'environnement `AUTOLOC_DB_PASSWORD` au lancement.

`pom.xml` indique à Maven quelles bibliothèques charger et comment compiler le projet. [application.properties](src/main/resources/application.properties) indique à Spring Boot comment lancer le serveur web et se connecter à la base.

## Démontrer l'Atelier 1

1. Montre la ligne `Started AutolocApiApplication` dans la console. Cela confirme le démarrage. Au premier lancement, les logs SQL affichent la création des tables.
2. Dans [phpMyAdmin](http://localhost/phpmyadmin/), ouvre la base `autoloc_db` et son onglet **SQL**. Exécute les requêtes suivantes.

```sql
SHOW TABLES;
SHOW CREATE TABLE vehicule;
```

3. Repère les neuf tables des entités de l'Atelier 1. `agence`, `client`, `contrat`, `employe`, `equipement`, `maintenance`, `paiement`, `reservation` et `vehicule`.
4. Dans la structure de `vehicule`, montre la clé primaire `id_vehicule` en `AUTO_INCREMENT`, l'immatriculation unique et le tarif journalier en `DECIMAL(10,2)`.

Le paramètre JDBC `createDatabaseIfNotExist=true` demande au pilote de créer la base si nécessaire. Ensuite, `ddl-auto=update` demande à Hibernate d'adapter les tables aux classes JPA. La présence des tables est donc la preuve à montrer pour cet atelier. Le test de démarrage se lance avec `.\mvnw.cmd test` pendant que MariaDB est actif.

Le rendu de l'Atelier 1 comprend aussi les commits de l'atelier et de la préparation de l'Atelier 2 à pousser sur le dépôt **AutoLoc** de l'équipe.

## Comprendre Vehicule

Dans [Vehicule.java](src/main/java/tn/esprit/autoloc/domain/Vehicule.java), `@Entity` déclare à JPA que la classe est persistante. `@Table` donne le nom SQL. Une instance de `Vehicule` sauvegardée correspond à une ligne de cette table.

| Dans le code | À expliquer à la validation |
| --- | --- |
| `@Id` et `@GeneratedValue(strategy = GenerationType.IDENTITY)` | `@Id` identifie la clé primaire. Avec `IDENTITY`, MariaDB attribue la valeur lors de l'insertion. |
| `@Column(unique = true)` | La base empêche deux véhicules d'avoir la même immatriculation. |
| `@Enumerated(EnumType.STRING)` | La base enregistre le nom du choix, par exemple `DISPONIBLE`. |
| `BigDecimal` et `LocalDate` | Les montants gardent une représentation décimale. Les dates de location représentent des jours du calendrier. |
| `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` | Lombok génère les accesseurs et les constructeurs à la compilation. JPA utilise le constructeur sans arguments pour instancier les entités. |

Astuce pour l'oral. Prends une Clio précise. Montre la classe `Vehicule`, puis la table `vehicule`, puis la colonne `immatriculation`. Tu viens d'expliquer le mapping objet-relationnel avec un exemple.

## Atelier 2 – Relation entre Agence et Vehicule

1. Dans [Vehicule.java](src/main/java/tn/esprit/autoloc/domain/Vehicule.java), `@ManyToOne` signifie que plusieurs véhicules peuvent avoir la même agence. Le champ `agence` porte `@JoinColumn(name = "agence_id")`. C'est lui qui pilote la clé étrangère en base.
2. Dans [Agence.java](src/main/java/tn/esprit/autoloc/domain/Agence.java), `@OneToMany(mappedBy = "agence")` permet de retrouver les véhicules de cette agence. `mappedBy` reprend le nom du champ Java `agence` dans `Vehicule`.
3. Dans phpMyAdmin, exécute `SHOW CREATE TABLE vehicule;` et repère `FOREIGN KEY (agence_id)`. La colonne référence `agence.id_agence`. Pour rattacher un véhicule en Java, renseigne le côté propriétaire avec `vehicule.setAgence(agence)` puis sauvegarde le véhicule.

La colonne accepte temporairement `NULL` pour les véhicules de l'Atelier 1 en attente d'affectation. Avec `LAZY`, Hibernate charge la relation au moment où le code en a besoin. MariaDB bloque la suppression d'une agence encore référencée.

## Atelier 2 – Relation entre Agence et Employe

1. Dans [Employe.java](src/main/java/tn/esprit/autoloc/domain/Employe.java), le champ `agence` porte `@ManyToOne` et pilote la colonne `employe.agence_id`. On le renseigne avec `employe.setAgence(agence)` avant de sauvegarder l'employé.
2. Dans [Agence.java](src/main/java/tn/esprit/autoloc/domain/Agence.java), la liste `employes` porte `@OneToMany(mappedBy = "agence")`. Comme la liste contient des employés, Hibernate cherche le champ `agence` dans la classe `Employe`.
3. Dans phpMyAdmin, `SHOW CREATE TABLE employe;` permet de vérifier que `agence_id` référence `agence.id_agence`.

Un employé de l'Atelier 1 peut garder `agence_id = NULL` en attendant son affectation. MariaDB bloque la suppression d'une agence tant que des employés ou des véhicules y sont rattachés.

## Atelier 2 – Relation entre Vehicule et Equipement

La Clio peut proposer les options GPS et siège bébé. La Polo peut proposer le même GPS. Une ligne de `vehicule_equipement` associe un véhicule à une option.

1. Dans [Vehicule.java](src/main/java/tn/esprit/autoloc/domain/Vehicule.java), `@ManyToMany` définit l'ensemble des équipements du véhicule. Sur ce côté propriétaire, `@JoinTable` déclare la table et ses deux clés étrangères. `joinColumns` décrit `vehicule_id` et `inverseJoinColumns` décrit `equipement_id`.
2. Dans [Equipement.java](src/main/java/tn/esprit/autoloc/domain/Equipement.java), `mappedBy = "equipements"` reprend le nom du champ Java `Vehicule.equipements`.
3. Dans phpMyAdmin, `SHOW CREATE TABLE vehicule_equipement;` montre les deux références et la clé primaire composée de `vehicule_id` et `equipement_id`.

Pour lier un équipement déjà enregistré, ajoute-le à `clio.getEquipements()` puis sauvegarde la Clio. Retirer le GPS de cet ensemble supprime la ligne qui relie la Clio au GPS. La Polo peut toujours proposer cette option.

## Atelier 2 – Relation entre Client et Reservation

1. Dans [Reservation.java](src/main/java/tn/esprit/autoloc/domain/Reservation.java), `@ManyToOne` relie la réservation à un client. Le champ `client` pilote la clé étrangère `reservation.client_id`.
2. Dans [Client.java](src/main/java/tn/esprit/autoloc/domain/Client.java), `@OneToMany(mappedBy = "client")` permet de retrouver les réservations du client. Sur cette liste, `cascade = CascadeType.PERSIST` propage `persist(client)` aux nouvelles réservations présentes dans la liste.
3. Pour créer un client et sa réservation en une opération, fais `reservation.setClient(client)` pour préparer la clé étrangère et `client.getReservations().add(reservation)` pour que la cascade atteigne la réservation. Appelle ensuite `entityManager.persist(client)` dans la transaction.

Dans phpMyAdmin, `SHOW CREATE TABLE reservation;` montre que `client_id` référence `client.id_client`. La colonne accepte `NULL` pour les réservations saisies à l'Atelier 1 en attente d'affectation. MariaDB protège un client encore référencé par une réservation contre la suppression.

## Atelier 2 – Réservation, contrat, paiement et maintenance

| Association | Clé étrangère | Choix de cascade |
| --- | --- | --- |
| Plusieurs réservations peuvent concerner le même véhicule. | `reservation.vehicule_id` | Le véhicule a sa propre fiche et peut servir à plusieurs réservations. |
| Un contrat concerne une seule réservation. Dès qu'il est établi, il est accessible depuis cette réservation. | `contrat.reservation_id`, avec une contrainte `UNIQUE` | Le contrat est créé à la signature, après la réservation. |
| Un contrat peut avoir plusieurs paiements, par exemple un acompte et un solde. | `paiement.contrat_id` | `CascadeType.ALL` depuis le contrat. JPA propage notamment l'enregistrement et la suppression aux paiements liés. |
| Un véhicule peut avoir plusieurs opérations de maintenance. | `maintenance.vehicule_id` | `CascadeType.PERSIST` depuis la maintenance. Enregistrer une maintenance peut aussi enregistrer un nouveau véhicule associé. |

Dans le lien réservation–contrat, `Contrat.reservation` détermine la valeur de `reservation_id`. `mappedBy = "reservation"` permet de retrouver le contrat depuis la réservation. La contrainte `UNIQUE` limite chaque réservation à un seul contrat.

Les nouveaux liens utilisent `LAZY` pour demander un chargement différé des entités associées. Les quatre nouvelles clés acceptent `NULL` pendant la reprise des lignes saisies avant leur affectation.

Pour vérifier le schéma, exécute `SHOW CREATE TABLE reservation;`, puis la même commande pour `contrat`, `paiement` et `maintenance`. Le modèle complet compte neuf tables d'entités et la table de jointure `vehicule_equipement`.

## Réviser les notions de base

**Architecture SI.** Le système d'information de l'agence comprend ses données, ses acteurs, ses procédures et ses outils. L'architecture précise les responsabilités et les échanges. Dans AutoLoc, on vise une application déployée en un seul bloc, avec un code organisé en couches.

**Persistance.** JPA définit les règles de mapping. Hibernate les applique et produit du SQL. JDBC permet à Java de communiquer avec la base au moyen d'un pilote. Spring Data JPA fournit les opérations des repositories comme `save` et `findById`. Spring Boot assemble ces composants au démarrage.

**Couches.** Le contrôleur reçoit la requête HTTP. Le service décide selon les règles métier. Le repository accède aux données. Par exemple, la règle « trois jours à 100 DT donnent 300 DT » appartient au service. Avec l'injection de dépendances, Spring fournit au service le repository déclaré dans son constructeur.

**Associations.** Pars des cardinalités du diagramme pour choisir l'annotation. `cascade` précise quelles opérations propager aux objets liés, par exemple la sauvegarde ou la suppression.

**API REST.** `GET` lit, `POST` crée, `PUT` remplace et `DELETE` supprime une ressource. Dans une API, `201` confirme une création et `404` signale une ressource absente. SpringDoc produit le document OpenAPI. Swagger UI permet de consulter ce document et d'envoyer les requêtes.

### Questions rapides de validation

- **Que vérifie `compile` ?** Que Maven peut produire les classes Java. Pour prouver la connexion SQL, démarre Spring Boot et inspecte les tables.
- **Où se met la règle « date de fin après la date de début » ?** La couche métier protège la création de la réservation. La base stocke les deux dates.
- **Comment retrouver le côté propriétaire dans un exercice JPA ?** Pour une relation 1–N comme agence–véhicule, cherche la table qui reçoit la clé étrangère. Ici, il s'agit de la table `vehicule`.

Les routes `/api` exposent le CRUD des neuf entités. Les routes `/api/metier` appliquent les règles de disponibilité, de tarification, de signature et de clôture.
