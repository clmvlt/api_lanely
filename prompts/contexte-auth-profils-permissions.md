# Prompt — Mise en contexte : authentification, types de comptes/profils & permissions

> Colle ce prompt tel quel dans l'assistant, au début d'une session ouverte à la racine
> du dépôt de l'API Lanely. Il ne te donne **pas** les réponses : il te dit où et quoi
> lire pour que tu reconstruises toi-même le modèle à partir du code réel.

---

Tu vas travailler sur l'API Lanely (Spring Boot / Java, Hibernate, PostgreSQL).
Avant toute autre tâche, **construis un modèle mental complet et à jour** de la façon dont
fonctionnent : les connexions, les types de comptes/profils, les tokens & sessions, et les
rôles & permissions.

**Méthode imposée :** ne te fie à aucune supposition ni à une connaissance générale de
Spring Security. **Lis le code source réel** du dépôt et déduis le fonctionnement depuis
ce qui est effectivement écrit. Quand tu affirmes quelque chose, appuie-toi sur un fichier
précis (cite `chemin:ligne`).

## Questions auxquelles ta synthèse doit répondre

1. **Types de comptes.** Quels types d'identités peuvent exister et s'authentifier ?
   Comment sont-ils modélisés en base (stratégie d'héritage, table(s), discriminateur) ?
   Qu'est-ce qui les distingue (champs, ce qu'ils possèdent ou non, périmètre) ?
2. **Connexion.** Comment un utilisateur « web » s'authentifie-t-il, et comment un profil
   « mobile » s'authentifie-t-il ? En quoi les deux flux diffèrent-ils (point d'entrée,
   données requises, étape préalable éventuelle) ?
3. **Tokens & sessions.** Quelle est la nature des jetons émis, leur durée de vie, et ce
   qu'ils transportent ? Comment plusieurs connexions simultanées (plusieurs appareils)
   sont-elles gérées ? Comment un jeton est-il renouvelé, et comment une session est-elle
   révoquée / déconnectée ? Que se passe-t-il exactement à la révocation ?
4. **Rôles & permissions.** Quels rôles et quelles permissions existent, où sont-ils
   définis, comment sont-ils stockés et **comment sont-ils vérifiés** au moment d'agir ?
   Y a-t-il un rôle qui possède tout implicitement ? Qui a le droit d'attribuer des
   permissions ? Le jeu de permissions est-il figé ou prévu pour être étendu ?
5. **Appartenance & invitations.** Comment un compte rejoint-il une entreprise ? Comment
   une entreprise est-elle créée et qui en devient responsable ?
6. **Désactivation.** Que permet de faire la désactivation d'un profil, et quel effet
   a-t-elle sur ses connexions et ses jetons existants ?
7. **Cartographie des endpoints.** Dresse la liste des routes liées à l'authentification,
   aux comptes/profils, aux entreprises et aux permissions, avec pour chacune : méthode,
   chemin, qui peut l'appeler (public / authentifié / rôle / permission requise).
8. **Erreurs.** Sous quelle forme l'API renvoie-t-elle les erreurs (format commun, codes) ?

## Où commencer à lire (points d'entrée, à suivre de proche en proche)

Pars de ces zones du code et **suis les références** (appels, imports, annotations) :

- Les entités du domaine (paquet `entity` et ses énumérations) — pour les types de comptes
  et le modèle de permissions.
- La configuration et la sécurité (paquets `config` et `security`) — pour le filtre
  d'authentification, la composition des jetons, et les règles d'accès aux routes.
- La couche service (paquet `service`) — pour la logique d'émission/rotation/révocation des
  jetons, et pour la façon dont les droits sont contrôlés avant chaque action sensible.
- Les contrôleurs (paquet `controller`) — pour la cartographie des endpoints et leurs
  exigences d'accès.
- La configuration applicative (`src/main/resources/application*.properties`) — pour les
  paramètres (durées, secrets, comportements par environnement).
- La doc OpenAPI exposée par l'application (annotations sur les contrôleurs/DTO) si tu veux
  recouper, mais **la source de vérité reste le code**.

Le fichier `CLAUDE.md` à la racine décrit les conventions du projet : lis-le aussi.

## Livrable attendu

- Une **synthèse structurée** répondant aux 8 questions ci-dessus, concise mais précise,
  avec des renvois `fichier:ligne` pour les points clés.
- Signale explicitement toute **incohérence**, tout **TODO** ou tout comportement qui te
  paraît incomplet (par ex. une protection qui semble manquer).
- N'invente rien : si une information n'est pas dans le code, dis-le.

Une fois cette synthèse produite, **garde ce modèle en mémoire pour le reste de la
session** et fonde tes réponses suivantes dessus. Si une demande ultérieure contredit ce
que tu as lu, ou si le code a changé depuis, **revérifie dans le code** avant de répondre.
