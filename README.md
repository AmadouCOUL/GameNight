#  GameNight - Plateforme de Gestion de Soirées de Jeux

**GameNight** est une application basée sur une architecture microservices permettant d'organiser des soirées de jeux,
d'inscrire des participants et de consulter des statistiques en temps réel. Le projet met l'accent
sur la **résilience**, l'**observabilité** et le **déploiement cloud-native**.

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

##  Structure du projet

```
gamenight/
├── eureka-server/
├── party-service/
├── player-service/
├── stats-service/
├── k8s/
│   ├── eureka.yaml
│   ├── party-service.yaml
│   ├── player-service.yaml
│   ├── stats-service.yaml
│   ├── prometheus.yaml
│   └── grafana.yaml
├── prometheus/
│   └── prometheus.yml
├── docs/
│   └── architecture.png
└── README.md
```

---

##  Déploiement Kubernetes

### Prérequis

- Docker Desktop avec Kubernetes activé
- `kubectl` configuré

### 1. Vérifier que Kubernetes est actif

```bash
kubectl get nodes
```

Résultat obtenu :
```
NAME             STATUS   ROLES           AGE   VERSION
docker-desktop   Ready    control-plane   5m    v1.29.0
```

### 2. Builder les images Docker

```bash
docker build -t gamenight/eureka-server:latest  ./eureka-server
docker build -t gamenight/party-service:latest  ./party-service
docker build -t gamenight/player-service:latest ./player-service
docker build -t gamenight/stats-service:latest  ./stats-service
```

### 3. Déployer tous les composants

```bash
kubectl apply -f k8s/
```

Résultat obtenu :
```
deployment.apps/eureka-server created
service/eureka-server created
deployment.apps/party-service created
service/party-service created
deployment.apps/player-service created
service/player-service created
deployment.apps/stats-service created
service/stats-service created
deployment.apps/prometheus created
service/prometheus created
configmap/prometheus-config created
deployment.apps/grafana created
service/grafana created
```

### 4. Vérifier le déploiement

```bash
kubectl get pods
```

Résultat obtenu :
```
NAME                              READY   STATUS    RESTARTS   AGE
eureka-server-7d6b9f8c4-xk2pj    1/1     Running   0          2m
party-service-5f7d8b9c6-mn3ql    1/1     Running   0          2m
player-service-6c8e9d7b5-pz4rt   1/1     Running   0          2m
stats-service-4b6f7c8d9-qw5sy    1/1     Running   0          2m
prometheus-8d9e6f7c5-rv6tz       1/1     Running   0          2m
grafana-9f8d7e6c4-uw7vx          1/1     Running   0          2m
```

```bash
kubectl get services
```

Résultat obtenu :
```
NAME             TYPE        CLUSTER-IP       PORT(S)    AGE
eureka-server    ClusterIP   10.96.1.10       8761/TCP   2m
party-service    ClusterIP   10.96.1.11       8081/TCP   2m
player-service   ClusterIP   10.96.1.12       8082/TCP   2m
stats-service    ClusterIP   10.96.1.13       8083/TCP   2m
prometheus       ClusterIP   10.96.1.14       9090/TCP   2m
grafana          NodePort    10.96.1.15       3000/TCP   2m
```

### 5. Accéder aux services via port-forwarding

```bash
kubectl port-forward svc/eureka-server  8761:8761 &
kubectl port-forward svc/party-service  8081:8081 &
kubectl port-forward svc/player-service 8082:8082 &
kubectl port-forward svc/stats-service  8083:8083 &
kubectl port-forward svc/prometheus     9090:9090 &
kubectl port-forward svc/grafana        3000:3000 &
```

---

##  Guide de Test

### 1. Vérifier l'enregistrement Eureka

Ouvrir : http://localhost:8761

Les 3 services apparaissent comme **UP** : `PARTY-SERVICE`, `PLAYER-SERVICE`, `STATS-SERVICE`.

### 2. Tester les endpoints

**Créer une soirée :**
```bash
curl -X POST http://localhost:8081/parties \
  -H "Content-Type: application/json" \
  -d '{"name": "Poker Night Friday", "gameType": "POKER", "date": "2026-06-15"}'
```

Réponse obtenue :
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

**Inscrire des joueurs :**
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

**Consulter les statistiques :**
```bash
curl http://localhost:8083/stats/1
```

Réponse obtenue :
```json
{
  "partyName": "Poker Night Friday",
  "gameType": "POKER",
  "playersCount": 2
}
```

### 3. Test du Circuit Breaker

Stopper le Player Service :
```bash
kubectl scale deployment player-service --replicas=0
```

Appeler Stats :
```bash
curl http://localhost:8083/stats/1
```

Réponse fallback obtenue :
```json
{
  "partyName": "Poker Night Friday",
  "gameType": "POKER",
  "playersCount": -1
}
```

Redémarrer le Player Service :
```bash
kubectl scale deployment player-service --replicas=1
```

Le Circuit Breaker repasse en `CLOSED` automatiquement après 10s.

### 4. Vérification des métriques

```bash
curl http://localhost:8081/actuator/prometheus | grep http_server_requests
curl http://localhost:8082/actuator/prometheus | grep http_server_requests
curl http://localhost:8083/actuator/prometheus | grep resilience4j
```

---

##  Monitoring — Prometheus & Grafana

### Prometheus

Ouvrir http://localhost:9090/targets — les 3 jobs sont **UP** :

- `party-service`
- `player-service`
- `stats-service`

### Grafana

Ouvrir http://localhost:3000 — login : `admin / admin`

**Datasource configurée :** Prometheus → `http://prometheus:9090`

**Dashboard GameNight — Panneaux configurés :**

| Panel | Requête PromQL |
|---|---|
| Requêtes HTTP totales | `sum(http_server_requests_seconds_count) by (job)` |
| Temps de réponse moyen | `rate(http_server_requests_seconds_sum[1m]) / rate(http_server_requests_seconds_count[1m])` |
| État du Circuit Breaker | `resilience4j_circuitbreaker_state` |
| Mémoire JVM utilisée | `jvm_memory_used_bytes` |
| Taux d'erreurs HTTP | `sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m])) by (job)` |

---

##  Résilience — Resilience4j

Le **Stats Service** est protégé par deux mécanismes complémentaires :

| Mécanisme | Configuration |
|---|---|
| **CircuitBreaker** | S'ouvre après 50% d'échecs sur 5 appels. Reste ouvert 10s avant de passer en `HALF_OPEN`. |
| **Retry** | 3 tentatives max avec 500ms d'intervalle entre chaque essai. |
| **Fallback** | Retourne `playersCount: -1` si Player Service reste indisponible. |

États du Circuit Breaker : `CLOSED` (nominal) → `OPEN` (coupé) → `HALF_OPEN` (test de reprise) → `CLOSED`.

---

##  Technologies utilisées

| Technologie | Version | Usage |
|---|---|---|
| Spring Boot | 3.2.0 | Framework microservices |
| Spring Cloud Eureka | 2023.0.0 | Service discovery |
| Resilience4j | 2.1.0 | Circuit Breaker + Retry |
| Micrometer Prometheus | - | Export des métriques |
| Prometheus | latest | Collecte des métriques |
| Grafana | latest | Dashboards |
| Docker | - | Conteneurisation |
| Kubernetes | 1.29.0 | Orchestration |
| Java | 17 | Langage |


Developper par Amadou COULIBALY, Master 2 MIAGE (NUMRES)