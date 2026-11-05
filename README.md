# RDAP Conformance Tool (rdapct)

Stand-alone Java 21 CLI (and embeddable library) that verifies the conformity of an RDAP server
against the IETF specifications (RFC 7480, 7481, 7482, 7483, 7484) and the ICANN gTLD RDAP
Profile (February 2024). It only tests domain-related RDAP servers; there are no specific tests
for IP address or AS number RDAP servers.

Installation and usage guide: <https://icann.github.io/rdap-conformance-tool/>.
Architecture: [`docs/architecture.md`](docs/architecture.md) (C4 diagrams in
[`docs/diagrams/`](docs/diagrams/)). Logging: [`docs/logging-info.md`](docs/logging-info.md).
Execution flow: [`docs/process_flow.md`](docs/process_flow.md). Test codes: [`docs/codes.md`](docs/codes.md).
Developer docs index: [`docs/README.md`](docs/README.md).

## Why This Tool Exists

Registries and registrars must publish RDAP services that comply with the gTLD RDAP Profile.
`rdapct` automates the 300+ checks so that ICANN Technical Services, contracted parties and the
RDAP Conformance Tool Frontend get the same, reproducible verdict from one engine.

## Architecture Overview

- **Single-shot process, no server, no database.** Input: URI + definition file. Output:
  `results/results-<timestamp>.json` + exit code.
- **Three Maven modules** (`pom.xml:13-15`):

| Module | Role |
|---|---|
| `jitpack` | POM-only; pulls `org.everit.json.schema` from jitpack.io |
| `validator` | Validation engine library (datasets, DNS, HTTP, JSON schema, profile checks, results) |
| `tool` | picocli CLI -> shaded `tool/target/rdapct-<version>.jar`; `RdapWebValidator` for embedding |

- **Flow:** load 13 IANA datasets -> resolve query type -> DNS A/AAAA -> for each
  (IPv4/IPv6) x (`application/json`, `application/rdap+json`): HTTP GET -> schema validation ->
  profile validation -> aggregate -> apply definition file -> write results.
- **Entry points:** `org.icann.rdapconformance.tool.Main` (CLI) and
  `org.icann.rdapconformance.tool.RdapWebValidator` (thread-safe, in-memory results).

## Technology Stack

| Area | Choice |
|---|---|
| Language / build | Java 21, Maven 3.9 multi-module, `flatten-maven-plugin` |
| CLI | picocli 4.6.1 |
| Logging | SLF4J 2.0.13 API; logback-classic 1.5.13 binding in `tool` |
| HTTP | Apache httpclient5 5.6.3 (IPv4/IPv6 source binding, redirects, TLS checks) |
| DNS | dnsjava |
| JSON | Jackson 2.22.2, json-path, everit json-schema 1.12.2 |
| Validation helpers | icu4j, ipaddress, owasp-java-html-sanitizer, commons-text |
| Tests | TestNG, AssertJ, Mockito, WireMock; JaCoCo -> SonarQube |
| Packaging | Shaded jar; Docker (`alpine` + `openjdk21-jre`) |

## Getting Started

### Prerequisites

- JDK 21
- Maven 3.x
- Outbound HTTPS to `iana.org` / `data.iana.org` (datasets) and `jitpack.io` (first build)
- Docker (optional, for the container image)
- For `mvn deploy`: credentials for Artifactory `ICANN-repository` (`artifactory.icann.org`) in `~/.m2/settings.xml`

### Build and Run Tests

```bash
# Build everything and run all tests
mvn clean verify

# Build the executable jar without tests
mvn package -DskipTests
# -> tool/target/rdapct-<version>.jar

# Run a single test class
mvn -pl tool test -Dtest=LoggingLevelTest
mvn -pl validator test -Dtest=SchemaValidatorDomainTest
```

### Run Locally

