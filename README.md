#  GameNight - Plateforme de Gestion de Soirées de jeux

**GameNight** est une application basée sur une architecture microservices permettant d'organiser des soirées de jeux,
d'inscrire des participants et de consulter des statistiques en temps réel. Le projet met l'accent
sur la **résilience**, l'**observabilité** et le **déploiement cloud-native**.

---

##  Architecture du Projet

L'application est décomposée en 4 services principaux :
- **Eureka Server** : Annuaire de services (Service Discovery).
- **Party Service** : Gestion de la création et de la consultation des soirées.
- **Player Service** : Gestion des inscriptions des joueurs.
- **Stats Service** : Agrégateur de données utilisant **Feign Clients** et **Resilience4j**.

```
                    +-------------------+
                    |      Grafana      |  :3000
                    +---------+---------+
                              |
                              v
                    +-------------------+
                    |    Prometheus     |  :9090
                    +----+----+----+----+
                         |    |    |
             scrape      |    |    |     scrape
         +---------------+    |    +---------------+
         |                    |                    |
         v                    v                    v
  +--------------+   +--------------+   +--------------+
  | Party Service|   | Stats Service|   |Player Service|
  |    :8081     |   |    :8083     |   |    :8082     |
  +------+-------+   +---+------+---+   +-------+------+
         |               |      |               |
         +-------+--------+      +-------+-------+
                 |                       |
                 +----------+------------+
                            |
                            v
                    +---------------+
                    | Eureka Server |  :8761
                    +---------------+

        Tous les composants sont déployés sur Kubernetes
```

---

##  Stack Technique

| Technologie                 | Usage                                  |
|-----------------------------|----------------------------------------|
| Spring Boot 3.4.2 / Java 17 | Framework principal                    |
| Spring Cloud Netflix Eureka | Service Discovery                      |
| Resilience4j                | Circuit Breaker & Retry                |
| Prometheus                  | Collecte des métriques                 |
| Grafana                     | Dashboard de visualisation             |
| Docker                      | Conteneurisation des services          |
| Kubernetes (Minikube)       | Orchestration et déploiement           |

---

##  Services

| Service        | Port | Description                              |
|----------------|------|------------------------------------------|
| Eureka Server  | 8761 | Service Discovery                        |
| Party Service  | 8081 | Gestion des soirées                      |
| Player Service | 8082 | Gestion des participants                 |
| Stats Service  | 8083 | Statistiques (avec Resilience4j)         |
| Prometheus     | 9090 | Collecte des métriques                   |
| Grafana        | 3000 | Dashboard de monitoring                  |

---

##  Lancer les services en local

### Prérequis

- Java 17+
- Maven 3.8+
- Docker (pour Prometheus et Grafana)

### Ordre de démarrage

>  Eureka doit être UP avant les autres services.

```bash
# Terminal 1 — Eureka Server
cd eureka-server
mvn spring-boot:run

# Terminal 2 — Party Service
cd party-service
mvn spring-boot:run

# Terminal 3 — Player Service
cd player-service
mvn spring-boot:run

# Terminal 4 — Stats Service (après les deux précédents)
cd stats-service
mvn spring-boot:run
```

### Vérifier l'enregistrement Eureka

Ouvrir : http://localhost:8761

Les 3 services doivent apparaître comme **UP** : `PARTY-SERVICE`, `PLAYER-SERVICE`, `STATS-SERVICE`.

---

##  Guide de Test (Validation du sujet)

Pour vérifier que tous les critères de l'examen sont respectés, vous pouvez suivre ces étapes :

### 1. Vérification de la Discovery (Eureka)

