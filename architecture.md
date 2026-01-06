# Architecture Diagram

## System Overview

```mermaid
graph TB
    Client[Client Applications]
    LB[Load Balancer]
    
    subgraph "Spring Boot Instances"
        API1[API Instance 1]
        API2[API Instance N]
    end
    
    subgraph "Data Layer"
        PG[(PostgreSQL<br/>Events + Aggregations)]
        Redis[(Redis<br/>Cache + Idempotency)]
        RMQ[RabbitMQ<br/>Event Queue]
    end
    
    subgraph "Worker Pool"
        W1[Worker 1]
        W2[Worker N]
    end
    
    subgraph "Observability"
        Prom[Prometheus]
        Graf[Grafana]
    end
    
    Client -->|JWT/API Key| LB
    LB --> API1
    LB --> API2
    
    API1 -->|Publish| RMQ
    API2 -->|Publish| RMQ
    API1 -->|Cache/Rate Limit| Redis
    API2 -->|Cache/Rate Limit| Redis
    API1 -->|Query| PG
    API2 -->|Query| PG
    
    RMQ -->|Consume| W1
    RMQ -->|Consume| W2
    W1 -->|Persist| PG
    W2 -->|Persist| PG
    W1 -->|Dedup Check| Redis
    W2 -->|Dedup Check| Redis
    
    API1 -->|Metrics| Prom
    API2 -->|Metrics| Prom
    Prom --> Graf
```

## Request Flow: Event Ingestion

```mermaid
sequenceDiagram
    participant C as Client
    participant A as API
    participant R as Redis
    participant Q as RabbitMQ
    participant W as Worker
    participant D as PostgreSQL
    
    C->>A: POST /v1/ingest + API Key
    A->>A: Validate API Key (cached)
    A->>A: Extract workspace_id
    A->>Q: Publish event message
    A->>C: 202 Accepted
    
    Q->>W: Consume message
    W->>R: Check idempotency key
    alt Key exists
        W->>Q: ACK (skip duplicate)
    else Key missing
        W->>R: Set idempotency key (TTL)
        W->>D: INSERT event
        W->>Q: ACK
    end
```

## Request Flow: Analytics Query

```mermaid
sequenceDiagram
    participant C as Client
    participant A as API
    participant R as Redis
    participant D as PostgreSQL
    
    C->>A: GET /analytics + JWT
    A->>A: Validate JWT
    A->>A: Extract workspace_id
    A->>R: Check cache (query hash)
    alt Cache hit
        R->>A: Return cached result
        A->>C: 200 OK (cached)
    else Cache miss
        A->>D: Query aggregations (filtered by workspace_id)
        D->>A: Result set
        A->>R: Cache result (TTL: 5min)
        A->>C: 200 OK
    end
```

## Multi-Tenancy Isolation

```mermaid
graph LR
    subgraph "Workspace A"
        UA[User A]
        PA[Project A1]
        EA[Events A]
    end
    
    subgraph "Workspace B"
        UB[User B]
        PB[Project B1]
        EB[Events B]
    end
    
    UA -->|workspace_id=1| PA
    PA -->|workspace_id=1| EA
    
    UB -->|workspace_id=2| PB
    PB -->|workspace_id=2| EB
    
    style EA fill:#e1f5e1
    style EB fill:#e1e5f5
```

**Enforcement:**
- All queries filtered by `workspace_id` at repository layer
- JWT/API Key contains workspace context
- Database indexes include `workspace_id` as first column

## Data Model

```mermaid
erDiagram
    WORKSPACE ||--o{ PROJECT : contains
    WORKSPACE ||--o{ MEMBERSHIP : has
    WORKSPACE ||--o{ API_KEY : owns
    USER ||--o{ MEMBERSHIP : belongs_to
    PROJECT ||--o{ EVENT : receives
    EVENT ||--o{ AGGREGATION : rolls_up_to
    
    WORKSPACE {
        bigint id PK
        string name
        timestamp created_at
    }
    
    USER {
        bigint id PK
        string email UK
        string password_hash
        timestamp created_at
    }
    
    MEMBERSHIP {
        bigint id PK
        bigint workspace_id FK
        bigint user_id FK
        string role
    }
    
    PROJECT {
        bigint id PK
        bigint workspace_id FK
        string name
        timestamp created_at
    }
    
    API_KEY {
        bigint id PK
        bigint workspace_id FK
        string key_hash UK
        timestamp created_at
    }
    
    EVENT {
        bigint id PK
        bigint workspace_id FK
        bigint project_id FK
        string idempotency_key UK
        jsonb payload
        timestamp event_time
        timestamp ingested_at
    }
    
    AGGREGATION {
        bigint id PK
        bigint workspace_id FK
        bigint project_id FK
        timestamp hour_bucket
        string event_type
        bigint count
    }
```

## Deployment Architecture

```mermaid
graph TB
    subgraph "Production Environment"
        subgraph "Compute"
            K8S[Kubernetes Cluster]
            POD1[API Pod 1]
            POD2[API Pod N]
            WPOD1[Worker Pod 1]
            WPOD2[Worker Pod N]
        end
        
        subgraph "Managed Services"
            RDS[(RDS PostgreSQL<br/>Multi-AZ)]
            ELASTICACHE[(ElastiCache Redis<br/>Cluster Mode)]
            MQ[Amazon MQ<br/>RabbitMQ]
        end
        
        subgraph "Observability"
            CW[CloudWatch Logs]
            PROM[Prometheus]
            GRAF[Grafana]
        end
        
        K8S --> POD1
        K8S --> POD2
        K8S --> WPOD1
        K8S --> WPOD2
        
        POD1 --> RDS
        POD2 --> RDS
        POD1 --> ELASTICACHE
        POD2 --> ELASTICACHE
        POD1 --> MQ
        POD2 --> MQ
        
        WPOD1 --> RDS
        WPOD2 --> RDS
        WPOD1 --> ELASTICACHE
        WPOD2 --> ELASTICACHE
        WPOD1 --> MQ
        WPOD2 --> MQ
        
        POD1 --> CW
        POD2 --> CW
        POD1 --> PROM
        POD2 --> PROM
        PROM --> GRAF
    end
```

## Scaling Strategy

| Component | Scaling Method | Trigger | Max Capacity |
|-----------|---------------|---------|--------------|
| API Pods | Horizontal (HPA) | CPU > 70% or RPS > 1000 | 10 pods |
| Worker Pods | Horizontal (HPA) | Queue depth > 1000 | 20 pods |
| PostgreSQL | Vertical + Read Replicas | CPU > 80% | 3 replicas |
| Redis | Cluster Mode | Memory > 80% | 6 shards |
| RabbitMQ | Cluster | Queue depth > 10000 | 3 nodes |
