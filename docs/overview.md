# Installing and Using

There are two methods for installing and using the RDAP Conformance Tool. The first method uses Docker and the second method uses Java.
**Please be aware that running these this tool from within the network of a registry, registrar, or RDAP service provider may not
find problems that are caused by CDNs, proxies, and other network elements.** It is always advised to use this tool from "outside"
your network. For this reason, the [online version](https://webrdapct.icann.org/) may be more appropriate.

With each method, the RDAP Conformance Tool must be given a full RDAP URL as defined by [RFC 9082](https://datatracker.ietf.org/doc/rfc9082/). 
For example, if the "base" RDAP URL for a domain registry or registrar is `https://rdap.example` then the complete URL for a domain at that 
registry or registrar would be `https://rdap.example/domain/example.net`.

The supplied configuration file, `rdapct_config.json` (which can be downloaded from a [release](https://github.com/icann/rdap-conformance-tool/releases)) 
is configured to instruct the tool to ignore known false positives.

## Using with Docker

To use Docker, you must either download a [source release](https://github.com/icann/rdap-conformance-tool/releases) or 
clone the repository. This repository contains a Dockerfile which builds an image containing the tool.

To build it, run this command:

    docker buildx build -t [image_name] .

Replace `[image_name]` with a unique memorable name such as `rdapct`. Once the
image has been built, you can run the tool using

    docker run rdapct ARGS

When run via Docker, a minimal configuration file is used, and the results are
printed to `STDOUT`.

Datasets are added to the image at build time and are not refreshed, so the image
should be rebuilt in order to ensure they are up-to-date.

## Using with Java

To use with Java, you must obtain the RDAPCT JAR file and a configuration file. Both are available to download
from a [GitHub Release](https://github.com/icann/rdap-conformance-tool/releases). You may also use Maven to build
and compile it yourself.

To run the tool, invoke the Java command with the `jar` option referencing the rdapct JAR file and the configuration file:

    java -jar rdapct-3.0.0.jar -c ./rdapct-config.json https://rdap.registry.com/domain/example.com

where rdapct-config.json is the configuration file.

## Getting the Results
 
The tool issues the query specified on the command line, parses the response and tests the response.

The tool will return with exit code 0 if it was able to successfully query the RDAP server, 
otherwise it will return with a non-zero exit code. The details of the tests are found in the /results 
directory, and the datasets retrieved from the relevant IANA registries are found in the /datasets directory.

Queries such as domain, nameserver, entity are supported. Basic search (RFC 7482) is also supported.

## Testing for RFC Compliance

By default, the RDAP Conformance Tool tests only for compliance with the IETF RDAP RFCs. 
To invoke the tool for this purpose, no special parameters are required:

Docker method:
```
docker run rdapct https://test.example
```

Java method:
```
java -jar rdapct-3.0.0.jar -c ./rdapct-config.json https://test.example
```

## Testing As a gTLD Registry

For gTLD registries, there are additional parameters to be used:
* `--gtld-registry` signifies to test the output for gTLD registry compliance
* `--thin` if the gTLD registry is a "thin" registry
* `--use-rdap-profile-february-2019` to test for compliance with the 2019 gTLD RDAP Profile.
* `--use-rdap-profile-february-2024` to test for compliance with the 2024 gTLD RDAP Profile.

Docker method:
```
docker run rdapct --gtld-registry --use-rdap-profile-february-2024 https://test.example
```

or for thin registries
```
docker run rdapct --gtld-registry --thin --use-rdap-profile-february-2024 https://test.example
```

Java method:
```
java -jar rdapct-3.0.0.jar -c ./rdapct-config.json --gtld-registry --use-rdap-profile-february-2024 https://test.example
```

or for thin registries
```
java -jar rdapct-3.0.0.jar -c ./rdapct-config.json --gtld-registry --thin --use-rdap-profile-february-2024 https://test.example
```

## Testing As a gTLD Registrar

For gTLD registrars, the following parameters are requried:
* `--gtld-registrar` signifies to test the output for gTLD registrar compliance
* `--use-rdap-profile-february-2019` to test for compliance with the 2019 gTLD RDAP Profile.
* `--use-rdap-profile-february-2024` to test for compliance with the 2024 gTLD RDAP Profile.

Docker method:
```
docker run rdapct --gtld-registrar --use-rdap-profile-february-2024 https://test.example
```

Java method:
```
java -jar rdapct-3.0.0.jar -c ./rdapct-config.json --gtld-registrar --use-rdap-profile-february-2024 https://test.example
```

## Additional Queries

The `--additional-conformance-queries` comamnd line parameter will cause RDAPCT to issue the following RDAP queries in addition
to the one provided in the give RDAP URL:

1. `/help` - queries the servers help path to asses conformance.
2. `/domain/not-a-domain.invalid` - queries for a domain that cannot be registered to asses conformance with a negative answer.

## Supressing IPv4 or IPv6 Queries

By default, RDAPCT issues all queries over both IPv4 and IPv6. The `--no-ipv4-queries` may be used to suppress queries over
IPv4, and the `--no-ipv6-queries` may be used to suppress queries over IPv6.

## Specifying the Results File

By default, RDAPCT writes the results to a file in a subordinate `results` directory with the file name `results-XXXX.json` where
XXXX is the timestamp when the file was generated.  The `--results-file` may be used to write the results to a file with a specific
file name.

## Exit Codes

RDAPCT emits the following exit codes when the process terminates:

| Value | Meaning |
| ----- | ------- |
| 0 | Normal Exit. |
| 1 | The configuration definition file is syntactically invalid. |
| 2 | The tool was not able to download a dataset. |
| 3 | The RDAP query is not supported by the tool. |
| 4 | The RDAP query is domain/<domain name> or nameserver/<nameserver>, but A-labels and U-labels are mixed. |
| 5 | No longer used. See result code -13000. |
| 6 | No longer used. See result code -13001. |
| 7 | No longer used. See result code -13002. |
| 8 | No longer used. See result code -13003. |
| 9 | The RDAP query is invalid because the TLD uses the thin model. |
| 10 | Failed to connect to host. |
| 11 | The TLS handshake failed. |
| 12 | TLS server certificate - common name invalid. |
| 13 | TLS server certificate - revoked. |
| 14 | TLS server certificate - expired. |
| 15 | Other errors with the TLS server certificate. |
| 16 | Too many redirects. |
| 17 | HTTP errors. |
| 18 | HTTP/2 errors. |
| 19 | Failure sending network data. |
| 20 | Failure in receiving network data. |
| 21 | Failure in writing result file. |
| 22 | Failure in reading result file. |
| 23 | Reserved. |
| 24 | Reserved. |
| 25 | Bad user input. |