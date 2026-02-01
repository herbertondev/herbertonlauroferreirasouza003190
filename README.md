# API REST - Artistas e Álbuns

API RESTful para gerenciamento de artistas musicais e seus álbuns, desenvolvida com **Quarkus 3.30.8** e **Java 21**.

## 📋 Dados do Candidato

- **Nome**: [Preencher nome]
- **Vaga**: Desenvolvedor Java Sênior
- **Processo Seletivo**: SEPLAG-MT

## 🚀 Tecnologias

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| Java | 21 | Linguagem |
| Quarkus | 3.30.8 | Framework |
| PostgreSQL | 16 | Banco de Dados |
| MinIO | Latest | Armazenamento S3 |
| Docker | 24+ | Containerização |

## 🎯 Requisitos Implementados

### Requisitos Gerais
- ✅ Segurança CORS
- ✅ Autenticação JWT (5 min expiração + refresh)
- ✅ POST, PUT, GET endpoints
- ✅ Paginação na consulta de álbuns
- ✅ Filtros parametrizados (SOLO/BANDA)
- ✅ Ordenação alfabética (asc/desc)
- ✅ Upload múltiplo de imagens
- ✅ Armazenamento no MinIO
- ✅ URLs pré-assinadas (30 min)
- ✅ Versionamento de endpoints (/v1)
- ✅ Flyway Migrations
- ✅ OpenAPI/Swagger

### Requisitos Sênior
- ✅ Health Checks (Liveness/Readiness)
- ✅ Testes unitários
- ✅ WebSocket para notificações
- ✅ Rate Limiting (10 req/min)
- ✅ Sincronização de Regionais

## 🏗️ Arquitetura

```
src/main/java/org/projetoseletivo/
├── client/          # REST Clients
├── config/          # Configurações
├── domain/entity/   # Entidades JPA
├── domain/enums/    # Enumerações
├── dto/             # DTOs Request/Response
├── filter/          # Filtros JAX-RS
├── health/          # Health Checks
├── mapper/          # MapStruct Mappers
├── repository/      # Repositórios Panache
├── resource/        # REST Resources
├── service/         # Serviços de negócio
└── websocket/       # Endpoints WebSocket
```

## 🐳 Como Executar

### Pré-requisitos
- Docker e Docker Compose
- Java 21 (opcional, para dev local)
- Maven 3.9+ (opcional, para dev local)


### Com Docker Hub (Sem Build Local)

Se você baixar apenas o `docker-compose-hub.yml`, pode rodar sem precisar compilar o código (requer que a imagem esteja no Docker Hub):

```bash
docker compose -f docker-compose-hub.yml up -d
```

### Com Docker Compose (Manual / Local Build)

1. **Compile a aplicação (Necessário antes de subir)**
```bash
# Isso gera o diretório target/ com o JAR da aplicação
./mvnw clean package -DskipTests
```

2. **Suba os containers**
```bash
# Versão moderna do Docker
docker compose up --build -d

# Versão antiga do Docker (se o comando acima falhar)
docker-compose up --build -d
```

3. **Verifique os logs**
```bash
docker compose logs -f api
```

4. **Parar e remover containers**
```bash
docker compose down
```

### Desenvolvimento Local

```bash
# Subir apenas BD e MinIO
docker-compose up -d db minio

# Executar API em modo dev
./mvnw quarkus:dev
```

## 📚 Documentação da API

Após iniciar a aplicação, acesse:

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui |
| OpenAPI JSON | http://localhost:8080/openapi |
| Health Check | http://localhost:8080/q/health |
| MinIO Console | http://localhost:9011 |
| MinIO API | http://localhost:9110 |

## 🔐 Autenticação

### Login
```bash
curl -X POST http://localhost:8080/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","senha":"admin123"}'
```

### Usar Token
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/v1/artistas
```

## 📡 Endpoints Principais

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | /v1/auth/login | Login |
| POST | /v1/auth/refresh | Renovar token |
| GET | /v1/artistas | Listar artistas |
| POST | /v1/artistas | Criar artista |
| PUT | /v1/artistas/{id} | Atualizar artista |
| GET | /v1/albuns | Listar álbuns |
| POST | /v1/albuns | Criar álbum |
| POST | /v1/albuns/{id}/imagens | Upload imagens |
| GET | /v1/albuns/{id}/imagens | Listar imagens |
| POST | /v1/regionais/sincronizar | Sincronizar regionais |

### Parâmetros de Consulta

```bash
# Paginação e ordenação
GET /v1/artistas?pagina=0&tamanho=10&ordem=asc

# Filtrar por tipo
GET /v1/albuns?tipo=BANDA

# Filtrar por artista
GET /v1/albuns?artista=Serj
```

## 🔌 WebSocket - Notificações em Tempo Real

Receba notificações automáticas quando novos álbuns são cadastrados.

### Página de Teste Visual
Acesse **http://localhost:8080/websocket-test.html** para testar o WebSocket diretamente no navegador.

### Conexão via Terminal
```bash
npx wscat -c ws://localhost:8080/ws/albuns
```

### Conexão via JavaScript
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/albuns');
ws.onmessage = (e) => console.log('Novo álbum:', JSON.parse(e.data));
```

### Formato da Notificação
```json
{
  "tipo": "NOVO_ALBUM",
  "album": {
    "id": 1,
    "titulo": "Nome do Álbum",
    "anoLancamento": 2026,
    "artistas": [],
    "imagens": []
  }
}
```

## 🧪 Testes

```bash
# Executar todos os testes
./mvnw test

# Executar testes específicos
./mvnw test -Dtest=ArtistaServiceTest
```

## 📦 Build para Produção

```bash
# Build JVM
./mvnw package

# Build Docker
docker build -f src/main/docker/Dockerfile.jvm -t artistas-api .
```

## 🎵 Dados Iniciais

A aplicação vem pré-carregada com os seguintes artistas e álbuns:

| Artista | Tipo | Álbuns |
|---------|------|--------|
| Serj Tankian | SOLO | Harakiri, Black Blooms, The Rough Dog |
| Mike Shinoda | SOLO | The Rising Tied, Post Traumatic, Post Traumatic EP, Where'd You Go |
| Michel Teló | SOLO | Bem Sertanejo, Bem Sertanejo - O Show, Bem Sertanejo EP |
| Guns N' Roses | BANDA | Use Your Illusion I, Use Your Illusion II, Greatest Hits |

## 📄 Licença

Este projeto foi desenvolvido para o processo seletivo SEPLAG-MT.
