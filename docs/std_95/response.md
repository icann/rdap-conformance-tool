# Response Tests

Test group: [[stdResponseValidation]](#id-stdResponseValidation){ #id-stdResponseValidation } 

1. Validate that the content-type HTTP header has the media type “application/rdap+json”. This validation should allow for media type parameters such as “charset”, etc…
```json
{
  "code": -13000,
  "value": "<content-type header>",
  "message": "The content-type header does not contain the application/rdap+json media type."
}
```
2. Validate that the response is parsable JSON.
```json
{
  "code": -13001,
  "value": "response body not given",
  "message": "The response was not valid JSON."
}
```
3. Validate that the HTTP status code is either 200 or 404.
```json
{
  "code": -13002,
  "value": "<HTTP status code>",
  "message": "The HTTP status code was not 200 nor 404."
}
```
4. If the query is a lookup (/domain, /ip, /nameserver, /autnum, /entity), validate that it has an objectClassName string member.
```json
{
  "code": -13003,
  "value": "<response>",
  "message": "The response does not have an objectClassName string."
}
```
5. Validate that a query for “example.invalid” with random query parameters does not result in an HTTP 3xx response in which the URI in the “location” header contains the same query parameters used in the query.
```json
{
  "code": -13004,
  "value": "<location header value>",
  "message": "Response redirect contained query parameters copied from the request."
}
```
6. Validate that a query for “test.invalid” does not result in an HTTP redirect (that is, if the server answers with a 3xx validate that the redirect does not point back to the same server).
```json
{
  "code": -13005,
  "value": "<location header value>",
  "message": "Server responded with a redirect to itself for domain 'test.invalid'."
}
```
7. Validate that a query for “test.invalid” does not result in an HTTP 200 Ok.
```json
{
  "code": -13006,
  "value": "<response body>",
  "message": "Server responded with a 200 Ok for 'test.invalid'."
}
```
8. If a query fails to connect (CONNECTION FAILED), issue the following error:
```json
{
  "code": -13007,
  "value": "no response available",
  "message": "Failed to connect to server."
}
```
9. If a query fails because of a TLS handshake (HANDSHAKE_FAILED), issue the following error:
```json
{
  "code": -13008,
  "value": "no response available",
  "message": "TLS handshake failed."
}
```
10. If a query fails to connect because of an invalid TLS certificate (INVALID_CERTIFICATE), issue the following error:
```json
{
  "code": -13009,
  "value": "no response available",
  "message": "Invalid TLS certificate."
}
```
11. If a query fails to connect because of revoked TLS certificate (REVOKED_CERTIFICATE), issue the following error:
```json
{
  "code": -13010,
  "value": "no response available",
  "message": "Revoked TLS certificate."
}
```
12. If a query fails to connect because of an expired TLS certificate (EXPIRED_CERTIFICATE), issue the following error:
```json
{
  "code": -13011,
  "value": "no response available",
  "message": "Expired certificate."
}
```
13. If a query fails to connect because of TLS certificate errors, issue the following error:
```json
{
  "code": -13012,
  "value": "no response available",
  "message": "TLS certificate error."
}
```
14. If a query fails to connect because of too many HTTP redirects (TOO_MANY_REDIRECTS), issue the following error:
```json
{
  "code": -13013,
  "value": "no response available",
  "message": "Too many HTTP redirects."
}
```
15. If a query fails because of an HTTP error (HTTP_ERROR), issue the following error:
```json
{
  "code": -13014,
  "value": "no response available",
  "message": "HTTP error."
}
```
16. If a query fails because of an HTTP2 error (HTTP2_ERROR), issue the following error:
```json
{
  "code": -13015,
  "value": "no response available",
  "message": "HTTP2 error."
}
```
17. If a query fails because data failed to be sent on the network (NETWORK_SEND_FAIL), issue the following error:
```json
{
  "code": -13016,
  "value": "no response available",
  "message": "Network send fail."
}
```
18. If a query fails because data failed to be received on the network (NETWORK_RECEIVE_ERROR), issue the following error:
```json
{
  "code": -13017,
  "value": "no response available",
  "message": "Network receive fail."
}
```
19. Validate that all queries issued over all IP protocols for all HTTP methods except queries for domains under .invalid (see codes -130004, -13005, -130006, and -65300) have the same HTTP status code.
```json
{
  "code": -13018,
  "value": "<array of tuples composed of validation codes and status code>",
  "message": "Queries do not produce the same HTTP status code."
}
```
20. If a query fails because the DNS resolution process does not yield an IP address, issue the following error.
```json
{
  "code": -13019,
  "value": "<host name>",
  "message": "Unable to resolve an IP address endpoint using DNS."
}
```
21. If the input URL (that is, the URL given by the user for executing this tool) returns an HTTP 404 status code for both a GET request and a HEAD request, if applicable because a gTLD profile has been selected, and no other validations are given in the error array, then this validation should appear only in the WARNING array.
```json
{
  "code": -13020,
  "value": "<URL>",
  "message": "This URL returned an HTTP 404 status code that was validly formed. If the provided URL does not reference a registered resource, then this warning may be ignored. If the provided URL does reference a registered resource, then this should be considered an error."
}
```
22. If the input URL results in a refused connection, then issue the following error:
```json
{
  "code": -13021,
  "value": "<URL>",
  "message": "Connection refused by host."
}
```

