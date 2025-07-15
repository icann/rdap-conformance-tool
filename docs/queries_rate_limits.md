# Queries & Rate Limits

## HTTP Methods and Transports

RDAPCT issues queries based on the URL it is given. Each URL may result in numerous queries:

* Once over IPv4 (if available) using an accept header containing the media type “application/json” using the GET method.
* Once over IPv4 (if available) using an accept header containing the media type “application/rdap+json” using the GET method.
* Once over IPv6 (if available) using an accept header containing the media type “application/json” using the GET method.
* Once over IPv6 (if available) using an accept header containing the media type “application/rdap+json” using the GET method.
* Once over IPv4 (if available) using an accept header containing the media type “application/json” using the HEAD method if either of the ICANN gTLD RDAP Profiles is specified.
* Once over IPv4 (if available) using an accept header containing the media type “application/rdap+json” using the HEAD method if either of the ICANN gTLD RDAP Profiles is specified.
* Once over IPv6 (if available) using an accept header containing the media type “application/json” using the HEAD method if either of the ICANN gTLD RDAP Profiles is specified.
* Once over IPv6 (if available) using an accept header containing the media type “application/rdap+json” using the HEAD method if either of the ICANN gTLD RDAP Profiles is specified.

Queries using the HEAD method will return no content and therefore tests comparing content will not be applicable.

Use of the HEAD method is determined when an ICANN gTLD profile is specified using either the `--use-rdap-profile-february-2019`
option or the `--use-rdap-profile-february-2024` option (see [Registry Testing](../overview/#testing-as-a-gtld-registry)
and [Registrar Testing](../overview/#testing-as-a-gtld-registrar)).

Usage of IPv4 and IPv6 can be suppressed with the `--no-ipv4-queries` and `--no-ipv6-queries`
(see [Suppressing Queries](../overview/#supressing-ipv4-or-ipv6-queries)).

## Case Folding Queries

URLs for ASCII domains will automatically result in a "case-folded" equivalent query,
and the results of each query will be compared for equivalence. For example, if the
given URL is `https://reg.example/domain/example.com` then a case-folded query of
`https://reg.example/domain/eXaMpLe.CoM` will sent as well.

## Additional Queries

The `--additional-conformance-queries` option will also send queries for `/help` and
`/domain/not-a-domain.invalid`. See [Additional Queries](../overview/#additional-queries).

## Rate Limiting

RDAPCT honors the HTTP status code 429, as specified by [RFC 7480](https://datatracker.ietf.org/doc/html/rfc7480#section-5.5).

If a 429 response contains a `retry-after` header (see [RFC 9110](https://datatracker.ietf.org/doc/html/rfc9110#name-retry-after)),
RDAPCT will requery once after the number of seconds specified by the value given in the `retry-after` header up to 120 seconds.

If a 429 response does not contain a `retry-after` header, RDAPCT will requery once after 30 seconds.