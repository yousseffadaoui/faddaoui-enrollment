# ESM - Persistance des données avec Docker

Les données sont sauvegardées dans des bases **PostgreSQL** persistantes.

## Option 1 : Tout en Docker (recommandé)

```bash
docker-compose up -d --build
```

## Option 2 : Connexion instable – Postgres uniquement

Si le build échoue (connexion coupée), lancez seulement les bases :

```bash
docker-compose -f docker-compose.db-only.yml up -d
```

Puis exécutez les services localement (IDE ou `mvn spring-boot:run`) avec :

```
--spring.profiles.active=docker,docker-local
```

Services disponibles :
- **Eureka** : http://localhost:8761
- **Course Service** : http://localhost:8083
- **Enrollment Service** : http://localhost:8084
- **PostgreSQL (courses)** : localhost:5432 - base `courses_db`
- **PostgreSQL (enrollments)** : localhost:5433 - base `enrollments_db`

## Volumes persistants

| Volume | Données sauvegardées |
|--------|----------------------|
| `pg-courses-data` | Cours, catégories, instructeurs, modules, leçons |
| `pg-enrollments-data` | Inscriptions, progression, certificats |
| `course-uploads` | Images uploadées (thumbnails des cours) |

## Redémarrage

```bash
docker-compose down
docker-compose up -d
```

Les données restent intactes. Pour **supprimer toutes les données** :

```bash
docker-compose down -v
```
