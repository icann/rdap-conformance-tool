# RDAP SchemaValidator System Documentation

## Overview

The RDAP SchemaValidator system is a comprehensive JSON schema validation framework designed specifically for RDAP (Registration Data Access Protocol) responses. It combines JSON Schema Draft-07 validation with custom format validators, dataset validators, and specialized exception parsers to provide thorough validation of RDAP data structures against both syntactic and semantic rules.

## System Architecture

### Core Components

1. **SchemaValidator.java**: Main validation engine that orchestrates schema loading, validation execution, and error processing
2. **SchemaValidatorCache.java**: Caching system for compiled schema objects to improve performance
3. **JSON Schema Files**: 44+ schema definition files in `validator/src/main/resources/json-schema/`
4. **Custom Format Validators**: 6 specialized validators for RDAP-specific formats
5. **Exception Parser System**: 22+ parsers that convert validation failures into structured error codes
6. **Dataset Validators**: IANA registry compliance validators

### Validation Flow

1. **Content Parsing**: JSON content parsed with duplicate key detection
2. **Schema Loading**: Appropriate schema loaded with custom format validators attached
3. **Primary Validation**: JSON Schema validation executed with custom validators
4. **Exception Processing**: All validation exceptions processed through specialized parser system  
5. **Special Validations**: Additional checks for event uniqueness, vCard structure, etc.
6. **Error Reporting**: Structured error results with codes, messages, and JSON pointer locations

## JSON Schema Files

### Core RDAP Object Schemas

#### `rdap_domain.json`
- **Purpose**: Validates domain lookup responses
- **Required Properties**: `objectClassName`, `ldhName` 
- **Optional Properties**: `unicodeName`, `nameservers`, `secureDNS`, `entities`, `status`, `events`, `variants`, `publicIds`, `remarks`, `links`, `port43`, `lang`
- **Error Code Range**: -12200 to -12219
- **Key Validations**:
  - objectClassName must equal "domain"
  - ldhName must be valid LDH domain name format
  - unicodeName must be valid IDN hostname format
  - nameservers array validation
  - secureDNS key data and DS data validation

#### `rdap_entity.json`
- **Purpose**: Validates entity lookup responses  
- **Required Properties**: `objectClassName`
- **Optional Properties**: `handle`, `vcardArray`, `roles`, `publicIds`, `entities`, `remarks`, `links`, `events`, `asEventActor`, `status`, `port43`, `lang`
- **Error Code Range**: -12300 to -12319
- **Key Validations**:
  - objectClassName must equal "entity"
  - vcardArray must conform to jCard format
  - roles must be valid IANA role values
  - handle string validation

#### `rdap_nameserver.json`
- **Purpose**: Validates nameserver objects
- **Required Properties**: `objectClassName`, `ldhName`
- **Optional Properties**: `unicodeName`, `ipAddresses`, `entities`, `status`, `events`, `remarks`, `links`, `port43`, `lang`
- **Error Code Range**: -12400 to -12416
- **Key Validations**:
  - objectClassName must equal "nameserver"
  - ldhName must be valid hostname
  - IPv4/IPv6 address validation in ipAddresses

#### `rdap_autnum.json`
- **Purpose**: Validates autonomous system number objects
- **Required Properties**: `objectClassName`, `startAutnum`, `endAutnum`
- **Optional Properties**: `name`, `type`, `country`, `entities`, `status`, `events`, `remarks`, `links`, `port43`, `lang`
- **Error Code Range**: -12300 to -12314
- **Key Validations**:
  - objectClassName must equal "autnum"
  - startAutnum and endAutnum must be valid integers
  - country must be valid ISO 3166-1 alpha-2 code

#### `rdap_ip_network.json`
- **Purpose**: Validates IP network objects
- **Required Properties**: `objectClassName`, `startAddress`, `endAddress`, `ipVersion`
- **Optional Properties**: `name`, `type`, `country`, `parentHandle`, `entities`, `status`, `events`, `remarks`, `links`, `port43`, `lang`
- **Key Validations**:
  - objectClassName must equal "ip network"
  - IP address format validation for startAddress and endAddress
  - ipVersion must be 4 or 6

### Supporting Schemas

