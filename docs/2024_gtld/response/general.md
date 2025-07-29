# General Tests

## RP 2024 Section 1.2

Test group: [[rdapResponseProfile2024_1_2_Validation]](#id-rdapResponseProfile2024_1_2_Validation){ #id-rdapResponseProfile2024_1_2_Validation }

1. Test case [-62000](#id-testCase-62000){ #id-testCase-62000 }: Validate that the JSON string value “icann_rdap_response_profile_1” is included in the RDAP Conformance data structure.
```json
{
  "code": -62000,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include icann_rdap_response_profile_1."
}
```
2. Test case [-62001](#id-testCase-62001){ #id-testCase-62001 }: Validate that the JSON string value “redacted” is in the RDAP Conformance data structure if the redacted data structure is in the topmost of the response as specified by RFC 9537.
```json
{
  "code": -62001,
  "value": "<rdapConformance data structure>",
  "message": "The RDAP Conformance data structure does not include redacted but RFC 9537 is being used."
}
```