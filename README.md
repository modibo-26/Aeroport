# ✈️ Aéroport - Backend Microservices

Système de gestion aéroportuaire basé sur une architecture microservices Spring Boot.

## Architecture

```
                         Client (React / Vue)
                               │
                               ▼
                      ┌────────────────┐
                      │  API Gateway   │  ← JWT Filter + CORS
                      │   port 8080    │
                      └───────┬────────┘
                              │
                      ┌───────▼────────┐
                      │     Eureka     │  ← Service Discovery
                      │   port 8761    │
                      └───────┬────────┘
                              │
        ┌─────────┬───────────┼───────────┬──────────┐
        ▼         ▼           ▼           ▼          ▼
   service-   service-   service-    service-     Kafka
     auth       vols    reservations notifications (async)
   :8081      :8082       :8083       :8084      :9092
     │          │           │           │
     ▼          ▼           ▼           ▼
   PostgreSQL PostgreSQL PostgreSQL  PostgreSQL
   :5431      :5432       :5433       :5434
```

## Stack technique

- **Java 21** + **Spring Boot 3.5**
- **Spring Cloud** (Eureka, Gateway, OpenFeign)
- **Spring Security** + **JWT** (dans service-auth et Gateway)
- **PostgreSQL 15** (une base par microservice)
- **Apache Kafka** (communication asynchrone)
- **Docker Compose** (infrastructure)
- **Lombok** + **Maven**

---

## Services

### Eureka Server (port 8761)

Service Discovery — annuaire central où tous les microservices s'enregistrent.

### API Gateway (port 8080)

Point d'entrée unique. Responsabilités :
- Routage des requêtes vers les services
- Validation JWT (filtre personnalisé)
- Extraction des infos utilisateur (headers `X-User-Id`, `X-User-Role`)
- Gestion CORS

**Routes :**

| Prefix | Service cible |
|--------|---------------|
| `/auth/**` | SERVICE-AUTH |
| `/vols/**` | SERVICE-VOLS |
| `/reservations/**` | SERVICE-RESERVATIONS |
| `/notifications/**` | SERVICE-NOTIFICATIONS |

### Service Auth (port 8081)

Authentification et inscription.

**Entités :** `User` (id, email, password, nom, prenom, role)

**Enum Role :** `PASSAGER`, `ADMIN`

**Endpoints :**

| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/auth/register` | Inscription |
| POST | `/auth/login` | Connexion (retourne JWT) |

**Sécurité :** Spring Security + BCrypt pour le hashage des mots de passe. JWT généré avec `io.jsonwebtoken` (jjwt), contient `sub` (userId), `email`, `role`, expiration 24h.

### Service Vols (port 8082)

Gestion des vols.

**Entités :** `Vol` (id, numeroVol, origine, destination, dateDepart, dateArrivee, placesDisponibles, prixBase, compagnie, statut)

**Enum StatutVol :** `A_L_HEURE`, `RETARDE`, `ANNULE`

**Endpoints :**

| Méthode | URL | Accès | Description |
|---------|-----|-------|-------------|
| GET | `/vols` | Public | Liste tous les vols |
| GET | `/vols/{id}` | Public | Détail d'un vol |
| GET | `/vols/destination/{destination}` | Public | Vols par destination |
| GET | `/vols/{id}/places-disponibles` | Public | Places restantes |
| POST | `/vols` | Admin | Créer un vol |
| PUT | `/vols/{id}` | Admin | Modifier un vol |
| PUT | `/vols/{id}/statut` | Admin | Changer le statut |
| DELETE | `/vols/{id}` | Admin | Supprimer un vol |

**Kafka Producer :** Publie sur le topic `vol-events` lors de la modification ou du changement de statut d'un vol.

### Service Réservations (port 8083)

Gestion des réservations avec vérification des places disponibles via Feign.

**Entités :** `Reservation` (id, passagerId, volId, dateReservation, statut, nombrePlaces)

**Enum Statut :** `EN_ATTENTE`, `CONFIRMEE`, `ANNULEE`

**Endpoints :**

| Méthode | URL | Accès | Description |
|---------|-----|-------|-------------|
| POST | `/reservations` | Authentifié | Créer une réservation |
| GET | `/reservations/{id}` | Authentifié | Détail réservation |
| PUT | `/reservations/{id}/confirmer` | Authentifié | Confirmer |
| PUT | `/reservations/{id}/annuler` | Authentifié | Annuler |
| GET | `/reservations/passager/{id}` | Authentifié | Mes réservations |
| GET | `/reservations/vol/{volId}` | Admin | Réservations d'un vol |

**Communication :**
- **Feign Client** vers SERVICE-VOLS pour vérifier les places disponibles avant réservation
- **Feign Client** vers SERVICE-VOLS pour incrémenter/décrémenter les places lors de la confirmation/annulation
- **Kafka Producer** : Publie sur `reservation-events` lors de la création, confirmation et annulation
- **Kafka Consumer** : Écoute `vol-events` pour annuler automatiquement les réservations si un vol est annulé

### Service Notifications (port 8084)

Notifications aux passagers, alimenté par Kafka.

**Entités :** `Notification` (id, passagerId, volId, message, dateCreation, lue)

**Endpoints :**

| Méthode | URL | Accès | Description |
|---------|-----|-------|-------------|
| POST | `/notifications` | Admin | Créer une notification |
| GET | `/notifications/passager/{id}` | Authentifié | Mes notifications |
| GET | `/notifications/vol/{volId}` | Admin | Notifications d'un vol |
| PUT | `/notifications/{id}/lue` | Authentifié | Marquer comme lue |
| DELETE | `/notifications/{id}` | Authentifié | Supprimer |

**Kafka Consumer :** Écoute les topics `vol-events` et `reservation-events` pour créer automatiquement des notifications.

---

## Communication inter-services

### Synchrone (Feign Client)

```
Service Réservations ──► Service Vols
   - Vérifier places disponibles
   - Incrémenter/décrémenter places