```bash
# Registry, gTLD profile Feb-2024, default definition file
java -jar tool/target/rdapct-4.0.1.jar \
  -c tool/bin/rdapct_config.json \
  --use-rdap-profile-february-2024 --gtld-registry \
  https://rdap.example/domain/example.com

# Registrar, verbose logs, reuse cached datasets
java -jar tool/target/rdapct-4.0.1.jar \
  -c tool/bin/rdapct_config.json \
  --use-rdap-profile-february-2024 --gtld-registrar \
  --use-local-datasets --logging DEBUG \
  https://rdap.example/domain/example.com

# Validate a local response file
java -jar tool/target/rdapct-4.0.1.jar -c tool/bin/rdapct_config.json \
  --use-rdap-profile-february-2024 --gtld-registry file:///tmp/domain.json

java -jar tool/target/rdapct-4.0.1.jar --help
```

Results are written to `./results/results-<timestamp>.json`; datasets are cached in `./datasets/`.

Most-used options: `-c/--config` (required), `--timeout` (20 s), `--maximum-redirects` (3),
`--use-local-datasets`, `--additional-conformance-queries`, `--no-ipv4-queries`,
`--no-ipv6-queries`, `--dns-resolver <ip>`, `--ssrf-allowed-hosts`, `--thin`,
`--logging CLI|INFO|DEBUG|ERROR|VERBOSE`, `-v`.

### Run with Docker

```bash
docker build -t rdapct .
# Datasets are baked into the image; results stream to stdout, logs to stderr
docker run --rm rdapct --use-rdap-profile-february-2024 --gtld-registry \
  https://rdap.example/domain/example.com > results.json
```

### Exit Codes

Source of truth: `validator/src/main/java/org/icann/rdapconformance/validator/ToolResult.java`
(`Main` passes `ToolResult.getCode()` straight to `System.exit`).

| Code | Name | Meaning |
|---|---|---|
| 0 | `SUCCESS` | Response received, parsed and results file written |
| 1 | `CONFIG_INVALID` | Definition file is syntactically invalid |
| 2 | `DATASET_UNAVAILABLE` | A dataset could not be downloaded |
| 3 | `UNSUPPORTED_QUERY` | Query type not supported |
| 4 | `MIXED_LABEL_FORMAT` | domain/nameserver query mixes A-labels and U-labels |
| 9 | `USES_THIN_MODEL` | TLD uses the thin model |
| 21 | `FILE_WRITE_ERROR` | Could not write results file |
| 22 | `FILE_READ_ERROR` | Could not read results file |
| 25 | `BAD_USER_INPUT` | Invalid CLI input |
| 26 | `CONFIG_DOES_NOT_EXIST` | Definition file not found |

### Embedding (RdapWebValidator)

```java
URI uri = URI.create("https://rdap.example/domain/example.com");

// Flag constructors (RdapWebValidator.java:83-154): isRegistry=true -> gTLD registry profile
try (var validator = new RdapWebValidator(uri, true)) {
    RDAPValidatorResults results = validator.validate();
}

// Full control: implement org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration
// yourself and pass it in (RdapWebValidator.java:166). The inner Simple/Configurable
// configuration classes are private and not usable from outside.
RDAPValidatorConfiguration config = new MyRdapConfig(uri);
try (var validator = new RdapWebValidator(uri, config)) {
    RDAPValidatorResults results = validator.validate();
}
```

See `tool/src/main/java/org/icann/rdapconformance/tool/RdapWebExample.java`.

## Configuration file

Configuration definition
The definition file specifies which single tests are errors, warnings, or ignored (i.e. not tested for).
Note: a definition file is required for starting a test.
A configuration definition file specifies:

- definitionIdentifier: a required JSON string that identifies the configuration definition file.
  The string is copied verbatim to the definitionIdentifier element of the results file.
- definitionError: an optional JSON array of objects.
    - Each object contains the following elements:
        - code: a required JSON number that identifies a single test.
        - notes: a required JSON string that is copied verbatim if the test fails, generating an
          entry in the results section in the results file.
- definitionWarning: an optional JSON array of objects.
    - Each object contains the following elements:
        - code: a required JSON number that identifies a single test.
        - notes: an optional JSON string that is copied verbatim if the test fails, generating an
          entry in the results section in the results file.