Ouvrez le tableau de bord Eureka via `minikube service eureka-server` (ou http://localhost:8761 en local).

- **Attendu** : Les services `PARTY-SERVICE`, `PLAYER-SERVICE` et `STATS-SERVICE` seront listés comme **UP**.

### 2. Test fonctionnel (Endpoints)

> Adaptez l'IP/Port selon votre environnement (`minikube service <nom> --url` ou `localhost` en local).

**Créer une soirée :**
```bash
curl -X POST http://localhost:8081/parties \
  -H "Content-Type: application/json" \
  -d '{"name": "Poker Night Friday", "gameType": "POKER", "date": "2026-06-15"}'
```

Réponse attendue :
```json
{
  "id": 1,
  "name": "Poker Night Friday",
  "gameType": "POKER",
  "date": "2026-06-15"
}
```

**Lister toutes les soirées :**
```bash
curl http://localhost:8081/parties
```

**Consulter une soirée par ID :**
```bash
curl http://localhost:8081/parties/1
```

**Inscrire des joueurs à la soirée 1 :**
```bash
curl -X POST http://localhost:8082/players \
  -H "Content-Type: application/json" \
  -d '{"partyId": 1, "playerName": "Alice"}'

curl -X POST http://localhost:8082/players \
  -H "Content-Type: application/json" \
  -d '{"partyId": 1, "playerName": "Bob"}'
```

**Lister les joueurs d'une soirée :**
```bash
curl http://localhost:8082/players/party/1
```

**Consulter les statistiques d'une soirée :**
```bash
curl http://localhost:8083/stats/1
```

Réponse attendue :
```json
{
  "partyName": "Poker Night Friday",
  "gameType": "POKER",
  "playersCount": 2
}
```

### 3. Test de résilience — Circuit Breaker & Fallback

Stopper le **Player Service** (`Ctrl+C` dans son terminal), puis :

```bash
curl http://localhost:8083/stats/1
```

Réponse fallback attendue (`playersCount: -1` indique que Player Service est indisponible) :
```json
{
  "partyName": "Poker Night Friday",
  "gameType": "POKER",
  "playersCount": -1
}
```

Relancer le Player Service — le Circuit Breaker repasse en `CLOSED` automatiquement après 10s et `playersCount` revient à la valeur réelle.

### 4. Vérification des métriques Prometheus

Chaque service expose `/actuator/prometheus` :

```bash
curl http://localhost:8081/actuator/prometheus | grep http_server_requests
curl http://localhost:8082/actuator/prometheus | grep http_server_requests
curl http://localhost:8083/actuator/prometheus | grep resilience4j
```

---

##  Monitoring — Prometheus & Grafana

### Lancer Prometheus

```bash
docker run -d --name prometheus \
  --network host \
  -v $(pwd)/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml \
  prom/prometheus:v2.50.0
```

Accéder à : http://localhost:9090

Vérifier que les 3 jobs sont **UP** : http://localhost:9090/targets

### Lancer Grafana

```bash
docker run -d --name grafana \
  --network host \
  -e GF_SECURITY_ADMIN_PASSWORD=gamenight123 \
  grafana/grafana:10.3.0
```

Accéder à : http://localhost:3000 — login `admin / gamenight123`

### Configurer le dashboard Grafana

1. **Connections → Data sources → Add data source → Prometheus**
    - URL : `http://localhost:9090` → **Save & Test**

2. **Dashboards → New → Add visualization**, puis utiliser les métriques suivantes :

| Panel                      | Requête PromQL                                                                              |
|----------------------------|---------------------------------------------------------------------------------------------|
| Requêtes HTTP totales       | `sum(http_server_requests_seconds_count) by (job)`                                          |
| Temps de réponse moyen      | `rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])` |
| État du Circuit Breaker     | `resilience4j_circuitbreaker_state`                                                         |
| Mémoire JVM utilisée        | `jvm_memory_used_bytes`                                                                     |
| Taux d'erreurs HTTP         | `sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) by (job)`                 |

---

##  Déploiement Kubernetes

### Prérequis

- `kubectl` configuré
- Cluster Kubernetes disponible (Minikube recommandé)
- Images Docker buildées

### 1. Démarrer Minikube

```bash
minikube start --memory=4096 --cpus=2
```

### 2. Builder les images dans l'environnement Minikube

```bash
eval $(minikube docker-env)

cd eureka-server  && mvn package -DskipTests && docker build -t gamenight/eureka-server:1.0.0  . && cd ..
cd party-service  && mvn package -DskipTests && docker build -t gamenight/party-service:1.0.0  . && cd ..
cd player-service && mvn package -DskipTests && docker build -t gamenight/player-service:1.0.0 . && cd ..
cd stats-service  && mvn package -DskipTests && docker build -t gamenight/stats-service:1.0.0  . && cd ..
```

### 3. Déployer tous les composants

```bash
kubectl apply -f k8s/eureka.yaml
kubectl apply -f k8s/party-service.yaml
kubectl apply -f k8s/player-service.yaml
kubectl apply -f k8s/stats-service.yaml
kubectl apply -f k8s/prometheus.yaml
kubectl apply -f k8s/grafana.yaml
```

### 4. Vérifier le déploiement

```bash
# Surveiller les pods jusqu'à ce qu'ils soient tous Running
kubectl get pods -w

# Vérifier les services exposés
kubectl get services

# Vérifier les deployments
kubectl get deployments
```

Tous les pods doivent afficher `STATUS = Running` et `READY = 1/1`.

### 5. Accéder aux services

```bash
# URLs Minikube (NodePort)
minikube service eureka-server --url
minikube service prometheus --url
minikube service grafana --url
```

Ou via port-forwarding :
```bash
kubectl port-forward svc/eureka-server  8761:8761 &
kubectl port-forward svc/party-service  8081:8081 &
kubectl port-forward svc/player-service 8082:8082 &
kubectl port-forward svc/stats-service  8083:8083 &
kubectl port-forward svc/prometheus     9090:9090 &
kubectl port-forward svc/grafana        3000:3000 &
```

---

##  Résilience — Resilience4j

Le **Stats Service** est protégé par deux mécanismes complémentaires :

| Mécanisme          | Configuration                                                                          |
|--------------------|----------------------------------------------------------------------------------------|
| **CircuitBreaker** | S'ouvre après 50% d'échecs sur 10 appels. Reste ouvert 10s avant de passer en `HALF_OPEN`. |
| **Retry**          | 3 tentatives max avec backoff exponentiel : 500ms → 1s → 2s.                           |
| **Fallback**       | Retourne `playersCount: -1` si Player Service reste indisponible.                       |

États du Circuit Breaker : `CLOSED` (nominal) → `OPEN` (coupé) → `HALF_OPEN` (test de reprise) → `CLOSED`.

---




