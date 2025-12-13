# RDAP Conformance Tool - Process Flow

This document describes the process flow of the RDAP Conformance Tool CLI, from invocation to results output.

## Table of Contents

1. [Overview](#overview)
2. [Entry Point](#entry-point)
3. [Command Line Arguments](#command-line-arguments)
4. [Configuration File Format](#configuration-file-format)
5. [Execution Flow](#execution-flow)
6. [Dataset Loading](#dataset-loading)
7. [QueryContext](#querycontext)
8. [DNS Resolution](#dns-resolution)
9. [HTTP Query Execution](#http-query-execution)
10. [Schema Validation](#schema-validation)
11. [SSL/TLS Validation](#ssltls-validation)
12. [Profile Validation](#profile-validation)
13. [Profile Differences (2019 vs 2024)](#profile-differences-2019-vs-2024)
14. [Multiple Validation Rounds](#multiple-validation-rounds)
15. [Results Collection](#results-collection)
16. [Exit Codes](#exit-codes)
17. [Web API Entry Point (RdapWebValidator)](#web-api-entry-point-rdapwebvalidator)
18. [Key Design Patterns](#key-design-patterns)

## Overview

The RDAP Conformance Tool validates RDAP (Registration Data Access Protocol) server responses against RFC specifications and ICANN gTLD policies. The tool:

1. Downloads IANA reference datasets
2. Makes HTTP queries to the target RDAP server
3. Validates responses against JSON schemas
4. Validates responses against profile requirements (2019 or 2024)
5. Generates a JSON results file with errors, warnings, and notes

```
CLI Args --> RdapConformanceTool.call()
                    |
                    v
        Load Datasets (13 parallel downloads)
                    |
                    v
        Create QueryContext (central state holder)
                    |
                    v
            Determine Query Type
                    |
                    v
        DNS Resolution (if HTTP query)
                    |
                    v
    For each (IPv4/IPv6) x (application/json, application/rdap+json):
        |-- Make HTTP Request
        |-- Schema Validation
        |-- Profile Validation
                    |
                    v
            Aggregate Results
                    |
                    v
        Generate JSON Results File
                    |
                    v
            Return Exit Code
```

## Entry Point

**File**: `tool/src/main/java/org/icann/rdapconformance/tool/Main.java`

The `Main` class is the entry point. It:

1. Creates a `RdapConformanceTool` instance
2. Uses picocli to parse command-line arguments via `CommandLine`
3. Validates user input through `UserInputValidator.parseOptions()`
4. Executes the tool via `commandLine.execute(args)`

```java
public static void main(String[] args) {
    RdapConformanceTool tool = new RdapConformanceTool();
    CommandLine commandLine = new CommandLine(tool);
    // ... argument parsing and execution
}
```

## Command Line Arguments

**File**: `tool/src/main/java/org/icann/rdapconformance/tool/RdapConformanceTool.java`

The tool uses picocli `@Command` and `@Option` annotations for argument parsing.

### Required Arguments

| Argument | Description |
|----------|-------------|
| `RDAP_URI` | The URI to validate (positional argument) |
| `-c, --config` | Path to the definition/configuration file |

### Optional Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `--timeout` | 20s | HTTP connection timeout in seconds |
| `--maximum-redirects` | 3 | Maximum HTTP redirects to follow |
| `--use-local-datasets` | false | Use cached datasets instead of downloading |
| `--results-file` | auto-generated | Output file path |
| `--gtld-registrar` | false | Enable gTLD Registrar profile |
| `--gtld-registry` | false | Enable gTLD Registry profile |
| `--thin` | false | TLD uses thin registration model |
| `--use-rdap-profile-february-2019` | false | Use 2019 RDAP profile |
| `--use-rdap-profile-february-2024` | false | Use 2024 RDAP profile |
| `--no-ipv4-queries` | false | Skip IPv4 queries |
| `--no-ipv6-queries` | false | Skip IPv6 queries |
| `-v, --verbose` | false | Enable verbose logging |
| `--logging` | CLI | Logging level (CLI, INFO, DEBUG, ERROR, VERBOSE) |
| `--dns-resolver` | system | Custom DNS server IP address |

## Configuration File Format

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/configuration/ConfigurationFile.java`

The configuration file (`-c, --config`) is a JSON file with the following structure:

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `definitionIdentifier` | string | Yes | Identifier for this configuration |
| `definitionError` | array | No | Error code customizations with `code` and `notes` fields |
| `definitionWarning` | array | No | Warning code customizations with `code` and `notes` fields |
| `definitionIgnore` | array | No | Array of error codes to move to "ignore" in results |
| `definitionNotes` | array | No | Notes to include verbatim in results file |

### Example Configuration

```json
{
  "definitionIdentifier": "Standard gTLD RDAP Server Conformance",
  "definitionIgnore": [-10601, -10701, -11201],
  "definitionNotes": ["This conformance configuration is typical of gTLD RDAP server needs."]
}
```

### Result Filtering

Error codes listed in `definitionIgnore` are still validated but moved to the `ignore` section of the results file instead of `error`. This allows tracking validation issues without treating them as failures.

## Execution Flow

**File**: `tool/src/main/java/org/icann/rdapconformance/tool/RdapConformanceTool.java`

The `call()` method (lines 360-726) orchestrates the entire validation workflow:

### Step 1: Logging Configuration

- Configures logback based on verbose flag and logging level
- Sets SSL/TLS properties (CRL/OCSP checking, TLS 1.3 support)

### Step 2: Initialize Progress Tracking

Creates `ProgressTracker` with estimated steps:
- Dataset operations: 13 datasets x 2 operations (download + parse)
- DNS resolution: 2 steps
- Network validation: 2-4 rounds based on IP versions x ~75 validations per round
- Results generation: 1 step

### Step 3: Load IANA Datasets

See [Dataset Loading](#dataset-loading) section.

### Step 4: Verify Configuration File

- Checks if config file exists
- Parses and validates the configuration file
- Returns error if invalid

### Step 5: Create QueryContext

See [QueryContext](#querycontext) section.

### Step 6: Determine Query Type

Analyzes the URI to determine the query type:
- `DOMAIN` - Domain lookup
- `NAMESERVER` - Nameserver lookup
- `ENTITY` - Entity lookup
- `AUTNUM` - Autonomous number lookup
- `IP_NETWORK` - IP network lookup
- `HELP` - Help endpoint
- Search queries (domain, nameserver, entity)

### Step 7: Execute Validation

Based on URI scheme:
- **HTTP/HTTPS**: Creates `RDAPHttpValidator` for network queries
- **File path**: Creates `RDAPFileValidator` for local file validation

## Dataset Loading

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/workflow/rdap/RDAPDatasetService.java`

The tool downloads and parses 13 IANA reference datasets:

| Dataset | Description |
|---------|-------------|
| `ipv4AddressSpace` | IANA IPv4 address space allocations |
| `ipv6AddressSpace` | IANA IPv6 address space allocations |
| `specialIPv4Addresses` | Special-purpose IPv4 addresses |
| `specialIPv6Addresses` | Special-purpose IPv6 addresses |
| `RDAPExtensions` | Registered RDAP extensions |
| `linkRelations` | IANA link relation types |
| `mediaTypes` | IANA media types |
| `dsRrTypes` | DNS DS RR types |
| `dnsSecAlgNumbers` | DNSSEC algorithm numbers |
| `bootstrapDomainNameSpace` | RDAP bootstrap service registry |
| `registrarId` | IANA registrar IDs |
| `EPPROID` | EPP repository IDs |
| `RDAPJSONValues` | RDAP JSON value registrations |

Datasets are downloaded in parallel using a thread pool (2 threads per core, max 8 threads).

### Dataset Caching

When `--use-local-datasets` is specified, the tool uses previously downloaded datasets from the `datasets/` directory instead of downloading fresh copies.

## QueryContext

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/QueryContext.java`

The `QueryContext` is the central "world object" that encapsulates all stateful components during validation.

### Immutable Components

| Component | Description |
|-----------|-------------|
| `queryId` | Unique UUID for this validation run |
| `configuration` | Parsed configuration settings |
| `datasetService` | Access to loaded IANA datasets |
| `rdapQuery` | The query implementation (HTTP or File) |
| `queryType` | Type of RDAP query being validated |

### Mutable State Holders

| Component | Description |
|-----------|-------------|
| `RDAPValidatorResults` | Accumulates validation errors and warnings |
| `ConnectionTracker` | Tracks HTTP connections and their status |
| `RDAPValidationResultFile` | Manages results file generation |
| `DNSCacheResolver` | DNS resolution with caching |
| `HttpClientManager` | HTTP client lifecycle management |

## DNS Resolution

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/DNSCacheResolver.java`

For HTTP/HTTPS queries, the tool resolves the hostname to IP addresses:

1. Resolves hostname to IPv4 addresses (A records)
2. Resolves hostname to IPv6 addresses (AAAA records)
3. Validates at least one IP address exists
4. Caches results to avoid repeated DNS queries

### Custom DNS Server

Use `--dns-resolver` to specify a custom DNS server:
```bash
java -jar rdapct.jar --dns-resolver 8.8.8.8 ...
```

Supports both IPv4 and IPv6 DNS servers.

## HTTP Query Execution

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/workflow/rdap/http/RDAPHttpQuery.java`

### Request Flow

1. **Select IP Stack**: Choose IPv4 or IPv6 based on configuration and availability
2. **Configure Local Bind**: Set local address for outbound connection
3. **Build Request**: Create HTTP GET request with appropriate headers
4. **Execute**: Send request with timeout handling
5. **Follow Redirects**: Up to maximum configured redirects
6. **Track Connection**: Log connection details for debugging

### Accept Headers

The tool makes requests with two different Accept headers:
- `application/json` - Generic JSON response
- `application/rdap+json` - RDAP-specific JSON response

Both must return valid RDAP responses.

### HEAD Request Comparison

The tool also makes a HEAD request to verify it returns the same HTTP status code as the GET request. This validates TIG Section 1.6 requirements.

**Error Code**: -20300 if HEAD and GET status codes differ

### Redirect Handling

The tool follows HTTP redirects with security validation:

- **Supported codes**: 301, 302, 303, 307, 308
- **Redirect limit**: Maximum redirects configurable (default: 3), error -13013 if exceeded
- **Resolution**: Relative redirects resolved against current URI; absolute redirects used as-is
- **Security check**: Error -13004 if redirect blindly copies query parameters from the original request (prevents redirect chain injection attacks)

### Response Validation

1. Check HTTP status code (expects 200 or 404 for error responses)
2. Validate Content-Type header (must be JSON-based)
3. Parse JSON response body
4. Validate `rdapConformance` field exists
5. Validate query-type-specific fields

## Schema Validation

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/SchemaValidator.java`

The tool validates responses against JSON schemas located in `json-schema/` directory.

### Schema Files

| Query Type | Schema File |
|------------|-------------|
| Domain | `rdap_domain.json` |
| Nameserver | `rdap_nameserver.json` |
| Entity | `rdap_entity.json` |
| IP Network | `rdap_ip_network.json` |
| Autnum | `rdap_autnum.json` |
| Help | `rdap_help.json` |
| Error | `rdap_error.json` |

### Custom Format Validators

The schema validator uses custom format validators:

| Validator | Purpose |
|-----------|---------|
| `Ipv4FormatValidator` | Validates IPv4 against IANA address space |
| `Ipv6FormatValidator` | Validates IPv6 against IANA address space |
| `DatasetValidator` | Validates values against RDAP datasets |
| `IdnHostNameFormatValidator` | Validates IDN hostnames |
| `HostNameInUriFormatValidator` | Validates URIs |
| `RdapExtensionsFormatValidator` | Validates RDAP extensions |

## SSL/TLS Validation

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/workflow/profile/tig_section/general/DefaultSSLValidator.java`

For HTTPS connections, the tool performs SSL/TLS validation as part of the TIG (Technical Implementation Guide) requirements.

### Certificate Validation

- Uses system default trust store via `SSLContext.getDefault()`
- Validates full certificate chain to a trusted root CA
- Performs hostname verification using SNI (Server Name Indication)
- 5-second connection timeout for SSL handshake

### Protocol Validation

- Validates enabled TLS protocols on the server
- Specifically validates TLS 1.2 support and cipher suites
- IPv4/IPv6 aware - resolves addresses based on current network stack

### Related Error Codes

| Code | Description |
|------|-------------|
| -20100 | SSL/TLS connection failed |
| -20101 | Certificate validation failed |
| -20102 | TLS protocol version not supported |

## Profile Validation

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/workflow/profile/ProfileValidation.java`

After schema validation, the tool runs profile-specific validations based on the selected RDAP profile (2019 or 2024).

### Validation Categories

- **General validations**: TIG sections 3.2, 4.1, 7.1-7.2
- **Response validations**: Sections 1.x, 2.x
- **Domain validations**: Response 2.x.x series
- **Entity validations**: Response 3.x
- **Nameserver validations**: Response 4.x
- **vCard validations**: Contact information
- **SSL/TLS validations**: Certificate and protocol checks
- **DNS validations**: DNS-related requirements
- **Link validations**: Link structure and values

### Validation Execution Pattern

Each `ProfileValidation` implementation:

```java
1. Check doLaunch() - determines if validation should run
2. Add validation group name to results
3. Execute doValidate() - actual validation logic
4. Catch exceptions and report as group error
```

## Profile Differences (2019 vs 2024)

The tool supports two RDAP profiles with significant differences:

### 2024 Profile Additions

The 2024 profile includes stricter requirements:

| Error Code | Description |
|------------|-------------|
| -12107 | `errorCode` required in error responses |
| -12108 | `errorCode` must match HTTP status code |

Additional 2024 requirements:
- Stricter SSL/TLS validation (TIG 1.5)
- Updated vCard/jCard requirements for entities
- URI security validation for links
- Enhanced status value validation

### 2024 Profile Removals

The following 2019 validations were removed or replaced:
- TIG 1.14 validation
- Response 1.3 validation
- Generic notices requirement (replaced with specific notice validations)
- Several entity validations (replaced with stricter 2024 versions)

### Simultaneous Profile Testing

Both profiles can be enabled simultaneously using:
```bash
--use-rdap-profile-february-2019 --use-rdap-profile-february-2024
```

This allows comparative testing to identify differences in validation results between profiles.

## Multiple Validation Rounds

For HTTP queries, the tool runs multiple validation rounds to ensure compliance across different scenarios:

### IPv6 Round (if enabled and addresses resolved)

1. Set IP stack to IPv6
2. Request with `Accept: application/json` -> validate
3. Request with `Accept: application/rdap+json` -> validate

### IPv4 Round (if enabled and addresses resolved)

1. Set IP stack to IPv4
2. Request with `Accept: application/json` -> validate
3. Request with `Accept: application/rdap+json` -> validate

Each round is a complete validation including schema and profile checks. This ensures the server responds correctly regardless of:
- IP version (IPv4 vs IPv6)
- Accept header (generic JSON vs RDAP-specific)

## Results Collection

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/workflow/rdap/RDAPValidationResultFile.java`

### Result Structure

Each validation result contains:

| Field | Description |
|-------|-------------|
| `code` | Error/warning code (negative for errors) |
| `value` | The problematic value or context |
| `message` | Human-readable description |
| `acceptMediaType` | Accept header used for this request |
| `httpMethod` | HTTP method (GET, HEAD) |
| `serverIpAddress` | Server IP address used |
| `receivedHttpStatusCode` | HTTP status code received |
| `queriedURI` | The URI that was queried |

### Result Categories

| Category | Description |
|----------|-------------|
| `error` | Critical validation failures |
| `warning` | Non-critical issues |
| `ignore` | Filtered results based on configuration |
| `notes` | Informational messages |

### Output File Format

Results are written to a JSON file with timestamp: `results-YYYYMMDDHHmmss.json`

```json
{
    "testedDate": "2025-01-15T10:30:00Z",
    "testedURI": "https://rdap.example.com/domain/example.com",
    "definitionIdentifier": "Standard gTLD RDAP Server Conformance",
    "rdapProfileFebruary2024": true,
    "rdapProfileFebruary2019": false,
    "gtldRegistry": true,
    "gtldRegistrar": false,
    "thinRegistry": false,
    "noIpv4": false,
    "noIpv6": false,
    "groupOK": ["stdRdapConformanceValidation", ...],
    "groupErrorWarning": ["stdRdapErrorResponseBodyValidation", ...],
    "results": {
        "error": [...],
        "warning": [...],
        "ignore": [...],
        "notes": [...]
    }
}
```

## Exit Codes

**File**: `validator/src/main/java/org/icann/rdapconformance/validator/ToolResult.java`

| Code | Name | Description |
|------|------|-------------|
| 0 | SUCCESS | Validation completed successfully |
| 1 | BAD_USER_INPUT | Invalid command-line arguments |
| 2 | CONFIG_DOES_NOT_EXIST | Configuration file not found |
| 2 | CONFIG_INVALID | Configuration file is invalid |
| 3 | UNSUPPORTED_QUERY | Query type not supported |
| 4 | DATASET_UNAVAILABLE | Failed to download required datasets |
| 5 | FILE_WRITE_ERROR | Failed to write results file |
| 10 | USES_THIN_MODEL | TLD uses thin registration model |

## Web API Entry Point (RdapWebValidator)

**File**: `tool/src/main/java/org/icann/rdapconformance/tool/RdapWebValidator.java`

For web applications (like the RDAP Conformance Tool Frontend), the `RdapWebValidator` class provides a thread-safe, embeddable entry point.

### Key Differences from CLI

| Aspect | CLI (RdapConformanceTool) | Web (RdapWebValidator) |
|--------|---------------------------|------------------------|
| Entry | `main()` with picocli | Constructor with config object |
| State | Modifies global logging/TLS properties | No global state modification |
| Output | Writes JSON file to disk | Returns `RDAPValidatorResults` in memory |
| Lifecycle | Single run, exits with code | `AutoCloseable`, reusable |
| Thread Safety | Not designed for concurrent use | Thread-safe, isolated state |

### Configuration Options

`RdapWebValidator` accepts configuration through two inner classes:

**SimpleRDAPValidatorConfiguration**: Basic settings
- `uri` - The RDAP URI to validate
- `definitionFile` - Path to configuration file
- `timeout` - HTTP timeout in seconds (default: 20)
- `maxRedirects` - Maximum redirects (default: 3)
- `useLocalDatasets` - Use cached datasets (default: false)
- `gtldRegistrar` / `gtldRegistry` - Enable gTLD profiles
- `thin` - Thin registration model
- `noIpv4Queries` / `noIpv6Queries` - Disable IP version queries

**ConfigurableRDAPValidatorConfiguration**: Extended settings
- All of `SimpleRDAPValidatorConfiguration` plus:
- `rdapProfile2019` / `rdapProfile2024` - Profile selection
- `tempDir` - Custom temporary directory

### Validation Flow

The validation flow is identical to the CLI:

1. **Dataset Loading**: Downloads/loads IANA datasets (parallel)
2. **QueryContext Creation**: Initializes central state holder
3. **Validation Rounds**: For each enabled IP version:
   - Request with `Accept: application/json` -> validate
   - Request with `Accept: application/rdap+json` -> validate
4. **Results Aggregation**: Collects errors, warnings, notes

### Usage Example

```java
RdapWebValidator.SimpleRDAPValidatorConfiguration config =
    new RdapWebValidator.SimpleRDAPValidatorConfiguration();
config.setUri("https://rdap.example.com/domain/example.com");
config.setDefinitionFile("/path/to/config.json");
config.setGtldRegistry(true);
config.setRdapProfile2024(true);

try (RdapWebValidator validator = new RdapWebValidator(config)) {
    RDAPValidatorResults results = validator.validate();
    // Process results in memory
}
```

### Resource Management

- Implements `AutoCloseable` for proper resource cleanup
- Creates temporary directory for dataset caching (cleaned up on close)
- HTTP client connections managed via `HttpClientManager`

## Key Design Patterns

### QueryContext Pattern

Central holder for all validation state. Ensures thread safety and isolation between validation runs.

### Builder Pattern

`RDAPValidationResult` uses builder pattern for flexible object construction with many optional fields.

### Strategy Pattern

Different validators (`RDAPHttpValidator`, `RDAPFileValidator`) implement the `ValidatorWorkflow` interface, allowing the tool to validate both network endpoints and local files.

### Template Method Pattern

`ProfileValidation` abstract class provides `doValidate()` hook method that subclasses implement for specific validation logic.

### Chain of Responsibility

Sequential validation passes accumulate results, with each validation adding its findings to the shared results object.

## Related Documentation

- [Error Codes Reference](codes.md) - Complete list of validation error codes
- [Schema Validator](schema_validator.md) - Details on JSON schema validation
- [README](README.md) - Documentation index
