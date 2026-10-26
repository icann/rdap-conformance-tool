# General Tests

## RP 2024 Section 1.2

Test group: [[rdapResponseProfile2024_1_2_Validation]](#id-rdapResponseProfile2024_1_2_Validation){ #id-rdapResponseProfile2024_1_2_Validation }

1. Test case [-62000](#id-testCase-62000){ #id-testCase-62000 }: Validate that the JSON string value “icann_rdap_response_profile_1” is included in the 
RDAP Conformance data structure.
```json
{
  "code": -62000,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include icann_rdap_response_profile_1."
}
```
2. Test case [-62001](#id-testCase-62001){ #id-testCase-62001 }: Validate that the JSON string value “redacted” is in the RDAP Conformance data 
structure if the redacted data structure is in the topmost of the response as specified by RFC 9537.
```json
{
  "code": -62001,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include redacted but RFC 9537 is being used."
}
```
3. Test case [-62002](#id-testCase-62002){ #id-testCase-62002 }: If the response has a “redacted” JSON member in the topmost object, 
validate that it is an array of JSON objects:
```json
{
  "code": -62002,
  "value": "<redacted data structure>",
  "message": "The 'redacted' JSON member must be an array of objects."
}
```
4. Test case [-62003](#id-testCase-62003){ #id-testCase-62003 }: If a response has a “redacted” member, for every object in that array 
verify that it has a “name” member, and verify that the “name” member is a JSON object containing either a JSON string of “type” or “description” but not both.
```json
{
  "code": -62003,
  "value": "<redacted object>",
  "message": "The 'name' must be an object with either the strings 'type' or 'description'"
}
```
5. Test case [-62004](#id-testCase-62004){ #id-testCase-62004 }: If a response has a “redacted” member, for every object in that array 
verify that if the members “postPath”, “pathLang”, “prePath”, “replacementPath”, or “method” are present that they are strings.
```json
{
  "code": -62004,
  "value": "<redacted object>",
  "message": "The members 'postPath', 'pathLang', 'prePath', 'replacementPath', and 'method' must be strings."
}
```
6. Test case [-62005](#id-testCase-62005){ #id-testCase-62005 }: If a response has a “redacted” member, for every object in that array 
verify if a “reason” member is present that it is an object that contains only the optional strings “lang”, “type”, and “description”
```json
{
  "code": -62005,
  "value": "<redacted object>",
  "message": "The 'reason' member is an object and may only contain the optional string 'lang', 'type', and 'description'."
}
```

## RP 2024 Section 1.2

Test group: [[rdapResponseProfile2024_1_4_Validation]](#id-rdapResponseProfile2024_1_4_Validation){ #id-rdapResponseProfile2024_1_4_Validation }

1. Test case [-62100](#id-testCase-62100){ #id-testCase-62100 }: Validate that the country name property of the adr structure in every 
jCard in every entity is present and is an empty string (““).
```json
{
  "code": -62100,
  "value": "<jCard data structure>",
  "message": "All country names MUST be an empty string."
}
```
2. Test case [-62101](#id-testCase-62101){ #id-testCase-62101 }: Validate that the “cc” parameter is on every jCard with an “adr” property in every 
entity, and the value is a ISO 3166-1 alpha-2 code.
```json
{
  "code": -62101,
  "value": "<jCard data structure>",
  "message": "All jCards MUST have an ISO 3166-1 Alpha 2 cc parameter",
}
```
