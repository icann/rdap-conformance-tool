# RDAP Conformance Tool - Error Codes Analysis

## Overview

The RDAP Conformance Tool uses negative error codes to identify specific validation violations. All codes follow the pattern `-XXXXX` (negative 4-6 digit numbers). Each error code has been mapped to the relevant RFC sections and RDAP profile requirements.

## RFC Mapping Methodology

Each error code has been systematically mapped to the most appropriate RFC sections and RDAP profile requirements based on:

### Primary RDAP RFCs Referenced:
- **RFC 9083** (JSON Responses for the Registration Data Access Protocol - obsoletes RFC 7483)
- **RFC 9082** (Registration Data Access Protocol (RDAP) Query Format - obsoletes RFC 7482)
- **RFC 7481** (Security Services for the Registration Data Access Protocol)
- **RFC 7480** (HTTP Usage in the Registration Data Access Protocol)
- **RFC 7484** (Finding the Authoritative Registration Data Access Protocol Service)
- **RFC 6350** (vCard Format Specification)
- **RFC 5731** (Extensible Provisioning Protocol Domain Name Mapping)
- **RFC 3915** (Domain Registry Grace Period Mapping for EPP)

### Profile Documents Referenced:
- **RDAP Response Profile 2019**
- **RDAP Response Profile 2024**
- **Technical Implementation Guide (TIG)** - ICANN RDAP Technical Implementation Guide

### Mapping Format:
Error codes are formatted as: `-XXXXX: Description - "Message" [RFC XXXX Section X.X]`

Where applicable, multiple RFC sections and profile requirements are referenced for comprehensive coverage.

## Error Code Ranges Identified

### HTTP Protocol Errors (-13XXX)
- `-13000`: Content-Type header validation - "The content-type header does not contain the application/rdap+json media type." [RFC 7480 Section 4] {RDAPHttpQuery.java}
- `-13001`: Invalid JSON response - "The response was not valid JSON." [RFC 9083 Section 2] {RDAPHttpQuery.java}
- `-13002`: Invalid HTTP status code - "The HTTP status code was neither 200 nor 404." [RFC 7480 Section 5] {RDAPHttpQuery.java}
- `-13003`: Missing objectClassName in response - "The response does not have an objectClassName string." [RFC 9083 Section 4] {RDAPHttpQuery.java}
- `-13004`: Query parameters copied in redirect Location header - "Response redirect contained query parameters copied from the request." [RFC 7480 Section 5.2] {RDAPHttpQuery.java}
- `-13005`: Invalid redirect test (2024 profile) [RFC 7480 Section 5.2, RDAP Response Profile 2024] {ResponseValidationTestInvalidRedirect_2024.java}
- `-13006`: Invalid redirect test (2024 profile) [RFC 7480 Section 5.2, RDAP Response Profile 2024] {ResponseValidationTestInvalidRedirect_2024.java}
- `-13013`: Too many HTTP redirects - "Too many HTTP redirects." [RFC 7480 Section 5.2] {RDAPHttpQuery.java}
- `-13018`: Mixed HTTP status codes in validation results - "Mixed HTTP status codes found in validation results." [RFC 7480 Section 5] {RDAPValidatorResultsImpl.java, RDAPValidationResultFile.java}
- `-13019`: DNS resolution issues - "Unable to resolve an IP address endpoint using DNS." [RFC 7484 Section 3] {DNSCacheResolver.java}

### IP Address Validation (-10XXX)
- `-10100`: IPv4 address validation - "The IPv4 address is not syntactically valid in dot-decimal notation." [RFC 9083 Section 5.2] {Ipv4ValidationExceptionParser.java}
- `-10101`: IPv4 address validation - "The IPv4 address is not allocated or legacy." [RFC 9083 Section 5.2] {Ipv4ValidationExceptionParser.java}
- `-10102`: IPv4 address validation - "The IPv4 address is part of special addresses." [RFC 9083 Section 5.2] {Ipv4ValidationExceptionParser.java}
- `-10200`: IPv6 address validation - "The v6 structure is not syntactically valid." [RFC 9083 Section 5.2] {Ipv6ValidationExceptionParser.java}
- `-10201`: IPv6 address validation - "The IPv6 address is not allocated or legacy." [RFC 9083 Section 5.2] {Ipv6ValidationExceptionParser.java}
- `-10202`: IPv6 address validation - "The IPv6 address is part of special addresses." [RFC 9083 Section 5.2] {Ipv6ValidationExceptionParser.java}

### Domain Name Validation (-103XX)
- `-10300`: Label length validation - "A DNS label with length not between 1 and 63 was found." [RFC 1123 Section 2.1, RFC 5890 Section 2.3.1] {rdap_domain_name.json}
- `-10301`: Domain name length validation - "A domain name of more than 253 characters was found." [RFC 1123 Section 2.1, RFC 5890 Section 2.3.1] {rdap_domain_name.json}
- `-10302`: Label count validation - "A domain name with less than two labels was found." [RFC 1123 Section 2.1, RDAP Technical Implementation Guide Section 1.10] {rdap_domain_name.json}
- `-10303`: Label type validation - "A DNS label not being a valid 'A-label', 'U-label', or 'NR-LDH label' was found." [RFC 5890 Section 2.3.2, IANA IDNA Rules] {rdap_domain_name.json}

### URI and Domain Validation (-104XX)
- `-10400`: URI validation - "URI not syntactically valid per RFC3986." [RFC 3986] {rdap_common.json}
- `-10401`: URI scheme validation - "URI scheme not 'http' or 'https'." [RFC 7480 Section 4] {rdap_common.json}
- `-10402`: URI host validation - "URI host fails validation." [RFC 3986] {rdap_common.json}
- `-10403`: Domain case folding validation - Domain name case folding validation failures [RFC 9082 Section 3.1.3] {DomainCaseFoldingValidation.java}

