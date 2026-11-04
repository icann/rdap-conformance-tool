# Logging - RDAP Conformance Tool

How `rdapct` writes logs, where they go, and the rules for adding or changing a log line.
Aligned with the team engineering standards (section 7, Observability) as applied in
`alerting-proxy-processing-service/docs/logging-info.md`. Checked against the code at version 4.0.1.

---

## 1. Stack

| Piece | Detail |
|---|---|
| API | SLF4J 2.0.13 everywhere (`org.slf4j.LoggerFactory`, 79 classes). No Log4j2, no JUL bridge |
| Binding | `logback-classic` 1.5.13 in `tool` only (`tool/pom.xml:42-48`). `validator` ships `slf4j-api` and uses `slf4j-simple` for tests (`validator/pom.xml:46,120`) |
| Config | `tool/src/main/resources/logback.xml` |
| Runtime control | `--logging <CLI|INFO|DEBUG|ERROR|VERBOSE>` (default `CLI`) and legacy `-v` (= `VERBOSE`), applied in `RdapConformanceTool.call()` |
| Telemetry | None (no OTel, no metrics). The tool is a short-lived process |

Embedding consumers (`RdapWebValidator`) provide their own binding; the library never
reconfigures the logger context.

## 2. Output format

Single `ConsoleAppender` (`STDOUT`):

```
%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

Example:

```
14:03:21.117 [main] INFO  o.i.r.v.workflow.rdap.RDAPDatasetServiceImpl - Downloading dataset rdap-extensions.xml
```

- Timestamps are local time without date. For long-lived embedding, prefer the JSON layout in §6.
- Logs go to **stdout**; the results JSON goes to `results/` (or stdout via the Docker
  entrypoint, which writes logs to **stderr** - `docker-entrypoint.sh:19` redirects `1>&2`).
  Never print log lines with `System.out` - they would corrupt piped results.

## 3. Levels and modes

Root and package levels are set per mode (`RdapConformanceTool.java:368-400`):

| `--logging` | Progress bar | `org.icann.rdapconformance` | root (3rd-party) |
|---|---|---|---|
| `CLI` (default) | yes | ERROR | ERROR |
| `ERROR` | no | ERROR | ERROR |
| `INFO` | no | INFO | ERROR |
| `DEBUG` | no | DEBUG | ERROR |
| `VERBOSE` / `-v` | no | DEBUG (inherits root) | DEBUG |

Choose the level by operational value:

| Level | Use for | Examples |
|---|---|---|
| ERROR | Run cannot proceed or a result cannot be produced | dataset download failure, results file write error, invalid definition file |
| WARN | Degraded but continuing | one protocol (IPv4/IPv6) unreachable, redirect limit hit, dataset served from stale cache |
| INFO | Lifecycle milestones, one line each | datasets loaded, query type resolved, each HTTP round started/finished, results file path |
| DEBUG | Per-test / per-response detail | schema validation node paths, DNS answers, HTTP headers, profile check outcomes |
| TRACE | Raw payloads | full response bodies (only here, never above) |

Rules:

- No per-test INFO lines - 300+ tests run per response; use DEBUG.
- Failures must not be logged at INFO.
- Log an exception once, where it is handled, with the throwable as the last argument.
- Third-party noise (httpclient5, dnsjava, everit) stays at root level; do not raise root to
  DEBUG outside `VERBOSE`.

## 4. Declaring a logger

```java
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
```

- Field name `logger`, `private static final` (66 of 79 classes already do this). Use this form in new code.
- SLF4J only. Do not import logback classes outside `RdapConformanceTool` (the only place that
  reconfigures the context).
- No Lombok `@Slf4j`.

## 5. Writing a log line

```java
logger.info("Dataset {} loaded from {}", dataset.getName(), source);
logger.error("Unable to write results file {}", path, e);
```

- `{}` placeholders, never string concatenation.
- Throwable as **last argument**.
- Never log: full RDAP response bodies above TRACE, definition-file contents, hostnames in
  `--ssrf-allowed-hosts` at INFO, or anything that could be registrant contact data (entities,
  vCards) above DEBUG.
- Messages say what happened; do not encode `key=value` pairs in the text - use MDC (§6).
- Repeated message text and state values are constants (e.g. `ProgressPhase`, `ToolResult`).

## 6. Structured JSON output and MDC (target state)

The current pattern layout is sufficient for interactive CLI use. For embedding (frontend) and
for shipping logs to Coralogix, adopt the following - recommended direction, not current behaviour:

### 6.1 MDC fields

Populate `org.slf4j.MDC` once per run in `RdapConformanceTool.call()` /
`RdapWebValidator.validate()` and clear it in `finally` (the web validator is reused across threads):

| Key | Set where | Value |
|---|---|---|
| `runId` | entry point | UUID per validation run |
| `app` | entry point | `rdapct` |
| `version` | entry point | `BuildInfo` / `version.properties` |
| `definitionId` | after config parse | `definitionIdentifier` |
| `queryType` | `RDAPQueryTypeProcessor` | `DOMAIN`, `NAMESERVER`, `ENTITY`, `HELP`, ... |
| `targetHost` | after URI parse | host of the RDAP URI |
| `protocol` | per round (`NetworkInfo`) | `IPv4` / `IPv6` |
| `acceptHeader` | per round | `application/json` / `application/rdap+json` |
| `phase` | `ProgressTracker` | `ProgressPhase` constant |
| `state` | outcome line | closed vocabulary: `STARTED`, `COMPLETED`, `FAILED`, `SKIPPED` |

Per-round keys (`protocol`, `acceptHeader`) must be set/removed around each round, not left
dangling.

### 6.2 JSON encoder

Add `net.logstash.logback:logstash-logback-encoder` to `tool` and a second appender selected by
system property `rdapct.log.format=json` (default `text` keeps today's behaviour):

```xml
<configuration>
    <appender name="TEXT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <timeZone>UTC</timeZone>
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <message>msg</message>
                <logger>component</logger>
            </fieldNames>
            <includeMdc>true</includeMdc>
            <shortenedLoggerNameLength>36</shortenedLoggerNameLength>
        </encoder>
    </appender>

    <logger name="org.icann.rdapconformance"
            level="${logging.level.org.icann.rdapconformance:-${defaultLogLevel:-ERROR}}"
            additivity="true"/>

    <root level="${logging.level.root:-${logLevel:-${defaultLogLevel:-ERROR}}}">
        <if condition='property("rdapct.log.format").equalsIgnoreCase("json")'>
            <then><appender-ref ref="JSON"/></then>
            <else><appender-ref ref="TEXT"/></else>
        </if>
    </root>