#### `rdap_domain_name.json`
- **Purpose**: Validates domain names for RDAP queries before processing
- **Error Code Range**: -10300 to -10303
- **Key Validations**:
  - -10300: Label length validation (1-63 characters per label)
  - -10301: Total domain name length validation (max 253 characters)
  - -10302: Minimum label count validation (at least two labels required)
  - -10303: IDNA label format validation (valid A-label, U-label, or NR-LDH label)
- **Example**: Domain "zz--main-1234" fails with errors -10302 and -10303 due to single label format and invalid IDNA hyphen positioning

#### `rdap_common.json`
- **Purpose**: Central definitions for shared RDAP components
- **Key Definitions**:
  - URI validation with http/https scheme requirements (-10400 series)
  - Language tag validation (-10800 series)
  - Link structure validation (-10500 to -10599)
  - Notice/remark definitions (-10600 to -10699)
  - Event structure validation (-10900 to -10912)
  - Status value validation (-11000 to -11002)
  - Role validation (-11800 to -11803)
  - LDH name validation (-11700 to -11703)

#### `rdap_error.json`
- **Purpose**: Validates RDAP error response structure
- **Required Properties**: `errorCode`, `title`, `description`
- **Error Code Range**: -12100 to -12106
- **Key Validations**:
  - errorCode must be numeric
  - title and description must be strings
  - Optional notices array validation

#### `rdap_help.json`
- **Purpose**: Validates help query responses
- **Error Code Range**: -12500 to -12505
- **Key Validations**:
  - Standard notices/remarks validation
  - Conformance array validation

#### `rdap_secureDNS.json`
- **Purpose**: Validates DNSSEC information
- **Error Code Range**: -12000 to -12025
- **Key Validations**:
  - DS data records (keyTag, algorithm, digestType, digest)
  - Key data records (flags, protocol, algorithm, publicKey)
  - maxSigLife validation
  - zoneSigned boolean validation

### Specialized Schemas

#### `jcard.json`
- **Purpose**: Validates vCard 4.0 contact information structure
- **Error Code Range**: -20800+ series
- **Key Validations**:
  - vCard property array structure
  - Property parameters validation
  - Value type validation (text, uri, date-time, etc.)
  - Multi-value property handling

#### Test Schemas
- **`test_rdap_*.json`**: Validation group definitions for compound testing
- **Purpose**: Define validation groups that trigger multiple related validations
- **Examples**: `test_rdap_events.json`, `test_rdap_links.json`, `test_rdap_status.json`

## Custom Format Validators

### DatasetValidator
- **Format Names**: `linkRelations`, `mediaTypes`, `noticeAndRemark`, `eventAction`, `status`, `redactedExpressionLanguage`, `redactedName`, `variantRelation`, `role`
- **Purpose**: Validates values against IANA registry datasets
- **Validation**: Ensures submitted values exist in the appropriate IANA registry
- **Error Handling**: Returns generic dataset violation errors

### Ipv4FormatValidator  
- **Format Name**: `ipv4-validation`
- **Purpose**: Validates IPv4 addresses against IANA IPv4 Address Space Registry
- **Validation Rules**:
  - Address must be in ALLOCATED or LEGACY status in IANA registry
  - Address must not be in Special-Purpose Address Registry (unless explicitly allowed)
  - Proper IPv4 address format validation
- **Error Codes**: -10100 to -10102

### Ipv6FormatValidator
- **Format Name**: `ipv6-validation` 
- **Purpose**: Validates IPv6 addresses against IANA IPv6 Address Space Registry
- **Validation Rules**:
  - Address must be categorized as Global Unicast in IANA registry
  - Address must not be in Special-Purpose Address Registry (unless explicitly allowed)
  - Proper IPv6 address format validation
- **Error Codes**: -10200 to -10202

### IdnHostNameFormatValidator
- **Format Name**: `idn-hostname`
- **Purpose**: Validates Internationalized Domain Names (IDN)
- **Validation Rules**:
  - Supports both ASCII and Unicode domain names
  - Validates proper IDN encoding (A-labels vs U-labels)
  - Hostname length and structure validation
  - Label length limits (63 characters per label, 253 total)