### RDAP Conformance Validation (-105XX)
- `-10500`: Standard RDAP conformance validation [RFC 9083 Section 4.1] {rdap_common.json, rdap_conformance.json}
- `-10501`: Standard RDAP conformance format validation (not list of strings) [RFC 9083 Section 4.1] {rdap_common.json}
- `-10502`: Standard RDAP conformance enum validation (wrong enum value) [RFC 9083 Section 4.1] {rdap_common.json}
- `-10503`: Standard RDAP conformance validation - "The #/rdapConformance data structure does not include rdap_level_0." [RFC 9083 Section 4.1] {rdap_conformance.json}
- `-10504`: Standard RDAP conformance validation (2024 profile) [RFC 9083 Section 4.1, RDAP Response Profile 2024] {StdRdapConformanceValidation_2024.java}
- `-10505`: Standard RDAP conformance validation (2024 profile) [RFC 9083 Section 4.1, RDAP Response Profile 2024] {StdRdapConformanceValidation_2024.java}

### Links Validation (-106XX)
- `-10603`: Link element validation [RFC 9083 Section 4.2] {rdap_common.json}
- `-10604`: Link structure validation [RFC 9083 Section 4.2] {rdap_common.json}
- `-10605`: Link format validation [RFC 9083 Section 4.2] {rdap_common.json}
- `-10606`: Link properties validation [RFC 9083 Section 4.2] {rdap_common.json}
- `-10607`: Link value validation [RFC 9083 Section 4.2] {rdap_common.json}
- `-10608`: Link media type validation [RFC 9083 Section 4.2] {rdap_common.json}
- `-10612`: Link elements validation (2024 profile) [RFC 9083 Section 4.2, RDAP Response Profile 2024] {ResponseValidationLinkElements_2024.java}
- `-10613`: Link elements validation (2024 profile) [RFC 9083 Section 4.2, RDAP Response Profile 2024] {ResponseValidationLinkElements_2024.java}

### Notices Validation (-107XX)
- `-10703`: Notice validation [RFC 9083 Section 4.3] {rdap_notice.json, rdap_common.json}
- `-10705`: Notice description validation [RFC 9083 Section 4.3] {rdap_common.json}
- `-10706`: Notice structure validation [RFC 9083 Section 4.3] {rdap_common.json}
- `-10708`: Notices validation [RFC 9083 Section 4.3] {rdap_common.json}
- `-10709`: Notice links validation [RFC 9083 Section 4.3] {rdap_notice.json, rdap_common.json}

### Language Validation (-108XX)
- `-10800`: Language validation (regex pattern) [RFC 9083 Section 4.4] {rdap_common.json}

### Event Validation (-109XX)
- `-10904`: Event action validation [RFC 9083 Section 4.5] {rdap_event.json}
- `-10905`: Event action format validation [RFC 9083 Section 4.5] {rdap_event.json}
- `-10907`: Event date validation [RFC 9083 Section 4.5] {rdap_event.json}
- `-10908`: Event date format validation [RFC 9083 Section 4.5] {rdap_event.json}
- `-10909`: Event structure validation [RFC 9083 Section 4.5] {rdap_event.json}
- `-10910`: Event validation [RFC 9083 Section 4.5] {rdap_events.json}

### Schema Validation Errors (-11XXX)

#### Status Validation (-110XX)
- `-11001`: Status validation [RFC 9083 Section 4.6] {rdap_status.json}
- `-11002`: Status format validation [RFC 9083 Section 4.6] {rdap_status.json}
- `-11003`: Status duplication validation (2024 profile) [RFC 9083 Section 4.6, RDAP Response Profile 2024] {ResponseValidationStatusDuplication_2024.java}

#### Port 43 Validation (-111XX)
- `-11100`: Port 43 validation [RFC 9083 Section 4.7] {rdap_port43.json}

#### Public IDs Validation (-112XX)
- `-11200`: Public IDs validation [RFC 9083 Section 4.8] {rdap_publicIds.json}
- `-11201`: Public IDs array structure validation [RFC 9083 Section 4.8] {rdap_publicIds.json}
- `-11203`: Public IDs missing required keys ("type" or "identifier") [RFC 9083 Section 4.8] {rdap_publicIds.json}
- `-11204`: Public IDs "type" field validation (not a string) [RFC 9083 Section 4.8] {rdap_publicIds.json}
- `-11205`: Public IDs "identifier" field validation (not a string) [RFC 9083 Section 4.8] {rdap_publicIds.json}

#### Event Actor Validation (-113XX)
- `-11305`: Event actor validation [RFC 9083 Section 4.5] {rdap_asEventActor_object.json}
- `-11306`: Event actor format validation [RFC 9083 Section 4.5] {rdap_asEventActor_object.json}
- `-11308`: Event actor structure validation [RFC 9083 Section 4.5] {rdap_asEventActor_object.json}
- `-11309`: Event actor properties validation [RFC 9083 Section 4.5] {rdap_asEventActor_object.json}
- `-11310`: Event actor validation [RFC 9083 Section 4.5] {SchemaValidator.java}

#### IP Address Schema Validation (-114XX)
- `-11400`: IP address validation [RFC 9083 Section 5.1, 5.2] {rdap_ipAddress.json}
- `-11401`: IP address authorized keys validation (v4, v6) [RFC 9083 Section 5.2] {rdap_ipAddress.json}
- `-11403`: IP address validation in entities [RFC 9083 Section 5.1] {rdap_ipAddress.json}
- `-11404`: IPv4 address format validation - "The v4 structure is not syntactically valid." [RFC 9083 Section 5.2] {rdap_ipAddress.json}
- `-11405`: IPv4 address format validation (not a string) [RFC 9083 Section 5.2] {rdap_ipAddress.json}
- `-11406`: IPv4 address format validation (invalid value) [RFC 9083 Section 5.2] {rdap_ipAddress.json}
- `-11407`: IPv6 address format validation - "The v6 structure is not syntactically valid." [RFC 9083 Section 5.2] {rdap_ipAddress.json}
- `-11408`: IPv6 address format validation (not a string) [RFC 9083 Section 5.2] {rdap_ipAddress.json}
- `-11409`: IPv6 address validation - "The v6 structure is not syntactically valid." [RFC 9083 Section 5.2] {Ipv6ValidationExceptionParser.java}

