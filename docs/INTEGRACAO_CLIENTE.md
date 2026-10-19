# Guia de Integração para Clientes Desacoplados (Shiori API)

Este guia orienta desenvolvedores e mantenedores na criação e integração de clientes front-end (web SPAs, aplicativos mobile, ferramentas CLI ou bots) com a **Shiori API**.

---

## 1. Visão Geral da Comunicação

A Shiori API é uma API RESTful stateless. Toda comunicação trafega via HTTPS com corpos de requisição e resposta formatados em JSON (`Content-Type: application/json`).

### Formato de Erro Padronizado (RFC 9457)
Todas as respostas de erro HTTP (4xx e 5xx) utilizam o MIME Type `application/problem+json`:

```json
{
  "type": "https://shiori.dev/erros/validacao",
  "title": "Erro de Validação",
  "status": 400,
  "detail": "O campo título não pode ser vazio.",
  "timestamp": "2026-10-19T20:00:00.000Z"
}
```

---

## 2. Endpoints Públicos (Sem Autenticação)

Os seguintes endpoints podem ser consumidos livremente por clientes desacoplados sem o envio de token JWT:

### 2.1. Verificação de Saúde (Health Check)
- **Método:** `GET /actuator/health`
- **Uso:** Utilizado pelo cliente web para testar latência e conectividade com o Hub.
- **Resposta:**
  ```json
  {
    "status": "UP"
  }
  ```

### 2.2. Autocomplete de Obras
- **Método:** `GET /api/obras/autocomplete?termo={query}`
- **Uso:** Sugestão rápida de títulos para busca e incorporação na estante.
- **Resposta (Array):**
  ```json
  [
    {
      "id": 10,
      "titulo": "Solo Leveling",
      "tipoObra": "MANHWA",
      "capaUrl": "https://cdn.shiori.dev/capas/solo-leveling.webp"
    }
  ]
  ```

### 2.3. Catálogo Global de Obras
- **Método:** `GET /api/obras?page=0&size=20&busca={termo}&tipo={MANGA|NOVEL|...}`
- **Uso:** Listagem paginada de obras públicas com sinopses, gêneros e contadores comunitários.

---

## 3. Autenticação e Autorização (JWT)

A autenticação é baseada em JSON Web Token (JWT).

### 3.1. Login do Usuário
- **Método:** `POST /api/auth/login`
- **Payload:**
  ```json
  {
    "email": "leitor@shiori.dev",
    "senha": "MinhaSenhaSuperSegura123"
  }
  ```
- **Resposta de Sucesso (200 OK):**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tipo": "Bearer",
    "usuario": {
      "id": 1,
      "apelido": "LeitorSombra",
      "email": "leitor@shiori.dev",
      "role": "ROLE_USER"
    }
  }
  ```

### 3.2. Envio do Token em Requisições Protegidas
Para endpoints que manipulam a estante, perfil ou histórico de leitura, inclua o cabeçalho HTTP:
```http
Authorization: Bearer <seu_token_jwt>
```

---

## 4. Endpoints da Estante Pessoal (Soberania do Leitor)

### 4.1. Listar Itens da Estante
- **Método:** `GET /api/catalogo/estante`
- **Cabeçalho:** `Authorization: Bearer <token>`
- **Resposta:** Lista de itens do leitor, contendo snapshots locais e status de leitura.

### 4.2. Adicionar Obra à Estante
- **Método:** `POST /api/catalogo/estante`
- **Cabeçalho:** `Authorization: Bearer <token>`
- **Payload:**
  ```json
  {
    "obraId": 10,
    "status": "LENDO",
    "progressoAtual": 45,
    "urlLeitura": "https://minha-fonte-privada.com/manga/solo-leveling/45"
  }
  ```
  *(Nota: A `urlLeitura` é automaticamente cifrada em repouso com AES-256-GCM).*

### 4.3. Portabilidade da Estante (Exportação e Importação Soberana)
- **Exportar:** `GET /api/catalogo/estante/exportar` -> Retorna dump JSON estruturado com todos os snapshots e histórico.
- **Importar:** `POST /api/catalogo/estante/importar` -> Restaura estante em qualquer cliente ou instância autônoma.

---

## 5. Pareamento Mesh Descentralizado

Clientes web podem se parear anonimamente com o Hub para submeter metadados comunitários à enciclopédia global:

- **Solicitar Pareamento:** `POST /api/hub/pareamento/solicitar`
- **Consultar Status:** `GET /api/hub/pareamento/status/{instanceUuid}`
- **Submissão Comunitária:** `POST /api/hub/pareamento/submeter-obra` (Requer aprovação prévia do Hub).
