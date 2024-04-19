# RDAP Conformance Tool (rdapct)

The RDAP Conformance Tool is a stand-alone tool acting like a test suite that verifies the conformity of an RDAP server against 
the specifications developed by the IETF (RFC7481, RFC7482, RFC7483, RFC7484) and the ICANN gTLD RDAP
profile (https://www.icann.org/gtld-rdap-profile). It only tests RDAP servers related to domains. 
Apart from generic RDAP tests, there are no specific tests for IP addresses and AS Numbers RDAP servers.

This tool implements more than 300 tests, as detailed in the doc directory.

The tool is divided into two modules with their respective documentation:

- [Command line module (tool)](./tool/README.md) the main entry point
- [Validator module](./validator/README.md) the rdap validation library

# Using with Docker

This repository contains a Dockerfile which builds an image containing the tool.

To build it, run this command:

    docker buildx build -t [image_name] .

Replace `[image_name]` with a unique memorable name such as `rdapct`. Once the
image has been built, you can run the tool using

    docker run rdapct ARGS

When run via Docker, a minimal configuration file is used, and the results are
printed to `STDOUT`.

Datasets are added to the image at build time and are not refreshed, so the image
should be rebuilt in order to ensure they are up-to-date.

# Normal usage

There is no installation needed. The compliled executable jar file and configuration file are available for download at [tool/bin/](./tool/bin/) that can be executed with java. Alternatively, the executable JAVA11 jar file can be created by building the tool (see below). e.g.:

    java -jar rdapct-1.0.jar -c ./rdapct-config.json https://rdap.registry.com/domain/example.com

where rdapct-config.json is the configuration file.
 
The tool do the query specified on the command line, parses the response and test the response. It also do other queries
to the server such as the /help query.

The result code shows the primary issue, if any, of all issues found. The details of the tests are found in the /results 
directory, and the datasets retrieved from the relevant IANA registries are found in the /datasets directory.

Queries such as domain, nameserver, entity are supported. Basic search (RFC 7482) is also supported.

# Finding the RDAP server

To find the RDAP server for a domain, look at the IANA Bootstrap Service Registry for Domain Name Space 
(https://www.iana.org/assignments/rdap-dns/rdap-dns.xhtml) and look for the TLD of the domain. The second element
of the array for that TLD is the URL of the RDAP server. To lookup for a domain, just add domain to the URL 
followed by the domain itself.

# Configuration file

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

## Minimal config file

The simplest config file one can write looks like this:

```
{
    "definitionIdentifier": "test"
}
```

This config file will simply tells the tool to put the id "test" inside the output result file. This
id allows the user to discriminate test runs between each other.

## A more complete example

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

# How to build this project

This is a maven project: to build the executable jar simply run

    mvn package -DskipTests

# Requirements

- Java 11

# Internals

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

# License

 Copyright 2021 Internet Corporation for Assigned Names and Numbers ("ICANN")

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 

# Acknowledgements

This RDAP conformance tool has been developed by Viagénie
(Julien Bernard, Guillaume Blanchet, Marc Blanchet, Pierre Larochelle) under a contract from ICANN.
