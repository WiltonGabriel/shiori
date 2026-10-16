# Status e Diretrizes da Mentoria: Projeto Shiori

Documento vivo de sincronização de estado, diretrizes pedagógicas e histórico de progresso do projeto Shiori.

---

## Parte 1: Diretrizes da Mentoria (Para a IA ler)

* **Zero Código Pronto (Regra de Ouro):** O propósito absoluto deste projeto é o aprendizado prático de Java, Orientação a Objetos (POO) e Spring Boot pelo desenvolvedor. A IA **NUNCA** deve criar arquivos `.java`, **NUNCA** deve editar arquivos `.java` no lugar do desenvolvedor e **NUNCA** deve fornecer blocos de código prontos no chat para copiar/colar. **NÃO USAR LOMBOK EM HIPÓTESE ALGUMA.** A IA atua exclusivamente como Mentora Socrática e Revisora de Código (explicando conceitos, apontando regras a cumprir e inspecionando o código escrito pelo dev).
* **Flexibilidade e Evolução (Documentação Viva):** A IA deve consultar `Docs/Guia_Pratico_e_Simplificado.md` e `Docs/Manual_de_Engenharia_e_POO.md` como base arquitetural e fonte da verdade de nomenclatura, mas esses arquivos não são uma camisa de força. A IA tem total liberdade para sugerir melhorias caso identifique falhas ou lacunas e deve acolher ideias e validações extras criadas pelo dev, atualizando as documentações em seguida para refletir o código real.
* **Ambiente Multiplataforma:** Ao executar comandos Maven no terminal para checar compilações ou testes, detectar automaticamente o Sistema Operacional:
  * Linux / CachyOS / macOS: `./mvnw`
  * Windows: `.\mvnw.cmd`

---

## Parte 2: Save Game do Projeto (Estado Atualizado Hoje)

### 1. Arquivos Concluídos e Revisados
* `src/main/java/dev/shiori/domain/enums/StatusObra.java`: Constantes oficiais `PREVISTO, ANDAMENTO, FINALIZADO, HIATO, CANCELADO`.
* `src/main/java/dev/shiori/domain/enums/StatusLeitura.java`: Constantes oficiais `PLANEJADO, LENDO, PAUSADO, TERMINOU, DROPOU`.
* `src/main/java/dev/shiori/domain/exception/RecursoNaoEncontradoException.java`: Exceção unchecked de domínio com repasse de mensagem para `super`.
* `src/main/java/dev/shiori/domain/exception/RegraDeNegocioException.java`: Exceção unchecked para validações de regras de negócio.
* `src/main/java/dev/shiori/domain/model/EntidadeBase.java`: `@MappedSuperclass` abstrata com `id`, `@GeneratedValue`, `equals` e `hashCode` baseados no identificador.
* `src/main/java/dev/shiori/domain/model/HistoricoLeitura.java`: Entidade de domínio mapeada para `tb_historico_leitura`, com construtor protegido, construtor de negócio com validação de invariantes, cálculo de extensão de ciclo, identificação de capítulo único, detecção de ciclo em aberto e extensão dinâmica de capítulo final.
* `src/main/java/dev/shiori/domain/model/Obra.java`: Entidade de domínio mapeada para `tb_obra`, herda de `EntidadeBase`, construtores JPA e de negócio com validação de invariantes, método `registrarCicloLeitura` com suporte a ciclo elástico e anotações atreladas à nota, cálculo de média ponderada dos ciclos `calcularMediaAvaliacaoCiclos`, atualização de status e nota pessoal `atualizarDadosLeitura`, e todos os Getters com coleção protegida por `Collections.unmodifiableList`.

### 2. Onde Parei Exatamente (WIP)
* **Próximo Pacote:** `dev.shiori.dto`
* **Arquivos a implementar:**
  1. `ObraCadastroRequest.java` (record com dados de entrada para criação da obra).
  2. `NovoCicloRequest.java` (record para registro de novo ciclo: `capituloAtingido`, `nota`, `anotacoes`).
  3. `AtualizarLeituraRequest.java` (record para atualização de status e avaliação pessoal).
  4. `HistoricoCicloResponse.java` (record com dados do ciclo para a API, incluindo construtor que converte de `HistoricoLeitura`).
  5. `ObraResponse.java` (record com dados completos da obra e lista de históricos convertidos).

### 3. Próximo Passo Imediato
A IA mentora deve guiar o desenvolvedor na criação e estruturação dos 5 DTOs do pacote `dev.shiori.dto` utilizando `record` do Java 21+, explicando o papel dos DTOs como fronteira do sistema e a diferença entre DTOs de entrada (Request) e saída (Response).

---

## Parte 3: Meus Prompts de Ctrl+C / Ctrl+V

### 1) PROMPT DE INÍCIO / RETOMADA (Para mandar na 1ª mensagem ao abrir outro PC ou nova IA):

```
Estou iniciando/retomando o desenvolvimento do projeto Shiori neste computador. Siga este protocolo antes de responder:

1. Execute `git pull` no terminal para garantir que estamos na versão mais recente do repositório.
2. Leia o arquivo `Docs/STATUS_MENTORIA.md` (e consulte `Docs/Guia_Pratico_e_Simplificado.md` e `Docs/Manual_de_Engenharia_e_POO.md` como base arquitetural).
3. Respeite nossas regras de ouro:
* PROIBIDO escrever código Java por mim ou criar arquivos `.java` (atue apenas como mentor socrático e revisor de código, sem usar Lombok).
* DOCUMENTAÇÃO FLEXÍVEL: Os arquivos em `Docs/` são um guia vivo, não uma camisa de força. Se notar algo na documentação que pode melhorar, me dê sugestões; se eu tiver ideias melhores no código (como validações ou erros extras), apoie e atualize a documentação depois.
* Detecte o SO automaticamente para rodar o Maven (`./mvnw` no Linux ou `.\mvnw.cmd` no Windows).

4. Me dê um resumo rápido de onde parei segundo o `Docs/STATUS_MENTORIA.md` e me oriente conceitualmente para o próximo passo!
```

### 2) PROMPT DE ENCERRAMENTO DO DIA (Para mandar quando eu for parar de programar):

```
Estou encerrando por hoje. Execute nosso protocolo de fechamento:

1. NÃO altere nenhum arquivo `.java` meu.
2. Revise o que programei hoje. Se criei melhorias ou mudanças válidas em relação ao plano original, atualize `Docs/Guia_Pratico_e_Simplificado.md` para manter a documentação viva.
3. Atualize a "Parte 2: Save Game do Projeto" dentro de `Docs/STATUS_MENTORIA.md` com os arquivos concluídos, o ponto exato onde parei hoje e a próxima missão (mantendo as Partes 1 e 3 intactas).
4. Faça `git add -A`, crie um commit descritivo do meu progresso e execute `git push`.
```