### HostNameInUriFormatValidator
- **Format Name**: `hostname-in-uri`
- **Purpose**: Validates hostnames within URI contexts
- **Validation Rules**:
  - Domain name validation using IdnHostNameFormatValidator
  - IPv4 address validation using Ipv4FormatValidator
  - IPv6 address validation using Ipv6FormatValidator
  - Proper URI hostname format requirements

### RdapExtensionsFormatValidator
- **Format Name**: `rdapExtensions`
- **Purpose**: Validates RDAP extension identifiers
- **Validation Rules**:
  - Validates against IANA RDAP Extensions registry
  - Special case: "rdap_level_0" is always considered valid
  - Extension identifier format validation

## Error Code Structure

### Error Code Ranges and Meanings

#### -10xxx Series: Common/Shared Validation Errors
- **-10100 to -10102**: IPv4 address validation errors
- **-10200 to -10202**: IPv6 address validation errors  
- **-10300 to -10303**: Domain name validation errors (label length, domain length, label count, IDNA format)
- **-10400 to -10499**: URI validation errors
- **-10500 to -10599**: Link structure validation errors
- **-10600 to -10699**: Notice/remark validation errors
- **-10700 to -10799**: Public ID validation errors
- **-10800 to -10899**: Language tag validation errors
- **-10900 to -10912**: Event validation errors

#### -11xxx Series: Specific Property Validation
- **-11000 to -11099**: Status validation errors
- **-11100 to -11199**: Port43 validation errors
- **-11200 to -11299**: Conformance validation errors
- **-11300 to -11399**: AsEventActor validation errors
- **-11400 to -11499**: Unicode name validation errors
- **-11500 to -11599**: Handle validation errors
- **-11600 to -11699**: Country validation errors
- **-11700 to -11799**: LDH name validation errors
- **-11800 to -11899**: Role validation errors

#### -12xxx Series: Object-Specific Validation Errors
- **-12000 to -12099**: SecureDNS validation errors
- **-12100 to -12199**: Error response validation errors
- **-12200 to -12299**: Domain object validation errors
- **-12300 to -12399**: Entity/autnum/IP network validation errors
- **-12400 to -12499**: Nameserver validation errors
- **-12500 to -12599**: Help response validation errors

#### -20xxx Series: vCard/jCard Validation
- **-20800+**: Complex vCard property validation errors

#### Special Error Codes
- **-999**: Unknown error fallback when no specific error code can be determined

### Error Code Assignment Patterns

Each object type follows standardized error code patterns:

- **`structureInvalid`**: Basic JSON structure issues (first error code in range)
- **`unknownKeys`**: Additional properties not allowed
- **`duplicateKeys`**: Duplicate JSON keys detected
- **`*Missing`**: Required properties missing (e.g., "objectClassNameMissing")
- **`std*Validation`**: Validation group errors (e.g., "stdRdapLinksValidation")

## Exception Parser System

The system uses 22+ specialized exception parsers to convert JSON Schema validation exceptions into structured error results:

### Core Parsers

#### BasicTypeExceptionParser
- **Purpose**: Handles fundamental type mismatches
- **Covers**: string, boolean, null, integer, number type violations
- **Error Assignment**: Extracts error codes from schema definitions

#### EnumExceptionParser  
- **Purpose**: Validates enum value compliance
- **Covers**: Values that must match predefined enum sets
- **Error Assignment**: Schema-based error code lookup

#### MissingKeyExceptionParser
- **Purpose**: Detects missing required properties
- **Covers**: Required property violations
- **Error Pattern**: Typically "*Missing" error codes

#### UnknownKeyExceptionParser
- **Purpose**: Catches additional properties violations  
- **Covers**: Properties not defined in schema when additionalProperties=false
- **Error Pattern**: "unknownKeys" error codes

#### ConstExceptionParser
- **Purpose**: Validates constant values
- **Covers**: Properties that must equal specific constant values (like objectClassName)
- **Error Assignment**: Const violation error codes

### Specialized Parsers

#### IP Address Parsers
- **Ipv4ValidationExceptionParser**: IPv4 address validation failures
- **Ipv6ValidationExceptionParser**: IPv6 address validation failures
- **Error Codes**: -10100 series (IPv4), -10200 series (IPv6)

#### Hostname Parsers  
- **IdnHostNameExceptionParser**: Domain name validation failures
- **HostNameInUriExceptionParser**: URI hostname validation failures
- **Integration**: Works with custom format validators