- definitionIgnore: an optional JSON array of single test identifiers that are ignored (i.e. not
  tested for). The contents of this element are copied verbatim to the ignore section in the results
  file.
- definitionNotes: an optional JSON array of strings that are copied verbatim to the notes section
  in the results file.

### Minimal config file

The simplest config file one can write looks like this:

```
{
    "definitionIdentifier": "test"
}
```

This config file will simply tells the tool to put the id "test" inside the output result file. This
id allows the user to discriminate test runs between each other.

### A more complete example

A more complete example of a configuration definition file is shown below:

```
{
    "definitionIdentifier": "Sample rdap-conformance-tool configuration file",
    "definitionWarning": [
       { "code": -12308,
         "notes": "Entity includes invalid Entities array: See errors for details." },
       { "code": -12309,
         "notes": "Entity includes invalid Remarks array: See errors for details." },
       { "code": -12213,
         "notes": "Domain includes invalid Remarks object; See errors for details." },
       { "code": -12210,
         "notes": "Domain includes invalid Entities array; See errors for details." },
       { "code": -11901,
         "notes": "Entities array includes invalid Entity: See errors for details." },
       { "code": -12214,
         "notes": "Domain includes invalid Links array: See errors for details." }
    ],
    "definitionIgnore": [],
    "definitionNotes": ["This is a custom configuration definition to filter hierarchical errors as warnings."]
}
```

## Internals

This software fetches various datasets to be used for doing the tests. These datasets are fetched from IANA registries,
saved into the datasets directory, and parsed by the various tests as needed.
The datasets are:
- RDAPExtensionsDataset: https://www.iana.org/assignments/rdap-extensions/rdap-extensions.xml
- RegistrarIdDataset: https://www.iana.org/assignments/registrar-ids/registrar-ids.xml
- BootstrapDomainNameSpaceDataset: https://data.iana.org/rdap/dns.json
- EPPRoidDataset: https://www.iana.org/assignments/epp-repository-ids/epp-repository-ids.xml
- SpecialIPv4AddressesDataset: https://www.iana.org/assignments/iana-ipv4-special-registry/iana-ipv4-special-registry.xml
- IPv4AddressSpaceDataset: https://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml
- LinkRelationsDataset: https://www.iana.org/assignments/link-relations/link-relations.xml
- RDAPJsonValuesDataset: https://www.iana.org/assignments/rdap-json-values/rdap-json-values.xml
- IPv6AddressSpaceDataset: https://www.iana.org/assignments/ipv6-address-space/ipv6-address-space.xml
- DsRrTypesDataset: https://www.iana.org/assignments/ds-rr-types/ds-rr-types.xml
- DNSSecAlgNumbersDataset: https://www.iana.org/assignments/dns-sec-alg-numbers/dns-sec-alg-numbers.xml
- MediaTypesDataset: https://www.iana.org/assignments/media-types/media-types.xml
- SpecialIPv6AddressesDataset: https://www.iana.org/assignments/iana-ipv6-special-registry/iana-ipv6-special-registry.xml



## CI/CD

Two pipelines run on this repository.

### Jenkins (internal, authoritative for the Maven artefact)

`Jenkinsfile` uses the shared library `ICANN_LIB` (`icann.Utilities`) on a `docker` agent.
Build retention: 8 builds / 7 days.

