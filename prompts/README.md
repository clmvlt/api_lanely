# Prompts — API Lanely

Dossier de **prompts réutilisables** à coller dans un assistant IA (Claude Code, etc.)
qui travaille sur ce dépôt.

Principe : un prompt ne **contient pas** les informations (elles deviendraient vite
obsolètes), il **demande à l'IA d'aller lire le code** pour construire elle-même sa
compréhension, puis de la garder en contexte pour la suite de la session.

## Prompts disponibles

- [`contexte-auth-profils-permissions.md`](contexte-auth-profils-permissions.md) —
  met en contexte tout le fonctionnement des connexions (users web / profils mobiles),
  des tokens & sessions, et du système de rôles & permissions. À utiliser **en début de
  session** avant de demander une évolution touchant à l'authentification, aux comptes
  ou aux permissions. → *fait lire le code à l'IA (pour travailler SUR le backend).*

- [`frontend-integration-auth.md`](frontend-integration-auth.md) —
  brief **auto-suffisant** à donner à l'IA qui développe le **frontend** : contrat complet
  (endpoints, payloads, codes de statut, flux signup/login/logout/refresh/`/me`, entreprises,
  invitations, permissions). → *contient les infos (l'IA frontend ne voit pas le backend) ;
  ne parle pas de design.*

> Deux familles de prompts : ceux qui **font lire le code** (pour travailler sur l'API) et
> ceux qui **embarquent le contrat** (pour un client externe qui n'a pas accès au code).

## Comment l'utiliser

1. Ouvre une session de l'assistant **à la racine du projet** (pour qu'il ait accès au code).
2. Colle le contenu du prompt voulu.
3. Laisse l'IA explorer le code et te restituer sa synthèse.
4. Enchaîne ensuite avec ta vraie demande : le modèle reste en contexte.
