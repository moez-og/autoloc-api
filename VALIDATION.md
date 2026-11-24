# AutoLoc — Fiche de validation ASI

AutoLoc gère une flotte de location multi-agences. Une agence gère ses véhicules et ses employés, un client réserve un véhicule, puis la réservation peut donner lieu à un contrat et à des paiements.

## Démonstration en cinq minutes

1. Dans XAMPP, démarre **MySQL** pour MariaDB et **Apache** si tu utilises phpMyAdmin. Dans PowerShell, ouvert dans `autoloc-api`, lance `.\mvnw.cmd spring-boot:run`. Dans IntelliJ, tu peux aussi lancer `AutolocApiApplication`. La console indique le port HTTP, `8081` par défaut.
2. Dans phpMyAdmin, sélectionne `autoloc_db`. Montre la table `vehicule`, le lien `reservation.client_id`, la contrainte unique de `contrat.reservation_id` et les deux clés de `vehicule_equipement`.

```sql
SHOW TABLES;
SHOW CREATE TABLE reservation;
SHOW CREATE TABLE contrat;
SHOW CREATE TABLE vehicule_equipement;
```

3. Ouvre Swagger UI sur [localhost:8081](http://localhost:8081/swagger-ui/index.html). Le document OpenAPI se trouve à [`/v3/api-docs`](http://localhost:8081/v3/api-docs). Adapte le port si ta console en indique un autre.
4. Dans Swagger, crée une agence avec `POST /api/agences`, puis un véhicule avec `POST /api/vehicules`. Reprends l'identifiant d'agence retourné dans le champ `agenceId` du véhicule. Fais ensuite `GET /api/vehicules/{id}` avec l'identifiant du véhicule pour montrer que l'affectation a été enregistrée.

Si le port `8081` est occupé, lance le JAR compilé sur `8082` avec `java -jar target\autoloc-api-0.0.1-SNAPSHOT.jar --server.port=8082` et ouvre [Swagger sur le port 8082](http://localhost:8082/swagger-ui/index.html).

Dans Postman, importe [AutoLoc-API.postman_collection.json](AutoLoc-API.postman_collection.json). La variable `baseUrl` est réglée sur `http://localhost:8082` pour la démonstration. Adapte-la au port affiché par ta console, puis utilise **Run collection** pour enchaîner les onze requêtes. La collection crée ses propres données de démo et transmet leurs identifiants aux appels suivants.

Agence de démonstration.

```json
{
  "nom": "AutoLoc Tunis",
  "ville": "Tunis",
  "adresse": "Avenue de la Liberté",
  "telephone": "71123456"
}
```

Véhicule de démonstration. Ici, `agenceId` vaut `1` dans l'exemple. Utilise une immatriculation disponible dans ta base.

```json
{
  "immatriculation": "269 TU 4815",
  "marque": "Renault",
  "modele": "Clio",
  "categorie": "CITADINE",
  "tarifJournalier": 60.00,
  "statut": "DISPONIBLE",
  "agenceId": 1
}
```

## Démonstration du parcours métier

1. Appelle `GET /api/metier/vehicules-disponibles?du=2026-12-04&au=2026-12-07&ville=Tunis&page=0&taille=10` pour retrouver les véhicules disponibles sur une période paginée.
2. Envoie un devis avec `POST /api/metier/devis` et le JSON ci-dessous. Remplace `vehiculeId` par l'identifiant retourné lors de la création de ta Clio.
3. Crée un client dans `/api/clients`, puis appelle `POST /api/metier/reservations` avec `clientId`, `vehiculeId`, `dateDebut` et `dateFin`. Signe avec `POST /api/metier/reservations/{id}/signer` et un corps `{"equipementIds": []}` pour une réservation sans option. Le contrat prend le prix calculé par le service métier.
4. Pour retrouver les réservations de Lina, appelle `GET /api/metier/clients/{id}/reservations?page=0&taille=10`. Pour les statistiques, montre `GET /api/metier/statistiques/flotte` et `GET /api/metier/statistiques/recettes` dans Swagger. Le total encaissé prend en compte les paiements saisis via `POST /api/paiements`.

```json
{
  "vehiculeId": 1,
  "dateDebut": "2026-12-04",
  "dateFin": "2026-12-07",
  "equipementIds": []
}
```

À 60 DT par jour et sans option, le devis est de **204 DT** pour le vendredi, le samedi et le dimanche. Les jours de week-end coûtent 20 % de plus. La date de fin est exclue du calcul. Chaque option choisie ajoute 5 DT par jour.

Exemple de client pour `POST /api/clients`.

```json
{
  "nom": "Ben Salem",
  "prenom": "Lina",
  "email": "lina.demo@example.com",
  "telephone": "22123456",
  "numPermis": "P-DEMO-ASI",
  "dateInscription": "2026-11-19"
}
```

Pour montrer l'affectation transactionnelle, essaie `PUT /api/associations/vehicule-agence/{vehiculeId}/{agenceId}` sur les identifiants créés en démo. `DELETE` sur le même chemin désaffecte ce couple. Les deux requêtes renvoient `204` en cas de succès.

Pour expliquer la clôture, montre la tâche planifiée et la route `POST /api/metier/clotures`. Le bilan contient les identifiants des contrats qui arrivent à échéance. L'appel HTTP exige le jeton de responsable configuré au démarrage avec `AUTOLOC_MANAGER_TOKEN` et transmis en `Authorization: Bearer ...`.

## Démonstration Spring AOP — Séance 13

1. Démarre la version à jour dans IntelliJ et ouvre Swagger. Dans **Véhicules**, exécute `GET /api/vehicules/{id}` avec l'identifiant d'une voiture existante. Montre le `200` puis la console **Run** d'IntelliJ. Tu verras `Début méthode CrudService.obtenir(..)`, le log de fin et la durée en millisecondes.
2. Répète avec un identifiant absent. Montre le `404` puis le log `Échec de CrudService.obtenir(..) (RessourceIntrouvableException)`. Le service transmet toujours son exception au gestionnaire HTTP.
3. Pour montrer `@AfterReturning`, ajoute `--logging.level.tn.esprit.autoloc.aspect.LoggingAspect=DEBUG` aux arguments de lancement. Le log `Retour normal` apparaît après l'appel réussi. Un appel de plus de 1 000 ms affiche un `WARN`.

Le **pointcut** sélectionne les méthodes publiques des beans `@Service` du package `service` et de ses sous-packages. Le **join point** est une exécution concrète, par exemple celle de `CrudService.obtenir(..)`. Les **advices** indiquent quand agir. `@Before` journalise l'entrée, `@After` la sortie dans les deux cas, `@AfterReturning` le retour normal, `@AfterThrowing` le type de l'exception et `@Around` mesure la durée autour de `proceed()`. Les logs affichent les paramètres simples utiles à la démo. Le texte libre et les objets sont masqués.

Spring crée le **proxy** au démarrage. Un appel au service depuis le contrôleur traverse ce proxy. Un appel interne à la même instance, par exemple `obtenir()` qui appelle `exiger()`, contourne le proxy. À l'oral, explique que le proxy permet d'ajouter la journalisation à plusieurs méthodes en un seul endroit.

## Récap des ateliers et des séances

Le guide nomme **Atelier 0** l'installation. L'**Atelier 1** correspond à la séance 2 et l'**Atelier 2** à la séance 3. Les sujets suivants sont repérés par leurs séances dans le plan du module.

| Repère | Ce qu'il faut expliquer simplement |
| --- | --- |
| Atelier 0 — Installation | Java 17 exécute l'application, Maven gère la compilation et les dépendances, MariaDB stocke les données et Postman ou Swagger sert à tester l'API. Le guide demande aussi le dépôt Git AutoLoc avec son README et une preuve de démarrage. |
| Séance 1 — Architecture du SI | AutoLoc est un monolithe organisé en couches. Le contrôleur reçoit HTTP, le service applique les règles métier, le repository accède aux données et le domaine représente les objets métier. |
| Atelier 1 · séance 2 — Entités | Une classe `@Entity` correspond à une table, `@Id` à la clé primaire et `@GeneratedValue(strategy = GenerationType.IDENTITY)` à l'identifiant généré en base. Les neuf entités du diagramme ont chacune une table. Un `enum` fixe les valeurs autorisées, par exemple `ModePaiement.CARTE`. Avec `EnumType.STRING`, la base conserve le nom de la valeur. |
| Atelier 2 · séance 3 — Associations | L'annotation vient des cardinalités UML. La clé étrangère est côté `@ManyToOne` dans une relation 1–N. `mappedBy` reprend le nom du champ Java propriétaire. Une relation N–N utilise une table de jointure. Une relation 1–1 exige une clé étrangère unique. |
| Séance 4 — Patrons GoF | **Strategy** permet à `TarificationService` d'appliquer `TarifSemaine` ou `TarifWeekend`. **Factory** centralise la création dans `FabriqueContrat`. **Observer** permet à `JournalLocations` de réagir à l'événement `ContratSigne`. Pour expliquer **Singleton** dans Spring, montre la portée par défaut de `TarificationService`, avec une instance du bean par conteneur. |
| Séances 5–6 — Repository, Service, IoC/DI et SOLID | Une interface `JpaRepository<Vehicule, Long>` fournit `save`, `findById`, `findAll` et `deleteById`. Spring crée le repository et l'injecte au service par le constructeur. Dans AutoLoc, chaque couche a une responsabilité, et le calcul du prix peut évoluer derrière un contrat commun. |
| Séance 7 — Spring MVC et SpringDoc | Spring MVC relie un verbe HTTP et un chemin à une méthode de contrôleur. SpringDoc génère OpenAPI et Swagger UI permet d'essayer les requêtes. Le TP SpringDoc demande le CRUD pour les neuf entités du diagramme AutoLoc. |
| Séance 8 — Affectations et transactions | Affecter un véhicule à une agence modifie le côté propriétaire `vehicule.agence`. La collection d'agence reflète ce lien. `@Transactional` regroupe les écritures liées dans une même transaction. Une opération qui échoue peut être annulée en bloc. |
| Séance 9 — DTO, validation et réorganisation du code | Un DTO fixe les champs du JSON, par exemple les identifiants d'associations. `@Valid` déclenche les contraintes de la requête. Pour la démo, un tarif journalier négatif, une réservation dont la fin précède le début ou un champ obligatoire manquant donnent `400`. |
| Séance 10 — Requêtes dérivées, JPQL et statistiques | Dans `ReservationRepository`, `findByClient_IdClientOrderByDateDebutDescIdReservationDesc` suit le lien `client.idClient`, puis trie les réservations. JPQL interroge les entités et leurs attributs. Pour deux réservations en intervalles `[du, au[`, un chevauchement vérifie `dateDebut < au` et `dateFin > du`. Pagination et agrégations limitent les résultats et calculent les indicateurs. |
| Séance 11 — Spring Scheduler | `ContratEcheanceScheduler` lance la clôture chaque jour à 1 h, heure de Tunis, par défaut. Une réservation confirmée devient terminée lorsque sa date de fin est atteinte. Le bilan compte aussi les véhicules libérés, les locations activées et les contrats qui se terminent dans les trois prochains jours. |
| Spring AOP — support Séance 13 | `LoggingAspect` journalise les appels, `PerformanceAspect` mesure leur durée et `ExceptionHandlingAspect` signale les exceptions. `ControleRoleAspect` vérifie le jeton requis avant la route de clôture. |
| Qualité logicielle | Les tests donnent une preuve sur des comportements identifiés. La couverture indique quelles lignes ou branches s'exécutent pendant les tests. Les métriques de complexité et la dette technique servent à choisir les améliorations prioritaires. SonarQube est indiqué comme optionnel dans l'étude de cas. |
| Séance 14 — Révision pratique | Rejoue le fil `requête HTTP → contrôleur → service → repository → Hibernate → MariaDB`. Prépare un exemple 1–N, un exemple N–N et un exemple 1–1, puis une création suivie d'une lecture dans Swagger. |

## Associations AutoLoc à retrouver à l'oral

| Type | Exemple | Preuve en base |
| --- | --- | --- |
| 1–N | Agence–Véhicule et Agence–Employé | `vehicule.agence_id` et `employe.agence_id` pointent vers `agence.id_agence`. |
| 1–N | Client–Réservation et Véhicule–Réservation | La ligne de réservation porte `client_id` et `vehicule_id`. |
| N–N | Véhicule–Équipement | `vehicule_equipement` contient une ligne par couple, par exemple Clio–GPS et Polo–GPS. |
| 1–1 | Réservation–Contrat | `contrat.reservation_id` est une clé étrangère unique. |
| 1–N | Contrat–Paiement et Véhicule–Maintenance | `paiement.contrat_id` et `maintenance.vehicule_id` pointent vers leurs entités de référence. |

**LAZY et cascade.** `LAZY` demande le chargement différé des objets liés. `PERSIST` propage la création. Il est utilisé de Client vers Réservation et de Maintenance vers Véhicule. `ALL` inclut aussi `REMOVE`. Dans le lien Contrat–Paiement, une suppression de contrat via JPA entraîne la suppression de ses paiements. Le losange noir UML côté Contrat représente cette composition.

## CRUD et réponses HTTP

Les chemins de base sont `/api/agences`, `/api/vehicules`, `/api/clients`, `/api/employes`, `/api/equipements`, `/api/reservations`, `/api/contrats`, `/api/paiements` et `/api/maintenances`.

Pour chaque ressource, `GET` liste ou lit par identifiant, `POST` crée, `PUT` modifie et `DELETE` supprime. À l'oral, associe un code HTTP à un cas concret. `200` confirme une lecture, `201` une création, `204` une suppression ou une écriture sans corps, `400` une requête invalide, `404` un identifiant inconnu et `409` un conflit de données.

## Preuves à montrer

- Le démarrage Spring Boot et le port affiché dans la console.
- La table de jointure, une clé étrangère et l'unicité de `contrat.reservation_id` dans MariaDB.
- Une écriture et une lecture dans Swagger.
- Le résultat de `.\mvnw.cmd test` dans la console Maven.
- Le commit local avec `git log -1 --oneline`. La remise sur GitHub demande l'URL du dépôt d'équipe.

Le rapport Surefire dans `target/surefire-reports` recense les tests Maven du modèle JPA, des cascades et des aspects. Le scénario sur MariaDB a vérifié 105 réponses HTTP, les 45 opérations CRUD dans OpenAPI, la disponibilité, la tarification et le contrôle d'accès à la clôture.

Pour la séance qualité, les tests locaux couvrent des comportements observables. Un point à mesurer lors d'un audit plus poussé est le nombre de requêtes SQL lors d'une liste qui affiche des associations. Le projet utilise une porte de démonstration par jeton partagé pour la clôture. Une gestion d'identités et de rôles par compte serait une évolution distincte.

## Supports de référence

- `Atelier0_Guide de mise en place de l'environnement.pdf`, pages 1 et 4–5.
- `Atelier 1 - Spring Boot & 1ère Entité.pdf`, pages 7–8.
- `Etude de cas_AutoLoc_ASI.pdf`, pages 1–2 pour le plan, 4–5 pour le modèle et 6–8 pour l'architecture.
- `Séance 13 - Spring AOP.pdf`, diapositives 13–27 pour les concepts et 28–31 pour les trois TP.
- Chapitre 1, diapo 2, ainsi que chapitres 5 et 6. Le chapitre Spring MVC illustre le CRUD avec le projet pédagogique `gestion-projets`. La diapo 13 de SpringDoc demande d'appliquer le CRUD aux entités de l'étude de cas.
