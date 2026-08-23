# Orcamento por Voz — API com Spring AI (v2.1.0)

API de orcamento pessoal que recebe comandos de voz, transcreve o audio,
interpreta a intencao com um LLM (Tool Calling) e executa acoes reais
(registrar transacao, consultar saldo, historico, gasto por categoria,
resumo por periodo). Tambem expõe os mesmos casos de uso via REST puro,
sem depender de voz/IA.

## Fluxo

```
POST /comandos (audio)
        │
        ▼
  salva ComandoVoz (RECEBIDO)
        │
        ▼
  publica evento no RabbitMQ  ──► responde 202 Accepted (comandoId)
        │                              cliente consulta:
        ▼                          GET /comandos/{id}/status
  ComandoVozListener consome
        │
        ▼
  TranscricaoService (Whisper) ──► texto
        │
        ▼
  InterpretadorService (ChatClient + @Tool)
        │
        ├─► RegistrarTransacaoTool          ──┐
        ├─► ConsultarSaldoTool                │
        ├─► ConsultarHistoricoTool            ├─► TransacaoService (cache Caffeine, 30s)
        ├─► ConsultarGastoPorCategoriaTool    │
        └─► ConsultarResumoPeriodoTool      ──┘
        │
        ▼
  status atualizado no Postgres + Redis (CONCLUIDO/ERRO)
```

`TransacaoController` (REST puro) chama o **mesmo** `TransacaoService` das tools
de IA — registrar/consultar por voz ou por HTTP direto tem exatamente o
mesmo comportamento (validação, cache, persistência).

O upload responde imediatamente (202) porque a transcricao + chamada ao LLM
podem demorar — o processamento roda em background (RabbitMQ + Virtual Threads),
e o cliente consulta o resultado via polling no endpoint de status.

## Tecnologias

- Java 21 (Virtual Threads)
- Spring Boot 3.3 / Spring AI (ChatClient, Tool Calling, transcricao Whisper)
- PostgreSQL (persistencia)
- Liquibase (versionamento e migracao de schema)
- Caffeine (cache local do saldo, evita recalcular a cada consulta)
- RabbitMQ (desacoplamento do pipeline de IA)
- Redis (status de processamento, consulta rapida)
- Docker Compose (infraestrutura local)
- Gradle (build)

## Como executar

