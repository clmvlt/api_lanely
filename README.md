# API Lanely

API Spring Boot (Java 17) avec Hibernate / JPA et PostgreSQL.
Squelette vierge — aucune route d'exemple.

## Prerequis

- Java 17
- Maven 3.8+
- PostgreSQL

## Environnements

Le projet gere 4 environnements, chacun avec son propre fichier de config et sa propre base :

| Env   | Fichier de config                  | Port | Base par defaut |
|-------|------------------------------------|------|-----------------|
| dev   | `application-dev.properties`       | 8080 | `lanely-dev`    |
| prod  | `application-prod.properties`      | 8080 | `lanely-prod`   |
| beta  | `application-beta.properties`      | 8081 | `lanely-beta`   |
| demo  | `application-demo.properties`      | 8082 | `lanely-demo`   |

La config commune est dans `application.properties`.

## Lancer en DEV

```bash
mvn spring-boot:run
```

> Le profil `dev` est force automatiquement (voir `pom.xml`).

## Build

```bash
mvn clean package
```

Le jar est genere dans `target/api-lanely.jar`.

## Lancer PROD / BETA / DEMO

```bash
java -jar target/api-lanely.jar --spring.profiles.active=prod
java -jar target/api-lanely.jar --spring.profiles.active=beta
java -jar target/api-lanely.jar --spring.profiles.active=demo
```

## Surcharger la base sans modifier les fichiers (prod/beta/demo)

Les URL/identifiants peuvent etre passes via variables d'environnement :

```bash
DB_URL=jdbc:postgresql://mon-serveur:5432/ma_base \
DB_USER=mon_user \
DB_PASSWORD=mon_mdp \
java -jar target/api-lanely.jar --spring.profiles.active=prod
```
