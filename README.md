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
        ┌─────────┬───────────┼───────────┬──────────┬──────────┐
        ▼         ▼           ▼           ▼          ▼          ▼
   service-   service-   service-    service-    service-    Kafka
     auth       vols    reservations notifications paiement  (async)
   :8081      :8082       :8083       :8084       :8085     :9092
     │          │           │           │           │
     ▼          ▼           ▼           ▼           ▼
   PostgreSQL PostgreSQL PostgreSQL  PostgreSQL  PostgreSQL
   :5431      :5432       :5433       :5434       :5435
```

## Stack technique

- **Java 17** + **Spring Boot 4.0.2**
- **Spring Cloud 2025.1.0** (Eureka, Gateway, OpenFeign)
- **Spring Security** + **JWT** (dans service-auth et Gateway)
- **PostgreSQL 15** (une base par microservice)
- **Apache Kafka** (communication asynchrone)
- **Stripe** (paiement en ligne via Checkout Sessions)
- **Docker Compose** (infrastructure + services)
- **Lombok** + **Maven**

---

## Lancement rapide

### Prérequis

- Docker + Docker Compose

### Option 1 : Images Docker Hub (recommandé)

Aucun build nécessaire, les images sont téléchargées automatiquement :

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Option 2 : Build local (développement)

```bash
docker-compose up -d --build
```

### Vérification

- Eureka Dashboard : http://localhost:8761
- API Gateway : http://localhost:8080
- Tous les services doivent être enregistrés (6 services)

---

## Docker Hub

Les images sont disponibles publiquement :

| Service | Image |
|---------|-------|
| Eureka | `moboks/aeroport-eureka:v1` |
| Gateway | `moboks/aeroport-gateway:v1` |
| Auth | `moboks/aeroport-service-auth:v1` |
| Vols | `moboks/aeroport-service-vols:v1` |
| Reservations | `moboks/aeroport-service-reservations:v1` |
| Notifications | `moboks/aeroport-service-notifications:v1` |
| Paiement | `moboks/aeroport-service-paiement:v1` |

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
| `/paiements/**` | SERVICE-PAIEMENT |

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
- **Feign Client** vers SERVICE-VOLS pour incrémenter/décrémenter places lors de la confirmation/annulation
- **Kafka Producer** : Publie sur `reservation-events` lors de la création, confirmation et annulation (avec champ `source` : PASSAGER ou VOL)
- **Kafka Consumer** : Écoute `vol-events` pour annuler automatiquement les réservations si un vol est annulé
- **Kafka Consumer** : Écoute `paiement-events` pour confirmer la réservation après paiement

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
- Ignore les `reservation-events` avec `source=VOL` (la notification vient déjà de `vol-events`)

### Service Paiement (port 8085)

Gestion des paiements via Stripe Checkout Sessions.

**Entités :** `Paiement` (id, reservationId, passagerId, montant, stripeSessionId, stripePaymentIntentId, statut, createdAt)

**Enum StatutPaiement :** `EN_ATTENTE`, `PAYEE`, `ECHOUEE`, `REMBOURSEE`

**Endpoints :**

| Méthode | URL | Accès | Description |
|---------|-----|-------|-------------|
| POST | `/paiements?reservationId=&passagerId=&montant=` | Authentifié | Créer une session Stripe |
| POST | `/paiements/webhook` | Public | Webhook Stripe |
| POST | `/paiements/{id}/rembourser` | Admin | Rembourser un paiement |
| GET | `/paiements/reservation/{reservationId}` | Authentifié | Paiement d'une réservation |
| GET | `/paiements/{id}` | Authentifié | Détail d'un paiement |

**Stripe :**
- Crée une Checkout Session et retourne l'URL de paiement
- Le webhook écoute `checkout.session.completed` (confirme) et `checkout.session.expired` (échec)
- Garde anti-doublons : empêche de créer un paiement si un EN_ATTENTE ou PAYEE existe déjà

**Communication :**
- **Kafka Producer** : Publie sur `paiement-events` lors de la confirmation et du remboursement manuel
- **Kafka Consumer** : Écoute `reservation-events` pour rembourser silencieusement lors d'une annulation

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
Service Vols ──────────► vol-events ──────────┬──► Service Notifications (notif passagers)
                                              └──► Service Réservations (annulation auto)

Service Réservations ──► reservation-events ──┬──► Service Notifications (sauf source=VOL)
                                              └──► Service Paiement (remboursement silencieux)

Service Paiement ──────► paiement-events ─────┬──► Service Reservations (confirmation résa)
                                              └──► (extensible)
```

**Topics Kafka :**
- `vol-events` : événements liés aux vols (modification, retard, annulation)
- `reservation-events` : événements liés aux réservations (création, confirmation, annulation) — contient un champ `source` (PASSAGER/VOL)
- `paiement-events` : événements liés aux paiements (confirmation, remboursement)

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
- `POST /paiements/webhook` (webhook Stripe)

### Routes protégées

- Authentifié : réservations passager, notifications passager
- Admin : CRUD vols (POST/PUT/DELETE), notifications par vol, réservations par vol

---

## Test rapide

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

## Frontend

Le backend est consommé par un frontend React :

| Framework | Repo | Port | UI Library | Docker Hub |
|-----------|------|------|------------|------------|
| React | [Aeroport-Front](https://github.com/modibo-26/Aeroport-Front) | 3000 | Material UI | `moboks/aeroport-frontend:v1` |