```

### Asynchrone (Kafka)

```
Service Vols ──────────► vol-events ──────────┐
                                              ├──► Service Notifications
Service Réservations ──► reservation-events ──┘
                                              └──► Service Réservations
                                                   (annulation auto si vol annulé)
```

**Topics Kafka :**
- `vol-events` : événements liés aux vols (modification, retard, annulation)
- `reservation-events` : événements liés aux réservations (création, confirmation, annulation)

---

## Sécurité

### Approche Gateway-centric

Le Gateway valide le JWT et transmet les infos utilisateur aux services via headers :

```
Client → [JWT dans Authorization header] → Gateway
  → Validation JWT
  → Extraction userId + role
  → Headers X-User-Id, X-User-Role → Services métier
```

Les services métier ne valident pas le JWT — ils font confiance au Gateway.

### Routes publiques

- `POST /auth/**` (login, register)
- `GET /vols/**` (consultation)

### Routes protégées

- Authentifié : réservations passager, notifications passager
- Admin : CRUD vols (POST/PUT/DELETE), notifications par vol, réservations par vol

---

## Infrastructure Docker

```yaml
# docker-compose.yml
services:
  postgres-auth:        # port 5431 → auth_db
  postgres-vols:        # port 5432 → vols_db
  postgres-reservations: # port 5433 → reservations_db
  postgres-notifications: # port 5434 → notifications_db
  kafka:                # port 9092 (KRaft, sans Zookeeper)
```

---

## Lancement

### 1. Infrastructure

```bash
docker-compose up -d
```

### 2. Services (dans l'ordre)

1. **eureka-server** (attendre que le dashboard soit accessible sur http://localhost:8761)
2. **api-gateway**
3. **service-auth**
4. **service-vols**
5. **service-reservations**
6. **service-notifications**

### 3. Vérification

- Eureka Dashboard : http://localhost:8761
- Tous les services doivent être enregistrés (5 services)

### 4. Test rapide

```bash
# Register
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456","nom":"Dupont","prenom":"Jean"}'

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}'

# Liste des vols (public)
curl http://localhost:8080/vols

# Créer un vol (admin + token)
curl -X POST http://localhost:8080/vols \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"numeroVol":"AF123","origine":"Paris","destination":"New York","dateDepart":"2026-02-01T10:00:00","dateArrivee":"2026-02-01T18:00:00","placesDisponibles":150,"prixBase":450.0,"compagnie":"Air France"}'
```

---

## Frontends

Le backend est consommé par deux frontends :

| Framework | Répertoire | Port | UI Library |
|-----------|-----------|------|------------|
| React | `aeroport-react/` | 3000 | Material UI |
| Vue 3 | `aeroport-vue/` | 5173 | Vuetify |

Les deux utilisent la même API via le Gateway (`http://localhost:8080`).
