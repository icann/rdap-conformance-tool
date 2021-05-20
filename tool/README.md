# RDAP Conformance Tool

# Command line options

```
$ java -jar rdapct-1.0.jar -h
Usage: rdap-conformance-tool [-hV] [--use-local-datasets]
                             -c=<configurationFile>
                             [--maximum-redirects=<maxRedirects>]
                             [--timeout=<timeout>]
                             [[--use-rdap-profile-february-2019]
                             ([--gtld-registrar] | [--gtld-registry [--thin]])]
                             RDAP_URI
      RDAP_URI               The URI to be tested
  -c, --config=<configurationFile>
                             Definition file
      --gtld-registrar       Validate the response as coming from a gTLD
                               registrar
      --gtld-registry        Validate the response as coming from a gTLD
                               registry
  -h, --help                 Show this help message and exit.
      --maximum-redirects=<maxRedirects>
                             Maximum number of redirects to follow
      --thin                 The TLD uses the thin model
      --timeout=<timeout>    Timeout for connecting to the server
      --use-local-datasets   Use locally-persisted datasets
      --use-rdap-profile-february-2019
                             Use RDAP Profile February 2019
  -V, --version              Print version information and exit.
```

# Parameters authorized combinations

|                                                                  | domain/<domain name> | domain/<domain name> | domain/<domain name> | nameserver/<nameserver name> | nameserver/<nameserver name> | entity/<handle> | entity/<handle> | help            | help | nameservers?ip=... | nameservers?ip=... |
|------------------------------------------------------------------|----------------------|----------------|------------------|------------------------------|------------------|-----------------|------------------|-----------------|------------------|--------------------|------------------|
|                                                                  | --gtld-registry      |                | --gtld-registrar | --gtld-registry              | --gtld-registrar | --gtld-registry | --gtld-registrar | --gtld-registry | --gtld-registrar | --gtld-registry    | --gtld-registrar |
|                                                                  | --thin set           | --thin not set |                  |                              |                  |                 |                  |                 |                  |                    |                  |
| Technical Implementation Guide - General                         | x                    | x              | x                | x                            | x                | x               | x                | x               | x                | x                  | x                |
| tigRegistrySection_1_11_1_Validation                             | x                    | x              |                  |                              |                  |                 |                  |                 |                  |                    |                  |
| tigRegistrySection_3_2_Validation                                | x                    | x              |                  |                              |                  |                 |                  |                 |                  |                    |                  |
| tigRegistrySection_6_1_Validation                                | x                    | x              | x                | x                            | x                | x               | x                |                 |                  |                    |                  |
| tigRegistrarSection_1_12_1_Validation                            | x                    | x              | x                | x                            | x                | x               | x                |                 |                  |                    |                  |
| RDAP Response Profile - General                                  | x                    | x              | x                | x                            | x                | x               | x                | x               | x                | x                  | x                |
| rdapResponseProfile_2_3_1_3_and_2_7_6_and_3_3_and_4_4_Validation | x                    | x              | x                | x                            | x                | x               | x                |                 |                  |                    |                  |
| RDAP Response Profile - Domain                                   | x                    | x              | x                |                              |                  |                 |                  |                 |                  |                    |                  |
| RDAP Response Profile - Nameserver                               |                      |                |                  | x                            |                  |                 |                  |                 |                  |                    |                  |
| RDAP Response Profile - Entities within Domain                   |                      | x              | x                |                              |                  |                 |                  |                 |                  |                    |                  |
| RDAP Response Profile - Entities within Domain - Registry        |                      | x              |                  |                              |                  |                 |                  |                 |                  |                    |                  |
| RDAP Response Profile - Entities within Domain - Registrar       |                      |                | x                |                              |                  |                 |                  |                 |                  |                    |                  |
| RDAP Response Profile - Entity - Registrar                       |                      |                |                  |                              |                  | x               |                  |                 |                  |                    |                  |
# Requirements
- Java 11

# Acknowledgements
This RDAP conformance tool has been developed by Viagénie
(Julien Bernard, Guillaume Blanchet, Marc Blanchet, Pierre Larochelle) under a contract from ICANN.
