# CLAUDE.md — API Lanely

API Spring Boot 3 / Java 17, Hibernate + PostgreSQL.

## Règles

- Ne commente jamais le code, sauf indication contraire explicite.
- Le **code**, les **logs**, la **doc OpenAPI** et les **identifiants techniques** (clés JSON, noms de champs, énumérations…) sont **toujours en anglais**.
- En revanche, **tout texte destiné à l'utilisateur final** (messages d'erreur, messages de validation, sujets/corps d'emails) est **internationalisé (i18n) `en`/`fr`** via le header `Accept-Language`. Voir la section « Internationalisation (i18n) ». On n'écrit JAMAIS un texte utilisateur en dur : il passe par une **clé de message** présente dans `messages.properties` (en) **et** `messages_fr.properties` (fr).

## Stack

- Spring Boot 3.4 · Java 17 · Maven
- Spring Web · Spring Data JPA (Hibernate) · Validation · PostgreSQL

## Clients (front)

Cette API sert deux clients :

- **Application mobile** en **Flutter** (les `Profile` livreurs s'y connectent via le code société + username/mot de passe).
- **Site web** en **React** (les `User` web s'y connectent via email/mot de passe ou Google).

L'API est purement REST/stateless (JWT) : pas de rendu serveur ni de session serveur. Les deux clients consomment les mêmes endpoints. Penser à ces deux consommateurs pour le CORS, les formats d'échange et l'auth.

## Environnements

4 profils, 4 bases différentes. Config commune dans `application.properties`, spécifique dans `application-<env>.properties`.

- **dev** → `mvn spring-boot:run` (profil dev forcé)
- **prod / beta / demo** → `java -jar target/api-lanely.jar --spring.profiles.active=<env>`

## Architecture (par couches)

```
com.lanely.api
├── controller   # endpoints REST, fin (parse requête + délègue)
├── service      # logique métier, @Transactional, lève les exceptions métier
├── repository   # interfaces JpaRepository
├── entity       # entités JPA (@Entity)
├── dto          # objets d'échange API (jamais exposer une entity)
├── mapper       # entity <-> dto
├── exception    # exceptions métier + @RestControllerAdvice global
└── config       # configuration
```

## Conventions clés

- **Chemins de routes SANS préfixe `/api`.** L'API est servie sous le sous-domaine `api.` (ex. `api.lanely.fr`), donc le préfixe serait redondant. Les `@RequestMapping`/`@GetMapping`/... commencent directement par la ressource : `/auth/login`, `/companies/{id}/profiles`, `/invitations/accept`... Penser aussi aux matchers de `SecurityConfig` et aux scripts (`scripts/seed.*`).
- **Injection par constructeur** (champs `final`), pas de `@Autowired` sur champ.
- **DTO** en entrée/sortie d'API ; ne jamais sérialiser une entity directement.
- **Validation** sur les DTO (`@Valid` + `@NotNull`, `@Size`, `@Email`...).
- **Exceptions** : métier dans le `service`, gérées globalement via `@RestControllerAdvice` → réponses d'erreur cohérentes.
- **Transactions** : `@Transactional` sur le service ; `@Transactional(readOnly = true)` en lecture.
- **Pagination** (`Pageable`) sur les listes.
- **ddl-auto** : `update` en dev, `validate` ailleurs (jamais `create`/`create-drop` hors test).
- Secrets via variables d'env (`DB_URL`, `DB_USER`, `DB_PASSWORD`), jamais en dur committé.

## Dates & heures — format global (RÈGLE ABSOLUE)

L'API sert le monde entier : **toute date/heure est un instant absolu en UTC, sérialisé en ISO-8601 avec le `Z` final**, ex. `2026-06-10T09:15:30Z`. Jamais d'heure « locale » ambiguë, jamais d'epoch millis. C'est le seul format échangé, dans les deux sens.

- **Type Java = `java.time.Instant`** pour tout horodatage (création, expiration, "last used"...). Ne JAMAIS utiliser `LocalDateTime`, `java.util.Date`, ni `Timestamp` pour un instant. `Instant` n'a pas de fuseau → aucune ambiguïté.
  - Cas particulier d'une **date civile sans heure** (anniversaire, échéance facturée au jour) : utiliser `LocalDate` (format `2026-06-10`), car ajouter un fuseau n'aurait pas de sens. Sinon, toujours `Instant`.
  - Si un endpoint a besoin du fuseau choisi par l'utilisateur (affichage, planification récurrente), stocker l'`Instant` **+** un champ `String zoneId` IANA séparé (ex. `Europe/Paris`). On ne mélange jamais les deux dans un seul champ.
- **En base** : Hibernate écrit les `Instant` en `timestamptz` normalisés UTC (`hibernate.jdbc.time_zone=UTC`). La JVM est forcée en UTC au démarrage (`TimeZone.setDefault(UTC)` dans `ApiLanelyApplication`) → logs, `Instant.now()` et SQL cohérents quel que soit le serveur.
- **En JSON** : Jackson configuré globalement (`write-dates-as-timestamps=false`, `time-zone=UTC`) → un `Instant` sort toujours en `2026-06-10T09:15:30Z`. En entrée, accepter le même format ISO-8601 (`...Z` ou avec offset `+02:00`, converti en UTC).
- **Calculs de temps** : `Instant.now().plus(...)`, `Duration`, `ChronoUnit`. Jamais de maths sur des millis bruts.
- **Doc OpenAPI** : chaque champ date/heure porte `@Schema(type = "string", format = "date-time", example = "2026-06-10T09:15:30Z")` (ou `format = "date"`, `example = "2026-06-10"` pour un `LocalDate`), et la description précise « ISO-8601 UTC ».
- **Config concernée** : `application.properties` (Jackson + Hibernate) et `ApiLanelyApplication.main` (UTC JVM). Ne pas réintroduire de fuseau serveur.

## Internationalisation (i18n) — `Accept-Language` (RÈGLE ABSOLUE)

L'API sert le monde entier. **Tout texte rendu à l'utilisateur final est traduit selon le header HTTP `Accept-Language` envoyé par le client** (le front Flutter et le front React envoient `Accept-Language: en` ou `fr`). Deux langues supportées : **`en` (défaut)** et **`fr`**. Une langue inconnue (`de`, `es`…) retombe sur l'anglais.

**RÈGLE ABSOLUE : on n'écrit JAMAIS un texte utilisateur en dur dans le code.** Chaque message passe par une **clé** résolue dans les bundles. À chaque ajout/modif d'un message utilisateur, mettre à jour **les deux** bundles dans le même temps — un message présent dans un seul des deux est considéré comme non terminé.

### Ce qui est i18n vs ce qui ne l'est pas

- **i18n (traduit)** : messages d'exception métier, messages de validation Bean, sujets et corps d'emails — tout ce que l'utilisateur lit.
- **PAS i18n (toujours anglais)** : code, noms de champs/clés JSON, valeurs d'énumérations, logs, doc OpenAPI, identifiants techniques.

### Où vivent les traductions

- `src/main/resources/i18n/messages.properties` → **anglais, bundle par défaut** (sert aussi de repli).
- `src/main/resources/i18n/messages_fr.properties` → **français**.
- Les **deux fichiers ont exactement le même jeu de clés** (en nombre et en noms). Convention de nommage : `error.<domaine>.<cas>` (erreurs), `jakarta.validation.constraints.<Annotation>.message` (validation), `mail.<type>.<part>` (emails).

### Comment lever une erreur traduite

Les exceptions métier (`extends ApiException`) ne portent **pas** un texte mais une **clé de message** (+ args optionnels) :

```java
throw new ResourceNotFoundException("error.user.not-found");
throw new MissingPermissionException("error.permission.missing", permission.name()); // {0} = arg
```

Le `GlobalExceptionHandler` résout la clé via `MessageSource` + `LocaleContextHolder.getLocale()` (locale issue de `Accept-Language`) et renvoie le texte dans la langue du client. Le `ErrorResponse.message` est donc déjà localisé.

### Validation Bean

Les annotations (`@NotBlank`, `@Size`, `@Email`…) sont localisées automatiquement : le validateur (`config/LocaleConfig.defaultValidator`) est branché sur un interpolateur **locale-aware** (`LocaleContextMessageInterpolator`) qui lit le bundle `messages`. Les clés `jakarta.validation.constraints.*.message` y sont surchargées en `en`/`fr`. Les paramètres `{min}`/`{max}`/`{value}` sont interpolés par Hibernate Validator (les laisser tels quels dans les bundles, ne pas les passer en `{0}`).

### Emails

`DefaultMailService` résout sujet/corps via `MessageSource` + `LocaleContextHolder.getLocale()` (la locale de la requête HTTP qui déclenche l'envoi). Clés `mail.*`.

### Piège MessageFormat (échappement des apostrophes)

Un message résolu **avec arguments** (`{0}`, `{1}`…) passe par `java.text.MessageFormat` → **toute apostrophe `'` doit être doublée `''`** dans le `.properties` (sinon le texte est tronqué). Un message **sans argument** est renvoyé tel quel (apostrophe simple OK). En pratique : doubler les apostrophes uniquement dans les messages français qui contiennent un `{n}` (ex. `L''image…`, `l''invitation…`).

### Config concernée

- `application.properties` : `spring.messages.basename=i18n/messages`, `spring.messages.encoding=UTF-8`, `spring.messages.fallback-to-system-locale=false`.
- `config/LocaleConfig` : `LocaleResolver` (`AcceptHeaderLocaleResolver`, supporte `en`/`fr`, défaut `en`) + validateur locale-aware.
- `ApiLanelyApplication.main` : `Locale.setDefault(Locale.ENGLISH)` (comme l'UTC pour les dates) → garantit l'anglais comme repli quelle que soit la locale du serveur.
- `CorsConfig` : `Accept-Language` est dans les en-têtes autorisés.
- Bundles UTF-8 : `messages.properties` (en) + `messages_fr.properties` (fr).

## Documentation Swagger / OpenAPI (springdoc)

**Règle absolue : à CHAQUE ajout ou modification d'une route (ou de son input/output), la doc OpenAPI DOIT être mise à jour dans le même temps.** Une route sans doc à jour est considérée comme non terminée.

- **Langue : anglais**, toujours (résumés, descriptions, exemples, messages).
- **Objectif : doc auto-suffisante.** Quelqu'un qui copie les infos d'une route depuis Swagger UI et les colle dans une IA doit pouvoir comprendre TOUTE la route sans autre contexte : méthode HTTP, chemin, format exact de chaque champ d'entrée (type, contraintes, exemple, obligatoire/optionnel), format de sortie, et tous les codes de statut possibles (succès + erreurs).
- **DTO obligatoires** en entrée et sortie (jamais une entity). Ex. une route `create user` prend un `CreateUserDto` et le DTO s'affiche dynamiquement dans Swagger. Chaque champ de DTO porte `@Schema(description, example, requiredMode)`.
- **Annoter chaque endpoint** :
  - `@Tag` sur le controller (nom + description du groupe).
  - `@Operation(summary, description)` sur la méthode.
  - `@ApiResponses` avec chaque code possible (`200/201/400/404/409/500`...) + description + schéma de réponse (succès et erreur).
  - `@Parameter(description, example)` sur les path/query params.
  - Validation Bean (`@NotNull`, `@Size`, `@Email`...) sur les DTO → reflétée automatiquement dans le schéma.
- Le schéma d'erreur global (`@RestControllerAdvice`) est lui aussi documenté et référencé dans les réponses d'erreur.

## Design des e-mails transactionnels (RÈGLE ABSOLUE)

> **Tout e-mail HTML envoyé par l'API DOIT passer par le layout d'e-mail commun et respecter la charte Lanely (couleur de marque #0153FD, carte blanche bordée #D9DDE5 rayon 10px sur fond #F7F8FA, texte #161A22/#4A5160, CTA primaire #0153FD). Jamais de couleur hors charte, jamais de HTML d'e-mail dupliqué, toujours bilingue EN/FR, toujours un fallback texte.**

### Où c'est implémenté

- **`email/template/EmailLayout`** : builder Java unique qui rend l'en-tête (marque « Lanely ») + la carte blanche + le pied de page. Tous les e-mails passent par lui ; aucun HTML/CSS dupliqué. Le layout ne porte QUE structure et style ; il reçoit du texte **déjà localisé**. Tout texte est HTML-échappé (une donnée utilisateur — nom de société — ne peut pas injecter de markup).
- **`email/template/EmailPalette`** : constantes de couleurs = **source de vérité**. Aucun hex en dur ailleurs ; si une nuance manque, prendre la plus proche de la charte.
- **`email/template/DeliveryStatusBadge`** : composant badge de statut réutilisable (paires de couleurs figées : pending/collected/transit/delivered/failed).
- **`service/mail/DefaultMailService`** : résout les clés i18n puis alimente `EmailLayout` et envoie en **multipart** (`EmailMessage.html` = HTML + fallback texte). Pas de chaîne en dur : tout vient des bundles `messages*.properties`.

### Comment ajouter un nouvel e-mail

1. Ajouter les clés dans **les deux** bundles (`mail.<type>.subject/preheader/title/intro/cta/note` + `mail.<type>.body` pour le repli texte). Pièges apostrophes MessageFormat : **doubler `''`** uniquement dans les clés résolues avec arguments (`{0}`…), apostrophe simple sinon.
2. Construire le HTML via `EmailLayout.builder()...render()` (en réutilisant `mail.layout.greeting/footer/support`), envoyer via `EmailMessage.html(to, subject, textFallback, html)`.
3. Pour une notif de livraison : ajouter `.badge(DeliveryStatusBadge.DELIVERED, label)` (label localisé).

### Charte (rappel — voir `EmailPalette` pour la liste complète)

- **Marque** : brand-500 `#0153FD` (primaire/CTA/marque), brand-600 `#0140C9` (hover), brand-800 `#002E94` (liens en corps), brand-50 `#E8EFFF` (accent doux).
- **Neutres** : page `#F7F8FA`, carte `#FFFFFF`, bordure `#D9DDE5`, séparateur `#EDEFF3`, texte `#161A22`, secondaire `#4A5160`, discret `#9AA1AE`. Danger `#E23B3B`.
- **Statuts livraison** (fort / fond / texte) : pending `#9AA1AE`/`#EDEFF3`/`#4A5160` · collected `#0153FD`/`#E8EFFF`/`#002E94` · transit `#F59E0B`/`#FEF3DC`/`#854F0B` · delivered `#16A05C`/`#E3F5EC`/`#0D6E3D` · failed `#E23B3B`/`#FCEAEA`/`#9B2727`.
- **Typo/rayons** : police système `-apple-system,'Segoe UI',Roboto,Helvetica,Arial,sans-serif` ; titre ~22px/600, corps ~15px/1.6, pied ~12px ; rayon 10px (carte/bouton), 6px (badges) ; espacements multiples de 4, carte padding 32px.

### Contraintes techniques e-mail (impératif)

- **Layout en tables** (`role="presentation"`, `cellpadding/cellspacing/border=0`), jamais flex/grid.
- **CSS inline** sur les éléments critiques ; `<style>` en `<head>` toléré en complément seulement.
- Carte `max-width:480px;width:100%`, responsive simple ; images `width` en attribut + `max-width:100%` + `alt`.
- **Bouton CTA « bulletproof »** à base de `<table>` avec **fallback VML** pour Outlook.
- **Toujours un fallback texte** (multipart) ; aucune dépendance externe (pas de CDN/webfont distante).
- **Bilingue EN/FR** via `Accept-Language` (cf. section i18n) : le template ne contient aucune chaîne en dur.
- Lien support du pied de page : `app.email.support-url` (URL ou `mailto:`).

## Commandes

```bash
mvn spring-boot:run          # dev
mvn clean package            # build -> target/api-lanely.jar
mvn test                     # tests
```
