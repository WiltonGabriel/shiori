# Shiori API 栞

> Back-end robusto e API REST da Catálogo de Leituras **Shiori**, focada em acompanhamento de ciclos de leitura, curadoria comunitária de obras literárias e privacidade por design (Privacy by Design).

---

## 📖 Visão Geral

O **Shiori API** é o coração da plataforma Shiori. Ele provê serviços de alta performance para gerenciar acervos literários (livros, mangás, light novels, webcomics, manhwas e manhuas), rastreamento granular de ciclos de leitura, teia social de conexões entre leitores, moderação comunitária auditável e total conformidade com a LGPD (Lei Geral de Proteção de Dados).

### Principais Funcionalidades

- **Catálogo Global Curado:** Governança com Regra de Ouro (obras oficiais imutáveis para usuários comuns), pipeline de submissão comunitária e canal de crowdsourcing para sugestões de edição com auditoria administrativa.
- **Estante Pessoal & Ciclos Elásticos:** Rastreamento de progresso de leitura, re-leituras através de ciclos elásticos (`HistoricoLeitura`) e links de leitura estritamente privados e isolados na estante do usuário.
- **Taxonomia & Folksonomia:** Gêneros canônicos oficiais e tags livres da comunidade com sistema de votação líquida (upvotes/downvotes).
- **Interações Sociais & Feed:** Timeline assíncrona desnormalizada, conexões mútuas e comentários com máscara anti-spoiler e moderação por denúncias.
- **Segurança & Privacy by Design (LGPD/ANPD):** Encriptação simétrica de e-mails em repouso no PostgreSQL via AES-256, hash irreversível de senhas com BCrypt, consentimento granular de cookies com trilha de auditoria e rotina de direito ao esquecimento com anonimização total de dados pessoais.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 25 (OpenJDK 27 runtime)
- **Framework:** Spring Boot 4.x
  - Spring Web (RESTful API)
  - Spring Data JPA / Hibernate 7
  - Spring Security (Autenticação Stateless e Autorização RBAC)
  - Spring Validation (Bean Validation)
- **Banco de Dados:** PostgreSQL (Produção / Docker) e H2 in-memory (Testes automatizados)
- **Armazenamento de Objetos (Storage):** Armazenamento Compatível com S3 (AWS, R2, MinIO) para avatares, capas e mídias estáticas
- **Respostas de Erro:** RFC 9457 (`ProblemDetail`)
- **SDK de Armazenamento:** AWS Java SDK v2 (S3)
- **Gerenciador de Dependências:** Apache Maven com Maven Wrapper

---

## 🏗️ Arquitetura do Sistema

A arquitetura do Shiori segue os princípios de **Clean Architecture** e **Domain-Driven Design (DDD)**, dividida em módulos desacoplados:

1. **Identidade, Consentimento & Onboarding:** Entidade `Usuario`, gestão de `Role` (User, Curador, Admin) e registro legal auditável em `ConsentimentoUsuario`.
2. **Catálogo Canônico & Governança:** Entidades `Obra`, `Genero`, `Tag`, `VotoTag` e fluxo de sugestões de alteração em `SugestaoEdicaoObra`.
3. **Catálogo Pessoal (Estante):** `ItemCatalogo` e `HistoricoLeitura`, garantindo isolamento absoluto de metadados privados.
4. **Social & Moderação:** `ConexaoUsuario`, `AtividadeFeed`, `Comentario`, `DenunciaComentario` e `DenunciaObra`.
5. **Visão de Futuro (Modelo Federado):** Arquitetura preparada para evolução em protocolo federado descentralizado entre servidores autônomos utilizando UUIDs públicos.

---

## 🚀 Como Executar o Projeto Localmente

### Pré-requisitos

- Java Development Kit (JDK 25 ou superior)
- Git
- Docker & Docker Compose (opcional, para instâncias locais de banco de dados e S3)

### Passo a Passo

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/WiltonGabriel/shiori.git
   cd shiori
   ```

2. **Configure as Variáveis de Ambiente:**
   Copie o arquivo de exemplo `.env.example` para `.env` e ajuste as variáveis para o seu ambiente:
   ```bash
   cp .env.example .env
   ```

   Principais variáveis configuráveis:
   - `PORT`: Porta HTTP da aplicação (padrão `8080`).
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`: Conexão com o banco PostgreSQL.
   - `SHIORI_CRYPTO_KEY`: Chave simétrica AES-256 (gerada via `openssl rand -hex 32`).
   - `JWT_SECRET`: Chave para assinatura de tokens JWT (mínimo de 256 bits).
   - `S3_ENDPOINT`: URL base do serviço S3 compatível (ex: `http://localhost:9000` ou endpoint AWS/R2).
   - `S3_ACCESS_KEY`: Access Key para autenticação S3.
   - `S3_SECRET_KEY`: Secret Key para autenticação S3.
   - `S3_REGION`: Região S3 (ex: `us-east-1`).
   - `S3_BUCKET_NAME`: Nome do bucket para armazenamento de mídias (ex: `shiori-media`).

3. **Execute os Testes Automatizados:**
   Valide se o ambiente local compila e passa na suíte completa de testes:
   ```bash
   ./mvnw clean test
   ```

4. **Inicie a Aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```

A API estará acessível por padrão em `http://localhost:8080`.

---

## 📚 Documentação Pública e Guia de Integração (Open Source)

Se você é um desenvolvedor construindo um front-end alternativo (Web, Mobile, Desktop ou CLI) ou integrando bots/ferramentas externas com a Shiori API, consulte o nosso guia detalhado com arquitetura, autenticação Bearer JWT, estrutura de erros RFC 9457 e catálogo de endpoints:

📖 **[Guia Completo de Integração com a Shiori API](docs/API_INTEGRATION_GUIDE.md)**

---

## 🐳 Execução via Docker e Docker Compose

Para subir o back-end em container utilizando o Docker Compose de produção:

```bash
docker compose up -d
docker compose logs -f shiori-api
```

---

## 🛡️ Segurança e Proteção de Dados

Para relatar vulnerabilidades ou dúvidas sobre proteção de dados, por favor abra uma issue de segurança ou entre em contato com os mantenedores. Consulte nossa documentação de conformidade e as diretrizes do projeto.

---

## 📄 Licença

Este projeto é software de código aberto licenciado sob os termos da licença [MIT](LICENSE).
