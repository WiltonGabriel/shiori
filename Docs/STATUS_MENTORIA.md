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

### 2. Onde Parei Exatamente (WIP)
* **Arquivo Atual:** `src/main/java/dev/shiori/domain/model/Obra.java`
* **O que já foi feito na classe `Obra`:**
  * Herança de `EntidadeBase`, anotações `@Entity` e `@Table(name = "tb_obra")`.
  * Declaração completa dos atributos de estado e relacionamento `@OneToMany` com `List<HistoricoLeitura> historicosLeitura`.
  * Construtor protegido `protected Obra() {}` para o JPA.
  * Construtor público de negócio validando invariantes.
  * Implementação completa do método de negócio `registrarCicloLeitura(Double capituloAtingido, Double nota, String anotacoes)`:
    * Validação de capítulo atingido maior que o progresso atual e não nulo.
    * Validação de nota entre 0 e 10 (quando informada).
    * Nova regra de domínio consolidada: para registrar anotação, é obrigatório atribuir nota.
    * Lógica do Ciclo Elástico: estende o último ciclo se a nota for nula e ele estiver em aberto; caso contrário, cria novo `HistoricoLeitura`.
    * Atualização final de `progressoAtual = capituloAtingido`.
  * Implementação completa do método `public Double calcularMediaAvaliacaoCiclos()` com cálculo de média ponderada pelas extensões dos ciclos avaliados, retornando `null` quando não houver notas.
* **Ajustes e Pontos Pendentes para a Próxima Sessão em `Obra.java`:**
  1. No método `registrarCicloLeitura`: adicionar a chamada `this.mediaAvaliacaoCiclos = this.calcularMediaAvaliacaoCiclos();` logo abaixo de `this.progressoAtual = capituloAtingido;` para manter a nota oculta do olhinho sempre atualizada.
  2. Implementar `public void atualizarDadosLeitura(StatusLeitura novoStatus, Double novaAvaliacaoPessoal)`.
  3. Implementar os Getters da classe, protegendo `getHistoricosLeitura()` com `Collections.unmodifiableList(...)`.
  4. Seguir para o pacote `dev.shiori.dto` (criação dos DTOs com `record`).

### 3. Próximo Passo Imediato
A IA mentora deve orientar o dev a adicionar a atualização de `this.mediaAvaliacaoCiclos` dentro de `registrarCicloLeitura`, implementar o método `atualizarDadosLeitura` e fechar a classe `Obra.java` com seus Getters encapsulados.

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
