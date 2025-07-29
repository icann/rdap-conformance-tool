# Conformance Considerations

## CORS (Access-Control-Allow-Origin)

The "access-control-allow-origin" HTTP header is part of the 
[Cross-Origin Resource Sharing (CORS)](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) mechanism
to help applications that run inside of web browsers to access web APIs, such as RDAP.

Section 1.14 of the 2024 ICANN 
[RDAP Technical Implementation Guide (TIG)](https://www.icann.org/en/system/files/files/rdap-technical-implementation-guide-21feb24-en.pdf)
states:

> 1.14. When responding to RDAP valid requests, an RDAP server MUST include the
> Access-Control-Allow-Origin response header, as specified by
> [W3C.REC-cors-20140116]. Unless otherwise specified, a value of "*" MUST be
> used.

**NOTE:** This is in section 1.13 of the 2019 
[TIG](https://www.icann.org/en/system/files/files/rdap-technical-implementation-guide-15feb19-en.pdf)

This means that an RDAP server must set the "access-control-allow-origin" HTTP header to the value "*":

```
access-control-allow-origin: *
```

**NOTE:** HTTP headers are not required to be capitalized.

## HTTP URLs

Section 1.4 of the 2024 
[ICANN RDAP Technical Implementation Guide](https://www.icann.org/en/system/files/files/rdap-technical-implementation-guide-21feb24-en.pdf)
states:

> 1.4. The RDAP service MUST be provided over HTTPS only as described in
> RFC9110 or its successors.

**NOTE:** This in section 1.2 of the 
[2019 TIG](https://www.icann.org/en/system/files/files/rdap-technical-implementation-guide-15feb19-en.pdf)

This means that RDAP services for gTLD registries and registrars is only to be offered over HTTPS, and the URL for the RDAP service should use the "https" URI scheme.

## HTTP to HTTPS Redirects

Section 1.4 of the 2024 [ICANN RDAP Technical Implementation Guide](https://www.icann.org/en/system/files/files/rdap-technical-implementation-guide-21feb24-en.pdf) states:

> 1.4. The RDAP service MUST be provided over HTTPS only as described in RFC9110 or its successors.

NOTE: This in section 1.2 of the [2019 TIG](https://www.icann.org/en/system/files/files/rdap-technical-implementation-guide-15feb19-en.pdf)

This means that RDAP services for gTLD registries and registrars is only to be offered over HTTPS, and the URL for the RDAP service should use the "https" URI scheme. For gTLD registries, this means they are to provide HTTPS and only HTTPS to IANA to be placed in the 
[RDAP bootstrap files](https://data.iana.org/rdap/dns.json).

If an HTTP URL is operational, it must redirect to the HTTPS URL even if the HTTP URL is not publicly known.

## Registrant Without CC Parameter

Section 1.4 of the 2024 ICANN [RDAP Response Profile](https://www.icann.org/en/system/files/files/rdap-response-profile-21feb24-en.pdf)
states:

> 1.4. In an entity object with an adr structure, the country name property MUST be
> empty and the cc property MUST be populated by a value from ISO 3166-1
> alpha-2.

This means that the country name must be empty and the ISO 3166-2 Alpha-2 country code for that
country should be used instead. RDAP uses [jCard](https://datatracker.ietf.org/doc/html/rfc7095),
 which is the JSON form of [vCard](https://datatracker.ietf.org/doc/html/rfc6350), and vCard defines
postal addresses to use country name. However, [RFC 8605](https://www.rfc-editor.org/rfc/rfc8605.html)
defines a "cc" parameter for vCard to specify the ISO 3166-1 Alpha-2 country code.

### Usage With Structured Postal Addresses

When using structured postal addresses in jCard with the "cc" parameter, the "adr" property looks like this:

```json
[
  "adr",
  {
    "cc": "CA"
  },
  "text",
  [
    "",   
    "",    
    "123 Maple Ave", 
    "Quebec",        
    "QC",            
    "G1V 2M2",       
    ""
  ]
]
```

The last string in string array that is the last element in the "adr" property is designated to be the country name.
It is to be left blank, and the "cc" parameter is to be used.

**NOTE:** The array is always seven elements in length. The first 2 empty strings represent a mail box number and
suite/apartment number, but neither is recommended for use.

### Using with Unstructured Postal Addresses

*NOTE: Unstructured postal addresses are disallowed by Section 3.8.1 of the Technical Implementation Guide. This information is provided for educational purposes.*

jCard allows the use of unstructured postal addresses, where each line of an address on a postal envelope is separated
by a newline (`\n`). Here is an example with the "cc" parameter.

```json
[
  "adr",
  {
    "cc": "CA",
    "label":"123 Maple Ave\nQuebec\nQC\nG1V 2M2\n\n"
  },
  "text",
  [
    "", "", "", "", "", "", ""
  ]
]
```

**NOTE:** The seven-element text array is still given but all strings are empty, and the "label" parameter is
used to represent the address.