#### Variant Validation (-115XX)
- `-11503`: Variant relation validation [RFC 9083 Section 5.3] {rdap_variant.json}
- `-11504`: Variant relation format validation (not list of strings) [RFC 9083 Section 5.3] {rdap_variant.json}
- `-11505`: Variant relation enum validation against datasets [RFC 9083 Section 5.3] {rdap_variant.json}
- `-11506`: Variant structure validation [RFC 9083 Section 5.3] {rdap_variant.json}

#### Unicode/LDH Name Validation (-116XX, -117XX)
- `-11603`: Unicode name validation [RFC 9083 Section 3.1] {rdap_common.json}
- `-11700`: LDH name length validation (exceeding 63 characters) [RFC 9083 Section 3.1] {NOT IMPLEMENTED}
- `-11701`: LDH name total length validation (exceeding 253 characters) [RFC 9083 Section 3.1] {NOT IMPLEMENTED}
- `-11703`: LDH name validation [RFC 9083 Section 3.1] {rdap_common.json}

#### Roles Validation (-118XX)
- `-11800`: Roles validation [RFC 9083 Section 10.2] {NOT IMPLEMENTED}
- `-11801`: Roles format validation (not list of strings) [RFC 9083 Section 10.2] {rdap_entity.json}
- `-11802`: Roles enum validation against datasets [RFC 9083 Section 10.2] {rdap_entity.json}
- `-11803`: Duplicate roles validation [RFC 9083 Section 10.2] {NOT IMPLEMENTED}

### JSON Structure Validation (-12XXX)