</configuration>
```

Resulting line:

```json
{"timestamp":"2026-11-04T20:03:21.117Z","level":"INFO","thread":"main","component":"o.i.r.v.w.r.RDAPDatasetServiceImpl","msg":"Dataset loaded","runId":"5e1c...","app":"rdapct","version":"4.0.1","queryType":"DOMAIN","targetHost":"rdap.example","phase":"DATASETS","state":"COMPLETED"}
```

(`<if>` requires `janino` on the classpath; alternatively select the appender programmatically
in `RdapConformanceTool` where the context is already reset.)

### 6.3 File rotation (embedded / long-running use only)

The CLI must stay stdout-only. When `rdapct` runs inside a long-lived JVM (frontend, batch
runner), add a rolling appender gated by `rdapct.log.file`:

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${rdapct.log.file:-logs/rdapct.log}</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
        <fileNamePattern>${rdapct.log.file:-logs/rdapct.log}.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
        <maxFileSize>50MB</maxFileSize>
        <maxHistory>14</maxHistory>
        <totalSizeCap>1GB</totalSizeCap>
    </rollingPolicy>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <timeZone>UTC</timeZone>
        <includeMdc>true</includeMdc>
    </encoder>
</appender>
```

Policy: daily + 50 MB rollover, gzip, 14 days, 1 GB cap. In Kubernetes rely on stdout and the
cluster collector instead; do not enable `FILE` there.

## 7. Team observability rules and how this tool measures up

| Rule (team standards section 7) | Status in rdapct |
|---|---|
| Context (`action`, `state`, ids) in MDC fields, not message text | **Not met.** No MDC usage in `tool` or `validator` |
| Message must never contradict the fields | N/A until MDC exists |
| Changing a log message is a breaking change for dashboards | Low impact today (CLI). Applies once the frontend/Coralogix consume JSON logs |
| Log level reflects operational value | **Met in structure** (five modes, package vs root split). Audit individual INFO lines per §3 when touching a class |
| Closed state vocabulary via constants | Partial: `ProgressPhase`, `ToolResult`, `ConnectionStatus` exist; log messages still inline literals |
| Structured output | **Not met.** Pattern layout only (§6.2 is the plan) |

## 8. Checklist for a PR that touches logging

- [ ] `private static final Logger logger = LoggerFactory.getLogger(X.class)`; SLF4J only
- [ ] `{}` placeholders, no concatenation; throwable last
- [ ] Level matches §3; no per-test or per-response INFO; payloads only at TRACE
- [ ] No contact data, definition-file contents or full response bodies above DEBUG
- [ ] No `System.out` / `System.err` for diagnostics (progress bar excepted)
- [ ] New repeated message text or state value is a constant
- [ ] If MDC keys are added: set and cleared in the same method (`try/finally`), names from the §6.1 table
- [ ] `--logging` modes still behave as in §3 (run `mvn -pl tool test -Dtest='LoggingLevelTest,RdapConformanceToolCallBranchesTest'`)