0. **Gere o wrapper do Gradle** (uma vez só, se você tiver o Gradle instalado localmente):
   ```bash
   gradle wrapper --gradle-version 8.10
   ```
   Isso cria `gradlew`, `gradlew.bat` e `gradle/wrapper/gradle-wrapper.jar`. Sem o Gradle instalado,
   baixe em https://gradle.org/install/ ou use o `gradlew` de qualquer outro projeto Spring Boot
   gerado pelo [start.spring.io](https://start.spring.io) e copie os 3 arquivos pra cá.
1. Suba a infraestrutura:
   ```bash
   docker compose up -d
   ```
2. Copie `.env.example` para `.env` e preencha `OPENAI_API_KEY`.
3. Rode a aplicacao:
   ```bash
   ./gradlew bootRun
   ```

## Endpoints

**Via voz (IA):**
```bash
curl -X POST http://localhost:8080/comandos \
  -F "audio=@caminho/para/comando.mp3"
# -> {"comandoId": "...", "status": "RECEBIDO", ...}

curl http://localhost:8080/comandos/{comandoId}/status
# -> {"status": "CONCLUIDO", "textoTranscrito": "...", "respostaIa": "...", ...}
```

**Via REST direto (sem voz/IA) — mesmo `TransacaoService`, mesmo cache:**
```bash
# registrar
curl -X POST http://localhost:8080/transacoes \
  -H "Content-Type: application/json" \
  -d '{"descricao":"mercado","valor":50.00,"tipo":"DESPESA","categoria":"ALIMENTACAO"}'

# saldo
curl http://localhost:8080/transacoes/saldo

# historico (opcionalmente filtrado por categoria, limite padrao 10)
curl "http://localhost:8080/transacoes?categoria=TRANSPORTE&limite=5"

# total gasto numa categoria
curl http://localhost:8080/transacoes/categoria/ALIMENTACAO/total

# resumo dos ultimos N dias (padrao 30)
curl "http://localhost:8080/transacoes/periodo?dias=7"
```

## Arquitetura (DDD em 3 camadas)

```
com.seuprojeto.orcamentovoz
│
├── domain/                    ← regras de negocio puras, sem depender de infra
│   ├── model/                 - Transacao, ComandoVoz, Categoria, TipoTransacao, StatusComando
│   ├── vo/                    - Value Objects: TransacaoId, ComandoVozId, Valor
│   ├── repository/            - portas de persistencia (interfaces Spring Data JPA)
│   └── exception/             - excecoes de negocio (TransacaoInvalidaException, etc.)
│
├── application/                ← orquestra os casos de uso, sem saber COMO a infra funciona
│   ├── service/                - TransacaoService (registrar/consultar, cache), OrcamentoService (pipeline de voz), StatusProcessamentoService
│   └── dto/
│       ├── request/            - RegistrarTransacaoRequest (validado com Bean Validation)
│       └── response/           - TransacaoDTO, SaldoResponse, ResumoPeriodoResponse, StatusComandoResponse
│
└── infrastructure/             ← tudo que fala com o mundo externo (framework, IO, rede)
    ├── web/
    │   ├── controller/          - ComandoVozController (voz), TransacaoController (REST direto)
    │   └── exception/           - GlobalExceptionHandler (traduz excecao de dominio -> HTTP)
    ├── ai/                      - TranscricaoService, InterpretadorService (chamam o OpenAI)
    │   └── tools/                - RegistrarTransacaoTool, ConsultarSaldoTool, ConsultarHistoricoTool,
    │                                ConsultarGastoPorCategoriaTool, ConsultarResumoPeriodoTool (@Tool)
    ├── messaging/                - RabbitMQ: event, publisher, listener
    └── config/                   - RabbitMQ (Virtual Threads), Redis, Spring AI, Cache (Caffeine)

resources/db/changelog/           - migracoes Liquibase (schema versionado, uma tabela por changeset)
```

**Regra de dependencia:** `infrastructure` depende de `application`, que depende de `domain` —
nunca o contrario. `domain` nao importa nada de `infrastructure` (nem Spring AI, nem RabbitMQ,
nem Redis) — so `jakarta.persistence` nas entidades, aceito aqui como um trade-off pragmatico
(evita uma camada extra de mapeamento so pra manter o domain 100% livre de anotacoes).

## Testes

```bash
./gradlew test
```

Cobertura por camada:
- `domain/` — regras de negócio dos agregados (`Transacao`, `ComandoVoz`) e Value Objects (`Valor`, `TransacaoId`, `ComandoVozId`)
- `application/service/` — `TransacaoService` (registrar, saldo, histórico, gasto por categoria, resumo por período), `TransacaoServiceCacheTest` (cache real via proxy AOP, não mock), `OrcamentoService` (pipeline de voz), `StatusProcessamentoService`
- `infrastructure/ai/tools/` — as 5 tools (`RegistrarTransacaoTool`, `ConsultarSaldoTool`, `ConsultarHistoricoTool`, `ConsultarGastoPorCategoriaTool`, `ConsultarResumoPeriodoTool`), todas com `TransacaoService` mockado
- `infrastructure/web/controller/` — `ComandoVozController` e `TransacaoController` via `MockMvc`, incluindo validação (`@Valid`) e o `GlobalExceptionHandler`
- `infrastructure/web/exception/` — mapeamento de cada exceção de domínio e de validação para o status HTTP correto
- `infrastructure/messaging/publisher/` — `ComandoVozPublisher` publica na exchange/routing key certas

## O que foi implementado como evolucao do projeto base

- Pipeline assincrono via RabbitMQ (desacopla upload do processamento de IA)
- Status de processamento consultavel via Redis (sem sobrecarregar o Postgres)
- Virtual Threads para o processamento em background
- Exceptions de dominio personalizadas + GlobalExceptionHandler
- Value Objects tipados para todos os IDs (TransacaoId, ComandoVozId)
- Variaveis de ambiente/credenciais nunca hardcoded (.env + application.yml)
- Testes unitarios cobrindo dominio, tools de IA, orquestrador, controller e cache

## v2.1.0 — o que mudou

- **Novas ferramentas de Tool Calling**: `ConsultarHistoricoTool` (lista as
  últimas transações, com filtro opcional por categoria), `ConsultarGastoPorCategoriaTool`
  (total gasto numa categoria) e `ConsultarResumoPeriodoTool` (receitas/despesas/saldo
  dos últimos N dias — "como estão meus gastos essa semana?").
- **Prompt do assistente refinado**: o `defaultSystem()` do `ChatClient` agora
  lista as 5 ferramentas disponíveis, orienta como mapear fala livre para as
  categorias válidas (ex: "uber"/"gasolina" → TRANSPORTE), como distinguir
  receita de despesa pela fala, e pede pra nunca inventar valores que não
  vieram de uma ferramenta.
- **Novos endpoints REST** (`TransacaoController`): `POST /transacoes`,
  `GET /transacoes/saldo`, `GET /transacoes`, `GET /transacoes/categoria/{categoria}/total`,
  `GET /transacoes/periodo` — todos chamando o mesmo `TransacaoService` das
  tools de IA, com validação via Bean Validation (`@Valid`) e tratamento de
  erro dedicado (corpo JSON inválido, enum inválido, campo inválido) no
  `GlobalExceptionHandler`.
- **Refatoração**: a lógica de registrar/consultar transação (antes espalhada
  dentro das tools de IA) foi extraída para `TransacaoService` — ponto único
  de verdade usado tanto pela IA quanto pelo REST. O cache Caffeine migrou
  junto, então agora funciona igual nos dois caminhos de entrada (antes só
  funcionava se a chamada viesse da IA).

## Ideias de evolução do desafio DIO — status

| Ideia sugerida | Status |
|---|---|
| Adicionar novos tipos de consulta financeira | ✅ histórico, gasto por categoria, resumo por período |
| Melhorar as respostas geradas pela IA | ✅ prompt de sistema reescrito com regras de mapeamento e estilo |
| Criar novas ferramentas para o Tool Calling | ✅ 3 tools novas (5 no total) |
| Adicionar validações antes de salvar uma transação | ✅ `Transacao` (domínio) + `@Valid` no REST |
| Melhorar os endpoints REST | ✅ `TransacaoController` com 5 endpoints novos |
| Criar testes para os principais fluxos | ✅ cobertura em todas as camadas |
| Documentar melhor como usar a API | ✅ este README |
| Propor uma nova ideia de assistente com a mesma base técnica | não explorado |

## v2.0.0 — o que mudou

- **Liquibase**: o schema do Postgres agora e' versionado por changesets
  (`db/changelog/`) em vez de `hibernate.ddl-auto=update`. O Hibernate so
  valida (`ddl-auto=validate`) que as entidades batem com o schema aplicado.
- **Caffeine**: cache do saldo por 30s (cache local, por instancia),
  invalidado a cada nova transacao registrada.
- **Reestruturacao DDD**: o pacote unico "por tipo" (controller/, service/,
  ai/, config/, etc. todos soltos na raiz) virou 3 camadas explicitas
  (`domain`, `application`, `infrastructure` — ver secao "Arquitetura"
  acima). As excecoes de negocio saem do `GlobalExceptionHandler` (que e'
  puramente web/HTTP) e vao para `domain/exception`. Imports padronizados
  em ordem alfabetica em todos os arquivos.