#### Secure DNS Validation (-120XX)
- `-12003`: Secure DNS validation [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12005`: Secure DNS structure validation [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12006`: Secure DNS format validation [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12007`: Secure DNS validation (dsData or keyData element missing) [RFC 9083 Section 5.3] {NOT IMPLEMENTED}
- `-12008`: DS Data validation [RFC 9083 Section 5.3] {NOT IMPLEMENTED}
- `-12009`: DS Data authorized keys validation [RFC 9083 Section 5.3] {NOT IMPLEMENTED}
- `-12011`: Missing required keys in DS Data [RFC 9083 Section 5.3] {NOT IMPLEMENTED}
- `-12012`: Invalid keyTag format [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12013`: Invalid algorithm value [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12014`: Invalid digest value [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12015`: Invalid digestType value [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12016`: DS Data events validation [RFC 9083 Section 5.3]
- `-12017`: DS Data links validation [RFC 9083 Section 5.3]
- `-12022`: Key data validation [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12023`: Key data format validation [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12024`: Key data structure validation [RFC 9083 Section 5.3] {rdap_secureDNS.json}
- `-12025`: Key data properties validation [RFC 9083 Section 5.3] {rdap_secureDNS.json}

#### Error Response Validation (-121XX)
- `-12100`: Error response validation [RFC 9083 Section 6]
- `-12101`: Error response structure validation [RFC 9083 Section 6] {rdap_error.json}
- `-12102`: Error response format validation [RFC 9083 Section 6]
- `-12103`: Error code validation (not a number) [RFC 9083 Section 6] {rdap_error.json}
- `-12104`: Title validation (not a string) [RFC 9083 Section 6] {rdap_error.json}
- `-12105`: Error response description validation [RFC 9083 Section 6]
- `-12106`: Description format validation [RFC 9083 Section 6] {rdap_error.json}
- `-12107`: Missing errorCode in error response (2024 profile) - "The errorCode value is required in an error response." [RFC 9083 Section 6, RDAP Response Profile 2024] {RDAPHttpQuery.java}
- `-12108`: ErrorCode value mismatch (2024 profile) - "The errorCode value does not match the HTTP status code." [RFC 9083 Section 6, RDAP Response Profile 2024] {RDAPHttpQuery.java}

#### Domain Validation (-122XX)
- `-12203`: Domain objectClassName validation [RFC 9083 Section 5.3] {rdap_domain.json}
- `-12204`: Domain structure validation [RFC 9083 Section 5.3] {rdap_domain.json}

#### Entity Validation (-123XX)
- `-12300`: Entity validation [RFC 9083 Section 5.1]
- `-12301`: Entity structure validation [RFC 9083 Section 5.1]
- `-12302`: Entity format validation [RFC 9083 Section 5.1]
- `-12303`: Entity objectClassName validation [RFC 9083 Section 5.1] {rdap_entity.json, rdap_autnum.json, rdap_ip_network.json}
- `-12304`: Entity handle validation [RFC 9083 Section 5.1] {rdap_entity.json, rdap_autnum.json, rdap_ip_network.json}
- `-12305`: vCard array validation - "The vcard array does not contain valid values." or "The vCard array structure is incorrect." [RFC 9083 Section 5.1] {VcardArrayGeneralValidation.java, RDAPProfileVcardArrayValidation.java, VcardExceptionParser.java}

#### Nameserver Validation (-124XX)
- `-12403`: Nameserver objectClassName validation [RFC 9083 Section 5.2] {rdap_nameserver.json}
- `-12404`: Nameserver structure validation [RFC 9083 Section 5.2] {rdap_nameserver.json}

#### Help Response Validation (-125XX)
- `-12500`: Help response validation [RFC 9082 Section 4.3]
- `-12501`: Help response structure validation [RFC 9082 Section 4.3]
- `-12502`: Help response format validation [RFC 9082 Section 4.3]
- `-12503`: Help response notices/remarks validation [RFC 9082 Section 4.3]
- `-12505`: Help response RDAP conformance validation [RFC 9082 Section 4.3]

#### Nameserver Search Validation (-126XX)
- `-12600`: Nameserver search validation [RFC 9082 Section 3.2.2]
- `-12601`: Nameserver search structure [RFC 9082 Section 3.2.2]
- `-12602`: Nameserver search format [RFC 9082 Section 3.2.2]
- `-12604`: Nameserver search results validation [RFC 9082 Section 3.2.2]
- `-12605`: Nameserver search remarks validation [RFC 9082 Section 3.2.2]
- `-12606`: Nameserver search events validation [RFC 9082 Section 3.2.2]
- `-12607`: Nameserver search notices validation [RFC 9082 Section 3.2.2]
- `-12608`: Nameserver search conformance validation [RFC 9082 Section 3.2.2]
- `-12609`: Nameserver search conformance validation [RFC 9082 Section 3.2.2]
- `-12610`: Missing nameserverSearchResults (2024 profile) - "The nameserverSearchResults structure is required." [RFC 9082 Section 3.2.2, RDAP Response Profile 2024] {RDAPHttpQuery.java}

### TIG Validation Errors (-20XXX)
- `-20100`: TIG 1.2 validation [TIG Section 1.2] {TigValidation1Dot2.java}
- `-20101`: TIG 1.2 validation [TIG Section 1.2] {TigValidation1Dot2.java}
- `-20200`: TIG 1.3 validation [TIG Section 1.3] {TigValidation1Dot3.java}
- `-20300`: TIG 1.6 validation (HTTP HEAD request) [TIG Section 1.6, RFC 7480 Section 4] {TigValidation1Dot6.java}
- `-20400`: IPv4 DNS validation - "The RDAP service is not provided over IPv4 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1." [TIG Section 1.8] {DNSCacheResolver.java, RDAPValidatorResultsImpl.java, RDAPValidationResultFile.java, TigValidation1Dot8.java}
- `-20401`: IPv6 DNS validation - "The RDAP service is not provided over IPv6 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1." [TIG Section 1.8] {DNSCacheResolver.java, RDAPValidatorResultsImpl.java, RDAPValidationResultFile.java, TigValidation1Dot8.java}
- `-20500`: TIG 1.13 validation (HTTP headers) [TIG Section 1.13, RFC 7480 Section 4] {TigValidation1Dot13.java}
- `-20700`: TIG 3.3 and 3.4 validation [TIG Section 3.3, 3.4] {TigValidation3Dot3And3Dot4.java}
- `-20701`: Help query validation (2024 profile) - "Response to a /help query did not yield a proper status code or RDAP response." [RFC 9082 Section 4.3, RDAP Response Profile 2024] {ResponseValidationHelp_2024.java}
- `-20800`: TIG 4.1 validation [TIG Section 4.1] {TigValidation4Dot1.java, jcard.json}
- `-20900`: TIG 7.1 and 7.2 validation - "Tel property without voice or fax type" [TIG Section 7.1, 7.2, RFC 6350] {TigValidation7Dot1And7Dot2.java}

### Registry-Specific Errors (-23XXX)
- `-23100`: Registry URL validation [RFC 7484 Section 3, RDAP Response Profile] {TigValidation1Dot11Dot1.java}
- `-23101`: Registry URL format validation [RFC 7484 Section 3, RDAP Response Profile] {TigValidation1Dot11Dot1.java}
- `-23102`: Registry URL structure validation [RFC 7484 Section 3, RDAP Response Profile] {TigValidation1Dot11Dot1.java}
- `-23200`: Registry TIG 3.2 validation [TIG Section 3.2] {TigValidation3Dot2.java}
- `-23201`: Registry TIG 3.2 validation (2024 profile) [TIG Section 3.2, RDAP Response Profile 2024] {TigValidation3Dot2_2024.java}
- `-23202`: Registry href domain query validation (2024 profile) - "the href property must be domain query as defined by Section 3.1.3 of RFC 9082." [RFC 9082 Section 3.1.3, TIG Section 3.2, RDAP Response Profile 2024] {TigValidation3Dot2_2024.java}
- `-23301`: Registry entity public IDs identifier validation [RFC 9083 Section 4.8, TIG Section 6.1] {TigValidation6Dot1.java}

### Registrar-Specific Errors (-26XXX)
- `-26100`: Registrar URL validation [RFC 7484 Section 3, RDAP Response Profile] {TigValidation1Dot12Dot1.java}
- `-26101`: Registrar URL format validation [RFC 7484 Section 3, RDAP Response Profile] {TigValidation1Dot12Dot1.java}
- `-26102`: Registrar URL structure validation [RFC 7484 Section 3, RDAP Response Profile] {TigValidation1Dot12Dot1.java}

### Handle and Domain Validation Errors (-4XXXX)
- `-40400`: General response validation [RFC 9083 Section 4] {ResponseValidation1Dot4.java}
- `-46100`: Domain validation (section 2.1) [RDAP Response Profile Section 2.1] {ResponseValidation2Dot1.java}
- `-46101`: Domain validation (unicode name) [RDAP Response Profile Section 2.1] {ResponseValidation2Dot1.java}
- `-46200`: Handle validation - "Handle format violation" [RFC 9083 Section 3.1] {ResponseValidation2Dot2.java, ResponseValidation2Dot2_1_2024.java}
- `-46201`: Handle validation (2024 profile) [RFC 9083 Section 3.1, RDAP Response Profile 2024] {ResponseValidation2Dot2_1_2024.java}
- `-46202`: Handle validation (2024 profile) [RFC 9083 Section 3.1, RDAP Response Profile 2024] {ResponseValidation2Dot2_1_2024.java}
- `-46203`: Handle validation (2024 profile) [RFC 9083 Section 3.1, RDAP Response Profile 2024] {ResponseValidation2Dot2_1_2024.java}
- `-46204`: Handle validation (2024 profile) [RFC 9083 Section 3.1, RDAP Response Profile 2024] {ResponseValidation2Dot2_1_2024.java}
- `-46205`: Handle validation (2024 profile) - ICANN testing EPPROID [RFC 9083 Section 3.1, RDAP Response Profile 2024] {ResponseValidation2Dot2_2024.java}
- `-46206`: Handle validation (2024 profile) - Registry Domain ID redaction consistency [RFC 9537, RDAP Response Profile 2024] {ResponseValidation2Dot2_1_2024.java}
- `-46300`: Domain validation (section 2.3.1.1) [RDAP Response Profile Section 2.3.1.1] {ResponseValidation2Dot3Dot1Dot1.java}
- `-46500`: Notices validation [RFC 9083 Section 4.3] {ResponseValidationNoticesIncluded.java}
- `-46600`: Domain validation (section 2.6.3) [RDAP Response Profile Section 2.6.3] {ResponseValidation2Dot6Dot3.java}
- `-46601`: Domain validation (2024 profile) [RDAP Response Profile 2024 Section 2.6.3] {ResponseValidation2Dot6Dot3_2024.java}
- `-46602`: Domain validation (2024 profile) [RDAP Response Profile 2024 Section 2.6.3] {ResponseValidation2Dot6Dot3_2024.java}
- `-46603`: Domain validation (2024 profile) [RDAP Response Profile 2024 Section 2.6.3] {ResponseValidation2Dot6Dot3_2024.java}
- `-46604`: Domain validation (2024 profile) [RDAP Response Profile 2024 Section 2.6.3] {ResponseValidation2Dot6Dot3_2024.java}
- `-46605`: Domain validation (2024 profile) [RDAP Response Profile 2024 Section 2.6.3] {ResponseValidation2Dot6Dot3_2024.java}
- `-46606`: Domain validation (2024 profile) [RDAP Response Profile 2024 Section 2.6.3] {ResponseValidation2Dot6Dot3_2024.java}
- `-46701`: Domain validation (2024 profile) [RDAP Response Profile 2024] {ResponseValidation2Dot10_2024.java}
- `-46702`: Domain validation (2024 profile) [RDAP Response Profile 2024] {ResponseValidation2Dot10_2024.java}
- `-46703`: Domain validation (2024 profile) [RDAP Response Profile 2024] {ResponseValidation2Dot10_2024.java}
- `-46704`: Domain validation (2024 profile) [RDAP Response Profile 2024] {ResponseValidation2Dot10_2024.java}
- `-46705`: Domain validation (2024 profile) [RDAP Response Profile 2024] {ResponseValidation2Dot10_2024.java}
- `-46706`: Domain validation (2024 profile) [RDAP Response Profile 2024] {ResponseValidation2Dot10_2024.java}
- `-46800`: Domain validation [RFC 9083 Section 5.3] {ResponseValidation2Dot10.java}
- `-46801`: Domain validation [RFC 9083 Section 5.3] {ResponseValidation2Dot10.java}
- `-46802`: Domain validation [RFC 9083 Section 5.3] {ResponseValidation2Dot10.java}
- `-46900`: Domain validation (RFC5731) [RFC 5731 EPP Domain Mapping] {ResponseValidationRFC5731.java}
- `-47001`: Domain validation (RFC3915) [RFC 3915 Domain Registry Grace Period] {ResponseValidationRFC3915.java}
- `-47002`: Domain validation (RFC3915) [RFC 3915 Domain Registry Grace Period] {ResponseValidationRFC3915.java}
- `-47100`: Domain validation (section 2.6.1) [RDAP Response Profile Section 2.6.1] {ResponseValidation2Dot6Dot1.java}
- `-47205`: Domain validation (section 2.9.1 and 2.9.2, 2024 profile) [RDAP Response Profile 2024 Section 2.9.1, 2.9.2] {ResponseValidation2Dot9Dot1And2Dot9Dot2_2024.java}
- `-47300`: Domain validation [RFC 9083 Section 5.3] {ResponseValidation2Dot4Dot1.java}
- `-47301`: Domain validation [RFC 9083 Section 5.3] {ResponseValidation2Dot4Dot1.java}
- `-47302`: Domain validation [RFC 9083 Section 5.3] {ResponseValidation2Dot4Dot1.java}
- `-47500`: Domain validation (section 2.4.5) [RDAP Response Profile Section 2.4.5] {ResponseValidation2Dot4Dot5.java}
- `-47600`: Domain entity validation (2024 profile) [RFC 9083 Section 5.3, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot3_2024.java}
- `-47601`: Domain entity handle EPPROID validation (2024 profile) [RFC 9083 Section 5.3, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot3_2024.java}
- `-47700`: Domain validation (section 2.4.6, 2024 profile) [RDAP Response Profile 2024 Section 2.4.6] {ResponseValidation2Dot4Dot6_2024.java}
- `-47701`: Domain validation (section 2.4.6, 2024 profile) [RDAP Response Profile 2024 Section 2.4.6] {ResponseValidation2Dot4Dot6_2024.java}
- `-47702`: Domain validation (section 2.4.6, 2024 profile) [RDAP Response Profile 2024 Section 2.4.6] {ResponseValidation2Dot4Dot6_2024.java}
- `-47703`: Domain validation (section 2.4.6, 2024 profile) [RDAP Response Profile 2024 Section 2.4.6] {ResponseValidation2Dot4Dot6_2024.java}
- `-49100`: Nameserver query validation (section 4.1) [RDAP Response Profile Section 4.1] {ResponseValidation4Dot1Query.java}
- `-49101`: Nameserver query validation (unicode name) [RDAP Response Profile Section 4.1] {ResponseValidation4Dot1Query.java}
- `-49102`: Nameserver handle validation [RFC 9083 Section 5.2] {ResponseValidation4Dot1Handle.java}
- `-49103`: Nameserver handle EPPROID validation [RFC 9083 Section 5.2] {ResponseValidation4Dot1Handle.java}
- `-49104`: Nameserver handle validation (2024 profile) [RFC 9083 Section 5.2, RDAP Response Profile 2024] {ResponseValidation4Dot1Handle_2024.java}
- `-49200`: Nameserver validation (section 4.3) [RDAP Response Profile Section 4.3] {ResponseValidation4Dot3.java}
- `-49205`: Nameserver validation [RFC 9083 Section 5.2] {ResponseValidation4Dot3.java}

### Entity Validation Errors (-5XXXX)
- `-52100`: Entity validation [RFC 9083 Section 5.1] {ResponseValidation2Dot7Dot1DotXAndRelated1.java}
- `-52101`: Entity validation [RFC 9083 Section 5.1] {ResponseValidation2Dot7Dot1DotXAndRelated2.java}
- `-52104`: Entity validation (Related5) [RFC 9083 Section 5.1] {ResponseValidation2Dot7Dot1DotXAndRelated5.java}
- `-52105`: Entity validation [RFC 9083 Section 5.1] {ResponseValidation2Dot7Dot1DotXAndRelated6.java}
- `-52106`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024.java}
- `-55000`: Entity validation - "An entity with the administrative, technical, or billing role without a valid \"EMAIL REDACTED FOR PRIVACY\" remark was found. See section 2.7.5.3 of the RDAP_Response_Profile_2_1." [RDAP Response Profile Section 2.7.5.3] {ResponseValidation2Dot7Dot5Dot3.java}

### Contact Validation Errors (-58XXX)
- `-58000`: Contact validation [RFC 9083 Section 5.1, RFC 6350 vCard] {ResponseValidation2Dot7Dot5Dot2.java}
- `-58001`: Contact URI validation - administrative, technical, or billing role contact URI must contain email or http/https link [RFC 9083 Section 5.1, RFC 6350 vCard] {ResponseValidation2Dot7Dot5Dot2.java}

### SSL/TLS and Security Validation Errors (-6XXXX)
- `-60100`: Entity validation [RFC 9083 Section 5.1] {ResponseValidation3Dot1.java}
- `-60101`: Entity validation [RFC 9083 Section 5.1] {ResponseValidation3Dot1.java}
- `-60200`: Entity validation [RFC 9083 Section 5.1] {ResponseValidation3Dot2.java}
- `-61100`: SSL/TLS validation (2024 profile) - "The RDAP server must only use TLS 1.2 or TLS 1.3" [RFC 7481 Section 3, RDAP Response Profile 2024] {TigValidation1Dot5_2024.java}
- `-61101`: SSL/TLS certificate validation (2024 profile) - "The RDAP server must use one of the following cipher suites when using TLS 1.2: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384." [RFC 7481 Section 3, RDAP Response Profile 2024] {TigValidation1Dot5_2024.java}

### Redaction Validation Errors (2024 Profile) (-65XXX)
- `-65000`: Technical contact fn property validation (2024 profile) - "The fn property is required on the vcard for the technical contact." [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot1_2024.java}
- `-65001`: Technical contact name redaction validation (2024 profile) - "a redaction of type Tech Name is required." [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot1_2024.java}
- `-65002`: Technical name JSONPath validation (2024 profile) - "jsonpath is invalid for Tech Name" [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot1_2024.java}
- `-65003`: Technical name redaction path validation (2024 profile) - "jsonpath must evaluate to a non-empty set for redaction by empty value of Tech Name." [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot1_2024.java}
- `-65004`: Technical name redaction method validation (2024 profile) - "Tech Name redaction method must be emptyValue" [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot1_2024.java}
- `-65100`: Technical contact phone redaction validation (2024 profile) - "a redaction of type Tech Phone is required." [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot2_2024.java}
- `-65101`: Technical phone JSONPath validation (2024 profile) - "jsonpath is invalid for Tech Phone." [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot2_2024.java}
- `-65102`: Technical phone redaction path validation (2024 profile) - "jsonpath must evaluate to a zero set for redaction by removal of Tech Phone." [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot2_2024.java}
- `-65103`: Technical phone redaction method validation (2024 profile) - "Tech Phone redaction method must be removal if present" [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot2_2024.java}
- `-65200`: Technical email contact validation (2024 profile) - "a redaction of Tech Email may not have both the email and contact-uri" [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65201`: Technical email requirement validation (2024 profile) - "a redaction of Tech Email must have either the email or contact-uri" [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65202`: Technical email redaction method validation (2024 profile) - "Tech Email redaction method must be replacementValue" [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65203`: Technical email postPath validation (2024 profile) - "jsonpath is invalid for Tech Email postPath" [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65204`: Technical email postPath evaluation validation (2024 profile) - "jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email." [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65205`: Technical email replacementPath validation (2024 profile) - "jsonpath is invalid for Tech Email replacementPath" [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65206`: Technical email prePath validation (2024 profile) - "jsonpath is invalid for Tech Email prePath" [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65207`: Technical email replacementPath evaluation validation (2024 profile) - "jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email in replacementPath" [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot6Dot3_2024.java}
- `-65300`: Domain invalid validation (2024 profile) [RDAP Response Profile 2024] {ResponseValidationDomainInvalid_2024.java, RDAPValidatorResultsImpl.java, RDAPValidationResultFile.java}
- `-65500`: Tech Email JSONPath validation (2024 profile) - "jsonpath is invalid for Tech Email" [RFC 9537 Section 2, RDAP Response Profile 2024] {ResponseValidationTechEmail_2024.java}
- `-65501`: Tech Email redaction path validation (2024 profile) - "jsonpath must evaluate to a zero set for redaction by removal of Tech Email." [RFC 9537 Section 2, RDAP Response Profile 2024] {ResponseValidationTechEmail_2024.java}
- `-65502`: Tech Email redaction method validation (2024 profile) - "Tech Email redaction method must be removal if present" [RFC 9537 Section 2, RDAP Response Profile 2024] {ResponseValidationTechEmail_2024.java}
- `-65503`: Tech Email redaction consistency validation (2024 profile) - "a redaction of type Tech Email was found but email was not redacted." [RFC 9537 Section 2, RDAP Response Profile 2024] {ResponseValidationTechEmail_2024.java}

### vCard Validation Errors (2024 Profile) (-63XXX)
- `-63000`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot2_2024.java}
- `-63100`: Registrant handle validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidationRegistrantHandle_2024.java}
- `-63101`: Registrant handle validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidationRegistrantHandle_2024.java}
- `-63102`: Registrant handle validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidationRegistrantHandle_2024.java}
- `-63103`: Registrant handle validation (2024 profile) [RFC 9083 Section 5.1, RFC 9537, RDAP Response Profile 2024] {ResponseValidationRegistrantHandle_2024.java}
- `-63104`: Registrant handle validation (2024 profile) [RFC 9083 Section 5.1, RFC 9537, RDAP Response Profile 2024] {ResponseValidationRegistrantHandle_2024.java}
- `-63105`: Registrant handle validation (2024 profile) [RFC 9083 Section 5.1, RFC 9537, RDAP Response Profile 2024] {ResponseValidationRegistrantHandle_2024.java}
- `-63200`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot4Dot1_2024.java}
- `-63201`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63202`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63203`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63204`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63301`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RFC 9537, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot4Dot2_2024.java}
- `-63302`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RFC 9537, RDAP Response Profile 2024]
- `-63303`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RFC 9537, RDAP Response Profile 2024]
- `-63400`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot4Dot3_2024.java}
- `-63401`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63402`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63403`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63404`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63500`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot4Dot4_2024.java}
- `-63501`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63502`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63503`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63504`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63600`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot4Dot6_2024.java}
- `-63601`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63602`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63603`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63604`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63700`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63701`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63702`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63703`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-63800`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot5Dot1_2024.java}
- `-63801`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024]
- `-63802`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024]
- `-63900`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot5Dot2_2024.java}
- `-63901`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024]
- `-63902`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024]
- `-64000`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot5Dot3_2024.java}
- `-64001`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024]
- `-64002`: Entity validation (2024 profile) [RFC 9083 Section 5.1, RDAP Response Profile 2024]
- `-64100`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidation2Dot7Dot4Dot9_2024.java}
- `-64101`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-64102`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-64103`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-64104`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-64105`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-64106`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-64107`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-64108`: vCard validation (2024 profile) [RFC 9083 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-65400`: vCard validation (2024 profile) [RFC 9537 Section 5.1, RFC 6350, RDAP Response Profile 2024] {ResponseValidationRegistrantEmail_2024.java}
- `-65401`: vCard validation (2024 profile) [RFC 9537 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-65402`: vCard validation (2024 profile) [RFC 9537 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-65403`: vCard validation (2024 profile) [RFC 9537 Section 5.1, RFC 6350, RDAP Response Profile 2024]
- `-65404`: vCard validation (2024 profile) [RFC 9537 Section 5.1, RFC 6350, RDAP Response Profile 2024]

## Source File Mappings

Each error code has been mapped to its corresponding Java source file(s) where the validation logic is implemented. The mappings are shown in curly braces `{SourceFile.java}` after each error code description.

## Files Containing Error Codes (60+ identified)

### Core Validation Files
- `RDAPHttpQuery.java` - HTTP request/response validation
- `DNSCacheResolver.java` - DNS resolution validation
- `DomainCaseFoldingValidation.java` - Domain name case validation
- `SchemaValidator.java` - JSON schema validation

### Profile Validation Files
- `TigValidation*.java` - Technical Implementation Guide validations
- `ResponseValidation*.java` - RDAP response structure validations
- `ResponseValidation*_2024.java` - 2024 profile specific validations

### Exception Parsers
Exception parsers are responsible for converting JSON schema validation exceptions into RDAP error codes. All 19 exception parsers that generate error codes are listed below:

#### Core Type and Format Validation Parsers
- `BasicTypeExceptionParser.java` - Validates basic JSON types (string, boolean, null)
- `NumberExceptionParser.java` - Validates numeric values and ranges
- `StringFormatExceptionParser.java` - Base class for string format validation
- `DatetimeExceptionParser.java` - Validates RFC3339 datetime formats
- `EnumExceptionParser.java` - Validates enumerated values against datasets
- `RegexExceptionParser.java` - Validates regular expression patterns

#### Network and Domain Validation Parsers
- `Ipv4ValidationExceptionParser.java` - IPv4 address validation (generates codes -10100, -10101, -10102)
- `Ipv6ValidationExceptionParser.java` - IPv6 address validation (generates codes -10200, -10201, -10202, -11409)
- `IdnHostNameExceptionParser.java` - Domain name validation (generates codes -10300, -10301, -10302, -10303)
- `HostNameInUriExceptionParser.java` - URI hostname validation (delegates to IdnHostNameExceptionParser)
- `DatasetExceptionParser.java` - Validates values against IANA datasets

#### Structure and Schema Validation Parsers
- `MissingKeyExceptionParser.java` - Validates required JSON properties
- `UnknowKeyExceptionParser.java` - Validates unauthorized JSON properties
- `ComplexTypeExceptionParser.java` - Validates complex object structures
- `DependenciesExceptionParser.java` - Validates conditional schema dependencies
- `UniqueItemsExceptionParser.java` - Validates array uniqueness constraints
- `ConstExceptionParser.java` - Validates constant values
- `ContainsConstExceptionParser.java` - Validates arrays containing specific constant values

#### Entity and Contact Validation Parsers
- `VcardExceptionParser.java` - vCard array validation (generates code -12305)
- `RdapExtensionsExceptionParser.java` - RDAP extension validation

#### Non-Error Code Generating Parsers (Base Classes)
The following 5 parsers are test files or do not generate error codes:
- `ValidationExceptionNodeTest.java` - Test file
- `ExceptionParserTest.java` - Test file  
- `ExceptionParser.java` - Base abstract class
- `ValidationExceptionNode.java` - Exception data structure

### Entity and Contact Validations
- Various `ResponseValidation2Dot7Dot*` files for entity and contact validations
- vCard validation files for contact information

## Profile Differences

### 2019 Profile vs 2024 Profile
- Many validation classes have separate 2024 versions with enhanced checks
- 2024 profile introduces new error codes (e.g., `-12610`, `-61100`, `-61101`)
- 2024 profile has stricter requirements for error responses (e.g., `-12107`)

## Analysis Summary

### Error Code Statistics
- **Total Error Codes Documented:** 284 codes
- **HTTP Protocol Errors (-13XXX):** 10 codes
- **Network and Address Validation (-10XXX):** 29 codes total
  - IPv4 Address Validation (-101XX): 3 codes  
  - IPv6 Address Validation (-102XX): 3 codes
  - Domain Name Validation (-103XX): 4 codes
  - URI and Domain Validation (-104XX): 4 codes
  - RDAP Conformance Validation (-105XX): 6 codes
  - Links Validation (-106XX): 8 codes
  - Notices Validation (-107XX): 5 codes
  - Language Validation (-108XX): 1 code
  - Event Validation (-109XX): 6 codes
- **Schema Validation (-11XXX):** 35 codes (organized by subcategory)
  - Status Validation (-110XX): 3 codes
  - Port 43 Validation (-111XX): 1 code
  - Public IDs Validation (-112XX): 5 codes
  - Event Actor Validation (-113XX): 5 codes
  - IP Address Schema Validation (-114XX): 9 codes
  - Variant Validation (-115XX): 4 codes
  - Unicode/LDH Name Validation (-116XX, -117XX): 4 codes
  - Roles Validation (-118XX): 4 codes
- **JSON Structure Validation (-12XXX):** 53 codes (organized by subcategory)
  - Secure DNS Validation (-120XX): 15 codes
  - Error Response Validation (-121XX): 9 codes
  - Domain Validation (-122XX): 2 codes
  - Entity Validation (-123XX): 3 codes
  - Nameserver Validation (-124XX): 2 codes
  - Help Response Validation (-125XX): 5 codes
  - Nameserver Search Validation (-126XX): 11 codes
- **TIG Validation (-20XXX):** 11 codes
- **Registry-Specific (-23XXX):** 6 codes
- **Registrar-Specific (-26XXX):** 3 codes
- **Handle/Domain Validation (-4XXXX):** 44 codes
- **Entity Validation (-5XXXX):** 8 codes
- **Contact Validation (-58XXX):** 2 codes
- **Security and vCard Validation (-6XXXX):** 58 codes (heavily used in 2024 profile)
- **Redaction Validation (-65XXX):** 18 codes (2024 profile technical contact redaction)

### Error Code Numbering Convention
The RDAP Conformance Tool uses a systematic numbering scheme for its 284 documented error codes:

- **-10XXX:** Network and address validation
  - **-101XX:** IPv4 address validation
  - **-102XX:** IPv6 address validation  
  - **-103XX:** Domain name validation (length, labels, IDNA)
  - **-104XX:** URI and domain validation
  - **-105XX:** RDAP conformance validation
  - **-106XX:** Links validation
  - **-107XX:** Notices and remarks validation
  - **-108XX:** Language identifier validation
  - **-109XX:** Events validation
- **-11XXX:** Schema validation (JSON schema structure validation)
  - **-110XX:** Status validation
  - **-111XX:** Port 43 validation
  - **-112XX:** Public IDs validation
  - **-113XX:** Event actor validation
  - **-114XX:** IP address schema validation
  - **-115XX:** Variant relations validation
  - **-116XX, -117XX:** Unicode/LDH name validation
  - **-118XX:** Roles validation
- **-12XXX:** JSON structure validation (complex object validation)
  - **-120XX:** Secure DNS validation
  - **-121XX:** Error response validation
  - **-122XX:** Domain object validation
  - **-123XX:** Entity object validation
  - **-124XX:** Nameserver object validation
  - **-125XX:** Help response validation
  - **-126XX:** Nameserver search validation
- **-13XXX:** HTTP protocol validation (headers, status codes, redirects)
- **-20XXX:** Technical Implementation Guide (TIG) validation
- **-23XXX:** Registry-specific validation
- **-26XXX:** Registrar-specific validation
- **-4XXXX:** Handle and domain validation (including 2024 profile enhancements)
- **-5XXXX:** Entity validation (high-level entity structure)
- **-58XXX:** Contact validation (contact-specific rules)
- **-6XXXX:** Security and vCard validation (SSL/TLS, certificates, vCard structure)  
- **-65XXX:** Redaction validation (2024 profile technical contact redaction requirements)

### Profile Differences Summary

#### 2019 Profile vs 2024 Profile
- **2024 Profile introduces 118+ new error codes** primarily in the -63XXX, -64XXX, and -65XXX ranges for vCard and redaction validation
- **Enhanced SSL/TLS validation** with specific cipher suite requirements (-61100, -61101)
- **Stricter error response requirements** (-12107 for mandatory errorCode, -12108 for errorCode matching HTTP status)
- **New help query validation** (-20701)
- **Enhanced redirect validation** (-13005, -13006)
- **Comprehensive vCard validation** covering administrative, technical, and billing contacts
- **Technical contact redaction validation** (-65XXX range for name, phone, and email redaction requirements)
- **Special domain invalid validation** (-65300)
- **Enhanced handle validation** with codes -46201 through -46206
- **Enhanced domain validation** with new codes in the -46XXX and -47XXX ranges