#### Dataset Parser
- **DatasetExceptionParser**: IANA dataset validation failures
- **Covers**: All dataset format validators (linkRelations, mediaTypes, etc.)
- **Error Assignment**: Generic dataset violation codes

#### vCard Parser
- **VcardExceptionParser**: Complex vCard structure validation
- **Error Codes**: -20800+ series
- **Covers**: vCard property array structure violations

#### DateTime Parser
- **DatetimeExceptionParser**: RFC3339 datetime validation
- **Covers**: Event dates and other datetime fields
- **Standards**: ISO 8601/RFC3339 compliance

### Parser Workflow

1. **Exception Creation**: Each validation failure creates ValidationException instances
2. **Parser Instantiation**: System creates all applicable parser instances for each exception
3. **Pattern Matching**: Each parser checks if it matches the specific exception characteristics
4. **Error Code Resolution**: Matching parsers extract appropriate error codes from schema definitions
5. **Message Generation**: Parsers create human-readable error messages with context
6. **Group Validation**: Some parsers trigger additional validation group tests
7. **Result Building**: Structured RDAPValidationResult objects created with codes, values, and messages

## Special Validation Logic

### Event Action Uniqueness Validation
- **Purpose**: Ensures each eventAction appears only once per events array
- **Error Codes**: -10912 (events), -11310 (asEventActor)
- **Implementation**: Custom validation beyond JSON schema capabilities
- **Scope**: Applied to both "events" and "asEventActor" arrays

### vCard Array Validation  
- **Purpose**: Complex vCard 4.0 property validation beyond basic JSON schema
- **Implementation**: VcardArrayGeneralValidation class
- **Scope**: Triggered when "vcardArray" detected in content
- **Standards**: vCard 4.0 RFC compliance

### Top-Most Notices Validation
- **Purpose**: Validates notice structures at the top level of responses
- **Implementation**: NoticesTopMostValidation class  
- **Scope**: Triggered when "notices" detected in content
- **Integration**: Uses schema root node for validation context

### Validation Groups
- **Purpose**: Enable compound validation across multiple schema elements
- **Implementation**: Schema elements can belong to validation groups
- **Examples**: "stdRdapLinksValidation", "stdRdapEventsValidation", "stdRdapStatusValidation"
- **Benefit**: Provides higher-level validation context and error grouping

## Performance Optimizations

### Caching Systems
- **Schema Compilation Cache**: Compiled schema objects cached to avoid expensive recompilation
- **JSON Parsing Cache**: Parsed JSON objects cached for repeated validation of same content
- **Regex Pattern Cache**: Compiled regex patterns cached for performance
- **Cache Management**: LRU eviction and size limits prevent memory leaks

### Validation Optimizations
- **Early Termination**: Validation stops on first critical errors where appropriate
- **Lazy Loading**: Datasets and schemas loaded on-demand
- **Concurrent Safe**: Thread-safe caching for multi-threaded validation

## Integration with RDAP Conformance Tool

### Validation Triggers
The SchemaValidator is primarily invoked from:
1. **RDAPHttpQueryTypeProcessor**: Domain name pre-validation before RDAP queries (generates -10300 series errors)
2. **RDAPValidator**: Full RDAP response validation after retrieval
3. **Profile-specific validators**: TIG section validations for IP addresses

### Error Reporting Integration
- **RDAPValidationResult**: Structured error objects with codes, messages, and context
- **RDAPValidatorResults**: Collection and management of validation results
- **JSON Pointer Paths**: Exact error location identification within JSON structures
- **ToolResult Status**: High-level validation status (SUCCESS, BAD_USER_INPUT, etc.) for calling applications
- **Group Testing**: Validation group results for comprehensive error reporting

### Dataset Integration
- **IANA Registries**: Real-time validation against IPv4/IPv6 address space, media types, link relations
- **Extension Registries**: RDAP extension identifier validation
- **Event Actions**: Valid RDAP event action validation
- **Status Values**: RDAP status value compliance checking

This comprehensive validation system ensures RDAP responses comply with both syntactic JSON schema requirements and semantic RDAP protocol specifications, providing detailed error reporting for non-compliant data structures.