# Scripts — API Lanely

Scripts utilitaires pour le développement.

## `seed` — données de base après un reset de la BDD

Crée un jeu de données de départ **via l'API HTTP** (et non en SQL brut), pour que toutes
les règles métier s'appliquent (hachage BCrypt des mots de passe, génération du code
société, sessions, permissions...).

Ce qui est créé :
- un **compte owner** web (tous les droits) ;
- une **société** (l'owner en devient le gérant) avec son **code public** ;
- un **membre** web, invité puis rattaché, avec une permission (`MANAGE_PROFILES` par défaut) ;
- deux **profils livreurs** pour l'app mobile.

À la fin, un **récapitulatif** affiche tous les identifiants (emails/mots de passe, code
société, profils mobiles).

### Pré-requis

L'API doit **tourner**. En dev, Hibernate (re)crée le schéma au démarrage (`ddl-auto=update`),
donc le flux après un reset est :

```
1. (re)créer / vider la base PostgreSQL
2. démarrer l'app :   mvn spring-boot:run
3. lancer le seed (voir ci-dessous)
```

### Utilisation

PowerShell (Windows, natif) :

```powershell
./scripts/seed.ps1
# ou contre un autre port / hôte :
./scripts/seed.ps1 -BaseUrl http://localhost:8090
```

Bash (Git Bash / WSL / Linux / macOS) :

```bash
./scripts/seed.sh
# ou :
LANELY_BASE_URL=http://localhost:8090 ./scripts/seed.sh
```

> Par défaut l'URL est `http://localhost:8080`. Variable d'env : `LANELY_BASE_URL`.

### Personnalisation

Les valeurs (emails, mots de passe, nom de société, permissions du membre, liste des
profils) sont déclarées **en haut de chaque script** — modifie-les directement.

## `config.ps1` — token et société partagés

Les scripts qui appellent l'API **authentifiée** (`seed-clients.ps1`, `seed-vehicles.ps1`,
`seed-ldv.ps1`) lisent le **token d'accès**, l'**identifiant de société**, l'URL de base et la langue depuis
un fichier unique : `scripts/config.ps1`. Édite-le une seule fois :

```powershell
$LanelyToken     = "<jeton JWT frais>"
$LanelyCompanyId = "<id de la société>"
$LanelyBaseUrl   = "http://localhost:8080"
$LanelyLang      = "fr"
```

> Le token d'accès est de courte durée (~15 min) : remplace `$LanelyToken` par un jeton frais
> d'un compte web membre de la société, avec les permissions requises (`MANAGE_CLIENTS`,
> `MANAGE_VEHICLES`… ou OWNER). Les paramètres `-BaseUrl` / `-Lang` passés en ligne de commande
> ont la priorité sur `config.ps1`.

## `seed-vehicles.ps1` — véhicules de test

Crée une dizaine de véhicules variés (camions, utilitaires, semi-remorques…) avec leurs
informations (immatriculation, VIN, assurance, dates de carte grise/contrôle technique…) et
ajoute pour chacun un **relevé kilométrique** initial. Nécessite la permission `MANAGE_VEHICLES`.

```powershell
./scripts/seed-vehicles.ps1
# ou un autre nombre / hôte :
./scripts/seed-vehicles.ps1 -Count 25 -BaseUrl http://localhost:8090
```

## `seed-ldv.ps1` — lettres de voiture de test

Crée des **lettres de voiture (waybills)** en s'appuyant sur les **clients existants** de la
société. Le script liste d'abord les clients actifs et leurs adresses, ne garde que ceux qui
ont une **adresse géolocalisée**, puis crée des waybills reliant deux clients distincts :

- **donneur d'ordre + expéditeur (shipper)** = client A (son adresse principale géolocalisée) ;
- **destinataire (consignee)** = client B (son adresse principale géolocalisée).

Les lieux de prise en charge / livraison sont liés à ces clients/adresses, donc l'API en dérive
les coordonnées et calcule l'itinéraire elle-même. Chaque waybill est créé en `DRAFT` puis
amené à un statut réaliste (`ISSUED`, `COLLECTED`, `IN_TRANSIT`, `DELIVERED`, `FAILED`) pour un
jeu de données varié ; une partie reste en `DRAFT`. Les dates de prise en charge sont réparties
autour d'aujourd'hui (passé pour les livrés/échoués, futur pour les émis/brouillons). Nécessite
la permission `MANAGE_TRANSPORTS`.

```powershell
./scripts/seed-ldv.ps1
# ou un autre nombre / hôte :
./scripts/seed-ldv.ps1 -Count 50 -BaseUrl http://localhost:8090
```

> Pré-requis : avoir déjà des clients géolocalisés (lancer `seed-clients.ps1` au préalable).

### Ré-exécution

Les scripts sont **tolérants à la ré-exécution** : un compte déjà créé est détecté
(409 → login), un membre déjà rattaché est ignoré, un profil déjà présent est sauté. Chaque
appel `./scripts/seed.ps1` crée toutefois **une nouvelle société** (le nom n'est pas unique).

### Note importante

Le rattachement automatique du membre nécessite que le **code d'invitation soit renvoyé
dans la réponse**, ce qui n'est le cas qu'en environnement de dev
(`app.mail.expose-code-in-response=true`). Hors dev, le seed crée les comptes mais n'auto-
accepte pas l'invitation (le lien serait normalement envoyé par email).

## `sql/` — migrations SQL manuelles

Hibernate tourne en `ddl-auto=update` : il **ajoute** colonnes/tables mais ne **renomme**
ni ne **supprime** jamais. Les changements de schéma destructifs ou les renommages sont donc
fournis sous forme de scripts SQL datés à exécuter **une fois par environnement**, dans
l'ordre chronologique.

- `2026-06-25-remove-waybill-carrier.sql` — supprime les parties `CARRIER` orphelines.
- `2026-06-26-rename-assigned-profile-to-account.sql` — renomme `assigned_profile_id` →
  `assigned_account_id` (FK vers `accounts`) sur `tours` et `waybills`, pour permettre
  d'assigner une tournée / lettre de voiture à un **compte web** (`User`) en plus d'un
  livreur mobile (`Profile`). **À exécuter AVANT de déployer le nouveau build** : si la
  nouvelle app démarre d'abord, Hibernate créerait une colonne `assigned_account_id` vide
  et laisserait l'ancienne colonne (avec ses données) orpheline. Migration sans perte (les
  valeurs existantes sont déjà des `accounts.id`).
