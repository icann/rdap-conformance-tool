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