| Stage | What runs | Branch |
|---|---|---|
| `Environment` | `checkout scm` | all |
| `Run Tests` | `utils.mvn(args: 'clean verify', jdkVersion: 'jdk21')` | non-`master` |
| `Run Tests` | `utils.mvn(args: 'clean deploy', jdkVersion: 'jdk21', publishArtifacts: true)` - runs tests **and** deploys `org.icann.rdap-conformance:{jitpack,validator,tool}` (version `rdap-conformance.version`) to Artifactory `ICANN-repository` (`pom.xml:18-24`); artefacts are also archived on the Jenkins build | `master` |
| `SonarQube Analysis` | `mvn sonar:sonar -Dsonar.java.source=21 -Dsonar.java.target=21` under `withSonarQubeEnv('ICANN')`; JaCoCo XML from both modules (`pom.xml:55-58`). Advisory - no quality-gate wait | all |
| `Publish C4` | `utils.buildC4Diagram(path: 'docs/diagrams', pillar: 'ts')` - renders `docs/diagrams/*.puml` and publishes them to the TS C4 site. Runs only on `master` **and** when `docs/diagrams` changed in the **last commit** (`hasPathChanged` diffs `HEAD^ HEAD`) or build parameter `FORCE_C4_PUBLISH=true`. The parameter has no effect on other branches | `master` |
| `Notify JIRA` | `jiraSendDeploymentInfo environmentId: 'qa'` - marks the linked issues as deployed to QA. No branch guard: runs on every branch build | all |
| `finally` | `step([$class: 'Publisher'])` (test reports) + `utils.notifyBuild(result, 'ts-eng-builds')`; start notification goes to `jenkinsjobs` | all |

Build parameters: `FORCE_C4_PUBLISH` (default `false`) - republish the C4 diagrams even if `docs/diagrams` did not change in the last commit. Only honoured on `master`. Needed after a rebase-merge whose final commit does not touch `docs/diagrams`.

Deployment is therefore **automatic on every merge to `master`**: the pipeline builds with
JDK 21, runs the full test suite, publishes the versioned jars to Artifactory and records the
QA deployment in JIRA. Downstream consumers (RDAP Conformance Tool Frontend) pick up the new
`org.icann.rdap-conformance:tool` version from Artifactory. C4 diagrams are published alongside
when they change. No container image or Helm chart is published by Jenkins today.

To ship a new version through Jenkins:

1. Bump `rdap-conformance.version` in the parent `pom.xml`.
2. Merge to `master`. Jenkins runs `clean deploy`; verify the artefact in Artifactory and the
   `ts-eng-builds` notification.

### GitHub Actions (public releases)

| Workflow | Trigger | Steps |
|---|---|---|
| `.github/workflows/main.yml` | push / PR to `master` | JDK 21, `mvn -B package` |
| `.github/workflows/release.yml` | tag `v*.*.*` | JDK 21, `mvn -B package`, publish `tool/target/rdapct-*.jar` and `tool/bin/rdapct_config.json` as a GitHub Release |

### Code quality

- SonarQube project `ICANN` (Jenkins stage above).
- Pull requests must follow `.github/pull_request_template.md` (branch naming, Conventional Commits).

## Project Structure

```
rdap-conformance-tool/
├── pom.xml                 # parent; rdap-conformance.version
├── jitpack/                # POM module for everit json-schema
├── validator/              # validation engine library
│   └── src/main/java/org/icann/rdapconformance/validator/
├── tool/                   # CLI + RdapWebValidator; builds rdapct-<v>.jar
│   ├── bin/rdapct_config.json
│   └── src/main/resources/logback.xml
├── docs/                   # developer docs: architecture, logging, codes, process flow, schema validator, diagrams/*.puml
├── datasets/               # cached IANA datasets (runtime)
├── results/                # results JSON (runtime)
├── Dockerfile, docker-entrypoint.sh
├── Jenkinsfile
└── .github/workflows/      # main.yml, release.yml
```

## Release Process

All pushes and pull requests to the master branch are built using GitHub Actions. GitHub Actions are also used
to create a new version and put it in a [release](https://github.com/icann/rdap-conformance-tool/releases).

To publish, a release:
1. Update the version in the parent `pom.xml` file.
1. Commit that change.
1. Create a tag with the version number such as `v1.0.1` (must be "vX.X.X").
1. Push the tag to GitHub and a GitHub action will build and release the tool.

## License

 Copyright 2025 Internet Corporation for Assigned Names and Numbers ("ICANN")

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 

## Acknowledgements

This RDAP conformance tool has been developed by Viagénie
(Julien Bernard, Guillaume Blanchet, Marc Blanchet, Pierre Larochelle) under a contract from ICANN.
