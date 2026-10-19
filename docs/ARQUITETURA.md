# Arquitetura do Sistema Shiori (API & Ecossistema)

Este documento descreve as decisões arquiteturais, padrões de projeto e diretrizes de segurança que regem o **Shiori API** e sua interação com clientes desacoplados na rede.

---

## 1. Filosofia Arquitetural

O Shiori foi concebido sob a filosofia de **Soberania do Leitor** (*Reader Sovereignty*) e **Privacidade por Design** (*Privacy by Design*):

1. **Separação de Domínios:**
   - **O Domínio Canônico Comunitário (Hub):** Catálogo enciclopédico público de metadados de obras literárias (mangás, webtoons, light novels, livros).
   - **O Domínio Pessoal (Estante do Leitor):** Coleção pessoal de leitura (`ItemCatalogo` e `HistoricoLeitura`), estritamente privada, desacoplada e independente de catálogos centrais.

2. **Resistência à Fragilidade e Censura:**
   - Se uma obra for descontinuada, sofrer DMCA ou for removida do Hub global, o leitor **não perde** seu histórico, seus volumes lidos nem seus metadados salvos.
   - O leitor mantém **snapshots locais** autônomos dos metadados da obra diretamente na sua estante.

---

## 2. Segregação de Camadas (Clean Architecture & SOLID)

A API Java Spring Boot adota divisão estrita de responsabilidades:

```
[Requisição HTTP Externa]
           │
           ▼
┌──────────────────────────────────────────────┐
│ Controllers (infra/controllers)              │  Responsabilidade Única: Roteamento HTTP,
│                                              │  validação de DTOs e retorno RFC 9457
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Services (domain/service)                    │  Responsabilidade Única: Regras de negócio,
│                                              │  orquestração e isolamento transacional
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Repositories (domain/repository)             │  Responsabilidade Única: Persistência e
│                                              │  consultas JPA/SQL especializadas
└──────────────────────┬───────────────────────┘
                       │
                       ▼
┌──────────────────────────────────────────────┐
│ Entities (domain/entity)                     │  Responsabilidade Única: Modelagem de dados
│                                              │  e invariantes de domínio
└──────────────────────────────────────────────┘
```

- **Injeção de Dependências por Construtor:** Obrigatória em todos os serviços e controladores (princípio DIP do SOLID). Proibido `@Autowired` em campos.
- **DTOs Exclusivos:** Toda entrada e saída utiliza Records ou DTOs especializados (`*Request`, `*Response`), desacoplando entidades de persistência do contrato da API pública.

---

## 3. Soberania da Estante e Snapshot Autônomo

Na entidade `ItemCatalogo`:
- O campo `obra_id` é **anulável (`nullable = true`)**.
- A estante do leitor armazena snapshots locais:
  - `tituloSnapshot`
  - `autorSnapshot`
  - `capaUrlSnapshot`
  - `tipoObraSnapshot`
  - `statusObraSnapshot`
- **Importação/Exportação Soberana:** O leitor pode exportar sua estante completa em formato JSON padronizado e portá-la para qualquer outro cliente ou instância Shiori autônoma.

---

## 4. Segurança de Dados Pessoais e Segredos

1. **Criptografia Simétrica em Repouso (AES-256-GCM):**
   - URLs de leitura privadas (`urlLeitura`) do usuário são encriptadas antes de serem gravadas no banco de dados, utilizando chave secreta de 256 bits (`SHIORI_CRYPTO_SECRET_KEY`).
   - Evita qualquer vazamento de links privados ou credenciais de terceiros mesmo em caso de dump não autorizado do banco de dados.
2. **Pseudonimização da Rede Mesh:**
   - Comunicações comunitárias com o Hub utilizam um identificador aleatório (`instanceUuid`). O Hub não rastreia nem armazena endereços IP ou perfis dos leitores ao receber submissões comunitárias anônimas.
3. **Respostas de Erro RFC 9457 (`ProblemDetail`):**
   - Nenhuma stacktrace interna é exposta em respostas de erro para clientes externos.

---

## 5. Política de CORS Flexível e Dinâmica

Para permitir que instâncias do `shiori-web` hospedadas em diferentes origens (ex: `http://localhost:3000`, `https://shiori.meudominio.com` ou provedores serverless) consumam a API sem rejeição de preflight:

1. **Origens Dinâmicas Configuráveis:**
   - A propriedade `shiori.cors.allowed-origins` (variável `CORS_ALLOWED_ORIGINS`) suporta múltiplos domínios separados por vírgula ou o curinga `*` via `allowedOriginPatterns`.
2. **Cabeçalhos Autorizados:**
   - Permite o tráfego dos cabeçalhos `Authorization: Bearer <token>`, `X-Instance-UUID`, `Content-Type`, `Accept` e `X-Client-Version`.
3. **Exposição de Cabeçalhos:**
   - Expõe cabeçalhos essenciais como `Authorization`, `Link` e `X-Total-Count` para os clientes web.
