# General Tests

## TIG Section 1.3

Test group: [[tigSection_1_3_Validation]][id-tigSection_1_3_Validation]

1. Test case [-61000](#id-testCase-61000){ #id-testCase-61000 }: Validate that the JSON string value "icann_rdap_technical_implementation_guide_1" is included in the RDAP Conformance data structure.
```json
{
  "code": -61000,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include icann_rdap_technical_implementation_guide_1."
}
```

## TIG 2024 Section 1.5

Test group: [[tigSection2024_1_5_Validation]](#id-tigSection2024_1_5_Validation){ #id-tigSection2024_1_5_Validation }

1. Test case [-61100](#id-testCase-61100){ #id-testCase-61100 }: If the scheme of the URI to be tested is "https", verify that only TLS 1.2 or TLS 1.3 are offered by the RDAP server.
```json
{
  "code": -61100,
  "value": "<URI>",
  "message": "The RDAP server must only use TLS 1.2 or TLS 1.3."
}
```
2. Test case [-61101](#id-testCase-61101){ #id-testCase-61101 }: If the server is using TLS 1.2, verify that the cipher suite being used is one of: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384
```json
{
  "code": -61101,
  "value": "<URI>",
  "message": "The RDAP server must one of the following cipher suites when using TLS 1.2: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384."
}
```

## TIG 2024 Section 3.2

Test group: [[tigSection2024_3_2_Validation]](#id-tigSection2024_3_2_Validation){ #id-tigSection2024_3_2_Validation }

1. Test case [-23201](#id-testCase-23201){ #id-testCase-23201 }: Validate that the links object found with the test of code -23200 has a value property and that the property contents match the URI used to query the server.
```json
{
  "code": -23201,
  "value": "<links data structure>",
  "message": "a value property must be specified and it must match the URI of the query."
}
```

## TIG 2024 Section 3.3

Test group: [[tigSection2024_3_3_Validation]](#id-tigSection2024_3_3_Validation){ #id-tigSection2024_3_3_Validation }

1. Test case [-61200](#id-testCase-61200){ #id-testCase-61200 }: Validate that at least one notices object is in the topmost object containing a links object with a rel property of “terms-of-service:
```json
{
  "code": -61200,
  "value": "<RDAP response>",
  "message": "The response must have one notice to the terms of service."
}
```
2. Test case [-61201](#id-testCase-61201){ #id-testCase-61201 }: Validate that the links object above has an href property with an https or http URL.
```json
{
  "code": -61201,
  "value": "<link>",
  "message": "This link must have an href."
}
```
3. Test case [-61202](#id-testCase-61202){ #id-testCase-61202 }: Validate that the links object above has a value property that is the URI used to query the RDAP server for this specific response.
```json
{
  "code": -61202,
  "value": "<link>",
  "message": "This link must have a value that is the same as the queried URI."
}
```

## TIG 2024 Section 3.4

Test group: [[tigSection2024_3_4_Validation]](#id-tigSection2024_3_4_Validation){ #id-tigSection2024_3_4_Validation }

1. Test case [-20701](#id-testCase-20701){ #id-testCase-20701 }: Validate that the server responds to a /help query with an HTTP 200 OK and a valid RDAP JSON Response to /help.
```json
{
  "code": -20701,
  "value": "<response>",
  "message": "Response to a /help query did not yield a proper status code or RDAP response."
}
```