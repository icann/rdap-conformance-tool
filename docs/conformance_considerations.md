# Conformance Considerations

## CORS (Access-Control-Allow-Origin)

The `access-control-allow-origin` HTTP header is part of the 
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

Some web application firewalls and gateways will automatically respond with an `access-control-allow-origin`
header in the response when the request contains an `origin` header, as is the case with some web browsers.
Hence, checking for `access-control-allow-origin` with a web browser may be misleading in some cases, and
confusion may arise between what is returned to the web browser in these specific conditions compared to the
results seen by RDAPCT.

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

## Media Types

Testing an RDAP service with just a web browser (i.e. not an RDAP web-app) can mask issues with the acceptance of media types (formerly called MIME types). A bare web browser will not properly account for the correct media types required of RDAP.

[RFC 7480 Section 4.2](https://datatracker.ietf.org/doc/html/rfc7480#autoid-6) states the following:

> To indicate to servers that an RDAP response is desired, clients
> include an Accept header field with an RDAP-specific JSON media type,
> the generic JSON media type, or both.  Servers receiving an RDAP
> request return an entity with a Content-Type header containing the
> RDAP-specific JSON media type.

This means that a client may use either the "application/json" or "application/rdap+json" media type in the "accept" HTTP header of an RDAP
request, but the RDAP response must only have the "application/rdap+json" media type in the "content-type" HTTP header.

This can be demonstrated using these curl command:

- `curl -v -H "accept: application/rdap+json" https://rdap.example`. 
    - The output should contain:
        - `> accept: application/rdap+json`
        - `< content-type: application/rdap+json`
- `curl -v -H "accept: application/json" https://rdap.example`. 
    - The output should contain:
        - `> accept: application/json`
        - `< content-type: application/rdap+json`

**NOTE:** HTTP headers are not required to be capitalized.

## Referrals to Registars

Both the 2019 and the 2024 gTLD RDAP profiles require gTLD registries to provide link referrals to the corresponding
objects in the registrar's RDAP service. The 2019 profile is silent on how a registry obtains a registrar's RDAP URL.
However, the [2024 profile](https://itp.cdn.icann.org/en/files/registry-operators/rdap-technical-implementation-guide-21feb24-en.pdf) 
clarifies that the [IANA Registrar IDs registry](https://www.iana.org/assignments/registrar-ids/registrar-ids.xhtml) is
to be used to obtain a registrar's RDAP base URL.

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

## Notice of EPP Status Codes

Section 2.6.3 of the 2024 ICANN [RDAP Response Profile](https://www.icann.org/en/system/files/files/rdap-response-profile-21feb24-en.pdf)
requires servers provide an RDAP notice with a link to the EPP Status Codes:

> 2.6.3. A domain name RDAP response MUST contain a notices member with a
> title “Status Codes”, a description containing the string “For more
> information on domain status codes, please visit https://icann.org/epp”
> and a links member with the https://icann.org/epp URL in the href,
> rel:glossary, and a value with the RDAP lookup path that generated the
> RDAP response.

**NOTE:** This is section 2.6.3 of the 2019 [Profile](https://www.icann.org/en/system/files/files/rdap-response-profile-15feb19-en.pdf).

Here is an example of the notice, which is one of the JSON objects inside the "notices" array that MUST only appear at the top-most JSON of the RDAP response:

```json
{
  "title": "Status Codes",
  "description": [
    "“For more information on domain status codes, please visit https://icann.org/epp"
  ],
  "links": [
    {
      "value": "https://some-value.example",
      "rel": "glossary",
      "href": "https://icann.org/epp",
      "type": "text/html"
    }
  ]
}
```

The "type" attribute is not required to be set, but if set it should match the media type of the value
used in "href", which is "text/html" for "https://icann.org/epp". Setting this attribute is good practice.

### The "value" attribute

The "value" attribute must be the request URL of the RDAP query. For example, if the RDAP query used
"https://rdap.example/domain/foo.example" to query for a domain where this notice is found, then the
"value" attribute must be "https://rdap.example/domain/foo.example".

### The "rel" attribute

The value of the "rel" attribute must be "glossary". Values of "alternate" or "about" will trigger a compliance issue.

### Using www.icann.org

While using the URL of "https://www.icann.org/epp" in either the "href" attribute or the link description
will lead user to the right page, the conformance tool strictly checks for the usage of "https://icann.org/epp"
(notice the lack of "www.").

## Notice of RDDS Inaccuracy Report

Section 2.10 of the 2024 ICANN [RDAP Response Profile](https://www.icann.org/en/system/files/files/rdap-response-profile-21feb24-en.pdf)
requires servers provide an RDAP notice with a link the ICANN RDDS Inaccuracy Form:

> 2.10. RDDS Inaccuracy - A domain name RDAP response MUST contain a notices
> member with a title “RDDS Inaccuracy Complaint Form”, a description containing
> the string “URL of the ICANN RDDS Inaccuracy Complaint Form:
> https://icann.org/wicf” and a links member with the https://icann.org/wicf URL in
> the href, rel:help, and a value with the RDAP lookup path that generated the
> RDAP response.

**NOTE:** This is section 2.11 of the 2019 [Profile](https://www.icann.org/en/system/files/files/rdap-response-profile-15feb19-en.pdf).

Here is an example of the notice, which is one of the JSON objects inside the "notices" array that MUST only appear at the top-most JSON of the RDAP response:

```json
{
  "title": "RDDS Inaccuracy Complaint Form",
  "description": [
    "URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf"
  ],
  "links": [
    {
      "value": "https://some-value.example",
      "rel": "help",
      "href": "https://icann.org/wicf",
      "type": "text/html"
    }
  ]
}
```

The "type" attribute is not required to be set, but if set it should match the media type of the value
used in "href", which is "text/html" for "https://icann.org/wicf". Setting this attribute is good practice.

### The "value" attribute

The "value" attribute must be the request URL of the RDAP query. For example, if the RDAP query used
"https://rdap.example/domain/foo.example" to query for a domain where this notice is found, then the
"value" attribute must be "https://rdap.example/domain/foo.example".

### The "rel" attribute

The value of the "rel" attribute must be "help". Values of "alternate" or "about" will trigger a compliance issue.

### Using www.icann.org

While using the URL of "https://www.icann.org/wicf" in either the "href" attribute or the link description
will lead user to the right page, the conformance tool strictly checks for the usage of "https://icann.org/wicf"
(notice the lack of "www.").

## TLS Handshake Issues

TLS handshake issues can be caused by TLS certificates signed with out-of-date signing algorithms. This can be
an issue with the TLS certificate issued for the RDAP server or any certificate in the chain, including the root
certificate used by the issuing Certificate Authority.

The `openssl` command can be used to determine if this is the issue.

```
openssl s_client -connect example.com:443 -showcerts < /dev/null 2>/dev/null | grep sigalg  
```

Might produce the following:

```
   a:PKEY: RSA, 2048 (bit); sigalg: sha256WithRSAEncryption
   a:PKEY: RSA, 2048 (bit); sigalg: sha384WithRSAEncryption
   a:PKEY: RSA, 4096 (bit); sigalg: sha384WithRSAEncryption
   a:PKEY: RSA, 2048 (bit); sigalg: sha1WithRSAEncryption
```

Here, the very last certificate has the signing algorithm has "sha1WithRSAEncryption".
As SHA1 is deprected by [RFC 9155](https://datatracker.ietf.org/doc/rfc9155/), the TLS handshake
will because SHA1 is considered insecure.

Another tool to view TLS certificates used in HTTPS is <https://www.ssllabs.com/ssltest/index.html>.
